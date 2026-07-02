package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.ApiKeyListQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyPageResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticChart;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticItem;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticLine;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticListResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticOverview;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticOverviewItem;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticPoint;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticRecordItem;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticRecordResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticResult;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.AppStatisticItem;
import com.unicomai.wanwu.api.app.dto.AppStatisticListResult;
import com.unicomai.wanwu.api.app.dto.AppStatisticOverview;
import com.unicomai.wanwu.api.app.dto.AppStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.AppStatisticResult;
import com.unicomai.wanwu.api.app.dto.ModelStatisticItem;
import com.unicomai.wanwu.api.app.dto.ModelStatisticListResult;
import com.unicomai.wanwu.api.app.dto.ModelStatisticOverview;
import com.unicomai.wanwu.api.app.dto.ModelStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.ModelStatisticResult;
import com.unicomai.wanwu.api.app.dto.StatisticChart;
import com.unicomai.wanwu.api.app.dto.StatisticLine;
import com.unicomai.wanwu.api.app.dto.StatisticOverviewItem;
import com.unicomai.wanwu.api.app.dto.StatisticPoint;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuStatisticApiController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";
    private static final String ORG_NAME = "Default Organization";

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    @Autowired(required = false)
    private OpenApiUsageMeter usageMeter = new OpenApiUsageMeter();

    public WanwuStatisticApiController() {
    }

    public WanwuStatisticApiController(AppService appService, ModelService modelService) {
        this(appService, modelService, new OpenApiUsageMeter());
    }

    public WanwuStatisticApiController(AppService appService, ModelService modelService, OpenApiUsageMeter usageMeter) {
        this.appService = appService;
        this.modelService = modelService;
        this.usageMeter = usageMeter == null ? new OpenApiUsageMeter() : usageMeter;
    }

    @GetMapping("/statistic/app/select")
    public FrontendResponse<Map<String, Object>> selectApps(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "appType", required = false, defaultValue = "agent") String appType) {
        ApplicationListResult result = listApplications(authorization, appType);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : safeList(result.getList())) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("appId", value(item, "appId"));
            row.put("name", defaultIfBlank(value(item, "name"), value(item, "appName")));
            row.put("appType", defaultIfBlank(value(item, "appType"), appType));
            row.put("avatar", avatar(item.get("avatar")));
            list.add(row);
        }
        return FrontendResponse.ok(page(list, list.size(), 1, list.size()));
    }

    @GetMapping("/statistic/app")
    public FrontendResponse<Map<String, Object>> appStatistic(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "appType", required = false, defaultValue = "agent") String appType,
            @RequestParam(value = "apps", required = false) String apps) {
        UserContext ctx = userContext(authorization);
        DateRange range = dateRange(startDate, endDate);
        AppStatisticResult persistent = persistentAppStatistic(ctx, range, split(apps), appType);
        if (persistent != null) {
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("overview", appOverview(persistent.getOverview()));
            Map<String, Object> trend = new LinkedHashMap<String, Object>();
            trend.put("callTrend", statisticChart(persistent.getTrend() == null ? null : persistent.getTrend().getCallTrend()));
            data.put("trend", trend);
            return FrontendResponse.ok(data);
        }
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("overview", overview("callCount", "callFailure", "avgStreamCosts", "avgNonStreamCosts",
                "streamCount", "nonStreamCount"));
        Map<String, Object> trend = new LinkedHashMap<String, Object>();
        trend.put("callTrend", chart("App Calls", dates(startDate, endDate), "Total Calls", "Web Calls", "OpenAPI Calls"));
        data.put("trend", trend);
        return FrontendResponse.ok(data);
    }

    @GetMapping("/statistic/app/list")
    public FrontendResponse<Map<String, Object>> appStatisticList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "appType", required = false, defaultValue = "agent") String appType,
            @RequestParam(value = "apps", required = false) String apps,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        List<String> selected = split(apps);
        UserContext ctx = userContext(authorization);
        DateRange range = dateRange(startDate, endDate);
        AppStatisticListResult persistent = persistentAppStatisticList(ctx, range, selected, appType, pageNo, pageSize);
        if (persistent != null) {
            Map<String, Map<String, Object>> appMap = appMap(authorization, appType);
            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            for (AppStatisticItem item : safeAppStatisticItems(persistent.getList())) {
                Map<String, Object> source = appMap.get(item.getAppId());
                Map<String, Object> row = metricRow();
                row.put("appId", item.getAppId());
                row.put("appType", defaultIfBlank(item.getAppType(), appType));
                row.put("appName", source == null
                        ? item.getAppId()
                        : defaultIfBlank(value(source, "appName"), value(source, "name")));
                row.put("orgName", ORG_NAME);
                row.put("callCount", item.getCallCount());
                row.put("callFailure", item.getCallFailure());
                row.put("failureRate", item.getFailureRate());
                row.put("avgStreamCosts", item.getAvgStreamCosts());
                row.put("avgNonStreamCosts", item.getAvgNonStreamCosts());
                row.put("streamCount", item.getStreamCount());
                row.put("nonStreamCount", item.getNonStreamCount());
                rows.add(row);
            }
            return FrontendResponse.ok(page(rows, persistent.getTotal(), persistent.getPageNo(), persistent.getPageSize()));
        }
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        ApplicationListResult result = listApplications(authorization, appType);
        for (Map<String, Object> item : safeList(result.getList())) {
            String appId = value(item, "appId");
            if (!selected.isEmpty() && !selected.contains(appId)) {
                continue;
            }
            Map<String, Object> row = metricRow();
            row.put("appId", appId);
            row.put("appType", defaultIfBlank(value(item, "appType"), appType));
            row.put("appName", defaultIfBlank(value(item, "appName"), value(item, "name")));
            row.put("orgName", ORG_NAME);
            rows.add(row);
        }
        return FrontendResponse.ok(paged(rows, pageNo, pageSize));
    }

    @GetMapping("/statistic/app/export")
    public ResponseEntity<byte[]> exportAppStatistic() {
        return csv("app-statistic.csv", "appName,appType,callCount,callFailure\n");
    }

    @GetMapping("/statistic/model")
    public FrontendResponse<Map<String, Object>> modelStatistic(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "modelType", required = false, defaultValue = "llm") String modelType,
            @RequestParam(value = "models", required = false) String models) {
        UserContext ctx = userContext(authorization);
        DateRange range = dateRange(startDate, endDate);
        ModelStatisticResult persistent = persistentModelStatistic(ctx, range, split(models), modelType);
        if (persistent != null) {
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("overview", modelOverview(persistent.getOverview()));
            Map<String, Object> trend = new LinkedHashMap<String, Object>();
            trend.put("modelCalls", statisticChart(persistent.getTrend() == null ? null : persistent.getTrend().getModelCalls()));
            trend.put("tokensUsage", statisticChart(persistent.getTrend() == null ? null : persistent.getTrend().getTokensUsage()));
            data.put("trend", trend);
            return FrontendResponse.ok(data);
        }
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("overview", overview("totalTokensTotal", "promptTokensTotal", "completionTokensTotal",
                "avgCosts", "callCountTotal", "callFailureTotal", "avgFirstTokenLatency"));
        Map<String, Object> trend = new LinkedHashMap<String, Object>();
        List<String> days = dates(startDate, endDate);
        trend.put("modelCalls", chart("Model Calls", days, "Total Calls", "Failures"));
        trend.put("tokensUsage", chart("Token Usage", days, "Prompt Tokens", "Completion Tokens", "Total Tokens"));
        data.put("trend", trend);
        return FrontendResponse.ok(data);
    }

    @GetMapping("/statistic/model/list")
    public FrontendResponse<Map<String, Object>> modelStatisticList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "modelType", required = false, defaultValue = "llm") String modelType,
            @RequestParam(value = "models", required = false) String models,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        List<String> selected = split(models);
        UserContext ctx = userContext(authorization);
        DateRange range = dateRange(startDate, endDate);
        ModelStatisticListResult persistent = persistentModelStatisticList(ctx, range, selected, modelType, pageNo, pageSize);
        if (persistent != null) {
            Map<String, ModelInfo> modelMap = modelMap(authorization, modelType);
            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            for (ModelStatisticItem item : safeModelStatisticItems(persistent.getList())) {
                ModelInfo info = modelMap.get(item.getModelId());
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                row.put("uuid", info == null ? item.getModelId() : defaultIfBlank(info.getUuid(), item.getModelId()));
                row.put("modelId", item.getModelId());
                row.put("model", info == null
                        ? defaultIfBlank(item.getModel(), item.getModelId())
                        : defaultIfBlank(info.getDisplayName(), info.getModel()));
                row.put("provider", info == null ? defaultIfBlank(item.getProvider(), "") : defaultIfBlank(info.getProvider(), ""));
                row.put("orgName", ORG_NAME);
                row.put("callCount", item.getCallCount());
                row.put("callFailure", item.getCallFailure());
                row.put("failureRate", item.getFailureRate());
                row.put("promptTokens", item.getPromptTokens());
                row.put("completionTokens", item.getCompletionTokens());
                row.put("totalTokens", item.getTotalTokens());
                row.put("avgCosts", item.getAvgCosts());
                row.put("avgFirstTokenLatency", item.getAvgFirstTokenLatency());
                rows.add(row);
            }
            return FrontendResponse.ok(page(rows, persistent.getTotal(), persistent.getPageNo(), persistent.getPageSize()));
        }
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        ModelListResult result = listModels(authorization, modelType);
        for (ModelInfo item : safeModels(result.getList())) {
            if (!selected.isEmpty() && !selected.contains(item.getModelId())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("uuid", defaultIfBlank(item.getUuid(), item.getModelId()));
            row.put("modelId", item.getModelId());
            row.put("model", defaultIfBlank(item.getDisplayName(), item.getModel()));
            row.put("provider", defaultIfBlank(item.getProvider(), ""));
            row.put("orgName", ORG_NAME);
            row.put("callCount", 0);
            row.put("callFailure", 0);
            row.put("failureRate", 0);
            row.put("promptTokens", 0);
            row.put("completionTokens", 0);
            row.put("totalTokens", 0);
            row.put("avgCosts", 0);
            row.put("avgFirstTokenLatency", 0);
            rows.add(row);
        }
        return FrontendResponse.ok(paged(rows, pageNo, pageSize));
    }

    @GetMapping("/statistic/model/export")
    public ResponseEntity<byte[]> exportModelStatistic() {
        return csv("model-statistic.csv", "model,provider,callCount,callFailure,totalTokens\n");
    }

    @GetMapping("/statistic/api/select")
    public FrontendResponse<Map<String, Object>> selectApiKeys(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        ApiKeyPageResult result = listApiKeys(authorization);
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (ApiKeyInfo item : safeApiKeys(result.getList())) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("keyId", item.getKeyId());
            row.put("name", defaultIfBlank(item.getName(), item.getKeyId()));
            row.put("apiKey", defaultIfBlank(item.getKey(), ""));
            rows.add(row);
        }
        return FrontendResponse.ok(page(rows, rows.size(), 1, rows.size()));
    }

    @GetMapping("/statistic/api/routes")
    public FrontendResponse<Map<String, Object>> apiRoutes() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        addRoute(rows, "POST", "/service/api/openapi/v1/agent/chat");
        addRoute(rows, "POST", "/service/api/openapi/v1/rag/chat");
        addRoute(rows, "POST", "/service/api/openapi/v1/workflow/run");
        addRoute(rows, "POST", "/service/api/openapi/v1/chatflow/chat");
        addRoute(rows, "POST", "/service/api/openapi/v1/knowledge/hit");
        addRoute(rows, "GET", "/service/api/openapi/v1/model/list");
        for (String methodPath : usageMeter.methodPaths()) {
            int splitAt = methodPath.indexOf('-');
            if (splitAt > 0 && splitAt + 1 < methodPath.length()) {
                addRoute(rows, methodPath.substring(0, splitAt), methodPath.substring(splitAt + 1));
            }
        }
        return FrontendResponse.ok(page(rows, rows.size(), 1, rows.size()));
    }

    @PostMapping("/statistic/api")
    public FrontendResponse<Map<String, Object>> apiStatistic(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        DateRange range = dateRange(request);
        List<String> apiKeyIds = apiKeyIds(request);
        List<String> methodPaths = methodPaths(request);
        ApiKeyStatisticResult persistent = persistentApiStatistic(ctx, range, apiKeyIds, methodPaths);
        if (persistent != null) {
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("overview", apiOverview(persistent.getOverview()));
            Map<String, Object> trend = new LinkedHashMap<String, Object>();
            trend.put("apiCalls", apiCallChart(persistent.getTrend() == null ? null : persistent.getTrend().getApiCalls()));
            data.put("trend", trend);
            return FrontendResponse.ok(data);
        }
        OpenApiUsageMeter.Aggregate current = usageMeter.total(
                ctx.userId, ctx.orgId, range.startDate, range.endDate, apiKeyIds, methodPaths);
        OpenApiUsageMeter.Aggregate previous = usageMeter.total(
                ctx.userId, ctx.orgId, range.previousStartDate, range.previousEndDate, apiKeyIds, methodPaths);
        List<OpenApiUsageMeter.Record> records = usageMeter.records(
                ctx.userId, ctx.orgId, range.startDate, range.endDate, apiKeyIds, methodPaths);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("overview", apiOverview(current, previous));
        Map<String, Object> trend = new LinkedHashMap<String, Object>();
        trend.put("apiCalls", apiCallChart(records, dates(value(request, "startDate"), value(request, "endDate"))));
        data.put("trend", trend);
        return FrontendResponse.ok(data);
    }

    @PostMapping("/statistic/api/list")
    public FrontendResponse<Map<String, Object>> apiStatisticList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        DateRange range = dateRange(request);
        Map<String, ApiKeyInfo> apiKeyMap = apiKeyMap(authorization);
        ApiKeyStatisticListResult persistent = persistentApiStatisticList(
                ctx, range, apiKeyIds(request), methodPaths(request), pageNo(request), pageSize(request));
        if (persistent != null) {
            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            for (ApiKeyStatisticItem item : safeStatisticItems(persistent.getList())) {
                Map<String, Object> row = metricRow();
                ApiKeyInfo info = apiKeyMap.get(item.getApiKeyId());
                row.put("name", apiKeyName(info, item.getApiKeyId()));
                row.put("apiKey", apiKeyValue(info));
                row.put("methodPath", item.getMethodPath());
                row.put("callCount", item.getCallCount());
                row.put("callFailure", item.getCallFailure());
                row.put("failureRate", item.getCallCount() == 0L ? 0D : ((double) item.getCallFailure() / (double) item.getCallCount()) * 100D);
                row.put("avgStreamCosts", item.getAvgStreamCosts());
                row.put("avgNonStreamCosts", item.getAvgNonStreamCosts());
                row.put("streamCount", item.getStreamCount());
                row.put("nonStreamCount", item.getNonStreamCount());
                rows.add(row);
            }
            return FrontendResponse.ok(page(rows, persistent.getTotal(), persistent.getPageNo(), persistent.getPageSize()));
        }
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (OpenApiUsageMeter.Aggregate item : usageMeter.aggregates(
                ctx.userId, ctx.orgId, range.startDate, range.endDate, apiKeyIds(request), methodPaths(request))) {
            Map<String, Object> row = metricRow();
            ApiKeyInfo info = apiKeyMap.get(item.getApiKeyId());
            row.put("name", apiKeyName(info, item.getApiKeyId()));
            row.put("apiKey", apiKeyValue(info));
            row.put("methodPath", item.getMethodPath());
            row.put("callCount", item.getCallCount());
            row.put("callFailure", item.getCallFailure());
            row.put("failureRate", item.failureRate());
            row.put("avgStreamCosts", item.avgStreamCosts());
            row.put("avgNonStreamCosts", item.avgNonStreamCosts());
            row.put("streamCount", item.getStreamCount());
            row.put("nonStreamCount", item.getNonStreamCount());
            rows.add(row);
        }
        return FrontendResponse.ok(paged(rows, pageNo(request), pageSize(request)));
    }

    @PostMapping("/statistic/api/record")
    public FrontendResponse<Map<String, Object>> apiStatisticRecord(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        DateRange range = dateRange(request);
        Map<String, ApiKeyInfo> apiKeyMap = apiKeyMap(authorization);
        ApiKeyStatisticRecordResult persistent = persistentApiStatisticRecords(
                ctx, range, apiKeyIds(request), methodPaths(request), pageNo(request), pageSize(request));
        if (persistent != null) {
            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            for (ApiKeyStatisticRecordItem item : safeStatisticRecords(persistent.getList())) {
                ApiKeyInfo info = apiKeyMap.get(item.getApiKeyId());
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                row.put("name", apiKeyName(info, item.getApiKeyId()));
                row.put("apiKey", apiKeyValue(info));
                row.put("methodPath", item.getMethodPath());
                row.put("callTime", formatTime(item.getCallTime()));
                row.put("responseStatus", item.getResponseStatus());
                row.put("streamCosts", item.getStreamCosts());
                row.put("nonStreamCosts", item.getNonStreamCosts());
                row.put("requestBody", item.getRequestBody());
                row.put("responseBody", item.getResponseBody());
                rows.add(row);
            }
            return FrontendResponse.ok(page(rows, persistent.getTotal(), persistent.getPageNo(), persistent.getPageSize()));
        }
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (OpenApiUsageMeter.Record item : usageMeter.records(
                ctx.userId, ctx.orgId, range.startDate, range.endDate, apiKeyIds(request), methodPaths(request))) {
            ApiKeyInfo info = apiKeyMap.get(item.getApiKeyId());
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("name", apiKeyName(info, item.getApiKeyId()));
            row.put("apiKey", apiKeyValue(info));
            row.put("methodPath", item.getMethodPath());
            row.put("callTime", formatTime(item.getCallTime()));
            row.put("responseStatus", item.getResponseStatus());
            row.put("streamCosts", item.getStreamCosts());
            row.put("nonStreamCosts", item.getNonStreamCosts());
            row.put("requestBody", item.getRequestBody());
            row.put("responseBody", item.getResponseBody());
            rows.add(row);
        }
        return FrontendResponse.ok(paged(rows, pageNo(request), pageSize(request)));
    }

    @PostMapping("/statistic/api/{type}/export")
    public ResponseEntity<byte[]> exportApiStatistic(@PathVariable("type") String type) {
        return csv("api-" + type + "-statistic.csv", "name,apiKey,methodPath,callCount,callFailure\n");
    }

    private ApplicationListResult listApplications(String authorization, String appType) {
        UserContext ctx = userContext(authorization);
        return appService.listApplications(new ApplicationListQuery(
                defaultIfBlank(appType, "agent"),
                "",
                ctx.userId,
                ctx.orgId));
    }

    private ModelListResult listModels(String authorization, String modelType) {
        UserContext ctx = userContext(authorization);
        return modelService.listModels(new ModelListQuery(
                ctx.userId,
                ctx.orgId,
                defaultIfBlank(modelType, "llm"),
                "",
                "",
                "",
                ""));
    }

    private ApiKeyPageResult listApiKeys(String authorization) {
        UserContext ctx = userContext(authorization);
        return appService.listApiKeys(new ApiKeyListQuery(1, 1000, ctx.userId, ctx.orgId));
    }

    private List<Map<String, Object>> apiRows(String authorization, Map<String, Object> request) {
        List<String> selectedKeys = splitList(request == null ? null : request.get("apiKeyIds"));
        List<String> selectedRoutes = splitList(request == null ? null : request.get("methodPaths"));
        if (selectedKeys.contains("ALL")) {
            selectedKeys = Collections.emptyList();
        }
        if (selectedRoutes.isEmpty()) {
            selectedRoutes = Collections.singletonList("POST-/assistant/stream");
        }

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (ApiKeyInfo apiKey : safeApiKeys(listApiKeys(authorization).getList())) {
            if (!selectedKeys.isEmpty() && !selectedKeys.contains(apiKey.getKeyId())) {
                continue;
            }
            for (String methodPath : selectedRoutes) {
                Map<String, Object> row = metricRow();
                row.put("name", defaultIfBlank(apiKey.getName(), apiKey.getKeyId()));
                row.put("apiKey", defaultIfBlank(apiKey.getKey(), ""));
                row.put("methodPath", methodPath);
                rows.add(row);
            }
        }
        return rows;
    }

    private void addRoute(List<Map<String, Object>> rows, String method, String path) {
        String routeKey = method + "-" + path;
        for (Map<String, Object> row : rows) {
            if (routeKey.equals(row.get("method") + "-" + row.get("path"))) {
                return;
            }
        }
        rows.add(route(method, path));
    }

    private Map<String, Object> apiOverview(OpenApiUsageMeter.Aggregate current,
                                            OpenApiUsageMeter.Aggregate previous) {
        Map<String, Object> overview = new LinkedHashMap<String, Object>();
        overview.put("callCount", overviewItem(current.getCallCount(), previous.getCallCount()));
        overview.put("callFailure", overviewItem(current.getCallFailure(), previous.getCallFailure()));
        overview.put("avgStreamCosts", overviewItem(current.avgStreamCosts(), previous.avgStreamCosts()));
        overview.put("avgNonStreamCosts", overviewItem(current.avgNonStreamCosts(), previous.avgNonStreamCosts()));
        overview.put("streamCount", overviewItem(current.getStreamCount(), previous.getStreamCount()));
        overview.put("nonStreamCount", overviewItem(current.getNonStreamCount(), previous.getNonStreamCount()));
        return overview;
    }

    private Map<String, Object> apiOverview(ApiKeyStatisticOverview source) {
        Map<String, Object> overview = new LinkedHashMap<String, Object>();
        overview.put("callCount", overviewItem(source == null ? null : source.getCallCount()));
        overview.put("callFailure", overviewItem(source == null ? null : source.getCallFailure()));
        overview.put("avgStreamCosts", overviewItem(source == null ? null : source.getAvgStreamCosts()));
        overview.put("avgNonStreamCosts", overviewItem(source == null ? null : source.getAvgNonStreamCosts()));
        overview.put("streamCount", overviewItem(source == null ? null : source.getStreamCount()));
        overview.put("nonStreamCount", overviewItem(source == null ? null : source.getNonStreamCount()));
        return overview;
    }

    private Map<String, Object> appOverview(AppStatisticOverview source) {
        Map<String, Object> overview = new LinkedHashMap<String, Object>();
        overview.put("callCount", overviewItem(source == null ? null : source.getCallCount()));
        overview.put("callFailure", overviewItem(source == null ? null : source.getCallFailure()));
        overview.put("avgStreamCosts", overviewItem(source == null ? null : source.getAvgStreamCosts()));
        overview.put("avgNonStreamCosts", overviewItem(source == null ? null : source.getAvgNonStreamCosts()));
        overview.put("streamCount", overviewItem(source == null ? null : source.getStreamCount()));
        overview.put("nonStreamCount", overviewItem(source == null ? null : source.getNonStreamCount()));
        return overview;
    }

    private Map<String, Object> modelOverview(ModelStatisticOverview source) {
        Map<String, Object> overview = new LinkedHashMap<String, Object>();
        overview.put("totalTokensTotal", overviewItem(source == null ? null : source.getTotalTokens()));
        overview.put("promptTokensTotal", overviewItem(source == null ? null : source.getPromptTokens()));
        overview.put("completionTokensTotal", overviewItem(source == null ? null : source.getCompletionTokens()));
        overview.put("avgCosts", overviewItem(source == null ? null : source.getAvgCosts()));
        overview.put("callCountTotal", overviewItem(source == null ? null : source.getCallCount()));
        overview.put("callFailureTotal", overviewItem(source == null ? null : source.getCallFailure()));
        overview.put("avgFirstTokenLatency", overviewItem(source == null ? null : source.getAvgFirstTokenLatency()));
        return overview;
    }

    private Map<String, Object> overviewItem(double current, double previous) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("value", current);
        item.put("periodOverPeriod", previous == 0D ? -9999 : ((current - previous) / previous) * 100D);
        return item;
    }

    private Map<String, Object> overviewItem(ApiKeyStatisticOverviewItem source) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("value", source == null ? 0D : source.getValue());
        item.put("periodOverPeriod", source == null ? -9999D : source.getPeriodOverPeriod());
        return item;
    }

    private Map<String, Object> overviewItem(StatisticOverviewItem source) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("value", source == null ? 0D : source.getValue());
        item.put("periodOverPeriod", source == null ? -9999D : source.getPeriodOverPeriod());
        return item;
    }

    private Map<String, Object> apiCallChart(List<OpenApiUsageMeter.Record> records, List<String> days) {
        Map<String, int[]> counters = new LinkedHashMap<String, int[]>();
        for (String day : days) {
            counters.put(day, new int[]{0, 0, 0});
        }
        for (OpenApiUsageMeter.Record record : records) {
            String day = record.getDate().toString();
            int[] row = counters.get(day);
            if (row == null) {
                continue;
            }
            row[0]++;
            if ("200".equals(record.getResponseStatus())) {
                row[1]++;
            } else {
                row[2]++;
            }
        }
        Map<String, Object> chart = new LinkedHashMap<String, Object>();
        chart.put("tableName", "app_statistic_api_key_call_trend");
        List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>();
        lines.add(apiCallLine("app_statistic_api_call_count_total", days, counters, 0));
        lines.add(apiCallLine("app_statistic_api_call_success", days, counters, 1));
        lines.add(apiCallLine("app_statistic_api_call_failure", days, counters, 2));
        chart.put("lines", lines);
        return chart;
    }

    private Map<String, Object> apiCallLine(String lineName, List<String> days, Map<String, int[]> counters, int index) {
        Map<String, Object> line = new LinkedHashMap<String, Object>();
        line.put("lineName", lineName);
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (String day : days) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("key", day);
            int[] row = counters.get(day);
            item.put("value", row == null ? 0 : row[index]);
            items.add(item);
        }
        line.put("items", items);
        return line;
    }

    private Map<String, Object> apiCallChart(ApiKeyStatisticChart source) {
        Map<String, Object> chart = new LinkedHashMap<String, Object>();
        chart.put("tableName", source == null ? "app_statistic_api_key_call_trend" : source.getTableName());
        List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>();
        if (source != null && source.getLines() != null) {
            for (ApiKeyStatisticLine sourceLine : source.getLines()) {
                Map<String, Object> line = new LinkedHashMap<String, Object>();
                line.put("lineName", sourceLine.getLineName());
                List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
                if (sourceLine.getItems() != null) {
                    for (ApiKeyStatisticPoint sourceItem : sourceLine.getItems()) {
                        Map<String, Object> item = new LinkedHashMap<String, Object>();
                        item.put("key", sourceItem.getKey());
                        item.put("value", sourceItem.getValue());
                        items.add(item);
                    }
                }
                line.put("items", items);
                lines.add(line);
            }
        }
        chart.put("lines", lines);
        return chart;
    }

    private Map<String, Object> statisticChart(StatisticChart source) {
        Map<String, Object> chart = new LinkedHashMap<String, Object>();
        chart.put("tableName", source == null ? "" : source.getTableName());
        List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>();
        if (source != null && source.getLines() != null) {
            for (StatisticLine sourceLine : source.getLines()) {
                Map<String, Object> line = new LinkedHashMap<String, Object>();
                line.put("lineName", sourceLine.getLineName());
                List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
                if (sourceLine.getItems() != null) {
                    for (StatisticPoint sourceItem : sourceLine.getItems()) {
                        Map<String, Object> item = new LinkedHashMap<String, Object>();
                        item.put("key", sourceItem.getKey());
                        item.put("value", sourceItem.getValue());
                        items.add(item);
                    }
                }
                line.put("items", items);
                lines.add(line);
            }
        }
        chart.put("lines", lines);
        return chart;
    }

    private List<String> apiKeyIds(Map<String, Object> request) {
        List<String> keys = splitList(request == null ? null : request.get("apiKeyIds"));
        if (keys.contains("ALL")) {
            return Collections.emptyList();
        }
        return keys;
    }

    private List<String> methodPaths(Map<String, Object> request) {
        return splitList(request == null ? null : request.get("methodPaths"));
    }

    private DateRange dateRange(Map<String, Object> request) {
        return dateRange(value(request, "startDate"), value(request, "endDate"));
    }

    private DateRange dateRange(String startDateValue, String endDateValue) {
        LocalDate end = parseDate(endDateValue, LocalDate.now());
        LocalDate start = parseDate(startDateValue, end.minusDays(6));
        if (start.isAfter(end)) {
            start = end;
        }
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        LocalDate previousEnd = start.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(Math.max(0L, days - 1));
        return new DateRange(start, end, previousStart, previousEnd);
    }

    private Map<String, ApiKeyInfo> apiKeyMap(String authorization) {
        Map<String, ApiKeyInfo> result = new LinkedHashMap<String, ApiKeyInfo>();
        for (ApiKeyInfo item : safeApiKeys(listApiKeys(authorization).getList())) {
            result.put(item.getKeyId(), item);
        }
        return result;
    }

    private Map<String, Map<String, Object>> appMap(String authorization, String appType) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<String, Map<String, Object>>();
        for (Map<String, Object> item : safeList(listApplications(authorization, appType).getList())) {
            result.put(value(item, "appId"), item);
        }
        return result;
    }

    private Map<String, ModelInfo> modelMap(String authorization, String modelType) {
        Map<String, ModelInfo> result = new LinkedHashMap<String, ModelInfo>();
        for (ModelInfo item : safeModels(listModels(authorization, modelType).getList())) {
            result.put(item.getModelId(), item);
        }
        return result;
    }

    private String apiKeyName(ApiKeyInfo info, String apiKeyId) {
        if (info == null) {
            return "Deleted API Key";
        }
        return defaultIfBlank(info.getName(), apiKeyId);
    }

    private String apiKeyValue(ApiKeyInfo info) {
        return info == null ? "Deleted API Key" : defaultIfBlank(info.getKey(), "");
    }

    private AppStatisticResult persistentAppStatistic(UserContext ctx,
                                                      DateRange range,
                                                      List<String> appIds,
                                                      String appType) {
        try {
            return appService == null ? null : appService.getAppStatistic(
                    new AppStatisticPageQuery(
                            ctx.userId,
                            ctx.orgId,
                            range.startDate.toString(),
                            range.endDate.toString(),
                            appIds,
                            appType,
                            1,
                            10));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private AppStatisticListResult persistentAppStatisticList(UserContext ctx,
                                                              DateRange range,
                                                              List<String> appIds,
                                                              String appType,
                                                              int pageNo,
                                                              int pageSize) {
        try {
            return appService == null ? null : appService.listAppStatistics(
                    new AppStatisticPageQuery(
                            ctx.userId,
                            ctx.orgId,
                            range.startDate.toString(),
                            range.endDate.toString(),
                            appIds,
                            appType,
                            pageNo,
                            pageSize));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private ModelStatisticResult persistentModelStatistic(UserContext ctx,
                                                          DateRange range,
                                                          List<String> modelIds,
                                                          String modelType) {
        try {
            return appService == null ? null : appService.getModelStatistic(
                    new ModelStatisticPageQuery(
                            ctx.userId,
                            ctx.orgId,
                            range.startDate.toString(),
                            range.endDate.toString(),
                            modelIds,
                            modelType,
                            1,
                            10));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private ModelStatisticListResult persistentModelStatisticList(UserContext ctx,
                                                                  DateRange range,
                                                                  List<String> modelIds,
                                                                  String modelType,
                                                                  int pageNo,
                                                                  int pageSize) {
        try {
            return appService == null ? null : appService.listModelStatistics(
                    new ModelStatisticPageQuery(
                            ctx.userId,
                            ctx.orgId,
                            range.startDate.toString(),
                            range.endDate.toString(),
                            modelIds,
                            modelType,
                            pageNo,
                            pageSize));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private ApiKeyStatisticResult persistentApiStatistic(UserContext ctx,
                                                        DateRange range,
                                                        List<String> apiKeyIds,
                                                        List<String> methodPaths) {
        try {
            return appService == null ? null : appService.getApiKeyStatistic(apiQuery(ctx, range, apiKeyIds, methodPaths));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private ApiKeyStatisticListResult persistentApiStatisticList(UserContext ctx,
                                                                 DateRange range,
                                                                 List<String> apiKeyIds,
                                                                 List<String> methodPaths,
                                                                 int pageNo,
                                                                 int pageSize) {
        try {
            return appService == null ? null : appService.listApiKeyStatistics(
                    apiPageQuery(ctx, range, apiKeyIds, methodPaths, pageNo, pageSize));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private ApiKeyStatisticRecordResult persistentApiStatisticRecords(UserContext ctx,
                                                                      DateRange range,
                                                                      List<String> apiKeyIds,
                                                                      List<String> methodPaths,
                                                                      int pageNo,
                                                                      int pageSize) {
        try {
            return appService == null ? null : appService.listApiKeyStatisticRecords(
                    apiPageQuery(ctx, range, apiKeyIds, methodPaths, pageNo, pageSize));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private ApiKeyStatisticPageQuery apiPageQuery(UserContext ctx,
                                                  DateRange range,
                                                  List<String> apiKeyIds,
                                                  List<String> methodPaths,
                                                  int pageNo,
                                                  int pageSize) {
        return new ApiKeyStatisticPageQuery(
                ctx.userId,
                ctx.orgId,
                range.startDate.toString(),
                range.endDate.toString(),
                apiKeyIds,
                methodPaths,
                pageNo,
                pageSize);
    }

    private ApiKeyStatisticPageQuery apiQuery(UserContext ctx,
                                              DateRange range,
                                              List<String> apiKeyIds,
                                              List<String> methodPaths) {
        return apiPageQuery(ctx, range, apiKeyIds, methodPaths, 1, 10);
    }

    private String formatTime(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DATE_TIME_FORMATTER);
    }

    private Map<String, Object> overview(String... keys) {
        Map<String, Object> overview = new LinkedHashMap<String, Object>();
        for (String key : keys) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("value", 0);
            item.put("periodOverPeriod", -9999);
            overview.put(key, item);
        }
        return overview;
    }

    private Map<String, Object> chart(String tableName, List<String> dates, String... lineNames) {
        Map<String, Object> chart = new LinkedHashMap<String, Object>();
        chart.put("tableName", tableName);
        List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>();
        for (String lineName : lineNames) {
            Map<String, Object> line = new LinkedHashMap<String, Object>();
            line.put("lineName", lineName);
            List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
            for (String date : dates) {
                Map<String, Object> item = new LinkedHashMap<String, Object>();
                item.put("key", date);
                item.put("value", 0);
                items.add(item);
            }
            line.put("items", items);
            lines.add(line);
        }
        chart.put("lines", lines);
        return chart;
    }

    private List<String> dates(String startDate, String endDate) {
        LocalDate end = parseDate(endDate, LocalDate.now());
        LocalDate start = parseDate(startDate, end.minusDays(6));
        if (start.isAfter(end)) {
            start = end;
        }
        List<String> result = new ArrayList<String>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end) && result.size() < 31) {
            result.add(cursor.toString());
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    private LocalDate parseDate(String value, LocalDate fallback) {
        try {
            return value == null || value.trim().isEmpty() ? fallback : LocalDate.parse(value.trim());
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private Map<String, Object> metricRow() {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("callCount", 0);
        row.put("callFailure", 0);
        row.put("failureRate", 0);
        row.put("avgStreamCosts", 0);
        row.put("avgNonStreamCosts", 0);
        row.put("streamCount", 0);
        row.put("nonStreamCount", 0);
        return row;
    }

    private Map<String, Object> route(String method, String path) {
        Map<String, Object> route = new LinkedHashMap<String, Object>();
        route.put("method", method);
        route.put("path", path);
        return route;
    }

    private Map<String, Object> paged(List<Map<String, Object>> rows, int pageNo, int pageSize) {
        int safePageNo = pageNo <= 0 ? 1 : pageNo;
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int from = Math.min((safePageNo - 1) * safePageSize, rows.size());
        int to = Math.min(from + safePageSize, rows.size());
        return page(new ArrayList<Map<String, Object>>(rows.subList(from, to)), rows.size(), safePageNo, safePageSize);
    }

    private Map<String, Object> page(List<Map<String, Object>> rows, long total, int pageNo, int pageSize) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("list", rows);
        result.put("total", total);
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> safeList(List<Map<String, Object>> rows) {
        return rows == null ? Collections.<Map<String, Object>>emptyList() : rows;
    }

    private List<ModelInfo> safeModels(List<ModelInfo> rows) {
        return rows == null ? Collections.<ModelInfo>emptyList() : rows;
    }

    private List<ApiKeyInfo> safeApiKeys(List<ApiKeyInfo> rows) {
        return rows == null ? Collections.<ApiKeyInfo>emptyList() : rows;
    }

    private List<ApiKeyStatisticItem> safeStatisticItems(List<ApiKeyStatisticItem> rows) {
        return rows == null ? Collections.<ApiKeyStatisticItem>emptyList() : rows;
    }

    private List<ApiKeyStatisticRecordItem> safeStatisticRecords(List<ApiKeyStatisticRecordItem> rows) {
        return rows == null ? Collections.<ApiKeyStatisticRecordItem>emptyList() : rows;
    }

    private List<AppStatisticItem> safeAppStatisticItems(List<AppStatisticItem> rows) {
        return rows == null ? Collections.<AppStatisticItem>emptyList() : rows;
    }

    private List<ModelStatisticItem> safeModelStatisticItems(List<ModelStatisticItem> rows) {
        return rows == null ? Collections.<ModelStatisticItem>emptyList() : rows;
    }

    private List<String> split(String value) {
        List<String> result = new ArrayList<String>();
        if (value == null || value.trim().isEmpty()) {
            return result;
        }
        for (String item : value.split(",")) {
            if (!item.trim().isEmpty()) {
                result.add(item.trim());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> splitList(Object value) {
        if (value instanceof List) {
            List<String> result = new ArrayList<String>();
            for (Object item : (List<Object>) value) {
                if (item != null && !item.toString().trim().isEmpty()) {
                    result.add(item.toString().trim());
                }
            }
            return result;
        }
        return split(value == null ? "" : value.toString());
    }

    private int pageNo(Map<String, Object> request) {
        return number(request == null ? null : request.get("pageNo"), 1);
    }

    private int pageSize(Map<String, Object> request) {
        return number(request == null ? null : request.get("pageSize"), 10);
    }

    private int number(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value == null ? fallback : Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> avatar(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        Map<String, Object> avatar = new LinkedHashMap<String, Object>();
        avatar.put("path", "");
        return avatar;
    }

    private String value(Map<String, Object> map, String key) {
        if (map == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private UserContext userContext(String authorization) {
        if (authorization != null && authorization.contains("dev-token-app")) {
            return new UserContext(DEV_APP_USER_ID, DEV_ORG_ID);
        }
        return new UserContext(DEV_USER_ID, DEV_ORG_ID);
    }

    private ResponseEntity<byte[]> csv(String fileName, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    private static class UserContext {
        private final String userId;
        private final String orgId;

        private UserContext(String userId, String orgId) {
            this.userId = userId;
            this.orgId = orgId;
        }
    }

    private static class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final LocalDate previousStartDate;
        private final LocalDate previousEndDate;

        private DateRange(LocalDate startDate, LocalDate endDate,
                          LocalDate previousStartDate, LocalDate previousEndDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.previousStartDate = previousStartDate;
            this.previousEndDate = previousEndDate;
        }
    }
}
