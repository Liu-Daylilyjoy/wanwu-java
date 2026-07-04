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
import com.unicomai.wanwu.api.app.dto.AssistantResourceCommand;
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyCreateCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyDeleteCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.ApiKeyListQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyPageResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatusCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticChart;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticItem;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticLine;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticListResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticOverview;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticOverviewItem;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticPoint;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticRecordItem;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticRecordResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticTrend;
import com.unicomai.wanwu.api.app.dto.ApiKeyUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyCreateCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyInfo;
import com.unicomai.wanwu.api.app.dto.AppKeyListQuery;
import com.unicomai.wanwu.api.app.dto.AppStatisticItem;
import com.unicomai.wanwu.api.app.dto.AppStatisticListResult;
import com.unicomai.wanwu.api.app.dto.AppStatisticOverview;
import com.unicomai.wanwu.api.app.dto.AppStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.AppStatisticQuery;
import com.unicomai.wanwu.api.app.dto.AppStatisticResult;
import com.unicomai.wanwu.api.app.dto.AppStatisticTrend;
import com.unicomai.wanwu.api.app.dto.AppUrlCreateCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlInfo;
import com.unicomai.wanwu.api.app.dto.AppUrlListQuery;
import com.unicomai.wanwu.api.app.dto.AppUrlStatusCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlSuffixQuery;
import com.unicomai.wanwu.api.app.dto.AppUrlUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.AppVersionRollbackCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationInfoQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationChatCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteByIdCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationMessageListQuery;
import com.unicomai.wanwu.api.app.dto.ExplorationAppFavoriteCommand;
import com.unicomai.wanwu.api.app.dto.ExplorationAppHistoryCommand;
import com.unicomai.wanwu.api.app.dto.ModelStatisticItem;
import com.unicomai.wanwu.api.app.dto.ModelStatisticListResult;
import com.unicomai.wanwu.api.app.dto.ModelStatisticOverview;
import com.unicomai.wanwu.api.app.dto.ModelStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.ModelStatisticQuery;
import com.unicomai.wanwu.api.app.dto.ModelStatisticResult;
import com.unicomai.wanwu.api.app.dto.ModelStatisticTrend;
import com.unicomai.wanwu.api.app.dto.RagConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RagCopyCommand;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RagCreateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateResult;
import com.unicomai.wanwu.api.app.dto.RagDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.RagUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RecordApiKeyStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.api.app.dto.StatisticChart;
import com.unicomai.wanwu.api.app.dto.StatisticLine;
import com.unicomai.wanwu.api.app.dto.StatisticOverviewItem;
import com.unicomai.wanwu.api.app.dto.StatisticPoint;
import com.unicomai.wanwu.api.app.dto.WorkflowCopyCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateResult;
import com.unicomai.wanwu.api.app.dto.WorkflowDeleteCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowExportQuery;
import com.unicomai.wanwu.api.app.dto.WorkflowExportResult;
import com.unicomai.wanwu.api.app.dto.WorkflowImportCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.safety.SafetyService;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import com.unicomai.wanwu.service.app.domain.AssistantConversationMessageRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationRecord;
import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.AssistantSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyUsageAggregateRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyUsageRecord;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.AppFavoriteRecord;
import com.unicomai.wanwu.service.app.domain.AppHistoryRecord;
import com.unicomai.wanwu.service.app.domain.AppKeyRecord;
import com.unicomai.wanwu.service.app.domain.AppStatisticAggregateRecord;
import com.unicomai.wanwu.service.app.domain.AppUrlRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import com.unicomai.wanwu.service.app.domain.ModelStatisticAggregateRecord;
import com.unicomai.wanwu.service.app.domain.RagDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.RagSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowDraftRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowSnapshotRecord;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class AppServiceImpl implements AppService {

    private static final String APP_TYPE_AGENT = "agent";
    private static final String APP_TYPE_ASSISTANT = "assistant";
    private static final String APP_TYPE_RAG = "rag";
    private static final String APP_TYPE_WORKFLOW = "workflow";
    private static final String APP_TYPE_CHATFLOW = "chatflow";
    private static final String STAT_SOURCE_WEB = "web";
    private static final String STAT_SOURCE_OPENAPI = "openapi";
    private static final String STAT_SOURCE_WEB_URL = "webURL";
    private static final String PUBLISH_TYPE_UNPUBLISHED = "";
    private static final String PUBLISH_TYPE_PRIVATE = "private";
    private static final String PUBLISH_TYPE_ORGANIZATION = "organization";
    private static final String PUBLISH_TYPE_PUBLIC = "public";
    private static final String SEARCH_TYPE_ALL = "all";
    private static final String SEARCH_TYPE_PRIVATE = "private";
    private static final String SEARCH_TYPE_FAVORITE = "favorite";
    private static final String SEARCH_TYPE_HISTORY = "history";
    private static final String CONVERSATION_TYPE_PUBLISHED = "published";
    private static final String CONVERSATION_TYPE_DRAFT = "draft";
    private static final String CONVERSATION_TYPE_CHATFLOW_OPENAPI = "chatflow_openapi";
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
    private static final String DEFAULT_VERSION = "v1.0.0";
    private static final Pattern VERSION_PATTERN = Pattern.compile("^v\\d+\\.\\d+\\.\\d+$");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private static final TypeReference<List<Map<String, Object>>> MAP_LIST_TYPE =
            new TypeReference<List<Map<String, Object>>>() {
            };
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<List<String>>() {
    };
    private static final TypeReference<List<Object>> OBJECT_LIST_TYPE = new TypeReference<List<Object>>() {
    };
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Shanghai"));
    private static final DateTimeFormatter DATE_TIME_INPUT_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd");
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    private final ApplicationRepository applicationRepository;
    private final Clock clock;
    private final ObjectMapper objectMapper;
    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private KnowledgeService knowledgeService;
    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private SafetyService safetyService;

    @Autowired
    public AppServiceImpl(ApplicationRepository applicationRepository) {
        this(applicationRepository, Clock.systemUTC(), new ObjectMapper(), null, null);
    }

    public AppServiceImpl(ApplicationRepository applicationRepository, Clock clock) {
        this(applicationRepository, clock, new ObjectMapper(), null, null);
    }

    AppServiceImpl(ApplicationRepository applicationRepository, Clock clock, KnowledgeService knowledgeService) {
        this(applicationRepository, clock, new ObjectMapper(), knowledgeService, null);
    }

    AppServiceImpl(ApplicationRepository applicationRepository,
                   Clock clock,
                   KnowledgeService knowledgeService,
                   SafetyService safetyService) {
        this(applicationRepository, clock, new ObjectMapper(), knowledgeService, safetyService);
    }

    AppServiceImpl(ApplicationRepository applicationRepository, Clock clock, ObjectMapper objectMapper) {
        this(applicationRepository, clock, objectMapper, null, null);
    }

    AppServiceImpl(ApplicationRepository applicationRepository,
                   Clock clock,
                   ObjectMapper objectMapper,
                   KnowledgeService knowledgeService) {
        this(applicationRepository, clock, objectMapper, knowledgeService, null);
    }

    AppServiceImpl(ApplicationRepository applicationRepository,
                   Clock clock,
                   ObjectMapper objectMapper,
                   KnowledgeService knowledgeService,
                   SafetyService safetyService) {
        this.applicationRepository = applicationRepository;
        this.clock = clock;
        this.objectMapper = objectMapper;
        this.knowledgeService = knowledgeService;
        this.safetyService = safetyService;
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
        preserveAssistantResources(
                applicationRepository.findAssistantConfig(userId, orgId, command.getAssistantId()),
                record);
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
    public RagCreateResult createRag(RagCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("rag create command is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException("rag name is required");
        }
        long now = clock.millis();
        AppRecord record = new AppRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(defaultIfBlank(command.getUserId(), DEV_USER_ID));
        record.setOrgId(defaultIfBlank(command.getOrgId(), DEV_ORG_ID));
        record.setAppId(newRagId());
        record.setAppType(APP_TYPE_RAG);
        record.setPublishType(PUBLISH_TYPE_UNPUBLISHED);
        record.setName(command.getName().trim());
        record.setDesc(defaultIfBlank(command.getDesc(), ""));
        record.setAvatarKey(defaultIfBlank(command.getAvatarKey(), ""));
        record.setAvatarPath(defaultIfBlank(command.getAvatarPath(), ""));
        record.setCategory(0);
        applicationRepository.saveRag(record);
        return new RagCreateResult(record.getAppId());
    }

    @Override
    public void updateRag(RagUpdateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("rag update command is required");
        }
        if (isBlank(command.getRagId())) {
            throw new IllegalArgumentException("rag id is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException("rag name is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord existing = applicationRepository.findRag(userId, orgId, command.getRagId());
        if (existing == null) {
            throw new IllegalArgumentException("rag draft not found");
        }

        AppRecord record = new AppRecord();
        record.setId(existing.getId());
        record.setCreatedAt(existing.getCreatedAt());
        record.setUpdatedAt(clock.millis());
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setAppId(command.getRagId());
        record.setAppType(APP_TYPE_RAG);
        record.setPublishType(existing.getPublishType());
        record.setName(command.getName().trim());
        record.setDesc(defaultIfBlank(command.getDesc(), ""));
        record.setAvatarKey(defaultIfBlank(command.getAvatarKey(), ""));
        record.setAvatarPath(defaultIfBlank(command.getAvatarPath(), ""));
        record.setCategory(existing.getCategory() == null ? 0 : existing.getCategory());
        if (applicationRepository.updateRag(record) == null) {
            throw new IllegalArgumentException("rag draft not found");
        }
    }

    @Override
    public void updateRagConfig(RagConfigUpdateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("rag config command is required");
        }
        if (isBlank(command.getRagId())) {
            throw new IllegalArgumentException("rag id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        if (applicationRepository.findRag(userId, orgId, command.getRagId()) == null) {
            throw new IllegalArgumentException("rag draft not found");
        }

        long now = clock.millis();
        RagDraftConfigRecord record = new RagDraftConfigRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setRagId(command.getRagId());
        record.setModelConfigJson(toJsonOrNull(command.getModelConfig()));
        record.setRerankConfigJson(toJsonOrNull(command.getRerankConfig()));
        record.setQaRerankConfigJson(toJsonOrNull(command.getQaRerankConfig()));
        record.setKnowledgeBaseConfigJson(toJsonOrNull(command.getKnowledgeBaseConfig()));
        record.setQaKnowledgeBaseConfigJson(toJsonOrNull(command.getQaKnowledgeBaseConfig()));
        record.setSafetyConfigJson(toJsonOrNull(command.getSafetyConfig()));
        record.setVisionConfigJson(toJsonOrNull(command.getVisionConfig()));
        applicationRepository.saveRagConfig(record);
    }

    @Override
    public void deleteRag(RagDeleteCommand command) {
        if (command == null || isBlank(command.getRagId())) {
            throw new IllegalArgumentException("rag id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        if (applicationRepository.findRag(userId, orgId, command.getRagId()) == null) {
            throw new IllegalArgumentException("rag draft not found");
        }
        if (!applicationRepository.deleteRag(userId, orgId, command.getRagId())) {
            throw new IllegalArgumentException("rag draft not found");
        }
    }

    @Override
    public RagCreateResult copyRag(RagCopyCommand command) {
        if (command == null || isBlank(command.getRagId())) {
            throw new IllegalArgumentException("rag id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord source = applicationRepository.findRag(userId, orgId, command.getRagId());
        if (source == null) {
            throw new IllegalArgumentException("rag draft not found");
        }

        long now = clock.millis();
        String newRagId = newRagId();
        AppRecord copied = copyBaseRecord(source, newRagId, nextRagCopyName(userId, orgId, source.getName()), now);
        RagDraftConfigRecord copiedConfig = copyRagConfig(
                applicationRepository.findRagConfig(userId, orgId, command.getRagId()),
                userId,
                orgId,
                newRagId,
                now);
        applicationRepository.copyRag(copied, copiedConfig);
        return new RagCreateResult(newRagId);
    }

    @Override
    public void publishApp(AppPublishCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app publish command is required");
        }
        String appType = normalizeAppType(command.getAppType());
        if (isBlank(command.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String publishType = normalizePublishType(command.getPublishType(), PUBLISH_TYPE_PRIVATE);
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        if (APP_TYPE_RAG.equals(appType)) {
            publishRag(command, userId, orgId, publishType);
            return;
        }
        if (isWorkflowLike(appType)) {
            publishWorkflow(command, userId, orgId, publishType, appType);
            return;
        }
        if (!APP_TYPE_AGENT.equals(appType)) {
            throw new IllegalArgumentException("unsupported app type");
        }
        AppRecord record = applicationRepository.findAssistant(userId, orgId, command.getAppId());
        if (record == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }

        AssistantSnapshotRecord latest = applicationRepository.findLatestAssistantSnapshot(userId, orgId, command.getAppId());
        String version = isBlank(command.getVersion()) ? nextVersion(latestVersion(latest)) : command.getVersion().trim();
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
        String appType = normalizeAppType(command.getAppType());
        if (isBlank(command.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        if (APP_TYPE_RAG.equals(appType)) {
            if (!applicationRepository.updateRagPublishType(
                    userId, orgId, command.getAppId(), PUBLISH_TYPE_UNPUBLISHED, clock.millis())) {
                throw new IllegalArgumentException("rag draft not found");
            }
            return;
        }
        if (isWorkflowLike(appType)) {
            if (!applicationRepository.updateWorkflowPublishType(
                    userId, orgId, command.getAppId(), appType, PUBLISH_TYPE_UNPUBLISHED, clock.millis())) {
                throw new IllegalArgumentException(appType + " draft not found");
            }
            return;
        }
        if (!APP_TYPE_AGENT.equals(appType)) {
            throw new IllegalArgumentException("unsupported app type");
        }
        if (!applicationRepository.updateAssistantPublishType(
                userId, orgId, command.getAppId(), PUBLISH_TYPE_UNPUBLISHED, clock.millis())) {
            throw new IllegalArgumentException("assistant draft not found");
        }
    }

    @Override
    public AppVersionInfo getLatestAppVersion(AppVersionQuery query) {
        VersionContext context = versionContext(query);
        if (APP_TYPE_RAG.equals(context.appType)) {
            AppRecord record = applicationRepository.findRag(context.userId, context.orgId, context.appId);
            if (record == null) {
                throw new IllegalArgumentException("rag draft not found");
            }
            RagSnapshotRecord latest = applicationRepository.findLatestRagSnapshot(
                    context.userId, context.orgId, context.appId);
            if (latest == null) {
                return new AppVersionInfo("", "", "", defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
            }
            return toVersionInfo(latest, defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
        }
        if (isWorkflowLike(context.appType)) {
            AppRecord record = applicationRepository.findWorkflow(context.userId, context.orgId, context.appId, context.appType);
            if (record == null) {
                throw new IllegalArgumentException(context.appType + " draft not found");
            }
            WorkflowSnapshotRecord latest = applicationRepository.findLatestWorkflowSnapshot(
                    context.userId, context.orgId, context.appId);
            if (latest == null) {
                return new AppVersionInfo("", "", "", defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
            }
            return toVersionInfo(latest, defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
        }
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
        if (APP_TYPE_RAG.equals(context.appType)) {
            List<RagSnapshotRecord> snapshots = applicationRepository.listRagSnapshots(
                    context.userId, context.orgId, context.appId);
            List<AppVersionInfo> versions = new ArrayList<>(snapshots.size());
            for (RagSnapshotRecord snapshot : snapshots) {
                versions.add(toVersionInfo(snapshot, ""));
            }
            return new AppVersionListResult(versions, versions.size());
        }
        if (isWorkflowLike(context.appType)) {
            List<WorkflowSnapshotRecord> snapshots = applicationRepository.listWorkflowSnapshots(
                    context.userId, context.orgId, context.appId);
            List<AppVersionInfo> versions = new ArrayList<>(snapshots.size());
            for (WorkflowSnapshotRecord snapshot : snapshots) {
                versions.add(toVersionInfo(snapshot, ""));
            }
            return new AppVersionListResult(versions, versions.size());
        }
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
        if (APP_TYPE_RAG.equals(context.appType)) {
            if (!applicationRepository.updateLatestRagSnapshot(
                    context.userId, context.orgId, context.appId, defaultIfBlank(command.getDesc(), ""), now)) {
                throw new IllegalArgumentException("rag snapshot not found");
            }
            if (!applicationRepository.updateRagPublishType(context.userId, context.orgId, context.appId, publishType, now)) {
                throw new IllegalArgumentException("rag draft not found");
            }
            return;
        }
        if (isWorkflowLike(context.appType)) {
            if (!applicationRepository.updateLatestWorkflowSnapshot(
                    context.userId, context.orgId, context.appId, defaultIfBlank(command.getDesc(), ""), now)) {
                throw new IllegalArgumentException(context.appType + " snapshot not found");
            }
            if (!applicationRepository.updateWorkflowPublishType(context.userId, context.orgId, context.appId, context.appType, publishType, now)) {
                throw new IllegalArgumentException(context.appType + " draft not found");
            }
            return;
        }
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
        if (APP_TYPE_RAG.equals(context.appType)) {
            rollbackRagVersion(command, context);
            return;
        }
        if (isWorkflowLike(context.appType)) {
            rollbackWorkflowVersion(command, context);
            return;
        }
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
    public ApplicationListResult listApplications(ApplicationListQuery query) {
        String requestedType = query == null ? "" : defaultIfBlank(query.getAppType(), "");
        String appType = requestedType.isEmpty() ? "" : normalizeAppType(requestedType);
        String userId = query == null ? DEV_USER_ID : defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = query == null ? DEV_ORG_ID : defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        String name = query == null ? "" : defaultIfBlank(query.getName(), "");
        String searchType = query == null ? SEARCH_TYPE_ALL : defaultIfBlank(query.getSearchType(), SEARCH_TYPE_ALL);

        List<AppFavoriteRecord> favorites = applicationRepository.listAppFavorites(userId, appType);
        Set<String> favoriteKeys = favoriteKeys(favorites);
        Map<String, AppHistoryRecord> historiesByKey = new LinkedHashMap<String, AppHistoryRecord>();
        List<AppRecord> records;
        if (SEARCH_TYPE_HISTORY.equals(searchType)) {
            long oneMonthAgo = clock.millis() - 31L * 24L * 60L * 60L * 1000L;
            List<AppHistoryRecord> histories = applicationRepository.listAppHistories(userId, appType, oneMonthAgo);
            records = recordsFromHistories(userId, orgId, name, histories);
            for (AppHistoryRecord history : histories) {
                historiesByKey.put(appKey(history.getAppType(), history.getAppId()), history);
            }
        } else {
            records = listApplicationRecords(userId, orgId, name, appType);
            if (SEARCH_TYPE_FAVORITE.equals(searchType)) {
                records = favoriteRecords(records, favoriteKeys);
            } else if (SEARCH_TYPE_PRIVATE.equals(searchType)) {
                records = privateRecords(records);
            }
            records.sort(new java.util.Comparator<AppRecord>() {
                @Override
                public int compare(AppRecord left, AppRecord right) {
                    return Long.valueOf(defaultLong(right.getUpdatedAt())).compareTo(defaultLong(left.getUpdatedAt()));
                }
            });
        }

        List<Map<String, Object>> items = new ArrayList<>(records.size());
        for (AppRecord record : records) {
            Map<String, Object> item = toFrontendCard(record);
            item.put("isFavorite", favoriteKeys.contains(appKey(record.getAppType(), record.getAppId())));
            AppHistoryRecord history = historiesByKey.get(appKey(record.getAppType(), record.getAppId()));
            if (history != null) {
                item.put("visitedAt", formatMillis(history.getUpdatedAt()));
                item.put("historyCreatedAt", formatMillis(history.getCreatedAt()));
            }
            items.add(item);
        }
        return new ApplicationListResult(items, items.size());
    }

    @Override
    public void changeExplorationAppFavorite(ExplorationAppFavoriteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("favorite command is required");
        }
        if (isBlank(command.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        String appType = normalizeAppType(command.getAppType());
        if (findApp(userId, orgId, command.getAppId(), appType) == null) {
            throw new IllegalArgumentException("app not found");
        }
        if (!command.isFavorite()) {
            applicationRepository.deleteAppFavorite(userId, command.getAppId(), appType);
            return;
        }
        long now = clock.millis();
        AppFavoriteRecord record = new AppFavoriteRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setAppId(command.getAppId());
        record.setAppType(appType);
        applicationRepository.saveAppFavorite(record);
    }

    @Override
    public void recordAppHistory(ExplorationAppHistoryCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app history command is required");
        }
        if (isBlank(command.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        String appType = normalizeAppType(command.getAppType());
        if (findApp(userId, orgId, command.getAppId(), appType) == null) {
            throw new IllegalArgumentException("app not found");
        }
        long now = clock.millis();
        AppHistoryRecord record = new AppHistoryRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setAppId(command.getAppId());
        record.setAppType(appType);
        applicationRepository.saveAppHistory(record);
    }

    @Override
    public ApiKeyInfo createApiKey(ApiKeyCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("api key create command is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException("api key name is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        String name = command.getName().trim();
        if (applicationRepository.findApiKeyByName(userId, orgId, name) != null) {
            throw new IllegalArgumentException("api key name already exists");
        }

        long now = clock.millis();
        ApiKeyRecord record = new ApiKeyRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setKey(newApiKey());
        record.setName(name);
        record.setDescription(defaultIfBlank(command.getDesc(), ""));
        record.setExpiredAt(parseDateOnly(command.getExpiredAt()));
        record.setStatus(true);
        return toApiKeyInfo(applicationRepository.saveApiKey(record));
    }

    @Override
    public void updateApiKey(ApiKeyUpdateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("api key update command is required");
        }
        if (isBlank(command.getKeyId())) {
            throw new IllegalArgumentException("api key id is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException("api key name is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        Long id = parseLongId(command.getKeyId(), "api key id is invalid");
        ApiKeyRecord existing = applicationRepository.findApiKeyById(id);
        if (!ownedBy(existing, userId, orgId)) {
            throw new IllegalArgumentException("api key not found");
        }
        String name = command.getName().trim();
        ApiKeyRecord sameName = applicationRepository.findApiKeyByName(userId, orgId, name);
        if (sameName != null && !id.equals(sameName.getId())) {
            throw new IllegalArgumentException("api key name already exists");
        }

        ApiKeyRecord record = new ApiKeyRecord();
        record.setId(id);
        record.setCreatedAt(existing.getCreatedAt());
        record.setUpdatedAt(clock.millis());
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setKey(existing.getKey());
        record.setName(name);
        record.setDescription(defaultIfBlank(command.getDesc(), ""));
        record.setExpiredAt(parseDateOnly(command.getExpiredAt()));
        record.setStatus(existing.getStatus());
        if (applicationRepository.updateApiKey(record) == null) {
            throw new IllegalArgumentException("api key not found");
        }
    }

    @Override
    public void deleteApiKey(ApiKeyDeleteCommand command) {
        if (command == null || isBlank(command.getKeyId())) {
            throw new IllegalArgumentException("api key id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        Long id = parseLongId(command.getKeyId(), "api key id is invalid");
        if (!ownedBy(applicationRepository.findApiKeyById(id), userId, orgId)) {
            throw new IllegalArgumentException("api key not found");
        }
        if (!applicationRepository.deleteApiKey(id)) {
            throw new IllegalArgumentException("api key not found");
        }
    }

    @Override
    public void updateApiKeyStatus(ApiKeyStatusCommand command) {
        if (command == null || isBlank(command.getKeyId())) {
            throw new IllegalArgumentException("api key id is required");
        }
        if (command.getStatus() == null) {
            throw new IllegalArgumentException("api key status is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        Long id = parseLongId(command.getKeyId(), "api key id is invalid");
        if (!ownedBy(applicationRepository.findApiKeyById(id), userId, orgId)) {
            throw new IllegalArgumentException("api key not found");
        }
        if (!applicationRepository.updateApiKeyStatus(id, command.getStatus(), clock.millis())) {
            throw new IllegalArgumentException("api key not found");
        }
    }

    @Override
    public ApiKeyPageResult listApiKeys(ApiKeyListQuery query) {
        String userId = query == null ? DEV_USER_ID : defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = query == null ? DEV_ORG_ID : defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        int pageNo = normalizePageNo(query == null ? 1 : query.getPageNo());
        int pageSize = normalizePageSize(query == null ? 20 : query.getPageSize());
        List<ApiKeyRecord> records = applicationRepository.listApiKeys(userId, orgId, offset(pageNo, pageSize), pageSize);
        List<ApiKeyInfo> items = new ArrayList<>(records.size());
        for (ApiKeyRecord record : records) {
            items.add(toApiKeyInfo(record));
        }
        return new ApiKeyPageResult(items, applicationRepository.countApiKeys(userId, orgId), pageNo, pageSize);
    }

    @Override
    public ApiKeyInfo getApiKeyByKey(String key) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("api key is required");
        }
        ApiKeyRecord record = applicationRepository.findApiKeyByKey(key);
        if (record == null) {
            throw new IllegalArgumentException("api key not found");
        }
        return toApiKeyInfo(record);
    }

    @Override
    public void recordApiKeyStatistic(RecordApiKeyStatisticCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("api key statistic command is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        if (isBlank(command.getApiKeyId())) {
            throw new IllegalArgumentException("api key id is required");
        }
        if (isBlank(command.getMethodPath())) {
            throw new IllegalArgumentException("method path is required");
        }
        long now = clock.millis();
        long callTime = command.getCallTime() <= 0L ? now : command.getCallTime();
        String date = formatDateOnly(callTime);
        String status = defaultIfBlank(command.getHttpStatus(), "200");
        boolean success = "200".equals(status);

        ApiKeyUsageRecord record = new ApiKeyUsageRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setApiKeyId(command.getApiKeyId());
        record.setMethodPath(command.getMethodPath());
        record.setCallTime(callTime);
        record.setResponseStatus(status);
        record.setStream(command.isStream());
        record.setStreamCosts(command.getStreamCosts());
        record.setNonStreamCosts(command.getNonStreamCosts());
        record.setRequestBody(defaultIfBlank(command.getRequestBody(), ""));
        record.setResponseBody(defaultIfBlank(command.getResponseBody(), ""));
        record.setDate(date);

        ApiKeyUsageAggregateRecord aggregate = new ApiKeyUsageAggregateRecord();
        aggregate.setCreatedAt(now);
        aggregate.setUpdatedAt(now);
        aggregate.setUserId(userId);
        aggregate.setOrgId(orgId);
        aggregate.setApiKeyId(command.getApiKeyId());
        aggregate.setMethodPath(command.getMethodPath());
        aggregate.setDate(date);
        aggregate.setCallCount(1L);
        aggregate.setCallFailure(success ? 0L : 1L);
        aggregate.setStreamCount(command.isStream() ? 1L : 0L);
        aggregate.setNonStreamCount(command.isStream() ? 0L : 1L);
        aggregate.setStreamFailure(command.isStream() && !success ? 1L : 0L);
        aggregate.setNonStreamFailure(!command.isStream() && !success ? 1L : 0L);
        aggregate.setStreamCosts(command.getStreamCosts());
        aggregate.setNonStreamCosts(command.getNonStreamCosts());
        applicationRepository.recordApiKeyUsage(record, aggregate);
    }

    @Override
    public ApiKeyStatisticResult getApiKeyStatistic(ApiKeyStatisticQuery query) {
        ApiKeyQueryContext ctx = apiKeyQuery(query);
        ApiKeyUsageAggregateRecord current = applicationRepository.sumApiKeyUsage(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.apiKeyIds, ctx.methodPaths);
        ApiKeyUsageAggregateRecord previous = applicationRepository.sumApiKeyUsage(
                ctx.userId, ctx.orgId, ctx.previousStartDate, ctx.previousEndDate, ctx.apiKeyIds, ctx.methodPaths);

        ApiKeyStatisticResult result = new ApiKeyStatisticResult();
        result.setOverview(toStatisticOverview(current, previous));
        result.setTrend(toStatisticTrend(applicationRepository.listApiKeyUsageTrend(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.apiKeyIds, ctx.methodPaths),
                ctx.dates));
        return result;
    }

    @Override
    public ApiKeyStatisticListResult listApiKeyStatistics(ApiKeyStatisticPageQuery query) {
        ApiKeyQueryContext ctx = apiKeyQuery(query);
        int pageNo = normalizePageNo(query == null ? 1 : query.getPageNo());
        int pageSize = normalizePageSize(query == null ? 10 : query.getPageSize());
        List<ApiKeyUsageAggregateRecord> records = applicationRepository.listApiKeyUsageAggregates(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.apiKeyIds, ctx.methodPaths,
                offset(pageNo, pageSize), pageSize);

        List<ApiKeyStatisticItem> items = new ArrayList<ApiKeyStatisticItem>(records.size());
        for (ApiKeyUsageAggregateRecord record : records) {
            items.add(toStatisticItem(record));
        }
        ApiKeyStatisticListResult result = new ApiKeyStatisticListResult();
        result.setList(items);
        result.setTotal(applicationRepository.countApiKeyUsageAggregates(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.apiKeyIds, ctx.methodPaths));
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public ApiKeyStatisticRecordResult listApiKeyStatisticRecords(ApiKeyStatisticPageQuery query) {
        ApiKeyQueryContext ctx = apiKeyQuery(query);
        int pageNo = normalizePageNo(query == null ? 1 : query.getPageNo());
        int pageSize = normalizePageSize(query == null ? 10 : query.getPageSize());
        List<ApiKeyUsageRecord> records = applicationRepository.listApiKeyUsageRecords(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.apiKeyIds, ctx.methodPaths,
                offset(pageNo, pageSize), pageSize);

        List<ApiKeyStatisticRecordItem> items = new ArrayList<ApiKeyStatisticRecordItem>(records.size());
        for (ApiKeyUsageRecord record : records) {
            items.add(toStatisticRecordItem(record));
        }
        ApiKeyStatisticRecordResult result = new ApiKeyStatisticRecordResult();
        result.setList(items);
        result.setTotal(applicationRepository.countApiKeyUsageRecords(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.apiKeyIds, ctx.methodPaths));
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public void recordAppStatistic(RecordAppStatisticCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app statistic command is required");
        }
        if (isBlank(command.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        String appType = normalizeAppType(command.getAppType());
        long now = clock.millis();
        long callTime = command.getCallTime() <= 0L ? now : command.getCallTime();

        AppStatisticAggregateRecord aggregate = new AppStatisticAggregateRecord();
        aggregate.setCreatedAt(now);
        aggregate.setUpdatedAt(now);
        aggregate.setUserId(userId);
        aggregate.setOrgId(orgId);
        aggregate.setAppId(command.getAppId());
        aggregate.setAppType(appType);
        aggregate.setDate(formatDateOnly(callTime));
        aggregate.setCallCount(1L);
        aggregate.setCallFailure(command.isSuccess() ? 0L : 1L);
        aggregate.setStreamCount(command.isStream() ? 1L : 0L);
        aggregate.setStreamFailure(command.isStream() && !command.isSuccess() ? 1L : 0L);
        aggregate.setStreamCosts(command.getStreamCosts());
        aggregate.setNonStreamCount(command.isStream() ? 0L : 1L);
        aggregate.setNonStreamFailure(!command.isStream() && !command.isSuccess() ? 1L : 0L);
        aggregate.setNonStreamCosts(command.getNonStreamCosts());
        String source = defaultIfBlank(command.getSource(), STAT_SOURCE_WEB);
        if (STAT_SOURCE_OPENAPI.equals(source)) {
            aggregate.setOpenapiCallCount(1L);
            aggregate.setOpenapiCallFailure(command.isSuccess() ? 0L : 1L);
        } else if (STAT_SOURCE_WEB_URL.equals(source)) {
            aggregate.setWebUrlCallCount(1L);
            aggregate.setWebUrlCallFailure(command.isSuccess() ? 0L : 1L);
        } else {
            aggregate.setWebCallCount(1L);
            aggregate.setWebCallFailure(command.isSuccess() ? 0L : 1L);
        }
        applicationRepository.recordAppStatistic(aggregate);
    }

    @Override
    public AppStatisticResult getAppStatistic(AppStatisticQuery query) {
        StatisticQueryContext ctx = appStatisticQuery(query);
        AppStatisticAggregateRecord current = applicationRepository.sumAppStatistic(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.ids, ctx.type);
        AppStatisticAggregateRecord previous = applicationRepository.sumAppStatistic(
                ctx.userId, ctx.orgId, ctx.previousStartDate, ctx.previousEndDate, ctx.ids, ctx.type);
        AppStatisticResult result = new AppStatisticResult();
        result.setOverview(toAppStatisticOverview(current, previous));
        result.setTrend(toAppStatisticTrend(applicationRepository.listAppStatisticTrend(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.ids, ctx.type), ctx.dates, ctx.type));
        return result;
    }

    @Override
    public AppStatisticListResult listAppStatistics(AppStatisticPageQuery query) {
        StatisticQueryContext ctx = appStatisticQuery(query);
        int pageNo = normalizePageNo(query == null ? 1 : query.getPageNo());
        int pageSize = normalizePageSize(query == null ? 10 : query.getPageSize());
        List<AppStatisticAggregateRecord> records = applicationRepository.listAppStatisticAggregates(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.ids, ctx.type, offset(pageNo, pageSize), pageSize);
        List<AppStatisticItem> items = new ArrayList<AppStatisticItem>(records.size());
        for (AppStatisticAggregateRecord record : records) {
            items.add(toAppStatisticItem(record));
        }
        AppStatisticListResult result = new AppStatisticListResult();
        result.setList(items);
        result.setTotal(applicationRepository.countAppStatisticAggregates(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.ids, ctx.type));
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public void recordModelStatistic(RecordModelStatisticCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("model statistic command is required");
        }
        if (isBlank(command.getModelId())) {
            throw new IllegalArgumentException("model id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        long now = clock.millis();
        long callTime = command.getCallTime() <= 0L ? now : command.getCallTime();

        ModelStatisticAggregateRecord aggregate = new ModelStatisticAggregateRecord();
        aggregate.setCreatedAt(now);
        aggregate.setUpdatedAt(now);
        aggregate.setUserId(userId);
        aggregate.setOrgId(orgId);
        aggregate.setModelId(command.getModelId());
        aggregate.setModel(defaultIfBlank(command.getModel(), command.getModelId()));
        aggregate.setProvider(defaultIfBlank(command.getProvider(), ""));
        aggregate.setModelType(defaultIfBlank(command.getModelType(), "llm"));
        aggregate.setDate(formatDateOnly(callTime));
        aggregate.setPromptTokens(command.getPromptTokens());
        aggregate.setCompletionTokens(command.getCompletionTokens());
        aggregate.setTotalTokens(command.getTotalTokens());
        aggregate.setFirstTokenLatency(command.getFirstTokenLatency());
        aggregate.setCosts(command.getCosts());
        aggregate.setCallCount(1L);
        aggregate.setStreamCount(command.isStream() ? 1L : 0L);
        aggregate.setNonStreamCount(command.isStream() ? 0L : 1L);
        aggregate.setCallFailure(command.isSuccess() ? 0L : 1L);
        aggregate.setStreamFailure(command.isStream() && !command.isSuccess() ? 1L : 0L);
        aggregate.setNonStreamFailure(!command.isStream() && !command.isSuccess() ? 1L : 0L);
        applicationRepository.recordModelStatistic(aggregate);
    }

    @Override
    public ModelStatisticResult getModelStatistic(ModelStatisticQuery query) {
        StatisticQueryContext ctx = modelStatisticQuery(query);
        ModelStatisticAggregateRecord current = applicationRepository.sumModelStatistic(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.ids, ctx.type);
        ModelStatisticAggregateRecord previous = applicationRepository.sumModelStatistic(
                ctx.userId, ctx.orgId, ctx.previousStartDate, ctx.previousEndDate, ctx.ids, ctx.type);
        ModelStatisticResult result = new ModelStatisticResult();
        result.setOverview(toModelStatisticOverview(current, previous));
        result.setTrend(toModelStatisticTrend(applicationRepository.listModelStatisticTrend(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.ids, ctx.type), ctx.dates));
        return result;
    }

    @Override
    public ModelStatisticListResult listModelStatistics(ModelStatisticPageQuery query) {
        StatisticQueryContext ctx = modelStatisticQuery(query);
        int pageNo = normalizePageNo(query == null ? 1 : query.getPageNo());
        int pageSize = normalizePageSize(query == null ? 10 : query.getPageSize());
        List<ModelStatisticAggregateRecord> records = applicationRepository.listModelStatisticAggregates(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.ids, ctx.type, offset(pageNo, pageSize), pageSize);
        List<ModelStatisticItem> items = new ArrayList<ModelStatisticItem>(records.size());
        for (ModelStatisticAggregateRecord record : records) {
            items.add(toModelStatisticItem(record));
        }
        ModelStatisticListResult result = new ModelStatisticListResult();
        result.setList(items);
        result.setTotal(applicationRepository.countModelStatisticAggregates(
                ctx.userId, ctx.orgId, ctx.startDate, ctx.endDate, ctx.ids, ctx.type));
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public AppKeyInfo createAppKey(AppKeyCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app key create command is required");
        }
        if (isBlank(command.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String appType = normalizeAgentAppType(command.getAppType());
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        long now = clock.millis();
        AppKeyRecord record = new AppKeyRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setAppId(command.getAppId());
        record.setAppType(appType);
        record.setApiKey(newApiKey());
        return toAppKeyInfo(applicationRepository.saveAppKey(record));
    }

    @Override
    public void deleteAppKey(AppKeyDeleteCommand command) {
        if (command == null || isBlank(command.getApiId())) {
            throw new IllegalArgumentException("app key id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        Long id = parseLongId(command.getApiId(), "app key id is invalid");
        if (!ownedBy(applicationRepository.findAppKeyById(id), userId, orgId)) {
            throw new IllegalArgumentException("app key not found");
        }
        if (!applicationRepository.deleteAppKey(id)) {
            throw new IllegalArgumentException("app key not found");
        }
    }

    @Override
    public List<AppKeyInfo> listAppKeys(AppKeyListQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("app key list query is required");
        }
        if (isBlank(query.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        String appType = normalizeAgentAppType(query.getAppType());
        List<AppKeyRecord> records = applicationRepository.listAppKeys(userId, orgId, query.getAppId(), appType);
        List<AppKeyInfo> result = new ArrayList<>(records.size());
        for (AppKeyRecord record : records) {
            result.add(toAppKeyInfo(record));
        }
        return result;
    }

    @Override
    public AppKeyInfo getAppKeyByKey(String appKey) {
        if (isBlank(appKey)) {
            throw new IllegalArgumentException("app key is required");
        }
        AppKeyRecord record = applicationRepository.findAppKeyByKey(appKey);
        if (record == null) {
            throw new IllegalArgumentException("app key not found");
        }
        return toAppKeyInfo(record);
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
    public Map<String, Object> getRagDraft(RagDetailQuery query) {
        if (query == null || isBlank(query.getRagId())) {
            throw new IllegalArgumentException("rag id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        AppRecord record = applicationRepository.findRag(userId, orgId, query.getRagId());
        if (record == null) {
            throw new IllegalArgumentException("rag draft not found");
        }
        RagDraftConfigRecord config = applicationRepository.findRagConfig(userId, orgId, query.getRagId());
        return toFrontendRag(record, config);
    }

    @Override
    public Map<String, Object> getPublishedRag(RagDetailQuery query) {
        if (query == null || isBlank(query.getRagId())) {
            throw new IllegalArgumentException("rag id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        RagSnapshotRecord snapshot;
        if (isBlank(query.getVersion())) {
            snapshot = applicationRepository.findLatestRagSnapshot(userId, orgId, query.getRagId());
        } else {
            snapshot = applicationRepository.findRagSnapshotByVersion(userId, orgId, query.getRagId(), query.getVersion());
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("rag snapshot not found");
        }
        Map<String, Object> rag = mapOrDefault(snapshot.getRagInfoJson(), new LinkedHashMap<String, Object>());
        AppRecord record = applicationRepository.findRag(userId, orgId, query.getRagId());
        rag.put("ragId", query.getRagId());
        rag.put("uuid", query.getRagId());
        rag.put("publishType", record == null ? PUBLISH_TYPE_UNPUBLISHED : defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
        Map<String, Object> publish = mapValue(rag.get("appPublishConfig"));
        publish.put("publishType", record == null ? PUBLISH_TYPE_UNPUBLISHED : defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
        rag.put("appPublishConfig", publish);
        return rag;
    }

    @Override
    public RagChatResult streamRagChat(RagChatCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("rag chat command is required");
        }
        if (isBlank(command.getRagId())) {
            throw new IllegalArgumentException("rag id is required");
        }
        if (isBlank(command.getQuestion())) {
            throw new IllegalArgumentException("rag question is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord rag = applicationRepository.findRag(userId, orgId, command.getRagId());
        if (rag == null) {
            throw new IllegalArgumentException("rag draft not found");
        }
        RagSnapshotRecord snapshot = null;
        if (!command.isDraft()) {
            snapshot = applicationRepository.findLatestRagSnapshot(userId, orgId, command.getRagId());
        }
        if (!command.isDraft() && snapshot == null) {
            throw new IllegalArgumentException("rag snapshot not found");
        }
        RagDraftConfigRecord config = ragConfigForChat(command.isDraft(), userId, orgId, command.getRagId(), snapshot);
        String safetyConfigJson = config == null ? null : config.getSafetyConfigJson();
        SensitiveBlock sensitiveBlock = matchSensitiveResponse(
                userId,
                orgId,
                safetyConfigJson,
                command.getQuestion());
        if (sensitiveBlock != null) {
            RagChatResult result = new RagChatResult();
            result.setRagId(command.getRagId());
            result.setQuestion(command.getQuestion());
            result.setResponse(sensitiveBlock.reply);
            result.setSearchList(Collections.<Map<String, Object>>emptyList());
            result.setQaSearchList(Collections.<Map<String, Object>>emptyList());
            result.setCreatedAt(clock.millis());
            return result;
        }
        Map<String, Object> knowledgeHit = hitConfiguredKnowledge(
                userId,
                orgId,
                command.getQuestion(),
                config == null ? null : config.getKnowledgeBaseConfigJson(),
                false);
        Map<String, Object> qaHit = hitConfiguredKnowledge(
                userId,
                orgId,
                command.getQuestion(),
                config == null ? null : config.getQaKnowledgeBaseConfigJson(),
                true);
        List<Map<String, Object>> searchList = hitSearchList(knowledgeHit);
        List<Map<String, Object>> qaSearchList = hitSearchList(qaHit);
        RagChatResult result = new RagChatResult();
        result.setRagId(command.getRagId());
        result.setQuestion(command.getQuestion());
        String response = deterministicRagResponse(rag, command.getQuestion(), command.getFileInfo());
        response = enrichRagResponse(response, knowledgeHit, searchList, qaHit, qaSearchList);
        SensitiveBlock outputBlock = matchSensitiveResponse(userId, orgId, safetyConfigJson, response);
        if (outputBlock != null) {
            response = outputBlock.reply;
            searchList = Collections.emptyList();
            qaSearchList = Collections.emptyList();
        }
        result.setResponse(response);
        result.setSearchList(searchList);
        result.setQaSearchList(qaSearchList);
        result.setCreatedAt(clock.millis());
        return result;
    }

    @Override
    public WorkflowCreateResult createWorkflow(WorkflowCreateCommand command) {
        return createFlow(command, APP_TYPE_WORKFLOW);
    }

    @Override
    public WorkflowCreateResult createChatflow(WorkflowCreateCommand command) {
        return createFlow(command, APP_TYPE_CHATFLOW);
    }

    private WorkflowCreateResult createFlow(WorkflowCreateCommand command, String appType) {
        if (command == null) {
            throw new IllegalArgumentException(appType + " create command is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException(appType + " name is required");
        }
        long now = clock.millis();
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        String workflowId = newFlowId(appType);

        AppRecord record = new AppRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setAppId(workflowId);
        record.setAppType(appType);
        record.setPublishType(PUBLISH_TYPE_UNPUBLISHED);
        record.setName(command.getName().trim());
        record.setDesc(defaultIfBlank(command.getDesc(), ""));
        record.setAvatarKey(defaultIfBlank(command.getAvatarKey(), ""));
        record.setAvatarPath(defaultIfBlank(command.getAvatarPath(), ""));
        record.setCategory(0);

        WorkflowDraftRecord draft = workflowDraft(userId, orgId, workflowId, now,
                defaultIfBlank(command.getSchema(), defaultWorkflowSchema(workflowId)));
        applicationRepository.saveWorkflow(record, draft);
        return new WorkflowCreateResult(workflowId);
    }

    @Override
    public WorkflowCreateResult importWorkflow(WorkflowImportCommand command) {
        return importFlow(command, APP_TYPE_WORKFLOW);
    }

    @Override
    public WorkflowCreateResult importChatflow(WorkflowImportCommand command) {
        return importFlow(command, APP_TYPE_CHATFLOW);
    }

    private WorkflowCreateResult importFlow(WorkflowImportCommand command, String appType) {
        if (command == null) {
            throw new IllegalArgumentException(appType + " import command is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException(appType + " name is required");
        }
        if (isBlank(command.getDesc())) {
            throw new IllegalArgumentException(appType + " desc is required");
        }
        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName(command.getName());
        create.setDesc(command.getDesc());
        create.setSchema(command.getSchema());
        create.setUserId(command.getUserId());
        create.setOrgId(command.getOrgId());
        return createFlow(create, appType);
    }

    @Override
    public WorkflowCreateResult copyWorkflow(WorkflowCopyCommand command) {
        return copyFlow(command, APP_TYPE_WORKFLOW);
    }

    @Override
    public WorkflowCreateResult copyChatflow(WorkflowCopyCommand command) {
        return copyFlow(command, APP_TYPE_CHATFLOW);
    }

    private WorkflowCreateResult copyFlow(WorkflowCopyCommand command, String appType) {
        if (command == null || isBlank(command.getWorkflowId())) {
            throw new IllegalArgumentException(appType + " id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord source = applicationRepository.findWorkflow(userId, orgId, command.getWorkflowId(), appType);
        if (source == null) {
            throw new IllegalArgumentException(appType + " draft not found");
        }
        if (command.isNeedPublished()
                && applicationRepository.findLatestWorkflowSnapshot(userId, orgId, command.getWorkflowId()) == null) {
            throw new IllegalArgumentException(appType + " snapshot not found");
        }

        long now = clock.millis();
        String newWorkflowId = newFlowId(appType);
        AppRecord copied = copyBaseRecord(source, newWorkflowId,
                nextWorkflowCopyName(userId, orgId, source.getName(), appType), now);
        WorkflowDraftRecord sourceDraft = applicationRepository.findWorkflowDraft(userId, orgId, command.getWorkflowId());
        WorkflowDraftRecord copiedDraft = copyWorkflowDraft(sourceDraft, userId, orgId, newWorkflowId, now);
        applicationRepository.copyWorkflow(copied, copiedDraft);
        return new WorkflowCreateResult(newWorkflowId);
    }

    @Override
    public void deleteWorkflow(WorkflowDeleteCommand command) {
        deleteFlow(command, APP_TYPE_WORKFLOW);
    }

    @Override
    public void deleteChatflow(WorkflowDeleteCommand command) {
        deleteFlow(command, APP_TYPE_CHATFLOW);
    }

    private void deleteFlow(WorkflowDeleteCommand command, String appType) {
        if (command == null || isBlank(command.getWorkflowId())) {
            throw new IllegalArgumentException(appType + " id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        if (!applicationRepository.deleteWorkflow(userId, orgId, command.getWorkflowId(), appType)) {
            throw new IllegalArgumentException(appType + " draft not found");
        }
    }

    @Override
    public WorkflowExportResult exportWorkflow(WorkflowExportQuery query) {
        return exportFlow(query, APP_TYPE_WORKFLOW);
    }

    @Override
    public WorkflowExportResult exportChatflow(WorkflowExportQuery query) {
        return exportFlow(query, APP_TYPE_CHATFLOW);
    }

    private WorkflowExportResult exportFlow(WorkflowExportQuery query, String appType) {
        if (query == null || isBlank(query.getWorkflowId())) {
            throw new IllegalArgumentException(appType + " id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        AppRecord workflow = applicationRepository.findWorkflow(userId, orgId, query.getWorkflowId(), appType);
        if (workflow == null) {
            throw new IllegalArgumentException(appType + " draft not found");
        }
        if (query.isPublished() || !isBlank(query.getVersion())) {
            WorkflowSnapshotRecord snapshot = isBlank(query.getVersion())
                    ? applicationRepository.findLatestWorkflowSnapshot(userId, orgId, query.getWorkflowId())
                    : applicationRepository.findWorkflowSnapshotByVersion(userId, orgId, query.getWorkflowId(), query.getVersion());
            if (snapshot == null) {
                throw new IllegalArgumentException(appType + " snapshot not found");
            }
            Map<String, Object> snapshotInfo = mapOrDefault(snapshot.getWorkflowInfoJson(), new LinkedHashMap<String, Object>());
            return new WorkflowExportResult(
                    defaultIfBlank((String) snapshotInfo.get("name"), workflow.getName()),
                    defaultIfBlank((String) snapshotInfo.get("desc"), workflow.getDesc()),
                    defaultIfBlank(snapshot.getWorkflowSchemaJson(), defaultWorkflowSchema(query.getWorkflowId())));
        }
        WorkflowDraftRecord draft = applicationRepository.findWorkflowDraft(userId, orgId, query.getWorkflowId());
        return new WorkflowExportResult(
                workflow.getName(),
                defaultIfBlank(workflow.getDesc(), ""),
                draft == null ? defaultWorkflowSchema(query.getWorkflowId()) : defaultIfBlank(draft.getSchemaJson(), defaultWorkflowSchema(query.getWorkflowId())));
    }

    @Override
    public WorkflowRunResult runWorkflow(WorkflowRunCommand command) {
        if (command == null || isBlank(command.getWorkflowId())) {
            throw new IllegalArgumentException("workflow id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        if (applicationRepository.findWorkflow(userId, orgId, command.getWorkflowId()) == null) {
            throw new IllegalArgumentException("workflow draft not found");
        }
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("workflowId", command.getWorkflowId());
        output.put("status", "success");
        output.put("message", "Demo workflow response");
        Map<String, Object> input = command.getInput() == null
                ? Collections.<String, Object>emptyMap()
                : command.getInput();
        output.putAll(input);
        return new WorkflowRunResult(command.getWorkflowId(), output);
    }

    @Override
    public Map<String, Object> listChatflowApplications(ChatflowApplicationListQuery query) {
        if (query == null || isBlank(query.getWorkflowId())) {
            throw new IllegalArgumentException("chatflow workflow id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        AppRecord chatflow = applicationRepository.findWorkflow(userId, orgId, query.getWorkflowId(), APP_TYPE_CHATFLOW);
        if (chatflow == null) {
            throw new IllegalArgumentException("chatflow draft not found");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("intelligences", Collections.singletonList(chatflowIntelligence(chatflow)));
        result.put("total", 1);
        result.put("has_more", false);
        result.put("next_cursor_id", "");
        return result;
    }

    @Override
    public Map<String, Object> getChatflowApplication(ChatflowApplicationInfoQuery query) {
        if (query == null || isBlank(query.getIntelligenceId())) {
            throw new IllegalArgumentException("chatflow intelligence id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        AppRecord chatflow = findChatflowByApplicationId(userId, orgId, query.getIntelligenceId());
        if (chatflow == null) {
            throw new IllegalArgumentException("chatflow application not found");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("intelligence_type", query.getIntelligenceType() == null ? 1L : query.getIntelligenceType());
        result.put("basic_info", chatflowBasicInfo(chatflow));
        result.put("publish_info", chatflowPublishInfo(chatflow));
        result.put("owner_info", chatflowOwnerInfo(chatflow));
        return result;
    }

    @Override
    public void deleteChatflowConversation(ChatflowConversationDeleteCommand command) {
        if (command == null || isBlank(command.getProjectId())) {
            throw new IllegalArgumentException("chatflow project id is required");
        }
        if (isBlank(command.getUniqueId())) {
            throw new IllegalArgumentException("chatflow unique id is required");
        }
    }

    @Override
    public Map<String, Object> createChatflowOpenApiConversation(ChatflowConversationCreateCommand command) {
        if (command == null || isBlank(command.getChatflowId())) {
            throw new IllegalArgumentException("chatflow id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord chatflow = ensureChatflowExists(userId, orgId, command.getChatflowId());
        String conversationName = defaultIfBlank(command.getConversationName(),
                defaultIfBlank(chatflow.getName(), "New conversation"));
        AssistantConversationRecord record = newConversation(
                userId, orgId, chatflow.getAppId(), CONVERSATION_TYPE_CHATFLOW_OPENAPI, conversationName);
        record.setTitle(conversationName);
        applicationRepository.saveConversation(record);
        return chatflowConversationInfo(record);
    }

    @Override
    public Map<String, Object> listChatflowOpenApiConversations(ChatflowConversationListQuery query) {
        if (query == null || isBlank(query.getChatflowId())) {
            throw new IllegalArgumentException("chatflow id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        AppRecord chatflow = ensureChatflowExists(userId, orgId, query.getChatflowId());
        int pageNo = normalizePageNo(query.getPageNo());
        int pageSize = normalizePageSize(defaultInt(query.getPageSize(), 1000));
        List<AssistantConversationRecord> records = applicationRepository.listConversations(
                userId, orgId, chatflow.getAppId(), CONVERSATION_TYPE_CHATFLOW_OPENAPI, offset(pageNo, pageSize), pageSize);
        long total = applicationRepository.countConversations(
                userId, orgId, chatflow.getAppId(), CONVERSATION_TYPE_CHATFLOW_OPENAPI);
        List<Map<String, Object>> rows = new ArrayList<>(records.size());
        for (AssistantConversationRecord record : records) {
            rows.add(chatflowConversationInfo(record));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("conversations", rows);
        result.put("list", rows);
        result.put("total", total);
        return result;
    }

    @Override
    public Map<String, Object> listChatflowOpenApiConversationMessages(ChatflowConversationMessageListQuery query) {
        if (query == null || isBlank(query.getConversationId())) {
            throw new IllegalArgumentException("conversation id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        AssistantConversationRecord conversation = ensureChatflowOpenApiConversation(
                userId, orgId, query.getChatflowId(), query.getConversationId());
        int limit = normalizePageSize(defaultInt(query.getLimit(), 50));
        long totalTurns = applicationRepository.countConversationMessages(userId, orgId, conversation.getConversationId());
        int offset = (int) Math.max(0L, totalTurns - limit);
        List<AssistantConversationMessageRecord> records = applicationRepository.listConversationMessages(
                userId, orgId, conversation.getConversationId(), offset, limit);
        List<Map<String, Object>> messages = new ArrayList<>(records.size() * 2);
        for (int i = 0; i < records.size(); i++) {
            messages.add(chatflowOpenApiMessage(records.get(i), "user", i));
            messages.add(chatflowOpenApiMessage(records.get(i), "assistant", i));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", messages);
        result.put("has_more", totalTurns > records.size());
        result.put("first_id", messages.isEmpty() ? 0 : messages.get(0).get("id"));
        result.put("last_id", messages.isEmpty() ? 0 : messages.get(messages.size() - 1).get("id"));
        return result;
    }

    @Override
    public Map<String, Object> chatflowOpenApiChat(ChatflowConversationChatCommand command) {
        if (command == null || isBlank(command.getChatflowId())) {
            throw new IllegalArgumentException("chatflow id is required");
        }
        if (isBlank(command.getConversationId())) {
            throw new IllegalArgumentException("conversation id is required");
        }
        if (isBlank(command.getQuery())) {
            throw new IllegalArgumentException("query is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AssistantConversationRecord conversation = ensureChatflowOpenApiConversation(
                userId, orgId, command.getChatflowId(), command.getConversationId());
        long now = clock.millis();
        AssistantConversationMessageRecord message = new AssistantConversationMessageRecord();
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        message.setUserId(userId);
        message.setOrgId(orgId);
        message.setAssistantId(conversation.getAssistantId());
        message.setConversationId(conversation.getConversationId());
        message.setDetailId(newDetailId());
        message.setPrompt(command.getQuery());
        message.setSysPrompt("");
        message.setResponse("Chatflow response: " + command.getQuery());
        message.setResponseListJson(toJsonOrNull(Collections.emptyList()));
        message.setSearchListJson(toJsonOrNull(Collections.emptyList()));
        message.setRequestFilesJson(toJsonOrNull(command.getParameters()));
        message.setResponseFilesJson(toJsonOrNull(Collections.emptyList()));
        message.setSubConversationListJson(toJsonOrNull(Collections.emptyList()));
        message.setFileSize(0L);
        message.setFileName("");
        message.setQaType(0);
        applicationRepository.saveConversationMessage(message);
        applicationRepository.touchConversation(userId, orgId, conversation.getConversationId(), now);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("conversation_id", conversation.getConversationId());
        result.put("response", message.getResponse());
        result.put("finish", 1);
        return result;
    }

    @Override
    public void deleteChatflowOpenApiConversation(ChatflowConversationDeleteByIdCommand command) {
        if (command == null || isBlank(command.getConversationId())) {
            throw new IllegalArgumentException("conversation id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AssistantConversationRecord conversation = ensureChatflowOpenApiConversation(
                userId, orgId, command.getChatflowId(), command.getConversationId());
        applicationRepository.deleteConversation(userId, orgId, conversation.getConversationId());
    }

    @Override
    public void addAssistantWorkflow(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getWorkflowInfosJson());
        upsertResource(list, workflowItem(command), "workFlowId");
        config.setWorkflowInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void deleteAssistantWorkflow(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getWorkflowInfosJson());
        removeResource(list, command, "workFlowId");
        config.setWorkflowInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void switchAssistantWorkflow(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getWorkflowInfosJson());
        switchResource(list, command, "workFlowId");
        config.setWorkflowInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void addAssistantMcp(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getMcpInfosJson());
        upsertResource(list, mcpItem(command), "mcpId", "mcpType", "actionName");
        config.setMcpInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void deleteAssistantMcp(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getMcpInfosJson());
        removeResource(list, command, "mcpId", "mcpType", "actionName");
        config.setMcpInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void switchAssistantMcp(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getMcpInfosJson());
        switchResource(list, command, "mcpId", "mcpType", "actionName");
        config.setMcpInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void addAssistantTool(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getToolInfosJson());
        upsertResource(list, toolItem(command), "toolId", "toolType", "actionName");
        config.setToolInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void deleteAssistantTool(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getToolInfosJson());
        removeResource(list, command, "toolId", "toolType", "actionName");
        config.setToolInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void switchAssistantTool(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getToolInfosJson());
        switchResource(list, command, "toolId", "toolType", "actionName");
        config.setToolInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void configureAssistantTool(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getToolInfosJson());
        String toolId = requireResourceId(command);
        Map<String, Object> found = null;
        for (Map<String, Object> item : list) {
            if (toolId.equals(stringValue(item.get("toolId")))) {
                found = item;
                break;
            }
        }
        if (found == null) {
            found = toolItem(command);
            list.add(found);
        }
        found.put("toolConfig", command.getToolConfig() == null
                ? Collections.<String, Object>emptyMap()
                : command.getToolConfig());
        config.setToolInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void addAssistantSkill(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getSkillInfosJson());
        upsertResource(list, skillItem(command), "skillId", "skillType");
        config.setSkillInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void deleteAssistantSkill(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getSkillInfosJson());
        removeResource(list, command, "skillId", "skillType");
        config.setSkillInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void switchAssistantSkill(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getSkillInfosJson());
        switchResource(list, command, "skillId", "skillType");
        config.setSkillInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void addAssistantAgent(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getMultiAgentInfosJson());
        upsertResource(list, agentItem(command), "agentId");
        config.setMultiAgentInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void deleteAssistantAgent(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getMultiAgentInfosJson());
        removeResource(list, command, "agentId");
        config.setMultiAgentInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void switchAssistantAgent(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getMultiAgentInfosJson());
        switchResource(list, command, "agentId");
        config.setMultiAgentInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public void updateAssistantAgentConfig(AssistantResourceCommand command) {
        AssistantDraftConfigRecord config = resourceConfig(command);
        List<Map<String, Object>> list = listMapOrDefault(config.getMultiAgentInfosJson());
        String agentId = requireResourceId(command);
        for (Map<String, Object> item : list) {
            if (agentId.equals(stringValue(item.get("agentId")))) {
                item.put("desc", defaultIfBlank(command.getDesc(), stringValue(item.get("desc"))));
            }
        }
        config.setMultiAgentInfosJson(toJsonOrNull(list));
        saveResourceConfig(config);
    }

    @Override
    public Map<String, Object> listAssistantToolSelect(String userId, String orgId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> weather = new LinkedHashMap<>();
        weather.put("uniqueId", "builtin_builtin-weather");
        weather.put("toolId", "builtin-weather");
        weather.put("toolName", "Weather Tool");
        weather.put("toolType", "builtin");
        weather.put("desc", "Development built-in tool for assistant configuration.");
        weather.put("needApiKeyInput", false);
        weather.put("apiKey", "");
        weather.put("avatar", emptyAvatar());
        list.add(weather);
        return listResult(list);
    }

    @Override
    public Map<String, Object> listAssistantToolActions(AssistantResourceCommand command) {
        return actionList(defaultIfBlank(command == null ? "" : command.getActionName(), "get_weather"));
    }

    @Override
    public Map<String, Object> getAssistantToolActionDetail(AssistantResourceCommand command) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("needApiKeyInput", false);
        result.put("apiKey", "");
        result.put("action", toolAction(defaultIfBlank(command == null ? "" : command.getActionName(), "get_weather")));
        return result;
    }

    @Override
    public Map<String, Object> listAssistantMcpSelect(String userId, String orgId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> search = new LinkedHashMap<>();
        search.put("uniqueId", "mcp_mcp-search");
        search.put("mcpId", "mcp-search");
        search.put("mcpSquareId", "");
        search.put("name", "Search MCP");
        search.put("type", "mcp");
        search.put("toolId", "mcp-search");
        search.put("toolName", "Search MCP");
        search.put("toolType", "mcp");
        search.put("description", "Development MCP entry for assistant configuration.");
        search.put("serverFrom", "local");
        search.put("serverUrl", "");
        search.put("streamableUrl", "");
        search.put("transport", "streamable");
        search.put("avatar", emptyAvatar());
        list.add(search);
        return listResult(list);
    }

    @Override
    public Map<String, Object> listAssistantMcpActions(AssistantResourceCommand command) {
        return actionList(defaultIfBlank(command == null ? "" : command.getActionName(), "search"));
    }

    @Override
    public Map<String, Object> listAssistantWorkflowSelect(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<AppRecord> workflows = applicationRepository.listWorkflows(
                defaultIfBlank(userId, DEV_USER_ID),
                defaultIfBlank(orgId, DEV_ORG_ID),
                defaultIfBlank(name, ""));
        for (AppRecord workflowRecord : workflows) {
            Map<String, Object> workflow = toFrontendWorkflow(workflowRecord);
            workflow.put("uniqueId", uniqueId(APP_TYPE_WORKFLOW, workflowRecord.getAppId()));
            workflow.put("workFlowId", workflowRecord.getAppId());
            list.add(workflow);
        }
        if (!list.isEmpty()) {
            return listResult(list);
        }
        Map<String, Object> workflow = new LinkedHashMap<>();
        workflow.put("uniqueId", "workflow_workflow-demo");
        workflow.put("workFlowId", "workflow-demo");
        workflow.put("appId", "workflow-demo");
        workflow.put("appType", "workflow");
        workflow.put("name", "Demo Workflow");
        workflow.put("desc", "Development workflow entry for assistant configuration.");
        workflow.put("avatar", emptyAvatar());
        if (isBlank(name) || "Demo Workflow".contains(name) || "workflow-demo".contains(name)) {
            list.add(workflow);
        }
        return listResult(list);
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
        ensureConversationAssistantExists(userId, orgId, command.getAssistantId());
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
        AppRecord assistant = ensureConversationAssistantExists(userId, orgId, command.getAssistantId());
        AssistantSnapshotRecord snapshot = null;
        if (!command.isDraft()) {
            snapshot = applicationRepository.findLatestAssistantSnapshot(
                    assistant.getUserId(), assistant.getOrgId(), command.getAssistantId());
            if (snapshot == null) {
                throw new IllegalArgumentException("assistant snapshot not found");
            }
        }
        AssistantDraftConfigRecord config = assistantConfigForChat(
                command.isDraft(),
                assistant.getUserId(),
                assistant.getOrgId(),
                command.getAssistantId(),
                snapshot);
        AssistantConversationRecord conversation = resolveConversation(command, userId, orgId);
        String safetyConfigJson = config == null ? null : config.getSafetyConfigJson();
        SensitiveBlock sensitiveBlock = matchSensitiveResponse(
                userId,
                orgId,
                safetyConfigJson,
                command.getPrompt());
        String response = sensitiveBlock == null ? deterministicResponse(assistant, command.getPrompt()) : sensitiveBlock.reply;
        if (sensitiveBlock == null) {
            SensitiveBlock outputBlock = matchSensitiveResponse(userId, orgId, safetyConfigJson, response);
            if (outputBlock != null) {
                response = outputBlock.reply;
            }
        }
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

    @Override
    public void createAppUrl(AppUrlCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app url create command is required");
        }
        String appType = normalizeAgentAppType(command.getAppType());
        if (!APP_TYPE_AGENT.equals(appType)) {
            throw new IllegalArgumentException("only agent app url is supported");
        }
        if (isBlank(command.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException("app url name is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        ensureAssistantExists(userId, orgId, command.getAppId());
        if (applicationRepository.findLatestAssistantSnapshot(userId, orgId, command.getAppId()) == null) {
            throw new IllegalArgumentException("assistant snapshot not found");
        }
        String name = command.getName().trim();
        if (applicationRepository.findAppUrlByName(userId, orgId, command.getAppId(), appType, name) != null) {
            throw new IllegalArgumentException("app url name already exists");
        }

        long now = clock.millis();
        AppUrlRecord record = new AppUrlRecord();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setAppId(command.getAppId());
        record.setAppType(appType);
        record.setName(name);
        record.setDescription(defaultIfBlank(command.getDescription(), ""));
        record.setExpiredAt(parseAppUrlExpiredAt(command.getExpiredAt()));
        record.setCopyright(defaultIfBlank(command.getCopyright(), ""));
        record.setCopyrightEnable(command.isCopyrightEnable());
        record.setPrivacyPolicy(defaultIfBlank(command.getPrivacyPolicy(), ""));
        record.setPrivacyPolicyEnable(command.isPrivacyPolicyEnable());
        record.setDisclaimer(defaultIfBlank(command.getDisclaimer(), ""));
        record.setDisclaimerEnable(command.isDisclaimerEnable());
        record.setSuffix(newAppUrlSuffix());
        record.setStatus(true);
        applicationRepository.saveAppUrl(record);
    }

    @Override
    public void updateAppUrl(AppUrlUpdateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app url update command is required");
        }
        if (isBlank(command.getUrlId())) {
            throw new IllegalArgumentException("app url id is required");
        }
        if (isBlank(command.getName())) {
            throw new IllegalArgumentException("app url name is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        Long id = parseAppUrlId(command.getUrlId());
        AppUrlRecord existing = applicationRepository.findAppUrlById(userId, orgId, id);
        if (existing == null) {
            throw new IllegalArgumentException("app url not found");
        }
        String name = command.getName().trim();
        AppUrlRecord sameName = applicationRepository.findAppUrlByName(
                userId, orgId, existing.getAppId(), existing.getAppType(), name);
        if (sameName != null && !id.equals(sameName.getId())) {
            throw new IllegalArgumentException("app url name already exists");
        }

        AppUrlRecord record = new AppUrlRecord();
        record.setId(id);
        record.setCreatedAt(existing.getCreatedAt());
        record.setUpdatedAt(clock.millis());
        record.setUserId(userId);
        record.setOrgId(orgId);
        record.setAppId(existing.getAppId());
        record.setAppType(existing.getAppType());
        record.setName(name);
        record.setDescription(defaultIfBlank(command.getDescription(), ""));
        record.setExpiredAt(parseAppUrlExpiredAt(command.getExpiredAt()));
        record.setCopyright(defaultIfBlank(command.getCopyright(), ""));
        record.setCopyrightEnable(command.isCopyrightEnable());
        record.setPrivacyPolicy(defaultIfBlank(command.getPrivacyPolicy(), ""));
        record.setPrivacyPolicyEnable(command.isPrivacyPolicyEnable());
        record.setDisclaimer(defaultIfBlank(command.getDisclaimer(), ""));
        record.setDisclaimerEnable(command.isDisclaimerEnable());
        record.setSuffix(existing.getSuffix());
        record.setStatus(existing.getStatus());
        if (applicationRepository.updateAppUrl(record) == null) {
            throw new IllegalArgumentException("app url not found");
        }
    }

    @Override
    public void deleteAppUrl(AppUrlDeleteCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app url delete command is required");
        }
        if (isBlank(command.getUrlId())) {
            throw new IllegalArgumentException("app url id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        Long id = parseAppUrlId(command.getUrlId());
        if (!applicationRepository.deleteAppUrl(userId, orgId, id)) {
            throw new IllegalArgumentException("app url not found");
        }
    }

    @Override
    public void updateAppUrlStatus(AppUrlStatusCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("app url status command is required");
        }
        if (isBlank(command.getUrlId())) {
            throw new IllegalArgumentException("app url id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        Long id = parseAppUrlId(command.getUrlId());
        if (!applicationRepository.updateAppUrlStatus(userId, orgId, id, command.isStatus(), clock.millis())) {
            throw new IllegalArgumentException("app url not found");
        }
    }

    @Override
    public List<AppUrlInfo> listAppUrls(AppUrlListQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("app url list query is required");
        }
        String appType = normalizeAgentAppType(query.getAppType());
        if (!APP_TYPE_AGENT.equals(appType)) {
            throw new IllegalArgumentException("only agent app url is supported");
        }
        if (isBlank(query.getAppId())) {
            throw new IllegalArgumentException("app id is required");
        }
        String userId = defaultIfBlank(query.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(query.getOrgId(), DEV_ORG_ID);
        ensureAssistantExists(userId, orgId, query.getAppId());
        List<AppUrlRecord> records = applicationRepository.listAppUrls(userId, orgId, query.getAppId(), appType);
        List<AppUrlInfo> result = new ArrayList<>(records.size());
        for (AppUrlRecord record : records) {
            result.add(toAppUrlInfo(record));
        }
        return result;
    }

    @Override
    public AppUrlInfo getAppUrlBySuffix(AppUrlSuffixQuery query) {
        if (query == null || isBlank(query.getSuffix())) {
            throw new IllegalArgumentException("app url suffix is required");
        }
        AppUrlRecord record = applicationRepository.findAppUrlBySuffix(query.getSuffix());
        if (record == null) {
            throw new IllegalArgumentException("app url not found");
        }
        if (!booleanValue(record.getStatus())) {
            throw new IllegalArgumentException("app url disabled");
        }
        Long expiredAt = record.getExpiredAt();
        if (expiredAt != null && expiredAt > 0 && clock.millis() > expiredAt) {
            throw new IllegalArgumentException("app url expired");
        }
        return toAppUrlInfo(record);
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

    private AppRecord ensureConversationAssistantExists(String userId, String orgId, String assistantId) {
        AppRecord record = applicationRepository.findAssistant(userId, orgId, assistantId);
        if (record == null) {
            record = applicationRepository.findAssistantByOrg(orgId, assistantId);
        }
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
        ensureConversationAssistantExists(userId, orgId, query.getAssistantId());
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

    private AppRecord ensureChatflowExists(String userId, String orgId, String chatflowId) {
        AppRecord record = applicationRepository.findWorkflow(userId, orgId, chatflowId, APP_TYPE_CHATFLOW);
        if (record == null) {
            throw new IllegalArgumentException("chatflow draft not found");
        }
        return record;
    }

    private AssistantConversationRecord ensureChatflowOpenApiConversation(String userId,
                                                                          String orgId,
                                                                          String chatflowId,
                                                                          String conversationId) {
        AppRecord chatflow = ensureChatflowExists(userId, orgId, chatflowId);
        AssistantConversationRecord conversation = applicationRepository.findConversation(userId, orgId, conversationId);
        if (conversation == null
                || !chatflow.getAppId().equals(conversation.getAssistantId())
                || !CONVERSATION_TYPE_CHATFLOW_OPENAPI.equals(conversation.getConversationType())) {
            throw new IllegalArgumentException("chatflow conversation not found");
        }
        return conversation;
    }

    private Map<String, Object> chatflowConversationInfo(AssistantConversationRecord conversation) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("conversation_id", conversation.getConversationId());
        item.put("conversationId", conversation.getConversationId());
        item.put("conversation_name", defaultIfBlank(conversation.getTitle(), "New conversation"));
        item.put("conversationName", defaultIfBlank(conversation.getTitle(), "New conversation"));
        item.put("uuid", conversation.getAssistantId());
        return item;
    }

    private Map<String, Object> chatflowOpenApiMessage(AssistantConversationMessageRecord record, String role, int index) {
        long turnId = defaultLong(record.getId()) <= 0L ? index + 1L : defaultLong(record.getId());
        long messageId = turnId * 2L + ("assistant".equals(role) ? 0L : -1L);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", String.valueOf(messageId));
        item.put("bot_id", defaultIfBlank(record.getAssistantId(), ""));
        item.put("role", role);
        item.put("content", "assistant".equals(role)
                ? defaultIfBlank(record.getResponse(), "")
                : defaultIfBlank(record.getPrompt(), ""));
        item.put("conversation_id", defaultIfBlank(record.getConversationId(), ""));
        item.put("meta_data", "assistant".equals(role)
                ? Collections.<String, Object>emptyMap()
                : mapOrDefault(record.getRequestFilesJson(), new LinkedHashMap<String, Object>()));
        item.put("created_at", defaultLong(record.getCreatedAt()));
        item.put("updated_at", defaultLong(record.getUpdatedAt()));
        item.put("chat_id", String.valueOf(messageId));
        item.put("content_type", "text");
        item.put("type", "answer");
        item.put("section_id", "");
        item.put("reasoning_content", "");
        return item;
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

    private ApiKeyInfo toApiKeyInfo(ApiKeyRecord record) {
        ApiKeyInfo info = new ApiKeyInfo();
        info.setKeyId(record.getId() == null ? "" : String.valueOf(record.getId()));
        info.setKey(record.getKey());
        info.setUserId(defaultIfBlank(record.getUserId(), ""));
        info.setOrgId(defaultIfBlank(record.getOrgId(), ""));
        info.setCreator(defaultIfBlank(record.getUserId(), ""));
        info.setName(defaultIfBlank(record.getName(), ""));
        info.setDesc(defaultIfBlank(record.getDescription(), ""));
        info.setExpiredAt(formatDateOnly(record.getExpiredAt()));
        info.setCreatedAt(formatMillis(record.getCreatedAt()));
        info.setStatus(booleanValue(record.getStatus()));
        return info;
    }

    private ApiKeyStatisticOverview toStatisticOverview(ApiKeyUsageAggregateRecord current,
                                                        ApiKeyUsageAggregateRecord previous) {
        ApiKeyStatisticOverview overview = new ApiKeyStatisticOverview();
        overview.setCallCount(overviewItem(current.getCallCount(), previous.getCallCount()));
        overview.setCallFailure(overviewItem(current.getCallFailure(), previous.getCallFailure()));
        overview.setAvgStreamCosts(overviewItem(avg(current.getStreamCosts(), current.getStreamCount()),
                avg(previous.getStreamCosts(), previous.getStreamCount())));
        overview.setAvgNonStreamCosts(overviewItem(avg(current.getNonStreamCosts(), current.getNonStreamCount()),
                avg(previous.getNonStreamCosts(), previous.getNonStreamCount())));
        overview.setStreamCount(overviewItem(current.getStreamCount(), previous.getStreamCount()));
        overview.setNonStreamCount(overviewItem(current.getNonStreamCount(), previous.getNonStreamCount()));
        return overview;
    }

    private ApiKeyStatisticOverviewItem overviewItem(double current, double previous) {
        return new ApiKeyStatisticOverviewItem(current, previous == 0D ? -9999D : ((current - previous) / previous) * 100D);
    }

    private ApiKeyStatisticTrend toStatisticTrend(List<ApiKeyUsageAggregateRecord> records, List<String> dates) {
        Map<String, ApiKeyUsageAggregateRecord> byDate = new HashMap<String, ApiKeyUsageAggregateRecord>();
        for (ApiKeyUsageAggregateRecord record : records) {
            byDate.put(record.getDate(), record);
        }
        ApiKeyStatisticChart chart = new ApiKeyStatisticChart();
        chart.setTableName("app_statistic_api_key_call_trend");
        List<ApiKeyStatisticLine> lines = new ArrayList<ApiKeyStatisticLine>();
        lines.add(statisticLine("app_statistic_api_call_count_total", dates, byDate, "total"));
        lines.add(statisticLine("app_statistic_api_call_success", dates, byDate, "success"));
        lines.add(statisticLine("app_statistic_api_call_failure", dates, byDate, "failure"));
        chart.setLines(lines);
        ApiKeyStatisticTrend trend = new ApiKeyStatisticTrend();
        trend.setApiCalls(chart);
        return trend;
    }

    private ApiKeyStatisticLine statisticLine(String name,
                                              List<String> dates,
                                              Map<String, ApiKeyUsageAggregateRecord> byDate,
                                              String field) {
        List<ApiKeyStatisticPoint> points = new ArrayList<ApiKeyStatisticPoint>();
        for (String date : dates) {
            ApiKeyUsageAggregateRecord record = byDate.get(date);
            double value = 0D;
            if (record != null) {
                if ("total".equals(field)) {
                    value = record.getCallCount();
                } else if ("success".equals(field)) {
                    value = record.getCallCount() - record.getCallFailure();
                } else if ("failure".equals(field)) {
                    value = record.getCallFailure();
                }
            }
            points.add(new ApiKeyStatisticPoint(date, value));
        }
        return new ApiKeyStatisticLine(name, points);
    }

    private ApiKeyStatisticItem toStatisticItem(ApiKeyUsageAggregateRecord record) {
        ApiKeyStatisticItem item = new ApiKeyStatisticItem();
        item.setApiKeyId(defaultIfBlank(record.getApiKeyId(), ""));
        item.setMethodPath(defaultIfBlank(record.getMethodPath(), ""));
        item.setCallCount(record.getCallCount());
        item.setCallFailure(record.getCallFailure());
        item.setAvgStreamCosts(avg(record.getStreamCosts(), record.getStreamCount()));
        item.setAvgNonStreamCosts(avg(record.getNonStreamCosts(), record.getNonStreamCount()));
        item.setStreamCount(record.getStreamCount());
        item.setNonStreamCount(record.getNonStreamCount());
        return item;
    }

    private ApiKeyStatisticRecordItem toStatisticRecordItem(ApiKeyUsageRecord record) {
        ApiKeyStatisticRecordItem item = new ApiKeyStatisticRecordItem();
        item.setApiKeyId(defaultIfBlank(record.getApiKeyId(), ""));
        item.setMethodPath(defaultIfBlank(record.getMethodPath(), ""));
        item.setCallTime(record.getCallTime());
        item.setResponseStatus(defaultIfBlank(record.getResponseStatus(), ""));
        item.setStreamCosts(record.getStreamCosts());
        item.setNonStreamCosts(record.getNonStreamCosts());
        item.setRequestBody(defaultIfBlank(record.getRequestBody(), ""));
        item.setResponseBody(defaultIfBlank(record.getResponseBody(), ""));
        return item;
    }

    private AppStatisticOverview toAppStatisticOverview(AppStatisticAggregateRecord current,
                                                        AppStatisticAggregateRecord previous) {
        AppStatisticOverview overview = new AppStatisticOverview();
        overview.setCallCount(statisticOverviewItem(current.getCallCount(), previous.getCallCount()));
        overview.setCallFailure(statisticOverviewItem(current.getCallFailure(), previous.getCallFailure()));
        overview.setStreamCount(statisticOverviewItem(current.getStreamCount(), previous.getStreamCount()));
        overview.setNonStreamCount(statisticOverviewItem(current.getNonStreamCount(), previous.getNonStreamCount()));
        overview.setAvgStreamCosts(statisticOverviewItem(
                avg(current.getStreamCosts(), successCount(current.getStreamCount(), current.getStreamFailure())),
                avg(previous.getStreamCosts(), successCount(previous.getStreamCount(), previous.getStreamFailure()))));
        overview.setAvgNonStreamCosts(statisticOverviewItem(
                avg(current.getNonStreamCosts(), successCount(current.getNonStreamCount(), current.getNonStreamFailure())),
                avg(previous.getNonStreamCosts(), successCount(previous.getNonStreamCount(), previous.getNonStreamFailure()))));
        return overview;
    }

    private AppStatisticTrend toAppStatisticTrend(List<AppStatisticAggregateRecord> records,
                                                  List<String> dates,
                                                  String appType) {
        Map<String, AppStatisticAggregateRecord> byDate = new HashMap<String, AppStatisticAggregateRecord>();
        for (AppStatisticAggregateRecord record : records) {
            byDate.put(record.getDate(), record);
        }
        StatisticChart chart = new StatisticChart();
        chart.setTableName("app_statistic_app_call_trend");
        List<StatisticLine> lines = new ArrayList<StatisticLine>();
        lines.add(appStatisticLine("app_statistic_call_count_total", dates, byDate, "total"));
        lines.add(appStatisticLine("app_statistic_web_call_count", dates, byDate, "web"));
        lines.add(appStatisticLine("app_statistic_openapi_call_count", dates, byDate, "openapi"));
        if (APP_TYPE_AGENT.equals(appType)) {
            lines.add(appStatisticLine("app_statistic_web_url_call_count", dates, byDate, "webUrl"));
        }
        chart.setLines(lines);
        AppStatisticTrend trend = new AppStatisticTrend();
        trend.setCallTrend(chart);
        return trend;
    }

    private StatisticLine appStatisticLine(String name,
                                           List<String> dates,
                                           Map<String, AppStatisticAggregateRecord> byDate,
                                           String field) {
        List<StatisticPoint> points = new ArrayList<StatisticPoint>();
        for (String date : dates) {
            AppStatisticAggregateRecord record = byDate.get(date);
            double value = 0D;
            if (record != null) {
                if ("total".equals(field)) {
                    value = record.getCallCount();
                } else if ("web".equals(field)) {
                    value = record.getWebCallCount();
                } else if ("openapi".equals(field)) {
                    value = record.getOpenapiCallCount();
                } else if ("webUrl".equals(field)) {
                    value = record.getWebUrlCallCount();
                }
            }
            points.add(new StatisticPoint(date, value));
        }
        return new StatisticLine(name, points);
    }

    private AppStatisticItem toAppStatisticItem(AppStatisticAggregateRecord record) {
        AppStatisticItem item = new AppStatisticItem();
        item.setAppId(defaultIfBlank(record.getAppId(), ""));
        item.setAppType(defaultIfBlank(record.getAppType(), ""));
        item.setOrgId(defaultIfBlank(record.getOrgId(), ""));
        item.setCallCount(record.getCallCount());
        item.setCallFailure(record.getCallFailure());
        item.setFailureRate(failureRate(record.getCallCount(), record.getCallFailure()));
        item.setStreamCount(record.getStreamCount());
        item.setNonStreamCount(record.getNonStreamCount());
        item.setAvgStreamCosts(avg(record.getStreamCosts(), successCount(record.getStreamCount(), record.getStreamFailure())));
        item.setAvgNonStreamCosts(avg(record.getNonStreamCosts(), successCount(record.getNonStreamCount(), record.getNonStreamFailure())));
        return item;
    }

    private ModelStatisticOverview toModelStatisticOverview(ModelStatisticAggregateRecord current,
                                                            ModelStatisticAggregateRecord previous) {
        ModelStatisticOverview overview = new ModelStatisticOverview();
        overview.setCallCount(statisticOverviewItem(current.getCallCount(), previous.getCallCount()));
        overview.setCallFailure(statisticOverviewItem(current.getCallFailure(), previous.getCallFailure()));
        overview.setTotalTokens(statisticOverviewItem(current.getTotalTokens(), previous.getTotalTokens()));
        overview.setCompletionTokens(statisticOverviewItem(current.getCompletionTokens(), previous.getCompletionTokens()));
        overview.setPromptTokens(statisticOverviewItem(current.getPromptTokens(), previous.getPromptTokens()));
        overview.setAvgCosts(statisticOverviewItem(
                avg(current.getCosts(), successCount(current.getNonStreamCount(), current.getNonStreamFailure())),
                avg(previous.getCosts(), successCount(previous.getNonStreamCount(), previous.getNonStreamFailure()))));
        overview.setAvgFirstTokenLatency(statisticOverviewItem(
                avg(current.getFirstTokenLatency(), successCount(current.getStreamCount(), current.getStreamFailure())),
                avg(previous.getFirstTokenLatency(), successCount(previous.getStreamCount(), previous.getStreamFailure()))));
        return overview;
    }

    private ModelStatisticTrend toModelStatisticTrend(List<ModelStatisticAggregateRecord> records, List<String> dates) {
        Map<String, ModelStatisticAggregateRecord> byDate = new HashMap<String, ModelStatisticAggregateRecord>();
        for (ModelStatisticAggregateRecord record : records) {
            byDate.put(record.getDate(), record);
        }
        StatisticChart modelCalls = new StatisticChart();
        modelCalls.setTableName("app_statistic_model_call_trend");
        List<StatisticLine> callLines = new ArrayList<StatisticLine>();
        callLines.add(modelStatisticLine("app_statistic_call_count_total", dates, byDate, "total"));
        callLines.add(modelStatisticLine("app_statistic_call_success", dates, byDate, "success"));
        callLines.add(modelStatisticLine("app_statistic_call_failure", dates, byDate, "failure"));
        modelCalls.setLines(callLines);

        StatisticChart tokensUsage = new StatisticChart();
        tokensUsage.setTableName("app_statistic_model_tokens_usage_trend");
        List<StatisticLine> tokenLines = new ArrayList<StatisticLine>();
        tokenLines.add(modelStatisticLine("app_statistic_total_tokens", dates, byDate, "totalTokens"));
        tokenLines.add(modelStatisticLine("app_statistic_completion_tokens", dates, byDate, "completionTokens"));
        tokenLines.add(modelStatisticLine("app_statistic_prompt_tokens", dates, byDate, "promptTokens"));
        tokensUsage.setLines(tokenLines);

        ModelStatisticTrend trend = new ModelStatisticTrend();
        trend.setModelCalls(modelCalls);
        trend.setTokensUsage(tokensUsage);
        return trend;
    }

    private StatisticLine modelStatisticLine(String name,
                                             List<String> dates,
                                             Map<String, ModelStatisticAggregateRecord> byDate,
                                             String field) {
        List<StatisticPoint> points = new ArrayList<StatisticPoint>();
        for (String date : dates) {
            ModelStatisticAggregateRecord record = byDate.get(date);
            double value = 0D;
            if (record != null) {
                if ("total".equals(field)) {
                    value = record.getCallCount();
                } else if ("success".equals(field)) {
                    value = record.getCallCount() - record.getCallFailure();
                } else if ("failure".equals(field)) {
                    value = record.getCallFailure();
                } else if ("totalTokens".equals(field)) {
                    value = record.getTotalTokens();
                } else if ("completionTokens".equals(field)) {
                    value = record.getCompletionTokens();
                } else if ("promptTokens".equals(field)) {
                    value = record.getPromptTokens();
                }
            }
            points.add(new StatisticPoint(date, value));
        }
        return new StatisticLine(name, points);
    }

    private ModelStatisticItem toModelStatisticItem(ModelStatisticAggregateRecord record) {
        ModelStatisticItem item = new ModelStatisticItem();
        item.setModelId(defaultIfBlank(record.getModelId(), ""));
        item.setModel(defaultIfBlank(record.getModel(), ""));
        item.setProvider(defaultIfBlank(record.getProvider(), ""));
        item.setOrgId(defaultIfBlank(record.getOrgId(), ""));
        item.setCallCount(record.getCallCount());
        item.setCallFailure(record.getCallFailure());
        item.setFailureRate(failureRate(record.getCallCount(), record.getCallFailure()));
        item.setPromptTokens(record.getPromptTokens());
        item.setCompletionTokens(record.getCompletionTokens());
        item.setTotalTokens(record.getTotalTokens());
        item.setAvgCosts(avg(record.getCosts(), successCount(record.getNonStreamCount(), record.getNonStreamFailure())));
        item.setAvgFirstTokenLatency(avg(record.getFirstTokenLatency(), successCount(record.getStreamCount(), record.getStreamFailure())));
        return item;
    }

    private StatisticOverviewItem statisticOverviewItem(double current, double previous) {
        return new StatisticOverviewItem(current, previous == 0D ? -9999D : ((current - previous) / previous) * 100D);
    }

    private long successCount(long total, long failures) {
        return Math.max(0L, total - failures);
    }

    private double failureRate(long callCount, long callFailure) {
        return callCount <= 0L ? 0D : ((double) callFailure / (double) callCount) * 100D;
    }

    private double avg(long total, long count) {
        return count <= 0L ? 0D : ((double) total) / ((double) count);
    }

    private AppKeyInfo toAppKeyInfo(AppKeyRecord record) {
        AppKeyInfo info = new AppKeyInfo();
        info.setApiId(record.getId() == null ? "" : String.valueOf(record.getId()));
        info.setApiKey(defaultIfBlank(record.getApiKey(), ""));
        info.setUserId(defaultIfBlank(record.getUserId(), ""));
        info.setOrgId(defaultIfBlank(record.getOrgId(), ""));
        info.setAppId(defaultIfBlank(record.getAppId(), ""));
        info.setAppType(defaultIfBlank(record.getAppType(), ""));
        info.setCreatedAt(formatMillis(record.getCreatedAt()));
        return info;
    }

    private long parseDateOnly(String value) {
        if (isBlank(value)) {
            return 0L;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_ONLY_FORMATTER)
                    .atStartOfDay(APP_ZONE)
                    .toInstant()
                    .toEpochMilli();
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("api key expiredAt format is invalid", ex);
        }
    }

    private String formatDateOnly(Long millis) {
        if (millis == null || millis <= 0) {
            return "";
        }
        return DATE_ONLY_FORMATTER.format(Instant.ofEpochMilli(millis).atZone(APP_ZONE));
    }

    private Long parseLongId(String value, String message) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message, ex);
        }
    }

    private boolean ownedBy(ApiKeyRecord record, String userId, String orgId) {
        return record != null
                && userId.equals(record.getUserId())
                && orgId.equals(record.getOrgId());
    }

    private boolean ownedBy(AppKeyRecord record, String userId, String orgId) {
        return record != null
                && userId.equals(record.getUserId())
                && orgId.equals(record.getOrgId());
    }

    private AppUrlInfo toAppUrlInfo(AppUrlRecord record) {
        AppUrlInfo info = new AppUrlInfo();
        info.setUrlId(record.getId() == null ? "" : String.valueOf(record.getId()));
        info.setAppId(record.getAppId());
        info.setAppType(record.getAppType());
        info.setName(defaultIfBlank(record.getName(), ""));
        info.setCreatedAt(formatMillis(record.getCreatedAt()));
        info.setExpiredAt(formatOptionalMillis(record.getExpiredAt()));
        info.setCopyright(defaultIfBlank(record.getCopyright(), ""));
        info.setCopyrightEnable(booleanValue(record.getCopyrightEnable()));
        info.setPrivacyPolicy(defaultIfBlank(record.getPrivacyPolicy(), ""));
        info.setPrivacyPolicyEnable(booleanValue(record.getPrivacyPolicyEnable()));
        info.setDisclaimer(defaultIfBlank(record.getDisclaimer(), ""));
        info.setDisclaimerEnable(booleanValue(record.getDisclaimerEnable()));
        info.setSuffix(defaultIfBlank(record.getSuffix(), ""));
        info.setStatus(booleanValue(record.getStatus()));
        info.setUserId(defaultIfBlank(record.getUserId(), ""));
        info.setOrgId(defaultIfBlank(record.getOrgId(), ""));
        info.setDescription(defaultIfBlank(record.getDescription(), ""));
        return info;
    }

    private long parseAppUrlExpiredAt(String value) {
        if (isBlank(value)) {
            return 0L;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME_INPUT_FORMATTER)
                    .atZone(APP_ZONE)
                    .toInstant()
                    .toEpochMilli();
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("app url expiredAt format is invalid", ex);
        }
    }

    private Long parseAppUrlId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("app url id is invalid", ex);
        }
    }

    private boolean booleanValue(Boolean value) {
        return value != null && value;
    }

    private int defaultInt(int value, int fallback) {
        return value <= 0 ? fallback : value;
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

    private ApiKeyQueryContext apiKeyQuery(ApiKeyStatisticQuery query) {
        LocalDate end = parseStatisticDate(query == null ? "" : query.getEndDate(),
                Instant.ofEpochMilli(clock.millis()).atZone(APP_ZONE).toLocalDate());
        LocalDate start = parseStatisticDate(query == null ? "" : query.getStartDate(), end.minusDays(6));
        if (start.isAfter(end)) {
            start = end;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1L;
        LocalDate previousEnd = start.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(Math.max(0L, days - 1L));
        return new ApiKeyQueryContext(
                query == null ? DEV_USER_ID : defaultIfBlank(query.getUserId(), DEV_USER_ID),
                query == null ? DEV_ORG_ID : defaultIfBlank(query.getOrgId(), DEV_ORG_ID),
                start.toString(),
                end.toString(),
                previousStart.toString(),
                previousEnd.toString(),
                normalizeStatisticFilter(query == null ? null : query.getApiKeyIds()),
                normalizeStatisticFilter(query == null ? null : query.getMethodPaths()),
                statisticDates(start, end));
    }

    private StatisticQueryContext appStatisticQuery(AppStatisticQuery query) {
        String appType = query == null ? APP_TYPE_AGENT : normalizeAppType(query.getAppType());
        return statisticQuery(
                query == null ? DEV_USER_ID : defaultIfBlank(query.getUserId(), DEV_USER_ID),
                query == null ? DEV_ORG_ID : defaultIfBlank(query.getOrgId(), DEV_ORG_ID),
                query == null ? "" : query.getStartDate(),
                query == null ? "" : query.getEndDate(),
                normalizeStatisticFilter(query == null ? null : query.getAppIds()),
                appType);
    }

    private StatisticQueryContext modelStatisticQuery(ModelStatisticQuery query) {
        return statisticQuery(
                query == null ? DEV_USER_ID : defaultIfBlank(query.getUserId(), DEV_USER_ID),
                query == null ? DEV_ORG_ID : defaultIfBlank(query.getOrgId(), DEV_ORG_ID),
                query == null ? "" : query.getStartDate(),
                query == null ? "" : query.getEndDate(),
                normalizeStatisticFilter(query == null ? null : query.getModelIds()),
                query == null ? "llm" : defaultIfBlank(query.getModelType(), "llm"));
    }

    private StatisticQueryContext statisticQuery(String userId,
                                                String orgId,
                                                String startDateValue,
                                                String endDateValue,
                                                List<String> ids,
                                                String type) {
        LocalDate end = parseStatisticDate(endDateValue,
                Instant.ofEpochMilli(clock.millis()).atZone(APP_ZONE).toLocalDate());
        LocalDate start = parseStatisticDate(startDateValue, end.minusDays(6));
        if (start.isAfter(end)) {
            start = end;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1L;
        LocalDate previousEnd = start.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(Math.max(0L, days - 1L));
        return new StatisticQueryContext(
                userId,
                orgId,
                start.toString(),
                end.toString(),
                previousStart.toString(),
                previousEnd.toString(),
                ids,
                type,
                statisticDates(start, end));
    }

    private LocalDate parseStatisticDate(String value, LocalDate fallback) {
        if (isBlank(value)) {
            return fallback;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_ONLY_FORMATTER);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private List<String> normalizeStatisticFilter(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            if (isBlank(value) || "ALL".equalsIgnoreCase(value.trim())) {
                continue;
            }
            result.add(value.trim());
        }
        return result;
    }

    private List<String> statisticDates(LocalDate start, LocalDate end) {
        List<String> dates = new ArrayList<String>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end) && dates.size() < 366) {
            dates.add(cursor.toString());
            cursor = cursor.plusDays(1);
        }
        return dates;
    }

    private String conversationTitle(String prompt) {
        String title = defaultIfBlank(prompt, "").trim();
        return title.length() > 60 ? title.substring(0, 60) : title;
    }

    private String deterministicResponse(AppRecord assistant, String prompt) {
        return "Demo response from " + defaultIfBlank(assistant.getName(), "Agent") + ": " + prompt;
    }

    private String deterministicRagResponse(AppRecord rag, String question, List<Map<String, Object>> fileInfo) {
        int fileCount = fileInfo == null ? 0 : fileInfo.size();
        String suffix = fileCount > 0 ? " Attached files: " + fileCount + "." : "";
        return "Demo RAG response from " + defaultIfBlank(rag.getName(), "RAG") + ": " + question + suffix;
    }

    private static class SensitiveBlock {
        private final String reply;

        SensitiveBlock(String reply) {
            this.reply = reply;
        }
    }

    private RagDraftConfigRecord ragConfigForChat(boolean draft,
                                                  String userId,
                                                  String orgId,
                                                  String ragId,
                                                  RagSnapshotRecord snapshot) {
        if (draft) {
            return applicationRepository.findRagConfig(userId, orgId, ragId);
        }
        return ragConfigFromSnapshot(snapshot);
    }

    private RagDraftConfigRecord ragConfigFromSnapshot(RagSnapshotRecord snapshot) {
        if (snapshot == null || isBlank(snapshot.getRagConfigJson())) {
            return null;
        }
        try {
            return objectMapper.readValue(snapshot.getRagConfigJson(), RagDraftConfigRecord.class);
        } catch (Exception ex) {
            throw new IllegalStateException("rag snapshot config is invalid", ex);
        }
    }

    private AssistantDraftConfigRecord assistantConfigForChat(boolean draft,
                                                              String userId,
                                                              String orgId,
                                                              String assistantId,
                                                              AssistantSnapshotRecord snapshot) {
        if (draft) {
            return applicationRepository.findAssistantConfig(userId, orgId, assistantId);
        }
        return assistantConfigFromSnapshot(snapshot);
    }

    private AssistantDraftConfigRecord assistantConfigFromSnapshot(AssistantSnapshotRecord snapshot) {
        if (snapshot == null || isBlank(snapshot.getAssistantConfigJson())) {
            return null;
        }
        try {
            return objectMapper.readValue(snapshot.getAssistantConfigJson(), AssistantDraftConfigRecord.class);
        } catch (Exception ex) {
            throw new IllegalStateException("assistant snapshot config is invalid", ex);
        }
    }

    private SensitiveBlock matchSensitiveResponse(String userId, String orgId, String safetyConfigJson, String input) {
        if (safetyService == null || isBlank(input)) {
            return null;
        }
        for (String tableId : sensitiveTableIds(userId, orgId, safetyConfigJson)) {
            SensitiveBlock block = matchSensitiveTable(userId, orgId, tableId, input);
            if (block != null) {
                return block;
            }
        }
        return null;
    }

    private List<String> sensitiveTableIds(String userId, String orgId, String safetyConfigJson) {
        List<String> tableIds = new ArrayList<>();
        addTableIds(tableIds, globalSensitiveTables(userId, orgId));
        if (isBlank(safetyConfigJson)) {
            return tableIds;
        }
        Map<String, Object> config = mapOrDefault(safetyConfigJson, new LinkedHashMap<String, Object>());
        if (!enabled(config.get("enable")) && !enabled(config.get("enabled"))) {
            return tableIds;
        }
        addTableIds(tableIds, listValue(config.get("tables")));
        addTableIds(tableIds, listValue(config.get("sensitiveTable")));
        addTableIds(tableIds, listValue(config.get("sensitiveTables")));
        addTableIds(tableIds, listValue(config.get("tableIds")));
        return tableIds;
    }

    private List<Object> globalSensitiveTables(String userId, String orgId) {
        try {
            Map<String, Object> result = safetyService.listSensitiveWordTables(userId, orgId, "global");
            return listValue(result == null ? null : result.get("list"));
        } catch (RuntimeException ex) {
            return Collections.emptyList();
        }
    }

    private void addTableIds(List<String> tableIds, List<Object> rawTables) {
        for (Object raw : rawTables) {
            String tableId;
            if (raw instanceof Map) {
                Map<String, Object> table = mapValue(raw);
                tableId = firstNonBlank(
                        stringValue(table.get("tableId")),
                        stringValue(table.get("id")),
                        stringValue(table.get("value")));
            } else {
                tableId = stringValue(raw);
            }
            if (!isBlank(tableId) && !tableIds.contains(tableId)) {
                tableIds.add(tableId);
            }
        }
    }

    private SensitiveBlock matchSensitiveTable(String userId, String orgId, String tableId, String input) {
        try {
            Map<String, Object> table = safetyService.getSensitiveWordTable(userId, orgId, tableId);
            String reply = firstNonBlank(
                    stringValue(table == null ? null : table.get("reply")),
                    "Content blocked by sensitive word filter");
            Map<String, Object> words = safetyService.listSensitiveWords(userId, orgId, tableId, 1, 1000);
            for (Object item : listValue(words == null ? null : words.get("list"))) {
                Map<String, Object> row = mapValue(item);
                String word = firstNonBlank(
                        stringValue(row.get("word")),
                        stringValue(row.get("content")),
                        stringValue(row.get("sensitiveWord")),
                        stringValue(row.get("name")));
                if (!isBlank(word) && input.contains(word)) {
                    return new SensitiveBlock(reply);
                }
            }
            return null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private boolean enabled(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        String text = stringValue(value).trim();
        return "true".equalsIgnoreCase(text) || "1".equals(text) || "yes".equalsIgnoreCase(text);
    }

    private Map<String, Object> hitConfiguredKnowledge(String userId,
                                                       String orgId,
                                                       String question,
                                                       String configJson,
                                                       boolean qa) {
        if (knowledgeService == null || isBlank(configJson)) {
            return Collections.emptyMap();
        }
        Map<String, Object> config = mapOrDefault(configJson, new LinkedHashMap<String, Object>());
        List<Map<String, Object>> knowledgeList = knowledgeHitList(config);
        if (knowledgeList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("question", question);
        request.put("knowledgeList", knowledgeList);
        request.put("knowledgeMatchParams", mapValue(config.get("config")));
        Map<String, Object> hit = qa
                ? knowledgeService.hitQaPairs(userId, orgId, request)
                : knowledgeService.hitKnowledge(userId, orgId, request);
        return hit == null ? Collections.<String, Object>emptyMap() : hit;
    }

    private List<Map<String, Object>> knowledgeHitList(Map<String, Object> config) {
        List<Map<String, Object>> knowledgeList = new ArrayList<>();
        for (Object item : listValue(config.get("knowledgebases"))) {
            Map<String, Object> source = mapValue(item);
            String knowledgeId = defaultIfBlank(stringValue(source.get("knowledgeId")), stringValue(source.get("id")));
            if (isBlank(knowledgeId)) {
                continue;
            }
            Map<String, Object> target = new LinkedHashMap<>();
            target.put("knowledgeId", knowledgeId);
            copyIfPresent(source, target, "knowledgeName", "knowledgeName");
            copyIfPresent(source, target, "name", "knowledgeName");
            copyIfPresent(source, target, "category", "category");
            copyIfPresent(source, target, "external", "external");
            copyIfPresent(source, target, "graphSwitch", "graphSwitch");
            copyIfPresent(source, target, "metaDataFilterParams", "metaDataFilterParams");
            knowledgeList.add(target);
        }
        return knowledgeList;
    }

    private void copyIfPresent(Map<String, Object> source,
                               Map<String, Object> target,
                               String sourceKey,
                               String targetKey) {
        if (source.containsKey(sourceKey) && source.get(sourceKey) != null && !target.containsKey(targetKey)) {
            target.put(targetKey, source.get(sourceKey));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> listValue(Object value) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return Collections.emptyList();
    }

    private List<Map<String, Object>> hitSearchList(Map<String, Object> hit) {
        List<Map<String, Object>> searchList = new ArrayList<>();
        if (hit == null) {
            return searchList;
        }
        for (Object item : listValue(hit.get("searchList"))) {
            if (item instanceof Map) {
                searchList.add(new LinkedHashMap<>(mapValue(item)));
            }
        }
        return searchList;
    }

    private String enrichRagResponse(String base,
                                     Map<String, Object> knowledgeHit,
                                     List<Map<String, Object>> searchList,
                                     Map<String, Object> qaHit,
                                     List<Map<String, Object>> qaSearchList) {
        String evidence = firstNonBlank(
                stringValue(knowledgeHit == null ? null : knowledgeHit.get("prompt")),
                stringValue(qaHit == null ? null : qaHit.get("prompt")),
                firstHitText(searchList),
                firstHitText(qaSearchList));
        if (isBlank(evidence) || base.contains(evidence)) {
            return base;
        }
        return base + "\n\n" + evidence;
    }

    private String firstHitText(List<Map<String, Object>> searchList) {
        if (searchList == null || searchList.isEmpty()) {
            return "";
        }
        Map<String, Object> first = searchList.get(0);
        return firstNonBlank(
                stringValue(first.get("snippet")),
                stringValue(first.get("content")),
                stringValue(first.get("answer")),
                stringValue(first.get("text")));
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

    private AssistantDraftConfigRecord resourceConfig(AssistantResourceCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("assistant resource command is required");
        }
        if (isBlank(command.getAssistantId())) {
            throw new IllegalArgumentException("assistant id is required");
        }
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord assistant = applicationRepository.findAssistant(userId, orgId, command.getAssistantId());
        if (assistant == null) {
            throw new IllegalArgumentException("assistant draft not found");
        }
        AssistantDraftConfigRecord config = applicationRepository.findAssistantConfig(
                userId, orgId, command.getAssistantId());
        if (config == null) {
            config = newConfigRecord(assistant, clock.millis());
        }
        return config;
    }

    private AssistantDraftConfigRecord newConfigRecord(AppRecord assistant, long now) {
        AssistantDraftConfigRecord config = new AssistantDraftConfigRecord();
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        config.setUserId(assistant.getUserId());
        config.setOrgId(assistant.getOrgId());
        config.setAssistantId(assistant.getAppId());
        config.setPrologue("");
        config.setInstructions("");
        config.setWorkflowInfosJson("[]");
        config.setMcpInfosJson("[]");
        config.setToolInfosJson("[]");
        config.setSkillInfosJson("[]");
        config.setMultiAgentInfosJson("[]");
        return config;
    }

    private void saveResourceConfig(AssistantDraftConfigRecord config) {
        if (config.getCreatedAt() == null) {
            config.setCreatedAt(clock.millis());
        }
        config.setUpdatedAt(clock.millis());
        applicationRepository.saveAssistantConfig(config);
    }

    private void preserveAssistantResources(AssistantDraftConfigRecord source, AssistantDraftConfigRecord target) {
        target.setWorkflowInfosJson(source == null ? "[]" : source.getWorkflowInfosJson());
        target.setMcpInfosJson(source == null ? "[]" : source.getMcpInfosJson());
        target.setToolInfosJson(source == null ? "[]" : source.getToolInfosJson());
        target.setSkillInfosJson(source == null ? "[]" : source.getSkillInfosJson());
        target.setMultiAgentInfosJson(source == null ? "[]" : source.getMultiAgentInfosJson());
    }

    private Map<String, Object> workflowItem(AssistantResourceCommand command) {
        String workflowId = requireResourceId(command);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uniqueId", uniqueId("workflow", workflowId));
        item.put("workFlowId", workflowId);
        item.put("apiName", workflowId);
        item.put("enable", true);
        item.put("avatar", emptyAvatar());
        item.put("name", workflowId);
        item.put("workFlowDesc", "");
        return item;
    }

    private Map<String, Object> mcpItem(AssistantResourceCommand command) {
        String mcpId = requireResourceId(command);
        String mcpType = defaultIfBlank(command.getResourceType(), "mcp");
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uniqueId", uniqueId(mcpType, mcpId));
        item.put("mcpId", mcpId);
        item.put("mcpType", mcpType);
        item.put("mcpName", titleFromId(mcpId));
        item.put("actionName", defaultIfBlank(command.getActionName(), ""));
        item.put("enable", true);
        item.put("valid", true);
        item.put("avatar", emptyAvatar());
        return item;
    }

    private Map<String, Object> toolItem(AssistantResourceCommand command) {
        String toolId = requireResourceId(command);
        String toolType = defaultIfBlank(command.getResourceType(), "builtin");
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uniqueId", uniqueId(toolType, toolId));
        item.put("toolId", toolId);
        item.put("toolType", toolType);
        item.put("toolName", titleFromId(toolId));
        item.put("actionName", defaultIfBlank(command.getActionName(), ""));
        item.put("enable", true);
        item.put("valid", true);
        item.put("toolConfig", command.getToolConfig() == null
                ? Collections.<String, Object>emptyMap()
                : command.getToolConfig());
        item.put("avatar", emptyAvatar());
        return item;
    }

    private Map<String, Object> skillItem(AssistantResourceCommand command) {
        String skillId = requireResourceId(command);
        String skillType = defaultIfBlank(command.getResourceType(), "builtin");
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("skillId", skillId);
        item.put("skillType", skillType);
        item.put("skillName", titleFromId(skillId));
        item.put("author", "admin");
        item.put("enable", true);
        item.put("valid", true);
        item.put("avatar", emptyAvatar());
        return item;
    }

    private Map<String, Object> agentItem(AssistantResourceCommand command) {
        String agentId = requireResourceId(command);
        String userId = defaultIfBlank(command.getUserId(), DEV_USER_ID);
        String orgId = defaultIfBlank(command.getOrgId(), DEV_ORG_ID);
        AppRecord child = applicationRepository.findAssistant(userId, orgId, agentId);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("agentId", agentId);
        item.put("name", child == null ? agentId : child.getName());
        item.put("desc", defaultIfBlank(command.getDesc(), child == null ? "" : defaultIfBlank(child.getDesc(), "")));
        item.put("enable", true);
        item.put("avatar", child == null ? emptyAvatar() : avatar(child));
        return item;
    }

    private String requireResourceId(AssistantResourceCommand command) {
        if (command == null || isBlank(command.getResourceId())) {
            throw new IllegalArgumentException("assistant resource id is required");
        }
        return command.getResourceId().trim();
    }

    private void upsertResource(List<Map<String, Object>> list, Map<String, Object> item, String... keys) {
        for (int i = 0; i < list.size(); i++) {
            if (matches(list.get(i), item, keys)) {
                list.set(i, item);
                return;
            }
        }
        list.add(item);
    }

    private void removeResource(List<Map<String, Object>> list, AssistantResourceCommand command, String... keys) {
        Map<String, Object> criteria = criteria(command, keys);
        for (int i = list.size() - 1; i >= 0; i--) {
            if (matches(list.get(i), criteria, keys)) {
                list.remove(i);
            }
        }
    }

    private void switchResource(List<Map<String, Object>> list, AssistantResourceCommand command, String... keys) {
        Map<String, Object> criteria = criteria(command, keys);
        for (Map<String, Object> item : list) {
            if (matches(item, criteria, keys)) {
                item.put("enable", command.getEnable() == null ? true : command.getEnable());
            }
        }
    }

    private Map<String, Object> criteria(AssistantResourceCommand command, String... keys) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        for (String key : keys) {
            criteria.put(key, commandValue(command, key));
        }
        return criteria;
    }

    private boolean matches(Map<String, Object> item, Map<String, Object> criteria, String... keys) {
        for (String key : keys) {
            String expected = stringValue(criteria.get(key));
            if (!expected.equals(stringValue(item.get(key)))) {
                return false;
            }
        }
        return true;
    }

    private Object commandValue(AssistantResourceCommand command, String key) {
        if ("mcpType".equals(key) || "toolType".equals(key) || "skillType".equals(key)) {
            return defaultIfBlank(command.getResourceType(), "builtin");
        }
        if ("actionName".equals(key)) {
            return defaultIfBlank(command.getActionName(), "");
        }
        return requireResourceId(command);
    }

    private Map<String, Object> listResult(List<Map<String, Object>> list) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return result;
    }

    private Map<String, Object> actionList(String actionName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("actions", Collections.singletonList(toolAction(actionName)));
        return result;
    }

    private Map<String, Object> toolAction(String actionName) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("name", actionName);
        action.put("description", "Development action");
        action.put("inputSchema", Collections.singletonMap("type", "object"));
        return action;
    }

    private String uniqueId(String type, String id) {
        if (isBlank(type) || isBlank(id)) {
            return "";
        }
        return type + "_" + id;
    }

    private String titleFromId(String id) {
        if (isBlank(id)) {
            return "";
        }
        String normalized = id.replace('-', ' ').replace('_', ' ').trim();
        if (normalized.isEmpty()) {
            return id;
        }
        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
    }

    private Map<String, Object> emptyAvatar() {
        Map<String, Object> avatar = new LinkedHashMap<>();
        avatar.put("key", "");
        avatar.put("path", "");
        return avatar;
    }

    private List<AppRecord> listApplicationRecords(String userId, String orgId, String name, String appType) {
        List<AppRecord> records = new ArrayList<AppRecord>();
        if (appType.isEmpty() || APP_TYPE_AGENT.equals(appType)) {
            records.addAll(applicationRepository.listAssistants(userId, orgId, name));
        }
        if (appType.isEmpty() || APP_TYPE_RAG.equals(appType)) {
            records.addAll(applicationRepository.listRags(userId, orgId, name));
        }
        if (appType.isEmpty() || APP_TYPE_WORKFLOW.equals(appType)) {
            records.addAll(applicationRepository.listWorkflows(userId, orgId, name, APP_TYPE_WORKFLOW));
        }
        if (appType.isEmpty() || APP_TYPE_CHATFLOW.equals(appType)) {
            records.addAll(applicationRepository.listWorkflows(userId, orgId, name, APP_TYPE_CHATFLOW));
        }
        return records;
    }

    private List<AppRecord> favoriteRecords(List<AppRecord> records, Set<String> favoriteKeys) {
        List<AppRecord> matches = new ArrayList<AppRecord>();
        for (AppRecord record : records) {
            if (favoriteKeys.contains(appKey(record.getAppType(), record.getAppId()))) {
                matches.add(record);
            }
        }
        return matches;
    }

    private List<AppRecord> privateRecords(List<AppRecord> records) {
        List<AppRecord> matches = new ArrayList<AppRecord>();
        for (AppRecord record : records) {
            if (PUBLISH_TYPE_PRIVATE.equals(record.getPublishType())) {
                matches.add(record);
            }
        }
        return matches;
    }

    private List<AppRecord> recordsFromHistories(String userId,
                                                 String orgId,
                                                 String name,
                                                 List<AppHistoryRecord> histories) {
        List<AppRecord> records = new ArrayList<AppRecord>();
        for (AppHistoryRecord history : histories) {
            AppRecord record = findApp(userId, orgId, history.getAppId(), history.getAppType());
            if (record == null || !matchesName(record, name)) {
                continue;
            }
            records.add(record);
        }
        return records;
    }

    private Set<String> favoriteKeys(List<AppFavoriteRecord> favorites) {
        Set<String> keys = new java.util.HashSet<String>();
        for (AppFavoriteRecord favorite : favorites) {
            keys.add(appKey(favorite.getAppType(), favorite.getAppId()));
        }
        return keys;
    }

    private AppRecord findApp(String userId, String orgId, String appId, String appType) {
        if (APP_TYPE_RAG.equals(appType)) {
            return applicationRepository.findRag(userId, orgId, appId);
        }
        if (isWorkflowLike(appType)) {
            return applicationRepository.findWorkflow(userId, orgId, appId, appType);
        }
        if (APP_TYPE_AGENT.equals(appType)) {
            return applicationRepository.findAssistant(userId, orgId, appId);
        }
        return null;
    }

    private boolean matchesName(AppRecord record, String name) {
        if (isBlank(name)) {
            return true;
        }
        String lower = name.toLowerCase(java.util.Locale.ENGLISH);
        return defaultIfBlank(record.getName(), "").toLowerCase(java.util.Locale.ENGLISH).contains(lower)
                || defaultIfBlank(record.getDesc(), "").toLowerCase(java.util.Locale.ENGLISH).contains(lower)
                || defaultIfBlank(record.getAppId(), "").toLowerCase(java.util.Locale.ENGLISH).contains(lower);
    }

    private String appKey(String appType, String appId) {
        return defaultIfBlank(appType, "") + ":" + defaultIfBlank(appId, "");
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private Map<String, Object> toFrontendCard(AppRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uniqueId", record.getAppType() + "_" + record.getAppId());
        item.put("appId", record.getAppId());
        item.put("appType", record.getAppType());
        if (isWorkflowLike(record.getAppType())) {
            item.put("workflowId", record.getAppId());
            item.put("workflow_id", record.getAppId());
        }
        item.put("avatar", avatar(record));
        item.put("name", record.getName());
        item.put("desc", record.getDesc());
        item.put("createdAt", formatMillis(record.getCreatedAt()));
        item.put("updatedAt", formatMillis(record.getUpdatedAt()));
        item.put("publishType", record.getPublishType());
        item.put("category", record.getCategory());
        item.put("version", latestVersion(record));
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
        item.put("workFlowInfos", config == null ? Collections.emptyList() : listMapOrDefault(config.getWorkflowInfosJson()));
        item.put("mcpInfos", config == null ? Collections.emptyList() : listMapOrDefault(config.getMcpInfosJson()));
        item.put("toolInfos", config == null ? Collections.emptyList() : listMapOrDefault(config.getToolInfosJson()));
        item.put("skillInfos", config == null ? Collections.emptyList() : listMapOrDefault(config.getSkillInfosJson()));
        item.put("multiAgentInfos", config == null ? Collections.emptyList() : listMapOrDefault(config.getMultiAgentInfosJson()));
        return item;
    }

    private Map<String, Object> toFrontendRag(AppRecord record, RagDraftConfigRecord config) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("ragId", record.getAppId());
        item.put("uuid", record.getAppId());
        item.put("avatar", avatar(record));
        item.put("name", record.getName());
        item.put("desc", record.getDesc());
        item.put("category", record.getCategory());
        item.put("publishType", record.getPublishType());
        item.put("modelConfig", config == null ? modelConfig() : mapOrDefault(config.getModelConfigJson(), modelConfig()));
        item.put("rerankConfig", config == null ? rerankConfig() : mapOrDefault(config.getRerankConfigJson(), rerankConfig()));
        item.put("qaRerankConfig", config == null ? rerankConfig() : mapOrDefault(config.getQaRerankConfigJson(), rerankConfig()));
        item.put("knowledgeBaseConfig", config == null
                ? knowledgeBaseConfig()
                : mapOrDefault(config.getKnowledgeBaseConfigJson(), knowledgeBaseConfig()));
        item.put("qaKnowledgeBaseConfig", config == null
                ? qaKnowledgeBaseConfig()
                : mapOrDefault(config.getQaKnowledgeBaseConfigJson(), qaKnowledgeBaseConfig()));
        item.put("safetyConfig", config == null ? safetyConfig() : mapOrDefault(config.getSafetyConfigJson(), safetyConfig()));
        item.put("visionConfig", config == null ? ragVisionConfig() : mapOrDefault(config.getVisionConfigJson(), ragVisionConfig()));
        Map<String, Object> publishConfig = new LinkedHashMap<>();
        publishConfig.put("publishType", defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
        item.put("appPublishConfig", publishConfig);
        return item;
    }

    private Map<String, Object> toFrontendWorkflow(AppRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("workflowId", record.getAppId());
        item.put("workflow_id", record.getAppId());
        item.put("appId", record.getAppId());
        item.put("appType", defaultIfBlank(record.getAppType(), APP_TYPE_WORKFLOW));
        item.put("uuid", record.getAppId());
        item.put("avatar", avatar(record));
        item.put("name", record.getName());
        item.put("desc", record.getDesc());
        item.put("category", record.getCategory());
        item.put("publishType", defaultIfBlank(record.getPublishType(), PUBLISH_TYPE_UNPUBLISHED));
        return item;
    }

    private Map<String, Object> chatflowIntelligence(AppRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("basic_info", chatflowBasicInfo(record));
        item.put("type", 1L);
        item.put("publish_info", chatflowPublishInfo(record));
        Map<String, Object> permission = new LinkedHashMap<>();
        permission.put("in_collaboration", false);
        permission.put("can_delete", true);
        permission.put("can_view", true);
        item.put("permission_info", permission);
        item.put("owner_info", chatflowOwnerInfo(record));
        Map<String, Object> favorite = new LinkedHashMap<>();
        favorite.put("is_fav", false);
        favorite.put("fav_time", "");
        item.put("favorite_info", favorite);
        return item;
    }

    private Map<String, Object> chatflowBasicInfo(AppRecord record) {
        Map<String, Object> basic = new LinkedHashMap<>();
        basic.put("id", chatflowApplicationId(record.getAppId()));
        basic.put("name", defaultIfBlank(record.getName(), ""));
        basic.put("description", defaultIfBlank(record.getDesc(), ""));
        basic.put("icon_uri", defaultIfBlank(record.getAvatarKey(), ""));
        basic.put("icon_url", defaultIfBlank(record.getAvatarPath(), ""));
        basic.put("space_id", record.getOrgId());
        basic.put("owner_id", record.getUserId());
        basic.put("create_time", record.getCreatedAt() == null ? 0L : record.getCreatedAt() / 1000L);
        basic.put("update_time", record.getUpdatedAt() == null ? 0L : record.getUpdatedAt() / 1000L);
        basic.put("status", 1L);
        basic.put("publish_time", 0L);
        basic.put("enterprise_id", "");
        basic.put("organization_id", 0L);
        return basic;
    }

    private Map<String, Object> chatflowPublishInfo(AppRecord record) {
        Map<String, Object> publish = new LinkedHashMap<>();
        publish.put("publish_time", "");
        publish.put("has_published", !isBlank(record.getPublishType()));
        publish.put("connectors", Collections.emptyList());
        return publish;
    }

    private Map<String, Object> chatflowOwnerInfo(AppRecord record) {
        Map<String, Object> owner = new LinkedHashMap<>();
        owner.put("user_id", record.getUserId());
        owner.put("nickname", "admin");
        owner.put("avatar_url", "");
        owner.put("user_unique_name", record.getUserId());
        owner.put("user_label", null);
        return owner;
    }

    private AppRecord findChatflowByApplicationId(String userId, String orgId, String applicationId) {
        List<AppRecord> chatflows = applicationRepository.listWorkflows(userId, orgId, "", APP_TYPE_CHATFLOW);
        for (AppRecord record : chatflows) {
            if (chatflowApplicationId(record.getAppId()).equals(applicationId)) {
                return record;
            }
        }
        return null;
    }

    private String chatflowApplicationId(String workflowId) {
        long hash = workflowId == null ? 0 : workflowId.hashCode();
        long positive = hash == Long.MIN_VALUE ? 0 : Math.abs(hash);
        return String.valueOf(1000000000L + positive);
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
        copied.setWorkflowInfosJson(source.getWorkflowInfosJson());
        copied.setMcpInfosJson(source.getMcpInfosJson());
        copied.setToolInfosJson(source.getToolInfosJson());
        copied.setSkillInfosJson(source.getSkillInfosJson());
        copied.setMultiAgentInfosJson(source.getMultiAgentInfosJson());
        return copied;
    }

    private RagDraftConfigRecord copyRagConfig(RagDraftConfigRecord source,
                                               String userId,
                                               String orgId,
                                               String newRagId,
                                               long now) {
        RagDraftConfigRecord copied = new RagDraftConfigRecord();
        copied.setCreatedAt(now);
        copied.setUpdatedAt(now);
        copied.setUserId(userId);
        copied.setOrgId(orgId);
        copied.setRagId(newRagId);
        if (source == null) {
            return copied;
        }
        copied.setModelConfigJson(source.getModelConfigJson());
        copied.setRerankConfigJson(source.getRerankConfigJson());
        copied.setQaRerankConfigJson(source.getQaRerankConfigJson());
        copied.setKnowledgeBaseConfigJson(source.getKnowledgeBaseConfigJson());
        copied.setQaKnowledgeBaseConfigJson(source.getQaKnowledgeBaseConfigJson());
        copied.setSafetyConfigJson(source.getSafetyConfigJson());
        copied.setVisionConfigJson(source.getVisionConfigJson());
        return copied;
    }

    private WorkflowDraftRecord workflowDraft(String userId, String orgId, String workflowId, long now, String schema) {
        WorkflowDraftRecord draft = new WorkflowDraftRecord();
        draft.setCreatedAt(now);
        draft.setUpdatedAt(now);
        draft.setUserId(userId);
        draft.setOrgId(orgId);
        draft.setWorkflowId(workflowId);
        draft.setSchemaJson(defaultIfBlank(schema, defaultWorkflowSchema(workflowId)));
        return draft;
    }

    private WorkflowDraftRecord copyWorkflowDraft(WorkflowDraftRecord source,
                                                  String userId,
                                                  String orgId,
                                                  String newWorkflowId,
                                                  long now) {
        WorkflowDraftRecord copied = workflowDraft(userId, orgId, newWorkflowId, now,
                source == null ? defaultWorkflowSchema(newWorkflowId) : source.getSchemaJson());
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

    private String nextRagCopyName(String userId, String orgId, String sourceName) {
        String prefix = sourceName + "_";
        int max = 0;
        for (String name : applicationRepository.listRagNamesByPrefix(userId, orgId, prefix)) {
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

    private String nextWorkflowCopyName(String userId, String orgId, String sourceName) {
        return nextWorkflowCopyName(userId, orgId, sourceName, APP_TYPE_WORKFLOW);
    }

    private String nextWorkflowCopyName(String userId, String orgId, String sourceName, String appType) {
        String prefix = sourceName + "_";
        int max = 0;
        for (String name : applicationRepository.listWorkflowNamesByPrefix(userId, orgId, prefix, appType)) {
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

    private void publishRag(AppPublishCommand command, String userId, String orgId, String publishType) {
        AppRecord record = applicationRepository.findRag(userId, orgId, command.getAppId());
        if (record == null) {
            throw new IllegalArgumentException("rag draft not found");
        }
        RagSnapshotRecord latest = applicationRepository.findLatestRagSnapshot(userId, orgId, command.getAppId());
        String version = isBlank(command.getVersion()) ? nextVersion(latestVersion(latest)) : command.getVersion().trim();
        validateVersion(version);
        if (latest != null && compareVersion(version, latest.getVersion()) <= 0) {
            throw new IllegalArgumentException("app version must be greater than latest version");
        }
        if (applicationRepository.findRagSnapshotByVersion(userId, orgId, command.getAppId(), version) != null) {
            throw new IllegalArgumentException("app version must be greater than latest version");
        }

        long now = clock.millis();
        RagDraftConfigRecord config = applicationRepository.findRagConfig(userId, orgId, command.getAppId());
        RagSnapshotRecord snapshot = new RagSnapshotRecord();
        snapshot.setCreatedAt(now);
        snapshot.setUpdatedAt(now);
        snapshot.setUserId(userId);
        snapshot.setOrgId(orgId);
        snapshot.setRagId(command.getAppId());
        snapshot.setVersion(version);
        snapshot.setDesc(defaultIfBlank(command.getDesc(), ""));
        snapshot.setCategory(record.getCategory());
        Map<String, Object> ragInfo = toFrontendRag(record, config);
        Map<String, Object> publishConfig = mapValue(ragInfo.get("appPublishConfig"));
        publishConfig.put("publishType", publishType);
        ragInfo.put("appPublishConfig", publishConfig);
        snapshot.setRagInfoJson(toJsonOrNull(ragInfo));
        snapshot.setRagConfigJson(toJsonOrNull(config));
        applicationRepository.saveRagSnapshot(snapshot);
        applicationRepository.updateRagPublishType(userId, orgId, command.getAppId(), publishType, now);
    }

    private void publishWorkflow(AppPublishCommand command, String userId, String orgId, String publishType) {
        publishWorkflow(command, userId, orgId, publishType, APP_TYPE_WORKFLOW);
    }

    private void publishWorkflow(AppPublishCommand command, String userId, String orgId, String publishType, String appType) {
        AppRecord record = applicationRepository.findWorkflow(userId, orgId, command.getAppId(), appType);
        if (record == null) {
            throw new IllegalArgumentException(appType + " draft not found");
        }
        WorkflowSnapshotRecord latest = applicationRepository.findLatestWorkflowSnapshot(userId, orgId, command.getAppId());
        String version = isBlank(command.getVersion()) ? nextVersion(latestVersion(latest)) : command.getVersion().trim();
        validateVersion(version);
        if (latest != null && compareVersion(version, latest.getVersion()) <= 0) {
            throw new IllegalArgumentException("app version must be greater than latest version");
        }
        if (applicationRepository.findWorkflowSnapshotByVersion(userId, orgId, command.getAppId(), version) != null) {
            throw new IllegalArgumentException("app version must be greater than latest version");
        }

        long now = clock.millis();
        WorkflowDraftRecord draft = applicationRepository.findWorkflowDraft(userId, orgId, command.getAppId());
        WorkflowSnapshotRecord snapshot = new WorkflowSnapshotRecord();
        snapshot.setCreatedAt(now);
        snapshot.setUpdatedAt(now);
        snapshot.setUserId(userId);
        snapshot.setOrgId(orgId);
        snapshot.setWorkflowId(command.getAppId());
        snapshot.setVersion(version);
        snapshot.setDesc(defaultIfBlank(command.getDesc(), ""));
        snapshot.setCategory(record.getCategory());
        Map<String, Object> workflowInfo = toFrontendWorkflow(record);
        workflowInfo.put("publishType", publishType);
        snapshot.setWorkflowInfoJson(toJsonOrNull(workflowInfo));
        snapshot.setWorkflowSchemaJson(draft == null
                ? defaultWorkflowSchema(command.getAppId())
                : defaultIfBlank(draft.getSchemaJson(), defaultWorkflowSchema(command.getAppId())));
        applicationRepository.saveWorkflowSnapshot(snapshot);
        applicationRepository.updateWorkflowPublishType(userId, orgId, command.getAppId(), appType, publishType, now);
    }

    private void rollbackRagVersion(AppVersionRollbackCommand command, VersionContext context) {
        AppRecord existing = applicationRepository.findRag(context.userId, context.orgId, context.appId);
        if (existing == null) {
            throw new IllegalArgumentException("rag draft not found");
        }
        RagSnapshotRecord snapshot = applicationRepository.findRagSnapshotByVersion(
                context.userId, context.orgId, context.appId, command.getVersion());
        if (snapshot == null) {
            throw new IllegalArgumentException("rag snapshot not found");
        }
        Map<String, Object> snapshotDraft = mapOrDefault(snapshot.getRagInfoJson(), new LinkedHashMap<String, Object>());
        AppRecord restored = restoreRecord(existing, snapshotDraft, clock.millis());
        RagDraftConfigRecord restoredConfig = restoreRagConfig(
                snapshotDraft, context.userId, context.orgId, context.appId, restored.getUpdatedAt());
        if (!applicationRepository.rollbackRag(restored, restoredConfig)) {
            throw new IllegalArgumentException("rag draft not found");
        }
    }

    private void rollbackWorkflowVersion(AppVersionRollbackCommand command, VersionContext context) {
        AppRecord existing = applicationRepository.findWorkflow(context.userId, context.orgId, context.appId, context.appType);
        if (existing == null) {
            throw new IllegalArgumentException(context.appType + " draft not found");
        }
        WorkflowSnapshotRecord snapshot = applicationRepository.findWorkflowSnapshotByVersion(
                context.userId, context.orgId, context.appId, command.getVersion());
        if (snapshot == null) {
            throw new IllegalArgumentException("workflow snapshot not found");
        }
        Map<String, Object> snapshotDraft = mapOrDefault(snapshot.getWorkflowInfoJson(), new LinkedHashMap<String, Object>());
        AppRecord restored = restoreRecord(existing, snapshotDraft, clock.millis());
        WorkflowDraftRecord restoredDraft = workflowDraft(
                context.userId,
                context.orgId,
                context.appId,
                restored.getUpdatedAt(),
                snapshot.getWorkflowSchemaJson());
        if (!applicationRepository.rollbackWorkflow(restored, restoredDraft)) {
            throw new IllegalArgumentException("workflow draft not found");
        }
    }

    private VersionContext versionContext(AppVersionQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("app version query is required");
        }
        return versionContext(query.getAppId(), query.getAppType(), query.getUserId(), query.getOrgId());
    }

    private VersionContext versionContext(String appId, String appType, String userId, String orgId) {
        String normalizedAppType = normalizeAppType(appType);
        if (!APP_TYPE_AGENT.equals(normalizedAppType)
                && !APP_TYPE_RAG.equals(normalizedAppType)
                && !isWorkflowLike(normalizedAppType)) {
            throw new IllegalArgumentException("unsupported app type");
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

    private String normalizeAppType(String appType) {
        return normalizeAgentAppType(appType);
    }

    private String normalizeAgentAppType(String appType) {
        String normalized = defaultIfBlank(appType, APP_TYPE_AGENT);
        if (APP_TYPE_ASSISTANT.equals(normalized)) {
            return APP_TYPE_AGENT;
        }
        return normalized;
    }

    private boolean isWorkflowLike(String appType) {
        return APP_TYPE_WORKFLOW.equals(appType) || APP_TYPE_CHATFLOW.equals(appType);
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

    private String nextVersion(String latest) {
        if (isBlank(latest)) {
            return DEFAULT_VERSION;
        }
        int[] parts = versionParts(latest);
        return "v" + parts[0] + "." + parts[1] + "." + (parts[2] + 1);
    }

    private String latestVersion(AssistantSnapshotRecord latest) {
        return latest == null ? "" : latest.getVersion();
    }

    private String latestVersion(RagSnapshotRecord latest) {
        return latest == null ? "" : latest.getVersion();
    }

    private String latestVersion(WorkflowSnapshotRecord latest) {
        return latest == null ? "" : latest.getVersion();
    }

    private String latestVersion(AppRecord record) {
        if (APP_TYPE_RAG.equals(record.getAppType())) {
            return latestVersion(applicationRepository.findLatestRagSnapshot(
                    record.getUserId(), record.getOrgId(), record.getAppId()));
        }
        if (isWorkflowLike(record.getAppType())) {
            return latestVersion(applicationRepository.findLatestWorkflowSnapshot(
                    record.getUserId(), record.getOrgId(), record.getAppId()));
        }
        AssistantSnapshotRecord latest = applicationRepository.findLatestAssistantSnapshot(
                record.getUserId(), record.getOrgId(), record.getAppId());
        return latestVersion(latest);
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

    private AppVersionInfo toVersionInfo(RagSnapshotRecord snapshot, String publishType) {
        return new AppVersionInfo(
                snapshot.getVersion(),
                defaultIfBlank(snapshot.getDesc(), ""),
                formatMillis(snapshot.getCreatedAt()),
                publishType);
    }

    private AppVersionInfo toVersionInfo(WorkflowSnapshotRecord snapshot, String publishType) {
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
        config.setWorkflowInfosJson(toJsonOrNull(snapshotDraft.get("workFlowInfos")));
        config.setMcpInfosJson(toJsonOrNull(snapshotDraft.get("mcpInfos")));
        config.setToolInfosJson(toJsonOrNull(snapshotDraft.get("toolInfos")));
        config.setSkillInfosJson(toJsonOrNull(snapshotDraft.get("skillInfos")));
        config.setMultiAgentInfosJson(toJsonOrNull(snapshotDraft.get("multiAgentInfos")));
        return config;
    }

    private RagDraftConfigRecord restoreRagConfig(Map<String, Object> snapshotDraft,
                                                  String userId,
                                                  String orgId,
                                                  String ragId,
                                                  long now) {
        RagDraftConfigRecord config = new RagDraftConfigRecord();
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        config.setUserId(userId);
        config.setOrgId(orgId);
        config.setRagId(ragId);
        config.setModelConfigJson(toJsonOrNull(snapshotDraft.get("modelConfig")));
        config.setRerankConfigJson(toJsonOrNull(snapshotDraft.get("rerankConfig")));
        config.setQaRerankConfigJson(toJsonOrNull(snapshotDraft.get("qaRerankConfig")));
        config.setKnowledgeBaseConfigJson(toJsonOrNull(snapshotDraft.get("knowledgeBaseConfig")));
        config.setQaKnowledgeBaseConfigJson(toJsonOrNull(snapshotDraft.get("qaKnowledgeBaseConfig")));
        config.setSafetyConfigJson(toJsonOrNull(snapshotDraft.get("safetyConfig")));
        config.setVisionConfigJson(toJsonOrNull(snapshotDraft.get("visionConfig")));
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

    private Map<String, Object> qaKnowledgeBaseConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("keywordPriority", 0.8);
        config.put("matchType", "mix");
        config.put("priorityMatch", 1);
        config.put("rerankModelId", "");
        config.put("semanticsPriority", 0.2);
        config.put("topK", 5);
        config.put("threshold", 0.4);
        config.put("maxHistory", 0);

        Map<String, Object> knowledgeBaseConfig = new LinkedHashMap<>();
        knowledgeBaseConfig.put("config", config);
        knowledgeBaseConfig.put("knowledgebases", Collections.emptyList());
        return knowledgeBaseConfig;
    }

    private Map<String, Object> safetyConfig() {
        Map<String, Object> safetyConfig = new LinkedHashMap<>();
        safetyConfig.put("enable", false);
        safetyConfig.put("tables", Collections.emptyList());
        return safetyConfig;
    }

    private Map<String, Object> ragVisionConfig() {
        Map<String, Object> visionConfig = new LinkedHashMap<>();
        visionConfig.put("picNum", 0);
        return visionConfig;
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

    private String defaultWorkflowSchema(String workflowId) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("workflowId", workflowId);
        schema.put("nodes", Collections.emptyList());
        schema.put("edges", Collections.emptyList());
        return toJsonOrNull(schema);
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

    private String formatOptionalMillis(Long millis) {
        if (millis == null || millis <= 0) {
            return "";
        }
        return formatMillis(millis);
    }

    private String newAssistantId() {
        return "assistant-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String newRagId() {
        return "rag-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String newWorkflowId() {
        return newFlowId(APP_TYPE_WORKFLOW);
    }

    private String newFlowId(String appType) {
        String prefix = APP_TYPE_CHATFLOW.equals(appType) ? "chatflow-" : "workflow-";
        return prefix + UUID.randomUUID().toString().replace("-", "");
    }

    private String newConversationId() {
        return "conversation-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String newDetailId() {
        return "detail-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String newApiKey() {
        for (int i = 0; i < 8; i++) {
            String key = UUID.randomUUID().toString().replace("-", "");
            if (applicationRepository.findApiKeyByKey(key) == null
                    && applicationRepository.findAppKeyByKey(key) == null) {
                return key;
            }
        }
        throw new IllegalStateException("api key generation failed");
    }

    private String newAppUrlSuffix() {
        for (int i = 0; i < 8; i++) {
            String suffix = UUID.randomUUID().toString().replace("-", "");
            if (applicationRepository.findAppUrlBySuffix(suffix) == null) {
                return suffix;
            }
        }
        throw new IllegalStateException("app url suffix generation failed");
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

    private List<Map<String, Object>> listMapOrDefault(String json) {
        if (isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(json, MAP_LIST_TYPE);
            return list == null ? new ArrayList<Map<String, Object>>() : list;
        } catch (Exception ex) {
            throw new IllegalStateException("assistant draft resource config is invalid", ex);
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
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

    private static class StatisticQueryContext {
        private final String userId;
        private final String orgId;
        private final String startDate;
        private final String endDate;
        private final String previousStartDate;
        private final String previousEndDate;
        private final List<String> ids;
        private final String type;
        private final List<String> dates;

        private StatisticQueryContext(String userId,
                                      String orgId,
                                      String startDate,
                                      String endDate,
                                      String previousStartDate,
                                      String previousEndDate,
                                      List<String> ids,
                                      String type,
                                      List<String> dates) {
            this.userId = userId;
            this.orgId = orgId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.previousStartDate = previousStartDate;
            this.previousEndDate = previousEndDate;
            this.ids = ids;
            this.type = type;
            this.dates = dates;
        }
    }

    private static class ApiKeyQueryContext {
        private final String userId;
        private final String orgId;
        private final String startDate;
        private final String endDate;
        private final String previousStartDate;
        private final String previousEndDate;
        private final List<String> apiKeyIds;
        private final List<String> methodPaths;
        private final List<String> dates;

        private ApiKeyQueryContext(String userId,
                                   String orgId,
                                   String startDate,
                                   String endDate,
                                   String previousStartDate,
                                   String previousEndDate,
                                   List<String> apiKeyIds,
                                   List<String> methodPaths,
                                   List<String> dates) {
            this.userId = userId;
            this.orgId = orgId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.previousStartDate = previousStartDate;
            this.previousEndDate = previousEndDate;
            this.apiKeyIds = apiKeyIds;
            this.methodPaths = methodPaths;
            this.dates = dates;
        }
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
