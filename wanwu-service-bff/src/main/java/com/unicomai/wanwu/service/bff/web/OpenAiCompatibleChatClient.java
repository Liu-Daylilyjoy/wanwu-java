package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.model.dto.ModelInfo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class OpenAiCompatibleChatClient {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int READ_TIMEOUT_MILLIS = 30000;

    ChatCompletionResult complete(ModelInfo model, String modelId, String prompt) {
        return complete(model, modelId, userMessages(prompt));
    }

    ChatCompletionResult complete(ModelInfo model, String modelId, List<Map<String, Object>> messages) {
        ChatCompletionResult streamResult = stream(model, modelId, messages);
        if (!isBlank(streamResult.getContent())) {
            return streamResult;
        }
        return answer(model, modelId, messages);
    }

    long estimateTokens(String value) {
        String text = defaultIfBlank(value, "");
        if (isBlank(text)) {
            return 0L;
        }
        return Math.max(1L, (text.length() + 3L) / 4L);
    }

    private ChatCompletionResult stream(ModelInfo model, String modelId, List<Map<String, Object>> messages) {
        if (model == null || model.getConfig() == null) {
            return ChatCompletionResult.empty();
        }
        try {
            String endpoint = firstText(model.getConfig(), "endpointUrl", "inferUrl", "baseUrl", "url");
            String apiKey = firstText(model.getConfig(), "apiKey");
            if (isBlank(endpoint) || isBlank(apiKey) || isDevelopmentApiKey(apiKey)) {
                return ChatCompletionResult.empty();
            }
            Map<String, Object> payload = payload(model, modelId, messages, true);
            return postStream(modelEndpointUrl(endpoint, "/chat/completions"),
                    apiKey, JSON.writeValueAsString(payload));
        } catch (RuntimeException | IOException ignored) {
            return ChatCompletionResult.empty();
        }
    }

    private ChatCompletionResult answer(ModelInfo model, String modelId, List<Map<String, Object>> messages) {
        if (model == null || model.getConfig() == null) {
            return ChatCompletionResult.empty();
        }
        try {
            String endpoint = firstText(model.getConfig(), "endpointUrl", "inferUrl", "baseUrl", "url");
            String apiKey = firstText(model.getConfig(), "apiKey");
            if (isBlank(endpoint) || isBlank(apiKey) || isDevelopmentApiKey(apiKey)) {
                return ChatCompletionResult.empty();
            }
            Map<String, Object> payload = payload(model, modelId, messages, false);
            String response = postJson(modelEndpointUrl(endpoint, "/chat/completions"),
                    apiKey, JSON.writeValueAsString(payload));
            return extractAnswer(response);
        } catch (RuntimeException | IOException ignored) {
            return ChatCompletionResult.empty();
        }
    }

    private Map<String, Object> payload(ModelInfo model,
                                        String modelId,
                                        List<Map<String, Object>> messages,
                                        boolean stream) {
        List<Map<String, Object>> safeMessages = sanitizeMessages(messages);
        if (safeMessages.isEmpty()) {
            safeMessages = userMessages("");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", defaultIfBlank(model == null ? "" : model.getModel(), modelId));
        payload.put("messages", safeMessages);
        payload.put("stream", stream);
        return payload;
    }

    private List<Map<String, Object>> userMessages(String prompt) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", defaultIfBlank(prompt, ""));
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);
        return messages;
    }

    private List<Map<String, Object>> sanitizeMessages(List<Map<String, Object>> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> source : messages) {
            if (source == null) {
                continue;
            }
            String role = firstText(source, "role");
            String content = firstText(source, "content");
            if (isBlank(role) || isBlank(content)) {
                continue;
            }
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", role);
            message.put("content", content);
            result.add(message);
        }
        return result;
    }

    private ChatCompletionResult postStream(String endpoint, String apiKey, String json) throws IOException {
        HttpURLConnection connection = openConnection(endpoint, apiKey, "text/event-stream");
        try (OutputStream body = connection.getOutputStream()) {
            body.write(json.getBytes(StandardCharsets.UTF_8));
        }
        try {
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (status >= 400) {
                throw new IOException("chat upstream returned " + status);
            }
            String contentType = defaultIfBlank(connection.getHeaderField("Content-Type"), "");
            if (!isBlank(contentType) && !contentType.toLowerCase().contains("text/event-stream")) {
                return ChatCompletionResult.empty();
            }
            return readStream(stream);
        } finally {
            connection.disconnect();
        }
    }

    private String postJson(String endpoint, String apiKey, String json) throws IOException {
        HttpURLConnection connection = openConnection(endpoint, apiKey, "application/json");
        try (OutputStream body = connection.getOutputStream()) {
            body.write(json.getBytes(StandardCharsets.UTF_8));
        }
        try {
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String response = readRaw(stream);
            if (status >= 400) {
                throw new IOException("chat upstream returned " + status);
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection(String endpoint, String apiKey, String accept) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", accept);
        connection.setDoOutput(true);
        return connection;
    }

    private ChatCompletionResult readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return ChatCompletionResult.empty();
        }
        StringBuilder answer = new StringBuilder();
        Usage usage = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    StreamChunk chunk = parseStreamChunk(line.substring("data:".length()).trim());
                    answer.append(chunk.content);
                    if (chunk.usage != null) {
                        usage = chunk.usage;
                    }
                }
            }
        }
        return new ChatCompletionResult(answer.toString(), usage);
    }

    @SuppressWarnings("unchecked")
    private StreamChunk parseStreamChunk(String data) {
        if (isBlank(data) || "[DONE]".equals(data)) {
            return StreamChunk.empty();
        }
        try {
            Map<String, Object> root = JSON.readValue(data, Map.class);
            Map<String, Object> choice = firstMap(root.get("choices"));
            Map<String, Object> delta = objectMap(choice.get("delta"));
            Map<String, Object> message = objectMap(choice.get("message"));
            String content = firstNonBlank(firstText(delta, "content"), firstText(message, "content"));
            return new StreamChunk(content, usage(objectMap(root.get("usage"))));
        } catch (IOException ignored) {
            return StreamChunk.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private ChatCompletionResult extractAnswer(String response) throws IOException {
        if (isBlank(response)) {
            return ChatCompletionResult.empty();
        }
        Map<String, Object> root = JSON.readValue(response, Map.class);
        Map<String, Object> choice = firstMap(root.get("choices"));
        Map<String, Object> message = objectMap(choice.get("message"));
        Map<String, Object> delta = objectMap(choice.get("delta"));
        String content = firstNonBlank(firstText(message, "content"), firstText(delta, "content"));
        return new ChatCompletionResult(content, usage(objectMap(root.get("usage"))));
    }

    private Usage usage(Map<String, Object> usage) {
        if (usage == null || usage.isEmpty()) {
            return null;
        }
        return new Usage(
                longValue(usage, "prompt_tokens"),
                longValue(usage, "completion_tokens"),
                longValue(usage, "total_tokens"));
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

    private Map<String, Object> firstMap(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyMap();
        }
        List<?> list = (List<?>) value;
        if (list.isEmpty() || !(list.get(0) instanceof Map)) {
            return Collections.emptyMap();
        }
        return objectMap(list.get(0));
    }

    private String firstText(Map<String, Object> map, String... keys) {
        if (keys == null) {
            return "";
        }
        for (String key : keys) {
            String value = text(map, key);
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String text(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
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

    private String modelEndpointUrl(String endpoint, String suffix) {
        String base = trimTrailingSlash(endpoint);
        if (base.endsWith(suffix)) {
            return base;
        }
        return base + suffix;
    }

    private String trimTrailingSlash(String value) {
        String result = defaultIfBlank(value, "");
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String readRaw(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private long longValue(Map<String, Object> payload, String key) {
        Object value = payload == null ? null : payload.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private boolean isDevelopmentApiKey(String value) {
        return "dev-model-key".equals(value)
                || "useless-api-key".equals(value)
                || "it-is-not-your-api-key".equals(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    static final class ChatCompletionResult {
        private final String content;
        private final Usage usage;

        private ChatCompletionResult(String content, Usage usage) {
            this.content = content;
            this.usage = usage;
        }

        static ChatCompletionResult empty() {
            return new ChatCompletionResult("", null);
        }

        String getContent() {
            return content;
        }

        Usage getUsage() {
            return usage;
        }
    }

    private static final class StreamChunk {
        private final String content;
        private final Usage usage;

        private StreamChunk(String content, Usage usage) {
            this.content = content;
            this.usage = usage;
        }

        private static StreamChunk empty() {
            return new StreamChunk("", null);
        }
    }

    static final class Usage {
        private final long promptTokens;
        private final long completionTokens;
        private final long totalTokens;

        private Usage(long promptTokens, long completionTokens, long totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }

        long getPromptTokens() {
            return promptTokens;
        }

        long getCompletionTokens() {
            return completionTokens;
        }

        long getTotalTokens() {
            return totalTokens;
        }
    }
}
