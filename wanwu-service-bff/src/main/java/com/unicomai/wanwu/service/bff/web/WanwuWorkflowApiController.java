package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow/api")
public class WanwuWorkflowApiController {

    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
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
            body.put("name", workflow == null ? "" : defaultIfBlank(workflow.getName(), ""));
            body.put("desc", workflow == null ? "" : defaultIfBlank(workflow.getDesc(), ""));
            body.put("schema", workflow == null ? "" : defaultIfBlank(workflow.getSchema(), ""));
            body.put("parameters", Collections.emptyList());
            body.put("inputs", Collections.emptyList());
            body.put("outputs", Collections.emptyList());
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
        try {
            Map<String, Object> body = request == null ? Collections.<String, Object>emptyMap() : request;
            String workflowId = workflowId(body);
            WorkflowRunCommand command = new WorkflowRunCommand();
            command.setWorkflowId(workflowId);
            command.setInput(workflowInput(body));
            command.setUserId(defaultIfBlank(userId, DEV_USER_ID));
            command.setOrgId(defaultIfBlank(orgId, DEV_ORG_ID));
            return FrontendResponse.ok(workflowRunResult(appService.runWorkflow(command)));
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
}
