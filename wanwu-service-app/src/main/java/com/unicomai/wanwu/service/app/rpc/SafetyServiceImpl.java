package com.unicomai.wanwu.service.app.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.safety.SafetyService;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import com.unicomai.wanwu.service.app.persistence.entity.SafetyRecordEntity;
import com.unicomai.wanwu.service.app.persistence.mapper.SafetyRecordMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class SafetyServiceImpl implements SafetyService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String DEFAULT_ORG = "default-org";
    private static final String TYPE_PERSONAL = "personal";
    private static final String DEFAULT_REPLY = "Your request contains sensitive content.";
    private static final String TYPE_SNAPSHOT = "snapshot";
    private static final String SNAPSHOT_ID = "state";
    private static final int MAX_WORDS = 100;

    private final ConcurrentMap<String, Map<String, Object>> tables = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<Map<String, Object>>> words = new ConcurrentHashMap<>();
    private final AtomicInteger wordSequence = new AtomicInteger(1);

    @Autowired(required = false)
    private SafetyRecordMapper safetyRecordMapper;

    public SafetyServiceImpl() {
    }

    SafetyServiceImpl(SafetyRecordMapper safetyRecordMapper) {
        this.safetyRecordMapper = safetyRecordMapper;
        loadPersistedSnapshot();
    }

    @PostConstruct
    synchronized void loadPersistedSnapshot() {
        if (safetyRecordMapper == null) {
            return;
        }
        List<SafetyRecordEntity> records = safetyRecordMapper.selectByType(TYPE_SNAPSHOT);
        if (records == null || records.isEmpty()) {
            return;
        }
        SafetyRecordEntity record = records.get(records.size() - 1);
        if (record.getPayload() == null || record.getPayload().trim().isEmpty()) {
            return;
        }
        try {
            SafetySnapshot snapshot = JSON.readValue(record.getPayload(), SafetySnapshot.class);
            applySnapshot(snapshot);
        } catch (Exception ignored) {
            // A malformed development snapshot must not prevent the service from starting.
        }
    }

    @Override
    public ServiceDescriptor describe() {
        return ServiceDescriptor.of(ServiceNames.APP, "Safety Service", "app");
    }

    @Override
    public Map<String, Object> createSensitiveWordTable(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> item = tableBase(request);
        String tableId = id("table");
        item.put("tableId", tableId);
        item.put("ownerUserId", userId);
        item.put("ownerOrgId", org(orgId));
        item.put("createdAt", now());
        tables.put(scoped(orgId, tableId), item);
        words.put(scoped(orgId, tableId), Collections.synchronizedList(new ArrayList<Map<String, Object>>()));
        saveSnapshot();
        return singleton("tableId", tableId);
    }

    @Override
    public void updateSensitiveWordTable(String userId, String orgId, Map<String, Object> request) {
        String tableId = text(request, "tableId");
        Map<String, Object> current = requireTable(orgId, tableId);
        current.put("tableName", defaultText(request, "tableName", text(current, "tableName")));
        current.put("remark", defaultText(request, "remark", text(current, "remark")));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> getSensitiveWordTable(String userId, String orgId, String tableId) {
        return tableDetail(requireTable(orgId, tableId));
    }

    @Override
    public void updateSensitiveWordTableReply(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> current = requireTable(orgId, text(request, "tableId"));
        current.put("reply", defaultText(request, "reply", DEFAULT_REPLY));
        current.put("version", version());
        saveSnapshot();
    }

    @Override
    public void deleteSensitiveWordTable(String userId, String orgId, Map<String, Object> request) {
        String tableId = text(request, "tableId");
        tables.remove(scoped(orgId, tableId));
        words.remove(scoped(orgId, tableId));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listSensitiveWordTables(String userId, String orgId, String type) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : tables.values()) {
            if (sameOrg(item, orgId) && (blank(type) || type.equals(text(item, "type")))) {
                list.add(tableDetail(item));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> selectSensitiveWordTables(String userId, String orgId) {
        return listSensitiveWordTables(userId, orgId, TYPE_PERSONAL);
    }

    @Override
    public Map<String, Object> listSensitiveWords(String userId, String orgId, String tableId, int pageNo,
                                                  int pageSize) {
        requireTable(orgId, tableId);
        List<Map<String, Object>> all = copyList(wordsFor(orgId, tableId));
        int safePageNo = pageNo <= 0 ? 1 : pageNo;
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int from = Math.min((safePageNo - 1) * safePageSize, all.size());
        int to = Math.min(from + safePageSize, all.size());

        Map<String, Object> result = listResult(new ArrayList<>(all.subList(from, to)));
        result.put("pageNo", safePageNo);
        result.put("pageSize", safePageSize);
        result.put("total", all.size());
        return result;
    }

    @Override
    public void uploadSensitiveWord(String userId, String orgId, Map<String, Object> request) {
        String tableId = text(request, "tableId");
        requireTable(orgId, tableId);
        List<Map<String, Object>> currentWords = wordsFor(orgId, tableId);
        synchronized (currentWords) {
            if (currentWords.size() >= MAX_WORDS) {
                throw new IllegalArgumentException("sensitive word table is full: " + tableId);
            }
            String word = defaultText(request, "word", defaultText(request, "fileName", "imported-sensitive-word"));
            for (Map<String, Object> item : currentWords) {
                if (word.equals(text(item, "word"))) {
                    return;
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("wordId", "word-" + wordSequence.getAndIncrement());
            row.put("word", word);
            row.put("sensitiveType", defaultText(request, "sensitiveType", "Other"));
            currentWords.add(row);
        }
        touchTableVersion(orgId, tableId);
        saveSnapshot();
    }

    @Override
    public void deleteSensitiveWord(String userId, String orgId, Map<String, Object> request) {
        String tableId = text(request, "tableId");
        requireTable(orgId, tableId);
        String wordId = text(request, "wordId");
        wordsFor(orgId, tableId).removeIf(item -> wordId.equals(text(item, "wordId")));
        touchTableVersion(orgId, tableId);
        saveSnapshot();
    }

    private Map<String, Object> tableBase(Map<String, Object> request) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("tableName", defaultText(request, "tableName", "Sensitive Table"));
        item.put("remark", defaultText(request, "remark", ""));
        item.put("reply", defaultText(request, "reply", DEFAULT_REPLY));
        item.put("type", defaultText(request, "type", TYPE_PERSONAL));
        item.put("version", version());
        return item;
    }

    private Map<String, Object> tableDetail(Map<String, Object> item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("tableId", item.get("tableId"));
        row.put("tableName", item.get("tableName"));
        row.put("remark", item.get("remark"));
        row.put("reply", item.get("reply"));
        row.put("version", item.get("version"));
        row.put("createdAt", item.get("createdAt"));
        row.put("type", item.get("type"));
        return row;
    }

    private void touchTableVersion(String orgId, String tableId) {
        requireTable(orgId, tableId).put("version", version());
    }

    private Map<String, Object> requireTable(String orgId, String tableId) {
        Map<String, Object> item = tables.get(scoped(orgId, tableId));
        if (item == null) {
            throw new IllegalArgumentException("sensitive word table not found: " + tableId);
        }
        return item;
    }

    private List<Map<String, Object>> wordsFor(String orgId, String tableId) {
        String key = scoped(orgId, tableId);
        List<Map<String, Object>> current = words.get(key);
        if (current != null) {
            return current;
        }
        List<Map<String, Object>> created = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
        List<Map<String, Object>> raced = words.putIfAbsent(key, created);
        return raced == null ? created : raced;
    }

    private Map<String, Object> listResult(List<Map<String, Object>> list) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return result;
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    private List<Map<String, Object>> copyList(List<Map<String, Object>> source) {
        List<Map<String, Object>> result = new ArrayList<>();
        synchronized (source) {
            for (Map<String, Object> item : source) {
                result.add(new LinkedHashMap<>(item));
            }
        }
        return result;
    }

    private boolean sameOrg(Map<String, Object> item, String orgId) {
        return org(orgId).equals(text(item, "ownerOrgId"));
    }

    private String scoped(String orgId, String id) {
        return org(orgId) + ":" + id;
    }

    private String org(String orgId) {
        return blank(orgId) ? DEFAULT_ORG : orgId;
    }

    private String id(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private String version() {
        return String.valueOf(System.currentTimeMillis());
    }

    private synchronized void saveSnapshot() {
        if (safetyRecordMapper == null) {
            return;
        }
        SafetyRecordEntity entity = new SafetyRecordEntity();
        long now = System.currentTimeMillis();
        entity.setRecordType(TYPE_SNAPSHOT);
        entity.setRecordId(SNAPSHOT_ID);
        try {
            entity.setPayload(JSON.writeValueAsString(snapshot()));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to persist safety snapshot", ex);
        }
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        safetyRecordMapper.upsertRecord(entity);
    }

    private SafetySnapshot snapshot() {
        SafetySnapshot snapshot = new SafetySnapshot();
        snapshot.tables = new LinkedHashMap<>(tables);
        snapshot.words = new LinkedHashMap<>(words);
        snapshot.wordSequence = wordSequence.get();
        return snapshot;
    }

    private void applySnapshot(SafetySnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        restoreTables(snapshot.tables);
        restoreWords(snapshot.words);
        int next = snapshot.wordSequence == null ? 1 : snapshot.wordSequence;
        next = Math.max(next, nextWordSequence(snapshot.words));
        wordSequence.set(Math.max(1, next));
    }

    private void restoreTables(Map<String, Map<String, Object>> source) {
        tables.clear();
        if (source == null) {
            return;
        }
        for (Map.Entry<String, Map<String, Object>> entry : source.entrySet()) {
            tables.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
        }
    }

    private void restoreWords(Map<String, List<Map<String, Object>>> source) {
        words.clear();
        if (source == null) {
            return;
        }
        for (Map.Entry<String, List<Map<String, Object>>> entry : source.entrySet()) {
            List<Map<String, Object>> rows = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
            for (Map<String, Object> item : entry.getValue()) {
                rows.add(new LinkedHashMap<>(item));
            }
            words.put(entry.getKey(), rows);
        }
    }

    private int nextWordSequence(Map<String, List<Map<String, Object>>> source) {
        int next = 1;
        if (source == null) {
            return next;
        }
        for (List<Map<String, Object>> rows : source.values()) {
            for (Map<String, Object> item : rows) {
                String wordId = text(item, "wordId");
                if (wordId.startsWith("word-")) {
                    try {
                        next = Math.max(next, Integer.parseInt(wordId.substring("word-".length())) + 1);
                    } catch (NumberFormatException ignored) {
                        // Non-numeric development IDs do not affect the generated sequence.
                    }
                }
            }
        }
        return next;
    }

    private String defaultText(Map<String, Object> map, String key, String fallback) {
        String value = text(map, key);
        return blank(value) ? fallback : value;
    }

    private String text(Map<String, Object> map, String key) {
        if (map == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class SafetySnapshot {
        public Map<String, Map<String, Object>> tables = Collections.emptyMap();
        public Map<String, List<Map<String, Object>>> words = Collections.emptyMap();
        public Integer wordSequence = 1;
    }
}
