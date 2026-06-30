package com.unicomai.wanwu.service.model.rpc;

import com.unicomai.wanwu.api.model.dto.ModelInfo;
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
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelServiceImplTest {

    private final ModelServiceImpl service = new ModelServiceImpl();

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
}
