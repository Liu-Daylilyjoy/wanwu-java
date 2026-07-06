package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.operate.OperateService;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuOperationApiController {

    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private OperateService operateService;

    public WanwuOperationApiController() {
    }

    public WanwuOperationApiController(IamService iamService) {
        this(iamService, null);
    }

    public WanwuOperationApiController(IamService iamService, OperateService operateService) {
        this.iamService = iamService;
        this.operateService = operateService;
    }

    @PostMapping("/oauth/app")
    public FrontendResponse<Map<String, Object>> createOauthApp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = body(request);
        if (!isValidRedirectUri(body)) {
            return FrontendResponse.failure(1001, "redirect uri invalid");
        }
        iamService.createOauthApp(userContext(authorization).userId, body);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PutMapping("/oauth/app")
    public FrontendResponse<Map<String, Object>> updateOauthApp(
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = body(request);
        if (!isValidRedirectUri(body)) {
            return FrontendResponse.failure(1001, "redirect uri invalid");
        }
        iamService.updateOauthApp(body);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @DeleteMapping("/oauth/app")
    public FrontendResponse<Map<String, Object>> deleteOauthApp(
            @RequestBody(required = false) Map<String, Object> request) {
        iamService.deleteOauthApp(body(request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping("/oauth/app/list")
    public FrontendResponse<Map<String, Object>> listOauthApps(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        UserContext ctx = userContext(authorization);
        return FrontendResponse.ok(iamService.listOauthApps(ctx.userId, defaultIfBlank(name, ""), pageNo, pageSize));
    }

    @PutMapping("/oauth/app/status")
    public FrontendResponse<Map<String, Object>> updateOauthAppStatus(
            @RequestBody(required = false) Map<String, Object> request) {
        iamService.updateOauthAppStatus(body(request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping("/statistic/client")
    public FrontendResponse<Map<String, Object>> clientStatistic(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        UserContext ctx = userContext(authorization);
        Map<String, Object> oauthApps = iamService == null
                ? Collections.<String, Object>emptyMap()
                : iamService.listOauthApps(ctx.userId, "", 1, 10000);
        Map<String, Object> local = OperationClientStatisticStore.INSTANCE.clientStatistic(oauthApps, startDate, endDate);
        if (operateService == null) {
            return FrontendResponse.ok(local);
        }
        try {
            Map<String, Object> result = operateService.getClientStatistic(startDate, endDate);
            return FrontendResponse.ok(mergeBrowseCompatibility(result, local));
        } catch (RuntimeException ex) {
            return FrontendResponse.ok(local);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeBrowseCompatibility(Map<String, Object> result, Map<String, Object> local) {
        if (result == null || result.isEmpty()) {
            return local;
        }
        Map<String, Object> overview = map(result.get("overview"));
        Map<String, Object> localOverview = map(local.get("overview"));
        if (!localOverview.isEmpty() && localOverview.get("browse") != null) {
            overview.put("browse", localOverview.get("browse"));
        }
        result.put("overview", overview);

        Map<String, Object> trend = map(result.get("trend"));
        Map<String, Object> localTrend = map(local.get("trend"));
        if (!localTrend.isEmpty() && localTrend.get("browse") != null) {
            trend.put("browse", localTrend.get("browse"));
        }
        result.put("trend", trend);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        if (value instanceof Map) {
            return new LinkedHashMap<>((Map<String, Object>) value);
        }
        return new LinkedHashMap<>();
    }

    private boolean isValidRedirectUri(Map<String, Object> request) {
        String redirectUri = defaultIfBlank(value(request, "redirectUri"), "");
        if (redirectUri.isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(redirectUri);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    private Map<String, Object> body(Map<String, Object> request) {
        return request == null ? new LinkedHashMap<>() : request;
    }

    private String value(Map<String, Object> request, String key) {
        if (request == null || request.get(key) == null) {
            return "";
        }
        return String.valueOf(request.get(key));
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private UserContext userContext(String authorization) {
        BffUserContextResolver.ResolvedUser resolved = BffUserContextResolver.resolve(authorization);
        return new UserContext(resolved.getUserId(), resolved.getOrgId());
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
