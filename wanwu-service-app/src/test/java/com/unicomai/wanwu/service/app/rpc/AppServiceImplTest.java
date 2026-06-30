package com.unicomai.wanwu.service.app.rpc;

import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCopyCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
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
        assertEquals("private", item.get("publishType"));
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
        assertEquals("private", draft.get("publishType"));
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
