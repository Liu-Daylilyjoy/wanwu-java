package com.unicomai.wanwu.service.bff.web;

import com.jayway.jsonpath.JsonPath;
import com.unicomai.wanwu.api.app.AppService;
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
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuOpenApiController(
                    appService, modelService, knowledgeService, iamService, new OpenApiChatflowSessionStore()))
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
    public void chatRagAndWorkflowRoutesMapToExistingAppService() throws Exception {
        AssistantConversationStreamResult assistantResult = new AssistantConversationStreamResult();
        assistantResult.setConversationId("conversation-openapi-001");
        assistantResult.setDetailId("detail-openapi-001");
        assistantResult.setResponse("assistant answer");
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class)))
                .thenReturn(assistantResult);

        RagChatResult ragResult = new RagChatResult();
        ragResult.setResponse("rag answer");
        Map<String, Object> ragSearch = new LinkedHashMap<>();
        ragSearch.put("title", "PolicyGuide.txt");
        ragSearch.put("snippet", "Policy hit");
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
                .andExpect(jsonPath("$.finish").value(1));

        mockMvc.perform(post("/service/api/openapi/v1/rag/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"rag-openapi-001\",\"prompt\":\"search me\",\"file_info\":[{\"fileName\":\"note.txt\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.output").value("rag answer"))
                .andExpect(jsonPath("$.data.searchList[0].title").value("PolicyGuide.txt"));

        mockMvc.perform(post("/service/api/openapi/v1/workflow/run")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"workflow-openapi-001\",\"parameters\":{\"city\":\"Beijing\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.output.result").value("workflow answer"));

        verify(appService).streamAssistantConversation(any(AssistantConversationStreamCommand.class));
        ArgumentCaptor<RagChatCommand> ragCaptor = forClass(RagChatCommand.class);
        verify(appService).streamRagChat(ragCaptor.capture());
        assertEquals("rag-openapi-001", ragCaptor.getValue().getRagId());
        assertEquals("search me", ragCaptor.getValue().getQuestion());
        assertEquals(false, ragCaptor.getValue().isDraft());
        assertEquals(1, ragCaptor.getValue().getFileInfo().size());
        verify(appService).runWorkflow(any(WorkflowRunCommand.class));
    }

    @Test
    public void ragOpenApiStreamReturnsLegacySseWithSearchList() throws Exception {
        RagChatResult ragResult = new RagChatResult();
        ragResult.setRagId("rag-openapi-stream-001");
        ragResult.setResponse("stream rag answer");
        Map<String, Object> ragSearch = new LinkedHashMap<>();
        ragSearch.put("title", "PolicyGuide.txt");
        ragSearch.put("snippet", "Policy hit");
        ragResult.setSearchList(Collections.singletonList(ragSearch));
        when(appService.streamRagChat(any(RagChatCommand.class))).thenReturn(ragResult);

        mockMvc.perform(post("/service/api/openapi/v1/rag/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"rag-openapi-stream-001\",\"query\":\"PolicyGuide\",\"stream\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("data: {\"code\":0")))
                .andExpect(content().string(containsString("\"searchList\":[{\"title\":\"PolicyGuide.txt\"")))
                .andExpect(content().string(containsString("data: [DONE]")));
    }

    @Test
    public void agentConfigAndPublishRoutesUseAppService() throws Exception {
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
    public void chatflowOpenApiRoutesKeepLocalConversationState() throws Exception {
        String createdBody = mockMvc.perform(post("/service/api/openapi/v1/chatflow/conversation")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"chatflow-openapi-001\",\"conversation_name\":\"Policy chat\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversation_id").exists())
                .andExpect(jsonPath("$.data.conversation_name").value("Policy chat"))
                .andReturn().getResponse().getContentAsString();
        String conversationId = JsonPath.read(createdBody, "$.data.conversation_id");

        mockMvc.perform(post("/service/api/openapi/v1/chatflow/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"chatflow-openapi-001\",\"conversation_id\":\"" + conversationId + "\",\"query\":\"hello chatflow\",\"parameters\":{\"city\":\"Beijing\"}}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("hello chatflow")))
                .andExpect(content().string(containsString(conversationId)));

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
                        .header("Authorization", "Bearer dev-token"))
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
                .andExpect(jsonPath("$.data.fileId").exists());

        mockMvc.perform(get("/service/api/openapi/v1/oauth/jwks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"));

        mockMvc.perform(get("/service/api/openapi/v1/mcp/server/sse").param("apiKey", "app-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("notifications/initialized")));

        verify(knowledgeService).createKnowledge(eq("dev-admin"), eq("default-org"), any());
        verify(knowledgeService).selectKnowledge(eq("dev-admin"), eq("default-org"), any());
        verify(knowledgeService).getDocConfig(eq("dev-admin"), eq("default-org"), any());
        verify(knowledgeService).importDocs(eq("dev-admin"), eq("default-org"), any());
        verify(knowledgeService).hitKnowledge(eq("dev-admin"), eq("default-org"), any());
    }

    @Test
    public void oauthAuthorizationCodeFlowUsesManagedOauthApp() throws Exception {
        when(iamService.listOauthApps(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(oauthPage());

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
    }

    private Map<String, Object> appRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("uuid", "assistant-openapi-001");
        row.put("name", "OpenAPI Agent");
        row.put("desc", "from api");
        return row;
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
