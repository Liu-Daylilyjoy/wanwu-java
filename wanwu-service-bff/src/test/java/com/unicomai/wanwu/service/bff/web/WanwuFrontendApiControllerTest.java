package com.unicomai.wanwu.service.bff.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
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
import com.unicomai.wanwu.api.app.dto.AppTypeConvertCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationInfoQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RagCopyCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateResult;
import com.unicomai.wanwu.api.app.dto.RagDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RagUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
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
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordSaveCommand;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.model.dto.ModelTypeQuery;
import com.unicomai.wanwu.api.model.dto.ModelTypeInfo;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeInfo;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeResult;
import com.unicomai.wanwu.api.model.dto.RecommendModelInfo;
import com.unicomai.wanwu.api.model.dto.RecommendModelResult;
import com.unicomai.wanwu.api.operate.OperateService;
import com.unicomai.wanwu.api.safety.SafetyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuFrontendApiControllerTest {

    @AfterEach
    public void clearOperationClientStatisticStore() {
        OperationClientStatisticStore.INSTANCE.clear();
        ExplorationAppHistoryStore.INSTANCE.clear();
    }

    private final IamService iamService = mock(IamService.class);
    private final AppService appService = mock(AppService.class);
    private final ModelService modelService = mock(ModelService.class);
    private final KnowledgeService knowledgeService = mock(KnowledgeService.class);
    private final McpService mcpService = mock(McpService.class);
    private final OperateService operateService = mock(OperateService.class);
    private final SafetyService safetyService = mock(SafetyService.class);
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(
                new WanwuFrontendApiController(iamService, appService, modelService, knowledgeService, mcpService,
                        operateService, safetyService),
                new WanwuResourceApiController(mcpService, modelService),
                new WanwuSkillApiController(mcpService, modelService),
                new WanwuSafetyApiController(safetyService),
                new WanwuSettingApiController(operateService),
                    new WanwuOperationApiController(iamService, operateService),
                    new WanwuExplorationApiController(appService),
                    new WanwuStatisticApiController(appService, modelService),
                    new WanwuTemplateApiController(appService))
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
                .andExpect(jsonPath("$.data.orgPermission.permissions[4].perm").value("setting"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[5].perm").value("model"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[6].perm").value("model.model_management"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[7].perm").value("app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[8].perm").value("app.rag"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[9].perm").value("app.workflow"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[10].perm").value("app.agent"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[11].perm").value("api_key"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[12].perm").value("api_key.api_key_management"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[13].perm").value("resource"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[14].perm").value("resource.knowledge"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[15].perm").value("resource.tool"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[16].perm").value("resource.mcp"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[17].perm").value("resource.prompt"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[18].perm").value("resource.skill"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[19].perm").value("resource.safety"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[20].perm").value("operation"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[21].perm").value("operation.oauth"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[22].perm").value("operation.statistic_client"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[23].perm").value("exploration"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[24].perm").value("exploration.app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[25].perm").value("exploration.mcp"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[26].perm").value("exploration.template"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[27].perm").value("exploration.skill"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[28].perm").value("app_observability"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[29].perm").value("app_observability.statistic"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[30]").doesNotExist())
                .andExpect(jsonPath("$.data.custom.loginEmail.email.status").value(false));

        verify(iamService).login(any(LoginCommand.class));
    }

    @Test
    public void permissionAndOrgSelectFallbackWhenIamUnavailable() throws Exception {
        when(iamService.permission("dev-token")).thenThrow(new IllegalStateException("iam offline"));
        when(iamService.permission("dev-token-app")).thenThrow(new IllegalStateException("iam offline"));
        when(iamService.selectOrganizations()).thenThrow(new IllegalStateException("iam offline"));

        mockMvc.perform(get("/user/api/v1/user/permission")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orgPermission.org.id").value("default-org"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[0].perm").value("permission"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[29].perm").value("app_observability.statistic"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[30]").doesNotExist())
                .andExpect(jsonPath("$.data.orgPermission.isAdmin").value(true))
                .andExpect(jsonPath("$.data.isUpdatePassword").value(true))
                .andExpect(jsonPath("$.data.language.code").value("zh"))
                .andExpect(jsonPath("$.data.avatar.path").value(""));

        mockMvc.perform(get("/user/api/v1/user/permission")
                        .header("Authorization", "Bearer dev-token-app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orgPermission.org.id").value("default-org"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[0].perm").value("app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[1].perm").value("app.rag"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[2].perm").value("app.workflow"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[3].perm").value("app.agent"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[4]").doesNotExist())
                .andExpect(jsonPath("$.data.orgPermission.isAdmin").value(false));

        mockMvc.perform(get("/user/api/v1/org/select"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.select[0].id").value("default-org"))
                .andExpect(jsonPath("$.data.select[0].name").value("Default Organization"));
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
    public void assistantListLegacyAliasReturnsEmptyFrontendList() throws Exception {
        when(appService.listAssistants(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.emptyList()));

        mockMvc.perform(get("/user/api/v1/assistant/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list", hasSize(0)));

        verify(appService).listAssistants(any(ApplicationListQuery.class));
    }

    @Test
    public void settingRoutesUpdatePlatformConfig() throws Exception {
        mockMvc.perform(post("/user/api/v1/custom/tab")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tabTitle\":\"Smoke Tab\",\"tabLogo\":{\"path\":\"/tab.png\",\"key\":\"tab.png\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/user/api/v1/custom/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginBg\":{\"path\":\"/bg.png\",\"key\":\"bg.png\"},\"loginLogo\":{\"path\":\"/logo.png\",\"key\":\"logo.png\"},\"loginWelcomeText\":\"Welcome\",\"loginButtonColor\":\"#111111\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/user/api/v1/custom/home")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"homeName\":\"Smoke Home\",\"homeLogo\":{\"path\":\"/home.png\",\"key\":\"home.png\"},\"homeBgColor\":\"#ffffff\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(operateService).createSystemCustomTab(anyString(), anyString(), anyString(), any(Map.class));
        verify(operateService).createSystemCustomLogin(anyString(), anyString(), anyString(), any(Map.class));
        verify(operateService).createSystemCustomHome(anyString(), anyString(), anyString(), any(Map.class));
    }

    @Test
    public void baseCustomReadsOperatePlatformConfig() throws Exception {
        when(operateService.getSystemCustom("light"))
                .thenReturn(platformConfig(), customPlatformConfig());

        mockMvc.perform(get("/user/api/v1/base/custom")
                        .header("x-language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.login.welcomeText")
                        .value("Hi! Welcome to Yuanjing Wanwu Intelligent Body Platform"))
                .andExpect(jsonPath("$.data.login.platformDesc").value(""))
                .andExpect(jsonPath("$.data.home.title").value("Yuanjing Wanwu Intelligent Body Platform"))
                .andExpect(jsonPath("$.data.tab.title").value("Yuanjing Wanwu"))
                .andExpect(jsonPath("$.data.about.copyright").value("© 大模型开源平台"))
                .andExpect(jsonPath("$.data.loginEmail.email.status").value(false));

        mockMvc.perform(get("/user/api/v1/base/custom")
                        .header("x-language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login.welcomeText").value("Custom welcome"))
                .andExpect(jsonPath("$.data.home.title").value("Custom home"))
                .andExpect(jsonPath("$.data.tab.title").value("Custom tab"));

        verify(operateService, times(2)).getSystemCustom("light");
    }

    @Test
    public void operationRoutesManageOauthAppsAndClientStatistic() throws Exception {
        OperationClientStatisticStore.INSTANCE.clear();
        OperationClientStatisticStore.INSTANCE.recordBrowse("oauth-client-1", LocalDate.of(2026, 6, 15));
        Map<String, Object> oauthApp = map("clientId", "oauth-client-1",
                "name", "Console",
                "desc", "dev oauth",
                "clientSecret", "oauth-secret-1",
                "redirectUri", "http://localhost/callback",
                "status", true,
                "createdAt", "2026-06-15 10:00:00");
        when(iamService.listOauthApps(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(listResult(oauthApp));
        when(operateService.getClientStatistic(anyString(), anyString()))
                .thenReturn(OperationClientStatisticStore.INSTANCE.clientStatistic(
                        listResult(oauthApp), "2026-06-01", "2026-06-30"));

        mockMvc.perform(post("/user/api/v1/oauth/app")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Console\",\"desc\":\"dev oauth\",\"redirectUri\":\"http://localhost/callback\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/user/api/v1/oauth/app/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("name", "Console")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].clientId").value("oauth-client-1"))
                .andExpect(jsonPath("$.data.list[0].clientSecret").value("oauth-secret-1"));
        mockMvc.perform(put("/user/api/v1/oauth/app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\":\"oauth-client-1\",\"name\":\"Console 2\",\"desc\":\"dev oauth\",\"redirectUri\":\"http://localhost/callback2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(put("/user/api/v1/oauth/app/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\":\"oauth-client-1\",\"status\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(delete("/user/api/v1/oauth/app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\":\"oauth-client-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/user/api/v1/statistic/client")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.overview.cumulativeClient.value").value(1.0))
                .andExpect(jsonPath("$.data.overview.additionClient.value").value(1.0))
                .andExpect(jsonPath("$.data.overview.activeClient.value").value(1.0))
                .andExpect(jsonPath("$.data.overview.browse.value").value(1.0))
                .andExpect(jsonPath("$.data.trend.client.lines[0].items[0].key").value("2026-06-01"))
                .andExpect(jsonPath("$.data.trend.client.lines[0].items[14].value").value(1.0))
                .andExpect(jsonPath("$.data.trend.browse.lines[0].items[14].value").value(1.0));

        verify(iamService).createOauthApp(anyString(), any(Map.class));
        verify(iamService, times(2)).listOauthApps(anyString(), anyString(), anyInt(), anyInt());
        verify(iamService).updateOauthApp(any(Map.class));
        verify(iamService).updateOauthAppStatus(any(Map.class));
        verify(iamService).deleteOauthApp(any(Map.class));
        verify(operateService).getClientStatistic("2026-06-01", "2026-06-30");
    }

    @Test
    public void statisticDashboardRoutesReturnFrontendContracts() throws Exception {
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("appId", "assistant-001");
        app.put("appType", "agent");
        app.put("name", "Agent One");
        app.put("appName", "Agent One");
        app.put("avatar", Collections.singletonMap("path", ""));
        when(appService.listApplications(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(app), 1));
        when(modelService.listModels(any(ModelListQuery.class)))
                .thenReturn(new ModelListResult(Collections.singletonList(modelInfo("model-001", "DeepSeek Chat", "llm")), 1));
        when(appService.listApiKeys(any(ApiKeyListQuery.class)))
                .thenReturn(new ApiKeyPageResult(
                        Collections.singletonList(apiKeyInfo("key-001", "wanwu_api_001", "Main key", true)),
                        1,
                        1,
                        20));

        mockMvc.perform(get("/user/api/v1/statistic/app/select")
                        .header("Authorization", "Bearer dev-token")
                        .param("appType", "agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].appId").value("assistant-001"))
                .andExpect(jsonPath("$.data.list[0].name").value("Agent One"));
        mockMvc.perform(get("/user/api/v1/statistic/app")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overview.callCount.value").value(0))
                .andExpect(jsonPath("$.data.trend.callTrend.lines[0].items[0].key").value("2026-06-01"));
        mockMvc.perform(get("/user/api/v1/statistic/app/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("appType", "agent")
                        .param("apps", "assistant-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].appName").value("Agent One"))
                .andExpect(jsonPath("$.data.list[0].callCount").value(0));

        mockMvc.perform(get("/user/api/v1/statistic/model")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overview.totalTokensTotal.value").value(0))
                .andExpect(jsonPath("$.data.trend.modelCalls.lines[0].items[0].key").value("2026-06-01"))
                .andExpect(jsonPath("$.data.trend.tokensUsage.lines[0].items[0].key").value("2026-06-01"));
        mockMvc.perform(get("/user/api/v1/statistic/model/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("modelType", "llm")
                        .param("models", "model-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].model").value("DeepSeek Chat"))
                .andExpect(jsonPath("$.data.list[0].totalTokens").value(0));

        mockMvc.perform(get("/user/api/v1/statistic/api/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].keyId").value("key-001"))
                .andExpect(jsonPath("$.data.list[0].apiKey").value("wanwu_api_001"));
        mockMvc.perform(get("/user/api/v1/statistic/api/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].method").value("POST"))
                .andExpect(jsonPath("$.data.list[0].path").value("/service/api/openapi/v1/agent/chat"));
        mockMvc.perform(post("/user/api/v1/statistic/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-06-01\",\"endDate\":\"2026-06-02\",\"apiKeyIds\":[\"ALL\"],\"methodPaths\":[\"POST-/service/api/openapi/v1/agent/chat\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overview.callCount.value").value(0))
                .andExpect(jsonPath("$.data.trend.apiCalls.lines[0].items[0].key").value("2026-06-01"));
        mockMvc.perform(post("/user/api/v1/statistic/api/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apiKeyIds\":[\"key-001\"],\"methodPaths\":[\"POST-/service/api/openapi/v1/agent/chat\"],\"pageNo\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
        mockMvc.perform(post("/user/api/v1/statistic/api/record")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apiKeyIds\":[\"key-001\"],\"methodPaths\":[\"POST-/service/api/openapi/v1/agent/chat\"],\"pageNo\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(get("/user/api/v1/statistic/app/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".xlsx")));
        mockMvc.perform(get("/user/api/v1/statistic/model/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".xlsx")));
        mockMvc.perform(post("/user/api/v1/statistic/api/list/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".xlsx")));

        verify(appService, times(2)).listApplications(any(ApplicationListQuery.class));
        verify(modelService, times(1)).listModels(any(ModelListQuery.class));
        verify(appService, times(4)).listApiKeys(any(ApiKeyListQuery.class));
    }

    @Test
    public void templateRoutesReturnFrontendContractsAndCopyApps() throws Exception {
        when(appService.createAssistant(any(AssistantCreateCommand.class)))
                .thenReturn(new AssistantCreateResult("assistant-template-copy-001"));
        when(appService.createWorkflow(any(WorkflowCreateCommand.class)))
                .thenReturn(new WorkflowCreateResult("workflow-template-copy-001"));

        mockMvc.perform(get("/user/api/v1/assistant/template/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("category", "industry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[0].assistantTemplateId").value("assistant-template-policy"))
                .andExpect(jsonPath("$.data.list[0].appType").value("agentTemplate"))
                .andExpect(jsonPath("$.data.list[0].name").value("Policy Analyst"));

        mockMvc.perform(get("/user/api/v1/assistant/template/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("category", "tourism"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(6))
                .andExpect(jsonPath("$.data.list[0].assistantTemplateId").value("cultural_tourism_research_agent"));

        mockMvc.perform(get("/user/api/v1/assistant/template")
                        .param("assistantTemplateId", "assistant-template-policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assistantTemplateId").value("assistant-template-policy"))
                .andExpect(jsonPath("$.data.prologue").value("I can help read policy documents."))
                .andExpect(jsonPath("$.data.recommendQuestion[0]").value("What is the goal?"));

        mockMvc.perform(get("/user/api/v1/assistant/template")
                        .param("assistantTemplateId", "cultural_tourism_research_agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assistantTemplateId").value("cultural_tourism_research_agent"))
                .andExpect(jsonPath("$.data.category").value("tourism"))
                .andExpect(jsonPath("$.data.prologue").value("你好，我是文旅信息检索助手。你可以问我景区玩法、路线、活动、附近酒店餐厅、开放信息和出行注意事项。"))
                .andExpect(jsonPath("$.data.recommendQuestion[0]").value("帮我检索莫高窟和鸣沙山月牙泉两天一晚玩法"));

        mockMvc.perform(post("/user/api/v1/assistant/template")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantTemplateId\":\"assistant-template-policy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assistantId").value("assistant-template-copy-001"));

        mockMvc.perform(post("/user/api/v1/assistant/template")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantTemplateId\":\"cultural_tourism_research_agent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assistantId").value("assistant-template-copy-001"));

        mockMvc.perform(get("/user/api/v1/workflow/template/list")
                        .param("category", "office"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.downloadLink.url").value(""))
                .andExpect(jsonPath("$.data.list[0].templateId").value("workflow-template-doc-review"))
                .andExpect(jsonPath("$.data.list[0].name").value("Document Review"));

        mockMvc.perform(get("/user/api/v1/workflow/template/list")
                        .param("category", "gov"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.list[0].templateId").value("policy_assistant"));

        mockMvc.perform(get("/user/api/v1/workflow/template/detail")
                        .param("templateId", "workflow-template-doc-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateId").value("workflow-template-doc-review"))
                .andExpect(jsonPath("$.data.summary").value("Review uploaded documents and return a structured checklist."));

        mockMvc.perform(get("/user/api/v1/workflow/template/detail")
                        .param("templateId", "policy_assistant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateId").value("policy_assistant"))
                .andExpect(jsonPath("$.data.category").value("gov"))
                .andExpect(jsonPath("$.data.schema.nodes[0].id").value("100001"));

        mockMvc.perform(get("/user/api/v1/workflow/template/recommend")
                        .param("templateId", "workflow-template-doc-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list[0].templateId").value("workflow-template-faq"));

        mockMvc.perform(get("/user/api/v1/workflow/template/download")
                        .param("templateId", "workflow-template-doc-review"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("workflow-template-doc-review")));

        mockMvc.perform(post("/user/api/v1/workflow/template")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\":\"workflow-template-doc-review\",\"name\":\"Doc Copy\",\"desc\":\"copy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-template-copy-001"))
                .andExpect(jsonPath("$.data.workflowId").value("workflow-template-copy-001"));

        mockMvc.perform(post("/user/api/v1/workflow/template")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\":\"policy_assistant\",\"name\":\"Policy Copy\",\"desc\":\"copy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-template-copy-001"))
                .andExpect(jsonPath("$.data.workflowId").value("workflow-template-copy-001"));

        verify(appService, times(2)).createAssistant(any(AssistantCreateCommand.class));
        verify(appService, times(2)).updateAssistantConfig(any(AssistantConfigUpdateCommand.class));
        verify(appService, times(2)).createWorkflow(any(WorkflowCreateCommand.class));
        verify(appService).recordAppTemplateDownload(eq("workflow"), eq("workflow-template-doc-review"));
    }

    @Test
    public void explorationRoutesReturnAppSquareContractsAndFavoriteState() throws Exception {
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("appId", "assistant-001");
        app.put("appType", "agent");
        app.put("name", "Agent One");
        app.put("desc", "public agent");
        app.put("publishType", "public");
        app.put("createdAt", "2026-06-30 00:00:00");
        when(appService.listApplications(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(app), 1));
        AssistantConversationStreamResult stream = new AssistantConversationStreamResult();
        stream.setAssistantId("assistant-001");
        stream.setConversationId("conversation-001");
        stream.setDetailId("detail-001");
        stream.setPrompt("hello");
        stream.setResponse("hi");
        stream.setCreatedAt(1782806400000L);
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class)))
                .thenReturn(stream);

        mockMvc.perform(get("/user/api/v1/exploration/app/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("appType", "agent")
                        .param("searchType", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].appId").value("assistant-001"))
                .andExpect(jsonPath("$.data.list[0].isFavorite").value(false))
                .andExpect(jsonPath("$.data.list[0].user.userName").value("Wanwu"));

        mockMvc.perform(post("/user/api/v1/exploration/app/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"assistant-001\",\"appType\":\"agent\",\"isFavorite\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/user/api/v1/exploration/app/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("appType", "agent")
                        .param("searchType", "favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].isFavorite").value(true));
        mockMvc.perform(post("/user/api/v1/assistant/stream")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"prompt\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hi")));
        mockMvc.perform(get("/user/api/v1/exploration/app/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("searchType", "history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].appId").value("assistant-001"))
                .andExpect(jsonPath("$.data.list[0].visitedAt").exists());
        mockMvc.perform(get("/user/api/v1/exploration/app/history")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].appId").value("assistant-001"))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(appService, times(4)).listApplications(any(ApplicationListQuery.class));
        verify(appService).streamAssistantConversation(any(AssistantConversationStreamCommand.class));
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
        WorkflowRunResult workflowRun = new WorkflowRunResult("workflow-001", Collections.singletonMap("answer", "ok"));
        workflowRun.setRunId("workflow-run-001");
        workflowRun.setStatus("success");
        workflowRun.setCreatedAt(100L);
        workflowRun.setFinishedAt(125L);
        workflowRun.setCostMillis(25L);
        when(appService.runWorkflow(any(WorkflowRunCommand.class)))
                .thenReturn(workflowRun);

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

        mockMvc.perform(post("/user/api/v1/appspace/workflow/convert")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflow_id\":\"workflow-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-001"));

        ArgumentCaptor<AppTypeConvertCommand> convertCaptor = forClass(AppTypeConvertCommand.class);
        verify(appService).convertAppType(convertCaptor.capture());
        assertEquals("workflow-001", convertCaptor.getValue().getAppId());
        assertEquals("workflow", convertCaptor.getValue().getOldAppType());
        assertEquals("chatflow", convertCaptor.getValue().getNewAppType());
        assertEquals("dev-admin", convertCaptor.getValue().getUserId());
        assertEquals("default-org", convertCaptor.getValue().getOrgId());

        mockMvc.perform(get("/user/api/v1/appspace/workflow/export/draft")
                        .header("Authorization", "Bearer dev-token")
                        .param("workflow_id", "workflow-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", containsString("workflow_export.json")))
                .andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"))
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
                .andExpect(jsonPath("$.data.runId").value("workflow-run-001"))
                .andExpect(jsonPath("$.data.status").value("success"))
                .andExpect(jsonPath("$.data.costMillis").value(25))
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
    public void chatflowRoutesReturnFrontendContractsAndMapCommands() throws Exception {
        Map<String, Object> chatflowCard = new LinkedHashMap<>();
        chatflowCard.put("appId", "chatflow-001");
        chatflowCard.put("workflowId", "chatflow-001");
        chatflowCard.put("workflow_id", "chatflow-001");
        chatflowCard.put("appType", "chatflow");
        chatflowCard.put("name", "PolicyChat");
        chatflowCard.put("desc", "policy chatflow");
        chatflowCard.put("publishType", "private");
        chatflowCard.put("version", "");

        Map<String, Object> basicInfo = new LinkedHashMap<>();
        basicInfo.put("id", "100001");
        basicInfo.put("name", "PolicyChat");
        Map<String, Object> intelligence = new LinkedHashMap<>();
        intelligence.put("basic_info", basicInfo);
        Map<String, Object> applicationList = new LinkedHashMap<>();
        applicationList.put("intelligences", Collections.singletonList(intelligence));
        applicationList.put("total", 1);
        applicationList.put("has_more", false);
        applicationList.put("next_cursor_id", "");

        Map<String, Object> applicationInfo = new LinkedHashMap<>();
        applicationInfo.put("intelligence_type", 1L);
        applicationInfo.put("basic_info", basicInfo);

        when(appService.createChatflow(any(WorkflowCreateCommand.class)))
                .thenReturn(new WorkflowCreateResult("chatflow-001"));
        when(appService.listApplications(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(chatflowCard), 1));
        when(appService.copyChatflow(any(WorkflowCopyCommand.class)))
                .thenReturn(new WorkflowCreateResult("chatflow-002"));
        when(appService.exportChatflow(any(WorkflowExportQuery.class)))
                .thenReturn(new WorkflowExportResult("PolicyChat", "policy chatflow", "{\"nodes\":[]}"));
        when(appService.importChatflow(any(WorkflowImportCommand.class)))
                .thenReturn(new WorkflowCreateResult("chatflow-003"));
        when(appService.listChatflowApplications(any(ChatflowApplicationListQuery.class)))
                .thenReturn(applicationList);
        when(appService.getChatflowApplication(any(ChatflowApplicationInfoQuery.class)))
                .thenReturn(applicationInfo);

        mockMvc.perform(post("/user/api/v1/appspace/chatflow")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"PolicyChat\",\"desc\":\"policy chatflow\",\"avatar\":{\"key\":\"avatar-key\",\"path\":\"/avatar.png\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("chatflow-001"))
                .andExpect(jsonPath("$.data.workflowId").value("chatflow-001"));

        org.mockito.ArgumentCaptor<WorkflowCreateCommand> createCaptor = forClass(WorkflowCreateCommand.class);
        verify(appService).createChatflow(createCaptor.capture());
        assertEquals("PolicyChat", createCaptor.getValue().getName());
        assertEquals("avatar-key", createCaptor.getValue().getAvatarKey());

        mockMvc.perform(get("/user/api/v1/appspace/workflow/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("appType", "chatflow")
                        .param("name", "Policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].appType").value("chatflow"))
                .andExpect(jsonPath("$.data.list[0].workflow_id").value("chatflow-001"));

        mockMvc.perform(get("/user/api/v1/appspace/chatflow/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("name", "Policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].appType").value("chatflow"))
                .andExpect(jsonPath("$.data.list[0].workflow_id").value("chatflow-001"));

        org.mockito.ArgumentCaptor<ApplicationListQuery> listCaptor = forClass(ApplicationListQuery.class);
        verify(appService, times(2)).listApplications(listCaptor.capture());
        assertEquals("chatflow", listCaptor.getAllValues().get(0).getAppType());
        assertEquals("chatflow", listCaptor.getAllValues().get(1).getAppType());

        mockMvc.perform(post("/user/api/v1/appspace/chatflow/copy")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflow_id\":\"chatflow-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("chatflow-002"));

        mockMvc.perform(post("/user/api/v1/appspace/chatflow/convert")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflow_id\":\"chatflow-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("chatflow-001"));

        ArgumentCaptor<AppTypeConvertCommand> convertCaptor = forClass(AppTypeConvertCommand.class);
        verify(appService).convertAppType(convertCaptor.capture());
        assertEquals("chatflow-001", convertCaptor.getValue().getAppId());
        assertEquals("chatflow", convertCaptor.getValue().getOldAppType());
        assertEquals("workflow", convertCaptor.getValue().getNewAppType());
        assertEquals("dev-admin", convertCaptor.getValue().getUserId());
        assertEquals("default-org", convertCaptor.getValue().getOrgId());

        mockMvc.perform(get("/user/api/v1/appspace/chatflow/export/draft")
                        .header("Authorization", "Bearer dev-token")
                        .param("workflow_id", "chatflow-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", containsString("chatflow_export.json")))
                .andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"))
                .andExpect(content().string(containsString("\"name\":\"PolicyChat\"")));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "chatflow.json",
                "application/json",
                "{\"name\":\"ImportedChat\",\"desc\":\"imported\",\"schema\":\"{\\\"nodes\\\":[]}\"}"
                        .getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/user/api/v1/appspace/chatflow/import")
                        .file(file)
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflow_id").value("chatflow-003"));

        mockMvc.perform(post("/user/api/v1/chatflow/application/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflow_id\":\"chatflow-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.intelligences[0].basic_info.id").value("100001"));

        mockMvc.perform(post("/user/api/v1/chatflow/application/info")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"intelligence_id\":\"100001\",\"intelligence_type\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.basic_info.name").value("PolicyChat"));

        mockMvc.perform(delete("/user/api/v1/chatflow/conversation/delete")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"project_id\":\"chatflow-001\",\"unique_id\":\"100001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/appspace/app")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appId\":\"chatflow-001\",\"appType\":\"chatflow\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(appService).copyChatflow(any(WorkflowCopyCommand.class));
        verify(appService).exportChatflow(any(WorkflowExportQuery.class));
        verify(appService).importChatflow(any(WorkflowImportCommand.class));
        verify(appService).listChatflowApplications(any(ChatflowApplicationListQuery.class));
        verify(appService).getChatflowApplication(any(ChatflowApplicationInfoQuery.class));
        verify(appService).deleteChatflowConversation(any(ChatflowConversationDeleteCommand.class));
        verify(appService).deleteChatflow(any(WorkflowDeleteCommand.class));
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
        when(mcpService.listMcpTools(anyString(), anyString(), any(Map.class)))
                .thenReturn(Collections.<String, Object>singletonMap("tools", Collections.singletonList(action)));
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

        mockMvc.perform(get("/user/api/v1/mcp/tool/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("serverUrl", "http://127.0.0.1/mcp")
                        .param("transport", "streamable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tools[0].name").value("get_weather"));

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
    public void promptOptimizeUsesConfiguredOpenAiCompatibleModelWhenAvailable() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"chatcmpl-prompt\",\"object\":\"chat.completion\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                    + "\"content\":\"optimized by upstream\"},\"finish_reason\":\"stop\"}],"
                    + "\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":3,\"total_tokens\":5}}");
        });
        server.start();
        try {
            ModelInfo model = modelInfo("model-001", "DeepSeek Chat", "llm");
            model.setProvider("openai-compatible");
            model.setConfig(map("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1",
                    "apiKey", "local-key"));
            when(modelService.getModel(anyString(), anyString(), eq("model-001"))).thenReturn(model);

            mockMvc.perform(post("/user/api/v1/prompt/optimize")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"modelId\":\"model-001\",\"prompt\":\"review this\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                    .andExpect(content().string(containsString("\"response\":\"optimized by upstream\"")))
                    .andExpect(content().string(containsString("\"finish\":1")));

            assertEquals("Bearer local-key", authorization.get());
            assertTrue(upstreamBody.get().contains("\"model\":\"deepseek-chat\""));
            assertTrue(upstreamBody.get().contains("Optimize the following prompt"));
            verify(mcpService, times(0)).optimizePrompt(anyString(), anyString(), any(Map.class));
        } finally {
            server.stop(0);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void promptSseRoutesCheckModelIdBeforeGeneration() throws Exception {
        when(mcpService.optimizePrompt(anyString(), anyString(), any(Map.class)))
                .thenReturn(map("response", "optimized prompt", "finish", 1));
        when(mcpService.reasonPrompt(anyString(), anyString(), any(Map.class)))
                .thenReturn(map("response", "reasoned prompt", "finish", 1));
        when(mcpService.evaluatePrompt(anyString(), anyString(), any(Map.class)))
                .thenReturn(map("response", "evaluated prompt", "finish", 1));

        mockMvc.perform(post("/user/api/v1/prompt/optimize")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-001\",\"prompt\":\"review this\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"response\":\"optimized prompt\"")));

        mockMvc.perform(post("/user/api/v1/prompt/reason")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-002\",\"prompt\":\"review this\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"response\":\"reasoned prompt\"")));

        mockMvc.perform(post("/user/api/v1/prompt/evaluate")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-003\",\"prompt\":\"review this\",\"answer\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"response\":\"evaluated prompt\"")));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService, times(3)).checkModelUserPermission(
                eq("dev-admin"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Collections.singletonList("model-001"), modelIdsCaptor.getAllValues().get(0));
        assertEquals(Collections.singletonList("model-002"), modelIdsCaptor.getAllValues().get(1));
        assertEquals(Collections.singletonList("model-003"), modelIdsCaptor.getAllValues().get(2));
        verify(mcpService).optimizePrompt(eq("dev-admin"), eq("default-org"), any(Map.class));
        verify(mcpService).reasonPrompt(eq("dev-admin"), eq("default-org"), any(Map.class));
        verify(mcpService).evaluatePrompt(eq("dev-admin"), eq("default-org"), any(Map.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void promptSseRouteStopsBeforeGenerationWhenModelIsDenied() throws Exception {
        doThrow(new IllegalArgumentException("bff_model_perm: model-private"))
                .when(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), any(List.class));

        mockMvc.perform(post("/user/api/v1/prompt/optimize")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-private\",\"prompt\":\"review this\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":1001")))
                .andExpect(content().string(containsString("\"msg\":\"bff_model_perm: model-private\"")));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Collections.singletonList("model-private"), modelIdsCaptor.getValue());
        verify(mcpService, times(0)).optimizePrompt(anyString(), anyString(), any(Map.class));
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
    @SuppressWarnings("unchecked")
    public void skillConversationChatUsesConfiguredOpenAiCompatibleModelWhenAvailable() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"chatcmpl-skill\",\"object\":\"chat.completion\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                    + "\"content\":\"upstream skill draft\"},\"finish_reason\":\"stop\"}],"
                    + "\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":3,\"total_tokens\":5}}");
        });
        server.start();
        try {
            ModelInfo model = modelInfo("model-001", "DeepSeek Chat", "llm");
            model.setProvider("openai-compatible");
            model.setConfig(map("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1",
                    "apiKey", "local-key"));
            when(modelService.getModel(anyString(), anyString(), eq("model-001"))).thenReturn(model);
            when(mcpService.chatSkillConversation(anyString(), anyString(), any(Map.class)))
                    .thenAnswer(invocation -> map("response",
                            ((Map<String, Object>) invocation.getArgument(2)).get("_responseOverride"),
                            "finish", 1));

            mockMvc.perform(post("/user/api/v1/agent/skill/conversation/chat")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"conversationId\":\"skill-conv-001\",\"query\":\"build one\","
                                    + "\"modelConfig\":{\"modelId\":\"model-001\"}}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                    .andExpect(content().string(containsString("\"response\":\"upstream skill draft\"")));

            assertEquals("Bearer local-key", authorization.get());
            assertTrue(upstreamBody.get().contains("\"model\":\"deepseek-chat\""));
            assertTrue(upstreamBody.get().contains("Generate or refine a Wanwu skill"));
            ArgumentCaptor<Map> requestCaptor = forClass(Map.class);
            verify(mcpService).chatSkillConversation(eq("dev-admin"), eq("default-org"), requestCaptor.capture());
            assertEquals("upstream skill draft", requestCaptor.getValue().get("_responseOverride"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void skillConversationChatStopsBeforeGenerationWhenModelIsDenied() throws Exception {
        doThrow(new IllegalArgumentException("bff_model_perm: model-private"))
                .when(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), any(List.class));

        mockMvc.perform(post("/user/api/v1/agent/skill/conversation/chat")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationId\":\"skill-conv-001\",\"query\":\"build one\","
                                + "\"modelConfig\":{\"modelId\":\"model-private\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":1001")))
                .andExpect(content().string(containsString("\"msg\":\"bff_model_perm: model-private\"")));

        verify(mcpService, times(0)).chatSkillConversation(anyString(), anyString(), any(Map.class));
    }

    @Test
    public void safetyRoutesReturnFrontendContracts() throws Exception {
        Map<String, Object> table = map("tableId", "table-001", "tableName", "Policy Guard",
                "remark", "policy words", "reply", "blocked", "createdAt", "2026-06-30 00:00:00",
                "type", "personal");
        Map<String, Object> word = map("wordId", "word-001", "word", "blocked", "sensitiveType", "Other");

        when(safetyService.createSensitiveWordTable(anyString(), anyString(), any(Map.class)))
                .thenReturn(Collections.<String, Object>singletonMap("tableId", "table-001"));
        when(safetyService.getSensitiveWordTable(anyString(), anyString(), anyString()))
                .thenReturn(table);
        when(safetyService.listSensitiveWordTables(anyString(), anyString(), nullable(String.class)))
                .thenReturn(listResult(table));
        when(safetyService.selectSensitiveWordTables(anyString(), anyString()))
                .thenReturn(listResult(table));
        when(safetyService.listSensitiveWords(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(map("list", Collections.singletonList(word), "total", 1, "pageNo", 1, "pageSize", 10));

        mockMvc.perform(post("/user/api/v1/safe/sensitive/table")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tableName\":\"Policy Guard\",\"remark\":\"policy words\",\"type\":\"personal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tableId").value("table-001"));
        mockMvc.perform(put("/user/api/v1/safe/sensitive/table")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tableId\":\"table-001\",\"tableName\":\"Policy Guard Updated\",\"remark\":\"policy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/user/api/v1/safe/sensitive/table")
                        .header("Authorization", "Bearer dev-token")
                        .param("tableId", "table-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tableName").value("Policy Guard"));
        mockMvc.perform(put("/user/api/v1/safe/sensitive/table/reply")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tableId\":\"table-001\",\"reply\":\"blocked\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/user/api/v1/safe/sensitive/table/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("type", "personal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].tableId").value("table-001"));
        mockMvc.perform(get("/user/api/v1/safe/sensitive/table/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].tableName").value("Policy Guard"));
        mockMvc.perform(post("/user/api/v1/safe/sensitive/word")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tableId\":\"table-001\",\"importType\":\"single\",\"word\":\"blocked\",\"sensitiveType\":\"Other\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/user/api/v1/safe/sensitive/word/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("tableId", "table-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].wordId").value("word-001"));
        mockMvc.perform(delete("/user/api/v1/safe/sensitive/word")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tableId\":\"table-001\",\"wordId\":\"word-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(delete("/user/api/v1/safe/sensitive/table")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tableId\":\"table-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    public void safetyFileImportReadsUploadedXlsxContent() throws Exception {
        String fileId = "sensitive-upload-bff-test.xlsx";
        UploadedFileStore.defaultStore().writeBytes(fileId, SimpleXlsxWriter.write("sensitive",
                Arrays.asList(
                        Arrays.<Object>asList("Political", "Illegal", "Other"),
                        Arrays.<Object>asList("alpha", "beta", "loose"))));

        mockMvc.perform(post("/user/api/v1/safe/sensitive/word")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tableId\":\"table-001\",\"importType\":\"file\",\"fileName\":\"" + fileId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<Map> captor = forClass(Map.class);
        verify(safetyService).uploadSensitiveWord(anyString(), anyString(), captor.capture());
        String content = String.valueOf(captor.getValue().get("content"));
        assertTrue(content.contains("Political\tIllegal\tOther"));
        assertTrue(content.contains("alpha\tbeta\tloose"));
    }

    @Test
    public void workflowApiAndBotUploadRoutesReturnFrontendContracts() throws Exception {
        MockMvc workflowApiMvc = MockMvcBuilders
                .standaloneSetup(new WanwuWorkflowApiController(appService))
                .build();
        String workflowSchema = "{\"parameters\":[{\"name\":\"city\",\"type\":\"string\",\"description\":\"City name\",\"required\":true}],"
                + "\"outputs\":[{\"name\":\"summary\",\"type\":\"string\",\"desc\":\"Summary\"}]}";
        when(appService.exportWorkflow(any(WorkflowExportQuery.class)))
                .thenReturn(new WorkflowExportResult("PolicyFlow", "policy workflow", workflowSchema));
        WorkflowRunResult workflowApiRun = new WorkflowRunResult("workflow-001", Collections.singletonMap("answer", "ok"));
        workflowApiRun.setRunId("workflow-run-001");
        workflowApiRun.setStatus("success");
        workflowApiRun.setCreatedAt(100L);
        workflowApiRun.setFinishedAt(125L);
        workflowApiRun.setCostMillis(25L);
        when(appService.runWorkflow(any(WorkflowRunCommand.class)))
                .thenReturn(workflowApiRun);
        Map<String, Object> workflowProcess = new LinkedHashMap<>();
        workflowProcess.put("workFlowId", "workflow-001");
        workflowProcess.put("executeId", "workflow-run-001");
        workflowProcess.put("executeStatus", 2);
        workflowProcess.put("nodeResults", Collections.singletonList(
                map("nodeId", "900001", "NodeType", "End", "output", "{\"answer\":\"ok\"}")));
        when(appService.getWorkflowRunProcess(eq("dev-admin"), eq("default-org"),
                eq("workflow-001"), eq("workflow-run-001"))).thenReturn(workflowProcess);

        workflowApiMvc.perform(get("/workflow/api/workflow/parameter")
                        .header("x-user-id", "dev-admin")
                        .header("x-org-id", "default-org")
                        .param("workflowID", "workflow-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.workflow_id").value("workflow-001"))
                .andExpect(jsonPath("$.data.schema").value(workflowSchema))
                .andExpect(jsonPath("$.data.parameters[0].name").value("city"))
                .andExpect(jsonPath("$.data.parameters[0].required").value(true))
                .andExpect(jsonPath("$.data.outputs[0].name").value("summary"));

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
                .andExpect(jsonPath("$.data.run_id").value("workflow-run-001"))
                .andExpect(jsonPath("$.data.status").value("success"))
                .andExpect(jsonPath("$.data.costMillis").value(25))
                .andExpect(jsonPath("$.data.output.answer").value("ok"));

        workflowApiMvc.perform(get("/workflow/api/api/workflow_api/get_process")
                        .header("x-user-id", "dev-admin")
                        .header("x-org-id", "default-org")
                        .param("workflow_id", "workflow-001")
                        .param("execute_id", "workflow-run-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.workFlowId").value("workflow-001"))
                .andExpect(jsonPath("$.data.executeId").value("workflow-run-001"))
                .andExpect(jsonPath("$.data.executeStatus").value(2))
                .andExpect(jsonPath("$.data.nodeResults[0].nodeId").value("900001"));

        org.mockito.ArgumentCaptor<WorkflowRunCommand> runCaptor = forClass(WorkflowRunCommand.class);
        verify(appService).runWorkflow(runCaptor.capture());
        verify(appService).getWorkflowRunProcess("dev-admin", "default-org", "workflow-001", "workflow-run-001");
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
    public void permissionManagementWriteRoutesCallIamService() throws Exception {
        when(iamService.createUser(anyString(), anyString(), any(Map.class)))
                .thenReturn(userInfo("user-001", "alice"));
        when(iamService.importUsers(anyString(), anyString(), any(List.class)))
                .thenReturn(map("total", 2, "successCount", 2, "failCount", 0));
        when(iamService.listUsersOutsideOrg(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(page(userInfo("user-002", "bob"), 1, 1, 10));
        when(iamService.createRole(anyString(), anyString(), any(Map.class)))
                .thenReturn(roleInfo("role-001", "Operator"));
        when(iamService.createOrganization(anyString(), anyString(), any(Map.class)))
                .thenReturn(orgInfo("org-001", "Sub Org"));

        mockMvc.perform(post("/user/api/v1/user")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"roleIds\":[\"app\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("user-001"));
        mockMvc.perform(multipart("/user/api/v1/user/batch")
                        .file(new MockMultipartFile("file", "users.csv", "text/csv",
                                ("username,nickname,password,email,phone,company,role,remark\n"
                                        + "carol,Carol,Password1!,carol@example.local,13800000001,Wanwu Java,app,imported\n"
                                        + "dave,Dave,Password1!,dave@example.local,13800000002,Wanwu Java,app,imported")
                                        .getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(2));
        mockMvc.perform(put("/user/api/v1/user")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-001\",\"nickname\":\"Alice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(put("/user/api/v1/user/status")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-001\",\"status\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(delete("/user/api/v1/user")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/user/api/v1/org/other/select")
                        .header("Authorization", "Bearer dev-token")
                        .param("name", "bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].userId").value("user-002"));
        mockMvc.perform(post("/user/api/v1/org/user")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orgId\":\"default-org\",\"userId\":\"user-002\",\"roleIds\":[\"app\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/role")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Operator\",\"permissions\":[\"app\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleId").value("role-001"));
        mockMvc.perform(put("/user/api/v1/role")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleId\":\"role-001\",\"name\":\"Operator 2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(put("/user/api/v1/role/status")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleId\":\"role-001\",\"status\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(delete("/user/api/v1/role")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleId\":\"role-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/org")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sub Org\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orgId").value("org-001"));
        mockMvc.perform(put("/user/api/v1/org")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orgId\":\"org-001\",\"name\":\"Sub Org 2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(put("/user/api/v1/org/status")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orgId\":\"org-001\",\"status\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(delete("/user/api/v1/org")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orgId\":\"org-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(iamService).createUser(anyString(), anyString(), any(Map.class));
        ArgumentCaptor<List> importedUsers = forClass(List.class);
        verify(iamService).importUsers(anyString(), anyString(), importedUsers.capture());
        assertEquals(2, importedUsers.getValue().size());
        assertEquals("carol", ((Map) importedUsers.getValue().get(0)).get("username"));
        assertEquals("Password1!", ((Map) importedUsers.getValue().get(0)).get("password"));
        assertEquals("Wanwu Java", ((Map) importedUsers.getValue().get(0)).get("company"));
        assertEquals("13800000001", ((Map) importedUsers.getValue().get(0)).get("phone"));
        assertEquals("app", ((Map) importedUsers.getValue().get(0)).get("roleName"));
        verify(iamService).updateUser(anyString(), anyString(), any(Map.class));
        verify(iamService).updateUserStatus(anyString(), anyString(), any(Map.class));
        verify(iamService).deleteUser(anyString(), anyString(), any(Map.class));
        verify(iamService).listUsersOutsideOrg(anyString(), anyString(), anyInt(), anyInt());
        verify(iamService).addOrgUser(anyString(), anyString(), any(Map.class));
        verify(iamService).createRole(anyString(), anyString(), any(Map.class));
        verify(iamService).updateRole(anyString(), anyString(), any(Map.class));
        verify(iamService).updateRoleStatus(anyString(), anyString(), any(Map.class));
        verify(iamService).deleteRole(anyString(), anyString(), any(Map.class));
        verify(iamService).createOrganization(anyString(), anyString(), any(Map.class));
        verify(iamService).updateOrganization(anyString(), anyString(), any(Map.class));
        verify(iamService).updateOrganizationStatus(anyString(), anyString(), any(Map.class));
        verify(iamService).deleteOrganization(anyString(), anyString(), any(Map.class));
    }

    @Test
    public void batchUserImportAcceptsGoChineseTemplateHeadersAndRejectsMissingHeaders() throws Exception {
        when(iamService.importUsers(anyString(), anyString(), any(List.class)))
                .thenReturn(map("total", 1, "successCount", 1, "failCount", 0));

        mockMvc.perform(multipart("/user/api/v1/user/batch")
                        .file(new MockMultipartFile("file", "users.csv", "text/csv",
                                ("\u7528\u6237\u540d,\u5bc6\u7801,\u5355\u4f4d,\u7535\u8bdd,\u89d2\u8272,\u5907\u6ce8\n"
                                        + "zhangsan,Password1!,Wanwu Java,13800000003,app,imported")
                                        .getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1));

        ArgumentCaptor<List> importedUsers = forClass(List.class);
        verify(iamService).importUsers(anyString(), anyString(), importedUsers.capture());
        Map row = (Map) importedUsers.getValue().get(0);
        assertEquals("zhangsan", row.get("username"));
        assertEquals("Password1!", row.get("password"));
        assertEquals("Wanwu Java", row.get("company"));
        assertEquals("13800000003", row.get("phone"));
        assertEquals("app", row.get("roleName"));
        assertEquals("imported", row.get("remark"));

        mockMvc.perform(multipart("/user/api/v1/user/batch")
                        .file(new MockMultipartFile("file", "users.csv", "text/csv",
                                ("username,password,company\nzhangsan,Password1!,Wanwu Java")
                                        .getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg", containsString("missing phone")));
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
    public void modelValidateThinkingChecksEnableAndDisableResponses() throws Exception {
        AtomicReference<String> enableBody = new AtomicReference<>();
        AtomicReference<String> disableBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            String body = readBody(exchange);
            if (body.contains("\"enable_thinking\":true")) {
                enableBody.set(body);
                respondJson(exchange, "{\"id\":\"chatcmpl-enable\",\"object\":\"chat.completion\","
                        + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                        + "\"content\":\"hi\",\"reasoning_content\":\"thinking\"},\"finish_reason\":\"stop\"}]}");
            } else {
                disableBody.set(body);
                respondJson(exchange, "{\"id\":\"chatcmpl-disable\",\"object\":\"chat.completion\","
                        + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                        + "\"content\":\"hi\"},\"finish_reason\":\"stop\"}]}");
            }
        });
        server.start();
        try {
            mockMvc.perform(post("/user/api/v1/model/validate-thinking")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"provider\":\"openai-compatible\",\"modelType\":\"llm\","
                                    + "\"model\":\"deepseek-chat\",\"displayName\":\"DeepSeek Chat\","
                                    + "\"config\":{\"endpointUrl\":\"http://127.0.0.1:"
                                    + server.getAddress().getPort()
                                    + "/v1\",\"apiKey\":\"real-key\"}}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            assertTrue(enableBody.get().contains("\"model\":\"deepseek-chat\""));
            assertTrue(enableBody.get().contains("\"enable_thinking\":true"));
            assertTrue(disableBody.get().contains("\"enable_thinking\":false"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void modelValidateThinkingRejectsProviderWithoutReasoningWhenEnabled() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> respondJson(exchange,
                "{\"id\":\"chatcmpl-no-reasoning\",\"object\":\"chat.completion\","
                        + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                        + "\"content\":\"hi\"},\"finish_reason\":\"stop\"}]}"));
        server.start();
        try {
            mockMvc.perform(post("/user/api/v1/model/validate-thinking")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"provider\":\"openai-compatible\",\"modelType\":\"llm\","
                                    + "\"model\":\"plain-chat\",\"displayName\":\"Plain Chat\","
                                    + "\"config\":{\"endpointUrl\":\"http://127.0.0.1:"
                                    + server.getAddress().getPort()
                                    + "/v1\",\"apiKey\":\"real-key\"}}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001))
                    .andExpect(jsonPath("$.msg", containsString("enable_thinking=true")));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void modelValidateThinkingRejectsProviderThatCannotDisableReasoning() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> respondJson(exchange,
                "{\"id\":\"chatcmpl-forced-reasoning\",\"object\":\"chat.completion\","
                        + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                        + "\"content\":\"hi\",\"reasoning_content\":\"thinking\"},\"finish_reason\":\"stop\"}]}"));
        server.start();
        try {
            mockMvc.perform(post("/user/api/v1/model/validate-thinking")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"provider\":\"openai-compatible\",\"modelType\":\"llm\","
                                    + "\"model\":\"forced-thinking\",\"displayName\":\"Forced Thinking\","
                                    + "\"config\":{\"endpointUrl\":\"http://127.0.0.1:"
                                    + server.getAddress().getPort()
                                    + "/v1\",\"apiKey\":\"real-key\"}}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001))
                    .andExpect(jsonPath("$.msg", containsString("turning off thinking")));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void workflowAsrModelSelectMapsPathTypeAndUserContext() throws Exception {
        when(modelService.listTypeModels(any()))
                .thenReturn(new ModelListResult(Collections.singletonList(modelInfo("asr-001", "XunFei ASR", "sync-asr")), 1));

        mockMvc.perform(get("/user/api/v1/appspace/workflow/model/select/asr")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].modelId").value("asr-001"));

        ArgumentCaptor<ModelTypeQuery> captor = forClass(ModelTypeQuery.class);
        verify(modelService).listTypeModels(captor.capture());
        assertEquals("dev-admin", captor.getValue().getUserId());
        assertEquals("default-org", captor.getValue().getOrgId());
        assertEquals("sync-asr", captor.getValue().getModelType());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void asrStreamRouteReturnsDevelopmentEventStream() throws Exception {
        mockMvc.perform(get("/user/api/v1/asr/stream")
                        .header("Authorization", "Bearer dev-token")
                        .param("modelId", "asr-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("asr.connected")))
                .andExpect(content().string(containsString("asr.closed")))
                .andExpect(content().string(containsString("asr-001")));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService).checkModelUserPermission(eq("dev-admin"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Collections.singletonList("asr-001"), modelIdsCaptor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void asrStreamStopsBeforeEventsWhenModelIsDenied() throws Exception {
        doThrow(new IllegalArgumentException("bff_model_perm: asr-private"))
                .when(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), any(List.class));

        mockMvc.perform(get("/user/api/v1/asr/stream")
                        .header("Authorization", "Bearer dev-token-app")
                        .param("modelId", "asr-private"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"msg\":\"bff_model_perm: asr-private\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("asr.connected"))));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Collections.singletonList("asr-private"), modelIdsCaptor.getValue());
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
    @SuppressWarnings("unchecked")
    public void modelExperienceDialogChecksModelPermissionBeforeSave() throws Exception {
        doThrow(new IllegalArgumentException("bff_model_perm: model-private"))
                .when(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), any(List.class));

        mockMvc.perform(post("/user/api/v1/model/experience/dialog")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-private\",\"sessionId\":\"session-001\",\"title\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("bff_model_perm: model-private"));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Collections.singletonList("model-private"), modelIdsCaptor.getValue());
        verify(modelService, times(0)).saveModelExperienceDialog(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void modelExperienceLlmChecksModelPermissionBeforeStreaming() throws Exception {
        doThrow(new IllegalArgumentException("bff_model_perm: model-private"))
                .when(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), any(List.class));

        mockMvc.perform(post("/user/api/v1/model/experience/llm")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-private\",\"sessionId\":\"session-001\",\"modelExperienceId\":\"exp-001\",\"content\":\"hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"msg\":\"bff_model_perm: model-private\"")));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Collections.singletonList("model-private"), modelIdsCaptor.getValue());
        verify(modelService, times(0)).getModel(anyString(), anyString(), anyString());
        verify(modelService, times(0)).saveModelExperienceDialogRecord(any());
    }

    @Test
    public void modelExperienceLlmUsesConfiguredOpenAiCompatibleUpstreamWhenAvailable() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"chatcmpl-model-exp\",\"object\":\"chat.completion\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                    + "\"content\":\"upstream model answer\"},\"finish_reason\":\"stop\"}],"
                    + "\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":3,\"total_tokens\":5}}");
        });
        server.start();
        try {
            ModelInfo model = modelInfo("model-001", "DeepSeek Chat", "llm");
            model.setProvider("openai-compatible");
            model.setConfig(map("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1",
                    "apiKey", "local-key"));
            when(modelService.getModel(anyString(), anyString(), eq("model-001"))).thenReturn(model);

            mockMvc.perform(post("/user/api/v1/model/experience/llm")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\","
                                    + "\"modelExperienceId\":\"exp-001\",\"content\":\"hello\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                    .andExpect(content().string(containsString("\"content\":\"upstream model answer\"")))
                    .andExpect(content().string(containsString("\"finish_reason\":\"stop\"")));

            assertEquals("Bearer local-key", authorization.get());
            assertTrue(upstreamBody.get().contains("\"model\":\"deepseek-chat\""));
            assertTrue(upstreamBody.get().contains("\"content\":\"hello\""));
            assertTrue(upstreamBody.get().contains("\"stream\":false"));

            ArgumentCaptor<ModelExperienceDialogRecordSaveCommand> captor =
                    forClass(ModelExperienceDialogRecordSaveCommand.class);
            verify(modelService, times(2)).saveModelExperienceDialogRecord(captor.capture());
            assertEquals("user", captor.getAllValues().get(0).getRole());
            assertEquals("hello", captor.getAllValues().get(0).getOriginalContent());
            assertEquals("assistant", captor.getAllValues().get(1).getRole());
            assertEquals("upstream model answer", captor.getAllValues().get(1).getOriginalContent());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void modelExperienceLlmStreamsConfiguredOpenAiCompatibleUpstream() throws Exception {
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange));
            respondSse(exchange, "data: {\"id\":\"chatcmpl-stream\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"first \"},"
                    + "\"finish_reason\":null}],\"usage\":{\"prompt_tokens\":5,\"completion_tokens\":1,\"total_tokens\":6}}\n\n"
                    + "data: {\"id\":\"chatcmpl-stream\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"second\"},"
                    + "\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":5,\"completion_tokens\":2,\"total_tokens\":7}}\n\n"
                    + "data: [DONE]\n\n");
        });
        server.start();
        try {
            ModelInfo model = modelInfo("model-001", "DeepSeek Chat", "llm");
            model.setProvider("openai-compatible");
            model.setConfig(map("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1",
                    "apiKey", "local-key"));
            when(modelService.getModel(anyString(), anyString(), eq("model-001"))).thenReturn(model);

            mockMvc.perform(post("/user/api/v1/model/experience/llm")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\","
                                    + "\"modelExperienceId\":\"exp-001\",\"content\":\"hello\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                    .andExpect(content().string(containsString("\"content\":\"first \"")))
                    .andExpect(content().string(containsString("\"content\":\"second\"")))
                    .andExpect(content().string(containsString("data: [DONE]")));

            assertTrue(upstreamBody.get().contains("\"stream\":true"));

            ArgumentCaptor<ModelExperienceDialogRecordSaveCommand> recordCaptor =
                    forClass(ModelExperienceDialogRecordSaveCommand.class);
            verify(modelService, times(2)).saveModelExperienceDialogRecord(recordCaptor.capture());
            assertEquals("hello", recordCaptor.getAllValues().get(0).getOriginalContent());
            assertEquals("first second", recordCaptor.getAllValues().get(1).getOriginalContent());

            ArgumentCaptor<RecordModelStatisticCommand> statisticCaptor = forClass(RecordModelStatisticCommand.class);
            verify(appService).recordModelStatistic(statisticCaptor.capture());
            assertEquals(5L, statisticCaptor.getValue().getPromptTokens());
            assertEquals(2L, statisticCaptor.getValue().getCompletionTokens());
            assertEquals(7L, statisticCaptor.getValue().getTotalTokens());
            assertTrue(statisticCaptor.getValue().isStream());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void modelExperienceLlmIncludesDialogHistoryInConfiguredUpstreamRequest() throws Exception {
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"chatcmpl-history\",\"object\":\"chat.completion\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                    + "\"content\":\"history aware answer\"},\"finish_reason\":\"stop\"}],"
                    + "\"usage\":{\"prompt_tokens\":6,\"completion_tokens\":3,\"total_tokens\":9}}");
        });
        server.start();
        try {
            ModelInfo model = modelInfo("model-001", "DeepSeek Chat", "llm");
            model.setProvider("openai-compatible");
            model.setConfig(map("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1",
                    "apiKey", "local-key"));
            ModelExperienceDialogRecordInfo previousAssistant =
                    modelExperienceRecord("exp-001", "model-001", "session-001", "raw previous answer", "", "assistant");
            previousAssistant.setHandledContent("clean previous answer");
            when(modelService.getModel(anyString(), anyString(), eq("model-001"))).thenReturn(model);
            when(modelService.listModelExperienceDialogRecords(any()))
                    .thenReturn(new ModelExperienceDialogRecordListResult(Arrays.asList(
                            modelExperienceRecord("exp-001", "model-001", "session-001", "previous user", "", "user"),
                            previousAssistant), 2));

            mockMvc.perform(post("/user/api/v1/model/experience/llm")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\","
                                    + "\"modelExperienceId\":\"exp-001\",\"content\":\"latest question\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("\"content\":\"history aware answer\"")));

            String body = upstreamBody.get();
            assertTrue(body.contains("\"role\":\"user\",\"content\":\"previous user\""));
            assertTrue(body.contains("\"role\":\"assistant\",\"content\":\"clean previous answer\""));
            assertTrue(body.contains("\"role\":\"user\",\"content\":\"latest question\""));
            assertTrue(body.indexOf("\"content\":\"previous user\"")
                    < body.indexOf("\"content\":\"clean previous answer\""));
            assertTrue(body.indexOf("\"content\":\"clean previous answer\"")
                    < body.indexOf("\"content\":\"latest question\""));
            assertTrue(!body.contains("raw previous answer"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void modelExperienceLlmPassesEnabledInferenceParamsToConfiguredUpstream() throws Exception {
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange));
            respondJson(exchange, "{\"id\":\"chatcmpl-params\",\"object\":\"chat.completion\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                    + "\"content\":\"param aware answer\"},\"finish_reason\":\"stop\"}]}");
        });
        server.start();
        try {
            ModelInfo model = modelInfo("model-001", "DeepSeek Chat", "llm");
            model.setProvider("openai-compatible");
            model.setConfig(map("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1",
                    "apiKey", "local-key"));
            when(modelService.getModel(anyString(), anyString(), eq("model-001"))).thenReturn(model);

            mockMvc.perform(post("/user/api/v1/model/experience/llm")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\","
                                    + "\"modelExperienceId\":\"exp-001\",\"content\":\"hello\","
                                    + "\"temperature\":0.2,\"temperatureEnable\":true,"
                                    + "\"topP\":0.9,\"topPEnable\":true,"
                                    + "\"frequencyPenalty\":0.3,\"frequencyPenaltyEnable\":true,"
                                    + "\"presencePenalty\":0.4,\"presencePenaltyEnable\":true,"
                                    + "\"maxTokens\":128,\"maxTokensEnable\":true,"
                                    + "\"thinkingEnable\":false}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("\"content\":\"param aware answer\"")));

            String body = upstreamBody.get();
            assertTrue(body.contains("\"temperature\":0.2"));
            assertTrue(body.contains("\"top_p\":0.9"));
            assertTrue(body.contains("\"frequency_penalty\":0.3"));
            assertTrue(body.contains("\"presence_penalty\":0.4"));
            assertTrue(body.contains("\"max_tokens\":128"));
            assertTrue(body.contains("\"enable_thinking\":false"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void modelExperienceLlmRecordsProviderUsageWhenAvailable() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> respondJson(exchange,
                "{\"id\":\"chatcmpl-usage\",\"object\":\"chat.completion\","
                        + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                        + "\"content\":\"usage answer\"},\"finish_reason\":\"stop\"}],"
                        + "\"usage\":{\"prompt_tokens\":11,\"completion_tokens\":7,\"total_tokens\":18}}"));
        server.start();
        try {
            ModelInfo model = modelInfo("model-001", "DeepSeek Chat", "llm");
            model.setProvider("openai-compatible");
            model.setConfig(map("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1",
                    "apiKey", "local-key"));
            when(modelService.getModel(anyString(), anyString(), eq("model-001"))).thenReturn(model);

            mockMvc.perform(post("/user/api/v1/model/experience/llm")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\","
                                    + "\"modelExperienceId\":\"exp-001\",\"content\":\"hello\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("\"content\":\"usage answer\"")));

            ArgumentCaptor<RecordModelStatisticCommand> captor = forClass(RecordModelStatisticCommand.class);
            verify(appService).recordModelStatistic(captor.capture());
            assertEquals(11L, captor.getValue().getPromptTokens());
            assertEquals(7L, captor.getValue().getCompletionTokens());
            assertEquals(18L, captor.getValue().getTotalTokens());
            assertTrue(captor.getValue().isStream());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void modelExperienceLlmBlocksGlobalSensitiveInput() throws Exception {
        when(modelService.getModel(anyString(), anyString(), anyString()))
                .thenReturn(modelInfo("model-001", "DeepSeek Chat", "llm"));
        Map<String, Object> table = map("tableId", "global-001", "tableName", "Global Guard",
                "reply", "Safety reply", "type", "global", "version", "v1");
        Map<String, Object> word = map("wordId", "word-001", "word", "banned-token", "sensitiveType", "Other");
        when(safetyService.listSensitiveWordTables(anyString(), anyString(), eq("global")))
                .thenReturn(listResult(table));
        when(safetyService.getSensitiveWordTable(anyString(), anyString(), eq("global-001")))
                .thenReturn(table);
        when(safetyService.listSensitiveWords(anyString(), anyString(), eq("global-001"), anyInt(), anyInt()))
                .thenReturn(map("list", Collections.singletonList(word), "total", 1));

        mockMvc.perform(post("/user/api/v1/model/experience/llm")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\",\"modelExperienceId\":\"exp-001\",\"content\":\"hello banned-token\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"content\":\"Safety reply\"")))
                .andExpect(content().string(containsString("\"finish_reason\":\"stop\"")));

        verify(modelService, times(0)).saveModelExperienceDialogRecord(any());
    }

    @Test
    public void modelExperienceLlmReplacesSensitiveGeneratedOutput() throws Exception {
        when(modelService.getModel(anyString(), anyString(), anyString()))
                .thenReturn(modelInfo("model-001", "DeepSeek Chat", "llm"));
        Map<String, Object> table = map("tableId", "global-001", "tableName", "Global Guard",
                "reply", "Safety reply", "type", "global", "version", "v1");
        Map<String, Object> word = map("wordId", "word-001", "word", "Echo:", "sensitiveType", "Other");
        when(safetyService.listSensitiveWordTables(anyString(), anyString(), eq("global")))
                .thenReturn(listResult(table));
        when(safetyService.getSensitiveWordTable(anyString(), anyString(), eq("global-001")))
                .thenReturn(table);
        when(safetyService.listSensitiveWords(anyString(), anyString(), eq("global-001"), anyInt(), anyInt()))
                .thenReturn(map("list", Collections.singletonList(word), "total", 1));

        mockMvc.perform(post("/user/api/v1/model/experience/llm")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelId\":\"model-001\",\"sessionId\":\"session-001\",\"modelExperienceId\":\"exp-001\",\"content\":\"plain question\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"content\":\"Safety reply\"")))
                .andExpect(content().string(containsString("\"finish_reason\":\"stop\"")));

        ArgumentCaptor<ModelExperienceDialogRecordSaveCommand> captor =
                forClass(ModelExperienceDialogRecordSaveCommand.class);
        verify(modelService, times(2)).saveModelExperienceDialogRecord(captor.capture());
        assertEquals("user", captor.getAllValues().get(0).getRole());
        assertEquals("plain question", captor.getAllValues().get(0).getOriginalContent());
        assertEquals("assistant", captor.getAllValues().get(1).getRole());
        assertEquals("Safety reply", captor.getAllValues().get(1).getOriginalContent());
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
        when(knowledgeService.listKeywords(anyString(), anyString(), any(Map.class)))
                .thenReturn(keywordPage(keywordInfo(1001L, "question", "document", "knowledge-001", "Dev KB")));
        when(knowledgeService.createKeyword(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("id", 1001L));
        when(knowledgeService.getKeyword(anyString(), anyString(), any(Map.class)))
                .thenReturn(keywordInfo(1001L, "question", "document", "knowledge-001", "Dev KB"));
        when(knowledgeService.listSplitters(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeSplitterList", Collections.singletonList(splitter("splitter-001", "paragraph", "\n\n", "preset"))));
        when(knowledgeService.listDocs(anyString(), anyString(), any(Map.class)))
                .thenReturn(docPage("knowledge-001", "Dev KB"));
        when(knowledgeService.getKnowledgeGraph(anyString(), anyString(), any(Map.class)))
                .thenReturn(knowledgeGraph("knowledge-001", "Dev KB", "Guide.txt"));
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
        when(knowledgeService.listDocChildSegments(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("contentList", Collections.singletonList(
                        childSegment("child-001", "segment-001", 1, "Child content"))));
        when(knowledgeService.selectMetaKeys(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeMetaList", Collections.singletonList(
                        map("metaId", "meta-001", "metaKey", "source", "metaValueType", "string"))));
        when(knowledgeService.listMetaValues(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeMetaValues", Collections.singletonList(
                        map("metaId", "meta-001", "metaKey", "source", "metaValue",
                                Collections.singletonList("manual"), "metaValueType", "string"))));
        when(knowledgeService.hitKnowledge(anyString(), anyString(), any(Map.class)))
                .thenReturn(map("prompt", "Question: Guide\nReference 1: Guide content\n",
                        "searchList", Collections.singletonList(knowledgeHitResult("Guide.txt", "Guide content", "Dev KB")),
                        "score", Collections.singletonList(1.0D),
                        "useGraph", false));
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

        mockMvc.perform(get("/user/api/v1/knowledge/keywords")
                        .header("Authorization", "Bearer dev-token")
                        .param("name", "question")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].id").value(1001))
                .andExpect(jsonPath("$.data.list[0].knowledgeBaseNames[0]").value("Dev KB"));

        mockMvc.perform(post("/user/api/v1/knowledge/keywords")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"question\",\"alias\":\"document\",\"knowledgeBaseIds\":[\"knowledge-001\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1001));

        mockMvc.perform(get("/user/api/v1/knowledge/keywords/detail")
                        .header("Authorization", "Bearer dev-token")
                        .param("id", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alias").value("document"));

        mockMvc.perform(put("/user/api/v1/knowledge/keywords")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1001,\"name\":\"question2\",\"alias\":\"document2\",\"knowledgeBaseIds\":[\"knowledge-001\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/knowledge/keywords")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1001}"))
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

        mockMvc.perform(get("/user/api/v1/knowledge/graph")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.graph.nodes[0].entity_name").value("Knowledge: Dev KB"))
                .andExpect(jsonPath("$.data.graph.edges[0].source_entity").value("Knowledge: Dev KB"));

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

        mockMvc.perform(post("/user/api/v1/knowledge/doc/meta")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"docId\":\"\",\"metaDataList\":[{\"metaKey\":\"source\",\"metaValueType\":\"string\",\"option\":\"add\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/knowledge/meta/select")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeMetaList[0].metaKey").value("source"));

        mockMvc.perform(post("/user/api/v1/knowledge/meta/value/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"docIdList\":[\"doc-guide\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeMetaValues[0].metaValue[0]").value("manual"));

        mockMvc.perform(post("/user/api/v1/knowledge/meta/value/update")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"docIdList\":[\"doc-guide\"],\"metaValueList\":[{\"metaId\":\"meta-001\",\"metaKey\":\"source\",\"metaValue\":\"manual\",\"metaValueType\":\"string\",\"option\":\"add\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/knowledge/doc/segment/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("docId", "doc-guide")
                        .param("keyword", "Guide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("Guide.txt"))
                .andExpect(jsonPath("$.data.contentList[0].contentId").value("segment-001"));

        mockMvc.perform(get("/user/api/v1/knowledge/doc/segment/child/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("docId", "doc-guide")
                        .param("contentId", "segment-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contentList[0].childId").value("child-001"))
                .andExpect(jsonPath("$.data.contentList[0].childNum").value(1));

        mockMvc.perform(post("/user/api/v1/knowledge/hit")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Guide\",\"knowledgeList\":[{\"knowledgeId\":\"knowledge-001\"}],\"knowledgeMatchParams\":{\"matchType\":\"mix\",\"topK\":5,\"threshold\":0.4}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.searchList[0].title").value("Guide.txt"))
                .andExpect(jsonPath("$.data.searchList[0].knowledgeName").value("Dev KB"))
                .andExpect(jsonPath("$.data.searchList[0].contentType").value("text"))
                .andExpect(jsonPath("$.data.score[0]").value(1.0))
                .andExpect(jsonPath("$.data.useGraph").value(false));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/segment/create")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-guide\",\"content\":\"Extra segment\",\"labels\":[\"manual\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/segment/child/create")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-guide\",\"parentId\":\"segment-001\",\"content\":[\"Child content\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/segment/child/update")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-guide\",\"parentId\":\"segment-001\",\"parentChunkNo\":1,\"childChunk\":{\"chunkNo\":1,\"content\":\"Updated child\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/knowledge/doc/segment/child/delete")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-guide\",\"parentId\":\"segment-001\",\"parentChunkNo\":1,\"ChildChunkNoList\":[1]}"))
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
        verify(knowledgeService).listKeywords(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).createKeyword(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).getKeyword(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateKeyword(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteKeyword(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).listDocs(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).analyzeDocUrls(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).importDocs(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateDocMeta(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).selectMetaKeys(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).listMetaValues(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateMetaValues(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).listDocSegments(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).listDocChildSegments(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).hitKnowledge(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).createDocSegment(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).createDocChildSegment(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateDocChildSegment(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteDocChildSegment(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateDocSegment(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateDocSegmentStatus(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateDocSegmentLabels(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteDocSegment(anyString(), anyString(), any(Map.class));
    }

    @Test
    public void knowledgeReportRoutesDispatchToServiceAndReturnFrontendContract() throws Exception {
        when(knowledgeService.listReports(anyString(), anyString(), any(Map.class)))
                .thenReturn(reportPage(report("report-001", "Manual", "Manual content")));

        mockMvc.perform(get("/user/api/v1/knowledge/report/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-001")
                        .param("pageNo", "1")
                        .param("pageSize", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.canGenerate").value(true))
                .andExpect(jsonPath("$.data.canAddReport").value(true))
                .andExpect(jsonPath("$.data.list[0].contentId").value("report-001"));

        mockMvc.perform(post("/user/api/v1/knowledge/report/generate")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/report/add")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"title\":\"Manual\",\"content\":\"Manual content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/report/update")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"contentId\":\"report-001\",\"title\":\"Updated\",\"content\":\"Updated content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/report/batch/add")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"fileUploadId\":\"file-report-csv\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/knowledge/report/delete")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-001\",\"contentId\":\"report-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(knowledgeService).listReports(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).generateReport(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).addReport(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateReport(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).batchAddReports(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteReport(anyString(), anyString(), any(Map.class));
    }

    @Test
    public void knowledgeExternalRoutesDispatchToServiceAndReturnFrontendContract() throws Exception {
        when(knowledgeService.listExternalApis(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("externalApiList", Collections.singletonList(
                        externalApi("external-api-001", "Dify Dev"))));
        when(knowledgeService.createExternalApi(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("externalApiId", "external-api-001"));
        when(knowledgeService.listExternalKnowledge(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("externalKnowledgeList", Collections.singletonList(
                        externalKnowledge("external-knowledge-001", "Dify Dataset", "external-api-001"))));
        when(knowledgeService.createExternalKnowledge(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeId", "knowledge-external-001"));

        mockMvc.perform(get("/user/api/v1/knowledge/external/api/select")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.externalApiList[0].externalApiId").value("external-api-001"))
                .andExpect(jsonPath("$.data.externalApiList[0].name").value("Dify Dev"));

        mockMvc.perform(post("/user/api/v1/knowledge/external/api")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dify Dev\",\"description\":\"api\",\"baseUrl\":\"https://dify.example/v1\",\"apiKey\":\"key\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.externalApiId").value("external-api-001"));

        mockMvc.perform(put("/user/api/v1/knowledge/external/api")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"externalApiId\":\"external-api-001\",\"name\":\"Dify Updated\",\"description\":\"api\",\"baseUrl\":\"https://dify.example/v2\",\"apiKey\":\"key2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/knowledge/external/select")
                        .header("Authorization", "Bearer dev-token")
                        .param("externalApiId", "external-api-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.externalKnowledgeList[0].externalKnowledgeId").value("external-knowledge-001"))
                .andExpect(jsonPath("$.data.externalKnowledgeList[0].externalKnowledgeName").value("Dify Dataset"));

        mockMvc.perform(post("/user/api/v1/knowledge/external")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"External KB\",\"description\":\"docs\",\"externalSource\":\"dify\",\"externalApiId\":\"external-api-001\",\"externalKnowledgeId\":\"external-knowledge-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeId").value("knowledge-external-001"));

        mockMvc.perform(put("/user/api/v1/knowledge/external")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-external-001\",\"name\":\"External KB 2\",\"description\":\"docs\",\"externalSource\":\"dify\",\"externalApiId\":\"external-api-001\",\"externalKnowledgeId\":\"external-knowledge-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/knowledge/external")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-external-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/user/api/v1/knowledge/external/api")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"externalApiId\":\"external-api-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(knowledgeService).listExternalApis(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).createExternalApi(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateExternalApi(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).listExternalKnowledge(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).createExternalKnowledge(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).updateExternalKnowledge(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteExternalKnowledge(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteExternalApi(anyString(), anyString(), any(Map.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createKnowledgeChecksEmbeddingAndGraphModelsBeforeSave() throws Exception {
        when(knowledgeService.createKnowledge(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeId", "knowledge-001"));

        mockMvc.perform(post("/user/api/v1/knowledge")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dev KB\",\"embeddingModelInfo\":{\"modelId\":\"embedding-001\"},\"knowledgeGraph\":{\"llmModelId\":\"graph-llm-001\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.knowledgeId").value("knowledge-001"));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService).checkModelUserPermission(eq("dev-admin"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Arrays.asList("embedding-001", "graph-llm-001"), modelIdsCaptor.getValue());
        verify(knowledgeService).createKnowledge(eq("dev-admin"), eq("default-org"), any(Map.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createKnowledgeStopsBeforeSaveWhenModelIsDenied() throws Exception {
        doThrow(new IllegalArgumentException("bff_model_perm: embedding-private"))
                .when(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), any(List.class));

        mockMvc.perform(post("/user/api/v1/knowledge")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dev KB\",\"embeddingModelInfo\":{\"modelId\":\"embedding-private\"},\"knowledgeGraph\":{\"llmModelId\":\"graph-private\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("bff_model_perm: embedding-private"));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Arrays.asList("embedding-private", "graph-private"), modelIdsCaptor.getValue());
        verify(knowledgeService, times(0)).createKnowledge(anyString(), anyString(), any(Map.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void knowledgeHitRoutesCheckRerankModelBeforeSearch() throws Exception {
        when(knowledgeService.hitKnowledge(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("searchList", Collections.singletonList(
                        knowledgeHitResult("Guide.txt", "Guide content", "Dev KB"))));
        when(knowledgeService.hitQaPairs(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("searchList", Collections.singletonList(
                        qaHitResult("qa-001", "knowledge-qa-001", "Dev QA", "What is Wanwu?", "An AI platform."))));

        mockMvc.perform(post("/user/api/v1/knowledge/hit")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Guide\",\"knowledgeList\":[{\"knowledgeId\":\"knowledge-001\"}],\"knowledgeMatchParams\":{\"rerankModelId\":\"rerank-001\",\"topK\":5}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/qa/hit")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Wanwu\",\"knowledgeList\":[{\"knowledgeId\":\"knowledge-qa-001\"}],\"knowledgeMatchParams\":{\"rerankModelId\":\"qa-rerank-001\",\"topK\":5}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService, times(2)).checkModelUserPermission(
                eq("dev-admin"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Collections.singletonList("rerank-001"), modelIdsCaptor.getAllValues().get(0));
        assertEquals(Collections.singletonList("qa-rerank-001"), modelIdsCaptor.getAllValues().get(1));
        verify(knowledgeService).hitKnowledge(eq("dev-admin"), eq("default-org"), any(Map.class));
        verify(knowledgeService).hitQaPairs(eq("dev-admin"), eq("default-org"), any(Map.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void knowledgeRoutesCheckDirectKnowledgeIdPermissionsBeforeService() throws Exception {
        when(knowledgeService.listDocs(anyString(), anyString(), any(Map.class)))
                .thenReturn(docPage("knowledge-view", "View KB"));

        mockMvc.perform(put("/user/api/v1/knowledge")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-system\",\"name\":\"System KB\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-view\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/import")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-edit\",\"docInfoList\":[{\"name\":\"Guide.txt\",\"content\":\"Guide\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<String> knowledgeIdCaptor = forClass(String.class);
        ArgumentCaptor<Integer> permissionCaptor = forClass(Integer.class);
        verify(knowledgeService, times(3)).checkKnowledgeUserPermission(
                eq("dev-admin"), eq("default-org"), knowledgeIdCaptor.capture(), permissionCaptor.capture());
        assertEquals(Arrays.asList("knowledge-system", "knowledge-view", "knowledge-edit"),
                knowledgeIdCaptor.getAllValues());
        assertEquals(Arrays.asList(30, 0, 10), permissionCaptor.getAllValues());
        verify(knowledgeService).updateKnowledge(eq("dev-admin"), eq("default-org"), any(Map.class));
        verify(knowledgeService).listDocs(eq("dev-admin"), eq("default-org"), any(Map.class));
        verify(knowledgeService).importDocs(eq("dev-admin"), eq("default-org"), any(Map.class));
    }

    @Test
    public void knowledgeRouteStopsBeforeServiceWhenPermissionIsDenied() throws Exception {
        doThrow(new IllegalArgumentException("knowledge_perm: denied"))
                .when(knowledgeService).checkKnowledgeUserPermission(
                        eq("dev-app"), eq("default-org"), eq("knowledge-private"), eq(10));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/import")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-private\",\"docInfoList\":[{\"name\":\"Guide.txt\",\"content\":\"Guide\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("knowledge_perm: denied"));

        verify(knowledgeService).checkKnowledgeUserPermission(
                eq("dev-app"), eq("default-org"), eq("knowledge-private"), eq(10));
        verify(knowledgeService, times(0)).importDocs(anyString(), anyString(), any(Map.class));
    }

    @Test
    public void knowledgeRouteRequiresDirectKnowledgeIdBeforeService() throws Exception {
        mockMvc.perform(post("/user/api/v1/knowledge/doc/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("knowledgeId is required"));

        verify(knowledgeService, times(0)).checkKnowledgeUserPermission(anyString(), anyString(), anyString(), anyInt());
        verify(knowledgeService, times(0)).listDocs(anyString(), anyString(), any(Map.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void knowledgeDocIdRouteResolvesKnowledgeBeforeService() throws Exception {
        when(knowledgeService.resolveKnowledgeId(anyString(), anyString(), any(Map.class)))
                .thenReturn("knowledge-from-doc");
        when(knowledgeService.listDocSegments(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("list", Collections.emptyList()));

        mockMvc.perform(get("/user/api/v1/knowledge/doc/segment/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("docId", "doc-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(knowledgeService).resolveKnowledgeId(eq("dev-admin"), eq("default-org"), any(Map.class));
        verify(knowledgeService).checkKnowledgeUserPermission(
                eq("dev-admin"), eq("default-org"), eq("knowledge-from-doc"), eq(0));
        verify(knowledgeService).listDocSegments(eq("dev-admin"), eq("default-org"), any(Map.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void knowledgeQaPairRouteStopsBeforeServiceWhenResolvedPermissionIsDenied() throws Exception {
        when(knowledgeService.resolveKnowledgeId(anyString(), anyString(), any(Map.class)))
                .thenReturn("knowledge-from-qa");
        doThrow(new IllegalArgumentException("knowledge_perm: qa denied"))
                .when(knowledgeService).checkKnowledgeUserPermission(
                        eq("dev-app"), eq("default-org"), eq("knowledge-from-qa"), eq(10));

        mockMvc.perform(put("/user/api/v1/knowledge/qa/pair")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qaPairId\":\"qa-001\",\"question\":\"Q\",\"answer\":\"A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("knowledge_perm: qa denied"));

        verify(knowledgeService).resolveKnowledgeId(eq("dev-app"), eq("default-org"), any(Map.class));
        verify(knowledgeService, times(0)).updateQaPair(anyString(), anyString(), any(Map.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void knowledgeNameRouteResolvesKnowledgeBeforeService() throws Exception {
        when(knowledgeService.resolveKnowledgeId(anyString(), anyString(), any(Map.class)))
                .thenReturn("knowledge-from-name");
        when(knowledgeService.getDocByName(anyString(), anyString(), any(Map.class)))
                .thenReturn(singleton("knowledgeId", "knowledge-from-name"));

        mockMvc.perform(get("/user/api/v1/knowledge/doc/by/name")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeName", "Resolve KB")
                        .param("docName", "Guide.txt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(knowledgeService).resolveKnowledgeId(eq("dev-admin"), eq("default-org"), any(Map.class));
        verify(knowledgeService).checkKnowledgeUserPermission(
                eq("dev-admin"), eq("default-org"), eq("knowledge-from-name"), eq(0));
        verify(knowledgeService).getDocByName(eq("dev-admin"), eq("default-org"), any(Map.class));
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
                .thenReturn(exportCreated("export-001", "/user/api/v1/knowledge/export/file/export-001/dev.csv"));
        when(knowledgeService.exportDocs(anyString(), anyString(), any(Map.class)))
                .thenReturn(exportCreated("export-002", "/user/api/v1/knowledge/export/file/export-002/dev.zip"));
        when(knowledgeService.listExportRecords(anyString(), anyString(), any(Map.class)))
                .thenReturn(exportRecordPage(exportRecord("export-001", "/user/api/v1/knowledge/export/file/export-001/dev.csv")));
        when(knowledgeService.getExportRecordFile(anyString(), anyString(), any(Map.class)))
                .thenReturn(exportFile("dev.csv", "text/csv;charset=UTF-8", "knowledgeName,question\nDev QA,Wanwu"));
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
                .andExpect(jsonPath("$.data.recordCreated").value(true))
                .andExpect(jsonPath("$.data.exportRecordId").value("export-001"));

        mockMvc.perform(post("/user/api/v1/knowledge/doc/export")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-qa-001\",\"docIdList\":[\"doc-001\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordCreated").value(true))
                .andExpect(jsonPath("$.data.exportRecordId").value("export-002"));

        mockMvc.perform(get("/user/api/v1/knowledge/export/record/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("knowledgeId", "knowledge-qa-001")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].exportRecordId").value("export-001"))
                .andExpect(jsonPath("$.data.list[0].status").value(2));

        mockMvc.perform(get("/user/api/v1/knowledge/export/file/export-001/dev.csv")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().string(containsString("Dev QA,Wanwu")));

        mockMvc.perform(delete("/user/api/v1/knowledge/export/record")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-qa-001\",\"exportRecordId\":\"export-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

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
        verify(knowledgeService).exportDocs(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).listExportRecords(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).getExportRecordFile(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteExportRecord(anyString(), anyString(), any(Map.class));
        verify(knowledgeService).deleteQaPairs(anyString(), anyString(), any(Map.class));
    }

    @Test
    public void knowledgeImportRoutesReadUploadedFileContent() throws Exception {
        String qaFileId = "qa-upload-bff-test.csv";
        String reportFileId = "report-upload-bff-test.csv";
        String xlsxFileId = "doc-upload-bff-test.xlsx";
        String qaXlsxFileId = "qa-upload-bff-test.xlsx";
        String reportXlsxFileId = "report-upload-bff-test.xlsx";
        byte[] xlsxBytes = new byte[]{80, 75, 3, 4, 1, 2, 3, 4};
        byte[] qaXlsxBytes = new byte[]{80, 75, 3, 4, 5, 6, 7, 8};
        byte[] reportXlsxBytes = new byte[]{80, 75, 3, 4, 9, 10, 11, 12};
        UploadedFileStore.defaultStore().writeText(qaFileId, "question,answer\nWhat is Wanwu?,An AI platform");
        UploadedFileStore.defaultStore().writeText(reportFileId, "title,content\nReport A,Uploaded report content");
        UploadedFileStore.defaultStore().writeBytes(xlsxFileId, xlsxBytes);
        UploadedFileStore.defaultStore().writeBytes(qaXlsxFileId, qaXlsxBytes);
        UploadedFileStore.defaultStore().writeBytes(reportXlsxFileId, reportXlsxBytes);

        mockMvc.perform(post("/user/api/v1/knowledge/doc/import")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-doc-001\","
                                + "\"docInfoList\":[{\"fileUploadId\":\"" + xlsxFileId
                                + "\",\"docName\":\"Guide.xlsx\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<Map> docCaptor = forClass(Map.class);
        verify(knowledgeService).importDocs(anyString(), anyString(), docCaptor.capture());
        Map<String, Object> docRequest = docCaptor.getValue();
        Map<String, Object> docInfo = listMap(docRequest.get("docInfoList")).get(0);
        assertEquals(Base64.getEncoder().encodeToString(xlsxBytes), docInfo.get("contentBase64"));
        assertEquals("xlsx", docInfo.get("docType"));

        mockMvc.perform(post("/user/api/v1/knowledge/qa/pair/import")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-qa-001\","
                                + "\"docInfoList\":[{\"docId\":\"" + qaFileId + "\",\"docName\":\"qa.csv\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/qa/pair/import")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-qa-001\","
                                + "\"docInfoList\":[{\"docUrl\":\"" + qaXlsxFileId + "\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<Map> qaCaptor = forClass(Map.class);
        verify(knowledgeService, times(2)).importQaPairs(anyString(), anyString(), qaCaptor.capture());
        Map<String, Object> qaRequest = qaCaptor.getAllValues().get(0);
        Map<String, Object> qaDoc = listMap(qaRequest.get("docInfoList")).get(0);
        assertEquals("question,answer\nWhat is Wanwu?,An AI platform", qaDoc.get("content"));
        Map<String, Object> qaXlsxRequest = qaCaptor.getAllValues().get(1);
        Map<String, Object> qaXlsxDoc = listMap(qaXlsxRequest.get("docInfoList")).get(0);
        assertEquals(Base64.getEncoder().encodeToString(qaXlsxBytes), qaXlsxDoc.get("contentBase64"));
        assertEquals("xlsx", qaXlsxDoc.get("docType"));

        mockMvc.perform(post("/user/api/v1/knowledge/report/batch/add")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-report-001\","
                                + "\"fileUploadId\":\"" + reportFileId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/api/v1/knowledge/report/batch/add")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"knowledge-report-001\","
                                + "\"fileUploadId\":\"" + reportXlsxFileId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<Map> reportCaptor = forClass(Map.class);
        verify(knowledgeService, times(2)).batchAddReports(anyString(), anyString(), reportCaptor.capture());
        assertEquals("title,content\nReport A,Uploaded report content", reportCaptor.getAllValues().get(0).get("content"));
        assertEquals(Base64.getEncoder().encodeToString(reportXlsxBytes), reportCaptor.getAllValues().get(1).get("contentBase64"));
        assertEquals("xlsx", reportCaptor.getAllValues().get(1).get("docType"));
    }

    @Test
    public void batchSegmentImportReadsUploadedCsvContent() throws Exception {
        String segmentFileId = "segment-upload-bff-test.csv";
        String csv = "content,labels\n"
                + "\"Bulk segment A\",\"alpha,beta\"\n"
                + "Bulk segment B,manual";
        UploadedFileStore.defaultStore().writeText(segmentFileId, csv);
        when(knowledgeService.resolveKnowledgeId(anyString(), anyString(), any(Map.class)))
                .thenReturn("knowledge-segment-001");

        mockMvc.perform(post("/user/api/v1/knowledge/doc/segment/batch/create")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-segment-001\","
                                + "\"fileUploadId\":\"" + segmentFileId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<Map> segmentCaptor = forClass(Map.class);
        verify(knowledgeService).batchCreateDocSegment(anyString(), anyString(), segmentCaptor.capture());
        assertEquals(csv, segmentCaptor.getValue().get("content"));
        assertEquals(segmentFileId, segmentCaptor.getValue().get("fileUploadId"));
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
    public void createAssistantUsesAppUserForAppToken() throws Exception {
        when(appService.createAssistant(any(AssistantCreateCommand.class)))
                .thenReturn(new AssistantCreateResult("assistant-app-001"));

        mockMvc.perform(post("/user/api/v1/assistant")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":1,\"name\":\"AppAgent\",\"desc\":\"app scoped\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assistantId").value("assistant-app-001"));

        org.mockito.ArgumentCaptor<AssistantCreateCommand> captor = forClass(AssistantCreateCommand.class);
        verify(appService).createAssistant(captor.capture());
        assertEquals("dev-app", captor.getValue().getUserId());
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
    @SuppressWarnings("unchecked")
    public void assistantConfigChecksReferencedModelIdsBeforeSave() throws Exception {
        doThrow(new IllegalArgumentException("bff_model_perm: model-private"))
                .when(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), any(List.class));

        mockMvc.perform(put("/user/api/v1/assistant/config")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assistantId\":\"assistant-001\",\"modelConfig\":{\"modelId\":\"model-private\"},\"rerankConfig\":{\"modelId\":\"rerank-private\"},\"recommendConfig\":{\"modelConfig\":{\"modelId\":\"recommend-private\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg").value("bff_model_perm: model-private"));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService).checkModelUserPermission(eq("dev-app"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Arrays.asList("model-private", "rerank-private", "recommend-private"), modelIdsCaptor.getValue());
        verify(appService, times(0)).updateAssistantConfig(any(AssistantConfigUpdateCommand.class));
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
    @SuppressWarnings("unchecked")
    public void ragConfigChecksReferencedModelIdsBeforeSave() throws Exception {
        mockMvc.perform(put("/user/api/v1/appspace/rag/config")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ragId\":\"rag-001\",\"modelConfig\":{\"modelId\":\"llm-001\"},\"rerankConfig\":{\"modelId\":\"rerank-001\"},\"qaRerankConfig\":{\"modelId\":\"qa-rerank-001\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<List> modelIdsCaptor = forClass(List.class);
        verify(modelService).checkModelUserPermission(eq("dev-admin"), eq("default-org"), modelIdsCaptor.capture());
        assertEquals(Arrays.asList("llm-001", "rerank-001", "qa-rerank-001"), modelIdsCaptor.getValue());
        verify(appService).updateRagConfig(any(RagConfigUpdateCommand.class));
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
        when(mcpService.listToolSelect(anyString(), anyString(), anyString()))
                .thenReturn(null)
                .thenReturn(listResult(toolSelect("builtin-weather", "Weather Tool", "builtin")));
        when(mcpService.listToolActions(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null)
                .thenReturn(actionList("get_weather"));
        when(mcpService.getToolActionDetail(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null)
                .thenReturn(actionDetail("get_weather"));
        when(mcpService.getToolSquare(anyString(), anyString(), anyString()))
                .thenReturn(toolSquareDetail("builtin-weather", "Weather Tool"));
        when(appService.listAssistantMcpSelect(anyString(), anyString()))
                .thenReturn(listResult(mcpSelect("mcp-001", "Search MCP", "mcp")));
        when(appService.listAssistantMcpActions(any(AssistantResourceCommand.class)))
                .thenReturn(actionList("search"));
        when(appService.listAssistantWorkflowSelect(anyString(), anyString(), anyString()))
                .thenReturn(listResult(workflowSelect("workflow-001", "Workflow One")));
        Map<String, Object> persistedAction = map(
                "actionId", "legacy-action-001",
                "id", "legacy-action-001",
                "assistantId", "assistant-action-test",
                "toolId", "custom-tool-001",
                "toolType", "custom",
                "actionName", "lookup_policy",
                "name", "lookup_policy",
                "enable", true);
        Map<String, Object> disabledAction = new LinkedHashMap<>(persistedAction);
        disabledAction.put("enable", false);
        when(appService.createLegacyAssistantAction(any())).thenReturn(persistedAction);
        when(appService.updateLegacyAssistantAction(any())).thenReturn(disabledAction);
        when(appService.listLegacyAssistantActions(any()))
                .thenReturn(listResult(persistedAction))
                .thenReturn(listResult(persistedAction))
                .thenReturn(listResult(disabledAction))
                .thenReturn(emptyListResult());

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
        mockMvc.perform(get("/user/api/v1/workflow/tool/select")
                        .header("Authorization", "Bearer dev-token")
                        .param("toolType", "builtin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].toolId").value("builtin-weather"))
                .andExpect(jsonPath("$.data.list[0].actions[0].actionName").value("get_weather"));
        mockMvc.perform(get("/user/api/v1/workflow/tool/action")
                        .header("Authorization", "Bearer dev-token")
                        .param("toolId", "builtin-weather")
                        .param("toolType", "builtin")
                        .param("actionName", "get_weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actionId").value("get_weather"))
                .andExpect(jsonPath("$.data.inputs[0].name").value("query"));
        mockMvc.perform(get("/user/api/v1/workflow/tool/box")
                        .header("Authorization", "Bearer dev-token")
                        .param("box_id", "builtin-weather")
                        .param("box_type", "builtin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.box_id").value("builtin-weather"))
                .andExpect(jsonPath("$.data.tools[0].tool_id").value("get_weather"))
                .andExpect(jsonPath("$.data.tools[0].metadata.api_spec.request_body.type").value("object"));
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

        mockMvc.perform(post("/user/api/v1/assistant/action")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actionId\":\"legacy-action-001\",\"assistantId\":\"assistant-action-test\",\"toolId\":\"custom-tool-001\",\"toolType\":\"custom\",\"actionName\":\"lookup_policy\",\"desc\":\"Lookup policy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actionId").value("legacy-action-001"))
                .andExpect(jsonPath("$.data.enable").value(true));
        mockMvc.perform(get("/user/api/v1/assistant/action")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-action-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].actionName").value("lookup_policy"))
                .andExpect(jsonPath("$.data.total").value(1));
        mockMvc.perform(put("/user/api/v1/assistant/action/enable")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actionId\":\"legacy-action-001\",\"enable\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enable").value(false));
        mockMvc.perform(get("/user/api/v1/assistant/action")
                        .header("Authorization", "Bearer dev-token")
                        .param("actionId", "legacy-action-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enable").value(false));
        mockMvc.perform(delete("/user/api/v1/assistant/action")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actionId\":\"legacy-action-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/user/api/v1/assistant/action")
                        .header("Authorization", "Bearer dev-token")
                        .param("assistantId", "assistant-action-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

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
    public void assistantDraftStreamUsesConfiguredOpenAiCompatibleModelBeforePersisting() throws Exception {
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange));
            respondSse(exchange, "data: {\"id\":\"chatcmpl-agent\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"model \"},"
                    + "\"finish_reason\":null}],\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":1,\"total_tokens\":4}}\n\n"
                    + "data: {\"id\":\"chatcmpl-agent\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"answer\"},"
                    + "\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":2,\"total_tokens\":5}}\n\n"
                    + "data: [DONE]\n\n");
        });
        server.start();
        try {
            Map<String, Object> assistant = map("assistantId", "assistant-001",
                    "modelConfig", map("modelId", "model-001"));
            when(appService.getAssistantDraft(any(AssistantDetailQuery.class))).thenReturn(assistant);
            ModelInfo model = modelInfo("model-001", "DeepSeek Chat", "llm");
            model.setProvider("openai-compatible");
            model.setConfig(map("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1",
                    "apiKey", "local-key"));
            when(modelService.getModel(anyString(), anyString(), eq("model-001"))).thenReturn(model);
            when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class)))
                    .thenAnswer(invocation -> {
                        AssistantConversationStreamCommand command = invocation.getArgument(0);
                        AssistantConversationStreamResult result = new AssistantConversationStreamResult();
                        result.setAssistantId(command.getAssistantId());
                        result.setConversationId("conversation-001");
                        result.setDetailId("detail-001");
                        result.setResponse(command.getOverrideResponse());
                        result.setCreatedAt(1782806400000L);
                        return result;
                    });

            mockMvc.perform(post("/user/api/v1/assistant/stream/draft")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"assistantId\":\"assistant-001\",\"prompt\":\"hello agent\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                    .andExpect(content().string(containsString("model answer")));

            assertTrue(upstreamBody.get().contains("\"stream\":true"));
            assertTrue(upstreamBody.get().contains("\"content\":\"hello agent\""));
            ArgumentCaptor<AssistantConversationStreamCommand> captor =
                    forClass(AssistantConversationStreamCommand.class);
            verify(appService).streamAssistantConversation(captor.capture());
            assertEquals("model answer", captor.getValue().getOverrideResponse());
        } finally {
            server.stop(0);
        }
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
        Map<String, Object> searchItem = new LinkedHashMap<>();
        searchItem.put("title", "PolicyGuide.txt");
        searchItem.put("snippet", "RAG evidence.");
        result.setSearchList(Collections.singletonList(searchItem));
        when(appService.streamRagChat(any(RagChatCommand.class))).thenReturn(result);

        mockMvc.perform(post("/user/api/v1/rag/chat/draft")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ragId\":\"rag-001\",\"question\":\"what is policy\",\"history\":[{\"query\":\"q1\",\"response\":\"a1\",\"needHistory\":true}],\"fileInfo\":[{\"fileName\":\"a.txt\",\"fileSize\":3,\"fileUrl\":\"http://file/a.txt\"}]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"type\":\"RUN_STARTED\"")))
                .andExpect(content().string(containsString("\"type\":\"TEXT_MESSAGE_CONTENT\"")))
                .andExpect(content().string(containsString("rag_search_list")))
                .andExpect(content().string(containsString("PolicyGuide.txt")))
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
    public void ragDraftChatUsesConfiguredOpenAiCompatibleModelBeforePersisting() throws Exception {
        AtomicReference<String> upstreamBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            upstreamBody.set(readBody(exchange));
            respondSse(exchange, "data: {\"id\":\"chatcmpl-rag\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"rag \"},"
                    + "\"finish_reason\":null}],\"usage\":{\"prompt_tokens\":4,\"completion_tokens\":1,\"total_tokens\":5}}\n\n"
                    + "data: {\"id\":\"chatcmpl-rag\",\"object\":\"chat.completion.chunk\","
                    + "\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"answer\"},"
                    + "\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":4,\"completion_tokens\":2,\"total_tokens\":6}}\n\n"
                    + "data: [DONE]\n\n");
        });
        server.start();
        try {
            Map<String, Object> rag = map("ragId", "rag-001",
                    "modelConfig", map("modelId", "model-001"));
            when(appService.getRagDraft(any(RagDetailQuery.class))).thenReturn(rag);
            ModelInfo model = modelInfo("model-001", "DeepSeek Chat", "llm");
            model.setProvider("openai-compatible");
            model.setConfig(map("endpointUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1",
                    "apiKey", "local-key"));
            when(modelService.getModel(anyString(), anyString(), eq("model-001"))).thenReturn(model);
            when(appService.streamRagChat(any(RagChatCommand.class))).thenAnswer(invocation -> {
                RagChatCommand command = invocation.getArgument(0);
                RagChatResult result = new RagChatResult();
                result.setRagId(command.getRagId());
                result.setQuestion(command.getQuestion());
                result.setResponse(command.getOverrideResponse());
                result.setSearchList(Collections.emptyList());
                result.setQaSearchList(Collections.emptyList());
                result.setCreatedAt(1782806400000L);
                return result;
            });

            mockMvc.perform(post("/user/api/v1/rag/chat/draft")
                            .header("Authorization", "Bearer dev-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"ragId\":\"rag-001\",\"question\":\"what is policy\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                    .andExpect(content().string(containsString("rag answer")));

            assertTrue(upstreamBody.get().contains("\"stream\":true"));
            assertTrue(upstreamBody.get().contains("\"content\":\"what is policy\""));
            ArgumentCaptor<RagChatCommand> captor = forClass(RagChatCommand.class);
            verify(appService).streamRagChat(captor.capture());
            assertEquals("rag answer", captor.getValue().getOverrideResponse());
        } finally {
            server.stop(0);
        }
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

    private Map<String, Object> keywordPage(Map<String, Object> keyword) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("list", Collections.singletonList(keyword));
        page.put("total", 1);
        page.put("pageNo", 1);
        page.put("pageSize", 10);
        return page;
    }

    private Map<String, Object> keywordInfo(long id, String name, String alias, String knowledgeId,
                                            String knowledgeName) {
        Map<String, Object> keyword = new LinkedHashMap<>();
        keyword.put("id", id);
        keyword.put("name", name);
        keyword.put("alias", alias);
        keyword.put("knowledgeBaseIds", Collections.singletonList(knowledgeId));
        keyword.put("knowledgeBaseNames", Collections.singletonList(knowledgeName));
        keyword.put("updatedAt", "2026-06-30 00:00:00");
        return keyword;
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

    private Map<String, Object> knowledgeGraph(String knowledgeId, String knowledgeName, String docName) {
        Map<String, Object> knowledgeNode = new LinkedHashMap<>();
        knowledgeNode.put("entity_name", "Knowledge: " + knowledgeName);
        knowledgeNode.put("entity_type", "knowledge");
        knowledgeNode.put("description", "docs");
        knowledgeNode.put("source_id", Collections.singletonList(knowledgeId));
        knowledgeNode.put("rank", 10);
        knowledgeNode.put("pagerank", 1.0D);

        Map<String, Object> docNode = new LinkedHashMap<>();
        docNode.put("entity_name", "Document: " + docName);
        docNode.put("entity_type", "document");
        docNode.put("description", "txt");
        docNode.put("source_id", Collections.singletonList("doc-001"));
        docNode.put("rank", 5);
        docNode.put("pagerank", 0.75D);

        Map<String, Object> edge = new LinkedHashMap<>();
        edge.put("source_entity", "Knowledge: " + knowledgeName);
        edge.put("target_entity", "Document: " + docName);
        edge.put("description", "contains document");
        edge.put("weight", 1.0D);
        edge.put("source_id", Collections.singletonList("doc-001"));

        Map<String, Object> graph = new LinkedHashMap<>();
        graph.put("directed", true);
        graph.put("multigraph", false);
        graph.put("graph", singleton("source_id", Collections.singletonList(knowledgeId)));
        graph.put("nodes", Arrays.asList(knowledgeNode, docNode));
        graph.put("edges", Collections.singletonList(edge));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processingCount", 0);
        result.put("successCount", 1);
        result.put("failCount", 0);
        result.put("total", 1);
        result.put("graph", graph);
        return result;
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

    private Map<String, Object> reportPage(Map<String, Object> report) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("list", Collections.singletonList(report));
        page.put("total", 1);
        page.put("pageNo", 1);
        page.put("pageSize", 8);
        page.put("createdAt", "1782857150000");
        page.put("status", 2);
        page.put("canGenerate", true);
        page.put("canAddReport", true);
        page.put("generateLabel", "Regenerate");
        page.put("lastImportStatus", -1);
        return page;
    }

    private Map<String, Object> report(String contentId, String title, String content) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("contentId", contentId);
        report.put("title", title);
        report.put("content", content);
        return report;
    }

    private Map<String, Object> externalApi(String externalApiId, String name) {
        Map<String, Object> api = new LinkedHashMap<>();
        api.put("externalApiId", externalApiId);
        api.put("name", name);
        api.put("description", "external api");
        api.put("baseUrl", "https://dify.example/v1");
        api.put("apiKey", "key");
        return api;
    }

    private Map<String, Object> externalKnowledge(String externalKnowledgeId, String name, String externalApiId) {
        Map<String, Object> knowledge = new LinkedHashMap<>();
        knowledge.put("externalKnowledgeId", externalKnowledgeId);
        knowledge.put("externalKnowledgeName", name);
        knowledge.put("externalApiId", externalApiId);
        knowledge.put("externalApiName", "Dify Dev");
        knowledge.put("externalSource", "dify");
        knowledge.put("docCount", 3);
        return knowledge;
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

    private Map<String, Object> exportCreated(String exportRecordId, String filePath) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordCreated", true);
        result.put("exportRecordId", exportRecordId);
        result.put("fileUrl", filePath);
        result.put("downloadUrl", filePath);
        return result;
    }

    private Map<String, Object> exportRecordPage(Map<String, Object> record) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("list", Collections.singletonList(record));
        page.put("total", 1);
        page.put("pageNo", 1);
        page.put("pageSize", 10);
        return page;
    }

    private Map<String, Object> exportRecord(String exportRecordId, String filePath) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("exportRecordId", exportRecordId);
        record.put("author", "admin");
        record.put("status", 2);
        record.put("filePath", filePath);
        record.put("errorMsg", "");
        record.put("exportTime", "2026-06-30 00:00:00");
        record.put("knowledgeName", "Dev QA");
        return record;
    }

    private Map<String, Object> exportFile(String fileName, String contentType, String content) {
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("fileName", fileName);
        file.put("contentType", contentType);
        file.put("content", content);
        file.put("contentBase64", "");
        return file;
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

    private Map<String, Object> knowledgeHitResult(String title, String snippet, String knowledgeName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", title);
        result.put("snippet", snippet);
        result.put("knowledgeName", knowledgeName);
        result.put("childContentList", Collections.emptyList());
        result.put("childScore", Collections.emptyList());
        result.put("contentType", "text");
        result.put("score", 1.0D);
        result.put("rerankInfo", Collections.emptyList());
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

    private Map<String, Object> childSegment(String childId, String parentId, int childNum, String content) {
        Map<String, Object> child = new LinkedHashMap<>();
        child.put("childId", childId);
        child.put("parentId", parentId);
        child.put("childNum", childNum);
        child.put("content", content);
        return child;
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
                permission("setting"),
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
                permission("resource.skill"),
                permission("resource.safety"),
                permission("operation"),
                permission("operation.oauth"),
                permission("operation.statistic_client"),
                permission("exploration"),
                permission("exploration.app"),
                permission("exploration.mcp"),
                permission("exploration.template"),
                permission("exploration.skill"),
                permission("app_observability"),
                permission("app_observability.statistic")
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

    private static void respondSse(HttpExchange exchange, String sse) throws IOException {
        byte[] response = sse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listMap(Object value) {
        return (List<Map<String, Object>>) value;
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

    private Map<String, Object> toolSquareDetail(String toolId, String name) {
        Map<String, Object> result = toolSelect(toolId, name, "builtin");
        result.put("toolSquareId", toolId);
        result.put("name", name);
        result.put("tools", Collections.singletonList(action("get_weather")));
        result.put("apiAuth", Collections.emptyMap());
        return result;
    }

    private Map<String, Object> action(String actionName) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("type", "string");
        query.put("description", "Input text");
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("query", query);
        Map<String, Object> inputSchema = new LinkedHashMap<>();
        inputSchema.put("type", "object");
        inputSchema.put("properties", properties);
        inputSchema.put("required", Collections.singletonList("query"));
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("name", actionName);
        action.put("description", "Development action");
        action.put("inputSchema", inputSchema);
        action.put("method", "POST");
        action.put("path", "/" + actionName);
        return action;
    }

    private Map<String, Object> platformConfig() {
        Map<String, Object> email = new LinkedHashMap<>();
        email.put("status", false);
        Map<String, Object> loginEmail = new LinkedHashMap<>();
        loginEmail.put("email", email);
        Map<String, Object> login = new LinkedHashMap<>();
        login.put("welcomeText", "bff_custom_login_welcome_text");
        login.put("platformDesc", "bff_custom_login_platform_desc");
        login.put("loginButtonColor", "#384BF7");
        Map<String, Object> home = new LinkedHashMap<>();
        home.put("title", "bff_custom_home_title");
        Map<String, Object> tab = new LinkedHashMap<>();
        tab.put("title", "bff_custom_tab_title");
        Map<String, Object> about = new LinkedHashMap<>();
        about.put("copyright", "bff_custom_about_copyright");
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("login", login);
        config.put("home", home);
        config.put("tab", tab);
        config.put("about", about);
        config.put("loginEmail", loginEmail);
        return config;
    }

    private Map<String, Object> customPlatformConfig() {
        Map<String, Object> config = platformConfig();
        ((Map<String, Object>) config.get("login")).put("welcomeText", "Custom welcome");
        ((Map<String, Object>) config.get("home")).put("title", "Custom home");
        ((Map<String, Object>) config.get("tab")).put("title", "Custom tab");
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
