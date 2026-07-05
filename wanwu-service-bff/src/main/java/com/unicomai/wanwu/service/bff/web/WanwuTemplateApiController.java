package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateResult;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuTemplateApiController {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    public WanwuTemplateApiController() {
    }

    public WanwuTemplateApiController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/assistant/template/list")
    public FrontendResponse<Map<String, Object>> listAssistantTemplates(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "name", required = false) String name) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : assistantTemplateRows(category, name)) {
            if (!matches(item, category, name)) {
                continue;
            }
            rows.add(copy(item));
        }
        return FrontendResponse.ok(listResult(rows));
    }

    @GetMapping("/assistant/template")
    public FrontendResponse<Map<String, Object>> getAssistantTemplate(
            @RequestParam("assistantTemplateId") String assistantTemplateId) {
        return FrontendResponse.ok(copy(assistantTemplateDetail(assistantTemplateId)));
    }

    @PostMapping("/assistant/template")
    public FrontendResponse<AssistantCreateResult> createAssistantFromTemplate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            UserContext ctx = userContext(authorization);
            Map<String, Object> body = body(request);
            Map<String, Object> template = assistantTemplateDetail(text(body, "assistantTemplateId"));
            AssistantCreateCommand command = new AssistantCreateCommand();
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            command.setCategory(1);
            command.setName(defaultIfBlank(text(body, "name"), text(template, "name") + " Copy"));
            command.setDesc(defaultIfBlank(text(body, "desc"), text(template, "desc")));
            Map<String, Object> avatar = avatar(body.get("avatar") instanceof Map ? body.get("avatar") : template.get("avatar"));
            command.setAvatarKey(text(avatar, "key"));
            command.setAvatarPath(text(avatar, "path"));
            AssistantCreateResult created = appService.createAssistant(command);
            updateAssistantConfig(ctx, created, template);
            return FrontendResponse.ok(created);
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/workflow/template/list")
    public FrontendResponse<Map<String, Object>> listWorkflowTemplates(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "name", required = false) String name) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : workflowTemplateRows(category, name)) {
            if (!matches(item, category, name)) {
                continue;
            }
            rows.add(workflowListItem(item));
        }
        Map<String, Object> data = listResult(rows);
        data.put("downloadLink", Collections.singletonMap("url", ""));
        return FrontendResponse.ok(data);
    }

    @GetMapping("/workflow/template/detail")
    public FrontendResponse<Map<String, Object>> getWorkflowTemplate(
            @RequestParam("templateId") String templateId) {
        return FrontendResponse.ok(copy(workflowTemplateDetail(templateId)));
    }

    @GetMapping("/workflow/template/recommend")
    public FrontendResponse<Map<String, Object>> recommendWorkflowTemplates(
            @RequestParam(value = "templateId", required = false) String templateId) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : workflowTemplateRows("", "")) {
            if (!text(item, "templateId").equals(templateId)) {
                rows.add(workflowListItem(item));
            }
            if (rows.size() >= 3) {
                break;
            }
        }
        Map<String, Object> data = listResult(rows);
        data.put("downloadLink", Collections.singletonMap("url", ""));
        return FrontendResponse.ok(data);
    }

    @GetMapping("/workflow/template/download")
    public ResponseEntity<byte[]> downloadWorkflowTemplate(@RequestParam("templateId") String templateId) {
        Map<String, Object> template = workflowTemplateDetail(templateId);
        recordWorkflowTemplateDownload(templateId);
        String payload;
        try {
            payload = JSON.writeValueAsString(template.get("schema"));
        } catch (JsonProcessingException ex) {
            payload = "{}";
        }
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + templateId + ".json\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @PostMapping("/workflow/template")
    public FrontendResponse<Map<String, Object>> createWorkflowFromTemplate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            UserContext ctx = userContext(authorization);
            Map<String, Object> body = body(request);
            Map<String, Object> template = workflowTemplateDetail(text(body, "templateId"));
            WorkflowCreateCommand command = new WorkflowCreateCommand();
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            command.setName(defaultIfBlank(text(body, "name"), text(template, "name") + " Copy"));
            command.setDesc(defaultIfBlank(text(body, "desc"), text(template, "desc")));
            Map<String, Object> avatar = avatar(body.get("avatar") instanceof Map ? body.get("avatar") : template.get("avatar"));
            command.setAvatarKey(text(avatar, "key"));
            command.setAvatarPath(text(avatar, "path"));
            command.setSchema(toSchema(template.get("schema")));
            return FrontendResponse.ok(workflowCreateResult(appService.createWorkflow(command)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private void updateAssistantConfig(UserContext ctx, AssistantCreateResult created, Map<String, Object> template) {
        if (created == null || isBlank(created.getAssistantId())) {
            return;
        }
        AssistantConfigUpdateCommand config = new AssistantConfigUpdateCommand();
        config.setAssistantId(created.getAssistantId());
        config.setUserId(ctx.userId);
        config.setOrgId(ctx.orgId);
        config.setPrologue(text(template, "prologue"));
        config.setInstructions(text(template, "instructions"));
        config.setRecommendQuestion(stringList(template.get("recommendQuestion")));
        appService.updateAssistantConfig(config);
    }

    private Map<String, Object> workflowCreateResult(WorkflowCreateResult result) {
        String workflowId = result == null ? "" : defaultIfBlank(result.getWorkflowId(), "");
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("workflow_id", workflowId);
        body.put("workflowId", workflowId);
        return body;
    }

    private void recordWorkflowTemplateDownload(String templateId) {
        if (appService == null) {
            return;
        }
        try {
            appService.recordAppTemplateDownload("workflow", templateId);
        } catch (RuntimeException ignored) {
            // Go logs and continues when template download count recording fails.
        }
    }

    private List<Map<String, Object>> assistantTemplateRows(String category, String name) {
        return templateRows("assistant", category, name, assistantTemplates());
    }

    private Map<String, Object> assistantTemplateDetail(String assistantTemplateId) {
        return templateDetail("assistant", assistantTemplateId, "assistantTemplateId", assistantTemplates());
    }

    private List<Map<String, Object>> workflowTemplateRows(String category, String name) {
        return templateRows("workflow", category, name, workflowTemplates());
    }

    private Map<String, Object> workflowTemplateDetail(String templateId) {
        return templateDetail("workflow", templateId, "templateId", workflowTemplates());
    }

    private List<Map<String, Object>> templateRows(String templateType,
                                                   String category,
                                                   String name,
                                                   List<Map<String, Object>> fallback) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        if (appService != null) {
            try {
                List<Map<String, Object>> serviceRows = appService.listAppTemplates(templateType, category, name);
                if (serviceRows != null && !serviceRows.isEmpty()) {
                    rows.addAll(copyRows(serviceRows));
                }
            } catch (RuntimeException ignored) {
                // Keep the template square usable while AppService is still booting in development.
            }
        }
        String idKey = "assistant".equals(templateType) ? "assistantTemplateId" : "templateId";
        for (Map<String, Object> item : fallback) {
            if (matches(item, category, name) && !containsTemplate(rows, idKey, text(item, idKey))) {
                rows.add(copy(item));
            }
        }
        return rows;
    }

    private Map<String, Object> templateDetail(String templateType,
                                               String templateId,
                                               String idKey,
                                               List<Map<String, Object>> fallback) {
        if (appService != null) {
            try {
                Map<String, Object> template = appService.getAppTemplate(templateType, templateId);
                if (template != null && !template.isEmpty()) {
                    return copy(template);
                }
            } catch (RuntimeException ignored) {
                // Fall back to local seeds for the zero-change frontend compatibility path.
            }
        }
        for (Map<String, Object> item : fallback) {
            if (text(item, idKey).equals(templateId)) {
                return copy(item);
            }
        }
        throw new IllegalArgumentException(templateType + " template not found: " + templateId);
    }

    private List<Map<String, Object>> copyRows(List<Map<String, Object>> source) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : source) {
            rows.add(copy(item));
        }
        return rows;
    }

    private List<Map<String, Object>> assistantTemplates() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(assistantTemplate("assistant-template-policy", "industry", "Policy Analyst",
                "Summarize policy documents and produce action lists.",
                "I can help read policy documents.",
                "You are a precise policy analysis assistant.",
                "For policy, research, and compliance teams."));
        rows.add(assistantTemplate("assistant-template-learning", "edu", "Learning Coach",
                "Create learning plans and quizzes from a topic.",
                "Tell me what you want to learn.",
                "You are a patient learning coach.",
                "For education and training scenarios."));
        rows.add(assistantTemplate("assistant-template-support", "industry", "Support Assistant",
                "Classify customer issues and draft replies.",
                "Describe the customer issue.",
                "You are a calm customer support assistant.",
                "For customer service teams."));
        appendGoAssistantTemplates(rows);
        return rows;
    }

    @SuppressWarnings("unchecked")
    private void appendGoAssistantTemplates(List<Map<String, Object>> rows) {
        InputStream input = getClass().getClassLoader().getResourceAsStream("assistant-template-bundle.json");
        if (input == null) {
            return;
        }
        try {
            Map<String, Object> root = JSON.readValue(input, Map.class);
            for (Object item : listValue(root.get("assistantTemplates"))) {
                Map<String, Object> template = assistantTemplateFromBundle(mapValue(item));
                if (!containsTemplate(rows, "assistantTemplateId", text(template, "assistantTemplateId"))) {
                    rows.add(template);
                }
            }
        } catch (IOException ignored) {
            // Keep hard-coded development templates available if the generated Go bundle is absent or invalid.
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
            }
        }
    }

    private Map<String, Object> assistantTemplateFromBundle(Map<String, Object> source) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        String id = text(source, "assistantTemplateId");
        item.put("assistantTemplateId", id);
        item.put("appType", "agentTemplate");
        item.put("category", text(source, "category"));
        item.put("avatar", avatar(source.get("avatar")));
        item.put("name", defaultIfBlank(text(source, "name"), id));
        item.put("desc", text(source, "desc"));
        item.put("prologue", text(source, "prologue"));
        item.put("instructions", text(source, "instructions"));
        item.put("recommendQuestion", stringList(source.get("recommendQuestion")));
        item.put("summary", defaultIfBlank(text(source, "summary"), text(source, "desc")));
        item.put("feature", text(source, "feature"));
        item.put("scenario", text(source, "scenario"));
        item.put("workFlowInstruction", text(source, "workFlowInstruction"));
        return item;
    }

    private Map<String, Object> assistantTemplate(String id, String category, String name, String desc,
                                                  String prologue, String instructions, String scenario) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("assistantTemplateId", id);
        item.put("appType", "agentTemplate");
        item.put("category", category);
        item.put("avatar", avatar(""));
        item.put("name", name);
        item.put("desc", desc);
        item.put("prologue", prologue);
        item.put("instructions", instructions);
        item.put("recommendQuestion", Arrays.asList("What is the goal?", "What constraints matter?"));
        item.put("summary", desc);
        item.put("feature", "Structured output, follow-up questions, and concise summaries.");
        item.put("scenario", scenario);
        item.put("workFlowInstruction", "Optional workflow tools can be added after creation.");
        return item;
    }

    private Map<String, Object> assistantTemplate(String id) {
        for (Map<String, Object> item : assistantTemplates()) {
            if (text(item, "assistantTemplateId").equals(id)) {
                return item;
            }
        }
        throw new IllegalArgumentException("assistant template not found: " + id);
    }

    private List<Map<String, Object>> workflowTemplates() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(workflowTemplate("workflow-template-doc-review", "office", "Document Review",
                "Extract risks, owners, and next actions from a document.",
                "Review uploaded documents and return a structured checklist."));
        rows.add(workflowTemplate("workflow-template-faq", "service", "FAQ Generator",
                "Generate question-answer pairs from product notes.",
                "Turn product or help-center material into FAQ content."));
        rows.add(workflowTemplate("workflow-template-brief", "research", "Research Brief",
                "Collect inputs and create a concise research brief.",
                "Use this template for market, product, or policy research."));
        appendGoWorkflowTemplates(rows);
        return rows;
    }

    @SuppressWarnings("unchecked")
    private void appendGoWorkflowTemplates(List<Map<String, Object>> rows) {
        InputStream input = getClass().getClassLoader().getResourceAsStream("workflow-template-bundle.json");
        if (input == null) {
            return;
        }
        try {
            Map<String, Object> root = JSON.readValue(input, Map.class);
            for (Object item : listValue(root.get("workflowTemplates"))) {
                Map<String, Object> template = workflowTemplateFromBundle(mapValue(item));
                if (!containsTemplate(rows, "templateId", text(template, "templateId"))) {
                    rows.add(template);
                }
            }
        } catch (IOException ignored) {
            // Keep hard-coded development templates available if the generated Go bundle is absent or invalid.
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
            }
        }
    }

    private Map<String, Object> workflowTemplateFromBundle(Map<String, Object> source) {
        String id = text(source, "templateId");
        String name = defaultIfBlank(text(source, "name"), id);
        String desc = text(source, "desc");
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("templateId", id);
        item.put("avatar", avatar(source.get("avatar")));
        item.put("name", name);
        item.put("desc", desc);
        item.put("category", text(source, "category"));
        item.put("author", defaultIfBlank(text(source, "author"), "中国联通"));
        item.put("downloadCount", source.get("downloadCount") == null ? 0 : source.get("downloadCount"));
        item.put("summary", defaultIfBlank(text(source, "summary"), desc));
        item.put("feature", text(source, "feature"));
        item.put("scenario", text(source, "scenario"));
        item.put("note", text(source, "note"));
        Map<String, Object> schema = mapValue(source.get("schema"));
        item.put("schema", schema.isEmpty() ? workflowSchema(id, name, desc) : schema);
        return item;
    }

    private Map<String, Object> workflowTemplate(String id, String category, String name, String desc, String summary) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("templateId", id);
        item.put("avatar", avatar(""));
        item.put("name", name);
        item.put("desc", desc);
        item.put("category", category);
        item.put("author", "Wanwu Java");
        item.put("downloadCount", 0);
        item.put("summary", summary);
        item.put("feature", "Local Java development template with deterministic schema.");
        item.put("scenario", "Use it to create a workflow draft and continue editing in App Space.");
        item.put("note", "The runtime workflow engine is still a local compatibility shell.");
        item.put("schema", workflowSchema(id, name, desc));
        return item;
    }

    private Map<String, Object> workflowTemplate(String id) {
        for (Map<String, Object> item : workflowTemplates()) {
            if (text(item, "templateId").equals(id)) {
                return item;
            }
        }
        throw new IllegalArgumentException("workflow template not found: " + id);
    }

    private Map<String, Object> workflowListItem(Map<String, Object> source) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("templateId", source.get("templateId"));
        item.put("avatar", source.get("avatar"));
        item.put("name", source.get("name"));
        item.put("desc", source.get("desc"));
        item.put("category", source.get("category"));
        item.put("author", source.get("author"));
        item.put("downloadCount", source.get("downloadCount"));
        return item;
    }

    private Map<String, Object> workflowSchema(String id, String name, String desc) {
        Map<String, Object> schema = new LinkedHashMap<String, Object>();
        schema.put("id", id);
        schema.put("name", name);
        schema.put("desc", desc);
        schema.put("nodes", Collections.emptyList());
        schema.put("edges", Collections.emptyList());
        return schema;
    }

    private boolean matches(Map<String, Object> item, String category, String name) {
        if (!isBlank(category) && !"all".equalsIgnoreCase(category)
                && !category.equalsIgnoreCase(text(item, "category"))) {
            return false;
        }
        return isBlank(name) || text(item, "name").toLowerCase().contains(name.toLowerCase());
    }

    private boolean containsTemplate(List<Map<String, Object>> rows, String idKey, String templateId) {
        if (isBlank(templateId)) {
            return true;
        }
        for (Map<String, Object> row : rows) {
            if (templateId.equals(text(row, idKey))) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> listResult(List<Map<String, Object>> rows) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("list", rows);
        result.put("total", rows.size());
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> body(Map<String, Object> request) {
        return request == null ? Collections.<String, Object>emptyMap() : request;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> copy(Map<String, Object> source) {
        return source == null ? new LinkedHashMap<String, Object>() : new LinkedHashMap<String, Object>(source);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Collections.<String, Object>emptyMap();
    }

    @SuppressWarnings("unchecked")
    private List<Object> listValue(Object value) {
        return value instanceof List ? (List<Object>) value : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> avatar(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return avatar(value == null ? "" : value.toString());
    }

    private Map<String, Object> avatar(String path) {
        Map<String, Object> avatar = new LinkedHashMap<String, Object>();
        avatar.put("key", "");
        avatar.put("path", defaultIfBlank(path, ""));
        return avatar;
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
        if (value instanceof List) {
            List<String> result = new ArrayList<String>();
            for (Object item : (List<Object>) value) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private String toSchema(Object value) {
        try {
            return JSON.writeValueAsString(value == null ? Collections.emptyMap() : value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String text(Map<String, Object> map, String key) {
        if (map == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private UserContext userContext(String authorization) {
        if (authorization != null && authorization.contains("dev-token-app")) {
            return new UserContext(DEV_APP_USER_ID, DEV_ORG_ID);
        }
        return new UserContext(DEV_USER_ID, DEV_ORG_ID);
    }

    private static class UserContext {
        private final String userId;
        private final String orgId;

        private UserContext(String userId, String orgId) {
            this.userId = userId;
            this.orgId = orgId;
        }
    }
}
