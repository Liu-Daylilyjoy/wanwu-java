package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.RecordApiKeyStatisticCommand;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class OpenApiUsageRecordFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiUsageRecordFilter.class);
    private static final String PREFIX = "/service/api/openapi/v1";
    private static final String DEV_ADMIN_TOKEN = "dev-token";
    private static final String DEV_APP_TOKEN = "dev-token-app";
    private static final String DEV_ADMIN_ID = "dev-admin";
    private static final String DEV_APP_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final OpenApiUsageMeter usageMeter;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AppService appService;

    public OpenApiUsageRecordFilter() {
        this(new OpenApiUsageMeter(), null);
    }

    @Autowired
    public OpenApiUsageRecordFilter(OpenApiUsageMeter usageMeter) {
        this(usageMeter, null);
    }

    public OpenApiUsageRecordFilter(OpenApiUsageMeter usageMeter, AppService appService) {
        this.usageMeter = usageMeter == null ? new OpenApiUsageMeter() : usageMeter;
        this.appService = appService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith(PREFIX + "/")) {
            return true;
        }
        return uri.startsWith(PREFIX + "/mcp/")
                || uri.startsWith(PREFIX + "/oauth/")
                || uri.startsWith(PREFIX + "/.well-known/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, 4096);
        long startedAt = System.currentTimeMillis();
        RuntimeException runtimeFailure = null;
        ServletException servletFailure = null;
        IOException ioFailure = null;
        try {
            filterChain.doFilter(wrapped, response);
        } catch (RuntimeException ex) {
            runtimeFailure = ex;
            throw ex;
        } catch (ServletException ex) {
            servletFailure = ex;
            throw ex;
        } catch (IOException ex) {
            ioFailure = ex;
            throw ex;
        } finally {
            int status = response.getStatus();
            if (status == 0 && (runtimeFailure != null || servletFailure != null || ioFailure != null)) {
                status = 500;
            }
            if (status == 0) {
                status = 200;
            }
            record(wrapped, startedAt, status);
        }
    }

    private void record(ContentCachingRequestWrapper request, long startedAt, int status) {
        UsageContext context = usageContext(request);
        if (context == null || isBlank(context.apiKeyId)) {
            return;
        }
        long costs = Math.max(0L, System.currentTimeMillis() - startedAt);
        String requestBody = jsonRequestBody(request);
        boolean stream = streamRequest(request, requestBody);
        String methodPath = request.getMethod() + "-" + request.getRequestURI();
        long streamCosts = stream ? costs : 0L;
        long nonStreamCosts = stream ? 0L : costs;
        usageMeter.record(
                context.userId,
                context.orgId,
                context.apiKeyId,
                methodPath,
                startedAt,
                String.valueOf(status),
                stream,
                streamCosts,
                nonStreamCosts,
                requestBody,
                "");
        recordAppService(context, methodPath, startedAt, status, stream, streamCosts, nonStreamCosts, requestBody);
    }

    private void recordAppService(UsageContext context,
                                  String methodPath,
                                  long callTime,
                                  int status,
                                  boolean stream,
                                  long streamCosts,
                                  long nonStreamCosts,
                                  String requestBody) {
        if (appService == null) {
            return;
        }
        try {
            RecordApiKeyStatisticCommand command = new RecordApiKeyStatisticCommand();
            command.setUserId(context.userId);
            command.setOrgId(context.orgId);
            command.setApiKeyId(context.apiKeyId);
            command.setMethodPath(methodPath);
            command.setCallTime(callTime);
            command.setHttpStatus(String.valueOf(status));
            command.setStream(stream);
            command.setStreamCosts(streamCosts);
            command.setNonStreamCosts(nonStreamCosts);
            command.setRequestBody(requestBody);
            command.setResponseBody("");
            appService.recordApiKeyStatistic(command);
        } catch (RuntimeException ex) {
            LOGGER.warn("OpenAPI API key statistic persistence failed, falling back to local meter: apiKeyId={}, methodPath={}",
                    context.apiKeyId,
                    methodPath,
                    ex);
        }
    }

    private UsageContext usageContext(HttpServletRequest request) {
        String token = apiToken(request);
        if (DEV_APP_TOKEN.equals(token)) {
            return new UsageContext(DEV_APP_ID, DEV_ORG_ID, "dev-app-key");
        }
        if (DEV_ADMIN_TOKEN.equals(token) || isBlank(token)) {
            return new UsageContext(DEV_ADMIN_ID, DEV_ORG_ID, "dev-admin-key");
        }
        try {
            ApiKeyInfo apiKey = appService == null ? null : appService.getApiKeyByKey(token);
            if (apiKey == null || Boolean.FALSE.equals(apiKey.getStatus())) {
                return null;
            }
            return new UsageContext(
                    defaultIfBlank(apiKey.getUserId(), DEV_ADMIN_ID),
                    defaultIfBlank(apiKey.getOrgId(), DEV_ORG_ID),
                    defaultIfBlank(apiKey.getKeyId(), token));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String jsonRequestBody(ContentCachingRequestWrapper request) {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            return "";
        }
        byte[] bytes = request.getContentAsByteArray();
        if (bytes.length == 0) {
            return "";
        }
        Charset charset = request.getCharacterEncoding() == null
                ? StandardCharsets.UTF_8
                : Charset.forName(request.getCharacterEncoding());
        return new String(bytes, charset);
    }

    private boolean streamRequest(HttpServletRequest request, String requestBody) {
        if ("POST".equalsIgnoreCase(request.getMethod())
                && (PREFIX + "/chatflow/chat").equals(request.getRequestURI())) {
            return true;
        }
        if (isBlank(requestBody)) {
            return false;
        }
        try {
            Map<String, Object> body = JSON.readValue(requestBody, new TypeReference<Map<String, Object>>() {
            });
            Object stream = body.get("stream");
            if (stream instanceof Boolean) {
                return (Boolean) stream;
            }
            return stream != null && Boolean.parseBoolean(String.valueOf(stream));
        } catch (IOException ex) {
            return false;
        }
    }

    private String apiToken(HttpServletRequest request) {
        String value = firstHeader(request, "X-API-Key");
        if (isBlank(value)) {
            value = firstHeader(request, "Api-Key");
        }
        if (isBlank(value)) {
            value = firstHeader(request, HttpHeaders.AUTHORIZATION);
        }
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.toLowerCase().startsWith("bearer ")) {
            return trimmed.substring("bearer ".length()).trim();
        }
        return trimmed;
    }

    private String firstHeader(HttpServletRequest request, String name) {
        List<String> values = request == null ? null : java.util.Collections.list(request.getHeaders(name));
        return values == null || values.isEmpty() ? "" : values.get(0);
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class UsageContext {
        private final String userId;
        private final String orgId;
        private final String apiKeyId;

        private UsageContext(String userId, String orgId, String apiKeyId) {
            this.userId = userId;
            this.orgId = orgId;
            this.apiKeyId = apiKeyId;
        }
    }
}
