package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConfigQuery;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationListQuery;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationQuery;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationStateCommand;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.mcp.McpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuGeneralAgentApiControllerTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private AppService appService;
    private McpService mcpService;
    private KnowledgeService knowledgeService;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        appService = mock(AppService.class);
        mcpService = mock(McpService.class);
        knowledgeService = mock(KnowledgeService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new WanwuGeneralAgentApiController(appService, mcpService, knowledgeService))
                .build();

        when(appService.listAssistants(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(assistant()), 1));
        final AtomicReference<Map<String, List<Map<String, Object>>>> generalAgentConfig =
                new AtomicReference<Map<String, List<Map<String, Object>>>>(
                        Collections.<String, List<Map<String, Object>>>emptyMap());
        when(appService.getGeneralAgentConfig(any(GeneralAgentConfigQuery.class)))
                .thenAnswer(invocation -> generalAgentConfig.get());
        when(appService.updateGeneralAgentConfig(any(GeneralAgentConfigUpdateCommand.class)))
                .thenAnswer(invocation -> {
                    GeneralAgentConfigUpdateCommand command = invocation.getArgument(0);
                    generalAgentConfig.set(command.getConfig());
                    return command.getConfig();
                });
        final AtomicReference<List<Map<String, Object>>> generalAgentConversations =
                new AtomicReference<List<Map<String, Object>>>(new ArrayList<Map<String, Object>>());
        when(appService.saveGeneralAgentConversationState(any(GeneralAgentConversationStateCommand.class)))
                .thenAnswer(invocation -> {
                    GeneralAgentConversationStateCommand command = invocation.getArgument(0);
                    Map<String, Object> state = generalAgentConversationState(command);
                    List<Map<String, Object>> states = new ArrayList<Map<String, Object>>();
                    boolean replaced = false;
                    for (Map<String, Object> item : generalAgentConversations.get()) {
                        if (command.getThreadId().equals(item.get("threadId"))) {
                            states.add(state);
                            replaced = true;
                        } else {
                            states.add(item);
                        }
                    }
                    if (!replaced) {
                        states.add(0, state);
                    }
                    generalAgentConversations.set(states);
                    return state;
                });
        when(appService.getGeneralAgentConversationState(any(GeneralAgentConversationQuery.class)))
                .thenAnswer(invocation -> {
                    GeneralAgentConversationQuery query = invocation.getArgument(0);
                    for (Map<String, Object> state : generalAgentConversations.get()) {
                        if (query.getThreadId() != null && query.getThreadId().equals(state.get("threadId"))) {
                            return state;
                        }
                        if (query.getPreviewId() != null && query.getPreviewId().equals(state.get("previewId"))) {
                            return state;
                        }
                    }
                    return Collections.<String, Object>emptyMap();
                });
        when(appService.listGeneralAgentConversationStates(any(GeneralAgentConversationListQuery.class)))
                .thenAnswer(invocation -> generalAgentConversations.get());
        org.mockito.Mockito.doAnswer(invocation -> {
            GeneralAgentConversationDeleteCommand command = invocation.getArgument(0);
            List<Map<String, Object>> states = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> item : generalAgentConversations.get()) {
                if (!command.getThreadId().equals(item.get("threadId"))) {
                    states.add(item);
                }
            }
            generalAgentConversations.set(states);
            return null;
        }).when(appService).deleteGeneralAgentConversationState(any(GeneralAgentConversationDeleteCommand.class));
        when(appService.listAssistantWorkflowSelect(anyString(), anyString(), anyString()))
                .thenReturn(listResult(Collections.singletonList(workflow())));
        when(mcpService.listToolSelect(anyString(), anyString(), anyString()))
                .thenReturn(listResult(Collections.singletonList(tool())));
        when(mcpService.listToolActions(anyString(), anyString(), eq("builtin-weather"), eq("builtin")))
                .thenReturn(singleton("actions", Collections.singletonList(action())));
        when(mcpService.listMcpSelect(anyString(), anyString(), anyString()))
                .thenReturn(listResult(Collections.singletonList(mcp())));
        when(mcpService.listSkillSelect(anyString(), anyString(), anyString(), isNull()))
                .thenReturn(listResult(Collections.singletonList(skill())));
        when(mcpService.listSkillSelect(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(listResult(Collections.singletonList(skill())));
        when(knowledgeService.selectKnowledge(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeList", Collections.singletonList(knowledge())));
    }

    @Test
    public void generalAgentRoutesReturnFrontendContracts() throws Exception {
        mockMvc.perform(get("/service/api/v1/general/agent/sub/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wgaAgentList", hasSize(4)))
                .andExpect(jsonPath("$.data.wgaAgentList[0].agentId").value("Supervisor Agent"))
                .andExpect(jsonPath("$.data.wgaAgentList[3].agentId").value("Skill Chat Agent"));

        mockMvc.perform(get("/service/api/v1/general/agent/upload/limit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadLimitList", hasSize(2)));

        mockMvc.perform(get("/service/api/v1/general/agent/tool/select")
                        .param("agentId", "General Agent")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].condition").value("optional"))
                .andExpect(jsonPath("$.data.list[0].toolList[0].toolId").value("builtin-weather"));

        mockMvc.perform(get("/service/api/v1/general/agent/tool/info")
                        .param("toolId", "builtin-weather")
                        .param("toolType", "builtin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actions[0].name").value("forecast"));

        mockMvc.perform(get("/service/api/v1/general/agent/resource/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].listType").value("assistant"))
                .andExpect(jsonPath("$.data[1].listType").value("mcp"))
                .andExpect(jsonPath("$.data[2].listType").value("workflow"))
                .andExpect(jsonPath("$.data[3].listType").value("skill"))
                .andExpect(jsonPath("$.data[4].listType").value("knowledge"));

        mockMvc.perform(put("/service/api/v1/general/agent/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tool\":[{\"toolId\":\"builtin-weather\",\"toolType\":\"builtin\"}],"
                                + "\"mcp\":[{\"id\":\"mcp-1\",\"type\":\"mcp\"}],"
                                + "\"ontology\":[{\"id\":\"ontology-1\",\"type\":\"ontology\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/service/api/v1/general/agent/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(6)))
                .andExpect(jsonPath("$.data[0].list[0].toolId").value("builtin-weather"))
                .andExpect(jsonPath("$.data[1].list[0].id").value("mcp-1"))
                .andExpect(jsonPath("$.data[?(@.listType=='ontology')]", hasSize(0)));

        MvcResult created = mockMvc.perform(post("/service/api/v1/general/agent/conversation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Smoke\",\"modelConfig\":{\"modelId\":\"model-1\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.threadId", containsString("wga-thread-")))
                .andReturn();
        String threadId = data(created).get("threadId").toString();

        mockMvc.perform(get("/service/api/v1/general/agent/conversation/list")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].threadId").value(threadId))
                .andExpect(jsonPath("$.data.list[0].isSkillConversation").value(false));

        mockMvc.perform(get("/service/api/v1/general/agent/conversation/config")
                        .param("threadId", threadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modelConfig.modelId").value("model-1"));

        mockMvc.perform(put("/service/api/v1/general/agent/conversation/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"threadId\":\"" + threadId + "\",\"modelConfig\":{\"modelId\":\"model-2\"}}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/service/api/v1/general/agent/conversation/config/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"threadId\":\"" + threadId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meet").value(true))
                .andExpect(jsonPath("$.data.modelMeet").value(true));

        MvcResult chat = mockMvc.perform(post("/service/api/v1/general/agent/conversation/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"threadId\":\"" + threadId + "\",\"agentId\":\"General Agent\","
                                + "\"messages\":[{\"id\":\"m1\",\"role\":\"user\",\"content\":\"hello\"}]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("RUN_STARTED")))
                .andExpect(content().string(containsString("TEXT_MESSAGE_CONTENT")))
                .andReturn();
        String runId = extractRunId(chat.getResponse().getContentAsString());

        mockMvc.perform(get("/service/api/v1/general/agent/conversation/detail")
                        .param("threadId", threadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].runId").value(runId))
                .andExpect(jsonPath("$.data.list[0].events[0].type").value("RUN_STARTED"));

        mockMvc.perform(get("/service/api/v1/general/agent/conversation/workspace")
                        .param("threadId", threadId)
                        .param("runId", runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileCount").value(2))
                .andExpect(jsonPath("$.data.files[0].name").value("answer.md"))
                .andExpect(jsonPath("$.data.files[1].name").value("metadata.json"));

        mockMvc.perform(get("/service/api/v1/general/agent/conversation/workspace/preview")
                        .param("threadId", threadId)
                        .param("runId", runId)
                        .param("path", "answer.md"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Wanwu General Agent")));

        mockMvc.perform(get("/service/api/v1/general/agent/conversation/workspace/download")
                        .param("threadId", threadId)
                        .param("runId", runId)
                        .param("path", "metadata.json"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("metadata.json")))
                .andExpect(content().string(containsString(runId)));

        MvcResult skillCreated = mockMvc.perform(post("/service/api/v1/general/agent/skill/conversation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Skill Smoke\",\"modelConfig\":{\"modelId\":\"model-1\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customSkillId", containsString("custom-skill-wga-")))
                .andExpect(jsonPath("$.data.previewId", containsString("wga-preview-")))
                .andReturn();
        Map<String, Object> skillData = data(skillCreated);
        String skillThreadId = skillData.get("threadId").toString();
        String previewId = skillData.get("previewId").toString();

        mockMvc.perform(post("/service/api/v1/general/agent/skill/conversation/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"threadId\":\"" + skillThreadId + "\",\"customSkillId\":\""
                                + skillData.get("customSkillId") + "\",\"messages\":[{\"role\":\"user\",\"content\":\"build\"}]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Skill Chat Agent")));

        mockMvc.perform(get("/service/api/v1/general/agent/skill/preview/conversation/detail")
                        .param("previewId", previewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].events[0].type").value("RUN_STARTED"));

        mockMvc.perform(post("/service/api/v1/general/agent/question/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"runId\":\"" + runId + "\",\"questionId\":\"q1\",\"answers\":[[\"yes\"]]}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/service/api/v1/general/agent/question/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"runId\":\"" + runId + "\",\"questionId\":\"q2\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/service/api/v1/general/agent/conversation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"threadId\":\"" + threadId + "\"}"))
                .andExpect(status().isOk());

        verify(appService).listAssistants(any(ApplicationListQuery.class));
        verify(mcpService).listToolSelect(anyString(), anyString(), anyString());
        verify(knowledgeService).selectKnowledge(anyString(), anyString(), any(Map.class));
    }

    private Map<String, Object> data(MvcResult result) throws Exception {
        Map response = JSON.readValue(result.getResponse().getContentAsString(), Map.class);
        return (Map<String, Object>) response.get("data");
    }

    private String extractRunId(String sse) throws Exception {
        for (String line : sse.split("\n")) {
            if (line.startsWith("data: ") && line.contains("RUN_STARTED")) {
                Map event = JSON.readValue(line.substring("data: ".length()), Map.class);
                return String.valueOf(event.get("runId"));
            }
        }
        throw new IllegalStateException("RUN_STARTED event not found");
    }

    private Map<String, Object> generalAgentConversationState(GeneralAgentConversationStateCommand command) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("threadId", command.getThreadId());
        state.put("title", command.getTitle());
        state.put("createdAt", command.getCreatedAt());
        state.put("updatedAt", command.getUpdatedAt());
        state.put("isSkillConversation", command.isSkillConversation());
        state.put("skillId", command.getSkillId());
        state.put("previewId", command.getPreviewId());
        state.put("modelConfig", command.getModelConfig());
        state.put("runs", command.getRuns());
        return state;
    }

    private Map<String, Object> assistant() {
        return map("appId", "assistant-1", "name", "Assistant One", "desc", "assistant", "avatar", avatar());
    }

    private Map<String, Object> workflow() {
        return map("workFlowId", "workflow-1", "name", "Workflow One", "desc", "workflow", "avatar", avatar());
    }

    private Map<String, Object> tool() {
        return map("uniqueId", "builtin_builtin-weather", "toolId", "builtin-weather", "toolName", "Weather",
                "toolType", "builtin", "desc", "Weather tool", "avatar", avatar());
    }

    private Map<String, Object> action() {
        return map("name", "forecast", "desc", "Forecast weather");
    }

    private Map<String, Object> mcp() {
        return map("mcpId", "mcp-1", "name", "MCP One", "type", "mcp", "desc", "mcp", "avatar", avatar());
    }

    private Map<String, Object> skill() {
        return map("skillId", "skill-1", "name", "Skill One", "type", "builtin", "desc", "skill",
                "avatar", avatar());
    }

    private Map<String, Object> knowledge() {
        return map("knowledgeId", "knowledge-1", "name", "Knowledge One", "desc", "knowledge", "avatar", avatar());
    }

    private Map<String, Object> avatar() {
        return singleton("path", "");
    }

    private Map<String, Object> listResult(List<Map<String, Object>> list) {
        return map("list", list, "total", list.size());
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }
}
