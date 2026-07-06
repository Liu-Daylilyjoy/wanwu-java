package com.unicomai.wanwu.service.bff.web;

final class BffUserContextResolver {

    static final String DEV_ADMIN_TOKEN = "dev-token";
    static final String DEV_APP_TOKEN = "dev-token-app";
    static final String DEV_ADMIN_ID = "dev-admin";
    static final String DEV_APP_ID = "dev-app";
    static final String DEV_ORG_ID = "default-org";

    private BffUserContextResolver() {
    }

    static ResolvedUser resolve(String authorization) {
        return resolve(authorization, null, null);
    }

    static ResolvedUser resolve(String authorization, String userId, String orgId) {
        String token = extractToken(authorization);
        boolean app = DEV_APP_TOKEN.equals(token) || DEV_APP_ID.equals(userId);
        String resolvedUserId = defaultIfBlank(userId, app ? DEV_APP_ID : DEV_ADMIN_ID);
        String resolvedOrgId = defaultIfBlank(orgId, DEV_ORG_ID);
        return new ResolvedUser(token, resolvedUserId, resolvedOrgId, username(resolvedUserId), !DEV_APP_ID.equals(resolvedUserId));
    }

    static String extractToken(String authorization) {
        if (authorization == null) {
            return "";
        }
        String value = authorization.trim();
        if (value.toLowerCase().startsWith("bearer ")) {
            return value.substring(7).trim();
        }
        return value;
    }

    private static String username(String userId) {
        if (DEV_APP_ID.equals(userId)) {
            return "app";
        }
        if (DEV_ADMIN_ID.equals(userId)) {
            return "admin";
        }
        return defaultIfBlank(userId, "admin");
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    static final class ResolvedUser {
        private final String token;
        private final String userId;
        private final String orgId;
        private final String username;
        private final boolean admin;

        private ResolvedUser(String token, String userId, String orgId, String username, boolean admin) {
            this.token = token;
            this.userId = userId;
            this.orgId = orgId;
            this.username = username;
            this.admin = admin;
        }

        String getToken() {
            return token;
        }

        String getUserId() {
            return userId;
        }

        String getOrgId() {
            return orgId;
        }

        String getUsername() {
            return username;
        }

        boolean isAdmin() {
            return admin;
        }
    }
}
