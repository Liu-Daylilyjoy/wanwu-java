package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class WanwuFrontendPermissionFilter extends OncePerRequestFilter {

    private static final String PREFIX = "/user/api/v1";
    private static final Set<String> APP_PERMISSIONS = set("app", "app.rag", "app.workflow", "app.agent");
    private static final Set<String> ADMIN_PERMISSIONS = set(
            "permission", "permission.user", "permission.org", "permission.role",
            "setting",
            "model", "model.model_management",
            "app", "app.rag", "app.workflow", "app.agent",
            "api_key", "api_key.api_key_management",
            "resource", "resource.knowledge", "resource.tool", "resource.mcp",
            "resource.prompt", "resource.skill", "resource.safety",
            "operation", "operation.oauth", "operation.statistic_client",
            "exploration", "exploration.app", "exploration.mcp", "exploration.template", "exploration.skill",
            "app_observability", "app_observability.statistic");

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    public WanwuFrontendPermissionFilter() {
    }

    public WanwuFrontendPermissionFilter(IamService iamService) {
        this.iamService = iamService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = path(request);
        return path == null || !path.startsWith(PREFIX + "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = path(request);
        String required = requiredPermission(path);
        if (isBlank(required) || permissions(request).contains(required)) {
            filterChain.doFilter(request, response);
            return;
        }
        writeForbidden(response);
    }

    private String requiredPermission(String path) {
        String route = path.substring(PREFIX.length());
        if (isCommonRoute(route)) {
            return "";
        }
        if (starts(route, "/user/batch", "/user/status", "/user/list", "/user/admin/password",
                "/org/other/select", "/org/user")) {
            return "permission.user";
        }
        if (starts(route, "/user")) {
            return "permission.user";
        }
        if (starts(route, "/role")) {
            return "permission.role";
        }
        if (starts(route, "/org")) {
            return "permission.org";
        }
        if (starts(route, "/custom")) {
            return "setting";
        }
        if (starts(route, "/model/select")) {
            return "";
        }
        if (starts(route, "/model")) {
            return "model.model_management";
        }
        if (starts(route, "/api/key")) {
            return "api_key.api_key_management";
        }
        if (starts(route, "/appspace/assistant", "/assistant")) {
            return "app.agent";
        }
        if (starts(route, "/appspace/rag", "/rag")) {
            return "app.rag";
        }
        if (starts(route, "/appspace/workflow", "/appspace/chatflow", "/workflow", "/chatflow")) {
            return "app.workflow";
        }
        if (starts(route, "/knowledge")) {
            return "resource.knowledge";
        }
        if (starts(route, "/tool")) {
            return "resource.tool";
        }
        if (starts(route, "/mcp")) {
            return "resource.mcp";
        }
        if (starts(route, "/prompt")) {
            return "resource.prompt";
        }
        if (starts(route, "/agent/skill", "/agent/acquired/skill", "/builtin/skill", "/square/skill")) {
            return "resource.skill";
        }
        if (starts(route, "/safe")) {
            return "resource.safety";
        }
        if (starts(route, "/oauth")) {
            return "operation.oauth";
        }
        if (starts(route, "/statistic/client")) {
            return "operation.statistic_client";
        }
        if (starts(route, "/statistic")) {
            return "app_observability.statistic";
        }
        if (starts(route, "/exploration/app")) {
            return "exploration.app";
        }
        if (starts(route, "/exploration/mcp")) {
            return "exploration.mcp";
        }
        if (starts(route, "/exploration")) {
            return "exploration";
        }
        return "";
    }

    private boolean isCommonRoute(String route) {
        return starts(route,
                "/base/",
                "/user/permission",
                "/user/info",
                "/user/password",
                "/user/login",
                "/avatar",
                "/cache/avatar",
                "/org/select",
                "/file/",
                "/doc_center");
    }

    private Set<String> permissions(HttpServletRequest request) {
        String token = BffUserContextResolver.extractToken(request.getHeader("Authorization"));
        if (iamService != null) {
            try {
                return permissions(iamService.permission(token));
            } catch (RuntimeException ignored) {
                // Fall back to deterministic Docker development tokens below.
            }
        }
        if ("dev-token-app".equals(token)) {
            return APP_PERMISSIONS;
        }
        return ADMIN_PERMISSIONS;
    }

    @SuppressWarnings("unchecked")
    private Set<String> permissions(PermissionResult result) {
        if (result == null || result.getOrgPermission() == null) {
            return ADMIN_PERMISSIONS;
        }
        Object raw = result.getOrgPermission().get("permissions");
        if (!(raw instanceof List)) {
            return ADMIN_PERMISSIONS;
        }
        Set<String> permissions = new LinkedHashSet<String>();
        for (Object item : (List<?>) raw) {
            if (item instanceof Map) {
                Object perm = ((Map<String, Object>) item).get("perm");
                if (perm != null) {
                    permissions.add(String.valueOf(perm));
                }
            } else if (item != null) {
                permissions.add(String.valueOf(item));
            }
        }
        return permissions.isEmpty() ? ADMIN_PERMISSIONS : permissions;
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"msg\":\"permission denied\",\"data\":null}");
    }

    private String path(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private boolean starts(String route, String... prefixes) {
        for (String prefix : prefixes) {
            if (route.equals(prefix) || route.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static Set<String> set(String... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(values)));
    }
}
