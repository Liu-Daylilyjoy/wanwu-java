package com.unicomai.wanwu.service.bff.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ExplorationAppHistoryStore {

    static final ExplorationAppHistoryStore INSTANCE = new ExplorationAppHistoryStore();

    private final Map<String, Map<String, HistoryEntry>> entriesByUser =
            new ConcurrentHashMap<String, Map<String, HistoryEntry>>();

    private ExplorationAppHistoryStore() {
    }

    void record(String userId, String appType, String appId) {
        if (isBlank(userId) || isBlank(appType) || isBlank(appId)) {
            return;
        }
        String key = appKey(appType, appId);
        Map<String, HistoryEntry> entries = entries(userId);
        HistoryEntry current = entries.get(key);
        long now = System.currentTimeMillis();
        if (current == null) {
            current = new HistoryEntry(appType.trim(), appId.trim(), now, now);
        } else {
            current = new HistoryEntry(current.getAppType(), current.getAppId(), current.getCreatedAt(), now);
        }
        entries.put(key, current);
    }

    List<HistoryEntry> list(String userId, String appType) {
        if (isBlank(userId)) {
            return Collections.emptyList();
        }
        Map<String, HistoryEntry> entries = entriesByUser.get(userId);
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<HistoryEntry> result = new ArrayList<HistoryEntry>();
        for (HistoryEntry entry : entries.values()) {
            if (isBlank(appType) || appType.equals(entry.getAppType())) {
                result.add(entry);
            }
        }
        Collections.sort(result, new Comparator<HistoryEntry>() {
            @Override
            public int compare(HistoryEntry left, HistoryEntry right) {
                return Long.valueOf(right.getUpdatedAt()).compareTo(left.getUpdatedAt());
            }
        });
        return result;
    }

    void clear() {
        entriesByUser.clear();
    }

    private Map<String, HistoryEntry> entries(String userId) {
        Map<String, HistoryEntry> entries = entriesByUser.get(userId);
        if (entries == null) {
            Map<String, HistoryEntry> created = new ConcurrentHashMap<String, HistoryEntry>();
            Map<String, HistoryEntry> existing = entriesByUser.putIfAbsent(userId, created);
            entries = existing == null ? created : existing;
        }
        return entries;
    }

    static String appKey(String appType, String appId) {
        return safe(appType) + ":" + safe(appId);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static final class HistoryEntry {
        private final String appType;
        private final String appId;
        private final long createdAt;
        private final long updatedAt;

        private HistoryEntry(String appType, String appId, long createdAt, long updatedAt) {
            this.appType = appType;
            this.appId = appId;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        String getAppType() {
            return appType;
        }

        String getAppId() {
            return appId;
        }

        long getCreatedAt() {
            return createdAt;
        }

        long getUpdatedAt() {
            return updatedAt;
        }

        String createdAtText() {
            return format(createdAt);
        }

        String updatedAtText() {
            return format(updatedAt);
        }

        private static String format(long millis) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(millis));
        }
    }
}
