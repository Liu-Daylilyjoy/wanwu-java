package com.unicomai.wanwu.common.core.model;

import java.util.UUID;

public final class TraceIds {

    public static final String HEADER = "X-Trace-Id";

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<String>();

    private TraceIds() {
    }

    public static String current() {
        String traceId = CURRENT.get();
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString();
            CURRENT.set(traceId);
        }
        return traceId;
    }

    public static void set(String traceId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            CURRENT.remove();
        } else {
            CURRENT.set(traceId);
        }
    }

    public static void clear() {
        CURRENT.remove();
    }
}
