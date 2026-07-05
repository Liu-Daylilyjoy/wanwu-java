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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuFileApiControllerTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @TempDir
    public Path tempDir;

    @Test
    public void chunkUploadCheckMergeDownloadCleanAndDeleteReturnGoCompatibleContracts() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/service/api/v1/file/check")
                        .param("fileName", "note.txt")
                        .param("sequence", "1")
                        .param("chunkName", "chunk-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value(0));

        mockMvc.perform(multipart("/service/api/v1/file/upload")
                        .file(new MockMultipartFile("files", "chunk-1", "text/plain",
                                "hello ".getBytes(StandardCharsets.UTF_8)))
                        .param("fileName", "note.txt")
                        .param("sequence", "1")
                        .param("chunkName", "chunk-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));
        mockMvc.perform(multipart("/service/api/v1/file/upload")
                        .file(new MockMultipartFile("files", "chunk-2", "text/plain",
                                "world".getBytes(StandardCharsets.UTF_8)))
                        .param("fileName", "note.txt")
                        .param("sequence", "2")
                        .param("chunkName", "chunk-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));

        mockMvc.perform(get("/service/api/v1/file/check")
                        .param("fileName", "note.txt")
                        .param("sequence", "1")
                        .param("chunkName", "chunk-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));
        mockMvc.perform(get("/service/api/v1/file/check/chunk/list")
                        .param("chunkName", "chunk-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadedFileSequences[0]").value(1))
                .andExpect(jsonPath("$.data.uploadedFileSequences[1]").value(2));

        MvcResult merged = mockMvc.perform(post("/service/api/v1/file/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"note.txt\",\"fileSize\":11,\"chunkName\":\"chunk-001\",\"chunkTotal\":2,\"isExpired\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalFileName").value("note.txt"))
                .andExpect(jsonPath("$.data.filePath", containsString("/service/api/v1/file/download/")))
                .andReturn();
        JsonNode data = JSON.readTree(merged.getResponse().getContentAsString()).get("data");
        String fileName = data.get("fileName").asText();
        String filePath = data.get("filePath").asText();

        mockMvc.perform(get(filePath))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));
        mockMvc.perform(post("/service/api/v1/file/clean")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chunkName\":\"chunk-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));
        mockMvc.perform(delete("/service/api/v1/file/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileList\":[\"" + fileName + "\"],\"isExpired\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    public void directOpenUrlAndProxyUploadRoutesReturnFrontendFileShapes() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(multipart("/service/api/v1/file/upload/direct")
                        .file(new MockMultipartFile("files", "avatar.png", "image/png",
                                "png".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.files[0].fileName").value("avatar.png"))
                .andExpect(jsonPath("$.data.files[0].filePath", containsString("/service/api/v1/file/download/")));

        mockMvc.perform(multipart("/service/url/openurl/v1/file/upload")
                        .file(new MockMultipartFile("files", "public-1", "text/plain",
                                "public".getBytes(StandardCharsets.UTF_8)))
                        .param("fileName", "public.txt")
                        .param("sequence", "1")
                        .param("chunkName", "public-chunk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1));
        mockMvc.perform(post("/service/url/openurl/v1/file/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"public.txt\",\"fileSize\":6,\"chunkName\":\"public-chunk\",\"chunkTotal\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalFileName").value("public.txt"));

        mockMvc.perform(multipart("/service/api/v1/proxy/file/upload")
                        .file(new MockMultipartFile("file", "workflow-input.json", "application/json",
                                "{}".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.download_link", containsString("/service/api/v1/file/download/")))
                .andExpect(jsonPath("$.data.fileName").value("workflow-input.json"));

        mockMvc.perform(multipart("/service/api/v1/inferpub/upload")
                        .file(new MockMultipartFile("file", "flow.json", "application/json",
                                "{\"nodes\":[]}".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.downloadLink", containsString("/service/api/v1/file/download/")))
                .andExpect(jsonPath("$.data.fileName").value("flow.json"));
    }

    @Test
    public void directUploadControllerDoesNotOwnOpenApiRoute() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(multipart("/service/api/openapi/v1/file/upload/direct")
                        .file(new MockMultipartFile("file", "openapi.txt", "text/plain",
                                "openapi".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isNotFound());
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .standaloneSetup(new WanwuFileApiController(tempDir))
                .build();
    }
}
