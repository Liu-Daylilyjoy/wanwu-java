package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuCallbackApiControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuCallbackApiController())
            .build();

    @Test
    public void modelInfoCallbackReturnsRedactedModelConfigAndCallbackEndpoint() throws Exception {
        ModelService modelService = mock(ModelService.class);
        ModelInfo model = new ModelInfo();
        model.setModelId("model-123");
        model.setModel("deepseek-chat");
        model.setProvider("DeepSeek");
        model.setModelType("llm");
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("apiKey", "real-key");
        config.put("apiSecret", "real-secret");
        config.put("appKey", "real-app-key");
        config.put("accessKey", "real-access-key");
        config.put("endpointUrl", "https://api.deepseek.com/v1");
        config.put("region", "cn");
        model.setConfig(config);
        when(modelService.getModel("", "", "model-123")).thenReturn(model);

        MockMvc callbackMvc = MockMvcBuilders
                .standaloneSetup(new WanwuCallbackApiController(modelService))
                .build();

        callbackMvc.perform(get("/callback/v1/model/model-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.modelId").value("model-123"))
                .andExpect(jsonPath("$.data.model").value("deepseek-chat"))
                .andExpect(jsonPath("$.data.provider").value("DeepSeek"))
                .andExpect(jsonPath("$.data.config.apiKey").value("useless-api-key"))
                .andExpect(jsonPath("$.data.config.apiSecret").value("useless-api-key"))
                .andExpect(jsonPath("$.data.config.appKey").value("useless-api-key"))
                .andExpect(jsonPath("$.data.config.accessKey").value("useless-api-key"))
                .andExpect(jsonPath("$.data.config.endpointUrl").value("http://bff:8080/callback/v1/model/model-123"))
                .andExpect(jsonPath("$.data.config.region").value("cn"));
    }

    @Test
    public void fileAndModelCallbackRoutesKeepGoRouteShapesAvailable() throws Exception {
        mockMvc.perform(post("/callback/v1/file/url/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("aGVsbG8="));

        mockMvc.perform(post("/callback/v1/file/upload/base64")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"hello.txt\",\"base64\":\"aGVsbG8=\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId").exists())
                .andExpect(jsonPath("$.data.file_name").value("hello.txt"));

        mockMvc.perform(get("/callback/v1/model/model-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modelId").value("model-001"))
                .andExpect(jsonPath("$.data.status").value("available"));

        mockMvc.perform(post("/callback/v1/model/model-001/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("model-001"))
                .andExpect(jsonPath("$.choices[0].message.content", containsString("model-001")));

        mockMvc.perform(post("/callback/v1/model/model-001/embeddings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].embedding[0]").value(0.0));
    }

    @Test
    public void specializedModelCallbacksReturnGoCompatibleBodies() throws Exception {
        mockMvc.perform(post("/callback/v1/model/model-ocr/ocr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"file:///demo.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data[0].page_num[0]").value(1))
                .andExpect(jsonPath("$.data[0].type").value("text"));

        mockMvc.perform(post("/callback/v1/model/model-pdf/pdf-parser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"file_name\":\"demo.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.content", containsString("model-pdf")));

        mockMvc.perform(post("/callback/v1/model/model-gui/gui")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"task\":\"open settings\",\"platform\":\"WIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.content.operation").value("finish"))
                .andExpect(jsonPath("$.usage.total_tokens").value(0));

        mockMvc.perform(post("/callback/v1/model/model-asr/asr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"qwen3-asr-flash\",\"messages\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.choices[0].message.role").value("assistant"))
                .andExpect(jsonPath("$.choices[0].message.content[0].text", containsString("model-asr")));
    }

    @Test
    public void workflowMcpRagSkillAndSandboxCallbacksReturnFrontendSafeResponses() throws Exception {
        mockMvc.perform(get("/callback/v1/workflow/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray());

        mockMvc.perform(get("/callback/v1/mcp").param("id", "mcp-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("mcp-001"));

        mockMvc.perform(post("/callback/v1/skill/builtin/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyword\":\"skill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray());

        mockMvc.perform(post("/callback/v1/wga/sandbox/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"print(1)\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("sandbox_ok"));

        mockMvc.perform(post("/callback/v1/agent/assistant-001/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("assistant-001")));

        mockMvc.perform(post("/callback/v1/rag/knowledge/stream/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("success")));
    }

    @Test
    public void v1CallbackAliasesMatchOriginalGoCompatibilityEntrypoints() throws Exception {
        mockMvc.perform(post("/user/api/v1/api/docstatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docId\":\"doc-001\",\"status\":\"success\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("doc_status_updated"));

        mockMvc.perform(post("/api/knowledge/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"knowledgeId\":\"kb-001\",\"status\":\"ready\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("knowledge_status_updated"));

        mockMvc.perform(get("/api/deploy/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.platform").value("wanwu-java"));
    }
}
