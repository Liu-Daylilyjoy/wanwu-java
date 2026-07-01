package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
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
    public void docCenterRoutesReturnMenuMarkdownSearchAndEntryContracts() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/user/api/v1/doc_center"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.docCenterPath").value("/aibase/docCenter/pages/doc_first"));
        mockMvc.perform(get("/user/api/v1/doc_center/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Getting Started"))
                .andExpect(jsonPath("$.data[0].path").value("getting-started.md"))
                .andExpect(jsonPath("$.data[0].children").isArray());
        mockMvc.perform(get("/user/api/v1/doc_center/markdown")
                        .param("path", "getting-started.md"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", containsString("Wanwu Java")));
        mockMvc.perform(get("/user/api/v1/doc_center/search")
                        .param("content", "workflow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Application Development"))
                .andExpect(jsonPath("$.data[0].list[0].url").value("/aibase/docCenter/pages/application-development.md"));
    }

    @Test
    public void guestEmailAuthRoutesReturnDevelopmentContracts() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(post("/user/api/v1/base/register/email/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"email\":\"tester@example.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sent").value(true))
                .andExpect(jsonPath("$.data.email").value("tester@example.local"));
        mockMvc.perform(post("/user/api/v1/base/register/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"email\":\"tester@example.local\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registered").value(true))
                .andExpect(jsonPath("$.data.username").value("tester"));
        mockMvc.perform(post("/user/api/v1/base/password/email/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@example.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sent").value(true));
        mockMvc.perform(post("/user/api/v1/base/password/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@example.local\",\"code\":\"123456\",\"password\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reset").value(true));
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
        mockMvc.perform(put("/user/api/v1/user/login")
                        .header("Authorization", "Bearer dev-token-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"x\",\"newPassword\":\"y\",\"email\":\"app@example.local\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uid").value("dev-app"))
                .andExpect(jsonPath("$.data.token").value("dev-token-app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[0].perm").value("app"))
                .andExpect(jsonPath("$.data.orgPermission.permissions[3].perm").value("app.agent"));
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .standaloneSetup(new WanwuCommonApiController(tempDir))
                .build();
    }
}
