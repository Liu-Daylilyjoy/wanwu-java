package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.iam.IamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuCommonApiControllerTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @TempDir
    public Path tempDir;

    @Test
    public void userInfoLanguagePasswordAndAvatarRoutesReturnFrontendContracts() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/user/api/v1/user/info")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value("dev-admin"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.company").value("Wanwu Java"))
                .andExpect(jsonPath("$.data.language.code").value("zh"))
                .andExpect(jsonPath("$.data.avatar.path").value(""));

        mockMvc.perform(get("/user/api/v1/user/info")
                        .header("Authorization", "Bearer dev-token-app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("dev-app"))
                .andExpect(jsonPath("$.data.username").value("app"));

        mockMvc.perform(get("/user/api/v1/base/language/select"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultLanguage.code").value("zh"))
                .andExpect(jsonPath("$.data.languages[1].code").value("en"));
        mockMvc.perform(put("/user/api/v1/user/language")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"language\":\"en\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("en"));
        mockMvc.perform(put("/user/api/v1/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult avatar = mockMvc.perform(multipart("/user/api/v1/avatar")
                        .file(new MockMultipartFile("avatar", "avatar.png", "image/png",
                                "img".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.key", containsString("avatars/")))
                .andExpect(jsonPath("$.data.path", containsString("/user/api/v1/avatar/download/")))
                .andReturn();
        JsonNode avatarData = JSON.readTree(avatar.getResponse().getContentAsString()).get("data");
        mockMvc.perform(get(avatarData.get("path").asText()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("img".getBytes(StandardCharsets.UTF_8)));
        mockMvc.perform(put("/user/api/v1/user/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatar\":{\"key\":\"" + avatarData.get("key").asText() + "\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    public void commonProfileRoutesUseIamServiceWhenAvailable() throws Exception {
        IamService iamService = mock(IamService.class);
        when(iamService.getUserInfo("dev-admin", "default-org"))
                .thenReturn(user("dev-admin", "admin", "Persisted Admin", "en",
                        "avatars/admin.png", "/user/api/v1/avatar/download/admin.png"));
        MockMvc mockMvc = mockMvc(iamService);

        mockMvc.perform(get("/user/api/v1/user/info")
                        .header("Authorization", "Bearer dev-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("dev-admin"))
                .andExpect(jsonPath("$.data.nickname").value("Persisted Admin"))
                .andExpect(jsonPath("$.data.language.code").value("en"))
                .andExpect(jsonPath("$.data.avatar.key").value("avatars/admin.png"));
        verify(iamService).getUserInfo("dev-admin", "default-org");

        mockMvc.perform(put("/user/api/v1/user/language")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"language\":\"en\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("en"));
        verify(iamService).updateUserLanguage("dev-admin", "en");

        mockMvc.perform(put("/user/api/v1/user/avatar")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatar\":{\"key\":\"avatars/admin.png\","
                                + "\"path\":\"/user/api/v1/avatar/download/admin.png\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(iamService).updateUserAvatar("dev-admin",
                "avatars/admin.png", "/user/api/v1/avatar/download/admin.png");

        mockMvc.perform(put("/user/api/v1/user/password")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"dev-admin\",\"oldPassword\":\"old\","
                                + "\"newPassword\":\"New-password1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(iamService).changeUserPassword("dev-admin", "old", "New-password1!");

        mockMvc.perform(put("/user/api/v1/user/admin/password")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"dev-app\",\"password\":\"Admin-password1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(iamService).adminChangeUserPassword(eq("dev-admin"), eq("dev-app"), eq("Admin-password1!"));
    }

    @Test
    public void docCenterRoutesReturnMenuMarkdownSearchAndEntryContracts() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/user/api/v1/doc_center"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.docCenterPath").value("/aibase/docCenter/pages/doc_first"));
        mockMvc.perform(get("/user/api/v1/doc_center/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("0.\u5e73\u53f0\u4ecb\u7ecd"))
                .andExpect(jsonPath("$.data[0].pathRaw").value("0.\u5e73\u53f0\u4ecb\u7ecd.md"))
                .andExpect(jsonPath("$.data[0].children").doesNotExist())
                .andExpect(jsonPath("$.data[2].name").value("2.\u8d44\u6e90\u5e93"))
                .andExpect(jsonPath("$.data[2].children").isArray());
        mockMvc.perform(get("/user/api/v1/doc_center/markdown")
                        .param("path", "0.\u5e73\u53f0\u4ecb\u7ecd.md"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", containsString("MCP")));
        mockMvc.perform(get("/user/api/v1/doc_center/markdown")
                        .param("path", "1.\u6a21\u578b\u7ba1\u7406.md"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data",
                        containsString("![](../../../user/api/v1/static/manual/assets/image-20250904111744304.png)")));
        mockMvc.perform(get("/user/api/v1/doc_center/search")
                        .param("content", "License Free"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("0.\u5e73\u53f0\u4ecb\u7ecd"))
                .andExpect(jsonPath("$.data[0].list[0].url")
                        .value("/aibase/docCenter/pages/0.%E5%B9%B3%E5%8F%B0%E4%BB%8B%E7%BB%8D.md"));
    }

    @Test
    public void guestEmailAuthRoutesReturnDevelopmentContracts() throws Exception {
        IamService iamService = mock(IamService.class);
        when(iamService.listUsers(eq("default-org"), eq(""), eq(1), eq(1000)))
                .thenReturn(users("registered-user", "tester", "tester@example.local"));
        MockMvc mockMvc = mockMvc(iamService);

        mockMvc.perform(post("/user/api/v1/base/register/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"email\":\"tester@example.local\",\"code\":\"000000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.msg", containsString("email code")));

        mockMvc.perform(post("/user/api/v1/base/register/email/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"email\":\"tester@example.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sent").value(true))
                .andExpect(jsonPath("$.data.email").value("tester@example.local"))
                .andExpect(jsonPath("$.data.code").value("123456"));
        mockMvc.perform(post("/user/api/v1/base/register/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"email\":\"tester@example.local\","
                                + "\"password\":\"Tester-pass1!\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registered").value(true))
                .andExpect(jsonPath("$.data.username").value("tester"));
        verify(iamService).createUser(eq("dev-admin"), eq("default-org"), anyMap());

        mockMvc.perform(post("/user/api/v1/base/password/email/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@example.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sent").value(true));
        mockMvc.perform(post("/user/api/v1/base/password/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@example.local\",\"code\":\"123456\","
                                + "\"password\":\"Reset-pass1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reset").value(true));
        verify(iamService).adminChangeUserPassword(eq("dev-admin"), eq("registered-user"), eq("Reset-pass1!"));

        mockMvc.perform(post("/user/api/v1/base/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"x\",\"key\":\"dev-captcha\",\"code\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isEmailCheck").value(false))
                .andExpect(jsonPath("$.data.token").value("dev-token"))
                .andExpect(jsonPath("$.data.isUpdatePassword").value(true));
        mockMvc.perform(post("/user/api/v1/user/login/email/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sent").value(true));
        mockMvc.perform(post("/user/api/v1/user/login")
                        .header("Authorization", "Bearer dev-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.local\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uid").value("dev-admin"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[31].perm").value("wga.wanwu_bot"));
        mockMvc.perform(post("/user/api/v1/user/login/email/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"app@example.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sent").value(true));
        mockMvc.perform(put("/user/api/v1/user/login")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"x\",\"newPassword\":\"App-pass1!\","
                                + "\"email\":\"app@example.local\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uid").value("dev-app"))
                .andExpect(jsonPath("$.data.token").value("dev-token-app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[0].perm").value("app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[3].perm").value("app.agent"));
        verify(iamService).changeUserPassword(eq("dev-app"), eq("x"), eq("App-pass1!"));
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .standaloneSetup(new WanwuCommonApiController(tempDir))
                .build();
    }

    private MockMvc mockMvc(IamService iamService) {
        return MockMvcBuilders
                .standaloneSetup(new WanwuCommonApiController(tempDir, iamService))
                .build();
    }

    private Map<String, Object> user(String userId, String username, String nickname,
                                     String languageCode, String avatarKey, String avatarPath) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("userId", userId);
        body.put("uid", userId);
        body.put("username", username);
        body.put("nickname", nickname);
        body.put("company", "Wanwu Java");
        body.put("phone", "");
        body.put("email", username + "@example.local");
        body.put("remark", "development account");
        Map<String, Object> language = new LinkedHashMap<String, Object>();
        language.put("code", languageCode);
        language.put("name", languageCode);
        body.put("language", language);
        Map<String, Object> avatar = new LinkedHashMap<String, Object>();
        avatar.put("key", avatarKey);
        avatar.put("path", avatarPath);
        body.put("avatar", avatar);
        body.put("orgId", "default-org");
        body.put("orgName", "Default Organization");
        return body;
    }

    private Map<String, Object> users(String userId, String username, String email) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Map<String, Object> item = user(userId, username, username, "zh", "", "");
        item.put("email", email);
        result.put("list", Collections.singletonList(item));
        result.put("total", 1);
        return result;
    }
}
