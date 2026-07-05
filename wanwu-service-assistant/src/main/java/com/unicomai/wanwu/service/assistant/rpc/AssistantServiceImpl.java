package com.unicomai.wanwu.service.assistant.rpc;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.AppVersionRollbackCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationPageResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantCopyCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantPublishedQuery;
import com.unicomai.wanwu.api.app.dto.AssistantResourceCommand;
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.assistant.AssistantService;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class AssistantServiceImpl implements AssistantService {

    private static final String APP_TYPE_AGENT = "agent";
    private static final String PUBLISH_TYPE_PRIVATE = "private";
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    private final ConcurrentMap<String, List<String>> esDocuments = new ConcurrentHashMap<String, List<String>>();

    public AssistantServiceImpl() {
    }

    AssistantServiceImpl(AppService appService) {
        this.appService = appService;
    }

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.ASSISTANT, "Assistant Service", "assistant");
    }

    @Override
    public void saveToES(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        String indexName = defaultIfBlank(stringValue(first(safeRequest, "index_name", "indexName")), "assistant");
        String docJson = defaultIfBlank(stringValue(first(safeRequest, "doc_json", "docJson")), "{}");
        esList(indexName).add(docJson);
    }

    @Override
    public void deleteFromES(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        String indexName = defaultIfBlank(stringValue(first(safeRequest, "index_name", "indexName")), "assistant");
        Map<String, Object> conditions = mapValue(first(safeRequest, "conditions"));
        List<String> docs = esDocuments.get(indexName);
        if (docs == null) {
            return;
        }
        synchronized (docs) {
            if (conditions.isEmpty()) {
                docs.clear();
                return;
            }
            List<String> kept = new ArrayList<String>();
            for (String doc : docs) {
                if (!matches(doc, conditions)) {
                    kept.add(doc);
                }
            }
            docs.clear();
            docs.addAll(kept);
        }
    }

    @Override
    public Map<String, Object> searchFromES(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        String indexName = defaultIfBlank(stringValue(first(safeRequest, "index_name", "indexName")), "assistant");
        Map<String, Object> conditions = mapValue(first(safeRequest, "conditions"));
        int pageNo = positiveInt(first(safeRequest, "page_no", "pageNo"), 1);
        int pageSize = positiveInt(first(safeRequest, "page_size", "pageSize"), 20);
        List<String> docs = esDocuments.get(indexName);
        List<String> matched = new ArrayList<String>();
        if (docs != null) {
            synchronized (docs) {
                for (String doc : docs) {
                    if (matches(doc, conditions)) {
                        matched.add(doc);
                    }
                }
            }
        }
        int from = Math.min((pageNo - 1) * pageSize, matched.size());
        int to = Math.min(from + pageSize, matched.size());
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("doc_json_list", new ArrayList<String>(matched.subList(from, to)));
        response.put("docJsonList", new ArrayList<String>(matched.subList(from, to)));
        response.put("total", (long) matched.size());
        return response;
    }

    @Override
    public Map<String, Object> getAssistantByIds(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        List<String> ids = stringList(first(safeRequest, "assistantIdList", "assistantIds"));
        List<Map<String, Object>> all = listAssistantItems(safeRequest, "");
        List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : all) {
            if (ids.contains(assistantId(item))) {
                filtered.add(item);
            }
        }
        return assistantListResponse(filtered, filtered.size());
    }

    @Override
    public Map<String, Object> assistantCreate(Map<String, Object> request) {
        AssistantCreateResult result = appService().createAssistant(createCommand(safe(request)));
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("assistantId", result == null ? "" : result.getAssistantId());
        return response;
    }

    @Override
    public void assistantUpdate(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        Map<String, Object> brief = brief(safeRequest);
        AssistantUpdateCommand command = new AssistantUpdateCommand();
        command.setAssistantId(assistantId(safeRequest));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        command.setName(defaultIfBlank(stringValue(first(brief, "name", "title")), stringValue(first(safeRequest, "name", "title"))));
        command.setDesc(defaultIfBlank(stringValue(first(brief, "desc", "description")), stringValue(first(safeRequest, "desc", "description"))));
        command.setCategory(intValue(first(safeRequest, "category"), intValue(first(brief, "category"), 1)));
        command.setAvatarKey(avatarValue(brief, safeRequest, "key", "avatarKey"));
        command.setAvatarPath(avatarValue(brief, safeRequest, "path", "avatarPath", "avatarUrl"));
        appService().updateAssistant(command);
    }

    @Override
    public void assistantConfigUpdate(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AssistantConfigUpdateCommand command = new AssistantConfigUpdateCommand();
        command.setAssistantId(assistantId(safeRequest));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        command.setInstructions(defaultIfBlank(stringValue(first(safeRequest, "instructions")), ""));
        command.setPrologue(defaultIfBlank(stringValue(first(safeRequest, "prologue")), ""));
        command.setRecommendQuestion(stringList(first(safeRequest, "recommendQuestion", "recommendQuestions")));
        command.setModelConfig(mapValue(first(safeRequest, "modelConfig")));
        command.setKnowledgeBaseConfig(mapValue(first(safeRequest, "knowledgeBaseConfig")));
        command.setRerankConfig(mapValue(first(safeRequest, "rerankConfig")));
        command.setSafetyConfig(mapValue(first(safeRequest, "safetyConfig")));
        command.setVisionConfig(mapValue(first(safeRequest, "visionConfig")));
        command.setMemoryConfig(mapValue(first(safeRequest, "memoryConfig")));
        command.setRecommendConfig(mapValue(first(safeRequest, "recommendConfig")));
        appService().updateAssistantConfig(command);
    }

    @Override
    public void assistantDelete(Map<String, Object> request) {
        AssistantDeleteCommand command = new AssistantDeleteCommand();
        command.setAssistantId(assistantId(safe(request)));
        command.setUserId(userId(safe(request)));
        command.setOrgId(orgId(safe(request)));
        appService().deleteAssistant(command);
    }

    @Override
    public Map<String, Object> getAssistantListMyAll(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        ApplicationListResult result = appService().listAssistants(
                new ApplicationListQuery(APP_TYPE_AGENT, defaultIfBlank(stringValue(first(safeRequest, "name")), ""),
                        userId(safeRequest), orgId(safeRequest)));
        List<Map<String, Object>> list = result == null || result.getList() == null
                ? Collections.<Map<String, Object>>emptyList()
                : result.getList();
        return assistantListResponse(list, result == null ? 0L : result.getTotal());
    }

    @Override
    public Map<String, Object> getAssistantInfo(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        Map<String, Object> detail = appService().getAssistantDraft(
                new AssistantDetailQuery(assistantId(safeRequest), userId(safeRequest), orgId(safeRequest)));
        return assistantInfo(detail, assistantId(safeRequest), userId(safeRequest), orgId(safeRequest));
    }

    @Override
    public Map<String, Object> getAssistantIdByUuid(Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("assistantId", defaultIfBlank(stringValue(first(safe(request), "uuid", "assistantId")), ""));
        return response;
    }

    @Override
    public Map<String, Object> assistantCopy(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AssistantCopyCommand command = new AssistantCopyCommand();
        command.setAssistantId(assistantId(safeRequest));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        AssistantCreateResult result = appService().copyAssistant(command);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("assistantId", result == null ? "" : result.getAssistantId());
        return response;
    }

    @Override
    public Map<String, Object> getAssistantDetailById(Map<String, Object> request) {
        Map<String, Object> detail = assistantDetail(safe(request));
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("agentDetail", detail);
        return response;
    }

    @Override
    public Map<String, Object> getMultiAssistantById(Map<String, Object> request) {
        Map<String, Object> detail = assistantDetail(safe(request));
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("multiAgent", detail);
        response.put("subAgents", Collections.emptyList());
        return response;
    }

    @Override
    public Map<String, Object> assistantSnapshotCreate(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AppPublishCommand command = publishCommand(safeRequest);
        appService().publishApp(command);
        AppVersionInfo latest = appService().getLatestAppVersion(versionQuery(safeRequest));
        return snapshotInfo(command.getAppId(), latest);
    }

    @Override
    public void assistantSnapshotUpdate(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AppVersionUpdateCommand command = new AppVersionUpdateCommand();
        command.setAppId(assistantId(safeRequest));
        command.setAppType(APP_TYPE_AGENT);
        command.setDesc(defaultIfBlank(stringValue(first(safeRequest, "desc", "description")), ""));
        command.setPublishType(defaultIfBlank(stringValue(first(safeRequest, "publishType")), PUBLISH_TYPE_PRIVATE));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        appService().updateAppVersion(command);
    }

    @Override
    public Map<String, Object> assistantSnapshotList(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AppVersionListResult result = appService().listAppVersions(versionQuery(safeRequest));
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (result != null && result.getList() != null) {
            for (AppVersionInfo version : result.getList()) {
                list.add(snapshotInfo(assistantId(safeRequest), version));
            }
        }
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("list", list);
        response.put("total", result == null ? 0L : (long) result.getTotal());
        return response;
    }

    @Override
    public void assistantSnapshotRollback(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AppVersionRollbackCommand command = new AppVersionRollbackCommand();
        command.setAppId(assistantId(safeRequest));
        command.setAppType(APP_TYPE_AGENT);
        command.setVersion(defaultIfBlank(stringValue(first(safeRequest, "version")), ""));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        appService().rollbackAppVersion(command);
    }

    @Override
    public Map<String, Object> assistantSnapshotInfo(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        Map<String, Object> detail = appService().getPublishedAssistant(new AssistantPublishedQuery(
                assistantId(safeRequest),
                defaultIfBlank(stringValue(first(safeRequest, "version")), ""),
                userId(safeRequest),
                orgId(safeRequest)));
        return assistantInfo(detail, assistantId(safeRequest), userId(safeRequest), orgId(safeRequest));
    }

    @Override
    public Map<String, Object> assistantSnapshotLatest(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        return snapshotInfo(assistantId(safeRequest), appService().getLatestAppVersion(versionQuery(safeRequest)));
    }

    @Override
    public Map<String, Object> assistantSnapshotLatestBatch(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (String id : stringList(first(safeRequest, "assistantIdList", "assistantIds"))) {
            Map<String, Object> query = new LinkedHashMap<String, Object>(safeRequest);
            query.put("assistantId", id);
            list.add(assistantSnapshotLatest(query));
        }
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("list", list);
        return response;
    }

    @Override
    public void assistantWorkFlowCreate(Map<String, Object> request) {
        appService().addAssistantWorkflow(resourceCommand(safe(request), "workflow", "workFlowId", "workflowId"));
    }

    @Override
    public void assistantWorkFlowDelete(Map<String, Object> request) {
        appService().deleteAssistantWorkflow(resourceCommand(safe(request), "workflow", "workFlowId", "workflowId"));
    }

    @Override
    public void assistantWorkFlowEnableSwitch(Map<String, Object> request) {
        appService().switchAssistantWorkflow(resourceCommand(safe(request), "workflow", "workFlowId", "workflowId"));
    }

    @Override
    public void assistantWorkFlowDeleteByWorkflowId(Map<String, Object> request) {
        if (!isBlank(assistantId(safe(request)))) {
            assistantWorkFlowDelete(request);
        }
    }

    @Override
    public void assistantMCPCreate(Map<String, Object> request) {
        appService().addAssistantMcp(resourceCommand(safe(request), "mcp", "mcpId"));
    }

    @Override
    public void assistantMCPDelete(Map<String, Object> request) {
        appService().deleteAssistantMcp(resourceCommand(safe(request), "mcp", "mcpId"));
    }

    @Override
    public void assistantMCPEnableSwitch(Map<String, Object> request) {
        appService().switchAssistantMcp(resourceCommand(safe(request), "mcp", "mcpId"));
    }

    @Override
    public Map<String, Object> assistantMCPGetList(Map<String, Object> request) {
        Map<String, Object> info = getAssistantInfo(request);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("assistantMCPInfos", mapList(info.get("mcpInfos")));
        return response;
    }

    @Override
    public void assistantMCPDeleteByMCPId(Map<String, Object> request) {
        if (!isBlank(assistantId(safe(request)))) {
            assistantMCPDelete(request);
        }
    }

    @Override
    public void assistantToolCreate(Map<String, Object> request) {
        appService().addAssistantTool(resourceCommand(safe(request), "tool", "toolId"));
    }

    @Override
    public void assistantToolDelete(Map<String, Object> request) {
        appService().deleteAssistantTool(resourceCommand(safe(request), "tool", "toolId"));
    }

    @Override
    public void assistantToolEnableSwitch(Map<String, Object> request) {
        appService().switchAssistantTool(resourceCommand(safe(request), "tool", "toolId"));
    }

    @Override
    public void assistantToolConfig(Map<String, Object> request) {
        appService().configureAssistantTool(resourceCommand(safe(request), "tool", "toolId"));
    }

    @Override
    public void assistantToolDeleteByToolId(Map<String, Object> request) {
        if (!isBlank(assistantId(safe(request)))) {
            assistantToolDelete(request);
        }
    }

    @Override
    public void assistantSkillCreate(Map<String, Object> request) {
        appService().addAssistantSkill(resourceCommand(safe(request), "skill", "skillId"));
    }

    @Override
    public void assistantSkillDelete(Map<String, Object> request) {
        appService().deleteAssistantSkill(resourceCommand(safe(request), "skill", "skillId"));
    }

    @Override
    public void assistantSkillEnableSwitch(Map<String, Object> request) {
        appService().switchAssistantSkill(resourceCommand(safe(request), "skill", "skillId"));
    }

    @Override
    public void multiAgentCreate(Map<String, Object> request) {
        appService().addAssistantAgent(resourceCommand(safe(request), "agent", "agentId"));
    }

    @Override
    public void multiAgentDelete(Map<String, Object> request) {
        appService().deleteAssistantAgent(resourceCommand(safe(request), "agent", "agentId"));
    }

    @Override
    public void multiAgentEnableSwitch(Map<String, Object> request) {
        appService().switchAssistantAgent(resourceCommand(safe(request), "agent", "agentId"));
    }

    @Override
    public void multiAgentConfigUpdate(Map<String, Object> request) {
        appService().updateAssistantAgentConfig(resourceCommand(safe(request), "agent", "agentId"));
    }

    @Override
    public Map<String, Object> conversationCreate(Map<String, Object> request) {
        AssistantConversationCreateResult result = appService().createAssistantConversation(conversationCreateCommand(safe(request)));
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("conversationId", result == null ? "" : result.getConversationId());
        return response;
    }

    @Override
    public void conversationDelete(Map<String, Object> request) {
        appService().deleteAssistantConversation(conversationDeleteCommand(safe(request)));
    }

    @Override
    public void clearConversationES(Map<String, Object> request) {
        appService().clearAssistantConversation(conversationDeleteCommand(safe(request)));
        deleteFromES(request);
    }

    @Override
    public Map<String, Object> getConversationIdByAssistantId(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AssistantConversationPageResult page = appService().listAssistantConversations(conversationListQuery(safeRequest));
        if (page != null && page.getList() != null && !page.getList().isEmpty()) {
            Map<String, Object> response = new LinkedHashMap<String, Object>();
            response.put("conversationId", defaultIfBlank(stringValue(page.getList().get(0).get("conversationId")), ""));
            return response;
        }
        return conversationCreate(safeRequest);
    }

    @Override
    public Map<String, Object> getConversationList(Map<String, Object> request) {
        return pageResponse(appService().listAssistantConversations(conversationListQuery(safe(request))));
    }

    @Override
    public Map<String, Object> getConversationDetailList(Map<String, Object> request) {
        return pageResponse(appService().listAssistantConversationDetails(conversationDetailQuery(safe(request))));
    }

    @Override
    public Map<String, Object> assistantConversionStream(Map<String, Object> request) {
        return streamResponse(appService().streamAssistantConversation(streamCommand(safe(request))));
    }

    @Override
    public Map<String, Object> multiAssistantConversionStream(Map<String, Object> request) {
        return assistantConversionStream(request);
    }

    private AssistantCreateCommand createCommand(Map<String, Object> request) {
        Map<String, Object> brief = brief(request);
        AssistantCreateCommand command = new AssistantCreateCommand();
        command.setUserId(userId(request));
        command.setOrgId(orgId(request));
        command.setName(defaultIfBlank(stringValue(first(brief, "name", "title")), stringValue(first(request, "name", "title"))));
        command.setDesc(defaultIfBlank(stringValue(first(brief, "desc", "description")), stringValue(first(request, "desc", "description"))));
        command.setCategory(intValue(first(request, "category"), intValue(first(brief, "category"), 1)));
        command.setAvatarKey(avatarValue(brief, request, "key", "avatarKey"));
        command.setAvatarPath(avatarValue(brief, request, "path", "avatarPath", "avatarUrl"));
        return command;
    }

    private AppPublishCommand publishCommand(Map<String, Object> request) {
        AppPublishCommand command = new AppPublishCommand();
        command.setAppId(assistantId(request));
        command.setAppType(APP_TYPE_AGENT);
        command.setVersion(defaultIfBlank(stringValue(first(request, "version")), ""));
        command.setDesc(defaultIfBlank(stringValue(first(request, "desc", "description")), ""));
        command.setPublishType(defaultIfBlank(stringValue(first(request, "publishType")), PUBLISH_TYPE_PRIVATE));
        command.setUserId(userId(request));
        command.setOrgId(orgId(request));
        return command;
    }

    private AppVersionQuery versionQuery(Map<String, Object> request) {
        return new AppVersionQuery(assistantId(request), APP_TYPE_AGENT, userId(request), orgId(request));
    }

    private AssistantResourceCommand resourceCommand(Map<String, Object> request, String defaultType, String... idKeys) {
        AssistantResourceCommand command = new AssistantResourceCommand();
        command.setAssistantId(assistantId(request));
        command.setUserId(userId(request));
        command.setOrgId(orgId(request));
        command.setResourceId(defaultIfBlank(stringValue(first(request, idKeys)), ""));
        command.setResourceType(defaultIfBlank(stringValue(first(request, defaultType + "Type", "resourceType")), defaultType));
        command.setActionName(defaultIfBlank(stringValue(first(request, "actionName", "apiName")), ""));
        command.setDesc(defaultIfBlank(stringValue(first(request, "desc", "description")), ""));
        command.setEnable(booleanValue(first(request, "enable"), true));
        command.setToolConfig(toolConfig(request));
        return command;
    }

    private AssistantConversationCreateCommand conversationCreateCommand(Map<String, Object> request) {
        AssistantConversationCreateCommand command = new AssistantConversationCreateCommand();
        command.setAssistantId(assistantId(request));
        command.setPrompt(defaultIfBlank(stringValue(first(request, "prompt", "title")), ""));
        command.setConversationType(defaultIfBlank(stringValue(first(request, "conversationType")), "agent"));
        command.setUserId(userId(request));
        command.setOrgId(orgId(request));
        return command;
    }

    private AssistantConversationDeleteCommand conversationDeleteCommand(Map<String, Object> request) {
        AssistantConversationDeleteCommand command = new AssistantConversationDeleteCommand();
        command.setAssistantId(assistantId(request));
        command.setConversationId(defaultIfBlank(stringValue(first(request, "conversationId")), ""));
        command.setDetailId(defaultIfBlank(stringValue(first(request, "detailId")), ""));
        command.setUserId(userId(request));
        command.setOrgId(orgId(request));
        return command;
    }

    private AssistantConversationListQuery conversationListQuery(Map<String, Object> request) {
        AssistantConversationListQuery query = new AssistantConversationListQuery();
        query.setAssistantId(assistantId(request));
        query.setConversationType(defaultIfBlank(stringValue(first(request, "conversationType")), "agent"));
        query.setPageNo(positiveInt(first(request, "pageNo", "page_no"), 1));
        query.setPageSize(positiveInt(first(request, "pageSize", "page_size"), 20));
        query.setUserId(userId(request));
        query.setOrgId(orgId(request));
        return query;
    }

    private AssistantConversationDetailQuery conversationDetailQuery(Map<String, Object> request) {
        AssistantConversationDetailQuery query = new AssistantConversationDetailQuery();
        query.setConversationId(defaultIfBlank(stringValue(first(request, "conversationId")), ""));
        query.setPageNo(positiveInt(first(request, "pageNo", "page_no"), 1));
        query.setPageSize(positiveInt(first(request, "pageSize", "page_size"), 20));
        query.setUserId(userId(request));
        query.setOrgId(orgId(request));
        return query;
    }

    private AssistantConversationStreamCommand streamCommand(Map<String, Object> request) {
        AssistantConversationStreamCommand command = new AssistantConversationStreamCommand();
        command.setAssistantId(assistantId(request));
        command.setConversationId(defaultIfBlank(stringValue(first(request, "conversationId")), ""));
        command.setPrompt(defaultIfBlank(stringValue(first(request, "prompt", "question")), ""));
        command.setSystemPrompt(defaultIfBlank(stringValue(first(request, "systemPrompt")), ""));
        command.setDraft(booleanValue(first(request, "draft"), true));
        command.setFileInfo(mapList(first(request, "fileInfo", "fileInfoList")));
        command.setUserId(userId(request));
        command.setOrgId(orgId(request));
        return command;
    }

    private Map<String, Object> assistantDetail(Map<String, Object> request) {
        if (booleanValue(first(request, "draft"), true)) {
            return assistantInfo(appService().getAssistantDraft(
                    new AssistantDetailQuery(assistantId(request), userId(request), orgId(request))),
                    assistantId(request), userId(request), orgId(request));
        }
        return assistantInfo(appService().getPublishedAssistant(new AssistantPublishedQuery(
                assistantId(request),
                defaultIfBlank(stringValue(first(request, "version")), ""),
                userId(request),
                orgId(request))), assistantId(request), userId(request), orgId(request));
    }

    private Map<String, Object> assistantInfo(Map<String, Object> detail, String assistantId, String userId, String orgId) {
        Map<String, Object> response = new LinkedHashMap<String, Object>(safe(detail));
        response.put("assistantId", defaultIfBlank(stringValue(response.get("assistantId")), assistantId));
        response.put("uuid", defaultIfBlank(stringValue(response.get("uuid")), assistantId));
        response.put("identity", identity(userId, orgId));
        return response;
    }

    private Map<String, Object> assistantListResponse(List<Map<String, Object>> list, long total) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("assistantInfos", list);
        response.put("list", list);
        response.put("total", total);
        return response;
    }

    private List<Map<String, Object>> listAssistantItems(Map<String, Object> request, String name) {
        ApplicationListResult result = appService().listAssistants(new ApplicationListQuery(
                APP_TYPE_AGENT, name, userId(request), orgId(request)));
        return result == null || result.getList() == null
                ? Collections.<Map<String, Object>>emptyList()
                : result.getList();
    }

    private Map<String, Object> snapshotInfo(String assistantId, AppVersionInfo version) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        String versionValue = version == null ? "" : defaultIfBlank(version.getVersion(), "");
        item.put("snapshotId", isBlank(versionValue) ? "" : assistantId + ":" + versionValue);
        item.put("assistantId", defaultIfBlank(assistantId, ""));
        item.put("version", versionValue);
        item.put("desc", version == null ? "" : defaultIfBlank(version.getDesc(), ""));
        item.put("createdAt", version == null ? "" : defaultIfBlank(version.getCreatedAt(), ""));
        item.put("createAt", version == null ? 0L : createAt(version.getCreatedAt()));
        item.put("category", 0);
        return item;
    }

    private Map<String, Object> pageResponse(AssistantConversationPageResult page) {
        List<Map<String, Object>> list = page == null || page.getList() == null
                ? Collections.<Map<String, Object>>emptyList()
                : page.getList();
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("data", list);
        response.put("list", list);
        response.put("total", page == null ? 0L : page.getTotal());
        response.put("pageNo", page == null ? 1 : page.getPageNo());
        response.put("pageSize", page == null ? 20 : page.getPageSize());
        return response;
    }

    private Map<String, Object> streamResponse(AssistantConversationStreamResult result) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("assistantId", result == null ? "" : defaultIfBlank(result.getAssistantId(), ""));
        response.put("conversationId", result == null ? "" : defaultIfBlank(result.getConversationId(), ""));
        response.put("detailId", result == null ? "" : defaultIfBlank(result.getDetailId(), ""));
        response.put("prompt", result == null ? "" : defaultIfBlank(result.getPrompt(), ""));
        response.put("content", result == null ? "" : defaultIfBlank(result.getResponse(), ""));
        response.put("response", result == null ? "" : defaultIfBlank(result.getResponse(), ""));
        response.put("createdAt", result == null ? 0L : result.getCreatedAt());
        return response;
    }

    private Map<String, Object> brief(Map<String, Object> request) {
        Map<String, Object> brief = mapValue(first(request, "assistantBrief", "appBrief", "briefConfig"));
        return brief.isEmpty() ? request : brief;
    }

    private String avatarValue(Map<String, Object> brief, Map<String, Object> request, String avatarField, String... directFields) {
        Map<String, Object> avatar = mapValue(brief.get("avatar"));
        String value = stringValue(avatar.get(avatarField));
        if (!isBlank(value)) {
            return value;
        }
        for (String field : directFields) {
            value = stringValue(first(brief, field));
            if (!isBlank(value)) {
                return value;
            }
            value = stringValue(first(request, field));
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private Map<String, Object> identity(String userId, String orgId) {
        Map<String, Object> identity = new LinkedHashMap<String, Object>();
        identity.put("userId", userId);
        identity.put("orgId", orgId);
        return identity;
    }

    private Map<String, Object> toolConfig(Map<String, Object> request) {
        Object value = first(request, "toolConfig");
        Map<String, Object> config = mapValue(value);
        if (!config.isEmpty() || isBlank(stringValue(value))) {
            return config;
        }
        config.put("raw", stringValue(value));
        return config;
    }

    private String assistantId(Map<String, Object> request) {
        return defaultIfBlank(stringValue(first(request, "assistantId", "agentId", "appId", "uuid")), "");
    }

    private String userId(Map<String, Object> request) {
        Map<String, Object> identity = mapValue(request.get("identity"));
        String userId = stringValue(first(request, "userId"));
        return defaultIfBlank(userId, defaultIfBlank(stringValue(identity.get("userId")), DEV_USER_ID));
    }

    private String orgId(Map<String, Object> request) {
        Map<String, Object> identity = mapValue(request.get("identity"));
        String orgId = stringValue(first(request, "orgId"));
        return defaultIfBlank(orgId, defaultIfBlank(stringValue(identity.get("orgId")), DEV_ORG_ID));
    }

    private Object first(Map<String, Object> request, String... keys) {
        for (String key : keys) {
            if (request.containsKey(key)) {
                return request.get(key);
            }
        }
        return null;
    }

    private Map<String, Object> safe(Map<String, Object> request) {
        return request == null ? new LinkedHashMap<String, Object>() : request;
    }

    private Map<String, Object> mapValue(Object value) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (!(value instanceof Map)) {
            return map;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (entry.getKey() != null) {
                map.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return map;
    }

    private List<Map<String, Object>> mapList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Object item : (List<?>) value) {
            result.add(mapValue(item));
        }
        return result;
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (Object item : (List<?>) value) {
            String text = stringValue(item);
            if (!isBlank(text)) {
                result.add(text);
            }
        }
        return result;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(stringValue(value));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private int positiveInt(Object value, int fallback) {
        int parsed = intValue(value, fallback);
        return parsed <= 0 ? fallback : parsed;
    }

    private Boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String text = stringValue(value);
        if (isBlank(text)) {
            return fallback;
        }
        return Boolean.valueOf(text);
    }

    private long createAt(String value) {
        if (isBlank(value)) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER).atZone(APP_ZONE).toInstant().toEpochMilli();
        } catch (Exception ex) {
            return 0L;
        }
    }

    private List<String> esList(String indexName) {
        List<String> existing = esDocuments.get(indexName);
        if (existing != null) {
            return existing;
        }
        List<String> created = Collections.synchronizedList(new ArrayList<String>());
        List<String> previous = esDocuments.putIfAbsent(indexName, created);
        return previous == null ? created : previous;
    }

    private boolean matches(String doc, Map<String, Object> conditions) {
        if (conditions.isEmpty()) {
            return true;
        }
        for (Object value : conditions.values()) {
            String text = stringValue(value);
            if (!isBlank(text) && !doc.contains(text)) {
                return false;
            }
        }
        return true;
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private AppService appService() {
        if (appService == null) {
            throw new IllegalStateException("AppService is not available");
        }
        return appService;
    }
}
