package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuSettingApiController {

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    public WanwuSettingApiController() {
    }

    public WanwuSettingApiController(IamService iamService) {
        this.iamService = iamService;
    }

    @PostMapping("/custom/tab")
    public FrontendResponse<Map<String, Object>> updateCustomTab(
            @RequestBody(required = false) Map<String, Object> request) {
        iamService.updateCustomTab(body(request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/custom/login")
    public FrontendResponse<Map<String, Object>> updateCustomLogin(
            @RequestBody(required = false) Map<String, Object> request) {
        iamService.updateCustomLogin(body(request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/custom/home")
    public FrontendResponse<Map<String, Object>> updateCustomHome(
            @RequestBody(required = false) Map<String, Object> request) {
        iamService.updateCustomHome(body(request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    private Map<String, Object> body(Map<String, Object> request) {
        return request == null ? new LinkedHashMap<>() : request;
    }
}
