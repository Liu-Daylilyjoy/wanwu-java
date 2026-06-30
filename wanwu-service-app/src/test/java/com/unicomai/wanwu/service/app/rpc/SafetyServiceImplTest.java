package com.unicomai.wanwu.service.app.rpc;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SafetyServiceImplTest {

    private static final String USER_ID = "dev-admin";
    private static final String ORG_ID = "default-org";

    private final SafetyServiceImpl service = new SafetyServiceImpl();

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
}
