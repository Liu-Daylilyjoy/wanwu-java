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
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticPoint;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticRecordItem;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticRecordResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticResult;
import com.unicomai.wanwu.api.app.dto.AppStatisticItem;
import com.unicomai.wanwu.api.app.dto.AppStatisticListResult;
import com.unicomai.wanwu.api.app.dto.AppStatisticOverview;
import com.unicomai.wanwu.api.app.dto.AppStatisticResult;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.ModelStatisticItem;
import com.unicomai.wanwu.api.app.dto.ModelStatisticListResult;
import com.unicomai.wanwu.api.app.dto.ModelStatisticOverview;
import com.unicomai.wanwu.api.app.dto.ModelStatisticResult;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.StatisticChart;
import com.unicomai.wanwu.api.app.dto.StatisticLine;
import com.unicomai.wanwu.api.app.dto.StatisticOverviewItem;
import com.unicomai.wanwu.api.app.dto.StatisticPoint;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.model.ModelService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuStatisticApiControllerTest {

    private final AppService appService = mock(AppService.class);
    private final ModelService modelService = mock(ModelService.class);
    private final OpenApiUsageMeter usageMeter = new OpenApiUsageMeter();

    @Test
    public void openApiCallsAreVisibleInApiKeyStatistics() throws Exception {
        RagChatResult ragResult = new RagChatResult();
        ragResult.setRagId("rag-stat-001");
        ragResult.setResponse("stat answer");
        when(appService.streamRagChat(any(RagChatCommand.class))).thenReturn(ragResult);
        when(appService.listApiKeys(any(ApiKeyListQuery.class)))
                .thenReturn(new ApiKeyPageResult(Collections.singletonList(devAdminApiKey()), 1, 1, 1000));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new WanwuOpenApiController(appService, modelService, null, new OpenApiChatflowSessionStore()),
                        new WanwuStatisticApiController(appService, modelService, usageMeter))
                .addFilters(new OpenApiUsageRecordFilter(usageMeter, appService))
                .build();

        mockMvc.perform(post("/service/api/openapi/v1/rag/chat")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uuid\":\"rag-stat-001\",\"query\":\"count me\",\"stream\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        String today = LocalDate.now().toString();
        String methodPath = "POST-/service/api/openapi/v1/rag/chat";
        String query = "{\"startDate\":\"" + today + "\",\"endDate\":\"" + today
                + "\",\"apiKeyIds\":[\"ALL\"],\"methodPaths\":[\"" + methodPath + "\"]}";

        mockMvc.perform(post("/user/api/v1/statistic/api")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overview.callCount.value").value(1.0))
                .andExpect(jsonPath("$.data.overview.streamCount.value").value(1.0))
                .andExpect(jsonPath("$.data.overview.nonStreamCount.value").value(0.0));

        mockMvc.perform(post("/user/api/v1/statistic/api/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].name").value("Development Admin Key"))
                .andExpect(jsonPath("$.data.list[0].apiKey").value("dev-token"))
                .andExpect(jsonPath("$.data.list[0].methodPath").value(methodPath))
                .andExpect(jsonPath("$.data.list[0].callCount").value(1))
                .andExpect(jsonPath("$.data.list[0].streamCount").value(1));

        mockMvc.perform(post("/user/api/v1/statistic/api/record")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].methodPath").value(methodPath))
                .andExpect(jsonPath("$.data.list[0].responseStatus").value("200"))
                .andExpect(jsonPath("$.data.list[0].requestBody").value(containsString("\"stream\":true")));
    }

    @Test
    public void apiKeyStatisticsUsePersistentAppServiceWhenAvailable() throws Exception {
        String methodPath = "GET-/service/api/openapi/v1/model/list";
        when(appService.listApiKeys(any(ApiKeyListQuery.class)))
                .thenReturn(new ApiKeyPageResult(Collections.singletonList(devAdminApiKey()), 1, 1, 1000));
        when(appService.getApiKeyStatistic(any())).thenReturn(persistentOverview(methodPath));
        when(appService.listApiKeyStatistics(any())).thenReturn(persistentList(methodPath));
        when(appService.listApiKeyStatisticRecords(any())).thenReturn(persistentRecords(methodPath));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new WanwuStatisticApiController(appService, modelService, new OpenApiUsageMeter()))
                .build();
        String query = "{\"startDate\":\"2026-06-29\",\"endDate\":\"2026-06-29\","
                + "\"apiKeyIds\":[\"dev-admin-key\"],\"methodPaths\":[\"" + methodPath + "\"],"
                + "\"pageNo\":1,\"pageSize\":10}";

        mockMvc.perform(post("/user/api/v1/statistic/api")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overview.callCount.value").value(7.0))
                .andExpect(jsonPath("$.data.trend.apiCalls.lines[0].items[0].value").value(7.0));

        mockMvc.perform(post("/user/api/v1/statistic/api/list")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].callCount").value(7));

        mockMvc.perform(post("/user/api/v1/statistic/api/record")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].requestBody").value("{\"ok\":true}"));
    }

    @Test
    public void appAndModelStatisticsUsePersistentAppServiceWhenAvailable() throws Exception {
        when(appService.getAppStatistic(any())).thenReturn(persistentAppOverview());
        when(appService.listAppStatistics(any())).thenReturn(persistentAppList());
        when(appService.listApplications(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.singletonList(appRow()), 1));
        when(appService.getModelStatistic(any())).thenReturn(persistentModelOverview());
        when(appService.listModelStatistics(any())).thenReturn(persistentModelList());
        when(modelService.listModels(any(ModelListQuery.class)))
                .thenReturn(new ModelListResult(Collections.singletonList(modelInfo()), 1));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new WanwuStatisticApiController(appService, modelService, new OpenApiUsageMeter()))
                .build();

        mockMvc.perform(get("/user/api/v1/statistic/app")
                        .header("Authorization", "Bearer dev-token")
                        .param("startDate", "2026-06-29")
                        .param("endDate", "2026-06-29")
                        .param("appType", "agent")
                        .param("apps", "agent-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overview.callCount.value").value(3.0))
                .andExpect(jsonPath("$.data.trend.callTrend.tableName").value("app_statistic_app_call_trend"))
                .andExpect(jsonPath("$.data.trend.callTrend.lines[1].items[0].value").value(2.0));

        mockMvc.perform(get("/user/api/v1/statistic/app/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("startDate", "2026-06-29")
                        .param("endDate", "2026-06-29")
                        .param("appType", "agent")
                        .param("apps", "agent-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].appName").value("Stats Agent"))
                .andExpect(jsonPath("$.data.list[0].callCount").value(3));

        mockMvc.perform(get("/user/api/v1/statistic/model")
                        .header("Authorization", "Bearer dev-token")
                        .param("startDate", "2026-06-29")
                        .param("endDate", "2026-06-29")
                        .param("modelType", "llm")
                        .param("models", "model-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overview.totalTokensTotal.value").value(42.0))
                .andExpect(jsonPath("$.data.overview.callCountTotal.value").value(2.0))
                .andExpect(jsonPath("$.data.trend.tokensUsage.tableName").value("app_statistic_model_tokens_usage_trend"));

        mockMvc.perform(get("/user/api/v1/statistic/model/list")
                        .header("Authorization", "Bearer dev-token")
                        .param("startDate", "2026-06-29")
                        .param("endDate", "2026-06-29")
                        .param("modelType", "llm")
                        .param("models", "model-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].model").value("Stats Model"))
                .andExpect(jsonPath("$.data.list[0].totalTokens").value(42));
    }

    private ApiKeyInfo devAdminApiKey() {
        ApiKeyInfo info = new ApiKeyInfo();
        info.setKeyId("dev-admin-key");
        info.setKey("dev-token");
        info.setName("Development Admin Key");
        info.setUserId("dev-admin");
        info.setOrgId("default-org");
        info.setStatus(Boolean.TRUE);
        return info;
    }

    private Map<String, Object> appRow() {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("appId", "agent-001");
        row.put("appType", "agent");
        row.put("appName", "Stats Agent");
        row.put("name", "Stats Agent");
        return row;
    }

    private ModelInfo modelInfo() {
        ModelInfo info = new ModelInfo();
        info.setModelId("model-001");
        info.setUuid("model-001");
        info.setModel("stats-model");
        info.setDisplayName("Stats Model");
        info.setProvider("local");
        info.setModelType("llm");
        return info;
    }

    private AppStatisticResult persistentAppOverview() {
        AppStatisticOverview overview = new AppStatisticOverview();
        overview.setCallCount(new StatisticOverviewItem(3D, -9999D));
        overview.setCallFailure(new StatisticOverviewItem(1D, -9999D));
        overview.setStreamCount(new StatisticOverviewItem(2D, -9999D));
        overview.setNonStreamCount(new StatisticOverviewItem(1D, -9999D));
        overview.setAvgStreamCosts(new StatisticOverviewItem(11D, -9999D));
        overview.setAvgNonStreamCosts(new StatisticOverviewItem(5D, -9999D));
        AppStatisticResult result = new AppStatisticResult();
        result.setOverview(overview);
        result.getTrend().setCallTrend(statisticChart(
                "app_statistic_app_call_trend",
                "app_statistic_call_count_total",
                3D,
                "app_statistic_web_call_count",
                2D,
                "app_statistic_openapi_call_count",
                1D));
        return result;
    }

    private AppStatisticListResult persistentAppList() {
        AppStatisticItem item = new AppStatisticItem();
        item.setAppId("agent-001");
        item.setAppType("agent");
        item.setCallCount(3L);
        item.setCallFailure(1L);
        item.setFailureRate(33.3D);
        item.setStreamCount(2L);
        item.setNonStreamCount(1L);
        item.setAvgStreamCosts(11D);
        item.setAvgNonStreamCosts(5D);
        AppStatisticListResult result = new AppStatisticListResult();
        result.setList(Collections.singletonList(item));
        result.setTotal(1L);
        result.setPageNo(1);
        result.setPageSize(10);
        return result;
    }

    private ModelStatisticResult persistentModelOverview() {
        ModelStatisticOverview overview = new ModelStatisticOverview();
        overview.setTotalTokens(new StatisticOverviewItem(42D, -9999D));
        overview.setPromptTokens(new StatisticOverviewItem(20D, -9999D));
        overview.setCompletionTokens(new StatisticOverviewItem(22D, -9999D));
        overview.setCallCount(new StatisticOverviewItem(2D, -9999D));
        overview.setCallFailure(new StatisticOverviewItem(1D, -9999D));
        overview.setAvgCosts(new StatisticOverviewItem(7D, -9999D));
        overview.setAvgFirstTokenLatency(new StatisticOverviewItem(9D, -9999D));
        ModelStatisticResult result = new ModelStatisticResult();
        result.setOverview(overview);
        result.getTrend().setModelCalls(statisticChart(
                "app_statistic_model_call_trend",
                "app_statistic_call_count_total",
                2D));
        result.getTrend().setTokensUsage(statisticChart(
                "app_statistic_model_tokens_usage_trend",
                "app_statistic_total_tokens",
                42D));
        return result;
    }

    private ModelStatisticListResult persistentModelList() {
        ModelStatisticItem item = new ModelStatisticItem();
        item.setModelId("model-001");
        item.setModel("stats-model");
        item.setProvider("local");
        item.setCallCount(2L);
        item.setCallFailure(1L);
        item.setFailureRate(50D);
        item.setPromptTokens(20L);
        item.setCompletionTokens(22L);
        item.setTotalTokens(42L);
        item.setAvgCosts(7D);
        item.setAvgFirstTokenLatency(9D);
        ModelStatisticListResult result = new ModelStatisticListResult();
        result.setList(Collections.singletonList(item));
        result.setTotal(1L);
        result.setPageNo(1);
        result.setPageSize(10);
        return result;
    }

    private StatisticChart statisticChart(String tableName, Object... pairs) {
        StatisticChart chart = new StatisticChart();
        chart.setTableName(tableName);
        List<StatisticLine> lines = new ArrayList<StatisticLine>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            lines.add(new StatisticLine(
                    String.valueOf(pairs[i]),
                    Collections.singletonList(new StatisticPoint("2026-06-29", ((Number) pairs[i + 1]).doubleValue()))));
        }
        chart.setLines(lines);
        return chart;
    }

    private ApiKeyStatisticResult persistentOverview(String methodPath) {
        ApiKeyStatisticOverview overview = new ApiKeyStatisticOverview();
        overview.setCallCount(new ApiKeyStatisticOverviewItem(7D, -9999D));
        overview.setCallFailure(new ApiKeyStatisticOverviewItem(1D, -9999D));
        overview.setAvgStreamCosts(new ApiKeyStatisticOverviewItem(5D, -9999D));
        overview.setAvgNonStreamCosts(new ApiKeyStatisticOverviewItem(3D, -9999D));
        overview.setStreamCount(new ApiKeyStatisticOverviewItem(2D, -9999D));
        overview.setNonStreamCount(new ApiKeyStatisticOverviewItem(5D, -9999D));
        ApiKeyStatisticChart chart = new ApiKeyStatisticChart();
        chart.setTableName("app_statistic_api_key_call_trend");
        chart.setLines(Collections.singletonList(new ApiKeyStatisticLine(
                "app_statistic_api_call_count_total",
                Collections.singletonList(new ApiKeyStatisticPoint("2026-06-29", 7D)))));
        ApiKeyStatisticResult result = new ApiKeyStatisticResult();
        result.setOverview(overview);
        result.getTrend().setApiCalls(chart);
        return result;
    }

    private ApiKeyStatisticListResult persistentList(String methodPath) {
        ApiKeyStatisticItem item = new ApiKeyStatisticItem();
        item.setApiKeyId("dev-admin-key");
        item.setMethodPath(methodPath);
        item.setCallCount(7);
        item.setCallFailure(1);
        item.setAvgStreamCosts(5D);
        item.setAvgNonStreamCosts(3D);
        item.setStreamCount(2);
        item.setNonStreamCount(5);
        ApiKeyStatisticListResult result = new ApiKeyStatisticListResult();
        result.setList(Collections.singletonList(item));
        result.setTotal(1);
        result.setPageNo(1);
        result.setPageSize(10);
        return result;
    }

    private ApiKeyStatisticRecordResult persistentRecords(String methodPath) {
        ApiKeyStatisticRecordItem item = new ApiKeyStatisticRecordItem();
        item.setApiKeyId("dev-admin-key");
        item.setMethodPath(methodPath);
        item.setCallTime(1782660000000L);
        item.setResponseStatus("200");
        item.setRequestBody("{\"ok\":true}");
        item.setResponseBody("");
        ApiKeyStatisticRecordResult result = new ApiKeyStatisticRecordResult();
        result.setList(Collections.singletonList(item));
        result.setTotal(1);
        result.setPageNo(1);
        result.setPageSize(10);
        return result;
    }
}
