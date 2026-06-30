package com.unicomai.wanwu.service.app.rpc;

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
import com.unicomai.wanwu.api.app.dto.ApiKeyCreateCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyDeleteCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.ApiKeyListQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyPageResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatusCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationInfoQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyCreateCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyInfo;
import com.unicomai.wanwu.api.app.dto.AppKeyListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.RagConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RagCopyCommand;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RagCreateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateResult;
import com.unicomai.wanwu.api.app.dto.RagDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.RagUpdateCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCopyCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateResult;
import com.unicomai.wanwu.api.app.dto.WorkflowDeleteCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowExportQuery;
import com.unicomai.wanwu.api.app.dto.WorkflowExportResult;
import com.unicomai.wanwu.api.app.dto.WorkflowImportCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.service.app.domain.AssistantConversationMessageRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationRecord;
import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.AssistantSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyRecord;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.AppKeyRecord;
import com.unicomai.wanwu.service.app.domain.AppUrlRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import com.unicomai.wanwu.service.app.domain.RagDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.RagSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowDraftRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowSnapshotRecord;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppServiceImplTest {

    @Test
    public void listAssistantsStartsWithEmptyDevelopmentList() {
        ApplicationListResult result = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock())
                .listAssistants(new ApplicationListQuery("", "", "dev-admin", "default-org"));

        assertTrue(result.getList().isEmpty());
    }

    @Test
    public void createAssistantPersistsAndListsNewestMatchingAgent() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        AssistantCreateResult first = service.createAssistant(command("FirstAgent", "first"));
        AssistantCreateResult second = service.createAssistant(command("SecondAgent", "second"));

        ApplicationListResult result = service.listAssistants(new ApplicationListQuery("agent", "Second", "dev-admin", "default-org"));

        assertEquals(1, result.getTotal());
        Map<String, Object> item = result.getList().get(0);
        assertEquals(second.getAssistantId(), item.get("appId"));
        assertEquals("agent", item.get("appType"));
        assertEquals("SecondAgent", item.get("name"));
        assertEquals("second", item.get("desc"));
        assertEquals("", item.get("publishType"));
        assertEquals(1, item.get("category"));
        assertTrue(repository.containsApp(first.getAssistantId()));
        assertTrue(repository.containsApp(second.getAssistantId()));
    }

    @Test
    public void listApplicationsReturnsAgentCardsForAppspaceList() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());
        AssistantCreateResult first = service.createAssistant(command("AlphaAgent", "alpha"));
        AssistantCreateResult second = service.createAssistant(command("BetaAgent", "beta"));

        AppPublishCommand publish = new AppPublishCommand();
        publish.setAppId(second.getAssistantId());
        publish.setAppType("agent");
        publish.setUserId("dev-admin");
        publish.setOrgId("default-org");
        publish.setVersion("v1.0.0");
        publish.setPublishType("public");
        service.publishApp(publish);

        ApplicationListResult result = service.listApplications(
                new ApplicationListQuery("agent", "Beta", "dev-admin", "default-org"));

        assertEquals(1, result.getTotal());
        assertEquals(second.getAssistantId(), result.getList().get(0).get("appId"));
        assertEquals("BetaAgent", result.getList().get(0).get("name"));
        assertEquals("public", result.getList().get(0).get("publishType"));
        assertEquals("v1.0.0", result.getList().get(0).get("version"));
        assertNotEquals(first.getAssistantId(), result.getList().get(0).get("appId"));
    }

    @Test
    public void ragLifecyclePersistsConfigAndSupportsPublishCopyDelete() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        RagCreateCommand create = new RagCreateCommand();
        create.setName("PolicyRag");
        create.setDesc("policy qa");
        create.setAvatarKey("avatars/rag.png");
        create.setAvatarPath("/static/rag.png");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        RagCreateResult created = service.createRag(create);

        RagConfigUpdateCommand config = new RagConfigUpdateCommand();
        config.setRagId(created.getRagId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setModelConfig(modelConfig("llm-001"));
        config.setRerankConfig(modelConfig("rerank-001"));
        config.setQaRerankConfig(modelConfig("qa-rerank-001"));
        config.setKnowledgeBaseConfig(knowledgeConfig("kb-001"));
        config.setQaKnowledgeBaseConfig(knowledgeConfig("qa-kb-001"));
        config.setSafetyConfig(safetyConfig("table-001"));
        config.setVisionConfig(visionConfig(1));
        service.updateRagConfig(config);

        Map<String, Object> draft = service.getRagDraft(
                new RagDetailQuery(created.getRagId(), "", "dev-admin", "default-org"));
        assertEquals(created.getRagId(), draft.get("ragId"));
        assertEquals("PolicyRag", draft.get("name"));
        assertEquals("llm-001", ((Map<?, ?>) draft.get("modelConfig")).get("modelId"));
        assertEquals(1, ((Map<?, ?>) draft.get("visionConfig")).get("picNum"));

        ApplicationListResult ragList = service.listApplications(
                new ApplicationListQuery("rag", "Policy", "dev-admin", "default-org"));
        assertEquals(1, ragList.getTotal());
        assertEquals("rag", ragList.getList().get(0).get("appType"));

        AppPublishCommand publish = new AppPublishCommand();
        publish.setAppId(created.getRagId());
        publish.setAppType("rag");
        publish.setUserId("dev-admin");
        publish.setOrgId("default-org");
        publish.setVersion("v1.0.0");
        publish.setDesc("first rag version");
        publish.setPublishType("public");
        service.publishApp(publish);

        Map<String, Object> published = service.getPublishedRag(
                new RagDetailQuery(created.getRagId(), "", "dev-admin", "default-org"));
        assertEquals("public", ((Map<?, ?>) published.get("appPublishConfig")).get("publishType"));
        assertEquals("PolicyRag", published.get("name"));
        assertEquals("v1.0.0", service.getLatestAppVersion(
                new AppVersionQuery(created.getRagId(), "rag", "dev-admin", "default-org")).getVersion());

        RagUpdateCommand update = new RagUpdateCommand();
        update.setRagId(created.getRagId());
        update.setName("PolicyRagUpdated");
        update.setDesc("updated policy qa");
        update.setUserId("dev-admin");
        update.setOrgId("default-org");
        service.updateRag(update);
        assertEquals("PolicyRagUpdated", service.getRagDraft(
                new RagDetailQuery(created.getRagId(), "", "dev-admin", "default-org")).get("name"));

        RagCopyCommand copy = new RagCopyCommand();
        copy.setRagId(created.getRagId());
        copy.setUserId("dev-admin");
        copy.setOrgId("default-org");
        RagCreateResult copied = service.copyRag(copy);
        assertNotEquals(created.getRagId(), copied.getRagId());
        assertEquals(2, service.listApplications(
                new ApplicationListQuery("rag", "PolicyRagUpdated", "dev-admin", "default-org")).getTotal());

        RagDeleteCommand delete = new RagDeleteCommand();
        delete.setRagId(created.getRagId());
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");
        service.deleteRag(delete);
        assertThrows(IllegalArgumentException.class, () -> service.getRagDraft(
                new RagDetailQuery(created.getRagId(), "", "dev-admin", "default-org")));
    }

    @Test
    public void ragChatValidatesDraftAndPublishedSnapshot() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        RagCreateCommand create = new RagCreateCommand();
        create.setName("PolicyRag");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        RagCreateResult created = service.createRag(create);

        RagChatCommand draft = ragChatCommand(created.getRagId(), "what is policy", true);
        RagChatResult draftResult = service.streamRagChat(draft);
        assertEquals(created.getRagId(), draftResult.getRagId());
        assertEquals("what is policy", draftResult.getQuestion());
        assertEquals(true, draftResult.getResponse().contains("PolicyRag"));

        assertThrows(IllegalArgumentException.class,
                () -> service.streamRagChat(ragChatCommand(created.getRagId(), "published", false)));

        AppPublishCommand publish = new AppPublishCommand();
        publish.setAppId(created.getRagId());
        publish.setAppType("rag");
        publish.setUserId("dev-admin");
        publish.setOrgId("default-org");
        publish.setVersion("v1.0.0");
        publish.setPublishType("public");
        service.publishApp(publish);

        RagChatResult published = service.streamRagChat(
                ragChatCommand(created.getRagId(), "published", false));
        assertEquals("published", published.getQuestion());
        assertEquals(true, published.getResponse().contains("Demo RAG response"));
    }

    @Test
    public void workflowLifecycleSupportsFrontendAppspaceLoop() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("PolicyFlow");
        create.setDesc("policy workflow");
        create.setAvatarKey("avatars/workflow.png");
        create.setAvatarPath("/static/workflow.png");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        ApplicationListResult list = service.listApplications(
                new ApplicationListQuery("workflow", "Policy", "dev-admin", "default-org"));
        assertEquals(1, list.getTotal());
        assertEquals(created.getWorkflowId(), list.getList().get(0).get("appId"));
        assertEquals("workflow", list.getList().get(0).get("appType"));

        Map<String, Object> workflowSelect = service.listAssistantWorkflowSelect("dev-admin", "default-org", "Policy");
        List<Map<String, Object>> workflowOptions = castList(workflowSelect.get("list"));
        assertEquals(1, workflowOptions.size());
        assertEquals(created.getWorkflowId(), workflowOptions.get(0).get("workFlowId"));
        assertEquals("PolicyFlow", workflowOptions.get(0).get("name"));

        WorkflowExportResult draftExport = service.exportWorkflow(
                new WorkflowExportQuery(created.getWorkflowId(), "", false, "dev-admin", "default-org"));
        assertEquals("PolicyFlow", draftExport.getName());
        assertTrue(draftExport.getSchema().contains(created.getWorkflowId()));

        AppPublishCommand publish = new AppPublishCommand();
        publish.setAppId(created.getWorkflowId());
        publish.setAppType("workflow");
        publish.setUserId("dev-admin");
        publish.setOrgId("default-org");
        publish.setVersion("v1.0.0");
        publish.setDesc("first workflow version");
        publish.setPublishType("public");
        service.publishApp(publish);

        assertEquals("v1.0.0", service.getLatestAppVersion(
                new AppVersionQuery(created.getWorkflowId(), "workflow", "dev-admin", "default-org")).getVersion());

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.singletonMap("question", "hello"));
        WorkflowRunResult runResult = service.runWorkflow(run);
        assertEquals(created.getWorkflowId(), runResult.getWorkflowId());
        assertEquals("hello", runResult.getOutput().get("question"));

        WorkflowCopyCommand copy = new WorkflowCopyCommand();
        copy.setWorkflowId(created.getWorkflowId());
        copy.setUserId("dev-admin");
        copy.setOrgId("default-org");
        WorkflowCreateResult copied = service.copyWorkflow(copy);
        assertNotEquals(created.getWorkflowId(), copied.getWorkflowId());
        assertEquals(2, service.listApplications(
                new ApplicationListQuery("workflow", "PolicyFlow", "dev-admin", "default-org")).getTotal());

        WorkflowImportCommand importCommand = new WorkflowImportCommand();
        importCommand.setName("ImportedFlow");
        importCommand.setDesc("imported");
        importCommand.setSchema("{\"nodes\":[]}");
        importCommand.setUserId("dev-admin");
        importCommand.setOrgId("default-org");
        WorkflowCreateResult imported = service.importWorkflow(importCommand);
        assertEquals("ImportedFlow", service.exportWorkflow(
                new WorkflowExportQuery(imported.getWorkflowId(), "", false, "dev-admin", "default-org")).getName());

        WorkflowDeleteCommand delete = new WorkflowDeleteCommand();
        delete.setWorkflowId(created.getWorkflowId());
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");
        service.deleteWorkflow(delete);
        assertEquals(0, service.listApplications(
                new ApplicationListQuery("workflow", created.getWorkflowId(), "dev-admin", "default-org")).getTotal());
    }

    @Test
    public void chatflowLifecycleUsesWorkflowStorageWithoutPollutingWorkflowList() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("PolicyChat");
        create.setDesc("policy chatflow");
        create.setAvatarKey("avatars/chatflow.png");
        create.setAvatarPath("/static/chatflow.png");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createChatflow(create);

        assertTrue(created.getWorkflowId().startsWith("chatflow-"));
        assertEquals(0, service.listApplications(
                new ApplicationListQuery("workflow", "Policy", "dev-admin", "default-org")).getTotal());

        ApplicationListResult chatflows = service.listApplications(
                new ApplicationListQuery("chatflow", "Policy", "dev-admin", "default-org"));
        assertEquals(1, chatflows.getTotal());
        assertEquals(created.getWorkflowId(), chatflows.getList().get(0).get("workflow_id"));
        assertEquals("chatflow", chatflows.getList().get(0).get("appType"));

        WorkflowExportResult draftExport = service.exportChatflow(
                new WorkflowExportQuery(created.getWorkflowId(), "", false, "dev-admin", "default-org"));
        assertEquals("PolicyChat", draftExport.getName());
        assertTrue(draftExport.getSchema().contains(created.getWorkflowId()));

        ChatflowApplicationListQuery applicationListQuery = new ChatflowApplicationListQuery();
        applicationListQuery.setWorkflowId(created.getWorkflowId());
        applicationListQuery.setUserId("dev-admin");
        applicationListQuery.setOrgId("default-org");
        Map<String, Object> applicationList = service.listChatflowApplications(applicationListQuery);
        assertEquals(1, applicationList.get("total"));
        List<Map<String, Object>> intelligences = castList(applicationList.get("intelligences"));
        Map<String, Object> basicInfo = castMap(intelligences.get(0).get("basic_info"));
        String applicationId = String.valueOf(basicInfo.get("id"));
        assertEquals("PolicyChat", basicInfo.get("name"));

        ChatflowApplicationInfoQuery infoQuery = new ChatflowApplicationInfoQuery();
        infoQuery.setIntelligenceId(applicationId);
        infoQuery.setIntelligenceType(1L);
        infoQuery.setUserId("dev-admin");
        infoQuery.setOrgId("default-org");
        Map<String, Object> info = service.getChatflowApplication(infoQuery);
        assertEquals(1L, info.get("intelligence_type"));
        assertEquals(applicationId, String.valueOf(castMap(info.get("basic_info")).get("id")));

        AppPublishCommand publish = new AppPublishCommand();
        publish.setAppId(created.getWorkflowId());
        publish.setAppType("chatflow");
        publish.setUserId("dev-admin");
        publish.setOrgId("default-org");
        publish.setVersion("v1.0.0");
        publish.setDesc("first chatflow version");
        publish.setPublishType("public");
        service.publishApp(publish);
        assertEquals("v1.0.0", service.getLatestAppVersion(
                new AppVersionQuery(created.getWorkflowId(), "chatflow", "dev-admin", "default-org")).getVersion());

        WorkflowCopyCommand copy = new WorkflowCopyCommand();
        copy.setWorkflowId(created.getWorkflowId());
        copy.setUserId("dev-admin");
        copy.setOrgId("default-org");
        WorkflowCreateResult copied = service.copyChatflow(copy);
        assertTrue(copied.getWorkflowId().startsWith("chatflow-"));
        assertEquals(2, service.listApplications(
                new ApplicationListQuery("chatflow", "PolicyChat", "dev-admin", "default-org")).getTotal());

        WorkflowImportCommand importCommand = new WorkflowImportCommand();
        importCommand.setName("ImportedChat");
        importCommand.setDesc("imported chatflow");
        importCommand.setSchema("{\"nodes\":[]}");
        importCommand.setUserId("dev-admin");
        importCommand.setOrgId("default-org");
        WorkflowCreateResult imported = service.importChatflow(importCommand);
        assertEquals("ImportedChat", service.exportChatflow(
                new WorkflowExportQuery(imported.getWorkflowId(), "", false, "dev-admin", "default-org")).getName());

        ChatflowConversationDeleteCommand deleteConversation = new ChatflowConversationDeleteCommand();
        deleteConversation.setProjectId(created.getWorkflowId());
        deleteConversation.setUniqueId(applicationId);
        service.deleteChatflowConversation(deleteConversation);

        WorkflowDeleteCommand delete = new WorkflowDeleteCommand();
        delete.setWorkflowId(created.getWorkflowId());
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");
        service.deleteChatflow(delete);
        assertEquals(0, service.listApplications(
                new ApplicationListQuery("chatflow", created.getWorkflowId(), "dev-admin", "default-org")).getTotal());
    }

    @Test
    public void apiKeyLifecycleMatchesGoUserOpenApiKeyBehavior() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        ApiKeyInfo created = service.createApiKey(apiKeyCreate("Main key", "first", "2030-01-01"));

        assertNotNull(created.getKeyId());
        assertTrue(created.getKey().length() >= 24);
        assertEquals("Main key", created.getName());
        assertEquals("first", created.getDesc());
        assertEquals("2030-01-01", created.getExpiredAt());
        assertTrue(created.getStatus());

        ApiKeyPageResult page = service.listApiKeys(new ApiKeyListQuery(1, 10, "dev-admin", "default-org"));
        assertEquals(1, page.getTotal());
        assertEquals(created.getKeyId(), page.getList().get(0).getKeyId());

        ApiKeyUpdateCommand update = new ApiKeyUpdateCommand();
        update.setKeyId(created.getKeyId());
        update.setName("Main key updated");
        update.setDesc("updated");
        update.setExpiredAt("");
        update.setUserId("dev-admin");
        update.setOrgId("default-org");
        service.updateApiKey(update);

        ApiKeyInfo updated = service.listApiKeys(new ApiKeyListQuery(1, 10, "dev-admin", "default-org"))
                .getList().get(0);
        assertEquals("Main key updated", updated.getName());
        assertEquals("", updated.getExpiredAt());

        ApiKeyStatusCommand status = new ApiKeyStatusCommand();
        status.setKeyId(created.getKeyId());
        status.setStatus(false);
        service.updateApiKeyStatus(status);
        assertFalse(service.getApiKeyByKey(created.getKey()).getStatus());

        ApiKeyDeleteCommand delete = new ApiKeyDeleteCommand();
        delete.setKeyId(created.getKeyId());
        service.deleteApiKey(delete);
        assertEquals(0, service.listApiKeys(new ApiKeyListQuery(1, 10, "dev-admin", "default-org")).getTotal());
    }

    @Test
    public void appKeyLifecycleMatchesGoApplicationKeyBehavior() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());
        AssistantCreateResult created = service.createAssistant(command("KeyAgent", "key desc"));

        AppKeyCreateCommand command = new AppKeyCreateCommand();
        command.setAppId(created.getAssistantId());
        command.setAppType("agent");
        command.setUserId("dev-admin");
        command.setOrgId("default-org");

        AppKeyInfo appKey = service.createAppKey(command);

        assertNotNull(appKey.getApiId());
        assertEquals(created.getAssistantId(), appKey.getAppId());
        assertEquals("agent", appKey.getAppType());
        assertTrue(appKey.getApiKey().length() >= 24);

        List<AppKeyInfo> list = service.listAppKeys(
                new AppKeyListQuery(created.getAssistantId(), "agent", "dev-admin", "default-org"));
        assertEquals(1, list.size());
        assertEquals(appKey.getApiKey(), list.get(0).getApiKey());

        AppKeyDeleteCommand delete = new AppKeyDeleteCommand();
        delete.setApiId(appKey.getApiId());
        service.deleteAppKey(delete);
        assertTrue(service.listAppKeys(
                new AppKeyListQuery(created.getAssistantId(), "agent", "dev-admin", "default-org")).isEmpty());
    }

    @Test
    public void getAssistantDraftReturnsEditorDefaultsFromPersistedAgent() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        AssistantCreateResult created = service.createAssistant(command("DraftAgent", "draft desc"));

        Map<String, Object> draft = service.getAssistantDraft(
                new AssistantDetailQuery(created.getAssistantId(), "dev-admin", "default-org"));

        assertEquals(created.getAssistantId(), draft.get("assistantId"));
        assertEquals(created.getAssistantId(), draft.get("uuid"));
        assertEquals("DraftAgent", draft.get("name"));
        assertEquals("draft desc", draft.get("desc"));
        assertEquals("", draft.get("publishType"));
        assertTrue(draft.containsKey("knowledgeBaseConfig"));
        assertTrue(draft.containsKey("modelConfig"));
        assertTrue(draft.containsKey("rerankConfig"));
    }

    @Test
    public void assistantResourceBindingsPersistIntoDraftAndSupportSwitchDelete() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());
        AssistantCreateResult created = service.createAssistant(command("ToolAgent", "tool desc"));
        AssistantCreateResult child = service.createAssistant(command("ChildAgent", "child desc"));

        service.addAssistantWorkflow(resource(created.getAssistantId(), "workflow-001", "workflow", null));
        AssistantResourceCommand workflowSwitch = resource(created.getAssistantId(), "workflow-001", "workflow", null);
        workflowSwitch.setEnable(false);
        service.switchAssistantWorkflow(workflowSwitch);

        service.addAssistantMcp(resource(created.getAssistantId(), "mcp-001", "mcp", "search"));
        AssistantResourceCommand mcpSwitch = resource(created.getAssistantId(), "mcp-001", "mcp", "search");
        mcpSwitch.setEnable(false);
        service.switchAssistantMcp(mcpSwitch);

        service.addAssistantTool(resource(created.getAssistantId(), "builtin-weather", "builtin", "get_weather"));
        AssistantResourceCommand toolConfig = resource(created.getAssistantId(), "builtin-weather", "builtin", "get_weather");
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("rerankId", "rerank-001");
        toolConfig.setToolConfig(config);
        service.configureAssistantTool(toolConfig);

        service.addAssistantSkill(resource(created.getAssistantId(), "builtin-summary", "builtin", null));
        AssistantResourceCommand skillSwitch = resource(created.getAssistantId(), "builtin-summary", "builtin", null);
        skillSwitch.setEnable(false);
        service.switchAssistantSkill(skillSwitch);

        AssistantResourceCommand childAgent = resource(created.getAssistantId(), child.getAssistantId(), "agent", null);
        childAgent.setDesc("Routes child tasks");
        service.addAssistantAgent(childAgent);

        Map<String, Object> draft = service.getAssistantDraft(
                new AssistantDetailQuery(created.getAssistantId(), "dev-admin", "default-org"));
        List<Map<String, Object>> workflows = castList(draft.get("workFlowInfos"));
        assertEquals(1, workflows.size());
        assertEquals("workflow-001", workflows.get(0).get("workFlowId"));
        assertEquals(false, workflows.get(0).get("enable"));
        assertEquals("workflow_workflow-001", workflows.get(0).get("uniqueId"));

        List<Map<String, Object>> mcps = castList(draft.get("mcpInfos"));
        assertEquals(1, mcps.size());
        assertEquals("mcp-001", mcps.get(0).get("mcpId"));
        assertEquals("search", mcps.get(0).get("actionName"));
        assertEquals(false, mcps.get(0).get("enable"));

        List<Map<String, Object>> tools = castList(draft.get("toolInfos"));
        assertEquals(1, tools.size());
        assertEquals("builtin-weather", tools.get(0).get("toolId"));
        assertEquals("get_weather", tools.get(0).get("actionName"));
        assertEquals("rerank-001", ((Map<?, ?>) tools.get(0).get("toolConfig")).get("rerankId"));

        List<Map<String, Object>> skills = castList(draft.get("skillInfos"));
        assertEquals(1, skills.size());
        assertEquals("builtin-summary", skills.get(0).get("skillId"));
        assertEquals(false, skills.get(0).get("enable"));

        List<Map<String, Object>> agents = castList(draft.get("multiAgentInfos"));
        assertEquals(1, agents.size());
        assertEquals(child.getAssistantId(), agents.get(0).get("agentId"));
        assertEquals("ChildAgent", agents.get(0).get("name"));
        assertEquals("Routes child tasks", agents.get(0).get("desc"));

        service.deleteAssistantTool(resource(created.getAssistantId(), "builtin-weather", "builtin", "get_weather"));
        Map<String, Object> afterDelete = service.getAssistantDraft(
                new AssistantDetailQuery(created.getAssistantId(), "dev-admin", "default-org"));
        assertTrue(castList(afterDelete.get("toolInfos")).isEmpty());
    }

    @Test
    public void updateAssistantPersistsAndListReflectsBaseFields() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());
        AssistantCreateResult created = service.createAssistant(command("BeforeAgent", "before desc"));

        AssistantUpdateCommand update = new AssistantUpdateCommand();
        update.setAssistantId(created.getAssistantId());
        update.setUserId("dev-admin");
        update.setOrgId("default-org");
        update.setName("AfterAgent");
        update.setDesc("after desc");
        update.setCategory(2);
        update.setAvatarKey("avatars/after.png");
        update.setAvatarPath("/static/after.png");

        service.updateAssistant(update);

        ApplicationListResult result = service.listAssistants(
                new ApplicationListQuery("agent", "After", "dev-admin", "default-org"));
        assertEquals(1, result.getTotal());
        Map<String, Object> item = result.getList().get(0);
        assertEquals(created.getAssistantId(), item.get("appId"));
        assertEquals("AfterAgent", item.get("name"));
        assertEquals("after desc", item.get("desc"));
        assertEquals(2, item.get("category"));
        assertEquals("/static/after.png", ((Map<?, ?>) item.get("avatar")).get("path"));

        Map<String, Object> draft = service.getAssistantDraft(
                new AssistantDetailQuery(created.getAssistantId(), "dev-admin", "default-org"));
        assertEquals("AfterAgent", draft.get("name"));
        assertEquals("after desc", draft.get("desc"));
    }

    @Test
    public void updateAssistantConfigPersistsAndDraftEchoesConfig() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());
        AssistantCreateResult created = service.createAssistant(command("ConfigAgent", "config desc"));

        AssistantConfigUpdateCommand config = new AssistantConfigUpdateCommand();
        config.setAssistantId(created.getAssistantId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setPrologue("Hello from draft");
        config.setInstructions("Always answer with concise steps.");

        Map<String, Object> memoryConfig = new LinkedHashMap<>();
        memoryConfig.put("maxHistoryLength", 9);
        config.setMemoryConfig(memoryConfig);

        Map<String, Object> modelParams = new LinkedHashMap<>();
        modelParams.put("temperature", 0.7);
        Map<String, Object> modelConfig = new LinkedHashMap<>();
        modelConfig.put("config", modelParams);
        modelConfig.put("modelId", "llm-001");
        modelConfig.put("model", "demo-model");
        config.setModelConfig(modelConfig);

        Map<String, Object> visionConfig = new LinkedHashMap<>();
        visionConfig.put("picNum", 5);
        config.setVisionConfig(visionConfig);
        config.setRecommendQuestion(Arrays.asList("What can you do?", "Show me an example."));

        service.updateAssistantConfig(config);

        Map<String, Object> draft = service.getAssistantDraft(
                new AssistantDetailQuery(created.getAssistantId(), "dev-admin", "default-org"));
        assertEquals("Hello from draft", draft.get("prologue"));
        assertEquals("Always answer with concise steps.", draft.get("instructions"));
        assertEquals(memoryConfig, draft.get("memoryConfig"));
        assertEquals(modelConfig, draft.get("modelConfig"));
        assertEquals(visionConfig, draft.get("visionConfig"));
        assertEquals(Arrays.asList("What can you do?", "Show me an example."), draft.get("recommendQuestion"));
    }

    @Test
    public void updateAssistantConfigRequiresExistingAssistant() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantConfigUpdateCommand config = new AssistantConfigUpdateCommand();
        config.setAssistantId("assistant-missing");
        config.setUserId("dev-admin");
        config.setOrgId("default-org");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.updateAssistantConfig(config));
        assertEquals("assistant draft not found", error.getMessage());
    }

    @Test
    public void deleteAssistantRemovesAssistantFromListAndDraftLookup() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());
        AssistantCreateResult created = service.createAssistant(command("DeleteAgent", "delete desc"));

        AssistantConfigUpdateCommand config = new AssistantConfigUpdateCommand();
        config.setAssistantId(created.getAssistantId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setInstructions("To be removed.");
        service.updateAssistantConfig(config);

        AssistantDeleteCommand delete = new AssistantDeleteCommand();
        delete.setAssistantId(created.getAssistantId());
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");

        service.deleteAssistant(delete);

        ApplicationListResult result = service.listAssistants(
                new ApplicationListQuery("agent", "Delete", "dev-admin", "default-org"));
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
        assertTrue(!repository.containsApp(created.getAssistantId()));
        assertTrue(!repository.containsConfig(created.getAssistantId()));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.getAssistantDraft(
                        new AssistantDetailQuery(created.getAssistantId(), "dev-admin", "default-org")));
        assertEquals("assistant draft not found", error.getMessage());
    }

    @Test
    public void deleteAssistantRequiresExistingAssistant() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantDeleteCommand delete = new AssistantDeleteCommand();
        delete.setAssistantId("assistant-missing");
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.deleteAssistant(delete));
        assertEquals("assistant draft not found", error.getMessage());
    }

    @Test
    public void copyAssistantDuplicatesBaseFieldsAndConfigWithNextName() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());
        AssistantCreateResult original = service.createAssistant(command("CopyAgent", "copy desc"));

        AssistantConfigUpdateCommand config = new AssistantConfigUpdateCommand();
        config.setAssistantId(original.getAssistantId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setPrologue("Hello copied agent");
        config.setInstructions("Copied instructions");
        Map<String, Object> memoryConfig = new LinkedHashMap<>();
        memoryConfig.put("maxHistoryLength", 11);
        config.setMemoryConfig(memoryConfig);
        service.updateAssistantConfig(config);

        AssistantCreateResult firstCopy = service.copyAssistant(copyCommand(original.getAssistantId()));
        AssistantCreateResult secondCopy = service.copyAssistant(copyCommand(original.getAssistantId()));

        assertNotEquals(original.getAssistantId(), firstCopy.getAssistantId());
        assertNotEquals(firstCopy.getAssistantId(), secondCopy.getAssistantId());

        ApplicationListResult result = service.listAssistants(
                new ApplicationListQuery("agent", "CopyAgent", "dev-admin", "default-org"));
        assertEquals(3, result.getTotal());
        assertTrue(listContainsName(result, "CopyAgent"));
        assertTrue(listContainsName(result, "CopyAgent_1"));
        assertTrue(listContainsName(result, "CopyAgent_2"));

        Map<String, Object> copiedDraft = service.getAssistantDraft(
                new AssistantDetailQuery(firstCopy.getAssistantId(), "dev-admin", "default-org"));
        assertEquals("CopyAgent_1", copiedDraft.get("name"));
        assertEquals("copy desc", copiedDraft.get("desc"));
        assertEquals("Hello copied agent", copiedDraft.get("prologue"));
        assertEquals("Copied instructions", copiedDraft.get("instructions"));
        assertEquals(memoryConfig, copiedDraft.get("memoryConfig"));

        Map<String, Object> originalDraft = service.getAssistantDraft(
                new AssistantDetailQuery(original.getAssistantId(), "dev-admin", "default-org"));
        assertEquals("CopyAgent", originalDraft.get("name"));
        assertEquals("Copied instructions", originalDraft.get("instructions"));
    }

    @Test
    public void copyAssistantRequiresExistingAssistant() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.copyAssistant(copyCommand("assistant-missing")));
        assertEquals("assistant draft not found", error.getMessage());
    }

    @Test
    public void publishAssistantCreatesSnapshotAndExposeLatestVersion() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());
        AssistantCreateResult created = service.createAssistant(command("PublishAgent", "draft desc"));

        AssistantConfigUpdateCommand config = new AssistantConfigUpdateCommand();
        config.setAssistantId(created.getAssistantId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setPrologue("Snapshot prologue");
        config.setInstructions("Snapshot instructions");
        Map<String, Object> memoryConfig = new LinkedHashMap<>();
        memoryConfig.put("maxHistoryLength", 7);
        config.setMemoryConfig(memoryConfig);
        service.updateAssistantConfig(config);

        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "organization"));

        AppVersionInfo latest = service.getLatestAppVersion(versionQuery(created.getAssistantId()));
        assertEquals("v1.0.0", latest.getVersion());
        assertEquals("first release", latest.getDesc());
        assertEquals("organization", latest.getPublishType());
        assertEquals("2026-06-29 10:00:00", latest.getCreatedAt());

        ApplicationListResult list = service.listAssistants(
                new ApplicationListQuery("agent", "PublishAgent", "dev-admin", "default-org"));
        assertEquals("organization", list.getList().get(0).get("publishType"));
        assertEquals("v1.0.0", list.getList().get(0).get("version"));

        Map<String, Object> published = service.getPublishedAssistant(
                new AssistantPublishedQuery(created.getAssistantId(), "v1.0.0", "dev-admin", "default-org"));
        assertEquals("PublishAgent", published.get("name"));
        assertEquals("Snapshot prologue", published.get("prologue"));
        assertEquals("Snapshot instructions", published.get("instructions"));
        assertEquals(memoryConfig, published.get("memoryConfig"));
    }

    @Test
    public void publishAssistantRequiresIncreasingVersion() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("VersionAgent", "version desc"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "private"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "duplicate", "private")));
        assertEquals("app version must be greater than latest version", error.getMessage());
    }

    @Test
    public void updateAppVersionUpdatesLatestSnapshotAndPublishType() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("UpdateVersionAgent", "version desc"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "private"));

        AppVersionUpdateCommand update = new AppVersionUpdateCommand();
        update.setAppId(created.getAssistantId());
        update.setAppType("agent");
        update.setUserId("dev-admin");
        update.setOrgId("default-org");
        update.setDesc("updated release");
        update.setPublishType("public");

        service.updateAppVersion(update);

        AppVersionInfo latest = service.getLatestAppVersion(versionQuery(created.getAssistantId()));
        assertEquals("v1.0.0", latest.getVersion());
        assertEquals("updated release", latest.getDesc());
        assertEquals("public", latest.getPublishType());
    }

    @Test
    public void rollbackAppVersionRestoresDraftFromSnapshot() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("RollbackAgent", "first desc"));

        AssistantConfigUpdateCommand firstConfig = new AssistantConfigUpdateCommand();
        firstConfig.setAssistantId(created.getAssistantId());
        firstConfig.setUserId("dev-admin");
        firstConfig.setOrgId("default-org");
        firstConfig.setPrologue("first prologue");
        firstConfig.setInstructions("first instructions");
        service.updateAssistantConfig(firstConfig);
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "private"));

        AssistantUpdateCommand update = new AssistantUpdateCommand();
        update.setAssistantId(created.getAssistantId());
        update.setUserId("dev-admin");
        update.setOrgId("default-org");
        update.setName("RollbackAgentChanged");
        update.setDesc("changed desc");
        update.setCategory(1);
        service.updateAssistant(update);

        AssistantConfigUpdateCommand changedConfig = new AssistantConfigUpdateCommand();
        changedConfig.setAssistantId(created.getAssistantId());
        changedConfig.setUserId("dev-admin");
        changedConfig.setOrgId("default-org");
        changedConfig.setPrologue("changed prologue");
        changedConfig.setInstructions("changed instructions");
        service.updateAssistantConfig(changedConfig);

        AppVersionRollbackCommand rollback = new AppVersionRollbackCommand();
        rollback.setAppId(created.getAssistantId());
        rollback.setAppType("agent");
        rollback.setVersion("v1.0.0");
        rollback.setUserId("dev-admin");
        rollback.setOrgId("default-org");

        service.rollbackAppVersion(rollback);

        Map<String, Object> draft = service.getAssistantDraft(
                new AssistantDetailQuery(created.getAssistantId(), "dev-admin", "default-org"));
        assertEquals("RollbackAgent", draft.get("name"));
        assertEquals("first desc", draft.get("desc"));
        assertEquals("first prologue", draft.get("prologue"));
        assertEquals("first instructions", draft.get("instructions"));
    }

    @Test
    public void listAppVersionsReturnsNewestFirst() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("HistoryAgent", "history desc"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "private"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.1", "second release", "private"));

        AppVersionListResult result = service.listAppVersions(versionQuery(created.getAssistantId()));

        assertEquals(2, result.getTotal());
        assertEquals("v1.0.1", result.getList().get(0).getVersion());
        assertEquals("v1.0.0", result.getList().get(1).getVersion());
    }

    @Test
    public void unpublishAppClearsPublishTypeButKeepsDraft() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("UnpublishAgent", "unpublish desc"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "private"));

        service.unpublishApp(publishCommand(created.getAssistantId(), "", "", ""));

        ApplicationListResult list = service.listAssistants(
                new ApplicationListQuery("agent", "UnpublishAgent", "dev-admin", "default-org"));
        assertEquals(1, list.getTotal());
        assertEquals("", list.getList().get(0).get("publishType"));
        assertEquals("v1.0.0", list.getList().get(0).get("version"));
    }

    @Test
    public void createPublishedConversationPersistsConversationList() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("ChatAgent", "chat desc"));

        AssistantConversationCreateResult conversation = service.createAssistantConversation(
                conversationCreateCommand(created.getAssistantId(), "hello agent", "published"));

        AssistantConversationPageResult result = service.listAssistantConversations(
                conversationListQuery(created.getAssistantId(), "published"));

        assertEquals(1, result.getTotal());
        assertEquals(conversation.getConversationId(), result.getList().get(0).get("conversationId"));
        assertEquals(created.getAssistantId(), result.getList().get(0).get("assistantId"));
        assertEquals("hello agent", result.getList().get(0).get("title"));
        assertEquals("2026-06-29 10:00:00", result.getList().get(0).get("createdAt"));
    }

    @Test
    public void draftStreamCreatesConversationAndPersistsMessageDetail() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("DraftChatAgent", "draft chat desc"));

        AssistantConversationStreamCommand stream = streamCommand(created.getAssistantId(), "", "How are you?", true);
        AssistantConversationStreamResult result = service.streamAssistantConversation(stream);

        assertEquals(created.getAssistantId(), result.getAssistantId());
        assertTrue(result.getConversationId().startsWith("conversation-"));
        assertTrue(result.getDetailId().startsWith("detail-"));
        assertTrue(result.getResponse().contains("DraftChatAgent"));
        assertTrue(result.getResponse().contains("How are you?"));

        AssistantConversationPageResult details = service.listAssistantConversationDetails(
                conversationDetailQuery(result.getConversationId()));
        assertEquals(1, details.getTotal());
        Map<String, Object> detail = details.getList().get(0);
        assertEquals(result.getDetailId(), detail.get("id"));
        assertEquals("How are you?", detail.get("prompt"));
        assertEquals(result.getResponse(), detail.get("response"));
        assertEquals(created.getAssistantId(), detail.get("assistantId"));
        assertEquals(result.getConversationId(), detail.get("conversationId"));
        assertTrue(((List<?>) detail.get("responseList")).isEmpty());
        assertTrue(((List<?>) detail.get("requestFiles")).isEmpty());
    }

    @Test
    public void draftConversationHistoryReusesOneConversationPerAssistant() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("DraftReuseAgent", "draft reuse desc"));

        AssistantConversationStreamResult first = service.streamAssistantConversation(
                streamCommand(created.getAssistantId(), "", "first question", true));
        AssistantConversationStreamResult second = service.streamAssistantConversation(
                streamCommand(created.getAssistantId(), "", "second question", true));

        assertEquals(first.getConversationId(), second.getConversationId());

        AssistantConversationPageResult draftHistory = service.listDraftAssistantConversationDetails(
                conversationListQuery(created.getAssistantId(), "draft"));
        assertEquals(2, draftHistory.getTotal());
        assertEquals("first question", draftHistory.getList().get(0).get("prompt"));
        assertEquals("second question", draftHistory.getList().get(1).get("prompt"));
    }

    @Test
    public void clearConversationDeletesSingleDetailButKeepsConversation() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("ClearChatAgent", "clear desc"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "private"));
        AssistantConversationCreateResult conversation = service.createAssistantConversation(
                conversationCreateCommand(created.getAssistantId(), "start", "published"));
        AssistantConversationStreamResult first = service.streamAssistantConversation(
                streamCommand(created.getAssistantId(), conversation.getConversationId(), "first", false));
        AssistantConversationStreamResult second = service.streamAssistantConversation(
                streamCommand(created.getAssistantId(), conversation.getConversationId(), "second", false));

        AssistantConversationDeleteCommand clear = new AssistantConversationDeleteCommand();
        clear.setConversationId(conversation.getConversationId());
        clear.setDetailId(first.getDetailId());
        clear.setUserId("dev-admin");
        clear.setOrgId("default-org");
        service.clearAssistantConversation(clear);

        AssistantConversationPageResult details = service.listAssistantConversationDetails(
                conversationDetailQuery(conversation.getConversationId()));
        assertEquals(1, details.getTotal());
        assertEquals(second.getDetailId(), details.getList().get(0).get("id"));

        AssistantConversationPageResult conversations = service.listAssistantConversations(
                conversationListQuery(created.getAssistantId(), "published"));
        assertEquals(1, conversations.getTotal());
    }

    @Test
    public void deleteConversationRemovesConversationAndDetails() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("DeleteChatAgent", "delete chat desc"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "private"));
        AssistantConversationCreateResult conversation = service.createAssistantConversation(
                conversationCreateCommand(created.getAssistantId(), "start", "published"));
        service.streamAssistantConversation(
                streamCommand(created.getAssistantId(), conversation.getConversationId(), "hello", false));

        AssistantConversationDeleteCommand delete = new AssistantConversationDeleteCommand();
        delete.setConversationId(conversation.getConversationId());
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");
        service.deleteAssistantConversation(delete);

        assertEquals(0, service.listAssistantConversations(
                conversationListQuery(created.getAssistantId(), "published")).getTotal());
        assertEquals(0, service.listAssistantConversationDetails(
                conversationDetailQuery(conversation.getConversationId())).getTotal());
    }

    @Test
    public void publishedStreamRequiresSnapshot() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("UnpublishedChatAgent", "unpublished desc"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.streamAssistantConversation(
                        streamCommand(created.getAssistantId(), "", "published question", false)));

        assertEquals("assistant snapshot not found", error.getMessage());
    }

    @Test
    public void publicConversationCanUseClientUserWhileResolvingAssistantByOwner() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("PublicClientAgent", "public desc"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "public"));

        AssistantConversationStreamCommand stream = streamCommand(
                created.getAssistantId(), "", "public question", false);
        stream.setUserId("client-001");
        stream.setOrgId("default-org");

        AssistantConversationStreamResult result = service.streamAssistantConversation(stream);

        assertEquals(created.getAssistantId(), result.getAssistantId());
        assertEquals("Demo response from PublicClientAgent: public question", result.getResponse());
        AssistantConversationListQuery query = conversationListQuery(created.getAssistantId(), "published");
        query.setUserId("client-001");
        AssistantConversationPageResult conversations = service.listAssistantConversations(query);
        assertEquals(1, conversations.getTotal());
        assertEquals(result.getConversationId(), conversations.getList().get(0).get("conversationId"));
    }

    @Test
    public void createAppUrlPersistsEnabledUrlForPublishedAssistant() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("OpenUrlAgent", "open desc"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "public"));

        service.createAppUrl(appUrlCreateCommand(created.getAssistantId(), "Public demo", "2026-07-01 12:30:00"));

        List<AppUrlInfo> urls = service.listAppUrls(appUrlListQuery(created.getAssistantId()));
        assertEquals(1, urls.size());
        AppUrlInfo info = urls.get(0);
        assertEquals(created.getAssistantId(), info.getAppId());
        assertEquals("agent", info.getAppType());
        assertEquals("Public demo", info.getName());
        assertEquals("OpenURL for frontend", info.getDescription());
        assertEquals("2026-06-29 10:00:00", info.getCreatedAt());
        assertEquals("2026-07-01 12:30:00", info.getExpiredAt());
        assertEquals(true, info.isStatus());
        assertTrue(info.getSuffix().length() >= 16);

        AppUrlInfo publicInfo = service.getAppUrlBySuffix(new AppUrlSuffixQuery(info.getSuffix()));
        assertEquals(info.getUrlId(), publicInfo.getUrlId());
        assertEquals(created.getAssistantId(), publicInfo.getAppId());
        assertEquals("default-org", publicInfo.getOrgId());
    }

    @Test
    public void createAppUrlRequiresPublishedAssistantSnapshot() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("NoSnapshotAgent", "draft only"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.createAppUrl(appUrlCreateCommand(created.getAssistantId(), "Public demo", "")));

        assertEquals("assistant snapshot not found", error.getMessage());
    }

    @Test
    public void disabledOrExpiredAppUrlCannotBeResolvedBySuffix() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("GuardedOpenUrlAgent", "guarded desc"));
        service.publishApp(publishCommand(created.getAssistantId(), "v1.0.0", "first release", "public"));
        service.createAppUrl(appUrlCreateCommand(created.getAssistantId(), "Guarded link", "2026-07-01 12:30:00"));
        AppUrlInfo info = service.listAppUrls(appUrlListQuery(created.getAssistantId())).get(0);

        AppUrlStatusCommand status = new AppUrlStatusCommand();
        status.setUrlId(info.getUrlId());
        status.setStatus(false);
        status.setUserId("dev-admin");
        status.setOrgId("default-org");
        service.updateAppUrlStatus(status);

        IllegalArgumentException disabled = assertThrows(IllegalArgumentException.class,
                () -> service.getAppUrlBySuffix(new AppUrlSuffixQuery(info.getSuffix())));
        assertEquals("app url disabled", disabled.getMessage());

        AppUrlUpdateCommand update = appUrlUpdateCommand(info.getUrlId(), "Guarded link", "2026-06-28 09:00:00");
        service.updateAppUrl(update);
        status.setStatus(true);
        service.updateAppUrlStatus(status);

        IllegalArgumentException expired = assertThrows(IllegalArgumentException.class,
                () -> service.getAppUrlBySuffix(new AppUrlSuffixQuery(info.getSuffix())));
        assertEquals("app url expired", expired.getMessage());
    }

    private AssistantCreateCommand command(String name, String desc) {
        AssistantCreateCommand command = new AssistantCreateCommand();
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        command.setName(name);
        command.setDesc(desc);
        command.setCategory(1);
        command.setAvatarKey("avatars/" + name + ".png");
        command.setAvatarPath("/static/" + name + ".png");
        return command;
    }

    private AssistantCopyCommand copyCommand(String assistantId) {
        AssistantCopyCommand command = new AssistantCopyCommand();
        command.setAssistantId(assistantId);
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        return command;
    }

    private AppPublishCommand publishCommand(String assistantId, String version, String desc, String publishType) {
        AppPublishCommand command = new AppPublishCommand();
        command.setAppId(assistantId);
        command.setAppType("agent");
        command.setVersion(version);
        command.setDesc(desc);
        command.setPublishType(publishType);
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        return command;
    }

    private AppVersionQuery versionQuery(String assistantId) {
        return new AppVersionQuery(assistantId, "agent", "dev-admin", "default-org");
    }

    private ApiKeyCreateCommand apiKeyCreate(String name, String desc, String expiredAt) {
        ApiKeyCreateCommand command = new ApiKeyCreateCommand();
        command.setName(name);
        command.setDesc(desc);
        command.setExpiredAt(expiredAt);
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        return command;
    }

    private AppUrlCreateCommand appUrlCreateCommand(String assistantId, String name, String expiredAt) {
        AppUrlCreateCommand command = new AppUrlCreateCommand();
        command.setAppId(assistantId);
        command.setAppType("agent");
        command.setName(name);
        command.setDescription("OpenURL for frontend");
        command.setExpiredAt(expiredAt);
        command.setCopyright("Copyright");
        command.setCopyrightEnable(true);
        command.setPrivacyPolicy("Privacy");
        command.setPrivacyPolicyEnable(true);
        command.setDisclaimer("Disclaimer");
        command.setDisclaimerEnable(true);
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        return command;
    }

    private AppUrlUpdateCommand appUrlUpdateCommand(String urlId, String name, String expiredAt) {
        AppUrlUpdateCommand command = new AppUrlUpdateCommand();
        command.setUrlId(urlId);
        command.setName(name);
        command.setDescription("OpenURL for frontend");
        command.setExpiredAt(expiredAt);
        command.setCopyright("Copyright");
        command.setCopyrightEnable(true);
        command.setPrivacyPolicy("Privacy");
        command.setPrivacyPolicyEnable(true);
        command.setDisclaimer("Disclaimer");
        command.setDisclaimerEnable(true);
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        return command;
    }

    private AppUrlListQuery appUrlListQuery(String assistantId) {
        AppUrlListQuery query = new AppUrlListQuery();
        query.setAppId(assistantId);
        query.setAppType("agent");
        query.setUserId("dev-admin");
        query.setOrgId("default-org");
        return query;
    }

    private AssistantConversationCreateCommand conversationCreateCommand(String assistantId,
                                                                         String prompt,
                                                                         String conversationType) {
        AssistantConversationCreateCommand command = new AssistantConversationCreateCommand();
        command.setAssistantId(assistantId);
        command.setPrompt(prompt);
        command.setConversationType(conversationType);
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        return command;
    }

    private AssistantConversationListQuery conversationListQuery(String assistantId, String conversationType) {
        AssistantConversationListQuery query = new AssistantConversationListQuery();
        query.setAssistantId(assistantId);
        query.setConversationType(conversationType);
        query.setPageNo(1);
        query.setPageSize(20);
        query.setUserId("dev-admin");
        query.setOrgId("default-org");
        return query;
    }

    private AssistantConversationDetailQuery conversationDetailQuery(String conversationId) {
        AssistantConversationDetailQuery query = new AssistantConversationDetailQuery();
        query.setConversationId(conversationId);
        query.setPageNo(1);
        query.setPageSize(1000);
        query.setUserId("dev-admin");
        query.setOrgId("default-org");
        return query;
    }

    private AssistantConversationStreamCommand streamCommand(String assistantId,
                                                             String conversationId,
                                                             String prompt,
                                                             boolean draft) {
        AssistantConversationStreamCommand command = new AssistantConversationStreamCommand();
        command.setAssistantId(assistantId);
        command.setConversationId(conversationId);
        command.setPrompt(prompt);
        command.setDraft(draft);
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        return command;
    }

    private RagChatCommand ragChatCommand(String ragId, String question, boolean draft) {
        RagChatCommand command = new RagChatCommand();
        command.setRagId(ragId);
        command.setQuestion(question);
        command.setDraft(draft);
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        return command;
    }

    private AssistantResourceCommand resource(String assistantId,
                                              String resourceId,
                                              String resourceType,
                                              String actionName) {
        AssistantResourceCommand command = new AssistantResourceCommand();
        command.setAssistantId(assistantId);
        command.setResourceId(resourceId);
        command.setResourceType(resourceType);
        command.setActionName(actionName);
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        return command;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object value) {
        return (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private boolean listContainsName(ApplicationListResult result, String name) {
        for (Map<String, Object> item : result.getList()) {
            if (name.equals(item.get("name"))) {
                return true;
            }
        }
        return false;
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-06-29T02:00:00Z"), ZoneOffset.UTC);
    }

    private Map<String, Object> modelConfig(String modelId) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("temperature", 0.14);
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("provider", "local");
        model.put("model", modelId);
        model.put("modelId", modelId);
        model.put("modelType", modelId.contains("rerank") ? "rerank" : "llm");
        model.put("displayName", modelId);
        model.put("config", config);
        return model;
    }

    private Map<String, Object> knowledgeConfig(String knowledgeId) {
        Map<String, Object> knowledge = new LinkedHashMap<>();
        knowledge.put("id", knowledgeId);
        knowledge.put("name", knowledgeId);
        knowledge.put("category", 0);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("maxHistory", 0);
        params.put("threshold", 0.4);
        params.put("topK", 5);
        params.put("matchType", "mix");
        params.put("priorityMatch", 1);
        params.put("semanticsPriority", 0.2);
        params.put("keywordPriority", 0.8);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("knowledgebases", Collections.singletonList(knowledge));
        config.put("config", params);
        return config;
    }

    private Map<String, Object> safetyConfig(String tableId) {
        Map<String, Object> table = new LinkedHashMap<>();
        table.put("tableId", tableId);
        table.put("tableName", tableId);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("enable", true);
        config.put("tables", Collections.singletonList(table));
        return config;
    }

    private Map<String, Object> visionConfig(int picNum) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("picNum", picNum);
        return config;
    }

    private static class InMemoryApplicationRepository implements ApplicationRepository {

        private final AtomicLong ids = new AtomicLong();
        private final List<AppRecord> records = new ArrayList<>();
        private final List<AssistantDraftConfigRecord> configs = new ArrayList<>();
        private final List<AssistantSnapshotRecord> snapshots = new ArrayList<>();
        private final List<RagDraftConfigRecord> ragConfigs = new ArrayList<>();
        private final List<RagSnapshotRecord> ragSnapshots = new ArrayList<>();
        private final List<WorkflowDraftRecord> workflowDrafts = new ArrayList<>();
        private final List<WorkflowSnapshotRecord> workflowSnapshots = new ArrayList<>();
        private final List<AppUrlRecord> appUrls = new ArrayList<>();
        private final List<ApiKeyRecord> apiKeys = new ArrayList<>();
        private final List<AppKeyRecord> appKeys = new ArrayList<>();
        private final List<AssistantConversationRecord> conversations = new ArrayList<>();
        private final List<AssistantConversationMessageRecord> messages = new ArrayList<>();

        @Override
        public AppRecord saveAssistant(AppRecord record) {
            record.setId(ids.incrementAndGet());
            records.add(record);
            return record;
        }

        @Override
        public List<AppRecord> listAssistants(String userId, String orgId, String name) {
            List<AppRecord> matches = new ArrayList<>();
            for (AppRecord record : records) {
                if (!userId.equals(record.getUserId())) {
                    continue;
                }
                if (!orgId.equals(record.getOrgId())) {
                    continue;
                }
                if (name != null && !name.isEmpty() && !record.getName().contains(name)) {
                    continue;
                }
                matches.add(record);
            }
            matches.sort(Comparator.comparing(AppRecord::getId).reversed());
            return matches;
        }

        @Override
        public AppRecord findAssistant(String userId, String orgId, String assistantId) {
            for (AppRecord record : records) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && assistantId.equals(record.getAppId())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public AppRecord findAssistantByOrg(String orgId, String assistantId) {
            for (AppRecord record : records) {
                if (orgId.equals(record.getOrgId())
                        && assistantId.equals(record.getAppId())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public AppRecord updateAssistant(AppRecord record) {
            AppRecord existing = findAssistant(record.getUserId(), record.getOrgId(), record.getAppId());
            if (existing == null) {
                return null;
            }
            existing.setUpdatedAt(record.getUpdatedAt());
            existing.setName(record.getName());
            existing.setDesc(record.getDesc());
            existing.setAvatarKey(record.getAvatarKey());
            existing.setAvatarPath(record.getAvatarPath());
            existing.setCategory(record.getCategory());
            return existing;
        }

        @Override
        public AssistantDraftConfigRecord saveAssistantConfig(AssistantDraftConfigRecord record) {
            AssistantDraftConfigRecord existing = findAssistantConfig(
                    record.getUserId(), record.getOrgId(), record.getAssistantId());
            if (existing != null) {
                configs.remove(existing);
            }
            configs.add(record);
            return record;
        }

        @Override
        public AssistantDraftConfigRecord findAssistantConfig(String userId, String orgId, String assistantId) {
            for (AssistantDraftConfigRecord config : configs) {
                if (userId.equals(config.getUserId())
                        && orgId.equals(config.getOrgId())
                        && assistantId.equals(config.getAssistantId())) {
                    return config;
                }
            }
            return null;
        }

        @Override
        public boolean deleteAssistant(String userId, String orgId, String assistantId) {
            AppRecord existing = findAssistant(userId, orgId, assistantId);
            if (existing == null) {
                return false;
            }
            records.remove(existing);
            AssistantDraftConfigRecord config = findAssistantConfig(userId, orgId, assistantId);
            if (config != null) {
                configs.remove(config);
            }
            List<AssistantSnapshotRecord> removed = new ArrayList<>();
            for (AssistantSnapshotRecord snapshot : snapshots) {
                if (assistantId.equals(snapshot.getAssistantId())) {
                    removed.add(snapshot);
                }
            }
            snapshots.removeAll(removed);
            List<AppUrlRecord> removedAppUrls = new ArrayList<>();
            for (AppUrlRecord appUrl : appUrls) {
                if (assistantId.equals(appUrl.getAppId())) {
                    removedAppUrls.add(appUrl);
                }
            }
            appUrls.removeAll(removedAppUrls);
            List<AssistantConversationRecord> removedConversations = new ArrayList<>();
            for (AssistantConversationRecord conversation : conversations) {
                if (assistantId.equals(conversation.getAssistantId())) {
                    removedConversations.add(conversation);
                }
            }
            conversations.removeAll(removedConversations);
            List<AssistantConversationMessageRecord> removedMessages = new ArrayList<>();
            for (AssistantConversationMessageRecord message : messages) {
                if (assistantId.equals(message.getAssistantId())) {
                    removedMessages.add(message);
                }
            }
            messages.removeAll(removedMessages);
            return true;
        }

        @Override
        public List<String> listAssistantNamesByPrefix(String userId, String orgId, String prefix) {
            List<String> names = new ArrayList<>();
            for (AppRecord record : records) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && record.getName() != null
                        && record.getName().startsWith(prefix)) {
                    names.add(record.getName());
                }
            }
            return names;
        }

        @Override
        public AppRecord copyAssistant(AppRecord record, AssistantDraftConfigRecord config) {
            record.setId(ids.incrementAndGet());
            records.add(record);
            if (config != null) {
                configs.add(config);
            }
            return record;
        }

        @Override
        public AssistantSnapshotRecord saveAssistantSnapshot(AssistantSnapshotRecord snapshot) {
            snapshot.setId(ids.incrementAndGet());
            snapshots.add(snapshot);
            return snapshot;
        }

        @Override
        public List<AssistantSnapshotRecord> listAssistantSnapshots(String userId, String orgId, String assistantId) {
            List<AssistantSnapshotRecord> matches = new ArrayList<>();
            for (AssistantSnapshotRecord snapshot : snapshots) {
                if (userId.equals(snapshot.getUserId())
                        && orgId.equals(snapshot.getOrgId())
                        && assistantId.equals(snapshot.getAssistantId())) {
                    matches.add(snapshot);
                }
            }
            matches.sort(Comparator.comparing(AssistantSnapshotRecord::getId).reversed());
            return matches;
        }

        @Override
        public AssistantSnapshotRecord findLatestAssistantSnapshot(String userId, String orgId, String assistantId) {
            List<AssistantSnapshotRecord> matches = listAssistantSnapshots(userId, orgId, assistantId);
            return matches.isEmpty() ? null : matches.get(0);
        }

        @Override
        public AssistantSnapshotRecord findAssistantSnapshotByVersion(String userId,
                                                                     String orgId,
                                                                     String assistantId,
                                                                     String version) {
            for (AssistantSnapshotRecord snapshot : snapshots) {
                if (userId.equals(snapshot.getUserId())
                        && orgId.equals(snapshot.getOrgId())
                        && assistantId.equals(snapshot.getAssistantId())
                        && version.equals(snapshot.getVersion())) {
                    return snapshot;
                }
            }
            return null;
        }

        @Override
        public boolean updateLatestAssistantSnapshot(String userId,
                                                     String orgId,
                                                     String assistantId,
                                                     String desc,
                                                     long updatedAt) {
            AssistantSnapshotRecord latest = findLatestAssistantSnapshot(userId, orgId, assistantId);
            if (latest == null) {
                return false;
            }
            latest.setDesc(desc);
            latest.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public boolean updateAssistantPublishType(String userId,
                                                  String orgId,
                                                  String assistantId,
                                                  String publishType,
                                                  long updatedAt) {
            AppRecord existing = findAssistant(userId, orgId, assistantId);
            if (existing == null) {
                return false;
            }
            existing.setPublishType(publishType);
            existing.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public boolean rollbackAssistant(AppRecord record, AssistantDraftConfigRecord config) {
            AppRecord existing = findAssistant(record.getUserId(), record.getOrgId(), record.getAppId());
            if (existing == null) {
                return false;
            }
            existing.setUpdatedAt(record.getUpdatedAt());
            existing.setName(record.getName());
            existing.setDesc(record.getDesc());
            existing.setAvatarKey(record.getAvatarKey());
            existing.setAvatarPath(record.getAvatarPath());
            existing.setCategory(record.getCategory());
            saveAssistantConfig(config);
            return true;
        }

        @Override
        public AppRecord saveRag(AppRecord record) {
            record.setId(ids.incrementAndGet());
            records.add(record);
            RagDraftConfigRecord config = new RagDraftConfigRecord();
            config.setCreatedAt(record.getCreatedAt());
            config.setUpdatedAt(record.getUpdatedAt());
            config.setUserId(record.getUserId());
            config.setOrgId(record.getOrgId());
            config.setRagId(record.getAppId());
            ragConfigs.add(config);
            return record;
        }

        @Override
        public AppRecord updateRag(AppRecord record) {
            AppRecord existing = findRag(record.getUserId(), record.getOrgId(), record.getAppId());
            if (existing == null) {
                return null;
            }
            existing.setUpdatedAt(record.getUpdatedAt());
            existing.setName(record.getName());
            existing.setDesc(record.getDesc());
            existing.setAvatarKey(record.getAvatarKey());
            existing.setAvatarPath(record.getAvatarPath());
            existing.setCategory(record.getCategory());
            return existing;
        }

        @Override
        public List<AppRecord> listRags(String userId, String orgId, String name) {
            List<AppRecord> matches = new ArrayList<>();
            for (AppRecord record : records) {
                if (!userId.equals(record.getUserId())
                        || !orgId.equals(record.getOrgId())
                        || !"rag".equals(record.getAppType())) {
                    continue;
                }
                if (name != null && !name.isEmpty() && !record.getName().contains(name)) {
                    continue;
                }
                matches.add(record);
            }
            matches.sort(Comparator.comparing(AppRecord::getId).reversed());
            return matches;
        }

        @Override
        public AppRecord findRag(String userId, String orgId, String ragId) {
            for (AppRecord record : records) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && ragId.equals(record.getAppId())
                        && "rag".equals(record.getAppType())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public boolean deleteRag(String userId, String orgId, String ragId) {
            AppRecord existing = findRag(userId, orgId, ragId);
            if (existing == null) {
                return false;
            }
            records.remove(existing);
            RagDraftConfigRecord config = findRagConfig(userId, orgId, ragId);
            if (config != null) {
                ragConfigs.remove(config);
            }
            List<RagSnapshotRecord> removed = new ArrayList<>();
            for (RagSnapshotRecord snapshot : ragSnapshots) {
                if (ragId.equals(snapshot.getRagId())) {
                    removed.add(snapshot);
                }
            }
            ragSnapshots.removeAll(removed);
            return true;
        }

        @Override
        public List<String> listRagNamesByPrefix(String userId, String orgId, String prefix) {
            List<String> names = new ArrayList<>();
            for (AppRecord record : records) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && "rag".equals(record.getAppType())
                        && record.getName() != null
                        && record.getName().startsWith(prefix)) {
                    names.add(record.getName());
                }
            }
            return names;
        }

        @Override
        public AppRecord copyRag(AppRecord record, RagDraftConfigRecord config) {
            record.setId(ids.incrementAndGet());
            records.add(record);
            if (config != null) {
                ragConfigs.add(config);
            }
            return record;
        }

        @Override
        public RagDraftConfigRecord saveRagConfig(RagDraftConfigRecord record) {
            RagDraftConfigRecord existing = findRagConfig(record.getUserId(), record.getOrgId(), record.getRagId());
            if (existing != null) {
                ragConfigs.remove(existing);
            }
            ragConfigs.add(record);
            AppRecord app = findRag(record.getUserId(), record.getOrgId(), record.getRagId());
            if (app != null) {
                app.setUpdatedAt(record.getUpdatedAt());
            }
            return record;
        }

        @Override
        public RagDraftConfigRecord findRagConfig(String userId, String orgId, String ragId) {
            for (RagDraftConfigRecord config : ragConfigs) {
                if (userId.equals(config.getUserId())
                        && orgId.equals(config.getOrgId())
                        && ragId.equals(config.getRagId())) {
                    return config;
                }
            }
            return null;
        }

        @Override
        public RagSnapshotRecord saveRagSnapshot(RagSnapshotRecord snapshot) {
            snapshot.setId(ids.incrementAndGet());
            ragSnapshots.add(snapshot);
            return snapshot;
        }

        @Override
        public List<RagSnapshotRecord> listRagSnapshots(String userId, String orgId, String ragId) {
            List<RagSnapshotRecord> matches = new ArrayList<>();
            for (RagSnapshotRecord snapshot : ragSnapshots) {
                if (userId.equals(snapshot.getUserId())
                        && orgId.equals(snapshot.getOrgId())
                        && ragId.equals(snapshot.getRagId())) {
                    matches.add(snapshot);
                }
            }
            matches.sort(Comparator.comparing(RagSnapshotRecord::getId).reversed());
            return matches;
        }

        @Override
        public RagSnapshotRecord findLatestRagSnapshot(String userId, String orgId, String ragId) {
            List<RagSnapshotRecord> matches = listRagSnapshots(userId, orgId, ragId);
            return matches.isEmpty() ? null : matches.get(0);
        }

        @Override
        public RagSnapshotRecord findRagSnapshotByVersion(String userId, String orgId, String ragId, String version) {
            for (RagSnapshotRecord snapshot : ragSnapshots) {
                if (userId.equals(snapshot.getUserId())
                        && orgId.equals(snapshot.getOrgId())
                        && ragId.equals(snapshot.getRagId())
                        && version.equals(snapshot.getVersion())) {
                    return snapshot;
                }
            }
            return null;
        }

        @Override
        public boolean updateLatestRagSnapshot(String userId, String orgId, String ragId, String desc, long updatedAt) {
            RagSnapshotRecord latest = findLatestRagSnapshot(userId, orgId, ragId);
            if (latest == null) {
                return false;
            }
            latest.setDesc(desc);
            latest.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public boolean updateRagPublishType(String userId, String orgId, String ragId, String publishType, long updatedAt) {
            AppRecord existing = findRag(userId, orgId, ragId);
            if (existing == null) {
                return false;
            }
            existing.setPublishType(publishType);
            existing.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public boolean rollbackRag(AppRecord record, RagDraftConfigRecord config) {
            AppRecord existing = findRag(record.getUserId(), record.getOrgId(), record.getAppId());
            if (existing == null) {
                return false;
            }
            existing.setUpdatedAt(record.getUpdatedAt());
            existing.setName(record.getName());
            existing.setDesc(record.getDesc());
            existing.setAvatarKey(record.getAvatarKey());
            existing.setAvatarPath(record.getAvatarPath());
            existing.setCategory(record.getCategory());
            saveRagConfig(config);
            return true;
        }

        @Override
        public AppRecord saveWorkflow(AppRecord record, WorkflowDraftRecord draft) {
            record.setId(ids.incrementAndGet());
            records.add(record);
            if (draft != null) {
                draft.setId(ids.incrementAndGet());
                workflowDrafts.add(draft);
            }
            return record;
        }

        @Override
        public List<AppRecord> listWorkflows(String userId, String orgId, String name) {
            return listWorkflows(userId, orgId, name, "workflow");
        }

        @Override
        public List<AppRecord> listWorkflows(String userId, String orgId, String name, String appType) {
            List<AppRecord> matches = new ArrayList<>();
            for (AppRecord record : records) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && appType.equals(record.getAppType())
                        && (name == null || name.isEmpty() || record.getName().contains(name))) {
                    matches.add(record);
                }
            }
            matches.sort(Comparator.comparing(AppRecord::getId).reversed());
            return matches;
        }

        @Override
        public AppRecord findWorkflow(String userId, String orgId, String workflowId) {
            return findWorkflow(userId, orgId, workflowId, "workflow");
        }

        @Override
        public AppRecord findWorkflow(String userId, String orgId, String workflowId, String appType) {
            for (AppRecord record : records) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && workflowId.equals(record.getAppId())
                        && appType.equals(record.getAppType())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public WorkflowDraftRecord findWorkflowDraft(String userId, String orgId, String workflowId) {
            for (WorkflowDraftRecord draft : workflowDrafts) {
                if (userId.equals(draft.getUserId())
                        && orgId.equals(draft.getOrgId())
                        && workflowId.equals(draft.getWorkflowId())) {
                    return draft;
                }
            }
            return null;
        }

        @Override
        public boolean deleteWorkflow(String userId, String orgId, String workflowId) {
            return deleteWorkflow(userId, orgId, workflowId, "workflow");
        }

        @Override
        public boolean deleteWorkflow(String userId, String orgId, String workflowId, String appType) {
            AppRecord existing = findWorkflow(userId, orgId, workflowId, appType);
            if (existing == null) {
                return false;
            }
            records.remove(existing);
            WorkflowDraftRecord draft = findWorkflowDraft(userId, orgId, workflowId);
            if (draft != null) {
                workflowDrafts.remove(draft);
            }
            List<WorkflowSnapshotRecord> removed = new ArrayList<>();
            for (WorkflowSnapshotRecord snapshot : workflowSnapshots) {
                if (userId.equals(snapshot.getUserId())
                        && orgId.equals(snapshot.getOrgId())
                        && workflowId.equals(snapshot.getWorkflowId())) {
                    removed.add(snapshot);
                }
            }
            workflowSnapshots.removeAll(removed);
            return true;
        }

        @Override
        public List<String> listWorkflowNamesByPrefix(String userId, String orgId, String prefix) {
            return listWorkflowNamesByPrefix(userId, orgId, prefix, "workflow");
        }

        @Override
        public List<String> listWorkflowNamesByPrefix(String userId, String orgId, String prefix, String appType) {
            List<String> names = new ArrayList<>();
            for (AppRecord record : records) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && appType.equals(record.getAppType())
                        && record.getName() != null
                        && record.getName().startsWith(prefix)) {
                    names.add(record.getName());
                }
            }
            return names;
        }

        @Override
        public AppRecord copyWorkflow(AppRecord record, WorkflowDraftRecord draft) {
            return saveWorkflow(record, draft);
        }

        @Override
        public WorkflowSnapshotRecord saveWorkflowSnapshot(WorkflowSnapshotRecord snapshot) {
            snapshot.setId(ids.incrementAndGet());
            workflowSnapshots.add(snapshot);
            return snapshot;
        }

        @Override
        public List<WorkflowSnapshotRecord> listWorkflowSnapshots(String userId, String orgId, String workflowId) {
            List<WorkflowSnapshotRecord> matches = new ArrayList<>();
            for (WorkflowSnapshotRecord snapshot : workflowSnapshots) {
                if (userId.equals(snapshot.getUserId())
                        && orgId.equals(snapshot.getOrgId())
                        && workflowId.equals(snapshot.getWorkflowId())) {
                    matches.add(snapshot);
                }
            }
            matches.sort(Comparator.comparing(WorkflowSnapshotRecord::getId).reversed());
            return matches;
        }

        @Override
        public WorkflowSnapshotRecord findLatestWorkflowSnapshot(String userId, String orgId, String workflowId) {
            List<WorkflowSnapshotRecord> matches = listWorkflowSnapshots(userId, orgId, workflowId);
            return matches.isEmpty() ? null : matches.get(0);
        }

        @Override
        public WorkflowSnapshotRecord findWorkflowSnapshotByVersion(String userId, String orgId, String workflowId, String version) {
            for (WorkflowSnapshotRecord snapshot : workflowSnapshots) {
                if (userId.equals(snapshot.getUserId())
                        && orgId.equals(snapshot.getOrgId())
                        && workflowId.equals(snapshot.getWorkflowId())
                        && version.equals(snapshot.getVersion())) {
                    return snapshot;
                }
            }
            return null;
        }

        @Override
        public boolean updateLatestWorkflowSnapshot(String userId, String orgId, String workflowId, String desc, long updatedAt) {
            WorkflowSnapshotRecord latest = findLatestWorkflowSnapshot(userId, orgId, workflowId);
            if (latest == null) {
                return false;
            }
            latest.setDesc(desc);
            latest.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public boolean updateWorkflowPublishType(String userId, String orgId, String workflowId, String publishType, long updatedAt) {
            return updateWorkflowPublishType(userId, orgId, workflowId, "workflow", publishType, updatedAt);
        }

        @Override
        public boolean updateWorkflowPublishType(String userId, String orgId, String workflowId, String appType, String publishType, long updatedAt) {
            AppRecord existing = findWorkflow(userId, orgId, workflowId, appType);
            if (existing == null) {
                return false;
            }
            existing.setPublishType(publishType);
            existing.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public boolean rollbackWorkflow(AppRecord record, WorkflowDraftRecord draft) {
            AppRecord existing = findWorkflow(record.getUserId(), record.getOrgId(), record.getAppId(), record.getAppType());
            if (existing == null) {
                return false;
            }
            existing.setUpdatedAt(record.getUpdatedAt());
            existing.setName(record.getName());
            existing.setDesc(record.getDesc());
            existing.setAvatarKey(record.getAvatarKey());
            existing.setAvatarPath(record.getAvatarPath());
            existing.setCategory(record.getCategory());
            WorkflowDraftRecord existingDraft = findWorkflowDraft(draft.getUserId(), draft.getOrgId(), draft.getWorkflowId());
            if (existingDraft != null) {
                workflowDrafts.remove(existingDraft);
            }
            workflowDrafts.add(draft);
            return true;
        }

        @Override
        public ApiKeyRecord saveApiKey(ApiKeyRecord record) {
            record.setId(ids.incrementAndGet());
            apiKeys.add(record);
            return record;
        }

        @Override
        public ApiKeyRecord updateApiKey(ApiKeyRecord record) {
            ApiKeyRecord existing = findApiKeyById(record.getId());
            if (existing == null) {
                return null;
            }
            existing.setUpdatedAt(record.getUpdatedAt());
            existing.setName(record.getName());
            existing.setDescription(record.getDescription());
            existing.setExpiredAt(record.getExpiredAt());
            existing.setStatus(record.getStatus());
            return existing;
        }

        @Override
        public ApiKeyRecord findApiKeyById(Long id) {
            for (ApiKeyRecord record : apiKeys) {
                if (id.equals(record.getId())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public ApiKeyRecord findApiKeyByKey(String key) {
            for (ApiKeyRecord record : apiKeys) {
                if (key.equals(record.getKey())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public ApiKeyRecord findApiKeyByName(String userId, String orgId, String name) {
            for (ApiKeyRecord record : apiKeys) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && name.equals(record.getName())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public List<ApiKeyRecord> listApiKeys(String userId, String orgId, int offset, int limit) {
            List<ApiKeyRecord> matches = new ArrayList<>();
            for (ApiKeyRecord record : apiKeys) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())) {
                    matches.add(record);
                }
            }
            matches.sort(Comparator.comparing(ApiKeyRecord::getId).reversed());
            return slice(matches, offset, limit);
        }

        @Override
        public long countApiKeys(String userId, String orgId) {
            return listApiKeys(userId, orgId, 0, Integer.MAX_VALUE).size();
        }

        @Override
        public boolean updateApiKeyStatus(Long id, boolean status, long updatedAt) {
            ApiKeyRecord existing = findApiKeyById(id);
            if (existing == null) {
                return false;
            }
            existing.setStatus(status);
            existing.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public boolean deleteApiKey(Long id) {
            ApiKeyRecord existing = findApiKeyById(id);
            if (existing == null) {
                return false;
            }
            apiKeys.remove(existing);
            return true;
        }

        @Override
        public AppKeyRecord saveAppKey(AppKeyRecord record) {
            record.setId(ids.incrementAndGet());
            appKeys.add(record);
            return record;
        }

        @Override
        public List<AppKeyRecord> listAppKeys(String userId, String orgId, String appId, String appType) {
            List<AppKeyRecord> matches = new ArrayList<>();
            for (AppKeyRecord record : appKeys) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && appId.equals(record.getAppId())
                        && appType.equals(record.getAppType())) {
                    matches.add(record);
                }
            }
            matches.sort(Comparator.comparing(AppKeyRecord::getId).reversed());
            return matches;
        }

        @Override
        public AppKeyRecord findAppKeyById(Long id) {
            for (AppKeyRecord record : appKeys) {
                if (id.equals(record.getId())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public AppKeyRecord findAppKeyByKey(String apiKey) {
            for (AppKeyRecord record : appKeys) {
                if (apiKey.equals(record.getApiKey())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public boolean deleteAppKey(Long id) {
            AppKeyRecord existing = findAppKeyById(id);
            if (existing == null) {
                return false;
            }
            appKeys.remove(existing);
            return true;
        }

        @Override
        public AppUrlRecord saveAppUrl(AppUrlRecord record) {
            record.setId(ids.incrementAndGet());
            appUrls.add(record);
            return record;
        }

        @Override
        public AppUrlRecord updateAppUrl(AppUrlRecord record) {
            AppUrlRecord existing = findAppUrlById(record.getUserId(), record.getOrgId(), record.getId());
            if (existing == null) {
                return null;
            }
            existing.setUpdatedAt(record.getUpdatedAt());
            existing.setName(record.getName());
            existing.setDescription(record.getDescription());
            existing.setExpiredAt(record.getExpiredAt());
            existing.setCopyright(record.getCopyright());
            existing.setCopyrightEnable(record.getCopyrightEnable());
            existing.setPrivacyPolicy(record.getPrivacyPolicy());
            existing.setPrivacyPolicyEnable(record.getPrivacyPolicyEnable());
            existing.setDisclaimer(record.getDisclaimer());
            existing.setDisclaimerEnable(record.getDisclaimerEnable());
            return existing;
        }

        @Override
        public AppUrlRecord findAppUrlById(String userId, String orgId, Long id) {
            for (AppUrlRecord record : appUrls) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && id.equals(record.getId())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public AppUrlRecord findAppUrlBySuffix(String suffix) {
            for (AppUrlRecord record : appUrls) {
                if (suffix.equals(record.getSuffix())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public AppUrlRecord findAppUrlByName(String userId, String orgId, String appId, String appType, String name) {
            for (AppUrlRecord record : appUrls) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && appId.equals(record.getAppId())
                        && appType.equals(record.getAppType())
                        && name.equals(record.getName())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public List<AppUrlRecord> listAppUrls(String userId, String orgId, String appId, String appType) {
            List<AppUrlRecord> matches = new ArrayList<>();
            for (AppUrlRecord record : appUrls) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && appId.equals(record.getAppId())
                        && appType.equals(record.getAppType())) {
                    matches.add(record);
                }
            }
            matches.sort(Comparator.comparing(AppUrlRecord::getId).reversed());
            return matches;
        }

        @Override
        public boolean updateAppUrlStatus(String userId, String orgId, Long id, boolean status, long updatedAt) {
            AppUrlRecord existing = findAppUrlById(userId, orgId, id);
            if (existing == null) {
                return false;
            }
            existing.setStatus(status);
            existing.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public boolean deleteAppUrl(String userId, String orgId, Long id) {
            AppUrlRecord existing = findAppUrlById(userId, orgId, id);
            if (existing == null) {
                return false;
            }
            appUrls.remove(existing);
            return true;
        }

        @Override
        public AssistantConversationRecord saveConversation(AssistantConversationRecord record) {
            record.setId(ids.incrementAndGet());
            conversations.add(record);
            return record;
        }

        @Override
        public AssistantConversationRecord findConversation(String userId, String orgId, String conversationId) {
            for (AssistantConversationRecord conversation : conversations) {
                if (userId.equals(conversation.getUserId())
                        && orgId.equals(conversation.getOrgId())
                        && conversationId.equals(conversation.getConversationId())) {
                    return conversation;
                }
            }
            return null;
        }

        @Override
        public AssistantConversationRecord findDraftConversation(String userId, String orgId, String assistantId) {
            for (AssistantConversationRecord conversation : conversations) {
                if (userId.equals(conversation.getUserId())
                        && orgId.equals(conversation.getOrgId())
                        && assistantId.equals(conversation.getAssistantId())
                        && "draft".equals(conversation.getConversationType())) {
                    return conversation;
                }
            }
            return null;
        }

        @Override
        public List<AssistantConversationRecord> listConversations(String userId,
                                                                   String orgId,
                                                                   String assistantId,
                                                                   String conversationType,
                                                                   int offset,
                                                                   int limit) {
            List<AssistantConversationRecord> matches = new ArrayList<>();
            for (AssistantConversationRecord conversation : conversations) {
                if (userId.equals(conversation.getUserId())
                        && orgId.equals(conversation.getOrgId())
                        && assistantId.equals(conversation.getAssistantId())
                        && conversationType.equals(conversation.getConversationType())) {
                    matches.add(conversation);
                }
            }
            matches.sort(Comparator.comparing(AssistantConversationRecord::getId).reversed());
            return slice(matches, offset, limit);
        }

        @Override
        public long countConversations(String userId, String orgId, String assistantId, String conversationType) {
            return listConversations(userId, orgId, assistantId, conversationType, 0, Integer.MAX_VALUE).size();
        }

        @Override
        public boolean touchConversation(String userId, String orgId, String conversationId, long updatedAt) {
            AssistantConversationRecord conversation = findConversation(userId, orgId, conversationId);
            if (conversation == null) {
                return false;
            }
            conversation.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public boolean deleteConversation(String userId, String orgId, String conversationId) {
            AssistantConversationRecord conversation = findConversation(userId, orgId, conversationId);
            if (conversation == null) {
                return false;
            }
            conversations.remove(conversation);
            deleteConversationMessages(userId, orgId, conversationId);
            return true;
        }

        @Override
        public AssistantConversationMessageRecord saveConversationMessage(AssistantConversationMessageRecord record) {
            record.setId(ids.incrementAndGet());
            messages.add(record);
            return record;
        }

        @Override
        public List<AssistantConversationMessageRecord> listConversationMessages(String userId,
                                                                                 String orgId,
                                                                                 String conversationId,
                                                                                 int offset,
                                                                                 int limit) {
            List<AssistantConversationMessageRecord> matches = new ArrayList<>();
            for (AssistantConversationMessageRecord message : messages) {
                if (userId.equals(message.getUserId())
                        && orgId.equals(message.getOrgId())
                        && conversationId.equals(message.getConversationId())) {
                    matches.add(message);
                }
            }
            matches.sort(Comparator.comparing(AssistantConversationMessageRecord::getId));
            return slice(matches, offset, limit);
        }

        @Override
        public long countConversationMessages(String userId, String orgId, String conversationId) {
            return listConversationMessages(userId, orgId, conversationId, 0, Integer.MAX_VALUE).size();
        }

        @Override
        public boolean deleteConversationMessage(String userId, String orgId, String conversationId, String detailId) {
            AssistantConversationMessageRecord found = null;
            for (AssistantConversationMessageRecord message : messages) {
                if (userId.equals(message.getUserId())
                        && orgId.equals(message.getOrgId())
                        && conversationId.equals(message.getConversationId())
                        && detailId.equals(message.getDetailId())) {
                    found = message;
                    break;
                }
            }
            if (found == null) {
                return false;
            }
            messages.remove(found);
            return true;
        }

        @Override
        public boolean deleteConversationMessages(String userId, String orgId, String conversationId) {
            List<AssistantConversationMessageRecord> removed = new ArrayList<>();
            for (AssistantConversationMessageRecord message : messages) {
                if (userId.equals(message.getUserId())
                        && orgId.equals(message.getOrgId())
                        && conversationId.equals(message.getConversationId())) {
                    removed.add(message);
                }
            }
            messages.removeAll(removed);
            return !removed.isEmpty();
        }

        private <T> List<T> slice(List<T> source, int offset, int limit) {
            if (offset >= source.size()) {
                return new ArrayList<>();
            }
            int toIndex = Math.min(source.size(), offset + limit);
            return new ArrayList<>(source.subList(offset, toIndex));
        }

        private boolean containsApp(String assistantId) {
            for (AppRecord record : records) {
                if (assistantId.equals(record.getAppId())) {
                    return true;
                }
            }
            return false;
        }

        private boolean containsConfig(String assistantId) {
            for (AssistantDraftConfigRecord config : configs) {
                if (assistantId.equals(config.getAssistantId())) {
                    return true;
                }
            }
            return false;
        }
    }
}
