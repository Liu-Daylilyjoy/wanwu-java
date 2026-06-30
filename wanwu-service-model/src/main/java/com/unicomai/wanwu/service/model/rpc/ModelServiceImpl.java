package com.unicomai.wanwu.service.model.rpc;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.model.ModelService;
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
import org.apache.dubbo.config.annotation.DubboService;

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

    private final Map<String, ModelInfo> models = new LinkedHashMap<String, ModelInfo>();
    private final AtomicLong nextId = new AtomicLong(100);

    public ModelServiceImpl() {
        seedBuiltInModels();
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
    }

    @Override
    public synchronized void deleteModel(String userId, String orgId, String modelId) {
        existing(modelId);
        models.remove(modelId);
    }

    @Override
    public synchronized void changeModelStatus(ModelStatusCommand command) {
        if (command == null || isBlank(command.getModelId())) {
            throw new IllegalArgumentException("modelId cannot be empty");
        }
        ModelInfo model = existing(command.getModelId());
        model.setIsActive(Boolean.TRUE.equals(command.getIsActive()));
        model.setUpdatedAt(CREATED_AT);
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

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.MODEL, "Model Service", "model");
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
