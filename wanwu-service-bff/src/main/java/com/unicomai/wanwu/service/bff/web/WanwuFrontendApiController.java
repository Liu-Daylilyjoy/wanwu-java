package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
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

import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuFrontendApiController {

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
            @RequestParam(value = "appType", required = false) String appType,
            @RequestParam(value = "name", required = false) String name) {
        return FrontendResponse.ok(appService.listAssistants(new ApplicationListQuery(appType, name)));
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
}
