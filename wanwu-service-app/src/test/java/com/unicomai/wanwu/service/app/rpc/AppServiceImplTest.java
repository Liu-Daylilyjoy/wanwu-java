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
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.AppVersionRollbackCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.service.app.domain.AssistantConversationMessageRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationRecord;
import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.AssistantSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    private static class InMemoryApplicationRepository implements ApplicationRepository {

        private final AtomicLong ids = new AtomicLong();
        private final List<AppRecord> records = new ArrayList<>();
        private final List<AssistantDraftConfigRecord> configs = new ArrayList<>();
        private final List<AssistantSnapshotRecord> snapshots = new ArrayList<>();
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
