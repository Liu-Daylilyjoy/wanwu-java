package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
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

    @GetMapping("/assistant/draft")
    public FrontendResponse<Map<String, Object>> assistantDraft(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("assistantId") String assistantId) {
        UserContext userContext = userContext(authorization);
        return FrontendResponse.ok(appService.getAssistantDraft(
                new AssistantDetailQuery(assistantId, userContext.getUserId(), userContext.getOrgId())));
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
