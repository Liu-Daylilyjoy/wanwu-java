package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.ApiKeyListQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyPageResult;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.model.ModelService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
