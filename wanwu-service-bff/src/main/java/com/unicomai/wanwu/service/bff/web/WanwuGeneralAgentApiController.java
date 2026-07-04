package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConfigQuery;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationListQuery;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationQuery;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationStateCommand;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.mcp.McpService;
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

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/service/api/v1/general/agent")
public class WanwuGeneralAgentApiController {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";
    private static final Object STORE_LOCK = new Object();
    private static final AtomicLong THREAD_SEQUENCE = new AtomicLong(1000);
    private static final AtomicLong RUN_SEQUENCE = new AtomicLong(1000);
    private static final AtomicLong SKILL_SEQUENCE = new AtomicLong(1000);
    private static final Map<String, ConversationState> CONVERSATIONS = new ConcurrentHashMap<>();
    private static final Map<String, LinkedList<ConversationState>> CONVERSATIONS_BY_SCOPE = new ConcurrentHashMap<>();

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private McpService mcpService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private KnowledgeService knowledgeService;

    public WanwuGeneralAgentApiController() {
    }

    public WanwuGeneralAgentApiController(AppService appService, McpService mcpService,
                                          KnowledgeService knowledgeService) {
        this.appService = appService;
        this.mcpService = mcpService;
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/sub/list")
    public FrontendResponse<Map<String, Object>> subList() {
        List<Map<String, Object>> agents = new ArrayList<>();
        agents.add(wgaAgent("Supervisor Agent", "WanwuBot",
                "Coordinate resources and answer through the Java reproduction shell."));
        agents.add(wgaAgent("General Agent", "General",
                "Answer questions with configured tools, MCPs, workflows, skills, and knowledge."));
        agents.add(wgaAgent("Data Analysis Agent", "Data Analysis",
                "Inspect uploaded data and produce deterministic analysis notes."));
        agents.add(wgaAgent("Skill Chat Agent", "Skill Chat",
                "Create, import, convert, preview, and iterate custom skills."));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("wgaAgentList", agents);
        return FrontendResponse.ok(result);
    }

    @GetMapping("/upload/limit")
    public FrontendResponse<Map<String, Object>> uploadLimit() {
        List<Map<String, Object>> limits = new ArrayList<>();
        limits.add(uploadLimit("image", Arrays.asList("png", "jpg", "jpeg", "webp"), 10L * 1024L * 1024L));
        limits.add(uploadLimit("document", Arrays.asList("pdf", "doc", "docx", "txt", "md", "xlsx", "pptx", "csv"),
                50L * 1024L * 1024L));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uploadLimitList", limits);
        result.put("list", limits);
        return FrontendResponse.ok(result);
    }

    @GetMapping("/assistant/select")
    public FrontendResponse<Map<String, Object>> assistantSelect(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam(value = "name", required = false) String name) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        ApplicationListQuery query = new ApplicationListQuery("agent", defaultIfBlank(name, ""), ctx.userId, ctx.orgId);
        ApplicationListResult result = safeApplicationResult(new AppResultCall() {
            @Override
            public ApplicationListResult execute() {
                return appService.listAssistants(query);
            }
        });
        return FrontendResponse.ok(listResult(result.getList(), result.getTotal()));
    }

    @GetMapping("/tool/select")
    public FrontendResponse<Map<String, Object>> toolSelect(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "agentId", required = false) String agentId) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        Map<String, Object> source = safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return mcpService.listToolSelect(ctx.userId, ctx.orgId, defaultIfBlank(name, ""));
            }
        });
        List<Map<String, Object>> tools = mapList(source.get("list"));
        List<Map<String, Object>> groups = new ArrayList<>();
        if (!tools.isEmpty()) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("category", defaultIfBlank(agentId, "default"));
            group.put("condition", "optional");
            group.put("toolList", tools);
            groups.add(group);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", groups);
        result.put("total", groups.size());
        return FrontendResponse.ok(result);
    }

    @GetMapping("/tool/info")
    public FrontendResponse<Map<String, Object>> toolInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam Map<String, String> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        final String toolId = firstText(request, "toolId", "id");
        final String toolType = defaultIfBlank(firstText(request, "toolType", "type"), "builtin");
        Map<String, Object> actions = safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return mcpService.listToolActions(ctx.userId, ctx.orgId, toolId, toolType);
            }
        });
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("toolId", toolId);
        result.put("toolType", toolType);
        result.put("toolName", defaultIfBlank(firstText(request, "toolName", "name"), toolId));
        result.put("name", defaultIfBlank(firstText(request, "toolName", "name"), toolId));
        result.put("desc", defaultIfBlank(firstText(request, "desc", "description"), ""));
        result.put("avatar", avatar(""));
        result.put("actions", mapList(actions.get("actions")));
        return FrontendResponse.ok(result);
    }

    @GetMapping("/mcp/select")
    public FrontendResponse<Map<String, Object>> mcpSelect(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam(value = "name", required = false) String name) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        return FrontendResponse.ok(safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return mcpService.listMcpSelect(ctx.userId, ctx.orgId, defaultIfBlank(name, ""));
            }
        }));
    }

    @GetMapping("/workflow/select")
    public FrontendResponse<Map<String, Object>> workflowSelect(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam(value = "name", required = false) String name) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        return FrontendResponse.ok(safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return appService.listAssistantWorkflowSelect(ctx.userId, ctx.orgId, defaultIfBlank(name, ""));
            }
        }));
    }

    @GetMapping("/skill/select")
    public FrontendResponse<Map<String, Object>> skillSelect(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "skillType", required = false) String skillType) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        return FrontendResponse.ok(safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return mcpService.listSkillSelect(ctx.userId, ctx.orgId, defaultIfBlank(name, ""), skillType);
            }
        }));
    }

    @PostMapping("/knowledge/select")
    public FrontendResponse<Map<String, Object>> knowledgeSelect(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        final Map<String, Object> body = body(request);
        return FrontendResponse.ok(safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return knowledgeService.selectKnowledge(ctx.userId, ctx.orgId, body);
            }
        }));
    }

    @GetMapping("/resource/select")
    public FrontendResponse<List<Map<String, Object>>> resourceSelect(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam(value = "name", required = false) String name) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        String search = defaultIfBlank(name, "");

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(section("assistant", toResourceList(applicationList(new AppResultCall() {
            @Override
            public ApplicationListResult execute() {
                return appService.listAssistants(new ApplicationListQuery("agent", search, ctx.userId, ctx.orgId));
            }
        }).getList(), "assistant")));
        result.add(section("mcp", toResourceList(mapList(safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return mcpService.listMcpSelect(ctx.userId, ctx.orgId, search);
            }
        }).get("list")), "mcp")));
        result.add(section("workflow", toResourceList(mapList(safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return appService.listAssistantWorkflowSelect(ctx.userId, ctx.orgId, search);
            }
        }).get("list")), "workflow")));
        result.add(section("skill", toResourceList(mapList(safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return mcpService.listSkillSelect(ctx.userId, ctx.orgId, search, null);
            }
        }).get("list")), "skill")));
        result.add(section("knowledge", toResourceList(knowledgeList(ctx, search), "knowledge")));
        return FrontendResponse.ok(result);
    }

    @PutMapping("/config")
    public FrontendResponse<Map<String, Object>> updateConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        synchronized (STORE_LOCK) {
            Map<String, List<Map<String, Object>>> config = defaultConfig();
            Map<String, Object> body = body(request);
            updateConfigSection(config, body, "tool");
            updateConfigSection(config, body, "mcp");
            updateConfigSection(config, body, "workflow");
            updateConfigSection(config, body, "skill");
            updateConfigSection(config, body, "assistant");
            updateConfigSection(config, body, "knowledge");
            updateConfigSection(config, body, "ontology");
            saveConfig(ctx, config);
        }
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping("/config")
    public FrontendResponse<List<Map<String, Object>>> getConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        return FrontendResponse.ok(configResponse(configForScope(ctx)));
    }

    @PostMapping("/conversation")
    public FrontendResponse<Map<String, Object>> createConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        ConversationState conversation = createConversationState(ctx, body(request), false);
        return FrontendResponse.ok(threadResult(conversation));
    }

    @DeleteMapping("/conversation")
    public FrontendResponse<Map<String, Object>> deleteConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        String threadId = firstText(body(request), "threadId", "id");
        synchronized (STORE_LOCK) {
            ConversationState removed = CONVERSATIONS.remove(threadId);
            if (removed != null) {
                LinkedList<ConversationState> conversations = CONVERSATIONS_BY_SCOPE.get(ctx.scope());
                if (conversations != null) {
                    conversations.remove(removed);
                }
            }
            deletePersistedConversation(ctx, threadId);
        }
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping("/conversation/list")
    public FrontendResponse<Map<String, Object>> conversationList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ConversationState conversation : conversationsForScope(ctx)) {
            rows.add(conversationSummary(conversation));
        }
        return FrontendResponse.ok(page(rows, intValue(pageNo, 1), intValue(pageSize, 10)));
    }

    @GetMapping("/conversation/detail")
    public FrontendResponse<Map<String, Object>> conversationDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam("threadId") String threadId) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        ConversationState conversation = conversationByThread(ctx, threadId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", conversation == null ? Collections.emptyList() : runList(conversation.runs));
        result.put("total", conversation == null ? 0 : conversation.runs.size());
        return FrontendResponse.ok(result);
    }

    @GetMapping("/conversation/config")
    public FrontendResponse<Map<String, Object>> conversationConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam("threadId") String threadId) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        ConversationState conversation = conversationByThread(ctx, threadId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("threadId", threadId);
        result.put("modelConfig", conversation == null ? Collections.emptyMap() : conversation.modelConfig);
        return FrontendResponse.ok(result);
    }

    @PutMapping("/conversation/config")
    public FrontendResponse<Map<String, Object>> updateConversationConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        Map<String, Object> body = body(request);
        String threadId = firstText(body, "threadId", "id");
        ConversationState conversation = conversationByThread(ctx, threadId);
        if (conversation != null) {
            conversation.modelConfig = objectMap(body.get("modelConfig"));
            conversation.updatedAt = System.currentTimeMillis();
            saveConversation(conversation);
        }
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/conversation/config/check")
    public FrontendResponse<Map<String, Object>> checkConversationConfig(
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("meet", true);
        result.put("modelMeet", true);
        result.put("toolsMeet", Collections.emptyList());
        return FrontendResponse.ok(result);
    }

    @PostMapping(value = "/conversation/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> conversationChat(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        return sseResponse(chat(ctx, body(request), false));
    }

    @GetMapping("/conversation/workspace")
    public FrontendResponse<Map<String, Object>> workspace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam("threadId") String threadId,
            @RequestParam(value = "runId", required = false) String runId,
            @RequestParam(value = "path", required = false) String path) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        return FrontendResponse.ok(workspaceInfo(ctx, threadId, runId, path));
    }

    @GetMapping("/conversation/workspace/preview")
    public ResponseEntity<byte[]> previewWorkspace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam("threadId") String threadId,
            @RequestParam(value = "runId", required = false) String runId,
            @RequestParam(value = "path", required = false) String path) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        byte[] bytes = workspaceContent(ctx, threadId, runId).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/conversation/workspace/download")
    public ResponseEntity<byte[]> downloadWorkspace(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam("threadId") String threadId,
            @RequestParam(value = "runId", required = false) String runId,
            @RequestParam(value = "path", required = false) String path) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        byte[] bytes = workspaceContent(ctx, threadId, runId).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"answer.md\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @PostMapping("/skill/conversation")
    public FrontendResponse<Map<String, Object>> createSkillConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        return FrontendResponse.ok(threadResult(createConversationState(ctx, body(request), true)));
    }

    @PostMapping("/skill/import/conversation")
    public FrontendResponse<Map<String, Object>> importSkillConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        return FrontendResponse.ok(threadResult(createConversationState(ctx, body(request), true)));
    }

    @PostMapping("/skill/convert/conversation")
    public FrontendResponse<Map<String, Object>> convertSkillConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        Map<String, Object> body = body(request);
        body.put("title", defaultIfBlank(firstText(body, "title", "name"), "Converted Skill"));
        return FrontendResponse.ok(threadResult(createConversationState(ctx, body, true)));
    }

    @PostMapping("/skill/refresh/conversation")
    public FrontendResponse<Map<String, Object>> refreshSkillConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        Map<String, Object> body = body(request);
        if (!hasText(firstText(body, "customSkillId", "skillId"))) {
            body.put("customSkillId", "custom-skill-wga-" + SKILL_SEQUENCE.incrementAndGet());
        }
        return FrontendResponse.ok(threadResult(createConversationState(ctx, body, true)));
    }

    @PostMapping(value = "/skill/conversation/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> skillConversationChat(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        return sseResponse(chat(ctx, body(request), true));
    }

    @GetMapping("/skill/preview/conversation/detail")
    public FrontendResponse<Map<String, Object>> skillPreviewConversationDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "x-user-id", required = false) String headerUserId,
            @RequestHeader(value = "x-org-id", required = false) String headerOrgId,
            @RequestParam(value = "previewId", required = false) String previewId,
            @RequestParam(value = "threadId", required = false) String threadId) {
        UserContext ctx = userContext(authorization, headerUserId, headerOrgId);
        ConversationState conversation = null;
        if (hasText(threadId)) {
            conversation = conversationByThread(ctx, threadId);
        }
        if (conversation == null && hasText(previewId)) {
            conversation = conversationByPreview(ctx, previewId);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", conversation == null ? Collections.emptyList() : runList(conversation.runs));
        result.put("total", conversation == null ? 0 : conversation.runs.size());
        return FrontendResponse.ok(result);
    }

    @PostMapping("/question/reply")
    public FrontendResponse<Map<String, Object>> replyQuestion(
            @RequestBody(required = false) Map<String, Object> request) {
        appendQuestionEvent(body(request), "answered");
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/question/reject")
    public FrontendResponse<Map<String, Object>> rejectQuestion(
            @RequestBody(required = false) Map<String, Object> request) {
        appendQuestionEvent(body(request), "rejected");
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    private ConversationState createConversationState(UserContext ctx, Map<String, Object> body, boolean skill) {
        long now = System.currentTimeMillis();
        ConversationState conversation = new ConversationState();
        conversation.scope = ctx.scope();
        conversation.threadId = "wga-thread-" + THREAD_SEQUENCE.incrementAndGet();
        conversation.title = defaultIfBlank(firstText(body, "title", "name"), skill ? "Skill Chat" : "New Conversation");
        conversation.userId = ctx.userId;
        conversation.orgId = ctx.orgId;
        conversation.createdAt = now;
        conversation.updatedAt = now;
        conversation.modelConfig = objectMap(body.get("modelConfig"));
        conversation.skillConversation = skill;
        conversation.skillId = defaultIfBlank(firstText(body, "customSkillId", "skillId"),
                skill ? "custom-skill-wga-" + SKILL_SEQUENCE.incrementAndGet() : "");
        conversation.previewId = skill ? "wga-preview-" + SKILL_SEQUENCE.incrementAndGet() : "";
        synchronized (STORE_LOCK) {
            CONVERSATIONS.put(conversation.threadId, conversation);
            LinkedList<ConversationState> scoped = CONVERSATIONS_BY_SCOPE.get(ctx.scope());
            if (scoped == null) {
                scoped = new LinkedList<>();
                CONVERSATIONS_BY_SCOPE.put(ctx.scope(), scoped);
            }
            scoped.addFirst(conversation);
        }
        saveConversation(conversation);
        return conversation;
    }

    private List<Map<String, Object>> chat(UserContext ctx, Map<String, Object> request, boolean skill) {
        String threadId = firstText(request, "threadId", "id");
        ConversationState conversation = hasText(threadId) ? conversationByThread(ctx, threadId) : null;
        if (conversation == null) {
            conversation = createConversationState(ctx, request, skill);
        }
        conversation.skillConversation = conversation.skillConversation || skill;
        String runId = "wga-run-" + RUN_SEQUENCE.incrementAndGet();
        String messageId = "wga-message-" + RUN_SEQUENCE.incrementAndGet();
        List<Map<String, Object>> messages = mapList(request.get("messages"));
        String prompt = lastUserContent(messages);
        String answer = answerText(defaultIfBlank(firstText(request, "agentId"), "General Agent"), prompt,
                conversation.skillConversation);
        long now = System.currentTimeMillis();

        List<Map<String, Object>> events = new ArrayList<>();
        events.add(event("type", "RUN_STARTED", "threadId", conversation.threadId, "runId", runId,
                "input", singleton("messages", messages), "timestamp", timestamp(now)));
        events.add(event("type", "TEXT_MESSAGE_START", "threadId", conversation.threadId, "runId", runId,
                "messageId", messageId, "role", "assistant", "timestamp", timestamp(now)));
        events.add(event("type", "TEXT_MESSAGE_CONTENT", "threadId", conversation.threadId, "runId", runId,
                "messageId", messageId, "delta", answer, "timestamp", timestamp(now + 1)));
        events.add(event("type", "TEXT_MESSAGE_END", "threadId", conversation.threadId, "runId", runId,
                "messageId", messageId, "timestamp", timestamp(now + 2)));
        events.add(event("type", "ACTIVITY_SNAPSHOT", "threadId", conversation.threadId, "runId", runId,
                "messageId", messageId, "activityId", "workspace-" + runId, "activityType", "workspace",
                "content", workspaceActivity(runId, answer), "timestamp", timestamp(now + 3)));
        events.add(event("type", "RUN_FINISHED", "threadId", conversation.threadId, "runId", runId,
                "timestamp", timestamp(now + 4)));

        RunState run = new RunState();
        run.threadId = conversation.threadId;
        run.runId = runId;
        run.createdAt = now;
        run.answer = answer;
        run.events = events;
        conversation.runs.add(0, run);
        conversation.updatedAt = now;
        saveConversation(conversation);
        return events;
    }

    private ResponseEntity<String> sseResponse(List<Map<String, Object>> events) {
        StringBuilder body = new StringBuilder();
        for (Map<String, Object> event : events) {
            body.append("data: ").append(toJson(event)).append("\n\n");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(body.toString());
    }

    private Map<String, Object> workspaceInfo(UserContext ctx, String threadId, String runId, String path) {
        RunState run = findRun(ctx, threadId, runId);
        String answer = run == null ? "" : run.answer;
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("name", "answer.md");
        file.put("path", "answer.md");
        file.put("type", "file");
        file.put("mimeType", "text/markdown");
        file.put("size", answer.getBytes(StandardCharsets.UTF_8).length);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("threadId", threadId);
        result.put("runId", run == null ? defaultIfBlank(runId, "") : run.runId);
        result.put("fileCount", run == null ? 0 : 1);
        result.put("totalSize", file.get("size"));
        result.put("isDisplay", run != null);
        result.put("path", defaultIfBlank(path, ""));
        result.put("files", run == null ? Collections.emptyList() : Collections.singletonList(file));
        return result;
    }

    private String workspaceContent(UserContext ctx, String threadId, String runId) {
        RunState run = findRun(ctx, threadId, runId);
        if (run == null) {
            return "# Wanwu General Agent\n\nNo workspace file has been generated yet.\n";
        }
        return "# Wanwu General Agent\n\n" + run.answer + "\n";
    }

    private RunState findRun(UserContext ctx, String threadId, String runId) {
        ConversationState conversation = conversationByThread(ctx, threadId);
        if (conversation == null) {
            return null;
        }
        for (RunState run : conversation.runs) {
            if (!hasText(runId) || runId.equals(run.runId)) {
                return run;
            }
        }
        return null;
    }

    private ConversationState conversationByThread(UserContext ctx, String threadId) {
        if (!hasText(threadId)) {
            return null;
        }
        ConversationState cached = CONVERSATIONS.get(threadId);
        if (cached != null) {
            return cached;
        }
        Map<String, Object> state = loadConversationState(ctx, threadId, "");
        if (state.isEmpty()) {
            return null;
        }
        ConversationState conversation = conversationFromState(ctx, state);
        cacheConversation(conversation);
        return conversation;
    }

    private ConversationState conversationByPreview(UserContext ctx, String previewId) {
        if (!hasText(previewId)) {
            return null;
        }
        for (ConversationState item : CONVERSATIONS.values()) {
            if (previewId.equals(item.previewId)) {
                return item;
            }
        }
        Map<String, Object> state = loadConversationState(ctx, "", previewId);
        if (state.isEmpty()) {
            return null;
        }
        ConversationState conversation = conversationFromState(ctx, state);
        cacheConversation(conversation);
        return conversation;
    }

    private List<ConversationState> conversationsForScope(UserContext ctx) {
        List<ConversationState> persisted = loadConversationStates(ctx);
        if (!persisted.isEmpty()) {
            for (ConversationState conversation : persisted) {
                cacheConversation(conversation);
            }
            return persisted;
        }
        synchronized (STORE_LOCK) {
            LinkedList<ConversationState> cached = CONVERSATIONS_BY_SCOPE.get(ctx.scope());
            return cached == null ? Collections.<ConversationState>emptyList() : new ArrayList<>(cached);
        }
    }

    private Map<String, Object> loadConversationState(UserContext ctx, String threadId, String previewId) {
        try {
            GeneralAgentConversationQuery query = new GeneralAgentConversationQuery();
            query.setUserId(ctx.userId);
            query.setOrgId(ctx.orgId);
            query.setThreadId(threadId);
            query.setPreviewId(previewId);
            Map<String, Object> state = appService.getGeneralAgentConversationState(query);
            return state == null ? Collections.<String, Object>emptyMap() : state;
        } catch (RuntimeException ex) {
            return Collections.emptyMap();
        }
    }

    private List<ConversationState> loadConversationStates(UserContext ctx) {
        List<ConversationState> result = new ArrayList<>();
        try {
            GeneralAgentConversationListQuery query = new GeneralAgentConversationListQuery();
            query.setUserId(ctx.userId);
            query.setOrgId(ctx.orgId);
            List<Map<String, Object>> states = appService.listGeneralAgentConversationStates(query);
            if (states == null) {
                return result;
            }
            for (Map<String, Object> state : states) {
                result.add(conversationFromState(ctx, state));
            }
        } catch (RuntimeException ex) {
            return Collections.emptyList();
        }
        return result;
    }

    private void cacheConversation(ConversationState conversation) {
        if (conversation == null || !hasText(conversation.threadId)) {
            return;
        }
        synchronized (STORE_LOCK) {
            CONVERSATIONS.put(conversation.threadId, conversation);
            LinkedList<ConversationState> scoped = CONVERSATIONS_BY_SCOPE.get(conversation.scope);
            if (scoped == null) {
                scoped = new LinkedList<>();
                CONVERSATIONS_BY_SCOPE.put(conversation.scope, scoped);
            }
            scoped.removeIf(item -> conversation.threadId.equals(item.threadId));
            scoped.addFirst(conversation);
        }
    }

    private void saveConversation(ConversationState conversation) {
        if (conversation == null || !hasText(conversation.threadId)) {
            return;
        }
        try {
            GeneralAgentConversationStateCommand command = new GeneralAgentConversationStateCommand();
            command.setUserId(conversation.userId);
            command.setOrgId(conversation.orgId);
            command.setThreadId(conversation.threadId);
            command.setTitle(conversation.title);
            command.setCreatedAt(conversation.createdAt);
            command.setUpdatedAt(conversation.updatedAt);
            command.setSkillConversation(conversation.skillConversation);
            command.setSkillId(conversation.skillId);
            command.setPreviewId(conversation.previewId);
            command.setModelConfig(conversation.modelConfig);
            command.setRuns(runStateList(conversation.runs));
            appService.saveGeneralAgentConversationState(command);
        } catch (RuntimeException ex) {
            // Keep the development shell usable even if the RPC provider is temporarily unavailable.
        }
    }

    private void deletePersistedConversation(UserContext ctx, String threadId) {
        if (!hasText(threadId)) {
            return;
        }
        try {
            GeneralAgentConversationDeleteCommand command = new GeneralAgentConversationDeleteCommand();
            command.setUserId(ctx.userId);
            command.setOrgId(ctx.orgId);
            command.setThreadId(threadId);
            appService.deleteGeneralAgentConversationState(command);
        } catch (RuntimeException ex) {
            // Local delete already happened; persistence will be retried by future writes.
        }
    }

    private ConversationState conversationFromState(UserContext ctx, Map<String, Object> state) {
        ConversationState conversation = new ConversationState();
        conversation.userId = ctx.userId;
        conversation.orgId = ctx.orgId;
        conversation.scope = ctx.scope();
        conversation.threadId = defaultIfBlank(firstText(state, "threadId"), "");
        conversation.title = defaultIfBlank(firstText(state, "title"), "New Conversation");
        conversation.createdAt = longValue(state.get("createdAt"), System.currentTimeMillis());
        conversation.updatedAt = longValue(state.get("updatedAt"), conversation.createdAt);
        conversation.skillConversation = booleanValue(state.get("isSkillConversation"));
        conversation.skillId = defaultIfBlank(firstText(state, "skillId"), "");
        conversation.previewId = defaultIfBlank(firstText(state, "previewId"), "");
        conversation.modelConfig = objectMap(state.get("modelConfig"));
        conversation.runs.addAll(runsFromState(mapList(state.get("runs"))));
        return conversation;
    }

    private List<Map<String, Object>> runStateList(List<RunState> runs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (RunState run : runs) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("threadId", run.threadId);
            row.put("runId", run.runId);
            row.put("createdAt", run.createdAt);
            row.put("answer", run.answer);
            row.put("events", run.events);
            result.add(row);
        }
        return result;
    }

    private List<RunState> runsFromState(List<Map<String, Object>> rows) {
        List<RunState> runs = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            RunState run = new RunState();
            run.threadId = defaultIfBlank(firstText(row, "threadId"), "");
            run.runId = defaultIfBlank(firstText(row, "runId"), "");
            run.createdAt = longValue(row.get("createdAt"), System.currentTimeMillis());
            run.answer = defaultIfBlank(firstText(row, "answer"), "");
            run.events = mapList(row.get("events"));
            runs.add(run);
        }
        return runs;
    }

    private void appendQuestionEvent(Map<String, Object> request, String status) {
        final String runId = firstText(request, "runId");
        final String questionId = firstText(request, "questionId");
        if (!hasText(runId) || !hasText(questionId)) {
            return;
        }
        for (ConversationState conversation : CONVERSATIONS.values()) {
            for (RunState run : conversation.runs) {
                if (runId.equals(run.runId)) {
                    Map<String, Object> content = new LinkedHashMap<>();
                    content.put("status", status);
                    content.put("runId", runId);
                    content.put("questionId", questionId);
                    content.put("answers", request.get("answers"));
                    run.events.add(event("type", "ACTIVITY_SNAPSHOT", "threadId", conversation.threadId, "runId", runId,
                            "activityId", "question-" + questionId, "activityType", "question",
                            "content", content, "timestamp", timestamp(System.currentTimeMillis())));
                    conversation.updatedAt = System.currentTimeMillis();
                    saveConversation(conversation);
                    return;
                }
            }
        }
    }

    private List<Map<String, Object>> knowledgeList(UserContext ctx, String name) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("name", defaultIfBlank(name, ""));
        query.put("category", 0);
        query.put("external", -1);
        query.put("tagId", Collections.emptyList());
        Map<String, Object> source = safeMap(new MapCall() {
            @Override
            public Map<String, Object> execute() {
                return knowledgeService.selectKnowledge(ctx.userId, ctx.orgId, query);
            }
        });
        List<Map<String, Object>> list = mapList(source.get("knowledgeList"));
        if (list.isEmpty()) {
            list = mapList(source.get("list"));
        }
        return list;
    }

    private ApplicationListResult applicationList(AppResultCall call) {
        return safeApplicationResult(call);
    }

    private ApplicationListResult safeApplicationResult(AppResultCall call) {
        try {
            if (appService == null) {
                return new ApplicationListResult(Collections.<Map<String, Object>>emptyList(), 0);
            }
            ApplicationListResult result = call.execute();
            return result == null ? new ApplicationListResult(Collections.<Map<String, Object>>emptyList(), 0) : result;
        } catch (RuntimeException ex) {
            return new ApplicationListResult(Collections.<Map<String, Object>>emptyList(), 0);
        }
    }

    private Map<String, Object> safeMap(MapCall call) {
        try {
            Map<String, Object> result = call.execute();
            return result == null ? listResult(Collections.<Map<String, Object>>emptyList(), 0) : result;
        } catch (RuntimeException ex) {
            return listResult(Collections.<Map<String, Object>>emptyList(), 0);
        }
    }

    private Map<String, Object> listResult(List<Map<String, Object>> list, long total) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list == null ? Collections.emptyList() : list);
        result.put("total", total);
        return result;
    }

    private Map<String, Object> page(List<Map<String, Object>> all, int pageNo, int pageSize) {
        int safePageNo = pageNo <= 0 ? 1 : pageNo;
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int from = Math.min((safePageNo - 1) * safePageSize, all.size());
        int to = Math.min(from + safePageSize, all.size());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", new ArrayList<>(all.subList(from, to)));
        result.put("total", all.size());
        result.put("pageNo", safePageNo);
        result.put("pageSize", safePageSize);
        return result;
    }

    private Map<String, Object> section(String listType, List<Map<String, Object>> list) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("listType", listType);
        result.put("list", list);
        return result;
    }

    private List<Map<String, Object>> toResourceList(List<Map<String, Object>> source, String type) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : source) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", resourceId(item, type));
            row.put("name", defaultIfBlank(firstText(item, "name", "toolName", "appName", "knowledgeName"), resourceId(item, type)));
            row.put("desc", defaultIfBlank(firstText(item, "desc", "description", "intro"), ""));
            row.put("avatar", item.get("avatar") == null ? avatar("") : item.get("avatar"));
            row.put("type", defaultIfBlank(firstText(item, "type", "toolType", "appType"), type));
            row.put("author", defaultIfBlank(firstText(item, "author", "creatorName", "creator"), ""));
            result.add(row);
        }
        return result;
    }

    private String resourceId(Map<String, Object> item, String type) {
        String value = firstText(item, "id", "appId", "assistantId", "mcpId", "toolId", "skillId", "knowledgeId",
                "workFlowId", "workflowId");
        return hasText(value) ? value : type + "-" + resultIndex(item);
    }

    private int resultIndex(Map<String, Object> item) {
        return Math.abs(item.hashCode());
    }

    private Map<String, List<Map<String, Object>>> configForScope(UserContext ctx) {
        Map<String, List<Map<String, Object>>> config = defaultConfig();
        Map<String, List<Map<String, Object>>> persisted = loadConfig(ctx);
        for (Map.Entry<String, List<Map<String, Object>>> entry : persisted.entrySet()) {
            if (config.containsKey(entry.getKey())) {
                config.put(entry.getKey(), copyList(entry.getValue()));
            }
        }
        return config;
    }

    private Map<String, List<Map<String, Object>>> loadConfig(UserContext ctx) {
        try {
            GeneralAgentConfigQuery query = new GeneralAgentConfigQuery();
            query.setUserId(ctx.userId);
            query.setOrgId(ctx.orgId);
            Map<String, List<Map<String, Object>>> config = appService.getGeneralAgentConfig(query);
            return config == null ? Collections.<String, List<Map<String, Object>>>emptyMap() : config;
        } catch (RuntimeException ex) {
            return Collections.emptyMap();
        }
    }

    private void saveConfig(UserContext ctx, Map<String, List<Map<String, Object>>> config) {
        GeneralAgentConfigUpdateCommand command = new GeneralAgentConfigUpdateCommand();
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setConfig(config);
        appService.updateGeneralAgentConfig(command);
    }

    private Map<String, List<Map<String, Object>>> defaultConfig() {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        result.put("tool", new ArrayList<Map<String, Object>>());
        result.put("mcp", new ArrayList<Map<String, Object>>());
        result.put("workflow", new ArrayList<Map<String, Object>>());
        result.put("skill", new ArrayList<Map<String, Object>>());
        result.put("assistant", new ArrayList<Map<String, Object>>());
        result.put("knowledge", new ArrayList<Map<String, Object>>());
        return result;
    }

    private void updateConfigSection(Map<String, List<Map<String, Object>>> config, Map<String, Object> body,
                                     String section) {
        if (!body.containsKey(section)) {
            return;
        }
        if ("ontology".equals(section)) {
            config.remove(section);
            return;
        }
        List<Map<String, Object>> source = mapList(body.get(section));
        config.put(section, normalizeConfigItems(source, section));
    }

    private List<Map<String, Object>> normalizeConfigItems(List<Map<String, Object>> source, String section) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : source) {
            Map<String, Object> row = new LinkedHashMap<>();
            if ("tool".equals(section)) {
                row.put("toolId", firstText(item, "toolId", "id"));
                row.put("toolType", defaultIfBlank(firstText(item, "toolType", "type"), "builtin"));
            } else {
                row.put("id", firstText(item, "id", "appId", "mcpId", "skillId", "knowledgeId", "workflowId", "workFlowId"));
                row.put("type", defaultIfBlank(firstText(item, "type", "appType", "toolType"), section));
            }
            result.add(row);
        }
        return result;
    }

    private List<Map<String, Object>> configResponse(Map<String, List<Map<String, Object>>> config) {
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(section("tool", copyList(config.get("tool"))));
        result.add(section("mcp", copyList(config.get("mcp"))));
        result.add(section("workflow", copyList(config.get("workflow"))));
        result.add(section("skill", copyList(config.get("skill"))));
        result.add(section("assistant", copyList(config.get("assistant"))));
        result.add(section("knowledge", copyList(config.get("knowledge"))));
        return result;
    }

    private List<Map<String, Object>> copyList(List<Map<String, Object>> source) {
        if (source == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : source) {
            result.add(new LinkedHashMap<>(item));
        }
        return result;
    }

    private Map<String, Object> threadResult(ConversationState conversation) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("threadId", conversation.threadId);
        result.put("customSkillId", conversation.skillId);
        result.put("previewId", conversation.previewId);
        return result;
    }

    private Map<String, Object> conversationSummary(ConversationState conversation) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("threadId", conversation.threadId);
        row.put("title", conversation.title);
        row.put("createdAt", timestamp(conversation.createdAt));
        row.put("isSkillConversation", conversation.skillConversation);
        row.put("skillId", conversation.skillId);
        row.put("previewId", conversation.previewId);
        return row;
    }

    private List<Map<String, Object>> runList(List<RunState> runs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (RunState run : runs) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("threadId", run.threadId);
            row.put("runId", run.runId);
            row.put("createdAt", timestamp(run.createdAt));
            row.put("events", run.events);
            result.add(row);
        }
        return result;
    }

    private Map<String, Object> workspaceActivity(String runId, String answer) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("status", "finished");
        content.put("runId", runId);
        content.put("fileCount", 1);
        content.put("totalSize", answer.getBytes(StandardCharsets.UTF_8).length);
        content.put("isDisplay", true);
        return content;
    }

    private String answerText(String agentId, String prompt, boolean skill) {
        String mode = skill ? "Skill Chat Agent" : defaultIfBlank(agentId, "General Agent");
        String question = defaultIfBlank(prompt, "empty message");
        return mode + " received: " + question
                + "\n\nThis response is generated by the Java WGA compatibility layer for frontend validation.";
    }

    private String lastUserContent(List<Map<String, Object>> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            Map<String, Object> message = messages.get(i);
            if ("user".equals(firstText(message, "role"))) {
                return contentText(message.get("content"));
            }
        }
        if (!messages.isEmpty()) {
            return contentText(messages.get(messages.size() - 1).get("content"));
        }
        return "";
    }

    private String contentText(Object content) {
        if (content == null) {
            return "";
        }
        if (content instanceof String) {
            return String.valueOf(content);
        }
        if (content instanceof List) {
            StringBuilder builder = new StringBuilder();
            for (Object item : (List<?>) content) {
                Map<String, Object> map = objectMap(item);
                if ("text".equals(firstText(map, "type"))) {
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                    builder.append(firstText(map, "text"));
                }
            }
            return builder.toString();
        }
        Map<String, Object> map = objectMap(content);
        return firstText(map, "text", "content");
    }

    private Map<String, Object> wgaAgent(String agentId, String agentName, String placeholder) {
        Map<String, Object> agent = new LinkedHashMap<>();
        agent.put("agentId", agentId);
        agent.put("agentName", agentName);
        agent.put("placeholder", placeholder);
        agent.put("avatar", avatar(""));
        return agent;
    }

    private Map<String, Object> uploadLimit(String type, List<String> extensions, long maxSize) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("type", type);
        row.put("extensions", extensions);
        row.put("suffix", extensions);
        row.put("maxSize", maxSize);
        row.put("size", maxSize);
        return row;
    }

    private Map<String, Object> avatar(String path) {
        Map<String, Object> avatar = new LinkedHashMap<>();
        avatar.put("path", defaultIfBlank(path, ""));
        return avatar;
    }

    private UserContext userContext(String authorization, String headerUserId, String headerOrgId) {
        String token = extractToken(authorization);
        String userId = defaultIfBlank(headerUserId, "dev-token-app".equals(token) ? DEV_APP_USER_ID : DEV_USER_ID);
        String orgId = defaultIfBlank(headerOrgId, DEV_ORG_ID);
        return new UserContext(userId, orgId);
    }

    private String extractToken(String authorization) {
        if (!hasText(authorization)) {
            return "";
        }
        String value = authorization.trim();
        return value.startsWith("Bearer ") ? value.substring("Bearer ".length()) : value;
    }

    private Map<String, Object> body(Map<String, Object> request) {
        return request == null ? new LinkedHashMap<String, Object>() : new LinkedHashMap<>(request);
    }

    private Map<String, Object> event(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    private Map<String, Object> objectMap(Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(value instanceof Map)) {
            return result;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private List<Map<String, Object>> mapList(Object value) {
        if (!(value instanceof List)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            result.add(objectMap(item));
        }
        return result;
    }

    private String firstText(Map<?, ?> source, String... keys) {
        if (source == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null && hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private int intValue(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private long longValue(Object value, long defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private String timestamp(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date(millis));
    }

    private String toJson(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"type\":\"RUN_FINISHED\"}";
        }
    }

    private interface MapCall {
        Map<String, Object> execute();
    }

    private interface AppResultCall {
        ApplicationListResult execute();
    }

    private static final class UserContext {
        private final String userId;
        private final String orgId;

        private UserContext(String userId, String orgId) {
            this.userId = userId;
            this.orgId = orgId;
        }

        private String scope() {
            return orgId + ":" + userId;
        }
    }

    private static final class ConversationState {
        private String scope;
        private String threadId;
        private String title;
        private String userId;
        private String orgId;
        private long createdAt;
        private long updatedAt;
        private boolean skillConversation;
        private String skillId;
        private String previewId;
        private Map<String, Object> modelConfig = new LinkedHashMap<>();
        private final List<RunState> runs = new ArrayList<>();
    }

    private static final class RunState {
        private String threadId;
        private String runId;
        private long createdAt;
        private String answer;
        private List<Map<String, Object>> events = new ArrayList<>();
    }
}
