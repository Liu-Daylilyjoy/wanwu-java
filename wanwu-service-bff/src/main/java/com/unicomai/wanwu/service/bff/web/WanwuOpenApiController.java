package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AppKeyInfo;
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
import com.unicomai.wanwu.api.app.dto.ChatflowConversationChatCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteByIdCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationMessageListQuery;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.mcp.McpService;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.operate.OperateService;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/service/api/openapi/v1")
public class WanwuOpenApiController {

    private static final String DEV_ADMIN_TOKEN = OpenApiAuthSupport.DEV_ADMIN_TOKEN;
    private static final String DEV_APP_TOKEN = OpenApiAuthSupport.DEV_APP_TOKEN;
    private static final String DEV_ADMIN_ID = OpenApiAuthSupport.DEV_ADMIN_ID;
    private static final String DEV_APP_ID = OpenApiAuthSupport.DEV_APP_ID;
    private static final String DEV_ORG_ID = OpenApiAuthSupport.DEV_ORG_ID;
    private static final String AGENT_APP_TYPE = "agent";
    private static final String RAG_APP_TYPE = "rag";
    private static final String WORKFLOW_APP_TYPE = "workflow";
    private static final String CHATFLOW_APP_TYPE = "chatflow";
    private static final String MCP_SERVER_APP_TYPE = "mcpserver";
    private static final String MCP_PROTOCOL_VERSION = "2024-11-05";
    private static final String MCP_SERVER_NAME = "wanwu-java-mcp-server";
    private static final int KNOWLEDGE_VIEW = 0;
    private static final int KNOWLEDGE_EDIT = 10;
    private static final int KNOWLEDGE_SYSTEM = 30;
    private static final String STAT_SOURCE_OPENAPI = "openapi";
    private static final String CONVERSATION_TYPE_PUBLISHED = "published";
    private static final long OAUTH_CODE_SECONDS = 600L;
    private static final long OAUTH_ACCESS_TOKEN_SECONDS = 86400L;
    private static final long OAUTH_REFRESH_TOKEN_SECONDS = 7L * 24L * 60L * 60L;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, OpenApiUploadedFile> OPENAPI_UPLOADS =
            new ConcurrentHashMap<String, OpenApiUploadedFile>();
    private static final OAuthJwtSupport OAUTH_JWT = new OAuthJwtSupport("wanwu-java", "wanwu-java-oauth-secret");

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private KnowledgeService knowledgeService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private OperateService operateService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private McpService mcpService;

    private OpenApiChatflowSessionStore chatflowSessionStore = new OpenApiChatflowSessionStore();
    private final Map<String, OAuthCode> oauthCodes = new ConcurrentHashMap<>();
    private final Map<String, OAuthToken> oauthRefreshTokens = new ConcurrentHashMap<>();
    private final OpenAiCompatibleChatClient chatClient = new OpenAiCompatibleChatClient();

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
        this(appService, modelService, knowledgeService, iamService, chatflowSessionStore, null);
    }

    public WanwuOpenApiController(AppService appService, ModelService modelService, KnowledgeService knowledgeService,
                                  IamService iamService, OpenApiChatflowSessionStore chatflowSessionStore,
                                  OperateService operateService) {
        this(appService, modelService, knowledgeService, iamService, chatflowSessionStore, operateService, null);
    }

    public WanwuOpenApiController(AppService appService, ModelService modelService, KnowledgeService knowledgeService,
                                  IamService iamService, OpenApiChatflowSessionStore chatflowSessionStore,
                                  OperateService operateService, McpService mcpService) {
        this.appService = appService;
        this.modelService = modelService;
        this.knowledgeService = knowledgeService;
        this.iamService = iamService;
        this.operateService = operateService;
        this.mcpService = mcpService;
        this.chatflowSessionStore = chatflowSessionStore == null ? new OpenApiChatflowSessionStore() : chatflowSessionStore;
    }

    @ExceptionHandler(OpenApiAuthException.class)
    public ResponseEntity<Map<String, Object>> openApiAuthError(OpenApiAuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(defaultIfBlank(ex.getMessage(), "invalid api key")));
    }

    @ExceptionHandler(OpenApiBadRequestException.class)
    public ResponseEntity<Map<String, Object>> openApiBadRequest(OpenApiBadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(errorBody(defaultIfBlank(ex.getMessage(), "invalid openapi request")));
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
            Map<String, Object> safe = body(request);
            authModelByUuid(ctx, safe, "modelConfig.modelId", "rerankConfig.modelId",
                    "recommendConfig.modelConfig.modelId");
            appService.updateAssistantConfig(openApiAgentConfig(ctx, safe));
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
    public FrontendResponse<Map<String, Object>> createAgentConversation(
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
            return FrontendResponse.ok(openApiAgentConversationCreate(
                    appService.createAssistantConversation(command)));
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

    @PostMapping("/agent/chat")
    public ResponseEntity<?> chatAgent(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        return chatAgentInternal(headers, request, false, false);
    }

    @PostMapping("/agent/chat/draft")
    public ResponseEntity<?> draftChatAgent(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        return chatAgentInternal(headers, request, true, true);
    }

    private ResponseEntity<?> chatAgentInternal(HttpHeaders headers,
                                                Map<String, Object> request,
                                                boolean draft,
                                                boolean forceStream) {
        long startedAt = System.currentTimeMillis();
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> body = body(request);
            List<Map<String, Object>> fileInfo = mapList(body.get("file_info"));
            if (fileInfo.size() > 1) {
                throw new IllegalArgumentException("file_info only supports one file");
            }
            AssistantConversationStreamCommand command = new AssistantConversationStreamCommand();
            command.setAssistantId(text(body, "uuid"));
            command.setConversationId(text(body, "conversation_id"));
            command.setPrompt(defaultIfBlank(text(body, "query"), text(body, "prompt")));
            command.setDraft(draft);
            command.setFileInfo(fileInfo);
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            attachConfiguredOpenApiAgentModelResponse(ctx, command, draft, startedAt);
            AssistantConversationStreamResult result = appService.streamAssistantConversation(command);
            boolean stream = forceStream || booleanValue(body.get("stream"), false);
            Map<String, Object> response = openApiAgentChat(result);
            recordAppStatistic(ctx, command.getAssistantId(), AGENT_APP_TYPE, true, stream, startedAt);
            if (stream) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(legacyOpenApiSse(response));
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(errorBody(ex.getMessage()));
        }
    }

    @PostMapping("/rag/chat")
    public ResponseEntity<?> chatRag(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        long startedAt = System.currentTimeMillis();
        OpenApiContext ctx = null;
        String ragId = "";
        boolean stream = false;
        try {
            ctx = context(headers);
            Map<String, Object> body = body(request);
            ragId = text(body, "uuid");
            stream = booleanValue(body.get("stream"), false);
            RagChatCommand command = new RagChatCommand();
            command.setRagId(ragId);
            command.setQuestion(defaultIfBlank(text(body, "query"), text(body, "prompt")));
            command.setDraft(false);
            command.setHistory(mapList(body.get("history")));
            command.setFileInfo(mapList(body.containsKey("file_info") ? body.get("file_info") : body.get("fileInfo")));
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            RagChatResult result = appService.streamRagChat(command);
            Map<String, Object> response = openApiRagChat(result);
            recordAppStatistic(ctx, command.getRagId(), RAG_APP_TYPE, true, stream, startedAt);
            if (stream) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(legacyOpenApiSse(response));
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            recordAppStatistic(ctx, ragId, RAG_APP_TYPE, false, stream, startedAt);
            return ResponseEntity.badRequest().body(errorBody(ex.getMessage()));
        }
    }

    @PostMapping("/workflow/run")
    public ResponseEntity<Map<String, Object>> runWorkflow(
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
            Map<String, Object> data = result == null || result.getOutput() == null
                    ? Collections.<String, Object>emptyMap()
                    : new LinkedHashMap<>(result.getOutput());
            recordAppStatistic(ctx, command.getWorkflowId(), WORKFLOW_APP_TYPE, true, false, startedAt);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(errorBody(ex.getMessage()));
        }
    }

    @PostMapping("/workflow/file/upload")
    public ResponseEntity<String> workflowFileUpload(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        context(headers);
        return ResponseEntity.ok(uploadUrl(file));
    }

    @PostMapping("/chatflow/conversation")
    public FrontendResponse<Map<String, Object>> createChatflowConversation(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        OpenApiContext ctx = context(headers);
        Map<String, Object> body = body(request);
        return FrontendResponse.ok(serviceCreateChatflowConversation(ctx, body));
    }

    @DeleteMapping("/chatflow/conversation")
    public FrontendResponse<Map<String, Object>> deleteChatflowConversation(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        OpenApiContext ctx = context(headers);
        Map<String, Object> body = body(request);
        serviceDeleteChatflowConversation(ctx, body);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/chatflow/conversation/list")
    public FrontendResponse<Map<String, Object>> listChatflowConversations(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        OpenApiContext ctx = context(headers);
        return FrontendResponse.ok(serviceListChatflowConversations(ctx, body(request)));
    }

    @PostMapping("/chatflow/conversation/message/list")
    public FrontendResponse<Map<String, Object>> listChatflowMessages(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        OpenApiContext ctx = context(headers);
        Map<String, Object> body = body(request);
        return FrontendResponse.ok(serviceListChatflowMessages(ctx, body));
    }

    @PostMapping("/chatflow/chat")
    public ResponseEntity<String> chatflowChat(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        OpenApiContext ctx = context(headers);
        Map<String, Object> body = body(request);
        long startedAt = System.currentTimeMillis();
        String chatflowId = text(body, "uuid");
        try {
            Map<String, Object> data = serviceChatflowChat(ctx, body);
            recordAppStatistic(ctx, chatflowId, CHATFLOW_APP_TYPE, true, true, startedAt);
            return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body("data: " + toJson(data) + "\n\n");
        } catch (IllegalArgumentException ex) {
            recordAppStatistic(ctx, chatflowId, CHATFLOW_APP_TYPE, false, true, startedAt);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJson(errorBody(ex.getMessage())));
        }
    }

    @PostMapping("/chatflow/file/upload")
    public ResponseEntity<String> chatflowFileUpload(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        context(headers);
        return ResponseEntity.ok(uploadUrl(file));
    }

    @GetMapping("/model/list")
    public FrontendResponse<Object> listModels(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "modelType", required = false) String modelType,
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "filterScope", required = false) String filterScope,
            @RequestParam(value = "scopeType", required = false) String scopeType) {
        try {
            OpenApiContext ctx = context(headers);
            if (modelService == null) {
                return FrontendResponse.ok(listResult(Collections.emptyList()));
            }
            ModelListResult result = modelService.listModels(new ModelListQuery(
                    ctx.userId, ctx.orgId, defaultIfBlank(modelType, ""), defaultIfBlank(provider, ""),
                    defaultIfBlank(displayName, ""), defaultIfBlank(filterScope, ""), defaultIfBlank(scopeType, "")));
            return FrontendResponse.ok(openApiModelList(result));
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

    @GetMapping("/file/download/{fileId:.+}")
    public ResponseEntity<byte[]> downloadOpenApiFile(@PathVariable("fileId") String fileId) {
        OpenApiUploadedFile file = OPENAPI_UPLOADS.get(fileId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName + "\"")
                .contentType(MediaType.parseMediaType(
                        defaultIfBlank(file.contentType, MediaType.APPLICATION_OCTET_STREAM_VALUE)))
                .contentLength(file.bytes.length)
                .body(file.bytes);
    }

    @PostMapping("/knowledge")
    public FrontendResponse<Map<String, Object>> createKnowledge(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> safe = body(request);
            authModelByUuid(ctx, safe, "embeddingModelInfo.modelId", "knowledgeGraph.llmModelId");
            Map<String, Object> data = knowledgeService.createKnowledge(ctx.userId, ctx.orgId, safe);
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
            Map<String, Object> safe = body(request);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_SYSTEM);
            knowledgeService.updateKnowledge(ctx.userId, ctx.orgId, safe);
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
            Map<String, Object> safe = body(request);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_SYSTEM);
            knowledgeService.deleteKnowledge(ctx.userId, ctx.orgId, safe);
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
    public FrontendResponse<Map<String, Object>> docConfig(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "knowledgeId", required = false) String knowledgeId) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> safe = new LinkedHashMap<>();
            safe.put("knowledgeId", knowledgeId);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_VIEW);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.getDocConfig(ctx.userId, ctx.orgId, safe)));
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
            Map<String, Object> safe = body(request);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_VIEW);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.listDocs(ctx.userId, ctx.orgId, safe)));
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
            Map<String, Object> safe = body(request);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_EDIT);
            knowledgeService.importDocs(ctx.userId, ctx.orgId, safe);
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
            Map<String, Object> safe = body(request);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_EDIT);
            knowledgeService.updateDocConfig(ctx.userId, ctx.orgId, safe);
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
            Map<String, Object> safe = body(request);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_EDIT);
            knowledgeService.exportDocs(ctx.userId, ctx.orgId, safe);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
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
            Map<String, Object> safe = body(request);
            authModelByUuid(ctx, safe, "knowledgeMatchParams.rerankModelId");
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.hitKnowledge(ctx.userId, ctx.orgId, safe)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/knowledge/doc/import/tip")
    public FrontendResponse<Map<String, Object>> knowledgeImportTip(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "knowledgeId", required = false) String knowledgeId) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> safe = new LinkedHashMap<>();
            safe.put("knowledgeId", knowledgeId);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_VIEW);
            return FrontendResponse.ok(serviceResult(
                    knowledgeService.getDocImportTip(ctx.userId, ctx.orgId, safe)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/knowledge/export/record/list")
    public FrontendResponse<Map<String, Object>> listKnowledgeExportRecords(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "knowledgeId", required = false) String knowledgeId,
            @RequestParam(value = "pageNo", required = false) String pageNo,
            @RequestParam(value = "pageSize", required = false) String pageSize) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> safe = new LinkedHashMap<>();
            safe.put("knowledgeId", knowledgeId);
            safe.put("pageNo", pageNo);
            safe.put("pageSize", pageSize);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_VIEW);
            return FrontendResponse.ok(openApiExportRecordsResult(
                    knowledgeService.listExportRecords(ctx.userId, ctx.orgId, safe)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/knowledge/export/file/{exportRecordId}/{fileName:.+}")
    public ResponseEntity<byte[]> downloadKnowledgeExportRecord(
            @RequestHeader HttpHeaders headers,
            @PathVariable("exportRecordId") String exportRecordId,
            @PathVariable("fileName") String fileName) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> request = new LinkedHashMap<String, Object>();
            request.put("exportRecordId", exportRecordId);
            request.put("fileName", fileName);
            Map<String, Object> file = serviceResult(
                    knowledgeService.getExportRecordFile(ctx.userId, ctx.orgId, request));
            String resolvedName = defaultIfBlank(text(file, "fileName"), fileName);
            String contentType = defaultIfBlank(text(file, "contentType"), MediaType.APPLICATION_OCTET_STREAM_VALUE);
            byte[] payload = exportPayload(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resolvedName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(payload.length)
                    .body(payload);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new byte[0]);
        }
    }

    @DeleteMapping({"/knowledge/doc", "/knowledge/export/record"})
    public FrontendResponse<Map<String, Object>> knowledgeDeleteShell(
            @RequestHeader HttpHeaders headers,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            OpenApiContext ctx = context(headers);
            Map<String, Object> safe = body(request);
            authKnowledge(ctx, safe, "knowledgeId", KNOWLEDGE_EDIT);
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
    public ResponseEntity<String> mcpGet(@RequestParam(value = "key", required = false) String key,
                                         @RequestParam(value = "apiKey", required = false) String apiKey) {
        AppKeyInfo appKey = mcpAppKey(key, apiKey);
        String token = defaultIfBlank(key, apiKey);
        String event = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\",\"params\":{\"apiKey\":\""
                + jsonEscape(defaultIfBlank(token, "")) + "\",\"appId\":\""
                + jsonEscape(defaultIfBlank(appKey.getAppId(), "")) + "\"}}";
        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body("data: " + event + "\n\n");
    }

    @PostMapping({"/mcp/server/message", "/mcp/server/streamable"})
    public ResponseEntity<Map<String, Object>> mcpPost(@RequestParam(value = "key", required = false) String key,
                                                       @RequestParam(value = "apiKey", required = false) String apiKey,
                                                       @RequestBody(required = false) Map<String, Object> request) {
        AppKeyInfo appKey = mcpAppKey(key, apiKey);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jsonrpc", "2.0");
        data.put("id", request == null ? null : request.get("id"));
        data.put("result", mcpJsonRpcResult(appKey, body(request)));
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
            recordOauthVisit(text(app, "clientId"));
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
            recordOauthVisit(text(app, "clientId"));
            String code = "wanwu-oauth-code-" + compactId();
            saveOAuthCode(code, new OAuthCode(
                    text(app, "clientId"), userId, stringList(scope), Instant.now().plusSeconds(OAUTH_CODE_SECONDS).toEpochMilli()));
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
            OAuthCode payload = consumeOAuthCode(defaultIfBlank(code, ""));
            if (payload == null || !payload.clientId.equals(text(app, "clientId")) || payload.expiresAt < Instant.now().toEpochMilli()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody("invalid authorization code"));
            }
            recordOauthVisit(payload.clientId);
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
            OAuthToken oldRefresh = consumeOAuthRefreshToken(text(body, "refresh_token"));
            if (oldRefresh == null || !oldRefresh.clientId.equals(text(app, "clientId"))
                    || oldRefresh.expiresAt < Instant.now().toEpochMilli()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody("invalid refresh token"));
            }
            recordOauthVisit(oldRefresh.clientId);
            Map<String, Object> data = oauthTokenResponse(oldRefresh.userId, oldRefresh.clientId, oldRefresh.scopes);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("access_token", data.get("access_token"));
            response.put("refresh_token", data.get("refresh_token"));
            response.put("expires_at", String.valueOf(Instant.now().plusSeconds(OAUTH_ACCESS_TOKEN_SECONDS).toEpochMilli()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(ex.getMessage()));
        }
    }

    private Map<String, Object> oauthTokenResponse(String userId, String clientId, List<String> scopes) {
        long now = Instant.now().getEpochSecond();
        String accessToken = OAUTH_JWT.accessToken(userId, clientId, scopes, now + OAUTH_ACCESS_TOKEN_SECONDS);
        String refreshToken = "wanwu-oauth-refresh-" + compactId();
        OAuthToken refresh = new OAuthToken(
                userId, clientId, scopes, Instant.now().plusSeconds(OAUTH_REFRESH_TOKEN_SECONDS).toEpochMilli());
        saveOAuthRefreshToken(refreshToken, refresh);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("access_token", accessToken);
        data.put("expires_in", OAUTH_ACCESS_TOKEN_SECONDS);
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
        String token = OpenApiAuthSupport.extractToken(headers);
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
        recordOauthVisit(payload.clientId());
        return ResponseEntity.ok(data);
    }

    private void recordOauthVisit(String clientId) {
        OperationClientStatisticStore.INSTANCE.recordBrowse(clientId);
        if (operateService == null) {
            return;
        }
        try {
            operateService.addClientRecord(clientId);
        } catch (RuntimeException ignored) {
            // Keep OAuth runtime available even if the development Operate provider is temporarily unavailable.
        }
    }

    private OpenApiContext context(HttpHeaders headers) {
        OpenApiAuthSupport.AuthResult auth = OpenApiAuthSupport.resolve(
                appService, OpenApiAuthSupport.extractToken(headers));
        return new OpenApiContext(auth.userId, auth.orgId, auth.apiKeyId);
    }

    private void authModelByUuid(OpenApiContext ctx, Map<String, Object> body, String... fieldPaths) {
        List<String> uuids = nestedTextValues(body, fieldPaths);
        if (uuids.isEmpty()) {
            return;
        }
        if (modelService == null) {
            throw new OpenApiBadRequestException("model service unavailable");
        }
        try {
            List<String> modelIds = modelService.listModelIdsByUuids(uuids);
            modelService.checkModelUserPermission(ctx.userId, ctx.orgId,
                    modelIds == null ? Collections.<String>emptyList() : modelIds);
        } catch (RuntimeException ex) {
            throw new OpenApiBadRequestException(defaultIfBlank(ex.getMessage(), "model permission denied"));
        }
    }

    private void authKnowledge(OpenApiContext ctx, Map<String, Object> body, String fieldName, int permissionType) {
        String knowledgeId = nestedText(body, fieldName);
        if (isBlank(knowledgeId)) {
            throw new OpenApiBadRequestException("knowledgeId is required");
        }
        if (knowledgeService == null) {
            throw new OpenApiBadRequestException("knowledge service unavailable");
        }
        try {
            knowledgeService.checkKnowledgeUserPermission(ctx.userId, ctx.orgId, knowledgeId, permissionType);
        } catch (RuntimeException ex) {
            throw new OpenApiBadRequestException(defaultIfBlank(ex.getMessage(), "knowledge permission denied"));
        }
    }

    private List<String> nestedTextValues(Map<String, Object> body, String... fieldPaths) {
        List<String> values = new ArrayList<>();
        if (fieldPaths == null) {
            return values;
        }
        for (String fieldPath : fieldPaths) {
            String value = nestedText(body, fieldPath);
            if (!isBlank(value) && !values.contains(value)) {
                values.add(value);
            }
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private String nestedText(Map<String, Object> body, String fieldPath) {
        if (body == null || isBlank(fieldPath)) {
            return "";
        }
        Object current = body;
        for (String key : fieldPath.split("\\.")) {
            if (!(current instanceof Map)) {
                return "";
            }
            current = ((Map<String, Object>) current).get(key);
            if (current == null) {
                return "";
            }
        }
        return current instanceof String ? ((String) current).trim() : "";
    }

    private AppKeyInfo mcpAppKey(String key, String legacyApiKey) {
        String token = defaultIfBlank(key, legacyApiKey);
        if (isBlank(token)) {
            throw new OpenApiAuthException("token is nil");
        }
        if (appService == null) {
            throw new OpenApiAuthException("invalid app key");
        }
        AppKeyInfo appKey;
        try {
            appKey = appService.getAppKeyByKey(token);
        } catch (RuntimeException ex) {
            throw new OpenApiAuthException("invalid app key");
        }
        if (appKey == null) {
            throw new OpenApiAuthException("invalid app key");
        }
        if (!MCP_SERVER_APP_TYPE.equals(defaultIfBlank(appKey.getAppType(), ""))) {
            throw new OpenApiAuthException("invalid appType");
        }
        return appKey;
    }

    private Map<String, Object> mcpJsonRpcResult(AppKeyInfo appKey, Map<String, Object> request) {
        String method = text(request, "method");
        if ("initialize".equals(method)) {
            return mcpInitializeResult(appKey, request);
        }
        if ("ping".equals(method)) {
            return Collections.emptyMap();
        }
        if ("tools/list".equals(method)) {
            return Collections.<String, Object>singletonMap("tools", mcpToolDescriptors(appKey));
        }
        if ("tools/call".equals(method)) {
            return mcpToolCallResult(appKey, objectMap(request.get("params")));
        }
        return Collections.singletonMap("mcpServerId", defaultIfBlank(appKey.getAppId(), ""));
    }

    private Map<String, Object> mcpInitializeResult(AppKeyInfo appKey, Map<String, Object> request) {
        Map<String, Object> params = objectMap(request.get("params"));
        String protocolVersion = defaultIfBlank(text(params, "protocolVersion"), MCP_PROTOCOL_VERSION);

        Map<String, Object> tools = new LinkedHashMap<>();
        tools.put("listChanged", false);
        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("tools", tools);

        Map<String, Object> serverInfo = new LinkedHashMap<>();
        serverInfo.put("name", MCP_SERVER_NAME);
        serverInfo.put("version", "development");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", protocolVersion);
        result.put("capabilities", capabilities);
        result.put("serverInfo", serverInfo);
        result.put("mcpServerId", defaultIfBlank(appKey.getAppId(), ""));
        return result;
    }

    private List<Map<String, Object>> mcpToolDescriptors(AppKeyInfo appKey) {
        List<Map<String, Object>> tools = new ArrayList<>();
        for (Map<String, Object> tool : mcpServerTools(appKey)) {
            Map<String, Object> descriptor = new LinkedHashMap<>();
            descriptor.put("name", defaultIfBlank(text(tool, "methodName"), text(tool, "name")));
            descriptor.put("description", defaultIfBlank(text(tool, "desc"), text(tool, "description")));
            Map<String, Object> inputSchema = new LinkedHashMap<>();
            inputSchema.put("type", "object");
            inputSchema.put("additionalProperties", true);
            descriptor.put("inputSchema", inputSchema);
            tools.add(descriptor);
        }
        return tools;
    }

    private Map<String, Object> mcpToolCallResult(AppKeyInfo appKey, Map<String, Object> params) {
        String name = firstText(params, "name", "toolName", "methodName");
        Map<String, Object> arguments = objectMap(params.get("arguments"));
        if (mcpService != null) {
            try {
                Map<String, Object> call = new LinkedHashMap<>();
                call.put("name", name);
                call.put("arguments", arguments);
                Map<String, Object> result = mcpService.callMcpServerTool(
                        defaultIfBlank(appKey.getUserId(), DEV_ADMIN_ID),
                        defaultIfBlank(appKey.getOrgId(), DEV_ORG_ID),
                        defaultIfBlank(appKey.getAppId(), ""),
                        call);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            } catch (RuntimeException ignored) {
            }
        }
        Map<String, Object> matched = Collections.emptyMap();
        for (Map<String, Object> tool : mcpServerTools(appKey)) {
            String methodName = defaultIfBlank(text(tool, "methodName"), text(tool, "name"));
            if (name.equals(methodName)) {
                matched = tool;
                break;
            }
        }
        String text = "Executed MCP tool " + defaultIfBlank(name, "unknown")
                + " on server " + defaultIfBlank(appKey.getAppId(), "")
                + " with arguments " + arguments;
        if (!matched.isEmpty() && !isBlank(text(matched, "desc"))) {
            text = text + ". " + text(matched, "desc");
        }
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "text");
        content.put("text", text);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", Collections.singletonList(content));
        Map<String, Object> structuredContent = new LinkedHashMap<>();
        structuredContent.put("mcpServerId", defaultIfBlank(appKey.getAppId(), ""));
        structuredContent.put("name", defaultIfBlank(name, "unknown"));
        structuredContent.put("arguments", arguments);
        if (!matched.isEmpty()) {
            structuredContent.put("toolId", firstText(matched, "id", "toolId", "mcpServerToolId"));
            structuredContent.put("toolType", firstText(matched, "type", "toolType"));
            structuredContent.put("description", defaultIfBlank(text(matched, "desc"), text(matched, "description")));
        }
        result.put("structuredContent", structuredContent);
        result.put("isError", matched.isEmpty());
        return result;
    }

    private List<Map<String, Object>> mcpServerTools(AppKeyInfo appKey) {
        if (mcpService == null) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> server = mcpService.getMcpServer(
                    defaultIfBlank(appKey.getUserId(), DEV_ADMIN_ID),
                    defaultIfBlank(appKey.getOrgId(), DEV_ORG_ID),
                    defaultIfBlank(appKey.getAppId(), ""));
            return mapList(server.get("tools"));
        } catch (RuntimeException ex) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> serviceCreateChatflowConversation(OpenApiContext ctx, Map<String, Object> body) {
        String chatflowId = text(body, "uuid");
        if (appService != null) {
            try {
                ChatflowConversationCreateCommand command = new ChatflowConversationCreateCommand();
                command.setChatflowId(chatflowId);
                command.setConversationName(text(body, "conversation_name"));
                command.setUserId(ctx.userId);
                command.setOrgId(ctx.orgId);
                Map<String, Object> result = appService.createChatflowOpenApiConversation(command);
                if (result != null) {
                    return result;
                }
            } catch (RuntimeException ignored) {
            }
        }
        return chatflowSessionStore.create(ctx.userId, ctx.orgId, chatflowId, text(body, "conversation_name"));
    }

    private void serviceDeleteChatflowConversation(OpenApiContext ctx, Map<String, Object> body) {
        String chatflowId = text(body, "uuid");
        String conversationId = firstText(body, "conversation_id", "conversationId");
        if (appService != null) {
            try {
                ChatflowConversationDeleteByIdCommand command = new ChatflowConversationDeleteByIdCommand();
                command.setChatflowId(chatflowId);
                command.setConversationId(conversationId);
                command.setUserId(ctx.userId);
                command.setOrgId(ctx.orgId);
                appService.deleteChatflowOpenApiConversation(command);
                return;
            } catch (RuntimeException ignored) {
            }
        }
        chatflowSessionStore.delete(ctx.userId, ctx.orgId, chatflowId, conversationId);
    }

    private Map<String, Object> serviceListChatflowConversations(OpenApiContext ctx, Map<String, Object> body) {
        String chatflowId = text(body, "uuid");
        if (appService != null) {
            try {
                ChatflowConversationListQuery query = new ChatflowConversationListQuery();
                query.setChatflowId(chatflowId);
                query.setPageNo(1);
                query.setPageSize(1000);
                query.setUserId(ctx.userId);
                query.setOrgId(ctx.orgId);
                Map<String, Object> result = appService.listChatflowOpenApiConversations(query);
                if (result != null) {
                    return result;
                }
            } catch (RuntimeException ignored) {
            }
        }
        return chatflowSessionStore.list(ctx.userId, ctx.orgId, chatflowId);
    }

    private Map<String, Object> serviceListChatflowMessages(OpenApiContext ctx, Map<String, Object> body) {
        String chatflowId = text(body, "uuid");
        String conversationId = firstText(body, "conversation_id", "conversationId");
        int limit = intValue(body.get("limit"), 50);
        if (appService != null) {
            try {
                ChatflowConversationMessageListQuery query = new ChatflowConversationMessageListQuery();
                query.setChatflowId(chatflowId);
                query.setConversationId(conversationId);
                query.setLimit(limit);
                query.setUserId(ctx.userId);
                query.setOrgId(ctx.orgId);
                Map<String, Object> result = appService.listChatflowOpenApiConversationMessages(query);
                if (result != null) {
                    return result;
                }
            } catch (RuntimeException ignored) {
            }
        }
        return chatflowSessionStore.messages(ctx.userId, ctx.orgId, chatflowId, conversationId, limit);
    }

    private Map<String, Object> serviceChatflowChat(OpenApiContext ctx, Map<String, Object> body) {
        String chatflowId = text(body, "uuid");
        String conversationId = firstText(body, "conversation_id", "conversationId");
        String queryText = text(body, "query");
        Map<String, Object> parameters = objectMap(body.get("parameters"));
        if (appService != null) {
            try {
                ChatflowConversationChatCommand command = new ChatflowConversationChatCommand();
                command.setChatflowId(chatflowId);
                command.setConversationId(conversationId);
                command.setQuery(queryText);
                command.setParameters(parameters);
                command.setUserId(ctx.userId);
                command.setOrgId(ctx.orgId);
                Map<String, Object> result = appService.chatflowOpenApiChat(command);
                if (result != null) {
                    return result;
                }
            } catch (IllegalArgumentException ex) {
                throw ex;
            } catch (RuntimeException ignored) {
            }
        }
        return chatflowSessionStore.chat(ctx.userId, ctx.orgId, chatflowId, conversationId, queryText, parameters);
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

    private void attachConfiguredOpenApiAgentModelResponse(OpenApiContext ctx,
                                                           AssistantConversationStreamCommand command,
                                                           boolean draft,
                                                           long startedAt) {
        if (ctx == null || command == null || appService == null || modelService == null
                || isBlank(command.getAssistantId()) || isBlank(command.getPrompt())) {
            return;
        }
        try {
            Map<String, Object> assistant;
            if (draft) {
                assistant = appService.getAssistantDraft(new AssistantDetailQuery(
                        command.getAssistantId(), ctx.userId, ctx.orgId));
            } else {
                assistant = appService.getPublishedAssistant(new AssistantPublishedQuery(
                        command.getAssistantId(), null, ctx.userId, ctx.orgId));
            }
            String modelId = configuredModelId(assistant);
            if (isBlank(modelId)) {
                return;
            }
            ModelInfo model = modelService.getModel(ctx.userId, ctx.orgId, modelId);
            if (model == null || !Boolean.TRUE.equals(model.getIsActive())) {
                return;
            }
            OpenAiCompatibleChatClient.ChatCompletionResult chatResult =
                    chatClient.complete(model, modelId, command.getPrompt());
            String answer = defaultIfBlank(chatResult.getContent(), "");
            OpenAiCompatibleChatClient.Usage usage = chatResult.getUsage();
            if (isBlank(answer)) {
                return;
            }
            command.setOverrideResponse(answer);
            recordModelStatistic(ctx, model, modelId, command.getPrompt(), answer, startedAt, usage);
        } catch (RuntimeException ignored) {
        }
    }

    private void attachConfiguredOpenApiRagModelResponse(OpenApiContext ctx,
                                                         RagChatCommand command,
                                                         long startedAt) {
        if (ctx == null || command == null || appService == null || modelService == null
                || isBlank(command.getRagId()) || isBlank(command.getQuestion())) {
            return;
        }
        try {
            Map<String, Object> rag = appService.getPublishedRag(new RagDetailQuery(
                    command.getRagId(), null, ctx.userId, ctx.orgId));
            String modelId = configuredModelId(rag);
            if (isBlank(modelId)) {
                return;
            }
            ModelInfo model = modelService.getModel(ctx.userId, ctx.orgId, modelId);
            if (model == null || !Boolean.TRUE.equals(model.getIsActive())) {
                return;
            }
            OpenAiCompatibleChatClient.ChatCompletionResult chatResult =
                    chatClient.complete(model, modelId, ragChatMessages(command.getHistory(), command.getQuestion()));
            String answer = defaultIfBlank(chatResult.getContent(), "");
            OpenAiCompatibleChatClient.Usage usage = chatResult.getUsage();
            if (isBlank(answer)) {
                return;
            }
            command.setOverrideResponse(answer);
            recordModelStatistic(ctx, model, modelId, command.getQuestion(), answer, startedAt, usage);
        } catch (RuntimeException ignored) {
        }
    }

    private List<Map<String, Object>> ragChatMessages(List<Map<String, Object>> history, String question) {
        List<Map<String, Object>> messages = new ArrayList<>();
        if (history != null) {
            for (Map<String, Object> item : history) {
                if (item == null || !booleanValue(item.get("needHistory"), true)) {
                    continue;
                }
                addChatMessage(messages, "user", firstText(item, "query", "question", "prompt"));
                addChatMessage(messages, "assistant", firstText(item, "response", "answer", "content"));
            }
        }
        addChatMessage(messages, "user", question);
        return messages;
    }

    private void addChatMessage(List<Map<String, Object>> messages, String role, String content) {
        if (messages == null || isBlank(content)) {
            return;
        }
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        messages.add(message);
    }

    private void recordModelStatistic(OpenApiContext ctx,
                                      ModelInfo model,
                                      String modelId,
                                      String question,
                                      String answer,
                                      long startedAt,
                                      OpenAiCompatibleChatClient.Usage usage) {
        if (ctx == null || appService == null || model == null || isBlank(modelId)) {
            return;
        }
        try {
            long promptTokens = usage == null ? chatClient.estimateTokens(question) : usage.getPromptTokens();
            long completionTokens = usage == null ? chatClient.estimateTokens(answer) : usage.getCompletionTokens();
            long totalTokens = usage == null || usage.getTotalTokens() <= 0L
                    ? promptTokens + completionTokens : usage.getTotalTokens();
            RecordModelStatisticCommand command = new RecordModelStatisticCommand();
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
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
            command.setCosts(Math.max(1L, System.currentTimeMillis() - startedAt));
            appService.recordModelStatistic(command);
        } catch (RuntimeException ignored) {
        }
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
        return text(objectMap(map == null ? null : map.get(first)), second);
    }

    private String nestedText(Map<String, Object> map, String first, String second, String third) {
        return text(objectMap(objectMap(map == null ? null : map.get(first)).get(second)), third);
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
        data.put("search_list", openApiRagSearchList(result == null ? null : result.getSearchList()));
        data.put("history", Collections.emptyList());
        data.put("usage", usage());
        data.put("finish", 1);
        if (result != null) {
            data.put("conversation_id", result.getConversationId());
            data.put("detail_id", result.getDetailId());
        }
        return data;
    }

    private Map<String, Object> openApiAgentConversationCreate(AssistantConversationCreateResult result) {
        String conversationId = result == null ? "" : defaultIfBlank(result.getConversationId(), "");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("conversation_id", conversationId);
        data.put("conversationId", conversationId);
        return data;
    }

    private Map<String, Object> openApiRagChat(RagChatResult result) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", 0);
        data.put("message", "success");
        data.put("msg_id", result == null ? "" : defaultIfBlank(result.getRagId(), ""));
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("output", result == null ? "" : defaultIfBlank(result.getResponse(), ""));
        output.put("searchList", openApiRagSearchList(result == null ? null : result.getSearchList()));
        output.put("qaSearchList", result == null || result.getQaSearchList() == null
                ? Collections.emptyList() : result.getQaSearchList());
        data.put("data", output);
        data.put("history", Collections.emptyList());
        data.put("finish", 1);
        return data;
    }

    private List<Map<String, Object>> openApiRagSearchList(List<Map<String, Object>> searchList) {
        if (searchList == null || searchList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : searchList) {
            Map<String, Object> source = objectMap(item);
            Map<String, Object> target = new LinkedHashMap<>();
            target.put("kb_name", firstText(source, "kb_name", "kbName", "knowledgeName", "knowledge_name", "user_kb_name"));
            target.put("title", firstText(source, "title", "docName", "name"));
            target.put("snippet", firstText(source, "snippet", "content", "text"));
            result.add(target);
        }
        return result;
    }

    private String legacyOpenApiSse(Map<String, Object> response) {
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
        byte[] bytes = new byte[0];
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (file != null && !file.isEmpty()) {
            try {
                bytes = file.getBytes();
                contentType = defaultIfBlank(file.getContentType(), contentType);
            } catch (IOException ex) {
                throw new IllegalArgumentException("file upload failed: " + ex.getMessage());
            }
        }
        OPENAPI_UPLOADS.put(fileId, new OpenApiUploadedFile(fileName, contentType, bytes));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileId", fileId);
        data.put("file_id", fileId);
        data.put("fileName", fileName);
        data.put("file_name", fileName);
        data.put("url", "/service/api/openapi/v1/file/download/" + fileId);
        return data;
    }

    private String uploadUrl(MultipartFile file) {
        return defaultIfBlank(text(uploadBody(file), "url"), "");
    }

    private Map<String, Object> listResult(Object rows) {
        List<?> list = rows instanceof List ? (List<?>) rows : Collections.emptyList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", list.size());
        return data;
    }

    private Map<String, Object> openApiModelList(ModelListResult result) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (result != null && result.getList() != null) {
            for (ModelInfo model : result.getList()) {
                if (model == null) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("uuid", defaultIfBlank(model.getUuid(), ""));
                row.put("displayName", defaultIfBlank(model.getDisplayName(), ""));
                row.put("provider", defaultIfBlank(model.getProvider(), ""));
                row.put("modelType", defaultIfBlank(model.getModelType(), ""));
                row.put("model", defaultIfBlank(model.getModel(), ""));
                row.put("scopeType", defaultIfBlank(model.getScopeType(), ""));
                list.add(row);
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", result == null ? 0L : result.getTotal());
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

    private Map<String, Object> openApiExportRecordsResult(Map<String, Object> response) {
        Map<String, Object> data = new LinkedHashMap<>(serviceResult(response));
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : mapList(data.get("list"))) {
            Map<String, Object> copy = new LinkedHashMap<String, Object>(row);
            rewriteOpenApiExportFilePath(copy, "filePath");
            rewriteOpenApiExportFilePath(copy, "fileUrl");
            rewriteOpenApiExportFilePath(copy, "downloadUrl");
            rows.add(copy);
        }
        if (data.containsKey("list")) {
            data.put("list", rows);
        }
        return data;
    }

    private void rewriteOpenApiExportFilePath(Map<String, Object> row, String fieldName) {
        String value = text(row, fieldName);
        if (isBlank(value)) {
            return;
        }
        String marker = "/knowledge/export/file/";
        int index = value.indexOf(marker);
        if (index < 0) {
            return;
        }
        row.put(fieldName, "/service/api/openapi/v1" + value.substring(index));
    }

    private byte[] exportPayload(Map<String, Object> file) {
        String base64Content = text(file, "contentBase64");
        if (!isBlank(base64Content)) {
            return Base64.getDecoder().decode(base64Content);
        }
        return text(file, "content").getBytes(StandardCharsets.UTF_8);
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

    private void saveOAuthCode(String code, OAuthCode payload) {
        if (operateService != null) {
            try {
                operateService.saveOAuthCode(code, payload.toMap());
                return;
            } catch (RuntimeException ignored) {
            }
        }
        oauthCodes.put(code, payload);
    }

    private OAuthCode consumeOAuthCode(String code) {
        if (operateService != null) {
            try {
                Map<String, Object> payload = operateService.consumeOAuthCode(code);
                if (payload != null) {
                    return OAuthCode.from(payload);
                }
            } catch (RuntimeException ignored) {
            }
        }
        return oauthCodes.remove(code);
    }

    private void saveOAuthRefreshToken(String refreshToken, OAuthToken payload) {
        if (operateService != null) {
            try {
                operateService.saveOAuthRefreshToken(refreshToken, payload.toMap());
                return;
            } catch (RuntimeException ignored) {
            }
        }
        oauthRefreshTokens.put(refreshToken, payload);
    }

    private OAuthToken consumeOAuthRefreshToken(String refreshToken) {
        if (operateService != null) {
            try {
                Map<String, Object> payload = operateService.consumeOAuthRefreshToken(refreshToken);
                if (payload != null) {
                    return OAuthToken.from(payload);
                }
            } catch (RuntimeException ignored) {
            }
        }
        return oauthRefreshTokens.remove(refreshToken);
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

    private static String payloadText(Map<String, Object> payload, String key) {
        if (payload == null || key == null) {
            return "";
        }
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static long payloadLong(Map<String, Object> payload, String key) {
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

    private static List<String> payloadScopes(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<?> source = (List<?>) value;
        List<String> scopes = new ArrayList<>();
        for (Object item : source) {
            if (item != null) {
                scopes.add(String.valueOf(item));
            }
        }
        return scopes;
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

        private Map<String, Object> toMap() {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("clientId", clientId);
            payload.put("userId", userId);
            payload.put("scopes", scopes);
            payload.put("expiresAt", expiresAt);
            return payload;
        }

        private static OAuthCode from(Map<String, Object> payload) {
            return new OAuthCode(
                    payloadText(payload, "clientId"),
                    payloadText(payload, "userId"),
                    payloadScopes(payload == null ? null : payload.get("scopes")),
                    payloadLong(payload, "expiresAt"));
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

        private Map<String, Object> toMap() {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userId", userId);
            payload.put("clientId", clientId);
            payload.put("scopes", scopes);
            payload.put("expiresAt", expiresAt);
            return payload;
        }

        private static OAuthToken from(Map<String, Object> payload) {
            return new OAuthToken(
                    payloadText(payload, "userId"),
                    payloadText(payload, "clientId"),
                    payloadScopes(payload == null ? null : payload.get("scopes")),
                    payloadLong(payload, "expiresAt"));
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

    private static class OpenApiUploadedFile {
        private final String fileName;
        private final String contentType;
        private final byte[] bytes;

        private OpenApiUploadedFile(String fileName, String contentType, byte[] bytes) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.bytes = bytes == null ? new byte[0] : bytes;
        }
    }
}
