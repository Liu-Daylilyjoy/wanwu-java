package com.unicomai.wanwu.service.app.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
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

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class AppServiceImpl implements AppService {

    private static final String APP_TYPE_AGENT = "agent";
    private static final String PUBLISH_TYPE_PRIVATE = "private";
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_ORG_ID = "default-org";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<List<String>>() {
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
        record.setPublishType(PUBLISH_TYPE_PRIVATE);
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

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.APP, "App Service", "app");
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
        item.put("version", "v0.0.1");
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

    private String defaultIfBlank(String value, String defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
