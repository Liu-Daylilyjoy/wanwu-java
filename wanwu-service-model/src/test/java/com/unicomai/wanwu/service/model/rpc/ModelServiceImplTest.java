package com.unicomai.wanwu.service.model.rpc;

import com.sun.net.httpserver.HttpServer;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.dto.ModelInvokeCommand;
import com.unicomai.wanwu.api.model.dto.ModelInvokeResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogDeleteCommand;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordInfo;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordListResult;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordQuery;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogRecordSaveCommand;
import com.unicomai.wanwu.api.model.dto.ModelExperienceDialogSaveCommand;
import com.unicomai.wanwu.api.model.dto.ModelListQuery;
import com.unicomai.wanwu.api.model.dto.ModelListResult;
import com.unicomai.wanwu.api.model.dto.ModelStatusCommand;
import com.unicomai.wanwu.api.model.dto.ModelTypeQuery;
import com.unicomai.wanwu.api.model.dto.ModelUpsertCommand;
import com.unicomai.wanwu.api.model.dto.ProviderListQuery;
import com.unicomai.wanwu.api.model.dto.ProviderModelTypeResult;
import com.unicomai.wanwu.api.model.dto.RecommendModelQuery;
import com.unicomai.wanwu.api.model.dto.RecommendModelResult;
import com.unicomai.wanwu.service.model.persistence.entity.ModelRecordEntity;
import com.unicomai.wanwu.service.model.persistence.mapper.ModelRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ModelServiceImplTest {

    private ModelServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new ModelServiceImpl();
    }

    @Test
    public void listModelsReturnsBuiltInDevelopmentModelsForFrontend() {
        ModelListResult result = service.listModels(new ModelListQuery("dev-admin", "default-org", "", "", "", "", ""));

        assertTrue(result.getTotal() >= 3);
        ModelInfo first = result.getList().get(0);
        assertNotNull(first.getModelId());
        assertNotNull(first.getUuid());
        assertNotNull(first.getDisplayName());
        assertTrue(first.getIsActive());
        assertEquals("builtin", first.getImportSource());
        assertNotNull(first.getConfig());
        assertFalse(first.getTags().isEmpty());
    }

    @Test
    public void invokeModelAggregatesOpenAiCompatibleChatStream() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            byte[] response = ("data: {\"choices\":[{\"delta\":{\"content\":\"grounded \"}}]}\n\n"
                    + "data: {\"choices\":[{\"delta\":{\"content\":\"answer\"}}],"
                    + "\"usage\":{\"prompt_tokens\":8,\"completion_tokens\":2,\"total_tokens\":10}}\n\n"
                    + "data: [DONE]\n\n").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            ModelUpsertCommand create = new ModelUpsertCommand();
            create.setUserId("dev-admin");
            create.setOrgId("default-org");
            create.setProvider("OpenAI-API-compatible");
            create.setModelType("llm");
            create.setModel("test-chat");
            create.setDisplayName("Test Chat");
            Map<String, Object> config = new LinkedHashMap<String, Object>();
            config.put("apiKey", "local-test-key");
            config.put("inferUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            create.setConfig(config);
            ModelInfo model = service.importModel(create);

            ModelInvokeCommand command = new ModelInvokeCommand();
            command.setUserId("dev-admin");
            command.setOrgId("default-org");
            command.setModelId(model.getModelId());
            command.setOperation("chat");
            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("stream", true);
            payload.put("messages", Collections.singletonList(
                    Collections.<String, Object>singletonMap("content", "question")));
            command.setPayload(payload);

            ModelInvokeResult result = service.invokeModel(command);

            assertEquals("grounded answer", result.getContent());
            assertEquals(Arrays.asList("grounded ", "answer"), result.getChunks());
            assertEquals(10, ((Number) result.getUsage().get("total_tokens")).intValue());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void invokeModelReturnsEmbeddingAndRerankProviderResponses() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/embeddings", exchange -> respondJson(exchange,
                "{\"data\":[{\"index\":0,\"embedding\":[0.1,0.9]}],\"usage\":{\"total_tokens\":2}}"));
        server.createContext("/v1/rerank", exchange -> respondJson(exchange,
                "{\"results\":[{\"index\":1,\"relevance_score\":0.98}]}"));
        server.start();
        try {
            ModelInfo embedding = importProviderModel("embedding", "test-embedding", server.getAddress().getPort());
            ModelInfo rerank = importProviderModel("rerank", "test-rerank", server.getAddress().getPort());

            ModelInvokeCommand embeddingCommand = invokeCommand(embedding.getModelId(), "embeddings");
            embeddingCommand.setPayload(Collections.<String, Object>singletonMap("input", "hello"));
            ModelInvokeResult embeddingResult = service.invokeModel(embeddingCommand);
            Map<?, ?> embeddingRow = (Map<?, ?>) ((java.util.List<?>) embeddingResult.getResponse().get("data")).get(0);
            assertEquals(Arrays.asList(0.1D, 0.9D), embeddingRow.get("embedding"));

            ModelInvokeCommand rerankCommand = invokeCommand(rerank.getModelId(), "rerank");
            Map<String, Object> rerankPayload = new LinkedHashMap<String, Object>();
            rerankPayload.put("query", "hello");
            rerankPayload.put("documents", Arrays.asList("first", "second"));
            rerankCommand.setPayload(rerankPayload);
            ModelInvokeResult rerankResult = service.invokeModel(rerankCommand);
            Map<?, ?> rerankRow = (Map<?, ?>) ((java.util.List<?>) rerankResult.getResponse().get("results")).get(0);
            assertEquals(1, ((Number) rerankRow.get("index")).intValue());
            assertEquals(0.98D, ((Number) rerankRow.get("relevance_score")).doubleValue(), 0.0001D);
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void invokeModelHonorsCommandReadTimeout() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/embeddings", exchange -> {
            try {
                Thread.sleep(250L);
                respondJson(exchange, "{\"data\":[]}");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            ModelInfo model = importProviderModel("embedding", "timeout-embedding",
                    server.getAddress().getPort());
            ModelInvokeCommand command = invokeCommand(model.getModelId(), "embeddings");
            command.setTimeoutMillis(50);
            command.setPayload(Collections.<String, Object>singletonMap("input", "hello"));

            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> service.invokeModel(command));

            assertEquals("model inference request failed", error.getMessage());
            assertTrue(error.getCause() instanceof java.net.SocketTimeoutException);
        } finally {
            server.stop(0);
        }
    }

    private ModelInfo importProviderModel(String type, String modelName, int port) {
        ModelUpsertCommand create = new ModelUpsertCommand();
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        create.setProvider("OpenAI-API-compatible");
        create.setModelType(type);
        create.setModel(modelName);
        create.setDisplayName(modelName);
        Map<String, Object> config = new LinkedHashMap<String, Object>();
        config.put("apiKey", "local-test-key");
        config.put("inferUrl", "http://127.0.0.1:" + port + "/v1");
        create.setConfig(config);
        return service.importModel(create);
    }

    private ModelInvokeCommand invokeCommand(String modelId, String operation) {
        ModelInvokeCommand command = new ModelInvokeCommand();
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        command.setModelId(modelId);
        command.setOperation(operation);
        return command;
    }

    private void respondJson(com.sun.net.httpserver.HttpExchange exchange, String body) throws java.io.IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    @Test
    public void modelCrudAndStatusFollowGoFrontendContract() {
        ModelUpsertCommand create = new ModelUpsertCommand();
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        create.setProvider("DeepSeek");
        create.setModelType("llm");
        create.setModel("deepseek-chat");
        create.setDisplayName("DeepSeek Chat Local");
        create.setModelDesc("created by test");
        create.setScopeType("1");
        create.setConfig(Collections.singletonMap("apiKey", "test-key"));

        ModelInfo created = service.importModel(create);
        assertNotNull(created.getModelId());
        assertEquals("DeepSeek Chat Local", created.getDisplayName());
        assertTrue(created.getAllowEdit());

        ModelInfo detail = service.getModel("dev-admin", "default-org", created.getModelId());
        assertEquals("test-key", detail.getConfig().get("apiKey"));

        service.changeModelStatus(new ModelStatusCommand("dev-admin", "default-org", created.getModelId(), false));
        assertFalse(service.getModel("dev-admin", "default-org", created.getModelId()).getIsActive());

        create.setModelId(created.getModelId());
        create.setDisplayName("DeepSeek Chat Updated");
        service.updateModel(create);
        assertEquals("DeepSeek Chat Updated", service.getModel("dev-admin", "default-org", created.getModelId()).getDisplayName());

        service.deleteModel("dev-admin", "default-org", created.getModelId());
        ModelListResult afterDelete = service.listModels(new ModelListQuery("dev-admin", "default-org", "", "", "DeepSeek Chat Updated", "", ""));
        assertEquals(0, afterDelete.getTotal());
    }

    @Test
    public void modelUuidLookupAndPermissionFollowGoScopeRules() {
        ModelInfo builtin = service.listModels(new ModelListQuery("dev-admin", "default-org", "", "", "", "", ""))
                .getList().get(0);
        assertEquals(Collections.singletonList(builtin.getModelId()),
                service.listModelIdsByUuids(Collections.singletonList(builtin.getUuid())));

        service.checkModelUserPermission("dev-admin", "default-org",
                Collections.singletonList(builtin.getModelId()));
        assertThrows(IllegalArgumentException.class, () ->
                service.checkModelUserPermission("dev-app", "default-org",
                        Collections.singletonList(builtin.getModelId())));

        ModelUpsertCommand create = new ModelUpsertCommand();
        create.setUserId("model-owner");
        create.setOrgId("owner-org");
        create.setProvider("OpenAI-API-compatible");
        create.setModelType("llm");
        create.setModel("public-compatible");
        create.setDisplayName("Public Compatible");
        create.setScopeType("2");
        ModelInfo publicModel = service.importModel(create);
        service.checkModelUserPermission("another-user", "another-org",
                Collections.singletonList(publicModel.getModelId()));
    }

    @Test
    public void listAndGetModelsApplyGoUserOrgOrPublicScopeRules() {
        ModelInfo adminPrivate = service.importModel(modelCommand(
                "dev-admin", "default-org", "Admin Private Model", "1"));
        ModelInfo orgShared = service.importModel(modelCommand(
                "dev-admin", "default-org", "Org Shared Model", "3"));
        ModelInfo publicModel = service.importModel(modelCommand(
                "other-user", "other-org", "Public Model", "2"));

        ModelListResult appList = service.listModels(new ModelListQuery("dev-app", "default-org", "", "", "", "", ""));
        assertFalse(containsModel(appList, adminPrivate.getModelId()));
        assertTrue(containsModel(appList, orgShared.getModelId()));
        assertTrue(containsModel(appList, publicModel.getModelId()));

        assertThrows(IllegalArgumentException.class,
                () -> service.getModel("dev-app", "default-org", adminPrivate.getModelId()));
        assertEquals("Org Shared Model",
                service.getModel("dev-app", "default-org", orgShared.getModelId()).getDisplayName());

        ModelListResult appTypeList = service.listTypeModels(new ModelTypeQuery("dev-app", "default-org", "llm"));
        assertFalse(containsModel(appTypeList, adminPrivate.getModelId()));
        assertTrue(containsModel(appTypeList, orgShared.getModelId()));
    }

    @Test
    public void typeSelectProviderAndRecommendModelsFollowFrontendContract() {
        ModelListResult llm = service.listTypeModels(new ModelTypeQuery("dev-admin", "default-org", "llm"));
        assertTrue(llm.getTotal() >= 1);
        assertEquals("llm", llm.getList().get(0).getModelType());

        ProviderModelTypeResult providers = service.listImportProviders(new ProviderListQuery("", "llm"));
        assertTrue(providers.getTotal() >= 1);
        assertEquals("llm", providers.getList().get(0).getChildren().get(0).getKey());

        RecommendModelResult recommended = service.recommendModels(new RecommendModelQuery("DeepSeek", "llm"));
        assertTrue(recommended.getTotal() >= 1);
        assertEquals("deepseek-chat", recommended.getList().get(0).getModel());
        assertEquals("Text Generation", recommended.getList().get(0).getTags().get(0).get("text"));

        RecommendModelResult embedding = service.recommendModels(new RecommendModelQuery("OpenAI-API-compatible", "embedding"));
        assertEquals("Text Embedding", embedding.getList().get(0).getTags().get(0).get("text"));

        RecommendModelResult rerank = service.recommendModels(new RecommendModelQuery("Jina", "rerank"));
        assertEquals("Rerank", rerank.getList().get(0).getTags().get(0).get("text"));

        ModelListResult multiEmbedding = service.listTypeModels(new ModelTypeQuery("dev-admin", "default-org", "multi-embedding"));
        assertEquals(1, multiEmbedding.getTotal());
        assertEquals("multimodal-embedding", multiEmbedding.getList().get(0).getModelType());

        ModelListResult multiRerank = service.listTypeModels(new ModelTypeQuery("dev-admin", "default-org", "multi-rerank"));
        assertEquals(1, multiRerank.getTotal());
        assertEquals("multimodal-rerank", multiRerank.getList().get(0).getModelType());

        ProviderModelTypeResult multiProviders = service.listImportProviders(new ProviderListQuery("", "multi-embedding"));
        assertTrue(multiProviders.getTotal() >= 1);
        assertEquals("multimodal-embedding", multiProviders.getList().get(0).getChildren().get(0).getKey());

        RecommendModelResult multiRecommended = service.recommendModels(new RecommendModelQuery("Qwen", "multi-rerank"));
        assertEquals(1, multiRecommended.getTotal());
        assertEquals("Multimodal Rerank", multiRecommended.getList().get(0).getTags().get(0).get("text"));
    }

    @Test
    public void syncAsrSelectAndRecommendReturnDevelopmentModel() {
        ModelListResult asr = service.listTypeModels(new ModelTypeQuery("dev-admin", "default-org", "sync-asr"));
        assertEquals(1, asr.getTotal());
        assertEquals("sync-asr", asr.getList().get(0).getModelType());
        assertEquals("qwen3-asr-flash", asr.getList().get(0).getModel());

        ModelListResult asrAlias = service.listTypeModels(new ModelTypeQuery("dev-admin", "default-org", "asr"));
        assertEquals(1, asrAlias.getTotal());
        assertEquals("sync-asr", asrAlias.getList().get(0).getModelType());

        RecommendModelResult recommended = service.recommendModels(new RecommendModelQuery("Qwen", "sync-asr"));
        assertEquals(1, recommended.getTotal());
        assertEquals("qwen3-asr-flash", recommended.getList().get(0).getModel());
        assertEquals("ASR", recommended.getList().get(0).getTags().get(0).get("text"));
    }

    @Test
    public void yuanjingSpecializedSelectAndRecommendReturnDevelopmentModels() {
        assertSpecializedModel("ocr", "unicom-ocr", "OCR");
        assertSpecializedModel("pdf-parser", "pdf-parser", "PDF Parser");
        assertSpecializedModel("gui", "gui_agent_v1", "GUI");
    }

    @Test
    public void modelExperienceDialogLifecycleFollowsGoContract() {
        ModelExperienceDialogSaveCommand create = new ModelExperienceDialogSaveCommand();
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        create.setModelId("1");
        create.setSessionId("session-001");
        create.setTitle("hello model");
        create.setModelSetting("{\"temperature\":0.7}");

        ModelExperienceDialogInfo created = service.saveModelExperienceDialog(create);
        assertNotNull(created.getId());
        assertEquals("1", created.getModelId());
        assertEquals("session-001", created.getSessionId());
        assertEquals("hello model", created.getTitle());

        create.setModelId("2");
        create.setModelSetting("{\"temperature\":0.2}");
        ModelExperienceDialogInfo updated = service.saveModelExperienceDialog(create);
        assertEquals(created.getId(), updated.getId());
        assertEquals("1", updated.getModelId());
        assertEquals("{\"temperature\":0.2}", updated.getModelSetting());

        service.saveModelExperienceDialogRecord(new ModelExperienceDialogRecordSaveCommand(
                "dev-admin", "default-org", created.getId(), "2", "session-001",
                "hello", "", "", "user"));
        service.saveModelExperienceDialogRecord(new ModelExperienceDialogRecordSaveCommand(
                "dev-admin", "default-org", created.getId(), "2", "session-001",
                "Echo: hello", "", "thinking", "assistant"));

        ModelExperienceDialogRecordListResult records = service.listModelExperienceDialogRecords(
                new ModelExperienceDialogRecordQuery("dev-admin", "default-org", created.getId(), ""));
        assertEquals(2, records.getTotal());
        ModelExperienceDialogRecordInfo first = records.getList().get(0);
        assertEquals("user", first.getRole());
        assertEquals("hello", first.getOriginalContent());
        assertEquals("assistant", records.getList().get(1).getRole());
        assertEquals("thinking", records.getList().get(1).getReasoningContent());

        ModelExperienceDialogListResult dialogs = service.listModelExperienceDialogs(
                new ModelExperienceDialogListQuery("dev-admin", "default-org"));
        assertEquals(1, dialogs.getTotal());
        assertEquals(created.getId(), dialogs.getList().get(0).getId());

        service.deleteModelExperienceDialog(new ModelExperienceDialogDeleteCommand("dev-admin", "default-org", created.getId()));
        assertEquals(0, service.listModelExperienceDialogs(
                new ModelExperienceDialogListQuery("dev-admin", "default-org")).getTotal());
        assertEquals(0, service.listModelExperienceDialogRecords(
                new ModelExperienceDialogRecordQuery("dev-admin", "default-org", created.getId(), "")).getTotal());
    }

    @Test
    public void modelWritesArePersistedAsJsonRecordsWhenMapperIsAvailable() {
        ModelRecordMapper mapper = mock(ModelRecordMapper.class);
        when(mapper.selectByType(anyString())).thenReturn(Collections.<ModelRecordEntity>emptyList());
        ModelServiceImpl persistent = new ModelServiceImpl(mapper);

        ModelUpsertCommand create = new ModelUpsertCommand();
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        create.setProvider("DeepSeek");
        create.setModelType("llm");
        create.setModel("deepseek-chat");
        create.setDisplayName("Persistent Model");
        create.setConfig(Collections.singletonMap("apiKey", "persist-key"));
        ModelInfo created = persistent.importModel(create);
        persistent.changeModelStatus(new ModelStatusCommand("dev-admin", "default-org", created.getModelId(), false));
        persistent.deleteModel("dev-admin", "default-org", created.getModelId());

        ModelExperienceDialogSaveCommand dialog = new ModelExperienceDialogSaveCommand();
        dialog.setUserId("dev-admin");
        dialog.setOrgId("default-org");
        dialog.setModelId("1");
        dialog.setSessionId("persist-session");
        dialog.setTitle("Persistent Dialog");
        ModelExperienceDialogInfo createdDialog = persistent.saveModelExperienceDialog(dialog);
        persistent.saveModelExperienceDialogRecord(new ModelExperienceDialogRecordSaveCommand(
                "dev-admin", "default-org", createdDialog.getId(), "1", "persist-session",
                "hello", "", "", "user"));

        ArgumentCaptor<ModelRecordEntity> captor = ArgumentCaptor.forClass(ModelRecordEntity.class);
        verify(mapper, atLeastOnce()).upsertRecord(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(item ->
                "model".equals(item.getRecordType()) && item.getPayload().contains("Persistent Model")));
        assertTrue(captor.getAllValues().stream().anyMatch(item ->
                "model_deleted".equals(item.getRecordType()) && created.getModelId().equals(item.getRecordId())));
        assertTrue(captor.getAllValues().stream().anyMatch(item ->
                "dialog".equals(item.getRecordType()) && item.getPayload().contains("Persistent Dialog")));
        assertTrue(captor.getAllValues().stream().anyMatch(item ->
                "records".equals(item.getRecordType()) && item.getPayload().contains("hello")));
        verify(mapper).deleteRecord("model", created.getModelId());
    }

    @Test
    public void persistedModelRecordsAreLoadedAndSequencesContinueAfterRestart() {
        ModelRecordMapper mapper = mock(ModelRecordMapper.class);
        when(mapper.selectByType(eq("model_deleted"))).thenReturn(Collections.<ModelRecordEntity>emptyList());
        when(mapper.selectByType(eq("dialog"))).thenReturn(Collections.singletonList(record("dialog", "1009",
                "{\"id\":\"1009\",\"userId\":\"dev-admin\",\"orgId\":\"default-org\",\"modelId\":\"120\",\"sessionId\":\"loaded-session\",\"title\":\"Loaded Dialog\",\"modelSetting\":\"{}\",\"createdAt\":1782806400000}")));
        when(mapper.selectByType(eq("records"))).thenReturn(Collections.singletonList(record("records", "all",
                "[{\"modelExperienceId\":\"1009\",\"modelId\":\"120\",\"sessionId\":\"loaded-session\",\"originalContent\":\"loaded\",\"handledContent\":\"\",\"reasoningContent\":\"\",\"role\":\"user\"}]")));
        when(mapper.selectByType(eq("model"))).thenReturn(Collections.singletonList(record("model", "120",
                "{\"modelId\":\"120\",\"uuid\":\"model-uuid-120\",\"provider\":\"DeepSeek\",\"modelType\":\"llm\",\"model\":\"loaded-model\",\"displayName\":\"Loaded Model\",\"avatar\":{\"path\":\"\"},\"publishDate\":\"2026-06-30\",\"isActive\":true,\"userId\":\"dev-admin\",\"orgId\":\"default-org\",\"createdAt\":\"2026-06-30 00:00:00\",\"updatedAt\":\"2026-06-30 00:00:00\",\"modelDesc\":\"\",\"tags\":[],\"config\":{},\"scopeType\":\"1\",\"allowEdit\":true,\"importSource\":\"external\"}")));

        ModelServiceImpl persistent = new ModelServiceImpl(mapper);

        assertEquals("Loaded Model", persistent.getModel("dev-admin", "default-org", "120").getDisplayName());
        assertEquals("Loaded Dialog", persistent.listModelExperienceDialogs(
                new ModelExperienceDialogListQuery("dev-admin", "default-org")).getList().get(0).getTitle());
        assertEquals("loaded", persistent.listModelExperienceDialogRecords(
                new ModelExperienceDialogRecordQuery("dev-admin", "default-org", "1009", "")).getList().get(0).getOriginalContent());

        ModelUpsertCommand create = new ModelUpsertCommand();
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        create.setProvider("DeepSeek");
        create.setModelType("llm");
        create.setModel("next-model");
        ModelInfo next = persistent.importModel(create);
        assertEquals("121", next.getModelId());
    }

    private ModelRecordEntity record(String type, String id, String payload) {
        ModelRecordEntity record = new ModelRecordEntity();
        record.setRecordType(type);
        record.setRecordId(id);
        record.setPayload(payload);
        record.setCreatedAt(1L);
        record.setUpdatedAt(1L);
        return record;
    }

    private ModelUpsertCommand modelCommand(String userId, String orgId, String displayName, String scopeType) {
        ModelUpsertCommand command = new ModelUpsertCommand();
        command.setUserId(userId);
        command.setOrgId(orgId);
        command.setProvider("DeepSeek");
        command.setModelType("llm");
        command.setModel(displayName.toLowerCase().replace(' ', '-'));
        command.setDisplayName(displayName);
        command.setScopeType(scopeType);
        return command;
    }

    private boolean containsModel(ModelListResult result, String modelId) {
        for (ModelInfo model : result.getList()) {
            if (modelId.equals(model.getModelId())) {
                return true;
            }
        }
        return false;
    }

    private void assertSpecializedModel(String modelType, String model, String tag) {
        ModelListResult selected = service.listTypeModels(new ModelTypeQuery("dev-admin", "default-org", modelType));
        assertEquals(1, selected.getTotal());
        assertEquals(model, selected.getList().get(0).getModel());

        RecommendModelResult recommended = service.recommendModels(new RecommendModelQuery("YuanJing", modelType));
        assertEquals(1, recommended.getTotal());
        assertEquals(model, recommended.getList().get(0).getModel());
        assertEquals(tag, recommended.getList().get(0).getTags().get(0).get("text"));
    }
}
