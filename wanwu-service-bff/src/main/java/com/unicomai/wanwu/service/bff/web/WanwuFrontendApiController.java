package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCopyCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationPageResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantPublishedQuery;
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.AppVersionRollbackCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationSelectResult;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuFrontendApiController {

    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
    private static final String AGENT_APP_TYPE = "agent";
    private static final String CONVERSATION_TYPE_PUBLISHED = "published";
    private static final String CONVERSATION_TYPE_DRAFT = "draft";

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    public WanwuFrontendApiController() {
    }

    public WanwuFrontendApiController(IamService iamService, AppService appService) {
        this.iamService = iamService;
        this.appService = appService;
    }

    @GetMapping("/base/captcha")
    public FrontendResponse<CaptchaResult> captcha() {
        return FrontendResponse.ok(iamService.captcha());
    }

    @PostMapping("/base/login")
    public FrontendResponse<LoginResult> login(@RequestBody LoginCommand command) {
        try {
            return FrontendResponse.ok(iamService.login(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/user/permission")
    public FrontendResponse<PermissionResult> permission(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return FrontendResponse.ok(iamService.permission(extractToken(authorization)));
    }

    @GetMapping("/org/select")
    public FrontendResponse<OrganizationSelectResult> selectOrganizations() {
        return FrontendResponse.ok(iamService.selectOrganizations());
    }

    @GetMapping("/base/custom")
    public FrontendResponse<Map<String, Object>> platformConfig() {
        return FrontendResponse.ok(iamService.platformConfig());
    }

    @GetMapping("/appspace/assistant/list")
    public FrontendResponse<ApplicationListResult> assistantList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "appType", required = false) String appType,
            @RequestParam(value = "name", required = false) String name) {
        UserContext userContext = userContext(authorization);
        String effectiveAppType = isBlank(appType) ? AGENT_APP_TYPE : appType;
        return FrontendResponse.ok(appService.listAssistants(
                new ApplicationListQuery(effectiveAppType, name, userContext.getUserId(), userContext.getOrgId())));
    }

    @PostMapping("/assistant")
    public FrontendResponse<AssistantCreateResult> createAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantCreateRequest request) {
        UserContext userContext = userContext(authorization);
        AssistantCreateCommand command = request.toCommand();
        command.setUserId(userContext.getUserId());
        command.setOrgId(userContext.getOrgId());
        return FrontendResponse.ok(appService.createAssistant(command));
    }

    @PutMapping("/assistant")
    public FrontendResponse<Map<String, Object>> updateAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantUpdateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantUpdateCommand command = request.toUpdateCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateAssistant(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/assistant/config")
    public FrontendResponse<Map<String, Object>> updateAssistantConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantConfigRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConfigUpdateCommand command = request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateAssistantConfig(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/appspace/app")
    public FrontendResponse<Map<String, Object>> deleteAppspaceApp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppspaceAppDeleteRequest request) {
        try {
            if (request == null || !AGENT_APP_TYPE.equals(request.getAppType())) {
                return FrontendResponse.failure(1001, "unsupported app type");
            }
            return deleteAssistant(userContext(authorization), request.getAppId());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/assistant")
    public FrontendResponse<Map<String, Object>> deleteAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantDeleteRequest request) {
        try {
            return deleteAssistant(userContext(authorization), request == null ? null : request.getAssistantId());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/assistant/copy")
    public FrontendResponse<AssistantCreateResult> copyAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AssistantCopyRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantCopyCommand command = new AssistantCopyCommand();
            command.setAssistantId(request == null ? null : request.getAssistantId());
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.copyAssistant(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/app/publish")
    public FrontendResponse<Map<String, Object>> publishApp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppPublishRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppPublishCommand command = request == null ? new AppPublishCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.publishApp(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/appspace/app/publish")
    public FrontendResponse<Map<String, Object>> unpublishApp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppPublishRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppPublishCommand command = request == null ? new AppPublishCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.unpublishApp(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/app/version")
    public FrontendResponse<AppVersionInfo> getLatestAppVersion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("appId") String appId,
            @RequestParam("appType") String appType) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.getLatestAppVersion(
                    new AppVersionQuery(appId, appType, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/appspace/app/version/list")
    public FrontendResponse<AppVersionListResult> listAppVersions(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("appId") String appId,
            @RequestParam("appType") String appType) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.listAppVersions(
                    new AppVersionQuery(appId, appType, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PutMapping("/appspace/app/version")
    public FrontendResponse<Map<String, Object>> updateAppVersion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppVersionUpdateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppVersionUpdateCommand command = request == null ? new AppVersionUpdateCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.updateAppVersion(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/appspace/app/version/rollback")
    public FrontendResponse<Map<String, Object>> rollbackAppVersion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AppVersionRollbackRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AppVersionRollbackCommand command = request == null ? new AppVersionRollbackCommand() : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.rollbackAppVersion(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant")
    public FrontendResponse<Map<String, Object>> assistantPublished(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("assistantId") String assistantId,
            @RequestParam(value = "version", required = false) String version) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.getPublishedAssistant(
                    new AssistantPublishedQuery(assistantId, version, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant/draft")
    public FrontendResponse<Map<String, Object>> assistantDraft(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("assistantId") String assistantId) {
        try {
            UserContext userContext = userContext(authorization);
            return FrontendResponse.ok(appService.getAssistantDraft(
                    new AssistantDetailQuery(assistantId, userContext.getUserId(), userContext.getOrgId())));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping("/assistant/conversation")
    public FrontendResponse<AssistantConversationCreateResult> createAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationCreateRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationCreateCommand command = request == null
                    ? new AssistantConversationCreateCommand()
                    : request.toCommand(CONVERSATION_TYPE_PUBLISHED);
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.createAssistantConversation(command));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/assistant/conversation")
    public FrontendResponse<Map<String, Object>> deleteAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationDeleteRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationDeleteCommand command = request == null
                    ? new AssistantConversationDeleteCommand()
                    : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.deleteAssistantConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/assistant/conversation/clear")
    public FrontendResponse<Map<String, Object>> clearAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationDeleteRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationDeleteCommand command = request == null
                    ? new AssistantConversationDeleteCommand()
                    : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.clearAssistantConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant/conversation/list")
    public FrontendResponse<AssistantConversationPageResult> listAssistantConversations(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("assistantId") String assistantId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationListQuery query = new AssistantConversationListQuery();
            query.setAssistantId(assistantId);
            query.setConversationType(CONVERSATION_TYPE_PUBLISHED);
            query.setPageNo(pageNo);
            query.setPageSize(pageSize);
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.listAssistantConversations(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant/conversation/detail")
    public FrontendResponse<AssistantConversationPageResult> listAssistantConversationDetails(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("conversationId") String conversationId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationDetailQuery query = new AssistantConversationDetailQuery();
            query.setConversationId(conversationId);
            query.setPageNo(pageNo);
            query.setPageSize(pageSize);
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.listAssistantConversationDetails(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @GetMapping("/assistant/conversation/draft/detail")
    public FrontendResponse<AssistantConversationPageResult> listDraftAssistantConversationDetails(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("assistantId") String assistantId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationListQuery query = new AssistantConversationListQuery();
            query.setAssistantId(assistantId);
            query.setConversationType(CONVERSATION_TYPE_DRAFT);
            query.setPageNo(pageNo);
            query.setPageSize(pageSize);
            query.setUserId(userContext.getUserId());
            query.setOrgId(userContext.getOrgId());
            return FrontendResponse.ok(appService.listDraftAssistantConversationDetails(query));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @DeleteMapping("/assistant/conversation/draft")
    public FrontendResponse<Map<String, Object>> deleteDraftAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationDeleteRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationDeleteCommand command = request == null
                    ? new AssistantConversationDeleteCommand()
                    : request.toCommand();
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            appService.deleteDraftAssistantConversation(command);
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    @PostMapping(value = "/assistant/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> assistantStream(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationStreamRequest request) {
        return streamAssistantConversation(authorization, request, false);
    }

    @PostMapping(value = {"/assistant/stream/draft", "/assistant/test/stream"},
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> assistantDraftStream(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ConversationStreamRequest request) {
        return streamAssistantConversation(authorization, request, true);
    }

    @PostMapping(value = "/assistant/question/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> assistantQuestionRecommend(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody QuestionRecommendRequest request) {
        try {
            UserContext userContext = userContext(authorization);
            QuestionRecommendRequest effectiveRequest = request == null
                    ? new QuestionRecommendRequest()
                    : request;
            if (isBlank(effectiveRequest.getAssistantId())) {
                throw new IllegalArgumentException("assistantId is required");
            }
            if (isBlank(effectiveRequest.getQuery())) {
                throw new IllegalArgumentException("query is required");
            }
            Map<String, Object> assistantInfo = resolveRecommendAssistant(userContext, effectiveRequest);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(toRecommendSseFrames(effectiveRequest, assistantInfo));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        }
    }

    @GetMapping("/appspace/app/url")
    public FrontendResponse<String> appOpenUrl() {
        return FrontendResponse.ok("");
    }

    @GetMapping({
            "/model/select/{modelType}",
            "/prompt/custom/list",
            "/prompt/template/list",
            "/mcp/select",
            "/workflow/select",
            "/safe/sensitive/table/select",
            "/tool/select",
            "/agent/skill/select",
    })
    public FrontendResponse<Map<String, Object>> emptySelectResult() {
        return FrontendResponse.ok(emptyListResult());
    }

    @PostMapping("/knowledge/select")
    public FrontendResponse<Map<String, Object>> emptyPostSelectResult() {
        return FrontendResponse.ok(emptyListResult());
    }

    private String extractToken(String authorization) {
        if (authorization == null) {
            return "";
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return authorization;
    }

    private UserContext userContext(String authorization) {
        String token = extractToken(authorization);
        if (token == null || token.isEmpty()) {
            return new UserContext(DEV_USER_ID, DEV_ORG_ID);
        }
        return new UserContext(DEV_USER_ID, DEV_ORG_ID);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, Object> emptyListResult() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", Collections.emptyList());
        result.put("total", 0);
        return result;
    }

    private ResponseEntity<String> streamAssistantConversation(String authorization,
                                                               ConversationStreamRequest request,
                                                               boolean draft) {
        try {
            UserContext userContext = userContext(authorization);
            AssistantConversationStreamCommand command = request == null
                    ? new AssistantConversationStreamCommand()
                    : request.toCommand(draft);
            command.setUserId(userContext.getUserId());
            command.setOrgId(userContext.getOrgId());
            AssistantConversationStreamResult result = appService.streamAssistantConversation(command);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(toSseFrame(result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson(ex.getMessage()));
        }
    }

    private Map<String, Object> resolveRecommendAssistant(UserContext userContext,
                                                          QuestionRecommendRequest request) {
        if (request.isTrial()) {
            return appService.getAssistantDraft(
                    new AssistantDetailQuery(request.getAssistantId(), userContext.getUserId(), userContext.getOrgId()));
        }
        return appService.getPublishedAssistant(
                new AssistantPublishedQuery(request.getAssistantId(), null, userContext.getUserId(), userContext.getOrgId()));
    }

    private String toRecommendSseFrames(QuestionRecommendRequest request, Map<String, Object> assistantInfo) {
        String id = "recommend-" + compactText(request.getAssistantId(), 48);
        String content = recommendContent(request.getQuery(), mapString(assistantInfo, "name"));
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

    private FrontendResponse<Map<String, Object>> deleteAssistant(UserContext userContext, String assistantId) {
        AssistantDeleteCommand command = new AssistantDeleteCommand();
        command.setAssistantId(assistantId);
        command.setUserId(userContext.getUserId());
        command.setOrgId(userContext.getOrgId());
        appService.deleteAssistant(command);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    public static class AssistantCreateRequest {
        private String name;
        private String desc;
        private int category;
        private AvatarRequest avatar = new AvatarRequest();

        public AssistantCreateCommand toCommand() {
            AssistantCreateCommand command = new AssistantCreateCommand();
            command.setName(name);
            command.setDesc(desc);
            command.setCategory(category);
            if (avatar != null) {
                command.setAvatarKey(avatar.getKey());
                command.setAvatarPath(avatar.getPath());
            }
            return command;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getCategory() {
            return category;
        }

        public void setCategory(int category) {
            this.category = category;
        }

        public AvatarRequest getAvatar() {
            return avatar;
        }

        public void setAvatar(AvatarRequest avatar) {
            this.avatar = avatar;
        }
    }

    public static class AssistantUpdateRequest extends AssistantCreateRequest {
        private String assistantId;

        public AssistantUpdateCommand toUpdateCommand() {
            AssistantUpdateCommand command = new AssistantUpdateCommand();
            command.setAssistantId(assistantId);
            command.setName(getName());
            command.setDesc(getDesc());
            command.setCategory(getCategory());
            if (getAvatar() != null) {
                command.setAvatarKey(getAvatar().getKey());
                command.setAvatarPath(getAvatar().getPath());
            }
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }
    }

    public static class AssistantConfigRequest {
        private String assistantId;
        private String prologue;
        private String instructions;
        private Map<String, Object> memoryConfig;
        private Map<String, Object> knowledgeBaseConfig;
        private Map<String, Object> modelConfig;
        private Map<String, Object> safetyConfig;
        private Map<String, Object> visionConfig;
        private Map<String, Object> rerankConfig;
        private Map<String, Object> recommendConfig;
        private List<String> recommendQuestion;

        public AssistantConfigUpdateCommand toCommand() {
            AssistantConfigUpdateCommand command = new AssistantConfigUpdateCommand();
            command.setAssistantId(assistantId);
            command.setPrologue(prologue);
            command.setInstructions(instructions);
            command.setMemoryConfig(memoryConfig);
            command.setKnowledgeBaseConfig(knowledgeBaseConfig);
            command.setModelConfig(modelConfig);
            command.setSafetyConfig(safetyConfig);
            command.setVisionConfig(visionConfig);
            command.setRerankConfig(rerankConfig);
            command.setRecommendConfig(recommendConfig);
            command.setRecommendQuestion(recommendQuestion);
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

        public String getPrologue() {
            return prologue;
        }

        public void setPrologue(String prologue) {
            this.prologue = prologue;
        }

        public String getInstructions() {
            return instructions;
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }

        public Map<String, Object> getMemoryConfig() {
            return memoryConfig;
        }

        public void setMemoryConfig(Map<String, Object> memoryConfig) {
            this.memoryConfig = memoryConfig;
        }

        public Map<String, Object> getKnowledgeBaseConfig() {
            return knowledgeBaseConfig;
        }

        public void setKnowledgeBaseConfig(Map<String, Object> knowledgeBaseConfig) {
            this.knowledgeBaseConfig = knowledgeBaseConfig;
        }

        public Map<String, Object> getModelConfig() {
            return modelConfig;
        }

        public void setModelConfig(Map<String, Object> modelConfig) {
            this.modelConfig = modelConfig;
        }

        public Map<String, Object> getSafetyConfig() {
            return safetyConfig;
        }

        public void setSafetyConfig(Map<String, Object> safetyConfig) {
            this.safetyConfig = safetyConfig;
        }

        public Map<String, Object> getVisionConfig() {
            return visionConfig;
        }

        public void setVisionConfig(Map<String, Object> visionConfig) {
            this.visionConfig = visionConfig;
        }

        public Map<String, Object> getRerankConfig() {
            return rerankConfig;
        }

        public void setRerankConfig(Map<String, Object> rerankConfig) {
            this.rerankConfig = rerankConfig;
        }

        public Map<String, Object> getRecommendConfig() {
            return recommendConfig;
        }

        public void setRecommendConfig(Map<String, Object> recommendConfig) {
            this.recommendConfig = recommendConfig;
        }

        public List<String> getRecommendQuestion() {
            return recommendQuestion;
        }

        public void setRecommendQuestion(List<String> recommendQuestion) {
            this.recommendQuestion = recommendQuestion;
        }
    }

    public static class ConversationCreateRequest {
        private String assistantId;
        private String prompt;

        public AssistantConversationCreateCommand toCommand(String conversationType) {
            AssistantConversationCreateCommand command = new AssistantConversationCreateCommand();
            command.setAssistantId(assistantId);
            command.setPrompt(prompt);
            command.setConversationType(conversationType);
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    public static class ConversationDeleteRequest {
        private String assistantId;
        private String conversationId;
        private String detailId;

        public AssistantConversationDeleteCommand toCommand() {
            AssistantConversationDeleteCommand command = new AssistantConversationDeleteCommand();
            command.setAssistantId(assistantId);
            command.setConversationId(conversationId);
            command.setDetailId(detailId);
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

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

    public static class ConversationStreamRequest {
        private String assistantId;
        private String conversationId;
        private String prompt;
        private String systemPrompt;
        private List<Map<String, Object>> fileInfo;

        public AssistantConversationStreamCommand toCommand(boolean draft) {
            AssistantConversationStreamCommand command = new AssistantConversationStreamCommand();
            command.setAssistantId(assistantId);
            command.setConversationId(conversationId);
            command.setPrompt(prompt);
            command.setSystemPrompt(systemPrompt);
            command.setFileInfo(fileInfo == null ? Collections.<Map<String, Object>>emptyList() : fileInfo);
            command.setDraft(draft);
            return command;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

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

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }

        public List<Map<String, Object>> getFileInfo() {
            return fileInfo;
        }

        public void setFileInfo(List<Map<String, Object>> fileInfo) {
            this.fileInfo = fileInfo;
        }
    }

    public static class QuestionRecommendRequest {
        private String query;
        private String assistantId;
        private String conversationId;
        private boolean trial;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public boolean isTrial() {
            return trial;
        }

        public void setTrial(boolean trial) {
            this.trial = trial;
        }
    }

    public static class AppspaceAppDeleteRequest {
        private String appId;
        private String appType;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }
    }

    public static class AssistantDeleteRequest {
        private String assistantId;

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }
    }

    public static class AssistantCopyRequest {
        private String assistantId;

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }
    }

    public static class AppPublishRequest {
        private String appId;
        private String appType;
        private String version;
        private String desc;
        private String publishType;

        public AppPublishCommand toCommand() {
            AppPublishCommand command = new AppPublishCommand();
            command.setAppId(appId);
            command.setAppType(appType);
            command.setVersion(version);
            command.setDesc(desc);
            command.setPublishType(publishType);
            return command;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getPublishType() {
            return publishType;
        }

        public void setPublishType(String publishType) {
            this.publishType = publishType;
        }
    }

    public static class AppVersionUpdateRequest {
        private String appId;
        private String appType;
        private String desc;
        private String publishType;

        public AppVersionUpdateCommand toCommand() {
            AppVersionUpdateCommand command = new AppVersionUpdateCommand();
            command.setAppId(appId);
            command.setAppType(appType);
            command.setDesc(desc);
            command.setPublishType(publishType);
            return command;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getPublishType() {
            return publishType;
        }

        public void setPublishType(String publishType) {
            this.publishType = publishType;
        }
    }

    public static class AppVersionRollbackRequest {
        private String appId;
        private String appType;
        private String version;

        public AppVersionRollbackCommand toCommand() {
            AppVersionRollbackCommand command = new AppVersionRollbackCommand();
            command.setAppId(appId);
            command.setAppType(appType);
            command.setVersion(version);
            return command;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppType() {
            return appType;
        }

        public void setAppType(String appType) {
            this.appType = appType;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class AvatarRequest {
        private String key;
        private String path;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    private static class UserContext {
        private final String userId;
        private final String orgId;

        private UserContext(String userId, String orgId) {
            this.userId = userId;
            this.orgId = orgId;
        }

        private String getUserId() {
            return userId;
        }

        private String getOrgId() {
            return orgId;
        }
    }
}
