package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationSelectResult;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogDeleteCommand;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordSaveCommand;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogSaveCommand;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.model.dto.ModelStatusCommand;
import com.unicomai.wanwu.api.model.dto.ModelTypeQuery;
import com.unicomai.wanwu.api.model.dto.ModelUpsertCommand;
import com.unicomai.wanwu.api.model.dto.ProviderListQuery;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeResult;
import com.unicomai.wanwu.api.model.dto.RecommendModelQuery;
import com.unicomai.wanwu.api.model.dto.RecommendModelResult;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuFrontendApiController {

    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
    private static final String AGENT_APP_TYPE = "agent";
    private static final String CONVERSATION_TYPE_PUBLISHED = "published";
    private static final String CONVERSATION_TYPE_DRAFT = "draft";
    private static final String OPENURL_PUBLIC_PREFIX = "/service/url/openurl/v1/agent";
    private static final ObjectMapper JSON = new ObjectMapper();

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private KnowledgeService knowledgeService;

    public WanwuFrontendApiController() {
    }

    public WanwuFrontendApiController(IamService iamService, AppService appService) {
        this(iamService, appService, null, null);
    }

    public WanwuFrontendApiController(IamService iamService, AppService appService, ModelService modelService) {
        this(iamService, appService, modelService, null);
    }

    public WanwuFrontendApiController(IamService iamService, AppService appService, ModelService modelService,
                                      KnowledgeService knowledgeService) {
        this.iamService = iamService;
        this.appService = appService;
        this.modelService = modelService;
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/base/captcha")
    public FrontendResponse<CaptchaResult> captcha() {
        return FrontendResponse.ok(iamService.captcha());
    }

    @PostMapping("/base/login")
    public FrontendResponse<LoginResult> login(@RequestBody LoginCommand command) {
        try {
            return FrontendResponse.ok(iamService.login(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/user/permission")
    public FrontendResponse<PermissionResult> permission(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return FrontendResponse.ok(iamService.permission(extractToken(authorization)));
    }

    @GetMapping("/org/select")
    public FrontendResponse<OrganizationSelectResult> selectOrganizations() {
        return FrontendResponse.ok(iamService.selectOrganizations());
    }

    @GetMapping("/user/list")
    public FrontendResponse<Map<String, Object>> listUsers(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.listUsers(userContext.getOrgId(), defaultIfBlank(name, ""), pageNo, pageSize));
    }

    @GetMapping("/role/select")
    public FrontendResponse<Map<String, Object>> selectRoles(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.selectRoles(userContext.getOrgId()));
    }

    @GetMapping("/role/template")
    public FrontendResponse<Map<String, Object>> roleTemplate(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.roleTemplate(userContext.getUserId(), userContext.getOrgId()));
    }

    @GetMapping("/role/list")
    public FrontendResponse<Map<String, Object>> listRoles(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.listRoles(userContext.getUserId(), userContext.getOrgId(), defaultIfBlank(name, ""), pageNo, pageSize));
    }

    @GetMapping("/role/info")
    public FrontendResponse<Map<String, Object>> roleInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("roleId") String roleId) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.roleInfo(userContext.getUserId(), userContext.getOrgId(), roleId));
    }

    @GetMapping("/org/list")
    public FrontendResponse<Map<String, Object>> listOrganizations(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.listOrganizations(userContext.getOrgId(), defaultIfBlank(name, ""), pageNo, pageSize));
    }

    @GetMapping("/org/info")
    public FrontendResponse<Map<String, Object>> organizationInfo(@RequestParam("orgId") String orgId) {
        return FrontendResponse.ok(iamService.organizationInfo(orgId));
    }

    @GetMapping("/base/custom")
    public FrontendResponse<Map<String, Object>> platformConfig() {
        return FrontendResponse.ok(iamService.platformConfig());
    }

    @GetMapping("/model/list")
    public FrontendResponse<ModelListResult> listModels(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "modelType", required = false) String modelType,
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "filterScope", required = false) String filterScope,
            @RequestParam(value = "scopeType", required = false) String scopeType) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(modelService.listModels(new ModelListQuery(
                userContext.getUserId(),
                userContext.getOrgId(),
                defaultIfBlank(modelType, ""),
                defaultIfBlank(provider, ""),
                defaultIfBlank(displayName, ""),
                defaultIfBlank(filterScope, ""),
                defaultIfBlank(scopeType, ""))));
    }

    @GetMapping("/model")
    public FrontendResponse<ModelInfo> getModel(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("modelId") String modelId) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(modelService.getModel(userContext.getUserId(), userContext.getOrgId(), modelId));
    }

    @PostMapping("/model")
    public FrontendResponse<ModelInfo> importModel(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ModelUpsertRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ModelUpsertCommand command = request == null ? new ModelUpsertCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(modelService.importModel(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/model")
    public FrontendResponse<Map<String, Object>> updateModel(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ModelUpsertRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ModelUpsertCommand command = request == null ? new ModelUpsertCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            modelService.updateModel(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/model")
    public FrontendResponse<Map<String, Object>> deleteModel(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ModelIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            String modelId = request == null ? "" : request.getModelId();
            modelService.deleteModel(userContext.getUserId(), userContext.getOrgId(), modelId);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/model/status")
    public FrontendResponse<Map<String, Object>> changeModelStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ModelStatusRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ModelStatusCommand command = request == null ? new ModelStatusCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            modelService.changeModelStatus(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/model/select/{modelType}")
    public FrontendResponse<ModelListResult> selectModels(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @org.springframework.web.bind.annotation.PathVariable("modelType") String modelType) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(modelService.listTypeModels(new ModelTypeQuery(
                userContext.getUserId(),
                userContext.getOrgId(),
                modelType)));
    }

    @GetMapping("/model/import/providers")
    public FrontendResponse<ProviderModelTypeResult> listModelImportProviders(
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "modelType", required = false) String modelType) {
        return FrontendResponse.ok(modelService.listImportProviders(
                new ProviderListQuery(defaultIfBlank(provider, ""), defaultIfBlank(modelType, ""))));
    }

    @GetMapping("/model/recommend")
    public FrontendResponse<RecommendModelResult> recommendModels(
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "modelType", required = false) String modelType) {
        return FrontendResponse.ok(modelService.recommendModels(
                new RecommendModelQuery(defaultIfBlank(provider, ""), defaultIfBlank(modelType, ""))));
    }

    @PostMapping("/model/validate-thinking")
    public FrontendResponse<Map<String, Object>> validateModelThinking() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/model/experience/dialog")
    public FrontendResponse<ModelExperienceDialogInfo> saveModelExperienceDialog(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ModelExperienceDialogRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ModelExperienceDialogSaveCommand command = request == null
                    ? new ModelExperienceDialogSaveCommand()
                    : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(modelService.saveModelExperienceDialog(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/model/experience/dialogs")
    public FrontendResponse<ModelExperienceDialogListResult> listModelExperienceDialogs(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(modelService.listModelExperienceDialogs(
                new ModelExperienceDialogListQuery(userContext.getUserId(), userContext.getOrgId())));
    }

    @DeleteMapping("/model/experience/dialog")
    public FrontendResponse<Map<String, Object>> deleteModelExperienceDialog(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ModelExperienceDialogIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            String modelExperienceId = request == null ? "" : request.getModelExperienceId();
            modelService.deleteModelExperienceDialog(
                    new ModelExperienceDialogDeleteCommand(userContext.getUserId(), userContext.getOrgId(), modelExperienceId));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/model/experience/dialog/records")
    public FrontendResponse<ModelExperienceDialogRecordListResult> listModelExperienceDialogRecords(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("modelExperienceId") String modelExperienceId) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(modelService.listModelExperienceDialogRecords(
                new ModelExperienceDialogRecordQuery(userContext.getUserId(), userContext.getOrgId(), modelExperienceId, "")));
    }

    @PostMapping(value = "/model/experience/llm", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> modelExperienceLlm(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ModelExperienceLlmRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ModelExperienceLlmRequest safe = request == null ? new ModelExperienceLlmRequest() : request;
            ModelInfo model = modelService.getModel(userContext.getUserId(), userContext.getOrgId(), safe.getModelId());
            if (!Boolean.TRUE.equals(model.getIsActive())) {
                return ResponseEntity.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson("model is inactive"));
            }
            String answer = "Echo: " + defaultIfBlank(safe.getContent(), "");
            modelService.saveModelExperienceDialogRecord(new ModelExperienceDialogRecordSaveCommand(
                    userContext.getUserId(), userContext.getOrgId(), safe.getModelExperienceId(), safe.getModelId(),
                    safe.getSessionId(), defaultIfBlank(safe.getContent(), ""), "", "", "user"));
            modelService.saveModelExperienceDialogRecord(new ModelExperienceDialogRecordSaveCommand(
                    userContext.getUserId(), userContext.getOrgId(), safe.getModelExperienceId(), safe.getModelId(),
                    safe.getSessionId(), answer, "", "", "assistant"));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(modelExperienceSseFrames(safe.getSessionId(), model.getModel(), answer));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        }
    }

    @GetMapping("/appspace/assistant/list")
    public FrontendResponse<ApplicationListResult> assistantList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "appType", required = false) String appType,
            @RequestParam(value = "name", required = false) String name) {
        UserContext userContext = userContext(authorization);
        String effectiveAppType = isBlank(appType) ? AGENT_APP_TYPE : appType;
        return FrontendResponse.ok(appService.listAssistants(
                new ApplicationListQuery(effectiveAppType, name, userContext.getUserId(), userContext.getOrgId())));
    }

    @GetMapping("/appspace/app/list")
    public FrontendResponse<ApplicationListResult> appspaceAppList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "appType", required = false) String appType,
            @RequestParam(value = "name", required = false) String name) {
        try {
            UserContext userContext = userContext(authorization);
            String effectiveAppType = isBlank(appType) ? AGENT_APP_TYPE : appType;
            return FrontendResponse.ok(appService.listApplications(
                    new ApplicationListQuery(effectiveAppType, name, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/api/key")
    public FrontendResponse<ApiKeyInfo> createApiKey(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ApiKeyCreateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ApiKeyCreateCommand command = request == null ? new ApiKeyCreateCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.createApiKey(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/api/key")
    public FrontendResponse<Map<String, Object>> updateApiKey(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ApiKeyUpdateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ApiKeyUpdateCommand command = request == null ? new ApiKeyUpdateCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateApiKey(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/api/key")
    public FrontendResponse<Map<String, Object>> deleteApiKey(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ApiKeyIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ApiKeyDeleteCommand command = request == null ? new ApiKeyDeleteCommand() : request.toDeleteCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.deleteApiKey(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/api/key/list")
    public FrontendResponse<ApiKeyPageResult> listApiKeys(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        try {
            UserContext userContext = userContext(authorization);
            ApiKeyListQuery query = new ApiKeyListQuery(pageNo, pageSize, userContext.getUserId(), userContext.getOrgId());
            return FrontendResponse.ok(appService.listApiKeys(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/api/key/status")
    public FrontendResponse<Map<String, Object>> updateApiKeyStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ApiKeyStatusRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ApiKeyStatusCommand command = request == null ? new ApiKeyStatusCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateApiKeyStatus(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/app/key")
    public FrontendResponse<AppKeyInfo> createAppKey(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppKeyCreateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppKeyCreateCommand command = request == null ? new AppKeyCreateCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.createAppKey(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/app/key/list")
    public FrontendResponse<List<AppKeyInfo>> listAppKeys(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("appId") String appId,
            @RequestParam("appType") String appType) {
        try {
            UserContext userContext = userContext(authorization);
            AppKeyListQuery query = new AppKeyListQuery(appId, appType, userContext.getUserId(), userContext.getOrgId());
            return FrontendResponse.ok(appService.listAppKeys(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/appspace/app/key")
    public FrontendResponse<Map<String, Object>> deleteAppKey(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppKeyIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppKeyDeleteCommand command = request == null ? new AppKeyDeleteCommand() : request.toDeleteCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.deleteAppKey(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/assistant")
    public FrontendResponse<AssistantCreateResult> createAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantCreateRequest request) {
        UserContext userContext = userContext(authorization);
        AssistantCreateCommand command = request.toCommand();
        command.setUserId(userContext.getUserId());
        command.setOrgId(userContext.getOrgId());
        return FrontendResponse.ok(appService.createAssistant(command));
    }

    @PutMapping("/assistant")
    public FrontendResponse<Map<String, Object>> updateAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantUpdateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantUpdateCommand command = request.toUpdateCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateAssistant(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/assistant/config")
    public FrontendResponse<Map<String, Object>> updateAssistantConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantConfigRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConfigUpdateCommand command = request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateAssistantConfig(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/appspace/app")
    public FrontendResponse<Map<String, Object>> deleteAppspaceApp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppspaceAppDeleteRequest request) {
        try {
            if (request == null || !AGENT_APP_TYPE.equals(request.getAppType())) {
                return FrontendResponse.failure(1001, "unsupported app type");
            }
            return deleteAssistant(userContext(authorization), request.getAppId());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/assistant")
    public FrontendResponse<Map<String, Object>> deleteAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantDeleteRequest request) {
        try {
            return deleteAssistant(userContext(authorization), request == null ? null : request.getAssistantId());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/assistant/copy")
    public FrontendResponse<AssistantCreateResult> copyAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantCopyRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantCopyCommand command = new AssistantCopyCommand();
            command.setAssistantId(request == null ? null : request.getAssistantId());
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.copyAssistant(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/app/publish")
    public FrontendResponse<Map<String, Object>> publishApp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppPublishRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppPublishCommand command = request == null ? new AppPublishCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.publishApp(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/appspace/app/publish")
    public FrontendResponse<Map<String, Object>> unpublishApp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppPublishRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppPublishCommand command = request == null ? new AppPublishCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.unpublishApp(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/app/version")
    public FrontendResponse<AppVersionInfo> getLatestAppVersion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("appId") String appId,
            @RequestParam("appType") String appType) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.getLatestAppVersion(
                    new AppVersionQuery(appId, appType, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/app/version/list")
    public FrontendResponse<AppVersionListResult> listAppVersions(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("appId") String appId,
            @RequestParam("appType") String appType) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.listAppVersions(
                    new AppVersionQuery(appId, appType, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/appspace/app/version")
    public FrontendResponse<Map<String, Object>> updateAppVersion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppVersionUpdateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppVersionUpdateCommand command = request == null ? new AppVersionUpdateCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateAppVersion(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/app/version/rollback")
    public FrontendResponse<Map<String, Object>> rollbackAppVersion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppVersionRollbackRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppVersionRollbackCommand command = request == null ? new AppVersionRollbackCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.rollbackAppVersion(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant")
    public FrontendResponse<Map<String, Object>> assistantPublished(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("assistantId") String assistantId,
            @RequestParam(value = "version", required = false) String version) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.getPublishedAssistant(
                    new AssistantPublishedQuery(assistantId, version, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant/draft")
    public FrontendResponse<Map<String, Object>> assistantDraft(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("assistantId") String assistantId) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.getAssistantDraft(
                    new AssistantDetailQuery(assistantId, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/assistant/conversation")
    public FrontendResponse<AssistantConversationCreateResult> createAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationCreateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationCreateCommand command = request == null
                    ? new AssistantConversationCreateCommand()
                    : request.toCommand(CONVERSATION_TYPE_PUBLISHED);
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.createAssistantConversation(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/assistant/conversation")
    public FrontendResponse<Map<String, Object>> deleteAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationDeleteRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationDeleteCommand command = request == null
                    ? new AssistantConversationDeleteCommand()
                    : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.deleteAssistantConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/assistant/conversation/clear")
    public FrontendResponse<Map<String, Object>> clearAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationDeleteRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationDeleteCommand command = request == null
                    ? new AssistantConversationDeleteCommand()
                    : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.clearAssistantConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant/conversation/list")
    public FrontendResponse<AssistantConversationPageResult> listAssistantConversations(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("assistantId") String assistantId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationListQuery query = new AssistantConversationListQuery();
            query.setAssistantId(assistantId);
            query.setConversationType(CONVERSATION_TYPE_PUBLISHED);
            query.setPageNo(pageNo);
            query.setPageSize(pageSize);
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.listAssistantConversations(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant/conversation/detail")
    public FrontendResponse<AssistantConversationPageResult> listAssistantConversationDetails(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("conversationId") String conversationId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationDetailQuery query = new AssistantConversationDetailQuery();
            query.setConversationId(conversationId);
            query.setPageNo(pageNo);
            query.setPageSize(pageSize);
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.listAssistantConversationDetails(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant/conversation/draft/detail")
    public FrontendResponse<AssistantConversationPageResult> listDraftAssistantConversationDetails(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("assistantId") String assistantId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationListQuery query = new AssistantConversationListQuery();
            query.setAssistantId(assistantId);
            query.setConversationType(CONVERSATION_TYPE_DRAFT);
            query.setPageNo(pageNo);
            query.setPageSize(pageSize);
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.listDraftAssistantConversationDetails(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/assistant/conversation/draft")
    public FrontendResponse<Map<String, Object>> deleteDraftAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationDeleteRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationDeleteCommand command = request == null
                    ? new AssistantConversationDeleteCommand()
                    : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.deleteDraftAssistantConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping(value = "/assistant/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> assistantStream(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationStreamRequest request) {
        return streamAssistantConversation(authorization, request, false);
    }

    @PostMapping(value = {"/assistant/stream/draft", "/assistant/test/stream"},
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> assistantDraftStream(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationStreamRequest request) {
        return streamAssistantConversation(authorization, request, true);
    }

    @PostMapping(value = "/assistant/question/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> assistantQuestionRecommend(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody QuestionRecommendRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            QuestionRecommendRequest effectiveRequest = request == null
                    ? new QuestionRecommendRequest()
                    : request;
            if (isBlank(effectiveRequest.getAssistantId())) {
                throw new IllegalArgumentException("assistantId is required");
            }
            if (isBlank(effectiveRequest.getQuery())) {
                throw new IllegalArgumentException("query is required");
            }
            Map<String, Object> assistantInfo = resolveRecommendAssistant(userContext, effectiveRequest);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(toRecommendSseFrames(effectiveRequest, assistantInfo));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        }
    }

    @GetMapping("/appspace/app/url")
    public FrontendResponse<String> appOpenUrl() {
        return FrontendResponse.ok(OPENURL_PUBLIC_PREFIX);
    }

    @PostMapping("/appspace/app/openurl")
    public FrontendResponse<Map<String, Object>> createAppUrl(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppUrlCreateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppUrlCreateCommand command = request == null ? new AppUrlCreateCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.createAppUrl(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/appspace/app/openurl")
    public FrontendResponse<Map<String, Object>> updateAppUrl(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppUrlUpdateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppUrlUpdateCommand command = request == null ? new AppUrlUpdateCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateAppUrl(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/appspace/app/openurl")
    public FrontendResponse<Map<String, Object>> deleteAppUrl(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppUrlIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppUrlDeleteCommand command = request == null ? new AppUrlDeleteCommand() : request.toDeleteCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.deleteAppUrl(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/app/openurl/list")
    public FrontendResponse<List<AppUrlInfo>> listAppUrls(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("appId") String appId,
            @RequestParam("appType") String appType) {
        try {
            UserContext userContext = userContext(authorization);
            AppUrlListQuery query = new AppUrlListQuery();
            query.setAppId(appId);
            query.setAppType(appType);
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(toFrontendAppUrlList(appService.listAppUrls(query)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/appspace/app/openurl/status")
    public FrontendResponse<Map<String, Object>> updateAppUrlStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppUrlStatusRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppUrlStatusCommand command = request == null ? new AppUrlStatusCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateAppUrlStatus(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping({
            "/prompt/custom/list",
            "/prompt/template/list",
            "/mcp/select",
            "/workflow/select",
            "/safe/sensitive/table/select",
            "/tool/select",
            "/agent/skill/select",
    })
    public FrontendResponse<Map<String, Object>> emptySelectResult() {
        return FrontendResponse.ok(emptyListResult());
    }

    @PostMapping("/knowledge/select")
    public FrontendResponse<Map<String, Object>> selectKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.selectKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge")
    public FrontendResponse<Map<String, Object>> createKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.createKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PutMapping("/knowledge")
    public FrontendResponse<Map<String, Object>> updateKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge")
    public FrontendResponse<Map<String, Object>> deleteKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping({"/knowledge/hit", "/knowledge/qa/hit"})
    public FrontendResponse<Map<String, Object>> hitKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.hitKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/tag")
    public FrontendResponse<Map<String, Object>> listKnowledgeTags(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listTags(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/tag")
    public FrontendResponse<Map<String, Object>> createKnowledgeTag(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.createTag(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PutMapping("/knowledge/tag")
    public FrontendResponse<Map<String, Object>> updateKnowledgeTag(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateTag(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/tag")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeTag(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteTag(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/tag/bind/count")
    public FrontendResponse<Map<String, Object>> countKnowledgeTagBindings(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.countTagBindings(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/tag/bind")
    public FrontendResponse<Map<String, Object>> bindKnowledgeTags(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.bindTags(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/splitter")
    public FrontendResponse<Map<String, Object>> listKnowledgeSplitters(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listSplitters(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/splitter")
    public FrontendResponse<Map<String, Object>> createKnowledgeSplitter(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.createSplitter(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PutMapping("/knowledge/splitter")
    public FrontendResponse<Map<String, Object>> updateKnowledgeSplitter(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateSplitter(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/splitter")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeSplitter(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteSplitter(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/list")
    public FrontendResponse<Map<String, Object>> listKnowledgeDocs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listDocs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/doc/config")
    public FrontendResponse<Map<String, Object>> getKnowledgeDocConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.getDocConfig(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/doc/import/tip")
    public FrontendResponse<Map<String, Object>> getKnowledgeDocImportTip(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.getDocImportTip(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/doc/upload/limit")
    public FrontendResponse<Map<String, Object>> getKnowledgeDocUploadLimit(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.getDocUploadLimit(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/doc/segment/list")
    public FrontendResponse<Map<String, Object>> listKnowledgeDocSegments(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listDocSegments(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/doc/segment/child/list")
    public FrontendResponse<Map<String, Object>> listKnowledgeDocChildSegments(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listDocChildSegments(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/url/analysis")
    public FrontendResponse<Map<String, Object>> analyzeKnowledgeDocUrls(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.analyzeDocUrls(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/import")
    public FrontendResponse<Map<String, Object>> importKnowledgeDocs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.importDocs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/update/config")
    public FrontendResponse<Map<String, Object>> updateKnowledgeDocConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateDocConfig(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/reimport")
    public FrontendResponse<Map<String, Object>> reimportKnowledgeDocs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.reimportDocs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/doc")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeDocs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteDocs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/export")
    public FrontendResponse<Map<String, Object>> exportKnowledgeDocs() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordCreated", false);
        return FrontendResponse.ok(result);
    }

    @PostMapping({"/knowledge/doc/meta", "/knowledge/meta/value/update"})
    public FrontendResponse<Map<String, Object>> updateKnowledgeDocMeta(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateDocMeta(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/meta/batch")
    public FrontendResponse<Map<String, Object>> batchUpdateKnowledgeDocMeta(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.batchUpdateDocMeta(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/meta/select")
    public FrontendResponse<Map<String, Object>> selectKnowledgeMetaKeys(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.selectMetaKeys(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/meta/value/list")
    public FrontendResponse<Map<String, Object>> listKnowledgeMetaValues(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listMetaValues(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping({
            "/knowledge/doc/segment/status/update",
            "/knowledge/doc/segment/labels",
            "/knowledge/doc/segment/create",
            "/knowledge/doc/segment/batch/create",
            "/knowledge/doc/segment/update",
            "/knowledge/doc/segment/child/create",
            "/knowledge/doc/segment/child/update"
    })
    public FrontendResponse<Map<String, Object>> mutateKnowledgeSegments(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request, (ctx, body) -> {
        });
    }

    @DeleteMapping({"/knowledge/doc/segment/delete", "/knowledge/doc/segment/child/delete"})
    public FrontendResponse<Map<String, Object>> deleteKnowledgeSegments(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request, (ctx, body) -> {
        });
    }

    @GetMapping("/knowledge/graph")
    public FrontendResponse<Map<String, Object>> getKnowledgeGraph(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.getKnowledgeGraph(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/org")
    public FrontendResponse<Map<String, Object>> listKnowledgeOrgs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listKnowledgeOrgs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/user")
    public FrontendResponse<Map<String, Object>> listKnowledgeUsers(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listKnowledgeUsers(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/user/no/permit")
    public FrontendResponse<Map<String, Object>> listKnowledgeUsersWithoutPermit(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listUsersWithoutPermit(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/user/add")
    public FrontendResponse<Map<String, Object>> addKnowledgeUsers(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.addKnowledgeUsers(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/user/edit")
    public FrontendResponse<Map<String, Object>> editKnowledgeUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.editKnowledgeUser(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/user/delete")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteKnowledgeUser(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/user/admin/transfer")
    public FrontendResponse<Map<String, Object>> transferKnowledgeAdmin(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.transferKnowledgeAdmin(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/report/list")
    public FrontendResponse<Map<String, Object>> listKnowledgeReports(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listReports(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping({"/knowledge/report/generate", "/knowledge/report/update", "/knowledge/report/add", "/knowledge/report/batch/add"})
    public FrontendResponse<Map<String, Object>> mutateKnowledgeReports(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request, (ctx, body) -> {
        });
    }

    @DeleteMapping("/knowledge/report/delete")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeReport(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request, (ctx, body) -> {
        });
    }

    @GetMapping("/knowledge/export/record/list")
    public FrontendResponse<Map<String, Object>> listKnowledgeExportRecords(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listExportRecords(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/export/record")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeExportRecord(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteExportRecord(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/doc/by/name")
    public FrontendResponse<Map<String, Object>> getKnowledgeDocByName(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.getDocByName(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping({"/knowledge/qa/pair", "/knowledge/qa/pair/import"})
    public FrontendResponse<Map<String, Object>> mutateQaPairPost() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PutMapping({"/knowledge/qa/pair", "/knowledge/qa/pair/switch"})
    public FrontendResponse<Map<String, Object>> mutateQaPairPut() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @DeleteMapping("/knowledge/qa/pair")
    public FrontendResponse<Map<String, Object>> deleteQaPair() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/knowledge/qa/pair/list")
    public FrontendResponse<Map<String, Object>> listQaPairs(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> safe = objectMap(request);
        Map<String, Object> result = emptyListResult();
        result.put("pageNo", intParam(safe.get("pageNo"), 1));
        result.put("pageSize", intParam(safe.get("pageSize"), 10));
        return FrontendResponse.ok(result);
    }

    @GetMapping("/knowledge/qa/pair/import/tip")
    public FrontendResponse<Map<String, Object>> qaImportTip(@RequestParam Map<String, String> request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("msg", "");
        result.put("uploadstatus", 2);
        result.put("knowledgeId", request.get("knowledgeId"));
        return FrontendResponse.ok(result);
    }

    @GetMapping("/knowledge/qa/export")
    public FrontendResponse<Map<String, Object>> exportQaPairs() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping("/knowledge/external/api/select")
    public FrontendResponse<Map<String, Object>> listExternalKnowledgeApis() {
        return FrontendResponse.ok(singleton("externalApiList", Collections.emptyList()));
    }

    @GetMapping("/knowledge/external/select")
    public FrontendResponse<Map<String, Object>> listExternalKnowledge() {
        return FrontendResponse.ok(singleton("externalKnowledgeList", Collections.emptyList()));
    }

    @PostMapping("/knowledge/external/api")
    public FrontendResponse<Map<String, Object>> createExternalKnowledgeApi() {
        return FrontendResponse.ok(singleton("externalApiId", "external-api-dev"));
    }

    @PostMapping("/knowledge/external")
    public FrontendResponse<Map<String, Object>> createExternalKnowledge() {
        return FrontendResponse.ok(singleton("knowledgeId", "external-knowledge-dev"));
    }

    @PutMapping({"/knowledge/external/api", "/knowledge/external"})
    public FrontendResponse<Map<String, Object>> updateExternalKnowledge() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @DeleteMapping({"/knowledge/external/api", "/knowledge/external"})
    public FrontendResponse<Map<String, Object>> deleteExternalKnowledge() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    private FrontendResponse<Map<String, Object>> knowledgeResponse(
            String authorization, Map<?, ?> request, KnowledgeCall call) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(call.execute(userContext, objectMap(request)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private FrontendResponse<Map<String, Object>> knowledgeVoidResponse(
            String authorization, Map<?, ?> request, KnowledgeVoidCall call) {
        try {
            UserContext userContext = userContext(authorization);
            call.execute(userContext, objectMap(request));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private Map<String, Object> objectMap(Map<?, ?> request) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (request == null) {
            return result;
        }
        for (Map.Entry<?, ?> entry : request.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    private int intParam(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private interface KnowledgeCall {
        Map<String, Object> execute(UserContext userContext, Map<String, Object> request);
    }

    private interface KnowledgeVoidCall {
        void execute(UserContext userContext, Map<String, Object> request);
    }

    private String extractToken(String authorization) {
        if (authorization == null) {
            return "";
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return authorization;
    }

    private UserContext userContext(String authorization) {
        String token = extractToken(authorization);
        if (token == null || token.isEmpty()) {
            return new UserContext(DEV_USER_ID, DEV_ORG_ID);
        }
        return new UserContext(DEV_USER_ID, DEV_ORG_ID);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private Map<String, Object> emptyListResult() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", Collections.emptyList());
        result.put("total", 0);
        return result;
    }

    private ResponseEntity<String> streamAssistantConversation(String authorization,
                                                               ConversationStreamRequest request,
                                                               boolean draft) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationStreamCommand command = request == null
                    ? new AssistantConversationStreamCommand()
                    : request.toCommand(draft);
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            AssistantConversationStreamResult result = appService.streamAssistantConversation(command);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(toSseFrame(result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        }
    }

    private Map<String, Object> resolveRecommendAssistant(UserContext userContext,
                                                          QuestionRecommendRequest request) {
        if (request.isTrial()) {
            return appService.getAssistantDraft(
                    new AssistantDetailQuery(request.getAssistantId(), userContext.getUserId(), userContext.getOrgId()));
        }
        return appService.getPublishedAssistant(
                new AssistantPublishedQuery(request.getAssistantId(), null, userContext.getUserId(), userContext.getOrgId()));
    }

    private String toRecommendSseFrames(QuestionRecommendRequest request, Map<String, Object> assistantInfo) {
        String id = "recommend-" + compactText(request.getAssistantId(), 48);
        String content = recommendContent(request.getQuery(), mapString(assistantInfo, "name"));
        return "data: " + recommendChunk(id, content, "", "answer") + "\n\n"
                + "data: " + recommendChunk(id, "", "stop", "answer") + "\n\n";
    }

    private String recommendContent(String query, String assistantName) {
        String topic = compactText(query, 80);
        String name = compactText(assistantName, 40);
        String subject = isBlank(name) ? "this assistant" : name;
        return "Can " + subject + " explain " + topic + " in more detail?\n"
                + "What is the next step for " + topic + "?\n"
                + "Can you give a practical example about " + topic + "?";
    }

    private String recommendChunk(String id, String content, String finishReason, String contentType) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":\"").append(jsonEscape(id)).append("\",");
        json.append("\"object\":\"chat.completion.chunk\",");
        json.append("\"created\":0,");
        json.append("\"model\":\"local-recommend\",");
        json.append("\"choices\":[{");
        json.append("\"index\":0,");
        json.append("\"delta\":{\"role\":\"assistant\",\"content\":\"").append(jsonEscape(content)).append("\"},");
        json.append("\"finish_reason\":\"").append(jsonEscape(finishReason)).append("\",");
        json.append("\"logprobs\":null,");
        json.append("\"contentType\":\"").append(jsonEscape(contentType)).append("\"");
        json.append("}],");
        json.append("\"usage\":{\"prompt_tokens\":0,\"completion_tokens\":0,\"total_tokens\":0}");
        json.append("}");
        return json.toString();
    }

    private String modelExperienceSseFrames(String sessionId, String model, String answer) {
        String id = "model-experience-" + compactText(sessionId, 48);
        String modelName = defaultIfBlank(model, "local-model");
        return "data: " + modelExperienceChunk(id, modelName, answer, "") + "\n\n"
                + "data: " + modelExperienceChunk(id, modelName, "", "stop") + "\n\n";
    }

    private String modelExperienceChunk(String id, String model, String content, String finishReason) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":\"").append(jsonEscape(id)).append("\",");
        json.append("\"object\":\"chat.completion.chunk\",");
        json.append("\"created\":0,");
        json.append("\"model\":\"").append(jsonEscape(model)).append("\",");
        json.append("\"choices\":[{");
        json.append("\"index\":0,");
        json.append("\"delta\":{\"role\":\"assistant\",\"content\":\"").append(jsonEscape(content)).append("\"},");
        json.append("\"finish_reason\":\"").append(jsonEscape(finishReason)).append("\",");
        json.append("\"logprobs\":null");
        json.append("}],");
        json.append("\"usage\":{\"prompt_tokens\":0,\"completion_tokens\":0,\"total_tokens\":0}");
        json.append("}");
        return json.toString();
    }

    private static String jsonString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return String.valueOf(value);
        }
        try {
            return JSON.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("modelSetting serialization failed");
        }
    }

    private String mapString(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String compactText(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String compacted = value.trim().replaceAll("\\s+", " ");
        if (compacted.length() <= maxLength) {
            return compacted;
        }
        return compacted.substring(0, maxLength);
    }

    private List<AppUrlInfo> toFrontendAppUrlList(List<AppUrlInfo> source) {
        List<AppUrlInfo> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (AppUrlInfo info : source) {
            AppUrlInfo copy = copyAppUrlInfo(info);
            copy.setSuffix(openUrlPath(info == null ? "" : info.getSuffix()));
            result.add(copy);
        }
        return result;
    }

    private AppUrlInfo copyAppUrlInfo(AppUrlInfo source) {
        AppUrlInfo copy = new AppUrlInfo();
        if (source == null) {
            return copy;
        }
        copy.setUrlId(source.getUrlId());
        copy.setAppId(source.getAppId());
        copy.setAppType(source.getAppType());
        copy.setName(source.getName());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setExpiredAt(source.getExpiredAt());
        copy.setCopyright(source.getCopyright());
        copy.setCopyrightEnable(source.isCopyrightEnable());
        copy.setPrivacyPolicy(source.getPrivacyPolicy());
        copy.setPrivacyPolicyEnable(source.isPrivacyPolicyEnable());
        copy.setDisclaimer(source.getDisclaimer());
        copy.setDisclaimerEnable(source.isDisclaimerEnable());
        copy.setSuffix(source.getSuffix());
        copy.setStatus(source.isStatus());
        copy.setUserId(source.getUserId());
        copy.setOrgId(source.getOrgId());
        copy.setDescription(source.getDescription());
        return copy;
    }

    private String openUrlPath(String suffix) {
        String safeSuffix = defaultIfBlank(suffix, "");
        if (safeSuffix.startsWith("http://")
                || safeSuffix.startsWith("https://")
                || safeSuffix.startsWith(OPENURL_PUBLIC_PREFIX + "/")) {
            return safeSuffix;
        }
        return OPENURL_PUBLIC_PREFIX + "/" + safeSuffix;
    }

    private String toSseFrame(AssistantConversationStreamResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"code\":0,");
        json.append("\"message\":\"\",");
        json.append("\"response\":\"").append(jsonEscape(result.getResponse())).append("\",");
        json.append("\"order\":0,");
        json.append("\"eventType\":0,");
        json.append("\"eventData\":null,");
        json.append("\"detailId\":\"").append(jsonEscape(result.getDetailId())).append("\",");
        json.append("\"conversationId\":\"").append(jsonEscape(result.getConversationId())).append("\",");
        json.append("\"finish\":1,");
        json.append("\"gen_file_url_list\":[],");
        json.append("\"search_list\":[],");
        json.append("\"responseFiles\":[]");
        json.append("}");
        return "data: " + json + "\n\n";
    }

    private String errorJson(String message) {
        return "{\"code\":1001,\"msg\":\"" + jsonEscape(message) + "\",\"data\":null}";
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(ch);
                    break;
            }
        }
        return escaped.toString();
    }

    private FrontendResponse<Map<String, Object>> deleteAssistant(UserContext userContext, String assistantId) {
        AssistantDeleteCommand command = new AssistantDeleteCommand();
        command.setAssistantId(assistantId);
        command.setUserId(userContext.getUserId());
        command.setOrgId(userContext.getOrgId());
        appService.deleteAssistant(command);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    public static class ModelExperienceDialogRequest {
        private String modelId;
        private String sessionId;
        private String title;
        private Object modelSetting;

        public ModelExperienceDialogSaveCommand toCommand() {
            ModelExperienceDialogSaveCommand command = new ModelExperienceDialogSaveCommand();
            command.setModelId(modelId);
            command.setSessionId(sessionId);
            command.setTitle(title);
            command.setModelSetting(jsonString(modelSetting));
            return command;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Object getModelSetting() {
            return modelSetting;
        }

        public void setModelSetting(Object modelSetting) {
            this.modelSetting = modelSetting;
        }
    }

    public static class ModelExperienceDialogIdRequest {
        private String modelExperienceId;

        public String getModelExperienceId() {
            return modelExperienceId;
        }

        public void setModelExperienceId(String modelExperienceId) {
            this.modelExperienceId = modelExperienceId;
        }
    }

    public static class ModelExperienceLlmRequest {
        private String modelId;
        private String sessionId;
        private String modelExperienceId;
        private String content;
        private List<String> fileIdList = new ArrayList<String>();

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getModelExperienceId() {
            return modelExperienceId;
        }

        public void setModelExperienceId(String modelExperienceId) {
            this.modelExperienceId = modelExperienceId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getFileIdList() {
            return fileIdList;
        }

        public void setFileIdList(List<String> fileIdList) {
            this.fileIdList = fileIdList == null ? new ArrayList<String>() : fileIdList;
        }
    }

    public static class ModelUpsertRequest {
        private String modelId;
        private String provider;
        private String modelType;
        private String model;
        private String displayName;
        private AvatarRequest avatar;
        private String publishDate;
        private String modelDesc;
        private String scopeType;
        private String importSource;
        private Map<String, Object> config;

        public ModelUpsertCommand toCommand() {
            ModelUpsertCommand command = new ModelUpsertCommand();
            command.setModelId(modelId);
            command.setProvider(provider);
            command.setModelType(modelType);
            command.setModel(model);
            command.setDisplayName(displayName);
            if (avatar != null) {
                command.setAvatarKey(avatar.getKey());
                command.setAvatarPath(avatar.getPath());
            }
            command.setPublishDate(publishDate);
            command.setModelDesc(modelDesc);
            command.setScopeType(scopeType);
            command.setImportSource(importSource);
            command.setConfig(config);
            return command;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModelType() {
            return modelType;
        }

        public void setModelType(String modelType) {
            this.modelType = modelType;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public AvatarRequest getAvatar() {
            return avatar;
        }

        public void setAvatar(AvatarRequest avatar) {
            this.avatar = avatar;
        }

        public String getPublishDate() {
            return publishDate;
        }

        public void setPublishDate(String publishDate) {
            this.publishDate = publishDate;
        }

        public String getModelDesc() {
            return modelDesc;
        }

        public void setModelDesc(String modelDesc) {
            this.modelDesc = modelDesc;
        }

        public String getScopeType() {
            return scopeType;
        }

        public void setScopeType(String scopeType) {
            this.scopeType = scopeType;
        }

        public String getImportSource() {
            return importSource;
        }

        public void setImportSource(String importSource) {
            this.importSource = importSource;
        }

        public Map<String, Object> getConfig() {
            return config;
        }

        public void setConfig(Map<String, Object> config) {
            this.config = config;
        }
    }

    public static class ModelIdRequest {
        private String modelId;

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }
    }

    public static class ModelStatusRequest extends ModelIdRequest {
        private Boolean isActive;

        public ModelStatusCommand toCommand() {
            return new ModelStatusCommand(null, null, getModelId(), isActive);
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean active) {
            isActive = active;
        }
    }

    public static class ApiKeyCreateRequest {
        private String name;
        private String desc;
        private String expiredAt;

        public ApiKeyCreateCommand toCommand() {
            ApiKeyCreateCommand command = new ApiKeyCreateCommand();
            command.setName(name);
            command.setDesc(desc);
            command.setExpiredAt(expiredAt);
            return command;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getExpiredAt() {
            return expiredAt;
        }

        public void setExpiredAt(String expiredAt) {
            this.expiredAt = expiredAt;
        }
    }

    public static class ApiKeyUpdateRequest {
        private String keyId;
        private String name;
        private String desc;
        private String expiredAt;

        public ApiKeyUpdateCommand toCommand() {
            ApiKeyUpdateCommand command = new ApiKeyUpdateCommand();
            command.setKeyId(keyId);
            command.setName(name);
            command.setDesc(desc);
            command.setExpiredAt(expiredAt);
            return command;
        }

        public String getKeyId() {
            return keyId;
        }

        public void setKeyId(String keyId) {
            this.keyId = keyId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getExpiredAt() {
            return expiredAt;
        }

        public void setExpiredAt(String expiredAt) {
            this.expiredAt = expiredAt;
        }
    }

    public static class ApiKeyIdRequest {
        private String keyId;

        public ApiKeyDeleteCommand toDeleteCommand() {
            ApiKeyDeleteCommand command = new ApiKeyDeleteCommand();
            command.setKeyId(keyId);
            return command;
        }

        public String getKeyId() {
            return keyId;
        }

        public void setKeyId(String keyId) {
            this.keyId = keyId;
        }
    }

    public static class ApiKeyStatusRequest extends ApiKeyIdRequest {
        private Boolean status;

        public ApiKeyStatusCommand toCommand() {
            ApiKeyStatusCommand command = new ApiKeyStatusCommand();
            command.setKeyId(getKeyId());
            command.setStatus(status);
            return command;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }
    }

    public static class AppKeyCreateRequest {
        private String appId;
        private String appType;

        public AppKeyCreateCommand toCommand() {
            AppKeyCreateCommand command = new AppKeyCreateCommand();
            command.setAppId(appId);
            command.setAppType(appType);
            return command;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }
    }

    public static class AppKeyIdRequest {
        private String apiId;

        public AppKeyDeleteCommand toDeleteCommand() {
            AppKeyDeleteCommand command = new AppKeyDeleteCommand();
            command.setApiId(apiId);
            return command;
        }

        public String getApiId() {
            return apiId;
        }

        public void setApiId(String apiId) {
            this.apiId = apiId;
        }
    }

    public static class AssistantCreateRequest {
        private String name;
        private String desc;
        private int category;
        private AvatarRequest avatar = new AvatarRequest();

        public AssistantCreateCommand toCommand() {
            AssistantCreateCommand command = new AssistantCreateCommand();
            command.setName(name);
            command.setDesc(desc);
            command.setCategory(category);
            if (avatar != null) {
                command.setAvatarKey(avatar.getKey());
                command.setAvatarPath(avatar.getPath());
            }
            return command;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getCategory() {
            return category;
        }

        public void setCategory(int category) {
            this.category = category;
        }

        public AvatarRequest getAvatar() {
            return avatar;
        }

        public void setAvatar(AvatarRequest avatar) {
            this.avatar = avatar;
        }
    }

    public static class AppUrlCreateRequest {
        private String appId;
        private String appType;
        private String name;
        private String description;
        private String expiredAt;
        private String copyright;
        private boolean copyrightEnable;
        private String privacyPolicy;
        private boolean privacyPolicyEnable;
        private String disclaimer;
        private boolean disclaimerEnable;

        public AppUrlCreateCommand toCommand() {
            AppUrlCreateCommand command = new AppUrlCreateCommand();
            command.setAppId(appId);
            command.setAppType(appType);
            command.setName(name);
            command.setDescription(description);
            command.setExpiredAt(expiredAt);
            command.setCopyright(copyright);
            command.setCopyrightEnable(copyrightEnable);
            command.setPrivacyPolicy(privacyPolicy);
            command.setPrivacyPolicyEnable(privacyPolicyEnable);
            command.setDisclaimer(disclaimer);
            command.setDisclaimerEnable(disclaimerEnable);
            return command;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getExpiredAt() {
            return expiredAt;
        }

        public void setExpiredAt(String expiredAt) {
            this.expiredAt = expiredAt;
        }

        public String getCopyright() {
            return copyright;
        }

        public void setCopyright(String copyright) {
            this.copyright = copyright;
        }

        public boolean isCopyrightEnable() {
            return copyrightEnable;
        }

        public void setCopyrightEnable(boolean copyrightEnable) {
            this.copyrightEnable = copyrightEnable;
        }

        public String getPrivacyPolicy() {
            return privacyPolicy;
        }

        public void setPrivacyPolicy(String privacyPolicy) {
            this.privacyPolicy = privacyPolicy;
        }

        public boolean isPrivacyPolicyEnable() {
            return privacyPolicyEnable;
        }

        public void setPrivacyPolicyEnable(boolean privacyPolicyEnable) {
            this.privacyPolicyEnable = privacyPolicyEnable;
        }

        public String getDisclaimer() {
            return disclaimer;
        }

        public void setDisclaimer(String disclaimer) {
            this.disclaimer = disclaimer;
        }

        public boolean isDisclaimerEnable() {
            return disclaimerEnable;
        }

        public void setDisclaimerEnable(boolean disclaimerEnable) {
            this.disclaimerEnable = disclaimerEnable;
        }
    }

    public static class AppUrlUpdateRequest {
        private String urlId;
        private String name;
        private String description;
        private String expiredAt;
        private String copyright;
        private boolean copyrightEnable;
        private String privacyPolicy;
        private boolean privacyPolicyEnable;
        private String disclaimer;
        private boolean disclaimerEnable;

        public AppUrlUpdateCommand toCommand() {
            AppUrlUpdateCommand command = new AppUrlUpdateCommand();
            command.setUrlId(urlId);
            command.setName(name);
            command.setDescription(description);
            command.setExpiredAt(expiredAt);
            command.setCopyright(copyright);
            command.setCopyrightEnable(copyrightEnable);
            command.setPrivacyPolicy(privacyPolicy);
            command.setPrivacyPolicyEnable(privacyPolicyEnable);
            command.setDisclaimer(disclaimer);
            command.setDisclaimerEnable(disclaimerEnable);
            return command;
        }

        public String getUrlId() {
            return urlId;
        }

        public void setUrlId(String urlId) {
            this.urlId = urlId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getExpiredAt() {
            return expiredAt;
        }

        public void setExpiredAt(String expiredAt) {
            this.expiredAt = expiredAt;
        }

        public String getCopyright() {
            return copyright;
        }

        public void setCopyright(String copyright) {
            this.copyright = copyright;
        }

        public boolean isCopyrightEnable() {
            return copyrightEnable;
        }

        public void setCopyrightEnable(boolean copyrightEnable) {
            this.copyrightEnable = copyrightEnable;
        }

        public String getPrivacyPolicy() {
            return privacyPolicy;
        }

        public void setPrivacyPolicy(String privacyPolicy) {
            this.privacyPolicy = privacyPolicy;
        }

        public boolean isPrivacyPolicyEnable() {
            return privacyPolicyEnable;
        }

        public void setPrivacyPolicyEnable(boolean privacyPolicyEnable) {
            this.privacyPolicyEnable = privacyPolicyEnable;
        }

        public String getDisclaimer() {
            return disclaimer;
        }

        public void setDisclaimer(String disclaimer) {
            this.disclaimer = disclaimer;
        }

        public boolean isDisclaimerEnable() {
            return disclaimerEnable;
        }

        public void setDisclaimerEnable(boolean disclaimerEnable) {
            this.disclaimerEnable = disclaimerEnable;
        }
    }

    public static class AppUrlIdRequest {
        private String urlId;

        public AppUrlDeleteCommand toDeleteCommand() {
            AppUrlDeleteCommand command = new AppUrlDeleteCommand();
            command.setUrlId(urlId);
            return command;
        }

        public String getUrlId() {
            return urlId;
        }

        public void setUrlId(String urlId) {
            this.urlId = urlId;
        }
    }

    public static class AppUrlStatusRequest extends AppUrlIdRequest {
        private boolean status;

        public AppUrlStatusCommand toCommand() {
            AppUrlStatusCommand command = new AppUrlStatusCommand();
            command.setUrlId(getUrlId());
            command.setStatus(status);
            return command;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }
    }

    public static class AssistantUpdateRequest extends AssistantCreateRequest {
        private String assistantId;

        public AssistantUpdateCommand toUpdateCommand() {
            AssistantUpdateCommand command = new AssistantUpdateCommand();
            command.setAssistantId(assistantId);
            command.setName(getName());
            command.setDesc(getDesc());
            command.setCategory(getCategory());
            if (getAvatar() != null) {
                command.setAvatarKey(getAvatar().getKey());
                command.setAvatarPath(getAvatar().getPath());
            }
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }
    }

    public static class AssistantConfigRequest {
        private String assistantId;
        private String prologue;
        private String instructions;
        private Map<String, Object> memoryConfig;
        private Map<String, Object> knowledgeBaseConfig;
        private Map<String, Object> modelConfig;
        private Map<String, Object> safetyConfig;
        private Map<String, Object> visionConfig;
        private Map<String, Object> rerankConfig;
        private Map<String, Object> recommendConfig;
        private List<String> recommendQuestion;

        public AssistantConfigUpdateCommand toCommand() {
            AssistantConfigUpdateCommand command = new AssistantConfigUpdateCommand();
            command.setAssistantId(assistantId);
            command.setPrologue(prologue);
            command.setInstructions(instructions);
            command.setMemoryConfig(memoryConfig);
            command.setKnowledgeBaseConfig(knowledgeBaseConfig);
            command.setModelConfig(modelConfig);
            command.setSafetyConfig(safetyConfig);
            command.setVisionConfig(visionConfig);
            command.setRerankConfig(rerankConfig);
            command.setRecommendConfig(recommendConfig);
            command.setRecommendQuestion(recommendQuestion);
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

        public String getPrologue() {
            return prologue;
        }

        public void setPrologue(String prologue) {
            this.prologue = prologue;
        }

        public String getInstructions() {
            return instructions;
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }

        public Map<String, Object> getMemoryConfig() {
            return memoryConfig;
        }

        public void setMemoryConfig(Map<String, Object> memoryConfig) {
            this.memoryConfig = memoryConfig;
        }

        public Map<String, Object> getKnowledgeBaseConfig() {
            return knowledgeBaseConfig;
        }

        public void setKnowledgeBaseConfig(Map<String, Object> knowledgeBaseConfig) {
            this.knowledgeBaseConfig = knowledgeBaseConfig;
        }

        public Map<String, Object> getModelConfig() {
            return modelConfig;
        }

        public void setModelConfig(Map<String, Object> modelConfig) {
            this.modelConfig = modelConfig;
        }

        public Map<String, Object> getSafetyConfig() {
            return safetyConfig;
        }

        public void setSafetyConfig(Map<String, Object> safetyConfig) {
            this.safetyConfig = safetyConfig;
        }

        public Map<String, Object> getVisionConfig() {
            return visionConfig;
        }

        public void setVisionConfig(Map<String, Object> visionConfig) {
            this.visionConfig = visionConfig;
        }

        public Map<String, Object> getRerankConfig() {
            return rerankConfig;
        }

        public void setRerankConfig(Map<String, Object> rerankConfig) {
            this.rerankConfig = rerankConfig;
        }

        public Map<String, Object> getRecommendConfig() {
            return recommendConfig;
        }

        public void setRecommendConfig(Map<String, Object> recommendConfig) {
            this.recommendConfig = recommendConfig;
        }

        public List<String> getRecommendQuestion() {
            return recommendQuestion;
        }

        public void setRecommendQuestion(List<String> recommendQuestion) {
            this.recommendQuestion = recommendQuestion;
        }
    }

    public static class ConversationCreateRequest {
        private String assistantId;
        private String prompt;

        public AssistantConversationCreateCommand toCommand(String conversationType) {
            AssistantConversationCreateCommand command = new AssistantConversationCreateCommand();
            command.setAssistantId(assistantId);
            command.setPrompt(prompt);
            command.setConversationType(conversationType);
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    public static class ConversationDeleteRequest {
        private String assistantId;
        private String conversationId;
        private String detailId;

        public AssistantConversationDeleteCommand toCommand() {
            AssistantConversationDeleteCommand command = new AssistantConversationDeleteCommand();
            command.setAssistantId(assistantId);
            command.setConversationId(conversationId);
            command.setDetailId(detailId);
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public String getDetailId() {
            return detailId;
        }

        public void setDetailId(String detailId) {
            this.detailId = detailId;
        }
    }

    public static class ConversationStreamRequest {
        private String assistantId;
        private String conversationId;
        private String prompt;
        private String systemPrompt;
        private List<Map<String, Object>> fileInfo;

        public AssistantConversationStreamCommand toCommand(boolean draft) {
            AssistantConversationStreamCommand command = new AssistantConversationStreamCommand();
            command.setAssistantId(assistantId);
            command.setConversationId(conversationId);
            command.setPrompt(prompt);
            command.setSystemPrompt(systemPrompt);
            command.setFileInfo(fileInfo == null ? Collections.<Map<String, Object>>emptyList() : fileInfo);
            command.setDraft(draft);
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }

        public List<Map<String, Object>> getFileInfo() {
            return fileInfo;
        }

        public void setFileInfo(List<Map<String, Object>> fileInfo) {
            this.fileInfo = fileInfo;
        }
    }

    public static class QuestionRecommendRequest {
        private String query;
        private String assistantId;
        private String conversationId;
        private boolean trial;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public boolean isTrial() {
            return trial;
        }

        public void setTrial(boolean trial) {
            this.trial = trial;
        }
    }

    public static class AppspaceAppDeleteRequest {
        private String appId;
        private String appType;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }
    }

    public static class AssistantDeleteRequest {
        private String assistantId;

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }
    }

    public static class AssistantCopyRequest {
        private String assistantId;

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }
    }

    public static class AppPublishRequest {
        private String appId;
        private String appType;
        private String version;
        private String desc;
        private String publishType;

        public AppPublishCommand toCommand() {
            AppPublishCommand command = new AppPublishCommand();
            command.setAppId(appId);
            command.setAppType(appType);
            command.setVersion(version);
            command.setDesc(desc);
            command.setPublishType(publishType);
            return command;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getPublishType() {
            return publishType;
        }

        public void setPublishType(String publishType) {
            this.publishType = publishType;
        }
    }

    public static class AppVersionUpdateRequest {
        private String appId;
        private String appType;
        private String desc;
        private String publishType;

        public AppVersionUpdateCommand toCommand() {
            AppVersionUpdateCommand command = new AppVersionUpdateCommand();
            command.setAppId(appId);
            command.setAppType(appType);
            command.setDesc(desc);
            command.setPublishType(publishType);
            return command;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getPublishType() {
            return publishType;
        }

        public void setPublishType(String publishType) {
            this.publishType = publishType;
        }
    }

    public static class AppVersionRollbackRequest {
        private String appId;
        private String appType;
        private String version;

        public AppVersionRollbackCommand toCommand() {
            AppVersionRollbackCommand command = new AppVersionRollbackCommand();
            command.setAppId(appId);
            command.setAppType(appType);
            command.setVersion(version);
            return command;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class AvatarRequest {
        private String key;
        private String path;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    private static class UserContext {
        private final String userId;
        private final String orgId;

        private UserContext(String userId, String orgId) {
            this.userId = userId;
            this.orgId = orgId;
        }

        private String getUserId() {
            return userId;
        }

        private String getOrgId() {
            return orgId;
        }
    }
}
