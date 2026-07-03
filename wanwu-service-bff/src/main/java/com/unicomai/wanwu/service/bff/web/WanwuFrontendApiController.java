package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import com.unicomai.wanwu.api.app.dto.AssistantResourceCommand;
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
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationInfoQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RagCopyCommand;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RagCreateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateResult;
import com.unicomai.wanwu.api.app.dto.RagDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.RagUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
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
import com.unicomai.wanwu.api.iam.dto.OrganizationSelectResult;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.mcp.McpService;
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
import com.unicomai.wanwu.api.operate.OperateService;
import com.unicomai.wanwu.api.safety.SafetyService;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuFrontendApiController {

    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
    private static final String AGENT_APP_TYPE = "agent";
    private static final String RAG_APP_TYPE = "rag";
    private static final String WORKFLOW_APP_TYPE = "workflow";
    private static final String CHATFLOW_APP_TYPE = "chatflow";
    private static final String STAT_SOURCE_WEB = "web";
    private static final String CONVERSATION_TYPE_PUBLISHED = "published";
    private static final String CONVERSATION_TYPE_DRAFT = "draft";
    private static final String OPENURL_PUBLIC_PREFIX = "/service/url/openurl/v1/agent";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int MODEL_PROXY_CONNECT_TIMEOUT_MILLIS = 3000;
    private static final int MODEL_PROXY_READ_TIMEOUT_MILLIS = 10000;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private KnowledgeService knowledgeService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private McpService mcpService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private OperateService operateService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private SafetyService safetyService;

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
        this(iamService, appService, modelService, knowledgeService, null);
    }

    public WanwuFrontendApiController(IamService iamService, AppService appService, ModelService modelService,
                                      KnowledgeService knowledgeService, McpService mcpService) {
        this(iamService, appService, modelService, knowledgeService, mcpService, null);
    }

    public WanwuFrontendApiController(IamService iamService, AppService appService, ModelService modelService,
                                      KnowledgeService knowledgeService, McpService mcpService,
                                      OperateService operateService) {
        this(iamService, appService, modelService, knowledgeService, mcpService, operateService, null);
    }

    public WanwuFrontendApiController(IamService iamService, AppService appService, ModelService modelService,
                                      KnowledgeService knowledgeService, McpService mcpService,
                                      OperateService operateService, SafetyService safetyService) {
        this.iamService = iamService;
        this.appService = appService;
        this.modelService = modelService;
        this.knowledgeService = knowledgeService;
        this.mcpService = mcpService;
        this.operateService = operateService;
        this.safetyService = safetyService;
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

    @PostMapping("/user")
    public FrontendResponse<Map<String, Object>> createUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.createUser(userContext.getUserId(), userContext.getOrgId(), request));
    }

    @PostMapping("/user/batch")
    public FrontendResponse<Map<String, Object>> createUserByFile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        UserContext userContext = userContext(authorization);
        String fileName = file == null ? "" : defaultIfBlank(file.getOriginalFilename(), "");
        long fileSize = file == null ? 0L : file.getSize();
        return FrontendResponse.ok(iamService.importUsers(userContext.getUserId(), userContext.getOrgId(), fileName, fileSize));
    }

    @PutMapping("/user")
    public FrontendResponse<Map<String, Object>> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.updateUser(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @DeleteMapping("/user")
    public FrontendResponse<Map<String, Object>> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.deleteUser(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PutMapping("/user/status")
    public FrontendResponse<Map<String, Object>> updateUserStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.updateUserStatus(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping("/org/other/select")
    public FrontendResponse<Map<String, Object>> listUsersOutsideOrg(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.listUsersOutsideOrg(userContext.getOrgId(), defaultIfBlank(name, ""), pageNo, pageSize));
    }

    @PostMapping("/org/user")
    public FrontendResponse<Map<String, Object>> addOrgUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.addOrgUser(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
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

    @PostMapping("/role")
    public FrontendResponse<Map<String, Object>> createRole(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.createRole(userContext.getUserId(), userContext.getOrgId(), request));
    }

    @PutMapping("/role")
    public FrontendResponse<Map<String, Object>> updateRole(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.updateRole(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @DeleteMapping("/role")
    public FrontendResponse<Map<String, Object>> deleteRole(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.deleteRole(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PutMapping("/role/status")
    public FrontendResponse<Map<String, Object>> updateRoleStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.updateRoleStatus(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
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

    @PostMapping("/org")
    public FrontendResponse<Map<String, Object>> createOrganization(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(iamService.createOrganization(userContext.getUserId(), userContext.getOrgId(), request));
    }

    @PutMapping("/org")
    public FrontendResponse<Map<String, Object>> updateOrganization(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.updateOrganization(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @DeleteMapping("/org")
    public FrontendResponse<Map<String, Object>> deleteOrganization(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.deleteOrganization(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PutMapping("/org/status")
    public FrontendResponse<Map<String, Object>> updateOrganizationStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext userContext = userContext(authorization);
        iamService.updateOrganizationStatus(userContext.getUserId(), userContext.getOrgId(), request);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping("/base/custom")
    public FrontendResponse<Map<String, Object>> platformConfig() {
        if (operateService != null) {
            return FrontendResponse.ok(operateService.getSystemCustom("default"));
        }
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
        long startedAt = System.currentTimeMillis();
        try {
            UserContext userContext = userContext(authorization);
            ModelExperienceLlmRequest safe = request == null ? new ModelExperienceLlmRequest() : request;
            ModelInfo model = modelService.getModel(userContext.getUserId(), userContext.getOrgId(), safe.getModelId());
            if (!Boolean.TRUE.equals(model.getIsActive())) {
                return ResponseEntity.status(400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson("model is inactive"));
            }
            String sensitiveReply = matchGlobalSensitiveReply(userContext, safe.getContent());
            if (!isBlank(sensitiveReply)) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(modelExperienceSseFrames(safe.getSessionId(), model.getModel(), sensitiveReply));
            }
            String answer = firstNonBlank(modelExperienceUpstreamAnswer(model, safe),
                    "Echo: " + defaultIfBlank(safe.getContent(), ""));
            String outputSensitiveReply = matchGlobalSensitiveReply(userContext, answer);
            if (!isBlank(outputSensitiveReply)) {
                answer = outputSensitiveReply;
            }
            modelService.saveModelExperienceDialogRecord(new ModelExperienceDialogRecordSaveCommand(
                    userContext.getUserId(), userContext.getOrgId(), safe.getModelExperienceId(), safe.getModelId(),
                    safe.getSessionId(), defaultIfBlank(safe.getContent(), ""), "", "", "user"));
            modelService.saveModelExperienceDialogRecord(new ModelExperienceDialogRecordSaveCommand(
                    userContext.getUserId(), userContext.getOrgId(), safe.getModelExperienceId(), safe.getModelId(),
                    safe.getSessionId(), answer, "", "", "assistant"));
            recordModelStatistic(userContext, model, safe, answer, startedAt);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(modelExperienceSseFrames(safe.getSessionId(), model.getModel(), answer));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        }
    }

    @GetMapping({"/appspace/assistant/list", "/assistant/list"})
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

    @GetMapping("/appspace/rag/list")
    public FrontendResponse<ApplicationListResult> ragList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.listApplications(
                    new ApplicationListQuery(RAG_APP_TYPE, name, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/rag")
    public FrontendResponse<RagCreateResult> createRag(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody RagCreateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            RagCreateCommand command = request == null ? new RagCreateRequest().toCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.createRag(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/appspace/rag")
    public FrontendResponse<Map<String, Object>> updateRag(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody RagUpdateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            RagUpdateCommand command = request == null ? new RagUpdateRequest().toCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateRag(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/appspace/rag/config")
    public FrontendResponse<Map<String, Object>> updateRagConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody RagConfigRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            RagConfigUpdateCommand command = request == null ? new RagConfigRequest().toCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateRagConfig(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/appspace/rag")
    public FrontendResponse<Map<String, Object>> deleteRag(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody RagIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            RagDeleteCommand command = new RagDeleteCommand();
            command.setRagId(request == null ? null : request.getRagId());
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.deleteRag(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/rag/draft")
    public FrontendResponse<Map<String, Object>> ragDraft(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("ragId") String ragId) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.getRagDraft(
                    new RagDetailQuery(ragId, "", userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/rag")
    public FrontendResponse<Map<String, Object>> ragPublished(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("ragId") String ragId,
            @RequestParam(value = "version", required = false) String version) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.getPublishedRag(
                    new RagDetailQuery(ragId, version, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/rag/copy")
    public FrontendResponse<RagCreateResult> copyRag(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody RagIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            RagCopyCommand command = new RagCopyCommand();
            command.setRagId(request == null ? null : request.getRagId());
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.copyRag(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/workflow/list")
    public FrontendResponse<ApplicationListResult> workflowList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "appType", required = false) String appType) {
        try {
            UserContext userContext = userContext(authorization);
            String effectiveAppType = CHATFLOW_APP_TYPE.equals(appType) ? CHATFLOW_APP_TYPE : WORKFLOW_APP_TYPE;
            return FrontendResponse.ok(appService.listApplications(
                    new ApplicationListQuery(effectiveAppType, name, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/workflow")
    public FrontendResponse<Map<String, Object>> createWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) WorkflowCreateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            WorkflowCreateCommand command = request == null ? new WorkflowCreateRequest().toCreateCommand() : request.toCreateCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(workflowCreateResult(appService.createWorkflow(command)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping({"/appspace/workflow/copy", "/appspace/workflow/copy/draft"})
    public FrontendResponse<Map<String, Object>> copyWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) WorkflowIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            WorkflowCopyCommand command = new WorkflowCopyCommand();
            command.setWorkflowId(request == null ? null : request.workflowId());
            command.setNeedPublished(request != null && request.isNeedPublished());
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(workflowCreateResult(appService.copyWorkflow(command)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/workflow/import")
    public FrontendResponse<Map<String, Object>> importWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam Map<String, String> form) {
        try {
            UserContext userContext = userContext(authorization);
            WorkflowImportRequest request = workflowImportRequest(file, form);
            WorkflowImportCommand command = request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(workflowCreateResult(appService.importWorkflow(command)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/workflow/export/draft")
    public ResponseEntity<byte[]> exportWorkflowDraft(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return exportWorkflowJson(authorization, request, false);
    }

    @GetMapping("/appspace/workflow/export")
    public ResponseEntity<byte[]> exportWorkflowPublished(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return exportWorkflowJson(authorization, request, true);
    }

    @PostMapping("/appspace/workflow/convert")
    public FrontendResponse<Map<String, Object>> convertWorkflow() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/appspace/chatflow")
    public FrontendResponse<Map<String, Object>> createChatflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) WorkflowCreateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            WorkflowCreateCommand command = request == null ? new WorkflowCreateRequest().toCreateCommand() : request.toCreateCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(workflowCreateResult(appService.createChatflow(command)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping({"/appspace/chatflow/copy", "/appspace/chatflow/copy/draft"})
    public FrontendResponse<Map<String, Object>> copyChatflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) WorkflowIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            WorkflowCopyCommand command = new WorkflowCopyCommand();
            command.setWorkflowId(request == null ? null : request.workflowId());
            command.setNeedPublished(request != null && request.isNeedPublished());
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(workflowCreateResult(appService.copyChatflow(command)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/chatflow/import")
    public FrontendResponse<Map<String, Object>> importChatflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam Map<String, String> form) {
        try {
            UserContext userContext = userContext(authorization);
            WorkflowImportRequest request = workflowImportRequest(file, form);
            WorkflowImportCommand command = request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(workflowCreateResult(appService.importChatflow(command)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/chatflow/export/draft")
    public ResponseEntity<byte[]> exportChatflowDraft(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return exportChatflowJson(authorization, request, false);
    }

    @GetMapping("/appspace/chatflow/export")
    public ResponseEntity<byte[]> exportChatflowPublished(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return exportChatflowJson(authorization, request, true);
    }

    @PostMapping("/appspace/chatflow/convert")
    public FrontendResponse<Map<String, Object>> convertChatflow() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/chatflow/application/list")
    public FrontendResponse<Map<String, Object>> listChatflowApplications(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) WorkflowIdRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ChatflowApplicationListQuery query = new ChatflowApplicationListQuery();
            query.setWorkflowId(request == null ? null : request.workflowId());
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.listChatflowApplications(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/chatflow/application/info")
    public FrontendResponse<Map<String, Object>> getChatflowApplication(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) ChatflowApplicationInfoRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ChatflowApplicationInfoQuery query = request == null
                    ? new ChatflowApplicationInfoRequest().toQuery()
                    : request.toQuery();
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.getChatflowApplication(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/chatflow/conversation/delete")
    public FrontendResponse<Map<String, Object>> deleteChatflowConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) ChatflowConversationDeleteRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            ChatflowConversationDeleteCommand command = request == null
                    ? new ChatflowConversationDeleteRequest().toCommand()
                    : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.deleteChatflowConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/workflow/model/select/{modelType}")
    public FrontendResponse<ModelListResult> selectWorkflowModels(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @org.springframework.web.bind.annotation.PathVariable("modelType") String modelType) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(modelService.listTypeModels(
                new ModelTypeQuery(userContext.getUserId(), userContext.getOrgId(), workflowModelType(modelType))));
    }

    private String workflowModelType(String modelType) {
        return "asr".equals(modelType) ? "sync-asr" : modelType;
    }

    @GetMapping("/workflow/tool/select")
    public FrontendResponse<Map<String, Object>> selectWorkflowTools(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "toolType", required = false) String toolType,
            @RequestParam(value = "name", required = false) String name) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(workflowToolSelect(
                    userContext, defaultIfBlank(toolType, ""), defaultIfBlank(name, "")));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/workflow/tool/action")
    public FrontendResponse<Map<String, Object>> getWorkflowToolAction(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        try {
            UserContext userContext = userContext(authorization);
            String toolId = firstText(request, "toolId", "tool_id", "box_id");
            String toolType = defaultIfBlank(firstText(request, "toolType", "tool_type", "box_type"), "builtin");
            String actionName = firstText(request, "actionName", "actionId", "operationId", "tool_id");
            return FrontendResponse.ok(workflowToolAction(userContext, toolId, toolType, actionName));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/workflow/tool/box")
    public FrontendResponse<Map<String, Object>> getWorkflowToolBox(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(workflowToolBox(userContext, request));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/workflow/run")
    public FrontendResponse<Map<String, Object>> runWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) WorkflowRunRequest request) {
        long startedAt = System.currentTimeMillis();
        try {
            UserContext userContext = userContext(authorization);
            WorkflowRunCommand command = request == null ? new WorkflowRunRequest().toCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            Map<String, Object> result = workflowRunResult(appService.runWorkflow(command));
            recordAppStatistic(userContext, command.getWorkflowId(), WORKFLOW_APP_TYPE, true, false, startedAt, STAT_SOURCE_WEB);
            return FrontendResponse.ok(result);
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping(value = "/rag/chat/draft", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> ragDraftChat(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody RagChatRequest request) {
        return streamRagChat(authorization, request, true);
    }

    @PostMapping(value = "/rag/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> ragPublishedChat(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody RagChatRequest request) {
        return streamRagChat(authorization, request, false);
    }

    @PostMapping("/rag/upload")
    public FrontendResponse<Map<String, Object>> ragUpload(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "markdown", required = false, defaultValue = "false") boolean markdown) {
        try {
            if (files == null || files.isEmpty()) {
                return FrontendResponse.failure(1001, "file is empty");
            }
            List<Map<String, Object>> fileList = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String fileName = originalFileName(file);
                String fileUrl = dataUrl(file);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("fileIndex", i);
                item.put("fileUrl", markdown ? markdownImage(fileName, fileUrl) : fileUrl);
                fileList.add(item);
            }
            if (fileList.isEmpty()) {
                return FrontendResponse.failure(1001, "file is empty");
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fileList", fileList);
            return FrontendResponse.ok(result);
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/appspace/app")
    public FrontendResponse<Map<String, Object>> deleteAppspaceApp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppspaceAppDeleteRequest request) {
        try {
            if (request == null) {
                return FrontendResponse.failure(1001, "unsupported app type");
            }
            if (RAG_APP_TYPE.equals(request.getAppType())) {
                RagDeleteCommand command = new RagDeleteCommand();
                command.setRagId(request.getAppId());
                UserContext userContext = userContext(authorization);
                command.setUserId(userContext.getUserId());
                command.setOrgId(userContext.getOrgId());
                appService.deleteRag(command);
                return FrontendResponse.ok(Collections.<String, Object>emptyMap());
            }
            if (WORKFLOW_APP_TYPE.equals(request.getAppType())) {
                WorkflowDeleteCommand command = new WorkflowDeleteCommand();
                command.setWorkflowId(request.getAppId());
                UserContext userContext = userContext(authorization);
                command.setUserId(userContext.getUserId());
                command.setOrgId(userContext.getOrgId());
                appService.deleteWorkflow(command);
                return FrontendResponse.ok(Collections.<String, Object>emptyMap());
            }
            if (CHATFLOW_APP_TYPE.equals(request.getAppType())) {
                WorkflowDeleteCommand command = new WorkflowDeleteCommand();
                command.setWorkflowId(request.getAppId());
                UserContext userContext = userContext(authorization);
                command.setUserId(userContext.getUserId());
                command.setOrgId(userContext.getOrgId());
                appService.deleteChatflow(command);
                return FrontendResponse.ok(Collections.<String, Object>emptyMap());
            }
            if (!AGENT_APP_TYPE.equals(request.getAppType())) {
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

    @GetMapping(value = "/asr/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> asrStream(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("modelId") String modelId) {
        UserContext userContext = userContext(authorization);
        String connected = "{\"type\":\"asr.connected\",\"modelId\":\"" + jsonEscape(modelId)
                + "\",\"userId\":\"" + jsonEscape(userContext.getUserId()) + "\"}";
        String closed = "{\"type\":\"asr.closed\",\"modelId\":\"" + jsonEscape(modelId) + "\"}";
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body("data: " + connected + "\n\n" + "data: " + closed + "\n\n");
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

    @GetMapping("/assistant/select")
    public FrontendResponse<ApplicationListResult> selectAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(appService.listAssistants(
                new ApplicationListQuery(AGENT_APP_TYPE, defaultIfBlank(name, ""),
                        userContext.getUserId(), userContext.getOrgId())));
    }

    @GetMapping("/tool/select")
    public FrontendResponse<Map<String, Object>> selectAssistantTools(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        UserContext userContext = userContext(authorization);
        if (mcpService != null) {
            Map<String, Object> result = mcpService.listToolSelect(
                    userContext.getUserId(), userContext.getOrgId(), defaultIfBlank(name, ""));
            if (result != null && !result.isEmpty()) {
                return FrontendResponse.ok(result);
            }
        }
        return FrontendResponse.ok(appService.listAssistantToolSelect(
                userContext.getUserId(), userContext.getOrgId()));
    }

    @GetMapping("/tool/action/list")
    public FrontendResponse<Map<String, Object>> listAssistantToolActions(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        UserContext userContext = userContext(authorization);
        if (mcpService != null) {
            Map<String, Object> result = mcpService.listToolActions(
                    userContext.getUserId(), userContext.getOrgId(),
                    defaultIfBlank(request.get("toolId"), ""),
                    defaultIfBlank(request.get("toolType"), "builtin"));
            if (result != null && !result.isEmpty()) {
                return FrontendResponse.ok(result);
            }
        }
        return FrontendResponse.ok(appService.listAssistantToolActions(
                resourceCommand(userContext, objectMap(request), "toolId", "toolType")));
    }

    @GetMapping("/tool/action/detail")
    public FrontendResponse<Map<String, Object>> getAssistantToolActionDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        UserContext userContext = userContext(authorization);
        if (mcpService != null) {
            Map<String, Object> result = mcpService.getToolActionDetail(
                    userContext.getUserId(), userContext.getOrgId(),
                    defaultIfBlank(request.get("toolId"), ""),
                    defaultIfBlank(request.get("toolType"), "builtin"),
                    defaultIfBlank(request.get("actionName"), ""));
            if (result != null && !result.isEmpty()) {
                return FrontendResponse.ok(result);
            }
        }
        return FrontendResponse.ok(appService.getAssistantToolActionDetail(
                resourceCommand(userContext, objectMap(request), "toolId", "toolType")));
    }

    @GetMapping("/mcp/select")
    public FrontendResponse<Map<String, Object>> selectAssistantMcps(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        UserContext userContext = userContext(authorization);
        if (mcpService != null) {
            Map<String, Object> result = mcpService.listMcpSelect(
                    userContext.getUserId(), userContext.getOrgId(), defaultIfBlank(name, ""));
            if (result != null && !result.isEmpty()) {
                return FrontendResponse.ok(result);
            }
        }
        return FrontendResponse.ok(appService.listAssistantMcpSelect(
                userContext.getUserId(), userContext.getOrgId()));
    }

    @GetMapping("/mcp/action/list")
    public FrontendResponse<Map<String, Object>> listAssistantMcpActions(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        UserContext userContext = userContext(authorization);
        if (mcpService != null) {
            Map<String, Object> result = mcpService.listMcpActions(
                    userContext.getUserId(), userContext.getOrgId(),
                    defaultIfBlank(request.get("toolId"), ""),
                    defaultIfBlank(request.get("toolType"), "mcp"));
            if (result != null && !result.isEmpty()) {
                return FrontendResponse.ok(result);
            }
        }
        return FrontendResponse.ok(appService.listAssistantMcpActions(
                resourceCommand(userContext, objectMap(request), "toolId", "toolType")));
    }

    @GetMapping("/workflow/select")
    public FrontendResponse<Map<String, Object>> selectAssistantWorkflows(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(appService.listAssistantWorkflowSelect(
                userContext.getUserId(), userContext.getOrgId(), defaultIfBlank(name, "")));
    }

    @GetMapping("/assistant/action")
    public FrontendResponse<Map<String, Object>> assistantActions() {
        return FrontendResponse.ok(emptyListResult());
    }

    @PostMapping("/assistant/action")
    public FrontendResponse<Map<String, Object>> createAssistantAction() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PutMapping({"/assistant/action", "/assistant/action/enable"})
    public FrontendResponse<Map<String, Object>> updateAssistantAction() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @DeleteMapping("/assistant/action")
    public FrontendResponse<Map<String, Object>> deleteAssistantAction() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/assistant/tool/workflow")
    public FrontendResponse<Map<String, Object>> addAssistantWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "workFlowId", null,
                command -> appService.addAssistantWorkflow(command));
    }

    @DeleteMapping("/assistant/tool/workflow")
    public FrontendResponse<Map<String, Object>> deleteAssistantWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "workFlowId", null,
                command -> appService.deleteAssistantWorkflow(command));
    }

    @PutMapping("/assistant/tool/workflow/switch")
    public FrontendResponse<Map<String, Object>> switchAssistantWorkflow(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "workFlowId", null,
                command -> appService.switchAssistantWorkflow(command));
    }

    @PostMapping("/assistant/tool/mcp")
    public FrontendResponse<Map<String, Object>> addAssistantMcp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "mcpId", "mcpType",
                command -> appService.addAssistantMcp(command));
    }

    @DeleteMapping("/assistant/tool/mcp")
    public FrontendResponse<Map<String, Object>> deleteAssistantMcp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "mcpId", "mcpType",
                command -> appService.deleteAssistantMcp(command));
    }

    @PutMapping("/assistant/tool/mcp/switch")
    public FrontendResponse<Map<String, Object>> switchAssistantMcp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "mcpId", "mcpType",
                command -> appService.switchAssistantMcp(command));
    }

    @PostMapping("/assistant/tool")
    public FrontendResponse<Map<String, Object>> addAssistantTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "toolId", "toolType",
                command -> appService.addAssistantTool(command));
    }

    @DeleteMapping("/assistant/tool")
    public FrontendResponse<Map<String, Object>> deleteAssistantTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "toolId", "toolType",
                command -> appService.deleteAssistantTool(command));
    }

    @PutMapping("/assistant/tool/switch")
    public FrontendResponse<Map<String, Object>> switchAssistantTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "toolId", "toolType",
                command -> appService.switchAssistantTool(command));
    }

    @PutMapping("/assistant/tool/config")
    public FrontendResponse<Map<String, Object>> configureAssistantTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "toolId", "toolType",
                command -> appService.configureAssistantTool(command));
    }

    @PostMapping("/assistant/skill")
    public FrontendResponse<Map<String, Object>> addAssistantSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "skillId", "skillType",
                command -> appService.addAssistantSkill(command));
    }

    @DeleteMapping("/assistant/skill")
    public FrontendResponse<Map<String, Object>> deleteAssistantSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "skillId", "skillType",
                command -> appService.deleteAssistantSkill(command));
    }

    @PutMapping("/assistant/skill/switch")
    public FrontendResponse<Map<String, Object>> switchAssistantSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "skillId", "skillType",
                command -> appService.switchAssistantSkill(command));
    }

    @PostMapping("/assistant/multi-agent")
    public FrontendResponse<Map<String, Object>> addAssistantAgent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "agentId", null,
                command -> appService.addAssistantAgent(command));
    }

    @DeleteMapping("/assistant/multi-agent")
    public FrontendResponse<Map<String, Object>> deleteAssistantAgent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "agentId", null,
                command -> appService.deleteAssistantAgent(command));
    }

    @PutMapping("/assistant/multi-agent/switch")
    public FrontendResponse<Map<String, Object>> switchAssistantAgent(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "agentId", null,
                command -> appService.switchAssistantAgent(command));
    }

    @PutMapping("/assistant/multi-agent/config")
    public FrontendResponse<Map<String, Object>> updateAssistantAgentConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return assistantVoidResponse(authorization, request, "agentId", null,
                command -> appService.updateAssistantAgentConfig(command));
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

    @PostMapping("/knowledge/hit")
    public FrontendResponse<Map<String, Object>> hitKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.hitKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/keywords")
    public FrontendResponse<Map<String, Object>> listKnowledgeKeywords(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listKeywords(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/keywords")
    public FrontendResponse<Map<String, Object>> createKnowledgeKeyword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.createKeyword(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/keywords/detail")
    public FrontendResponse<Map<String, Object>> getKnowledgeKeyword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.getKeyword(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PutMapping("/knowledge/keywords")
    public FrontendResponse<Map<String, Object>> updateKnowledgeKeyword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateKeyword(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/keywords")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeKeyword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteKeyword(ctx.getUserId(), ctx.getOrgId(), body));
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
        return knowledgeVoidResponse(authorization, enrichKnowledgeImportContent(request),
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
        return knowledgeVoidResponse(authorization, enrichKnowledgeImportContent(request),
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
    public FrontendResponse<Map<String, Object>> exportKnowledgeDocs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.exportDocs(ctx.getUserId(), ctx.getOrgId(), body));
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

    @PostMapping("/knowledge/doc/segment/status/update")
    public FrontendResponse<Map<String, Object>> updateKnowledgeDocSegmentStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateDocSegmentStatus(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/segment/labels")
    public FrontendResponse<Map<String, Object>> updateKnowledgeDocSegmentLabels(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateDocSegmentLabels(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/segment/create")
    public FrontendResponse<Map<String, Object>> createKnowledgeDocSegment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.createDocSegment(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/segment/batch/create")
    public FrontendResponse<Map<String, Object>> batchCreateKnowledgeDocSegments(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.batchCreateDocSegment(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/segment/update")
    public FrontendResponse<Map<String, Object>> updateKnowledgeDocSegment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateDocSegment(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/doc/segment/delete")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeDocSegment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteDocSegment(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/segment/child/create")
    public FrontendResponse<Map<String, Object>> createKnowledgeDocChildSegment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.createDocChildSegment(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/doc/segment/child/update")
    public FrontendResponse<Map<String, Object>> updateKnowledgeDocChildSegment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateDocChildSegment(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/doc/segment/child/delete")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeDocChildSegment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteDocChildSegment(ctx.getUserId(), ctx.getOrgId(), body));
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

    @PostMapping("/knowledge/report/generate")
    public FrontendResponse<Map<String, Object>> generateKnowledgeReport(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.generateReport(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/report/update")
    public FrontendResponse<Map<String, Object>> updateKnowledgeReport(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateReport(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/report/add")
    public FrontendResponse<Map<String, Object>> addKnowledgeReport(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.addReport(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/report/batch/add")
    public FrontendResponse<Map<String, Object>> batchAddKnowledgeReport(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, enrichKnowledgeImportContent(request),
                (ctx, body) -> knowledgeService.batchAddReports(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/report/delete")
    public FrontendResponse<Map<String, Object>> deleteKnowledgeReport(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteReport(ctx.getUserId(), ctx.getOrgId(), body));
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

    @GetMapping("/knowledge/export/file/{exportRecordId}/{fileName:.+}")
    public ResponseEntity<byte[]> downloadKnowledgeExportRecord(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("exportRecordId") String exportRecordId,
            @PathVariable("fileName") String fileName) {
        UserContext ctx = userContext(authorization);
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("exportRecordId", exportRecordId);
        request.put("fileName", fileName);
        Map<String, Object> file = knowledgeService.getExportRecordFile(ctx.getUserId(), ctx.getOrgId(), request);
        String resolvedName = defaultIfBlank(stringValue(file.get("fileName")), fileName);
        String contentType = defaultIfBlank(stringValue(file.get("contentType")), MediaType.APPLICATION_OCTET_STREAM_VALUE);
        String base64Content = stringValue(file.get("contentBase64"));
        byte[] payload = base64Content.isEmpty()
                ? stringValue(file.get("content")).getBytes(StandardCharsets.UTF_8)
                : Base64.getDecoder().decode(base64Content);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resolvedName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(payload.length)
                .body(payload);
    }

    @GetMapping("/knowledge/doc/by/name")
    public FrontendResponse<Map<String, Object>> getKnowledgeDocByName(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.getDocByName(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/qa/pair")
    public FrontendResponse<Map<String, Object>> createQaPair(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.createQaPair(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PutMapping("/knowledge/qa/pair")
    public FrontendResponse<Map<String, Object>> updateQaPair(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateQaPair(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PutMapping("/knowledge/qa/pair/switch")
    public FrontendResponse<Map<String, Object>> updateQaPairSwitch(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateQaPairSwitch(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/qa/pair")
    public FrontendResponse<Map<String, Object>> deleteQaPair(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteQaPairs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/qa/pair/list")
    public FrontendResponse<Map<String, Object>> listQaPairs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listQaPairs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/qa/pair/import/tip")
    public FrontendResponse<Map<String, Object>> qaImportTip(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.getQaImportTip(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/qa/pair/import")
    public FrontendResponse<Map<String, Object>> importQaPairs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, enrichKnowledgeImportContent(request),
                (ctx, body) -> knowledgeService.importQaPairs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/qa/export")
    public FrontendResponse<Map<String, Object>> exportQaPairs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.exportQaPairs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/qa/hit")
    public FrontendResponse<Map<String, Object>> hitQaPairs(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.hitQaPairs(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/external/api/select")
    public FrontendResponse<Map<String, Object>> listExternalKnowledgeApis(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listExternalApis(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @GetMapping("/knowledge/external/select")
    public FrontendResponse<Map<String, Object>> listExternalKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.listExternalKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/external/api")
    public FrontendResponse<Map<String, Object>> createExternalKnowledgeApi(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.createExternalApi(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PostMapping("/knowledge/external")
    public FrontendResponse<Map<String, Object>> createExternalKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeResponse(authorization, request,
                (ctx, body) -> knowledgeService.createExternalKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PutMapping("/knowledge/external/api")
    public FrontendResponse<Map<String, Object>> updateExternalKnowledgeApi(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateExternalApi(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @PutMapping("/knowledge/external")
    public FrontendResponse<Map<String, Object>> updateExternalKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.updateExternalKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/external/api")
    public FrontendResponse<Map<String, Object>> deleteExternalKnowledgeApi(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteExternalApi(ctx.getUserId(), ctx.getOrgId(), body));
    }

    @DeleteMapping("/knowledge/external")
    public FrontendResponse<Map<String, Object>> deleteExternalKnowledge(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return knowledgeVoidResponse(authorization, request,
                (ctx, body) -> knowledgeService.deleteExternalKnowledge(ctx.getUserId(), ctx.getOrgId(), body));
    }

    private FrontendResponse<Map<String, Object>> assistantVoidResponse(
            String authorization, Map<String, Object> request, String idKey, String typeKey, AssistantVoidCall call) {
        try {
            UserContext userContext = userContext(authorization);
            call.execute(resourceCommand(userContext, request, idKey, typeKey));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
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

    private Map<String, Object> enrichKnowledgeImportContent(Map<?, ?> request) {
        Map<String, Object> body = objectMap(request);
        enrichUploadedContent(body);
        enrichUploadedContentList(body, "docInfoList");
        enrichUploadedContentList(body, "docList");
        enrichUploadedContentList(body, "fileList");
        return body;
    }

    private void enrichUploadedContentList(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (!(value instanceof List)) {
            return;
        }
        List<Map<String, Object>> enriched = new ArrayList<>();
        for (Map<String, Object> item : mapList(value)) {
            enrichUploadedContent(item);
            enriched.add(item);
        }
        if (!enriched.isEmpty()) {
            body.put(key, enriched);
        }
    }

    private void enrichUploadedContent(Map<String, Object> body) {
        if (hasImportContent(body)) {
            return;
        }
        String fileId = firstText(body, "fileUploadId", "fileId", "docId");
        String content = UploadedFileStore.defaultStore().readText(fileId);
        if (isBlank(content)) {
            return;
        }
        body.put("content", content);
        if (isBlank(firstText(body, "docName", "name"))) {
            body.put("docName", fileId);
            body.put("name", fileId);
        }
    }

    private boolean hasImportContent(Map<String, Object> body) {
        return !isBlank(firstText(body, "content", "text", "docContent", "csv", "tsv",
                "contentBase64", "base64", "docContentBase64", "textBase64"));
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (entry.getKey() != null) {
                    result.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return result;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> mapList(Object value) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!(value instanceof List)) {
            return result;
        }
        for (Object item : (List<Object>) value) {
            if (item instanceof Map) {
                result.add(mapValue(item));
            }
        }
        return result;
    }

    private String firstText(Map<?, ?> source, String... keys) {
        if (source == null) {
            return "";
        }
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null && !isBlank(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private Map<String, Object> workflowToolSelect(UserContext userContext, String toolType, String name) {
        if (!isBlank(toolType) && !"builtin".equals(toolType) && !"custom".equals(toolType)) {
            throw new IllegalArgumentException("unsupported tool type");
        }
        Map<String, Object> source = null;
        if (mcpService != null) {
            source = mcpService.listToolSelect(userContext.getUserId(), userContext.getOrgId(), name);
        }
        if (source == null || source.isEmpty()) {
            source = appService.listAssistantToolSelect(userContext.getUserId(), userContext.getOrgId());
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : mapList(source.get("list"))) {
            String currentType = defaultIfBlank(firstText(item, "toolType", "type"), "builtin");
            if (!isBlank(toolType) && !toolType.equals(currentType)) {
                continue;
            }
            String toolId = firstText(item, "toolId", "toolSquareId", "customToolId");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("toolId", toolId);
            row.put("toolName", firstText(item, "toolName", "name"));
            row.put("toolType", currentType);
            row.put("iconUrl", workflowIconUrl(item));
            row.put("apiKey", firstText(item, "apiKey"));
            row.put("desc", firstText(item, "desc", "description"));
            row.put("actions", workflowToolActions(userContext, toolId, currentType));
            list.add(row);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return result;
    }

    private List<Map<String, Object>> workflowToolActions(UserContext userContext, String toolId, String toolType) {
        Map<String, Object> source = null;
        if (mcpService != null) {
            source = mcpService.listToolActions(userContext.getUserId(), userContext.getOrgId(), toolId, toolType);
        }
        if (source == null || source.isEmpty()) {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("toolId", toolId);
            request.put("toolType", toolType);
            source = appService.listAssistantToolActions(resourceCommand(
                    userContext, request, "toolId", "toolType"));
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> action : mapList(source.get("actions"))) {
            list.add(workflowActionSummary(action));
        }
        return list;
    }

    private Map<String, Object> workflowActionSummary(Map<String, Object> action) {
        String name = firstText(action, "name", "actionName", "actionId", "operationId");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("actionName", name);
        row.put("actionId", name);
        row.put("desc", firstText(action, "description", "desc", "summary"));
        return row;
    }

    private Map<String, Object> workflowToolAction(
            UserContext userContext, String toolId, String toolType, String actionName) {
        Map<String, Object> source = null;
        if (mcpService != null) {
            source = mcpService.getToolActionDetail(
                    userContext.getUserId(), userContext.getOrgId(), toolId, toolType, actionName);
        }
        if (source == null || source.isEmpty()) {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("toolId", toolId);
            request.put("toolType", toolType);
            request.put("actionName", actionName);
            source = appService.getAssistantToolActionDetail(
                    resourceCommand(userContext, request, "toolId", "toolType"));
        }
        Map<String, Object> action = mapValue(source.get("action"));
        if (action.isEmpty()) {
            action.put("name", actionName);
        }
        String resolvedAction = defaultIfBlank(firstText(action, "name", "actionName", "actionId"), actionName);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inputs", workflowParamsFromSchema(action.get("inputSchema")));
        result.put("outputs", workflowParamsFromSchema(action.get("outputSchema")));
        result.put("actionName", resolvedAction);
        result.put("actionId", resolvedAction);
        result.put("iconUrl", firstText(source, "iconUrl"));
        return result;
    }

    private Map<String, Object> workflowToolBox(UserContext userContext, Map<String, String> request) {
        String boxId = firstText(request, "box_id", "boxId", "toolId");
        String boxType = defaultIfBlank(firstText(request, "box_type", "boxType", "toolType"), "builtin");
        String toolId = firstText(request, "tool_id", "toolId", "actionName", "operationId");
        int page = intParam(firstText(request, "page"), 1);
        int pageSize = intParam(firstText(request, "page_size", "pageSize"), 100);
        if (page <= 0) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 100;
        }

        Map<String, Object> detail = new LinkedHashMap<>();
        if (!isBlank(boxId) && mcpService != null) {
            detail = "custom".equals(boxType)
                    ? mcpService.getCustomTool(userContext.getUserId(), userContext.getOrgId(), boxId)
                    : mcpService.getToolSquare(userContext.getUserId(), userContext.getOrgId(), boxId);
            if (detail == null) {
                detail = new LinkedHashMap<>();
            }
        }

        List<Map<String, Object>> tools = workflowToolBoxItems(detail, toolId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", tools.size());
        result.put("page", page);
        result.put("page_size", pageSize);
        result.put("total_pages", tools.isEmpty() ? 0 : 1);
        result.put("has_next", false);
        result.put("has_prev", false);
        result.put("box_id", boxId);
        result.put("api_key", firstText(detail, "apiKey"));
        result.put("api_auth", mapValue(detail.get("apiAuth")));
        result.put("tools", tools);
        return result;
    }

    private List<Map<String, Object>> workflowToolBoxItems(Map<String, Object> detail, String selectedToolId) {
        List<Map<String, Object>> actions = mapList(detail.get("tools"));
        if (actions.isEmpty()) {
            actions = mapList(detail.get("apiList"));
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> action : actions) {
            String actionName = firstText(action, "name", "actionName", "operationId");
            if (!isBlank(selectedToolId) && !selectedToolId.equals(actionName)) {
                continue;
            }
            list.add(workflowToolBoxItem(action, actionName));
        }
        return list;
    }

    private Map<String, Object> workflowToolBoxItem(Map<String, Object> action, String actionName) {
        long fixedTime = 1767225600000000000L;
        String description = firstText(action, "description", "desc", "summary");
        String method = defaultIfBlank(firstText(action, "method"), "POST");
        String path = defaultIfBlank(firstText(action, "path"), "/" + actionName);

        Map<String, Object> apiSpec = new LinkedHashMap<>();
        apiSpec.put("parameters", Collections.emptyList());
        apiSpec.put("request_body", action.get("inputSchema"));
        apiSpec.put("responses", Collections.emptyList());
        apiSpec.put("components", Collections.emptyMap());
        apiSpec.put("callbacks", Collections.emptyMap());
        apiSpec.put("security", Collections.emptyList());
        apiSpec.put("tags", Collections.emptyList());
        apiSpec.put("external_docs", Collections.emptyMap());

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("summary", actionName);
        metadata.put("description", description);
        metadata.put("server_url", "");
        metadata.put("path", path);
        metadata.put("method", method);
        metadata.put("create_time", fixedTime);
        metadata.put("update_time", fixedTime);
        metadata.put("create_user", "system");
        metadata.put("update_user", "system");
        metadata.put("api_spec", apiSpec);

        Map<String, Object> globalParameters = new LinkedHashMap<>();
        globalParameters.put("name", "");
        globalParameters.put("description", "");
        globalParameters.put("required", false);
        globalParameters.put("in", "");
        globalParameters.put("type", "");
        globalParameters.put("value", null);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("tool_id", actionName);
        row.put("name", actionName);
        row.put("description", description);
        row.put("status", "enabled");
        row.put("metadata_type", "openapi");
        row.put("metadata", metadata);
        row.put("use_rule", "");
        row.put("global_parameters", globalParameters);
        row.put("create_time", fixedTime);
        row.put("update_time", fixedTime);
        row.put("create_user", "system");
        row.put("update_user", "system");
        row.put("extend_info", Collections.emptyMap());
        row.put("resource_object", "");
        return row;
    }

    private String workflowIconUrl(Map<String, Object> item) {
        String iconUrl = firstText(item, "iconUrl", "iconURL", "avatarUrl");
        if (!isBlank(iconUrl)) {
            return iconUrl;
        }
        Map<String, Object> avatar = mapValue(item.get("avatar"));
        return firstText(avatar, "path", "url");
    }

    private List<Map<String, Object>> workflowParamsFromSchema(Object schemaObject) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (schemaObject == null) {
            return result;
        }
        try {
            JsonNode schema = JSON.valueToTree(schemaObject);
            JsonNode properties = schema.path("properties");
            if (!properties.isObject()) {
                return result;
            }
            java.util.Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                result.add(workflowParam(field.getKey(), field.getValue(), schema.path("required")));
            }
        } catch (IllegalArgumentException ignored) {
            result.clear();
        }
        return result;
    }

    private Map<String, Object> workflowParam(String name, JsonNode schema, JsonNode requiredList) {
        String type = workflowSchemaType(schema);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("input", Collections.emptyMap());
        row.put("description", schema.path("description").asText(""));
        row.put("name", name);
        row.put("type", type);
        row.put("required", containsText(requiredList, name));
        if ("list".equals(type)) {
            Map<String, Object> itemSchema = new LinkedHashMap<>();
            JsonNode items = schema.path("items");
            itemSchema.put("type", workflowSchemaType(items));
            itemSchema.put("schema", workflowChildParams(items));
            row.put("schema", itemSchema);
        } else {
            row.put("schema", workflowChildParams(schema));
        }
        return row;
    }

    private List<Map<String, Object>> workflowChildParams(JsonNode schema) {
        List<Map<String, Object>> children = new ArrayList<>();
        JsonNode properties = schema.path("properties");
        if (!properties.isObject()) {
            return children;
        }
        java.util.Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            children.add(workflowParam(field.getKey(), field.getValue(), schema.path("required")));
        }
        return children;
    }

    private boolean containsText(JsonNode list, String value) {
        if (!list.isArray()) {
            return false;
        }
        for (JsonNode item : list) {
            if (value.equals(item.asText())) {
                return true;
            }
        }
        return false;
    }

    private String workflowSchemaType(JsonNode schema) {
        String type = schema.path("type").asText("string");
        if ("array".equals(type)) {
            return "list";
        }
        if ("number".equals(type)) {
            return "float";
        }
        if ("integer".equals(type) || "boolean".equals(type) || "object".equals(type) || "string".equals(type)) {
            return type;
        }
        return isBlank(type) ? "string" : type;
    }

    @SuppressWarnings("unchecked")
    private AssistantResourceCommand resourceCommand(
            UserContext userContext, Map<String, Object> request, String idKey, String typeKey) {
        Map<String, Object> body = request == null ? Collections.<String, Object>emptyMap() : request;
        AssistantResourceCommand command = new AssistantResourceCommand();
        command.setUserId(userContext.getUserId());
        command.setOrgId(userContext.getOrgId());
        command.setAssistantId(stringValue(body.get("assistantId")));
        command.setResourceId(stringValue(body.get(idKey)));
        if (typeKey != null) {
            command.setResourceType(stringValue(body.get(typeKey)));
        }
        command.setActionName(stringValue(body.get("actionName")));
        command.setDesc(stringValue(body.get("desc")));
        Object enable = body.get("enable");
        if (enable instanceof Boolean) {
            command.setEnable((Boolean) enable);
        } else if (enable != null) {
            command.setEnable(Boolean.parseBoolean(String.valueOf(enable)));
        }
        Object toolConfig = body.get("toolConfig");
        if (toolConfig instanceof Map) {
            command.setToolConfig((Map<String, Object>) toolConfig);
        }
        return command;
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
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

    private String originalFileName(MultipartFile file) {
        String name = defaultIfBlank(file.getOriginalFilename(), "file");
        String normalized = name.replace("\\", "/");
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }

    private String dataUrl(MultipartFile file) throws IOException {
        String contentType = defaultIfBlank(file.getContentType(), "application/octet-stream");
        return "data:" + contentType + ";base64,"
                + Base64.getEncoder().encodeToString(file.getBytes());
    }

    private String markdownImage(String fileName, String fileUrl) {
        String alt = defaultIfBlank(fileName, "file").replace("[", "").replace("]", "");
        return "![" + alt + "](" + fileUrl + ")";
    }

    private interface KnowledgeCall {
        Map<String, Object> execute(UserContext userContext, Map<String, Object> request);
    }

    private interface KnowledgeVoidCall {
        void execute(UserContext userContext, Map<String, Object> request);
    }

    private interface AssistantVoidCall {
        void execute(AssistantResourceCommand command);
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
        long startedAt = System.currentTimeMillis();
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationStreamCommand command = request == null
                    ? new AssistantConversationStreamCommand()
                    : request.toCommand(draft);
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            AssistantConversationStreamResult result = appService.streamAssistantConversation(command);
            if (!draft) {
                recordAppStatistic(userContext, command.getAssistantId(), AGENT_APP_TYPE, true, true, startedAt, STAT_SOURCE_WEB);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(toSseFrame(result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        }
    }

    private ResponseEntity<String> streamRagChat(String authorization,
                                                 RagChatRequest request,
                                                 boolean draft) {
        long startedAt = System.currentTimeMillis();
        try {
            UserContext userContext = userContext(authorization);
            RagChatCommand command = request == null ? new RagChatRequest().toCommand(draft) : request.toCommand(draft);
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            RagChatResult result = appService.streamRagChat(command);
            if (!draft) {
                recordAppStatistic(userContext, command.getRagId(), RAG_APP_TYPE, true, true, startedAt, STAT_SOURCE_WEB);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(toRagAgUiSseFrames(result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        }
    }

    private void recordAppStatistic(UserContext userContext,
                                    String appId,
                                    String appType,
                                    boolean success,
                                    boolean stream,
                                    long startedAt,
                                    String source) {
        if (appService == null || isBlank(appId)) {
            return;
        }
        try {
            long costs = elapsedMillis(startedAt);
            RecordAppStatisticCommand command = new RecordAppStatisticCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            command.setAppId(appId);
            command.setAppType(appType);
            command.setSuccess(success);
            command.setStream(stream);
            command.setStreamCosts(stream ? costs : 0L);
            command.setNonStreamCosts(stream ? 0L : costs);
            command.setSource(source);
            appService.recordAppStatistic(command);
        } catch (RuntimeException ignored) {
        }
    }

    private void recordModelStatistic(UserContext userContext,
                                      ModelInfo model,
                                      ModelExperienceLlmRequest request,
                                      String answer,
                                      long startedAt) {
        if (appService == null || model == null || request == null || isBlank(request.getModelId())) {
            return;
        }
        try {
            RecordModelStatisticCommand command = new RecordModelStatisticCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            command.setModelId(request.getModelId());
            command.setModel(defaultIfBlank(model.getModel(), request.getModelId()));
            command.setProvider(defaultIfBlank(model.getProvider(), ""));
            command.setModelType(defaultIfBlank(model.getModelType(), "llm"));
            command.setPromptTokens(estimateTokens(request.getContent()));
            command.setCompletionTokens(estimateTokens(answer));
            command.setTotalTokens(command.getPromptTokens() + command.getCompletionTokens());
            command.setSuccess(true);
            command.setStream(true);
            command.setFirstTokenLatency(elapsedMillis(startedAt));
            command.setCosts(0L);
            appService.recordModelStatistic(command);
        } catch (RuntimeException ignored) {
        }
    }

    private String modelExperienceUpstreamAnswer(ModelInfo model, ModelExperienceLlmRequest request) {
        if (model == null || request == null || model.getConfig() == null) {
            return "";
        }
        try {
            String endpoint = firstText(model.getConfig(), "endpointUrl", "inferUrl", "baseUrl", "url");
            String apiKey = firstText(model.getConfig(), "apiKey");
            if (isBlank(endpoint) || isBlank(apiKey) || isDevelopmentApiKey(apiKey)) {
                return "";
            }
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", defaultIfBlank(request.getContent(), ""));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", defaultIfBlank(model.getModel(), request.getModelId()));
            payload.put("messages", Collections.singletonList(message));
            payload.put("stream", false);
            String response = postJson(modelEndpointUrl(endpoint, "/chat/completions"),
                    apiKey, JSON.writeValueAsString(payload));
            return extractChatContent(response);
        } catch (RuntimeException | IOException ignored) {
            return "";
        }
    }

    private String extractChatContent(String response) throws IOException {
        if (isBlank(response)) {
            return "";
        }
        JsonNode root = JSON.readTree(response);
        return root.path("choices").path(0).path("message").path("content").asText("");
    }

    private String postJson(String endpoint, String apiKey, String json) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(MODEL_PROXY_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(MODEL_PROXY_READ_TIMEOUT_MILLIS);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        try (OutputStream body = connection.getOutputStream()) {
            body.write(json.getBytes(StandardCharsets.UTF_8));
        }
        try {
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String response = readStream(stream);
            if (status >= 400) {
                throw new IOException("model experience upstream returned " + status);
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String modelEndpointUrl(String endpoint, String suffix) {
        String base = trimTrailingSlash(endpoint);
        if (base.endsWith(suffix)) {
            return base;
        }
        return base + suffix;
    }

    private String trimTrailingSlash(String value) {
        String result = defaultIfBlank(value, "");
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private boolean isDevelopmentApiKey(String value) {
        String normalized = value == null ? "" : value.trim();
        return "dev-model-key".equals(normalized)
                || "useless-api-key".equals(normalized)
                || "it-is-not-your-api-key".equals(normalized);
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0L, System.currentTimeMillis() - startedAt);
    }

    private long estimateTokens(String value) {
        if (isBlank(value)) {
            return 0L;
        }
        return Math.max(1L, (value.trim().length() + 3L) / 4L);
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

    private String matchGlobalSensitiveReply(UserContext userContext, String content) {
        if (safetyService == null || isBlank(content)) {
            return "";
        }
        try {
            Map<String, Object> tables = safetyService.listSensitiveWordTables(
                    userContext.getUserId(), userContext.getOrgId(), "global");
            for (Map<String, Object> table : mapList(tables == null ? null : tables.get("list"))) {
                String tableId = firstText(table, "tableId", "id", "value");
                if (isBlank(tableId)) {
                    continue;
                }
                String reply = matchSensitiveTable(userContext, tableId, firstText(table, "reply"), content);
                if (!isBlank(reply)) {
                    return reply;
                }
            }
        } catch (RuntimeException ignored) {
            return "";
        }
        return "";
    }

    private String matchSensitiveTable(UserContext userContext, String tableId, String fallbackReply, String content) {
        String reply = firstNonBlank(fallbackReply, "Content blocked by sensitive word filter");
        try {
            Map<String, Object> table = safetyService.getSensitiveWordTable(
                    userContext.getUserId(), userContext.getOrgId(), tableId);
            reply = firstNonBlank(firstText(table, "reply"), reply);
            Map<String, Object> words = safetyService.listSensitiveWords(
                    userContext.getUserId(), userContext.getOrgId(), tableId, 1, 1000);
            for (Map<String, Object> item : mapList(words == null ? null : words.get("list"))) {
                String word = firstText(item, "word", "content", "sensitiveWord", "name");
                if (!isBlank(word) && content.contains(word)) {
                    return reply;
                }
            }
        } catch (RuntimeException ignored) {
            return "";
        }
        return "";
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
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

    private String toRagAgUiSseFrames(RagChatResult result) {
        String threadId = "rag-thread-" + UUID.randomUUID().toString();
        String runId = "rag-run-" + UUID.randomUUID().toString();
        String messageId = "rag-message-" + UUID.randomUUID().toString();
        StringBuilder frames = new StringBuilder();
        frames.append(sseEvent(agUiEvent("RUN_STARTED", threadId, runId, null, null, null)));
        if (result.getQaSearchList() != null && !result.getQaSearchList().isEmpty()) {
            frames.append(sseEvent(agUiEvent("CUSTOM", threadId, runId, null, "rag_qa_search_list", result.getQaSearchList())));
        }
        if (result.getSearchList() != null && !result.getSearchList().isEmpty()) {
            frames.append(sseEvent(agUiEvent("CUSTOM", threadId, runId, null, "rag_search_list", result.getSearchList())));
        }
        frames.append(sseEvent(agUiEvent("TEXT_MESSAGE_START", threadId, runId, messageId, null, null)));
        Map<String, Object> content = agUiEvent("TEXT_MESSAGE_CONTENT", threadId, runId, messageId, null, null);
        content.put("delta", defaultIfBlank(result.getResponse(), ""));
        frames.append(sseEvent(content));
        frames.append(sseEvent(agUiEvent("TEXT_MESSAGE_END", threadId, runId, messageId, null, null)));
        frames.append(sseEvent(agUiEvent("RUN_FINISHED", threadId, runId, null, null, null)));
        return frames.toString();
    }

    private Map<String, Object> agUiEvent(String type,
                                          String threadId,
                                          String runId,
                                          String messageId,
                                          String name,
                                          Object value) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", type);
        event.put("threadId", threadId);
        event.put("runId", runId);
        if (!isBlank(messageId)) {
            event.put("messageId", messageId);
        }
        if (!isBlank(name)) {
            event.put("name", name);
            event.put("value", value == null ? Collections.emptyList() : value);
        }
        return event;
    }

    private String sseEvent(Map<String, Object> event) {
        try {
            return "data: " + JSON.writeValueAsString(event) + "\n\n";
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("rag sse serialization failed", ex);
        }
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

    private ResponseEntity<byte[]> exportWorkflowJson(String authorization, Map<String, String> request, boolean published) {
        return exportFlowJson(authorization, request, published, false);
    }

    private ResponseEntity<byte[]> exportChatflowJson(String authorization, Map<String, String> request, boolean published) {
        return exportFlowJson(authorization, request, published, true);
    }

    private ResponseEntity<byte[]> exportFlowJson(String authorization,
                                                  Map<String, String> request,
                                                  boolean published,
                                                  boolean chatflow) {
        try {
            UserContext userContext = userContext(authorization);
            WorkflowExportQuery query = new WorkflowExportQuery();
            query.setWorkflowId(workflowId(request));
            query.setVersion(request == null ? "" : request.get("version"));
            query.setPublished(published);
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            WorkflowExportResult result = chatflow
                    ? appService.exportChatflow(query)
                    : appService.exportWorkflow(query);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("name", result == null ? "" : result.getName());
            body.put("desc", result == null ? "" : result.getDesc());
            body.put("schema", result == null ? "" : result.getSchema());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(JSON.writeValueAsBytes(body));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    private WorkflowImportRequest workflowImportRequest(MultipartFile file, Map<String, String> form) throws IOException {
        WorkflowImportRequest request = new WorkflowImportRequest();
        if (file != null && !file.isEmpty()) {
            request = JSON.readValue(file.getBytes(), WorkflowImportRequest.class);
        }
        if (form == null) {
            return request;
        }
        if (!isBlank(form.get("name"))) {
            request.setName(form.get("name"));
        }
        if (!isBlank(form.get("workflowName"))) {
            request.setName(form.get("workflowName"));
        }
        if (!isBlank(form.get("desc"))) {
            request.setDesc(form.get("desc"));
        }
        if (!isBlank(form.get("workflowDesc"))) {
            request.setDesc(form.get("workflowDesc"));
        }
        if (!isBlank(form.get("schema"))) {
            request.setSchema(form.get("schema"));
        }
        return request;
    }

    private Map<String, Object> workflowCreateResult(WorkflowCreateResult result) {
        String workflowId = result == null ? "" : defaultIfBlank(result.getWorkflowId(), "");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workflow_id", workflowId);
        body.put("workflowId", workflowId);
        return body;
    }

    private Map<String, Object> workflowRunResult(WorkflowRunResult result) {
        String workflowId = result == null ? "" : defaultIfBlank(result.getWorkflowId(), "");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workflow_id", workflowId);
        body.put("workflowId", workflowId);
        body.put("output", result == null || result.getOutput() == null
                ? Collections.emptyMap()
                : result.getOutput());
        return body;
    }

    private String workflowId(Map<String, String> request) {
        if (request == null) {
            return "";
        }
        String workflowId = request.get("workflow_id");
        if (!isBlank(workflowId)) {
            return workflowId;
        }
        return defaultIfBlank(request.get("workflowId"), "");
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

    public static class RagCreateRequest {
        private String name;
        private String desc;
        private AvatarRequest avatar = new AvatarRequest();

        public RagCreateCommand toCommand() {
            RagCreateCommand command = new RagCreateCommand();
            command.setName(name);
            command.setDesc(desc);
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

        public AvatarRequest getAvatar() {
            return avatar;
        }

        public void setAvatar(AvatarRequest avatar) {
            this.avatar = avatar;
        }
    }

    public static class WorkflowCreateRequest {
        private String name;
        private String desc;
        private AvatarRequest avatar = new AvatarRequest();
        private String schema;

        public WorkflowCreateCommand toCreateCommand() {
            WorkflowCreateCommand command = new WorkflowCreateCommand();
            command.setName(name);
            command.setDesc(desc);
            command.setSchema(schema);
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

        public AvatarRequest getAvatar() {
            return avatar;
        }

        public void setAvatar(AvatarRequest avatar) {
            this.avatar = avatar;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }
    }

    public static class WorkflowImportRequest {
        private String name;
        private String desc;
        private String schema;

        public WorkflowImportCommand toCommand() {
            WorkflowImportCommand command = new WorkflowImportCommand();
            command.setName(name);
            command.setDesc(desc);
            command.setSchema(schema);
            return command;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setWorkflowName(String workflowName) {
            this.name = workflowName;
        }

        public void setWorkflow_name(String workflowName) {
            this.name = workflowName;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void setWorkflowDesc(String workflowDesc) {
            this.desc = workflowDesc;
        }

        public void setWorkflow_desc(String workflowDesc) {
            this.desc = workflowDesc;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }
    }

    public static class WorkflowIdRequest {
        private String workflowId;
        private boolean needPublished;

        public String workflowId() {
            return workflowId;
        }

        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
        }

        public void setWorkflow_id(String workflowId) {
            this.workflowId = workflowId;
        }

        public boolean isNeedPublished() {
            return needPublished;
        }

        public void setNeedPublished(boolean needPublished) {
            this.needPublished = needPublished;
        }
    }

    public static class ChatflowApplicationInfoRequest {
        private String intelligenceId;
        private Long intelligenceType;

        public ChatflowApplicationInfoQuery toQuery() {
            ChatflowApplicationInfoQuery query = new ChatflowApplicationInfoQuery();
            query.setIntelligenceId(intelligenceId);
            query.setIntelligenceType(intelligenceType);
            return query;
        }

        public String getIntelligenceId() {
            return intelligenceId;
        }

        public void setIntelligenceId(String intelligenceId) {
            this.intelligenceId = intelligenceId;
        }

        public void setIntelligence_id(String intelligenceId) {
            this.intelligenceId = intelligenceId;
        }

        public Long getIntelligenceType() {
            return intelligenceType;
        }

        public void setIntelligenceType(Long intelligenceType) {
            this.intelligenceType = intelligenceType;
        }

        public void setIntelligence_type(Long intelligenceType) {
            this.intelligenceType = intelligenceType;
        }
    }

    public static class ChatflowConversationDeleteRequest {
        private String projectId;
        private String uniqueId;

        public ChatflowConversationDeleteCommand toCommand() {
            ChatflowConversationDeleteCommand command = new ChatflowConversationDeleteCommand();
            command.setProjectId(projectId);
            command.setUniqueId(uniqueId);
            return command;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public void setProject_id(String projectId) {
            this.projectId = projectId;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public void setUnique_id(String uniqueId) {
            this.uniqueId = uniqueId;
        }
    }

    public static class WorkflowRunRequest {
        private String workflowId;
        private Map<String, Object> input;

        public WorkflowRunCommand toCommand() {
            WorkflowRunCommand command = new WorkflowRunCommand();
            command.setWorkflowId(workflowId);
            command.setInput(input == null ? Collections.<String, Object>emptyMap() : input);
            return command;
        }

        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
        }

        public void setWorkflow_id(String workflowId) {
            this.workflowId = workflowId;
        }

        public Map<String, Object> getInput() {
            return input;
        }

        public void setInput(Map<String, Object> input) {
            this.input = input;
        }
    }

    public static class RagUpdateRequest {
        private String ragId;
        private String name;
        private String desc;
        private AvatarRequest avatar = new AvatarRequest();

        public RagUpdateCommand toCommand() {
            RagUpdateCommand command = new RagUpdateCommand();
            command.setRagId(ragId);
            command.setName(name);
            command.setDesc(desc);
            if (avatar != null) {
                command.setAvatarKey(avatar.getKey());
                command.setAvatarPath(avatar.getPath());
            }
            return command;
        }

        public String getRagId() {
            return ragId;
        }

        public void setRagId(String ragId) {
            this.ragId = ragId;
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

        public AvatarRequest getAvatar() {
            return avatar;
        }

        public void setAvatar(AvatarRequest avatar) {
            this.avatar = avatar;
        }
    }

    public static class RagConfigRequest {
        private String ragId;
        private Map<String, Object> modelConfig;
        private Map<String, Object> rerankConfig;
        private Map<String, Object> qaRerankConfig;
        private Map<String, Object> knowledgeBaseConfig;
        private Map<String, Object> qaKnowledgeBaseConfig;
        private Map<String, Object> safetyConfig;
        private Map<String, Object> visionConfig;

        public RagConfigUpdateCommand toCommand() {
            RagConfigUpdateCommand command = new RagConfigUpdateCommand();
            command.setRagId(ragId);
            command.setModelConfig(modelConfig);
            command.setRerankConfig(rerankConfig);
            command.setQaRerankConfig(qaRerankConfig);
            command.setKnowledgeBaseConfig(knowledgeBaseConfig);
            command.setQaKnowledgeBaseConfig(qaKnowledgeBaseConfig);
            command.setSafetyConfig(safetyConfig);
            command.setVisionConfig(visionConfig);
            return command;
        }

        public String getRagId() {
            return ragId;
        }

        public void setRagId(String ragId) {
            this.ragId = ragId;
        }

        public Map<String, Object> getModelConfig() {
            return modelConfig;
        }

        public void setModelConfig(Map<String, Object> modelConfig) {
            this.modelConfig = modelConfig;
        }

        public Map<String, Object> getRerankConfig() {
            return rerankConfig;
        }

        public void setRerankConfig(Map<String, Object> rerankConfig) {
            this.rerankConfig = rerankConfig;
        }

        public Map<String, Object> getQaRerankConfig() {
            return qaRerankConfig;
        }

        public void setQaRerankConfig(Map<String, Object> qaRerankConfig) {
            this.qaRerankConfig = qaRerankConfig;
        }

        public Map<String, Object> getKnowledgeBaseConfig() {
            return knowledgeBaseConfig;
        }

        public void setKnowledgeBaseConfig(Map<String, Object> knowledgeBaseConfig) {
            this.knowledgeBaseConfig = knowledgeBaseConfig;
        }

        public Map<String, Object> getQaKnowledgeBaseConfig() {
            return qaKnowledgeBaseConfig;
        }

        public void setQaKnowledgeBaseConfig(Map<String, Object> qaKnowledgeBaseConfig) {
            this.qaKnowledgeBaseConfig = qaKnowledgeBaseConfig;
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
    }

    public static class RagIdRequest {
        private String ragId;

        public String getRagId() {
            return ragId;
        }

        public void setRagId(String ragId) {
            this.ragId = ragId;
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

    public static class RagChatRequest {
        private String ragId;
        private String question;
        private List<Map<String, Object>> history;
        private List<Map<String, Object>> fileInfo;

        public RagChatCommand toCommand(boolean draft) {
            RagChatCommand command = new RagChatCommand();
            command.setRagId(ragId);
            command.setQuestion(question);
            command.setDraft(draft);
            command.setHistory(history == null ? Collections.<Map<String, Object>>emptyList() : history);
            command.setFileInfo(fileInfo == null ? Collections.<Map<String, Object>>emptyList() : fileInfo);
            return command;
        }

        public String getRagId() {
            return ragId;
        }

        public void setRagId(String ragId) {
            this.ragId = ragId;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<Map<String, Object>> getHistory() {
            return history;
        }

        public void setHistory(List<Map<String, Object>> history) {
            this.history = history;
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
