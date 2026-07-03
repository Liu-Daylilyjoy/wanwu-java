package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

@RestController
public class WanwuCallbackApiController {

    private static final String DEFAULT_CALLBACK_MODEL_BASE_URL = "http://bff:8080/callback/v1/model";
    private static final int MODEL_PROXY_CONNECT_TIMEOUT_MILLIS = 3000;
    private static final int MODEL_PROXY_READ_TIMEOUT_MILLIS = 10000;
    private static final ObjectMapper JSON = new ObjectMapper();

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    public WanwuCallbackApiController() {
    }

    public WanwuCallbackApiController(ModelService modelService) {
        this(modelService, null);
    }

    public WanwuCallbackApiController(ModelService modelService, AppService appService) {
        this.modelService = modelService;
        this.appService = appService;
    }

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
        if (modelService != null) {
            try {
                return FrontendResponse.ok(callbackModelInfo(modelService.getModel("", "", modelId)));
            } catch (RuntimeException ignored) {
                // Keep callback discovery available in Docker development when the model service is absent.
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("modelId", modelId);
        data.put("model", modelId);
        data.put("provider", "wanwu-java");
        data.put("status", "available");
        return FrontendResponse.ok(data);
    }

    @PostMapping("/callback/v1/model/{modelId}/chat/completions")
    public Object chatCompletions(
            @PathVariable("modelId") String modelId,
            @RequestBody(required = false) Map<String, Object> request) {
        ResponseEntity<FrontendResponse<Object>> modelValidation =
                validateCallbackModel(modelId, request, "/chat/completions");
        if (modelValidation != null) {
            return modelValidation;
        }
        if (isTruthy(request, "stream")) {
            ResponseEntity<String> proxied = proxyModelStream(modelId, request, "/chat/completions");
            if (proxied != null) {
                return proxied;
            }
        }
        Map<String, Object> proxied = proxyModelJson(modelId, request, "/chat/completions");
        if (proxied != null) {
            recordCallbackModelStatistic(modelId, proxied, false);
            return proxied;
        }
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
    public Object embeddings(
            @PathVariable("modelId") String modelId,
            @RequestBody(required = false) Map<String, Object> request,
            HttpServletRequest httpRequest) {
        String endpointSuffix = routeSuffix(httpRequest, "embeddings", "multimodal-embeddings");
        ResponseEntity<FrontendResponse<Object>> modelValidation =
                validateCallbackModel(modelId, request, endpointSuffix);
        if (modelValidation != null) {
            return modelValidation;
        }
        Map<String, Object> proxied = proxyModelJson(modelId, request, endpointSuffix);
        if (proxied != null) {
            recordCallbackModelStatistic(modelId, proxied, false);
            return proxied;
        }
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
    public Object rerank(
            @PathVariable("modelId") String modelId,
            @RequestBody(required = false) Map<String, Object> request,
            HttpServletRequest httpRequest) {
        String endpointSuffix = routeSuffix(httpRequest, "rerank", "multimodal-rerank");
        ResponseEntity<FrontendResponse<Object>> modelValidation =
                validateCallbackModel(modelId, request, endpointSuffix);
        if (modelValidation != null) {
            return modelValidation;
        }
        Map<String, Object> proxied = proxyModelJson(modelId, request, endpointSuffix);
        if (proxied != null) {
            recordCallbackModelStatistic(modelId, proxied, false);
            return proxied;
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("index", 0);
        item.put("relevance_score", 1.0);
        Map<String, Object> data = openAiBase("rerank", modelId);
        data.put("results", Collections.singletonList(item));
        return data;
    }

    @PostMapping("/callback/v1/model/{modelId}/ocr")
    public Map<String, Object> modelOcr(
            @PathVariable("modelId") String modelId,
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> item = new LinkedHashMap<>();
        String text = defaultIfBlank(firstText(request, "text", "url", "data"), "Development OCR result for " + modelId);
        item.put("page_num", Collections.singletonList(1));
        item.put("type", "text");
        item.put("text", text);
        item.put("length", text.length());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", 0);
        data.put("message", "success");
        data.put("version", "dev");
        data.put("timestamp", Instant.EPOCH.toString());
        data.put("id", "callback-" + compactId());
        data.put("sha1", "");
        data.put("time_cost", 0);
        data.put("filename", defaultIfBlank(firstText(request, "file_name", "fileName"), "development-ocr.txt"));
        data.put("data", Collections.singletonList(item));
        return data;
    }

    @PostMapping("/callback/v1/model/{modelId}/pdf-parser")
    public Map<String, Object> modelPdfParser(@PathVariable("modelId") String modelId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", "0");
        data.put("content", "Development PDF parser result for " + modelId);
        data.put("message", "success");
        data.put("status", "success");
        data.put("trace_id", "callback-" + compactId());
        data.put("prefix_image_url", "");
        data.put("version", "dev");
        return data;
    }

    @PostMapping("/callback/v1/model/{modelId}/gui")
    public Map<String, Object> modelGui(
            @PathVariable("modelId") String modelId,
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("description", defaultIfBlank(firstText(request, "task"), "Development GUI result for " + modelId));
        content.put("operation", "finish");
        content.put("action", "finish");
        content.put("box", Collections.emptyList());
        content.put("value", "");
        content.put("sensitivity", "normal");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", 0);
        data.put("message", "success");
        data.put("content", content);
        data.put("usage", usage());
        return data;
    }

    @PostMapping("/callback/v1/model/{modelId}/asr")
    public Map<String, Object> modelAsr(@PathVariable("modelId") String modelId) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("text", "Development transcription for " + modelId);
        content.put("segmented_content", Collections.emptyList());

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "assistant");
        message.put("content", Collections.singletonList(content));

        Map<String, Object> choice = new LinkedHashMap<>();
        choice.put("finish_reason", "stop");
        choice.put("message", message);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", 0);
        data.put("choices", Collections.singletonList(choice));
        data.put("seconds", 0);
        return data;
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> proxyModelJson(String modelId, Map<String, Object> request, String endpointSuffix) {
        if (modelService == null) {
            return null;
        }
        try {
            ModelInfo model = modelService.getModel("", "", modelId);
            if (model == null || model.getConfig() == null) {
                return null;
            }
            String endpoint = firstText(model.getConfig(), "endpointUrl", "inferUrl", "baseUrl", "url");
            String apiKey = firstText(model.getConfig(), "apiKey");
            if (isBlank(endpoint) || isBlank(apiKey) || isDevelopmentApiKey(apiKey)) {
                return null;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            if (request != null) {
                payload.putAll(request);
            }
            if ("/chat/completions".equals(endpointSuffix)) {
                convertUserImageUrls(payload);
            } else if ("/multimodal-embeddings".equals(endpointSuffix)) {
                convertMultimodalEmbeddingUrls(payload);
            } else if ("/multimodal-rerank".equals(endpointSuffix)) {
                convertMultimodalRerankUrls(payload);
            }
            if (isBlank(firstText(payload, "model"))) {
                payload.put("model", defaultIfBlank(model.getModel(), modelId));
            }
            String upstream = postJson(modelEndpointUrl(endpoint, endpointSuffix), apiKey, JSON.writeValueAsString(payload));
            return isBlank(upstream) ? null : JSON.readValue(upstream, Map.class);
        } catch (RuntimeException | IOException ignored) {
            return null;
        }
    }

    private ResponseEntity<String> proxyModelStream(String modelId, Map<String, Object> request, String endpointSuffix) {
        if (modelService == null) {
            return null;
        }
        try {
            ModelInfo model = modelService.getModel("", "", modelId);
            if (model == null || model.getConfig() == null) {
                return null;
            }
            String endpoint = firstText(model.getConfig(), "endpointUrl", "inferUrl", "baseUrl", "url");
            String apiKey = firstText(model.getConfig(), "apiKey");
            if (isBlank(endpoint) || isBlank(apiKey) || isDevelopmentApiKey(apiKey)) {
                return null;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            if (request != null) {
                payload.putAll(request);
            }
            if ("/chat/completions".equals(endpointSuffix)) {
                convertUserImageUrls(payload);
            }
            if (isBlank(firstText(payload, "model"))) {
                payload.put("model", defaultIfBlank(model.getModel(), modelId));
            }
            String upstream = postJsonStream(modelEndpointUrl(endpoint, endpointSuffix),
                    apiKey, JSON.writeValueAsString(payload));
            if (isBlank(upstream)) {
                return null;
            }
            recordCallbackStreamStatistic(modelId, upstream);
            return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(upstream);
        } catch (RuntimeException | IOException ignored) {
            return null;
        }
    }

    private ResponseEntity<FrontendResponse<Object>> validateCallbackModel(
            String modelId, Map<String, Object> request, String endpointSuffix) {
        if (modelService == null) {
            return null;
        }
        try {
            ModelInfo model = modelService.getModel("", "", modelId);
            if (model == null) {
                return null;
            }
            if (!Boolean.TRUE.equals(model.getIsActive())) {
                return callbackModelFailure("model " + modelId + " is inactive");
            }
            String expectedModel = defaultIfBlank(model.getModel(), modelId);
            String requestedModel = firstText(request, "model");
            boolean chat = "/chat/completions".equals(endpointSuffix);
            if ((chat && !expectedModel.equals(requestedModel))
                    || (!chat && !isBlank(requestedModel) && !expectedModel.equals(requestedModel))) {
                return callbackModelFailure("model " + modelId + " "
                        + callbackModelOperation(endpointSuffix) + " err: model mismatch!");
            }
            return null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private ResponseEntity<FrontendResponse<Object>> callbackModelFailure(String message) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(FrontendResponse.<Object>failure(1001, message));
    }

    private String callbackModelOperation(String endpointSuffix) {
        if ("/chat/completions".equals(endpointSuffix)) {
            return "chat completions";
        }
        if (endpointSuffix != null && endpointSuffix.contains("rerank")) {
            return "rerank";
        }
        if (endpointSuffix != null && endpointSuffix.contains("embeddings")) {
            return "embeddings";
        }
        return "callback";
    }

    private String postJson(String endpoint, String apiKey, String json) throws IOException {
        return postJson(endpoint, apiKey, json, "application/json", false);
    }

    private String postJsonStream(String endpoint, String apiKey, String json) throws IOException {
        return postJson(endpoint, apiKey, json, "text/event-stream", true);
    }

    private String postJson(String endpoint, String apiKey, String json, String accept, boolean rawResponse)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(MODEL_PROXY_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(MODEL_PROXY_READ_TIMEOUT_MILLIS);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", accept);
        connection.setDoOutput(true);
        try (OutputStream body = connection.getOutputStream()) {
            body.write(json.getBytes(StandardCharsets.UTF_8));
        }
        try {
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String response = rawResponse ? readRawStream(stream) : readStream(stream);
            if (status >= 400) {
                throw new IOException("model callback upstream returned " + status);
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String readRawStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        return new String(readBytes(stream), StandardCharsets.UTF_8);
    }

    private byte[] readBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private String modelEndpointUrl(String endpoint, String suffix) {
        String base = trimTrailingSlash(endpoint);
        if (base.endsWith(suffix)) {
            return base;
        }
        return base + suffix;
    }

    private String routeSuffix(HttpServletRequest request, String textSuffix, String multimodalSuffix) {
        String uri = request == null ? "" : request.getRequestURI();
        if (uri.endsWith("/" + multimodalSuffix)) {
            return "/" + multimodalSuffix;
        }
        return "/" + textSuffix;
    }

    @SuppressWarnings("unchecked")
    private void convertUserImageUrls(Map<String, Object> payload) {
        Object messagesValue = payload.get("messages");
        if (!(messagesValue instanceof List)) {
            return;
        }
        for (Object messageValue : (List<?>) messagesValue) {
            if (!(messageValue instanceof Map)) {
                continue;
            }
            Map<Object, Object> message = (Map<Object, Object>) messageValue;
            if (!"user".equals(String.valueOf(message.get("role")))) {
                continue;
            }
            Object contentValue = message.get("content");
            if (contentValue instanceof String || !(contentValue instanceof List)) {
                continue;
            }
            if (convertFirstImageUrl((List<?>) contentValue)) {
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void convertMultimodalEmbeddingUrls(Map<String, Object> payload) {
        Object inputValue = payload.get("input");
        if (!(inputValue instanceof List)) {
            return;
        }
        for (Object itemValue : (List<?>) inputValue) {
            if (!(itemValue instanceof Map)) {
                continue;
            }
            Map<Object, Object> item = (Map<Object, Object>) itemValue;
            convertMediaUrlField(item, "image");
            convertMediaUrlField(item, "audio");
            convertMediaUrlField(item, "video");
        }
    }

    @SuppressWarnings("unchecked")
    private void convertMultimodalRerankUrls(Map<String, Object> payload) {
        Object documentsValue = payload.get("documents");
        if (documentsValue instanceof List) {
            for (Object documentValue : (List<?>) documentsValue) {
                if (documentValue instanceof Map) {
                    convertMediaUrlField((Map<Object, Object>) documentValue, "image");
                }
            }
        }
        Object queryValue = payload.get("query");
        if (queryValue instanceof Map) {
            convertMediaUrlField((Map<Object, Object>) queryValue, "image");
        }
    }

    private void convertMediaUrlField(Map<Object, Object> item, String field) {
        Object value = item.get(field);
        if (!(value instanceof String)) {
            return;
        }
        String url = (String) value;
        if (isBlank(url) || url.startsWith("data:")) {
            return;
        }
        String dataUrl = fetchUrlAsDataUrl(url);
        if (!isBlank(dataUrl)) {
            item.put(field, dataUrl);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean convertFirstImageUrl(List<?> content) {
        for (Object itemValue : content) {
            if (!(itemValue instanceof Map)) {
                continue;
            }
            Map<?, ?> item = (Map<?, ?>) itemValue;
            Object imageUrlValue = item.get("image_url");
            if (!(imageUrlValue instanceof Map)) {
                continue;
            }
            Map<Object, Object> imageUrl = (Map<Object, Object>) imageUrlValue;
            String url = imageUrl.get("url") == null ? "" : String.valueOf(imageUrl.get("url"));
            String dataUrl = fetchUrlAsDataUrl(url);
            if (!isBlank(dataUrl)) {
                imageUrl.put("url", dataUrl);
                return true;
            }
        }
        return false;
    }

    private String fetchUrlAsDataUrl(String url) {
        if (isBlank(url)) {
            return "";
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(MODEL_PROXY_CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(MODEL_PROXY_READ_TIMEOUT_MILLIS);
            connection.setRequestMethod("GET");
            int status = connection.getResponseCode();
            if (status != 200) {
                return "";
            }
            byte[] body = readBytes(connection.getInputStream());
            if (body.length == 0) {
                return "";
            }
            String contentType = defaultIfBlank(connection.getContentType(), "application/octet-stream");
            return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(body);
        } catch (RuntimeException | IOException ignored) {
            return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Map<String, Object> callbackModelInfo(ModelInfo model) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (model == null) {
            return data;
        }
        data.put("modelId", model.getModelId());
        data.put("uuid", model.getUuid());
        data.put("provider", model.getProvider());
        data.put("modelType", model.getModelType());
        data.put("model", model.getModel());
        data.put("displayName", model.getDisplayName());
        data.put("avatar", model.getAvatar());
        data.put("publishDate", model.getPublishDate());
        data.put("isActive", model.getIsActive());
        data.put("userId", model.getUserId());
        data.put("orgId", model.getOrgId());
        data.put("createdAt", model.getCreatedAt());
        data.put("updatedAt", model.getUpdatedAt());
        data.put("modelDesc", model.getModelDesc());
        data.put("tags", model.getTags());
        data.put("config", redactedCallbackConfig(model));
        data.put("scopeType", model.getScopeType());
        data.put("allowEdit", model.getAllowEdit());
        data.put("importSource", model.getImportSource());
        data.put("status", Boolean.FALSE.equals(model.getIsActive()) ? "unavailable" : "available");
        return data;
    }

    @SuppressWarnings("unchecked")
    private void recordCallbackModelStatistic(String modelId, Map<String, Object> response, boolean stream) {
        if (appService == null || isBlank(modelId)) {
            return;
        }
        try {
            ModelInfo model = modelService == null ? null : modelService.getModel("", "", modelId);
            Map<?, ?> usage = response == null || !(response.get("usage") instanceof Map)
                    ? Collections.emptyMap() : (Map<?, ?>) response.get("usage");
            long promptTokens = longValue(usage, "prompt_tokens");
            long completionTokens = longValue(usage, "completion_tokens");
            long totalTokens = longValue(usage, "total_tokens");
            if (totalTokens <= 0L) {
                totalTokens = promptTokens + completionTokens;
            }

            RecordModelStatisticCommand command = new RecordModelStatisticCommand();
            command.setUserId("");
            command.setOrgId("");
            command.setModelId(modelId);
            command.setModel(model == null ? modelId : defaultIfBlank(model.getModel(), modelId));
            command.setProvider(model == null ? "" : defaultIfBlank(model.getProvider(), ""));
            command.setModelType(model == null ? "llm" : defaultIfBlank(model.getModelType(), "llm"));
            command.setPromptTokens(promptTokens);
            command.setCompletionTokens(completionTokens);
            command.setTotalTokens(totalTokens);
            command.setSuccess(true);
            command.setStream(stream);
            command.setCosts(0L);
            command.setFirstTokenLatency(0L);
            appService.recordModelStatistic(command);
        } catch (RuntimeException ignored) {
        }
    }

    private void recordCallbackStreamStatistic(String modelId, String upstream) {
        Map<String, Object> response = callbackStreamUsage(upstream);
        if (!response.isEmpty()) {
            recordCallbackModelStatistic(modelId, response, true);
        }
    }

    private Map<String, Object> callbackStreamUsage(String upstream) {
        if (isBlank(upstream)) {
            return Collections.emptyMap();
        }
        Map<String, Object> latestUsage = Collections.emptyMap();
        String[] lines = upstream.split("\\r?\\n");
        for (String line : lines) {
            String data = line == null ? "" : line.trim();
            if (!data.startsWith("data:")) {
                continue;
            }
            data = data.substring("data:".length()).trim();
            if (isBlank(data) || "[DONE]".equals(data)) {
                continue;
            }
            try {
                JsonNode usage = JSON.readTree(data).get("usage");
                if (usage != null && usage.isObject()) {
                    Map<String, Object> parsed = new LinkedHashMap<>();
                    parsed.put("prompt_tokens", usage.path("prompt_tokens").asLong(0L));
                    parsed.put("completion_tokens", usage.path("completion_tokens").asLong(0L));
                    parsed.put("total_tokens", usage.path("total_tokens").asLong(0L));
                    latestUsage = parsed;
                }
            } catch (IOException ignored) {
            }
        }
        if (latestUsage.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("usage", latestUsage);
        return response;
    }

    private Map<String, Object> redactedCallbackConfig(ModelInfo model) {
        Map<String, Object> config = new LinkedHashMap<>();
        if (model.getConfig() != null) {
            config.putAll(model.getConfig());
        }
        for (String key : new String[]{"apiKey", "apiSecret", "appKey", "accessKey"}) {
            if (config.containsKey(key)) {
                config.put(key, "useless-api-key");
            }
        }
        if (!isBlank(model.getModelId()) && !isBlank(model.getModel())) {
            config.put("endpointUrl", callbackEndpoint(model.getModelId()));
        }
        return config;
    }

    private String callbackEndpoint(String modelId) {
        return trimTrailingSlash(callbackModelBaseUrl()) + "/" + modelId;
    }

    private String callbackModelBaseUrl() {
        String configured = System.getProperty("wanwu.callback.model-base-url");
        if (isBlank(configured)) {
            configured = System.getenv("WANWU_CALLBACK_MODEL_BASE_URL");
        }
        if (isBlank(configured)) {
            configured = System.getenv("WANWU_CALLBACK_LLM_BASE_URL");
        }
        return defaultIfBlank(configured, DEFAULT_CALLBACK_MODEL_BASE_URL);
    }

    private String trimTrailingSlash(String value) {
        String result = defaultIfBlank(value, "");
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
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

    private boolean isTruthy(Map<?, ?> map, String key) {
        if (map == null || key == null) {
            return false;
        }
        Object value = map.get(key);
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private long longValue(Map<?, ?> map, String key) {
        if (map == null || key == null) {
            return 0L;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return Math.max(0L, ((Number) value).longValue());
        }
        try {
            return Math.max(0L, Long.parseLong(String.valueOf(value)));
        } catch (RuntimeException ignored) {
            return 0L;
        }
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

    private boolean isDevelopmentApiKey(String value) {
        return "dev-model-key".equals(value)
                || "useless-api-key".equals(value)
                || "it-is-not-your-api-key".equals(value);
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}
