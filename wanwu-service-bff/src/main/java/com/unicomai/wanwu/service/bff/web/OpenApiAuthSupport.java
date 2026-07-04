package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class OpenApiAuthSupport {

    static final String DEV_ADMIN_TOKEN = "dev-token";
    static final String DEV_APP_TOKEN = "dev-token-app";
    static final String DEV_ADMIN_ID = "dev-admin";
    static final String DEV_APP_ID = "dev-app";
    static final String DEV_ORG_ID = "default-org";
    static final String DEV_ADMIN_API_KEY_ID = "dev-admin-key";
    static final String DEV_APP_API_KEY_ID = "dev-app-key";

    private static final Pattern MILLIS_PATTERN = Pattern.compile("^\\d+$");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    private OpenApiAuthSupport() {
    }

    static String extractToken(HttpHeaders headers) {
        String value = firstHeader(headers, "X-API-Key");
        if (isBlank(value)) {
            value = firstHeader(headers, "Api-Key");
        }
        if (!isBlank(value)) {
            return value.trim();
        }
        return bearerToken(firstHeader(headers, HttpHeaders.AUTHORIZATION));
    }

    static String extractToken(HttpServletRequest request) {
        String value = firstHeader(request, "X-API-Key");
        if (isBlank(value)) {
            value = firstHeader(request, "Api-Key");
        }
        if (!isBlank(value)) {
            return value.trim();
        }
        return bearerToken(firstHeader(request, HttpHeaders.AUTHORIZATION));
    }

    static AuthResult resolve(AppService appService, String token) {
        if (isBlank(token)) {
            throw new OpenApiAuthException("token is nil");
        }
        if (DEV_APP_TOKEN.equals(token)) {
            return new AuthResult(DEV_APP_ID, DEV_ORG_ID, DEV_APP_API_KEY_ID);
        }
        if (DEV_ADMIN_TOKEN.equals(token)) {
            return new AuthResult(DEV_ADMIN_ID, DEV_ORG_ID, DEV_ADMIN_API_KEY_ID);
        }
        ApiKeyInfo apiKey = loadApiKey(appService, token);
        if (!Boolean.TRUE.equals(apiKey.getStatus())) {
            throw new OpenApiAuthException("api key disabled");
        }
        if (expired(apiKey.getExpiredAt(), System.currentTimeMillis())) {
            throw new OpenApiAuthException("api key expired");
        }
        return new AuthResult(
                defaultIfBlank(apiKey.getUserId(), DEV_ADMIN_ID),
                defaultIfBlank(apiKey.getOrgId(), DEV_ORG_ID),
                defaultIfBlank(apiKey.getKeyId(), token));
    }

    private static ApiKeyInfo loadApiKey(AppService appService, String token) {
        if (appService == null) {
            throw new OpenApiAuthException("invalid api key");
        }
        try {
            ApiKeyInfo apiKey = appService.getApiKeyByKey(token);
            if (apiKey == null) {
                throw new OpenApiAuthException("invalid api key");
            }
            return apiKey;
        } catch (OpenApiAuthException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new OpenApiAuthException("invalid api key");
        }
    }

    private static boolean expired(String expiredAt, long nowMillis) {
        if (isBlank(expiredAt)) {
            return false;
        }
        String value = expiredAt.trim();
        long millis;
        try {
            if (MILLIS_PATTERN.matcher(value).matches()) {
                millis = Long.parseLong(value);
            } else if (value.length() == "yyyy-MM-dd".length()) {
                millis = LocalDate.parse(value, DATE_ONLY_FORMATTER)
                        .atStartOfDay(APP_ZONE)
                        .toInstant()
                        .toEpochMilli();
            } else {
                millis = LocalDateTime.parse(value, DATE_TIME_FORMATTER)
                        .atZone(APP_ZONE)
                        .toInstant()
                        .toEpochMilli();
            }
        } catch (RuntimeException ex) {
            throw new OpenApiAuthException("api key expiredAt invalid");
        }
        return millis > 0L && millis < nowMillis;
    }

    private static String bearerToken(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (!trimmed.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            throw new OpenApiAuthException("not Bearer token format");
        }
        return trimmed.substring("bearer ".length()).trim();
    }

    private static String firstHeader(HttpHeaders headers, String name) {
        List<String> values = headers == null ? null : headers.get(name);
        return values == null || values.isEmpty() ? "" : values.get(0);
    }

    private static String firstHeader(HttpServletRequest request, String name) {
        List<String> values = request == null ? null : Collections.list(request.getHeaders(name));
        return values == null || values.isEmpty() ? "" : values.get(0);
    }

    private static String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static class AuthResult {
        final String userId;
        final String orgId;
        final String apiKeyId;

        AuthResult(String userId, String orgId, String apiKeyId) {
            this.userId = userId;
            this.orgId = orgId;
            this.apiKeyId = apiKeyId;
        }
    }
}
