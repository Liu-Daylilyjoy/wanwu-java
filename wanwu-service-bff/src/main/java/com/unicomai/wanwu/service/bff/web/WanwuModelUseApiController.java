package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileUploadCommand;
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileUploadItem;
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogDeleteCommand;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogSaveCommand;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class WanwuModelUseApiController {

    private static final String MODEL_PREFIX = "/use/model/api/v1";
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";
    private static final String AGENT_APP_TYPE = "agent";
    private static final AtomicLong ACTION_SEQUENCE = new AtomicLong(0);
    private static final Map<String, Map<String, Object>> ACTIONS =
            new ConcurrentHashMap<String, Map<String, Object>>();

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    public WanwuModelUseApiController() {
    }

    public WanwuModelUseApiController(AppService appService, ModelService modelService) {
        this.appService = appService;
        this.modelService = modelService;
    }

    @PostMapping(MODEL_PREFIX + "/assistant/create")
    public FrontendResponse<AssistantCreateResult> createAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(appService.createAssistant(toCreateCommand(userContext(authorization), request)));
    }

    @PutMapping(MODEL_PREFIX + "/assistant/update")
    public FrontendResponse<Map<String, Object>> updateAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        appService.updateAssistant(toUpdateCommand(userContext(authorization), request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping(MODEL_PREFIX + "/assistant/info")
    public FrontendResponse<Map<String, Object>> assistantInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "assistantId", required = false) String assistantId) {
        UserContext ctx = userContext(authorization);
        return FrontendResponse.ok(appService.getAssistantDraft(
                new AssistantDetailQuery(defaultIfBlank(assistantId, ""), ctx.userId, ctx.orgId)));
    }

    @DeleteMapping(MODEL_PREFIX + "/assistant/delete")
    public FrontendResponse<Map<String, Object>> deleteAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        AssistantDeleteCommand command = new AssistantDeleteCommand();
        command.setAssistantId(firstText(request, "assistantId", "appId"));
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        appService.deleteAssistant(command);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping(MODEL_PREFIX + "/assistant/publish")
    public FrontendResponse<Map<String, Object>> publishAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        AppPublishCommand command = new AppPublishCommand();
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setAppType(AGENT_APP_TYPE);
        command.setAppId(firstText(request, "assistantId", "appId"));
        command.setVersion(defaultIfBlank(firstText(request, "version"), "v1"));
        command.setDesc(defaultIfBlank(firstText(request, "desc", "description"), "Published from legacy MODEL_API"));
        command.setPublishType(defaultIfBlank(firstText(request, "publishType"), "private"));
        appService.publishApp(command);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping({MODEL_PREFIX + "/assistant/list", MODEL_PREFIX + "/assistant/draft_list",
            MODEL_PREFIX + "/assistant/more_list", MODEL_PREFIX + "/assistant/common/list"})
    public FrontendResponse<Object> listAssistants(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        UserContext ctx = userContext(authorization);
        return FrontendResponse.ok(appService.listAssistants(
                new ApplicationListQuery(AGENT_APP_TYPE, defaultIfBlank(name, ""), ctx.userId, ctx.orgId)));
    }

    @DeleteMapping(MODEL_PREFIX + "/assistant/common/delete")
    public FrontendResponse<Map<String, Object>> deleteRecentAssistant() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping(MODEL_PREFIX + "/assistant/conversation/create")
    public FrontendResponse<Object> createAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        AssistantConversationCreateCommand command = new AssistantConversationCreateCommand();
        command.setAssistantId(firstText(request, "assistantId", "appId"));
        command.setPrompt(defaultIfBlank(firstText(request, "prompt", "question", "content"), ""));
        command.setConversationType("published");
        return FrontendResponse.ok(appService.createAssistantConversation(command));
    }

    @DeleteMapping(MODEL_PREFIX + "/assistant/conversation/delete")
    public FrontendResponse<Map<String, Object>> deleteAssistantConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        AssistantConversationDeleteCommand command = new AssistantConversationDeleteCommand();
        command.setAssistantId(firstText(request, "assistantId", "appId"));
        command.setConversationId(firstText(request, "conversationId"));
        command.setDetailId(firstText(request, "detailId"));
        appService.deleteAssistantConversation(command);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @DeleteMapping(MODEL_PREFIX + "/assistant/conversation/detail/delete")
    public FrontendResponse<Map<String, Object>> deleteAssistantConversationDetail(
            @RequestBody(required = false) Map<String, Object> request) {
        AssistantConversationDeleteCommand command = new AssistantConversationDeleteCommand();
        command.setAssistantId(firstText(request, "assistantId", "appId"));
        command.setConversationId(firstText(request, "conversationId"));
        command.setDetailId(firstText(request, "detailId"));
        appService.deleteAssistantConversation(command);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping(MODEL_PREFIX + "/assistant/conversation/list")
    public FrontendResponse<Object> listAssistantConversations(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "assistantId", required = false) String assistantId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        UserContext ctx = userContext(authorization);
        AssistantConversationListQuery query = new AssistantConversationListQuery();
        query.setAssistantId(defaultIfBlank(assistantId, ""));
        query.setConversationType("published");
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setUserId(ctx.userId);
        query.setOrgId(ctx.orgId);
        return FrontendResponse.ok(appService.listAssistantConversations(query));
    }

    @GetMapping(MODEL_PREFIX + "/assistant/conversation/detail")
    public FrontendResponse<Object> listAssistantConversationDetails(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "assistantId", required = false) String assistantId,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        UserContext ctx = userContext(authorization);
        AssistantConversationDetailQuery query = new AssistantConversationDetailQuery();
        query.setConversationId(defaultIfBlank(conversationId, ""));
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setUserId(ctx.userId);
        query.setOrgId(ctx.orgId);
        return FrontendResponse.ok(appService.listAssistantConversationDetails(query));
    }

    @PostMapping(MODEL_PREFIX + "/assistant/knowledge/file/upload")
    public FrontendResponse<Object> uploadAssistantKnowledgeFiles(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "assistantId", required = false) String assistantId,
            @RequestParam(value = "appId", required = false) String appId,
            @RequestParam(value = "file", required = false) List<MultipartFile> file,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        UserContext ctx = userContext(authorization);
        AssistantKnowledgeFileUploadCommand command = new AssistantKnowledgeFileUploadCommand();
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setAssistantId(defaultIfBlank(assistantId, defaultIfBlank(appId, "default-assistant")));
        command.setFiles(toKnowledgeFileItems(firstFiles(file, files)));
        return FrontendResponse.ok(appService.uploadAssistantKnowledgeFiles(command));
    }

    @GetMapping(MODEL_PREFIX + "/assistant/knowledge/file/list")
    public FrontendResponse<Object> listAssistantKnowledgeFiles(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "assistantId", required = false) String assistantId,
            @RequestParam(value = "appId", required = false) String appId) {
        UserContext ctx = userContext(authorization);
        AssistantKnowledgeFileListQuery query = new AssistantKnowledgeFileListQuery();
        query.setUserId(ctx.userId);
        query.setOrgId(ctx.orgId);
        query.setAssistantId(defaultIfBlank(assistantId, defaultIfBlank(appId, "default-assistant")));
        return FrontendResponse.ok(appService.listAssistantKnowledgeFiles(query));
    }

    @DeleteMapping(MODEL_PREFIX + "/assistant/knowledge/file/delete")
    public FrontendResponse<Map<String, Object>> deleteAssistantKnowledgeFile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        AssistantKnowledgeFileDeleteCommand command = new AssistantKnowledgeFileDeleteCommand();
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setAssistantId(firstText(request, "assistantId", "appId"));
        command.setFileId(fileIdValue(request == null ? null : request.get("fileId")));
        appService.deleteAssistantKnowledgeFile(command);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping(MODEL_PREFIX + "/assistant/app/publish")
    public FrontendResponse<Map<String, Object>> linkAssistantApp() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping(MODEL_PREFIX + "/assistant/recommend/list")
    public FrontendResponse<Map<String, Object>> listRecommendedAssistants() {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("list", Collections.emptyList());
        body.put("total", 0);
        return FrontendResponse.ok(body);
    }

    @PutMapping(MODEL_PREFIX + "/assistant/recommend/update")
    public FrontendResponse<Map<String, Object>> updateRecommendedAssistant() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping(MODEL_PREFIX + "/assistant/action/create")
    public FrontendResponse<Map<String, Object>> createAction(@RequestBody(required = false) Map<String, Object> request) {
        String id = "action-" + ACTION_SEQUENCE.incrementAndGet();
        Map<String, Object> action = copy(request);
        action.put("actionId", id);
        ACTIONS.put(id, action);
        return FrontendResponse.ok(action);
    }

    @PutMapping(MODEL_PREFIX + "/assistant/action/update")
    public FrontendResponse<Map<String, Object>> updateAction(@RequestBody(required = false) Map<String, Object> request) {
        String id = defaultIfBlank(firstText(request, "actionId", "id"), "action-" + ACTION_SEQUENCE.incrementAndGet());
        Map<String, Object> action = copy(request);
        action.put("actionId", id);
        ACTIONS.put(id, action);
        return FrontendResponse.ok(action);
    }

    @DeleteMapping(MODEL_PREFIX + "/assistant/action/delete")
    public FrontendResponse<Map<String, Object>> deleteAction(@RequestBody(required = false) Map<String, Object> request) {
        ACTIONS.remove(firstText(request, "actionId", "id"));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping(MODEL_PREFIX + "/assistant/action/info")
    public FrontendResponse<Map<String, Object>> actionInfo(@RequestParam(value = "actionId", required = false) String actionId) {
        Map<String, Object> action = ACTIONS.get(defaultIfBlank(actionId, ""));
        if (action == null) {
            action = new LinkedHashMap<String, Object>();
            action.put("actionId", defaultIfBlank(actionId, "action-local"));
            action.put("name", "Local Action");
            action.put("schema", Collections.emptyMap());
        }
        return FrontendResponse.ok(action);
    }

    @PostMapping(MODEL_PREFIX + "/assistant/auto/create")
    public FrontendResponse<Map<String, Object>> autoCreateAssistant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> bodyRequest = copy(request);
        String prompt = firstText(request, "prompt", "question", "content");
        if (!bodyRequest.containsKey("name")) {
            bodyRequest.put("name", defaultIfBlank(prompt, "Auto Assistant"));
        }
        if (!bodyRequest.containsKey("desc") && !bodyRequest.containsKey("description")) {
            bodyRequest.put("desc", defaultIfBlank(prompt, "Generated from legacy MODEL_API auto-create"));
        }
        AssistantCreateResult created = appService.createAssistant(toCreateCommand(userContext(authorization), bodyRequest));
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("assistantId", created.getAssistantId());
        body.put("name", defaultIfBlank(firstText(bodyRequest, "name", "assistantName"), "Auto Assistant"));
        body.put("status", "created");
        return FrontendResponse.ok(body);
    }

    @PostMapping(MODEL_PREFIX + "/chatllm/conversation/create")
    public FrontendResponse<Map<String, Object>> createChatLlmConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        AssistantConversationCreateCommand command = new AssistantConversationCreateCommand();
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setPrompt(defaultIfBlank(firstText(request, "prompt", "question", "content", "name"), "New Chat"));
        return FrontendResponse.ok(appService.createLegacyChatLlmConversation(command));
    }

    @GetMapping(MODEL_PREFIX + "/chatllm/conversation/list")
    public FrontendResponse<Object> listChatLlmConversations(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        UserContext ctx = userContext(authorization);
        AssistantConversationListQuery query = new AssistantConversationListQuery();
        query.setUserId(ctx.userId);
        query.setOrgId(ctx.orgId);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        return FrontendResponse.ok(appService.listLegacyChatLlmConversations(query));
    }

    @GetMapping(MODEL_PREFIX + "/chatllm/conversation/detail")
    public FrontendResponse<Object> chatLlmConversationDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        UserContext ctx = userContext(authorization);
        AssistantConversationDetailQuery query = new AssistantConversationDetailQuery();
        query.setUserId(ctx.userId);
        query.setOrgId(ctx.orgId);
        query.setConversationId(defaultIfBlank(conversationId, ""));
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        return FrontendResponse.ok(appService.listLegacyChatLlmConversationDetails(query));
    }

    @DeleteMapping(MODEL_PREFIX + "/chatllm/conversation/delete")
    public FrontendResponse<Map<String, Object>> deleteChatLlmConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        AssistantConversationDeleteCommand command = new AssistantConversationDeleteCommand();
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setConversationId(firstText(request, "conversationId"));
        appService.deleteLegacyChatLlmConversation(command);
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping(MODEL_PREFIX + "/model/experience/dialog")
    public FrontendResponse<Object> saveModelExperienceDialog(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        ModelExperienceDialogSaveCommand command = new ModelExperienceDialogSaveCommand();
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setModelId(defaultIfBlank(firstText(request, "modelId", "model"), "local-model"));
        command.setSessionId(defaultIfBlank(firstText(request, "sessionId"), UUID.randomUUID().toString()));
        command.setTitle(defaultIfBlank(firstText(request, "title", "name"), "New Chat"));
        command.setModelSetting(defaultIfBlank(firstText(request, "modelSetting", "modelConfig"), ""));
        return FrontendResponse.ok(modelService.saveModelExperienceDialog(command));
    }

    @GetMapping(MODEL_PREFIX + "/model/experience/dialogs")
    public FrontendResponse<Object> listModelExperienceDialogs(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserContext ctx = userContext(authorization);
        return FrontendResponse.ok(modelService.listModelExperienceDialogs(
                new ModelExperienceDialogListQuery(ctx.userId, ctx.orgId)));
    }

    @DeleteMapping(MODEL_PREFIX + "/model/experience/dialog")
    public FrontendResponse<Map<String, Object>> deleteModelExperienceDialog(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        modelService.deleteModelExperienceDialog(new ModelExperienceDialogDeleteCommand(
                ctx.userId, ctx.orgId, firstText(request, "modelExperienceId", "id")));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping(MODEL_PREFIX + "/model/experience/dialog/records")
    public FrontendResponse<Object> listModelExperienceDialogRecords(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "modelExperienceId", required = false) String modelExperienceId,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        UserContext ctx = userContext(authorization);
        return FrontendResponse.ok(modelService.listModelExperienceDialogRecords(
                new ModelExperienceDialogRecordQuery(ctx.userId, ctx.orgId,
                        defaultIfBlank(modelExperienceId, ""), defaultIfBlank(sessionId, ""))));
    }

    @PostMapping(MODEL_PREFIX + "/model/experience/file/extract")
    public FrontendResponse<Map<String, Object>> extractModelExperienceFile(
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("fileName", defaultIfBlank(firstText(request, "fileName", "name"), "file.txt"));
        body.put("content", defaultIfBlank(firstText(request, "content", "text"), ""));
        body.put("status", "extracted");
        return FrontendResponse.ok(body);
    }

    @PostMapping(MODEL_PREFIX + "/file/confirmPath")
    public FrontendResponse<Map<String, Object>> confirmPath(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("confirmed", true);
        body.put("fileName", defaultIfBlank(firstText(request, "fileName", "name"), "file"));
        body.put("path", "/tmp/wanwu-java/" + body.get("fileName"));
        return FrontendResponse.ok(body);
    }

    @PostMapping(value = {MODEL_PREFIX + "/file/batch/upload",
            "/service/api/v1/model/expansion/file/batch/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FrontendResponse<Map<String, Object>> batchUpload(
            @RequestParam(value = "file", required = false) List<MultipartFile> file,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return FrontendResponse.ok(fileUploadResult(firstFiles(file, files)));
    }

    private AssistantCreateCommand toCreateCommand(UserContext ctx, Map<String, Object> request) {
        AssistantCreateCommand command = new AssistantCreateCommand();
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setName(defaultIfBlank(firstText(request, "name", "assistantName"), "Legacy Assistant"));
        command.setDesc(defaultIfBlank(firstText(request, "desc", "description"), ""));
        command.setCategory(intValue(request, "category", 0));
        command.setAvatarKey(firstText(nested(request, "avatar"), "key"));
        command.setAvatarPath(firstText(nested(request, "avatar"), "path"));
        return command;
    }

    private AssistantUpdateCommand toUpdateCommand(UserContext ctx, Map<String, Object> request) {
        AssistantUpdateCommand command = new AssistantUpdateCommand();
        command.setAssistantId(firstText(request, "assistantId", "appId"));
        command.setUserId(ctx.userId);
        command.setOrgId(ctx.orgId);
        command.setName(defaultIfBlank(firstText(request, "name", "assistantName"), "Legacy Assistant"));
        command.setDesc(defaultIfBlank(firstText(request, "desc", "description"), ""));
        command.setCategory(intValue(request, "category", 0));
        command.setAvatarKey(firstText(nested(request, "avatar"), "key"));
        command.setAvatarPath(firstText(nested(request, "avatar"), "path"));
        return command;
    }

    private Map<String, Object> fileUploadResult(List<MultipartFile> files) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                String fileName = defaultIfBlank(file.getOriginalFilename(), "file");
                row.put("fileId", "model-use-file-" + UUID.randomUUID().toString().replace("-", ""));
                row.put("fileName", fileName);
                row.put("file_name", fileName);
                row.put("size", file.getSize());
                row.put("url", "/service/api/v1/file/download/" + row.get("fileId"));
                rows.add(row);
            }
        }
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("list", rows);
        body.put("fileList", rows);
        body.put("total", rows.size());
        return body;
    }

    private String fileIdValue(Object raw) {
        if (raw instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) raw;
            Object value = map.get("fileId");
            if (value == null) {
                value = map.get("id");
            }
            return value == null ? "" : String.valueOf(value);
        }
        return raw == null ? "" : String.valueOf(raw);
    }

    private List<AssistantKnowledgeFileUploadItem> toKnowledgeFileItems(List<MultipartFile> files) {
        List<AssistantKnowledgeFileUploadItem> items = new ArrayList<AssistantKnowledgeFileUploadItem>();
        if (files == null) {
            return items;
        }
        for (MultipartFile file : files) {
            if (file == null) {
                continue;
            }
            items.add(new AssistantKnowledgeFileUploadItem(
                    defaultIfBlank(file.getOriginalFilename(), "knowledge-file"),
                    file.getSize(),
                    defaultIfBlank(file.getContentType(), "")));
        }
        return items;
    }

    private List<MultipartFile> firstFiles(List<MultipartFile> first, List<MultipartFile> second) {
        if (first != null && !first.isEmpty()) {
            return first;
        }
        if (second != null && !second.isEmpty()) {
            return second;
        }
        return Collections.emptyList();
    }

    private UserContext userContext(String authorization) {
        String token = authorization == null ? "" : authorization.replace("Bearer", "").trim();
        if ("dev-token-app".equals(token)) {
            return new UserContext(DEV_APP_USER_ID, DEV_ORG_ID);
        }
        return new UserContext(DEV_USER_ID, DEV_ORG_ID);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nested(Map<String, Object> request, String key) {
        if (request == null) {
            return Collections.emptyMap();
        }
        Object value = request.get(key);
        return value instanceof Map ? (Map<String, Object>) value : Collections.<String, Object>emptyMap();
    }

    private Map<String, Object> copy(Map<String, Object> request) {
        return request == null
                ? new LinkedHashMap<String, Object>()
                : new LinkedHashMap<String, Object>(request);
    }

    private String firstText(Map<String, Object> request, String... keys) {
        if (request == null) {
            return "";
        }
        for (String key : keys) {
            Object value = request.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private int intValue(Map<String, Object> request, String key, int fallback) {
        String value = firstText(request, key);
        if (value.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
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
