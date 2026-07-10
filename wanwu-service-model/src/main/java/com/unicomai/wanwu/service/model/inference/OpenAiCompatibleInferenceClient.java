package com.unicomai.wanwu.service.model.inference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelInvokeCommand;
import com.unicomai.wanwu.api.model.dto.ModelInvokeResult;

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

public class OpenAiCompatibleInferenceClient {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int READ_TIMEOUT_MILLIS = 60000;

    public ModelInvokeResult invoke(ModelInfo model, ModelInvokeCommand command) {
        if (model == null || command == null) {
            throw new IllegalArgumentException("model invocation is required");
        }
        Map<String, Object> config = model.getConfig();
        String endpoint = firstText(config, "endpointUrl", "inferUrl", "baseUrl", "url");
        String apiKey = firstText(config, "apiKey");
        if (isBlank(endpoint) || isBlank(apiKey) || isDevelopmentApiKey(apiKey)) {
            throw new IllegalStateException("model inference configuration is unavailable");
        }
        String operation = normalizeOperation(command.getOperation());
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.putAll(command.getPayload() == null
                ? Collections.<String, Object>emptyMap()
                : command.getPayload());
        if (isBlank(string(payload.get("model")))) {
            payload.put("model", defaultIfBlank(model.getModel(), command.getModelId()));
        }
        boolean stream = "chat".equals(operation) && booleanValue(payload.get("stream"));
        String url = modelEndpointUrl(endpoint, endpointSuffix(operation));
        try {
            return stream
                    ? postStream(url, apiKey, JSON.writeValueAsString(payload))
                    : postJson(url, apiKey, JSON.writeValueAsString(payload), operation);
        } catch (IOException ex) {
            throw new IllegalStateException("model inference request failed", ex);
        }
    }

    private ModelInvokeResult postStream(String endpoint, String apiKey, String json) throws IOException {
        HttpURLConnection connection = openConnection(endpoint, apiKey, "text/event-stream");
        writeBody(connection, json);
        try {
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (status >= 400) {
                readRaw(stream);
                throw new IOException("model upstream returned " + status);
            }
            return readSse(stream);
        } finally {
            connection.disconnect();
        }
    }

    @SuppressWarnings("unchecked")
    private ModelInvokeResult postJson(String endpoint, String apiKey, String json, String operation)
            throws IOException {
        HttpURLConnection connection = openConnection(endpoint, apiKey, "application/json");
        writeBody(connection, json);
        try {
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String raw = readRaw(stream);
            if (status >= 400) {
                throw new IOException("model upstream returned " + status);
            }
            Map<String, Object> response = isBlank(raw)
                    ? new LinkedHashMap<String, Object>()
                    : JSON.readValue(raw, Map.class);
            ModelInvokeResult result = new ModelInvokeResult();
            result.setResponse(response);
            result.setUsage(map(response.get("usage")));
            if ("chat".equals(operation)) {
                String content = chatContent(response);
                result.setContent(content);
                result.setChunks(isBlank(content)
                        ? Collections.<String>emptyList()
                        : Collections.singletonList(content));
            }
            return result;
        } finally {
            connection.disconnect();
        }
    }

    private ModelInvokeResult readSse(InputStream stream) throws IOException {
        ModelInvokeResult result = new ModelInvokeResult();
        if (stream == null) {
            return result;
        }
        List<String> chunks = new ArrayList<String>();
        StringBuilder content = new StringBuilder();
        Map<String, Object> usage = new LinkedHashMap<String, Object>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring("data:".length()).trim();
                if (isBlank(data) || "[DONE]".equals(data)) {
                    continue;
                }
                Map<String, Object> event = readMap(data);
                String delta = chatContent(event);
                if (!isBlank(delta)) {
                    chunks.add(delta);
                    content.append(delta);
                }
                Map<String, Object> eventUsage = map(event.get("usage"));
                if (!eventUsage.isEmpty()) {
                    usage = eventUsage;
                }
            }
        } finally {
            reader.close();
        }
        result.setContent(content.toString());
        result.setChunks(chunks);
        result.setUsage(usage);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readMap(String json) {
        try {
            return JSON.readValue(json, Map.class);
        } catch (IOException ex) {
            return Collections.emptyMap();
        }
    }

    private String chatContent(Map<String, Object> response) {
        List<Object> choices = list(response.get("choices"));
        if (choices.isEmpty()) {
            return "";
        }
        Map<String, Object> choice = map(choices.get(0));
        Map<String, Object> delta = map(choice.get("delta"));
        Map<String, Object> message = map(choice.get("message"));
        return firstNonBlank(string(delta.get("content")), string(message.get("content")));
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

    private void writeBody(HttpURLConnection connection, String json) throws IOException {
        OutputStream output = connection.getOutputStream();
        try {
            output.write(json.getBytes(StandardCharsets.UTF_8));
        } finally {
            output.close();
        }
    }

    private String readRaw(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[2048];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } finally {
            stream.close();
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private String normalizeOperation(String operation) {
        String value = defaultIfBlank(operation, "").trim().toLowerCase();
        if ("chat".equals(value) || "embeddings".equals(value) || "rerank".equals(value)
                || "multimodal-embeddings".equals(value) || "multimodal-rerank".equals(value)) {
            return value;
        }
        throw new IllegalArgumentException("unsupported model operation: " + operation);
    }

    private String endpointSuffix(String operation) {
        if ("chat".equals(operation)) {
            return "/chat/completions";
        }
        return "/" + operation;
    }

    private String modelEndpointUrl(String endpoint, String suffix) {
        String base = defaultIfBlank(endpoint, "");
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base.endsWith(suffix) ? base : base + suffix;
    }

    private boolean isDevelopmentApiKey(String value) {
        return "dev-model-key".equals(value)
                || "useless-api-key".equals(value)
                || "it-is-not-your-api-key".equals(value);
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return "true".equalsIgnoreCase(string(value)) || "1".equals(string(value));
    }

    @SuppressWarnings("unchecked")
    private List<Object> list(Object value) {
        return value instanceof List ? (List<Object>) value : Collections.<Object>emptyList();
    }

    private Map<String, Object> map(Object value) {
        if (!(value instanceof Map)) {
            return new LinkedHashMap<String, Object>();
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private String firstText(Map<String, Object> values, String... keys) {
        for (String key : keys) {
            String value = string(values == null ? null : values.get(key));
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
