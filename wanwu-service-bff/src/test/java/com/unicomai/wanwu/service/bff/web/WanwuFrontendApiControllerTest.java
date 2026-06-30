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
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.AppVersionRollbackCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationOption;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuFrontendApiControllerTest {

    private final IamService iamService = mock(IamService.class);
    private final AppService appService = mock(AppService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuFrontendApiController(iamService, appService))
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
                .andExpect(jsonPath("$.data.orgPermission.permissions[0].perm").value("app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[1].perm").value("app.agent"))
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
    public void editorSelectEndpointsReturnEmptyListsForFrontend() throws Exception {
        mockMvc.perform(get("/user/api/v1/model/select/llm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list", hasSize(0)))
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(get("/user/api/v1/prompt/template/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list", hasSize(0)));

        mockMvc.perform(post("/user/api/v1/knowledge/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list", hasSize(0)));
    }

    @Test
    public void appOpenUrlReturnsEmptyStringUntilPublishIsImplemented() throws Exception {
        mockMvc.perform(get("/user/api/v1/appspace/app/url")
                        .param("appId", "assistant-001")
                        .param("appType", "agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(""));
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
        orgPermission.put("permissions", java.util.Arrays.asList(permission("app"), permission("app.agent")));
        orgPermission.put("roles", Collections.singletonList("admin"));
        orgPermission.put("isAdmin", true);
        orgPermission.put("isSystem", true);
        return orgPermission;
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
}
