package com.unicomai.wanwu.service.mcp.rpc;

import com.unicomai.wanwu.service.mcp.persistence.entity.McpRecordEntity;
import com.unicomai.wanwu.service.mcp.persistence.mapper.McpRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    public void resourceLifecycleCoversSkillResourceAndConversation() {
        Map<String, Object> check = service.checkCustomSkill(USER_ID, ORG_ID,
                map("zipUrl", "file-upload/skill.zip", "name", "Imported Skill", "desc", "imported"));
        assertEquals("Imported Skill", text(check, "name"));

        String customSkillId = text(service.createCustomSkill(USER_ID, ORG_ID,
                map("name", "Imported Skill", "author", "Wanwu", "desc", "imported",
                        "zipUrl", "file-upload/skill.zip")), "skillId");
        assertFalse(customSkillId.isEmpty());
        String customVariableId = text(service.createCustomSkillConfig(USER_ID, ORG_ID,
                map("skillId", customSkillId, "variable",
                        map("name", "API Key", "variableKey", "apiKey", "variableValue", "dev"))), "id");
        assertFalse(customVariableId.isEmpty());
        assertEquals(1, list(service.getCustomSkill(USER_ID, ORG_ID, customSkillId).get("variables")).size());
        assertEquals("custom", text(first(service.listSkillSelect(USER_ID, ORG_ID, "Imported", "custom")),
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
}
