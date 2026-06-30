package com.unicomai.wanwu.service.mcp.rpc;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpServiceImplTest {

    private static final String USER_ID = "dev-admin";
    private static final String ORG_ID = "default-org";

    private final McpServiceImpl service = new McpServiceImpl();

    @Test
    public void resourceLifecycleCoversToolMcpServerMcpAndPrompt() {
        Map<String, Object> toolRequest = new LinkedHashMap<>();
        toolRequest.put("name", "WeatherAPI");
        toolRequest.put("description", "weather lookup");
        toolRequest.put("schema", "{\"openapi\":\"3.0.1\",\"paths\":{\"/weather\":{\"get\":{\"operationId\":\"get_weather\",\"summary\":\"Get weather\"}}}}");
        toolRequest.put("apiAuth", auth());

        String customToolId = text(service.createCustomTool(USER_ID, ORG_ID, toolRequest), "customToolId");
        assertFalse(customToolId.isEmpty());
        assertEquals("WeatherAPI", text(service.getCustomTool(USER_ID, ORG_ID, customToolId), "name"));
        assertEquals(1, list(service.parseCustomToolSchema(USER_ID, ORG_ID, toolRequest).get("list")).size());
        assertEquals(customToolId, text(first(service.listCustomTools(USER_ID, ORG_ID, "Weather")), "customToolId"));

        Map<String, Object> toolSelect = service.listToolSelect(USER_ID, ORG_ID, "Weather");
        assertTrue(list(toolSelect.get("list")).size() >= 1);
        Map<String, Object> toolActions = service.listToolActions(USER_ID, ORG_ID, customToolId, "custom");
        assertEquals("get_weather", text(firstList(toolActions, "actions"), "name"));

        String mcpServerId = text(service.createMcpServer(USER_ID, ORG_ID, map("name", "Local MCP", "desc", "server")),
                "mcpServerId");
        Map<String, Object> bindRequest = map("mcpServerId", mcpServerId, "id", customToolId,
                "type", "custom", "methodName", "get_weather");
        String mcpServerToolId = text(service.createMcpServerTool(USER_ID, ORG_ID, bindRequest), "mcpServerToolId");
        assertFalse(mcpServerToolId.isEmpty());
        assertEquals(1, list(service.getMcpServer(USER_ID, ORG_ID, mcpServerId).get("tools")).size());
        assertEquals("get_weather", text(firstList(service.listMcpActions(USER_ID, ORG_ID, mcpServerId, "mcpserver"),
                "actions"), "name"));

        String mcpId = text(service.createMcp(USER_ID, ORG_ID, map("name", "Search MCP", "from", "local",
                "sseUrl", "https://example.invalid/sse", "desc", "search")), "mcpId");
        assertEquals("Search MCP", text(service.getMcp(USER_ID, ORG_ID, mcpId), "name"));
        assertTrue(list(service.listMcpTools(USER_ID, ORG_ID, map("mcpId", mcpId)).get("tools")).size() >= 1);

        String promptId = text(service.createCustomPrompt(USER_ID, ORG_ID, map("name", "ReviewPrompt",
                "desc", "review", "prompt", "review this")), "customPromptId");
        assertEquals(promptId, text(first(service.listCustomPrompts(USER_ID, ORG_ID, "Review")), "customPromptId"));
        String copiedPromptId = text(service.copyCustomPrompt(USER_ID, ORG_ID,
                map("customPromptId", promptId)), "customPromptId");
        assertNotNull(service.getCustomPrompt(USER_ID, ORG_ID, copiedPromptId));
        assertTrue(text(service.optimizePrompt(USER_ID, ORG_ID, map("prompt", "review this")), "response")
                .contains("优化后的提示词"));
    }

    private Map<String, Object> auth() {
        return map("authType", "none");
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }

    private Map<String, Object> first(Map<String, Object> listResult) {
        return list(listResult.get("list")).get(0);
    }

    private Map<String, Object> firstList(Map<String, Object> result, String key) {
        return list(result.get(key)).get(0);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        return (List<Map<String, Object>>) value;
    }

    private String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }
}
