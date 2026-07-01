package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationPageResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantPublishedQuery;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/service/api/openapi/v1")
public class WanwuOpenApiController {

    private static final String DEV_ADMIN_TOKEN = "dev-token";
    private static final String DEV_APP_TOKEN = "dev-token-app";
    private static final String DEV_ADMIN_ID = "dev-admin";
    private static final String DEV_APP_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";
    private static final String AGENT_APP_TYPE = "agent";
    private static final String WORKFLOW_APP_TYPE = "workflow";
    private static final String CONVERSATION_TYPE_PUBLISHED = "published";
    private static final ObjectMapper JSON = new ObjectMapper();

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private KnowledgeService knowledgeService;

    public WanwuOpenApiController() {
    }

    public WanwuOpenApiController(AppService appService, ModelService modelService) {
        this(appService, modelService, null);
    }

    public WanwuOpenApiController(AppService appService, ModelService modelService, KnowledgeService knowledgeService) {
        this.appService = appService;
        this.modelService = modelService;
        this.knowledgeService = knowledgeService;
    }

    @PostMapping("/agent")
    public FrontendResponse<Map<String, Object>> createAgent(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> body = body(request);
            AssistantCreateCommand command = new AssistantCreateCommand();
            command.setName(defaultIfBlank(text(body, "name"), "OpenAPI Agent"));
            command.setDesc(defaultIfBlank(text(body, "desc"), text(body, "description")));
            command.setCategory(intValue(body.get("category"), 1));
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            AssistantCreateResult created = appService.createAssistant(command);
            return FrontendResponse.ok(Collections.singletonMap("uuid", defaultIfBlank(created.getAssistantId(), "")));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/agent")
    public FrontendResponse<Map<String, Object>> deleteAgent(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "uuid", required = false) String uuid,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            AssistantDeleteCommand command = new AssistantDeleteCommand();
            command.setAssistantId(defaultIfBlank(uuid, text(request, "uuid")));
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            appService.deleteAssistant(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/agent/list")
    public FrontendResponse<Map<String, Object>> listAgents(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "name", required = false) String name) {
        try {
            OpenApiContext ctx = context(headers);
            ApplicationListQuery query = new ApplicationListQuery(
                    AGENT_APP_TYPE, defaultIfBlank(name, ""), ctx.userId, ctx.orgId);
            return FrontendResponse.ok(listResult(appService.listApplications(query).getList()));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/agent/info")
    public FrontendResponse<Map<String, Object>> agentInfo(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "uuid", required = false) String uuid,
            @RequestParam(value = "published", required = false, defaultValue = "false") boolean published) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> data = published
                    ? appService.getPublishedAssistant(new AssistantPublishedQuery(uuid, null, ctx.userId, ctx.orgId))
                    : appService.getAssistantDraft(new AssistantDetailQuery(uuid, ctx.userId, ctx.orgId));
            return FrontendResponse.ok(data);
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/agent/config")
    public FrontendResponse<Map<String, Object>> updateAgentConfig(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        context(headers);
        return FrontendResponse.ok(echo("agent_config_updated", request));
    }

    @PostMapping("/agent/publish")
    public FrontendResponse<Map<String, Object>> publishAgent(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        context(headers);
        return FrontendResponse.ok(echo("agent_published", request));
    }

    @PostMapping("/agent/conversation")
    public FrontendResponse<AssistantConversationCreateResult> createAgentConversation(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> body = body(request);
            AssistantConversationCreateCommand command = new AssistantConversationCreateCommand();
            command.setAssistantId(text(body, "uuid"));
            command.setPrompt(defaultIfBlank(text(body, "title"), text(body, "prompt")));
            command.setConversationType(CONVERSATION_TYPE_PUBLISHED);
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            return FrontendResponse.ok(appService.createAssistantConversation(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/agent/conversation")
    public FrontendResponse<Map<String, Object>> deleteAgentConversation(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "conversation_id", required = false) String conversationId,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            appService.deleteAssistantConversation(conversationDelete(ctx,
                    defaultIfBlank(conversationId, text(request, "conversation_id")),
                    text(request, "detail_id")));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/agent/conversation/clear")
    public FrontendResponse<Map<String, Object>> clearAgentConversation(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "conversation_id", required = false) String conversationId,
            @RequestParam(value = "detail_id", required = false) String detailId,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            appService.clearAssistantConversation(conversationDelete(ctx,
                    defaultIfBlank(conversationId, text(request, "conversation_id")),
                    defaultIfBlank(detailId, text(request, "detail_id"))));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/agent/conversation/list")
    public FrontendResponse<AssistantConversationPageResult> listAgentConversations(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "uuid", required = false) String uuid,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        try {
            OpenApiContext ctx = context(headers);
            AssistantConversationListQuery query = conversationList(ctx, uuid, pageNo, pageSize);
            query.setConversationType(CONVERSATION_TYPE_PUBLISHED);
            return FrontendResponse.ok(appService.listAssistantConversations(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/agent/conversation/detail")
    public FrontendResponse<AssistantConversationPageResult> listAgentConversationDetails(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "conversation_id", required = false) String conversationId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        try {
            OpenApiContext ctx = context(headers);
            AssistantConversationDetailQuery query = new AssistantConversationDetailQuery();
            query.setConversationId(conversationId);
            query.setPageNo(pageNo);
            query.setPageSize(pageSize);
            query.setUserId(ctx.userId);
            query.setOrgId(ctx.orgId);
            return FrontendResponse.ok(appService.listAssistantConversationDetails(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/agent/conversation/draft/detail")
    public FrontendResponse<AssistantConversationPageResult> listDraftAgentConversationDetails(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "uuid", required = false) String uuid,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        try {
            OpenApiContext ctx = context(headers);
            return FrontendResponse.ok(appService.listDraftAssistantConversationDetails(
                    conversationList(ctx, uuid, pageNo, pageSize)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/agent/conversation/draft")
    public FrontendResponse<Map<String, Object>> deleteDraftAgentConversation(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "uuid", required = false) String uuid,
            @RequestParam(value = "detail_id", required = false) String detailId) {
        try {
            OpenApiContext ctx = context(headers);
            AssistantConversationDeleteCommand command = conversationDelete(ctx, "", detailId);
            command.setAssistantId(uuid);
            appService.deleteDraftAssistantConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping({"/agent/chat", "/agent/chat/draft"})
    public ResponseEntity<Map<String, Object>> chatAgent(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> body = body(request);
            AssistantConversationStreamCommand command = new AssistantConversationStreamCommand();
            command.setAssistantId(text(body, "uuid"));
            command.setConversationId(text(body, "conversation_id"));
            command.setPrompt(defaultIfBlank(text(body, "query"), text(body, "prompt")));
            command.setDraft(false);
            command.setFileInfo(mapList(body.get("file_info")));
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            AssistantConversationStreamResult result = appService.streamAssistantConversation(command);
            return ResponseEntity.ok(openApiAgentChat(result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(errorBody(ex.getMessage()));
        }
    }

    @PostMapping("/rag/chat")
    public ResponseEntity<?> chatRag(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> body = body(request);
            RagChatCommand command = new RagChatCommand();
            command.setRagId(text(body, "uuid"));
            command.setQuestion(defaultIfBlank(text(body, "query"), text(body, "prompt")));
            command.setDraft(false);
            command.setHistory(mapList(body.get("history")));
            command.setFileInfo(mapList(body.containsKey("file_info") ? body.get("file_info") : body.get("fileInfo")));
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            RagChatResult result = appService.streamRagChat(command);
            Map<String, Object> response = openApiRagChat(result);
            if (booleanValue(body.get("stream"), false)) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(legacyRagSse(response));
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(errorBody(ex.getMessage()));
        }
    }

    @PostMapping("/workflow/run")
    public FrontendResponse<Map<String, Object>> runWorkflow(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> body = body(request);
            WorkflowRunCommand command = new WorkflowRunCommand();
            command.setWorkflowId(text(body, "uuid"));
            command.setInput(objectMap(body.get("parameters")));
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            WorkflowRunResult result = appService.runWorkflow(command);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("workflow_id", result == null ? text(body, "uuid") : defaultIfBlank(result.getWorkflowId(), text(body, "uuid")));
            data.put("output", result == null || result.getOutput() == null ? Collections.emptyMap() : result.getOutput());
            return FrontendResponse.ok(data);
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/workflow/file/upload")
    public FrontendResponse<Map<String, Object>> workflowFileUpload(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        context(headers);
        return FrontendResponse.ok(uploadBody(file));
    }

    @PostMapping("/chatflow/conversation")
    public FrontendResponse<Map<String, Object>> createChatflowConversation(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        context(headers);
        String id = "chatflow-conversation-" + compactId();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("conversation_id", id);
        data.put("conversationId", id);
        data.put("uuid", text(request, "uuid"));
        return FrontendResponse.ok(data);
    }

    @DeleteMapping("/chatflow/conversation")
    public FrontendResponse<Map<String, Object>> deleteChatflowConversation(@RequestHeader HttpHeaders headers) {
        context(headers);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/chatflow/conversation/list")
    public FrontendResponse<Map<String, Object>> listChatflowConversations(@RequestHeader HttpHeaders headers) {
        context(headers);
        return FrontendResponse.ok(listResult(Collections.emptyList()));
    }

    @PostMapping("/chatflow/conversation/message/list")
    public FrontendResponse<Map<String, Object>> listChatflowMessages(@RequestHeader HttpHeaders headers) {
        context(headers);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("data", Collections.emptyList());
        data.put("has_more", false);
        data.put("first_id", 0);
        data.put("last_id", 0);
        return FrontendResponse.ok(data);
    }

    @PostMapping("/chatflow/chat")
    public ResponseEntity<String> chatflowChat(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        context(headers);
        String query = text(request, "query");
        String json = "{\"code\":0,\"message\":\"success\",\"response\":\"" + jsonEscape(query) + "\",\"finish\":1}";
        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body("data: " + json + "\n\n");
    }

    @PostMapping("/chatflow/file/upload")
    public FrontendResponse<Map<String, Object>> chatflowFileUpload(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        context(headers);
        return FrontendResponse.ok(uploadBody(file));
    }

    @GetMapping("/model/list")
    public FrontendResponse<Object> listModels(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "modelType", required = false) String modelType,
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "displayName", required = false) String displayName) {
        try {
            OpenApiContext ctx = context(headers);
            if (modelService == null) {
                return FrontendResponse.ok(listResult(Collections.emptyList()));
            }
            return FrontendResponse.ok(modelService.listModels(new ModelListQuery(
                    ctx.userId, ctx.orgId, defaultIfBlank(modelType, ""), defaultIfBlank(provider, ""),
                    defaultIfBlank(displayName, ""), "", "")));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/file/upload/direct")
    public FrontendResponse<Map<String, Object>> directUpload(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        context(headers);
        return FrontendResponse.ok(uploadBody(file));
    }

    @PostMapping("/knowledge")
    public FrontendResponse<Map<String, Object>> createKnowledge(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> data = knowledgeService.createKnowledge(ctx.userId, ctx.orgId, body(request));
            return FrontendResponse.ok(withKnowledgeAliases(data));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/knowledge")
    public FrontendResponse<Map<String, Object>> updateKnowledge(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            knowledgeService.updateKnowledge(ctx.userId, ctx.orgId, body(request));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/knowledge")
    public FrontendResponse<Map<String, Object>> deleteKnowledge(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            knowledgeService.deleteKnowledge(ctx.userId, ctx.orgId, body(request));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/knowledge/select")
    public FrontendResponse<Map<String, Object>> selectKnowledge(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.selectKnowledge(ctx.userId, ctx.orgId, body(request))));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/knowledge/doc/config")
    public FrontendResponse<Map<String, Object>> docConfig(@RequestHeader HttpHeaders headers) {
        try {
            OpenApiContext ctx = context(headers);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.getDocConfig(ctx.userId, ctx.orgId, Collections.<String, Object>emptyMap())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/knowledge/doc/list")
    public FrontendResponse<Map<String, Object>> docList(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.listDocs(ctx.userId, ctx.orgId, body(request))));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/knowledge/doc/import")
    public FrontendResponse<Map<String, Object>> importDocs(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            knowledgeService.importDocs(ctx.userId, ctx.orgId, body(request));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/knowledge/doc/update/config")
    public FrontendResponse<Map<String, Object>> updateDocConfig(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            knowledgeService.updateDocConfig(ctx.userId, ctx.orgId, body(request));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/knowledge/doc/export")
    public FrontendResponse<Map<String, Object>> exportDocs(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.exportDocs(ctx.userId, ctx.orgId, body(request))));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/knowledge/hit")
    public FrontendResponse<Map<String, Object>> hitKnowledge(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.hitKnowledge(ctx.userId, ctx.orgId, body(request))));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/knowledge/doc/import/tip")
    public FrontendResponse<Map<String, Object>> knowledgeImportTip(@RequestHeader HttpHeaders headers) {
        try {
            OpenApiContext ctx = context(headers);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.getDocImportTip(ctx.userId, ctx.orgId, Collections.<String, Object>emptyMap())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/knowledge/export/record/list")
    public FrontendResponse<Map<String, Object>> listKnowledgeExportRecords(@RequestHeader HttpHeaders headers) {
        try {
            OpenApiContext ctx = context(headers);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.listExportRecords(ctx.userId, ctx.orgId, Collections.<String, Object>emptyMap())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping({"/knowledge/doc", "/knowledge/export/record"})
    public FrontendResponse<Map<String, Object>> knowledgeDeleteShell(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> safe = body(request);
            if (safe.containsKey("exportRecordId") || safe.containsKey("export_record_id")) {
                knowledgeService.deleteExportRecord(ctx.userId, ctx.orgId, safe);
            } else {
                knowledgeService.deleteDocs(ctx.userId, ctx.orgId, safe);
            }
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping({"/mcp/server/sse", "/mcp/server/streamable"})
    public ResponseEntity<String> mcpGet(@RequestParam(value = "apiKey", required = false) String apiKey) {
        String event = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\",\"params\":{\"apiKey\":\""
                + jsonEscape(defaultIfBlank(apiKey, "")) + "\"}}";
        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body("data: " + event + "\n\n");
    }

    @PostMapping({"/mcp/server/message", "/mcp/server/streamable"})
    public ResponseEntity<Map<String, Object>> mcpPost(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jsonrpc", "2.0");
        data.put("id", request == null ? null : request.get("id"));
        data.put("result", Collections.emptyMap());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/oauth/jwks")
    public Map<String, Object> oauthJwks() {
        return Collections.singletonMap("keys", Collections.emptyList());
    }

    @GetMapping("/oauth/login")
    public ResponseEntity<String> oauthLogin() {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/oauth/code/authorize")
    public FrontendResponse<Map<String, Object>> oauthAuthorize(
            @RequestParam(value = "client_id", required = false) String clientId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", "dev-oauth-code");
        data.put("client_id", defaultIfBlank(clientId, "wanwu-java"));
        return FrontendResponse.ok(data);
    }

    @PostMapping({"/oauth/code/token", "/oauth/code/token/refresh"})
    public Map<String, Object> oauthToken() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("access_token", "dev-oauth-access-token");
        data.put("refresh_token", "dev-oauth-refresh-token");
        data.put("token_type", "Bearer");
        data.put("expires_in", 3600);
        return data;
    }

    @GetMapping("/.well-known/openid-configuration")
    public Map<String, Object> oauthConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("issuer", "wanwu-java");
        data.put("jwks_uri", "/service/api/openapi/v1/oauth/jwks");
        data.put("authorization_endpoint", "/service/api/openapi/v1/oauth/code/authorize");
        data.put("token_endpoint", "/service/api/openapi/v1/oauth/code/token");
        data.put("userinfo_endpoint", "/service/api/openapi/v1/oauth/userinfo");
        return data;
    }

    @GetMapping("/oauth/userinfo")
    public Map<String, Object> oauthUserInfo() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sub", DEV_ADMIN_ID);
        data.put("name", "admin");
        data.put("org_id", DEV_ORG_ID);
        return data;
    }

    private OpenApiContext context(HttpHeaders headers) {
        String token = apiToken(headers);
        if (DEV_APP_TOKEN.equals(token)) {
            return new OpenApiContext(DEV_APP_ID, DEV_ORG_ID, "dev-app-key");
        }
        if (DEV_ADMIN_TOKEN.equals(token) || isBlank(token)) {
            return new OpenApiContext(DEV_ADMIN_ID, DEV_ORG_ID, "dev-admin-key");
        }
        ApiKeyInfo apiKey = appService.getApiKeyByKey(token);
        if (apiKey == null || Boolean.FALSE.equals(apiKey.getStatus())) {
            throw new IllegalArgumentException("invalid api key");
        }
        return new OpenApiContext(
                defaultIfBlank(apiKey.getUserId(), DEV_ADMIN_ID),
                defaultIfBlank(apiKey.getOrgId(), DEV_ORG_ID),
                defaultIfBlank(apiKey.getKeyId(), token));
    }

    private String apiToken(HttpHeaders headers) {
        String value = firstHeader(headers, "X-API-Key");
        if (isBlank(value)) {
            value = firstHeader(headers, "Api-Key");
        }
        if (isBlank(value)) {
            value = firstHeader(headers, "Authorization");
        }
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.toLowerCase().startsWith("bearer ")) {
            return trimmed.substring("bearer ".length()).trim();
        }
        return trimmed;
    }

    private String firstHeader(HttpHeaders headers, String name) {
        if (headers == null) {
            return "";
        }
        List<String> values = headers.get(name);
        return values == null || values.isEmpty() ? "" : values.get(0);
    }

    private AssistantConversationListQuery conversationList(OpenApiContext ctx, String assistantId, int pageNo, int pageSize) {
        AssistantConversationListQuery query = new AssistantConversationListQuery();
        query.setAssistantId(assistantId);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setUserId(ctx.userId);
        query.setOrgId(ctx.orgId);
        return query;
    }

    private AssistantConversationDeleteCommand conversationDelete(OpenApiContext ctx, String conversationId, String detailId) {
        AssistantConversationDeleteCommand command = new AssistantConversationDeleteCommand();
        command.setConversationId(conversationId);
        command.setDetailId(detailId);
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        return command;
    }

    private Map<String, Object> openApiAgentChat(AssistantConversationStreamResult result) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", 0);
        data.put("message", "success");
        data.put("response", result == null ? "" : defaultIfBlank(result.getResponse(), ""));
        data.put("gen_file_url_list", Collections.emptyList());
        data.put("search_list", Collections.emptyList());
        data.put("history", Collections.emptyList());
        data.put("usage", usage());
        data.put("finish", 1);
        if (result != null) {
            data.put("conversation_id", result.getConversationId());
            data.put("detail_id", result.getDetailId());
        }
        return data;
    }

    private Map<String, Object> openApiRagChat(RagChatResult result) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", 0);
        data.put("message", "success");
        data.put("msg_id", result == null ? "" : defaultIfBlank(result.getRagId(), ""));
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("output", result == null ? "" : defaultIfBlank(result.getResponse(), ""));
        output.put("searchList", result == null || result.getSearchList() == null
                ? Collections.emptyList() : result.getSearchList());
        output.put("qaSearchList", result == null || result.getQaSearchList() == null
                ? Collections.emptyList() : result.getQaSearchList());
        data.put("data", output);
        data.put("history", Collections.emptyList());
        data.put("finish", 1);
        return data;
    }

    private String legacyRagSse(Map<String, Object> response) {
        return "data: " + toJson(response) + "\n\n"
                + "data: [DONE]\n\n";
    }

    private String toJson(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("openapi response is invalid", ex);
        }
    }

    private Map<String, Object> usage() {
        Map<String, Object> usage = new LinkedHashMap<>();
        usage.put("completion_tokens", 0);
        usage.put("prompt_tokens", 0);
        usage.put("total_tokens", 0);
        return usage;
    }

    private Map<String, Object> uploadBody(MultipartFile file) {
        String fileName = file == null || file.getOriginalFilename() == null ? "file.bin" : file.getOriginalFilename();
        String fileId = "openapi-file-" + compactId();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileId", fileId);
        data.put("file_id", fileId);
        data.put("fileName", fileName);
        data.put("file_name", fileName);
        data.put("url", "/service/api/openapi/v1/file/download/" + fileId);
        return data;
    }

    private Map<String, Object> listResult(Object rows) {
        List<?> list = rows instanceof List ? (List<?>) rows : Collections.emptyList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", list.size());
        return data;
    }

    private Map<String, Object> serviceResult(Map<String, Object> response) {
        return response == null ? Collections.<String, Object>emptyMap() : response;
    }

    private Map<String, Object> withKnowledgeAliases(Map<String, Object> response) {
        Map<String, Object> data = new LinkedHashMap<>(serviceResult(response));
        if (data.containsKey("knowledgeId") && !data.containsKey("knowledge_id")) {
            data.put("knowledge_id", data.get("knowledgeId"));
        }
        return data;
    }

    private Map<String, Object> echo(String status, Map<String, Object> request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", status);
        data.put("request", request == null ? Collections.emptyMap() : request);
        data.put("timestamp", Instant.EPOCH.toString());
        return data;
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", 1001);
        data.put("message", message);
        return data;
    }

    private Map<String, Object> body(Map<String, Object> request) {
        return request == null ? Collections.<String, Object>emptyMap() : request;
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

    private List<Map<String, Object>> mapList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<?> source = (List<?>) value;
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : source) {
            if (item instanceof Map) {
                result.add(objectMap(item));
            }
        }
        return result;
    }

    private String text(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value instanceof String && !isBlank((String) value)) {
            return Boolean.parseBoolean((String) value);
        }
        return fallback;
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

    private static class OpenApiContext {
        private final String userId;
        private final String orgId;
        private final String apiKeyId;

        private OpenApiContext(String userId, String orgId, String apiKeyId) {
            this.userId = userId;
            this.orgId = orgId;
            this.apiKeyId = apiKeyId;
        }
    }
}
