package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.agent.AgentService;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.mcp.McpService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

@RestController
public class WanwuCallbackApiController {

    private static final String DEFAULT_CALLBACK_MODEL_BASE_URL = "http://bff:8080/callback/v1/model";
    private static final int MODEL_PROXY_CONNECT_TIMEOUT_MILLIS = 3000;
    private static final int MODEL_PROXY_READ_TIMEOUT_MILLIS = 10000;
    private static final int TOURISM_DEFAULT_RADIUS_METERS = 30000;
    private static final int TOURISM_MAX_RADIUS_METERS = 100000;
    private static final int TOURISM_DEFAULT_LIMIT = 10;
    private static final int TOURISM_MAX_LIMIT = 20;
    private static final String IMAGE_OUTLINE_PROMPT =
            "Generate a clean black-and-white digital line art outline from the input image.";
    private static final byte[] IMAGE_OUTLINE_PLACEHOLDER_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=");
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final UploadedFileStore CALLBACK_FILE_STORE = UploadedFileStore.defaultStore();
    private static final List<TourismLocation> TOURISM_LOCATIONS = tourismLocations();
    private static final List<TourismPoi> TOURISM_POIS = tourismPois();

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private KnowledgeService knowledgeService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private McpService mcpService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AgentService agentService;

    public WanwuCallbackApiController() {
    }

    public WanwuCallbackApiController(ModelService modelService) {
        this(modelService, null);
    }

    public WanwuCallbackApiController(ModelService modelService, AppService appService) {
        this(modelService, appService, null);
    }

    public WanwuCallbackApiController(ModelService modelService, AppService appService,
                                      KnowledgeService knowledgeService) {
        this(modelService, appService, knowledgeService, null);
    }

    public WanwuCallbackApiController(ModelService modelService, AppService appService,
                                      KnowledgeService knowledgeService, McpService mcpService) {
        this(modelService, appService, knowledgeService, mcpService, null);
    }

    public WanwuCallbackApiController(ModelService modelService, AppService appService,
                                      KnowledgeService knowledgeService, McpService mcpService,
                                      AgentService agentService) {
        this.modelService = modelService;
        this.appService = appService;
        this.knowledgeService = knowledgeService;
        this.mcpService = mcpService;
        this.agentService = agentService;
    }

    @PostMapping("/callback/v1/file/url/base64")
    public FrontendResponse<String> fileUrlToBase64(@RequestBody(required = false) Map<String, Object> request) {
        try {
            return FrontendResponse.ok(callbackFileUrlToBase64(request));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/callback/v1/file/upload/base64")
    public FrontendResponse<Map<String, Object>> uploadBase64(@RequestBody(required = false) Map<String, Object> request) {
        try {
            String encoded = firstText(request, "file", "base64", "content");
            byte[] content = decodeCallbackFileContent(encoded);
            String fileName = callbackUploadFileName(request, encoded);
            String fileId = "callback-file-" + compactId() + "-" + safeCallbackFileName(fileName);
            writeCallbackFile(fileId, content);
            String url = "/callback/v1/file/" + fileId;
            String uri = "file-upload/file-expire/" + fileId;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("url", url);
            data.put("uri", uri);
            data.put("fileId", fileId);
            data.put("file_id", fileId);
            data.put("fileName", fileName);
            data.put("file_name", fileName);
            data.put("path", url);
            return FrontendResponse.ok(data);
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/callback/v1/image/outline")
    public FrontendResponse<Map<String, Object>> imageOutline(@RequestBody(required = false) Map<String, Object> request) {
        try {
            return FrontendResponse.ok(localImageOutline(request));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/callback/v1/file/{fileId:.+}")
    public ResponseEntity<byte[]> downloadCallbackFile(@PathVariable("fileId") String fileId) {
        byte[] bytes = CALLBACK_FILE_STORE.readBytes(fileId);
        if (bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(callbackFileMediaType(fileId))
                .body(bytes);
    }

    @PostMapping("/callback/v1/tourism/poi/search")
    public FrontendResponse<Map<String, Object>> tourismPoi(@RequestBody(required = false) Map<String, Object> request) {
        try {
            return FrontendResponse.ok(localTourismPoiSearch(request));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
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
    public FrontendResponse<Map<String, Object>> workflowList(
            @RequestParam Map<String, String> request,
            HttpServletRequest httpRequest) {
        String uri = httpRequest == null ? "" : httpRequest.getRequestURI();
        String appType = uri.contains("/chatflow/") ? "chatflow" : "workflow";
        return FrontendResponse.ok(callbackWorkflowList(request, appType));
    }

    @GetMapping("/callback/v1/workflow/tool/square")
    public FrontendResponse<Map<String, Object>> callbackWorkflowSquareTool(
            @RequestParam Map<String, String> request) {
        String id = firstText(request, "toolSquareId", "id", "toolId");
        return FrontendResponse.ok(callbackDetailOrFallback(request, id, () ->
                mcpService.getToolSquare("", "", id)));
    }

    @GetMapping("/callback/v1/workflow/tool/custom")
    public FrontendResponse<Map<String, Object>> callbackWorkflowCustomTool(
            @RequestParam Map<String, String> request) {
        String id = firstText(request, "customToolId", "id", "toolId");
        return FrontendResponse.ok(callbackDetailOrFallback(request, id, () ->
                mcpService.getCustomTool("", "", id)));
    }

    @GetMapping("/callback/v1/mcp")
    public FrontendResponse<Map<String, Object>> callbackMcp(@RequestParam Map<String, String> request) {
        String id = firstText(request, "mcpId", "id");
        return FrontendResponse.ok(callbackDetailOrFallback(request, id, () ->
                mcpService.getMcp("", "", id)));
    }

    @GetMapping("/callback/v1/mcp/server")
    public FrontendResponse<Map<String, Object>> callbackMcpServer(@RequestParam Map<String, String> request) {
        String id = firstText(request, "mcpServerId", "id");
        return FrontendResponse.ok(callbackDetailOrFallback(request, id, () ->
                mcpService.getMcpServer("", "", id)));
    }

    @GetMapping("/callback/v1/skill/detail")
    public FrontendResponse<Map<String, Object>> callbackSkillDetail(@RequestParam Map<String, String> request) {
        String skillId = firstText(request, "skillId", "id");
        String skillType = defaultIfBlank(firstText(request, "skillType", "type"), "builtin");
        return FrontendResponse.ok(callbackDetailOrFallback(request, skillId, () ->
                callbackSkillDetail(skillId, skillType)));
    }

    @PostMapping("/callback/v1/agent/{assistantId}/chat")
    public FrontendResponse<String> agentChat(
            @PathVariable("assistantId") String assistantId,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            return FrontendResponse.ok(callbackAgentChat(assistantId, request));
        } catch (RuntimeException ex) {
            return FrontendResponse.failure(1001, defaultIfBlank(ex.getMessage(), "agent callback failed"));
        }
    }

    @PostMapping({"/callback/v1/rag/search-knowledge-base",
            "/callback/v1/rag/search-QA-base",
            "/callback/v1/wga/rag/search-knowledge-base"})
    public FrontendResponse<Map<String, Object>> callbackList(
            @RequestBody(required = false) Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            String uri = httpRequest == null ? "" : httpRequest.getRequestURI();
            Map<String, Object> safe = copyStringMap(request);
            if (uri.contains("/wga/")) {
                applyWgaKnowledgeDefaults(safe);
            }
            if (uri.contains("search-QA-base")) {
                return FrontendResponse.ok(callbackQaSearch(safe, httpRequest));
            }
            return FrontendResponse.ok(callbackKnowledgeSearch(safe, httpRequest));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        } catch (RuntimeException ex) {
            return FrontendResponse.failure(1001, defaultIfBlank(ex.getMessage(), "rag callback failed"));
        }
    }

    @PostMapping("/callback/v1/skill/builtin/list")
    public FrontendResponse<Map<String, Object>> callbackBuiltinSkillList(
            @RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(callbackSkillList(request, "builtin"));
    }

    @PostMapping("/callback/v1/skill/custom/list")
    public FrontendResponse<Map<String, Object>> callbackCustomSkillList(
            @RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(callbackSkillList(request, "custom"));
    }

    @PostMapping("/callback/v1/rag/knowledge/stream/search")
    public ResponseEntity<String> knowledgeStream(
            @RequestBody(required = false) Map<String, Object> request,
            HttpServletRequest httpRequest) {
        Map<String, Object> safe = copyStringMap(request);
        try {
            Map<String, Object> hit = callbackKnowledgeSearch(safe, httpRequest);
            return callbackSse(callbackKnowledgeStreamEvent(safe, hit));
        } catch (RuntimeException ex) {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("code", 1);
            event.put("message", defaultIfBlank(ex.getMessage(), "rag knowledge stream failed"));
            return callbackSse(event);
        }
    }

    @PostMapping("/callback/v1/wga/sandbox/run")
    public ResponseEntity<String> sandboxRun(@RequestBody(required = false) Map<String, Object> request) {
        return callbackSseWithDone(callbackWgaSandboxRunEvent(request));
    }

    @PostMapping("/callback/v1/wga/sandbox/cleanup")
    public FrontendResponse<Map<String, Object>> sandboxCleanup(
            @RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(callbackWgaSandboxCleanup(request));
    }

    @PostMapping("/callback/v1/app/record")
    public FrontendResponse<Map<String, Object>> appRecord(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(echo("recorded", request));
    }

    @PostMapping({"/user/api/v1/api/docstatus", "/api/docstatus"})
    public FrontendResponse<Map<String, Object>> updateDocStatus(@RequestBody(required = false) Map<String, Object> request) {
        try {
            if (knowledgeService != null) {
                knowledgeService.updateCallbackDocStatus("", "",
                        request == null ? Collections.<String, Object>emptyMap() : request);
            }
            return FrontendResponse.ok(echo("doc_status_updated", request));
        } catch (RuntimeException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
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
        try {
            if (knowledgeService != null) {
                knowledgeService.initCallbackDocStatus("", "");
            }
            return FrontendResponse.ok(echo("doc_status_initialized", Collections.<String, Object>emptyMap()));
        } catch (RuntimeException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping({"/user/api/v1/api/knowledge/status", "/api/knowledge/status"})
    public FrontendResponse<Map<String, Object>> knowledgeStatus(@RequestBody(required = false) Map<String, Object> request) {
        try {
            if (knowledgeService != null) {
                knowledgeService.updateCallbackKnowledgeStatus("", "",
                        request == null ? Collections.<String, Object>emptyMap() : request);
            }
            return FrontendResponse.ok(echo("knowledge_status_updated", request));
        } catch (RuntimeException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private String callbackAgentChat(String assistantId, Map<String, Object> request) {
        Map<String, Object> safe = copyStringMap(request);
        String input = firstText(safe, "input", "query", "prompt", "content");
        safe.put("assistantId", assistantId);
        safe.put("input", input);
        safe.put("stream", true);
        if (agentService != null) {
            Map<String, Object> result = agentService.chatAgent(safe);
            String response = firstText(result, "response", "content", "data", "output");
            if (!isBlank(response)) {
                return response;
            }
        }
        return defaultIfBlank(input, "callback response");
    }

    private Map<String, Object> callbackKnowledgeSearch(Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> serviceRequest = callbackRagServiceRequest(request, false);
        Map<String, Object> result = knowledgeService == null
                ? Collections.<String, Object>emptyMap()
                : knowledgeService.hitKnowledge(callbackUserId(request, httpRequest), callbackOrgId(request, httpRequest),
                serviceRequest);
        return callbackRagResult(result, serviceRequest, true);
    }

    private Map<String, Object> callbackQaSearch(Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> serviceRequest = callbackRagServiceRequest(request, true);
        Map<String, Object> result = knowledgeService == null
                ? Collections.<String, Object>emptyMap()
                : knowledgeService.hitQaPairs(callbackUserId(request, httpRequest), callbackOrgId(request, httpRequest),
                serviceRequest);
        return callbackRagResult(result, serviceRequest, false);
    }

    private Map<String, Object> callbackWgaSandboxRunEvent(Map<String, Object> request) {
        Map<String, Object> safe = copyStringMap(request);
        Map<String, Object> model = objectMap(safe.get("model"));
        String runId = defaultIfBlank(firstText(safe, "runId", "run_id"), compactId());
        String threadId = firstText(safe, "threadId", "thread_id");
        String task = defaultIfBlank(firstText(safe, "overallTask", "instruction", "task", "query", "prompt"),
                lastSandboxMessageContent(safe));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("threadId", threadId);
        summary.put("runId", runId);
        summary.put("modelId", firstText(model, "modelId", "model_id", "id"));
        summary.put("model", firstText(model, "model", "name"));
        summary.put("agentName", firstText(safe, "agentName", "agent_name"));
        summary.put("messageCount", objectListSize(safe.get("messages")));
        summary.put("toolCount", objectListSize(safe.get("tools")));
        summary.put("skillCount", objectListSize(safe.get("skills")));
        summary.put("mcpCount", objectListSize(safe.get("mcps")));
        summary.put("inputDir", firstText(safe, "inputDir", "input_dir"));
        summary.put("outputDir", firstText(safe, "outputDir", "output_dir"));
        summary.put("enableThinking", booleanValue(firstValue(safe, "enableThinking", "enable_thinking"), false));
        summary.put("skipCleanup", booleanValue(firstValue(safe, "skipCleanup", "skip_cleanup"), false));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("output", "Local WGA sandbox accepted task: " + defaultIfBlank(task, "sandbox task"));
        data.put("summary", summary);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("code", 0);
        event.put("message", "success");
        event.put("threadId", threadId);
        event.put("runId", runId);
        event.put("status", "sandbox_completed");
        event.put("data", data);
        event.put("finish", 1);
        return event;
    }

    private Map<String, Object> callbackWgaSandboxCleanup(Map<String, Object> request) {
        Map<String, Object> safe = copyStringMap(request);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "sandbox_cleaned");
        data.put("runId", firstText(safe, "runId", "run_id"));
        data.put("cleanedAt", Instant.now().toString());
        return data;
    }

    private String lastSandboxMessageContent(Map<String, Object> request) {
        List<?> messages = objectList(request.get("messages"));
        for (int i = messages.size() - 1; i >= 0; i--) {
            String content = firstText(objectMap(messages.get(i)), "content", "text");
            if (!isBlank(content)) {
                return content;
            }
        }
        return "";
    }

    private Map<String, Object> callbackKnowledgeStreamEvent(Map<String, Object> request, Map<String, Object> hit) {
        String question = firstText(request, "question", "query", "prompt", "content");
        String output = callbackKnowledgeStreamOutput(question, hit);

        Map<String, Object> data = new LinkedHashMap<>();
        Object score = firstValue(hit, "score");
        data.put("score", hasValue(score) ? score : Collections.emptyList());
        data.put("output", output);
        data.put("searchList", firstValue(hit, "searchList"));

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("code", 0);
        event.put("message", "success");
        event.put("msg_id", compactId());
        event.put("data", data);
        event.put("history", callbackKnowledgeStreamHistory(request, question, output));
        event.put("finish", 1);
        return event;
    }

    private String callbackKnowledgeStreamOutput(String question, Map<String, Object> hit) {
        String safeQuestion = defaultIfBlank(question, "knowledge request");
        return objectList(hit.get("searchList")).isEmpty()
                ? "No local knowledge references found for: " + safeQuestion
                : "Knowledge references ready for: " + safeQuestion;
    }

    private List<Map<String, Object>> callbackKnowledgeStreamHistory(Map<String, Object> request,
                                                                     String question, String output) {
        List<Map<String, Object>> history = new ArrayList<>();
        for (Object raw : objectList(request.get("history"))) {
            Map<String, Object> item = objectMap(raw);
            if (!item.isEmpty()) {
                history.add(item);
            }
        }
        Map<String, Object> current = new LinkedHashMap<>();
        current.put("query", question);
        current.put("response", output);
        current.put("needHistory", true);
        history.add(current);
        return history;
    }

    private ResponseEntity<String> callbackSse(Map<String, Object> event) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body("data: " + toJson(event) + "\n\n");
    }

    private ResponseEntity<String> callbackSseWithDone(Map<String, Object> event) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body("data: " + toJson(event) + "\n\ndata: [DONE]\n\n");
    }

    private String toJson(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (IOException ex) {
            return "{\"code\":1,\"message\":\"json serialization failed\"}";
        }
    }

    private Map<String, Object> callbackRagServiceRequest(Map<String, Object> request, boolean qa) {
        Map<String, Object> body = copyStringMap(request);
        Map<String, Object> matchParams = objectMap(body.get("knowledgeMatchParams"));
        copyIfPresent(body, matchParams, "topK", "topK");
        copyIfPresent(body, matchParams, "threshold", "threshold");
        copyIfPresent(body, matchParams, "score", "score");
        copyIfPresent(body, matchParams, "use_graph", "useGraph");
        copyIfPresent(body, matchParams, "useGraph", "useGraph");
        copyIfPresent(body, matchParams, "rerank_model_id", "rerankModelId");
        copyIfPresent(body, matchParams, "rerankModelId", "rerankModelId");
        copyIfPresent(body, matchParams, "rerank_mod", "rerankMod");
        copyIfPresent(body, matchParams, "rerankMod", "rerankMod");
        copyIfPresent(body, matchParams, "retrieve_method", "retrieveMethod");
        copyIfPresent(body, matchParams, "retrieveMethod", "retrieveMethod");
        copyIfPresent(body, matchParams, "weights", "weights");
        copyIfPresent(body, matchParams, "metadata_filtering", "metadataFiltering");
        copyIfPresent(body, matchParams, "metadataFiltering", "metadataFiltering");
        copyIfPresent(body, matchParams, "metadata_filtering_conditions", "metadataFilteringConditions");
        copyIfPresent(body, matchParams, "metadataFilteringConditions", "metadataFilteringConditions");

        String retrieveMethod = firstText(matchParams, "retrieveMethod");
        if (!isBlank(retrieveMethod) && !hasValue(matchParams.get("matchType"))) {
            matchParams.put("matchType", retrieveMethod);
        }
        body.put("knowledgeMatchParams", matchParams);

        List<String> knowledgeIds = callbackRagKnowledgeIds(body, qa);
        if (!knowledgeIds.isEmpty()) {
            body.put("knowledgeIdList", knowledgeIds);
        }
        List<Map<String, Object>> knowledgeList = callbackKnowledgeList(body.get("knowledgeList"), knowledgeIds);
        if (!knowledgeList.isEmpty()) {
            body.put("knowledgeList", knowledgeList);
        }
        return body;
    }

    private Map<String, Object> callbackRagResult(Map<String, Object> result, Map<String, Object> request,
                                                  boolean includeUseGraph) {
        Map<String, Object> data = copyStringMap(result);
        if (!hasValue(data.get("prompt"))) {
            data.put("prompt", "");
        }
        data.put("searchList", callbackRagSearchList(data.get("searchList")));
        if (!hasValue(data.get("score"))) {
            data.put("score", Collections.emptyList());
        }
        if (includeUseGraph) {
            Map<String, Object> matchParams = objectMap(request.get("knowledgeMatchParams"));
            boolean useGraph = booleanValue(firstValue(data, "use_graph", "useGraph"),
                    booleanValue(matchParams.get("useGraph"), false));
            data.put("use_graph", useGraph);
            data.put("useGraph", useGraph);
        }
        return data;
    }

    private List<Map<String, Object>> callbackRagSearchList(Object value) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object raw : objectList(value)) {
            Map<String, Object> item = objectMap(raw);
            callbackRagSearchItemAliases(item);
            result.add(item);
        }
        return result;
    }

    private void callbackRagSearchItemAliases(Map<String, Object> item) {
        putAlias(item, "knowledgeName", "knowledgeName", "kb_name", "qaBase", "QABase");
        putAlias(item, "kb_name", "kb_name", "knowledgeName", "qaBase", "QABase");
        putAlias(item, "user_kb_name", "user_kb_name", "userKbName", "knowledgeName", "kb_name", "qaBase", "QABase");
        putAlias(item, "userKbName", "userKbName", "user_kb_name");
        putAlias(item, "meta_data", "meta_data", "metaData", "metaDataList");
        putAlias(item, "metaDataList", "metaDataList", "meta_data");

        List<Map<String, Object>> childContentList = callbackChildContentList(
                firstValue(item, "child_content_list", "childContentList"));
        item.put("child_content_list", childContentList);
        item.put("childContentList", childContentList);

        Object childScore = firstValue(item, "child_score", "childScore");
        if (!hasValue(childScore)) {
            childScore = Collections.emptyList();
        }
        item.put("child_score", childScore);
        item.put("childScore", childScore);

        Object contentType = firstValue(item, "content_type", "contentType");
        if (!hasValue(contentType)) {
            contentType = hasValue(firstValue(item, "answer")) ? "qa" : "text";
        }
        item.put("content_type", contentType);
        item.put("contentType", contentType);

        List<Map<String, Object>> rerankInfo = callbackRerankInfoList(firstValue(item, "rerank_info", "rerankInfo"));
        item.put("rerank_info", rerankInfo);
        item.put("rerankInfo", rerankInfo);
    }

    private List<Map<String, Object>> callbackChildContentList(Object value) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object raw : objectList(value)) {
            Map<String, Object> child = objectMap(raw);
            if (child.isEmpty() && raw != null) {
                child.put("childSnippet", String.valueOf(raw));
            }
            putAlias(child, "child_snippet", "child_snippet", "childSnippet");
            putAlias(child, "childSnippet", "childSnippet", "child_snippet");
            result.add(child);
        }
        return result;
    }

    private List<Map<String, Object>> callbackRerankInfoList(Object value) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object raw : objectList(value)) {
            Map<String, Object> rerank = objectMap(raw);
            putAlias(rerank, "file_url", "file_url", "fileUrl");
            putAlias(rerank, "fileUrl", "fileUrl", "file_url");
            result.add(rerank);
        }
        return result;
    }

    private List<String> callbackRagKnowledgeIds(Map<String, Object> request, boolean qa) {
        Set<String> ids = new LinkedHashSet<>();
        ids.addAll(stringList(request.get("knowledgeIdList")));
        ids.addAll(stringList(request.get("knowledgeIds")));
        ids.addAll(stringList(request.get("knowledge_ids")));
        collectRagInfoIds(request.get(qa ? "QABaseInfo" : "knowledge_base_info"), qa, ids);
        return new ArrayList<>(ids);
    }

    private void collectRagInfoIds(Object value, boolean qa, Set<String> ids) {
        if (value instanceof Map) {
            Map<String, Object> map = objectMap(value);
            String id = qa
                    ? firstText(map, "QAId", "qaId", "knowledgeId", "kb_id")
                    : firstText(map, "kb_id", "knowledgeId", "knowledge_id", "QAId");
            if (!isBlank(id)) {
                ids.add(id);
                return;
            }
            for (Object child : map.values()) {
                collectRagInfoIds(child, qa, ids);
            }
            return;
        }
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                collectRagInfoIds(item, qa, ids);
            }
        }
    }

    private List<Map<String, Object>> callbackKnowledgeList(Object existing, List<String> knowledgeIds) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Object raw : objectList(existing)) {
            Map<String, Object> item = objectMap(raw);
            String knowledgeId = firstText(item, "knowledgeId", "kb_id", "QAId", "qaId");
            if (isBlank(knowledgeId)) {
                continue;
            }
            item.put("knowledgeId", knowledgeId);
            result.add(item);
            seen.add(knowledgeId);
        }
        for (String knowledgeId : knowledgeIds) {
            if (seen.add(knowledgeId)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("knowledgeId", knowledgeId);
                result.add(item);
            }
        }
        return result;
    }

    private void applyWgaKnowledgeDefaults(Map<String, Object> request) {
        if (!request.containsKey("topK")) {
            request.put("topK", 5);
        }
        if (!request.containsKey("threshold")) {
            request.put("threshold", 0.4D);
        }
        if (isBlank(firstText(request, "retrieve_method", "retrieveMethod"))) {
            request.put("retrieve_method", "hybrid_search");
        }
        if (isBlank(firstText(request, "rerank_mod", "rerankMod"))) {
            request.put("rerank_mod", "weighted_score");
        }
        if (!hasValue(request.get("weights"))) {
            Map<String, Object> weights = new LinkedHashMap<>();
            weights.put("vector_weight", 0.2D);
            weights.put("text_weight", 0.8D);
            request.put("weights", weights);
        }
    }

    private String callbackUserId(Map<String, Object> request, HttpServletRequest httpRequest) {
        String userId = firstText(request, "userId", "user_id");
        if (!isBlank(userId)) {
            return userId;
        }
        return firstHeader(httpRequest, "X-uid", "X-Uid", "x-uid");
    }

    private String callbackOrgId(Map<String, Object> request, HttpServletRequest httpRequest) {
        String orgId = firstText(request, "orgId", "org_id");
        if (!isBlank(orgId)) {
            return orgId;
        }
        return firstHeader(httpRequest, "X-org-id", "X-Org-Id", "x-org-id");
    }

    private String firstHeader(HttpServletRequest request, String... names) {
        if (request == null || names == null) {
            return "";
        }
        for (String name : names) {
            String value = request.getHeader(name);
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private Map<String, Object> copyStringMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source == null) {
            return result;
        }
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private Map<String, Object> objectMap(Object value) {
        if (value instanceof Map) {
            return copyStringMap((Map<?, ?>) value);
        }
        return new LinkedHashMap<>();
    }

    private List<?> objectList(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List) {
            return (List<?>) value;
        }
        return Collections.singletonList(value);
    }

    private int objectListSize(Object value) {
        return value == null ? 0 : objectList(value).size();
    }

    private void copyIfPresent(Map<String, Object> source, Map<String, Object> target,
                               String sourceKey, String targetKey) {
        if (source.containsKey(sourceKey) && source.get(sourceKey) != null) {
            target.put(targetKey, source.get(sourceKey));
        }
    }

    private Object firstValue(Map<?, ?> map, String... keys) {
        if (map == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (!map.containsKey(key)) {
                continue;
            }
            Object value = map.get(key);
            if (hasValue(value)) {
                return value;
            }
        }
        return null;
    }

    private void putAlias(Map<String, Object> target, String alias, String... sourceKeys) {
        if (hasValue(target.get(alias))) {
            return;
        }
        Object value = firstValue(target, sourceKeys);
        if (hasValue(value)) {
            target.put(alias, value);
        }
    }

    private boolean hasValue(Object value) {
        return value != null && (!(value instanceof String) || !isBlank((String) value));
    }

    private boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value);
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        return fallback;
    }

    private Map<String, Object> callbackDetailOrFallback(
            Map<String, String> request, String id, Supplier<Map<String, Object>> lookup) {
        if (mcpService != null && !isBlank(id)) {
            try {
                Map<String, Object> data = lookup.get();
                if (data != null && !data.isEmpty()) {
                    return data;
                }
            } catch (RuntimeException ignored) {
                // Keep callback discovery usable when the resource service is absent or the id is stale.
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("name", "callback-detail");
        data.put("request", request == null ? Collections.emptyMap() : request);
        return data;
    }

    private Map<String, Object> callbackWorkflowList(Map<String, String> request, String appType) {
        if (appService == null) {
            return listResult(Collections.emptyList());
        }
        ApplicationListQuery query = new ApplicationListQuery();
        query.setAppType(appType);
        query.setUserId(firstText(request, "userId", "userID", "uid"));
        query.setOrgId(firstText(request, "orgId", "orgID", "spaceId", "spaceID"));
        query.setName(firstText(request, "name", "keyword"));
        query.setSearchType(firstText(request, "searchType"));
        try {
            ApplicationListResult result = appService.listApplications(query);
            if (result == null) {
                return listResult(Collections.emptyList());
            }
            Map<String, Object> data = listResult(result.getList());
            data.put("total", result.getTotal());
            return data;
        } catch (RuntimeException ignored) {
            return listResult(Collections.emptyList());
        }
    }

    private Map<String, Object> callbackSkillDetail(String skillId, String skillType) {
        Map<String, Object> source = "custom".equals(skillType)
                ? mcpService.getCustomSkill("", "", skillId)
                : mcpService.getBuiltinSkill("", "", skillId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skillId", defaultIfBlank(firstText(source, "skillId"), skillId));
        data.put("skillType", skillType);
        data.put("name", firstText(source, "name"));
        data.put("desc", firstText(source, "desc"));
        data.put("avatar", avatarPath(source.get("avatar")));
        data.put("objectPath", firstText(source, "objectPath", "skillPath", "downloadUrl", "zipUrl"));
        return data;
    }

    private Map<String, Object> callbackSkillList(Map<String, Object> request, String skillType) {
        List<Map<String, Object>> details = new ArrayList<>();
        if (mcpService != null) {
            for (String skillId : stringList(request == null ? null : request.get("skillIdList"))) {
                if (isBlank(skillId)) {
                    continue;
                }
                try {
                    details.add(callbackSkillDetail(skillId, skillType));
                } catch (RuntimeException ignored) {
                    // Go omits missing skill ids from callback list responses.
                }
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("skillList", details);
        data.put("list", details);
        data.put("total", details.size());
        return data;
    }

    private String avatarPath(Object avatar) {
        if (avatar instanceof Map) {
            return firstText((Map<?, ?>) avatar, "path", "url", "key");
        }
        return avatar == null ? "" : String.valueOf(avatar);
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

    private Map<String, Object> localTourismPoiSearch(Map<String, Object> request) {
        TourismLocation location = resolveTourismLocation(request);
        String category = normalizeTourismCategory(firstText(request, "category"));
        int radius = normalizeTourismRadius(intValue(request, "radiusMeters", "radius_meters"));
        int limit = normalizeTourismLimit(intValue(request, "limit"));
        String keyword = firstText(request, "keyword", "query").trim().toLowerCase();

        List<Map<String, Object>> results = new ArrayList<>();
        for (TourismPoi poi : TOURISM_POIS) {
            if (!"all".equals(category) && !poi.category.equals(category)) {
                continue;
            }
            if (!keyword.isEmpty() && !poi.matches(keyword)) {
                continue;
            }
            int distance = distanceMeters(location.latitude, location.longitude, poi.latitude, poi.longitude);
            if (distance > radius) {
                continue;
            }
            results.add(tourismPoiResult(poi, distance));
        }
        Collections.sort(results, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                int rating = Double.compare((Double) right.get("rating"), (Double) left.get("rating"));
                if (rating != 0) {
                    return rating;
                }
                int distance = Integer.compare((Integer) left.get("distanceMeters"), (Integer) right.get("distanceMeters"));
                if (distance != 0) {
                    return distance;
                }
                return String.valueOf(left.get("name")).compareTo(String.valueOf(right.get("name")));
            }
        });
        if (results.size() > limit) {
            results = new ArrayList<>(results.subList(0, limit));
        }
        for (int i = 0; i < results.size(); i++) {
            results.get(i).put("rank", i + 1);
        }

        Map<String, Object> query = new LinkedHashMap<>();
        query.put("resolvedLocation", location.name);
        query.put("latitude", location.latitude);
        query.put("longitude", location.longitude);
        query.put("category", category);
        if (!keyword.isEmpty()) {
            query.put("keyword", keyword);
        }
        query.put("radiusMeters", radius);
        query.put("limit", limit);
        query.put("sort", "rating_desc,distance_asc");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("query", query);
        data.put("results", results);
        return data;
    }

    private Map<String, Object> tourismPoiResult(TourismPoi poi, int distance) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rank", 0);
        result.put("id", poi.id);
        result.put("name", poi.name);
        result.put("category", poi.category);
        result.put("categoryLabel", poi.categoryLabel);
        result.put("rating", poi.rating);
        result.put("distanceMeters", distance);
        result.put("latitude", poi.latitude);
        result.put("longitude", poi.longitude);
        result.put("address", poi.address);
        result.put("description", poi.description);
        result.put("recommendedFor", poi.recommendedFor);
        result.put("openHours", poi.openHours);
        result.put("priceLevel", poi.priceLevel);
        result.put("tags", poi.tags);
        return result;
    }

    private TourismLocation resolveTourismLocation(Map<String, Object> request) {
        double latitude = doubleValue(request, "latitude");
        double longitude = doubleValue(request, "longitude");
        if (latitude != 0.0 || longitude != 0.0) {
            if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
                throw new IllegalArgumentException("latitude or longitude out of range");
            }
            return new TourismLocation("Custom Coordinates", latitude, longitude);
        }
        String locationText = defaultIfBlank(firstText(request, "location"), "Dunhuang");
        TourismLocation parsed = parseTourismCoordinates(locationText);
        if (parsed != null) {
            return parsed;
        }
        String normalized = locationText.toLowerCase();
        for (TourismLocation candidate : TOURISM_LOCATIONS) {
            String candidateName = candidate.name.toLowerCase();
            if (normalized.contains(candidateName) || candidateName.contains(normalized)) {
                return candidate;
            }
        }
        return new TourismLocation("Dunhuang", 40.1421, 94.6619);
    }

    private TourismLocation parseTourismCoordinates(String locationText) {
        if (isBlank(locationText)) {
            return null;
        }
        String[] parts = locationText.trim().split("[,;:\\s]+");
        if (parts.length != 2) {
            return null;
        }
        try {
            double latitude = Double.parseDouble(parts[0]);
            double longitude = Double.parseDouble(parts[1]);
            if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
                return null;
            }
            return new TourismLocation(locationText.trim(), latitude, longitude);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String normalizeTourismCategory(String category) {
        String value = defaultIfBlank(category, "").trim().toLowerCase();
        if (value.isEmpty() || "all".equals(value) || "recommend".equals(value)) {
            return "all";
        }
        if ("attraction".equals(value) || "scenic".equals(value) || "spot".equals(value)) {
            return "attraction";
        }
        if ("hotel".equals(value) || "lodging".equals(value)) {
            return "hotel";
        }
        if ("restaurant".equals(value) || "food".equals(value) || "dining".equals(value)) {
            return "restaurant";
        }
        return "all";
    }

    private int normalizeTourismRadius(int radius) {
        if (radius <= 0) {
            return TOURISM_DEFAULT_RADIUS_METERS;
        }
        return Math.min(radius, TOURISM_MAX_RADIUS_METERS);
    }

    private int normalizeTourismLimit(int limit) {
        if (limit <= 0) {
            return TOURISM_DEFAULT_LIMIT;
        }
        return Math.min(limit, TOURISM_MAX_LIMIT);
    }

    private int distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusMeters = 6371000.0;
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(earthRadiusMeters * c);
    }

    private Map<String, Object> localImageOutline(Map<String, Object> request) {
        if (isBlank(firstText(request, "image"))) {
            throw new IllegalArgumentException("image is required");
        }
        String responseFormat = firstText(request, "response_format", "responseFormat");
        if (!isBlank(responseFormat)
                && !"url".equalsIgnoreCase(responseFormat)
                && !"b64_json".equalsIgnoreCase(responseFormat)) {
            throw new IllegalArgumentException("unsupported response_format \"" + responseFormat + "\"");
        }

        String fileId = "callback-image-outline-" + compactId() + ".png";
        writeCallbackFile(fileId, IMAGE_OUTLINE_PLACEHOLDER_PNG);
        String url = "/callback/v1/file/" + fileId;
        String uri = "callback/image-outline/" + fileId;
        String markdown = "![](" + url + ")";

        Map<String, Object> usage = new LinkedHashMap<>();
        usage.put("width", 0);
        usage.put("height", 0);
        usage.put("foregroundPixels", 0);
        usage.put("edgePixels", 0);
        usage.put("threshold", 0);
        usage.put("lineWidth", 0);
        usage.put("method", "qwen_image_edit");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "success");
        data.put("prompt", IMAGE_OUTLINE_PROMPT);
        data.put("markdown", markdown);
        data.put("result", Collections.singletonList(markdown));
        data.put("mimeType", "image/png");
        data.put("url", url);
        data.put("uri", uri);
        data.put("usage", usage);
        return data;
    }

    private String callbackFileUrlToBase64(Map<String, Object> request) {
        String fileUrl = firstText(request, "fileUrl", "file_url", "url");
        if (isBlank(fileUrl)) {
            throw new IllegalArgumentException("fileUrl is required");
        }
        CallbackFileContent file = readCallbackFileUrl(fileUrl);
        String base64 = Base64.getEncoder().encodeToString(file.bytes);
        if (!isTruthy(request, "addPrefix") && !isTruthy(request, "add_prefix")) {
            return base64;
        }
        String prefix = firstText(request, "customPrefix", "custom_prefix");
        if (isBlank(prefix)) {
            prefix = "data:" + defaultIfBlank(file.contentType, "application/octet-stream") + ";base64";
        }
        if (!prefix.contains(",")) {
            prefix += ",";
        }
        return prefix + base64;
    }

    private CallbackFileContent readCallbackFileUrl(String fileUrl) {
        String fileId = callbackFileIdFromUrl(fileUrl);
        if (!isBlank(fileId)) {
            byte[] bytes = CALLBACK_FILE_STORE.readBytes(fileId);
            if (bytes.length == 0) {
                throw new IllegalArgumentException("fileUrl is not readable");
            }
            return new CallbackFileContent(bytes, callbackFileMediaType(fileId).toString());
        }
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            throw new IllegalArgumentException("fileUrl must be an HTTP URL or callback file URL");
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(fileUrl).openConnection();
            connection.setConnectTimeout(MODEL_PROXY_CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(MODEL_PROXY_READ_TIMEOUT_MILLIS);
            connection.setRequestMethod("GET");
            int status = connection.getResponseCode();
            if (status != 200) {
                throw new IllegalArgumentException("fileUrl returned status " + status);
            }
            byte[] bytes = readBytes(connection.getInputStream());
            if (bytes.length == 0) {
                throw new IllegalArgumentException("fileUrl is empty");
            }
            return new CallbackFileContent(bytes,
                    defaultIfBlank(connection.getContentType(), "application/octet-stream"));
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to read fileUrl");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String callbackFileIdFromUrl(String fileUrl) {
        String value = defaultIfBlank(fileUrl, "").trim();
        int marker = value.indexOf("/callback/v1/file/");
        if (marker >= 0) {
            value = value.substring(marker + "/callback/v1/file/".length());
        } else if (value.startsWith("callback-file-") || value.startsWith("callback-image-outline-")) {
            return value;
        } else {
            return "";
        }
        int query = value.indexOf('?');
        if (query >= 0) {
            value = value.substring(0, query);
        }
        int hash = value.indexOf('#');
        if (hash >= 0) {
            value = value.substring(0, hash);
        }
        return value;
    }

    private byte[] decodeCallbackFileContent(String encoded) {
        if (isBlank(encoded)) {
            throw new IllegalArgumentException("file is required");
        }
        int comma = encoded.indexOf(',');
        if (comma >= 0 && encoded.substring(0, comma).toLowerCase().contains("base64")) {
            encoded = encoded.substring(comma + 1);
        }
        String compact = encoded.replace("\n", "").replace("\r", "").replace("\t", "").replace(" ", "");
        try {
            return Base64.getDecoder().decode(compact);
        } catch (IllegalArgumentException ignored) {
            throw new IllegalArgumentException("invalid base64 file");
        }
    }

    private String callbackUploadFileName(Map<String, Object> request, String encoded) {
        String fileName = defaultIfBlank(firstText(request, "fileName", "file_name"), compactId());
        String fileExt = firstText(request, "fileExt", "file_ext");
        if (isBlank(fileExt)) {
            fileExt = callbackFileExtensionFromDataUrl(encoded);
        }
        if (!isBlank(fileExt)) {
            fileName += "." + fileExt.replace(".", "");
        }
        return safeCallbackFileName(fileName);
    }

    private String callbackFileExtensionFromDataUrl(String encoded) {
        if (isBlank(encoded) || !encoded.startsWith("data:")) {
            return "";
        }
        int semicolon = encoded.indexOf(';');
        if (semicolon < 0) {
            return "";
        }
        String mimeType = encoded.substring("data:".length(), semicolon).toLowerCase();
        if ("image/png".equals(mimeType)) {
            return "png";
        }
        if ("image/jpeg".equals(mimeType) || "image/jpg".equals(mimeType)) {
            return "jpg";
        }
        if ("image/gif".equals(mimeType)) {
            return "gif";
        }
        if ("image/webp".equals(mimeType)) {
            return "webp";
        }
        if ("text/plain".equals(mimeType)) {
            return "txt";
        }
        return "";
    }

    private String safeCallbackFileName(String value) {
        String name = defaultIfBlank(value, "upload.bin")
                .replace('\\', '_')
                .replace('/', '_')
                .replace("..", "_")
                .trim();
        return name.isEmpty() ? "upload.bin" : name;
    }

    private void writeCallbackFile(String fileId, byte[] content) {
        try {
            CALLBACK_FILE_STORE.writeBytes(fileId, content);
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to store callback file");
        }
    }

    private MediaType callbackFileMediaType(String fileId) {
        String lower = defaultIfBlank(fileId, "").toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.parseMediaType("image/gif");
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (lower.endsWith(".txt") || lower.endsWith(".text")) {
            return MediaType.TEXT_PLAIN;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
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

    private int intValue(Map<?, ?> map, String... keys) {
        if (map == null || keys == null) {
            return 0;
        }
        for (String key : keys) {
            Object value = map.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (RuntimeException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private double doubleValue(Map<?, ?> map, String key) {
        if (map == null || key == null) {
            return 0.0;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (RuntimeException ignored) {
            return 0.0;
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

    private List<String> stringList(Object value) {
        if (!(value instanceof List)) {
            return isBlank(value == null ? "" : String.valueOf(value))
                    ? Collections.<String>emptyList()
                    : Collections.singletonList(String.valueOf(value));
        }
        List<String> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            result.add(item == null ? "" : String.valueOf(item));
        }
        return result;
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static List<TourismLocation> tourismLocations() {
        return Arrays.asList(
                new TourismLocation("Mogao Caves", 40.0472, 94.8090),
                new TourismLocation("Mingsha Mountain Crescent Spring", 40.0834, 94.6734),
                new TourismLocation("Crescent Spring", 40.0834, 94.6734),
                new TourismLocation("Shazhou Night Market", 40.1424, 94.6618),
                new TourismLocation("Dunhuang", 40.1421, 94.6619)
        );
    }

    private static List<TourismPoi> tourismPois() {
        return Arrays.asList(
                new TourismPoi("dh-attraction-mogao-caves", "Mogao Caves", "attraction", "Attraction", 4.9,
                        40.0472, 94.8090, "25km southeast of Dunhuang",
                        "World heritage caves famous for murals, painted sculptures, and Buddhist art.",
                        "Culture research, first-time Dunhuang visits, half-day deep tours",
                        "See daily scenic-area notice", "Ticketed",
                        Arrays.asList("world heritage", "caves", "murals")),
                new TourismPoi("dh-attraction-mingsha-yueya", "Mingsha Mountain and Crescent Spring",
                        "attraction", "Attraction", 4.8, 40.0834, 94.6734,
                        "South of Dunhuang city",
                        "Classic desert and spring landscape, especially suitable around sunset.",
                        "Family trips, photography, sunset, desert experience",
                        "See daily scenic-area notice", "Ticketed",
                        Arrays.asList("desert", "crescent spring", "sunset")),
                new TourismPoi("dh-attraction-museum", "Dunhuang Museum", "attraction", "Attraction", 4.7,
                        40.1398, 94.6742, "Mingshan North Road, Dunhuang",
                        "Indoor museum for Silk Road history, Dunhuang relics, and Mogao background.",
                        "Pre-trip context, family trips, heat shelter",
                        "See venue notice", "Usually free reservation",
                        Arrays.asList("museum", "silk road", "indoor")),
                new TourismPoi("dh-attraction-leiyin-temple", "Leiyin Temple", "attraction", "Attraction", 4.5,
                        40.0986, 94.6715, "Near Mingsha Mountain, Dunhuang",
                        "A quiet temple near Mingsha Mountain and Crescent Spring.",
                        "Short stopover, calm visits",
                        "See attraction notice", "Low",
                        Arrays.asList("temple", "near mingsha", "quiet")),
                new TourismPoi("dh-attraction-west-thousand-buddha-caves", "West Thousand Buddha Caves",
                        "attraction", "Attraction", 4.4, 40.0138, 94.3671,
                        "West bank of Dang River canyon, Dunhuang",
                        "Smaller cave site with fewer visitors.",
                        "Cave-art fans, niche routes",
                        "See daily scenic-area notice", "Ticketed",
                        Arrays.asList("caves", "niche", "culture")),
                new TourismPoi("dh-hotel-dunhuang-villa", "Dunhuang Villa", "hotel", "Hotel", 4.8,
                        40.0921, 94.6759, "Mingsha Mountain Road, Dunhuang",
                        "Resort-style hotel near Mingsha Mountain and Crescent Spring.",
                        "Sunset viewing, vacation, near-scenic-area stays",
                        "All day", "High",
                        Arrays.asList("resort", "near mingsha", "view")),
                new TourismPoi("dh-hotel-international", "Dunhuang International Hotel", "hotel", "Hotel", 4.7,
                        40.1455, 94.6720, "Yangguan Middle Road area, Dunhuang",
                        "Urban hotel suitable as a transfer base for multiple attractions.",
                        "Business, family, city transfer",
                        "All day", "Mid-high",
                        Arrays.asList("city", "transfer", "business")),
                new TourismPoi("dh-hotel-dunhuang-hotel", "Dunhuang Hotel", "hotel", "Hotel", 4.6,
                        40.1427, 94.6628, "Yangguan Middle Road, Dunhuang",
                        "Traditional city hotel close to the night market and dining streets.",
                        "City touring, night market, public transport",
                        "All day", "Mid",
                        Arrays.asList("city", "night market", "transport")),
                new TourismPoi("dh-hotel-silk-road-yiyuan", "Silk Road Yiyuan Hotel", "hotel", "Hotel", 4.5,
                        40.1444, 94.6535, "Downtown Dunhuang",
                        "Stable downtown stay for family or group itineraries.",
                        "Families, groups, city transfer",
                        "All day", "Mid",
                        Arrays.asList("city", "group", "family")),
                new TourismPoi("dh-restaurant-jingyuan-lamb", "Jingyuan Lamb Restaurant", "restaurant",
                        "Restaurant", 4.8, 40.1451, 94.6627, "Downtown Dunhuang",
                        "Local lamb dishes suited for a filling meal.",
                        "Lunch or dinner, groups, local flavor",
                        "See daily restaurant notice", "Mid",
                        Arrays.asList("lamb", "local cuisine", "meal")),
                new TourismPoi("dh-restaurant-shunzhang-noodles", "Shunzhang Yellow Noodles", "restaurant",
                        "Restaurant", 4.7, 40.1438, 94.6610, "Downtown Dunhuang",
                        "Local noodle restaurant known for donkey-meat yellow noodles.",
                        "Lunch, local snacks, quick meals",
                        "See daily restaurant notice", "Mid-low",
                        Arrays.asList("yellow noodles", "donkey meat", "local snack")),
                new TourismPoi("dh-restaurant-shazhou-night-market", "Shazhou Night Market Food Street",
                        "restaurant", "Restaurant", 4.6, 40.1424, 94.6618, "Shazhou Town, Dunhuang",
                        "Night food, souvenirs, and visitor atmosphere in one area.",
                        "Night visit, snack collection, shopping",
                        "Mainly evening", "Mid-low",
                        Arrays.asList("night market", "snacks", "souvenir")),
                new TourismPoi("dh-restaurant-daji-donkey-noodles", "Daji Donkey Yellow Noodles",
                        "restaurant", "Restaurant", 4.5, 40.1463, 94.6603, "Downtown Dunhuang",
                        "Local donkey-meat yellow noodle restaurant.",
                        "Local flavor, lunch or dinner",
                        "See daily restaurant notice", "Mid-low",
                        Arrays.asList("donkey noodles", "local cuisine", "noodles"))
        );
    }

    private static class CallbackFileContent {
        private final byte[] bytes;
        private final String contentType;

        private CallbackFileContent(byte[] bytes, String contentType) {
            this.bytes = bytes == null ? new byte[0] : bytes;
            this.contentType = contentType;
        }
    }

    private static class TourismLocation {
        private final String name;
        private final double latitude;
        private final double longitude;

        private TourismLocation(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private static class TourismPoi {
        private final String id;
        private final String name;
        private final String category;
        private final String categoryLabel;
        private final double rating;
        private final double latitude;
        private final double longitude;
        private final String address;
        private final String description;
        private final String recommendedFor;
        private final String openHours;
        private final String priceLevel;
        private final List<String> tags;

        private TourismPoi(String id, String name, String category, String categoryLabel, double rating,
                           double latitude, double longitude, String address, String description,
                           String recommendedFor, String openHours, String priceLevel, List<String> tags) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.categoryLabel = categoryLabel;
            this.rating = rating;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.description = description;
            this.recommendedFor = recommendedFor;
            this.openHours = openHours;
            this.priceLevel = priceLevel;
            this.tags = tags;
        }

        private boolean matches(String keyword) {
            String target = (name + " " + address + " " + description + " " + tags).toLowerCase();
            return target.contains(keyword);
        }
    }
}
