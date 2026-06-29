package com.unicomai.wanwu.service.app.rpc;

import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-06-29T02:00:00Z"), ZoneOffset.UTC);
    }

    private static class InMemoryApplicationRepository implements ApplicationRepository {

        private final AtomicLong ids = new AtomicLong();
        private final List<AppRecord> records = new ArrayList<>();

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

        private boolean containsApp(String assistantId) {
            for (AppRecord record : records) {
                if (assistantId.equals(record.getAppId())) {
                    return true;
                }
            }
            return false;
        }
    }
}
