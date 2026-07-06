package com.unicomai.wanwu.service.bff.web;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuFrontendPermissionFilterTest {

    @Test
    public void appTokenCanAccessAppAgentRoutesOnly() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/user/api/v1/appspace/assistant/list")
                        .header("Authorization", "Bearer dev-token-app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/model/list")
                        .header("Authorization", "Bearer dev-token-app"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.msg").value("permission denied"));

        mockMvc.perform(get("/user/api/v1/knowledge/select")
                        .header("Authorization", "Bearer dev-token-app"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    public void adminAndCommonRoutesPassThrough() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/user/api/v1/model/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/api/v1/user/permission")
                        .header("Authorization", "Bearer dev-token-app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .standaloneSetup(new DummyController())
                .addFilters(new WanwuFrontendPermissionFilter())
                .build();
    }

    @RestController
    private static class DummyController {

        @GetMapping("/user/api/v1/appspace/assistant/list")
        public FrontendResponse<Map<String, Object>> assistantList() {
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        }

        @GetMapping("/user/api/v1/model/list")
        public FrontendResponse<Map<String, Object>> modelList() {
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        }

        @GetMapping("/user/api/v1/knowledge/select")
        public FrontendResponse<Map<String, Object>> knowledgeSelect() {
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        }

        @GetMapping("/user/api/v1/user/permission")
        public FrontendResponse<Map<String, Object>> permission() {
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        }
    }
}
