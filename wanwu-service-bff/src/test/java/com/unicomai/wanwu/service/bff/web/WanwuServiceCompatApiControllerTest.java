package com.unicomai.wanwu.service.bff.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuServiceCompatApiControllerTest {

    @Test
    public void serviceRagUploadReturnsFrontendFileList() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new WanwuServiceCompatApiController())
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "files", "note.txt", "text/plain", "hello rag".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/service/api/v1/rag/upload")
                        .file(file)
                        .param("markdown", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.fileList[0].fileIndex").value(0))
                .andExpect(jsonPath("$.data.fileList[0].fileUrl").value(startsWith("data:text/plain;base64,")));
    }

    @Test
    public void serviceRagUploadCanReturnMarkdownImageUrl() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new WanwuServiceCompatApiController())
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "files", "diagram.png", "image/png", "png-data".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/service/api/v1/rag/upload")
                        .file(file)
                        .param("markdown", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.fileList[0].fileUrl").value(containsString("![diagram.png](data:image/png;base64,")));
    }
}
