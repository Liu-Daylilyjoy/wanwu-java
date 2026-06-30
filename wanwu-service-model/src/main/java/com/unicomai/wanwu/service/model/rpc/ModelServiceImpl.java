package com.unicomai.wanwu.service.model.rpc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogDeleteCommand;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordSaveCommand;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogSaveCommand;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.model.dto.ModelStatusCommand;
import com.unicomai.wanwu.api.model.dto.ModelTypeInfo;
import com.unicomai.wanwu.api.model.dto.ModelTypeQuery;
import com.unicomai.wanwu.api.model.dto.ModelUpsertCommand;
import com.unicomai.wanwu.api.model.dto.ProviderListQuery;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeInfo;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeResult;
import com.unicomai.wanwu.api.model.dto.RecommendModelInfo;
import com.unicomai.wanwu.api.model.dto.RecommendModelQuery;
import com.unicomai.wanwu.api.model.dto.RecommendModelResult;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import com.unicomai.wanwu.service.model.persistence.entity.ModelRecordEntity;
import com.unicomai.wanwu.service.model.persistence.mapper.ModelRecordMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class ModelServiceImpl implements ModelService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private static final TypeReference<List<ModelExperienceDialogRecordInfo>> RECORD_LIST_TYPE =
            new TypeReference<List<ModelExperienceDialogRecordInfo>>() {
            };
    private static final String TYPE_MODEL = "model";
    private static final String TYPE_MODEL_DELETED = "model_deleted";
    private static final String TYPE_DIALOG = "dialog";
    private static final String TYPE_RECORDS = "records";
    private static final String RECORDS_ID = "all";
    private static final String DEFAULT_USER_ID = "dev-admin";
    private static final String DEFAULT_ORG_ID = "default-org";
    private static final String CREATED_AT = "2026-06-30 00:00:00";
    private static final String SCOPE_PRIVATE = "1";
    private static final String SCOPE_PUBLIC = "2";
    private static final String SCOPE_ORG = "3";
    private static final String FILTER_PUBLIC = "public";
    private static final String FILTER_PRIVATE = "private";
    private static final String IMPORT_BUILTIN = "builtin";
    private static final String IMPORT_EXTERNAL = "external";
    private static final String MODEL_TYPE_LLM = "llm";
    private static final String MODEL_TYPE_EMBEDDING = "embedding";
    private static final String MODEL_TYPE_RERANK = "rerank";
    private static final String MODEL_TYPE_MULTI_EMBEDDING = "multimodal-embedding";
    private static final String MODEL_TYPE_MULTI_RERANK = "multimodal-rerank";
    private static final String MODEL_TYPE_OCR = "ocr";
    private static final String MODEL_TYPE_GUI = "gui";
    private static final String MODEL_TYPE_PDF = "pdf-parser";
    private static final String MODEL_TYPE_ASR = "sync-asr";
    private static final long CREATED_AT_MILLIS = 1782806400000L;

    private final Map<String, ModelInfo> models = new LinkedHashMap<String, ModelInfo>();
    private final Map<String, ExperienceDialogState> experienceDialogs = new LinkedHashMap<String, ExperienceDialogState>();
    private final Map<String, String> experienceDialogIdsBySession = new LinkedHashMap<String, String>();
    private final List<ModelExperienceDialogRecordInfo> experienceDialogRecords = new ArrayList<ModelExperienceDialogRecordInfo>();
    private final AtomicLong nextId = new AtomicLong(100);
    private final AtomicLong nextExperienceId = new AtomicLong(1000);
    @Autowired(required = false)
    private ModelRecordMapper modelRecordMapper;

    public ModelServiceImpl() {
        seedBuiltInModels();
    }

    ModelServiceImpl(ModelRecordMapper modelRecordMapper) {
        this();
        this.modelRecordMapper = modelRecordMapper;
        loadPersistedRecords();
    }

    @PostConstruct
    public synchronized void loadPersistedRecords() {
        if (modelRecordMapper == null) {
            return;
        }
        for (ModelRecordEntity record : modelRecordMapper.selectByType(TYPE_MODEL_DELETED)) {
            models.remove(record.getRecordId());
            bumpSequence(nextId, record.getRecordId());
        }
        for (ModelRecordEntity record : modelRecordMapper.selectByType(TYPE_MODEL)) {
            ModelInfo model = read(record, ModelInfo.class);
            models.put(record.getRecordId(), model);
            bumpSequence(nextId, record.getRecordId());
        }
        for (ModelRecordEntity record : modelRecordMapper.selectByType(TYPE_DIALOG)) {
            ExperienceDialogState dialog = dialogFromMap(readMap(record));
            experienceDialogs.put(dialog.id, dialog);
            experienceDialogIdsBySession.put(dialog.sessionId, dialog.id);
            bumpSequence(nextExperienceId, dialog.id);
        }
        for (ModelRecordEntity record : modelRecordMapper.selectByType(TYPE_RECORDS)) {
            if (RECORDS_ID.equals(record.getRecordId())) {
                experienceDialogRecords.clear();
                experienceDialogRecords.addAll(readRecords(record));
            }
        }
    }

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    @Override
    public synchronized ModelInfo importModel(ModelUpsertCommand command) {
        validateUpsert(command, false);
        ModelInfo model = fromCommand(command);
        String id = String.valueOf(nextId.incrementAndGet());
        model.setModelId(id);
        model.setUuid("model-uuid-" + id);
        model.setCreatedAt(CREATED_AT);
        model.setUpdatedAt(CREATED_AT);
        model.setIsActive(true);
        if (isBlank(model.getImportSource())) {
            model.setImportSource(IMPORT_EXTERNAL);
        }
        models.put(id, copyForUser(model, command.getUserId()));
        saveRecord(TYPE_MODEL, id, model);
        deleteRecord(TYPE_MODEL_DELETED, id);
        return copyForUser(model, command.getUserId());
    }

    @Override
    public synchronized void updateModel(ModelUpsertCommand command) {
        validateUpsert(command, true);
        ModelInfo existing = existing(command.getModelId());
        ModelInfo updated = fromCommand(command);
        updated.setModelId(existing.getModelId());
        updated.setUuid(existing.getUuid());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(CREATED_AT);
        updated.setIsActive(existing.getIsActive());
        if (isBlank(updated.getImportSource())) {
            updated.setImportSource(existing.getImportSource());
        }
        models.put(updated.getModelId(), copyForUser(updated, command.getUserId()));
        saveRecord(TYPE_MODEL, updated.getModelId(), updated);
        deleteRecord(TYPE_MODEL_DELETED, updated.getModelId());
    }

    @Override
    public synchronized void deleteModel(String userId, String orgId, String modelId) {
        existing(modelId);
        models.remove(modelId);
        deleteRecord(TYPE_MODEL, modelId);
        saveRecord(TYPE_MODEL_DELETED, modelId, singletonMap("modelId", modelId));
    }

    @Override
    public synchronized void changeModelStatus(ModelStatusCommand command) {
        if (command == null || isBlank(command.getModelId())) {
            throw new IllegalArgumentException("modelId cannot be empty");
        }
        ModelInfo model = existing(command.getModelId());
        model.setIsActive(Boolean.TRUE.equals(command.getIsActive()));
        model.setUpdatedAt(CREATED_AT);
        saveRecord(TYPE_MODEL, model.getModelId(), model);
    }

    @Override
    public synchronized ModelInfo getModel(String userId, String orgId, String modelId) {
        return copyForUser(existing(modelId), userId);
    }

    @Override
    public synchronized ModelListResult listModels(ModelListQuery query) {
        ModelListQuery safe = query == null ? new ModelListQuery() : query;
        List<ModelInfo> result = new ArrayList<ModelInfo>();
        for (ModelInfo model : models.values()) {
            if (!matches(safe, model)) {
                continue;
            }
            result.add(copyForUser(model, safe.getUserId()));
        }
        return new ModelListResult(result, result.size());
    }

    @Override
    public synchronized ModelListResult listTypeModels(ModelTypeQuery query) {
        ModelTypeQuery safe = query == null ? new ModelTypeQuery() : query;
        List<ModelInfo> result = new ArrayList<ModelInfo>();
        for (ModelInfo model : models.values()) {
            if (!Boolean.TRUE.equals(model.getIsActive())) {
                continue;
            }
            if (!isBlank(safe.getModelType()) && !safe.getModelType().equals(model.getModelType())) {
                continue;
            }
            result.add(copyForUser(model, safe.getUserId()));
        }
        return new ModelListResult(result, result.size());
    }

    @Override
    public ProviderModelTypeResult listImportProviders(ProviderListQuery query) {
        ProviderListQuery safe = query == null ? new ProviderListQuery() : query;
        List<ProviderModelTypeInfo> result = new ArrayList<ProviderModelTypeInfo>();
        for (ProviderModelTypeInfo provider : providerModelTypes()) {
            if (!isBlank(safe.getProvider()) && !containsIgnoreCase(provider.getName(), safe.getProvider())
                    && !containsIgnoreCase(provider.getKey(), safe.getProvider())) {
                continue;
            }
            ProviderModelTypeInfo filtered = new ProviderModelTypeInfo();
            filtered.setKey(provider.getKey());
            filtered.setName(provider.getName());
            List<ModelTypeInfo> children = new ArrayList<ModelTypeInfo>();
            for (ModelTypeInfo type : provider.getChildren()) {
                if (isBlank(safe.getModelType()) || safe.getModelType().equals(type.getKey())) {
                    children.add(type);
                }
            }
            if (!children.isEmpty()) {
                filtered.setChildren(children);
                result.add(filtered);
            }
        }
        return new ProviderModelTypeResult(result, result.size());
    }

    @Override
    public RecommendModelResult recommendModels(RecommendModelQuery query) {
        RecommendModelQuery safe = query == null ? new RecommendModelQuery() : query;
        List<RecommendModelInfo> result = new ArrayList<RecommendModelInfo>();
        for (RecommendModelInfo model : recommendCatalog(safe.getProvider(), safe.getModelType())) {
            result.add(model);
        }
        return new RecommendModelResult(result, result.size());
    }

    @Override
    public synchronized ModelExperienceDialogInfo saveModelExperienceDialog(ModelExperienceDialogSaveCommand command) {
        validateExperienceDialog(command);
        String existingId = experienceDialogIdsBySession.get(command.getSessionId());
        if (existingId != null) {
            ExperienceDialogState existing = experienceDialogs.get(existingId);
            existing.modelSetting = defaultIfBlank(command.getModelSetting(), "");
            saveRecord(TYPE_DIALOG, existing.id, dialogToMap(existing));
            return toDialogInfo(existing);
        }
        String id = String.valueOf(nextExperienceId.incrementAndGet());
        ExperienceDialogState dialog = new ExperienceDialogState();
        dialog.id = id;
        dialog.userId = defaultIfBlank(command.getUserId(), DEFAULT_USER_ID);
        dialog.orgId = defaultIfBlank(command.getOrgId(), DEFAULT_ORG_ID);
        dialog.modelId = command.getModelId();
        dialog.sessionId = command.getSessionId();
        dialog.title = defaultIfBlank(command.getTitle(), "");
        dialog.modelSetting = defaultIfBlank(command.getModelSetting(), "");
        dialog.createdAt = CREATED_AT_MILLIS;
        experienceDialogs.put(id, dialog);
        experienceDialogIdsBySession.put(dialog.sessionId, id);
        saveRecord(TYPE_DIALOG, id, dialogToMap(dialog));
        return toDialogInfo(dialog);
    }

    @Override
    public synchronized ModelExperienceDialogListResult listModelExperienceDialogs(ModelExperienceDialogListQuery query) {
        ModelExperienceDialogListQuery safe = query == null ? new ModelExperienceDialogListQuery() : query;
        List<ModelExperienceDialogInfo> result = new ArrayList<ModelExperienceDialogInfo>();
        for (ExperienceDialogState dialog : experienceDialogs.values()) {
            if (!matchesExperienceOwner(safe.getUserId(), safe.getOrgId(), dialog)) {
                continue;
            }
            result.add(0, toDialogInfo(dialog));
        }
        return new ModelExperienceDialogListResult(result, result.size());
    }

    @Override
    public synchronized void deleteModelExperienceDialog(ModelExperienceDialogDeleteCommand command) {
        if (command == null || isBlank(command.getModelExperienceId())) {
            throw new IllegalArgumentException("modelExperienceId cannot be empty");
        }
        ExperienceDialogState dialog = experienceDialogs.get(command.getModelExperienceId());
        if (dialog == null || !matchesExperienceOwner(command.getUserId(), command.getOrgId(), dialog)) {
            return;
        }
        experienceDialogs.remove(command.getModelExperienceId());
        experienceDialogIdsBySession.remove(dialog.sessionId);
        deleteRecord(TYPE_DIALOG, command.getModelExperienceId());
        for (int i = experienceDialogRecords.size() - 1; i >= 0; i--) {
            if (command.getModelExperienceId().equals(experienceDialogRecords.get(i).getModelExperienceId())) {
                experienceDialogRecords.remove(i);
            }
        }
        saveExperienceRecords();
    }

    @Override
    public synchronized void saveModelExperienceDialogRecord(ModelExperienceDialogRecordSaveCommand command) {
        validateExperienceRecord(command);
        experienceDialogRecords.add(toRecordInfo(command));
        saveExperienceRecords();
    }

    @Override
    public synchronized ModelExperienceDialogRecordListResult listModelExperienceDialogRecords(ModelExperienceDialogRecordQuery query) {
        ModelExperienceDialogRecordQuery safe = query == null ? new ModelExperienceDialogRecordQuery() : query;
        List<ModelExperienceDialogRecordInfo> result = new ArrayList<ModelExperienceDialogRecordInfo>();
        for (ModelExperienceDialogRecordInfo record : experienceDialogRecords) {
            if (!isBlank(safe.getModelExperienceId()) && !safe.getModelExperienceId().equals(record.getModelExperienceId())) {
                continue;
            }
            if (!isBlank(safe.getSessionId()) && !safe.getSessionId().equals(record.getSessionId())) {
                continue;
            }
            result.add(copyRecord(record));
        }
        return new ModelExperienceDialogRecordListResult(result, result.size());
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.MODEL, "Model Service", "model");
    }

    private <T> T read(ModelRecordEntity record, Class<T> type) {
        try {
            return JSON.readValue(record.getPayload(), type);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read model record " + record.getRecordType()
                    + "/" + record.getRecordId(), ex);
        }
    }

    private Map<String, Object> readMap(ModelRecordEntity record) {
        try {
            return JSON.readValue(record.getPayload(), MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read model record " + record.getRecordType()
                    + "/" + record.getRecordId(), ex);
        }
    }

    private List<ModelExperienceDialogRecordInfo> readRecords(ModelRecordEntity record) {
        try {
            return JSON.readValue(record.getPayload(), RECORD_LIST_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read model experience records", ex);
        }
    }

    private void saveRecord(String recordType, String recordId, Object payload) {
        if (modelRecordMapper == null || isBlank(recordId)) {
            return;
        }
        try {
            long now = System.currentTimeMillis();
            ModelRecordEntity entity = new ModelRecordEntity();
            entity.setRecordType(recordType);
            entity.setRecordId(recordId);
            entity.setPayload(JSON.writeValueAsString(payload));
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            modelRecordMapper.upsertRecord(entity);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to save model record " + recordType + "/" + recordId, ex);
        }
    }

    private void deleteRecord(String recordType, String recordId) {
        if (modelRecordMapper == null || isBlank(recordId)) {
            return;
        }
        modelRecordMapper.deleteRecord(recordType, recordId);
    }

    private void saveExperienceRecords() {
        saveRecord(TYPE_RECORDS, RECORDS_ID, experienceDialogRecords);
    }

    private Map<String, Object> dialogToMap(ExperienceDialogState dialog) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", dialog.id);
        result.put("userId", dialog.userId);
        result.put("orgId", dialog.orgId);
        result.put("modelId", dialog.modelId);
        result.put("sessionId", dialog.sessionId);
        result.put("title", dialog.title);
        result.put("modelSetting", dialog.modelSetting);
        result.put("createdAt", dialog.createdAt);
        return result;
    }

    private ExperienceDialogState dialogFromMap(Map<String, Object> map) {
        ExperienceDialogState dialog = new ExperienceDialogState();
        dialog.id = stringValue(map, "id", "");
        dialog.userId = stringValue(map, "userId", DEFAULT_USER_ID);
        dialog.orgId = stringValue(map, "orgId", DEFAULT_ORG_ID);
        dialog.modelId = stringValue(map, "modelId", "");
        dialog.sessionId = stringValue(map, "sessionId", "");
        dialog.title = stringValue(map, "title", "");
        dialog.modelSetting = stringValue(map, "modelSetting", "");
        dialog.createdAt = longValue(map, "createdAt", CREATED_AT_MILLIS);
        return dialog;
    }

    private void bumpSequence(AtomicLong sequence, String value) {
        if (isBlank(value)) {
            return;
        }
        try {
            long numeric = Long.parseLong(value);
            if (numeric > sequence.get()) {
                sequence.set(numeric);
            }
        } catch (NumberFormatException ignored) {
            // Human-supplied IDs do not participate in numeric sequence continuation.
        }
    }

    private void seedBuiltInModels() {
        if (!models.isEmpty()) {
            return;
        }
        putBuiltIn("1", "DeepSeek", MODEL_TYPE_LLM, "deepseek-chat", "DeepSeek Chat",
                config("apiKey", "dev-model-key", "inferUrl", "https://api.deepseek.com/v1",
                        "functionCalling", "toolCall", "thinkingSupport", "support", "visionSupport", "noSupport"),
                SCOPE_PRIVATE);
        putBuiltIn("2", "OpenAI-API-compatible", MODEL_TYPE_EMBEDDING, "text-embedding-3-small",
                "Text Embedding Small",
                config("apiKey", "dev-model-key", "inferUrl", "https://api.openai.com/v1"),
                SCOPE_PRIVATE);
        putBuiltIn("3", "Jina", MODEL_TYPE_RERANK, "jina-reranker-v2-base-multilingual",
                "Jina Reranker",
                config("apiKey", "dev-model-key", "inferUrl", "https://api.jina.ai/v1"),
                SCOPE_PRIVATE);
    }

    private void putBuiltIn(String id, String provider, String modelType, String model, String displayName,
                            Map<String, Object> config, String scopeType) {
        ModelInfo info = new ModelInfo();
        info.setModelId(id);
        info.setUuid("model-uuid-" + id);
        info.setProvider(provider);
        info.setModelType(modelType);
        info.setModel(model);
        info.setDisplayName(displayName);
        info.setAvatar(singletonMap("path", ""));
        info.setPublishDate("2026-06-30");
        info.setIsActive(true);
        info.setUserId(DEFAULT_USER_ID);
        info.setOrgId(DEFAULT_ORG_ID);
        info.setCreatedAt(CREATED_AT);
        info.setUpdatedAt(CREATED_AT);
        info.setModelDesc("Built-in Docker development model");
        info.setConfig(config);
        info.setScopeType(scopeType);
        info.setAllowEdit(true);
        info.setImportSource(IMPORT_BUILTIN);
        info.setTags(tags(scopeType, IMPORT_BUILTIN, modelType));
        models.put(id, info);
    }

    private ModelInfo fromCommand(ModelUpsertCommand command) {
        ModelInfo info = new ModelInfo();
        info.setProvider(command.getProvider());
        info.setModelType(command.getModelType());
        info.setModel(command.getModel());
        info.setDisplayName(isBlank(command.getDisplayName()) ? command.getModel() : command.getDisplayName());
        info.setAvatar(avatar(command.getAvatarPath()));
        info.setPublishDate(defaultIfBlank(command.getPublishDate(), "2026-06-30"));
        info.setUserId(defaultIfBlank(command.getUserId(), DEFAULT_USER_ID));
        info.setOrgId(defaultIfBlank(command.getOrgId(), DEFAULT_ORG_ID));
        info.setModelDesc(defaultIfBlank(command.getModelDesc(), ""));
        info.setConfig(command.getConfig());
        info.setScopeType(defaultIfBlank(command.getScopeType(), SCOPE_PRIVATE));
        info.setImportSource(defaultIfBlank(command.getImportSource(), IMPORT_EXTERNAL));
        info.setTags(tags(info.getScopeType(), info.getImportSource(), info.getModelType()));
        info.setAllowEdit(true);
        return info;
    }

    private boolean matches(ModelListQuery query, ModelInfo model) {
        if (!isBlank(query.getModelType()) && !query.getModelType().equals(model.getModelType())) {
            return false;
        }
        if (!isBlank(query.getProvider()) && !query.getProvider().equals(model.getProvider())) {
            return false;
        }
        if (!isBlank(query.getDisplayName()) && !containsIgnoreCase(model.getDisplayName(), query.getDisplayName())
                && !containsIgnoreCase(model.getModel(), query.getDisplayName())) {
            return false;
        }
        if (!isBlank(query.getScopeType()) && !query.getScopeType().equals(model.getScopeType())) {
            return false;
        }
        if (FILTER_PUBLIC.equals(query.getFilterScope())) {
            return SCOPE_PUBLIC.equals(model.getScopeType())
                    || (SCOPE_ORG.equals(model.getScopeType()) && !defaultIfBlank(query.getUserId(), "").equals(model.getUserId()));
        }
        if (FILTER_PRIVATE.equals(query.getFilterScope())) {
            return SCOPE_PRIVATE.equals(model.getScopeType())
                    || (SCOPE_ORG.equals(model.getScopeType()) && defaultIfBlank(query.getUserId(), "").equals(model.getUserId()));
        }
        return true;
    }

    private ModelInfo copyForUser(ModelInfo source, String userId) {
        ModelInfo copy = new ModelInfo();
        copy.setModelId(source.getModelId());
        copy.setUuid(source.getUuid());
        copy.setProvider(source.getProvider());
        copy.setModelType(source.getModelType());
        copy.setModel(source.getModel());
        copy.setDisplayName(source.getDisplayName());
        copy.setAvatar(new LinkedHashMap<String, Object>(source.getAvatar()));
        copy.setPublishDate(source.getPublishDate());
        copy.setIsActive(source.getIsActive());
        copy.setUserId(source.getUserId());
        copy.setOrgId(source.getOrgId());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        copy.setModelDesc(source.getModelDesc());
        copy.setTags(copyTags(source.getTags()));
        copy.setConfig(new LinkedHashMap<String, Object>(source.getConfig()));
        copy.setScopeType(source.getScopeType());
        copy.setAllowEdit(isBlank(userId) || userId.equals(source.getUserId()));
        copy.setImportSource(source.getImportSource());
        return copy;
    }

    private List<Map<String, Object>> copyTags(List<Map<String, Object>> tags) {
        List<Map<String, Object>> copy = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> tag : tags) {
            copy.add(new LinkedHashMap<String, Object>(tag));
        }
        return copy;
    }

    private ModelInfo existing(String modelId) {
        if (isBlank(modelId) || !models.containsKey(modelId)) {
            throw new IllegalArgumentException("model not found");
        }
        return models.get(modelId);
    }

    private void validateUpsert(ModelUpsertCommand command, boolean update) {
        if (command == null) {
            throw new IllegalArgumentException("model cannot be empty");
        }
        if (update && isBlank(command.getModelId())) {
            throw new IllegalArgumentException("modelId cannot be empty");
        }
        if (isBlank(command.getProvider())) {
            throw new IllegalArgumentException("provider cannot be empty");
        }
        if (isBlank(command.getModelType())) {
            throw new IllegalArgumentException("modelType cannot be empty");
        }
        if (isBlank(command.getModel())) {
            throw new IllegalArgumentException("model cannot be empty");
        }
    }

    private void validateExperienceDialog(ModelExperienceDialogSaveCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("model experience dialog cannot be empty");
        }
        if (isBlank(command.getModelId())) {
            throw new IllegalArgumentException("modelId cannot be empty");
        }
        if (isBlank(command.getSessionId())) {
            throw new IllegalArgumentException("sessionId cannot be empty");
        }
    }

    private void validateExperienceRecord(ModelExperienceDialogRecordSaveCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("model experience dialog record cannot be empty");
        }
        if (isBlank(command.getSessionId())) {
            throw new IllegalArgumentException("sessionId cannot be empty");
        }
        if (isBlank(command.getModelId())) {
            throw new IllegalArgumentException("modelId cannot be empty");
        }
        if (isBlank(command.getRole())) {
            throw new IllegalArgumentException("role cannot be empty");
        }
    }

    private boolean matchesExperienceOwner(String userId, String orgId, ExperienceDialogState dialog) {
        if (!isBlank(userId) && !userId.equals(dialog.userId)) {
            return false;
        }
        return isBlank(orgId) || orgId.equals(dialog.orgId);
    }

    private ModelExperienceDialogInfo toDialogInfo(ExperienceDialogState source) {
        ModelExperienceDialogInfo info = new ModelExperienceDialogInfo();
        info.setId(source.id);
        info.setModelId(source.modelId);
        info.setSessionId(source.sessionId);
        info.setTitle(source.title);
        info.setModelSetting(source.modelSetting);
        info.setCreatedAt(source.createdAt);
        return info;
    }

    private ModelExperienceDialogRecordInfo toRecordInfo(ModelExperienceDialogRecordSaveCommand source) {
        ModelExperienceDialogRecordInfo info = new ModelExperienceDialogRecordInfo();
        info.setModelExperienceId(defaultIfBlank(source.getModelExperienceId(), "0"));
        info.setModelId(source.getModelId());
        info.setSessionId(source.getSessionId());
        info.setOriginalContent(defaultIfBlank(source.getOriginalContent(), ""));
        info.setHandledContent(defaultIfBlank(source.getHandledContent(), ""));
        info.setReasoningContent(defaultIfBlank(source.getReasoningContent(), ""));
        info.setRole(source.getRole());
        return info;
    }

    private ModelExperienceDialogRecordInfo copyRecord(ModelExperienceDialogRecordInfo source) {
        ModelExperienceDialogRecordInfo copy = new ModelExperienceDialogRecordInfo();
        copy.setModelExperienceId(source.getModelExperienceId());
        copy.setModelId(source.getModelId());
        copy.setSessionId(source.getSessionId());
        copy.setOriginalContent(source.getOriginalContent());
        copy.setHandledContent(source.getHandledContent());
        copy.setReasoningContent(source.getReasoningContent());
        copy.setRole(source.getRole());
        return copy;
    }

    private List<Map<String, Object>> tags(String scopeType, String importSource, String modelType) {
        List<Map<String, Object>> tags = new ArrayList<Map<String, Object>>();
        tags.add(singletonMap("text", scopeName(scopeType)));
        tags.add(singletonMap("text", IMPORT_BUILTIN.equals(importSource) ? "Built-in" : "External"));
        tags.add(singletonMap("text", modelTypeName(modelType)));
        return tags;
    }

    private List<ProviderModelTypeInfo> providerModelTypes() {
        return Arrays.asList(
                provider("DeepSeek", "DeepSeek", MODEL_TYPE_LLM),
                provider("OpenAI-API-compatible", "OpenAI-API-compatible", MODEL_TYPE_LLM, MODEL_TYPE_EMBEDDING, MODEL_TYPE_RERANK),
                provider("Qwen", "Qwen", MODEL_TYPE_LLM, MODEL_TYPE_EMBEDDING, MODEL_TYPE_RERANK, MODEL_TYPE_MULTI_EMBEDDING, MODEL_TYPE_MULTI_RERANK, MODEL_TYPE_ASR),
                provider("Jina", "Jina", MODEL_TYPE_EMBEDDING, MODEL_TYPE_RERANK, MODEL_TYPE_MULTI_EMBEDDING, MODEL_TYPE_MULTI_RERANK),
                provider("YuanJing", "YuanJing", MODEL_TYPE_LLM, MODEL_TYPE_EMBEDDING, MODEL_TYPE_RERANK, MODEL_TYPE_MULTI_EMBEDDING, MODEL_TYPE_MULTI_RERANK, MODEL_TYPE_OCR, MODEL_TYPE_GUI, MODEL_TYPE_PDF, MODEL_TYPE_ASR)
        );
    }

    private ProviderModelTypeInfo provider(String key, String name, String... modelTypes) {
        ProviderModelTypeInfo info = new ProviderModelTypeInfo();
        info.setKey(key);
        info.setName(name);
        List<ModelTypeInfo> children = new ArrayList<ModelTypeInfo>();
        for (String modelType : modelTypes) {
            children.add(new ModelTypeInfo(modelType, modelTypeName(modelType)));
        }
        info.setChildren(children);
        return info;
    }

    private List<RecommendModelInfo> recommendCatalog(String provider, String modelType) {
        String safeProvider = defaultIfBlank(provider, "DeepSeek");
        String safeModelType = defaultIfBlank(modelType, MODEL_TYPE_LLM);
        if ("DeepSeek".equals(safeProvider) && MODEL_TYPE_LLM.equals(safeModelType)) {
            return Arrays.asList(
                    recommend("deepseek-chat", "DeepSeek Chat", "toolCall", "support"),
                    recommend("deepseek-reasoner", "DeepSeek Reasoner", "noSupport", "support")
            );
        }
        if (MODEL_TYPE_EMBEDDING.equals(safeModelType)) {
            return Collections.singletonList(recommend("text-embedding-3-small", "Text Embedding 3 Small", "noSupport", "noSupport"));
        }
        if (MODEL_TYPE_RERANK.equals(safeModelType)) {
            return Collections.singletonList(recommend("jina-reranker-v2-base-multilingual", "Jina Reranker", "noSupport", "noSupport"));
        }
        return Collections.singletonList(recommend(safeProvider.toLowerCase(Locale.ENGLISH) + "-" + safeModelType, safeProvider + " " + modelTypeName(safeModelType), "noSupport", "noSupport"));
    }

    private RecommendModelInfo recommend(String model, String displayName, String functionCalling, String thinkingSupport) {
        RecommendModelInfo info = new RecommendModelInfo();
        info.setModel(model);
        info.setDisplayName(displayName);
        info.setTags(Collections.singletonList(singletonMap("text", modelTypeName(MODEL_TYPE_LLM))));
        info.setVisionSupport("noSupport");
        info.setFunctionCalling(functionCalling);
        info.setThinkingSupport(thinkingSupport);
        return info;
    }

    private String modelTypeName(String modelType) {
        if (MODEL_TYPE_LLM.equals(modelType)) {
            return "Text Generation";
        }
        if (MODEL_TYPE_EMBEDDING.equals(modelType)) {
            return "Text Embedding";
        }
        if (MODEL_TYPE_RERANK.equals(modelType)) {
            return "Rerank";
        }
        if (MODEL_TYPE_MULTI_EMBEDDING.equals(modelType)) {
            return "Multimodal Embedding";
        }
        if (MODEL_TYPE_MULTI_RERANK.equals(modelType)) {
            return "Multimodal Rerank";
        }
        if (MODEL_TYPE_PDF.equals(modelType)) {
            return "PDF Parser";
        }
        if (MODEL_TYPE_ASR.equals(modelType)) {
            return "ASR";
        }
        return modelType;
    }

    private String scopeName(String scopeType) {
        if (SCOPE_PUBLIC.equals(scopeType)) {
            return "Public";
        }
        if (SCOPE_ORG.equals(scopeType)) {
            return "Organization";
        }
        return "Private";
    }

    private Map<String, Object> avatar(String path) {
        return singletonMap("path", defaultIfBlank(path, ""));
    }

    private Map<String, Object> config(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(key1, value1);
        result.put(key2, value2);
        return result;
    }

    private Map<String, Object> config(String key1, Object value1, String key2, Object value2, String key3, Object value3,
                                       String key4, Object value4, String key5, Object value5) {
        Map<String, Object> result = config(key1, value1, key2, value2);
        result.put(key3, value3);
        result.put(key4, value4);
        result.put(key5, value5);
        return result;
    }

    private Map<String, Object> singletonMap(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(key, value);
        return result;
    }

    private boolean containsIgnoreCase(String source, String expected) {
        return !isBlank(source) && !isBlank(expected)
                && source.toLowerCase(Locale.ENGLISH).contains(expected.toLowerCase(Locale.ENGLISH));
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String stringValue(Map<String, Object> map, String key, String fallback) {
        if (map == null || map.get(key) == null) {
            return fallback;
        }
        return String.valueOf(map.get(key));
    }

    private long longValue(Map<String, Object> map, String key, long fallback) {
        if (map == null || map.get(key) == null) {
            return fallback;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class ExperienceDialogState {
        private String id;
        private String userId;
        private String orgId;
        private String modelId;
        private String sessionId;
        private String title;
        private String modelSetting;
        private long createdAt;
    }
}
