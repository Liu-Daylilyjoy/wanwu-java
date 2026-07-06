package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationPageResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantPublishedQuery;
import com.unicomai.wanwu.api.app.dto.AppUrlInfo;
import com.unicomai.wanwu.api.app.dto.AppUrlSuffixQuery;
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuOpenUrlApiControllerTest {

    private final AppService appService = mock(AppService.class);
    private final ModelService modelService = mock(ModelService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuOpenUrlApiController(appService, modelService))
            .build();

    @Test
    public void openUrlInfoReturnsPublishedAssistantAndUrlInfo() throws Exception {
        when(appService.getAppUrlBySuffix(any(AppUrlSuffixQuery.class))).thenReturn(appUrlInfo("assistant-001"));
        Map<String, Object> assistant = new LinkedHashMap<>();
        assistant.put("assistantId", "assistant-001");
        assistant.put("name", "PublishedAgent");
        assistant.put("desc", "published desc");
        when(appService.getPublishedAssistant(any(AssistantPublishedQuery.class))).thenReturn(assistant);

        mockMvc.perform(get("/openurl/v1/agent/suffix-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.assistant.name").value("PublishedAgent"))
                .andExpect(jsonPath("$.data.appUrlInfo.suffix").value("suffix-001"));

        mockMvc.perform(get("/service/url/openurl/v1/agent/suffix-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assistant.assistantId").value("assistant-001"));

        verify(appService, times(2)).getAppUrlBySuffix(any(AppUrlSuffixQuery.class));
        verify(appService, times(2)).getPublishedAssistant(any(AssistantPublishedQuery.class));
    }

    @Test
    public void openUrlConversationAndStreamUseClientIdentityFromHeader() throws Exception {
        when(appService.getAppUrlBySuffix(any(AppUrlSuffixQuery.class))).thenReturn(appUrlInfo("assistant-001"));
        when(appService.createAssistantConversation(any(AssistantConversationCreateCommand.class)))
                .thenReturn(new AssistantConversationCreateResult("conversation-001"));
        AssistantConversationStreamResult streamResult = new AssistantConversationStreamResult();
        streamResult.setAssistantId("assistant-001");
        streamResult.setConversationId("conversation-001");
        streamResult.setDetailId("detail-001");
        streamResult.setResponse("Public answer.");
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class))).thenReturn(streamResult);

        mockMvc.perform(post("/service/url/openurl/v1/agent/suffix-001/conversation")
                        .header("X-Client-ID", "client-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello public\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.conversationId").value("conversation-001"));

        mockMvc.perform(post("/service/url/openurl/v1/agent/suffix-001/stream")
                        .header("X-Client-ID", "client-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationId\":\"conversation-001\",\"prompt\":\"continue public\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("Public answer.")))
                .andExpect(content().string(containsString("\"conversationId\":\"conversation-001\"")));

        org.mockito.ArgumentCaptor<AssistantConversationCreateCommand> createCaptor =
                forClass(AssistantConversationCreateCommand.class);
        verify(appService).createAssistantConversation(createCaptor.capture());
        assertEquals("assistant-001", createCaptor.getValue().getAssistantId());
        assertEquals("hello public", createCaptor.getValue().getPrompt());
        assertEquals("published", createCaptor.getValue().getConversationType());
        assertEquals("client-001", createCaptor.getValue().getUserId());
        assertEquals("default-org", createCaptor.getValue().getOrgId());

        org.mockito.ArgumentCaptor<AssistantConversationStreamCommand> streamCaptor =
                forClass(AssistantConversationStreamCommand.class);
        verify(appService).streamAssistantConversation(streamCaptor.capture());
        assertEquals("assistant-001", streamCaptor.getValue().getAssistantId());
        assertEquals("conversation-001", streamCaptor.getValue().getConversationId());
        assertEquals("continue public", streamCaptor.getValue().getPrompt());
        assertEquals(false, streamCaptor.getValue().isDraft());
        assertEquals("client-001", streamCaptor.getValue().getUserId());

        org.mockito.ArgumentCaptor<RecordAppStatisticCommand> statisticCaptor =
                forClass(RecordAppStatisticCommand.class);
        verify(appService).recordAppStatistic(statisticCaptor.capture());
        assertEquals("assistant-001", statisticCaptor.getValue().getAppId());
        assertEquals("agent", statisticCaptor.getValue().getAppType());
        assertEquals("dev-admin", statisticCaptor.getValue().getUserId());
        assertEquals("default-org", statisticCaptor.getValue().getOrgId());
        assertEquals("webURL", statisticCaptor.getValue().getSource());
        assertEquals(true, statisticCaptor.getValue().isSuccess());
        assertEquals(true, statisticCaptor.getValue().isStream());
        assertTrue(statisticCaptor.getValue().getCallTime() > 0);
    }

    @Test
    public void openUrlStreamUsesConfiguredOpenAiCompatibleModelBeforePersisting() throws Exception {
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange.getRequestBody()));
            respondSse(exchange, "data: {\"id\":\"chatcmpl-openurl\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"openurl \"},"
                    + "\"finish_reason\":null}],\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":1,\"total_tokens\":3}}\n\n"
                    + "data: {\"id\":\"chatcmpl-openurl\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"agent answer\"},"
                    + "\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":3,\"total_tokens\":5}}\n\n"
                    + "data: [DONE]\n\n");
        });
        server.start();
        try {
            when(appService.getAppUrlBySuffix(any(AppUrlSuffixQuery.class))).thenReturn(appUrlInfo("assistant-001"));
            Map<String, Object> modelConfig = new LinkedHashMap<>();
            modelConfig.put("modelId", "model-openurl-001");
            Map<String, Object> assistant = new LinkedHashMap<>();
            assistant.put("assistantId", "assistant-001");
            assistant.put("modelConfig", modelConfig);
            when(appService.getPublishedAssistant(any(AssistantPublishedQuery.class))).thenReturn(assistant);

            ModelInfo model = new ModelInfo();
            model.setModelId("model-openurl-001");
            model.setModel("deepseek-chat");
            model.setProvider("openai-compatible");
            model.setModelType("llm");
            model.setIsActive(true);
            model.getConfig().put("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            model.getConfig().put("apiKey", "local-key");
            when(modelService.getModel(anyString(), anyString(), eq("model-openurl-001"))).thenReturn(model);

            when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class)))
                    .thenAnswer(invocation -> {
                        AssistantConversationStreamCommand command = invocation.getArgument(0);
                        AssistantConversationStreamResult result = new AssistantConversationStreamResult();
                        result.setAssistantId(command.getAssistantId());
                        result.setConversationId("conversation-openurl-model-001");
                        result.setDetailId("detail-openurl-model-001");
                        result.setResponse(command.getOverrideResponse());
                        return result;
                    });

            mockMvc.perform(post("/service/url/openurl/v1/agent/suffix-001/stream")
                            .header("X-Client-ID", "client-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"prompt\":\"hello public model\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                    .andExpect(content().string(containsString("openurl agent answer")))
                    .andExpect(content().string(containsString("\"conversationId\":\"conversation-openurl-model-001\"")));

            assertTrue(upstreamBody.get().contains("\"stream\":true"));
            assertTrue(upstreamBody.get().contains("\"content\":\"hello public model\""));
            org.mockito.ArgumentCaptor<AssistantConversationStreamCommand> streamCaptor =
                    forClass(AssistantConversationStreamCommand.class);
            verify(appService).streamAssistantConversation(streamCaptor.capture());
            assertEquals("openurl agent answer", streamCaptor.getValue().getOverrideResponse());
            assertEquals("client-001", streamCaptor.getValue().getUserId());

            org.mockito.ArgumentCaptor<RecordModelStatisticCommand> modelStatisticCaptor =
                    forClass(RecordModelStatisticCommand.class);
            verify(appService).recordModelStatistic(modelStatisticCaptor.capture());
            assertEquals("model-openurl-001", modelStatisticCaptor.getValue().getModelId());
            assertEquals(5L, modelStatisticCaptor.getValue().getTotalTokens());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void openUrlConversationHistoryRoutesUsePublicIdentity() throws Exception {
        when(appService.getAppUrlBySuffix(any(AppUrlSuffixQuery.class))).thenReturn(appUrlInfo("assistant-001"));
        when(appService.listAssistantConversations(any(AssistantConversationListQuery.class)))
                .thenReturn(new AssistantConversationPageResult(Collections.emptyList(), 0, 1, 1000));
        when(appService.listAssistantConversationDetails(any(AssistantConversationDetailQuery.class)))
                .thenReturn(new AssistantConversationPageResult(Collections.emptyList(), 0, 1, 1000));

        mockMvc.perform(get("/service/url/openurl/v1/agent/suffix-001/conversation/list")
                        .header("X-Client-ID", "client-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray());

        mockMvc.perform(get("/service/url/openurl/v1/agent/suffix-001/conversation/detail")
                        .header("X-Client-ID", "client-001")
                        .param("conversationId", "conversation-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        verify(appService).listAssistantConversations(any(AssistantConversationListQuery.class));
        verify(appService).listAssistantConversationDetails(any(AssistantConversationDetailQuery.class));
    }

    private String readBody(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private void respondSse(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private AppUrlInfo appUrlInfo(String assistantId) {
        AppUrlInfo info = new AppUrlInfo();
        info.setUrlId("1");
        info.setAppId(assistantId);
        info.setAppType("agent");
        info.setName("Public demo");
        info.setCreatedAt("2026-06-29 10:00:00");
        info.setExpiredAt("2026-07-01 12:30:00");
        info.setSuffix("suffix-001");
        info.setStatus(true);
        info.setUserId("dev-admin");
        info.setOrgId("default-org");
        info.setDescription("open desc");
        return info;
    }
}
