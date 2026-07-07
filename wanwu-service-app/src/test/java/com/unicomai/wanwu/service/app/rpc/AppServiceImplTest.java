package com.unicomai.wanwu.service.app.rpc;

import com.sun.net.httpserver.HttpServer;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantActionDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantActionInfoQuery;
import com.unicomai.wanwu.api.app.dto.AssistantActionListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantActionUpsertCommand;
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
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileListResult;
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileUploadCommand;
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileUploadItem;
import com.unicomai.wanwu.api.app.dto.AssistantKnowledgeFileUploadResult;
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
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticListResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticRecordResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppStatisticListResult;
import com.unicomai.wanwu.api.app.dto.AppStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.AppStatisticResult;
import com.unicomai.wanwu.api.app.dto.AppTypeConvertCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationInfoQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationChatCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteByIdCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationMessageListQuery;
import com.unicomai.wanwu.api.app.dto.AppKeyCreateCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyInfo;
import com.unicomai.wanwu.api.app.dto.AppKeyListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.ExplorationAppFavoriteCommand;
import com.unicomai.wanwu.api.app.dto.ExplorationAppHistoryCommand;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConfigQuery;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationListQuery;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationQuery;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationStateCommand;
import com.unicomai.wanwu.api.app.dto.ModelStatisticListResult;
import com.unicomai.wanwu.api.app.dto.ModelStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.ModelStatisticResult;
import com.unicomai.wanwu.api.app.dto.RagConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RagCopyCommand;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RagCreateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateResult;
import com.unicomai.wanwu.api.app.dto.RagDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.RagUpdateCommand;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.api.safety.SafetyService;
import com.unicomai.wanwu.api.app.dto.WorkflowCopyCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateResult;
import com.unicomai.wanwu.api.app.dto.WorkflowDeleteCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowExportQuery;
import com.unicomai.wanwu.api.app.dto.WorkflowExportResult;
import com.unicomai.wanwu.api.app.dto.WorkflowImportCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.api.app.dto.RecordApiKeyStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.service.app.domain.AssistantActionRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationMessageRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationRecord;
import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.AssistantKnowledgeFileRecord;
import com.unicomai.wanwu.service.app.domain.AssistantSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyUsageAggregateRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyUsageRecord;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.AppFavoriteRecord;
import com.unicomai.wanwu.service.app.domain.AppHistoryRecord;
import com.unicomai.wanwu.service.app.domain.AppKeyRecord;
import com.unicomai.wanwu.service.app.domain.AppStatisticAggregateRecord;
import com.unicomai.wanwu.service.app.domain.AppTemplateRecord;
import com.unicomai.wanwu.service.app.domain.AppUrlRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import com.unicomai.wanwu.service.app.domain.GeneralAgentConfigRecord;
import com.unicomai.wanwu.service.app.domain.GeneralAgentConversationRecord;
import com.unicomai.wanwu.service.app.domain.ModelStatisticAggregateRecord;
import com.unicomai.wanwu.service.app.domain.RagChatRecord;
import com.unicomai.wanwu.service.app.domain.RagDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.RagSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowDraftRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowRunRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowSnapshotRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void explorationFavoriteAndHistoryUseRepositoryRecords() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());
        AssistantCreateResult created = service.createAssistant(command("SquareAgent", "square desc"));

        AppPublishCommand publish = new AppPublishCommand();
        publish.setAppId(created.getAssistantId());
        publish.setAppType("agent");
        publish.setUserId("dev-admin");
        publish.setOrgId("default-org");
        publish.setVersion("v1.0.0");
        publish.setPublishType("public");
        service.publishApp(publish);

        ExplorationAppFavoriteCommand favorite = new ExplorationAppFavoriteCommand();
        favorite.setUserId("dev-admin");
        favorite.setOrgId("default-org");
        favorite.setAppId(created.getAssistantId());
        favorite.setAppType("agent");
        favorite.setFavorite(true);
        service.changeExplorationAppFavorite(favorite);

        ApplicationListQuery favoriteQuery = new ApplicationListQuery("agent", "", "dev-admin", "default-org");
        favoriteQuery.setSearchType("favorite");
        ApplicationListResult favorites = service.listApplications(favoriteQuery);
        assertEquals(1, favorites.getTotal());
        assertEquals(created.getAssistantId(), favorites.getList().get(0).get("appId"));
        assertEquals(true, favorites.getList().get(0).get("isFavorite"));

        ExplorationAppHistoryCommand history = new ExplorationAppHistoryCommand();
        history.setUserId("dev-admin");
        history.setOrgId("default-org");
        history.setAppId(created.getAssistantId());
        history.setAppType("agent");
        service.recordAppHistory(history);

        ApplicationListQuery historyQuery = new ApplicationListQuery("agent", "Square", "dev-admin", "default-org");
        historyQuery.setSearchType("history");
        ApplicationListResult histories = service.listApplications(historyQuery);
        assertEquals(1, histories.getTotal());
        assertEquals(created.getAssistantId(), histories.getList().get(0).get("appId"));
        assertEquals("2026-06-29 10:00:00", histories.getList().get(0).get("visitedAt"));

        favorite.setFavorite(false);
        service.changeExplorationAppFavorite(favorite);
        ApplicationListResult removed = service.listApplications(favoriteQuery);
        assertEquals(0, removed.getTotal());
    }

    @Test
    public void appTemplatesUseRepositoryRecords() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        AppTemplateRecord assistant = new AppTemplateRecord();
        assistant.setCreatedAt(fixedClock().millis());
        assistant.setUpdatedAt(fixedClock().millis());
        assistant.setTemplateType("assistant");
        assistant.setTemplateId("assistant-template-custom");
        assistant.setCategory("industry");
        assistant.setName("Custom Support");
        assistant.setDesc("Handle support tickets.");
        assistant.setAvatarJson("{\"key\":\"avatar-key\",\"path\":\"/avatar.png\"}");
        assistant.setSummary("Support summary");
        assistant.setFeature("Routing and reply drafts.");
        assistant.setScenario("Support teams.");
        assistant.setPrologue("Describe the ticket.");
        assistant.setInstructions("You are a support assistant.");
        assistant.setRecommendQuestionsJson("[\"How urgent is it?\"]");
        assistant.setWorkflowInstruction("Bind workflow later.");
        repository.saveAppTemplate(assistant);

        AppTemplateRecord workflow = new AppTemplateRecord();
        workflow.setCreatedAt(fixedClock().millis());
        workflow.setUpdatedAt(fixedClock().millis());
        workflow.setTemplateType("workflow");
        workflow.setTemplateId("workflow-template-custom");
        workflow.setCategory("office");
        workflow.setName("Custom Workflow");
        workflow.setDesc("Run a custom workflow.");
        workflow.setAvatarJson("{\"key\":\"\",\"path\":\"\"}");
        workflow.setAuthor("Wanwu Java");
        workflow.setDownloadCount(7);
        workflow.setSummary("Workflow summary");
        workflow.setFeature("Schema-backed draft.");
        workflow.setScenario("Office automation.");
        workflow.setNote("Local runtime shell.");
        workflow.setSchemaJson("{\"id\":\"workflow-template-custom\",\"nodes\":[],\"edges\":[]}");
        repository.saveAppTemplate(workflow);

        List<Map<String, Object>> assistants = service.listAppTemplates("agentTemplate", "industry", "Support");
        assertEquals(1, assistants.size());
        assertEquals("assistant-template-custom", assistants.get(0).get("assistantTemplateId"));
        assertEquals("agentTemplate", assistants.get(0).get("appType"));
        assertEquals("Describe the ticket.", assistants.get(0).get("prologue"));
        assertEquals("How urgent is it?", ((List<?>) assistants.get(0).get("recommendQuestion")).get(0));
        assertEquals("/avatar.png", ((Map<?, ?>) assistants.get(0).get("avatar")).get("path"));

        Map<String, Object> workflowDetail = service.getAppTemplate("workflow", "workflow-template-custom");
        assertEquals("workflow-template-custom", workflowDetail.get("templateId"));
        assertEquals(7, workflowDetail.get("downloadCount"));
        assertEquals("workflow-template-custom", ((Map<?, ?>) workflowDetail.get("schema")).get("id"));

        service.recordAppTemplateDownload("workflow", "workflow-template-custom");
        Map<String, Object> downloaded = service.getAppTemplate("workflow", "workflow-template-custom");
        assertEquals(8, downloaded.get("downloadCount"));
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
    public void ragChatPersistsConfiguredModelUpstreamResponse() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        RagCreateCommand create = new RagCreateCommand();
        create.setName("ModelRag");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        RagCreateResult created = service.createRag(create);

        RagChatCommand command = ragChatCommand(created.getRagId(), "what is policy", true);
        command.setOverrideResponse("rag model upstream answer");
        RagChatResult result = service.streamRagChat(command);

        assertEquals("rag model upstream answer", result.getResponse());
        assertEquals("rag model upstream answer", repository.ragChats.get(0).getResponse());
    }

    @Test
    public void ragChatUsesConfiguredSafetyTableToBlockSensitiveQuestion() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock(), null,
                new FakeSafetyService("table-001", "banned-token", "Safety reply"));

        RagCreateCommand create = new RagCreateCommand();
        create.setName("PolicyRag");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        RagCreateResult created = service.createRag(create);

        RagConfigUpdateCommand config = new RagConfigUpdateCommand();
        config.setRagId(created.getRagId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setSafetyConfig(safetyConfig("table-001"));
        service.updateRagConfig(config);

        RagChatResult result = service.streamRagChat(
                ragChatCommand(created.getRagId(), "question with banned-token", true));

        assertEquals("Safety reply", result.getResponse());
        assertTrue(result.getSearchList().isEmpty());
        assertTrue(result.getQaSearchList().isEmpty());
    }

    @Test
    public void ragChatReplacesSensitiveGeneratedOutputWithTableReply() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock(), null,
                new FakeSafetyService("table-001", "GuardedRag", "Safety reply"));

        RagCreateCommand create = new RagCreateCommand();
        create.setName("GuardedRag");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        RagCreateResult created = service.createRag(create);

        RagConfigUpdateCommand config = new RagConfigUpdateCommand();
        config.setRagId(created.getRagId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setSafetyConfig(safetyConfig("table-001"));
        service.updateRagConfig(config);

        RagChatResult result = service.streamRagChat(
                ragChatCommand(created.getRagId(), "plain question", true));

        assertEquals("Safety reply", result.getResponse());
        assertTrue(result.getSearchList().isEmpty());
        assertTrue(result.getQaSearchList().isEmpty());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void ragChatReturnsConfiguredKnowledgeHits() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock(), knowledgeService);

        RagCreateCommand create = new RagCreateCommand();
        create.setName("PolicyRag");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        RagCreateResult created = service.createRag(create);

        RagConfigUpdateCommand config = new RagConfigUpdateCommand();
        config.setRagId(created.getRagId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setKnowledgeBaseConfig(knowledgeConfig("kb-001"));
        service.updateRagConfig(config);

        Map<String, Object> hitItem = new LinkedHashMap<>();
        hitItem.put("title", "PolicyGuide.txt");
        hitItem.put("knowledgeName", "Policy KB");
        hitItem.put("snippet", "Policy answer comes from the configured knowledge base.");
        Map<String, Object> rerankScore = new LinkedHashMap<>();
        rerankScore.put("score", 0.87D);
        Map<String, Object> rerankOnlyItem = new LinkedHashMap<>();
        rerankOnlyItem.put("title", "PolicyFaq.txt");
        rerankOnlyItem.put("QABase", "Policy QA");
        rerankOnlyItem.put("snippet", "A second hit keeps score compatibility.");
        rerankOnlyItem.put("rerank_info", Collections.singletonList(rerankScore));
        Map<String, Object> hit = new LinkedHashMap<>();
        hit.put("searchList", Arrays.asList(hitItem, rerankOnlyItem));
        hit.put("score", Collections.singletonList(1.0D));
        hit.put("prompt", "Policy answer comes from the configured knowledge base.");
        when(knowledgeService.hitKnowledge(eq("dev-admin"), eq("default-org"), any(Map.class))).thenReturn(hit);

        RagChatCommand chat = ragChatCommand(created.getRagId(), "what is policy", true);
        Map<String, Object> history = new LinkedHashMap<>();
        history.put("query", "prev");
        history.put("response", "answer");
        chat.setHistory(Collections.singletonList(history));
        Map<String, Object> fileInfo = new LinkedHashMap<>();
        fileInfo.put("fileName", "note.txt");
        chat.setFileInfo(Collections.singletonList(fileInfo));
        RagChatResult result = service.streamRagChat(chat);

        assertEquals(2, result.getSearchList().size());
        assertEquals("PolicyGuide.txt", result.getSearchList().get(0).get("title"));
        assertEquals(1.0D, result.getSearchList().get(0).get("score"));
        assertEquals("Policy KB", result.getSearchList().get(0).get("kb_name"));
        assertEquals("Policy KB", result.getSearchList().get(0).get("user_kb_name"));
        assertEquals(0.87D, result.getSearchList().get(1).get("score"));
        assertEquals("Policy QA", result.getSearchList().get(1).get("user_kb_name"));
        assertTrue(result.getResponse().contains("Policy answer comes from the configured knowledge base."));
        List<RagChatRecord> chats = repository.listRagChats("dev-admin", "default-org", created.getRagId(), 10);
        assertEquals(1, chats.size());
        assertTrue(chats.get(0).getChatId().startsWith("rag-chat-"));
        assertEquals(Boolean.TRUE, chats.get(0).getDraft());
        assertEquals("what is policy", chats.get(0).getQuestion());
        assertTrue(chats.get(0).getResponse().contains("Policy answer comes from the configured knowledge base."));
        assertTrue(chats.get(0).getHistoryJson().contains("prev"));
        assertTrue(chats.get(0).getFileInfoJson().contains("note.txt"));
        assertTrue(chats.get(0).getSearchListJson().contains("PolicyGuide.txt"));

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(knowledgeService).hitKnowledge(eq("dev-admin"), eq("default-org"), captor.capture());
        Map<String, Object> request = captor.getValue();
        assertEquals("what is policy", request.get("question"));
        List<Map<String, Object>> knowledgeList = (List<Map<String, Object>>) request.get("knowledgeList");
        assertEquals("kb-001", knowledgeList.get(0).get("knowledgeId"));
        assertEquals(5, ((Map<String, Object>) request.get("knowledgeMatchParams")).get("topK"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ragChatAcceptsGoProtoKnowledgeAndQaConfigShapes() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock(), knowledgeService);

        RagCreateCommand create = new RagCreateCommand();
        create.setName("GoShapeRag");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        RagCreateResult created = service.createRag(create);

        RagConfigUpdateCommand config = new RagConfigUpdateCommand();
        config.setRagId(created.getRagId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setKnowledgeBaseConfig(goProtoKnowledgeConfig("kb-go-001", 3));
        config.setQaKnowledgeBaseConfig(goProtoQaKnowledgeConfig("qa-go-001", 2));
        service.updateRagConfig(config);

        Map<String, Object> hitItem = new LinkedHashMap<>();
        hitItem.put("title", "GoKnowledge.txt");
        hitItem.put("knowledgeName", "Go KB");
        hitItem.put("content", "Go-shaped knowledge config should be searchable.");
        Map<String, Object> hit = new LinkedHashMap<>();
        hit.put("searchList", Collections.singletonList(hitItem));
        hit.put("prompt", "Go-shaped knowledge config should be searchable.");
        when(knowledgeService.hitKnowledge(eq("dev-admin"), eq("default-org"), any(Map.class))).thenReturn(hit);

        Map<String, Object> qaItem = new LinkedHashMap<>();
        qaItem.put("qaPairId", "qa-pair-go-001");
        qaItem.put("QABase", "Go QA");
        qaItem.put("answer", "Go-shaped QA config should be searchable.");
        Map<String, Object> qaHit = new LinkedHashMap<>();
        qaHit.put("searchList", Collections.singletonList(qaItem));
        qaHit.put("prompt", "Go-shaped QA config should be searchable.");
        when(knowledgeService.hitQaPairs(eq("dev-admin"), eq("default-org"), any(Map.class))).thenReturn(qaHit);

        RagChatResult result = service.streamRagChat(ragChatCommand(created.getRagId(), "go shape question", true));

        assertEquals(1, result.getSearchList().size());
        assertEquals("GoKnowledge.txt", result.getSearchList().get(0).get("title"));
        assertEquals(1, result.getQaSearchList().size());
        assertEquals("qa-pair-go-001", result.getQaSearchList().get(0).get("qaPairId"));
        assertTrue(result.getResponse().contains("Go-shaped knowledge config should be searchable."));

        ArgumentCaptor<Map> knowledgeCaptor = ArgumentCaptor.forClass(Map.class);
        verify(knowledgeService).hitKnowledge(eq("dev-admin"), eq("default-org"), knowledgeCaptor.capture());
        Map<String, Object> knowledgeRequest = knowledgeCaptor.getValue();
        List<Map<String, Object>> knowledgeList = (List<Map<String, Object>>) knowledgeRequest.get("knowledgeList");
        assertEquals("kb-go-001", knowledgeList.get(0).get("knowledgeId"));
        assertEquals(1, knowledgeList.get(0).get("graphSwitch"));
        assertEquals(Boolean.TRUE, ((Map<String, Object>) knowledgeList.get(0).get("metaDataFilterParams")).get("filterEnable"));
        assertEquals(3, ((Map<String, Object>) knowledgeRequest.get("knowledgeMatchParams")).get("topK"));
        assertEquals("mix", ((Map<String, Object>) knowledgeRequest.get("knowledgeMatchParams")).get("matchType"));

        ArgumentCaptor<Map> qaCaptor = ArgumentCaptor.forClass(Map.class);
        verify(knowledgeService).hitQaPairs(eq("dev-admin"), eq("default-org"), qaCaptor.capture());
        Map<String, Object> qaRequest = qaCaptor.getValue();
        List<Map<String, Object>> qaKnowledgeList = (List<Map<String, Object>>) qaRequest.get("knowledgeList");
        assertEquals("qa-go-001", qaKnowledgeList.get(0).get("knowledgeId"));
        assertEquals(Boolean.TRUE, ((Map<String, Object>) qaKnowledgeList.get(0).get("metaDataFilterParams")).get("filterEnable"));
        assertEquals(2, ((Map<String, Object>) qaRequest.get("knowledgeMatchParams")).get("topK"));
        assertEquals("text", ((Map<String, Object>) qaRequest.get("knowledgeMatchParams")).get("matchType"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void ragChatPassesConfiguredRerankModelsToKnowledgeHits() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock(), knowledgeService);

        RagCreateCommand create = new RagCreateCommand();
        create.setName("RerankRag");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        RagCreateResult created = service.createRag(create);

        Map<String, Object> knowledgeConfig = goProtoKnowledgeConfig("kb-rerank-001", 4);
        ((Map<String, Object>) knowledgeConfig.get("globalConfig")).put("priorityMatch", 0);
        Map<String, Object> qaKnowledgeConfig = goProtoQaKnowledgeConfig("qa-rerank-001", 2);
        ((Map<String, Object>) qaKnowledgeConfig.get("globalConfig")).put("priorityMatch", 0);

        RagConfigUpdateCommand config = new RagConfigUpdateCommand();
        config.setRagId(created.getRagId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setKnowledgeBaseConfig(knowledgeConfig);
        config.setQaKnowledgeBaseConfig(qaKnowledgeConfig);
        config.setRerankConfig(modelConfig("rerank-knowledge-001"));
        config.setQaRerankConfig(modelConfig("rerank-qa-001"));
        service.updateRagConfig(config);

        when(knowledgeService.hitKnowledge(eq("dev-admin"), eq("default-org"), any(Map.class)))
                .thenReturn(Collections.<String, Object>emptyMap());
        when(knowledgeService.hitQaPairs(eq("dev-admin"), eq("default-org"), any(Map.class)))
                .thenReturn(Collections.<String, Object>emptyMap());

        service.streamRagChat(ragChatCommand(created.getRagId(), "needs rerank", true));

        ArgumentCaptor<Map> knowledgeCaptor = ArgumentCaptor.forClass(Map.class);
        verify(knowledgeService).hitKnowledge(eq("dev-admin"), eq("default-org"), knowledgeCaptor.capture());
        Map<String, Object> knowledgeMatchParams =
                (Map<String, Object>) knowledgeCaptor.getValue().get("knowledgeMatchParams");
        assertEquals("rerank-knowledge-001", knowledgeMatchParams.get("rerankModelId"));
        assertEquals(0, knowledgeMatchParams.get("priorityMatch"));

        ArgumentCaptor<Map> qaCaptor = ArgumentCaptor.forClass(Map.class);
        verify(knowledgeService).hitQaPairs(eq("dev-admin"), eq("default-org"), qaCaptor.capture());
        Map<String, Object> qaMatchParams =
                (Map<String, Object>) qaCaptor.getValue().get("knowledgeMatchParams");
        assertEquals("rerank-qa-001", qaMatchParams.get("rerankModelId"));
        assertEquals(0, qaMatchParams.get("priorityMatch"));
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
        create.setSchema("{\"parameters\":[{\"name\":\"question\",\"type\":\"string\"}],"
                + "\"outputs\":[{\"name\":\"summary\",\"type\":\"string\"}]}");
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
        assertTrue(draftExport.getSchema().contains("\"summary\""));

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
        assertTrue(runResult.getRunId().startsWith("workflow-run-"));
        assertEquals("success", runResult.getStatus());
        assertEquals("hello", runResult.getOutput().get("question"));
        assertEquals("PolicyFlow generated summary for {question=hello}", runResult.getOutput().get("summary"));
        assertEquals(1, repository.listWorkflowRuns("dev-admin", "default-org", created.getWorkflowId(), 10).size());
        assertEquals(runResult.getRunId(), repository.listWorkflowRuns("dev-admin", "default-org", created.getWorkflowId(), 10).get(0).getRunId());

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
    public void workflowRunIncludesDeterministicNodeTraceForSchemaNodes() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("NodeFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"type\":\"start\",\"data\":{\"label\":\"Start\"}},"
                + "{\"id\":\"llm\",\"type\":\"llm\",\"data\":{\"label\":\"Summarize\"}}"
                + "],"
                + "\"edges\":[{\"source\":\"start\",\"target\":\"llm\"}],"
                + "\"outputs\":[{\"name\":\"summary\",\"type\":\"string\"},{\"name\":\"llm\",\"type\":\"object\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.singletonMap("question", "hello"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        List<Map<String, Object>> steps = castList(output.get("steps"));
        assertEquals(2, steps.size());
        assertEquals("start", steps.get(0).get("nodeId"));
        assertEquals("llm", steps.get(1).get("nodeId"));
        assertEquals(2, castMap(output.get("trace")).get("nodeCount"));
        assertEquals(1, castList(output.get("edges")).size());
        assertTrue(String.valueOf(output.get("summary")).contains("NodeFlow executed Summarize"));
        assertEquals("llm", castMap(output.get("llm")).get("nodeId"));
    }

    @Test
    public void workflowRunUsesLatestPublishedSnapshotWhenDraftChanges() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("PublishedSnapshotFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"}}},"
                + "{\"id\":\"llm\",\"type\":\"3\",\"data\":{\"nodeMeta\":{\"title\":\"Published\"},"
                + "\"outputs\":[{\"name\":\"answer\",\"template\":\"published {{question}}\"}]}}"
                + "],"
                + "\"edges\":[{\"sourceNodeID\":\"start\",\"targetNodeID\":\"llm\"}],"
                + "\"outputs\":[{\"name\":\"answer\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        AppPublishCommand publish = new AppPublishCommand();
        publish.setAppId(created.getWorkflowId());
        publish.setAppType("workflow");
        publish.setUserId("dev-admin");
        publish.setOrgId("default-org");
        publish.setVersion("v1.0.0");
        publish.setPublishType("public");
        service.publishApp(publish);

        WorkflowDraftRecord draft = repository.findWorkflowDraft("dev-admin", "default-org", created.getWorkflowId());
        draft.setSchemaJson("{"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"}}},"
                + "{\"id\":\"llm\",\"type\":\"3\",\"data\":{\"nodeMeta\":{\"title\":\"Draft\"},"
                + "\"outputs\":[{\"name\":\"answer\",\"template\":\"draft {{question}}\"}]}}"
                + "],"
                + "\"edges\":[{\"sourceNodeID\":\"start\",\"targetNodeID\":\"llm\"}],"
                + "\"outputs\":[{\"name\":\"answer\",\"type\":\"string\"}]"
                + "}");

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.singletonMap("question", "hello"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("published hello", output.get("answer"));
    }

    @Test
    public void workflowRunMapsDeclaredNodeOutputsFromSchemaTemplates() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("TemplateNodeFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"type\":\"start\",\"data\":{\"label\":\"Start\"}},"
                + "{\"id\":\"llm\",\"type\":\"llm\",\"data\":{\"label\":\"Draft\","
                + "\"outputs\":["
                + "{\"name\":\"summary\",\"template\":\"Summary for {{question}}\"},"
                + "{\"name\":\"answer\",\"source\":\"question\"},"
                + "{\"name\":\"score\",\"value\":0.91}"
                + "]}}"
                + "],"
                + "\"edges\":[{\"source\":\"start\",\"target\":\"llm\"}],"
                + "\"outputs\":["
                + "{\"name\":\"summary\",\"type\":\"string\"},"
                + "{\"name\":\"answer\",\"type\":\"string\"},"
                + "{\"name\":\"score\",\"type\":\"number\"}"
                + "]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.singletonMap("question", "hello"));

        WorkflowRunResult runResult = service.runWorkflow(run);
        Map<String, Object> output = runResult.getOutput();

        assertEquals("Summary for hello", output.get("summary"));
        assertEquals("hello", output.get("answer"));
        assertEquals(0.91d, output.get("score"));
        Map<String, Object> nodeOutputs = castMap(output.get("nodeOutputs"));
        Map<String, Object> llmOutput = castMap(nodeOutputs.get("llm"));
        assertEquals("Summary for hello", llmOutput.get("summary"));
        assertEquals("hello", llmOutput.get("answer"));
        assertEquals(0.91d, llmOutput.get("score"));

        Map<String, Object> process = service.getWorkflowRunProcess("dev-admin", "default-org",
                created.getWorkflowId(), runResult.getRunId());
        assertEquals(2, process.get("executeStatus"));
        assertEquals(runResult.getRunId(), process.get("executeId"));
        List<Map<String, Object>> nodeResults = castList(process.get("nodeResults"));
        assertEquals("llm", nodeResults.get(1).get("nodeId"));
        assertTrue(String.valueOf(nodeResults.get(1).get("output")).contains("Summary for hello"));
        assertEquals("900001", nodeResults.get(nodeResults.size() - 1).get("nodeId"));
    }

    @Test
    public void workflowRunExecutesVariableMergeNodeFromGoTemplateSchema() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("VariableMergeFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"}}},"
                + "{\"id\":\"emptyCity\",\"type\":\"3\",\"data\":{\"nodeMeta\":{\"title\":\"Empty\"},"
                + "\"outputs\":[{\"name\":\"city\",\"value\":\"\"}]}},"
                + "{\"id\":\"fallbackCity\",\"type\":\"3\",\"data\":{\"nodeMeta\":{\"title\":\"Fallback\"},"
                + "\"outputs\":[{\"name\":\"city\",\"value\":\"Beijing\"}]}},"
                + "{\"id\":\"merge\",\"type\":\"32\",\"data\":{\"nodeMeta\":{\"title\":\"Variable Merge\",\"subTitle\":\"Variable Merge\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"Group1\"}],"
                + "\"inputs\":{\"inputParameters\":null,\"mergeGroups\":[{\"name\":\"Group1\",\"variables\":["
                + "{\"type\":\"string\",\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"emptyCity\",\"name\":\"city\"}}},"
                + "{\"type\":\"string\",\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"fallbackCity\",\"name\":\"city\"}}}"
                + "]}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"start\",\"targetNodeID\":\"emptyCity\"},"
                + "{\"sourceNodeID\":\"start\",\"targetNodeID\":\"fallbackCity\"},"
                + "{\"sourceNodeID\":\"emptyCity\",\"targetNodeID\":\"merge\"},"
                + "{\"sourceNodeID\":\"fallbackCity\",\"targetNodeID\":\"merge\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"Group1\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("Beijing", output.get("Group1"));
    }

    @Test
    public void workflowRunBuildsStructuredLlmObjectOutputsFromSchema() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("StructuredLlmObjectFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"input\"}]}},"
                + "{\"id\":\"111424\",\"type\":\"3\",\"data\":{\"nodeMeta\":{\"title\":\"Extract Parameters\"},"
                + "\"outputs\":[{\"type\":\"object\",\"name\":\"output\",\"schema\":["
                + "{\"type\":\"string\",\"name\":\"city\"},"
                + "{\"type\":\"string\",\"name\":\"place\"}"
                + "]}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"input\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}}]}}},"
                + "{\"id\":\"122024\",\"type\":\"15\",\"data\":{\"nodeMeta\":{\"title\":\"Join\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"String1\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"111424\",\"name\":\"output.place\"}}}}],"
                + "\"concatParams\":[{\"name\":\"concatResult\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"Place {{String1}}\"}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"122024\",\"name\":\"output\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"111424\"},"
                + "{\"sourceNodeID\":\"111424\",\"targetNodeID\":\"122024\"},"
                + "{\"sourceNodeID\":\"122024\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("input", "Shanghai Tower"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("Place Shanghai Tower", output.get("output"));
        Map<String, Object> llmOutput = castMap(castMap(output.get("nodeOutputs")).get("111424"));
        Map<String, Object> structured = castMap(llmOutput.get("output"));
        assertEquals("Shanghai Tower", structured.get("place"));
        assertEquals("Shanghai Tower", structured.get("city"));
    }

    @Test
    public void workflowRunBuildsLlmListOutputsFromSchema() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("StructuredLlmListFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"topic\"}]}},"
                + "{\"id\":\"138600\",\"type\":\"3\",\"data\":{\"nodeMeta\":{\"title\":\"Generate Titles\"},"
                + "\"outputs\":[{\"type\":\"list\",\"name\":\"output\",\"schema\":{\"type\":\"string\"}}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"topic\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"topic\"}}}}],"
                + "\"llmParam\":[{\"name\":\"prompt\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"Ideas for {{topic}}\"}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"list\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"138600\",\"name\":\"output\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"138600\"},"
                + "{\"sourceNodeID\":\"138600\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"list\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("topic", "new product"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals(Collections.singletonList("Ideas for new product"), output.get("output"));
        Map<String, Object> llmOutput = castMap(castMap(output.get("nodeOutputs")).get("138600"));
        assertEquals(Collections.singletonList("Ideas for new product"), llmOutput.get("output"));
    }

    @Test
    public void workflowRunPropagatesNodeOutputsToLaterNodes() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("ChainedNodeFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"type\":\"start\",\"data\":{\"label\":\"Start\"}},"
                + "{\"id\":\"draft\",\"type\":\"llm\",\"data\":{\"label\":\"Draft\","
                + "\"outputs\":[{\"name\":\"summary\",\"template\":\"Draft {{question}}\"}]}},"
                + "{\"id\":\"final\",\"type\":\"llm\",\"data\":{\"label\":\"Final\","
                + "\"outputs\":[{\"name\":\"finalText\",\"template\":\"Final {{summary}}\"}]}}"
                + "],"
                + "\"edges\":["
                + "{\"source\":\"start\",\"target\":\"draft\"},"
                + "{\"source\":\"draft\",\"target\":\"final\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"finalText\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.singletonMap("question", "hello"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("Final Draft hello", output.get("finalText"));
        List<Map<String, Object>> steps = castList(output.get("steps"));
        assertEquals("Draft hello", castMap(steps.get(2).get("input")).get("summary"));
        Map<String, Object> nodeOutputs = castMap(output.get("nodeOutputs"));
        assertEquals("Final Draft hello", castMap(nodeOutputs.get("final")).get("finalText"));
    }

    @Test
    public void workflowRunExecutesJsonSerializeAndDeserializeNodes() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("JsonNodeFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"object\",\"name\":\"profile\"}]}},"
                + "{\"id\":\"110001\",\"type\":\"json_serialize\",\"data\":{\"nodeMeta\":{\"title\":\"To JSON\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"input\",\"input\":{\"type\":\"object\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"profile\"}}}}]}}},"
                + "{\"id\":\"120001\",\"type\":\"1059\",\"data\":{\"nodeMeta\":{\"title\":\"From JSON\"},"
                + "\"outputs\":[{\"type\":\"object\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"input\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"110001\",\"name\":\"output\"}}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"object\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"120001\",\"name\":\"output\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"110001\"},"
                + "{\"sourceNodeID\":\"110001\",\"targetNodeID\":\"120001\"},"
                + "{\"sourceNodeID\":\"120001\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"object\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", "Alice");
        profile.put("score", 98);
        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("profile", profile));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        Map<String, Object> nodeOutputs = castMap(output.get("nodeOutputs"));
        String json = String.valueOf(castMap(nodeOutputs.get("110001")).get("output"));
        assertTrue(json.contains("\"name\":\"Alice\""), json);
        Map<String, Object> parsed = castMap(castMap(nodeOutputs.get("120001")).get("output"));
        assertEquals("Alice", parsed.get("name"));
        assertEquals(98, parsed.get("score"));
        assertEquals(parsed, output.get("output"));
    }

    @Test
    public void workflowRunExecutesHttpRequestNode() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        List<String> captured = new ArrayList<>();
        server.createContext("/lookup/beijing", exchange -> {
            captured.add(exchange.getRequestURI().getRawQuery());
            captured.add(exchange.getRequestHeaders().getFirst("X-City"));
            byte[] body = "{\"ok\":true,\"city\":\"beijing\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("X-Upstream", "ok");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
            AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

            WorkflowCreateCommand create = new WorkflowCreateCommand();
            create.setName("HttpNodeFlow");
            create.setSchema("{"
                    + "\"nodes\":["
                    + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                    + "\"outputs\":[{\"type\":\"string\",\"name\":\"city\"}]}},"
                    + "{\"id\":\"110001\",\"type\":\"45\",\"data\":{\"nodeMeta\":{\"title\":\"Fetch\",\"subTitle\":\"HTTP 请求\"},"
                    + "\"outputs\":[{\"type\":\"string\",\"name\":\"body\"},{\"type\":\"integer\",\"name\":\"statusCode\"},{\"type\":\"string\",\"name\":\"headers\"}],"
                    + "\"inputs\":{\"apiInfo\":{\"method\":\"GET\",\"url\":\"http://127.0.0.1:"
                    + server.getAddress().getPort() + "/lookup/{{city}}\"},"
                    + "\"params\":[{\"name\":\"key\",\"input\":{\"type\":\"string\",\"value\":{\"type\":\"literal\",\"content\":\"dev-key\"}}},"
                    + "{\"name\":\"q\",\"input\":{\"type\":\"string\",\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"city\"}}}}],"
                    + "\"headers\":[{\"name\":\"X-City\",\"input\":{\"type\":\"string\",\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"city\"}}}}],"
                    + "\"setting\":{\"timeout\":5}}}},"
                    + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                    + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                    + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"110001\",\"name\":\"body\"}}}}]}}}"
                    + "],"
                    + "\"edges\":["
                    + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"110001\"},"
                    + "{\"sourceNodeID\":\"110001\",\"targetNodeID\":\"900001\"}"
                    + "],"
                    + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                    + "}");
            create.setUserId("dev-admin");
            create.setOrgId("default-org");
            WorkflowCreateResult created = service.createWorkflow(create);

            WorkflowRunCommand run = new WorkflowRunCommand();
            run.setWorkflowId(created.getWorkflowId());
            run.setUserId("dev-admin");
            run.setOrgId("default-org");
            run.setInput(Collections.<String, Object>singletonMap("city", "beijing"));

            Map<String, Object> output = service.runWorkflow(run).getOutput();

            assertEquals("{\"ok\":true,\"city\":\"beijing\"}", output.get("output"));
            assertEquals(2, captured.size());
            assertTrue(captured.get(0).contains("key=dev-key"), captured.get(0));
            assertTrue(captured.get(0).contains("q=beijing"), captured.get(0));
            assertEquals("beijing", captured.get(1));
            Map<String, Object> nodeOutputs = castMap(output.get("nodeOutputs"));
            Map<String, Object> httpOutput = castMap(nodeOutputs.get("110001"));
            assertEquals(200, httpOutput.get("statusCode"));
            assertTrue(String.valueOf(httpOutput.get("body")).contains("\"city\":\"beijing\""));
            assertTrue(String.valueOf(httpOutput.get("headers")).toLowerCase().contains("x-upstream"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void workflowRunKeepsGoingWhenHttpRequestFails() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("HttpFallbackFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"city\"}]}},"
                + "{\"id\":\"110001\",\"type\":\"45\",\"data\":{\"nodeMeta\":{\"title\":\"Fetch\",\"subTitle\":\"HTTP Request\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"body\"},{\"type\":\"integer\",\"name\":\"statusCode\"}],"
                + "\"inputs\":{\"apiInfo\":{\"method\":\"GET\",\"url\":\"http://127.0.0.1:1/weather/{{city}}\"},"
                + "\"setting\":{\"timeout\":1}}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"110001\",\"name\":\"body\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"110001\"},"
                + "{\"sourceNodeID\":\"110001\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("city", "beijing"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertTrue(String.valueOf(output.get("output")).contains("workflow http request failed"));
        Map<String, Object> httpOutput = castMap(castMap(output.get("nodeOutputs")).get("110001"));
        assertEquals(599, httpOutput.get("statusCode"));
        assertTrue(String.valueOf(httpOutput.get("body")).contains("http://127.0.0.1:1/weather/beijing"));
    }

    @Test
    public void workflowRunExecutesJsonToCsvCodeNode() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("CodeNodeFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"input\"}]}},"
                + "{\"id\":\"122185\",\"type\":\"5\",\"data\":{\"nodeMeta\":{\"title\":\"转换格式\",\"subTitle\":\"代码\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"csv\"},{\"type\":\"string\",\"name\":\"header\"},{\"type\":\"integer\",\"name\":\"rows\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"input\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}}],"
                + "\"code\":\"import json\\nimport csv\\ndata = json.loads(input)\"}}},"
                + "{\"id\":\"122024\",\"type\":\"15\",\"data\":{\"nodeMeta\":{\"title\":\"添加表头\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"String1\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"122185\",\"name\":\"header\"}}}},"
                + "{\"name\":\"String2\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"122185\",\"name\":\"csv\"}}}}],"
                + "\"concatParams\":[{\"name\":\"concatResult\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"{{String1}}\\n{{String2}}\"}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"122024\",\"name\":\"output\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"122185\"},"
                + "{\"sourceNodeID\":\"122185\",\"targetNodeID\":\"122024\"},"
                + "{\"sourceNodeID\":\"122024\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("input",
                "[{\"name\":\"Alice\",\"score\":98},{\"name\":\"Bob\",\"score\":88}]"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        Map<String, Object> nodeOutputs = castMap(output.get("nodeOutputs"));
        Map<String, Object> codeOutput = castMap(nodeOutputs.get("122185"));
        assertEquals("name,score", codeOutput.get("header"));
        assertEquals(2, codeOutput.get("rows"));
        assertTrue(String.valueOf(codeOutput.get("csv")).contains("Alice,98"));
        assertTrue(String.valueOf(codeOutput.get("csv")).contains("Bob,88"));
        assertTrue(String.valueOf(output.get("output")).startsWith("name,score\nAlice,98"));
    }

    @Test
    public void workflowRunExecutesLogLevelCodeNode() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("LogCodeFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"input\"}]}},"
                + "{\"id\":\"138483\",\"type\":\"5\",\"data\":{\"nodeMeta\":{\"title\":\"Count Levels\",\"subTitle\":\"Code\"},"
                + "\"outputs\":[{\"type\":\"integer\",\"name\":\"error_count\"},{\"type\":\"object\",\"name\":\"level_counts\"},"
                + "{\"type\":\"string\",\"name\":\"log_snippet\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"input\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}}],"
                + "\"code\":\"import re\\nimport collections\\nlevel_counts = collections.Counter(ERROR WARN INFO DEBUG)\"}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"integer\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"138483\",\"name\":\"error_count\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"138483\"},"
                + "{\"sourceNodeID\":\"138483\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"integer\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("input",
                "INFO boot\nERROR disk\nwarn retry\nerror disk"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals(2, output.get("output"));
        Map<String, Object> codeOutput = castMap(castMap(output.get("nodeOutputs")).get("138483"));
        assertEquals(2, codeOutput.get("error_count"));
        Map<String, Object> levelCounts = castMap(codeOutput.get("level_counts"));
        assertEquals(2, levelCounts.get("ERROR"));
        assertEquals(1, levelCounts.get("WARN"));
        assertEquals(1, levelCounts.get("INFO"));
        assertTrue(String.valueOf(codeOutput.get("log_snippet")).contains("INFO boot"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void workflowRunExecutesKnowledgeQueryNode() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock(), knowledgeService);

        Map<String, Object> hitItem = new LinkedHashMap<>();
        hitItem.put("title", "Attraction.txt");
        hitItem.put("snippet", "Bell tower travel guide");
        Map<String, Object> hit = new LinkedHashMap<>();
        hit.put("prompt", "Bell tower travel guide");
        hit.put("score", Collections.singletonList(0.91D));
        hit.put("searchList", Collections.singletonList(hitItem));
        when(knowledgeService.hitKnowledge(eq("dev-admin"), eq("default-org"), any(Map.class))).thenReturn(hit);

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("KnowledgeNodeFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"place\"}]}},"
                + "{\"id\":\"115653\",\"type\":\"1006\",\"data\":{\"nodeMeta\":{\"title\":\"检索景点信息\",\"subTitle\":\"知识库检索\"},"
                + "\"outputs\":[{\"type\":\"object\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"Query\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"place\"}}}}],"
                + "\"datasetParam\":[{\"name\":\"knowledgeList\",\"input\":{\"type\":\"list\","
                + "\"value\":{\"type\":\"literal\",\"content\":[\"kb-001\"]}}},"
                + "{\"name\":\"topK\",\"input\":{\"type\":\"integer\",\"value\":{\"type\":\"literal\",\"content\":3}}},"
                + "{\"name\":\"threshold\",\"input\":{\"type\":\"float\",\"value\":{\"type\":\"literal\",\"content\":0.4}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"list\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"115653\",\"name\":\"output.searchList\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"115653\"},"
                + "{\"sourceNodeID\":\"115653\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"list\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("place", "Bell tower"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        List<Map<String, Object>> searchList = (List<Map<String, Object>>) output.get("output");
        assertEquals("Attraction.txt", searchList.get(0).get("title"));
        Map<String, Object> knowledgeOutput = castMap(castMap(output.get("nodeOutputs")).get("115653"));
        assertEquals("Bell tower travel guide", castMap(knowledgeOutput.get("output")).get("prompt"));
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(knowledgeService).hitKnowledge(eq("dev-admin"), eq("default-org"), captor.capture());
        Map<String, Object> request = captor.getValue();
        assertEquals("Bell tower", request.get("question"));
        List<Map<String, Object>> knowledgeList = (List<Map<String, Object>>) request.get("knowledgeList");
        assertEquals("kb-001", knowledgeList.get(0).get("knowledgeId"));
        Map<String, Object> matchParams = (Map<String, Object>) request.get("knowledgeMatchParams");
        assertEquals(3, matchParams.get("topK"));
        assertEquals(0.4D, matchParams.get("threshold"));
    }

    @Test
    public void workflowRunExecutesDocumentParseNodeForTextFiles() throws Exception {
        Path file = Files.createTempFile("wanwu-workflow-doc-", ".txt");
        Files.write(file, Collections.singletonList("Wanwu document parser keeps workflow text."), StandardCharsets.UTF_8);
        try {
            InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
            AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

            WorkflowCreateCommand create = new WorkflowCreateCommand();
            create.setName("DocumentParseFlow");
            create.setSchema("{"
                    + "\"nodes\":["
                    + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                    + "\"outputs\":[{\"type\":\"string\",\"name\":\"file\"}]}},"
                    + "{\"id\":\"152281\",\"type\":\"1008\",\"data\":{\"nodeMeta\":{\"title\":\"Document Parse\"},"
                    + "\"outputs\":[{\"type\":\"string\",\"name\":\"text\"}],"
                    + "\"inputs\":{\"inputParameters\":[{\"name\":\"FileUrl\",\"input\":{\"type\":\"string\","
                    + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"file\"}}}}]}}},"
                    + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                    + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                    + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"152281\",\"name\":\"text\"}}}}]}}}"
                    + "],"
                    + "\"edges\":["
                    + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"152281\"},"
                    + "{\"sourceNodeID\":\"152281\",\"targetNodeID\":\"900001\"}"
                    + "],"
                    + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                    + "}");
            create.setUserId("dev-admin");
            create.setOrgId("default-org");
            WorkflowCreateResult created = service.createWorkflow(create);

            WorkflowRunCommand run = new WorkflowRunCommand();
            run.setWorkflowId(created.getWorkflowId());
            run.setUserId("dev-admin");
            run.setOrgId("default-org");
            run.setInput(Collections.<String, Object>singletonMap("file", file.toUri().toString()));

            Map<String, Object> output = service.runWorkflow(run).getOutput();

            assertEquals("Wanwu document parser keeps workflow text.", output.get("output"));
            Map<String, Object> parseOutput = castMap(castMap(output.get("nodeOutputs")).get("152281"));
            assertEquals("Wanwu document parser keeps workflow text.", parseOutput.get("text"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void workflowRunExecutesDocumentParseNodeForPdfFiles() throws Exception {
        Path file = Files.createTempFile("wanwu-workflow-doc-", ".pdf");
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);
            try {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(72, 720);
                content.showText("Wanwu PDF workflow parser keeps binary text.");
                content.endText();
            } finally {
                content.close();
            }
            document.save(file.toFile());
        } finally {
            document.close();
        }
        try {
            InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
            AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

            WorkflowCreateCommand create = new WorkflowCreateCommand();
            create.setName("DocumentParsePdfFlow");
            create.setSchema("{"
                    + "\"nodes\":["
                    + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                    + "\"outputs\":[{\"type\":\"string\",\"name\":\"file\"}]}},"
                    + "{\"id\":\"152281\",\"type\":\"1008\",\"data\":{\"nodeMeta\":{\"title\":\"Document Parse\"},"
                    + "\"outputs\":[{\"type\":\"string\",\"name\":\"text\"}],"
                    + "\"inputs\":{\"inputParameters\":[{\"name\":\"FileUrl\",\"input\":{\"type\":\"string\","
                    + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"file\"}}}}]}}},"
                    + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                    + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                    + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"152281\",\"name\":\"text\"}}}}]}}}"
                    + "],"
                    + "\"edges\":["
                    + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"152281\"},"
                    + "{\"sourceNodeID\":\"152281\",\"targetNodeID\":\"900001\"}"
                    + "],"
                    + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                    + "}");
            create.setUserId("dev-admin");
            create.setOrgId("default-org");
            WorkflowCreateResult created = service.createWorkflow(create);

            WorkflowRunCommand run = new WorkflowRunCommand();
            run.setWorkflowId(created.getWorkflowId());
            run.setUserId("dev-admin");
            run.setOrgId("default-org");
            run.setInput(Collections.<String, Object>singletonMap("file", file.toUri().toString()));

            Map<String, Object> output = service.runWorkflow(run).getOutput();

            assertTrue(String.valueOf(output.get("output")).contains("Wanwu PDF workflow parser"));
            Map<String, Object> parseOutput = castMap(castMap(output.get("nodeOutputs")).get("152281"));
            assertTrue(String.valueOf(parseOutput.get("text")).contains("binary text"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void workflowRunExecutesDocumentParseNodeForDocxFiles() throws Exception {
        Path file = Files.createTempFile("wanwu-workflow-doc-", ".docx");
        ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file));
        try {
            zip.putNextEntry(new ZipEntry("word/document.xml"));
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                    + "<w:body>"
                    + "<w:p><w:r><w:t>Wanwu DOCX workflow parser.</w:t></w:r></w:p>"
                    + "<w:p><w:r><w:t>Second paragraph survives extraction.</w:t></w:r></w:p>"
                    + "</w:body></w:document>";
            zip.write(xml.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        } finally {
            zip.close();
        }
        try {
            InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
            AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

            WorkflowCreateCommand create = new WorkflowCreateCommand();
            create.setName("DocumentParseDocxFlow");
            create.setSchema("{"
                    + "\"nodes\":["
                    + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                    + "\"outputs\":[{\"type\":\"string\",\"name\":\"file\"}]}},"
                    + "{\"id\":\"152281\",\"type\":\"1008\",\"data\":{\"nodeMeta\":{\"title\":\"Document Parse\"},"
                    + "\"outputs\":[{\"type\":\"string\",\"name\":\"text\"}],"
                    + "\"inputs\":{\"inputParameters\":[{\"name\":\"FileUrl\",\"input\":{\"type\":\"string\","
                    + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"file\"}}}}]}}},"
                    + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                    + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                    + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"152281\",\"name\":\"text\"}}}}]}}}"
                    + "],"
                    + "\"edges\":["
                    + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"152281\"},"
                    + "{\"sourceNodeID\":\"152281\",\"targetNodeID\":\"900001\"}"
                    + "],"
                    + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                    + "}");
            create.setUserId("dev-admin");
            create.setOrgId("default-org");
            WorkflowCreateResult created = service.createWorkflow(create);

            WorkflowRunCommand run = new WorkflowRunCommand();
            run.setWorkflowId(created.getWorkflowId());
            run.setUserId("dev-admin");
            run.setOrgId("default-org");
            run.setInput(Collections.<String, Object>singletonMap("file", file.toUri().toString()));

            Map<String, Object> output = service.runWorkflow(run).getOutput();

            assertTrue(String.valueOf(output.get("output")).contains("Wanwu DOCX workflow parser."));
            Map<String, Object> parseOutput = castMap(castMap(output.get("nodeOutputs")).get("152281"));
            assertTrue(String.valueOf(parseOutput.get("text")).contains("Second paragraph survives extraction."));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void workflowRunExecutesToolNodeWithResultContent() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("ToolNodeFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"input\"}]}},"
                + "{\"id\":\"126843\",\"type\":\"1009\",\"data\":{\"nodeMeta\":{\"title\":\"获取资料摘要\",\"subTitle\":\"MCP工具\"},"
                + "\"outputs\":[{\"type\":\"object\",\"name\":\"result\",\"schema\":[{\"type\":\"list\",\"name\":\"content\"}]}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"query\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}},"
                + "{\"name\":\"numResults\",\"input\":{\"type\":\"float\",\"value\":{\"type\":\"literal\",\"content\":3}}}],"
                + "\"mcpInfoList\":[]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"list\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"126843\",\"name\":\"result.content\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"126843\"},"
                + "{\"sourceNodeID\":\"126843\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"list\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("input", "wanwu java"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        List<Map<String, Object>> content = (List<Map<String, Object>>) output.get("output");
        assertEquals("text", content.get(0).get("type"));
        assertTrue(String.valueOf(content.get(0).get("text")).contains("wanwu java"));
        Map<String, Object> toolOutput = castMap(castMap(output.get("nodeOutputs")).get("126843"));
        assertEquals(content, castMap(toolOutput.get("result")).get("content"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void workflowRunEmitsStructuredPoiToolContent() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("PoiToolFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"keywords\"},{\"type\":\"string\",\"name\":\"location\"}]}},"
                + "{\"id\":\"138194\",\"type\":\"1009\",\"data\":{\"nodeMeta\":{\"title\":\"POI Search\",\"subTitle\":\"MCP Tool\"},"
                + "\"outputs\":[{\"type\":\"object\",\"name\":\"result\",\"schema\":[{\"type\":\"list\",\"name\":\"content\"}]}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"keywords\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"keywords\"}}}},"
                + "{\"name\":\"location\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"location\"}}}}],"
                + "\"mcpInfoList\":[]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"list\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"138194\",\"name\":\"result.content\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"138194\"},"
                + "{\"sourceNodeID\":\"138194\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"list\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("keywords", "bell tower");
        input.put("location", "xian");
        run.setInput(input);

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        List<Map<String, Object>> content = (List<Map<String, Object>>) output.get("output");
        Map<String, Object> first = content.get(0);
        assertEquals("bell tower", first.get("name"));
        assertEquals("xian bell tower", first.get("address"));
        assertEquals("poi", first.get("category"));
        assertEquals("POI", first.get("categoryLabel"));
        assertEquals(480, first.get("distanceMeters"));
        assertEquals(4.8D, first.get("rating"));
        assertTrue(((List<?>) first.get("tags")).contains("poi"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void workflowRunEmitsStructuredSearchToolContent() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("SearchToolFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"input\"}]}},"
                + "{\"id\":\"126843\",\"type\":\"1009\",\"data\":{\"nodeMeta\":{\"title\":\"Research Search\",\"subTitle\":\"MCP Tool\"},"
                + "\"outputs\":[{\"type\":\"object\",\"name\":\"result\",\"schema\":[{\"type\":\"list\",\"name\":\"content\"}]}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"query\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}},"
                + "{\"name\":\"numResults\",\"input\":{\"type\":\"integer\",\"value\":{\"type\":\"literal\",\"content\":2}}}],"
                + "\"mcpInfoList\":[]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"list\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"126843\",\"name\":\"result.content\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"126843\"},"
                + "{\"sourceNodeID\":\"126843\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"list\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("input", "wanwu java"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        List<Map<String, Object>> content = (List<Map<String, Object>>) output.get("output");
        assertEquals(2, content.size());
        Map<String, Object> first = content.get(0);
        assertEquals(1, first.get("rank"));
        assertEquals("local-search", first.get("source"));
        assertTrue(String.valueOf(first.get("title")).contains("wanwu java"));
        assertTrue(String.valueOf(first.get("snippet")).contains("wanwu java"));
        assertTrue(String.valueOf(first.get("url")).contains("wanwu-java"));
    }

    @Test
    public void workflowRunRoutesSelectorToInputNodeWhenValueMissing() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("SelectorInputFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"city\"}]}},"
                + "{\"id\":\"172810\",\"type\":\"8\",\"data\":{\"nodeMeta\":{\"title\":\"判断信息齐全\"},"
                + "\"inputs\":{\"branches\":[{\"condition\":{\"logic\":2,\"conditions\":[{\"operator\":10,"
                + "\"left\":{\"input\":{\"type\":\"string\",\"value\":{\"type\":\"ref\","
                + "\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"city\"}}}}}]}}]}}},"
                + "{\"id\":\"152577\",\"type\":\"30\",\"data\":{\"nodeMeta\":{\"title\":\"补充地区信息\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"city\",\"required\":true}],"
                + "\"inputs\":{\"outputSchema\":\"[{\\\"type\\\":\\\"string\\\",\\\"name\\\":\\\"city\\\",\\\"required\\\":true}]\"}}},"
                + "{\"id\":\"has-city\",\"type\":\"15\",\"data\":{\"nodeMeta\":{\"title\":\"Has City\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"String1\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"city\"}}}}],"
                + "\"concatParams\":[{\"name\":\"concatResult\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"city {{String1}}\"}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"152577\",\"name\":\"city\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"id\":\"start-selector\",\"sourceNodeID\":\"100001\",\"targetNodeID\":\"172810\"},"
                + "{\"id\":\"selector-input\",\"sourceNodeID\":\"172810\",\"targetNodeID\":\"152577\",\"sourcePortID\":\"false\"},"
                + "{\"id\":\"selector-has-city\",\"sourceNodeID\":\"172810\",\"targetNodeID\":\"has-city\",\"sourcePortID\":\"true\"},"
                + "{\"id\":\"input-end\",\"sourceNodeID\":\"152577\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("city", ""));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("unknown", output.get("output"), String.valueOf(output));
        List<Map<String, Object>> steps = castList(output.get("steps"));
        assertEquals("152577", steps.get(2).get("nodeId"));
        Map<String, Object> selectorOutput = castMap(castMap(output.get("nodeOutputs")).get("172810"));
        assertEquals(Boolean.FALSE, selectorOutput.get("result"));
        assertEquals("false", selectorOutput.get("branch"));
        assertFalse(castMap(output.get("nodeOutputs")).containsKey("has-city"));
    }

    @Test
    public void workflowRunExecutesDocumentGenerateNode() throws Exception {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("DocumentGenerateFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"content\"}]}},"
                + "{\"id\":\"164693\",\"type\":\"1007\",\"data\":{\"nodeMeta\":{\"title\":\"生成文档\",\"subTitle\":\"文档生成\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"fileUrl\"}],"
                + "\"inputs\":{\"inputParameters\":["
                + "{\"name\":\"Title\",\"input\":{\"type\":\"string\",\"value\":{\"type\":\"literal\",\"content\":\"Report\"}}},"
                + "{\"name\":\"Content\",\"input\":{\"type\":\"string\",\"value\":{\"type\":\"ref\","
                + "\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"content\"}}}},"
                + "{\"name\":\"fileType\",\"input\":{\"type\":\"string\",\"value\":{\"type\":\"literal\",\"content\":\"txt\"}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"164693\",\"name\":\"fileUrl\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"164693\"},"
                + "{\"sourceNodeID\":\"164693\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("content", "Generated report body"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        String fileUrl = String.valueOf(output.get("output"));
        assertTrue(fileUrl.startsWith("/service/api/v1/file/download/workflow-doc-"), fileUrl);
        String fileId = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        Path generated = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"),
                "wanwu-java-uploads", "files", fileId);
        try {
            assertTrue(Files.isRegularFile(generated), fileUrl);
            assertTrue(new String(Files.readAllBytes(generated), StandardCharsets.UTF_8)
                    .contains("Generated report body"));
        } finally {
            Files.deleteIfExists(generated);
        }
    }

    @Test
    public void workflowRunExecutesLoopNodeWithArrayInput() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("LoopFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"list\",\"name\":\"items\"}]}},"
                + "{\"id\":\"110184\",\"type\":\"21\",\"data\":{\"nodeMeta\":{\"title\":\"循环处理\",\"subTitle\":\"循环\"},"
                + "\"outputs\":[{\"name\":\"output\",\"input\":{\"type\":\"string\",\"value\":{\"type\":\"ref\","
                + "\"content\":{\"source\":\"block-output\",\"blockID\":\"110184\",\"name\":\"finalText\"}}}}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"input\",\"input\":{\"type\":\"list\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"items\"}}}}],"
                + "\"variableParameters\":[{\"name\":\"finalText\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"seed\"}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"110184\",\"name\":\"output\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"110184\"},"
                + "{\"sourceNodeID\":\"110184\",\"targetNodeID\":\"900001\",\"sourcePortID\":\"loop-output\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("items", Arrays.asList("intro", "body"));
        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(input);

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("seed\nintro\nbody", output.get("output"), String.valueOf(output));
        Map<String, Object> loopOutput = castMap(castMap(output.get("nodeOutputs")).get("110184"));
        assertEquals("seed\nintro\nbody", loopOutput.get("finalText"));
        assertEquals(2, loopOutput.get("iterations"));
    }

    @Test
    public void workflowRunExecutesLoopNodeInlineBlocksForEachArrayItem() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("InlineLoopFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"list\",\"name\":\"items\",\"schema\":{\"type\":\"string\"}}]}},"
                + "{\"id\":\"110184\",\"type\":\"21\",\"data\":{\"nodeMeta\":{\"title\":\"Loop\"},"
                + "\"outputs\":[{\"name\":\"output\",\"input\":{\"type\":\"string\",\"value\":{\"type\":\"ref\","
                + "\"content\":{\"source\":\"block-output\",\"blockID\":\"110184\",\"name\":\"finalText\"}}}}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"input\",\"input\":{\"type\":\"list\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"items\"}}}}],"
                + "\"loopType\":\"array\",\"variableParameters\":[{\"name\":\"finalText\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"\"}}}]}},"
                + "\"blocks\":["
                + "{\"id\":\"186910\",\"type\":\"15\",\"data\":{\"nodeMeta\":{\"title\":\"Append\"},"
                + "\"inputs\":{\"concatParams\":[{\"name\":\"concatResult\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"{{finalText}}{{input}};\"}}}]}}},"
                + "{\"id\":\"120390\",\"type\":\"20\",\"data\":{\"nodeMeta\":{\"title\":\"Set Variable\"},"
                + "\"inputs\":{\"inputParameters\":[{\"left\":{\"type\":\"string\",\"value\":{\"type\":\"ref\","
                + "\"content\":{\"source\":\"block-output\",\"blockID\":\"110184\",\"name\":\"finalText\"}}},"
                + "\"right\":{\"type\":\"string\",\"value\":{\"type\":\"ref\","
                + "\"content\":{\"source\":\"block-output\",\"blockID\":\"186910\",\"name\":\"output\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"110184\",\"targetNodeID\":\"186910\",\"sourcePortID\":\"loop-function-inline-output\"},"
                + "{\"sourceNodeID\":\"186910\",\"targetNodeID\":\"120390\"},"
                + "{\"sourceNodeID\":\"120390\",\"targetNodeID\":\"110184\",\"targetPortID\":\"loop-function-inline-input\"}"
                + "]},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"110184\",\"name\":\"output\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"110184\"},"
                + "{\"sourceNodeID\":\"110184\",\"targetNodeID\":\"900001\",\"sourcePortID\":\"loop-output\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("items", Arrays.asList("A", "B"));
        run.setInput(input);

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("A;B;", output.get("output"), String.valueOf(output));
        Map<String, Object> loopOutput = castMap(castMap(output.get("nodeOutputs")).get("110184"));
        assertEquals("A;B;", loopOutput.get("finalText"));
        assertEquals(2, loopOutput.get("iterations"));
        assertEquals(2, ((List<?>) loopOutput.get("loopOutputs")).size());
    }

    @Test
    public void workflowRunExecutesSetVariableNode() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("SetVariableFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"draft\"}]}},"
                + "{\"id\":\"120390\",\"type\":\"20\",\"data\":{\"nodeMeta\":{\"title\":\"设置变量\",\"subTitle\":\"设置变量\"},"
                + "\"inputs\":{\"inputParameters\":[{\"left\":{\"type\":\"string\",\"value\":{\"type\":\"ref\","
                + "\"content\":{\"source\":\"block-output\",\"blockID\":\"120390\",\"name\":\"finalText\"}}},"
                + "\"right\":{\"type\":\"string\",\"value\":{\"type\":\"ref\","
                + "\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"draft\"}}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"120390\",\"name\":\"finalText\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"120390\"},"
                + "{\"sourceNodeID\":\"120390\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("draft", "expanded paragraph"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("expanded paragraph", output.get("output"), String.valueOf(output));
        Map<String, Object> nodeOutput = castMap(castMap(output.get("nodeOutputs")).get("120390"));
        assertEquals("expanded paragraph", nodeOutput.get("finalText"));
    }

    @Test
    public void workflowRunResolvesGoTemplateNodeInputsAndNumericEdges() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("GoTemplateFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"input\"}]}},"
                + "{\"id\":\"110001\",\"type\":\"3\",\"data\":{\"nodeMeta\":{\"title\":\"Draft\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"input\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}}],"
                + "\"llmParam\":[{\"name\":\"prompt\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"Draft {{input}}\"}}}]}}},"
                + "{\"id\":\"120001\",\"type\":\"15\",\"data\":{\"nodeMeta\":{\"title\":\"Concat\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"String1\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"110001\",\"name\":\"output\"}}}}],"
                + "\"concatParams\":[{\"name\":\"concatResult\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"Final {{String1}}\"}}}]}}},"
                + "{\"id\":\"900001\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"120001\",\"name\":\"output\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"sourceNodeID\":\"100001\",\"targetNodeID\":\"110001\"},"
                + "{\"sourceNodeID\":\"110001\",\"targetNodeID\":\"120001\"},"
                + "{\"sourceNodeID\":\"120001\",\"targetNodeID\":\"900001\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("input", "hello"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("Final Draft hello", output.get("output"), String.valueOf(output));
        List<Map<String, Object>> steps = castList(output.get("steps"));
        assertEquals(4, steps.size());
        assertEquals("Start", steps.get(0).get("name"));
        assertEquals("Draft", steps.get(1).get("name"));
        assertEquals("Draft hello", castMap(steps.get(1).get("output")).get("output"));
        assertEquals("Draft hello", castMap(steps.get(2).get("input")).get("String1"));
        Map<String, Object> nodeOutputs = castMap(output.get("nodeOutputs"));
        assertEquals("Final Draft hello", castMap(nodeOutputs.get("120001")).get("output"));
        assertEquals("Final Draft hello", castMap(nodeOutputs.get("900001")).get("output"));
    }

    @Test
    public void workflowRunFollowsSimpleConditionalEdges() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("ApprovalFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"type\":\"start\",\"data\":{\"label\":\"Start\"}},"
                + "{\"id\":\"approved\",\"type\":\"llm\",\"data\":{\"label\":\"Approved Path\"}},"
                + "{\"id\":\"rejected\",\"type\":\"llm\",\"data\":{\"label\":\"Rejected Path\"}}"
                + "],"
                + "\"edges\":["
                + "{\"id\":\"edge-approved\",\"source\":\"start\",\"target\":\"approved\",\"condition\":\"approved == true\"},"
                + "{\"id\":\"edge-rejected\",\"source\":\"start\",\"target\":\"rejected\",\"condition\":\"approved == false\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"summary\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("approved", true));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        List<Map<String, Object>> steps = castList(output.get("steps"));
        assertEquals(2, steps.size());
        assertEquals("start", steps.get(0).get("nodeId"));
        assertEquals("approved", steps.get(1).get("nodeId"));
        assertTrue(String.valueOf(output.get("summary")).contains("Approved Path"));

        Map<String, Object> trace = castMap(output.get("trace"));
        assertEquals(2, trace.get("nodeCount"));
        assertTrue(String.valueOf(trace.get("skippedNodeIds")).contains("rejected"));

        List<Map<String, Object>> evaluations = castList(trace.get("edgeEvaluations"));
        assertEquals(2, evaluations.size());
        assertEquals("edge-approved", evaluations.get(0).get("edgeId"));
        assertEquals(Boolean.TRUE, evaluations.get(0).get("matched"));
        assertEquals("edge-rejected", evaluations.get(1).get("edgeId"));
        assertEquals(Boolean.FALSE, evaluations.get(1).get("matched"));

        WorkflowCreateCommand handleCreate = new WorkflowCreateCommand();
        handleCreate.setName("HandleFlow");
        handleCreate.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"type\":\"start\",\"data\":{\"label\":\"Start\"}},"
                + "{\"id\":\"approved\",\"type\":\"llm\",\"data\":{\"label\":\"Approved Path\"}},"
                + "{\"id\":\"rejected\",\"type\":\"llm\",\"data\":{\"label\":\"Rejected Path\"}}"
                + "],"
                + "\"edges\":["
                + "{\"id\":\"handle-true\",\"source\":\"start\",\"target\":\"approved\",\"sourceHandle\":\"true\"},"
                + "{\"id\":\"handle-false\",\"source\":\"start\",\"target\":\"rejected\",\"sourceHandle\":\"false\"}"
                + "]"
                + "}");
        handleCreate.setUserId("dev-admin");
        handleCreate.setOrgId("default-org");
        WorkflowCreateResult handleCreated = service.createWorkflow(handleCreate);

        WorkflowRunCommand handleRun = new WorkflowRunCommand();
        handleRun.setWorkflowId(handleCreated.getWorkflowId());
        handleRun.setUserId("dev-admin");
        handleRun.setOrgId("default-org");
        handleRun.setInput(Collections.<String, Object>singletonMap("approved", false));

        List<Map<String, Object>> handleSteps = castList(service.runWorkflow(handleRun).getOutput().get("steps"));
        assertEquals(2, handleSteps.size());
        assertEquals("start", handleSteps.get(0).get("nodeId"));
        assertEquals("rejected", handleSteps.get(1).get("nodeId"));

        WorkflowCreateCommand objectCreate = new WorkflowCreateCommand();
        objectCreate.setName("ObjectConditionFlow");
        objectCreate.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"type\":\"start\",\"data\":{\"label\":\"Start\"}},"
                + "{\"id\":\"high\",\"type\":\"llm\",\"data\":{\"label\":\"High Value\"}},"
                + "{\"id\":\"low\",\"type\":\"llm\",\"data\":{\"label\":\"Low Value\"}}"
                + "],"
                + "\"edges\":["
                + "{\"id\":\"object-high\",\"source\":\"start\",\"target\":\"high\","
                + "\"data\":{\"condition\":{\"conditions\":["
                + "{\"field\":\"score\",\"operator\":\"gte\",\"value\":80},"
                + "{\"field\":\"tags\",\"operator\":\"contains\",\"value\":\"vip\"}"
                + "]}}},"
                + "{\"id\":\"object-low\",\"source\":\"start\",\"target\":\"low\","
                + "\"data\":{\"condition\":{\"field\":\"score\",\"operator\":\"lt\",\"value\":80}}}"
                + "]"
                + "}");
        objectCreate.setUserId("dev-admin");
        objectCreate.setOrgId("default-org");
        WorkflowCreateResult objectCreated = service.createWorkflow(objectCreate);

        Map<String, Object> objectInput = new LinkedHashMap<>();
        objectInput.put("score", 90);
        objectInput.put("tags", "vip customer");
        WorkflowRunCommand objectRun = new WorkflowRunCommand();
        objectRun.setWorkflowId(objectCreated.getWorkflowId());
        objectRun.setUserId("dev-admin");
        objectRun.setOrgId("default-org");
        objectRun.setInput(objectInput);

        Map<String, Object> objectOutput = service.runWorkflow(objectRun).getOutput();
        List<Map<String, Object>> objectSteps = castList(objectOutput.get("steps"));
        assertEquals(2, objectSteps.size(), String.valueOf(objectOutput));
        assertEquals("start", objectSteps.get(0).get("nodeId"));
        assertEquals("high", objectSteps.get(1).get("nodeId"));

        Map<String, Object> objectTrace = castMap(objectOutput.get("trace"));
        List<Map<String, Object>> objectEvaluations = castList(objectTrace.get("edgeEvaluations"));
        assertEquals("score >= 80 && tags contains vip", objectEvaluations.get(0).get("condition"));
        assertEquals(Boolean.TRUE, objectEvaluations.get(0).get("matched"));
        assertEquals("score < 80", objectEvaluations.get(1).get("condition"));
        assertEquals(Boolean.FALSE, objectEvaluations.get(1).get("matched"));
    }

    @Test
    public void workflowRunUsesIntentNodeBranchOutputForHandles() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("IntentBranchFlow");
        create.setSchema("{"
                + "\"nodes\":["
                + "{\"id\":\"100001\",\"type\":\"1\",\"data\":{\"nodeMeta\":{\"title\":\"Start\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"input\"}]}},"
                + "{\"id\":\"150610\",\"type\":\"22\",\"data\":{\"nodeMeta\":{\"title\":\"Intent\"},"
                + "\"outputs\":[{\"type\":\"integer\",\"name\":\"classificationId\"},{\"type\":\"string\",\"name\":\"reason\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"query\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}}],"
                + "\"intents\":[{\"name\":\"咨询\"},{\"name\":\"投诉\"}]}}},"
                + "{\"id\":\"consult\",\"type\":\"15\",\"data\":{\"nodeMeta\":{\"title\":\"Consult\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"String1\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}}],"
                + "\"concatParams\":[{\"name\":\"concatResult\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"consult {{String1}}\"}}}]}}},"
                + "{\"id\":\"complaint\",\"type\":\"15\",\"data\":{\"nodeMeta\":{\"title\":\"Complaint\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"String1\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}}],"
                + "\"concatParams\":[{\"name\":\"concatResult\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"complaint {{String1}}\"}}}]}}},"
                + "{\"id\":\"unknown\",\"type\":\"15\",\"data\":{\"nodeMeta\":{\"title\":\"Unknown\"},"
                + "\"outputs\":[{\"type\":\"string\",\"name\":\"output\"}],"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"String1\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"100001\",\"name\":\"input\"}}}}],"
                + "\"concatParams\":[{\"name\":\"concatResult\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"literal\",\"content\":\"unknown {{String1}}\"}}}]}}},"
                + "{\"id\":\"end-complaint\",\"type\":\"2\",\"data\":{\"nodeMeta\":{\"title\":\"End\"},"
                + "\"inputs\":{\"inputParameters\":[{\"name\":\"output\",\"input\":{\"type\":\"string\","
                + "\"value\":{\"type\":\"ref\",\"content\":{\"source\":\"block-output\",\"blockID\":\"complaint\",\"name\":\"output\"}}}}]}}}"
                + "],"
                + "\"edges\":["
                + "{\"id\":\"start-intent\",\"sourceNodeID\":\"100001\",\"targetNodeID\":\"150610\"},"
                + "{\"id\":\"intent-consult\",\"sourceNodeID\":\"150610\",\"targetNodeID\":\"consult\",\"sourcePortID\":\"branch_0\"},"
                + "{\"id\":\"intent-complaint\",\"sourceNodeID\":\"150610\",\"targetNodeID\":\"complaint\",\"sourcePortID\":\"branch_1\"},"
                + "{\"id\":\"intent-default\",\"sourceNodeID\":\"150610\",\"targetNodeID\":\"unknown\",\"sourcePortID\":\"default\"},"
                + "{\"id\":\"complaint-end\",\"sourceNodeID\":\"complaint\",\"targetNodeID\":\"end-complaint\"}"
                + "],"
                + "\"outputs\":[{\"name\":\"output\",\"type\":\"string\"}]"
                + "}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        WorkflowRunCommand run = new WorkflowRunCommand();
        run.setWorkflowId(created.getWorkflowId());
        run.setUserId("dev-admin");
        run.setOrgId("default-org");
        run.setInput(Collections.<String, Object>singletonMap("input", "我要投诉楼下噪音扰民"));

        Map<String, Object> output = service.runWorkflow(run).getOutput();

        assertEquals("complaint 我要投诉楼下噪音扰民", output.get("output"), String.valueOf(output));
        List<Map<String, Object>> steps = castList(output.get("steps"));
        assertEquals(4, steps.size(), String.valueOf(output));
        assertEquals("100001", steps.get(0).get("nodeId"));
        assertEquals("150610", steps.get(1).get("nodeId"));
        assertEquals("complaint", steps.get(2).get("nodeId"));
        assertEquals("end-complaint", steps.get(3).get("nodeId"));
        Map<String, Object> nodeOutputs = castMap(output.get("nodeOutputs"));
        Map<String, Object> intentOutput = castMap(nodeOutputs.get("150610"));
        assertEquals(1, intentOutput.get("classificationId"));
        assertEquals("branch_1", intentOutput.get("branch"));
        assertFalse(nodeOutputs.containsKey("consult"));
        assertFalse(nodeOutputs.containsKey("unknown"));
    }

    @Test
    public void convertAppTypeMovesWorkflowBetweenWorkflowAndChatflowLists() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("ConvertibleFlow");
        create.setDesc("convertible");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult created = service.createWorkflow(create);

        AppTypeConvertCommand toChatflow = new AppTypeConvertCommand(
                created.getWorkflowId(), "workflow", "chatflow", "dev-admin", "default-org");
        service.convertAppType(toChatflow);

        assertEquals(0, service.listApplications(
                new ApplicationListQuery("workflow", "Convertible", "dev-admin", "default-org")).getTotal());
        ApplicationListResult chatflows = service.listApplications(
                new ApplicationListQuery("chatflow", "Convertible", "dev-admin", "default-org"));
        assertEquals(1, chatflows.getTotal());
        assertEquals(created.getWorkflowId(), chatflows.getList().get(0).get("workflow_id"));
        assertEquals("ConvertibleFlow", service.exportChatflow(
                new WorkflowExportQuery(created.getWorkflowId(), "", false, "dev-admin", "default-org")).getName());

        AppTypeConvertCommand toWorkflow = new AppTypeConvertCommand(
                created.getWorkflowId(), "chatflow", "workflow", "dev-admin", "default-org");
        service.convertAppType(toWorkflow);

        assertEquals(0, service.listApplications(
                new ApplicationListQuery("chatflow", "Convertible", "dev-admin", "default-org")).getTotal());
        assertEquals(1, service.listApplications(
                new ApplicationListQuery("workflow", "Convertible", "dev-admin", "default-org")).getTotal());
        assertEquals("ConvertibleFlow", service.exportWorkflow(
                new WorkflowExportQuery(created.getWorkflowId(), "", false, "dev-admin", "default-org")).getName());
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
    public void chatflowOpenApiConversationsPersistMessagesAndListState() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        WorkflowCreateCommand create = new WorkflowCreateCommand();
        create.setName("PolicyChat");
        create.setDesc("chatflow for openapi");
        create.setSchema("{\"nodes\":[]}");
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        WorkflowCreateResult chatflow = service.createChatflow(create);

        ChatflowConversationCreateCommand createConversation = new ChatflowConversationCreateCommand();
        createConversation.setChatflowId(chatflow.getWorkflowId());
        createConversation.setConversationName("Policy chat");
        createConversation.setUserId("dev-admin");
        createConversation.setOrgId("default-org");
        Map<String, Object> created = service.createChatflowOpenApiConversation(createConversation);
        String conversationId = String.valueOf(created.get("conversation_id"));
        assertTrue(conversationId.startsWith("conversation-"));
        assertEquals("Policy chat", created.get("conversation_name"));

        ChatflowConversationChatCommand chat = new ChatflowConversationChatCommand();
        chat.setChatflowId(chatflow.getWorkflowId());
        chat.setConversationId(conversationId);
        chat.setQuery("hello chatflow");
        chat.setParameters(Collections.<String, Object>singletonMap("city", "Beijing"));
        chat.setUserId("dev-admin");
        chat.setOrgId("default-org");
        Map<String, Object> chatResult = service.chatflowOpenApiChat(chat);
        assertEquals(0, chatResult.get("code"));
        assertEquals(conversationId, chatResult.get("conversation_id"));
        assertEquals("Chatflow response: hello chatflow", chatResult.get("response"));

        ChatflowConversationMessageListQuery messages = new ChatflowConversationMessageListQuery();
        messages.setChatflowId(chatflow.getWorkflowId());
        messages.setConversationId(conversationId);
        messages.setLimit(10);
        messages.setUserId("dev-admin");
        messages.setOrgId("default-org");
        Map<String, Object> messagePage = service.listChatflowOpenApiConversationMessages(messages);
        List<Map<String, Object>> rows = castList(messagePage.get("data"));
        assertEquals(2, rows.size());
        assertEquals("user", rows.get(0).get("role"));
        assertEquals("hello chatflow", rows.get(0).get("content"));
        assertEquals("Beijing", castMap(rows.get(0).get("meta_data")).get("city"));
        assertEquals("assistant", rows.get(1).get("role"));
        assertEquals("Chatflow response: hello chatflow", rows.get(1).get("content"));

        ChatflowConversationListQuery listQuery = new ChatflowConversationListQuery();
        listQuery.setChatflowId(chatflow.getWorkflowId());
        listQuery.setUserId("dev-admin");
        listQuery.setOrgId("default-org");
        Map<String, Object> conversations = service.listChatflowOpenApiConversations(listQuery);
        assertEquals(1L, conversations.get("total"));
        assertEquals(conversationId, castList(conversations.get("conversations")).get(0).get("conversation_id"));

        ChatflowConversationDeleteByIdCommand delete = new ChatflowConversationDeleteByIdCommand();
        delete.setChatflowId(chatflow.getWorkflowId());
        delete.setConversationId(conversationId);
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");
        service.deleteChatflowOpenApiConversation(delete);
        assertEquals(0L, service.listChatflowOpenApiConversations(listQuery).get("total"));
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
    public void apiKeyStatisticPersistsAggregatesAndRecords() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        service.recordApiKeyStatistic(apiKeyUsage(
                "dev-admin-key",
                "GET-/service/api/openapi/v1/model/list",
                Instant.parse("2026-06-28T03:00:00Z").toEpochMilli(),
                "200",
                false,
                0L,
                20L,
                ""));
        service.recordApiKeyStatistic(apiKeyUsage(
                "dev-admin-key",
                "GET-/service/api/openapi/v1/model/list",
                Instant.parse("2026-06-29T03:00:00Z").toEpochMilli(),
                "200",
                false,
                0L,
                40L,
                ""));
        service.recordApiKeyStatistic(apiKeyUsage(
                "dev-admin-key",
                "GET-/service/api/openapi/v1/model/list",
                Instant.parse("2026-06-29T04:00:00Z").toEpochMilli(),
                "500",
                true,
                100L,
                0L,
                "{\"stream\":true}"));

        ApiKeyStatisticPageQuery query = new ApiKeyStatisticPageQuery(
                "dev-admin",
                "default-org",
                "2026-06-29",
                "2026-06-29",
                Collections.singletonList("dev-admin-key"),
                Collections.singletonList("GET-/service/api/openapi/v1/model/list"),
                1,
                10);
        ApiKeyStatisticResult overview = service.getApiKeyStatistic(query);
        assertEquals(2D, overview.getOverview().getCallCount().getValue(), 0.001D);
        assertEquals(100D, overview.getOverview().getCallCount().getPeriodOverPeriod(), 0.001D);
        assertEquals(1D, overview.getOverview().getCallFailure().getValue(), 0.001D);
        assertEquals(100D, overview.getOverview().getAvgStreamCosts().getValue(), 0.001D);
        assertEquals(40D, overview.getOverview().getAvgNonStreamCosts().getValue(), 0.001D);
        assertEquals(2, overview.getTrend().getApiCalls().getLines().get(0).getItems().get(0).getValue(), 0.001D);

        ApiKeyStatisticListResult list = service.listApiKeyStatistics(query);
        assertEquals(1, list.getTotal());
        assertEquals(2, list.getList().get(0).getCallCount());
        assertEquals(1, list.getList().get(0).getCallFailure());
        assertEquals(1, list.getList().get(0).getStreamCount());
        assertEquals(1, list.getList().get(0).getNonStreamCount());

        ApiKeyStatisticRecordResult records = service.listApiKeyStatisticRecords(query);
        assertEquals(2, records.getTotal());
        assertEquals("500", records.getList().get(0).getResponseStatus());
        assertEquals("{\"stream\":true}", records.getList().get(0).getRequestBody());
    }

    @Test
    public void appAndModelStatisticsPersistAggregates() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        service.recordAppStatistic(appUsage(
                "agent-001",
                "agent",
                Instant.parse("2026-06-28T03:00:00Z").toEpochMilli(),
                true,
                true,
                60L,
                0L,
                "web"));
        service.recordAppStatistic(appUsage(
                "agent-001",
                "agent",
                Instant.parse("2026-06-29T03:00:00Z").toEpochMilli(),
                true,
                true,
                100L,
                0L,
                "web"));
        service.recordAppStatistic(appUsage(
                "agent-001",
                "agent",
                Instant.parse("2026-06-29T04:00:00Z").toEpochMilli(),
                false,
                false,
                0L,
                80L,
                "openapi"));

        AppStatisticPageQuery appQuery = new AppStatisticPageQuery(
                "dev-admin", "default-org", "2026-06-29", "2026-06-29",
                Collections.singletonList("agent-001"), "agent", 1, 10);
        AppStatisticResult appOverview = service.getAppStatistic(appQuery);
        assertEquals(2D, appOverview.getOverview().getCallCount().getValue(), 0.001D);
        assertEquals(100D, appOverview.getOverview().getCallCount().getPeriodOverPeriod(), 0.001D);
        assertEquals(1D, appOverview.getOverview().getCallFailure().getValue(), 0.001D);
        assertEquals(100D, appOverview.getOverview().getAvgStreamCosts().getValue(), 0.001D);
        assertEquals(0D, appOverview.getOverview().getAvgNonStreamCosts().getValue(), 0.001D);
        assertEquals(1D, appOverview.getTrend().getCallTrend().getLines().get(1).getItems().get(0).getValue(), 0.001D);
        assertEquals(1D, appOverview.getTrend().getCallTrend().getLines().get(2).getItems().get(0).getValue(), 0.001D);

        AppStatisticListResult appList = service.listAppStatistics(appQuery);
        assertEquals(1, appList.getTotal());
        assertEquals(50D, appList.getList().get(0).getFailureRate(), 0.001D);
        assertEquals(1, appList.getList().get(0).getStreamCount());
        assertEquals(1, appList.getList().get(0).getNonStreamCount());

        service.recordModelStatistic(modelUsage(
                "model-001",
                "qwen-local",
                "local",
                "llm",
                Instant.parse("2026-06-28T03:00:00Z").toEpochMilli(),
                5L,
                7L,
                true,
                true,
                40L,
                0L));
        service.recordModelStatistic(modelUsage(
                "model-001",
                "qwen-local",
                "local",
                "llm",
                Instant.parse("2026-06-29T03:00:00Z").toEpochMilli(),
                10L,
                20L,
                true,
                true,
                120L,
                0L));
        service.recordModelStatistic(modelUsage(
                "model-001",
                "qwen-local",
                "local",
                "llm",
                Instant.parse("2026-06-29T04:00:00Z").toEpochMilli(),
                8L,
                0L,
                false,
                false,
                0L,
                90L));

        ModelStatisticPageQuery modelQuery = new ModelStatisticPageQuery(
                "dev-admin", "default-org", "2026-06-29", "2026-06-29",
                Collections.singletonList("model-001"), "llm", 1, 10);
        ModelStatisticResult modelOverview = service.getModelStatistic(modelQuery);
        assertEquals(2D, modelOverview.getOverview().getCallCount().getValue(), 0.001D);
        assertEquals(38D, modelOverview.getOverview().getTotalTokens().getValue(), 0.001D);
        assertEquals(18D, modelOverview.getOverview().getPromptTokens().getValue(), 0.001D);
        assertEquals(20D, modelOverview.getOverview().getCompletionTokens().getValue(), 0.001D);
        assertEquals(120D, modelOverview.getOverview().getAvgFirstTokenLatency().getValue(), 0.001D);
        assertEquals(0D, modelOverview.getOverview().getAvgCosts().getValue(), 0.001D);
        assertEquals(1D, modelOverview.getTrend().getModelCalls().getLines().get(1).getItems().get(0).getValue(), 0.001D);
        assertEquals(38D, modelOverview.getTrend().getTokensUsage().getLines().get(0).getItems().get(0).getValue(), 0.001D);

        ModelStatisticListResult modelList = service.listModelStatistics(modelQuery);
        assertEquals(1, modelList.getTotal());
        assertEquals("model-001", modelList.getList().get(0).getModelId());
        assertEquals(50D, modelList.getList().get(0).getFailureRate(), 0.001D);
        assertEquals(120D, modelList.getList().get(0).getAvgFirstTokenLatency(), 0.001D);
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
        AppKeyInfo found = service.getAppKeyByKey(appKey.getApiKey());
        assertEquals(appKey.getApiId(), found.getApiId());
        assertEquals(created.getAssistantId(), found.getAppId());

        AppKeyDeleteCommand delete = new AppKeyDeleteCommand();
        delete.setApiId(appKey.getApiId());
        service.deleteAppKey(delete);
        assertTrue(service.listAppKeys(
                new AppKeyListQuery(created.getAssistantId(), "agent", "dev-admin", "default-org")).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.getAppKeyByKey(appKey.getApiKey()));
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
    public void legacyChatLlmConversationUsesPersistentConversationRepository() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantConversationCreateCommand create = new AssistantConversationCreateCommand();
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        create.setPrompt("legacy question");

        Map<String, Object> created = service.createLegacyChatLlmConversation(create);

        String conversationId = String.valueOf(created.get("conversationId"));
        assertTrue(conversationId.startsWith("conversation-"));
        assertEquals("legacy question", created.get("name"));
        assertEquals("legacy question", ((Map<?, ?>) ((List<?>) created.get("details")).get(0)).get("content"));

        AssistantConversationListQuery listQuery = new AssistantConversationListQuery();
        listQuery.setUserId("dev-admin");
        listQuery.setOrgId("default-org");
        AssistantConversationPageResult list = service.listLegacyChatLlmConversations(listQuery);
        assertEquals(1, list.getTotal());
        assertEquals(conversationId, list.getList().get(0).get("conversationId"));
        assertEquals("legacy question", list.getList().get(0).get("title"));

        AssistantConversationDetailQuery detailQuery = new AssistantConversationDetailQuery();
        detailQuery.setUserId("dev-admin");
        detailQuery.setOrgId("default-org");
        detailQuery.setConversationId(conversationId);
        AssistantConversationPageResult details = service.listLegacyChatLlmConversationDetails(detailQuery);
        assertEquals(1, details.getTotal());
        assertEquals("assistant", details.getList().get(0).get("role"));
        assertEquals("legacy question", details.getList().get(0).get("content"));

        AssistantConversationDeleteCommand delete = new AssistantConversationDeleteCommand();
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");
        delete.setConversationId(conversationId);
        service.deleteLegacyChatLlmConversation(delete);

        assertEquals(0, service.listLegacyChatLlmConversations(listQuery).getTotal());
    }

    @Test
    public void legacyAssistantKnowledgeFilesUsePersistentRepository() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        AssistantKnowledgeFileUploadCommand upload = new AssistantKnowledgeFileUploadCommand();
        upload.setUserId("dev-admin");
        upload.setOrgId("default-org");
        upload.setAssistantId("assistant-legacy-001");
        upload.setFiles(Collections.singletonList(
                new AssistantKnowledgeFileUploadItem("knowledge.txt", 9L, "text/plain")));

        AssistantKnowledgeFileUploadResult uploaded = service.uploadAssistantKnowledgeFiles(upload);

        assertEquals(1, uploaded.getTotal());
        assertEquals(1, uploaded.getList().size());
        String fileId = uploaded.getList().get(0);
        assertTrue(fileId.startsWith("model-use-knowledge-file-"));
        assertEquals(fileId, uploaded.getFileList().get(0).getFileId());
        assertEquals("knowledge.txt", uploaded.getFileList().get(0).getFileName());
        assertEquals("knowledge.txt", uploaded.getFileList().get(0).getFile_name());
        assertEquals(9L, uploaded.getFileList().get(0).getSize());

        AssistantKnowledgeFileListQuery listQuery = new AssistantKnowledgeFileListQuery();
        listQuery.setUserId("dev-admin");
        listQuery.setOrgId("default-org");
        listQuery.setAssistantId("assistant-legacy-001");
        AssistantKnowledgeFileListResult list = service.listAssistantKnowledgeFiles(listQuery);
        assertEquals(1, list.getTotal());
        assertEquals(fileId, list.getList().get(0).getId());
        assertEquals("/use/model/api/v1/assistant/knowledge/file/" + fileId, list.getList().get(0).getUrl());

        AssistantKnowledgeFileDeleteCommand delete = new AssistantKnowledgeFileDeleteCommand();
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");
        delete.setAssistantId("assistant-legacy-001");
        delete.setFileId(fileId);
        service.deleteAssistantKnowledgeFile(delete);

        assertEquals(0, service.listAssistantKnowledgeFiles(listQuery).getTotal());
        assertTrue(repository.assistantKnowledgeFiles.isEmpty());
    }

    @Test
    public void legacyAssistantActionsUsePersistentRepository() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("assistantId", "assistant-legacy-001");
        payload.put("name", "Weather Action");
        payload.put("schema", Collections.singletonMap("type", "object"));
        payload.put("x-legacy", "keep");
        AssistantActionUpsertCommand create = new AssistantActionUpsertCommand();
        create.setUserId("dev-admin");
        create.setOrgId("default-org");
        create.setAssistantId("assistant-legacy-001");
        create.setPayload(payload);

        Map<String, Object> created = service.createLegacyAssistantAction(create);

        String actionId = String.valueOf(created.get("actionId"));
        assertTrue(actionId.startsWith("action-"));
        assertEquals(actionId, created.get("id"));
        assertEquals("Weather Action", created.get("name"));
        assertEquals("keep", created.get("x-legacy"));
        assertEquals(1, repository.assistantActions.size());

        AssistantActionInfoQuery infoQuery = new AssistantActionInfoQuery();
        infoQuery.setUserId("dev-admin");
        infoQuery.setOrgId("default-org");
        infoQuery.setActionId(actionId);
        Map<String, Object> info = service.getLegacyAssistantAction(infoQuery);
        assertEquals(actionId, info.get("actionId"));
        assertEquals("Weather Action", info.get("name"));

        AssistantActionListQuery listQuery = new AssistantActionListQuery();
        listQuery.setUserId("dev-admin");
        listQuery.setOrgId("default-org");
        listQuery.setAssistantId("assistant-legacy-001");
        Map<String, Object> listed = service.listLegacyAssistantActions(listQuery);
        assertEquals(1, listed.get("total"));
        assertEquals(actionId, mapListValue(listed, "list").get(0).get("actionId"));

        Map<String, Object> updatePayload = new LinkedHashMap<String, Object>();
        updatePayload.put("assistantId", "assistant-legacy-001");
        updatePayload.put("actionId", actionId);
        updatePayload.put("actionName", "Weather Action V2");
        updatePayload.put("enabled", Boolean.FALSE);
        AssistantActionUpsertCommand update = new AssistantActionUpsertCommand();
        update.setUserId("dev-admin");
        update.setOrgId("default-org");
        update.setAssistantId("assistant-legacy-001");
        update.setActionId(actionId);
        update.setPayload(updatePayload);

        Map<String, Object> updated = service.updateLegacyAssistantAction(update);

        assertEquals(actionId, updated.get("actionId"));
        assertEquals("Weather Action V2", updated.get("name"));
        assertEquals(Boolean.FALSE, updated.get("enabled"));
        assertEquals(1, repository.assistantActions.size());
        assertEquals("Weather Action V2", repository.assistantActions.get(0).getName());

        AssistantActionDeleteCommand delete = new AssistantActionDeleteCommand();
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");
        delete.setActionId(actionId);
        service.deleteLegacyAssistantAction(delete);

        assertTrue(repository.assistantActions.isEmpty());
        Map<String, Object> fallback = service.getLegacyAssistantAction(infoQuery);
        assertEquals(actionId, fallback.get("actionId"));
        assertEquals("Local Action", fallback.get("name"));
    }

    @Test
    public void generalAgentConfigUsesPersistentRepository() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        Map<String, List<Map<String, Object>>> config =
                new LinkedHashMap<String, List<Map<String, Object>>>();
        config.put("tool", Collections.singletonList(configItem("toolId", "builtin-weather", "toolType", "builtin")));
        config.put("mcp", Collections.singletonList(configItem("id", "mcp-1", "type", "mcp")));
        config.put("ontology", Collections.singletonList(configItem("id", "ontology-1", "type", "ontology")));

        GeneralAgentConfigUpdateCommand update = new GeneralAgentConfigUpdateCommand();
        update.setUserId("dev-admin");
        update.setOrgId("default-org");
        update.setConfig(config);

        Map<String, List<Map<String, Object>>> saved = service.updateGeneralAgentConfig(update);

        assertEquals(1, repository.generalAgentConfigs.size());
        assertEquals("builtin-weather", saved.get("tool").get(0).get("toolId"));
        assertEquals("mcp-1", saved.get("mcp").get(0).get("id"));
        assertFalse(saved.containsKey("ontology"));

        GeneralAgentConfigQuery query = new GeneralAgentConfigQuery();
        query.setUserId("dev-admin");
        query.setOrgId("default-org");
        Map<String, List<Map<String, Object>>> loaded = service.getGeneralAgentConfig(query);

        assertEquals("builtin-weather", loaded.get("tool").get(0).get("toolId"));
        assertEquals("mcp-1", loaded.get("mcp").get(0).get("id"));
        assertTrue(loaded.get("workflow").isEmpty());
        assertFalse(loaded.containsKey("ontology"));
    }

    @Test
    public void generalAgentConversationStateUsesPersistentRepository() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock());

        Map<String, Object> run = new LinkedHashMap<String, Object>();
        run.put("threadId", "wga-thread-1");
        run.put("runId", "wga-run-1");
        run.put("createdAt", 1720000000000L);
        run.put("answer", "hello from WGA");
        run.put("events", Collections.singletonList(
                configItem("type", "RUN_FINISHED", "runId", "wga-run-1")));

        GeneralAgentConversationStateCommand command = new GeneralAgentConversationStateCommand();
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        command.setThreadId("wga-thread-1");
        command.setTitle("WGA Smoke");
        command.setCreatedAt(1720000000000L);
        command.setUpdatedAt(1720000001000L);
        command.setSkillConversation(true);
        command.setSkillId("custom-skill-1");
        command.setPreviewId("wga-preview-1");
        command.setModelConfig(Collections.<String, Object>singletonMap("modelId", "model-1"));
        command.setRuns(Collections.singletonList(run));

        Map<String, Object> saved = service.saveGeneralAgentConversationState(command);

        assertEquals(1, repository.generalAgentConversations.size());
        assertEquals("wga-thread-1", saved.get("threadId"));
        assertEquals(Boolean.TRUE, saved.get("isSkillConversation"));

        GeneralAgentConversationQuery query = new GeneralAgentConversationQuery();
        query.setUserId("dev-admin");
        query.setOrgId("default-org");
        query.setPreviewId("wga-preview-1");
        Map<String, Object> loaded = service.getGeneralAgentConversationState(query);

        assertEquals("WGA Smoke", loaded.get("title"));
        assertEquals("model-1", ((Map<?, ?>) loaded.get("modelConfig")).get("modelId"));
        assertEquals("wga-run-1", mapListValue(loaded, "runs").get(0).get("runId"));

        GeneralAgentConversationListQuery listQuery = new GeneralAgentConversationListQuery();
        listQuery.setUserId("dev-admin");
        listQuery.setOrgId("default-org");
        assertEquals(1, service.listGeneralAgentConversationStates(listQuery).size());

        GeneralAgentConversationDeleteCommand delete = new GeneralAgentConversationDeleteCommand();
        delete.setUserId("dev-admin");
        delete.setOrgId("default-org");
        delete.setThreadId("wga-thread-1");
        service.deleteGeneralAgentConversationState(delete);

        assertTrue(repository.generalAgentConversations.isEmpty());
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
    public void draftStreamPersistsConfiguredModelUpstreamResponse() {
        AppServiceImpl service = new AppServiceImpl(new InMemoryApplicationRepository(), fixedClock());
        AssistantCreateResult created = service.createAssistant(command("UpstreamAgent", "model backed desc"));

        AssistantConversationStreamCommand stream = streamCommand(created.getAssistantId(), "", "hello model", true);
        stream.setOverrideResponse("model upstream answer");
        AssistantConversationStreamResult result = service.streamAssistantConversation(stream);

        assertEquals("model upstream answer", result.getResponse());
        AssistantConversationPageResult details = service.listAssistantConversationDetails(
                conversationDetailQuery(result.getConversationId()));
        assertEquals("model upstream answer", details.getList().get(0).get("response"));
    }

    @Test
    public void assistantDraftStreamUsesConfiguredSafetyTableToBlockSensitivePrompt() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock(), null,
                new FakeSafetyService("table-001", "banned-token", "Safety reply"));
        AssistantCreateResult created = service.createAssistant(command("GuardedAgent", "guarded desc"));

        AssistantConfigUpdateCommand config = new AssistantConfigUpdateCommand();
        config.setAssistantId(created.getAssistantId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setSafetyConfig(safetyConfig("table-001"));
        service.updateAssistantConfig(config);

        AssistantConversationStreamResult result = service.streamAssistantConversation(
                streamCommand(created.getAssistantId(), "", "please say banned-token", true));

        assertEquals("Safety reply", result.getResponse());
        AssistantConversationPageResult details = service.listAssistantConversationDetails(
                conversationDetailQuery(result.getConversationId()));
        assertEquals("Safety reply", details.getList().get(0).get("response"));
    }

    @Test
    public void assistantDraftStreamReplacesSensitiveGeneratedOutputWithTableReply() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock(), null,
                new FakeSafetyService("table-001", "GuardedAgent", "Safety reply"));
        AssistantCreateResult created = service.createAssistant(command("GuardedAgent", "guarded desc"));

        AssistantConfigUpdateCommand config = new AssistantConfigUpdateCommand();
        config.setAssistantId(created.getAssistantId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setSafetyConfig(safetyConfig("table-001"));
        service.updateAssistantConfig(config);

        AssistantConversationStreamResult result = service.streamAssistantConversation(
                streamCommand(created.getAssistantId(), "", "plain question", true));

        assertEquals("Safety reply", result.getResponse());
        AssistantConversationPageResult details = service.listAssistantConversationDetails(
                conversationDetailQuery(result.getConversationId()));
        assertEquals("Safety reply", details.getList().get(0).get("response"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void assistantDraftStreamReturnsConfiguredKnowledgeHits() {
        InMemoryApplicationRepository repository = new InMemoryApplicationRepository();
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        AppServiceImpl service = new AppServiceImpl(repository, fixedClock(), knowledgeService);
        AssistantCreateResult created = service.createAssistant(command("KnowledgeAgent", "knowledge desc"));

        AssistantConfigUpdateCommand config = new AssistantConfigUpdateCommand();
        config.setAssistantId(created.getAssistantId());
        config.setUserId("dev-admin");
        config.setOrgId("default-org");
        config.setKnowledgeBaseConfig(knowledgeConfig("kb-001"));
        service.updateAssistantConfig(config);

        Map<String, Object> hitItem = new LinkedHashMap<>();
        hitItem.put("title", "Guide.txt");
        hitItem.put("knowledgeName", "Guide KB");
        hitItem.put("snippet", "Configured assistant knowledge answer.");
        Map<String, Object> hit = new LinkedHashMap<>();
        hit.put("searchList", Collections.singletonList(hitItem));
        hit.put("score", Collections.singletonList(0.91D));
        hit.put("prompt", "Configured assistant knowledge answer.");
        when(knowledgeService.hitKnowledge(eq("dev-admin"), eq("default-org"), any(Map.class))).thenReturn(hit);

        AssistantConversationStreamResult result = service.streamAssistantConversation(
                streamCommand(created.getAssistantId(), "", "what is configured?", true));

        assertTrue(result.getResponse().contains("Configured assistant knowledge answer."));
        assertEquals(1, result.getSearchList().size());
        assertEquals("Guide.txt", result.getSearchList().get(0).get("title"));
        AssistantConversationPageResult details = service.listAssistantConversationDetails(
                conversationDetailQuery(result.getConversationId()));
        Map<String, Object> detail = details.getList().get(0);
        assertTrue(((String) detail.get("response")).contains("Configured assistant knowledge answer."));
        List<Map<String, Object>> searchList = (List<Map<String, Object>>) detail.get("searchList");
        assertEquals(1, searchList.size());
        assertEquals("Guide.txt", searchList.get(0).get("title"));
        assertEquals("Guide KB", searchList.get(0).get("kb_name"));
        assertEquals(0.91D, searchList.get(0).get("score"));

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(knowledgeService).hitKnowledge(eq("dev-admin"), eq("default-org"), captor.capture());
        Map<String, Object> request = captor.getValue();
        assertEquals("what is configured?", request.get("question"));
        List<Map<String, Object>> knowledgeList = (List<Map<String, Object>>) request.get("knowledgeList");
        assertEquals("kb-001", knowledgeList.get(0).get("knowledgeId"));
        assertEquals(5, ((Map<String, Object>) request.get("knowledgeMatchParams")).get("topK"));
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> mapListValue(Map<String, Object> source, String key) {
        return (List<Map<String, Object>>) source.get(key);
    }

    private Map<String, Object> configItem(String firstKey, Object firstValue, String secondKey, Object secondValue) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put(firstKey, firstValue);
        item.put(secondKey, secondValue);
        return item;
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

    private RecordApiKeyStatisticCommand apiKeyUsage(String apiKeyId,
                                                     String methodPath,
                                                     long callTime,
                                                     String status,
                                                     boolean stream,
                                                     long streamCosts,
                                                     long nonStreamCosts,
                                                     String requestBody) {
        RecordApiKeyStatisticCommand command = new RecordApiKeyStatisticCommand();
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        command.setApiKeyId(apiKeyId);
        command.setMethodPath(methodPath);
        command.setCallTime(callTime);
        command.setHttpStatus(status);
        command.setStream(stream);
        command.setStreamCosts(streamCosts);
        command.setNonStreamCosts(nonStreamCosts);
        command.setRequestBody(requestBody);
        command.setResponseBody("");
        return command;
    }

    private RecordAppStatisticCommand appUsage(String appId,
                                               String appType,
                                               long callTime,
                                               boolean success,
                                               boolean stream,
                                               long streamCosts,
                                               long nonStreamCosts,
                                               String source) {
        RecordAppStatisticCommand command = new RecordAppStatisticCommand();
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        command.setAppId(appId);
        command.setAppType(appType);
        command.setCallTime(callTime);
        command.setSuccess(success);
        command.setStream(stream);
        command.setStreamCosts(streamCosts);
        command.setNonStreamCosts(nonStreamCosts);
        command.setSource(source);
        return command;
    }

    private RecordModelStatisticCommand modelUsage(String modelId,
                                                   String model,
                                                   String provider,
                                                   String modelType,
                                                   long callTime,
                                                   long promptTokens,
                                                   long completionTokens,
                                                   boolean success,
                                                   boolean stream,
                                                   long firstTokenLatency,
                                                   long costs) {
        RecordModelStatisticCommand command = new RecordModelStatisticCommand();
        command.setUserId("dev-admin");
        command.setOrgId("default-org");
        command.setModelId(modelId);
        command.setModel(model);
        command.setProvider(provider);
        command.setModelType(modelType);
        command.setCallTime(callTime);
        command.setPromptTokens(promptTokens);
        command.setCompletionTokens(completionTokens);
        command.setTotalTokens(promptTokens + completionTokens);
        command.setSuccess(success);
        command.setStream(stream);
        command.setFirstTokenLatency(firstTokenLatency);
        command.setCosts(costs);
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

    private Map<String, Object> goProtoKnowledgeConfig(String knowledgeId, int topK) {
        Map<String, Object> knowledge = new LinkedHashMap<>();
        knowledge.put("knowledgeId", knowledgeId);
        knowledge.put("graphSwitch", 1);
        knowledge.put("ragMetaFilter", goProtoMetaFilter());

        Map<String, Object> globalConfig = new LinkedHashMap<>();
        globalConfig.put("maxHistory", 2);
        globalConfig.put("threshold", 0.45);
        globalConfig.put("topK", topK);
        globalConfig.put("matchType", "mix");
        globalConfig.put("keywordPriority", 0.7);
        globalConfig.put("priorityMatch", 1);
        globalConfig.put("semanticsPriority", 0.3);
        globalConfig.put("termWeight", 0.5);
        globalConfig.put("termWeightEnable", true);
        globalConfig.put("useGraph", true);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("perKnowledgeConfigs", Collections.singletonList(knowledge));
        config.put("globalConfig", globalConfig);
        return config;
    }

    private Map<String, Object> goProtoQaKnowledgeConfig(String knowledgeId, int topK) {
        Map<String, Object> knowledge = new LinkedHashMap<>();
        knowledge.put("knowledgeId", knowledgeId);
        knowledge.put("ragMetaFilter", goProtoMetaFilter());

        Map<String, Object> globalConfig = new LinkedHashMap<>();
        globalConfig.put("maxHistory", 1);
        globalConfig.put("threshold", 0.6);
        globalConfig.put("topK", topK);
        globalConfig.put("matchType", "text");
        globalConfig.put("keywordPriority", 1.0);
        globalConfig.put("priorityMatch", 0);
        globalConfig.put("semanticsPriority", 0.0);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("perKnowledgeConfigs", Collections.singletonList(knowledge));
        config.put("globalConfig", globalConfig);
        return config;
    }

    private Map<String, Object> goProtoMetaFilter() {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", "city");
        item.put("type", "string");
        item.put("value", "Beijing");
        item.put("condition", "eq");

        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("filterEnable", true);
        filter.put("filterLogicType", "and");
        filter.put("filterItems", Collections.singletonList(item));
        return filter;
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

    private static class FakeSafetyService implements SafetyService {

        private final String tableId;
        private final String word;
        private final String reply;

        FakeSafetyService(String tableId, String word, String reply) {
            this.tableId = tableId;
            this.word = word;
            this.reply = reply;
        }

        @Override
        public ServiceDescriptor describe() {
            return ServiceDescriptor.of("safety", "Safety Service", "app");
        }

        @Override
        public Map<String, Object> createSensitiveWordTable(String userId, String orgId, Map<String, Object> request) {
            return Collections.emptyMap();
        }

        @Override
        public void updateSensitiveWordTable(String userId, String orgId, Map<String, Object> request) {
        }

        @Override
        public Map<String, Object> getSensitiveWordTable(String userId, String orgId, String tableId) {
            Map<String, Object> table = new LinkedHashMap<>();
            table.put("tableId", tableId);
            table.put("reply", this.tableId.equals(tableId) ? reply : "");
            table.put("version", "v1");
            return table;
        }

        @Override
        public void updateSensitiveWordTableReply(String userId, String orgId, Map<String, Object> request) {
        }

        @Override
        public void deleteSensitiveWordTable(String userId, String orgId, Map<String, Object> request) {
        }

        @Override
        public Map<String, Object> listSensitiveWordTables(String userId, String orgId, String type) {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, Object> selectSensitiveWordTables(String userId, String orgId) {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, Object> listSensitiveWords(String userId, String orgId, String tableId, int pageNo, int pageSize) {
            Map<String, Object> word = new LinkedHashMap<>();
            word.put("word", this.tableId.equals(tableId) ? this.word : "");
            word.put("sensitiveType", "Other");
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("list", Collections.singletonList(word));
            result.put("total", 1);
            return result;
        }

        @Override
        public void uploadSensitiveWord(String userId, String orgId, Map<String, Object> request) {
        }

        @Override
        public void deleteSensitiveWord(String userId, String orgId, Map<String, Object> request) {
        }
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
        private final List<RagChatRecord> ragChats = new ArrayList<>();
        private final List<WorkflowDraftRecord> workflowDrafts = new ArrayList<>();
        private final List<WorkflowSnapshotRecord> workflowSnapshots = new ArrayList<>();
        private final List<WorkflowRunRecord> workflowRuns = new ArrayList<>();
        private final List<AppUrlRecord> appUrls = new ArrayList<>();
        private final List<ApiKeyRecord> apiKeys = new ArrayList<>();
        private final List<ApiKeyUsageAggregateRecord> apiKeyUsageAggregates = new ArrayList<>();
        private final List<ApiKeyUsageRecord> apiKeyUsageRecords = new ArrayList<>();
        private final List<AppStatisticAggregateRecord> appStatisticAggregates = new ArrayList<>();
        private final List<ModelStatisticAggregateRecord> modelStatisticAggregates = new ArrayList<>();
        private final List<AppKeyRecord> appKeys = new ArrayList<>();
        private final List<AppFavoriteRecord> favorites = new ArrayList<>();
        private final List<AppHistoryRecord> histories = new ArrayList<>();
        private final List<AppTemplateRecord> templates = new ArrayList<>();
        private final List<AssistantConversationRecord> conversations = new ArrayList<>();
        private final List<AssistantConversationMessageRecord> messages = new ArrayList<>();
        private final List<AssistantKnowledgeFileRecord> assistantKnowledgeFiles = new ArrayList<>();
        private final List<AssistantActionRecord> assistantActions = new ArrayList<>();
        private final List<GeneralAgentConfigRecord> generalAgentConfigs = new ArrayList<>();
        private final List<GeneralAgentConversationRecord> generalAgentConversations = new ArrayList<>();

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
            deleteAssistantActions(userId, orgId, assistantId);
            deleteAssistantKnowledgeFiles(userId, orgId, assistantId);
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
            List<RagChatRecord> removedChats = new ArrayList<>();
            for (RagChatRecord chat : ragChats) {
                if (ragId.equals(chat.getRagId())) {
                    removedChats.add(chat);
                }
            }
            ragChats.removeAll(removedChats);
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
        public RagChatRecord saveRagChat(RagChatRecord record) {
            record.setId(ids.incrementAndGet());
            ragChats.add(record);
            return record;
        }

        @Override
        public List<RagChatRecord> listRagChats(String userId, String orgId, String ragId, int limit) {
            List<RagChatRecord> matches = new ArrayList<>();
            for (RagChatRecord chat : ragChats) {
                if (userId.equals(chat.getUserId())
                        && orgId.equals(chat.getOrgId())
                        && ragId.equals(chat.getRagId())) {
                    matches.add(chat);
                }
            }
            matches.sort(Comparator.comparing(RagChatRecord::getCreatedAt).reversed());
            int safeLimit = limit <= 0 ? matches.size() : Math.min(limit, matches.size());
            return new ArrayList<>(matches.subList(0, safeLimit));
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
        public AppTemplateRecord saveAppTemplate(AppTemplateRecord record) {
            record.setId(ids.incrementAndGet());
            templates.add(record);
            return record;
        }

        @Override
        public List<AppTemplateRecord> listAppTemplates(String templateType, String category, String name) {
            List<AppTemplateRecord> matches = new ArrayList<>();
            for (AppTemplateRecord record : templates) {
                if (!templateType.equals(record.getTemplateType())) {
                    continue;
                }
                if (category != null && !category.isEmpty() && !"all".equals(category)
                        && !category.equals(record.getCategory())) {
                    continue;
                }
                if (name != null && !name.isEmpty()
                        && (record.getName() == null
                        || !record.getName().toLowerCase().contains(name.toLowerCase()))) {
                    continue;
                }
                matches.add(record);
            }
            return matches;
        }

        @Override
        public AppTemplateRecord findAppTemplate(String templateType, String templateId) {
            for (AppTemplateRecord record : templates) {
                if (templateType.equals(record.getTemplateType()) && templateId.equals(record.getTemplateId())) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public boolean incrementAppTemplateDownload(String templateType, String templateId, long updatedAt) {
            AppTemplateRecord record = findAppTemplate(templateType, templateId);
            if (record == null) {
                return false;
            }
            record.setDownloadCount(record.getDownloadCount() == null ? 1 : record.getDownloadCount() + 1);
            record.setUpdatedAt(updatedAt);
            return true;
        }

        @Override
        public List<AppFavoriteRecord> listAppFavorites(String userId, String appType) {
            List<AppFavoriteRecord> matches = new ArrayList<>();
            for (AppFavoriteRecord favorite : favorites) {
                if (!userId.equals(favorite.getUserId())) {
                    continue;
                }
                if (appType != null && !appType.isEmpty() && !appType.equals(favorite.getAppType())) {
                    continue;
                }
                matches.add(favorite);
            }
            matches.sort(Comparator.comparing(AppFavoriteRecord::getUpdatedAt).reversed());
            return matches;
        }

        @Override
        public void saveAppFavorite(AppFavoriteRecord record) {
            AppFavoriteRecord existing = findFavorite(record.getUserId(), record.getAppId(), record.getAppType());
            if (existing == null) {
                record.setId(ids.incrementAndGet());
                favorites.add(record);
                return;
            }
            existing.setUpdatedAt(record.getUpdatedAt());
        }

        @Override
        public boolean deleteAppFavorite(String userId, String appId, String appType) {
            AppFavoriteRecord existing = findFavorite(userId, appId, appType);
            return existing != null && favorites.remove(existing);
        }

        @Override
        public List<AppHistoryRecord> listAppHistories(String userId, String appType, long startUpdatedAt) {
            List<AppHistoryRecord> matches = new ArrayList<>();
            for (AppHistoryRecord history : histories) {
                if (!userId.equals(history.getUserId())) {
                    continue;
                }
                if (appType != null && !appType.isEmpty() && !appType.equals(history.getAppType())) {
                    continue;
                }
                if (history.getUpdatedAt() == null || history.getUpdatedAt() < startUpdatedAt) {
                    continue;
                }
                matches.add(history);
            }
            matches.sort(Comparator.comparing(AppHistoryRecord::getUpdatedAt).reversed());
            return matches;
        }

        @Override
        public void saveAppHistory(AppHistoryRecord record) {
            AppHistoryRecord existing = findHistory(record.getUserId(), record.getAppId(), record.getAppType());
            if (existing == null) {
                record.setId(ids.incrementAndGet());
                histories.add(record);
                return;
            }
            existing.setUpdatedAt(record.getUpdatedAt());
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
            List<WorkflowRunRecord> removedRuns = new ArrayList<>();
            for (WorkflowRunRecord run : workflowRuns) {
                if (userId.equals(run.getUserId())
                        && orgId.equals(run.getOrgId())
                        && workflowId.equals(run.getWorkflowId())) {
                    removedRuns.add(run);
                }
            }
            workflowRuns.removeAll(removedRuns);
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
        public boolean convertWorkflowAppType(String userId, String orgId, String workflowId,
                                              String oldAppType, String newAppType, long updatedAt) {
            AppRecord existing = findWorkflow(userId, orgId, workflowId, oldAppType);
            if (existing == null) {
                return false;
            }
            existing.setAppType(newAppType);
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
        public WorkflowRunRecord saveWorkflowRun(WorkflowRunRecord record) {
            record.setId(ids.incrementAndGet());
            workflowRuns.add(record);
            return record;
        }

        @Override
        public List<WorkflowRunRecord> listWorkflowRuns(String userId, String orgId, String workflowId, int limit) {
            List<WorkflowRunRecord> matches = new ArrayList<>();
            for (WorkflowRunRecord run : workflowRuns) {
                if (userId.equals(run.getUserId())
                        && orgId.equals(run.getOrgId())
                        && workflowId.equals(run.getWorkflowId())) {
                    matches.add(run);
                }
            }
            matches.sort(Comparator.comparing(WorkflowRunRecord::getCreatedAt).reversed());
            int safeLimit = limit <= 0 ? matches.size() : Math.min(limit, matches.size());
            return new ArrayList<>(matches.subList(0, safeLimit));
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
        public void recordApiKeyUsage(ApiKeyUsageRecord record, ApiKeyUsageAggregateRecord aggregate) {
            record.setId(ids.incrementAndGet());
            apiKeyUsageRecords.add(record);
            ApiKeyUsageAggregateRecord existing = findUsageAggregate(aggregate);
            if (existing == null) {
                aggregate.setId(ids.incrementAndGet());
                apiKeyUsageAggregates.add(aggregate);
            } else {
                addAggregate(existing, aggregate);
            }
        }

        @Override
        public ApiKeyUsageAggregateRecord sumApiKeyUsage(String userId,
                                                         String orgId,
                                                         String startDate,
                                                         String endDate,
                                                         List<String> apiKeyIds,
                                                         List<String> methodPaths) {
            ApiKeyUsageAggregateRecord sum = new ApiKeyUsageAggregateRecord();
            for (ApiKeyUsageAggregateRecord record : apiKeyUsageAggregates) {
                if (matchesUsage(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getApiKeyId(), record.getMethodPath(), userId, orgId, startDate, endDate,
                        apiKeyIds, methodPaths)) {
                    addAggregate(sum, record);
                }
            }
            return sum;
        }

        @Override
        public List<ApiKeyUsageAggregateRecord> listApiKeyUsageTrend(String userId,
                                                                     String orgId,
                                                                     String startDate,
                                                                     String endDate,
                                                                     List<String> apiKeyIds,
                                                                     List<String> methodPaths) {
            Map<String, ApiKeyUsageAggregateRecord> byDate = new LinkedHashMap<>();
            for (ApiKeyUsageAggregateRecord record : apiKeyUsageAggregates) {
                if (!matchesUsage(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getApiKeyId(), record.getMethodPath(), userId, orgId, startDate, endDate,
                        apiKeyIds, methodPaths)) {
                    continue;
                }
                ApiKeyUsageAggregateRecord sum = byDate.get(record.getDate());
                if (sum == null) {
                    sum = new ApiKeyUsageAggregateRecord();
                    sum.setDate(record.getDate());
                    byDate.put(record.getDate(), sum);
                }
                addAggregate(sum, record);
            }
            return new ArrayList<>(byDate.values());
        }

        @Override
        public List<ApiKeyUsageAggregateRecord> listApiKeyUsageAggregates(String userId,
                                                                          String orgId,
                                                                          String startDate,
                                                                          String endDate,
                                                                          List<String> apiKeyIds,
                                                                          List<String> methodPaths,
                                                                          int offset,
                                                                          int limit) {
            List<ApiKeyUsageAggregateRecord> grouped = groupedUsage(userId, orgId, startDate, endDate, apiKeyIds, methodPaths);
            grouped.sort(Comparator.comparing(ApiKeyUsageAggregateRecord::getCallCount).reversed());
            return slice(grouped, offset, limit);
        }

        @Override
        public long countApiKeyUsageAggregates(String userId,
                                               String orgId,
                                               String startDate,
                                               String endDate,
                                               List<String> apiKeyIds,
                                               List<String> methodPaths) {
            return groupedUsage(userId, orgId, startDate, endDate, apiKeyIds, methodPaths).size();
        }

        @Override
        public List<ApiKeyUsageRecord> listApiKeyUsageRecords(String userId,
                                                              String orgId,
                                                              String startDate,
                                                              String endDate,
                                                              List<String> apiKeyIds,
                                                              List<String> methodPaths,
                                                              int offset,
                                                              int limit) {
            List<ApiKeyUsageRecord> matches = new ArrayList<>();
            for (ApiKeyUsageRecord record : apiKeyUsageRecords) {
                if (matchesUsage(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getApiKeyId(), record.getMethodPath(), userId, orgId, startDate, endDate,
                        apiKeyIds, methodPaths)) {
                    matches.add(record);
                }
            }
            matches.sort(Comparator.comparing(ApiKeyUsageRecord::getCallTime).reversed()
                    .thenComparing(Comparator.comparing(ApiKeyUsageRecord::getId).reversed()));
            return slice(matches, offset, limit);
        }

        @Override
        public long countApiKeyUsageRecords(String userId,
                                            String orgId,
                                            String startDate,
                                            String endDate,
                                            List<String> apiKeyIds,
                                            List<String> methodPaths) {
            return listApiKeyUsageRecords(userId, orgId, startDate, endDate, apiKeyIds, methodPaths, 0, Integer.MAX_VALUE).size();
        }

        @Override
        public List<String> listApiKeyUsageMethodPaths(String userId, String orgId) {
            List<String> paths = new ArrayList<>();
            for (ApiKeyUsageAggregateRecord record : apiKeyUsageAggregates) {
                if (userId.equals(record.getUserId())
                        && orgId.equals(record.getOrgId())
                        && !paths.contains(record.getMethodPath())) {
                    paths.add(record.getMethodPath());
                }
            }
            Collections.sort(paths);
            return paths;
        }

        @Override
        public void recordAppStatistic(AppStatisticAggregateRecord aggregate) {
            AppStatisticAggregateRecord existing = findAppStatisticAggregate(aggregate);
            if (existing == null) {
                aggregate.setId(ids.incrementAndGet());
                appStatisticAggregates.add(aggregate);
            } else {
                addAppAggregate(existing, aggregate);
            }
        }

        @Override
        public AppStatisticAggregateRecord sumAppStatistic(String userId,
                                                           String orgId,
                                                           String startDate,
                                                           String endDate,
                                                           List<String> appIds,
                                                           String appType) {
            AppStatisticAggregateRecord sum = new AppStatisticAggregateRecord();
            for (AppStatisticAggregateRecord record : appStatisticAggregates) {
                if (matchesStatistic(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getAppId(), record.getAppType(), userId, orgId, startDate, endDate, appIds, appType)) {
                    addAppAggregate(sum, record);
                }
            }
            return sum;
        }

        @Override
        public List<AppStatisticAggregateRecord> listAppStatisticTrend(String userId,
                                                                       String orgId,
                                                                       String startDate,
                                                                       String endDate,
                                                                       List<String> appIds,
                                                                       String appType) {
            Map<String, AppStatisticAggregateRecord> byDate = new LinkedHashMap<>();
            for (AppStatisticAggregateRecord record : appStatisticAggregates) {
                if (!matchesStatistic(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getAppId(), record.getAppType(), userId, orgId, startDate, endDate, appIds, appType)) {
                    continue;
                }
                AppStatisticAggregateRecord sum = byDate.get(record.getDate());
                if (sum == null) {
                    sum = new AppStatisticAggregateRecord();
                    sum.setDate(record.getDate());
                    byDate.put(record.getDate(), sum);
                }
                addAppAggregate(sum, record);
            }
            return new ArrayList<>(byDate.values());
        }

        @Override
        public List<AppStatisticAggregateRecord> listAppStatisticAggregates(String userId,
                                                                            String orgId,
                                                                            String startDate,
                                                                            String endDate,
                                                                            List<String> appIds,
                                                                            String appType,
                                                                            int offset,
                                                                            int limit) {
            List<AppStatisticAggregateRecord> grouped = groupedAppStatistics(userId, orgId, startDate, endDate, appIds, appType);
            grouped.sort(Comparator.comparing(AppStatisticAggregateRecord::getCallCount).reversed());
            return slice(grouped, offset, limit);
        }

        @Override
        public long countAppStatisticAggregates(String userId,
                                                String orgId,
                                                String startDate,
                                                String endDate,
                                                List<String> appIds,
                                                String appType) {
            return groupedAppStatistics(userId, orgId, startDate, endDate, appIds, appType).size();
        }

        @Override
        public void recordModelStatistic(ModelStatisticAggregateRecord aggregate) {
            ModelStatisticAggregateRecord existing = findModelStatisticAggregate(aggregate);
            if (existing == null) {
                aggregate.setId(ids.incrementAndGet());
                modelStatisticAggregates.add(aggregate);
            } else {
                addModelAggregate(existing, aggregate);
            }
        }

        @Override
        public ModelStatisticAggregateRecord sumModelStatistic(String userId,
                                                               String orgId,
                                                               String startDate,
                                                               String endDate,
                                                               List<String> modelIds,
                                                               String modelType) {
            ModelStatisticAggregateRecord sum = new ModelStatisticAggregateRecord();
            for (ModelStatisticAggregateRecord record : modelStatisticAggregates) {
                if (matchesStatistic(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getModelId(), record.getModelType(), userId, orgId, startDate, endDate, modelIds, modelType)) {
                    addModelAggregate(sum, record);
                }
            }
            return sum;
        }

        @Override
        public List<ModelStatisticAggregateRecord> listModelStatisticTrend(String userId,
                                                                           String orgId,
                                                                           String startDate,
                                                                           String endDate,
                                                                           List<String> modelIds,
                                                                           String modelType) {
            Map<String, ModelStatisticAggregateRecord> byDate = new LinkedHashMap<>();
            for (ModelStatisticAggregateRecord record : modelStatisticAggregates) {
                if (!matchesStatistic(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getModelId(), record.getModelType(), userId, orgId, startDate, endDate, modelIds, modelType)) {
                    continue;
                }
                ModelStatisticAggregateRecord sum = byDate.get(record.getDate());
                if (sum == null) {
                    sum = new ModelStatisticAggregateRecord();
                    sum.setDate(record.getDate());
                    byDate.put(record.getDate(), sum);
                }
                addModelAggregate(sum, record);
            }
            return new ArrayList<>(byDate.values());
        }

        @Override
        public List<ModelStatisticAggregateRecord> listModelStatisticAggregates(String userId,
                                                                                String orgId,
                                                                                String startDate,
                                                                                String endDate,
                                                                                List<String> modelIds,
                                                                                String modelType,
                                                                                int offset,
                                                                                int limit) {
            List<ModelStatisticAggregateRecord> grouped = groupedModelStatistics(
                    userId, orgId, startDate, endDate, modelIds, modelType);
            grouped.sort(Comparator.comparing(ModelStatisticAggregateRecord::getCallCount).reversed());
            return slice(grouped, offset, limit);
        }

        @Override
        public long countModelStatisticAggregates(String userId,
                                                  String orgId,
                                                  String startDate,
                                                  String endDate,
                                                  List<String> modelIds,
                                                  String modelType) {
            return groupedModelStatistics(userId, orgId, startDate, endDate, modelIds, modelType).size();
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

        @Override
        public AssistantKnowledgeFileRecord saveAssistantKnowledgeFile(AssistantKnowledgeFileRecord record) {
            record.setId(ids.incrementAndGet());
            assistantKnowledgeFiles.add(record);
            return record;
        }

        @Override
        public List<AssistantKnowledgeFileRecord> listAssistantKnowledgeFiles(String userId,
                                                                              String orgId,
                                                                              String assistantId) {
            List<AssistantKnowledgeFileRecord> matches = new ArrayList<>();
            for (AssistantKnowledgeFileRecord file : assistantKnowledgeFiles) {
                if (userId.equals(file.getUserId())
                        && orgId.equals(file.getOrgId())
                        && assistantId.equals(file.getAssistantId())) {
                    matches.add(file);
                }
            }
            matches.sort(Comparator.comparing(AssistantKnowledgeFileRecord::getId).reversed());
            return matches;
        }

        @Override
        public long countAssistantKnowledgeFiles(String userId, String orgId, String assistantId) {
            return listAssistantKnowledgeFiles(userId, orgId, assistantId).size();
        }

        @Override
        public boolean deleteAssistantKnowledgeFile(String userId, String orgId, String assistantId, String fileId) {
            AssistantKnowledgeFileRecord found = null;
            for (AssistantKnowledgeFileRecord file : assistantKnowledgeFiles) {
                if (userId.equals(file.getUserId())
                        && orgId.equals(file.getOrgId())
                        && assistantId.equals(file.getAssistantId())
                        && fileId.equals(file.getFileId())) {
                    found = file;
                    break;
                }
            }
            return found != null && assistantKnowledgeFiles.remove(found);
        }

        @Override
        public boolean deleteAssistantKnowledgeFile(String userId, String orgId, String fileId) {
            AssistantKnowledgeFileRecord found = null;
            for (AssistantKnowledgeFileRecord file : assistantKnowledgeFiles) {
                if (userId.equals(file.getUserId())
                        && orgId.equals(file.getOrgId())
                        && fileId.equals(file.getFileId())) {
                    found = file;
                    break;
                }
            }
            return found != null && assistantKnowledgeFiles.remove(found);
        }

        @Override
        public boolean deleteAssistantKnowledgeFiles(String userId, String orgId, String assistantId) {
            List<AssistantKnowledgeFileRecord> removed = new ArrayList<>();
            for (AssistantKnowledgeFileRecord file : assistantKnowledgeFiles) {
                if (userId.equals(file.getUserId())
                        && orgId.equals(file.getOrgId())
                        && assistantId.equals(file.getAssistantId())) {
                    removed.add(file);
                }
            }
            assistantKnowledgeFiles.removeAll(removed);
            return !removed.isEmpty();
        }

        @Override
        public AssistantActionRecord saveAssistantAction(AssistantActionRecord record) {
            AssistantActionRecord existing = findAssistantAction(record.getUserId(), record.getOrgId(), record.getActionId());
            if (existing != null) {
                assistantActions.remove(existing);
                record.setId(existing.getId());
                record.setCreatedAt(existing.getCreatedAt());
            } else {
                record.setId(ids.incrementAndGet());
            }
            assistantActions.add(record);
            return record;
        }

        @Override
        public AssistantActionRecord findAssistantAction(String userId, String orgId, String actionId) {
            for (AssistantActionRecord action : assistantActions) {
                if (userId.equals(action.getUserId())
                        && orgId.equals(action.getOrgId())
                        && actionId.equals(action.getActionId())) {
                    return action;
                }
            }
            return null;
        }

        @Override
        public List<AssistantActionRecord> listAssistantActions(String userId, String orgId, String assistantId) {
            List<AssistantActionRecord> matches = new ArrayList<>();
            for (AssistantActionRecord action : assistantActions) {
                if (userId.equals(action.getUserId())
                        && orgId.equals(action.getOrgId())
                        && (assistantId == null || assistantId.isEmpty()
                        || assistantId.equals(action.getAssistantId()))) {
                    matches.add(action);
                }
            }
            matches.sort(Comparator.comparing(AssistantActionRecord::getId).reversed());
            return matches;
        }

        @Override
        public boolean deleteAssistantAction(String userId, String orgId, String actionId) {
            AssistantActionRecord found = findAssistantAction(userId, orgId, actionId);
            return found != null && assistantActions.remove(found);
        }

        @Override
        public boolean deleteAssistantActions(String userId, String orgId, String assistantId) {
            List<AssistantActionRecord> removed = new ArrayList<>();
            for (AssistantActionRecord action : assistantActions) {
                if (userId.equals(action.getUserId())
                        && orgId.equals(action.getOrgId())
                        && assistantId.equals(action.getAssistantId())) {
                    removed.add(action);
                }
            }
            assistantActions.removeAll(removed);
            return !removed.isEmpty();
        }

        @Override
        public GeneralAgentConfigRecord saveGeneralAgentConfig(GeneralAgentConfigRecord record) {
            GeneralAgentConfigRecord existing = findGeneralAgentConfig(record.getUserId(), record.getOrgId());
            if (existing != null) {
                generalAgentConfigs.remove(existing);
                record.setId(existing.getId());
                record.setCreatedAt(existing.getCreatedAt());
            } else {
                record.setId(ids.incrementAndGet());
            }
            generalAgentConfigs.add(record);
            return record;
        }

        @Override
        public GeneralAgentConfigRecord findGeneralAgentConfig(String userId, String orgId) {
            for (GeneralAgentConfigRecord config : generalAgentConfigs) {
                if (userId.equals(config.getUserId())
                        && orgId.equals(config.getOrgId())) {
                    return config;
                }
            }
            return null;
        }

        @Override
        public GeneralAgentConversationRecord saveGeneralAgentConversation(GeneralAgentConversationRecord record) {
            GeneralAgentConversationRecord existing = findGeneralAgentConversation(
                    record.getUserId(), record.getOrgId(), record.getThreadId());
            if (existing != null) {
                generalAgentConversations.remove(existing);
                record.setId(existing.getId());
                record.setCreatedAt(existing.getCreatedAt());
            } else {
                record.setId(ids.incrementAndGet());
            }
            generalAgentConversations.add(record);
            return record;
        }

        @Override
        public GeneralAgentConversationRecord findGeneralAgentConversation(String userId, String orgId, String threadId) {
            for (GeneralAgentConversationRecord conversation : generalAgentConversations) {
                if (userId.equals(conversation.getUserId())
                        && orgId.equals(conversation.getOrgId())
                        && threadId.equals(conversation.getThreadId())) {
                    return conversation;
                }
            }
            return null;
        }

        @Override
        public GeneralAgentConversationRecord findGeneralAgentConversationByPreview(String userId,
                                                                                   String orgId,
                                                                                   String previewId) {
            for (GeneralAgentConversationRecord conversation : generalAgentConversations) {
                if (userId.equals(conversation.getUserId())
                        && orgId.equals(conversation.getOrgId())
                        && previewId.equals(conversation.getPreviewId())) {
                    return conversation;
                }
            }
            return null;
        }

        @Override
        public List<GeneralAgentConversationRecord> listGeneralAgentConversations(String userId, String orgId) {
            List<GeneralAgentConversationRecord> matches = new ArrayList<>();
            for (GeneralAgentConversationRecord conversation : generalAgentConversations) {
                if (userId.equals(conversation.getUserId())
                        && orgId.equals(conversation.getOrgId())) {
                    matches.add(conversation);
                }
            }
            matches.sort(Comparator.comparing(GeneralAgentConversationRecord::getUpdatedAt).reversed());
            return matches;
        }

        @Override
        public boolean deleteGeneralAgentConversation(String userId, String orgId, String threadId) {
            GeneralAgentConversationRecord found = findGeneralAgentConversation(userId, orgId, threadId);
            return found != null && generalAgentConversations.remove(found);
        }

        private ApiKeyUsageAggregateRecord findUsageAggregate(ApiKeyUsageAggregateRecord target) {
            for (ApiKeyUsageAggregateRecord record : apiKeyUsageAggregates) {
                if (sameUsageKey(record, target)) {
                    return record;
                }
            }
            return null;
        }

        private boolean sameUsageKey(ApiKeyUsageAggregateRecord left, ApiKeyUsageAggregateRecord right) {
            return left.getUserId().equals(right.getUserId())
                    && left.getOrgId().equals(right.getOrgId())
                    && left.getApiKeyId().equals(right.getApiKeyId())
                    && left.getMethodPath().equals(right.getMethodPath())
                    && left.getDate().equals(right.getDate());
        }

        private void addAggregate(ApiKeyUsageAggregateRecord target, ApiKeyUsageAggregateRecord delta) {
            target.setCallCount(target.getCallCount() + delta.getCallCount());
            target.setCallFailure(target.getCallFailure() + delta.getCallFailure());
            target.setStreamCount(target.getStreamCount() + delta.getStreamCount());
            target.setNonStreamCount(target.getNonStreamCount() + delta.getNonStreamCount());
            target.setStreamFailure(target.getStreamFailure() + delta.getStreamFailure());
            target.setNonStreamFailure(target.getNonStreamFailure() + delta.getNonStreamFailure());
            target.setStreamCosts(target.getStreamCosts() + delta.getStreamCosts());
            target.setNonStreamCosts(target.getNonStreamCosts() + delta.getNonStreamCosts());
        }

        private List<ApiKeyUsageAggregateRecord> groupedUsage(String userId,
                                                              String orgId,
                                                              String startDate,
                                                              String endDate,
                                                              List<String> apiKeyIds,
                                                              List<String> methodPaths) {
            Map<String, ApiKeyUsageAggregateRecord> grouped = new LinkedHashMap<>();
            for (ApiKeyUsageAggregateRecord record : apiKeyUsageAggregates) {
                if (!matchesUsage(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getApiKeyId(), record.getMethodPath(), userId, orgId, startDate, endDate,
                        apiKeyIds, methodPaths)) {
                    continue;
                }
                String key = record.getApiKeyId() + "|" + record.getMethodPath();
                ApiKeyUsageAggregateRecord sum = grouped.get(key);
                if (sum == null) {
                    sum = new ApiKeyUsageAggregateRecord();
                    sum.setApiKeyId(record.getApiKeyId());
                    sum.setMethodPath(record.getMethodPath());
                    grouped.put(key, sum);
                }
                addAggregate(sum, record);
            }
            return new ArrayList<>(grouped.values());
        }

        private boolean matchesUsage(String recordUserId,
                                     String recordOrgId,
                                     String recordDate,
                                     String recordApiKeyId,
                                     String recordMethodPath,
                                     String userId,
                                     String orgId,
                                     String startDate,
                                     String endDate,
                                     List<String> apiKeyIds,
                                     List<String> methodPaths) {
            if (!userId.equals(recordUserId) || !orgId.equals(recordOrgId)) {
                return false;
            }
            if (recordDate.compareTo(startDate) < 0 || recordDate.compareTo(endDate) > 0) {
                return false;
            }
            if (apiKeyIds != null && !apiKeyIds.isEmpty() && !apiKeyIds.contains(recordApiKeyId)) {
                return false;
            }
            return methodPaths == null || methodPaths.isEmpty() || methodPaths.contains(recordMethodPath);
        }

        private AppStatisticAggregateRecord findAppStatisticAggregate(AppStatisticAggregateRecord target) {
            for (AppStatisticAggregateRecord record : appStatisticAggregates) {
                if (record.getUserId().equals(target.getUserId())
                        && record.getOrgId().equals(target.getOrgId())
                        && record.getAppId().equals(target.getAppId())
                        && record.getAppType().equals(target.getAppType())
                        && record.getDate().equals(target.getDate())) {
                    return record;
                }
            }
            return null;
        }

        private ModelStatisticAggregateRecord findModelStatisticAggregate(ModelStatisticAggregateRecord target) {
            for (ModelStatisticAggregateRecord record : modelStatisticAggregates) {
                if (record.getUserId().equals(target.getUserId())
                        && record.getOrgId().equals(target.getOrgId())
                        && record.getModelId().equals(target.getModelId())
                        && record.getProvider().equals(target.getProvider())
                        && record.getDate().equals(target.getDate())) {
                    return record;
                }
            }
            return null;
        }

        private AppFavoriteRecord findFavorite(String userId, String appId, String appType) {
            for (AppFavoriteRecord record : favorites) {
                if (userId.equals(record.getUserId())
                        && appId.equals(record.getAppId())
                        && appType.equals(record.getAppType())) {
                    return record;
                }
            }
            return null;
        }

        private AppHistoryRecord findHistory(String userId, String appId, String appType) {
            for (AppHistoryRecord record : histories) {
                if (userId.equals(record.getUserId())
                        && appId.equals(record.getAppId())
                        && appType.equals(record.getAppType())) {
                    return record;
                }
            }
            return null;
        }

        private void addAppAggregate(AppStatisticAggregateRecord target, AppStatisticAggregateRecord delta) {
            target.setCallCount(target.getCallCount() + delta.getCallCount());
            target.setCallFailure(target.getCallFailure() + delta.getCallFailure());
            target.setStreamCount(target.getStreamCount() + delta.getStreamCount());
            target.setStreamFailure(target.getStreamFailure() + delta.getStreamFailure());
            target.setStreamCosts(target.getStreamCosts() + delta.getStreamCosts());
            target.setNonStreamCount(target.getNonStreamCount() + delta.getNonStreamCount());
            target.setNonStreamFailure(target.getNonStreamFailure() + delta.getNonStreamFailure());
            target.setNonStreamCosts(target.getNonStreamCosts() + delta.getNonStreamCosts());
            target.setWebCallCount(target.getWebCallCount() + delta.getWebCallCount());
            target.setWebCallFailure(target.getWebCallFailure() + delta.getWebCallFailure());
            target.setOpenapiCallCount(target.getOpenapiCallCount() + delta.getOpenapiCallCount());
            target.setOpenapiCallFailure(target.getOpenapiCallFailure() + delta.getOpenapiCallFailure());
            target.setWebUrlCallCount(target.getWebUrlCallCount() + delta.getWebUrlCallCount());
            target.setWebUrlCallFailure(target.getWebUrlCallFailure() + delta.getWebUrlCallFailure());
        }

        private void addModelAggregate(ModelStatisticAggregateRecord target, ModelStatisticAggregateRecord delta) {
            target.setPromptTokens(target.getPromptTokens() + delta.getPromptTokens());
            target.setCompletionTokens(target.getCompletionTokens() + delta.getCompletionTokens());
            target.setTotalTokens(target.getTotalTokens() + delta.getTotalTokens());
            target.setFirstTokenLatency(target.getFirstTokenLatency() + delta.getFirstTokenLatency());
            target.setCosts(target.getCosts() + delta.getCosts());
            target.setCallCount(target.getCallCount() + delta.getCallCount());
            target.setStreamCount(target.getStreamCount() + delta.getStreamCount());
            target.setNonStreamCount(target.getNonStreamCount() + delta.getNonStreamCount());
            target.setCallFailure(target.getCallFailure() + delta.getCallFailure());
            target.setStreamFailure(target.getStreamFailure() + delta.getStreamFailure());
            target.setNonStreamFailure(target.getNonStreamFailure() + delta.getNonStreamFailure());
        }

        private List<AppStatisticAggregateRecord> groupedAppStatistics(String userId,
                                                                       String orgId,
                                                                       String startDate,
                                                                       String endDate,
                                                                       List<String> appIds,
                                                                       String appType) {
            Map<String, AppStatisticAggregateRecord> grouped = new LinkedHashMap<>();
            for (AppStatisticAggregateRecord record : appStatisticAggregates) {
                if (!matchesStatistic(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getAppId(), record.getAppType(), userId, orgId, startDate, endDate, appIds, appType)) {
                    continue;
                }
                AppStatisticAggregateRecord sum = grouped.get(record.getAppId());
                if (sum == null) {
                    sum = new AppStatisticAggregateRecord();
                    sum.setAppId(record.getAppId());
                    sum.setAppType(record.getAppType());
                    sum.setOrgId(record.getOrgId());
                    grouped.put(record.getAppId(), sum);
                }
                addAppAggregate(sum, record);
            }
            return new ArrayList<>(grouped.values());
        }

        private List<ModelStatisticAggregateRecord> groupedModelStatistics(String userId,
                                                                           String orgId,
                                                                           String startDate,
                                                                           String endDate,
                                                                           List<String> modelIds,
                                                                           String modelType) {
            Map<String, ModelStatisticAggregateRecord> grouped = new LinkedHashMap<>();
            for (ModelStatisticAggregateRecord record : modelStatisticAggregates) {
                if (!matchesStatistic(record.getUserId(), record.getOrgId(), record.getDate(),
                        record.getModelId(), record.getModelType(), userId, orgId, startDate, endDate, modelIds, modelType)) {
                    continue;
                }
                ModelStatisticAggregateRecord sum = grouped.get(record.getModelId());
                if (sum == null) {
                    sum = new ModelStatisticAggregateRecord();
                    sum.setModelId(record.getModelId());
                    sum.setModel(record.getModel());
                    sum.setProvider(record.getProvider());
                    sum.setOrgId(record.getOrgId());
                    grouped.put(record.getModelId(), sum);
                }
                addModelAggregate(sum, record);
            }
            return new ArrayList<>(grouped.values());
        }

        private boolean matchesStatistic(String recordUserId,
                                         String recordOrgId,
                                         String recordDate,
                                         String recordId,
                                         String recordType,
                                         String userId,
                                         String orgId,
                                         String startDate,
                                         String endDate,
                                         List<String> ids,
                                         String type) {
            if (!userId.equals(recordUserId) || !orgId.equals(recordOrgId)) {
                return false;
            }
            if (recordDate.compareTo(startDate) < 0 || recordDate.compareTo(endDate) > 0) {
                return false;
            }
            if (type != null && !type.isEmpty() && !type.equals(recordType)) {
                return false;
            }
            return ids == null || ids.isEmpty() || ids.contains(recordId);
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
