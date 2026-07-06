package com.unicomai.wanwu.service.bff.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.unicomai.wanwu.api.agent.AgentService;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.mcp.McpService;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuCallbackApiControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuCallbackApiController())
            .build();

    @Test
    public void modelInfoCallbackReturnsRedactedModelConfigAndCallbackEndpoint() throws Exception {
        ModelService modelService = mock(ModelService.class);
        ModelInfo model = new ModelInfo();
        model.setModelId("model-123");
        model.setModel("deepseek-chat");
        model.setProvider("DeepSeek");
        model.setModelType("llm");
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("apiKey", "real-key");
        config.put("apiSecret", "real-secret");
        config.put("appKey", "real-app-key");
        config.put("accessKey", "real-access-key");
        config.put("endpointUrl", "https://api.deepseek.com/v1");
        config.put("region", "cn");
        model.setConfig(config);
        when(modelService.getModel("", "", "model-123")).thenReturn(model);

        MockMvc callbackMvc = MockMvcBuilders
                .standaloneSetup(new WanwuCallbackApiController(modelService))
                .build();

        callbackMvc.perform(get("/callback/v1/model/model-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.modelId").value("model-123"))
                .andExpect(jsonPath("$.data.model").value("deepseek-chat"))
                .andExpect(jsonPath("$.data.provider").value("DeepSeek"))
                .andExpect(jsonPath("$.data.config.apiKey").value("useless-api-key"))
                .andExpect(jsonPath("$.data.config.apiSecret").value("useless-api-key"))
                .andExpect(jsonPath("$.data.config.appKey").value("useless-api-key"))
                .andExpect(jsonPath("$.data.config.accessKey").value("useless-api-key"))
                .andExpect(jsonPath("$.data.config.endpointUrl").value("http://bff:8080/callback/v1/model/model-123"))
                .andExpect(jsonPath("$.data.config.region").value("cn"));
    }

    @Test
    public void chatCompletionsProxyToOpenAiCompatibleEndpointWhenModelConfigExists() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"upstream-chat-001\",\"object\":\"chat.completion\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                    + "\"content\":\"upstream answer\"},\"finish_reason\":\"stop\"}],"
                    + "\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":3,\"total_tokens\":5}}");
        });
        server.start();
        try {
            ModelService modelService = mock(ModelService.class);
            ModelInfo model = new ModelInfo();
            model.setModelId("model-123");
            model.setModel("deepseek-chat");
            model.setProvider("openai-compatible");
            model.setModelType("llm");
            model.setIsActive(true);
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            config.put("apiKey", "local-key");
            model.setConfig(config);
            when(modelService.getModel("", "", "model-123")).thenReturn(model);

            MockMvc callbackMvc = MockMvcBuilders
                    .standaloneSetup(new WanwuCallbackApiController(modelService))
                    .build();

            callbackMvc.perform(post("/callback/v1/model/model-123/chat/completions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"model\":\"deepseek-chat\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("upstream-chat-001"))
                    .andExpect(jsonPath("$.choices[0].message.content").value("upstream answer"))
                    .andExpect(jsonPath("$.usage.total_tokens").value(5));

            assertEquals("Bearer local-key", authorization.get());
            assertTrue(upstreamBody.get().contains("\"model\":\"deepseek-chat\""));
            assertTrue(upstreamBody.get().contains("\"content\":\"hi\""));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void chatCompletionsRecordsModelUsageForNonStreamProxy() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> respondJson(exchange,
                "{\"id\":\"upstream-chat-usage\",\"object\":\"chat.completion\","
                        + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                        + "\"content\":\"usage answer\"},\"finish_reason\":\"stop\"}],"
                        + "\"usage\":{\"prompt_tokens\":12,\"completion_tokens\":8,\"total_tokens\":20}}"));
        server.start();
        try {
            ModelService modelService = mock(ModelService.class);
            AppService appService = mock(AppService.class);
            when(modelService.getModel("", "", "model-123"))
                    .thenReturn(configuredModel("model-123", "deepseek-chat",
                            "http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "local-key"));

            MockMvc callbackMvc = MockMvcBuilders
                    .standaloneSetup(new WanwuCallbackApiController(modelService, appService))
                    .build();

            callbackMvc.perform(post("/callback/v1/model/model-123/chat/completions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"model\":\"deepseek-chat\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.choices[0].message.content").value("usage answer"));

            ArgumentCaptor<RecordModelStatisticCommand> captor = forClass(RecordModelStatisticCommand.class);
            verify(appService).recordModelStatistic(captor.capture());
            assertEquals("model-123", captor.getValue().getModelId());
            assertEquals("deepseek-chat", captor.getValue().getModel());
            assertEquals(12L, captor.getValue().getPromptTokens());
            assertEquals(8L, captor.getValue().getCompletionTokens());
            assertEquals(20L, captor.getValue().getTotalTokens());
            assertTrue(!captor.getValue().isStream());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void chatCompletionsStreamsConfiguredUpstreamWhenRequested() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            upstreamBody.set(readBody(exchange));
            respondSse(exchange, "data: {\"id\":\"chunk-1\",\"choices\":[{\"delta\":{\"content\":\"hel\"}}]}\n\n"
                    + "data: {\"id\":\"chunk-2\",\"choices\":[{\"delta\":{\"content\":\"lo\"},\"finish_reason\":\"stop\"}],"
                    + "\"usage\":{\"prompt_tokens\":4,\"completion_tokens\":2,\"total_tokens\":6}}\n\n"
                    + "data: [DONE]\n\n");
        });
        server.start();
        try {
            ModelService modelService = mock(ModelService.class);
            AppService appService = mock(AppService.class);
            when(modelService.getModel("", "", "model-123"))
                    .thenReturn(configuredModel("model-123", "deepseek-chat",
                            "http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "local-key"));

            MockMvc callbackMvc = MockMvcBuilders
                    .standaloneSetup(new WanwuCallbackApiController(modelService, appService))
                    .build();

            callbackMvc.perform(post("/callback/v1/model/model-123/chat/completions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"model\":\"deepseek-chat\",\"stream\":true,"
                                    + "\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                    .andExpect(content().string(containsString("data: {\"id\":\"chunk-1\"")))
                    .andExpect(content().string(containsString("data: [DONE]")));

            assertEquals("Bearer local-key", authorization.get());
            assertTrue(upstreamBody.get().contains("\"model\":\"deepseek-chat\""));
            assertTrue(upstreamBody.get().contains("\"stream\":true"));
            ArgumentCaptor<RecordModelStatisticCommand> captor = forClass(RecordModelStatisticCommand.class);
            verify(appService).recordModelStatistic(captor.capture());
            assertEquals(6L, captor.getValue().getTotalTokens());
            assertTrue(captor.getValue().isStream());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void chatCompletionsConvertsUserImageUrlToBase64BeforeProxying() throws Exception {
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer imageServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        imageServer.createContext("/image.png", exchange ->
                respondBytes(exchange, "image/png", new byte[]{
                        (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}));
        imageServer.start();
        HttpServer upstreamServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        upstreamServer.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"upstream-chat-vision\",\"object\":\"chat.completion\","
                    + "\"model\":\"deepseek-vl\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                    + "\"content\":\"vision answer\"},\"finish_reason\":\"stop\"}],"
                    + "\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":3,\"total_tokens\":5}}");
        });
        upstreamServer.start();
        try {
            ModelService modelService = mock(ModelService.class);
            when(modelService.getModel("", "", "model-vl"))
                    .thenReturn(configuredModel("model-vl", "deepseek-vl",
                            "http://127.0.0.1:" + upstreamServer.getAddress().getPort() + "/v1", "vision-key"));

            MockMvc callbackMvc = MockMvcBuilders
                    .standaloneSetup(new WanwuCallbackApiController(modelService))
                    .build();
            String imageUrl = "http://127.0.0.1:" + imageServer.getAddress().getPort() + "/image.png";

            callbackMvc.perform(post("/callback/v1/model/model-vl/chat/completions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"model\":\"deepseek-vl\",\"messages\":[{\"role\":\"user\",\"content\":["
                                    + "{\"type\":\"text\",\"text\":\"describe\"},"
                                    + "{\"type\":\"image_url\",\"image_url\":{\"url\":\"" + imageUrl + "\"}}]}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.choices[0].message.content").value("vision answer"));

            assertTrue(upstreamBody.get().contains("\"url\":\"data:image/png;base64,iVBORw0KGgo=\""));
            assertTrue(!upstreamBody.get().contains(imageUrl));
        } finally {
            upstreamServer.stop(0);
            imageServer.stop(0);
        }
    }

    @Test
    public void chatCompletionsRejectsInactiveCallbackModelBeforeProxying() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"should-not-proxy\"}");
        });
        server.start();
        try {
            ModelService modelService = mock(ModelService.class);
            ModelInfo model = configuredModel("model-123", "deepseek-chat",
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "local-key");
            model.setIsActive(false);
            when(modelService.getModel("", "", "model-123")).thenReturn(model);

            MockMvc callbackMvc = MockMvcBuilders
                    .standaloneSetup(new WanwuCallbackApiController(modelService))
                    .build();

            callbackMvc.perform(post("/callback/v1/model/model-123/chat/completions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"model\":\"deepseek-chat\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(1001))
                    .andExpect(jsonPath("$.msg", containsString("inactive")));

            assertTrue(upstreamBody.get() == null);
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void chatCompletionsRejectsCallbackModelMismatchBeforeProxying() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"should-not-proxy\"}");
        });
        server.start();
        try {
            ModelService modelService = mock(ModelService.class);
            when(modelService.getModel("", "", "model-123"))
                    .thenReturn(configuredModel("model-123", "deepseek-chat",
                            "http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "local-key"));

            MockMvc callbackMvc = MockMvcBuilders
                    .standaloneSetup(new WanwuCallbackApiController(modelService))
                    .build();

            callbackMvc.perform(post("/callback/v1/model/model-123/chat/completions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"model\":\"other-model\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(1001))
                    .andExpect(jsonPath("$.msg", containsString("model mismatch")));

            assertTrue(upstreamBody.get() == null);
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void embeddingsAndRerankProxyToConfiguredModelEndpoints() throws Exception {
        AtomicReference<String> embeddingAuthorization = new AtomicReference<>();
        AtomicReference<String> embeddingBody = new AtomicReference<>();
        AtomicReference<String> rerankAuthorization = new AtomicReference<>();
        AtomicReference<String> rerankBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/embeddings", exchange -> {
            embeddingAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            embeddingBody.set(readBody(exchange));
            respondJson(exchange, "{\"object\":\"list\",\"model\":\"text-embedding-3-small\","
                    + "\"data\":[{\"object\":\"embedding\",\"index\":0,\"embedding\":[0.1,0.2]}],"
                    + "\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":0,\"total_tokens\":1}}");
        });
        server.createContext("/v1/rerank", exchange -> {
            rerankAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            rerankBody.set(readBody(exchange));
            respondJson(exchange, "{\"model\":\"jina-reranker-v2-base-multilingual\","
                    + "\"results\":[{\"index\":0,\"relevance_score\":0.87}],"
                    + "\"usage\":{\"prompt_tokens\":4,\"completion_tokens\":0,\"total_tokens\":4}}");
        });
        server.start();
        try {
            ModelService modelService = mock(ModelService.class);
            AppService appService = mock(AppService.class);
            when(modelService.getModel("", "", "model-emb"))
                    .thenReturn(configuredModel("model-emb", "text-embedding-3-small",
                            "http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "emb-key"));
            when(modelService.getModel("", "", "model-rerank"))
                    .thenReturn(configuredModel("model-rerank", "jina-reranker-v2-base-multilingual",
                            "http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "rerank-key"));

            MockMvc callbackMvc = MockMvcBuilders
                    .standaloneSetup(new WanwuCallbackApiController(modelService, appService))
                    .build();

            callbackMvc.perform(post("/callback/v1/model/model-emb/embeddings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"input\":\"hello\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.model").value("text-embedding-3-small"))
                    .andExpect(jsonPath("$.data[0].embedding[0]").value(0.1))
                    .andExpect(jsonPath("$.usage.total_tokens").value(1));

            callbackMvc.perform(post("/callback/v1/model/model-rerank/rerank")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"query\":\"hello\",\"documents\":[\"hello world\"]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.model").value("jina-reranker-v2-base-multilingual"))
                    .andExpect(jsonPath("$.results[0].relevance_score").value(0.87))
                    .andExpect(jsonPath("$.usage.total_tokens").value(4));

            assertEquals("Bearer emb-key", embeddingAuthorization.get());
            assertTrue(embeddingBody.get().contains("\"model\":\"text-embedding-3-small\""));
            assertTrue(embeddingBody.get().contains("\"input\":\"hello\""));
            assertEquals("Bearer rerank-key", rerankAuthorization.get());
            assertTrue(rerankBody.get().contains("\"model\":\"jina-reranker-v2-base-multilingual\""));
            assertTrue(rerankBody.get().contains("\"documents\":[\"hello world\"]"));

            ArgumentCaptor<RecordModelStatisticCommand> captor = forClass(RecordModelStatisticCommand.class);
            verify(appService, times(2)).recordModelStatistic(captor.capture());
            assertEquals("model-emb", captor.getAllValues().get(0).getModelId());
            assertEquals(1L, captor.getAllValues().get(0).getTotalTokens());
            assertEquals("model-rerank", captor.getAllValues().get(1).getModelId());
            assertEquals(4L, captor.getAllValues().get(1).getTotalTokens());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void embeddingsRejectsCallbackModelMismatchBeforeProxying() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        server.createContext("/v1/embeddings", exchange -> {
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"should-not-proxy\"}");
        });
        server.start();
        try {
            ModelService modelService = mock(ModelService.class);
            when(modelService.getModel("", "", "model-emb"))
                    .thenReturn(configuredModel("model-emb", "text-embedding-3-small",
                            "http://127.0.0.1:" + server.getAddress().getPort() + "/v1", "emb-key"));

            MockMvc callbackMvc = MockMvcBuilders
                    .standaloneSetup(new WanwuCallbackApiController(modelService))
                    .build();

            callbackMvc.perform(post("/callback/v1/model/model-emb/embeddings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"model\":\"other-embedding\",\"input\":\"hello\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(1001))
                    .andExpect(jsonPath("$.msg", containsString("model mismatch")));

            assertTrue(upstreamBody.get() == null);
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void multimodalEmbeddingsAndRerankConvertFileUrlsBeforeProxying() throws Exception {
        HttpServer fileServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fileServer.createContext("/image.png", exchange ->
                respondBytes(exchange, "image/png", new byte[]{
                        (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}));
        fileServer.createContext("/audio.wav", exchange ->
                respondBytes(exchange, "audio/wav", "RIFF".getBytes(StandardCharsets.UTF_8)));
        fileServer.createContext("/video.mp4", exchange ->
                respondBytes(exchange, "video/mp4", new byte[]{0, 0, 0, 1}));
        fileServer.start();

        AtomicReference<String> embeddingBody = new AtomicReference<>();
        AtomicReference<String> rerankBody = new AtomicReference<>();
        HttpServer upstreamServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        upstreamServer.createContext("/v1/multimodal-embeddings", exchange -> {
            embeddingBody.set(readBody(exchange));
            respondJson(exchange, "{\"object\":\"list\",\"model\":\"multi-emb\","
                    + "\"data\":[{\"object\":\"embedding\",\"index\":0,\"embedding\":[0.3]}],"
                    + "\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":0,\"total_tokens\":1}}");
        });
        upstreamServer.createContext("/v1/multimodal-rerank", exchange -> {
            rerankBody.set(readBody(exchange));
            respondJson(exchange, "{\"model\":\"multi-rerank\","
                    + "\"results\":[{\"index\":0,\"relevance_score\":0.91}],"
                    + "\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":0,\"total_tokens\":2}}");
        });
        upstreamServer.start();
        try {
            String baseUrl = "http://127.0.0.1:" + fileServer.getAddress().getPort();
            String imageUrl = baseUrl + "/image.png";
            String audioUrl = baseUrl + "/audio.wav";
            String videoUrl = baseUrl + "/video.mp4";

            ModelService modelService = mock(ModelService.class);
            when(modelService.getModel("", "", "model-multi-emb"))
                    .thenReturn(configuredModel("model-multi-emb", "multi-emb",
                            "http://127.0.0.1:" + upstreamServer.getAddress().getPort() + "/v1", "emb-key"));
            when(modelService.getModel("", "", "model-multi-rerank"))
                    .thenReturn(configuredModel("model-multi-rerank", "multi-rerank",
                            "http://127.0.0.1:" + upstreamServer.getAddress().getPort() + "/v1", "rerank-key"));

            MockMvc callbackMvc = MockMvcBuilders
                    .standaloneSetup(new WanwuCallbackApiController(modelService))
                    .build();

            callbackMvc.perform(post("/callback/v1/model/model-multi-emb/multimodal-embeddings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"input\":[{\"text\":\"hello\",\"image\":\"" + imageUrl
                                    + "\",\"audio\":\"" + audioUrl + "\",\"video\":\"" + videoUrl + "\"}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.model").value("multi-emb"));

            callbackMvc.perform(post("/callback/v1/model/model-multi-rerank/multimodal-rerank")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"query\":{\"text\":\"hello\",\"image\":\"" + imageUrl + "\"},"
                                    + "\"documents\":[{\"text\":\"doc\",\"image\":\"" + imageUrl + "\"}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.model").value("multi-rerank"));

            assertTrue(embeddingBody.get().contains("\"image\":\"data:image/png;base64,iVBORw0KGgo=\""));
            assertTrue(embeddingBody.get().contains("\"audio\":\"data:audio/wav;base64,UklGRg==\""));
            assertTrue(embeddingBody.get().contains("\"video\":\"data:video/mp4;base64,AAAAAQ==\""));
            assertTrue(rerankBody.get().contains("\"image\":\"data:image/png;base64,iVBORw0KGgo=\""));
            assertTrue(!embeddingBody.get().contains(imageUrl));
            assertTrue(!embeddingBody.get().contains(audioUrl));
            assertTrue(!embeddingBody.get().contains(videoUrl));
            assertTrue(!rerankBody.get().contains(imageUrl));
        } finally {
            upstreamServer.stop(0);
            fileServer.stop(0);
        }
    }

    @Test
    public void fileAndModelCallbackRoutesKeepGoRouteShapesAvailable() throws Exception {
        mockMvc.perform(post("/callback/v1/file/url/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));

        mockMvc.perform(post("/callback/v1/file/upload/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"hello.txt\",\"base64\":\"aGVsbG8=\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId").exists())
                .andExpect(jsonPath("$.data.file_name").value("hello.txt"));

        mockMvc.perform(get("/callback/v1/model/model-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modelId").value("model-001"))
                .andExpect(jsonPath("$.data.status").value("available"));

        mockMvc.perform(post("/callback/v1/model/model-001/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("model-001"))
                .andExpect(jsonPath("$.choices[0].message.content", containsString("model-001")));

        mockMvc.perform(post("/callback/v1/model/model-001/embeddings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].embedding[0]").value(0.0));
    }

    @Test
    public void fileCallbacksReadAndUploadBytesWithGoCompatibleFields() throws Exception {
        String upload = mockMvc.perform(post("/callback/v1/file/upload/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"file\":\"aGVsbG8=\",\"fileName\":\"hello\",\"fileExt\":\"txt\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.url", containsString("/callback/v1/file/")))
                .andExpect(jsonPath("$.data.uri", containsString("file-upload/file-expire/")))
                .andExpect(jsonPath("$.data.fileName").value("hello.txt"))
                .andReturn().getResponse().getContentAsString();
        String fileId = upload.replaceAll("(?s).*?/callback/v1/file/([^\\\"}]+).*", "$1");

        mockMvc.perform(get("/callback/v1/file/" + fileId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().bytes("hello".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(post("/callback/v1/file/url/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileUrl\":\"/callback/v1/file/" + fileId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("aGVsbG8="));

        mockMvc.perform(post("/callback/v1/file/url/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileUrl\":\"/callback/v1/file/" + fileId
                                + "\",\"addPrefix\":true,\"customPrefix\":\"data:text/plain;base64\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("data:text/plain;base64,aGVsbG8="));

        mockMvc.perform(post("/callback/v1/file/upload/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"file\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg", containsString("file is required")));
    }

    @Test
    public void imageOutlineCallbackReturnsGoCompatibleMarkdownResult() throws Exception {
        String response = mockMvc.perform(post("/callback/v1/image/outline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"image\":\"data:image/png;base64,aW1hZ2U=\",\"response_format\":\"b64_json\","
                                + "\"threshold\":123,\"lineWidth\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("success"))
                .andExpect(jsonPath("$.data.prompt").isString())
                .andExpect(jsonPath("$.data.markdown", containsString("![](/callback/v1/file/")))
                .andExpect(jsonPath("$.data.result[0]", containsString("![](/callback/v1/file/")))
                .andExpect(jsonPath("$.data.mimeType").value("image/png"))
                .andExpect(jsonPath("$.data.url", containsString("/callback/v1/file/")))
                .andExpect(jsonPath("$.data.uri", containsString("callback/image-outline/")))
                .andExpect(jsonPath("$.data.usage.width").value(0))
                .andExpect(jsonPath("$.data.usage.height").value(0))
                .andExpect(jsonPath("$.data.usage.foregroundPixels").value(0))
                .andExpect(jsonPath("$.data.usage.edgePixels").value(0))
                .andExpect(jsonPath("$.data.usage.threshold").value(0))
                .andExpect(jsonPath("$.data.usage.lineWidth").value(0))
                .andExpect(jsonPath("$.data.usage.method").value("qwen_image_edit"))
                .andExpect(jsonPath("$.data.status").doesNotExist())
                .andExpect(jsonPath("$.data.request").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String fileId = response.replaceAll("(?s).*?/callback/v1/file/([^\\)\\\"]+).*", "$1");
        mockMvc.perform(get("/callback/v1/file/" + fileId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));

        mockMvc.perform(post("/callback/v1/image/outline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"image\":\"data:image/png;base64,aW1hZ2U=\",\"response_format\":\"raw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg", containsString("unsupported response_format")));
    }

    @Test
    public void tourismPoiCallbackReturnsGoCompatibleRanking() throws Exception {
        mockMvc.perform(post("/callback/v1/tourism/poi/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"Mogao Caves\",\"category\":\"restaurant\","
                                + "\"radiusMeters\":40000,\"limit\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.query.resolvedLocation").value("Mogao Caves"))
                .andExpect(jsonPath("$.data.query.category").value("restaurant"))
                .andExpect(jsonPath("$.data.query.radiusMeters").value(40000))
                .andExpect(jsonPath("$.data.query.limit").value(3))
                .andExpect(jsonPath("$.data.query.sort").value("rating_desc,distance_asc"))
                .andExpect(jsonPath("$.data.results.length()").value(3))
                .andExpect(jsonPath("$.data.results[0].rank").value(1))
                .andExpect(jsonPath("$.data.results[0].id").value("dh-restaurant-jingyuan-lamb"))
                .andExpect(jsonPath("$.data.results[0].category").value("restaurant"))
                .andExpect(jsonPath("$.data.results[0].rating").value(4.8))
                .andExpect(jsonPath("$.data.results[0].distanceMeters").isNumber())
                .andExpect(jsonPath("$.data.results[0].tags").isArray())
                .andExpect(jsonPath("$.data.list").doesNotExist());

        mockMvc.perform(post("/callback/v1/tourism/poi/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":40.0834,\"longitude\":94.6734,\"category\":\"all\",\"limit\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.query.resolvedLocation").value("Custom Coordinates"))
                .andExpect(jsonPath("$.data.query.limit").value(20))
                .andExpect(jsonPath("$.data.results[0].rank").value(1));

        mockMvc.perform(post("/callback/v1/tourism/poi/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":120,\"longitude\":94.6734}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg", containsString("latitude or longitude out of range")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ragCallbacksAdaptGoRequestsToKnowledgeServiceAndReturnGoAliases() throws Exception {
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        when(knowledgeService.hitKnowledge(eq("u1"), eq(""), anyMap())).thenReturn(map(
                "prompt", "Prompt text",
                "searchList", Collections.singletonList(map(
                        "title", "Guide.txt",
                        "snippet", "Wanwu Java guide",
                        "knowledgeName", "Dev KB",
                        "childContentList", Collections.singletonList(map("childSnippet", "Child text", "score", 0.7)),
                        "childScore", Collections.singletonList(0.7),
                        "contentType", "text",
                        "score", 0.91,
                        "rerankInfo", Collections.singletonList(map("fileUrl", "/files/1", "score", 0.91)),
                        "metaDataList", Collections.singletonList(map("key", "city", "value", "Dunhuang")))),
                "score", Collections.singletonList(0.91),
                "useGraph", true));
        when(knowledgeService.hitKnowledge(eq("wga-user"), eq(""), anyMap())).thenReturn(map(
                "prompt", "",
                "searchList", Collections.emptyList(),
                "score", Collections.emptyList(),
                "useGraph", false));
        when(knowledgeService.hitQaPairs(eq("qa-user"), eq(""), anyMap())).thenReturn(map(
                "searchList", Collections.singletonList(map(
                        "title", "What is Wanwu?",
                        "question", "What is Wanwu?",
                        "answer", "AI platform",
                        "qaBase", "FAQ",
                        "qaId", "qa-1",
                        "contentType", "qa")),
                "score", Collections.singletonList(0.88)));
        MockMvc callbackMvc = MockMvcBuilders
                .standaloneSetup(new WanwuCallbackApiController(null, null, knowledgeService))
                .build();

        callbackMvc.perform(post("/callback/v1/rag/search-knowledge-base")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u1\",\"question\":\"How to use Wanwu?\","
                                + "\"knowledgeIdList\":[\"kb-1\"],"
                                + "\"knowledge_base_info\":{\"u1\":[{\"kb_id\":\"kb-1\",\"kb_name\":\"Dev KB\"}]},"
                                + "\"topK\":3,\"threshold\":0.2,\"use_graph\":true,"
                                + "\"retrieve_method\":\"hybrid_search\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.prompt").value("Prompt text"))
                .andExpect(jsonPath("$.data.use_graph").value(true))
                .andExpect(jsonPath("$.data.useGraph").value(true))
                .andExpect(jsonPath("$.data.searchList[0].title").value("Guide.txt"))
                .andExpect(jsonPath("$.data.searchList[0].kb_name").value("Dev KB"))
                .andExpect(jsonPath("$.data.searchList[0].user_kb_name").value("Dev KB"))
                .andExpect(jsonPath("$.data.searchList[0].child_content_list[0].child_snippet").value("Child text"))
                .andExpect(jsonPath("$.data.searchList[0].childContentList[0].childSnippet").value("Child text"))
                .andExpect(jsonPath("$.data.searchList[0].content_type").value("text"))
                .andExpect(jsonPath("$.data.searchList[0].rerank_info[0].file_url").value("/files/1"))
                .andExpect(jsonPath("$.data.searchList[0].meta_data[0].key").value("city"))
                .andExpect(jsonPath("$.data.request").doesNotExist());

        ArgumentCaptor<Map> knowledgeCaptor = forClass(Map.class);
        verify(knowledgeService).hitKnowledge(eq("u1"), eq(""), knowledgeCaptor.capture());
        Map<String, Object> knowledgeRequest = knowledgeCaptor.getValue();
        assertEquals("How to use Wanwu?", knowledgeRequest.get("question"));
        List<Map<String, Object>> knowledgeList = (List<Map<String, Object>>) knowledgeRequest.get("knowledgeList");
        assertEquals("kb-1", knowledgeList.get(0).get("knowledgeId"));
        Map<String, Object> matchParams = (Map<String, Object>) knowledgeRequest.get("knowledgeMatchParams");
        assertEquals(3, ((Number) matchParams.get("topK")).intValue());
        assertEquals(0.2, ((Number) matchParams.get("threshold")).doubleValue());
        assertEquals(true, matchParams.get("useGraph"));
        assertEquals("hybrid_search", matchParams.get("retrieveMethod"));

        callbackMvc.perform(post("/callback/v1/wga/rag/search-knowledge-base")
                        .header("X-uid", "wga-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeIdList\":[\"kb-wga\"],\"question\":\"Where?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.searchList").isArray());

        ArgumentCaptor<Map> wgaCaptor = forClass(Map.class);
        verify(knowledgeService).hitKnowledge(eq("wga-user"), eq(""), wgaCaptor.capture());
        Map<String, Object> wgaRequest = wgaCaptor.getValue();
        Map<String, Object> wgaMatchParams = (Map<String, Object>) wgaRequest.get("knowledgeMatchParams");
        assertEquals(5, ((Number) wgaMatchParams.get("topK")).intValue());
        assertEquals(0.4, ((Number) wgaMatchParams.get("threshold")).doubleValue());
        assertEquals("hybrid_search", wgaMatchParams.get("retrieveMethod"));
        assertEquals("weighted_score", wgaMatchParams.get("rerankMod"));
        Map<String, Object> weights = (Map<String, Object>) wgaMatchParams.get("weights");
        assertEquals(0.2, ((Number) weights.get("vector_weight")).doubleValue());
        assertEquals(0.8, ((Number) weights.get("text_weight")).doubleValue());

        callbackMvc.perform(post("/callback/v1/rag/search-QA-base")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"qa-user\",\"question\":\"Wanwu?\","
                                + "\"QABaseInfo\":{\"qa-user\":[{\"QAId\":\"qa-1\",\"QABase\":\"FAQ\"}]},"
                                + "\"topK\":1,\"threshold\":0.3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.prompt").value(""))
                .andExpect(jsonPath("$.data.searchList[0].qaBase").value("FAQ"))
                .andExpect(jsonPath("$.data.searchList[0].user_kb_name").value("FAQ"))
                .andExpect(jsonPath("$.data.searchList[0].content_type").value("qa"))
                .andExpect(jsonPath("$.data.score[0]").value(0.88));

        ArgumentCaptor<Map> qaCaptor = forClass(Map.class);
        verify(knowledgeService).hitQaPairs(eq("qa-user"), eq(""), qaCaptor.capture());
        Map<String, Object> qaRequest = qaCaptor.getValue();
        List<Map<String, Object>> qaKnowledgeList = (List<Map<String, Object>>) qaRequest.get("knowledgeList");
        assertEquals("qa-1", qaKnowledgeList.get(0).get("knowledgeId"));
        Map<String, Object> qaMatchParams = (Map<String, Object>) qaRequest.get("knowledgeMatchParams");
        assertEquals(1, ((Number) qaMatchParams.get("topK")).intValue());
        assertEquals(0.3, ((Number) qaMatchParams.get("threshold")).doubleValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ragKnowledgeStreamReturnsSseWithLocalKnowledgeHits() throws Exception {
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        when(knowledgeService.hitKnowledge(eq("stream-user"), eq(""), anyMap())).thenReturn(map(
                "prompt", "Prompt text",
                "searchList", Collections.singletonList(map(
                        "title", "Guide.txt",
                        "snippet", "Wanwu Java guide",
                        "knowledgeName", "Dev KB",
                        "contentType", "text",
                        "score", 0.77)),
                "score", Collections.singletonList(0.77),
                "useGraph", false));
        MockMvc callbackMvc = MockMvcBuilders
                .standaloneSetup(new WanwuCallbackApiController(null, null, knowledgeService))
                .build();

        callbackMvc.perform(post("/callback/v1/rag/knowledge/stream/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"stream-user\",\"question\":\"Explain Wanwu\","
                                + "\"knowledge_base_info\":{\"stream-user\":[{\"kb_id\":\"kb-stream\","
                                + "\"kb_name\":\"Dev KB\"}]},\"topK\":2,\"threshold\":0.1,"
                                + "\"use_graph\":false,\"history\":[{\"query\":\"old\",\"response\":\"answer\","
                                + "\"needHistory\":true}]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"code\":0")))
                .andExpect(content().string(containsString("\"msg_id\"")))
                .andExpect(content().string(containsString("\"finish\":1")))
                .andExpect(content().string(containsString("\"output\":\"Knowledge references ready for: Explain Wanwu\"")))
                .andExpect(content().string(containsString("\"searchList\"")))
                .andExpect(content().string(containsString("\"kb_name\":\"Dev KB\"")))
                .andExpect(content().string(containsString("\"score\":[0.77]")))
                .andExpect(content().string(containsString("\"query\":\"Explain Wanwu\"")));

        ArgumentCaptor<Map> streamCaptor = forClass(Map.class);
        verify(knowledgeService).hitKnowledge(eq("stream-user"), eq(""), streamCaptor.capture());
        Map<String, Object> streamRequest = streamCaptor.getValue();
        List<Map<String, Object>> knowledgeList = (List<Map<String, Object>>) streamRequest.get("knowledgeList");
        assertEquals("kb-stream", knowledgeList.get(0).get("knowledgeId"));
        Map<String, Object> matchParams = (Map<String, Object>) streamRequest.get("knowledgeMatchParams");
        assertEquals(2, ((Number) matchParams.get("topK")).intValue());
        assertEquals(0.1, ((Number) matchParams.get("threshold")).doubleValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void agentCallbackReturnsGoJsonStringFromAgentService() throws Exception {
        AgentService agentService = mock(AgentService.class);
        when(agentService.chatAgent(anyMap())).thenReturn(map(
                "assistantId", "assistant-001",
                "conversationId", "conv-001",
                "response", "Agent answer"));
        MockMvc callbackMvc = MockMvcBuilders
                .standaloneSetup(new WanwuCallbackApiController(null, null, null, null, agentService))
                .build();

        callbackMvc.perform(post("/callback/v1/agent/assistant-001/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\":\"hello\",\"conversationId\":\"conv-001\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("Agent answer"));

        ArgumentCaptor<Map> requestCaptor = forClass(Map.class);
        verify(agentService).chatAgent(requestCaptor.capture());
        Map<String, Object> request = requestCaptor.getValue();
        assertEquals("assistant-001", request.get("assistantId"));
        assertEquals("hello", request.get("input"));
        assertEquals(true, request.get("stream"));
        assertEquals("conv-001", request.get("conversationId"));
    }

    @Test
    public void specializedModelCallbacksReturnGoCompatibleBodies() throws Exception {
        mockMvc.perform(post("/callback/v1/model/model-ocr/ocr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"file:///demo.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data[0].page_num[0]").value(1))
                .andExpect(jsonPath("$.data[0].type").value("text"));

        mockMvc.perform(post("/callback/v1/model/model-pdf/pdf-parser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"file_name\":\"demo.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.content", containsString("model-pdf")));

        mockMvc.perform(post("/callback/v1/model/model-gui/gui")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"task\":\"open settings\",\"platform\":\"WIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.content.operation").value("finish"))
                .andExpect(jsonPath("$.usage.total_tokens").value(0));

        mockMvc.perform(post("/callback/v1/model/model-asr/asr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"qwen3-asr-flash\",\"messages\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.choices[0].message.role").value("assistant"))
                .andExpect(jsonPath("$.choices[0].message.content[0].text", containsString("model-asr")));
    }

    @Test
    public void workflowMcpRagSkillAndSandboxCallbacksReturnFrontendSafeResponses() throws Exception {
        McpService mcpService = mock(McpService.class);
        when(mcpService.getToolSquare("", "", "builtin-weather"))
                .thenReturn(map("toolSquareId", "builtin-weather", "name", "Weather Tool"));
        when(mcpService.getCustomTool("", "", "tool-001"))
                .thenReturn(map("customToolId", "tool-001", "name", "WeatherAPI"));
        when(mcpService.getMcp("", "", "mcp-001"))
                .thenReturn(map("mcpId", "mcp-001", "name", "Search MCP"));
        when(mcpService.getMcpServer("", "", "mcpserver-001"))
                .thenReturn(map("mcpServerId", "mcpserver-001", "name", "Local MCP Server"));
        when(mcpService.getBuiltinSkill("", "", "builtin-summary"))
                .thenReturn(map("skillId", "builtin-summary", "name", "Summary Skill", "desc", "summary",
                        "avatar", map("path", "/imgs/skill.svg"), "downloadUrl", "/skill/builtin-summary.zip"));
        when(mcpService.getCustomSkill("", "", "skill-custom-001"))
                .thenReturn(map("skillId", "skill-custom-001", "name", "Custom Skill", "desc", "custom",
                        "avatar", map("path", "/imgs/custom-skill.svg"), "objectPath", "/upload/custom-skill.zip"));
        MockMvc callbackMvc = MockMvcBuilders
                .standaloneSetup(new WanwuCallbackApiController(null, null, null, mcpService))
                .build();

        mockMvc.perform(get("/callback/v1/workflow/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray());

        callbackMvc.perform(get("/callback/v1/workflow/tool/square").param("toolSquareId", "builtin-weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.toolSquareId").value("builtin-weather"))
                .andExpect(jsonPath("$.data.name").value("Weather Tool"));

        callbackMvc.perform(get("/callback/v1/workflow/tool/custom").param("customToolId", "tool-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customToolId").value("tool-001"))
                .andExpect(jsonPath("$.data.name").value("WeatherAPI"));

        callbackMvc.perform(get("/callback/v1/mcp").param("mcpId", "mcp-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mcpId").value("mcp-001"))
                .andExpect(jsonPath("$.data.name").value("Search MCP"));

        callbackMvc.perform(get("/callback/v1/mcp/server").param("mcpServerId", "mcpserver-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mcpServerId").value("mcpserver-001"))
                .andExpect(jsonPath("$.data.name").value("Local MCP Server"));

        callbackMvc.perform(get("/callback/v1/skill/detail")
                        .param("skillId", "builtin-summary")
                        .param("skillType", "builtin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skillId").value("builtin-summary"))
                .andExpect(jsonPath("$.data.skillType").value("builtin"))
                .andExpect(jsonPath("$.data.avatar").value("/imgs/skill.svg"));

        callbackMvc.perform(post("/callback/v1/skill/builtin/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillIdList\":[\"builtin-summary\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skillList[0].skillId").value("builtin-summary"))
                .andExpect(jsonPath("$.data.skillList[0].objectPath").value("/skill/builtin-summary.zip"));

        callbackMvc.perform(post("/callback/v1/skill/custom/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillIdList\":[\"skill-custom-001\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skillList[0].skillId").value("skill-custom-001"))
                .andExpect(jsonPath("$.data.skillList[0].skillType").value("custom"))
                .andExpect(jsonPath("$.data.skillList[0].objectPath").value("/upload/custom-skill.zip"));

        mockMvc.perform(post("/callback/v1/wga/sandbox/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"threadId\":\"thread-001\",\"runId\":\"run-001\","
                                + "\"model\":{\"modelId\":\"model-llm\",\"model\":\"qwen\"},"
                                + "\"overallTask\":\"Write a report\","
                                + "\"messages\":[{\"role\":\"user\",\"content\":\"hello\"}],"
                                + "\"tools\":[{\"schema\":\"{}\",\"operationIds\":[\"search\"]}],"
                                + "\"skills\":[{\"dir\":\"/skills/summary\",\"variables\":[]}],"
                                + "\"mcps\":[{\"name\":\"weather\",\"url\":\"http://mcp\"}],"
                                + "\"inputDir\":\"/input\",\"outputDir\":\"/output\","
                                + "\"enableThinking\":true,\"skipCleanup\":true,"
                                + "\"agentName\":\"WanwuBot\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"runId\":\"run-001\"")))
                .andExpect(content().string(containsString("\"threadId\":\"thread-001\"")))
                .andExpect(content().string(containsString("\"status\":\"sandbox_completed\"")))
                .andExpect(content().string(containsString("\"modelId\":\"model-llm\"")))
                .andExpect(content().string(containsString("\"toolCount\":1")))
                .andExpect(content().string(containsString("data: [DONE]")));

        mockMvc.perform(post("/callback/v1/wga/sandbox/cleanup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"runId\":\"run-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("sandbox_cleaned"))
                .andExpect(jsonPath("$.data.runId").value("run-001"))
                .andExpect(jsonPath("$.data.cleanedAt").isString());

        mockMvc.perform(post("/callback/v1/agent/assistant-001/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("hello"));

        mockMvc.perform(post("/callback/v1/rag/knowledge/stream/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("success")));

        verify(mcpService).getToolSquare("", "", "builtin-weather");
        verify(mcpService).getCustomTool("", "", "tool-001");
        verify(mcpService).getMcp("", "", "mcp-001");
        verify(mcpService).getMcpServer("", "", "mcpserver-001");
        verify(mcpService, times(2)).getBuiltinSkill("", "", "builtin-summary");
        verify(mcpService).getCustomSkill("", "", "skill-custom-001");
    }

    @Test
    public void v1CallbackAliasesMatchOriginalGoCompatibilityEntrypoints() throws Exception {
        mockMvc.perform(post("/user/api/v1/api/docstatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-001\",\"status\":\"success\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("doc_status_updated"));

        mockMvc.perform(post("/api/knowledge/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"kb-001\",\"status\":\"ready\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("knowledge_status_updated"));

        mockMvc.perform(get("/api/deploy/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.platform").value("wanwu-java"));
    }

    @Test
    public void callbackStatusAliasesProxyToKnowledgeServiceWhenAvailable() throws Exception {
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        MockMvc callbackMvc = MockMvcBuilders
                .standaloneSetup(new WanwuCallbackApiController(null, null, knowledgeService))
                .build();

        callbackMvc.perform(post("/api/docstatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"doc-001\",\"status\":31,"
                                + "\"metaDataList\":[{\"metaId\":\"meta-1\",\"value\":\"parsed\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("doc_status_updated"));

        callbackMvc.perform(post("/api/knowledge/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"kb-001\",\"reportStatus\":130}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("knowledge_status_updated"));

        callbackMvc.perform(get("/api/doc_status_init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("doc_status_initialized"));

        ArgumentCaptor<Map> docStatusCaptor = forClass(Map.class);
        verify(knowledgeService).updateCallbackDocStatus(eq(""), eq(""), docStatusCaptor.capture());
        assertEquals("doc-001", docStatusCaptor.getValue().get("id"));
        assertEquals(31, docStatusCaptor.getValue().get("status"));

        ArgumentCaptor<Map> knowledgeStatusCaptor = forClass(Map.class);
        verify(knowledgeService).updateCallbackKnowledgeStatus(eq(""), eq(""), knowledgeStatusCaptor.capture());
        assertEquals("kb-001", knowledgeStatusCaptor.getValue().get("knowledgeId"));
        assertEquals(130, knowledgeStatusCaptor.getValue().get("reportStatus"));
        verify(knowledgeService).initCallbackDocStatus("", "");
    }

    private static Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }

    private static ModelInfo configuredModel(String modelId, String modelName, String endpointUrl, String apiKey) {
        ModelInfo model = new ModelInfo();
        model.setModelId(modelId);
        model.setModel(modelName);
        model.setProvider("openai-compatible");
        model.setModelType("llm");
        model.setIsActive(true);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("endpointUrl", endpointUrl);
        config.put("apiKey", apiKey);
        model.setConfig(config);
        return model;
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int read;
        while ((read = exchange.getRequestBody().read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void respondJson(HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private static void respondSse(HttpExchange exchange, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private static void respondBytes(HttpExchange exchange, String contentType, byte[] response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
