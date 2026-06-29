package com.unicomai.wanwu.service.app.rpc;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
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
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Shanghai"));

    private final ApplicationRepository applicationRepository;
    private final Clock clock;

    @Autowired
    public AppServiceImpl(ApplicationRepository applicationRepository) {
        this(applicationRepository, Clock.systemUTC());
    }

    public AppServiceImpl(ApplicationRepository applicationRepository, Clock clock) {
        this.applicationRepository = applicationRepository;
        this.clock = clock;
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
        return toFrontendDraft(record);
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

    private Map<String, Object> toFrontendDraft(AppRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("assistantId", record.getAppId());
        item.put("uuid", record.getAppId());
        item.put("newAgent", true);
        item.put("avatar", avatar(record));
        item.put("name", record.getName());
        item.put("desc", record.getDesc());
        item.put("category", record.getCategory());
        item.put("publishType", record.getPublishType());
        item.put("prologue", "");
        item.put("instructions", "");
        item.put("memoryConfig", memoryConfig());
        item.put("visionConfig", visionConfig());
        item.put("knowledgeBaseConfig", knowledgeBaseConfig());
        item.put("modelConfig", modelConfig());
        item.put("rerankConfig", rerankConfig());
        item.put("recommendQuestion", Collections.emptyList());
        item.put("safetyConfig", null);
        item.put("recommendConfig", null);
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
