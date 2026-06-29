package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationOption;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuFrontendApiControllerTest {

    private final IamService iamService = mock(IamService.class);
    private final AppService appService = mock(AppService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuFrontendApiController(iamService, appService))
            .build();

    @Test
    public void captchaReturnsFrontendContract() throws Exception {
        when(iamService.captcha()).thenReturn(new CaptchaResult("dev-captcha", "data:image/svg+xml;base64,MTIzNA=="));

        mockMvc.perform(get("/user/api/v1/base/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.key").value("dev-captcha"))
                .andExpect(jsonPath("$.data.b64").value("data:image/svg+xml;base64,MTIzNA=="));
    }

    @Test
    public void loginReturnsSessionAndPermissionsForFrontend() throws Exception {
        when(iamService.login(any(LoginCommand.class))).thenReturn(devAdminResult());

        mockMvc.perform(post("/user/api/v1/base/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"encrypted\",\"key\":\"dev-captcha\",\"code\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.uid").value("dev-admin"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.token").value("dev-token"))
                .andExpect(jsonPath("$.data.isUpdatePassword").value(true))
                .andExpect(jsonPath("$.data.orgPermission.org.id").value("default-org"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[0].perm").value("app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[1].perm").value("app.agent"))
                .andExpect(jsonPath("$.data.custom.loginEmail.email.status").value(false));

        verify(iamService).login(any(LoginCommand.class));
    }

    @Test
    public void assistantListReturnsEmptyFrontendList() throws Exception {
        when(appService.listAssistants(any(ApplicationListQuery.class)))
                .thenReturn(new ApplicationListResult(Collections.emptyList()));

        mockMvc.perform(get("/user/api/v1/appspace/assistant/list")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.list", hasSize(0)));

        verify(appService).listAssistants(any(ApplicationListQuery.class));
    }

    private LoginResult devAdminResult() {
        LoginResult result = new LoginResult();
        result.setUid("dev-admin");
        result.setUsername("admin");
        result.setUserCategory("admin");
        result.setToken("dev-token");
        result.setExpiresAt(4102444800000L);
        result.setIsUpdatePassword(true);
        result.setOrgs(Collections.singletonList(new OrganizationOption("default-org", "Default Organization")));
        result.setOrgPermission(orgPermission());
        result.setCustom(platformConfig());
        return result;
    }

    private Map<String, Object> org(String id, String name) {
        Map<String, Object> org = new LinkedHashMap<>();
        org.put("id", id);
        org.put("name", name);
        return org;
    }

    private Map<String, Object> permission(String perm) {
        Map<String, Object> permission = new LinkedHashMap<>();
        permission.put("perm", perm);
        return permission;
    }

    private Map<String, Object> orgPermission() {
        Map<String, Object> orgPermission = new LinkedHashMap<>();
        orgPermission.put("org", org("default-org", "Default Organization"));
        orgPermission.put("permissions", java.util.Arrays.asList(permission("app"), permission("app.agent")));
        orgPermission.put("roles", Collections.singletonList("admin"));
        orgPermission.put("isAdmin", true);
        orgPermission.put("isSystem", true);
        return orgPermission;
    }

    private Map<String, Object> platformConfig() {
        Map<String, Object> email = new LinkedHashMap<>();
        email.put("status", false);
        Map<String, Object> loginEmail = new LinkedHashMap<>();
        loginEmail.put("email", email);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("loginEmail", loginEmail);
        return config;
    }
}
