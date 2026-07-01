package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
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
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuOpenApiControllerTest {

    private final AppService appService = mock(AppService.class);
    private final ModelService modelService = mock(ModelService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuOpenApiController(appService, modelService))
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
    public void modelKnowledgeUploadOauthAndMcpShellsDoNotReturnNotFound() throws Exception {
        when(modelService.listModels(any())).thenReturn(new ModelListResult(Collections.emptyList(), 0));

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
                .andExpect(jsonPath("$.data.knowledgeId").exists());

        mockMvc.perform(multipart("/service/api/openapi/v1/workflow/file/upload")
                        .file(new MockMultipartFile("file", "input.txt", "text/plain", "hello".getBytes()))
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId").exists());

        mockMvc.perform(get("/service/api/openapi/v1/oauth/jwks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray());

        mockMvc.perform(get("/service/api/openapi/v1/mcp/server/sse").param("apiKey", "app-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("notifications/initialized")));
    }

    private Map<String, Object> appRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("uuid", "assistant-openapi-001");
        row.put("name", "OpenAPI Agent");
        row.put("desc", "from api");
        return row;
    }
}
