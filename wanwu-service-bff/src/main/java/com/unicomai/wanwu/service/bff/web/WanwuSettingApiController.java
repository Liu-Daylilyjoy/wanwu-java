package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.operate.OperateService;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuSettingApiController {

    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private OperateService operateService;

    public WanwuSettingApiController() {
    }

    public WanwuSettingApiController(OperateService operateService) {
        this.operateService = operateService;
    }

    @PostMapping("/custom/tab")
    public FrontendResponse<Map<String, Object>> updateCustomTab(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext context = userContext(authorization);
        operateService.createSystemCustomTab(context.userId, context.orgId, mode(request), body(request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/custom/login")
    public FrontendResponse<Map<String, Object>> updateCustomLogin(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext context = userContext(authorization);
        operateService.createSystemCustomLogin(context.userId, context.orgId, mode(request), body(request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/custom/home")
    public FrontendResponse<Map<String, Object>> updateCustomHome(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext context = userContext(authorization);
        operateService.createSystemCustomHome(context.userId, context.orgId, mode(request), body(request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    private Map<String, Object> body(Map<String, Object> request) {
        return request == null ? new LinkedHashMap<>() : request;
    }

    private String mode(Map<String, Object> request) {
        if (request == null || request.get("mode") == null || String.valueOf(request.get("mode")).trim().isEmpty()) {
            return "light";
        }
        return String.valueOf(request.get("mode"));
    }

    private UserContext userContext(String authorization) {
        if (authorization != null && authorization.contains("dev-token-app")) {
            return new UserContext(DEV_APP_USER_ID, DEV_ORG_ID);
        }
        return new UserContext(DEV_USER_ID, DEV_ORG_ID);
    }

    private static class UserContext {
        private final String userId;
        private final String orgId;

        private UserContext(String userId, String orgId) {
            this.userId = userId;
            this.orgId = orgId;
        }
    }
}
