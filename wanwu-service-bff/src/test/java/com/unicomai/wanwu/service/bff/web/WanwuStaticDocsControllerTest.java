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
    public void userImportTemplateMatchesGoBatchImportHeaders() throws Exception {
        MvcResult result = mockMvc().perform(get("/user/api/v1/static/docs/users.xlsx"))
                .andExpect(status().isOk())
                .andReturn();

        String table = SimpleXlsxReader.toDelimitedText(result.getResponse().getContentAsByteArray());

        assertTrue(table.contains("\u7528\u6237\u540d\t\u5bc6\u7801\t\u5355\u4f4d\t\u7535\u8bdd\t\u89d2\u8272\t\u5907\u6ce8"));
        assertTrue(table.contains("zhangsan\tPassword1!\tWanwu Java\t13800000001\tapp\timported"));
    }

    @Test
    public void unknownStaticDocReturnsNotFoundInsteadOfReadingArbitraryFiles() throws Exception {
        mockMvc().perform(get("/user/api/v1/static/docs/../../application.yml"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void docCenterManualResourcesAreServedFromClasspath() throws Exception {
        mockMvc().perform(get("/user/api/v1/static/manual/0.\u5e73\u53f0\u4ecb\u7ecd.md"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/markdown")))
                .andExpect(header().string("Cache-Control", containsString("no-cache")))
                .andExpect(result -> assertTrue(result.getResponse()
                        .getContentAsString()
                        .contains("MCP")));
        mockMvc().perform(get("/user/api/v1/static/manual/assets/image-20250904111744304.png"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("image/png")))
                .andExpect(result -> assertTrue(result.getResponse()
                        .getContentAsByteArray().length > 100));
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
