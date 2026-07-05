package com.unicomai.wanwu.service.rag.rpc;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.AppVersionRollbackCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RagConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RagCopyCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateResult;
import com.unicomai.wanwu.api.app.dto.RagDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.RagUpdateCommand;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.rag.RagService;
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

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class RagServiceImpl implements RagService {

    private static final String APP_TYPE_RAG = "rag";
    private static final String PUBLISH_TYPE_PRIVATE = "private";
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    public RagServiceImpl() {
    }

    RagServiceImpl(AppService appService) {
        this.appService = appService;
    }

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.RAG, "RAG Service", "rag");
    }

    @Override
    public Map<String, Object> chatRag(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        RagChatCommand command = new RagChatCommand();
        command.setRagId(ragId(safeRequest));
        command.setQuestion(defaultIfBlank(stringValue(first(safeRequest, "question", "query")), ""));
        command.setDraft(intValue(first(safeRequest, "publish"), 0) != 1);
        command.setHistory(mapList(first(safeRequest, "history")));
        command.setFileInfo(mapList(first(safeRequest, "fileInfoList", "fileInfo")));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));

        RagChatResult result = appService().streamRagChat(command);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("ragId", result == null ? command.getRagId() : result.getRagId());
        response.put("question", result == null ? command.getQuestion() : result.getQuestion());
        response.put("content", result == null ? "" : defaultIfBlank(result.getResponse(), ""));
        response.put("response", result == null ? "" : defaultIfBlank(result.getResponse(), ""));
        response.put("searchList", result == null || result.getSearchList() == null
                ? Collections.<Map<String, Object>>emptyList()
                : result.getSearchList());
        response.put("qaSearchList", result == null || result.getQaSearchList() == null
                ? Collections.<Map<String, Object>>emptyList()
                : result.getQaSearchList());
        response.put("createdAt", result == null ? 0L : result.getCreatedAt());
        return response;
    }

    @Override
    public Map<String, Object> createRag(Map<String, Object> request) {
        RagCreateCommand command = createCommand(safe(request));
        RagCreateResult result = appService().createRag(command);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("ragId", result == null ? "" : result.getRagId());
        return response;
    }

    @Override
    public void updateRag(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        Map<String, Object> brief = brief(safeRequest);
        RagUpdateCommand command = new RagUpdateCommand();
        command.setRagId(ragId(safeRequest));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        command.setName(defaultIfBlank(stringValue(first(brief, "name", "title")), stringValue(first(safeRequest, "name", "title"))));
        command.setDesc(defaultIfBlank(stringValue(first(brief, "desc", "description")), stringValue(first(safeRequest, "desc", "description"))));
        command.setAvatarKey(avatarValue(brief, safeRequest, "key", "avatarKey"));
        command.setAvatarPath(avatarValue(brief, safeRequest, "path", "avatarPath", "avatarUrl"));
        appService().updateRag(command);
    }

    @Override
    public void updateRagConfig(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        RagConfigUpdateCommand command = new RagConfigUpdateCommand();
        command.setRagId(ragId(safeRequest));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        command.setModelConfig(mapValue(first(safeRequest, "modelConfig")));
        command.setRerankConfig(mapValue(first(safeRequest, "rerankConfig")));
        command.setQaRerankConfig(mapValue(first(safeRequest, "qaRerankConfig", "QArerankConfig", "QARerankConfig")));
        command.setKnowledgeBaseConfig(mapValue(first(safeRequest, "knowledgeBaseConfig")));
        command.setQaKnowledgeBaseConfig(mapValue(first(safeRequest,
                "qaKnowledgeBaseConfig", "QAknowledgeBaseConfig", "QAKnowledgeBaseConfig")));
        command.setSafetyConfig(mapValue(first(safeRequest, "safetyConfig", "sensitiveConfig")));
        command.setVisionConfig(mapValue(first(safeRequest, "visionConfig")));
        appService().updateRagConfig(command);
    }

    @Override
    public void deleteRag(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        RagDeleteCommand command = new RagDeleteCommand();
        command.setRagId(ragId(safeRequest));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        appService().deleteRag(command);
    }

    @Override
    public Map<String, Object> getRagDetail(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        RagDetailQuery query = detailQuery(safeRequest);
        Map<String, Object> detail = intValue(first(safeRequest, "publish"), 0) == 1
                ? appService().getPublishedRag(query)
                : appService().getRagDraft(query);
        Map<String, Object> response = new LinkedHashMap<String, Object>(safe(detail));
        response.put("ragId", defaultIfBlank(stringValue(response.get("ragId")), query.getRagId()));
        response.put("identity", identity(query.getUserId(), query.getOrgId()));
        return response;
    }

    @Override
    public Map<String, Object> listRag(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        ApplicationListQuery query = new ApplicationListQuery(
                APP_TYPE_RAG,
                defaultIfBlank(stringValue(first(safeRequest, "name")), ""),
                userId(safeRequest),
                orgId(safeRequest));
        query.setSearchType(defaultIfBlank(stringValue(first(safeRequest, "searchType")), ""));
        ApplicationListResult result = appService().listApplications(query);
        List<Map<String, Object>> list = result == null || result.getList() == null
                ? Collections.<Map<String, Object>>emptyList()
                : result.getList();
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("ragInfos", list);
        response.put("list", list);
        response.put("total", result == null ? 0L : result.getTotal());
        return response;
    }

    @Override
    public Map<String, Object> getRagByIds(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        List<String> ragIds = stringList(first(safeRequest, "ragIdList", "ragIds"));
        List<Map<String, Object>> all = mapList(listRag(safeRequest).get("ragInfos"));
        List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : all) {
            if (ragIds.contains(ragId(item))) {
                filtered.add(item);
            }
        }
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("ragInfos", filtered);
        return response;
    }

    @Override
    public Map<String, Object> copyRag(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        RagCopyCommand command = new RagCopyCommand();
        command.setRagId(ragId(safeRequest));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        RagCreateResult result = appService().copyRag(command);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("ragId", result == null ? "" : result.getRagId());
        return response;
    }

    @Override
    public void publishRag(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AppPublishCommand command = new AppPublishCommand();
        command.setAppId(ragId(safeRequest));
        command.setAppType(APP_TYPE_RAG);
        command.setVersion(defaultIfBlank(stringValue(first(safeRequest, "version")), ""));
        command.setDesc(defaultIfBlank(stringValue(first(safeRequest, "desc", "description")), ""));
        command.setPublishType(defaultIfBlank(stringValue(first(safeRequest, "publishType")), PUBLISH_TYPE_PRIVATE));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        appService().publishApp(command);
    }

    @Override
    public void updatePublishRag(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AppVersionUpdateCommand command = new AppVersionUpdateCommand();
        command.setAppId(ragId(safeRequest));
        command.setAppType(APP_TYPE_RAG);
        command.setDesc(defaultIfBlank(stringValue(first(safeRequest, "desc", "description")), ""));
        command.setPublishType(defaultIfBlank(stringValue(first(safeRequest, "publishType")), PUBLISH_TYPE_PRIVATE));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        appService().updateAppVersion(command);
    }

    @Override
    public Map<String, Object> listPublishRagHistory(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AppVersionListResult result = appService().listAppVersions(versionQuery(safeRequest));
        List<Map<String, Object>> history = new ArrayList<Map<String, Object>>();
        if (result != null && result.getList() != null) {
            for (AppVersionInfo version : result.getList()) {
                history.add(versionInfo(ragId(safeRequest), version));
            }
        }
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("historyList", history);
        response.put("total", result == null ? 0L : result.getTotal());
        return response;
    }

    @Override
    public void overwriteRagDraft(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        AppVersionRollbackCommand command = new AppVersionRollbackCommand();
        command.setAppId(ragId(safeRequest));
        command.setAppType(APP_TYPE_RAG);
        command.setVersion(defaultIfBlank(stringValue(first(safeRequest, "version")), ""));
        command.setUserId(userId(safeRequest));
        command.setOrgId(orgId(safeRequest));
        appService().rollbackAppVersion(command);
    }

    @Override
    public Map<String, Object> getPublishRagDesc(Map<String, Object> request) {
        AppVersionInfo version = appService().getLatestAppVersion(versionQuery(safe(request)));
        return versionInfo(ragId(safe(request)), version);
    }

    @Override
    public Map<String, Object> getPublishRagDescBatch(Map<String, Object> request) {
        Map<String, Object> safeRequest = safe(request);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (String id : stringList(first(safeRequest, "ragIdList", "ragIds"))) {
            Map<String, Object> itemRequest = new LinkedHashMap<String, Object>(safeRequest);
            itemRequest.put("ragId", id);
            list.add(getPublishRagDesc(itemRequest));
        }
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("list", list);
        return response;
    }

    private RagCreateCommand createCommand(Map<String, Object> request) {
        Map<String, Object> brief = brief(request);
        RagCreateCommand command = new RagCreateCommand();
        command.setUserId(userId(request));
        command.setOrgId(orgId(request));
        command.setName(defaultIfBlank(stringValue(first(brief, "name", "title")), stringValue(first(request, "name", "title"))));
        command.setDesc(defaultIfBlank(stringValue(first(brief, "desc", "description")), stringValue(first(request, "desc", "description"))));
        command.setAvatarKey(avatarValue(brief, request, "key", "avatarKey"));
        command.setAvatarPath(avatarValue(brief, request, "path", "avatarPath", "avatarUrl"));
        return command;
    }

    private RagDetailQuery detailQuery(Map<String, Object> request) {
        return new RagDetailQuery(
                ragId(request),
                defaultIfBlank(stringValue(first(request, "version")), ""),
                userId(request),
                orgId(request));
    }

    private AppVersionQuery versionQuery(Map<String, Object> request) {
        return new AppVersionQuery(ragId(request), APP_TYPE_RAG, userId(request), orgId(request));
    }

    private Map<String, Object> versionInfo(String ragId, AppVersionInfo version) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("ragId", defaultIfBlank(ragId, ""));
        item.put("version", version == null ? "" : defaultIfBlank(version.getVersion(), ""));
        item.put("desc", version == null ? "" : defaultIfBlank(version.getDesc(), ""));
        item.put("createdAt", version == null ? "" : defaultIfBlank(version.getCreatedAt(), ""));
        item.put("createAt", version == null ? 0L : createAt(version.getCreatedAt()));
        return item;
    }

    private Map<String, Object> brief(Map<String, Object> request) {
        Map<String, Object> brief = mapValue(first(request, "appBrief", "briefConfig"));
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

    private String ragId(Map<String, Object> request) {
        return defaultIfBlank(stringValue(first(request, "ragId", "appId", "uuid")), "");
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
