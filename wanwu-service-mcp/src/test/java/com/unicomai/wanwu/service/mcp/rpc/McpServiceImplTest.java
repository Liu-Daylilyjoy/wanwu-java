package com.unicomai.wanwu.service.mcp.rpc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.unicomai.wanwu.service.mcp.persistence.entity.McpRecordEntity;
import com.unicomai.wanwu.service.mcp.persistence.mapper.McpRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class McpServiceImplTest {

    private static final String USER_ID = "dev-admin";
    private static final String ORG_ID = "default-org";

    private McpServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new McpServiceImpl();
    }

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

    @Test
    public void listMcpToolsFetchesRemoteStreamableTools() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>("");
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/mcp", exchange -> {
            requestBody.set(readBody(exchange));
            byte[] response = ("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"tools\":["
                    + "{\"name\":\"wanwu_remote_search\",\"description\":\"Remote search\","
                    + "\"inputSchema\":{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}},"
                    + "\"required\":[\"query\"]}}]}}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/mcp";
            Map<String, Object> result = service.listMcpTools(USER_ID, ORG_ID,
                    map("serverUrl", url, "transport", "streamable"));

            Map<String, Object> tool = firstList(result, "tools");
            assertEquals("wanwu_remote_search", text(tool, "name"));
            assertEquals("Remote search", text(tool, "description"));
            assertTrue(requestBody.get().contains("\"method\":\"tools/list\""));

            String mcpId = text(service.createMcp(USER_ID, ORG_ID,
                    map("name", "Remote MCP", "desc", "remote", "from", "custom",
                            "streamableUrl", url, "transport", "streamable")), "mcpId");
            Map<String, Object> storedResult = service.listMcpTools(USER_ID, ORG_ID, map("mcpId", mcpId));
            assertEquals("wanwu_remote_search", text(firstList(storedResult, "tools"), "name"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void callMcpServerToolExecutesBoundCustomOpenApiTool() throws Exception {
        AtomicReference<String> query = new AtomicReference<>("");
        AtomicReference<String> authorization = new AtomicReference<>("");
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/weather", exchange -> {
            query.set(exchange.getRequestURI().getQuery());
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] response = "{\"city\":\"Hangzhou\",\"weather\":\"sunny\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
        try {
            String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
            String schema = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Weather\",\"version\":\"1\"},"
                    + "\"servers\":[{\"url\":\"" + baseUrl + "\"}],"
                    + "\"paths\":{\"/weather\":{\"get\":{\"operationId\":\"get_weather\","
                    + "\"parameters\":[{\"name\":\"city\",\"in\":\"query\",\"schema\":{\"type\":\"string\"}}],"
                    + "\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";
            Map<String, Object> toolRequest = map("name", "WeatherAPI", "description", "weather",
                    "schema", schema, "apiAuth", map("authType", "api_key_header",
                            "apiKeyHeaderPrefix", "bearer", "apiKeyHeader", "Authorization",
                            "apiKeyValue", "token-001"));
            String customToolId = text(service.createCustomTool(USER_ID, ORG_ID, toolRequest), "customToolId");
            String mcpServerId = text(service.createMcpServer(USER_ID, ORG_ID, map("name", "Weather MCP")),
                    "mcpServerId");
            service.createMcpServerTool(USER_ID, ORG_ID, map("mcpServerId", mcpServerId, "id", customToolId,
                    "type", "custom", "methodName", "get_weather", "name", "Weather"));

            Map<String, Object> result = service.callMcpServerTool(USER_ID, ORG_ID, mcpServerId,
                    map("name", "get_weather", "arguments", map("query-city", "Hangzhou")));

            assertEquals(false, result.get("isError"));
            assertTrue(text(firstList(result, "content"), "text").contains("\"weather\":\"sunny\""));
            assertEquals("city=Hangzhou", query.get());
            assertEquals("Bearer token-001", authorization.get());
            Map<String, Object> structuredContent = mapValue(result.get("structuredContent"));
            assertEquals("Hangzhou", text(mapValue(structuredContent.get("response")), "city"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void callMcpServerToolExecutesDirectOpenApiTool() throws Exception {
        AtomicReference<String> query = new AtomicReference<>("");
        AtomicReference<String> requestBody = new AtomicReference<>("");
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/echo", exchange -> {
            query.set(exchange.getRequestURI().getQuery());
            requestBody.set(readBody(exchange));
            byte[] response = "{\"echo\":\"Suzhou\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
        try {
            String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
            String schema = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Echo\",\"version\":\"1\"},"
                    + "\"servers\":[{\"url\":\"" + baseUrl + "\"}],"
                    + "\"paths\":{\"/echo\":{\"post\":{\"operationId\":\"echo_city\","
                    + "\"requestBody\":{\"content\":{\"application/json\":{\"schema\":{\"type\":\"object\","
                    + "\"properties\":{\"city\":{\"type\":\"string\"}}}}}},"
                    + "\"responses\":{\"200\":{\"description\":\"ok\"}}}}}}";
            String mcpServerId = text(service.createMcpServer(USER_ID, ORG_ID, map("name", "Direct MCP")),
                    "mcpServerId");
            service.createMcpServerOpenApiTool(USER_ID, ORG_ID, map("mcpServerId", mcpServerId,
                    "name", "Direct Echo", "schema", schema, "methodNames", Collections.singletonList("echo_city"),
                    "apiAuth", map("authType", "api_key_query", "apiKeyQueryParam", "key",
                            "apiKeyValue", "secret-001")));

            Map<String, Object> result = service.callMcpServerTool(USER_ID, ORG_ID, mcpServerId,
                    map("name", "echo_city", "arguments", map("city", "Suzhou")));

            assertEquals(false, result.get("isError"));
            assertTrue(text(firstList(result, "content"), "text").contains("\"echo\":\"Suzhou\""));
            assertEquals("key=secret-001", query.get());
            assertEquals("{\"city\":\"Suzhou\"}", requestBody.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void resourceLifecycleCoversSkillResourceAndConversation() throws Exception {
        File zip = skillZip("imported-skill/SKILL.md",
                "---\nname: imported-skill\ndescription: imported\n---\n# Imported Skill\n");
        Map<String, Object> check = service.checkCustomSkill(USER_ID, ORG_ID,
                map("zipUrl", zip.getAbsolutePath()));
        assertEquals("imported-skill", text(check, "name"));

        String customSkillId = text(service.createCustomSkill(USER_ID, ORG_ID,
                map("author", "Wanwu", "zipUrl", zip.getAbsolutePath())), "skillId");
        assertFalse(customSkillId.isEmpty());
        String customVariableId = text(service.createCustomSkillConfig(USER_ID, ORG_ID,
                map("skillId", customSkillId, "variable",
                        map("name", "API Key", "variableKey", "apiKey", "variableValue", "dev"))), "id");
        assertFalse(customVariableId.isEmpty());
        assertEquals(1, list(service.getCustomSkill(USER_ID, ORG_ID, customSkillId).get("variables")).size());
        assertEquals("custom", text(first(service.listSkillSelect(USER_ID, ORG_ID, "imported", "custom")),
                "skillType"));

        assertTrue(list(service.listBuiltinSkills(USER_ID, ORG_ID, "").get("list")).size() >= 1);
        Map<String, Object> builtin = service.getBuiltinSkill(USER_ID, ORG_ID, "builtin-summary");
        assertTrue(text(builtin, "skillMarkdown").contains("Summary Skill"));
        String builtinVariableId = text(service.createBuiltinSkillConfig(USER_ID, ORG_ID,
                map("skillId", "builtin-summary", "variable",
                        map("name", "Language", "variableKey", "language", "variableValue", "zh"))), "id");
        assertFalse(builtinVariableId.isEmpty());
        assertEquals(1, list(service.getBuiltinSkill(USER_ID, ORG_ID, "builtin-summary").get("variables")).size());
        assertTrue(new String(service.downloadBuiltinSkill(USER_ID, ORG_ID, "builtin-summary")).contains("builtin"));

        assertEquals("builtin-summary", text(first(service.listSquareSkills(USER_ID, ORG_ID, "Summary")),
                "skillId"));
        service.shareSquareSkill(USER_ID, ORG_ID, map("skillId", "builtin-summary"));
        Map<String, Object> acquired = first(service.listAcquiredSkills(USER_ID, ORG_ID, "Summary"));
        String acquiredSkillId = text(acquired, "skillId");
        assertFalse(acquiredSkillId.isEmpty());
        service.createAcquiredSkillConfig(USER_ID, ORG_ID,
                map("skillId", acquiredSkillId, "variable",
                        map("name", "Token", "variableKey", "token", "variableValue", "local")));
        assertEquals(1, list(service.getAcquiredSkill(USER_ID, ORG_ID, acquiredSkillId).get("variables")).size());

        String conversationId = text(service.createSkillConversation(USER_ID, ORG_ID,
                map("title", "Build skill")), "conversationId");
        Map<String, Object> chat = service.chatSkillConversation(USER_ID, ORG_ID,
                map("conversationId", conversationId, "query", "build one"));
        assertEquals(1, chat.get("finish"));
        assertEquals(2, list(service.getSkillConversationDetail(USER_ID, ORG_ID, conversationId).get("list")).size());
        assertFalse(text(service.saveSkillConversation(USER_ID, ORG_ID,
                map("conversationId", conversationId, "skillSaveId", "save-001", "name", "Generated Skill")),
                "skillId").isEmpty());
    }

    @Test
    public void checkAndCreateCustomSkillParseZipFrontMatter() throws Exception {
        File zip = skillZip("demo-skill/SKILL.md",
                "---\nname: demo-skill\ndescription: Demo skill from zip\n---\n# Demo Skill\n");

        Map<String, Object> check = service.checkCustomSkill(USER_ID, ORG_ID, map("zipUrl", zip.getAbsolutePath()));

        assertEquals("demo-skill", text(check, "name"));
        assertEquals("Demo skill from zip", text(check, "desc"));

        String skillId = text(service.createCustomSkill(USER_ID, ORG_ID,
                map("zipUrl", zip.getAbsolutePath(), "author", "Joy")), "skillId");
        Map<String, Object> detail = service.getCustomSkill(USER_ID, ORG_ID, skillId);

        assertEquals("demo-skill", text(detail, "name"));
        assertEquals("Demo skill from zip", text(detail, "desc"));
        assertEquals("Joy", text(detail, "author"));
        assertTrue(text(detail, "skillMarkdown").contains("# Demo Skill"));
    }

    @Test
    public void checkCustomSkillRejectsZipWithoutSkillMarkdown() throws Exception {
        File zip = skillZip("demo/README.md", "# Missing skill file\n");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.checkCustomSkill(USER_ID, ORG_ID, map("zipUrl", zip.getAbsolutePath())));

        assertTrue(error.getMessage().contains("SKILL.md file not found"));
    }

    @Test
    public void mutableResourceStateIsPersistedAsSnapshotRecord() {
        McpRecordMapper mapper = mock(McpRecordMapper.class);
        McpServiceImpl persistent = new McpServiceImpl(mapper);

        persistent.createCustomTool(USER_ID, ORG_ID, map("name", "PersistTool", "description", "persist",
                "schema", "{\"openapi\":\"3.0.1\",\"paths\":{}}", "apiAuth", auth()));
        persistent.createCustomPrompt(USER_ID, ORG_ID, map("name", "PersistPrompt", "desc", "persist",
                "prompt", "persist this"));

        ArgumentCaptor<McpRecordEntity> captor = ArgumentCaptor.forClass(McpRecordEntity.class);
        verify(mapper, atLeastOnce()).upsertRecord(captor.capture());
        McpRecordEntity last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("snapshot", last.getRecordType());
        assertEquals("state", last.getRecordId());
        assertTrue(last.getPayload().contains("PersistTool"));
        assertTrue(last.getPayload().contains("PersistPrompt"));
    }

    @Test
    public void persistedSnapshotIsLoadedAndVariableSequenceContinuesAfterRestart() {
        McpRecordMapper sourceMapper = mock(McpRecordMapper.class);
        McpServiceImpl source = new McpServiceImpl(sourceMapper);
        source.createCustomTool(USER_ID, ORG_ID, map("name", "RestartTool", "description", "persist",
                "schema", "{\"openapi\":\"3.0.1\",\"paths\":{}}", "apiAuth", auth()));
        source.createCustomPrompt(USER_ID, ORG_ID, map("name", "RestartPrompt", "desc", "persist",
                "prompt", "restart"));
        String skillId = text(source.createCustomSkill(USER_ID, ORG_ID, map("name", "RestartSkill",
                "author", "Wanwu", "desc", "persist", "zipUrl", "file-upload/skill.zip")), "skillId");
        String firstVariableId = text(source.createCustomSkillConfig(USER_ID, ORG_ID,
                map("skillId", skillId, "variable",
                        map("name", "Token", "variableKey", "token", "variableValue", "one"))), "id");
        assertEquals("var-1", firstVariableId);

        ArgumentCaptor<McpRecordEntity> captor = ArgumentCaptor.forClass(McpRecordEntity.class);
        verify(sourceMapper, atLeastOnce()).upsertRecord(captor.capture());
        String payload = captor.getAllValues().get(captor.getAllValues().size() - 1).getPayload();

        McpRecordMapper restartMapper = mock(McpRecordMapper.class);
        when(restartMapper.selectByType(eq("snapshot")))
                .thenReturn(Collections.singletonList(record("snapshot", "state", payload)));
        McpServiceImpl restarted = new McpServiceImpl(restartMapper);

        assertEquals("RestartTool", text(first(restarted.listCustomTools(USER_ID, ORG_ID, "RestartTool")), "name"));
        assertEquals("RestartPrompt", text(first(restarted.listCustomPrompts(USER_ID, ORG_ID, "Restart")), "name"));
        assertEquals(1, list(restarted.getCustomSkill(USER_ID, ORG_ID, skillId).get("variables")).size());
        String nextVariableId = text(restarted.createCustomSkillConfig(USER_ID, ORG_ID,
                map("skillId", skillId, "variable",
                        map("name", "Token2", "variableKey", "token2", "variableValue", "two"))), "id");
        assertEquals("var-2", nextVariableId);
        verify(restartMapper, atLeastOnce()).upsertRecord(any(McpRecordEntity.class));
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        return (Map<String, Object>) value;
    }

    private String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private McpRecordEntity record(String type, String id, String payload) {
        McpRecordEntity record = new McpRecordEntity();
        record.setRecordType(type);
        record.setRecordId(id);
        record.setPayload(payload);
        record.setCreatedAt(1L);
        record.setUpdatedAt(1L);
        return record;
    }

    private File skillZip(String entryName, String content) throws IOException {
        File file = File.createTempFile("wanwu-skill", ".zip");
        file.deleteOnExit();
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return file;
    }

    private String readBody(HttpExchange exchange) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuilder builder = new StringBuilder();
        int read;
        while ((read = exchange.getRequestBody().read(buffer)) != -1) {
            builder.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }
}
