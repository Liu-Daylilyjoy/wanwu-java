package com.unicomai.wanwu.service.bff.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
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
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("aGVsbG8="));

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
        mockMvc.perform(get("/callback/v1/workflow/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray());

        mockMvc.perform(get("/callback/v1/mcp").param("id", "mcp-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("mcp-001"));

        mockMvc.perform(post("/callback/v1/skill/builtin/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyword\":\"skill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());

        mockMvc.perform(post("/callback/v1/wga/sandbox/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"print(1)\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("sandbox_ok"));

        mockMvc.perform(post("/callback/v1/agent/assistant-001/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("assistant-001")));

        mockMvc.perform(post("/callback/v1/rag/knowledge/stream/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("success")));
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
