package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.safety.SafetyService;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuSafetyApiController {

    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private SafetyService safetyService;

    public WanwuSafetyApiController() {
    }

    public WanwuSafetyApiController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @PostMapping("/safe/sensitive/table")
    public FrontendResponse<Map<String, Object>> createSensitiveWordTable(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> safetyService.createSensitiveWordTable(ctx.userId, ctx.orgId, body),
                request);
    }

    @PutMapping("/safe/sensitive/table")
    public FrontendResponse<Map<String, Object>> updateSensitiveWordTable(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> safetyService.updateSensitiveWordTable(ctx.userId, ctx.orgId, body),
                request);
    }

    @GetMapping("/safe/sensitive/table")
    public FrontendResponse<Map<String, Object>> getSensitiveWordTable(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("tableId") String tableId) {
        return ok(authorization, ctx -> safetyService.getSensitiveWordTable(ctx.userId, ctx.orgId, tableId));
    }

    @PutMapping("/safe/sensitive/table/reply")
    public FrontendResponse<Map<String, Object>> updateSensitiveWordTableReply(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) ->
                safetyService.updateSensitiveWordTableReply(ctx.userId, ctx.orgId, body), request);
    }

    @DeleteMapping("/safe/sensitive/table")
    public FrontendResponse<Map<String, Object>> deleteSensitiveWordTable(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> safetyService.deleteSensitiveWordTable(ctx.userId, ctx.orgId, body),
                request);
    }

    @GetMapping("/safe/sensitive/table/list")
    public FrontendResponse<Map<String, Object>> listSensitiveWordTables(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "type", required = false) String type) {
        return ok(authorization, ctx -> safetyService.listSensitiveWordTables(ctx.userId, ctx.orgId, type));
    }

    @GetMapping("/safe/sensitive/table/select")
    public FrontendResponse<Map<String, Object>> selectSensitiveWordTables(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ok(authorization, ctx -> safetyService.selectSensitiveWordTables(ctx.userId, ctx.orgId));
    }

    @GetMapping("/safe/sensitive/word/list")
    public FrontendResponse<Map<String, Object>> listSensitiveWords(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("tableId") String tableId,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ok(authorization, ctx ->
                safetyService.listSensitiveWords(ctx.userId, ctx.orgId, tableId, pageNo, pageSize));
    }

    @PostMapping("/safe/sensitive/word")
    public FrontendResponse<Map<String, Object>> uploadSensitiveWord(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> safetyService.uploadSensitiveWord(ctx.userId, ctx.orgId, body),
                enrichSensitiveWordFile(request));
    }

    @DeleteMapping("/safe/sensitive/word")
    public FrontendResponse<Map<String, Object>> deleteSensitiveWord(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> safetyService.deleteSensitiveWord(ctx.userId, ctx.orgId, body),
                request);
    }

    private FrontendResponse<Map<String, Object>> ok(String authorization, Query query) {
        return FrontendResponse.ok(query.apply(userContext(authorization)));
    }

    private FrontendResponse<Map<String, Object>> ok(String authorization, Command command, Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        return FrontendResponse.ok(command.apply(ctx, body(request)));
    }

    private FrontendResponse<Map<String, Object>> voidOk(String authorization, VoidCommand command,
                                                         Map<String, Object> request) {
        UserContext ctx = userContext(authorization);
        command.apply(ctx, body(request));
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    private Map<String, Object> body(Map<String, Object> request) {
        return request == null ? new LinkedHashMap<>() : request;
    }

    private Map<String, Object> enrichSensitiveWordFile(Map<String, Object> request) {
        Map<String, Object> body = body(request);
        if (!"file".equalsIgnoreCase(text(body, "importType")) || !isBlank(text(body, "content"))) {
            return body;
        }
        String fileName = firstText(body, "fileName", "fileUploadId", "fileId");
        byte[] bytes = UploadedFileStore.defaultStore().readBytes(fileName);
        if (bytes.length == 0) {
            return body;
        }
        String content = isXlsx(fileName)
                ? SimpleXlsxReader.toDelimitedText(bytes)
                : new String(bytes, StandardCharsets.UTF_8);
        if (!isBlank(content)) {
            body.put("content", content);
        }
        return body;
    }

    private String firstText(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            String value = text(map, key);
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String text(Map<String, Object> map, String key) {
        if (map == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private boolean isXlsx(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".xlsx");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private UserContext userContext(String authorization) {
        if (authorization != null && authorization.contains("dev-token-app")) {
            return new UserContext(DEV_APP_USER_ID, DEV_ORG_ID);
        }
        return new UserContext(DEV_USER_ID, DEV_ORG_ID);
    }

    private interface Query {
        Map<String, Object> apply(UserContext ctx);
    }

    private interface Command {
        Map<String, Object> apply(UserContext ctx, Map<String, Object> body);
    }

    private interface VoidCommand {
        void apply(UserContext ctx, Map<String, Object> body);
    }

    private static class UserContext {
        private final String userId;
        private final String orgId;

        private UserContext(String userId, String orgId) {
            this.userId = userId;
            this.orgId = orgId;
        }
    }
}
