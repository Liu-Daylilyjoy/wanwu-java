package com.unicomai.wanwu.service.app.rpc;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.safety.SafetyService;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboService;

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

    private static final String DEFAULT_ORG = "default-org";
    private static final String TYPE_PERSONAL = "personal";
    private static final String DEFAULT_REPLY = "Your request contains sensitive content.";

    private final ConcurrentMap<String, Map<String, Object>> tables = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<Map<String, Object>>> words = new ConcurrentHashMap<>();
    private final AtomicInteger wordSequence = new AtomicInteger(1);

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
        return singleton("tableId", tableId);
    }

    @Override
    public void updateSensitiveWordTable(String userId, String orgId, Map<String, Object> request) {
        String tableId = text(request, "tableId");
        Map<String, Object> current = requireTable(orgId, tableId);
        current.put("tableName", defaultText(request, "tableName", text(current, "tableName")));
        current.put("remark", defaultText(request, "remark", text(current, "remark")));
    }

    @Override
    public Map<String, Object> getSensitiveWordTable(String userId, String orgId, String tableId) {
        return tableDetail(requireTable(orgId, tableId));
    }

    @Override
    public void updateSensitiveWordTableReply(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> current = requireTable(orgId, text(request, "tableId"));
        current.put("reply", defaultText(request, "reply", DEFAULT_REPLY));
    }

    @Override
    public void deleteSensitiveWordTable(String userId, String orgId, Map<String, Object> request) {
        String tableId = text(request, "tableId");
        tables.remove(scoped(orgId, tableId));
        words.remove(scoped(orgId, tableId));
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
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("wordId", "word-" + wordSequence.getAndIncrement());
        row.put("word", defaultText(request, "word", defaultText(request, "fileName", "imported-sensitive-word")));
        row.put("sensitiveType", defaultText(request, "sensitiveType", "Other"));
        wordsFor(orgId, tableId).add(row);
    }

    @Override
    public void deleteSensitiveWord(String userId, String orgId, Map<String, Object> request) {
        String tableId = text(request, "tableId");
        requireTable(orgId, tableId);
        String wordId = text(request, "wordId");
        wordsFor(orgId, tableId).removeIf(item -> wordId.equals(text(item, "wordId")));
    }

    private Map<String, Object> tableBase(Map<String, Object> request) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("tableName", defaultText(request, "tableName", "Sensitive Table"));
        item.put("remark", defaultText(request, "remark", ""));
        item.put("reply", defaultText(request, "reply", DEFAULT_REPLY));
        item.put("type", defaultText(request, "type", TYPE_PERSONAL));
        return item;
    }

    private Map<String, Object> tableDetail(Map<String, Object> item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("tableId", item.get("tableId"));
        row.put("tableName", item.get("tableName"));
        row.put("remark", item.get("remark"));
        row.put("reply", item.get("reply"));
        row.put("createdAt", item.get("createdAt"));
        row.put("type", item.get("type"));
        return row;
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
}
