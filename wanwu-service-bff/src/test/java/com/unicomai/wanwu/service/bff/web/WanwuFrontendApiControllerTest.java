package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCopyCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationPageResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantPublishedQuery;
import com.unicomai.wanwu.api.app.dto.AssistantResourceCommand;
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlCreateCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlInfo;
import com.unicomai.wanwu.api.app.dto.AppUrlListQuery;
import com.unicomai.wanwu.api.app.dto.AppUrlStatusCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.AppVersionRollbackCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyCreateCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyDeleteCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.ApiKeyListQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyPageResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatusCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyCreateCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyInfo;
import com.unicomai.wanwu.api.app.dto.AppKeyListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.RagConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RagCopyCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateResult;
import com.unicomai.wanwu.api.app.dto.RagDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RagUpdateCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCopyCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateResult;
import com.unicomai.wanwu.api.app.dto.WorkflowDeleteCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowExportQuery;
import com.unicomai.wanwu.api.app.dto.WorkflowExportResult;
import com.unicomai.wanwu.api.app.dto.WorkflowImportCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationOption;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.mcp.McpService;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordListResult;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.model.dto.ModelTypeInfo;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeInfo;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeResult;
import com.unicomai.wanwu.api.model.dto.RecommendModelInfo;
import com.unicomai.wanwu.api.model.dto.RecommendModelResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuFrontendApiControllerTest {

    private final IamService iamService = mock(IamService.class);
    private final AppService appService = mock(AppService.class);
    private final ModelService modelService = mock(ModelService.class);
    private final KnowledgeService knowledgeService = mock(KnowledgeService.class);
    private final McpService mcpService = mock(McpService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(
                    new WanwuFrontendApiController(iamService, appService, modelService, knowledgeService, mcpService),
                    new WanwuResourceApiController(mcpService),
                    new WanwuSkillApiController(mcpService))
            .build();

    @Test
    public void captchaReturnsFrontendContract() throws Exception {
        when(iamService.captcha()).thenReturn(new CaptchaResult("dev-captcha", "data:image/svg+xml;base64,MTIzNA=="));

        mockMvc.perform(get("/user/api/v1/base/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.key").value("dev-captcha"))
                .andExpect(jsonPath("$.data.b64").value("data:image/svg+xml;base64,MTIzNA=="));
    }

    @Test
    public void loginReturnsSessionAndPermissionsForFrontend() throws Exception {
        when(iamService.login(any(LoginCommand.class))).thenReturn(devAdminResult());

        mockMvc.perform(post("/user/api/v1/base/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"encrypted\",\"key\":\"dev-captcha\",\"code\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.uid").value("dev-admin"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.token").value("dev-token"))
                .andExpect(jsonPath("$.data.isUpdatePassword").value(true))
                .andExpect(jsonPath("$.data.orgPermission.org.id").value("default-org"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[0].perm").value("permission"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[1].perm").value("permission.user"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[2].perm").value("permission.org"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[3].perm").value("permission.role"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[4].perm").value("model"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[5].perm").value("model.model_management"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[6].perm").value("app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[7].perm").value("app.rag"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[8].perm").value("app.workflow"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[9].perm").value("app.agent"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[10].perm").value("api_key"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[11].perm").value("api_key.api_key_management"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[12].perm").value("resource"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[13].perm").value("resource.knowledge"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[14].perm").value("resource.tool"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[15].perm").value("resource.mcp"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[16].perm").value("resource.prompt"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[17].perm").value("resource.skill"))
                .andExpect(jsonPath("$.data.custom.loginEmail.email.status").value(false));

        verify(iamService).login(any(LoginCommand.class));
    }

    @Test
    public void assistantListReturnsEmptyFrontendList() throws Exception {
        when(appService.listAssistants(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.emptyList()));

        mockMvc.perform(get("/user/api/v1/appspace/assistant/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.list", hasSize(0)));

        verify(appService).listAssistants(any(ApplicationListQuery.class));
    }

    @Test
    public void appspaceAppListReturnsGenericApplicationCards() throws Exception {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("appId", "assistant-001");
        item.put("appType", "agent");
        item.put("name", "AgentOne");
        item.put("publishType", "public");
        item.put("version", "v1.0.0");
        when(appService.listApplications(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(item)));

        mockMvc.perform(get("/user/api/v1/appspace/app/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("appType", "agent")
                        .param("name", "Agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].appId").value("assistant-001"))
                .andExpect(jsonPath("$.data.list[0].version").value("v1.0.0"))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(appService).listApplications(any(ApplicationListQuery.class));
    }

    @Test
    public void workflowAppspaceRoutesReturnFrontendContractsAndMapCommands() throws Exception {
        Map<String, Object> workflowCard = new LinkedHashMap<>();
        workflowCard.put("appId", "workflow-001");
        workflowCard.put("workflowId", "workflow-001");
        workflowCard.put("workflow_id", "workflow-001");
        workflowCard.put("appType", "workflow");
        workflowCard.put("name", "PolicyFlow");
        workflowCard.put("desc", "policy workflow");
        workflowCard.put("publishType", "private");
        workflowCard.put("version", "");
        when(appService.createWorkflow(any(WorkflowCreateCommand.class)))
                .thenReturn(new WorkflowCreateResult("workflow-001"));
        when(appService.listApplications(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(workflowCard), 1));
        when(appService.copyWorkflow(any(WorkflowCopyCommand.class)))
                .thenReturn(new WorkflowCreateResult("workflow-002"));
        when(appService.exportWorkflow(any(WorkflowExportQuery.class)))
                .thenReturn(new WorkflowExportResult("PolicyFlow", "policy workflow", "{\"nodes\":[]}"));
        when(appService.importWorkflow(any(WorkflowImportCommand.class)))
                .thenReturn(new WorkflowCreateResult("workflow-003"));
        when(appService.runWorkflow(any(WorkflowRunCommand.class)))
                .thenReturn(new WorkflowRunResult("workflow-001", Collections.singletonMap("answer", "ok")));

        mockMvc.perform(post("/user/api/v1/appspace/workflow")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"PolicyFlow\",\"desc\":\"policy workflow\",\"avatar\":{\"key\":\"avatar-key\",\"path\":\"/avatar.png\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-001"))
                .andExpect(jsonPath("$.data.workflowId").value("workflow-001"));

        org.mockito.ArgumentCaptor<WorkflowCreateCommand> createCaptor = forClass(WorkflowCreateCommand.class);
        verify(appService).createWorkflow(createCaptor.capture());
        assertEquals("PolicyFlow", createCaptor.getValue().getName());
        assertEquals("policy workflow", createCaptor.getValue().getDesc());
        assertEquals("avatar-key", createCaptor.getValue().getAvatarKey());
        assertEquals("/avatar.png", createCaptor.getValue().getAvatarPath());
        assertEquals("dev-admin", createCaptor.getValue().getUserId());
        assertEquals("default-org", createCaptor.getValue().getOrgId());

        mockMvc.perform(get("/user/api/v1/appspace/workflow/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("name", "Policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].appType").value("workflow"))
                .andExpect(jsonPath("$.data.list[0].workflow_id").value("workflow-001"))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(post("/user/api/v1/appspace/workflow/copy")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflow_id\":\"workflow-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-002"));

        org.mockito.ArgumentCaptor<WorkflowCopyCommand> copyCaptor = forClass(WorkflowCopyCommand.class);
        verify(appService).copyWorkflow(copyCaptor.capture());
        assertEquals("workflow-001", copyCaptor.getValue().getWorkflowId());
        assertEquals("dev-admin", copyCaptor.getValue().getUserId());
        assertEquals("default-org", copyCaptor.getValue().getOrgId());

        mockMvc.perform(get("/user/api/v1/appspace/workflow/export/draft")
                        .header("Authorization", "Bearer dev-token")
                        .param("workflow_id", "workflow-001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"name\":\"PolicyFlow\"")))
                .andExpect(content().string(containsString("\"schema\":\"{\\\"nodes\\\":[]}\"")));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "workflow.json",
                "application/json",
                "{\"name\":\"ImportedFlow\",\"desc\":\"imported\",\"schema\":\"{\\\"nodes\\\":[]}\"}"
                        .getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/user/api/v1/appspace/workflow/import")
                        .file(file)
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-003"));

        org.mockito.ArgumentCaptor<WorkflowImportCommand> importCaptor = forClass(WorkflowImportCommand.class);
        verify(appService).importWorkflow(importCaptor.capture());
        assertEquals("ImportedFlow", importCaptor.getValue().getName());
        assertEquals("imported", importCaptor.getValue().getDesc());
        assertEquals("{\"nodes\":[]}", importCaptor.getValue().getSchema());
        assertEquals("dev-admin", importCaptor.getValue().getUserId());
        assertEquals("default-org", importCaptor.getValue().getOrgId());

        mockMvc.perform(post("/user/api/v1/workflow/run")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflow_id\":\"workflow-001\",\"input\":{\"question\":\"hello\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-001"))
                .andExpect(jsonPath("$.data.output.answer").value("ok"));

        mockMvc.perform(delete("/user/api/v1/appspace/app")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"workflow-001\",\"appType\":\"workflow\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        org.mockito.ArgumentCaptor<WorkflowDeleteCommand> deleteCaptor = forClass(WorkflowDeleteCommand.class);
        verify(appService).deleteWorkflow(deleteCaptor.capture());
        assertEquals("workflow-001", deleteCaptor.getValue().getWorkflowId());
        assertEquals("dev-admin", deleteCaptor.getValue().getUserId());
        assertEquals("default-org", deleteCaptor.getValue().getOrgId());
    }

    @Test
    public void resourceToolMcpPromptRoutesReturnFrontendContracts() throws Exception {
        Map<String, Object> createdTool = Collections.<String, Object>singletonMap("customToolId", "tool-001");
        Map<String, Object> customTool = new LinkedHashMap<>();
        customTool.put("customToolId", "tool-001");
        customTool.put("name", "WeatherAPI");
        customTool.put("description", "weather lookup");
        customTool.put("avatar", Collections.emptyMap());
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("name", "get_weather");
        action.put("description", "get weather");
        action.put("inputSchema", Collections.singletonMap("properties", Collections.emptyMap()));
        Map<String, Object> actions = Collections.<String, Object>singletonMap("actions",
                Collections.singletonList(action));
        when(mcpService.createCustomTool(anyString(), anyString(), any(Map.class))).thenReturn(createdTool);
        when(mcpService.listCustomTools(anyString(), anyString(), anyString()))
                .thenReturn(listResult(customTool));
        when(mcpService.listToolSelect(anyString(), anyString(), anyString()))
                .thenReturn(listResult(customTool));
        when(mcpService.listToolActions(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(actions);
        when(mcpService.createMcpServer(anyString(), anyString(), any(Map.class)))
                .thenReturn(Collections.<String, Object>singletonMap("mcpServerId", "mcpserver-001"));
        Map<String, Object> mcpServer = new LinkedHashMap<>();
        mcpServer.put("mcpServerId", "mcpserver-001");
        mcpServer.put("name", "Local MCP Server");
        mcpServer.put("toolNum", 0);
        when(mcpService.listMcpServers(anyString(), anyString(),
                org.mockito.ArgumentMatchers.<String>nullable(String.class)))
                .thenReturn(listResult(mcpServer));
        when(mcpService.createCustomPrompt(anyString(), anyString(), any(Map.class)))
                .thenReturn(Collections.<String, Object>singletonMap("customPromptId", "prompt-001"));
        Map<String, Object> prompt = new LinkedHashMap<>();
        prompt.put("customPromptId", "prompt-001");
        prompt.put("name", "ReviewPrompt");
        prompt.put("prompt", "review this");
        when(mcpService.listCustomPrompts(anyString(), anyString(),
                org.mockito.ArgumentMatchers.<String>nullable(String.class)))
                .thenReturn(listResult(prompt));
        Map<String, Object> optimizePayload = new LinkedHashMap<>();
        optimizePayload.put("response", "optimized prompt");
        optimizePayload.put("finish", 1);
        when(mcpService.optimizePrompt(anyString(), anyString(), any(Map.class))).thenReturn(optimizePayload);

        mockMvc.perform(post("/user/api/v1/tool/custom")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"WeatherAPI\",\"description\":\"weather lookup\",\"schema\":\"{}\",\"apiAuth\":{\"authType\":\"none\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.customToolId").value("tool-001"));

        mockMvc.perform(get("/user/api/v1/tool/custom/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("name", "Weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].customToolId").value("tool-001"));

        mockMvc.perform(get("/user/api/v1/tool/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].name").value("WeatherAPI"));

        mockMvc.perform(get("/user/api/v1/tool/action/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("toolId", "tool-001")
                        .param("toolType", "custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actions[0].name").value("get_weather"));

        mockMvc.perform(post("/user/api/v1/mcp/server")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Local MCP Server\",\"desc\":\"local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mcpServerId").value("mcpserver-001"));

        mockMvc.perform(get("/user/api/v1/mcp/server/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].mcpServerId").value("mcpserver-001"));

        mockMvc.perform(post("/user/api/v1/prompt/custom")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ReviewPrompt\",\"desc\":\"review\",\"prompt\":\"review this\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customPromptId").value("prompt-001"));

        mockMvc.perform(get("/user/api/v1/prompt/custom/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].customPromptId").value("prompt-001"));

        mockMvc.perform(post("/user/api/v1/prompt/optimize")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-001\",\"prompt\":\"review this\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"response\":\"optimized prompt\"")))
                .andExpect(content().string(containsString("\"finish\":1")));
    }

    @Test
    public void resourceSkillRoutesReturnFrontendContracts() throws Exception {
        when(mcpService.checkCustomSkill(anyString(), anyString(), any(Map.class)))
                .thenReturn(map("name", "Imported Skill", "desc", "imported desc"));
        when(mcpService.createCustomSkill(anyString(), anyString(), any(Map.class)))
                .thenReturn(Collections.<String, Object>singletonMap("skillId", "skill-custom-001"));
        Map<String, Object> skill = skill("skill-custom-001", "Imported Skill", "custom");
        when(mcpService.listCustomSkills(anyString(), anyString(), nullable(String.class)))
                .thenReturn(listResult(skill));
        when(mcpService.getCustomSkill(anyString(), anyString(), anyString()))
                .thenReturn(skill);
        when(mcpService.createCustomSkillConfig(anyString(), anyString(), any(Map.class)))
                .thenReturn(Collections.<String, Object>singletonMap("id", "var-001"));
        when(mcpService.listSkillSelect(anyString(), anyString(), nullable(String.class), nullable(String.class)))
                .thenReturn(listResult(skillSelect("skill-custom-001", "Imported Skill", "custom")));

        Map<String, Object> builtin = skill("builtin-summary", "Summary Skill", "builtin");
        builtin.put("skillMarkdown", "# Summary Skill");
        when(mcpService.listBuiltinSkills(anyString(), anyString(), nullable(String.class)))
                .thenReturn(listResult(builtin));
        when(mcpService.getBuiltinSkill(anyString(), anyString(), anyString()))
                .thenReturn(builtin);
        when(mcpService.downloadBuiltinSkill(anyString(), anyString(), anyString()))
                .thenReturn("builtin-zip".getBytes(StandardCharsets.UTF_8));

        Map<String, Object> acquired = skill("acquired-001", "Summary Skill", "acquired");
        acquired.put("squareSkillId", "builtin-summary");
        when(mcpService.listAcquiredSkills(anyString(), anyString(), nullable(String.class)))
                .thenReturn(listResult(acquired));
        when(mcpService.getAcquiredSkill(anyString(), anyString(), anyString()))
                .thenReturn(acquired);
        when(mcpService.createAcquiredSkillConfig(anyString(), anyString(), any(Map.class)))
                .thenReturn(Collections.<String, Object>singletonMap("id", "var-002"));

        Map<String, Object> square = skill("builtin-summary", "Summary Skill", "square");
        square.put("isShared", false);
        square.put("skillMarkdown", "# Summary Skill");
        when(mcpService.listSquareSkills(anyString(), anyString(), nullable(String.class)))
                .thenReturn(listResult(square));
        when(mcpService.listSquareBuiltinSkills(anyString(), anyString(), nullable(String.class)))
                .thenReturn(listResult(square));
        when(mcpService.getSquareSkill(anyString(), anyString(), anyString()))
                .thenReturn(square);
        when(mcpService.downloadSquareSkill(anyString(), anyString(), anyString()))
                .thenReturn("square-zip".getBytes(StandardCharsets.UTF_8));

        when(mcpService.createSkillConversation(anyString(), anyString(), any(Map.class)))
                .thenReturn(Collections.<String, Object>singletonMap("conversationId", "skill-conv-001"));
        when(mcpService.listSkillConversations(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(listResult(map("conversationId", "skill-conv-001", "title", "Build skill")));
        when(mcpService.getSkillConversationDetail(anyString(), anyString(), anyString()))
                .thenReturn(listResult(map("role", "assistant", "content", "Ready")));
        when(mcpService.chatSkillConversation(anyString(), anyString(), any(Map.class)))
                .thenReturn(map("response", "generated skill", "finish", 1,
                        "responseFiles", Collections.singletonList(map("skillSaveId", "save-001"))));
        when(mcpService.saveSkillConversation(anyString(), anyString(), any(Map.class)))
                .thenReturn(Collections.<String, Object>singletonMap("skillId", "skill-custom-002"));

        mockMvc.perform(post("/user/api/v1/agent/skill/custom/check")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zipUrl\":\"file-upload/skill.zip\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Imported Skill"));
        mockMvc.perform(post("/user/api/v1/agent/skill/custom")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zipUrl\":\"file-upload/skill.zip\",\"author\":\"Wanwu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skillId").value("skill-custom-001"));
        mockMvc.perform(get("/user/api/v1/agent/skill/custom/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].skillId").value("skill-custom-001"));
        mockMvc.perform(get("/user/api/v1/agent/skill/custom/detail")
                        .header("Authorization", "Bearer dev-token")
                        .param("skillId", "skill-custom-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Imported Skill"));
        mockMvc.perform(post("/user/api/v1/agent/skill/custom/config")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":\"skill-custom-001\",\"variable\":{\"name\":\"API Key\",\"variableKey\":\"apiKey\",\"variableValue\":\"dev\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("var-001"));
        mockMvc.perform(get("/user/api/v1/agent/skill/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].skillType").value("custom"));

        mockMvc.perform(get("/user/api/v1/agent/skill/builtin/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].skillId").value("builtin-summary"));
        mockMvc.perform(get("/user/api/v1/agent/skill/builtin/detail")
                        .header("Authorization", "Bearer dev-token")
                        .param("skillId", "builtin-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skillMarkdown").value("# Summary Skill"));
        mockMvc.perform(get("/user/api/v1/builtin/skill/download")
                        .header("Authorization", "Bearer dev-token")
                        .param("skillId", "builtin-summary"))
                .andExpect(status().isOk())
                .andExpect(content().string("builtin-zip"));

        mockMvc.perform(get("/user/api/v1/square/skill/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].skillId").value("builtin-summary"));
        mockMvc.perform(post("/user/api/v1/square/skill/share")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":\"builtin-summary\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/user/api/v1/agent/acquired/skill/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].skillId").value("acquired-001"));
        mockMvc.perform(post("/user/api/v1/agent/acquired/skill/config")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":\"acquired-001\",\"variable\":{\"name\":\"Token\",\"variableKey\":\"token\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("var-002"));

        mockMvc.perform(post("/user/api/v1/agent/skill/conversation")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Build skill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value("skill-conv-001"));
        mockMvc.perform(get("/user/api/v1/agent/skill/conversation/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].conversationId").value("skill-conv-001"));
        mockMvc.perform(post("/user/api/v1/agent/skill/conversation/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationId\":\"skill-conv-001\",\"query\":\"build one\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"response\":\"generated skill\"")))
                .andExpect(content().string(containsString("\"finish\":1")));
        mockMvc.perform(post("/user/api/v1/agent/skill/conversation/save")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationId\":\"skill-conv-001\",\"skillSaveId\":\"save-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skillId").value("skill-custom-002"));
    }

    @Test
    public void workflowApiAndBotUploadRoutesReturnFrontendContracts() throws Exception {
        MockMvc workflowApiMvc = MockMvcBuilders
                .standaloneSetup(new WanwuWorkflowApiController(appService))
                .build();
        when(appService.exportWorkflow(any(WorkflowExportQuery.class)))
                .thenReturn(new WorkflowExportResult("PolicyFlow", "policy workflow", "{\"nodes\":[]}"));
        when(appService.runWorkflow(any(WorkflowRunCommand.class)))
                .thenReturn(new WorkflowRunResult("workflow-001", Collections.singletonMap("answer", "ok")));

        workflowApiMvc.perform(get("/workflow/api/workflow/parameter")
                        .header("x-user-id", "dev-admin")
                        .header("x-org-id", "default-org")
                        .param("workflowID", "workflow-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-001"))
                .andExpect(jsonPath("$.data.schema").value("{\"nodes\":[]}"));

        workflowApiMvc.perform(get("/workflow/api/workflow/openapi_schema")
                        .header("x-user-id", "dev-admin")
                        .header("x-org-id", "default-org")
                        .param("workflowID", "workflow-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.workflowId").value("workflow-001"))
                .andExpect(jsonPath("$.data.base64OpenAPISchema").exists())
                .andExpect(jsonPath("$.data.openAPISchema", containsString("\"openapi\":\"3.0.1\"")));

        workflowApiMvc.perform(post("/workflow/api/api/workflow/use")
                        .header("x-user-id", "dev-admin")
                        .header("x-org-id", "default-org")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflowID\":\"workflow-001\",\"parameters\":{\"question\":\"hello\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-001"))
                .andExpect(jsonPath("$.data.output.answer").value("ok"));

        org.mockito.ArgumentCaptor<WorkflowRunCommand> runCaptor = forClass(WorkflowRunCommand.class);
        verify(appService).runWorkflow(runCaptor.capture());
        assertEquals("workflow-001", runCaptor.getValue().getWorkflowId());
        assertEquals("hello", runCaptor.getValue().getInput().get("question"));
        assertEquals("dev-admin", runCaptor.getValue().getUserId());
        assertEquals("default-org", runCaptor.getValue().getOrgId());

        MockMvc uploadMvc = MockMvcBuilders
                .standaloneSetup(new WanwuBotUploadController())
                .build();
        uploadMvc.perform(post("/api/bot/upload_file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"file_head\":{\"file_type\":\"png\",\"biz_type\":6},\"data\":\"aGVsbG8=\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.upload_uri", containsString("memory://bot/")))
                .andExpect(jsonPath("$.data.upload_url").value("data:image/png;base64,aGVsbG8="));
    }

    @Test
    public void permissionManagementReadRoutesReturnFrontendContracts() throws Exception {
        when(iamService.listUsers(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(page(userInfo("dev-admin", "admin"), 1, 1, 10));
        when(iamService.selectRoles(anyString()))
                .thenReturn(select(idName("admin", "System Admin")));
        when(iamService.listRoles(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(page(roleInfo("admin", "System Admin"), 1, 1, 10));
        when(iamService.roleTemplate(anyString(), anyString()))
                .thenReturn(roleTemplate());
        when(iamService.listOrganizations(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(page(orgInfo("default-org", "Default Organization"), 1, 1, 10));

        mockMvc.perform(get("/user/api/v1/user/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].userId").value("dev-admin"))
                .andExpect(jsonPath("$.data.list[0].username").value("admin"))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/user/api/v1/role/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.select[0].id").value("admin"));

        mockMvc.perform(get("/user/api/v1/role/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].roleId").value("admin"))
                .andExpect(jsonPath("$.data.list[0].permissions[0].perm").value("permission"));

        mockMvc.perform(get("/user/api/v1/role/template")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.routes[0].perm").value("permission"))
                .andExpect(jsonPath("$.data.routes[0].children", hasSize(3)));

        mockMvc.perform(get("/user/api/v1/org/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].orgId").value("default-org"));
    }

    @Test
    public void modelManagementRoutesReturnFrontendContracts() throws Exception {
        when(modelService.listModels(any()))
                .thenReturn(new ModelListResult(Collections.singletonList(modelInfo("model-001", "DeepSeek Chat", "llm")), 1));
        when(modelService.getModel(anyString(), anyString(), anyString()))
                .thenReturn(modelInfo("model-001", "DeepSeek Chat", "llm"));
        when(modelService.listTypeModels(any()))
                .thenReturn(new ModelListResult(Collections.singletonList(modelInfo("model-001", "DeepSeek Chat", "llm")), 1));
        when(modelService.listImportProviders(any()))
                .thenReturn(new ProviderModelTypeResult(Collections.singletonList(providerInfo()), 1));
        when(modelService.recommendModels(any()))
                .thenReturn(new RecommendModelResult(Collections.singletonList(recommendInfo()), 1));

        mockMvc.perform(get("/user/api/v1/model/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("modelType", "llm")
                        .param("provider", "DeepSeek"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].modelId").value("model-001"))
                .andExpect(jsonPath("$.data.list[0].displayName").value("DeepSeek Chat"))
                .andExpect(jsonPath("$.data.list[0].isActive").value(true))
                .andExpect(jsonPath("$.data.list[0].allowEdit").value(true))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/user/api/v1/model")
                        .header("Authorization", "Bearer dev-token")
                        .param("modelId", "model-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.config.apiKey").value("dev-model-key"));

        mockMvc.perform(get("/user/api/v1/model/select/llm")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].modelId").value("model-001"));

        mockMvc.perform(get("/user/api/v1/model/import/providers")
                        .header("Authorization", "Bearer dev-token")
                        .param("modelType", "llm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].key").value("DeepSeek"))
                .andExpect(jsonPath("$.data.list[0].children[0].key").value("llm"));

        mockMvc.perform(get("/user/api/v1/model/recommend")
                        .header("Authorization", "Bearer dev-token")
                        .param("provider", "DeepSeek")
                        .param("modelType", "llm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].model").value("deepseek-chat"));

        mockMvc.perform(put("/user/api/v1/model/status")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-001\",\"isActive\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(modelService).listModels(any());
        verify(modelService).getModel("dev-admin", "default-org", "model-001");
        verify(modelService).listTypeModels(any());
        verify(modelService).listImportProviders(any());
        verify(modelService).recommendModels(any());
        verify(modelService).changeModelStatus(any());
    }

    @Test
    public void modelExperienceRoutesReturnFrontendContracts() throws Exception {
        when(modelService.saveModelExperienceDialog(any()))
                .thenReturn(modelExperienceDialog("exp-001", "model-001", "session-001", "hello"));
        when(modelService.listModelExperienceDialogs(any()))
                .thenReturn(new ModelExperienceDialogListResult(
                        Collections.singletonList(modelExperienceDialog("exp-001", "model-001", "session-001", "hello")), 1));
        when(modelService.listModelExperienceDialogRecords(any()))
                .thenReturn(new ModelExperienceDialogRecordListResult(java.util.Arrays.asList(
                        modelExperienceRecord("exp-001", "model-001", "session-001", "hello", "", "user"),
                        modelExperienceRecord("exp-001", "model-001", "session-001", "Echo: hello", "thinking", "assistant")
                ), 2));
        when(modelService.getModel(anyString(), anyString(), anyString()))
                .thenReturn(modelInfo("model-001", "DeepSeek Chat", "llm"));

        mockMvc.perform(post("/user/api/v1/model/experience/dialog")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\",\"title\":\"hello\",\"modelSetting\":{\"temperature\":0.7}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("exp-001"))
                .andExpect(jsonPath("$.data.modelSetting").value("{\"temperature\":0.7}"));

        mockMvc.perform(get("/user/api/v1/model/experience/dialogs")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("exp-001"))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/user/api/v1/model/experience/dialog/records")
                        .header("Authorization", "Bearer dev-token")
                        .param("modelExperienceId", "exp-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].role").value("user"))
                .andExpect(jsonPath("$.data.list[1].role").value("assistant"))
                .andExpect(jsonPath("$.data.list[1].reasoningContent").value("thinking"));

        mockMvc.perform(post("/user/api/v1/model/experience/llm")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\",\"modelExperienceId\":\"exp-001\",\"content\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("data:")))
                .andExpect(content().string(containsString("\"content\":\"Echo: hello\"")))
                .andExpect(content().string(containsString("\"finish_reason\":\"stop\"")));

        mockMvc.perform(delete("/user/api/v1/model/experience/dialog")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelExperienceId\":\"exp-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(modelService).saveModelExperienceDialog(any());
        verify(modelService).listModelExperienceDialogs(any());
        verify(modelService).listModelExperienceDialogRecords(any());
        verify(modelService).deleteModelExperienceDialog(any());
        verify(modelService, times(2)).saveModelExperienceDialogRecord(any());
    }

    @Test
    public void knowledgeRoutesReturnFrontendContracts() throws Exception {
        when(knowledgeService.selectKnowledge(anyString(), anyString(), any(Map.class)))
                .thenReturn(knowledgeList(knowledgeItem("knowledge-001", "Dev KB", 0)));
        when(knowledgeService.createKnowledge(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeId", "knowledge-001"));
        when(knowledgeService.listTags(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeTagList", Collections.singletonList(tag("tag-001", "Backend", true))));
        when(knowledgeService.countTagBindings(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("tagBindCount", 1));
        when(knowledgeService.listSplitters(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeSplitterList", Collections.singletonList(splitter("splitter-001", "paragraph", "\n\n", "preset"))));
        when(knowledgeService.listDocs(anyString(), anyString(), any(Map.class)))
                .thenReturn(docPage("knowledge-001", "Dev KB"));
        when(knowledgeService.getDocConfig(anyString(), anyString(), any(Map.class)))
                .thenReturn(docConfig());
        when(knowledgeService.getDocImportTip(anyString(), anyString(), any(Map.class)))
                .thenReturn(docImportTip("knowledge-001", "Dev KB"));
        when(knowledgeService.getDocUploadLimit(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("uploadLimitList", Collections.singletonList(uploadLimit("document", 50, "txt", "pdf", "docx"))));
        when(knowledgeService.analyzeDocUrls(anyString(), anyString(), any(Map.class)))
                .thenReturn(urlAnalysis("https://example.com/files/guide.txt", "guide.txt"));
        when(knowledgeService.listDocSegments(anyString(), anyString(), any(Map.class)))
                .thenReturn(segmentPage("Guide.txt", segment("segment-001", "Guide content", true)));
        when(knowledgeService.listKnowledgeUsers(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeUserInfoList", Collections.singletonList(knowledgeUser("perm-001", "dev-admin", 20))));
        when(knowledgeService.listKnowledgeOrgs(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowOrgInfoList", Collections.singletonList(orgInfoBrief("default-org", "Default Organization"))));
        when(knowledgeService.listUsersWithoutPermit(anyString(), anyString(), any(Map.class)))
                .thenReturn(orgUsers("default-org", "Default Organization"));

        mockMvc.perform(post("/user/api/v1/knowledge/select")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dev\",\"tagId\":[\"tag-001\"],\"category\":0,\"external\":-1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.knowledgeList[0].knowledgeId").value("knowledge-001"))
                .andExpect(jsonPath("$.data.knowledgeList[0].permissionType").value(20))
                .andExpect(jsonPath("$.data.knowledgeList[0].embeddingModelInfo.modelId").value("2"));

        mockMvc.perform(post("/user/api/v1/knowledge")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dev KB\",\"description\":\"docs\",\"embeddingModelInfo\":{\"modelId\":\"2\"},\"knowledgeGraph\":{\"switch\":false},\"category\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeId").value("knowledge-001"));

        mockMvc.perform(put("/user/api/v1/knowledge")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"name\":\"Dev KB 2\",\"description\":\"updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/knowledge/tag")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001")
                        .param("tagName", "Back"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeTagList[0].tagId").value("tag-001"))
                .andExpect(jsonPath("$.data.knowledgeTagList[0].selected").value(true));

        mockMvc.perform(post("/user/api/v1/knowledge/tag")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tagName\":\"Backend\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/tag/bind")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"tagIdList\":[\"tag-001\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/knowledge/tag/bind/count")
                        .header("Authorization", "Bearer dev-token")
                        .param("tagId", "tag-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tagBindCount").value(1));

        mockMvc.perform(get("/user/api/v1/knowledge/splitter")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeSplitterList[0].splitterId").value("splitter-001"));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"pageNo\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list", hasSize(0)))
                .andExpect(jsonPath("$.data.docKnowledgeInfo.knowledgeName").value("Dev KB"));

        mockMvc.perform(get("/user/api/v1/knowledge/doc/config")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.docSegment.segmentMethod").value("0"));

        mockMvc.perform(get("/user/api/v1/knowledge/doc/import/tip")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadstatus").value(2));

        mockMvc.perform(get("/user/api/v1/knowledge/doc/upload/limit")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadLimitList[0].fileType").value("document"));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/url/analysis")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"urlList\":[\"https://example.com/files/guide.txt\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.urlList[0].fileName").value("guide.txt"));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/import")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"docInfoList\":[{\"docId\":\"doc-guide\",\"docName\":\"Guide.txt\",\"docType\":\"txt\",\"docSize\":42}],\"docSegment\":{\"segmentMethod\":\"0\",\"segmentType\":\"0\"},\"docAnalyzer\":[\"text\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/knowledge/doc/segment/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("docId", "doc-guide")
                        .param("keyword", "Guide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("Guide.txt"))
                .andExpect(jsonPath("$.data.contentList[0].contentId").value("segment-001"));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/segment/create")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-guide\",\"content\":\"Extra segment\",\"labels\":[\"manual\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/segment/update")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-guide\",\"contentId\":\"segment-001\",\"content\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/segment/status/update")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-guide\",\"contentId\":\"segment-001\",\"contentStatus\":\"false\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/segment/labels")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-guide\",\"contentId\":\"segment-001\",\"labels\":[\"manual\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/knowledge/doc/segment/delete")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-guide\",\"contentId\":\"segment-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/knowledge/user")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeUserInfoList[0].userId").value("dev-admin"));

        mockMvc.perform(get("/user/api/v1/knowledge/org")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowOrgInfoList[0].orgId").value("default-org"));

        mockMvc.perform(get("/user/api/v1/knowledge/user/no/permit")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001")
                        .param("orgId", "default-org"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userInfoList[0].userId").value("dev-app"));

        mockMvc.perform(delete("/user/api/v1/knowledge")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(knowledgeService).selectKnowledge(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).createKnowledge(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateKnowledge(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteKnowledge(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).listDocs(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).analyzeDocUrls(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).importDocs(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).listDocSegments(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).createDocSegment(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateDocSegment(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateDocSegmentStatus(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateDocSegmentLabels(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteDocSegment(anyString(), anyString(), any(Map.class));
    }

    @Test
    public void knowledgeQaPairRoutesReturnFrontendContracts() throws Exception {
        when(knowledgeService.createQaPair(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("qaPairId", "qa-001"));
        when(knowledgeService.listQaPairs(anyString(), anyString(), any(Map.class)))
                .thenReturn(qaPairPage("knowledge-qa-001", "Dev QA",
                        qaPair("qa-001", "knowledge-qa-001", "What is Wanwu?", "An AI platform.", true)));
        when(knowledgeService.getQaImportTip(anyString(), anyString(), any(Map.class)))
                .thenReturn(docImportTip("knowledge-qa-001", "Dev QA"));
        when(knowledgeService.exportQaPairs(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("recordCreated", true));
        when(knowledgeService.hitQaPairs(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("searchList", Collections.singletonList(
                        qaHitResult("qa-001", "knowledge-qa-001", "Dev QA", "What is Wanwu?", "An AI platform."))));

        mockMvc.perform(post("/user/api/v1/knowledge/qa/pair")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-qa-001\",\"question\":\"What is Wanwu?\",\"answer\":\"An AI platform.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.qaPairId").value("qa-001"));

        mockMvc.perform(post("/user/api/v1/knowledge/qa/pair/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-qa-001\",\"name\":\"Wanwu\",\"status\":[2],\"pageNo\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.qaKnowledgeInfo.knowledgeName").value("Dev QA"))
                .andExpect(jsonPath("$.data.list[0].qaPairId").value("qa-001"))
                .andExpect(jsonPath("$.data.list[0].switch").value(true));

        mockMvc.perform(put("/user/api/v1/knowledge/qa/pair")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qaPairId\":\"qa-001\",\"question\":\"Updated?\",\"answer\":\"Updated.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/user/api/v1/knowledge/qa/pair/switch")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qaPairId\":\"qa-001\",\"switch\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/qa/hit")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Wanwu\",\"knowledgeList\":[{\"knowledgeId\":\"knowledge-qa-001\"}],\"knowledgeMatchParams\":{\"topK\":5}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.searchList[0].qaPairId").value("qa-001"))
                .andExpect(jsonPath("$.data.searchList[0].contentType").value("qa"));

        mockMvc.perform(get("/user/api/v1/knowledge/qa/pair/import/tip")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-qa-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeName").value("Dev QA"));

        mockMvc.perform(post("/user/api/v1/knowledge/qa/pair/import")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-qa-001\",\"docInfoList\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/knowledge/qa/export")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-qa-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordCreated").value(true));

        mockMvc.perform(delete("/user/api/v1/knowledge/qa/pair")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-qa-001\",\"QAPairIdList\":[\"qa-001\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(knowledgeService).createQaPair(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).listQaPairs(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateQaPair(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateQaPairSwitch(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).hitQaPairs(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).getQaImportTip(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).importQaPairs(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).exportQaPairs(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteQaPairs(anyString(), anyString(), any(Map.class));
    }

    @Test
    public void createAssistantReturnsFrontendAssistantId() throws Exception {
        when(appService.createAssistant(any(AssistantCreateCommand.class)))
                .thenReturn(new AssistantCreateResult("assistant-001"));

        mockMvc.perform(post("/user/api/v1/assistant")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":1,\"name\":\"DemoAgent\",\"desc\":\"A demo agent\",\"avatar\":{\"key\":\"avatars/demo.png\",\"path\":\"/static/demo.png\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.assistantId").value("assistant-001"));

        org.mockito.ArgumentCaptor<AssistantCreateCommand> captor = forClass(AssistantCreateCommand.class);
        verify(appService).createAssistant(captor.capture());
        assertEquals("DemoAgent", captor.getValue().getName());
        assertEquals("A demo agent", captor.getValue().getDesc());
        assertEquals(1, captor.getValue().getCategory());
        assertEquals("avatars/demo.png", captor.getValue().getAvatarKey());
        assertEquals("/static/demo.png", captor.getValue().getAvatarPath());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void updateAssistantReturnsFrontendSuccessAndMapsRequest() throws Exception {
        mockMvc.perform(put("/user/api/v1/assistant")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"category\":2,\"name\":\"UpdatedAgent\",\"desc\":\"updated desc\",\"avatar\":{\"key\":\"avatars/updated.png\",\"path\":\"/static/updated.png\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"));

        org.mockito.ArgumentCaptor<AssistantUpdateCommand> captor = forClass(AssistantUpdateCommand.class);
        verify(appService).updateAssistant(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
        assertEquals("UpdatedAgent", captor.getValue().getName());
        assertEquals("updated desc", captor.getValue().getDesc());
        assertEquals(2, captor.getValue().getCategory());
        assertEquals("avatars/updated.png", captor.getValue().getAvatarKey());
        assertEquals("/static/updated.png", captor.getValue().getAvatarPath());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void updateAssistantConfigReturnsFrontendSuccessAndMapsNestedPayload() throws Exception {
        mockMvc.perform(put("/user/api/v1/assistant/config")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"prologue\":\"Hello from draft\",\"instructions\":\"Be concise\",\"memoryConfig\":{\"maxHistoryLength\":9},\"modelConfig\":{\"config\":{\"temperature\":0.7},\"modelId\":\"llm-001\"},\"visionConfig\":{\"picNum\":5},\"recommendQuestion\":[\"What can you do?\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"));

        org.mockito.ArgumentCaptor<AssistantConfigUpdateCommand> captor = forClass(AssistantConfigUpdateCommand.class);
        verify(appService).updateAssistantConfig(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
        assertEquals("Hello from draft", captor.getValue().getPrologue());
        assertEquals("Be concise", captor.getValue().getInstructions());
        assertEquals(9, captor.getValue().getMemoryConfig().get("maxHistoryLength"));
        assertEquals("llm-001", captor.getValue().getModelConfig().get("modelId"));
        assertEquals(5, captor.getValue().getVisionConfig().get("picNum"));
        assertEquals("What can you do?", captor.getValue().getRecommendQuestion().get(0));
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void updateAssistantConfigReturnsFrontendFailureWhenAssistantIsMissing() throws Exception {
        doThrow(new IllegalArgumentException("assistant draft not found"))
                .when(appService).updateAssistantConfig(any(AssistantConfigUpdateCommand.class));

        mockMvc.perform(put("/user/api/v1/assistant/config")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-missing\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("assistant draft not found"));
    }

    @Test
    public void deleteAppspaceAgentReturnsFrontendSuccessAndMapsRequest() throws Exception {
        mockMvc.perform(delete("/user/api/v1/appspace/app")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-001\",\"appType\":\"agent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"));

        org.mockito.ArgumentCaptor<AssistantDeleteCommand> captor = forClass(AssistantDeleteCommand.class);
        verify(appService).deleteAssistant(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void deleteAppspaceAgentReturnsFrontendFailureWhenAssistantIsMissing() throws Exception {
        doThrow(new IllegalArgumentException("assistant draft not found"))
                .when(appService).deleteAssistant(any(AssistantDeleteCommand.class));

        mockMvc.perform(delete("/user/api/v1/appspace/app")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-missing\",\"appType\":\"agent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("assistant draft not found"));
    }

    @Test
    public void copyAssistantReturnsFrontendAssistantIdAndMapsRequest() throws Exception {
        when(appService.copyAssistant(any(AssistantCopyCommand.class)))
                .thenReturn(new AssistantCreateResult("assistant-copy-001"));

        mockMvc.perform(post("/user/api/v1/assistant/copy")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.assistantId").value("assistant-copy-001"));

        org.mockito.ArgumentCaptor<AssistantCopyCommand> captor = forClass(AssistantCopyCommand.class);
        verify(appService).copyAssistant(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void copyAssistantReturnsFrontendFailureWhenAssistantIsMissing() throws Exception {
        when(appService.copyAssistant(any(AssistantCopyCommand.class)))
                .thenThrow(new IllegalArgumentException("assistant draft not found"));

        mockMvc.perform(post("/user/api/v1/assistant/copy")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-missing\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("assistant draft not found"));
    }

    @Test
    public void publishAppReturnsFrontendSuccessAndMapsRequest() throws Exception {
        mockMvc.perform(post("/user/api/v1/appspace/app/publish")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-001\",\"appType\":\"agent\",\"version\":\"v1.0.0\",\"desc\":\"first release\",\"publishType\":\"organization\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"));

        org.mockito.ArgumentCaptor<AppPublishCommand> captor = forClass(AppPublishCommand.class);
        verify(appService).publishApp(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAppId());
        assertEquals("agent", captor.getValue().getAppType());
        assertEquals("v1.0.0", captor.getValue().getVersion());
        assertEquals("first release", captor.getValue().getDesc());
        assertEquals("organization", captor.getValue().getPublishType());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void publishAppReturnsFrontendFailureWhenVersionIsInvalid() throws Exception {
        doThrow(new IllegalArgumentException("app version must be greater than latest version"))
                .when(appService).publishApp(any(AppPublishCommand.class));

        mockMvc.perform(post("/user/api/v1/appspace/app/publish")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-001\",\"appType\":\"agent\",\"version\":\"v1.0.0\",\"publishType\":\"private\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("app version must be greater than latest version"));
    }

    @Test
    public void unpublishAppReturnsFrontendSuccessAndMapsRequest() throws Exception {
        mockMvc.perform(delete("/user/api/v1/appspace/app/publish")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-001\",\"appType\":\"agent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"));

        org.mockito.ArgumentCaptor<AppPublishCommand> captor = forClass(AppPublishCommand.class);
        verify(appService).unpublishApp(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAppId());
        assertEquals("agent", captor.getValue().getAppType());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void getAppLatestVersionReturnsFrontendShape() throws Exception {
        when(appService.getLatestAppVersion(any(AppVersionQuery.class)))
                .thenReturn(versionInfo("v1.0.0", "first release", "2026-06-29 10:00:00", "private"));

        mockMvc.perform(get("/user/api/v1/appspace/app/version")
                        .header("Authorization", "Bearer dev-token")
                        .param("appId", "assistant-001")
                        .param("appType", "agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.version").value("v1.0.0"))
                .andExpect(jsonPath("$.data.desc").value("first release"))
                .andExpect(jsonPath("$.data.publishType").value("private"));

        org.mockito.ArgumentCaptor<AppVersionQuery> captor = forClass(AppVersionQuery.class);
        verify(appService).getLatestAppVersion(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAppId());
        assertEquals("agent", captor.getValue().getAppType());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void getAppVersionListReturnsFrontendTimelineShape() throws Exception {
        when(appService.listAppVersions(any(AppVersionQuery.class)))
                .thenReturn(new AppVersionListResult(Collections.singletonList(
                        versionInfo("v1.0.1", "second release", "2026-06-29 10:01:00", "private")), 1));

        mockMvc.perform(get("/user/api/v1/appspace/app/version/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("appId", "assistant-001")
                        .param("appType", "agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].version").value("v1.0.1"))
                .andExpect(jsonPath("$.data.list[0].desc").value("second release"))
                .andExpect(jsonPath("$.data.list[0].createdAt").value("2026-06-29 10:01:00"));

        verify(appService).listAppVersions(any(AppVersionQuery.class));
    }

    @Test
    public void updateAppVersionReturnsFrontendSuccessAndMapsRequest() throws Exception {
        mockMvc.perform(put("/user/api/v1/appspace/app/version")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-001\",\"appType\":\"agent\",\"desc\":\"updated release\",\"publishType\":\"public\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"));

        org.mockito.ArgumentCaptor<AppVersionUpdateCommand> captor = forClass(AppVersionUpdateCommand.class);
        verify(appService).updateAppVersion(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAppId());
        assertEquals("agent", captor.getValue().getAppType());
        assertEquals("updated release", captor.getValue().getDesc());
        assertEquals("public", captor.getValue().getPublishType());
    }

    @Test
    public void rollbackAppVersionReturnsFrontendSuccessAndMapsRequest() throws Exception {
        mockMvc.perform(post("/user/api/v1/appspace/app/version/rollback")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-001\",\"appType\":\"agent\",\"version\":\"v1.0.0\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"));

        org.mockito.ArgumentCaptor<AppVersionRollbackCommand> captor = forClass(AppVersionRollbackCommand.class);
        verify(appService).rollbackAppVersion(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAppId());
        assertEquals("agent", captor.getValue().getAppType());
        assertEquals("v1.0.0", captor.getValue().getVersion());
    }

    @Test
    public void assistantPublishedInfoReturnsSnapshotEditorShape() throws Exception {
        Map<String, Object> published = new LinkedHashMap<>();
        published.put("assistantId", "assistant-001");
        published.put("uuid", "assistant-001");
        published.put("name", "PublishedAgent");
        published.put("desc", "Published desc");
        published.put("publishType", "private");
        published.put("modelConfig", Collections.singletonMap("config", null));
        when(appService.getPublishedAssistant(any(AssistantPublishedQuery.class))).thenReturn(published);

        mockMvc.perform(get("/user/api/v1/assistant")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-001")
                        .param("version", "v1.0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.assistantId").value("assistant-001"))
                .andExpect(jsonPath("$.data.name").value("PublishedAgent"))
                .andExpect(jsonPath("$.data.publishType").value("private"));

        org.mockito.ArgumentCaptor<AssistantPublishedQuery> captor = forClass(AssistantPublishedQuery.class);
        verify(appService).getPublishedAssistant(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
        assertEquals("v1.0.0", captor.getValue().getVersion());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void assistantListReturnsFrontendCardShape() throws Exception {
        Map<String, Object> avatar = new LinkedHashMap<>();
        avatar.put("key", "avatars/demo.png");
        avatar.put("path", "/static/demo.png");
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uniqueId", "agent_assistant-001");
        item.put("appId", "assistant-001");
        item.put("appType", "agent");
        item.put("avatar", avatar);
        item.put("name", "DemoAgent");
        item.put("desc", "A demo agent");
        item.put("createdAt", "2026-06-29 10:00:00");
        item.put("updatedAt", "2026-06-29 10:00:00");
        item.put("publishType", "private");
        item.put("category", 1);
        item.put("version", "v0.0.1");
        when(appService.listAssistants(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(item)));

        mockMvc.perform(get("/user/api/v1/appspace/assistant/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("name", "Demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list", hasSize(1)))
                .andExpect(jsonPath("$.data.list[0].appId").value("assistant-001"))
                .andExpect(jsonPath("$.data.list[0].appType").value("agent"))
                .andExpect(jsonPath("$.data.list[0].name").value("DemoAgent"))
                .andExpect(jsonPath("$.data.list[0].avatar.path").value("/static/demo.png"));

        org.mockito.ArgumentCaptor<ApplicationListQuery> captor = forClass(ApplicationListQuery.class);
        verify(appService).listAssistants(captor.capture());
        assertEquals("Demo", captor.getValue().getName());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void assistantDraftReturnsPersistedEditorShape() throws Exception {
        Map<String, Object> draft = new LinkedHashMap<>();
        draft.put("assistantId", "assistant-001");
        draft.put("uuid", "assistant-001");
        draft.put("name", "DemoAgent");
        draft.put("desc", "A demo agent");
        draft.put("publishType", "private");
        draft.put("modelConfig", Collections.singletonMap("config", null));
        draft.put("rerankConfig", Collections.singletonMap("modelId", ""));
        when(appService.getAssistantDraft(any(AssistantDetailQuery.class))).thenReturn(draft);

        mockMvc.perform(get("/user/api/v1/assistant/draft")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.assistantId").value("assistant-001"))
                .andExpect(jsonPath("$.data.name").value("DemoAgent"))
                .andExpect(jsonPath("$.data.modelConfig").exists())
                .andExpect(jsonPath("$.data.rerankConfig.modelId").value(""));

        org.mockito.ArgumentCaptor<AssistantDetailQuery> captor = forClass(AssistantDetailQuery.class);
        verify(appService).getAssistantDraft(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void assistantDraftReturnsFrontendFailureWhenAssistantIsMissing() throws Exception {
        when(appService.getAssistantDraft(any(AssistantDetailQuery.class)))
                .thenThrow(new IllegalArgumentException("assistant draft not found"));

        mockMvc.perform(get("/user/api/v1/assistant/draft")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("assistant draft not found"));
    }

    @Test
    public void ragRoutesMapFrontendCrudConfigCopyAndDetailRequests() throws Exception {
        when(appService.createRag(any(RagCreateCommand.class))).thenReturn(new RagCreateResult("rag-001"));
        when(appService.copyRag(any(RagCopyCommand.class))).thenReturn(new RagCreateResult("rag-002"));

        Map<String, Object> avatar = new LinkedHashMap<>();
        avatar.put("key", "avatars/rag.png");
        avatar.put("path", "/static/rag.png");
        Map<String, Object> rag = new LinkedHashMap<>();
        rag.put("ragId", "rag-001");
        rag.put("avatar", avatar);
        rag.put("name", "PolicyRag");
        rag.put("desc", "policy qa");
        rag.put("modelConfig", Collections.singletonMap("modelId", "llm-001"));
        rag.put("rerankConfig", Collections.singletonMap("modelId", "rerank-001"));
        rag.put("qaRerankConfig", Collections.singletonMap("modelId", "qa-rerank-001"));
        rag.put("knowledgeBaseConfig", Collections.singletonMap("knowledgebases", Collections.emptyList()));
        rag.put("qaKnowledgeBaseConfig", Collections.singletonMap("knowledgebases", Collections.emptyList()));
        rag.put("safetyConfig", Collections.singletonMap("enable", false));
        rag.put("visionConfig", Collections.singletonMap("picNum", 0));
        rag.put("appPublishConfig", Collections.singletonMap("publishType", "public"));
        when(appService.getRagDraft(any(RagDetailQuery.class))).thenReturn(rag);
        when(appService.getPublishedRag(any(RagDetailQuery.class))).thenReturn(rag);

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("appId", "rag-001");
        card.put("appType", "rag");
        card.put("name", "PolicyRag");
        card.put("avatar", avatar);
        when(appService.listApplications(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(card)));

        mockMvc.perform(post("/user/api/v1/appspace/rag")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"PolicyRag\",\"desc\":\"policy qa\",\"avatar\":{\"key\":\"avatars/rag.png\",\"path\":\"/static/rag.png\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.ragId").value("rag-001"));

        mockMvc.perform(get("/user/api/v1/appspace/rag/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("name", "Policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].appType").value("rag"));

        mockMvc.perform(get("/user/api/v1/appspace/rag/draft")
                        .header("Authorization", "Bearer dev-token")
                        .param("ragId", "rag-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ragId").value("rag-001"))
                .andExpect(jsonPath("$.data.modelConfig.modelId").value("llm-001"));

        mockMvc.perform(put("/user/api/v1/appspace/rag")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ragId\":\"rag-001\",\"name\":\"PolicyRag2\",\"desc\":\"updated\",\"avatar\":{\"key\":\"avatars/rag2.png\",\"path\":\"/static/rag2.png\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/user/api/v1/appspace/rag/config")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ragId\":\"rag-001\",\"modelConfig\":{\"modelId\":\"llm-001\"},\"rerankConfig\":{\"modelId\":\"rerank-001\"},\"qaRerankConfig\":{\"modelId\":\"qa-rerank-001\"},\"knowledgeBaseConfig\":{\"knowledgebases\":[]},\"qaKnowledgeBaseConfig\":{\"knowledgebases\":[]},\"safetyConfig\":{\"enable\":false},\"visionConfig\":{\"picNum\":0}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/appspace/rag")
                        .header("Authorization", "Bearer dev-token")
                        .param("ragId", "rag-001")
                        .param("version", "v1.0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.appPublishConfig.publishType").value("public"));

        mockMvc.perform(post("/user/api/v1/appspace/rag/copy")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ragId\":\"rag-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ragId").value("rag-002"));

        mockMvc.perform(delete("/user/api/v1/appspace/rag")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ragId\":\"rag-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        org.mockito.ArgumentCaptor<RagCreateCommand> createCaptor = forClass(RagCreateCommand.class);
        verify(appService).createRag(createCaptor.capture());
        assertEquals("PolicyRag", createCaptor.getValue().getName());
        assertEquals("dev-admin", createCaptor.getValue().getUserId());

        verify(appService).updateRag(any(RagUpdateCommand.class));
        verify(appService).updateRagConfig(any(RagConfigUpdateCommand.class));
        verify(appService).getRagDraft(any(RagDetailQuery.class));
        verify(appService).getPublishedRag(any(RagDetailQuery.class));
        verify(appService).copyRag(any(RagCopyCommand.class));
        verify(appService).deleteRag(any(RagDeleteCommand.class));
    }

    @Test
    public void assistantExtensionRoutesMapFrontendConfigRequests() throws Exception {
        Map<String, Object> assistantSelectItem = new LinkedHashMap<>();
        assistantSelectItem.put("appId", "assistant-child");
        assistantSelectItem.put("appType", "agent");
        assistantSelectItem.put("name", "Child Agent");
        when(appService.listAssistants(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(assistantSelectItem)));
        when(appService.listAssistantToolSelect(anyString(), anyString()))
                .thenReturn(listResult(toolSelect("builtin-weather", "Weather Tool", "builtin")));
        when(appService.listAssistantToolActions(any(AssistantResourceCommand.class)))
                .thenReturn(actionList("get_weather"));
        when(appService.getAssistantToolActionDetail(any(AssistantResourceCommand.class)))
                .thenReturn(actionDetail("get_weather"));
        when(appService.listAssistantMcpSelect(anyString(), anyString()))
                .thenReturn(listResult(mcpSelect("mcp-001", "Search MCP", "mcp")));
        when(appService.listAssistantMcpActions(any(AssistantResourceCommand.class)))
                .thenReturn(actionList("search"));
        when(appService.listAssistantWorkflowSelect(anyString(), anyString(), anyString()))
                .thenReturn(listResult(workflowSelect("workflow-001", "Workflow One")));

        mockMvc.perform(get("/user/api/v1/assistant/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].appId").value("assistant-child"));
        mockMvc.perform(get("/user/api/v1/tool/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].toolId").value("builtin-weather"));
        mockMvc.perform(get("/user/api/v1/tool/action/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("toolId", "builtin-weather")
                        .param("toolType", "builtin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actions[0].name").value("get_weather"));
        mockMvc.perform(get("/user/api/v1/tool/action/detail")
                        .header("Authorization", "Bearer dev-token")
                        .param("toolId", "builtin-weather")
                        .param("toolType", "builtin")
                        .param("actionName", "get_weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.action.name").value("get_weather"));
        mockMvc.perform(get("/user/api/v1/mcp/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].mcpId").value("mcp-001"));
        mockMvc.perform(get("/user/api/v1/mcp/action/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("toolId", "mcp-001")
                        .param("toolType", "mcp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actions[0].name").value("search"));
        mockMvc.perform(get("/user/api/v1/workflow/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].workFlowId").value("workflow-001"));

        mockMvc.perform(post("/user/api/v1/assistant/tool/workflow")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"workFlowId\":\"workflow-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(put("/user/api/v1/assistant/tool/workflow/switch")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"workFlowId\":\"workflow-001\",\"enable\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/user/api/v1/assistant/tool/mcp")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"mcpId\":\"mcp-001\",\"mcpType\":\"mcp\",\"actionName\":\"search\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/user/api/v1/assistant/tool")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"toolId\":\"builtin-weather\",\"toolType\":\"builtin\",\"actionName\":\"get_weather\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(put("/user/api/v1/assistant/tool/config")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"toolId\":\"builtin-weather\",\"toolConfig\":{\"rerankId\":\"rerank-001\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/user/api/v1/assistant/skill")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"skillId\":\"builtin-summary\",\"skillType\":\"builtin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/user/api/v1/assistant/multi-agent")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"agentId\":\"assistant-child\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        org.mockito.ArgumentCaptor<AssistantResourceCommand> workflowCaptor = forClass(AssistantResourceCommand.class);
        verify(appService).addAssistantWorkflow(workflowCaptor.capture());
        assertEquals("assistant-001", workflowCaptor.getValue().getAssistantId());
        assertEquals("workflow-001", workflowCaptor.getValue().getResourceId());

        verify(appService).switchAssistantWorkflow(any(AssistantResourceCommand.class));
        verify(appService).addAssistantMcp(any(AssistantResourceCommand.class));
        verify(appService).addAssistantTool(any(AssistantResourceCommand.class));
        verify(appService).configureAssistantTool(any(AssistantResourceCommand.class));
        verify(appService).addAssistantSkill(any(AssistantResourceCommand.class));
        verify(appService).addAssistantAgent(any(AssistantResourceCommand.class));
        verify(appService).listAssistantToolSelect("dev-admin", "default-org");
        verify(appService).listAssistantMcpSelect("dev-admin", "default-org");
        verify(appService).listAssistantWorkflowSelect("dev-admin", "default-org", "");
    }

    @Test
    public void createAssistantConversationReturnsConversationIdAndMapsRequest() throws Exception {
        when(appService.createAssistantConversation(any(AssistantConversationCreateCommand.class)))
                .thenReturn(new AssistantConversationCreateResult("conversation-001"));

        mockMvc.perform(post("/user/api/v1/assistant/conversation")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"prompt\":\"hello agent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.conversationId").value("conversation-001"));

        org.mockito.ArgumentCaptor<AssistantConversationCreateCommand> captor =
                forClass(AssistantConversationCreateCommand.class);
        verify(appService).createAssistantConversation(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
        assertEquals("hello agent", captor.getValue().getPrompt());
        assertEquals("published", captor.getValue().getConversationType());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void assistantConversationListReturnsFrontendPageShape() throws Exception {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("conversationId", "conversation-001");
        item.put("assistantId", "assistant-001");
        item.put("title", "hello agent");
        item.put("createdAt", "2026-06-29 10:00:00");
        when(appService.listAssistantConversations(any(AssistantConversationListQuery.class)))
                .thenReturn(new AssistantConversationPageResult(Collections.singletonList(item), 1, 1, 20));

        mockMvc.perform(get("/user/api/v1/assistant/conversation/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-001")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].conversationId").value("conversation-001"))
                .andExpect(jsonPath("$.data.list[0].title").value("hello agent"));

        org.mockito.ArgumentCaptor<AssistantConversationListQuery> captor =
                forClass(AssistantConversationListQuery.class);
        verify(appService).listAssistantConversations(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
        assertEquals("published", captor.getValue().getConversationType());
        assertEquals(1, captor.getValue().getPageNo());
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    public void assistantConversationDetailReturnsFrontendHistoryShape() throws Exception {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", "detail-001");
        detail.put("assistantId", "assistant-001");
        detail.put("conversationId", "conversation-001");
        detail.put("prompt", "hello agent");
        detail.put("response", "Hello from DemoAgent.");
        detail.put("responseList", Collections.emptyList());
        when(appService.listAssistantConversationDetails(any(AssistantConversationDetailQuery.class)))
                .thenReturn(new AssistantConversationPageResult(Collections.singletonList(detail), 1, 1, 1000));

        mockMvc.perform(get("/user/api/v1/assistant/conversation/detail")
                        .header("Authorization", "Bearer dev-token")
                        .param("conversationId", "conversation-001")
                        .param("pageNo", "1")
                        .param("pageSize", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value("detail-001"))
                .andExpect(jsonPath("$.data.list[0].prompt").value("hello agent"))
                .andExpect(jsonPath("$.data.list[0].response").value("Hello from DemoAgent."));

        verify(appService).listAssistantConversationDetails(any(AssistantConversationDetailQuery.class));
    }

    @Test
    public void deleteAndClearConversationRoutesReturnFrontendSuccess() throws Exception {
        mockMvc.perform(delete("/user/api/v1/assistant/conversation")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationId\":\"conversation-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/assistant/conversation/clear")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationId\":\"conversation-001\",\"detailId\":\"detail-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        org.mockito.ArgumentCaptor<AssistantConversationDeleteCommand> captor =
                forClass(AssistantConversationDeleteCommand.class);
        verify(appService).deleteAssistantConversation(captor.capture());
        assertEquals("conversation-001", captor.getValue().getConversationId());
        verify(appService).clearAssistantConversation(any(AssistantConversationDeleteCommand.class));
    }

    @Test
    public void draftConversationHistoryAndDeleteUseAssistantId() throws Exception {
        when(appService.listDraftAssistantConversationDetails(any(AssistantConversationListQuery.class)))
                .thenReturn(new AssistantConversationPageResult(Collections.emptyList(), 0, 1, 30));

        mockMvc.perform(get("/user/api/v1/assistant/conversation/draft/detail")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-001")
                        .param("pageNo", "1")
                        .param("pageSize", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list", hasSize(0)));

        mockMvc.perform(delete("/user/api/v1/assistant/conversation/draft")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"detailId\":\"detail-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(appService).listDraftAssistantConversationDetails(any(AssistantConversationListQuery.class));
        verify(appService).deleteDraftAssistantConversation(any(AssistantConversationDeleteCommand.class));
    }

    @Test
    public void assistantStreamDraftReturnsSseFramesAndMapsCommand() throws Exception {
        AssistantConversationStreamResult result = new AssistantConversationStreamResult();
        result.setAssistantId("assistant-001");
        result.setConversationId("conversation-001");
        result.setDetailId("detail-001");
        result.setResponse("Hello from DemoAgent.");
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class))).thenReturn(result);

        mockMvc.perform(post("/user/api/v1/assistant/stream/draft")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"prompt\":\"hello agent\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"detailId\":\"detail-001\"")))
                .andExpect(content().string(containsString("\"finish\":1")));

        org.mockito.ArgumentCaptor<AssistantConversationStreamCommand> captor =
                forClass(AssistantConversationStreamCommand.class);
        verify(appService).streamAssistantConversation(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
        assertEquals("hello agent", captor.getValue().getPrompt());
        assertEquals(true, captor.getValue().isDraft());
    }

    @Test
    public void assistantPublishedStreamAndTestStreamShareSseContract() throws Exception {
        AssistantConversationStreamResult result = new AssistantConversationStreamResult();
        result.setAssistantId("assistant-001");
        result.setConversationId("conversation-001");
        result.setDetailId("detail-002");
        result.setResponse("Published answer.");
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class))).thenReturn(result);

        mockMvc.perform(post("/user/api/v1/assistant/stream")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"conversationId\":\"conversation-001\",\"prompt\":\"published\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("Published answer.")));

        mockMvc.perform(post("/user/api/v1/assistant/test/stream")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"prompt\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    public void ragChatDraftReturnsAgUiSseAndMapsFrontendRequest() throws Exception {
        RagChatResult result = new RagChatResult();
        result.setRagId("rag-001");
        result.setResponse("RAG local answer.");
        when(appService.streamRagChat(any(RagChatCommand.class))).thenReturn(result);

        mockMvc.perform(post("/user/api/v1/rag/chat/draft")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ragId\":\"rag-001\",\"question\":\"what is policy\",\"history\":[{\"query\":\"q1\",\"response\":\"a1\",\"needHistory\":true}],\"fileInfo\":[{\"fileName\":\"a.txt\",\"fileSize\":3,\"fileUrl\":\"http://file/a.txt\"}]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"type\":\"RUN_STARTED\"")))
                .andExpect(content().string(containsString("\"type\":\"TEXT_MESSAGE_CONTENT\"")))
                .andExpect(content().string(containsString("RAG local answer.")))
                .andExpect(content().string(containsString("\"type\":\"RUN_FINISHED\"")));

        org.mockito.ArgumentCaptor<RagChatCommand> captor = forClass(RagChatCommand.class);
        verify(appService).streamRagChat(captor.capture());
        assertEquals("rag-001", captor.getValue().getRagId());
        assertEquals("what is policy", captor.getValue().getQuestion());
        assertEquals(true, captor.getValue().isDraft());
        assertEquals(1, captor.getValue().getHistory().size());
        assertEquals(1, captor.getValue().getFileInfo().size());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
    }

    @Test
    public void ragPublishedChatUsesPublishedModeAndUploadReturnsGoShape() throws Exception {
        RagChatResult result = new RagChatResult();
        result.setRagId("rag-001");
        result.setResponse("Published RAG answer.");
        when(appService.streamRagChat(any(RagChatCommand.class))).thenReturn(result);

        mockMvc.perform(post("/user/api/v1/rag/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ragId\":\"rag-001\",\"question\":\"published question\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("Published RAG answer.")));

        org.mockito.ArgumentCaptor<RagChatCommand> captor = forClass(RagChatCommand.class);
        verify(appService).streamRagChat(captor.capture());
        assertEquals(false, captor.getValue().isDraft());

        MockMultipartFile file = new MockMultipartFile(
                "files", "diagram.png", "image/png", "png-data".getBytes("UTF-8"));
        mockMvc.perform(multipart("/user/api/v1/rag/upload")
                        .file(file)
                        .param("markdown", "true")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.fileList[0].fileIndex").value(0))
                .andExpect(jsonPath("$.data.fileList[0].fileUrl").value(containsString("![diagram.png](")));
    }

    @Test
    public void assistantQuestionRecommendReturnsOpenAiStyleSseAndUsesDraftLookup() throws Exception {
        Map<String, Object> draft = new LinkedHashMap<>();
        draft.put("assistantId", "assistant-001");
        draft.put("name", "DemoAgent");
        when(appService.getAssistantDraft(any(AssistantDetailQuery.class))).thenReturn(draft);

        mockMvc.perform(post("/user/api/v1/assistant/question/recommend")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"conversationId\":\"conversation-001\",\"query\":\"how to deploy\",\"trial\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"object\":\"chat.completion.chunk\"")))
                .andExpect(content().string(containsString("\"contentType\":\"answer\"")))
                .andExpect(content().string(containsString("how to deploy")))
                .andExpect(content().string(containsString("\"finish_reason\":\"stop\"")));

        org.mockito.ArgumentCaptor<AssistantDetailQuery> captor = forClass(AssistantDetailQuery.class);
        verify(appService).getAssistantDraft(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
    }

    @Test
    public void assistantQuestionRecommendUsesPublishedLookupWhenNotTrial() throws Exception {
        Map<String, Object> published = new LinkedHashMap<>();
        published.put("assistantId", "assistant-001");
        published.put("name", "PublishedAgent");
        when(appService.getPublishedAssistant(any(AssistantPublishedQuery.class))).thenReturn(published);

        mockMvc.perform(post("/user/api/v1/assistant/question/recommend")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"conversationId\":\"conversation-001\",\"query\":\"how to operate\",\"trial\":false}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"model\":\"local-recommend\"")));

        org.mockito.ArgumentCaptor<AssistantPublishedQuery> captor = forClass(AssistantPublishedQuery.class);
        verify(appService).getPublishedAssistant(captor.capture());
        assertEquals("assistant-001", captor.getValue().getAssistantId());
    }

    @Test
    public void appOpenUrlManagementRoutesMapFrontendPayloads() throws Exception {
        when(appService.listAppUrls(any(AppUrlListQuery.class)))
                .thenReturn(Collections.singletonList(appUrlInfo("1", "assistant-001", "suffix-001")));

        mockMvc.perform(post("/user/api/v1/appspace/app/openurl")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-001\",\"appType\":\"agent\",\"name\":\"Public demo\",\"description\":\"open desc\",\"expiredAt\":\"2026-07-01 12:30:00\",\"copyright\":\"Copyright\",\"copyrightEnable\":true,\"privacyPolicy\":\"Privacy\",\"privacyPolicyEnable\":true,\"disclaimer\":\"Disclaimer\",\"disclaimerEnable\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/appspace/app/openurl/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("appId", "assistant-001")
                        .param("appType", "agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].urlId").value("1"))
                .andExpect(jsonPath("$.data[0].suffix").value("/service/url/openurl/v1/agent/suffix-001"));

        mockMvc.perform(put("/user/api/v1/appspace/app/openurl")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"urlId\":\"1\",\"name\":\"Updated demo\",\"description\":\"updated desc\",\"expiredAt\":\"2026-07-02 12:30:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/user/api/v1/appspace/app/openurl/status")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"urlId\":\"1\",\"status\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/appspace/app/openurl")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"urlId\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        org.mockito.ArgumentCaptor<AppUrlCreateCommand> createCaptor = forClass(AppUrlCreateCommand.class);
        verify(appService).createAppUrl(createCaptor.capture());
        assertEquals("assistant-001", createCaptor.getValue().getAppId());
        assertEquals("agent", createCaptor.getValue().getAppType());
        assertEquals("Public demo", createCaptor.getValue().getName());
        assertEquals("dev-admin", createCaptor.getValue().getUserId());
        assertEquals("default-org", createCaptor.getValue().getOrgId());

        verify(appService).listAppUrls(any(AppUrlListQuery.class));
        verify(appService).updateAppUrl(any(AppUrlUpdateCommand.class));
        verify(appService).updateAppUrlStatus(any(AppUrlStatusCommand.class));
        verify(appService).deleteAppUrl(any(AppUrlDeleteCommand.class));
    }

    @Test
    public void apiKeyManagementRoutesMapFrontendPayloads() throws Exception {
        when(appService.createApiKey(any(ApiKeyCreateCommand.class)))
                .thenReturn(apiKeyInfo("1", "wanwu_api_001", "Main key", true));
        when(appService.listApiKeys(any(ApiKeyListQuery.class)))
                .thenReturn(new ApiKeyPageResult(
                        Collections.singletonList(apiKeyInfo("1", "wanwu_api_001", "Main key", true)),
                        1,
                        1,
                        20));

        mockMvc.perform(post("/user/api/v1/api/key")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Main key\",\"desc\":\"first\",\"expiredAt\":\"2030-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.keyId").value("1"))
                .andExpect(jsonPath("$.data.key").value("wanwu_api_001"));

        mockMvc.perform(get("/user/api/v1/api/key/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].keyId").value("1"))
                .andExpect(jsonPath("$.data.list[0].status").value(true))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(put("/user/api/v1/api/key")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyId\":\"1\",\"name\":\"Updated\",\"desc\":\"updated\",\"expiredAt\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(put("/user/api/v1/api/key/status")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyId\":\"1\",\"status\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/api/key")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyId\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(appService).createApiKey(any(ApiKeyCreateCommand.class));
        verify(appService).listApiKeys(any(ApiKeyListQuery.class));
        verify(appService).updateApiKey(any(ApiKeyUpdateCommand.class));
        verify(appService).updateApiKeyStatus(any(ApiKeyStatusCommand.class));
        verify(appService).deleteApiKey(any(ApiKeyDeleteCommand.class));
    }

    @Test
    public void appKeyRoutesMapFrontendPayloads() throws Exception {
        when(appService.createAppKey(any(AppKeyCreateCommand.class)))
                .thenReturn(appKeyInfo("3", "app_key_001", "assistant-001"));
        when(appService.listAppKeys(any(AppKeyListQuery.class)))
                .thenReturn(Collections.singletonList(appKeyInfo("3", "app_key_001", "assistant-001")));

        mockMvc.perform(post("/user/api/v1/appspace/app/key")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-001\",\"appType\":\"agent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.apiId").value("3"))
                .andExpect(jsonPath("$.data.apiKey").value("app_key_001"));

        mockMvc.perform(get("/user/api/v1/appspace/app/key/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("appId", "assistant-001")
                        .param("appType", "agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].apiId").value("3"))
                .andExpect(jsonPath("$.data[0].apiKey").value("app_key_001"));

        mockMvc.perform(delete("/user/api/v1/appspace/app/key")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apiId\":\"3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(appService).createAppKey(any(AppKeyCreateCommand.class));
        verify(appService).listAppKeys(any(AppKeyListQuery.class));
        verify(appService).deleteAppKey(any(AppKeyDeleteCommand.class));
    }

    @Test
    public void editorSelectEndpointsReturnEmptyListsForFrontend() throws Exception {
        when(mcpService.listPromptTemplates(anyString(), anyString(), org.mockito.ArgumentMatchers.<String>nullable(String.class)))
                .thenReturn(emptyListResult());
        when(knowledgeService.selectKnowledge(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeList", Collections.emptyList()));

        mockMvc.perform(get("/user/api/v1/prompt/template/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list", hasSize(0)));

        mockMvc.perform(post("/user/api/v1/knowledge/select")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.knowledgeList", hasSize(0)));
    }

    @Test
    public void appOpenUrlReturnsOpenUrlPublicPrefix() throws Exception {
        mockMvc.perform(get("/user/api/v1/appspace/app/url")
                        .param("appId", "assistant-001")
                        .param("appType", "agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("/service/url/openurl/v1/agent"));
    }

    private LoginResult devAdminResult() {
        LoginResult result = new LoginResult();
        result.setUid("dev-admin");
        result.setUsername("admin");
        result.setUserCategory("admin");
        result.setToken("dev-token");
        result.setExpiresAt(4102444800000L);
        result.setIsUpdatePassword(true);
        result.setOrgs(Collections.singletonList(new OrganizationOption("default-org", "Default Organization")));
        result.setOrgPermission(orgPermission());
        result.setCustom(platformConfig());
        return result;
    }

    private Map<String, Object> page(Map<String, Object> item, int total, int pageNo, int pageSize) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("list", Collections.singletonList(item));
        page.put("total", total);
        page.put("pageNo", pageNo);
        page.put("pageSize", pageSize);
        return page;
    }

    private Map<String, Object> select(Map<String, Object> item) {
        Map<String, Object> select = new LinkedHashMap<>();
        select.put("select", Collections.singletonList(item));
        return select;
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    private Map<String, Object> knowledgeList(Map<String, Object> item) {
        return singleton("knowledgeList", Collections.singletonList(item));
    }

    private Map<String, Object> knowledgeItem(String knowledgeId, String name, int category) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("knowledgeId", knowledgeId);
        item.put("name", name);
        item.put("orgName", "Default Organization");
        item.put("description", "docs");
        item.put("docCount", 0);
        item.put("embeddingModelInfo", singleton("modelId", "2"));
        item.put("knowledgeTagList", Collections.singletonList(tag("tag-001", "Backend", true)));
        item.put("createUserId", "dev-admin");
        item.put("createAt", "2026-06-30 00:00:00");
        item.put("permissionType", 20);
        item.put("share", false);
        item.put("ragName", name);
        item.put("graphSwitch", 0);
        item.put("category", category);
        item.put("llmModelId", "");
        item.put("updatedAt", "2026-06-30 00:00:00");
        item.put("external", 0);
        item.put("avatar", singleton("path", ""));
        return item;
    }

    private Map<String, Object> tag(String tagId, String tagName, boolean selected) {
        Map<String, Object> tag = new LinkedHashMap<>();
        tag.put("tagId", tagId);
        tag.put("tagName", tagName);
        tag.put("selected", selected);
        return tag;
    }

    private Map<String, Object> splitter(String splitterId, String splitterName, String splitterValue, String type) {
        Map<String, Object> splitter = new LinkedHashMap<>();
        splitter.put("splitterId", splitterId);
        splitter.put("splitterName", splitterName);
        splitter.put("splitterValue", splitterValue);
        splitter.put("type", type);
        return splitter;
    }

    private Map<String, Object> docPage(String knowledgeId, String knowledgeName) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("list", Collections.emptyList());
        page.put("total", 0);
        page.put("pageNo", 1);
        page.put("pageSize", 10);
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("knowledgeId", knowledgeId);
        info.put("knowledgeName", knowledgeName);
        info.put("graphSwitch", 0);
        info.put("showGraphReport", false);
        info.put("description", "docs");
        info.put("keywords", Collections.emptyList());
        info.put("embeddingModel", modelInfo("2", "Text Embedding Small", "embedding"));
        info.put("llmModelId", "");
        info.put("category", 0);
        info.put("avatar", singleton("path", ""));
        page.put("docKnowledgeInfo", info);
        return page;
    }

    private Map<String, Object> docConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("docImportType", 0);
        Map<String, Object> segment = new LinkedHashMap<>();
        segment.put("segmentMethod", "0");
        segment.put("segmentType", "0");
        config.put("docSegment", segment);
        config.put("docAnalyzer", Collections.singletonList("text"));
        config.put("parserModelId", "");
        config.put("asrModelId", "");
        config.put("multimodalModelId", "");
        config.put("docPreprocess", Collections.emptyList());
        return config;
    }

    private Map<String, Object> docImportTip(String knowledgeId, String knowledgeName) {
        Map<String, Object> tip = new LinkedHashMap<>();
        tip.put("msg", "");
        tip.put("uploadstatus", 2);
        tip.put("knowledgeId", knowledgeId);
        tip.put("knowledgeName", knowledgeName);
        return tip;
    }

    private Map<String, Object> qaPairPage(String knowledgeId, String knowledgeName, Map<String, Object> pair) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("list", Collections.singletonList(pair));
        page.put("total", 1);
        page.put("pageNo", 1);
        page.put("pageSize", 10);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("knowledgeId", knowledgeId);
        info.put("knowledgeName", knowledgeName);
        page.put("qaKnowledgeInfo", info);
        return page;
    }

    private Map<String, Object> qaPair(String qaPairId, String knowledgeId, String question, String answer, boolean enabled) {
        Map<String, Object> pair = new LinkedHashMap<>();
        pair.put("qaPairId", qaPairId);
        pair.put("knowledgeId", knowledgeId);
        pair.put("question", question);
        pair.put("answer", answer);
        pair.put("metaDataList", Collections.emptyList());
        pair.put("author", "admin");
        pair.put("uploadTime", "2026-06-30 00:00:00");
        pair.put("status", 2);
        pair.put("switch", enabled);
        pair.put("errorMsg", "");
        return pair;
    }

    private Map<String, Object> qaHitResult(String qaPairId, String knowledgeId, String knowledgeName,
                                            String question, String answer) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", question);
        result.put("question", question);
        result.put("answer", answer);
        result.put("qaPairId", qaPairId);
        result.put("qaBase", knowledgeName);
        result.put("qaId", knowledgeId);
        result.put("contentType", "qa");
        return result;
    }

    private Map<String, Object> uploadLimit(String type, int maxSize, String... ext) {
        Map<String, Object> limit = new LinkedHashMap<>();
        limit.put("fileType", type);
        limit.put("maxSize", maxSize);
        limit.put("extList", java.util.Arrays.asList(ext));
        return limit;
    }

    private Map<String, Object> urlAnalysis(String url, String fileName) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("url", url);
        item.put("fileName", fileName);
        item.put("fileSize", 0);
        return singleton("urlList", Collections.singletonList(item));
    }

    private Map<String, Object> segmentPage(String fileName, Map<String, Object> segment) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("fileName", fileName);
        page.put("pageTotal", 1);
        page.put("segmentTotalNum", 1);
        page.put("maxSegmentSize", 500);
        page.put("segmentType", "0");
        page.put("uploadTime", "2026-06-30 00:00:00");
        page.put("splitter", "");
        page.put("metaDataList", Collections.emptyList());
        page.put("contentList", Collections.singletonList(segment));
        page.put("segmentImportStatus", "");
        page.put("segmentMethod", "0");
        page.put("docAnalyzerText", Collections.singletonList(singleton("text", "text")));
        return page;
    }

    private Map<String, Object> segment(String contentId, String content, boolean available) {
        Map<String, Object> segment = new LinkedHashMap<>();
        segment.put("contentId", contentId);
        segment.put("content", content);
        segment.put("contentNum", 1);
        segment.put("available", available);
        segment.put("labels", Collections.emptyList());
        segment.put("isParent", false);
        segment.put("childNum", 0);
        return segment;
    }

    private Map<String, Object> knowledgeUser(String permissionId, String userId, int permissionType) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("permissionId", permissionId);
        user.put("userId", userId);
        user.put("userName", "admin");
        user.put("orgId", "default-org");
        user.put("orgName", "Default Organization");
        user.put("permissionType", permissionType);
        user.put("transfer", true);
        return user;
    }

    private Map<String, Object> orgInfoBrief(String orgId, String orgName) {
        Map<String, Object> org = new LinkedHashMap<>();
        org.put("orgId", orgId);
        org.put("orgName", orgName);
        return org;
    }

    private Map<String, Object> orgUsers(String orgId, String orgName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orgId", orgId);
        result.put("orgName", orgName);
        result.put("userInfoList", Collections.singletonList(idNameUser("dev-app", "app")));
        return result;
    }

    private Map<String, Object> idNameUser(String userId, String userName) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userId", userId);
        user.put("userName", userName);
        return user;
    }

    private Map<String, Object> idName(String id, String name) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", id);
        value.put("name", name);
        return value;
    }

    private Map<String, Object> userInfo(String userId, String username) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userId", userId);
        user.put("username", username);
        user.put("nickname", username);
        user.put("phone", "");
        user.put("email", "");
        user.put("gender", "");
        user.put("remark", "development account");
        user.put("company", "Wanwu Java");
        user.put("createdAt", "2026-06-30 00:00:00");
        user.put("creator", idName("system", "System"));
        user.put("status", true);
        user.put("language", idName("zh", "简体中文"));
        user.put("avatar", Collections.singletonMap("path", ""));

        Map<String, Object> orgRole = new LinkedHashMap<>();
        orgRole.put("org", idName("default-org", "Default Organization"));
        orgRole.put("roles", Collections.singletonList(idName("admin", "System Admin")));
        user.put("orgs", Collections.singletonList(orgRole));
        return user;
    }

    private Map<String, Object> roleInfo(String roleId, String name) {
        Map<String, Object> role = new LinkedHashMap<>();
        role.put("roleId", roleId);
        role.put("name", name);
        role.put("remark", "Built-in development administrator");
        role.put("createdAt", "2026-06-30 00:00:00");
        role.put("creator", idName("system", "System"));
        role.put("status", true);
        role.put("isAdmin", true);
        role.put("routes", roleTemplate().get("routes"));
        role.put("permissions", java.util.Arrays.asList(
                permission("permission"),
                permission("permission.user"),
                permission("permission.org"),
                permission("permission.role")
        ));
        return role;
    }

    private Map<String, Object> roleTemplate() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", "Permission");
        root.put("perm", "permission");
        root.put("children", java.util.Arrays.asList(
                route("Users", "permission.user"),
                route("Organizations", "permission.org"),
                route("Roles", "permission.role")
        ));

        Map<String, Object> template = new LinkedHashMap<>();
        template.put("routes", Collections.singletonList(root));
        return template;
    }

    private Map<String, Object> route(String name, String perm) {
        Map<String, Object> route = new LinkedHashMap<>();
        route.put("name", name);
        route.put("perm", perm);
        route.put("children", Collections.emptyList());
        return route;
    }

    private Map<String, Object> orgInfo(String orgId, String name) {
        Map<String, Object> org = new LinkedHashMap<>();
        org.put("orgId", orgId);
        org.put("name", name);
        org.put("remark", "Default development organization");
        org.put("creator", idName("system", "System"));
        org.put("createdAt", "2026-06-30 00:00:00");
        org.put("status", true);
        return org;
    }

    private Map<String, Object> org(String id, String name) {
        Map<String, Object> org = new LinkedHashMap<>();
        org.put("id", id);
        org.put("name", name);
        return org;
    }

    private Map<String, Object> permission(String perm) {
        Map<String, Object> permission = new LinkedHashMap<>();
        permission.put("perm", perm);
        return permission;
    }

    private Map<String, Object> orgPermission() {
        Map<String, Object> orgPermission = new LinkedHashMap<>();
        orgPermission.put("org", org("default-org", "Default Organization"));
        orgPermission.put("permissions", java.util.Arrays.asList(
                permission("permission"),
                permission("permission.user"),
                permission("permission.org"),
                permission("permission.role"),
                permission("model"),
                permission("model.model_management"),
                permission("app"),
                permission("app.rag"),
                permission("app.workflow"),
                permission("app.agent"),
                permission("api_key"),
                permission("api_key.api_key_management"),
                permission("resource"),
                permission("resource.knowledge"),
                permission("resource.tool"),
                permission("resource.mcp"),
                permission("resource.prompt"),
                permission("resource.skill")
        ));
        orgPermission.put("roles", Collections.singletonList("admin"));
        orgPermission.put("isAdmin", true);
        orgPermission.put("isSystem", true);
        return orgPermission;
    }

    private ModelInfo modelInfo(String modelId, String displayName, String modelType) {
        ModelInfo info = new ModelInfo();
        info.setModelId(modelId);
        info.setUuid("uuid-" + modelId);
        info.setProvider("DeepSeek");
        info.setModel("deepseek-chat");
        info.setModelType(modelType);
        info.setDisplayName(displayName);
        info.setAvatar(Collections.singletonMap("path", ""));
        info.setPublishDate("2026-06-30");
        info.setIsActive(true);
        info.setUserId("dev-admin");
        info.setOrgId("default-org");
        info.setCreatedAt("2026-06-30 00:00:00");
        info.setUpdatedAt("2026-06-30 00:00:00");
        info.setModelDesc("Docker development model");
        info.setTags(Collections.singletonList(Collections.singletonMap("text", "LLM")));
        info.setConfig(Collections.singletonMap("apiKey", "dev-model-key"));
        info.setScopeType("1");
        info.setAllowEdit(true);
        info.setImportSource("builtin");
        return info;
    }

    private ModelExperienceDialogInfo modelExperienceDialog(String id, String modelId, String sessionId, String title) {
        ModelExperienceDialogInfo info = new ModelExperienceDialogInfo();
        info.setId(id);
        info.setModelId(modelId);
        info.setSessionId(sessionId);
        info.setTitle(title);
        info.setModelSetting("{\"temperature\":0.7}");
        info.setCreatedAt(1782806400000L);
        return info;
    }

    private ModelExperienceDialogRecordInfo modelExperienceRecord(
            String id, String modelId, String sessionId, String content, String reasoning, String role) {
        ModelExperienceDialogRecordInfo info = new ModelExperienceDialogRecordInfo();
        info.setModelExperienceId(id);
        info.setModelId(modelId);
        info.setSessionId(sessionId);
        info.setOriginalContent(content);
        info.setReasoningContent(reasoning);
        info.setRole(role);
        return info;
    }

    private ProviderModelTypeInfo providerInfo() {
        ProviderModelTypeInfo provider = new ProviderModelTypeInfo();
        provider.setKey("DeepSeek");
        provider.setName("DeepSeek");
        provider.setChildren(Collections.singletonList(new ModelTypeInfo("llm", "文本生成")));
        return provider;
    }

    private RecommendModelInfo recommendInfo() {
        RecommendModelInfo info = new RecommendModelInfo();
        info.setModel("deepseek-chat");
        info.setDisplayName("DeepSeek Chat");
        info.setTags(Collections.singletonList(Collections.singletonMap("text", "Tool call")));
        info.setVisionSupport("noSupport");
        info.setFunctionCalling("toolCall");
        info.setThinkingSupport("support");
        return info;
    }

    private Map<String, Object> listResult(Map<String, Object> item) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", Collections.singletonList(item));
        result.put("total", 1);
        return result;
    }

    private Map<String, Object> emptyListResult() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", Collections.emptyList());
        result.put("total", 0);
        return result;
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }

    private Map<String, Object> skill(String skillId, String name, String type) {
        Map<String, Object> skill = new LinkedHashMap<>();
        skill.put("skillId", skillId);
        skill.put("name", name);
        skill.put("skillName", name);
        skill.put("skillType", type);
        skill.put("avatar", Collections.singletonMap("path", ""));
        skill.put("author", "Wanwu");
        skill.put("desc", "Development skill");
        skill.put("variables", Collections.emptyList());
        return skill;
    }

    private Map<String, Object> skillSelect(String skillId, String name, String type) {
        Map<String, Object> skill = skill(skillId, name, type);
        skill.remove("name");
        return skill;
    }

    private Map<String, Object> toolSelect(String toolId, String name, String type) {
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("uniqueId", type + "_" + toolId);
        tool.put("toolId", toolId);
        tool.put("toolName", name);
        tool.put("toolType", type);
        tool.put("desc", "Development tool");
        tool.put("needApiKeyInput", false);
        tool.put("apiKey", "");
        tool.put("avatar", Collections.singletonMap("path", ""));
        return tool;
    }

    private Map<String, Object> mcpSelect(String mcpId, String name, String type) {
        Map<String, Object> mcp = new LinkedHashMap<>();
        mcp.put("uniqueId", type + "_" + mcpId);
        mcp.put("mcpId", mcpId);
        mcp.put("name", name);
        mcp.put("type", type);
        mcp.put("toolId", mcpId);
        mcp.put("toolName", name);
        mcp.put("toolType", type);
        mcp.put("description", "Development MCP");
        mcp.put("avatar", Collections.singletonMap("path", ""));
        return mcp;
    }

    private Map<String, Object> workflowSelect(String workflowId, String name) {
        Map<String, Object> workflow = new LinkedHashMap<>();
        workflow.put("uniqueId", "workflow_" + workflowId);
        workflow.put("workFlowId", workflowId);
        workflow.put("appId", workflowId);
        workflow.put("appType", "workflow");
        workflow.put("name", name);
        workflow.put("desc", "Development workflow");
        workflow.put("avatar", Collections.singletonMap("path", ""));
        return workflow;
    }

    private Map<String, Object> actionList(String actionName) {
        Map<String, Object> action = action(actionName);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("actions", Collections.singletonList(action));
        return result;
    }

    private Map<String, Object> actionDetail(String actionName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("needApiKeyInput", false);
        result.put("apiKey", "");
        result.put("action", action(actionName));
        return result;
    }

    private Map<String, Object> action(String actionName) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("name", actionName);
        action.put("description", "Development action");
        action.put("inputSchema", Collections.singletonMap("type", "object"));
        return action;
    }

    private Map<String, Object> platformConfig() {
        Map<String, Object> email = new LinkedHashMap<>();
        email.put("status", false);
        Map<String, Object> loginEmail = new LinkedHashMap<>();
        loginEmail.put("email", email);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("loginEmail", loginEmail);
        return config;
    }

    private AppVersionInfo versionInfo(String version, String desc, String createdAt, String publishType) {
        AppVersionInfo info = new AppVersionInfo();
        info.setVersion(version);
        info.setDesc(desc);
        info.setCreatedAt(createdAt);
        info.setPublishType(publishType);
        return info;
    }

    private AppUrlInfo appUrlInfo(String urlId, String appId, String suffix) {
        AppUrlInfo info = new AppUrlInfo();
        info.setUrlId(urlId);
        info.setAppId(appId);
        info.setAppType("agent");
        info.setName("Public demo");
        info.setCreatedAt("2026-06-29 10:00:00");
        info.setExpiredAt("2026-07-01 12:30:00");
        info.setSuffix(suffix);
        info.setStatus(true);
        info.setUserId("dev-admin");
        info.setOrgId("default-org");
        info.setDescription("open desc");
        return info;
    }

    private ApiKeyInfo apiKeyInfo(String keyId, String key, String name, boolean status) {
        ApiKeyInfo info = new ApiKeyInfo();
        info.setKeyId(keyId);
        info.setKey(key);
        info.setName(name);
        info.setDesc("first");
        info.setExpiredAt("2030-01-01");
        info.setCreatedAt("2026-06-29 10:00:00");
        info.setCreator("admin");
        info.setStatus(status);
        info.setUserId("dev-admin");
        info.setOrgId("default-org");
        return info;
    }

    private AppKeyInfo appKeyInfo(String apiId, String apiKey, String appId) {
        AppKeyInfo info = new AppKeyInfo();
        info.setApiId(apiId);
        info.setApiKey(apiKey);
        info.setAppId(appId);
        info.setAppType("agent");
        info.setCreatedAt("2026-06-29 10:00:00");
        info.setUserId("dev-admin");
        info.setOrgId("default-org");
        return info;
    }
}
