package com.unicomai.wanwu.service.bff.web;

import com.jayway.jsonpath.JsonPath;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.AppKeyInfo;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantPublishedQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationChatCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteByIdCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationMessageListQuery;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.mcp.McpService;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.operate.OperateService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuOpenApiControllerTest {

    private final AppService appService = mock(AppService.class);
    private final ModelService modelService = mock(ModelService.class);
    private final KnowledgeService knowledgeService = mock(KnowledgeService.class);
    private final IamService iamService = mock(IamService.class);
    private final OperateService operateService = mock(OperateService.class);
    private final McpService mcpService = mock(McpService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuOpenApiController(
                    appService, modelService, knowledgeService, iamService, new OpenApiChatflowSessionStore(),
                    operateService, mcpService))
            .build();

    @Test
    public void agentManagementAndConversationRoutesUseOpenApiContext() throws Exception {
        when(appService.createAssistant(any(AssistantCreateCommand.class)))
                .thenReturn(new AssistantCreateResult("assistant-openapi-001"));
        when(appService.listApplications(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(appRow()), 1));
        when(appService.createAssistantConversation(any(AssistantConversationCreateCommand.class)))
                .thenReturn(new AssistantConversationCreateResult("conversation-openapi-001"));

        mockMvc.perform(post("/service/api/openapi/v1/agent")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"OpenAPI Agent\",\"desc\":\"from api\",\"category\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.uuid").value("assistant-openapi-001"));

        mockMvc.perform(get("/service/api/openapi/v1/agent/list")
                        .header("X-API-Key", "dev-token")
                        .param("name", "OpenAPI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].uuid").value("assistant-openapi-001"))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(post("/service/api/openapi/v1/agent/conversation")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"assistant-openapi-001\",\"title\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversation_id").value("conversation-openapi-001"))
                .andExpect(jsonPath("$.data.conversationId").value("conversation-openapi-001"));

        ArgumentCaptor<AssistantCreateCommand> createCaptor = forClass(AssistantCreateCommand.class);
        verify(appService).createAssistant(createCaptor.capture());
        assertEquals("dev-admin", createCaptor.getValue().getUserId());
        assertEquals("default-org", createCaptor.getValue().getOrgId());

        ArgumentCaptor<AssistantConversationCreateCommand> conversationCaptor =
                forClass(AssistantConversationCreateCommand.class);
        verify(appService).createAssistantConversation(conversationCaptor.capture());
        assertEquals("assistant-openapi-001", conversationCaptor.getValue().getAssistantId());
        assertEquals("published", conversationCaptor.getValue().getConversationType());
    }

    @Test
    public void openApiRoutesRejectMissingApiKey() throws Exception {
        mockMvc.perform(get("/service/api/openapi/v1/agent/list")
                        .param("name", "OpenAPI"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("token is nil"));
    }

    @Test
    public void openApiRoutesUseResolvedApiKeyContext() throws Exception {
        when(appService.getApiKeyByKey("wanwu-real-key"))
                .thenReturn(apiKey("api-key-9", "user-real", "org-real", true, "2099-01-01"));
        when(appService.listApplications(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(appRow()), 1));

        mockMvc.perform(get("/service/api/openapi/v1/agent/list")
                        .header("Authorization", "Bearer wanwu-real-key")
                        .param("name", "OpenAPI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        ArgumentCaptor<ApplicationListQuery> queryCaptor = forClass(ApplicationListQuery.class);
        verify(appService).listApplications(queryCaptor.capture());
        assertEquals("user-real", queryCaptor.getValue().getUserId());
        assertEquals("org-real", queryCaptor.getValue().getOrgId());
    }

    @Test
    public void openApiRoutesRejectDisabledAndExpiredApiKeys() throws Exception {
        when(appService.getApiKeyByKey("disabled-key"))
                .thenReturn(apiKey("api-key-disabled", "user-real", "org-real", false, "2099-01-01"));
        when(appService.getApiKeyByKey("expired-key"))
                .thenReturn(apiKey("api-key-expired", "user-real", "org-real", true, "2000-01-01"));

        mockMvc.perform(get("/service/api/openapi/v1/agent/list")
                        .header("Authorization", "Bearer disabled-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("api key disabled"));

        mockMvc.perform(get("/service/api/openapi/v1/agent/list")
                        .header("Authorization", "Bearer expired-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("api key expired"));
    }

    @Test
    public void openApiFineGrainedAuthRejectsModelAndKnowledgePermissionErrors() throws Exception {
        when(modelService.listModelIdsByUuids(Collections.singletonList("blocked-model-uuid")))
                .thenReturn(Collections.singletonList("blocked-model"));
        doAnswer(invocation -> {
            throw new IllegalArgumentException("bff_model_perm: blocked-model");
        }).when(modelService).checkModelUserPermission(eq("dev-admin"), eq("default-org"),
                eq(Collections.singletonList("blocked-model")));
        doAnswer(invocation -> {
            throw new IllegalArgumentException("knowledge permission denied");
        }).when(knowledgeService).checkKnowledgeUserPermission(eq("dev-admin"), eq("default-org"),
                eq("knowledge-blocked"), eq(30));

        mockMvc.perform(put("/service/api/openapi/v1/agent/config")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantUuid\":\"assistant-openapi-001\",\"modelConfig\":{\"modelId\":\"blocked-model-uuid\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("bff_model_perm: blocked-model"));

        mockMvc.perform(put("/service/api/openapi/v1/knowledge")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("knowledgeId is required"));

        mockMvc.perform(delete("/service/api/openapi/v1/knowledge")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-blocked\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("knowledge permission denied"));
    }

    @Test
    public void chatRagAndWorkflowRoutesMapToExistingAppService() throws Exception {
        AssistantConversationStreamResult assistantResult = new AssistantConversationStreamResult();
        assistantResult.setConversationId("conversation-openapi-001");
        assistantResult.setDetailId("detail-openapi-001");
        assistantResult.setResponse("assistant answer");
        Map<String, Object> assistantSearch = new LinkedHashMap<>();
        assistantSearch.put("title", "AgentPolicy.txt");
        assistantSearch.put("snippet", "Agent policy hit");
        assistantSearch.put("knowledgeName", "Agent KB");
        assistantResult.setSearchList(Collections.singletonList(assistantSearch));
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class)))
                .thenReturn(assistantResult);

        RagChatResult ragResult = new RagChatResult();
        ragResult.setResponse("rag answer");
        Map<String, Object> ragSearch = new LinkedHashMap<>();
        ragSearch.put("title", "PolicyGuide.txt");
        ragSearch.put("snippet", "Policy hit");
        ragSearch.put("knowledgeName", "Policy KB");
        ragResult.setSearchList(Collections.singletonList(ragSearch));
        when(appService.streamRagChat(any(RagChatCommand.class))).thenReturn(ragResult);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("result", "workflow answer");
        when(appService.runWorkflow(any(WorkflowRunCommand.class)))
                .thenReturn(new WorkflowRunResult("workflow-openapi-001", output));

        mockMvc.perform(post("/service/api/openapi/v1/agent/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"assistant-openapi-001\",\"conversation_id\":\"conversation-openapi-001\",\"query\":\"hi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("assistant answer"))
                .andExpect(jsonPath("$.search_list[0].kb_name").value("Agent KB"))
                .andExpect(jsonPath("$.search_list[0].title").value("AgentPolicy.txt"))
                .andExpect(jsonPath("$.search_list[0].snippet").value("Agent policy hit"))
                .andExpect(jsonPath("$.finish").value(1));

        mockMvc.perform(post("/service/api/openapi/v1/rag/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"rag-openapi-001\",\"prompt\":\"search me\",\"file_info\":[{\"fileName\":\"note.txt\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.output").value("rag answer"))
                .andExpect(jsonPath("$.data.searchList[0].kb_name").value("Policy KB"))
                .andExpect(jsonPath("$.data.searchList[0].title").value("PolicyGuide.txt"))
                .andExpect(jsonPath("$.data.searchList[0].snippet").value("Policy hit"));

        mockMvc.perform(post("/service/api/openapi/v1/workflow/run")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"workflow-openapi-001\",\"parameters\":{\"city\":\"Beijing\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("workflow answer"));

        ArgumentCaptor<AssistantConversationStreamCommand> assistantCaptor =
                forClass(AssistantConversationStreamCommand.class);
        verify(appService).streamAssistantConversation(assistantCaptor.capture());
        assertEquals("assistant-openapi-001", assistantCaptor.getValue().getAssistantId());
        assertEquals("conversation-openapi-001", assistantCaptor.getValue().getConversationId());
        assertEquals("hi", assistantCaptor.getValue().getPrompt());
        assertEquals(false, assistantCaptor.getValue().isDraft());
        ArgumentCaptor<RagChatCommand> ragCaptor = forClass(RagChatCommand.class);
        verify(appService).streamRagChat(ragCaptor.capture());
        assertEquals("rag-openapi-001", ragCaptor.getValue().getRagId());
        assertEquals("search me", ragCaptor.getValue().getQuestion());
        assertEquals(false, ragCaptor.getValue().isDraft());
        assertEquals(1, ragCaptor.getValue().getFileInfo().size());
        verify(appService).runWorkflow(any(WorkflowRunCommand.class));
    }

    @Test
    public void agentOpenApiStreamReturnsLegacySseWithSearchList() throws Exception {
        AssistantConversationStreamResult assistantResult = new AssistantConversationStreamResult();
        assistantResult.setConversationId("conversation-openapi-stream-001");
        assistantResult.setDetailId("detail-openapi-stream-001");
        assistantResult.setResponse("assistant stream answer");
        Map<String, Object> assistantSearch = new LinkedHashMap<>();
        assistantSearch.put("title", "StreamPolicy.txt");
        assistantSearch.put("snippet", "Stream hit");
        assistantSearch.put("knowledgeName", "Stream KB");
        assistantResult.setSearchList(Collections.singletonList(assistantSearch));
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class)))
                .thenReturn(assistantResult);

        mockMvc.perform(post("/service/api/openapi/v1/agent/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"assistant-openapi-stream-001\",\"query\":\"hi\",\"stream\":true,\"file_info\":[{\"fileName\":\"note.txt\"}]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("data: {\"code\":0")))
                .andExpect(content().string(containsString("\"conversation_id\":\"conversation-openapi-stream-001\"")))
                .andExpect(content().string(containsString("\"search_list\":[{\"kb_name\":\"Stream KB\",\"title\":\"StreamPolicy.txt\",\"snippet\":\"Stream hit\"")))
                .andExpect(content().string(containsString("data: [DONE]")));

        ArgumentCaptor<AssistantConversationStreamCommand> assistantCaptor =
                forClass(AssistantConversationStreamCommand.class);
        verify(appService).streamAssistantConversation(assistantCaptor.capture());
        assertEquals("assistant-openapi-stream-001", assistantCaptor.getValue().getAssistantId());
        assertEquals("hi", assistantCaptor.getValue().getPrompt());
        assertEquals(false, assistantCaptor.getValue().isDraft());
        assertEquals(1, assistantCaptor.getValue().getFileInfo().size());
    }

    @Test
    public void agentOpenApiChatUsesConfiguredOpenAiCompatibleModelBeforePersisting() throws Exception {
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange.getRequestBody()));
            respondSse(exchange, "data: {\"id\":\"chatcmpl-openapi-agent\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"openapi \"},"
                    + "\"finish_reason\":null}],\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":1,\"total_tokens\":3}}\n\n"
                    + "data: {\"id\":\"chatcmpl-openapi-agent\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"agent answer\"},"
                    + "\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":3,\"total_tokens\":5}}\n\n"
                    + "data: [DONE]\n\n");
        });
        server.start();
        try {
            Map<String, Object> modelConfig = new LinkedHashMap<>();
            modelConfig.put("modelId", "model-openapi-agent-001");
            Map<String, Object> assistant = new LinkedHashMap<>();
            assistant.put("assistantId", "assistant-openapi-model-001");
            assistant.put("modelConfig", modelConfig);
            when(appService.getPublishedAssistant(any(AssistantPublishedQuery.class))).thenReturn(assistant);

            ModelInfo model = new ModelInfo();
            model.setModelId("model-openapi-agent-001");
            model.setModel("deepseek-chat");
            model.setProvider("openai-compatible");
            model.setModelType("llm");
            model.setIsActive(true);
            model.getConfig().put("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            model.getConfig().put("apiKey", "local-key");
            when(modelService.getModel(anyString(), anyString(), eq("model-openapi-agent-001"))).thenReturn(model);

            when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class)))
                    .thenAnswer(invocation -> {
                        AssistantConversationStreamCommand command = invocation.getArgument(0);
                        AssistantConversationStreamResult result = new AssistantConversationStreamResult();
                        result.setConversationId("conversation-openapi-model-001");
                        result.setDetailId("detail-openapi-model-001");
                        result.setResponse(command.getOverrideResponse());
                        result.setSearchList(Collections.emptyList());
                        return result;
                    });

            mockMvc.perform(post("/service/api/openapi/v1/agent/chat")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"uuid\":\"assistant-openapi-model-001\",\"query\":\"hello agent\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.response").value("openapi agent answer"))
                    .andExpect(jsonPath("$.conversation_id").value("conversation-openapi-model-001"));

            assertTrue(upstreamBody.get().contains("\"stream\":true"));
            assertTrue(upstreamBody.get().contains("\"content\":\"hello agent\""));
            ArgumentCaptor<AssistantConversationStreamCommand> captor =
                    forClass(AssistantConversationStreamCommand.class);
            verify(appService).streamAssistantConversation(captor.capture());
            assertEquals("openapi agent answer", captor.getValue().getOverrideResponse());
            verify(appService).getPublishedAssistant(any(AssistantPublishedQuery.class));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void draftAgentOpenApiUsesDraftConversationAndSse() throws Exception {
        AssistantConversationStreamResult assistantResult = new AssistantConversationStreamResult();
        assistantResult.setConversationId("conversation-draft-openapi-001");
        assistantResult.setDetailId("detail-draft-openapi-001");
        assistantResult.setResponse("draft answer");
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class)))
                .thenReturn(assistantResult);

        mockMvc.perform(post("/service/api/openapi/v1/agent/chat/draft")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"assistant-draft-openapi-001\",\"query\":\"draft hi\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"response\":\"draft answer\"")))
                .andExpect(content().string(containsString("\"conversation_id\":\"conversation-draft-openapi-001\"")))
                .andExpect(content().string(containsString("data: [DONE]")));

        ArgumentCaptor<AssistantConversationStreamCommand> assistantCaptor =
                forClass(AssistantConversationStreamCommand.class);
        verify(appService).streamAssistantConversation(assistantCaptor.capture());
        assertEquals("assistant-draft-openapi-001", assistantCaptor.getValue().getAssistantId());
        assertEquals("draft hi", assistantCaptor.getValue().getPrompt());
        assertEquals("", assistantCaptor.getValue().getConversationId());
        assertEquals(true, assistantCaptor.getValue().isDraft());
    }

    @Test
    public void ragOpenApiStreamReturnsLegacySseWithSearchList() throws Exception {
        RagChatResult ragResult = new RagChatResult();
        ragResult.setRagId("rag-openapi-stream-001");
        ragResult.setResponse("stream rag answer");
        Map<String, Object> ragSearch = new LinkedHashMap<>();
        ragSearch.put("title", "PolicyGuide.txt");
        ragSearch.put("snippet", "Policy hit");
        ragSearch.put("knowledgeName", "Policy KB");
        ragResult.setSearchList(Collections.singletonList(ragSearch));
        when(appService.streamRagChat(any(RagChatCommand.class))).thenReturn(ragResult);

        mockMvc.perform(post("/service/api/openapi/v1/rag/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"rag-openapi-stream-001\",\"query\":\"PolicyGuide\",\"stream\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("data: {\"code\":0")))
                .andExpect(content().string(containsString("\"searchList\":[{\"kb_name\":\"Policy KB\",\"title\":\"PolicyGuide.txt\",\"snippet\":\"Policy hit\"")))
                .andExpect(content().string(containsString("data: [DONE]")));
    }

    @Test
    public void ragOpenApiChatLeavesGroundedModelExecutionToAppService() throws Exception {
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange.getRequestBody()));
            respondSse(exchange, "data: {\"id\":\"chatcmpl-openapi-rag\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"openapi \"},"
                    + "\"finish_reason\":null}],\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":1,\"total_tokens\":4}}\n\n"
                    + "data: {\"id\":\"chatcmpl-openapi-rag\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"rag answer\"},"
                    + "\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":3,\"total_tokens\":6}}\n\n"
                    + "data: [DONE]\n\n");
        });
        server.start();
        try {
            Map<String, Object> modelConfig = new LinkedHashMap<>();
            modelConfig.put("modelId", "model-openapi-rag-001");
            Map<String, Object> rag = new LinkedHashMap<>();
            rag.put("ragId", "rag-openapi-model-001");
            rag.put("modelConfig", modelConfig);
            when(appService.getPublishedRag(any(RagDetailQuery.class))).thenReturn(rag);

            ModelInfo model = new ModelInfo();
            model.setModelId("model-openapi-rag-001");
            model.setModel("deepseek-chat");
            model.setProvider("openai-compatible");
            model.setModelType("llm");
            model.setIsActive(true);
            model.getConfig().put("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            model.getConfig().put("apiKey", "local-key");
            when(modelService.getModel(anyString(), anyString(), eq("model-openapi-rag-001"))).thenReturn(model);

            when(appService.streamRagChat(any(RagChatCommand.class))).thenAnswer(invocation -> {
                RagChatCommand command = invocation.getArgument(0);
                RagChatResult result = new RagChatResult();
                result.setRagId(command.getRagId());
                result.setResponse("openapi rag answer");
                result.setSearchList(Collections.emptyList());
                result.setQaSearchList(Collections.emptyList());
                return result;
            });

            mockMvc.perform(post("/service/api/openapi/v1/rag/chat")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"uuid\":\"rag-openapi-model-001\",\"query\":\"what is policy\","
                                    + "\"history\":[{\"query\":\"openapi previous question\",\"response\":\"openapi previous answer\",\"needHistory\":true},"
                                    + "{\"query\":\"openapi ignored question\",\"response\":\"openapi ignored answer\",\"needHistory\":false}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.output").value("openapi rag answer"));

            assertEquals(null, upstreamBody.get());
            ArgumentCaptor<RagChatCommand> captor = forClass(RagChatCommand.class);
            verify(appService).streamRagChat(captor.capture());
            assertEquals(null, captor.getValue().getOverrideResponse());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void ragOpenApiChatRecordsFailureStatisticWhenServiceRejectsRequest() throws Exception {
        when(appService.streamRagChat(any(RagChatCommand.class)))
                .thenThrow(new IllegalArgumentException("rag snapshot not found"));

        mockMvc.perform(post("/service/api/openapi/v1/rag/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"rag-openapi-001\",\"prompt\":\"search me\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value("rag snapshot not found"));

        ArgumentCaptor<RecordAppStatisticCommand> statisticCaptor = forClass(RecordAppStatisticCommand.class);
        verify(appService).recordAppStatistic(statisticCaptor.capture());
        assertEquals("dev-admin", statisticCaptor.getValue().getUserId());
        assertEquals("default-org", statisticCaptor.getValue().getOrgId());
        assertEquals("rag-openapi-001", statisticCaptor.getValue().getAppId());
        assertEquals("rag", statisticCaptor.getValue().getAppType());
        assertEquals(false, statisticCaptor.getValue().isSuccess());
        assertEquals(false, statisticCaptor.getValue().isStream());
        assertEquals(0L, statisticCaptor.getValue().getStreamCosts());
        assertTrue(statisticCaptor.getValue().getNonStreamCosts() >= 0L);
        assertEquals("openapi", statisticCaptor.getValue().getSource());
    }

    @Test
    public void agentConfigAndPublishRoutesUseAppService() throws Exception {
        when(modelService.listModelIdsByUuids(Collections.singletonList("model-001")))
                .thenReturn(Collections.singletonList("model-001"));

        mockMvc.perform(put("/service/api/openapi/v1/agent/config")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantUuid\":\"assistant-openapi-001\",\"prologue\":\"hello\",\"instructions\":\"be useful\",\"recommendQuestion\":[\"q1\"],\"modelConfig\":{\"modelId\":\"model-001\",\"modelType\":\"llm\"},\"knowledgeBaseConfig\":{\"knowledgebases\":[{\"id\":\"knowledge-001\"}],\"config\":{\"topK\":3}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/service/api/openapi/v1/agent/publish")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantUuid\":\"assistant-openapi-001\",\"version\":\"v1.0.0\",\"desc\":\"first release\",\"publishType\":\"organization\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<AssistantConfigUpdateCommand> configCaptor =
                forClass(AssistantConfigUpdateCommand.class);
        verify(appService).updateAssistantConfig(configCaptor.capture());
        verify(modelService).checkModelUserPermission(eq("dev-admin"), eq("default-org"),
                eq(Collections.singletonList("model-001")));
        assertEquals("assistant-openapi-001", configCaptor.getValue().getAssistantId());
        assertEquals("dev-admin", configCaptor.getValue().getUserId());
        assertEquals("default-org", configCaptor.getValue().getOrgId());
        assertEquals("hello", configCaptor.getValue().getPrologue());
        assertEquals("be useful", configCaptor.getValue().getInstructions());
        assertEquals("model-001", configCaptor.getValue().getModelConfig().get("modelId"));
        assertEquals(3, configCaptor.getValue().getKnowledgeBaseConfig().get("config") instanceof Map
                ? ((Map<?, ?>) configCaptor.getValue().getKnowledgeBaseConfig().get("config")).get("topK") : null);
        assertEquals("q1", configCaptor.getValue().getRecommendQuestion().get(0));

        ArgumentCaptor<AppPublishCommand> publishCaptor = forClass(AppPublishCommand.class);
        verify(appService).publishApp(publishCaptor.capture());
        assertEquals("assistant-openapi-001", publishCaptor.getValue().getAppId());
        assertEquals("agent", publishCaptor.getValue().getAppType());
        assertEquals("v1.0.0", publishCaptor.getValue().getVersion());
        assertEquals("first release", publishCaptor.getValue().getDesc());
        assertEquals("organization", publishCaptor.getValue().getPublishType());
        assertEquals("dev-admin", publishCaptor.getValue().getUserId());
        assertEquals("default-org", publishCaptor.getValue().getOrgId());
    }

    @Test
    public void chatflowOpenApiRoutesUseAppServiceConversationState() throws Exception {
        String conversationId = "conversation-chatflow-openapi-001";
        Map<String, Object> created = new LinkedHashMap<>();
        created.put("conversation_id", conversationId);
        created.put("conversationId", conversationId);
        created.put("conversation_name", "Policy chat");
        created.put("conversationName", "Policy chat");
        created.put("uuid", "chatflow-openapi-001");
        when(appService.createChatflowOpenApiConversation(any(ChatflowConversationCreateCommand.class)))
                .thenReturn(created);

        Map<String, Object> chatResult = new LinkedHashMap<>();
        chatResult.put("code", 0);
        chatResult.put("message", "success");
        chatResult.put("conversation_id", conversationId);
        chatResult.put("response", "Hello from Chatflow");
        chatResult.put("finish", 1);
        chatResult.put("run_id", "workflow-run-chatflow-001");
        chatResult.put("chunks", java.util.Arrays.asList("Hello from ", "Chatflow"));
        chatResult.put("search_list", Collections.emptyList());
        chatResult.put("node_events", Collections.singletonList(
                Collections.<String, Object>singletonMap("node_id", "llm-1")));
        Map<String, Object> chatUsage = new LinkedHashMap<>();
        chatUsage.put("prompt_tokens", 8);
        chatUsage.put("completion_tokens", 3);
        chatUsage.put("total_tokens", 11);
        chatResult.put("usage", chatUsage);
        when(appService.chatflowOpenApiChat(any(ChatflowConversationChatCommand.class))).thenReturn(chatResult);

        Map<String, Object> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "hello chatflow");
        Map<String, Object> assistantMessage = new LinkedHashMap<>();
        assistantMessage.put("role", "assistant");
        assistantMessage.put("content", "Chatflow response: hello chatflow");
        Map<String, Object> messagePage = new LinkedHashMap<>();
        messagePage.put("data", java.util.Arrays.asList(userMessage, assistantMessage));
        messagePage.put("has_more", false);
        messagePage.put("first_id", "1");
        messagePage.put("last_id", "2");
        when(appService.listChatflowOpenApiConversationMessages(any(ChatflowConversationMessageListQuery.class)))
                .thenReturn(messagePage);

        Map<String, Object> conversation = new LinkedHashMap<>();
        conversation.put("conversation_id", conversationId);
        conversation.put("conversation_name", "Policy chat");
        Map<String, Object> conversationList = new LinkedHashMap<>();
        conversationList.put("conversations", Collections.singletonList(conversation));
        conversationList.put("list", Collections.singletonList(conversation));
        conversationList.put("total", 1L);
        Map<String, Object> emptyConversationList = new LinkedHashMap<>();
        emptyConversationList.put("conversations", Collections.emptyList());
        emptyConversationList.put("list", Collections.emptyList());
        emptyConversationList.put("total", 0L);
        when(appService.listChatflowOpenApiConversations(any(ChatflowConversationListQuery.class)))
                .thenReturn(conversationList)
                .thenReturn(emptyConversationList);

        String createdBody = mockMvc.perform(post("/service/api/openapi/v1/chatflow/conversation")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"chatflow-openapi-001\",\"conversation_name\":\"Policy chat\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversation_id").value(conversationId))
                .andExpect(jsonPath("$.data.conversation_name").value("Policy chat"))
                .andReturn().getResponse().getContentAsString();
        assertEquals(conversationId, JsonPath.read(createdBody, "$.data.conversation_id"));

        String chatBody = mockMvc.perform(post("/service/api/openapi/v1/chatflow/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"chatflow-openapi-001\",\"conversation_id\":\"" + conversationId + "\",\"query\":\"hello chatflow\",\"parameters\":{\"city\":\"Beijing\"}}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andExpect(header().string("X-Accel-Buffering", "no"))
                .andExpect(content().string(containsString("event: conversation.chat.created")))
                .andExpect(content().string(containsString("event: conversation.message.delta")))
                .andExpect(content().string(containsString("event: conversation.chat.completed")))
                .andExpect(content().string(containsString("event: done")))
                .andExpect(content().string(containsString("Hello from Chatflow")))
                .andExpect(content().string(containsString("\"token_count\":11")))
                .andExpect(content().string(containsString(conversationId)))
                .andReturn().getResponse().getContentAsString();
        assertTrue(chatBody.indexOf("event: conversation.chat.created")
                < chatBody.indexOf("event: conversation.chat.in_progress"));
        assertTrue(chatBody.indexOf("event: conversation.chat.in_progress")
                < chatBody.indexOf("event: conversation.message.delta"));
        assertTrue(chatBody.indexOf("event: conversation.message.completed")
                < chatBody.indexOf("event: conversation.chat.completed"));
        assertTrue(chatBody.indexOf("event: conversation.chat.completed") < chatBody.indexOf("event: done"));

        mockMvc.perform(post("/service/api/openapi/v1/chatflow/conversation/message/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"chatflow-openapi-001\",\"conversation_id\":\"" + conversationId + "\",\"limit\":\"10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].role").value("user"))
                .andExpect(jsonPath("$.data.data[0].content").value("hello chatflow"))
                .andExpect(jsonPath("$.data.data[1].role").value("assistant"));

        mockMvc.perform(post("/service/api/openapi/v1/chatflow/conversation/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"chatflow-openapi-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversations[0].conversation_id").value(conversationId))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(delete("/service/api/openapi/v1/chatflow/conversation")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"chatflow-openapi-001\",\"conversation_id\":\"" + conversationId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/service/api/openapi/v1/chatflow/conversation/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"chatflow-openapi-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        ArgumentCaptor<ChatflowConversationCreateCommand> createCaptor =
                forClass(ChatflowConversationCreateCommand.class);
        verify(appService).createChatflowOpenApiConversation(createCaptor.capture());
        assertEquals("chatflow-openapi-001", createCaptor.getValue().getChatflowId());
        assertEquals("Policy chat", createCaptor.getValue().getConversationName());
        assertEquals("dev-admin", createCaptor.getValue().getUserId());
        assertEquals("default-org", createCaptor.getValue().getOrgId());

        ArgumentCaptor<ChatflowConversationChatCommand> chatCaptor =
                forClass(ChatflowConversationChatCommand.class);
        verify(appService).chatflowOpenApiChat(chatCaptor.capture());
        assertEquals(conversationId, chatCaptor.getValue().getConversationId());
        assertEquals("hello chatflow", chatCaptor.getValue().getQuery());
        assertEquals("Beijing", chatCaptor.getValue().getParameters().get("city"));

        verify(appService).listChatflowOpenApiConversationMessages(any(ChatflowConversationMessageListQuery.class));
        verify(appService, times(2)).listChatflowOpenApiConversations(any(ChatflowConversationListQuery.class));
        verify(appService).deleteChatflowOpenApiConversation(any(ChatflowConversationDeleteByIdCommand.class));
    }

    @Test
    public void chatflowOpenApiChatRecordsFailureStatisticWhenServiceRejectsRequest() throws Exception {
        when(appService.chatflowOpenApiChat(any(ChatflowConversationChatCommand.class)))
                .thenThrow(new IllegalArgumentException("query is required"));

        mockMvc.perform(post("/service/api/openapi/v1/chatflow/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"chatflow-openapi-001\",\"conversation_id\":\"conversation-chatflow-openapi-001\",\"query\":\"hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value("query is required"));

        ArgumentCaptor<RecordAppStatisticCommand> statisticCaptor = forClass(RecordAppStatisticCommand.class);
        verify(appService).recordAppStatistic(statisticCaptor.capture());
        assertEquals("dev-admin", statisticCaptor.getValue().getUserId());
        assertEquals("default-org", statisticCaptor.getValue().getOrgId());
        assertEquals("chatflow-openapi-001", statisticCaptor.getValue().getAppId());
        assertEquals("chatflow", statisticCaptor.getValue().getAppType());
        assertEquals(false, statisticCaptor.getValue().isSuccess());
        assertEquals(true, statisticCaptor.getValue().isStream());
        assertTrue(statisticCaptor.getValue().getStreamCosts() >= 0L);
        assertEquals(0L, statisticCaptor.getValue().getNonStreamCosts());
        assertEquals("openapi", statisticCaptor.getValue().getSource());
    }

    @Test
    public void modelKnowledgeUploadOauthAndMcpShellsDoNotReturnNotFound() throws Exception {
        when(modelService.listModels(any())).thenReturn(new ModelListResult(Collections.emptyList(), 0));
        when(knowledgeService.createKnowledge(eq("dev-admin"), eq("default-org"), any()))
                .thenReturn(Collections.singletonMap("knowledgeId", "knowledge-openapi-001"));
        Map<String, Object> knowledgeRow = new LinkedHashMap<>();
        knowledgeRow.put("knowledgeId", "knowledge-openapi-001");
        knowledgeRow.put("name", "Knowledge");
        Map<String, Object> knowledgePage = new LinkedHashMap<>();
        knowledgePage.put("list", Collections.singletonList(knowledgeRow));
        knowledgePage.put("total", 1);
        when(knowledgeService.selectKnowledge(eq("dev-admin"), eq("default-org"), any())).thenReturn(knowledgePage);
        Map<String, Object> docConfig = new LinkedHashMap<>();
        docConfig.put("chunkSize", 700);
        when(knowledgeService.getDocConfig(eq("dev-admin"), eq("default-org"), any())).thenReturn(docConfig);
        Map<String, Object> hit = new LinkedHashMap<>();
        hit.put("searchList", Collections.singletonList(Collections.singletonMap("title", "OpenApiDoc.txt")));
        when(knowledgeService.hitKnowledge(eq("dev-admin"), eq("default-org"), any())).thenReturn(hit);
        when(appService.getAppKeyByKey("app-key"))
                .thenReturn(appKey("app-key-1", "app-key", "mcpserver-001", "mcpserver"));
        Map<String, Object> serverTool = new LinkedHashMap<>();
        serverTool.put("methodName", "get_weather");
        serverTool.put("desc", "Get weather by city");
        serverTool.put("type", "custom");
        serverTool.put("id", "tool-weather");
        Map<String, Object> mcpServer = new LinkedHashMap<>();
        mcpServer.put("mcpServerId", "mcpserver-001");
        mcpServer.put("tools", Collections.singletonList(serverTool));
        when(mcpService.getMcpServer(eq("dev-admin"), eq("default-org"), eq("mcpserver-001")))
                .thenReturn(mcpServer);

        mockMvc.perform(get("/service/api/openapi/v1/model/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("modelType", "llm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/service/api/openapi/v1/knowledge")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Knowledge\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeId").value("knowledge-openapi-001"));

        mockMvc.perform(post("/service/api/openapi/v1/knowledge/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].knowledgeId").value("knowledge-openapi-001"));

        mockMvc.perform(get("/service/api/openapi/v1/knowledge/doc/config")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-openapi-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chunkSize").value(700));

        mockMvc.perform(post("/service/api/openapi/v1/knowledge/doc/import")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-openapi-001\",\"docInfoList\":[{\"docName\":\"OpenApiDoc.txt\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/service/api/openapi/v1/knowledge/hit")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"OpenApiDoc\",\"knowledgeList\":[{\"knowledgeId\":\"knowledge-openapi-001\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.searchList[0].title").value("OpenApiDoc.txt"));

        mockMvc.perform(multipart("/service/api/openapi/v1/workflow/file/upload")
                        .file(new MockMultipartFile("file", "input.txt", "text/plain", "hello".getBytes()))
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/service/api/openapi/v1/file/download/openapi-file-")));

        mockMvc.perform(multipart("/service/api/openapi/v1/chatflow/file/upload")
                        .file(new MockMultipartFile("file", "chatflow-input.txt", "text/plain", "hello".getBytes()))
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/service/api/openapi/v1/file/download/openapi-file-")));

        mockMvc.perform(get("/service/api/openapi/v1/oauth/jwks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"));

        mockMvc.perform(get("/service/api/openapi/v1/mcp/server/sse").param("key", "app-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("notifications/initialized")))
                .andExpect(content().string(containsString("mcpserver-001")));

        mockMvc.perform(post("/service/api/openapi/v1/mcp/server/message")
                        .param("key", "app-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"mcp-call-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.mcpServerId").value("mcpserver-001"));

        mockMvc.perform(post("/service/api/openapi/v1/mcp/server/message")
                        .param("key", "app-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"mcp-init-1\",\"method\":\"initialize\","
                                + "\"params\":{\"protocolVersion\":\"2024-11-05\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("mcp-init-1"))
                .andExpect(jsonPath("$.result.protocolVersion").value("2024-11-05"))
                .andExpect(jsonPath("$.result.capabilities.tools.listChanged").value(false))
                .andExpect(jsonPath("$.result.serverInfo.name").value("wanwu-java-mcp-server"))
                .andExpect(jsonPath("$.result.mcpServerId").value("mcpserver-001"));

        mockMvc.perform(post("/service/api/openapi/v1/mcp/server/message")
                        .param("key", "app-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"mcp-ping-1\",\"method\":\"ping\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("mcp-ping-1"))
                .andExpect(jsonPath("$.result").isMap());

        mockMvc.perform(post("/service/api/openapi/v1/mcp/server/message")
                        .param("key", "app-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"mcp-list-1\",\"method\":\"tools/list\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tools[0].name").value("get_weather"))
                .andExpect(jsonPath("$.result.tools[0].description").value("Get weather by city"));

        mockMvc.perform(post("/service/api/openapi/v1/mcp/server/message")
                        .param("key", "app-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"mcp-call-2\",\"method\":\"tools/call\","
                                + "\"params\":{\"name\":\"get_weather\",\"arguments\":{\"city\":\"Hangzhou\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].type").value("text"))
                .andExpect(jsonPath("$.result.content[0].text").value(containsString("get_weather")))
                .andExpect(jsonPath("$.result.content[0].text").value(containsString("Hangzhou")))
                .andExpect(jsonPath("$.result.structuredContent.mcpServerId").value("mcpserver-001"))
                .andExpect(jsonPath("$.result.structuredContent.toolId").value("tool-weather"))
                .andExpect(jsonPath("$.result.structuredContent.arguments.city").value("Hangzhou"));

        verify(knowledgeService).createKnowledge(eq("dev-admin"), eq("default-org"), any());
        verify(knowledgeService).selectKnowledge(eq("dev-admin"), eq("default-org"), any());
        verify(knowledgeService).getDocConfig(eq("dev-admin"), eq("default-org"), any());
        verify(knowledgeService).importDocs(eq("dev-admin"), eq("default-org"), any());
        verify(knowledgeService).hitKnowledge(eq("dev-admin"), eq("default-org"), any());
    }

    @Test
    public void modelOpenApiListReturnsGoPublicFieldsOnly() throws Exception {
        ModelInfo model = new ModelInfo();
        model.setModelId("internal-model-id");
        model.setUuid("model-openapi-001");
        model.setDisplayName("OpenAPI LLM");
        model.setProvider("OpenAI-API-compatible");
        model.setModelType("llm");
        model.setModel("gpt-compatible");
        model.setScopeType("public");
        model.getConfig().put("apiKey", "hidden");
        when(modelService.listModels(any(ModelListQuery.class)))
                .thenReturn(new ModelListResult(Collections.singletonList(model), 1));

        mockMvc.perform(get("/service/api/openapi/v1/model/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("modelType", "llm")
                        .param("provider", "OpenAI-API-compatible")
                        .param("displayName", "OpenAPI")
                        .param("filterScope", "public")
                        .param("scopeType", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].uuid").value("model-openapi-001"))
                .andExpect(jsonPath("$.data.list[0].displayName").value("OpenAPI LLM"))
                .andExpect(jsonPath("$.data.list[0].provider").value("OpenAI-API-compatible"))
                .andExpect(jsonPath("$.data.list[0].modelType").value("llm"))
                .andExpect(jsonPath("$.data.list[0].model").value("gpt-compatible"))
                .andExpect(jsonPath("$.data.list[0].scopeType").value("public"))
                .andExpect(jsonPath("$.data.list[0].modelId").doesNotExist())
                .andExpect(jsonPath("$.data.list[0].config").doesNotExist());

        ArgumentCaptor<ModelListQuery> queryCaptor = forClass(ModelListQuery.class);
        verify(modelService).listModels(queryCaptor.capture());
        assertEquals("dev-admin", queryCaptor.getValue().getUserId());
        assertEquals("default-org", queryCaptor.getValue().getOrgId());
        assertEquals("llm", queryCaptor.getValue().getModelType());
        assertEquals("OpenAI-API-compatible", queryCaptor.getValue().getProvider());
        assertEquals("OpenAPI", queryCaptor.getValue().getDisplayName());
        assertEquals("public", queryCaptor.getValue().getFilterScope());
        assertEquals("2", queryCaptor.getValue().getScopeType());
    }

    @Test
    public void openApiUploadDownloadUrlReturnsUploadedBytes() throws Exception {
        MvcResult upload = mockMvc.perform(multipart("/service/api/openapi/v1/workflow/file/upload")
                        .file(new MockMultipartFile("file", "workflow-input.txt", "text/plain", "hello openapi".getBytes()))
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andReturn();

        String downloadUrl = upload.getResponse().getContentAsString();
        assertTrue(downloadUrl.startsWith("/service/api/openapi/v1/file/download/openapi-file-"));

        mockMvc.perform(get(downloadUrl))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("workflow-input.txt")))
                .andExpect(content().bytes("hello openapi".getBytes()));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void openApiKnowledgeExportFollowsGoAsyncRecordContract() throws Exception {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("exportRecordId", "export-openapi-001");
        record.put("author", "Admin");
        record.put("status", 2);
        record.put("filePath", "/user/api/v1/knowledge/export/file/export-openapi-001/Knowledge_export.zip");
        record.put("errorMsg", "");
        record.put("exportTime", "2026-07-05 22:00:00");
        record.put("knowledgeName", "Knowledge");
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("list", Collections.singletonList(record));
        page.put("total", 1);
        page.put("pageNo", 2);
        page.put("pageSize", 5);
        when(knowledgeService.listExportRecords(eq("dev-admin"), eq("default-org"), any())).thenReturn(page);
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("fileName", "Knowledge_export.zip");
        file.put("contentType", "application/zip");
        file.put("content", "zip-content");
        file.put("contentBase64", "");
        when(knowledgeService.getExportRecordFile(eq("dev-admin"), eq("default-org"), any())).thenReturn(file);

        mockMvc.perform(post("/service/api/openapi/v1/knowledge/doc/export")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-openapi-001\",\"docIdList\":[\"doc-1\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.exportRecordId").doesNotExist())
                .andExpect(jsonPath("$.data.fileUrl").doesNotExist());

        MvcResult listed = mockMvc.perform(get("/service/api/openapi/v1/knowledge/export/record/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-openapi-001")
                        .param("pageNo", "2")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNo").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.list[0].exportRecordId").value("export-openapi-001"))
                .andExpect(jsonPath("$.data.list[0].filePath")
                        .value("/service/api/openapi/v1/knowledge/export/file/export-openapi-001/Knowledge_export.zip"))
                .andReturn();

        String filePath = JsonPath.read(listed.getResponse().getContentAsString(), "$.data.list[0].filePath");
        mockMvc.perform(get(filePath).header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("Knowledge_export.zip")))
                .andExpect(content().bytes("zip-content".getBytes()));

        ArgumentCaptor<Map> exportCaptor = forClass(Map.class);
        verify(knowledgeService).exportDocs(eq("dev-admin"), eq("default-org"), exportCaptor.capture());
        assertEquals("knowledge-openapi-001", exportCaptor.getValue().get("knowledgeId"));

        ArgumentCaptor<Map> listCaptor = forClass(Map.class);
        verify(knowledgeService).listExportRecords(eq("dev-admin"), eq("default-org"), listCaptor.capture());
        assertEquals("knowledge-openapi-001", listCaptor.getValue().get("knowledgeId"));
        assertEquals("2", listCaptor.getValue().get("pageNo"));
        assertEquals("5", listCaptor.getValue().get("pageSize"));

        ArgumentCaptor<Map> fileCaptor = forClass(Map.class);
        verify(knowledgeService).getExportRecordFile(eq("dev-admin"), eq("default-org"), fileCaptor.capture());
        assertEquals("export-openapi-001", fileCaptor.getValue().get("exportRecordId"));
        assertEquals("Knowledge_export.zip", fileCaptor.getValue().get("fileName"));
    }

    @Test
    public void mcpOpenApiRoutesRejectMissingOrWrongAppKey() throws Exception {
        when(appService.getAppKeyByKey("agent-key"))
                .thenReturn(appKey("app-key-2", "agent-key", "agent-001", "agent"));

        mockMvc.perform(get("/service/api/openapi/v1/mcp/server/sse"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("token is nil"));

        mockMvc.perform(get("/service/api/openapi/v1/mcp/server/sse").param("key", "agent-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid appType"));
    }

    @Test
    public void mcpOpenApiToolCallDelegatesToMcpServiceRuntime() throws Exception {
        when(appService.getAppKeyByKey("runtime-key"))
                .thenReturn(appKey("app-key-runtime", "runtime-key", "mcpserver-002", "mcpserver"));
        Map<String, Object> serverTool = new LinkedHashMap<>();
        serverTool.put("methodName", "get_weather");
        serverTool.put("desc", "Get weather by city");
        serverTool.put("type", "custom");
        serverTool.put("id", "tool-weather-runtime");
        Map<String, Object> mcpServer = new LinkedHashMap<>();
        mcpServer.put("mcpServerId", "mcpserver-002");
        mcpServer.put("tools", Collections.singletonList(serverTool));
        when(mcpService.getMcpServer(eq("dev-admin"), eq("default-org"), eq("mcpserver-002")))
                .thenReturn(mcpServer);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "text");
        content.put("text", "{\"weather\":\"sunny\"}");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("weather", "sunny");
        Map<String, Object> structured = new LinkedHashMap<>();
        structured.put("mcpServerId", "mcpserver-002");
        structured.put("name", "get_weather");
        structured.put("response", response);
        Map<String, Object> runtimeResult = new LinkedHashMap<>();
        runtimeResult.put("content", Collections.singletonList(content));
        runtimeResult.put("structuredContent", structured);
        runtimeResult.put("isError", false);
        when(mcpService.callMcpServerTool(eq("dev-admin"), eq("default-org"), eq("mcpserver-002"), any()))
                .thenReturn(runtimeResult);

        mockMvc.perform(post("/service/api/openapi/v1/mcp/server/message")
                        .param("key", "runtime-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"mcp-call-runtime\",\"method\":\"tools/call\","
                                + "\"params\":{\"name\":\"get_weather\",\"arguments\":{\"query-city\":\"Hangzhou\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("mcp-call-runtime"))
                .andExpect(jsonPath("$.result.content[0].text").value("{\"weather\":\"sunny\"}"))
                .andExpect(jsonPath("$.result.structuredContent.response.weather").value("sunny"))
                .andExpect(jsonPath("$.result.isError").value(false));

        ArgumentCaptor<Map> requestCaptor = forClass(Map.class);
        verify(mcpService).callMcpServerTool(eq("dev-admin"), eq("default-org"), eq("mcpserver-002"),
                requestCaptor.capture());
        assertEquals("get_weather", requestCaptor.getValue().get("name"));
        assertEquals("Hangzhou", ((Map<?, ?>) requestCaptor.getValue().get("arguments")).get("query-city"));
    }

    @Test
    public void oauthAuthorizationCodeFlowUsesManagedOauthApp() throws Exception {
        when(iamService.listOauthApps(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(oauthPage());
        final Map<String, Map<String, Object>> oauthCodes = new LinkedHashMap<>();
        final Map<String, Map<String, Object>> refreshTokens = new LinkedHashMap<>();
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Map<String, Object> payload = invocation.getArgument(1);
            oauthCodes.put(key, new LinkedHashMap<>(payload));
            return null;
        }).when(operateService).saveOAuthCode(anyString(), any());
        when(operateService.consumeOAuthCode(anyString())).thenAnswer(invocation ->
                oauthCodes.remove(invocation.getArgument(0)));
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Map<String, Object> payload = invocation.getArgument(1);
            refreshTokens.put(key, new LinkedHashMap<>(payload));
            return null;
        }).when(operateService).saveOAuthRefreshToken(anyString(), any());
        when(operateService.consumeOAuthRefreshToken(anyString())).thenAnswer(invocation ->
                refreshTokens.remove(invocation.getArgument(0)));

        MvcResult authorized = mockMvc.perform(get("/service/api/openapi/v1/oauth/code/authorize")
                        .param("client_id", "oauth-client-1")
                        .param("redirect_uri", "http://localhost/callback")
                        .param("response_type", "code")
                        .param("scope", "openid profile")
                        .param("state", "state-1")
                        .param("jwt_token", "dev-token"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("http://localhost/callback?code=")))
                .andExpect(header().string("Location", containsString("state=state-1")))
                .andReturn();

        String location = authorized.getResponse().getHeader("Location");
        String code = location.substring(location.indexOf("code=") + "code=".length(), location.indexOf("&state="));
        MvcResult token = mockMvc.perform(post("/service/api/openapi/v1/oauth/code/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", "http://localhost/callback")
                        .param("client_id", "oauth-client-1")
                        .param("client_secret", "oauth-secret-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").value(containsString("wanwu-oauth-refresh-")))
                .andExpect(jsonPath("$.id_token").exists())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.scope[0]").value("openid"))
                .andReturn();

        String accessToken = JsonPath.read(token.getResponse().getContentAsString(), "$.access_token");
        String idToken = JsonPath.read(token.getResponse().getContentAsString(), "$.id_token");
        String refreshToken = JsonPath.read(token.getResponse().getContentAsString(), "$.refresh_token");
        assertTrue(accessToken.split("\\.").length == 3);
        assertTrue(idToken.split("\\.").length == 3);
        mockMvc.perform(get("/service/api/openapi/v1/oauth/userinfo")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("dev-admin"))
                .andExpect(jsonPath("$.username").value("admin"));

        mockMvc.perform(post("/service/api/openapi/v1/oauth/code/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grant_type\":\"refresh_token\",\"client_id\":\"oauth-client-1\","
                                + "\"client_secret\":\"oauth-secret-1\",\"refresh_token\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.expires_at").exists());

        mockMvc.perform(post("/service/api/openapi/v1/oauth/code/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", "http://localhost/callback")
                        .param("client_id", "oauth-client-1")
                        .param("client_secret", "oauth-secret-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid authorization code"));

        verify(operateService, times(4)).addClientRecord("oauth-client-1");
        verify(operateService).saveOAuthCode(eq(code), any());
        verify(operateService, times(2)).consumeOAuthCode(eq(code));
        verify(operateService, times(2)).saveOAuthRefreshToken(anyString(), any());
        verify(operateService).consumeOAuthRefreshToken(eq(refreshToken));
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

    private Map<String, Object> appRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("uuid", "assistant-openapi-001");
        row.put("name", "OpenAPI Agent");
        row.put("desc", "from api");
        return row;
    }

    private ApiKeyInfo apiKey(String keyId, String userId, String orgId, boolean status, String expiredAt) {
        ApiKeyInfo info = new ApiKeyInfo();
        info.setKeyId(keyId);
        info.setKey("wanwu-real-key");
        info.setUserId(userId);
        info.setOrgId(orgId);
        info.setName("Real Key");
        info.setStatus(status);
        info.setExpiredAt(expiredAt);
        return info;
    }

    private AppKeyInfo appKey(String apiId, String apiKey, String appId, String appType) {
        AppKeyInfo info = new AppKeyInfo();
        info.setApiId(apiId);
        info.setApiKey(apiKey);
        info.setUserId("dev-admin");
        info.setOrgId("default-org");
        info.setAppId(appId);
        info.setAppType(appType);
        return info;
    }

    private Map<String, Object> oauthPage() {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("list", Collections.singletonList(oauthApp()));
        page.put("total", 1);
        page.put("pageNo", 1);
        page.put("pageSize", 1000);
        return page;
    }

    private Map<String, Object> oauthApp() {
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("clientId", "oauth-client-1");
        app.put("name", "Console");
        app.put("desc", "dev oauth");
        app.put("clientSecret", "oauth-secret-1");
        app.put("redirectUri", "http://localhost/callback");
        app.put("status", true);
        return app;
    }
}
