package com.unicomai.wanwu.service.app.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import com.unicomai.wanwu.service.app.domain.AssistantConversationMessageRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationRecord;
import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.AssistantSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class AppServiceImpl implements AppService {

    private static final String APP_TYPE_AGENT = "agent";
    private static final String APP_TYPE_ASSISTANT = "assistant";
    private static final String PUBLISH_TYPE_UNPUBLISHED = "";
    private static final String PUBLISH_TYPE_PRIVATE = "private";
    private static final String PUBLISH_TYPE_ORGANIZATION = "organization";
    private static final String PUBLISH_TYPE_PUBLIC = "public";
    private static final String CONVERSATION_TYPE_PUBLISHED = "published";
    private static final String CONVERSATION_TYPE_DRAFT = "draft";
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
    private static final String DEFAULT_VERSION = "v1.0.0";
    private static final Pattern VERSION_PATTERN = Pattern.compile("^v\\d+\\.\\d+\\.\\d+$");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<List<String>>() {
    };
    private static final TypeReference<List<Object>> OBJECT_LIST_TYPE = new TypeReference<List<Object>>() {
    };
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Shanghai"));

    private final ApplicationRepository applicationRepository;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Autowired
    public AppServiceImpl(ApplicationRepository applicationRepository) {
        this(applicationRepository, Clock.systemUTC(), new ObjectMapper());
    }

    public AppServiceImpl(ApplicationRepository applicationRepository, Clock clock) {
        this(applicationRepository, clock, new ObjectMapper());
    }

    AppServiceImpl(ApplicationRepository applicationRepository, Clock clock, ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    @Override
    public AssistantCreateResult createAssistant(AssistantCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("assistant create command is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException("assistant name is required");
        }
        long now = clock.millis();
        AppRecord record = new AppRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(defaultIfBlank(command.getUserId(), DEV_USER_ID));
        record.setOrgId(defaultIfBlank(command.getOrgId(), DEV_ORG_ID));
        record.setAppId(newAssistantId());
        record.setAppType(APP_TYPE_AGENT);
        record.setPublishType(PUBLISH_TYPE_UNPUBLISHED);
        record.setName(command.getName().trim());
        record.setDesc(defaultIfBlank(command.getDesc(), ""));
        record.setAvatarKey(defaultIfBlank(command.getAvatarKey(), ""));
        record.setAvatarPath(defaultIfBlank(command.getAvatarPath(), ""));
        record.setCategory(command.getCategory() == 0 ? 1 : command.getCategory());
        applicationRepository.saveAssistant(record);
        return new AssistantCreateResult(record.getAppId());
    }

    @Override
    public void updateAssistant(AssistantUpdateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("assistant update command is required");
        }
        if (isBlank(command.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException("assistant name is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord existing = applicationRepository.findAssistant(userId, orgId, command.getAssistantId());
        if (existing == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }

        AppRecord record = new AppRecord();
        record.setId(existing.getId());
        record.setCreatedAt(existing.getCreatedAt());
        record.setUpdatedAt(clock.millis());
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setAppId(command.getAssistantId());
        record.setAppType(existing.getAppType());
        record.setPublishType(existing.getPublishType());
        record.setName(command.getName().trim());
        record.setDesc(defaultIfBlank(command.getDesc(), ""));
        record.setAvatarKey(defaultIfBlank(command.getAvatarKey(), ""));
        record.setAvatarPath(defaultIfBlank(command.getAvatarPath(), ""));
        record.setCategory(command.getCategory() == 0 ? existing.getCategory() : command.getCategory());
        if (record.getCategory() == null || record.getCategory() == 0) {
            record.setCategory(1);
        }

        AppRecord updated = applicationRepository.updateAssistant(record);
        if (updated == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }
    }

    @Override
    public void updateAssistantConfig(AssistantConfigUpdateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("assistant config command is required");
        }
        if (isBlank(command.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord existing = applicationRepository.findAssistant(userId, orgId, command.getAssistantId());
        if (existing == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }

        long now = clock.millis();
        AssistantDraftConfigRecord record = new AssistantDraftConfigRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setAssistantId(command.getAssistantId());
        record.setPrologue(defaultIfBlank(command.getPrologue(), ""));
        record.setInstructions(defaultIfBlank(command.getInstructions(), ""));
        record.setMemoryConfigJson(toJsonOrNull(command.getMemoryConfig()));
        record.setKnowledgeBaseConfigJson(toJsonOrNull(command.getKnowledgeBaseConfig()));
        record.setModelConfigJson(toJsonOrNull(command.getModelConfig()));
        record.setSafetyConfigJson(toJsonOrNull(command.getSafetyConfig()));
        record.setVisionConfigJson(toJsonOrNull(command.getVisionConfig()));
        record.setRerankConfigJson(toJsonOrNull(command.getRerankConfig()));
        record.setRecommendConfigJson(toJsonOrNull(command.getRecommendConfig()));
        record.setRecommendQuestionsJson(toJsonOrNull(command.getRecommendQuestion()));
        applicationRepository.saveAssistantConfig(record);
    }

    @Override
    public void deleteAssistant(AssistantDeleteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("assistant delete command is required");
        }
        if (isBlank(command.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord existing = applicationRepository.findAssistant(userId, orgId, command.getAssistantId());
        if (existing == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }
        if (!applicationRepository.deleteAssistant(userId, orgId, command.getAssistantId())) {
            throw new IllegalArgumentException("assistant draft not found");
        }
    }

    @Override
    public AssistantCreateResult copyAssistant(AssistantCopyCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("assistant copy command is required");
        }
        if (isBlank(command.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord source = applicationRepository.findAssistant(userId, orgId, command.getAssistantId());
        if (source == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }

        long now = clock.millis();
        String newAssistantId = newAssistantId();
        AppRecord copied = copyBaseRecord(source, newAssistantId, nextCopyName(userId, orgId, source.getName()), now);
        AssistantDraftConfigRecord copiedConfig = copyConfig(
                applicationRepository.findAssistantConfig(userId, orgId, command.getAssistantId()),
                userId,
                orgId,
                newAssistantId,
                now);
        applicationRepository.copyAssistant(copied, copiedConfig);
        return new AssistantCreateResult(newAssistantId);
    }

    @Override
    public void publishApp(AppPublishCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app publish command is required");
        }
        String appType = normalizeAgentAppType(command.getAppType());
        if (!APP_TYPE_AGENT.equals(appType)) {
            throw new IllegalArgumentException("only agent publish is supported");
        }
        if (isBlank(command.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String publishType = normalizePublishType(command.getPublishType(), PUBLISH_TYPE_PRIVATE);
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord record = applicationRepository.findAssistant(userId, orgId, command.getAppId());
        if (record == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }

        AssistantSnapshotRecord latest = applicationRepository.findLatestAssistantSnapshot(userId, orgId, command.getAppId());
        String version = isBlank(command.getVersion()) ? nextVersion(latest) : command.getVersion().trim();
        validateVersion(version);
        if (latest != null && compareVersion(version, latest.getVersion()) <= 0) {
            throw new IllegalArgumentException("app version must be greater than latest version");
        }
        if (applicationRepository.findAssistantSnapshotByVersion(userId, orgId, command.getAppId(), version) != null) {
            throw new IllegalArgumentException("app version must be greater than latest version");
        }

        long now = clock.millis();
        AssistantDraftConfigRecord config = applicationRepository.findAssistantConfig(userId, orgId, command.getAppId());
        AssistantSnapshotRecord snapshot = new AssistantSnapshotRecord();
        snapshot.setCreatedAt(now);
        snapshot.setUpdatedAt(now);
        snapshot.setUserId(userId);
        snapshot.setOrgId(orgId);
        snapshot.setAssistantId(command.getAppId());
        snapshot.setVersion(version);
        snapshot.setDesc(defaultIfBlank(command.getDesc(), ""));
        snapshot.setCategory(record.getCategory());
        snapshot.setAssistantInfoJson(toJsonOrNull(toFrontendDraft(record, config)));
        snapshot.setAssistantConfigJson(toJsonOrNull(config));
        applicationRepository.saveAssistantSnapshot(snapshot);
        applicationRepository.updateAssistantPublishType(userId, orgId, command.getAppId(), publishType, now);
    }

    @Override
    public void unpublishApp(AppPublishCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app publish command is required");
        }
        String appType = normalizeAgentAppType(command.getAppType());
        if (!APP_TYPE_AGENT.equals(appType)) {
            throw new IllegalArgumentException("only agent publish is supported");
        }
        if (isBlank(command.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        if (!applicationRepository.updateAssistantPublishType(
                userId, orgId, command.getAppId(), PUBLISH_TYPE_UNPUBLISHED, clock.millis())) {
            throw new IllegalArgumentException("assistant draft not found");
        }
    }

    @Override
    public AppVersionInfo getLatestAppVersion(AppVersionQuery query) {
        VersionContext context = versionContext(query);
        AppRecord record = applicationRepository.findAssistant(context.userId, context.orgId, context.appId);
        if (record == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }
        AssistantSnapshotRecord latest = applicationRepository.findLatestAssistantSnapshot(
                context.userId, context.orgId, context.appId);
        if (latest == null) {
            return new AppVersionInfo("", "", "", defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
        }
        return toVersionInfo(latest, defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
    }

    @Override
    public AppVersionListResult listAppVersions(AppVersionQuery query) {
        VersionContext context = versionContext(query);
        List<AssistantSnapshotRecord> snapshots = applicationRepository.listAssistantSnapshots(
                context.userId, context.orgId, context.appId);
        List<AppVersionInfo> versions = new ArrayList<>(snapshots.size());
        for (AssistantSnapshotRecord snapshot : snapshots) {
            versions.add(toVersionInfo(snapshot, ""));
        }
        return new AppVersionListResult(versions, versions.size());
    }

    @Override
    public void updateAppVersion(AppVersionUpdateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app version update command is required");
        }
        VersionContext context = versionContext(command.getAppId(), command.getAppType(), command.getUserId(), command.getOrgId());
        String publishType = normalizePublishType(command.getPublishType(), null);
        long now = clock.millis();
        if (!applicationRepository.updateLatestAssistantSnapshot(
                context.userId, context.orgId, context.appId, defaultIfBlank(command.getDesc(), ""), now)) {
            throw new IllegalArgumentException("assistant snapshot not found");
        }
        if (!applicationRepository.updateAssistantPublishType(context.userId, context.orgId, context.appId, publishType, now)) {
            throw new IllegalArgumentException("assistant draft not found");
        }
    }

    @Override
    public void rollbackAppVersion(AppVersionRollbackCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app version rollback command is required");
        }
        if (isBlank(command.getVersion())) {
            throw new IllegalArgumentException("app version is required");
        }
        VersionContext context = versionContext(command.getAppId(), command.getAppType(), command.getUserId(), command.getOrgId());
        AppRecord existing = applicationRepository.findAssistant(context.userId, context.orgId, context.appId);
        if (existing == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }
        AssistantSnapshotRecord snapshot = applicationRepository.findAssistantSnapshotByVersion(
                context.userId, context.orgId, context.appId, command.getVersion());
        if (snapshot == null) {
            throw new IllegalArgumentException("assistant snapshot not found");
        }
        Map<String, Object> snapshotDraft = mapOrDefault(snapshot.getAssistantInfoJson(), new LinkedHashMap<String, Object>());
        AppRecord restored = restoreRecord(existing, snapshotDraft, clock.millis());
        AssistantDraftConfigRecord restoredConfig = restoreConfig(
                snapshotDraft, context.userId, context.orgId, context.appId, restored.getUpdatedAt());
        if (!applicationRepository.rollbackAssistant(restored, restoredConfig)) {
            throw new IllegalArgumentException("assistant draft not found");
        }
    }

    @Override
    public ApplicationListResult listAssistants(ApplicationListQuery query) {
        String userId = query == null ? DEV_USER_ID : defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = query == null ? DEV_ORG_ID : defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        String name = query == null ? "" : defaultIfBlank(query.getName(), "");
        List<AppRecord> records = applicationRepository.listAssistants(userId, orgId, name);
        List<Map<String, Object>> items = new ArrayList<>(records.size());
        for (AppRecord record : records) {
            items.add(toFrontendCard(record));
        }
        return new ApplicationListResult(items, items.size());
    }

    @Override
    public Map<String, Object> getAssistantDraft(AssistantDetailQuery query) {
        if (query == null || isBlank(query.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        AppRecord record = applicationRepository.findAssistant(userId, orgId, query.getAssistantId());
        if (record == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }
        AssistantDraftConfigRecord config = applicationRepository.findAssistantConfig(userId, orgId, query.getAssistantId());
        return toFrontendDraft(record, config);
    }

    @Override
    public Map<String, Object> getPublishedAssistant(AssistantPublishedQuery query) {
        if (query == null || isBlank(query.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        AssistantSnapshotRecord snapshot;
        if (isBlank(query.getVersion())) {
            snapshot = applicationRepository.findLatestAssistantSnapshot(userId, orgId, query.getAssistantId());
        } else {
            snapshot = applicationRepository.findAssistantSnapshotByVersion(
                    userId, orgId, query.getAssistantId(), query.getVersion());
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("assistant snapshot not found");
        }
        Map<String, Object> draft = mapOrDefault(snapshot.getAssistantInfoJson(), new LinkedHashMap<String, Object>());
        AppRecord record = applicationRepository.findAssistant(userId, orgId, query.getAssistantId());
        draft.put("assistantId", query.getAssistantId());
        draft.put("uuid", query.getAssistantId());
        draft.put("newAgent", false);
        draft.put("publishType", record == null ? PUBLISH_TYPE_UNPUBLISHED : defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
        return draft;
    }

    @Override
    public AssistantConversationCreateResult createAssistantConversation(AssistantConversationCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("conversation create command is required");
        }
        if (isBlank(command.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        if (isBlank(command.getPrompt())) {
            throw new IllegalArgumentException("conversation prompt is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        ensureAssistantExists(userId, orgId, command.getAssistantId());
        String conversationType = normalizeConversationType(command.getConversationType(), CONVERSATION_TYPE_PUBLISHED);
        AssistantConversationRecord record = newConversation(
                userId, orgId, command.getAssistantId(), conversationType, command.getPrompt());
        applicationRepository.saveConversation(record);
        return new AssistantConversationCreateResult(record.getConversationId());
    }

    @Override
    public void deleteAssistantConversation(AssistantConversationDeleteCommand command) {
        ConversationDeleteContext context = conversationDeleteContext(command, true);
        if (!applicationRepository.deleteConversation(context.userId, context.orgId, context.conversation.getConversationId())) {
            throw new IllegalArgumentException("assistant conversation not found");
        }
    }

    @Override
    public void clearAssistantConversation(AssistantConversationDeleteCommand command) {
        ConversationDeleteContext context = conversationDeleteContext(command, true);
        if (isBlank(command.getDetailId())) {
            applicationRepository.deleteConversationMessages(context.userId, context.orgId, context.conversation.getConversationId());
            return;
        }
        applicationRepository.deleteConversationMessage(
                context.userId, context.orgId, context.conversation.getConversationId(), command.getDetailId());
    }

    @Override
    public void deleteDraftAssistantConversation(AssistantConversationDeleteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("conversation delete command is required");
        }
        if (isBlank(command.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AssistantConversationRecord conversation = applicationRepository.findDraftConversation(
                userId, orgId, command.getAssistantId());
        if (conversation == null) {
            return;
        }
        if (isBlank(command.getDetailId())) {
            applicationRepository.deleteConversation(userId, orgId, conversation.getConversationId());
            return;
        }
        applicationRepository.deleteConversationMessage(
                userId, orgId, conversation.getConversationId(), command.getDetailId());
    }

    @Override
    public AssistantConversationPageResult listAssistantConversations(AssistantConversationListQuery query) {
        ConversationListContext context = conversationListContext(query, CONVERSATION_TYPE_PUBLISHED);
        int offset = offset(context.pageNo, context.pageSize);
        List<AssistantConversationRecord> records = applicationRepository.listConversations(
                context.userId, context.orgId, context.assistantId, context.conversationType, offset, context.pageSize);
        long total = applicationRepository.countConversations(
                context.userId, context.orgId, context.assistantId, context.conversationType);
        List<Map<String, Object>> items = new ArrayList<>(records.size());
        for (AssistantConversationRecord record : records) {
            items.add(toConversationItem(record));
        }
        return new AssistantConversationPageResult(items, total, context.pageNo, context.pageSize);
    }

    @Override
    public AssistantConversationPageResult listAssistantConversationDetails(AssistantConversationDetailQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("conversation detail query is required");
        }
        if (isBlank(query.getConversationId())) {
            throw new IllegalArgumentException("conversation id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        int pageNo = normalizePageNo(query.getPageNo());
        int pageSize = normalizePageSize(query.getPageSize());
        int offset = offset(pageNo, pageSize);
        List<AssistantConversationMessageRecord> records = applicationRepository.listConversationMessages(
                userId, orgId, query.getConversationId(), offset, pageSize);
        long total = applicationRepository.countConversationMessages(userId, orgId, query.getConversationId());
        List<Map<String, Object>> items = new ArrayList<>(records.size());
        for (AssistantConversationMessageRecord record : records) {
            items.add(toConversationDetailItem(record));
        }
        return new AssistantConversationPageResult(items, total, pageNo, pageSize);
    }

    @Override
    public AssistantConversationPageResult listDraftAssistantConversationDetails(AssistantConversationListQuery query) {
        ConversationListContext context = conversationListContext(query, CONVERSATION_TYPE_DRAFT);
        AssistantConversationRecord conversation = applicationRepository.findDraftConversation(
                context.userId, context.orgId, context.assistantId);
        if (conversation == null) {
            return new AssistantConversationPageResult(Collections.<Map<String, Object>>emptyList(),
                    0, context.pageNo, context.pageSize);
        }
        AssistantConversationDetailQuery detailQuery = new AssistantConversationDetailQuery();
        detailQuery.setConversationId(conversation.getConversationId());
        detailQuery.setPageNo(context.pageNo);
        detailQuery.setPageSize(context.pageSize);
        detailQuery.setUserId(context.userId);
        detailQuery.setOrgId(context.orgId);
        return listAssistantConversationDetails(detailQuery);
    }

    @Override
    public AssistantConversationStreamResult streamAssistantConversation(AssistantConversationStreamCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("conversation stream command is required");
        }
        if (isBlank(command.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        if (isBlank(command.getPrompt())) {
            throw new IllegalArgumentException("conversation prompt is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord assistant = ensureAssistantExists(userId, orgId, command.getAssistantId());
        if (!command.isDraft()
                && applicationRepository.findLatestAssistantSnapshot(userId, orgId, command.getAssistantId()) == null) {
            throw new IllegalArgumentException("assistant snapshot not found");
        }
        AssistantConversationRecord conversation = resolveConversation(command, userId, orgId);
        String response = deterministicResponse(assistant, command.getPrompt());
        long now = clock.millis();
        String detailId = newDetailId();
        AssistantConversationMessageRecord message = new AssistantConversationMessageRecord();
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        message.setUserId(userId);
        message.setOrgId(orgId);
        message.setAssistantId(command.getAssistantId());
        message.setConversationId(conversation.getConversationId());
        message.setDetailId(detailId);
        message.setPrompt(command.getPrompt());
        message.setSysPrompt(defaultIfBlank(command.getSystemPrompt(), ""));
        message.setResponse(response);
        message.setResponseListJson(toJsonOrNull(Collections.emptyList()));
        message.setSearchListJson(toJsonOrNull(Collections.emptyList()));
        message.setRequestFilesJson(toJsonOrNull(command.getFileInfo() == null
                ? Collections.emptyList()
                : command.getFileInfo()));
        message.setResponseFilesJson(toJsonOrNull(Collections.emptyList()));
        message.setSubConversationListJson(toJsonOrNull(Collections.emptyList()));
        message.setFileSize(0L);
        message.setFileName("");
        message.setQaType(0);
        applicationRepository.saveConversationMessage(message);
        applicationRepository.touchConversation(userId, orgId, conversation.getConversationId(), now);

        AssistantConversationStreamResult result = new AssistantConversationStreamResult();
        result.setAssistantId(command.getAssistantId());
        result.setConversationId(conversation.getConversationId());
        result.setDetailId(detailId);
        result.setPrompt(command.getPrompt());
        result.setResponse(response);
        result.setCreatedAt(now);
        return result;
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.APP, "App Service", "app");
    }

    private AppRecord ensureAssistantExists(String userId, String orgId, String assistantId) {
        AppRecord record = applicationRepository.findAssistant(userId, orgId, assistantId);
        if (record == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }
        return record;
    }

    private AssistantConversationRecord newConversation(String userId,
                                                        String orgId,
                                                        String assistantId,
                                                        String conversationType,
                                                        String prompt) {
        long now = clock.millis();
        AssistantConversationRecord record = new AssistantConversationRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setAssistantId(assistantId);
        record.setConversationId(newConversationId());
        record.setConversationType(conversationType);
        record.setTitle(conversationTitle(prompt));
        return record;
    }

    private AssistantConversationRecord resolveConversation(AssistantConversationStreamCommand command,
                                                           String userId,
                                                           String orgId) {
        if (!isBlank(command.getConversationId())) {
            AssistantConversationRecord conversation = applicationRepository.findConversation(
                    userId, orgId, command.getConversationId());
            if (conversation == null) {
                throw new IllegalArgumentException("assistant conversation not found");
            }
            return conversation;
        }
        String conversationType = command.isDraft() ? CONVERSATION_TYPE_DRAFT : CONVERSATION_TYPE_PUBLISHED;
        if (command.isDraft()) {
            AssistantConversationRecord draft = applicationRepository.findDraftConversation(
                    userId, orgId, command.getAssistantId());
            if (draft != null) {
                return draft;
            }
        }
        AssistantConversationRecord created = newConversation(
                userId, orgId, command.getAssistantId(), conversationType, command.getPrompt());
        applicationRepository.saveConversation(created);
        return created;
    }

    private ConversationListContext conversationListContext(AssistantConversationListQuery query, String defaultType) {
        if (query == null) {
            throw new IllegalArgumentException("conversation list query is required");
        }
        if (isBlank(query.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        ensureAssistantExists(userId, orgId, query.getAssistantId());
        return new ConversationListContext(
                query.getAssistantId(),
                normalizeConversationType(query.getConversationType(), defaultType),
                normalizePageNo(query.getPageNo()),
                normalizePageSize(query.getPageSize()),
                userId,
                orgId);
    }

    private ConversationDeleteContext conversationDeleteContext(AssistantConversationDeleteCommand command,
                                                               boolean requireConversationId) {
        if (command == null) {
            throw new IllegalArgumentException("conversation delete command is required");
        }
        if (requireConversationId && isBlank(command.getConversationId())) {
            throw new IllegalArgumentException("conversation id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AssistantConversationRecord conversation = applicationRepository.findConversation(
                userId, orgId, command.getConversationId());
        if (conversation == null) {
            throw new IllegalArgumentException("assistant conversation not found");
        }
        return new ConversationDeleteContext(userId, orgId, conversation);
    }

    private Map<String, Object> toConversationItem(AssistantConversationRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("conversationId", record.getConversationId());
        item.put("assistantId", record.getAssistantId());
        item.put("title", defaultIfBlank(record.getTitle(), ""));
        item.put("createdAt", formatMillis(record.getCreatedAt()));
        return item;
    }

    private Map<String, Object> toConversationDetailItem(AssistantConversationMessageRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", record.getDetailId());
        item.put("assistantId", record.getAssistantId());
        item.put("conversationId", record.getConversationId());
        item.put("prompt", defaultIfBlank(record.getPrompt(), ""));
        item.put("sysPrompt", defaultIfBlank(record.getSysPrompt(), ""));
        item.put("response", defaultIfBlank(record.getResponse(), ""));
        item.put("responseList", listOrDefault(record.getResponseListJson()));
        item.put("searchList", listOrDefault(record.getSearchListJson()));
        item.put("qa_type", record.getQaType() == null ? 0 : record.getQaType());
        item.put("createdBy", record.getUserId());
        item.put("createdAt", record.getCreatedAt() == null ? 0L : record.getCreatedAt());
        item.put("updatedAt", record.getUpdatedAt() == null ? 0L : record.getUpdatedAt());
        item.put("requestFiles", listOrDefault(record.getRequestFilesJson()));
        item.put("fileSize", record.getFileSize() == null ? 0L : record.getFileSize());
        item.put("fileName", defaultIfBlank(record.getFileName(), ""));
        item.put("subConversationList", listOrDefault(record.getSubConversationListJson()));
        item.put("responseFiles", listOrDefault(record.getResponseFilesJson()));
        return item;
    }

    private String normalizeConversationType(String value, String defaultValue) {
        String normalized = defaultIfBlank(value, defaultValue);
        if (CONVERSATION_TYPE_PUBLISHED.equals(normalized) || CONVERSATION_TYPE_DRAFT.equals(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("conversation type is invalid");
    }

    private int normalizePageNo(int pageNo) {
        return pageNo <= 0 ? 1 : pageNo;
    }

    private int normalizePageSize(int pageSize) {
        return pageSize <= 0 ? 20 : pageSize;
    }

    private int offset(int pageNo, int pageSize) {
        return (normalizePageNo(pageNo) - 1) * normalizePageSize(pageSize);
    }

    private String conversationTitle(String prompt) {
        String title = defaultIfBlank(prompt, "").trim();
        return title.length() > 60 ? title.substring(0, 60) : title;
    }

    private String deterministicResponse(AppRecord assistant, String prompt) {
        return "Demo response from " + defaultIfBlank(assistant.getName(), "Agent") + ": " + prompt;
    }

    private Map<String, Object> toFrontendCard(AppRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uniqueId", record.getAppType() + "_" + record.getAppId());
        item.put("appId", record.getAppId());
        item.put("appType", record.getAppType());
        item.put("avatar", avatar(record));
        item.put("name", record.getName());
        item.put("desc", record.getDesc());
        item.put("createdAt", formatMillis(record.getCreatedAt()));
        item.put("updatedAt", formatMillis(record.getUpdatedAt()));
        item.put("publishType", record.getPublishType());
        item.put("category", record.getCategory());
        AssistantSnapshotRecord latest = applicationRepository.findLatestAssistantSnapshot(
                record.getUserId(), record.getOrgId(), record.getAppId());
        item.put("version", latest == null ? "" : latest.getVersion());
        item.put("user", user(record));
        return item;
    }

    private Map<String, Object> toFrontendDraft(AppRecord record, AssistantDraftConfigRecord config) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("assistantId", record.getAppId());
        item.put("uuid", record.getAppId());
        item.put("newAgent", true);
        item.put("avatar", avatar(record));
        item.put("name", record.getName());
        item.put("desc", record.getDesc());
        item.put("category", record.getCategory());
        item.put("publishType", record.getPublishType());
        item.put("prologue", config == null ? "" : defaultIfBlank(config.getPrologue(), ""));
        item.put("instructions", config == null ? "" : defaultIfBlank(config.getInstructions(), ""));
        item.put("memoryConfig", config == null ? memoryConfig() : mapOrDefault(config.getMemoryConfigJson(), memoryConfig()));
        item.put("visionConfig", config == null ? visionConfig() : mapOrDefault(config.getVisionConfigJson(), visionConfig()));
        item.put("knowledgeBaseConfig", config == null
                ? knowledgeBaseConfig()
                : mapOrDefault(config.getKnowledgeBaseConfigJson(), knowledgeBaseConfig()));
        item.put("modelConfig", config == null ? modelConfig() : mapOrDefault(config.getModelConfigJson(), modelConfig()));
        item.put("rerankConfig", config == null ? rerankConfig() : mapOrDefault(config.getRerankConfigJson(), rerankConfig()));
        item.put("recommendQuestion", config == null
                ? Collections.emptyList()
                : stringListOrDefault(config.getRecommendQuestionsJson(), Collections.<String>emptyList()));
        item.put("safetyConfig", config == null ? null : nullableMap(config.getSafetyConfigJson()));
        item.put("recommendConfig", config == null ? null : nullableMap(config.getRecommendConfigJson()));
        return item;
    }

    private AppRecord copyBaseRecord(AppRecord source, String newAssistantId, String newName, long now) {
        AppRecord copied = new AppRecord();
        copied.setCreatedAt(now);
        copied.setUpdatedAt(now);
        copied.setUserId(source.getUserId());
        copied.setOrgId(source.getOrgId());
        copied.setAppId(newAssistantId);
        copied.setAppType(source.getAppType());
        copied.setPublishType(PUBLISH_TYPE_UNPUBLISHED);
        copied.setName(newName);
        copied.setDesc(defaultIfBlank(source.getDesc(), ""));
        copied.setAvatarKey(defaultIfBlank(source.getAvatarKey(), ""));
        copied.setAvatarPath(defaultIfBlank(source.getAvatarPath(), ""));
        copied.setCategory(source.getCategory());
        return copied;
    }

    private AssistantDraftConfigRecord copyConfig(AssistantDraftConfigRecord source,
                                                  String userId,
                                                  String orgId,
                                                  String newAssistantId,
                                                  long now) {
        AssistantDraftConfigRecord copied = new AssistantDraftConfigRecord();
        copied.setCreatedAt(now);
        copied.setUpdatedAt(now);
        copied.setUserId(userId);
        copied.setOrgId(orgId);
        copied.setAssistantId(newAssistantId);
        if (source == null) {
            copied.setPrologue("");
            copied.setInstructions("");
            return copied;
        }
        copied.setPrologue(source.getPrologue());
        copied.setInstructions(source.getInstructions());
        copied.setMemoryConfigJson(source.getMemoryConfigJson());
        copied.setKnowledgeBaseConfigJson(source.getKnowledgeBaseConfigJson());
        copied.setModelConfigJson(source.getModelConfigJson());
        copied.setSafetyConfigJson(source.getSafetyConfigJson());
        copied.setVisionConfigJson(source.getVisionConfigJson());
        copied.setRerankConfigJson(source.getRerankConfigJson());
        copied.setRecommendConfigJson(source.getRecommendConfigJson());
        copied.setRecommendQuestionsJson(source.getRecommendQuestionsJson());
        return copied;
    }

    private String nextCopyName(String userId, String orgId, String sourceName) {
        String prefix = sourceName + "_";
        int max = 0;
        for (String name : applicationRepository.listAssistantNamesByPrefix(userId, orgId, prefix)) {
            if (name == null || !name.startsWith(prefix)) {
                continue;
            }
            String suffix = name.substring(prefix.length());
            try {
                max = Math.max(max, Integer.parseInt(suffix));
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + (max + 1);
    }

    private VersionContext versionContext(AppVersionQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("app version query is required");
        }
        return versionContext(query.getAppId(), query.getAppType(), query.getUserId(), query.getOrgId());
    }

    private VersionContext versionContext(String appId, String appType, String userId, String orgId) {
        String normalizedAppType = normalizeAgentAppType(appType);
        if (!APP_TYPE_AGENT.equals(normalizedAppType)) {
            throw new IllegalArgumentException("only agent publish is supported");
        }
        if (isBlank(appId)) {
            throw new IllegalArgumentException("app id is required");
        }
        return new VersionContext(
                appId,
                normalizedAppType,
                defaultIfBlank(userId, DEV_USER_ID),
                defaultIfBlank(orgId, DEV_ORG_ID));
    }

    private String normalizeAgentAppType(String appType) {
        String normalized = defaultIfBlank(appType, APP_TYPE_AGENT);
        if (APP_TYPE_ASSISTANT.equals(normalized)) {
            return APP_TYPE_AGENT;
        }
        return normalized;
    }

    private String normalizePublishType(String publishType, String defaultValue) {
        String normalized = defaultValue == null ? publishType : defaultIfBlank(publishType, defaultValue);
        if (PUBLISH_TYPE_PRIVATE.equals(normalized)
                || PUBLISH_TYPE_ORGANIZATION.equals(normalized)
                || PUBLISH_TYPE_PUBLIC.equals(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("publish type is invalid");
    }

    private void validateVersion(String version) {
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new IllegalArgumentException("app version format is invalid");
        }
    }

    private String nextVersion(AssistantSnapshotRecord latest) {
        if (latest == null || isBlank(latest.getVersion())) {
            return DEFAULT_VERSION;
        }
        int[] parts = versionParts(latest.getVersion());
        return "v" + parts[0] + "." + parts[1] + "." + (parts[2] + 1);
    }

    private int compareVersion(String left, String right) {
        int[] leftParts = versionParts(left);
        int[] rightParts = versionParts(right);
        for (int i = 0; i < leftParts.length; i++) {
            if (leftParts[i] != rightParts[i]) {
                return leftParts[i] - rightParts[i];
            }
        }
        return 0;
    }

    private int[] versionParts(String version) {
        validateVersion(version);
        String[] parts = version.substring(1).split("\\.");
        return new int[]{
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        };
    }

    private AppVersionInfo toVersionInfo(AssistantSnapshotRecord snapshot, String publishType) {
        return new AppVersionInfo(
                snapshot.getVersion(),
                defaultIfBlank(snapshot.getDesc(), ""),
                formatMillis(snapshot.getCreatedAt()),
                publishType);
    }

    private AppRecord restoreRecord(AppRecord existing, Map<String, Object> snapshotDraft, long now) {
        AppRecord restored = new AppRecord();
        restored.setId(existing.getId());
        restored.setCreatedAt(existing.getCreatedAt());
        restored.setUpdatedAt(now);
        restored.setUserId(existing.getUserId());
        restored.setOrgId(existing.getOrgId());
        restored.setAppId(existing.getAppId());
        restored.setAppType(existing.getAppType());
        restored.setPublishType(existing.getPublishType());
        restored.setName(defaultIfBlank((String) snapshotDraft.get("name"), existing.getName()));
        restored.setDesc(defaultIfBlank((String) snapshotDraft.get("desc"), ""));

        Map<String, Object> avatar = mapValue(snapshotDraft.get("avatar"));
        restored.setAvatarKey(defaultIfBlank((String) avatar.get("key"), ""));
        restored.setAvatarPath(defaultIfBlank((String) avatar.get("path"), ""));
        restored.setCategory(intValue(snapshotDraft.get("category"), existing.getCategory() == null ? 1 : existing.getCategory()));
        return restored;
    }

    private AssistantDraftConfigRecord restoreConfig(Map<String, Object> snapshotDraft,
                                                     String userId,
                                                     String orgId,
                                                     String assistantId,
                                                     long now) {
        AssistantDraftConfigRecord config = new AssistantDraftConfigRecord();
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        config.setUserId(userId);
        config.setOrgId(orgId);
        config.setAssistantId(assistantId);
        config.setPrologue(defaultIfBlank((String) snapshotDraft.get("prologue"), ""));
        config.setInstructions(defaultIfBlank((String) snapshotDraft.get("instructions"), ""));
        config.setMemoryConfigJson(toJsonOrNull(snapshotDraft.get("memoryConfig")));
        config.setKnowledgeBaseConfigJson(toJsonOrNull(snapshotDraft.get("knowledgeBaseConfig")));
        config.setModelConfigJson(toJsonOrNull(snapshotDraft.get("modelConfig")));
        config.setSafetyConfigJson(toJsonOrNull(snapshotDraft.get("safetyConfig")));
        config.setVisionConfigJson(toJsonOrNull(snapshotDraft.get("visionConfig")));
        config.setRerankConfigJson(toJsonOrNull(snapshotDraft.get("rerankConfig")));
        config.setRecommendConfigJson(toJsonOrNull(snapshotDraft.get("recommendConfig")));
        config.setRecommendQuestionsJson(toJsonOrNull(snapshotDraft.get("recommendQuestion")));
        return config;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return new LinkedHashMap<>();
    }

    private int intValue(Object value, int defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String && !isBlank((String) value)) {
            return Integer.parseInt((String) value);
        }
        return defaultValue;
    }

    private Map<String, Object> memoryConfig() {
        Map<String, Object> memoryConfig = new LinkedHashMap<>();
        memoryConfig.put("maxHistoryLength", 5);
        return memoryConfig;
    }

    private Map<String, Object> visionConfig() {
        Map<String, Object> visionConfig = new LinkedHashMap<>();
        visionConfig.put("picNum", 3);
        visionConfig.put("maxPicNum", 6);
        return visionConfig;
    }

    private Map<String, Object> knowledgeBaseConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("keywordPriority", 0.8);
        config.put("matchType", "mix");
        config.put("priorityMatch", 1);
        config.put("rerankModelId", "");
        config.put("semanticsPriority", 0.2);
        config.put("topK", 5);
        config.put("threshold", 0.4);
        config.put("maxHistory", 0);
        config.put("useGraph", false);

        Map<String, Object> knowledgeBaseConfig = new LinkedHashMap<>();
        knowledgeBaseConfig.put("config", config);
        knowledgeBaseConfig.put("knowledgebases", Collections.emptyList());
        return knowledgeBaseConfig;
    }

    private Map<String, Object> modelConfig() {
        Map<String, Object> modelConfig = new LinkedHashMap<>();
        modelConfig.put("config", null);
        return modelConfig;
    }

    private Map<String, Object> rerankConfig() {
        Map<String, Object> rerankConfig = new LinkedHashMap<>();
        rerankConfig.put("modelId", "");
        return rerankConfig;
    }

    private Map<String, Object> avatar(AppRecord record) {
        Map<String, Object> avatar = new LinkedHashMap<>();
        avatar.put("key", defaultIfBlank(record.getAvatarKey(), ""));
        avatar.put("path", defaultIfBlank(record.getAvatarPath(), ""));
        return avatar;
    }

    private Map<String, Object> user(AppRecord record) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userId", record.getUserId());
        user.put("userName", "admin");
        return user;
    }

    private String formatMillis(Long millis) {
        if (millis == null) {
            return "";
        }
        return DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(millis));
    }

    private String newAssistantId() {
        return "assistant-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String newConversationId() {
        return "conversation-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String newDetailId() {
        return "detail-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String toJsonOrNull(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("assistant config is invalid", ex);
        }
    }

    private Map<String, Object> mapOrDefault(String json, Map<String, Object> defaultValue) {
        if (isBlank(json)) {
            return defaultValue;
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("assistant draft config is invalid", ex);
        }
    }

    private Map<String, Object> nullableMap(String json) {
        if (isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("assistant draft config is invalid", ex);
        }
    }

    private List<String> stringListOrDefault(String json, List<String> defaultValue) {
        if (isBlank(json)) {
            return defaultValue;
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("assistant draft config is invalid", ex);
        }
    }

    private List<Object> listOrDefault(String json) {
        if (isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, OBJECT_LIST_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("assistant conversation detail is invalid", ex);
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class VersionContext {
        private final String appId;
        private final String appType;
        private final String userId;
        private final String orgId;

        private VersionContext(String appId, String appType, String userId, String orgId) {
            this.appId = appId;
            this.appType = appType;
            this.userId = userId;
            this.orgId = orgId;
        }
    }

    private static class ConversationListContext {
        private final String assistantId;
        private final String conversationType;
        private final int pageNo;
        private final int pageSize;
        private final String userId;
        private final String orgId;

        private ConversationListContext(String assistantId,
                                        String conversationType,
                                        int pageNo,
                                        int pageSize,
                                        String userId,
                                        String orgId) {
            this.assistantId = assistantId;
            this.conversationType = conversationType;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            this.userId = userId;
            this.orgId = orgId;
        }
    }

    private static class ConversationDeleteContext {
        private final String userId;
        private final String orgId;
        private final AssistantConversationRecord conversation;

        private ConversationDeleteContext(String userId, String orgId, AssistantConversationRecord conversation) {
            this.userId = userId;
            this.orgId = orgId;
            this.conversation = conversation;
        }
    }
}
