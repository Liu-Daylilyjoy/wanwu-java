package com.unicomai.wanwu.service.app.rpc;

import com.unicomai.wanwu.service.app.persistence.entity.SafetyRecordEntity;
import com.unicomai.wanwu.service.app.persistence.mapper.SafetyRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SafetyServiceImplTest {

    private static final String USER_ID = "dev-admin";
    private static final String ORG_ID = "default-org";

    private SafetyServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new SafetyServiceImpl();
    }

    @Test
    public void sensitiveWordTableLifecycleMatchesFrontendContract() {
        String tableId = text(service.createSensitiveWordTable(USER_ID, ORG_ID,
                map("tableName", "Policy Guard", "remark", "policy words", "type", "personal")), "tableId");
        assertFalse(tableId.isEmpty());

        Map<String, Object> detail = service.getSensitiveWordTable(USER_ID, ORG_ID, tableId);
        assertEquals("Policy Guard", text(detail, "tableName"));
        assertEquals("personal", text(detail, "type"));
        assertEquals(tableId, text(first(service.selectSensitiveWordTables(USER_ID, ORG_ID)), "tableId"));

        service.updateSensitiveWordTable(USER_ID, ORG_ID,
                map("tableId", tableId, "tableName", "Policy Guard Updated", "remark", "updated"));
        assertEquals("Policy Guard Updated", text(service.getSensitiveWordTable(USER_ID, ORG_ID, tableId),
                "tableName"));

        service.updateSensitiveWordTableReply(USER_ID, ORG_ID, map("tableId", tableId, "reply", "blocked"));
        assertEquals("blocked", text(service.getSensitiveWordTable(USER_ID, ORG_ID, tableId), "reply"));

        service.uploadSensitiveWord(USER_ID, ORG_ID,
                map("tableId", tableId, "importType", "single", "word", "blocked", "sensitiveType", "Other"));
        Map<String, Object> words = service.listSensitiveWords(USER_ID, ORG_ID, tableId, 1, 10);
        assertEquals(1, words.get("total"));
        Map<String, Object> word = list(words.get("list")).get(0);
        assertEquals("blocked", text(word, "word"));

        service.deleteSensitiveWord(USER_ID, ORG_ID, map("tableId", tableId, "wordId", text(word, "wordId")));
        assertEquals(0, service.listSensitiveWords(USER_ID, ORG_ID, tableId, 1, 10).get("total"));

        service.deleteSensitiveWordTable(USER_ID, ORG_ID, map("tableId", tableId));
        assertEquals(0, service.listSensitiveWordTables(USER_ID, ORG_ID, "personal").get("total"));
    }

    @Test
    public void sensitiveFileContentImportParsesMultipleWords() {
        String tableId = text(service.createSensitiveWordTable(USER_ID, ORG_ID,
                map("tableName", "Import Guard", "remark", "import", "type", "personal")), "tableId");

        service.uploadSensitiveWord(USER_ID, ORG_ID,
                map("tableId", tableId, "importType", "file",
                        "content", "Political,Illegal,Other\nalpha,beta,loose\nalpha,,"));

        Map<String, Object> words = service.listSensitiveWords(USER_ID, ORG_ID, tableId, 1, 10);
        assertEquals(3, words.get("total"));
        List<Map<String, Object>> list = list(words.get("list"));
        assertEquals("alpha", text(list.get(0), "word"));
        assertEquals("Political", text(list.get(0), "sensitiveType"));
        assertEquals("beta", text(list.get(1), "word"));
        assertEquals("Illegal", text(list.get(1), "sensitiveType"));
        assertEquals("loose", text(list.get(2), "word"));
        assertEquals("Other", text(list.get(2), "sensitiveType"));
    }

    @Test
    public void sensitiveStateIsPersistedAsSnapshotRecord() {
        SafetyRecordMapper mapper = mock(SafetyRecordMapper.class);
        service = new SafetyServiceImpl(mapper);

        String tableId = text(service.createSensitiveWordTable(USER_ID, ORG_ID,
                map("tableName", "Persist Guard", "remark", "persist", "type", "personal")), "tableId");
        service.updateSensitiveWordTableReply(USER_ID, ORG_ID, map("tableId", tableId, "reply", "blocked"));
        service.uploadSensitiveWord(USER_ID, ORG_ID,
                map("tableId", tableId, "importType", "single", "word", "secret", "sensitiveType", "Policy"));

        ArgumentCaptor<SafetyRecordEntity> captor = ArgumentCaptor.forClass(SafetyRecordEntity.class);
        verify(mapper, atLeast(3)).upsertRecord(captor.capture());
        SafetyRecordEntity saved = captor.getValue();
        assertEquals("snapshot", saved.getRecordType());
        assertEquals("state", saved.getRecordId());
        assertFalse(saved.getPayload().isEmpty());
        assertFalse(saved.getPayload().indexOf("Persist Guard") < 0);
        assertFalse(saved.getPayload().indexOf("secret") < 0);
    }

    @Test
    public void persistedSnapshotIsLoadedAndWordSequenceContinuesAfterRestart() {
        SafetyRecordMapper sourceMapper = mock(SafetyRecordMapper.class);
        service = new SafetyServiceImpl(sourceMapper);

        String tableId = text(service.createSensitiveWordTable(USER_ID, ORG_ID,
                map("tableName", "Restart Guard", "remark", "restart", "type", "personal")), "tableId");
        service.uploadSensitiveWord(USER_ID, ORG_ID,
                map("tableId", tableId, "importType", "single", "word", "alpha", "sensitiveType", "Policy"));

        ArgumentCaptor<SafetyRecordEntity> captor = ArgumentCaptor.forClass(SafetyRecordEntity.class);
        verify(sourceMapper, atLeast(2)).upsertRecord(captor.capture());
        String payload = captor.getValue().getPayload();

        SafetyRecordMapper restartMapper = mock(SafetyRecordMapper.class);
        when(restartMapper.selectByType(anyString())).thenReturn(Collections.singletonList(record(payload)));
        SafetyServiceImpl restarted = new SafetyServiceImpl(restartMapper);

        Map<String, Object> detail = restarted.getSensitiveWordTable(USER_ID, ORG_ID, tableId);
        assertEquals("Restart Guard", text(detail, "tableName"));
        Map<String, Object> words = restarted.listSensitiveWords(USER_ID, ORG_ID, tableId, 1, 10);
        assertEquals(1, words.get("total"));
        assertEquals("word-1", text(list(words.get("list")).get(0), "wordId"));

        restarted.uploadSensitiveWord(USER_ID, ORG_ID,
                map("tableId", tableId, "importType", "single", "word", "beta", "sensitiveType", "Policy"));
        List<Map<String, Object>> after = list(restarted.listSensitiveWords(USER_ID, ORG_ID, tableId, 1, 10)
                .get("list"));
        assertEquals("word-2", text(after.get(1), "wordId"));
        verify(restartMapper, atLeast(1)).upsertRecord(org.mockito.ArgumentMatchers.any(SafetyRecordEntity.class));
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }

    private Map<String, Object> first(Map<String, Object> listResult) {
        return list(listResult.get("list")).get(0);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        return (List<Map<String, Object>>) value;
    }

    private String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private SafetyRecordEntity record(String payload) {
        SafetyRecordEntity entity = new SafetyRecordEntity();
        entity.setRecordType("snapshot");
        entity.setRecordId("state");
        entity.setPayload(payload);
        entity.setCreatedAt(1L);
        entity.setUpdatedAt(1L);
        return entity;
    }
}
