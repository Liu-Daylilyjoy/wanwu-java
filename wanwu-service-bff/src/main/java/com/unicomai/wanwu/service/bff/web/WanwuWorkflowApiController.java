package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowExportQuery;
import com.unicomai.wanwu.api.app.dto.WorkflowExportResult;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow/api")
public class WanwuWorkflowApiController {

    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
    private static final String WORKFLOW_APP_TYPE = "workflow";
    private static final String STAT_SOURCE_WEB = "web";
    private static final ObjectMapper JSON = new ObjectMapper();

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    public WanwuWorkflowApiController() {
    }

    public WanwuWorkflowApiController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/workflow/parameter")
    public FrontendResponse<Map<String, Object>> workflowParameter(
            @RequestHeader(value = "x-user-id", required = false) String userId,
            @RequestHeader(value = "x-org-id", required = false) String orgId,
            @RequestParam Map<String, String> request) {
        try {
            String workflowId = workflowId(request);
            WorkflowExportResult workflow = exportWorkflow(userId, orgId, workflowId, false);
            Map<String, Object> body = workflowIdentity(workflowId);
            String schema = workflow == null ? "" : defaultIfBlank(workflow.getSchema(), "");
            List<Map<String, Object>> inputs = workflowInputs(schema);
            List<Map<String, Object>> outputs = workflowOutputs(schema);
            body.put("name", workflow == null ? "" : defaultIfBlank(workflow.getName(), ""));
            body.put("desc", workflow == null ? "" : defaultIfBlank(workflow.getDesc(), ""));
            body.put("schema", schema);
            body.put("parameters", inputs);
            body.put("inputs", inputs);
            body.put("outputs", outputs);
            return FrontendResponse.ok(body);
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/api/workflow/use")
    public FrontendResponse<Map<String, Object>> useWorkflow(
            @RequestHeader(value = "x-user-id", required = false) String userId,
            @RequestHeader(value = "x-org-id", required = false) String orgId,
            @RequestBody(required = false) Map<String, Object> request) {
        long startedAt = System.currentTimeMillis();
        String safeUserId = defaultIfBlank(userId, DEV_USER_ID);
        String safeOrgId = defaultIfBlank(orgId, DEV_ORG_ID);
        String workflowId = "";
        try {
            Map<String, Object> body = request == null ? Collections.<String, Object>emptyMap() : request;
            workflowId = workflowId(body);
            WorkflowRunCommand command = new WorkflowRunCommand();
            command.setWorkflowId(workflowId);
            command.setInput(workflowInput(body));
            command.setUserId(safeUserId);
            command.setOrgId(safeOrgId);
            WorkflowRunResult result = appService.runWorkflow(command);
            recordWorkflowStatistic(safeUserId, safeOrgId, workflowId, true, startedAt);
            return FrontendResponse.ok(workflowRunResult(result));
        } catch (IllegalArgumentException ex) {
            recordWorkflowStatistic(safeUserId, safeOrgId, workflowId, false, startedAt);
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private void recordWorkflowStatistic(String userId,
                                         String orgId,
                                         String workflowId,
                                         boolean success,
                                         long startedAt) {
        if (appService == null || isBlank(workflowId)) {
            return;
        }
        try {
            RecordAppStatisticCommand command = new RecordAppStatisticCommand();
            command.setUserId(defaultIfBlank(userId, DEV_USER_ID));
            command.setOrgId(defaultIfBlank(orgId, DEV_ORG_ID));
            command.setAppId(workflowId);
            command.setAppType(WORKFLOW_APP_TYPE);
            command.setSuccess(success);
            command.setStream(false);
            command.setNonStreamCosts(elapsedMillis(startedAt));
            command.setSource(STAT_SOURCE_WEB);
            appService.recordAppStatistic(command);
        } catch (RuntimeException ignored) {
        }
    }

    @GetMapping({"/api/workflow_api/get_process", "/api/workflow/get_process"})
    public FrontendResponse<Map<String, Object>> workflowProcess(
            @RequestHeader(value = "x-user-id", required = false) String userId,
            @RequestHeader(value = "x-org-id", required = false) String orgId,
            @RequestParam Map<String, String> request) {
        try {
            String workflowId = workflowId(request);
            String runId = defaultIfBlank(request.get("execute_id"), request.get("executeId"));
            return FrontendResponse.ok(appService.getWorkflowRunProcess(
                    defaultIfBlank(userId, DEV_USER_ID),
                    defaultIfBlank(orgId, DEV_ORG_ID),
                    workflowId,
                    runId));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/workflow/openapi_schema")
    public FrontendResponse<Map<String, Object>> openapiSchema(
            @RequestHeader(value = "x-user-id", required = false) String userId,
            @RequestHeader(value = "x-org-id", required = false) String orgId,
            @RequestParam Map<String, String> request) {
        try {
            String workflowId = workflowId(request);
            WorkflowExportResult workflow = exportWorkflow(userId, orgId, workflowId, false);
            String schema = openApiSchema(workflowId, workflow);
            Map<String, Object> body = workflowIdentity(workflowId);
            body.put("base64OpenAPISchema", Base64.getEncoder()
                    .encodeToString(schema.getBytes(StandardCharsets.UTF_8)));
            body.put("openAPISchema", schema);
            body.put("schema", workflow == null ? "" : defaultIfBlank(workflow.getSchema(), ""));
            return FrontendResponse.ok(body);
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private WorkflowExportResult exportWorkflow(String userId, String orgId, String workflowId, boolean published) {
        WorkflowExportQuery query = new WorkflowExportQuery();
        query.setWorkflowId(workflowId);
        query.setPublished(published);
        query.setUserId(defaultIfBlank(userId, DEV_USER_ID));
        query.setOrgId(defaultIfBlank(orgId, DEV_ORG_ID));
        return appService.exportWorkflow(query);
    }

    private Map<String, Object> workflowRunResult(WorkflowRunResult result) {
        String workflowId = result == null ? "" : defaultIfBlank(result.getWorkflowId(), "");
        Map<String, Object> body = workflowIdentity(workflowId);
        body.put("runId", result == null ? "" : defaultIfBlank(result.getRunId(), ""));
        body.put("run_id", result == null ? "" : defaultIfBlank(result.getRunId(), ""));
        body.put("status", result == null ? "" : defaultIfBlank(result.getStatus(), ""));
        body.put("createdAt", result == null ? 0L : result.getCreatedAt());
        body.put("finishedAt", result == null ? 0L : result.getFinishedAt());
        body.put("costMillis", result == null ? 0L : result.getCostMillis());
        body.put("output", result == null || result.getOutput() == null
                ? Collections.emptyMap()
                : result.getOutput());
        return body;
    }

    private Map<String, Object> workflowIdentity(String workflowId) {
        String safeWorkflowId = defaultIfBlank(workflowId, "");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workflow_id", safeWorkflowId);
        body.put("workflowId", safeWorkflowId);
        return body;
    }

    private Map<String, Object> workflowInput(Map<String, Object> request) {
        Object parameters = request.get("parameters");
        if (parameters instanceof Map) {
            return objectMap((Map<?, ?>) parameters);
        }
        Object input = request.get("input");
        if (input instanceof Map) {
            return objectMap((Map<?, ?>) input);
        }
        Map<String, Object> result = objectMap(request);
        result.remove("workflow_id");
        result.remove("workflowId");
        result.remove("workflowID");
        result.remove("uuid");
        result.remove("UUID");
        return result;
    }

    private Map<String, Object> objectMap(Map<?, ?> source) {
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

    private List<Map<String, Object>> workflowInputs(String schema) {
        JsonNode root = readSchema(schema);
        List<Map<String, Object>> explicit = explicitParams(root, "parameters");
        if (!explicit.isEmpty()) {
            return explicit;
        }
        explicit = explicitParams(root, "inputs");
        if (!explicit.isEmpty()) {
            return explicit;
        }
        return openApiInputs(root);
    }

    private List<Map<String, Object>> workflowOutputs(String schema) {
        JsonNode root = readSchema(schema);
        List<Map<String, Object>> explicit = explicitParams(root, "outputs");
        if (!explicit.isEmpty()) {
            return explicit;
        }
        return openApiOutputs(root);
    }

    private JsonNode readSchema(String schema) {
        if (isBlank(schema)) {
            return JSON.createObjectNode();
        }
        try {
            return JSON.readTree(schema);
        } catch (IOException ex) {
            return JSON.createObjectNode();
        }
    }

    private List<Map<String, Object>> explicitParams(JsonNode root, String field) {
        List<Map<String, Object>> result = new ArrayList<>();
        JsonNode params = root.path(field);
        if (!params.isArray()) {
            return result;
        }
        for (JsonNode param : params) {
            result.add(explicitParam(param));
        }
        return result;
    }

    private Map<String, Object> explicitParam(JsonNode param) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", param.path("name").asText(param.path("key").asText("")));
        row.put("type", schemaType(param));
        row.put("description", param.path("description").asText(param.path("desc").asText("")));
        row.put("desc", row.get("description"));
        row.put("required", param.path("required").asBoolean(false));
        if ("list".equals(row.get("type"))) {
            Map<String, Object> item = new LinkedHashMap<>();
            JsonNode schema = param.path("schema");
            JsonNode items = schema.isMissingNode() ? param.path("items") : schema;
            item.put("type", schemaType(items));
            item.put("children", schemaProperties(items.path("properties"), items.path("required")));
            row.put("schema", item);
        } else {
            row.put("children", schemaProperties(param.path("properties"), param.path("required")));
        }
        return row;
    }

    private List<Map<String, Object>> openApiInputs(JsonNode root) {
        JsonNode operation = firstOpenApiOperation(root);
        List<Map<String, Object>> result = new ArrayList<>();
        JsonNode parameters = operation.path("parameters");
        if (parameters.isArray()) {
            for (JsonNode parameter : parameters) {
                result.add(openApiParameter(parameter));
            }
        }
        JsonNode content = operation.path("requestBody").path("content");
        Iterator<Map.Entry<String, JsonNode>> mediaTypes = content.fields();
        while (mediaTypes.hasNext()) {
            JsonNode schema = mediaTypes.next().getValue().path("schema");
            result.addAll(schemaProperties(schema.path("properties"), schema.path("required")));
        }
        return result;
    }

    private List<Map<String, Object>> openApiOutputs(JsonNode root) {
        JsonNode operation = firstOpenApiOperation(root);
        JsonNode responses = operation.path("responses");
        JsonNode response = firstSuccessResponse(responses);
        Iterator<Map.Entry<String, JsonNode>> mediaTypes = response.path("content").fields();
        while (mediaTypes.hasNext()) {
            JsonNode schema = mediaTypes.next().getValue().path("schema");
            return schemaProperties(schema.path("properties"), schema.path("required"));
        }
        return Collections.emptyList();
    }

    private JsonNode firstOpenApiOperation(JsonNode root) {
        JsonNode paths = root.path("paths");
        Iterator<Map.Entry<String, JsonNode>> pathIterator = paths.fields();
        while (pathIterator.hasNext()) {
            JsonNode path = pathIterator.next().getValue();
            Iterator<Map.Entry<String, JsonNode>> methodIterator = path.fields();
            while (methodIterator.hasNext()) {
                return methodIterator.next().getValue();
            }
        }
        return JSON.createObjectNode();
    }

    private JsonNode firstSuccessResponse(JsonNode responses) {
        Iterator<Map.Entry<String, JsonNode>> iterator = responses.fields();
        JsonNode fallback = JSON.createObjectNode();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            if (fallback.isMissingNode() || fallback.size() == 0) {
                fallback = entry.getValue();
            }
            if (entry.getKey().startsWith("2")) {
                return entry.getValue();
            }
        }
        return fallback;
    }

    private Map<String, Object> openApiParameter(JsonNode parameter) {
        Map<String, Object> row = new LinkedHashMap<>();
        String location = parameter.path("in").asText("");
        String name = parameter.path("name").asText("");
        row.put("name", isBlank(location) ? name : location + "-" + name);
        row.put("type", schemaType(parameter.path("schema")));
        row.put("description", parameter.path("description").asText(""));
        row.put("desc", row.get("description"));
        row.put("required", parameter.path("required").asBoolean(false));
        if ("list".equals(row.get("type"))) {
            Map<String, Object> item = new LinkedHashMap<>();
            JsonNode items = parameter.path("schema").path("items");
            item.put("type", schemaType(items));
            item.put("children", schemaProperties(items.path("properties"), items.path("required")));
            row.put("schema", item);
        } else {
            row.put("children", schemaProperties(parameter.path("schema").path("properties"),
                    parameter.path("schema").path("required")));
        }
        return row;
    }

    private List<Map<String, Object>> schemaProperties(JsonNode properties, JsonNode required) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!properties.isObject()) {
            return result;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode schema = field.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            String type = schemaType(schema);
            row.put("name", field.getKey());
            row.put("type", type);
            row.put("description", schema.path("description").asText(""));
            row.put("desc", row.get("description"));
            row.put("required", requiredContains(required, field.getKey()));
            if ("list".equals(type)) {
                Map<String, Object> item = new LinkedHashMap<>();
                JsonNode items = schema.path("items");
                item.put("type", schemaType(items));
                item.put("children", schemaProperties(items.path("properties"), items.path("required")));
                row.put("schema", item);
            } else {
                row.put("children", schemaProperties(schema.path("properties"), schema.path("required")));
            }
            result.add(row);
        }
        return result;
    }

    private boolean requiredContains(JsonNode required, String name) {
        if (!required.isArray()) {
            return false;
        }
        for (JsonNode item : required) {
            if (name.equals(item.asText())) {
                return true;
            }
        }
        return false;
    }

    private String schemaType(JsonNode schema) {
        String type = schema.path("type").asText("string");
        if ("array".equals(type)) {
            return "list";
        }
        if ("number".equals(type)) {
            return "float";
        }
        if ("integer".equals(type) || "boolean".equals(type) || "object".equals(type) || "string".equals(type)) {
            return type;
        }
        return isBlank(type) ? "string" : type;
    }

    private String workflowId(Map<?, ?> request) {
        if (request == null) {
            return "";
        }
        List<String> keys = new ArrayList<>();
        keys.add("workflow_id");
        keys.add("workflowId");
        keys.add("workflowID");
        keys.add("uuid");
        keys.add("UUID");
        for (String key : keys) {
            Object value = request.get(key);
            if (value != null && !isBlank(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private String openApiSchema(String workflowId, WorkflowExportResult workflow) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("openapi", "3.0.1");
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", defaultIfBlank(workflow == null ? "" : workflow.getName(), defaultIfBlank(workflowId, "Workflow")));
        info.put("description", workflow == null ? "" : defaultIfBlank(workflow.getDesc(), ""));
        info.put("version", "1.0.0");
        doc.put("info", info);
        Map<String, Object> paths = new LinkedHashMap<>();
        Map<String, Object> runPath = new LinkedHashMap<>();
        Map<String, Object> post = new LinkedHashMap<>();
        post.put("operationId", "runWorkflow");
        post.put("summary", "Run workflow");
        post.put("requestBody", requestBodySchema());
        post.put("responses", successResponses());
        runPath.put("post", post);
        paths.put("/workflow/run", runPath);
        doc.put("paths", paths);
        doc.put("x-wanwu-workflow-id", defaultIfBlank(workflowId, ""));
        doc.put("x-wanwu-workflow-schema", workflow == null ? "" : defaultIfBlank(workflow.getSchema(), ""));
        try {
            return JSON.writeValueAsString(doc);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("workflow openapi schema serialization failed", ex);
        }
    }

    private Map<String, Object> requestBodySchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", true);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("application/json", Collections.singletonMap("schema", schema));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("required", false);
        body.put("content", content);
        return body;
    }

    private Map<String, Object> successResponses() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", true);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("application/json", Collections.singletonMap("schema", schema));
        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("description", "Workflow run result");
        ok.put("content", content);
        return Collections.singletonMap("200", ok);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0L, System.currentTimeMillis() - startedAt);
    }
}
