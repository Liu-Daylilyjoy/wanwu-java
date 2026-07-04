package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationPageResult;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordListResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuModelUseApiControllerTest {

    private final AppService appService = mock(AppService.class);
    private final ModelService modelService = mock(ModelService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuModelUseApiController(appService, modelService))
            .build();

    @Test
    public void legacyAssistantRoutesReturnFrontendContracts() throws Exception {
        Map<String, Object> assistant = new LinkedHashMap<String, Object>();
        assistant.put("assistantId", "assistant-001");
        assistant.put("name", "Legacy Assistant");
        assistant.put("desc", "from legacy MODEL_API");
        when(appService.createAssistant(any())).thenReturn(new AssistantCreateResult("assistant-001"));
        when(appService.listAssistants(any())).thenReturn(new ApplicationListResult(Collections.singletonList(assistant), 1));
        when(appService.getAssistantDraft(any())).thenReturn(assistant);
        when(appService.createAssistantConversation(any())).thenReturn(new AssistantConversationCreateResult("conv-001"));
        when(appService.listAssistantConversations(any())).thenReturn(page(row("conversationId", "conv-001", "name", "New Chat")));
        when(appService.listAssistantConversationDetails(any())).thenReturn(page(row("detailId", "detail-001", "content", "hello")));

        mockMvc.perform(post("/use/model/api/v1/assistant/create")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Legacy Assistant\",\"desc\":\"demo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.assistantId").value("assistant-001"));
        mockMvc.perform(get("/use/model/api/v1/assistant/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].assistantId").value("assistant-001"));
        mockMvc.perform(get("/use/model/api/v1/assistant/info")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Legacy Assistant"));
        mockMvc.perform(put("/use/model/api/v1/assistant/update")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"name\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/use/model/api/v1/assistant/publish")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(delete("/use/model/api/v1/assistant/delete")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/use/model/api/v1/assistant/conversation/create")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"prompt\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value("conv-001"));
        mockMvc.perform(get("/use/model/api/v1/assistant/conversation/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].conversationId").value("conv-001"));
        mockMvc.perform(get("/use/model/api/v1/assistant/conversation/detail")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-001")
                        .param("conversationId", "conv-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].detailId").value("detail-001"));

        verify(appService).createAssistant(any());
        verify(appService).updateAssistant(any());
        verify(appService).publishApp(any());
        verify(appService).deleteAssistant(any());
    }

    @Test
    public void chatLlmAndFileRoutesReturnDevelopmentContracts() throws Exception {
        when(appService.createAssistant(any())).thenReturn(new AssistantCreateResult("auto-assistant-001"));

        mockMvc.perform(post("/use/model/api/v1/chatllm/conversation/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId", containsString("chatllm-")));
        mockMvc.perform(get("/use/model/api/v1/chatllm/conversation/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].conversationId", containsString("chatllm-")));
        mockMvc.perform(get("/use/model/api/v1/chatllm/conversation/detail")
                        .param("conversationId", "chatllm-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());
        mockMvc.perform(delete("/use/model/api/v1/chatllm/conversation/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationId\":\"chatllm-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/use/model/api/v1/assistant/auto/create")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"build app\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assistantId").value("auto-assistant-001"))
                .andExpect(jsonPath("$.data.name").value("build app"));
        mockMvc.perform(post("/use/model/api/v1/file/confirmPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"demo.txt\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.confirmed").value(true));
        mockMvc.perform(multipart("/use/model/api/v1/file/batch/upload")
                        .file(new MockMultipartFile("file", "demo.txt", "text/plain",
                                "hello".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].fileName").value("demo.txt"));
        mockMvc.perform(multipart("/service/api/v1/model/expansion/file/batch/upload")
                        .file(new MockMultipartFile("file", "avatar.png", "image/png",
                                "img".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].fileName").value("avatar.png"));

        verify(appService).createAssistant(any());
    }

    @Test
    public void modelExperienceLegacyAliasesDelegateToModelService() throws Exception {
        ModelExperienceDialogInfo dialog = new ModelExperienceDialogInfo();
        dialog.setId("exp-001");
        dialog.setModelId("model-001");
        dialog.setSessionId("session-001");
        dialog.setTitle("Test chat");
        ModelExperienceDialogRecordInfo record = new ModelExperienceDialogRecordInfo();
        record.setModelExperienceId("exp-001");
        record.setSessionId("session-001");
        record.setOriginalContent("hello");
        record.setHandledContent("Echo: hello");
        record.setRole("assistant");
        when(modelService.saveModelExperienceDialog(any())).thenReturn(dialog);
        when(modelService.listModelExperienceDialogs(any()))
                .thenReturn(new ModelExperienceDialogListResult(Collections.singletonList(dialog), 1));
        when(modelService.listModelExperienceDialogRecords(any()))
                .thenReturn(new ModelExperienceDialogRecordListResult(Collections.singletonList(record), 1));

        mockMvc.perform(post("/use/model/api/v1/model/experience/dialog")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\",\"title\":\"Test chat\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("exp-001"));
        mockMvc.perform(get("/use/model/api/v1/model/experience/dialogs")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].id").value("exp-001"));
        mockMvc.perform(get("/use/model/api/v1/model/experience/dialog/records")
                        .header("Authorization", "Bearer dev-token")
                        .param("modelExperienceId", "exp-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].handledContent").value("Echo: hello"));
        mockMvc.perform(post("/use/model/api/v1/model/experience/file/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"demo.txt\",\"content\":\"hello file\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("hello file"));
        mockMvc.perform(delete("/use/model/api/v1/model/experience/dialog")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelExperienceId\":\"exp-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(modelService).saveModelExperienceDialog(any());
        verify(modelService).deleteModelExperienceDialog(any());
    }

    private AssistantConversationPageResult page(Map<String, Object> row) {
        return new AssistantConversationPageResult(Collections.singletonList(row), 1, 1, 10);
    }

    private Map<String, Object> row(String key1, String value1, String key2, String value2) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put(key1, value1);
        row.put(key2, value2);
        return row;
    }
}
