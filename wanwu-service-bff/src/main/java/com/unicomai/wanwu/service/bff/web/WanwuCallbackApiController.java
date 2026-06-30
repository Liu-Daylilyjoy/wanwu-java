package com.unicomai.wanwu.service.bff.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class WanwuCallbackApiController {

    @PostMapping("/callback/v1/file/url/base64")
    public FrontendResponse<String> fileUrlToBase64(@RequestBody(required = false) Map<String, Object> request) {
        String value = firstText(request, "url", "fileUrl", "file_url");
        return FrontendResponse.ok(Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8)));
    }

    @PostMapping("/callback/v1/file/upload/base64")
    public FrontendResponse<Map<String, Object>> uploadBase64(@RequestBody(required = false) Map<String, Object> request) {
        String fileName = defaultIfBlank(firstText(request, "fileName", "file_name"), "callback-upload.txt");
        String fileId = "callback-file-" + compactId();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileId", fileId);
        data.put("file_id", fileId);
        data.put("fileName", fileName);
        data.put("file_name", fileName);
        data.put("path", "/callback/v1/file/" + fileId);
        return FrontendResponse.ok(data);
    }

    @PostMapping("/callback/v1/image/outline")
    public FrontendResponse<Map<String, Object>> imageOutline(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> data = echo("outline_extracted", request);
        data.put("outline", Collections.emptyList());
        return FrontendResponse.ok(data);
    }

    @PostMapping("/callback/v1/tourism/poi/search")
    public FrontendResponse<Map<String, Object>> tourismPoi(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", defaultIfBlank(firstText(request, "keyword", "query"), "tourism-poi"));
        row.put("score", 1);
        return FrontendResponse.ok(listResult(Collections.singletonList(row)));
    }

    @GetMapping("/callback/v1/model/{modelId}")
    public FrontendResponse<Map<String, Object>> modelInfo(@PathVariable("modelId") String modelId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("modelId", modelId);
        data.put("model", modelId);
        data.put("provider", "wanwu-java");
        data.put("status", "available");
        return FrontendResponse.ok(data);
    }

    @PostMapping("/callback/v1/model/{modelId}/chat/completions")
    public Map<String, Object> chatCompletions(
            @PathVariable("modelId") String modelId,
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "assistant");
        message.put("content", "Callback model " + modelId + " development response.");
        Map<String, Object> choice = new LinkedHashMap<>();
        choice.put("index", 0);
        choice.put("message", message);
        choice.put("finish_reason", "stop");
        Map<String, Object> data = openAiBase("chat.completion", modelId);
        data.put("choices", Collections.singletonList(choice));
        data.put("usage", usage());
        data.put("request", request == null ? Collections.emptyMap() : request);
        return data;
    }

    @PostMapping({"/callback/v1/model/{modelId}/embeddings",
            "/callback/v1/model/{modelId}/multimodal-embeddings"})
    public Map<String, Object> embeddings(@PathVariable("modelId") String modelId) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("object", "embedding");
        item.put("index", 0);
        item.put("embedding", Collections.singletonList(0.0));
        Map<String, Object> data = openAiBase("list", modelId);
        data.put("data", Collections.singletonList(item));
        data.put("usage", usage());
        return data;
    }

    @PostMapping({"/callback/v1/model/{modelId}/rerank",
            "/callback/v1/model/{modelId}/multimodal-rerank"})
    public Map<String, Object> rerank(@PathVariable("modelId") String modelId) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("index", 0);
        item.put("relevance_score", 1.0);
        Map<String, Object> data = openAiBase("rerank", modelId);
        data.put("results", Collections.singletonList(item));
        return data;
    }

    @PostMapping({"/callback/v1/model/{modelId}/ocr",
            "/callback/v1/model/{modelId}/gui",
            "/callback/v1/model/{modelId}/pdf-parser",
            "/callback/v1/model/{modelId}/asr"})
    public FrontendResponse<Map<String, Object>> modelTask(
            @PathVariable("modelId") String modelId,
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> data = echo("model_task_completed", request);
        data.put("modelId", modelId);
        data.put("text", "");
        return FrontendResponse.ok(data);
    }

    @GetMapping({"/callback/v1/workflow/list", "/callback/v1/chatflow/list"})
    public FrontendResponse<Map<String, Object>> workflowList() {
        return FrontendResponse.ok(listResult(Collections.emptyList()));
    }

    @GetMapping({"/callback/v1/workflow/tool/square", "/callback/v1/workflow/tool/custom",
            "/callback/v1/mcp", "/callback/v1/mcp/server", "/callback/v1/skill/detail"})
    public FrontendResponse<Map<String, Object>> callbackDetail(@RequestParam Map<String, String> request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", firstText(request, "id", "toolId", "mcpId", "skillId"));
        data.put("name", "callback-detail");
        data.put("request", request);
        return FrontendResponse.ok(data);
    }

    @PostMapping("/callback/v1/agent/{assistantId}/chat")
    public ResponseEntity<String> agentChat(
            @PathVariable("assistantId") String assistantId,
            @RequestBody(required = false) Map<String, Object> request) {
        String query = firstText(request, "query", "prompt", "content");
        String json = "{\"assistantId\":\"" + jsonEscape(assistantId) + "\",\"response\":\""
                + jsonEscape(defaultIfBlank(query, "callback response")) + "\",\"finish\":1}";
        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body("data: " + json + "\n\n");
    }

    @PostMapping({"/callback/v1/rag/search-knowledge-base",
            "/callback/v1/rag/search-QA-base",
            "/callback/v1/wga/rag/search-knowledge-base",
            "/callback/v1/skill/builtin/list",
            "/callback/v1/skill/custom/list"})
    public FrontendResponse<Map<String, Object>> callbackList(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> data = listResult(Collections.emptyList());
        data.put("request", request == null ? Collections.emptyMap() : request);
        return FrontendResponse.ok(data);
    }

    @PostMapping("/callback/v1/rag/knowledge/stream/search")
    public ResponseEntity<String> knowledgeStream(@RequestBody(required = false) Map<String, Object> request) {
        String json = "{\"code\":0,\"message\":\"success\",\"data\":{\"list\":[]}}";
        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body("data: " + json + "\n\n");
    }

    @PostMapping({"/callback/v1/wga/sandbox/run", "/callback/v1/wga/sandbox/cleanup"})
    public FrontendResponse<Map<String, Object>> sandbox(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(echo("sandbox_ok", request));
    }

    @PostMapping("/callback/v1/app/record")
    public FrontendResponse<Map<String, Object>> appRecord(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(echo("recorded", request));
    }

    @PostMapping({"/user/api/v1/api/docstatus", "/api/docstatus"})
    public FrontendResponse<Map<String, Object>> updateDocStatus(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(echo("doc_status_updated", request));
    }

    @GetMapping({"/user/api/v1/api/deploy/info", "/api/deploy/info"})
    public FrontendResponse<Map<String, Object>> deployInfo() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("deployType", "docker-dev");
        data.put("platform", "wanwu-java");
        return FrontendResponse.ok(data);
    }

    @GetMapping({"/user/api/v1/api/category/info", "/api/category/info"})
    public FrontendResponse<Map<String, Object>> categoryInfo(@RequestParam Map<String, String> request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", firstText(request, "name", "categoryName"));
        data.put("knowledgeId", "");
        data.put("request", request);
        return FrontendResponse.ok(data);
    }

    @GetMapping({"/user/api/v1/api/doc_status_init", "/api/doc_status_init"})
    public FrontendResponse<Map<String, Object>> docStatusInit() {
        return FrontendResponse.ok(echo("doc_status_initialized", Collections.<String, Object>emptyMap()));
    }

    @PostMapping({"/user/api/v1/api/knowledge/status", "/api/knowledge/status"})
    public FrontendResponse<Map<String, Object>> knowledgeStatus(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(echo("knowledge_status_updated", request));
    }

    private Map<String, Object> openAiBase(String object, String modelId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", "callback-" + compactId());
        data.put("object", object);
        data.put("created", 0);
        data.put("model", modelId);
        return data;
    }

    private Map<String, Object> usage() {
        Map<String, Object> usage = new LinkedHashMap<>();
        usage.put("prompt_tokens", 0);
        usage.put("completion_tokens", 0);
        usage.put("total_tokens", 0);
        return usage;
    }

    private Map<String, Object> listResult(List<?> list) {
        List<?> rows = list == null ? Collections.emptyList() : list;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", rows);
        data.put("total", rows.size());
        return data;
    }

    private Map<String, Object> echo(String status, Map<String, Object> request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", status);
        data.put("request", request == null ? Collections.emptyMap() : request);
        data.put("timestamp", Instant.EPOCH.toString());
        return data;
    }

    private String firstText(Map<?, ?> map, String... keys) {
        if (map == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && !isBlank(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private String compactId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}
