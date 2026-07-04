package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
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
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuExplorationApiController {

    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";

    private final Set<String> favoriteApps = Collections.synchronizedSet(new HashSet<String>());

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    public WanwuExplorationApiController() {
    }

    public WanwuExplorationApiController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/exploration/app/list")
    public FrontendResponse<ApplicationListResult> listExplorationApps(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "appType", required = false) String appType,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "searchType", required = false, defaultValue = "all") String searchType) {
        UserContext ctx = userContext(authorization);
        if ("history".equals(defaultIfBlank(searchType, "all"))) {
            java.util.ArrayList<Map<String, Object>> apps = historyApps(ctx, appType, name);
            return FrontendResponse.ok(new ApplicationListResult(apps, apps.size()));
        }
        ApplicationListResult source = appService.listApplications(new ApplicationListQuery(
                defaultIfBlank(appType, ""),
                defaultIfBlank(name, ""),
                ctx.userId,
                ctx.orgId));

        java.util.ArrayList<Map<String, Object>> apps = new java.util.ArrayList<>();
        if (source.getList() != null) {
            for (Map<String, Object> item : source.getList()) {
                Map<String, Object> app = explorationApp(item);
                if (matchesSearchType(app, searchType)) {
                    apps.add(app);
                }
            }
        }
        return FrontendResponse.ok(new ApplicationListResult(apps, apps.size()));
    }

    @PostMapping("/exploration/app/favorite")
    public FrontendResponse<Map<String, Object>> changeFavorite(
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = body(request);
        String key = appKey(value(body, "appType"), value(body, "appId"));
        if (booleanValue(body, "isFavorite", false)) {
            favoriteApps.add(key);
        } else {
            favoriteApps.remove(key);
        }
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping("/exploration/app/history")
    public FrontendResponse<Map<String, Object>> history(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "appType", required = false) String appType,
            @RequestParam(value = "name", required = false) String name) {
        UserContext ctx = userContext(authorization);
        java.util.ArrayList<Map<String, Object>> apps = historyApps(ctx, appType, name);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", apps);
        result.put("total", apps.size());
        return FrontendResponse.ok(result);
    }

    private java.util.ArrayList<Map<String, Object>> historyApps(UserContext ctx, String appType, String name) {
        List<ExplorationAppHistoryStore.HistoryEntry> entries =
                ExplorationAppHistoryStore.INSTANCE.list(ctx.userId, defaultIfBlank(appType, ""));
        java.util.ArrayList<Map<String, Object>> apps = new java.util.ArrayList<>();
        if (entries.isEmpty()) {
            return apps;
        }
        Map<String, Map<String, Object>> sourceByKey = currentAppsByKey(ctx, appType, name);
        for (ExplorationAppHistoryStore.HistoryEntry entry : entries) {
            Map<String, Object> source = sourceByKey.get(appKey(entry.getAppType(), entry.getAppId()));
            Map<String, Object> app = source == null ? historyPlaceholder(entry) : explorationApp(source);
            app.put("visitedAt", entry.updatedAtText());
            app.put("historyCreatedAt", entry.createdAtText());
            if (matchesName(app, name)) {
                apps.add(app);
            }
        }
        return apps;
    }

    private Map<String, Map<String, Object>> currentAppsByKey(UserContext ctx, String appType, String name) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        ApplicationListResult source = appService.listApplications(new ApplicationListQuery(
                defaultIfBlank(appType, ""),
                defaultIfBlank(name, ""),
                ctx.userId,
                ctx.orgId));
        if (source == null || source.getList() == null) {
            return result;
        }
        for (Map<String, Object> item : source.getList()) {
            Map<String, Object> app = explorationApp(item);
            result.put(appKey(value(app, "appType"), value(app, "appId")), app);
        }
        return result;
    }

    private Map<String, Object> historyPlaceholder(ExplorationAppHistoryStore.HistoryEntry entry) {
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("appId", entry.getAppId());
        app.put("appType", entry.getAppType());
        app.put("name", entry.getAppId());
        app.put("desc", "");
        app.put("publishType", "public");
        app.put("createdAt", entry.createdAtText());
        app.put("updatedAt", entry.updatedAtText());
        return explorationApp(app);
    }

    private boolean matchesSearchType(Map<String, Object> app, String searchType) {
        String normalized = defaultIfBlank(searchType, "all");
        if ("favorite".equals(normalized)) {
            return Boolean.TRUE.equals(app.get("isFavorite"));
        }
        if ("private".equals(normalized)) {
            return "private".equals(app.get("publishType"));
        }
        return true;
    }

    private boolean matchesName(Map<String, Object> app, String name) {
        String keyword = defaultIfBlank(name, "");
        if (keyword.isEmpty()) {
            return true;
        }
        String lower = keyword.toLowerCase(java.util.Locale.ENGLISH);
        return value(app, "name").toLowerCase(java.util.Locale.ENGLISH).contains(lower)
                || value(app, "desc").toLowerCase(java.util.Locale.ENGLISH).contains(lower)
                || value(app, "appId").toLowerCase(java.util.Locale.ENGLISH).contains(lower);
    }

    private Map<String, Object> explorationApp(Map<String, Object> source) {
        Map<String, Object> app = new LinkedHashMap<>();
        if (source != null) {
            app.putAll(source);
        }
        String appType = value(app, "appType");
        String appId = value(app, "appId");
        app.put("isFavorite", favoriteApps.contains(appKey(appType, appId)));
        if (!app.containsKey("user")) {
            Map<String, Object> user = new LinkedHashMap<>();
            user.put("userId", DEV_USER_ID);
            user.put("userName", "Wanwu");
            app.put("user", user);
        }
        if (!app.containsKey("avatar")) {
            app.put("avatar", Collections.emptyMap());
        }
        if (!app.containsKey("createdAt")) {
            app.put("createdAt", "2026-06-30 00:00:00");
        }
        if (!app.containsKey("publishType")) {
            app.put("publishType", "public");
        }
        return app;
    }

    private String appKey(String appType, String appId) {
        return defaultIfBlank(appType, "") + ":" + defaultIfBlank(appId, "");
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

    private boolean booleanValue(Map<String, Object> request, String key, boolean fallback) {
        if (request == null || request.get(key) == null) {
            return fallback;
        }
        Object value = request.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
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
