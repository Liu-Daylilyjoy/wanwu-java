package com.unicomai.wanwu.service.bff.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.zip.ZipInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuStaticDocsControllerTest {

    @Test
    public void frontendStaticCsvTemplatesDownloadWithCsvContentType() throws Exception {
        MockMvc mockMvc = mockMvc();

        assertCsv(mockMvc, "qa_pair_template.csv", "question,answer");
        assertCsv(mockMvc, "report.csv", "title,content");
        assertCsv(mockMvc, "segment.csv", "content");
    }

    @Test
    public void frontendStaticExcelTemplatesDownloadAsValidXlsxFiles() throws Exception {
        MockMvc mockMvc = mockMvc();

        assertXlsx(mockMvc, "users.xlsx");
        assertXlsx(mockMvc, "sensitive.xlsx");
        assertXlsx(mockMvc, "graph_schema.xlsx");
        assertXlsx(mockMvc, "url_import_template.xlsx");
        assertXlsx(mockMvc, "qa_import_template.xlsx");
    }

    @Test
    public void unknownStaticDocReturnsNotFoundInsteadOfReadingArbitraryFiles() throws Exception {
        mockMvc().perform(get("/user/api/v1/static/docs/../../application.yml"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void docCenterManualResourcesAreServedFromClasspath() throws Exception {
        mockMvc().perform(get("/user/api/v1/static/manual/getting-started.md"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/markdown")))
                .andExpect(header().string("Cache-Control", containsString("no-cache")))
                .andExpect(result -> assertTrue(result.getResponse()
                        .getContentAsString()
                        .contains("Wanwu Java")));
        mockMvc().perform(get("/user/api/v1/static/manual/assets/doc-center.svg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("image/svg+xml")))
                .andExpect(result -> assertTrue(result.getResponse()
                        .getContentAsString()
                        .contains("Wanwu Doc Center")));
    }

    @Test
    public void docCenterManualResourcesRejectUnknownAndTraversalPaths() throws Exception {
        MockMvc mockMvc = mockMvc();

        mockMvc.perform(get("/user/api/v1/static/manual/missing.png"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/user/api/v1/static/manual/..%2Fapplication.yml"))
                .andExpect(status().isNotFound());
    }

    private void assertCsv(MockMvc mockMvc, String name, String expectedText) throws Exception {
        mockMvc.perform(get("/user/api/v1/static/docs/" + name))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("filename=\"" + name + "\"")))
                .andExpect(header().string("Cache-Control", containsString("no-cache")))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(expectedText)));
    }

    private void assertXlsx(MockMvc mockMvc, String name) throws Exception {
        MvcResult result = mockMvc.perform(get("/user/api/v1/static/docs/" + name))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        containsString("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
                .andExpect(header().string("Content-Disposition", containsString("filename=\"" + name + "\"")))
                .andReturn();

        assertTrue(hasZipEntry(result.getResponse().getContentAsByteArray(), "xl/workbook.xml"));
        assertTrue(hasZipEntry(result.getResponse().getContentAsByteArray(), "xl/worksheets/sheet1.xml"));
    }

    private boolean hasZipEntry(byte[] bytes, String entryName) throws Exception {
        ZipInputStream zip = new ZipInputStream(new java.io.ByteArrayInputStream(bytes));
        try {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entryName.equals(entry.getName())) {
                    return true;
                }
            }
            return false;
        } finally {
            zip.close();
        }
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(new WanwuStaticDocsController()).build();
    }
}
