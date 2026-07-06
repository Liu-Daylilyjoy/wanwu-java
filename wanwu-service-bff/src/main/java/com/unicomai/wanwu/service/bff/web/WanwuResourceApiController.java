package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.mcp.McpService;
import com.unicomai.wanwu.api.model.dto.ModelInfo;
import com.unicomai.wanwu.api.model.ModelService;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
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
public class WanwuResourceApiController {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String DEV_USER_ID = "dev-admin";
    private static final String DEV_APP_USER_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";
    private final OpenAiCompatibleChatClient chatClient = new OpenAiCompatibleChatClient();

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private McpService mcpService;

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private ModelService modelService;

    public WanwuResourceApiController() {
    }

    public WanwuResourceApiController(McpService mcpService) {
        this.mcpService = mcpService;
    }

    public WanwuResourceApiController(McpService mcpService, ModelService modelService) {
        this.mcpService = mcpService;
        this.modelService = modelService;
    }

    @PostMapping("/tool/custom")
    public FrontendResponse<Map<String, Object>> createCustomTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createCustomTool(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/tool/custom")
    public FrontendResponse<Map<String, Object>> getCustomTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("customToolId") String customToolId) {
        return ok(authorization, ctx -> mcpService.getCustomTool(ctx.userId, ctx.orgId, customToolId));
    }

    @PutMapping("/tool/custom")
    public FrontendResponse<Map<String, Object>> updateCustomTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.updateCustomTool(ctx.userId, ctx.orgId, body), request);
    }

    @DeleteMapping("/tool/custom")
    public FrontendResponse<Map<String, Object>> deleteCustomTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteCustomTool(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/tool/custom/list")
    public FrontendResponse<Map<String, Object>> listCustomTools(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listCustomTools(ctx.userId, ctx.orgId, name));
    }

    @PostMapping("/tool/custom/schema")
    public FrontendResponse<Map<String, Object>> parseCustomToolSchema(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.parseCustomToolSchema(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/tool/square/list")
    public FrontendResponse<Map<String, Object>> listToolSquares(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listToolSquares(ctx.userId, ctx.orgId, name));
    }

    @GetMapping("/tool/square")
    public FrontendResponse<Map<String, Object>> getToolSquare(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("toolSquareId") String toolSquareId) {
        return ok(authorization, ctx -> mcpService.getToolSquare(ctx.userId, ctx.orgId, toolSquareId));
    }

    @PostMapping("/tool/builtin")
    public FrontendResponse<Map<String, Object>> updateToolSquareApiKey(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.updateToolSquareApiKey(ctx.userId, ctx.orgId, body),
                request);
    }

    @PostMapping("/mcp")
    public FrontendResponse<Map<String, Object>> createMcp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createMcp(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/mcp")
    public FrontendResponse<Map<String, Object>> getMcp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("mcpId") String mcpId) {
        return ok(authorization, ctx -> mcpService.getMcp(ctx.userId, ctx.orgId, mcpId));
    }

    @PutMapping("/mcp")
    public FrontendResponse<Map<String, Object>> updateMcp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.updateMcp(ctx.userId, ctx.orgId, body), request);
    }

    @DeleteMapping("/mcp")
    public FrontendResponse<Map<String, Object>> deleteMcp(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteMcp(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/mcp/list")
    public FrontendResponse<Map<String, Object>> listMcps(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listMcps(ctx.userId, ctx.orgId, name));
    }

    @GetMapping("/mcp/tool/list")
    public FrontendResponse<Map<String, Object>> listMcpTools(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Map<String, String> request) {
        return ok(authorization, ctx -> mcpService.listMcpTools(ctx.userId, ctx.orgId, stringMap(request)));
    }

    @PostMapping("/mcp/server")
    public FrontendResponse<Map<String, Object>> createMcpServer(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createMcpServer(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/mcp/server")
    public FrontendResponse<Map<String, Object>> getMcpServer(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("mcpServerId") String mcpServerId) {
        return ok(authorization, ctx -> mcpService.getMcpServer(ctx.userId, ctx.orgId, mcpServerId));
    }

    @PutMapping("/mcp/server")
    public FrontendResponse<Map<String, Object>> updateMcpServer(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.updateMcpServer(ctx.userId, ctx.orgId, body), request);
    }

    @DeleteMapping("/mcp/server")
    public FrontendResponse<Map<String, Object>> deleteMcpServer(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteMcpServer(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/mcp/server/list")
    public FrontendResponse<Map<String, Object>> listMcpServers(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listMcpServers(ctx.userId, ctx.orgId, name));
    }

    @PostMapping("/mcp/server/tool")
    public FrontendResponse<Map<String, Object>> createMcpServerTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createMcpServerTool(ctx.userId, ctx.orgId, body), request);
    }

    @PutMapping("/mcp/server/tool")
    public FrontendResponse<Map<String, Object>> updateMcpServerTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.updateMcpServerTool(ctx.userId, ctx.orgId, body),
                request);
    }

    @DeleteMapping("/mcp/server/tool")
    public FrontendResponse<Map<String, Object>> deleteMcpServerTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteMcpServerTool(ctx.userId, ctx.orgId, body),
                request);
    }

    @PostMapping("/mcp/server/tool/openapi")
    public FrontendResponse<Map<String, Object>> createMcpServerOpenApiTool(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization,
                (ctx, body) -> mcpService.createMcpServerOpenApiTool(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/mcp/square/list")
    public FrontendResponse<Map<String, Object>> listMcpSquares(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listMcpSquares(ctx.userId, ctx.orgId, name));
    }

    @GetMapping("/mcp/square")
    public FrontendResponse<Map<String, Object>> getMcpSquare(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("mcpSquareId") String mcpSquareId) {
        return ok(authorization, ctx -> mcpService.getMcpSquare(ctx.userId, ctx.orgId, mcpSquareId));
    }

    @GetMapping("/mcp/square/recommend")
    public FrontendResponse<Map<String, Object>> recommendMcpSquares(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "mcpSquareId", required = false) String mcpSquareId) {
        return ok(authorization, ctx -> mcpService.recommendMcpSquares(ctx.userId, ctx.orgId, mcpSquareId));
    }

    @PostMapping("/prompt/custom")
    public FrontendResponse<Map<String, Object>> createCustomPrompt(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createCustomPrompt(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/prompt/custom")
    public FrontendResponse<Map<String, Object>> getCustomPrompt(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("customPromptId") String customPromptId) {
        return ok(authorization, ctx -> mcpService.getCustomPrompt(ctx.userId, ctx.orgId, customPromptId));
    }

    @PutMapping("/prompt/custom")
    public FrontendResponse<Map<String, Object>> updateCustomPrompt(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.updateCustomPrompt(ctx.userId, ctx.orgId, body),
                request);
    }

    @DeleteMapping("/prompt/custom")
    public FrontendResponse<Map<String, Object>> deleteCustomPrompt(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return voidOk(authorization, (ctx, body) -> mcpService.deleteCustomPrompt(ctx.userId, ctx.orgId, body),
                request);
    }

    @GetMapping("/prompt/custom/list")
    public FrontendResponse<Map<String, Object>> listCustomPrompts(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listCustomPrompts(ctx.userId, ctx.orgId, name));
    }

    @PostMapping("/prompt/custom/copy")
    public FrontendResponse<Map<String, Object>> copyCustomPrompt(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.copyCustomPrompt(ctx.userId, ctx.orgId, body), request);
    }

    @GetMapping("/prompt/template/list")
    public FrontendResponse<Map<String, Object>> listPromptTemplates(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "name", required = false) String name) {
        return ok(authorization, ctx -> mcpService.listPromptTemplates(ctx.userId, ctx.orgId, name));
    }

    @GetMapping("/prompt/template/detail")
    public FrontendResponse<Map<String, Object>> getPromptTemplate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("templateId") String templateId) {
        return ok(authorization, ctx -> mcpService.getPromptTemplate(ctx.userId, ctx.orgId, templateId));
    }

    @PostMapping("/prompt/template")
    public FrontendResponse<Map<String, Object>> createPromptByTemplate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return ok(authorization, (ctx, body) -> mcpService.createPromptByTemplate(ctx.userId, ctx.orgId, body),
                request);
    }

    @PostMapping(value = "/prompt/optimize", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> optimizePrompt(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return promptStream(authorization, request, (ctx, body) -> promptModelOrFallback(ctx, body, "optimize",
                (fallbackCtx, fallbackBody) -> mcpService.optimizePrompt(fallbackCtx.userId, fallbackCtx.orgId,
                        fallbackBody)));
    }

    @PostMapping(value = "/prompt/reason", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> reasonPrompt(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return promptStream(authorization, request, (ctx, body) -> promptModelOrFallback(ctx, body, "reason",
                (fallbackCtx, fallbackBody) -> mcpService.reasonPrompt(fallbackCtx.userId, fallbackCtx.orgId,
                        fallbackBody)));
    }

    @PostMapping(value = "/prompt/evaluate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> evaluatePrompt(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return promptStream(authorization, request, (ctx, body) -> promptModelOrFallback(ctx, body, "evaluate",
                (fallbackCtx, fallbackBody) -> mcpService.evaluatePrompt(fallbackCtx.userId, fallbackCtx.orgId,
                        fallbackBody)));
    }

    private FrontendResponse<Map<String, Object>> ok(String authorization, ResourceCall call) {
        try {
            return FrontendResponse.ok(call.execute(userContext(authorization)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private FrontendResponse<Map<String, Object>> ok(String authorization, ResourceBodyCall call,
                                                    Map<String, Object> request) {
        try {
            return FrontendResponse.ok(call.execute(userContext(authorization), body(request)));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private FrontendResponse<Map<String, Object>> voidOk(String authorization, ResourceVoidCall call,
                                                        Map<String, Object> request) {
        try {
            call.execute(userContext(authorization), body(request));
            return FrontendResponse.ok(Collections.<String, Object>emptyMap());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private ResponseEntity<String> promptStream(String authorization, Map<String, Object> request,
                                                ResourceBodyCall call) {
        try {
            UserContext context = userContext(authorization);
            Map<String, Object> payloadRequest = body(request);
            authorizePromptModel(context, payloadRequest);
            Map<String, Object> payload = call.execute(context, payloadRequest);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body("data: " + toJson(payload) + "\n\n");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"code\":1001,\"msg\":\"" + ex.getMessage() + "\"}");
        }
    }

    private Map<String, Object> promptModelOrFallback(UserContext context, Map<String, Object> request,
                                                      String operation, ResourceBodyCall fallback) {
        Map<String, Object> upstream = promptModelPayload(context, request, operation);
        if (upstream != null) {
            return upstream;
        }
        return fallback.execute(context, request);
    }

    private Map<String, Object> promptModelPayload(UserContext context, Map<String, Object> request,
                                                   String operation) {
        String modelId = text(request, "modelId");
        if (modelService == null || isBlank(modelId)) {
            return null;
        }
        try {
            ModelInfo model = modelService.getModel(context.userId, context.orgId, modelId);
            if (model == null) {
                return null;
            }
            String prompt = promptInstruction(operation, request);
            OpenAiCompatibleChatClient.ChatCompletionResult result = chatClient.complete(model, modelId, prompt);
            if (result == null || isBlank(result.getContent())) {
                return null;
            }
            return promptPayload(result.getContent());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Map<String, Object> promptPayload(String response) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("response", response);
        payload.put("finish", 1);
        return payload;
    }

    private String promptInstruction(String operation, Map<String, Object> request) {
        if ("evaluate".equals(operation)) {
            return "Evaluate whether the answer matches the expected output. Return concise findings.\n\n"
                    + "Expected output:\n" + text(request, "expectedOutput") + "\n\n"
                    + "Answer:\n" + text(request, "answer");
        }
        if ("reason".equals(operation)) {
            return "Run the following prompt and return the resulting answer.\n\nPrompt:\n"
                    + text(request, "prompt");
        }
        return "Optimize the following prompt. Return only the improved prompt.\n\nPrompt:\n"
                + text(request, "prompt");
    }

    private Map<String, Object> body(Map<String, Object> request) {
        return request == null ? new LinkedHashMap<>() : request;
    }

    private void authorizePromptModel(UserContext context, Map<String, Object> request) {
        String modelId = text(request, "modelId");
        if (modelService == null || isBlank(modelId)) {
            return;
        }
        modelService.checkModelUserPermission(context.userId, context.orgId, Collections.singletonList(modelId));
    }

    private String text(Map<String, Object> request, String key) {
        if (request == null || key == null || !request.containsKey(key)) {
            return "";
        }
        Object value = request.get(key);
        return value instanceof String ? String.valueOf(value) : "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, Object> stringMap(Map<String, String> request) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (request != null) {
            result.putAll(request);
        }
        return result;
    }

    private UserContext userContext(String authorization) {
        BffUserContextResolver.ResolvedUser resolved = BffUserContextResolver.resolve(authorization);
        return new UserContext(resolved.getUserId(), resolved.getOrgId());
    }

    private String extractToken(String authorization) {
        if (authorization == null || authorization.trim().isEmpty()) {
            return "";
        }
        String value = authorization.trim();
        return value.startsWith("Bearer ") ? value.substring("Bearer ".length()) : value;
    }

    private String toJson(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"response\":\"serialization error\",\"finish\":1}";
        }
    }

    private interface ResourceCall {
        Map<String, Object> execute(UserContext context);
    }

    private interface ResourceBodyCall {
        Map<String, Object> execute(UserContext context, Map<String, Object> request);
    }

    private interface ResourceVoidCall {
        void execute(UserContext context, Map<String, Object> request);
    }

    private static final class UserContext {
        private final String userId;
        private final String orgId;

        private UserContext(String userId, String orgId) {
            this.userId = userId;
            this.orgId = orgId;
        }
    }
}
