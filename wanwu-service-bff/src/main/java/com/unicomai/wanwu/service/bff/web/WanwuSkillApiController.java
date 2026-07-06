package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.mcp.McpService;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuSkillApiController {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private McpService mcpService;

    public WanwuSkillApiController() {
    }

    public WanwuSkillApiController(McpService mcpService) {
        this.mcpService = mcpService;
    }

    @GetMapping("/agent/skill/custom/list")
    public FrontendResponse<Map<String, Object>> listCustomSkills(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listCustomSkills(ctx.userId, ctx.orgId, name));
    }

    @GetMapping("/agent/skill/custom/detail")
    public FrontendResponse<Map<String, Object>> getCustomSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("skillId") String skillId) {
        return ok(authorization, ctx -> mcpService.getCustomSkill(ctx.userId, ctx.orgId, skillId));
    }

    @PostMapping("/agent/skill/custom/check")
    public FrontendResponse<Map<String, Object>> checkCustomSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.checkCustomSkill(ctx.userId, ctx.orgId, body), request);
    }

    @PostMapping("/agent/skill/custom")
    public FrontendResponse<Map<String, Object>> createCustomSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createCustomSkill(ctx.userId, ctx.orgId, body), request);
    }

    @DeleteMapping("/agent/skill/custom")
    public FrontendResponse<Map<String, Object>> deleteCustomSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteCustomSkill(ctx.userId, ctx.orgId, body), request);
    }

    @PostMapping("/agent/skill/custom/config")
    public FrontendResponse<Map<String, Object>> createCustomSkillConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createCustomSkillConfig(ctx.userId, ctx.orgId, body),
                request);
    }

    @PutMapping("/agent/skill/custom/config")
    public FrontendResponse<Map<String, Object>> updateCustomSkillConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.updateCustomSkillConfig(ctx.userId, ctx.orgId, body),
                request);
    }

    @DeleteMapping("/agent/skill/custom/config")
    public FrontendResponse<Map<String, Object>> deleteCustomSkillConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteCustomSkillConfig(ctx.userId, ctx.orgId, body),
                request);
    }

    @GetMapping("/agent/skill/builtin/list")
    public FrontendResponse<Map<String, Object>> listBuiltinSkills(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listBuiltinSkills(ctx.userId, ctx.orgId, name));
    }

    @GetMapping("/agent/skill/builtin/detail")
    public FrontendResponse<Map<String, Object>> getBuiltinSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("skillId") String skillId) {
        return ok(authorization, ctx -> mcpService.getBuiltinSkill(ctx.userId, ctx.orgId, skillId));
    }

    @GetMapping("/builtin/skill/download")
    public ResponseEntity<byte[]> downloadBuiltinSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("skillId") String skillId) {
        UserContext ctx = userContext(authorization);
        return download(skillId, mcpService.downloadBuiltinSkill(ctx.userId, ctx.orgId, skillId));
    }

    @PostMapping("/agent/skill/builtin/config")
    public FrontendResponse<Map<String, Object>> createBuiltinSkillConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createBuiltinSkillConfig(ctx.userId, ctx.orgId, body),
                request);
    }

    @PutMapping("/agent/skill/builtin/config")
    public FrontendResponse<Map<String, Object>> updateBuiltinSkillConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.updateBuiltinSkillConfig(ctx.userId, ctx.orgId, body),
                request);
    }

    @DeleteMapping("/agent/skill/builtin/config")
    public FrontendResponse<Map<String, Object>> deleteBuiltinSkillConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteBuiltinSkillConfig(ctx.userId, ctx.orgId, body),
                request);
    }

    @GetMapping("/agent/skill/select")
    public FrontendResponse<Map<String, Object>> listSkillSelect(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "skillType", required = false) String skillType) {
        return ok(authorization, ctx -> mcpService.listSkillSelect(ctx.userId, ctx.orgId, name, skillType));
    }

    @GetMapping("/agent/acquired/skill/list")
    public FrontendResponse<Map<String, Object>> listAcquiredSkills(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listAcquiredSkills(ctx.userId, ctx.orgId, name));
    }

    @GetMapping("/agent/acquired/skill/detail")
    public FrontendResponse<Map<String, Object>> getAcquiredSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("skillId") String skillId) {
        return ok(authorization, ctx -> mcpService.getAcquiredSkill(ctx.userId, ctx.orgId, skillId));
    }

    @DeleteMapping("/agent/acquired/skill")
    public FrontendResponse<Map<String, Object>> deleteAcquiredSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteAcquiredSkill(ctx.userId, ctx.orgId, body),
                request);
    }

    @PostMapping("/agent/acquired/skill/config")
    public FrontendResponse<Map<String, Object>> createAcquiredSkillConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createAcquiredSkillConfig(ctx.userId, ctx.orgId, body),
                request);
    }

    @PutMapping("/agent/acquired/skill/config")
    public FrontendResponse<Map<String, Object>> updateAcquiredSkillConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.updateAcquiredSkillConfig(ctx.userId, ctx.orgId, body),
                request);
    }

    @DeleteMapping("/agent/acquired/skill/config")
    public FrontendResponse<Map<String, Object>> deleteAcquiredSkillConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteAcquiredSkillConfig(ctx.userId, ctx.orgId, body),
                request);
    }

    @GetMapping("/square/skill/list")
    public FrontendResponse<Map<String, Object>> listSquareSkills(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listSquareSkills(ctx.userId, ctx.orgId, name));
    }

    @GetMapping("/square/skill/builtin/list")
    public FrontendResponse<Map<String, Object>> listSquareBuiltinSkills(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listSquareBuiltinSkills(ctx.userId, ctx.orgId, name));
    }

    @PostMapping("/square/skill/share")
    public FrontendResponse<Map<String, Object>> shareSquareSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.shareSquareSkill(ctx.userId, ctx.orgId, body),
                request);
    }

    @GetMapping("/square/skill/detail")
    public FrontendResponse<Map<String, Object>> getSquareSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("skillId") String skillId) {
        return ok(authorization, ctx -> mcpService.getSquareSkill(ctx.userId, ctx.orgId, skillId));
    }

    @GetMapping("/square/skill/download")
    public ResponseEntity<byte[]> downloadSquareSkill(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("skillId") String skillId) {
        UserContext ctx = userContext(authorization);
        return download(skillId, mcpService.downloadSquareSkill(ctx.userId, ctx.orgId, skillId));
    }

    @PostMapping("/agent/skill/conversation")
    public FrontendResponse<Map<String, Object>> createSkillConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createSkillConversation(ctx.userId, ctx.orgId, body),
                request);
    }

    @DeleteMapping("/agent/skill/conversation")
    public FrontendResponse<Map<String, Object>> deleteSkillConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteSkillConversation(ctx.userId, ctx.orgId, body),
                request);
    }

    @DeleteMapping("/agent/skill/conversation/clear")
    public FrontendResponse<Map<String, Object>> clearSkillConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.clearSkillConversation(ctx.userId, ctx.orgId, body),
                request);
    }

    @GetMapping("/agent/skill/conversation/list")
    public FrontendResponse<Map<String, Object>> listSkillConversations(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ok(authorization, ctx -> mcpService.listSkillConversations(ctx.userId, ctx.orgId, pageNo, pageSize));
    }

    @GetMapping("/agent/skill/conversation/detail")
    public FrontendResponse<Map<String, Object>> getSkillConversationDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("conversationId") String conversationId) {
        return ok(authorization, ctx -> mcpService.getSkillConversationDetail(ctx.userId, ctx.orgId, conversationId));
    }

    @PostMapping(value = "/agent/skill/conversation/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> chatSkillConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) throws JsonProcessingException {
        UserContext ctx = userContext(authorization);
        Map<String, Object> payload = mcpService.chatSkillConversation(ctx.userId, ctx.orgId, body(request));
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body("data: " + JSON.writeValueAsString(payload) + "\n\n");
    }

    @PostMapping("/agent/skill/conversation/save")
    public FrontendResponse<Map<String, Object>> saveSkillConversation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.saveSkillConversation(ctx.userId, ctx.orgId, body),
                request);
    }

    private ResponseEntity<byte[]> download(String skillId, byte[] payload) {
        byte[] body = payload == null ? new byte[0] : payload;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + skillId + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
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

    private UserContext userContext(String authorization) {
        BffUserContextResolver.ResolvedUser resolved = BffUserContextResolver.resolve(authorization);
        return new UserContext(resolved.getUserId(), resolved.getOrgId());
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
