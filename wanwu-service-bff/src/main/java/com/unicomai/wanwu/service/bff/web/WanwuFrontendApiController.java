package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCopyCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationSelectResult;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    public WanwuFrontendApiController() {
    }

    public WanwuFrontendApiController(IamService iamService, AppService appService) {
        this.iamService = iamService;
        this.appService = appService;
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

    @GetMapping("/base/custom")
    public FrontendResponse<Map<String, Object>> platformConfig() {
        return FrontendResponse.ok(iamService.platformConfig());
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

    @GetMapping("/appspace/app/url")
    public FrontendResponse<String> appOpenUrl() {
        return FrontendResponse.ok("");
    }

    @GetMapping({
            "/model/select/{modelType}",
            "/prompt/custom/list",
            "/prompt/template/list",
            "/mcp/select",
            "/workflow/select",
            "/safe/sensitive/table/select",
            "/tool/select",
            "/agent/skill/select",
            "/assistant/conversation/draft/detail",
            "/appspace/app/version/list"
    })
    public FrontendResponse<Map<String, Object>> emptySelectResult() {
        return FrontendResponse.ok(emptyListResult());
    }

    @PostMapping("/knowledge/select")
    public FrontendResponse<Map<String, Object>> emptyPostSelectResult() {
        return FrontendResponse.ok(emptyListResult());
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

    private Map<String, Object> emptyListResult() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", Collections.emptyList());
        result.put("total", 0);
        return result;
    }

    private FrontendResponse<Map<String, Object>> deleteAssistant(UserContext userContext, String assistantId) {
        AssistantDeleteCommand command = new AssistantDeleteCommand();
        command.setAssistantId(assistantId);
        command.setUserId(userContext.getUserId());
        command.setOrgId(userContext.getOrgId());
        appService.deleteAssistant(command);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
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
