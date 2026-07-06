package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationPageResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantPublishedQuery;
import com.unicomai.wanwu.api.app.dto.AppUrlInfo;
import com.unicomai.wanwu.api.app.dto.AppUrlSuffixQuery;
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WanwuOpenUrlApiController {

    private static final String OPENURL_PREFIX = "/openurl/v1";
    private static final String SERVICE_OPENURL_PREFIX = "/service/url/openurl/v1";
    private static final String PUBLIC_CLIENT_ID = "openurl-public-client";
    private static final String APP_TYPE_AGENT = "agent";
    private static final String STAT_SOURCE_WEB_URL = "webURL";

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    private final OpenAiCompatibleChatClient chatClient = new OpenAiCompatibleChatClient();

    public WanwuOpenUrlApiController() {
    }

    public WanwuOpenUrlApiController(AppService appService) {
        this(appService, null);
    }

    public WanwuOpenUrlApiController(AppService appService, ModelService modelService) {
        this.appService = appService;
        this.modelService = modelService;
    }

    @GetMapping({OPENURL_PREFIX + "/agent/{suffix}", SERVICE_OPENURL_PREFIX + "/agent/{suffix}"})
    public FrontendResponse<Map<String, Object>> getOpenUrlAgent(@PathVariable("suffix") String suffix) {
        try {
            AppUrlInfo appUrl = appService.getAppUrlBySuffix(new AppUrlSuffixQuery(suffix));
            Map<String, Object> assistant = appService.getPublishedAssistant(
                    new AssistantPublishedQuery(appUrl.getAppId(), null, appUrl.getUserId(), appUrl.getOrgId()));
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("assistant", assistant);
            data.put("appUrlInfo", appUrl);
            return FrontendResponse.ok(data);
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping({OPENURL_PREFIX + "/agent/{suffix}/conversation",
            SERVICE_OPENURL_PREFIX + "/agent/{suffix}/conversation"})
    public FrontendResponse<AssistantConversationCreateResult> createConversation(
            @PathVariable("suffix") String suffix,
            @RequestHeader(value = "X-Client-ID", required = false) String clientId,
            @RequestBody OpenUrlConversationCreateRequest request) {
        try {
            AppUrlInfo appUrl = appService.getAppUrlBySuffix(new AppUrlSuffixQuery(suffix));
            OpenUrlConversationCreateRequest effectiveRequest = request == null
                    ? new OpenUrlConversationCreateRequest()
                    : request;
            AssistantConversationCreateCommand command = new AssistantConversationCreateCommand();
            command.setAssistantId(appUrl.getAppId());
            command.setPrompt(effectiveRequest.getPrompt());
            command.setConversationType("published");
            command.setUserId(publicUserId(clientId));
            command.setOrgId(appUrl.getOrgId());
            return FrontendResponse.ok(appService.createAssistantConversation(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping({OPENURL_PREFIX + "/agent/{suffix}/conversation",
            SERVICE_OPENURL_PREFIX + "/agent/{suffix}/conversation"})
    public FrontendResponse<Map<String, Object>> deleteConversation(
            @PathVariable("suffix") String suffix,
            @RequestHeader(value = "X-Client-ID", required = false) String clientId,
            @RequestBody OpenUrlConversationIdRequest request) {
        try {
            AppUrlInfo appUrl = appService.getAppUrlBySuffix(new AppUrlSuffixQuery(suffix));
            AssistantConversationDeleteCommand command = conversationDeleteCommand(appUrl, clientId, request);
            appService.deleteAssistantConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping({OPENURL_PREFIX + "/agent/{suffix}/conversation/clear",
            SERVICE_OPENURL_PREFIX + "/agent/{suffix}/conversation/clear"})
    public FrontendResponse<Map<String, Object>> clearConversation(
            @PathVariable("suffix") String suffix,
            @RequestHeader(value = "X-Client-ID", required = false) String clientId,
            @RequestBody OpenUrlConversationIdRequest request) {
        try {
            AppUrlInfo appUrl = appService.getAppUrlBySuffix(new AppUrlSuffixQuery(suffix));
            AssistantConversationDeleteCommand command = conversationDeleteCommand(appUrl, clientId, request);
            appService.clearAssistantConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping({OPENURL_PREFIX + "/agent/{suffix}/conversation/list",
            SERVICE_OPENURL_PREFIX + "/agent/{suffix}/conversation/list"})
    public FrontendResponse<AssistantConversationPageResult> listConversations(
            @PathVariable("suffix") String suffix,
            @RequestHeader(value = "X-Client-ID", required = false) String clientId) {
        try {
            AppUrlInfo appUrl = appService.getAppUrlBySuffix(new AppUrlSuffixQuery(suffix));
            AssistantConversationListQuery query = new AssistantConversationListQuery();
            query.setAssistantId(appUrl.getAppId());
            query.setConversationType("published");
            query.setPageNo(1);
            query.setPageSize(1000);
            query.setUserId(publicUserId(clientId));
            query.setOrgId(appUrl.getOrgId());
            return FrontendResponse.ok(appService.listAssistantConversations(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping({OPENURL_PREFIX + "/agent/{suffix}/conversation/detail",
            SERVICE_OPENURL_PREFIX + "/agent/{suffix}/conversation/detail"})
    public FrontendResponse<AssistantConversationPageResult> listConversationDetails(
            @PathVariable("suffix") String suffix,
            @RequestHeader(value = "X-Client-ID", required = false) String clientId,
            @RequestParam("conversationId") String conversationId) {
        try {
            AppUrlInfo appUrl = appService.getAppUrlBySuffix(new AppUrlSuffixQuery(suffix));
            AssistantConversationDetailQuery query = new AssistantConversationDetailQuery();
            query.setConversationId(conversationId);
            query.setPageNo(1);
            query.setPageSize(1000);
            query.setUserId(publicUserId(clientId));
            query.setOrgId(appUrl.getOrgId());
            return FrontendResponse.ok(appService.listAssistantConversationDetails(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping(value = {OPENURL_PREFIX + "/agent/{suffix}/stream",
            SERVICE_OPENURL_PREFIX + "/agent/{suffix}/stream"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> streamConversation(
            @PathVariable("suffix") String suffix,
            @RequestHeader(value = "X-Client-ID", required = false) String clientId,
            @RequestBody OpenUrlConversationStreamRequest request) {
        long startedAt = System.currentTimeMillis();
        AppUrlInfo appUrl = null;
        boolean success = false;
        try {
            appUrl = appService.getAppUrlBySuffix(new AppUrlSuffixQuery(suffix));
            OpenUrlConversationStreamRequest effectiveRequest = request == null
                    ? new OpenUrlConversationStreamRequest()
                    : request;
            AssistantConversationStreamCommand command = new AssistantConversationStreamCommand();
            command.setAssistantId(appUrl.getAppId());
            command.setConversationId(effectiveRequest.getConversationId());
            command.setPrompt(effectiveRequest.getPrompt());
            command.setDraft(false);
            command.setFileInfo(effectiveRequest.getFileInfo());
            command.setUserId(publicUserId(clientId));
            command.setOrgId(appUrl.getOrgId());
            attachConfiguredModelResponse(appUrl, command, startedAt);
            AssistantConversationStreamResult result = appService.streamAssistantConversation(command);
            success = true;
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(toSseFrame(result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        } finally {
            recordOpenUrlAppStatistic(appUrl, success, startedAt);
        }
    }

    @PostMapping(value = {OPENURL_PREFIX + "/agent/{suffix}/recommend",
            SERVICE_OPENURL_PREFIX + "/agent/{suffix}/recommend"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> recommendQuestions(
            @PathVariable("suffix") String suffix,
            @RequestBody OpenUrlQuestionRecommendRequest request) {
        try {
            AppUrlInfo appUrl = appService.getAppUrlBySuffix(new AppUrlSuffixQuery(suffix));
            OpenUrlQuestionRecommendRequest effectiveRequest = request == null
                    ? new OpenUrlQuestionRecommendRequest()
                    : request;
            if (isBlank(effectiveRequest.getQuery())) {
                throw new IllegalArgumentException("query is required");
            }
            Map<String, Object> assistant = appService.getPublishedAssistant(
                    new AssistantPublishedQuery(appUrl.getAppId(), null, appUrl.getUserId(), appUrl.getOrgId()));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(toRecommendSseFrames(effectiveRequest.getQuery(), appUrl.getAppId(), assistant));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        }
    }

    private AssistantConversationDeleteCommand conversationDeleteCommand(AppUrlInfo appUrl,
                                                                         String clientId,
                                                                         OpenUrlConversationIdRequest request) {
        OpenUrlConversationIdRequest effectiveRequest = request == null
                ? new OpenUrlConversationIdRequest()
                : request;
        AssistantConversationDeleteCommand command = new AssistantConversationDeleteCommand();
        command.setAssistantId(appUrl.getAppId());
        command.setConversationId(effectiveRequest.getConversationId());
        command.setDetailId(effectiveRequest.getDetailId());
        command.setUserId(publicUserId(clientId));
        command.setOrgId(appUrl.getOrgId());
        return command;
    }

    private String publicUserId(String clientId) {
        return isBlank(clientId) ? PUBLIC_CLIENT_ID : clientId;
    }

    private void recordOpenUrlAppStatistic(AppUrlInfo appUrl, boolean success, long startedAt) {
        if (appUrl == null) {
            return;
        }
        try {
            RecordAppStatisticCommand command = new RecordAppStatisticCommand();
            command.setUserId(appUrl.getUserId());
            command.setOrgId(appUrl.getOrgId());
            command.setAppId(appUrl.getAppId());
            command.setAppType(defaultIfBlank(appUrl.getAppType(), APP_TYPE_AGENT));
            command.setSuccess(success);
            command.setStream(true);
            command.setStreamCosts(Math.max(0L, System.currentTimeMillis() - startedAt));
            command.setSource(STAT_SOURCE_WEB_URL);
            command.setCallTime(startedAt);
            appService.recordAppStatistic(command);
        } catch (RuntimeException ignored) {
            // Statistics must not break the public OpenURL chat path.
        }
    }

    private void attachConfiguredModelResponse(AppUrlInfo appUrl,
                                               AssistantConversationStreamCommand command,
                                               long startedAt) {
        if (appUrl == null || command == null || modelService == null
                || isBlank(command.getAssistantId()) || isBlank(command.getPrompt())) {
            return;
        }
        try {
            Map<String, Object> assistant = appService.getPublishedAssistant(new AssistantPublishedQuery(
                    command.getAssistantId(), null, appUrl.getUserId(), appUrl.getOrgId()));
            String modelId = configuredModelId(assistant);
            if (isBlank(modelId)) {
                return;
            }
            ModelInfo model = modelService.getModel(appUrl.getUserId(), appUrl.getOrgId(), modelId);
            if (model == null || !Boolean.TRUE.equals(model.getIsActive())) {
                return;
            }
            OpenAiCompatibleChatClient.ChatCompletionResult chatResult =
                    chatClient.complete(model, modelId, command.getPrompt());
            String answer = defaultIfBlank(chatResult.getContent(), "");
            if (isBlank(answer)) {
                return;
            }
            command.setOverrideResponse(answer);
            recordOpenUrlModelStatistic(appUrl, model, modelId, command.getPrompt(), answer,
                    startedAt, chatResult.getUsage());
        } catch (RuntimeException ignored) {
        }
    }

    private void recordOpenUrlModelStatistic(AppUrlInfo appUrl,
                                             ModelInfo model,
                                             String modelId,
                                             String prompt,
                                             String answer,
                                             long startedAt,
                                             OpenAiCompatibleChatClient.Usage usage) {
        if (appUrl == null || model == null || isBlank(modelId)) {
            return;
        }
        try {
            long promptTokens = usage == null ? chatClient.estimateTokens(prompt) : usage.getPromptTokens();
            long completionTokens = usage == null ? chatClient.estimateTokens(answer) : usage.getCompletionTokens();
            long totalTokens = usage == null || usage.getTotalTokens() <= 0L
                    ? promptTokens + completionTokens : usage.getTotalTokens();
            RecordModelStatisticCommand command = new RecordModelStatisticCommand();
            command.setUserId(appUrl.getUserId());
            command.setOrgId(appUrl.getOrgId());
            command.setModelId(modelId);
            command.setModel(defaultIfBlank(model.getModel(), modelId));
            command.setProvider(defaultIfBlank(model.getProvider(), ""));
            command.setModelType(defaultIfBlank(model.getModelType(), "llm"));
            command.setPromptTokens(promptTokens);
            command.setCompletionTokens(completionTokens);
            command.setTotalTokens(totalTokens);
            command.setSuccess(true);
            command.setStream(true);
            command.setFirstTokenLatency(Math.max(0L, System.currentTimeMillis() - startedAt));
            command.setCosts(0L);
            command.setCallTime(startedAt);
            appService.recordModelStatistic(command);
        } catch (RuntimeException ignored) {
        }
    }

    private String toSseFrame(AssistantConversationStreamResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"code\":0,");
        json.append("\"message\":\"\",");
        json.append("\"response\":\"").append(jsonEscape(result.getResponse())).append("\",");
        json.append("\"order\":0,");
        json.append("\"eventType\":0,");
        json.append("\"eventData\":null,");
        json.append("\"detailId\":\"").append(jsonEscape(result.getDetailId())).append("\",");
        json.append("\"conversationId\":\"").append(jsonEscape(result.getConversationId())).append("\",");
        json.append("\"finish\":1,");
        json.append("\"gen_file_url_list\":[],");
        json.append("\"search_list\":[],");
        json.append("\"responseFiles\":[]");
        json.append("}");
        return "data: " + json + "\n\n";
    }

    private String toRecommendSseFrames(String query, String assistantId, Map<String, Object> assistant) {
        String id = "recommend-" + compactText(assistantId, 48);
        String content = recommendContent(query, mapString(assistant, "name"));
        return "data: " + recommendChunk(id, content, "", "answer") + "\n\n"
                + "data: " + recommendChunk(id, "", "stop", "answer") + "\n\n";
    }

    private String recommendContent(String query, String assistantName) {
        String topic = compactText(query, 80);
        String name = compactText(assistantName, 40);
        String subject = isBlank(name) ? "this assistant" : name;
        return "Can " + subject + " explain " + topic + " in more detail?\n"
                + "What is the next step for " + topic + "?\n"
                + "Can you give a practical example about " + topic + "?";
    }

    private String recommendChunk(String id, String content, String finishReason, String contentType) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":\"").append(jsonEscape(id)).append("\",");
        json.append("\"object\":\"chat.completion.chunk\",");
        json.append("\"created\":0,");
        json.append("\"model\":\"local-recommend\",");
        json.append("\"choices\":[{");
        json.append("\"index\":0,");
        json.append("\"delta\":{\"role\":\"assistant\",\"content\":\"").append(jsonEscape(content)).append("\"},");
        json.append("\"finish_reason\":\"").append(jsonEscape(finishReason)).append("\",");
        json.append("\"logprobs\":null,");
        json.append("\"contentType\":\"").append(jsonEscape(contentType)).append("\"");
        json.append("}],");
        json.append("\"usage\":{\"prompt_tokens\":0,\"completion_tokens\":0,\"total_tokens\":0}");
        json.append("}");
        return json.toString();
    }

    private String mapString(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String configuredModelId(Map<String, Object> config) {
        if (config == null) {
            return "";
        }
        return firstNonBlank(
                nestedText(config, "modelConfig", "modelId"),
                nestedText(config, "modelConfig", "model_id"),
                nestedText(config, "modelConfig", "config", "modelId"),
                nestedText(config, "modelConfig", "config", "model_id"));
    }

    private String nestedText(Map<String, Object> map, String first, String second) {
        return mapString(objectMap(map == null ? null : map.get(first)), second);
    }

    private String nestedText(Map<String, Object> map, String first, String second, String third) {
        return mapString(objectMap(objectMap(map == null ? null : map.get(first)).get(second)), third);
    }

    private Map<String, Object> objectMap(Object value) {
        if (!(value instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<?, ?> source = (Map<?, ?>) value;
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String compactText(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String compacted = value.trim().replaceAll("\\s+", " ");
        if (compacted.length() <= maxLength) {
            return compacted;
        }
        return compacted.substring(0, maxLength);
    }

    private String errorJson(String message) {
        return "{\"code\":1001,\"msg\":\"" + jsonEscape(message) + "\",\"data\":null}";
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(ch);
                    break;
            }
        }
        return escaped.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    public static class OpenUrlConversationCreateRequest {
        private String prompt;

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    public static class OpenUrlConversationIdRequest {
        private String conversationId;
        private String detailId;

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public String getDetailId() {
            return detailId;
        }

        public void setDetailId(String detailId) {
            this.detailId = detailId;
        }
    }

    public static class OpenUrlConversationStreamRequest {
        private String conversationId;
        private String prompt;
        private List<Map<String, Object>> fileInfo = Collections.emptyList();

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public List<Map<String, Object>> getFileInfo() {
            return fileInfo;
        }

        public void setFileInfo(List<Map<String, Object>> fileInfo) {
            this.fileInfo = fileInfo;
        }
    }

    public static class OpenUrlQuestionRecommendRequest {
        private String conversationId;
        private String query;

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}
