package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
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
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/service/api/openapi/v1")
public class WanwuOpenApiController {

    private static final String DEV_ADMIN_TOKEN = "dev-token";
    private static final String DEV_APP_TOKEN = "dev-token-app";
    private static final String DEV_ADMIN_ID = "dev-admin";
    private static final String DEV_APP_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";
    private static final String AGENT_APP_TYPE = "agent";
    private static final String RAG_APP_TYPE = "rag";
    private static final String WORKFLOW_APP_TYPE = "workflow";
    private static final String STAT_SOURCE_OPENAPI = "openapi";
    private static final String CONVERSATION_TYPE_PUBLISHED = "published";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final OAuthJwtSupport OAUTH_JWT = new OAuthJwtSupport("wanwu-java", "wanwu-java-oauth-secret");

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private KnowledgeService knowledgeService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    private OpenApiChatflowSessionStore chatflowSessionStore = new OpenApiChatflowSessionStore();
    private final Map<String, OAuthCode> oauthCodes = new ConcurrentHashMap<>();
    private final Map<String, OAuthToken> oauthRefreshTokens = new ConcurrentHashMap<>();

    public WanwuOpenApiController() {
    }

    public WanwuOpenApiController(AppService appService, ModelService modelService) {
        this(appService, modelService, null);
    }

    public WanwuOpenApiController(AppService appService, ModelService modelService, KnowledgeService knowledgeService) {
        this(appService, modelService, knowledgeService, new OpenApiChatflowSessionStore());
    }

    public WanwuOpenApiController(AppService appService, ModelService modelService, KnowledgeService knowledgeService,
                                  OpenApiChatflowSessionStore chatflowSessionStore) {
        this(appService, modelService, knowledgeService, null, chatflowSessionStore);
    }

    public WanwuOpenApiController(AppService appService, ModelService modelService, KnowledgeService knowledgeService,
                                  IamService iamService, OpenApiChatflowSessionStore chatflowSessionStore) {
        this.appService = appService;
        this.modelService = modelService;
        this.knowledgeService = knowledgeService;
        this.iamService = iamService;
        this.chatflowSessionStore = chatflowSessionStore == null ? new OpenApiChatflowSessionStore() : chatflowSessionStore;
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
        try {
            OpenApiContext ctx = context(headers);
            appService.updateAssistantConfig(openApiAgentConfig(ctx, body(request)));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/agent/publish")
    public FrontendResponse<Map<String, Object>> publishAgent(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            appService.publishApp(openApiAgentPublish(ctx, body(request)));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
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
        long startedAt = System.currentTimeMillis();
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
            recordAppStatistic(ctx, command.getAssistantId(), AGENT_APP_TYPE, true, false, startedAt);
            return ResponseEntity.ok(openApiAgentChat(result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(errorBody(ex.getMessage()));
        }
    }

    @PostMapping("/rag/chat")
    public ResponseEntity<?> chatRag(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        long startedAt = System.currentTimeMillis();
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
            boolean stream = booleanValue(body.get("stream"), false);
            recordAppStatistic(ctx, command.getRagId(), RAG_APP_TYPE, true, stream, startedAt);
            if (stream) {
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
        long startedAt = System.currentTimeMillis();
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
            recordAppStatistic(ctx, command.getWorkflowId(), WORKFLOW_APP_TYPE, true, false, startedAt);
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
        OpenApiContext ctx = context(headers);
        Map<String, Object> body = body(request);
        return FrontendResponse.ok(chatflowSessionStore.create(
                ctx.userId, ctx.orgId, text(body, "uuid"), text(body, "conversation_name")));
    }

    @DeleteMapping("/chatflow/conversation")
    public FrontendResponse<Map<String, Object>> deleteChatflowConversation(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        OpenApiContext ctx = context(headers);
        Map<String, Object> body = body(request);
        chatflowSessionStore.delete(ctx.userId, ctx.orgId,
                text(body, "uuid"),
                firstText(body, "conversation_id", "conversationId"));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/chatflow/conversation/list")
    public FrontendResponse<Map<String, Object>> listChatflowConversations(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        OpenApiContext ctx = context(headers);
        return FrontendResponse.ok(chatflowSessionStore.list(ctx.userId, ctx.orgId, text(body(request), "uuid")));
    }

    @PostMapping("/chatflow/conversation/message/list")
    public FrontendResponse<Map<String, Object>> listChatflowMessages(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        OpenApiContext ctx = context(headers);
        Map<String, Object> body = body(request);
        return FrontendResponse.ok(chatflowSessionStore.messages(
                ctx.userId,
                ctx.orgId,
                text(body, "uuid"),
                firstText(body, "conversation_id", "conversationId"),
                intValue(body.get("limit"), 50)));
    }

    @PostMapping("/chatflow/chat")
    public ResponseEntity<String> chatflowChat(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        OpenApiContext ctx = context(headers);
        Map<String, Object> body = body(request);
        Map<String, Object> data = chatflowSessionStore.chat(
                ctx.userId,
                ctx.orgId,
                text(body, "uuid"),
                firstText(body, "conversation_id", "conversationId"),
                text(body, "query"),
                objectMap(body.get("parameters")));
        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body("data: " + toJson(data) + "\n\n");
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
        return Collections.singletonMap("keys", Collections.singletonList(OAUTH_JWT.jwk()));
    }

    @GetMapping("/oauth/login")
    public ResponseEntity<?> oauthLogin(
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "response_type", required = false, defaultValue = "code") String responseType,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state) {
        try {
            Map<String, Object> app = validateOauthApp(clientId, null, redirectUri);
            if (!"code".equals(defaultIfBlank(responseType, "code"))) {
                return oauthError("unsupported response_type");
            }
            String loginUri = appendQuery("/aibase/login",
                    "client_id", text(app, "clientId"),
                    "response_type", "code",
                    "scope", defaultIfBlank(scope, ""),
                    "client_name", text(app, "name"),
                    "redirect_uri", text(app, "redirectUri"),
                    "state", defaultIfBlank(state, ""));
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(loginUri)).build();
        } catch (IllegalArgumentException ex) {
            return oauthError(ex.getMessage());
        }
    }

    @GetMapping("/oauth/code/authorize")
    public ResponseEntity<?> oauthAuthorize(
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "response_type", required = false, defaultValue = "code") String responseType,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "jwt_token", required = false) String jwtToken) {
        try {
            if (!"code".equals(defaultIfBlank(responseType, "code"))) {
                return oauthError("unsupported response_type");
            }
            String userId = oauthUserId(jwtToken);
            Map<String, Object> app = validateOauthApp(clientId, null, redirectUri);
            String code = "wanwu-oauth-code-" + compactId();
            oauthCodes.put(code, new OAuthCode(text(app, "clientId"), userId, stringList(scope), Instant.now().plusSeconds(600).toEpochMilli()));
            String callback = appendQuery(text(app, "redirectUri"), "code", code, "state", defaultIfBlank(state, ""));
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(callback)).build();
        } catch (IllegalArgumentException ex) {
            return oauthError(ex.getMessage());
        }
    }

    @PostMapping("/oauth/code/token")
    public ResponseEntity<Map<String, Object>> oauthToken(
            @RequestParam(value = "grant_type", required = false) String grantType,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret) {
        try {
            if (!"authorization_code".equals(grantType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody("unsupported grant_type"));
            }
            if (isBlank(clientSecret)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody("client_secret is required"));
            }
            Map<String, Object> app = validateOauthApp(clientId, clientSecret, redirectUri);
            OAuthCode payload = oauthCodes.remove(defaultIfBlank(code, ""));
            if (payload == null || !payload.clientId.equals(text(app, "clientId")) || payload.expiresAt < Instant.now().toEpochMilli()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody("invalid authorization code"));
            }
            return ResponseEntity.ok(oauthTokenResponse(payload.userId, payload.clientId, payload.scopes));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(ex.getMessage()));
        }
    }

    @PostMapping("/oauth/code/token/refresh")
    public ResponseEntity<Map<String, Object>> oauthRefresh(@RequestBody(required = false) Map<String, Object> request) {
        try {
            Map<String, Object> body = body(request);
            if (!"refresh_token".equals(text(body, "grant_type"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody("unsupported grant_type"));
            }
            if (isBlank(text(body, "client_secret"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody("client_secret is required"));
            }
            Map<String, Object> app = validateOauthApp(text(body, "client_id"), text(body, "client_secret"), "");
            OAuthToken oldRefresh = oauthRefreshTokens.remove(text(body, "refresh_token"));
            if (oldRefresh == null || !oldRefresh.clientId.equals(text(app, "clientId"))
                    || oldRefresh.expiresAt < Instant.now().toEpochMilli()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody("invalid refresh token"));
            }
            Map<String, Object> data = oauthTokenResponse(oldRefresh.userId, oldRefresh.clientId, oldRefresh.scopes);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("access_token", data.get("access_token"));
            response.put("refresh_token", data.get("refresh_token"));
            response.put("expires_at", String.valueOf(Instant.now().plusSeconds(3600).toEpochMilli()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(ex.getMessage()));
        }
    }

    private Map<String, Object> oauthTokenResponse(String userId, String clientId, List<String> scopes) {
        long now = Instant.now().getEpochSecond();
        String accessToken = OAUTH_JWT.accessToken(userId, clientId, scopes, now + 86400);
        String refreshToken = "wanwu-oauth-refresh-" + compactId();
        OAuthToken refresh = new OAuthToken(userId, clientId, scopes, Instant.now().plusSeconds(86400).toEpochMilli());
        oauthRefreshTokens.put(refreshToken, refresh);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("access_token", accessToken);
        data.put("expires_in", 86400);
        data.put("id_token", OAUTH_JWT.idToken(userId, oauthUserName(userId), clientId, now + 43200));
        data.put("token_type", "Bearer");
        data.put("refresh_token", refreshToken);
        data.put("scope", scopes);
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
        data.put("response_types_supported", Collections.singletonList("code"));
        data.put("id_token_signing_alg_values_supported", Collections.singletonList("RS256"));
        data.put("subject_types_supported", Collections.singletonList("public"));
        return data;
    }

    @GetMapping("/oauth/userinfo")
    public ResponseEntity<Map<String, Object>> oauthUserInfo(@RequestHeader HttpHeaders headers) {
        String token = apiToken(headers);
        OAuthJwtSupport.AccessTokenClaims payload;
        try {
            payload = OAUTH_JWT.parseAccessToken(defaultIfBlank(token, ""));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody("invalid access token"));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", payload.userId());
        data.put("username", oauthUserName(payload.userId()));
        data.put("nickname", DEV_APP_ID.equals(payload.userId()) ? "App User" : "Administrator");
        data.put("phone", "");
        data.put("email", DEV_APP_ID.equals(payload.userId()) ? "app@example.com" : "admin@example.com");
        data.put("gender", "");
        data.put("remark", "");
        data.put("company", "Wanwu Java");
        data.put("avatar", "/user/api/v1/static/icon/user-default-icon.png");
        return ResponseEntity.ok(data);
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

    private void recordAppStatistic(OpenApiContext ctx,
                                    String appId,
                                    String appType,
                                    boolean success,
                                    boolean stream,
                                    long startedAt) {
        if (appService == null || ctx == null || isBlank(appId)) {
            return;
        }
        try {
            long costs = Math.max(0L, System.currentTimeMillis() - startedAt);
            RecordAppStatisticCommand command = new RecordAppStatisticCommand();
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            command.setAppId(appId);
            command.setAppType(appType);
            command.setSuccess(success);
            command.setStream(stream);
            command.setStreamCosts(stream ? costs : 0L);
            command.setNonStreamCosts(stream ? 0L : costs);
            command.setSource(STAT_SOURCE_OPENAPI);
            appService.recordAppStatistic(command);
        } catch (RuntimeException ignored) {
        }
    }

    private AssistantConfigUpdateCommand openApiAgentConfig(OpenApiContext ctx, Map<String, Object> body) {
        AssistantConfigUpdateCommand command = new AssistantConfigUpdateCommand();
        command.setAssistantId(firstText(body, "assistantUuid", "uuid", "assistantId"));
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setPrologue(text(body, "prologue"));
        command.setInstructions(text(body, "instructions"));
        command.setMemoryConfig(objectMap(body.get("memoryConfig")));
        command.setKnowledgeBaseConfig(openApiKnowledgeBaseConfig(body.get("knowledgeBaseConfig")));
        command.setModelConfig(objectMap(body.get("modelConfig")));
        command.setSafetyConfig(objectMap(body.get("safetyConfig")));
        command.setVisionConfig(objectMap(body.get("visionConfig")));
        command.setRerankConfig(objectMap(body.get("rerankConfig")));
        command.setRecommendConfig(objectMap(body.get("recommendConfig")));
        command.setRecommendQuestion(stringList(body.get("recommendQuestion")));
        return command;
    }

    private AppPublishCommand openApiAgentPublish(OpenApiContext ctx, Map<String, Object> body) {
        AppPublishCommand command = new AppPublishCommand();
        command.setAppId(firstText(body, "assistantUuid", "uuid", "appId"));
        command.setAppType(AGENT_APP_TYPE);
        command.setVersion(text(body, "version"));
        command.setDesc(text(body, "desc"));
        command.setPublishType(text(body, "publishType"));
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        return command;
    }

    private Map<String, Object> openApiKnowledgeBaseConfig(Object value) {
        Map<String, Object> config = objectMap(value);
        if (!config.isEmpty()) {
            return config;
        }
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("knowledgebases", Collections.emptyList());
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("matchType", "mix");
        params.put("priorityMatch", 1);
        params.put("threshold", 0.4D);
        params.put("topK", 5);
        defaults.put("config", params);
        return defaults;
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

    private List<String> stringList(Object value) {
        if (value instanceof String) {
            List<String> result = new ArrayList<>();
            String[] parts = ((String) value).split("[,\\s]+");
            for (String part : parts) {
                if (!isBlank(part)) {
                    result.add(part);
                }
            }
            return result;
        }
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<?> source = (List<?>) value;
        List<String> result = new ArrayList<>();
        for (Object item : source) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return result;
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

    private ResponseEntity<Map<String, Object>> oauthError(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(message));
    }

    private Map<String, Object> validateOauthApp(String clientId, String clientSecret, String redirectUri) {
        if (isBlank(clientId)) {
            throw new IllegalArgumentException("client_id is required");
        }
        Map<String, Object> app = oauthApp(clientId);
        if (!booleanValue(app.get("status"), true)) {
            throw new IllegalArgumentException("oauth app disabled");
        }
        if (!clientId.equals(text(app, "clientId"))) {
            throw new IllegalArgumentException("invalid client_id");
        }
        if (!isBlank(clientSecret) && !clientSecret.equals(text(app, "clientSecret"))) {
            throw new IllegalArgumentException("invalid client_secret");
        }
        if (!isBlank(redirectUri) && !redirectUri.equals(text(app, "redirectUri"))) {
            throw new IllegalArgumentException("invalid redirect_uri");
        }
        return app;
    }

    private Map<String, Object> oauthApp(String clientId) {
        if (iamService != null) {
            Map<String, Object> page = iamService.listOauthApps("", "", 1, 1000);
            for (Map<String, Object> item : mapList(page == null ? null : page.get("list"))) {
                if (clientId.equals(text(item, "clientId"))) {
                    return item;
                }
            }
        }
        if ("wanwu-java".equals(clientId)) {
            return devOauthApp();
        }
        throw new IllegalArgumentException("oauth app not found");
    }

    private Map<String, Object> devOauthApp() {
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("clientId", "wanwu-java");
        app.put("name", "Wanwu Java");
        app.put("desc", "Development OAuth App");
        app.put("clientSecret", "wanwu-java-secret");
        app.put("redirectUri", "http://localhost/oauth/callback");
        app.put("status", true);
        return app;
    }

    private String oauthUserId(String jwtToken) {
        String token = defaultIfBlank(jwtToken, "");
        if (token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length()).trim();
        }
        if (DEV_APP_TOKEN.equals(token)) {
            return DEV_APP_ID;
        }
        if (DEV_ADMIN_TOKEN.equals(token)) {
            return DEV_ADMIN_ID;
        }
        throw new IllegalArgumentException("invalid jwt_token");
    }

    private String oauthUserName(String userId) {
        return DEV_APP_ID.equals(userId) ? "app" : "admin";
    }

    private String appendQuery(String base, String... pairs) {
        StringBuilder builder = new StringBuilder(defaultIfBlank(base, "/"));
        char separator = builder.indexOf("?") >= 0 ? '&' : '?';
        for (int i = 0; pairs != null && i + 1 < pairs.length; i += 2) {
            builder.append(separator)
                    .append(encode(pairs[i]))
                    .append('=')
                    .append(encode(defaultIfBlank(pairs[i + 1], "")));
            separator = '&';
        }
        return builder.toString();
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(defaultIfBlank(value, ""), "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("UTF-8 is unavailable", ex);
        }
    }

    private static class OAuthCode {
        private final String clientId;
        private final String userId;
        private final List<String> scopes;
        private final long expiresAt;

        private OAuthCode(String clientId, String userId, List<String> scopes, long expiresAt) {
            this.clientId = clientId;
            this.userId = userId;
            this.scopes = scopes == null ? Collections.<String>emptyList() : new ArrayList<>(scopes);
            this.expiresAt = expiresAt;
        }
    }

    private static class OAuthToken {
        private final String userId;
        private final String clientId;
        private final List<String> scopes;
        private final long expiresAt;

        private OAuthToken(String userId, String clientId, List<String> scopes, long expiresAt) {
            this.userId = userId;
            this.clientId = clientId;
            this.scopes = scopes == null ? Collections.<String>emptyList() : new ArrayList<>(scopes);
            this.expiresAt = expiresAt;
        }
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
