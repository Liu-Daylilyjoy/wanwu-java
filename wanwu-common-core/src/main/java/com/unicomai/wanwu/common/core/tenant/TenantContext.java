package com.unicomai.wanwu.common.core.tenant;

public final class TenantContext {

    public static final String HEADER = "X-Tenant-Id";

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<String>();

    private TenantContext() {
    }

    public static String currentTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            CURRENT_TENANT.remove();
        } else {
            CURRENT_TENANT.set(tenantId);
        }
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
