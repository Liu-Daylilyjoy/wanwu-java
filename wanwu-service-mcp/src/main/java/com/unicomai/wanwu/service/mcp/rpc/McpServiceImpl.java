package com.unicomai.wanwu.service.mcp.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.mcp.McpService;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import com.unicomai.wanwu.service.mcp.persistence.entity.McpRecordEntity;
import com.unicomai.wanwu.service.mcp.persistence.mapper.McpRecordMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class McpServiceImpl implements McpService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String DEFAULT_ORG = "default-org";
    private static final String TYPE_SNAPSHOT = "snapshot";
    private static final String SNAPSHOT_ID = "state";

    private final ConcurrentMap<String, Map<String, Object>> customTools = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Object>> customMcps = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Object>> mcpServers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Object>> customPrompts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Object>> customSkills = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Object>> acquiredSkills = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Object>> builtinSkillVariableStores = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Object>> skillConversations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> builtinToolApiKeys = new ConcurrentHashMap<>();
    private final AtomicInteger variableSequence = new AtomicInteger(1);

    @Autowired(required = false)
    private McpRecordMapper mcpRecordMapper;

    public McpServiceImpl() {
    }

    McpServiceImpl(McpRecordMapper mcpRecordMapper) {
        this.mcpRecordMapper = mcpRecordMapper;
        loadPersistedSnapshot();
    }

    @PostConstruct
    synchronized void loadPersistedSnapshot() {
        if (mcpRecordMapper == null) {
            return;
        }
        List<McpRecordEntity> records = mcpRecordMapper.selectByType(TYPE_SNAPSHOT);
        if (records == null || records.isEmpty()) {
            return;
        }
        McpRecordEntity record = records.get(records.size() - 1);
        try {
            McpSnapshot snapshot = JSON.readValue(record.getPayload(), McpSnapshot.class);
            applySnapshot(snapshot);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load persisted MCP snapshot", ex);
        }
    }

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.MCP, "MCP Service", "mcp");
    }

    @Override
    public Map<String, Object> createCustomTool(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> item = customToolBase(request);
        String id = id("tool");
        item.put("customToolId", id);
        item.put("ownerUserId", userId);
        item.put("ownerOrgId", org(orgId));
        item.put("apiList", apiList(item.get("schema")));
        customTools.put(scoped(orgId, id), item);
        saveSnapshot();
        return singleton("customToolId", id);
    }

    @Override
    public Map<String, Object> getCustomTool(String userId, String orgId, String customToolId) {
        Map<String, Object> item = require(customTools, orgId, customToolId, "custom tool");
        Map<String, Object> result = copy(item);
        result.put("apiList", apiList(result.get("schema")));
        return result;
    }

    @Override
    public void updateCustomTool(String userId, String orgId, Map<String, Object> request) {
        String id = text(request, "customToolId");
        Map<String, Object> current = require(customTools, orgId, id, "custom tool");
        Map<String, Object> updated = customToolBase(request);
        updated.put("customToolId", id);
        updated.put("ownerUserId", current.get("ownerUserId"));
        updated.put("ownerOrgId", current.get("ownerOrgId"));
        updated.put("apiList", apiList(updated.get("schema")));
        customTools.put(scoped(orgId, id), updated);
        saveSnapshot();
    }

    @Override
    public void deleteCustomTool(String userId, String orgId, Map<String, Object> request) {
        customTools.remove(scoped(orgId, text(request, "customToolId")));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listCustomTools(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : customTools.values()) {
            if (sameOrg(item, orgId) && matches(item, "name", name)) {
                Map<String, Object> row = copy(item);
                row.remove("schema");
                row.remove("apiAuth");
                row.remove("privacyPolicy");
                row.remove("apiList");
                list.add(row);
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> parseCustomToolSchema(String userId, String orgId, Map<String, Object> request) {
        return listResult(apiList(text(request, "schema")));
    }

    @Override
    public Map<String, Object> listToolSquares(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : builtinTools()) {
            if (matches(item, "name", name)) {
                list.add(toolSquareInfo(item));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> getToolSquare(String userId, String orgId, String toolSquareId) {
        Map<String, Object> item = builtinTool(toolSquareId);
        Map<String, Object> result = toolSquareInfo(item);
        result.put("needApiKeyInput", item.get("needApiKeyInput"));
        result.put("apiKey", builtinToolApiKeys.getOrDefault(scoped(orgId, toolSquareId), ""));
        result.put("apiAuth", defaultApiAuth());
        result.put("tools", item.get("tools"));
        result.put("detail", item.get("detail"));
        result.put("actionSum", ((List<?>) item.get("tools")).size());
        result.put("schema", item.get("schema"));
        return result;
    }

    @Override
    public void updateToolSquareApiKey(String userId, String orgId, Map<String, Object> request) {
        String id = text(request, "toolSquareId");
        builtinTool(id);
        builtinToolApiKeys.put(scoped(orgId, id), text(request, "apiKey"));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listToolSelect(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : builtinTools()) {
            if (matches(item, "name", name)) {
                list.add(toolSelect(item, "builtin", text(item, "toolSquareId")));
            }
        }
        for (Map<String, Object> item : customTools.values()) {
            if (sameOrg(item, orgId) && matches(item, "name", name)) {
                list.add(toolSelect(item, "custom", text(item, "customToolId")));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> listToolActions(String userId, String orgId, String toolId, String toolType) {
        return singleton("actions", toolActions(orgId, toolId, toolType));
    }

    @Override
    public Map<String, Object> getToolActionDetail(String userId, String orgId, String toolId, String toolType,
                                                   String actionName) {
        Map<String, Object> detail = new LinkedHashMap<>();
        Map<String, Object> source = "custom".equals(toolType)
                ? require(customTools, orgId, toolId, "custom tool")
                : builtinTool(toolId);
        detail.put("needApiKeyInput", source.get("needApiKeyInput") == null ? false : source.get("needApiKeyInput"));
        detail.put("apiKey", builtinToolApiKeys.getOrDefault(scoped(orgId, toolId), ""));
        detail.put("action", findAction(toolActions(orgId, toolId, toolType), actionName));
        return detail;
    }

    @Override
    public Map<String, Object> createMcp(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> item = mcpBase(request);
        String id = id("mcp");
        item.put("mcpId", id);
        item.put("ownerUserId", userId);
        item.put("ownerOrgId", org(orgId));
        item.put("tools", sampleMcpTools(text(item, "name")));
        customMcps.put(scoped(orgId, id), item);
        saveSnapshot();
        return singleton("mcpId", id);
    }

    @Override
    public Map<String, Object> getMcp(String userId, String orgId, String mcpId) {
        return mcpDetail(require(customMcps, orgId, mcpId, "mcp"));
    }

    @Override
    public void updateMcp(String userId, String orgId, Map<String, Object> request) {
        String id = text(request, "mcpId");
        Map<String, Object> current = require(customMcps, orgId, id, "mcp");
        Map<String, Object> updated = mcpBase(request);
        updated.put("mcpId", id);
        updated.put("ownerUserId", current.get("ownerUserId"));
        updated.put("ownerOrgId", current.get("ownerOrgId"));
        updated.put("tools", current.get("tools"));
        customMcps.put(scoped(orgId, id), updated);
        saveSnapshot();
    }

    @Override
    public void deleteMcp(String userId, String orgId, Map<String, Object> request) {
        customMcps.remove(scoped(orgId, text(request, "mcpId")));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listMcps(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : customMcps.values()) {
            if (sameOrg(item, orgId) && matches(item, "name", name)) {
                list.add(mcpInfo(item));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> listMcpTools(String userId, String orgId, Map<String, Object> request) {
        String mcpId = text(request, "mcpId");
        if (!blank(mcpId)) {
            return singleton("tools", copyList(listValue(require(customMcps, orgId, mcpId, "mcp").get("tools"))));
        }
        return singleton("tools", sampleMcpTools(text(request, "serverUrl")));
    }

    @Override
    public Map<String, Object> createMcpServer(String userId, String orgId, Map<String, Object> request) {
        String id = id("mcpserver");
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("mcpServerId", id);
        item.put("avatar", avatar(request));
        item.put("name", defaultText(request, "name", "MCP Server"));
        item.put("desc", defaultText(request, "desc", ""));
        item.put("ownerUserId", userId);
        item.put("ownerOrgId", org(orgId));
        item.put("tools", new ArrayList<Map<String, Object>>());
        mcpServers.put(scoped(orgId, id), item);
        saveSnapshot();
        return singleton("mcpServerId", id);
    }

    @Override
    public Map<String, Object> getMcpServer(String userId, String orgId, String mcpServerId) {
        Map<String, Object> item = require(mcpServers, orgId, mcpServerId, "mcp server");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mcpServerId", item.get("mcpServerId"));
        result.put("avatar", item.get("avatar"));
        result.put("name", item.get("name"));
        result.put("desc", item.get("desc"));
        result.put("sseUrl", "/openapi/mcp/server/sse?mcpServerId=" + item.get("mcpServerId"));
        result.put("sseExample", "curl -N '" + result.get("sseUrl") + "'");
        result.put("streamableUrl", "/openapi/mcp/server/streamable?mcpServerId=" + item.get("mcpServerId"));
        result.put("streamableExample", "curl -X POST '" + result.get("streamableUrl") + "'");
        result.put("tools", copyList(listValue(item.get("tools"))));
        return result;
    }

    @Override
    public void updateMcpServer(String userId, String orgId, Map<String, Object> request) {
        String id = firstText(request, "mcpServerId", "MCPServerId");
        Map<String, Object> current = require(mcpServers, orgId, id, "mcp server");
        current.put("avatar", avatar(request));
        current.put("name", defaultText(request, "name", text(current, "name")));
        current.put("desc", defaultText(request, "desc", text(current, "desc")));
        saveSnapshot();
    }

    @Override
    public void deleteMcpServer(String userId, String orgId, Map<String, Object> request) {
        mcpServers.remove(scoped(orgId, firstText(request, "mcpServerId", "MCPServerId")));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listMcpServers(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : mcpServers.values()) {
            if (sameOrg(item, orgId) && matches(item, "name", name)) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("mcpServerId", item.get("mcpServerId"));
                row.put("avatar", item.get("avatar"));
                row.put("name", item.get("name"));
                row.put("desc", item.get("desc"));
                row.put("toolNum", listValue(item.get("tools")).size());
                list.add(row);
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> createMcpServerTool(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> server = require(mcpServers, orgId, text(request, "mcpServerId"), "mcp server");
        Map<String, Object> tool = serverTool(orgId, request);
        listValue(server.get("tools")).add(tool);
        saveSnapshot();
        return singleton("mcpServerToolId", tool.get("mcpServerToolId"));
    }

    @Override
    public void updateMcpServerTool(String userId, String orgId, Map<String, Object> request) {
        String id = text(request, "mcpServerToolId");
        for (Map<String, Object> server : mcpServers.values()) {
            for (Map<String, Object> tool : listValue(server.get("tools"))) {
                if (id.equals(text(tool, "mcpServerToolId"))) {
                    tool.put("methodName", defaultText(request, "methodName", text(tool, "methodName")));
                    tool.put("desc", defaultText(request, "desc", text(tool, "desc")));
                    saveSnapshot();
                    return;
                }
            }
        }
        throw new IllegalArgumentException("mcp server tool not found: " + id);
    }

    @Override
    public void deleteMcpServerTool(String userId, String orgId, Map<String, Object> request) {
        String id = text(request, "mcpServerToolId");
        for (Map<String, Object> server : mcpServers.values()) {
            listValue(server.get("tools")).removeIf(tool -> id.equals(text(tool, "mcpServerToolId")));
        }
        saveSnapshot();
    }

    @Override
    public Map<String, Object> createMcpServerOpenApiTool(String userId, String orgId, Map<String, Object> request) {
        String serverId = text(request, "mcpServerId");
        Map<String, Object> server = require(mcpServers, orgId, serverId, "mcp server");
        List<Map<String, Object>> created = new ArrayList<>();
        for (String methodName : stringList(request.get("methodNames"))) {
            Map<String, Object> create = new LinkedHashMap<>();
            create.put("mcpServerId", serverId);
            create.put("id", "openapi-" + compact(methodName));
            create.put("type", "openapi");
            create.put("methodName", methodName);
            create.put("name", defaultText(request, "name", "OpenAPI Tool"));
            create.put("desc", "OpenAPI action " + methodName);
            Map<String, Object> tool = serverTool(orgId, create);
            listValue(server.get("tools")).add(tool);
            created.add(tool);
        }
        saveSnapshot();
        return listResult(created);
    }

    @Override
    public Map<String, Object> listMcpSelect(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : customMcps.values()) {
            if (sameOrg(item, orgId) && matches(item, "name", name)) {
                list.add(mcpSelect(item, "mcp"));
            }
        }
        for (Map<String, Object> item : mcpServers.values()) {
            if (sameOrg(item, orgId) && matches(item, "name", name)) {
                list.add(mcpSelect(item, "mcpserver"));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> listMcpActions(String userId, String orgId, String toolId, String toolType) {
        if ("mcpserver".equals(toolType)) {
            Map<String, Object> server = require(mcpServers, orgId, toolId, "mcp server");
            List<Map<String, Object>> actions = new ArrayList<>();
            for (Map<String, Object> tool : listValue(server.get("tools"))) {
                actions.add(action(text(tool, "methodName"), text(tool, "desc")));
            }
            return singleton("actions", actions);
        }
        return singleton("actions", copyList(listValue(require(customMcps, orgId, toolId, "mcp").get("tools"))));
    }

    @Override
    public Map<String, Object> listMcpSquares(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : mcpSquares()) {
            if (matches(item, "name", name)) {
                list.add(mcpSquareInfo(item));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> getMcpSquare(String userId, String orgId, String mcpSquareId) {
        return mcpSquareDetail(mcpSquare(mcpSquareId));
    }

    @Override
    public Map<String, Object> recommendMcpSquares(String userId, String orgId, String mcpSquareId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : mcpSquares()) {
            if (!text(item, "mcpSquareId").equals(mcpSquareId)) {
                list.add(mcpSquareInfo(item));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> createCustomPrompt(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> item = promptBase(request);
        String id = id("prompt");
        item.put("customPromptId", id);
        item.put("ownerUserId", userId);
        item.put("ownerOrgId", org(orgId));
        customPrompts.put(scoped(orgId, id), item);
        saveSnapshot();
        return singleton("customPromptId", id);
    }

    @Override
    public Map<String, Object> getCustomPrompt(String userId, String orgId, String customPromptId) {
        return copy(require(customPrompts, orgId, customPromptId, "custom prompt"));
    }

    @Override
    public void updateCustomPrompt(String userId, String orgId, Map<String, Object> request) {
        String id = text(request, "customPromptId");
        Map<String, Object> current = require(customPrompts, orgId, id, "custom prompt");
        Map<String, Object> updated = promptBase(request);
        updated.put("customPromptId", id);
        updated.put("ownerUserId", current.get("ownerUserId"));
        updated.put("ownerOrgId", current.get("ownerOrgId"));
        customPrompts.put(scoped(orgId, id), updated);
        saveSnapshot();
    }

    @Override
    public void deleteCustomPrompt(String userId, String orgId, Map<String, Object> request) {
        customPrompts.remove(scoped(orgId, text(request, "customPromptId")));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listCustomPrompts(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : customPrompts.values()) {
            if (sameOrg(item, orgId) && matches(item, "name", name)) {
                list.add(copy(item));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> copyCustomPrompt(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> source = require(customPrompts, orgId, text(request, "customPromptId"), "custom prompt");
        Map<String, Object> copy = copy(source);
        String id = id("prompt");
        copy.put("customPromptId", id);
        copy.put("name", text(source, "name") + " Copy");
        copy.put("updateAt", now());
        customPrompts.put(scoped(orgId, id), copy);
        saveSnapshot();
        return singleton("customPromptId", id);
    }

    @Override
    public Map<String, Object> listPromptTemplates(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : promptTemplates()) {
            if (matches(item, "name", name)) {
                list.add(copy(item));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> getPromptTemplate(String userId, String orgId, String templateId) {
        return copy(promptTemplate(templateId));
    }

    @Override
    public Map<String, Object> createPromptByTemplate(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> template = promptTemplate(text(request, "templateId"));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", defaultText(request, "name", text(template, "name")));
        item.put("desc", defaultText(request, "desc", text(template, "desc")));
        item.put("avatar", request.get("avatar") instanceof Map ? request.get("avatar") : template.get("avatar"));
        item.put("prompt", text(template, "prompt"));
        return createCustomPrompt(userId, orgId, item);
    }

    @Override
    public Map<String, Object> optimizePrompt(String userId, String orgId, Map<String, Object> request) {
        return promptStreamPayload("优化后的提示词：\n" + defaultText(request, "prompt", ""));
    }

    @Override
    public Map<String, Object> reasonPrompt(String userId, String orgId, Map<String, Object> request) {
        return promptStreamPayload("根据提示词生成的本地推理结果：" + defaultText(request, "prompt", ""));
    }

    @Override
    public Map<String, Object> evaluatePrompt(String userId, String orgId, Map<String, Object> request) {
        String answer = defaultText(request, "answer", "");
        String expected = defaultText(request, "expectedOutput", "");
        return promptStreamPayload("评估结果：本地响应可用。answer=" + answer + " expected=" + expected);
    }

    @Override
    public Map<String, Object> createCustomSkill(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> item = customSkillBase(request);
        String id = id("skill");
        item.put("skillId", id);
        item.put("ownerUserId", userId);
        item.put("ownerOrgId", org(orgId));
        item.put("variables", new ArrayList<Map<String, Object>>());
        customSkills.put(scoped(orgId, id), item);
        saveSnapshot();
        return singleton("skillId", id);
    }

    @Override
    public Map<String, Object> getCustomSkill(String userId, String orgId, String skillId) {
        return customSkillDetail(require(customSkills, orgId, skillId, "custom skill"));
    }

    @Override
    public void deleteCustomSkill(String userId, String orgId, Map<String, Object> request) {
        customSkills.remove(scoped(orgId, text(request, "skillId")));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listCustomSkills(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : customSkills.values()) {
            if (sameOrg(item, orgId) && matches(item, "name", name)) {
                list.add(customSkillInfo(item));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> checkCustomSkill(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", defaultText(request, "name", "Imported Skill"));
        result.put("desc", defaultText(request, "desc", "Validated local skill package: " + text(request, "zipUrl")));
        return result;
    }

    @Override
    public Map<String, Object> createCustomSkillConfig(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> result = createSkillVariable(require(customSkills, orgId, text(request, "skillId"),
                "custom skill"), request);
        saveSnapshot();
        return result;
    }

    @Override
    public void updateCustomSkillConfig(String userId, String orgId, Map<String, Object> request) {
        updateSkillVariable(orgId, text(request, "id"), request);
        saveSnapshot();
    }

    @Override
    public void deleteCustomSkillConfig(String userId, String orgId, Map<String, Object> request) {
        deleteSkillVariable(orgId, text(request, "id"));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listBuiltinSkills(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : builtinSkills()) {
            if (matches(item, "name", name)) {
                list.add(skillDetail(item, skillVariables(builtinSkillVariableItem(orgId, text(item, "skillId"))),
                        false));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> getBuiltinSkill(String userId, String orgId, String skillId) {
        Map<String, Object> item = builtinSkill(skillId);
        return skillDetail(item, skillVariables(builtinSkillVariableItem(orgId, skillId)), true);
    }

    @Override
    public byte[] downloadBuiltinSkill(String userId, String orgId, String skillId) {
        builtinSkill(skillId);
        return skillArchive(skillId, "builtin");
    }

    @Override
    public Map<String, Object> createBuiltinSkillConfig(String userId, String orgId, Map<String, Object> request) {
        String skillId = text(request, "skillId");
        builtinSkill(skillId);
        Map<String, Object> result = createSkillVariable(builtinSkillVariableItem(orgId, skillId), request);
        saveSnapshot();
        return result;
    }

    @Override
    public void updateBuiltinSkillConfig(String userId, String orgId, Map<String, Object> request) {
        updateSkillVariable(orgId, text(request, "id"), request);
        saveSnapshot();
    }

    @Override
    public void deleteBuiltinSkillConfig(String userId, String orgId, Map<String, Object> request) {
        deleteSkillVariable(orgId, text(request, "id"));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listSkillSelect(String userId, String orgId, String name, String skillType) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (blank(skillType) || "builtin".equals(skillType)) {
            for (Map<String, Object> item : builtinSkills()) {
                if (matches(item, "name", name)) {
                    list.add(skillSelect(item, "builtin"));
                }
            }
        }
        if (blank(skillType) || "custom".equals(skillType)) {
            for (Map<String, Object> item : customSkills.values()) {
                if (sameOrg(item, orgId) && matches(item, "name", name)
                        && !blank(text(item, "name")) && !blank(text(item, "desc"))) {
                    list.add(skillSelect(item, "custom"));
                }
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> listAcquiredSkills(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : acquiredSkills.values()) {
            if (sameOrg(item, orgId) && matches(item, "name", name)) {
                list.add(acquiredSkillDetail(item, false));
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> getAcquiredSkill(String userId, String orgId, String skillId) {
        return acquiredSkillDetail(require(acquiredSkills, orgId, skillId, "acquired skill"), true);
    }

    @Override
    public void deleteAcquiredSkill(String userId, String orgId, Map<String, Object> request) {
        acquiredSkills.remove(scoped(orgId, text(request, "skillId")));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> createAcquiredSkillConfig(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> result = createSkillVariable(require(acquiredSkills, orgId, text(request, "skillId"),
                "acquired skill"), request);
        saveSnapshot();
        return result;
    }

    @Override
    public void updateAcquiredSkillConfig(String userId, String orgId, Map<String, Object> request) {
        updateSkillVariable(orgId, text(request, "id"), request);
        saveSnapshot();
    }

    @Override
    public void deleteAcquiredSkillConfig(String userId, String orgId, Map<String, Object> request) {
        deleteSkillVariable(orgId, text(request, "id"));
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listSquareSkills(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : builtinSkills()) {
            if (matches(item, "name", name)) {
                Map<String, Object> row = squareSkillInfo(item, isAcquired(orgId, text(item, "skillId")));
                list.add(row);
            }
        }
        return listResult(list);
    }

    @Override
    public Map<String, Object> listSquareBuiltinSkills(String userId, String orgId, String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> item : builtinSkills()) {
            if (matches(item, "name", name)) {
                list.add(squareBuiltinSkillInfo(item));
            }
        }
        return listResult(list);
    }

    @Override
    public void shareSquareSkill(String userId, String orgId, Map<String, Object> request) {
        String squareSkillId = text(request, "skillId");
        Map<String, Object> source = builtinSkill(squareSkillId);
        for (Map<String, Object> acquired : acquiredSkills.values()) {
            if (sameOrg(acquired, orgId) && squareSkillId.equals(text(acquired, "squareSkillId"))) {
                return;
            }
        }
        Map<String, Object> item = acquiredSkillFromSquare(source);
        String id = id("acquired");
        item.put("skillId", id);
        item.put("ownerUserId", userId);
        item.put("ownerOrgId", org(orgId));
        item.put("variables", new ArrayList<Map<String, Object>>());
        acquiredSkills.put(scoped(orgId, id), item);
        saveSnapshot();
    }

    @Override
    public Map<String, Object> getSquareSkill(String userId, String orgId, String skillId) {
        Map<String, Object> source = builtinSkill(skillId);
        Map<String, Object> result = squareSkillInfo(source, isAcquired(orgId, skillId));
        result.put("skillMarkdown", source.get("skillMarkdown"));
        result.put("downloadUrl", "/user/api/v1/square/skill/download?skillId=" + skillId);
        return result;
    }

    @Override
    public byte[] downloadSquareSkill(String userId, String orgId, String skillId) {
        builtinSkill(skillId);
        return skillArchive(skillId, "square");
    }

    @Override
    public Map<String, Object> createSkillConversation(String userId, String orgId, Map<String, Object> request) {
        String id = id("skillconv");
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("conversationId", id);
        item.put("title", defaultText(request, "title", "Skill Conversation"));
        item.put("createdAt", now());
        item.put("ownerUserId", userId);
        item.put("ownerOrgId", org(orgId));
        item.put("messages", new ArrayList<Map<String, Object>>());
        skillConversations.put(scoped(orgId, id), item);
        saveSnapshot();
        return singleton("conversationId", id);
    }

    @Override
    public void deleteSkillConversation(String userId, String orgId, Map<String, Object> request) {
        skillConversations.remove(scoped(orgId, text(request, "conversationId")));
        saveSnapshot();
    }

    @Override
    public void clearSkillConversation(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> item = require(skillConversations, orgId, text(request, "conversationId"),
                "skill conversation");
        item.put("messages", new ArrayList<Map<String, Object>>());
        saveSnapshot();
    }

    @Override
    public Map<String, Object> listSkillConversations(String userId, String orgId, int pageNo, int pageSize) {
        List<Map<String, Object>> all = new ArrayList<>();
        for (Map<String, Object> item : skillConversations.values()) {
            if (sameOrg(item, orgId)) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("conversationId", item.get("conversationId"));
                row.put("title", item.get("title"));
                row.put("createdAt", item.get("createdAt"));
                all.add(row);
            }
        }
        int safePageNo = pageNo <= 0 ? 1 : pageNo;
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int from = Math.min((safePageNo - 1) * safePageSize, all.size());
        int to = Math.min(from + safePageSize, all.size());
        Map<String, Object> result = listResult(new ArrayList<>(all.subList(from, to)));
        result.put("pageNo", safePageNo);
        result.put("pageSize", safePageSize);
        result.put("total", all.size());
        return result;
    }

    @Override
    public Map<String, Object> getSkillConversationDetail(String userId, String orgId, String conversationId) {
        Map<String, Object> item = require(skillConversations, orgId, conversationId, "skill conversation");
        return listResult(copyList(listValue(item.get("messages"))));
    }

    @Override
    public Map<String, Object> chatSkillConversation(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> item = require(skillConversations, orgId, text(request, "conversationId"),
                "skill conversation");
        String query = defaultText(request, "query", "");
        String response = "Generated local skill draft for: " + query;
        Map<String, Object> userMessage = conversationMessage("user", query, Collections.<Map<String, Object>>emptyList());
        Map<String, Object> assistantMessage = conversationMessage("assistant", response,
                Collections.singletonList(skillResponseFile(query)));
        listValue(item.get("messages")).add(userMessage);
        listValue(item.get("messages")).add(assistantMessage);
        saveSnapshot();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", null);
        payload.put("message", "success");
        payload.put("response", response);
        payload.put("finish", 1);
        payload.put("responseFiles", assistantMessage.get("responseFiles"));
        payload.put("usage", null);
        return payload;
    }

    @Override
    public Map<String, Object> saveSkillConversation(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> create = new LinkedHashMap<>();
        create.put("name", defaultText(request, "name", "Generated Skill"));
        create.put("desc", defaultText(request, "desc", "Saved from local skill conversation"));
        create.put("author", defaultText(request, "author", "Wanwu"));
        create.put("zipUrl", defaultText(request, "skillSaveId", ""));
        create.put("sourceType", "skill_conversation");
        return createCustomSkill(userId, orgId, create);
    }

    private Map<String, Object> customToolBase(Map<String, Object> request) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("avatar", avatar(request));
        item.put("name", defaultText(request, "name", "Custom Tool"));
        item.put("description", defaultText(request, "description", ""));
        item.put("desc", defaultText(request, "description", ""));
        item.put("apiAuth", request == null || !(request.get("apiAuth") instanceof Map)
                ? defaultApiAuth()
                : copy(castMap(request.get("apiAuth"))));
        item.put("schema", defaultText(request, "schema", "{}"));
        item.put("privacyPolicy", defaultText(request, "privacyPolicy", ""));
        item.put("needApiKeyInput", false);
        return item;
    }

    private Map<String, Object> mcpBase(Map<String, Object> request) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("mcpSquareId", defaultText(request, "mcpSquareId", ""));
        item.put("avatar", avatar(request));
        item.put("name", defaultText(request, "name", "Custom MCP"));
        item.put("desc", defaultText(request, "desc", ""));
        item.put("from", defaultText(request, "from", "custom"));
        item.put("sseUrl", defaultText(request, "sseUrl", ""));
        item.put("streamableUrl", defaultText(request, "streamableUrl", ""));
        item.put("transport", defaultText(request, "transport", "sse"));
        item.put("category", "custom");
        return item;
    }

    private Map<String, Object> promptBase(Map<String, Object> request) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("avatar", avatar(request));
        item.put("name", defaultText(request, "name", "Prompt"));
        item.put("desc", defaultText(request, "desc", ""));
        item.put("prompt", defaultText(request, "prompt", ""));
        item.put("updateAt", now());
        return item;
    }

    private Map<String, Object> customSkillBase(Map<String, Object> request) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("avatar", avatar(request));
        item.put("name", defaultText(request, "name", "Imported Skill"));
        item.put("author", defaultText(request, "author", "Wanwu"));
        item.put("desc", defaultText(request, "desc", "Imported local skill package."));
        item.put("zipUrl", defaultText(request, "zipUrl", ""));
        item.put("objectPath", defaultText(request, "zipUrl", ""));
        item.put("sourceType", defaultText(request, "sourceType", "skill_import"));
        item.put("threadId", defaultText(request, "threadId", ""));
        item.put("previewId", defaultText(request, "previewId", ""));
        return item;
    }

    private Map<String, Object> customSkillInfo(Map<String, Object> item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("skillId", item.get("skillId"));
        row.put("name", item.get("name"));
        row.put("avatar", item.get("avatar"));
        row.put("author", item.get("author"));
        row.put("desc", item.get("desc"));
        row.put("threadId", item.get("threadId"));
        row.put("previewId", item.get("previewId"));
        return row;
    }

    private Map<String, Object> customSkillDetail(Map<String, Object> item) {
        Map<String, Object> row = customSkillInfo(item);
        row.put("variables", skillVariables(item));
        return row;
    }

    private Map<String, Object> skillDetail(Map<String, Object> item, List<Map<String, Object>> variables,
                                            boolean includeMarkdown) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("skillId", item.get("skillId"));
        row.put("name", item.get("name"));
        row.put("avatar", item.get("avatar"));
        row.put("author", item.get("author"));
        row.put("desc", item.get("desc"));
        if (includeMarkdown) {
            row.put("skillMarkdown", item.get("skillMarkdown"));
        }
        row.put("variables", copyList(variables));
        return row;
    }

    private Map<String, Object> skillSelect(Map<String, Object> item, String type) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("skillId", item.get("skillId"));
        row.put("skillName", item.get("name"));
        row.put("skillType", type);
        row.put("desc", item.get("desc"));
        row.put("author", item.get("author"));
        row.put("avatar", item.get("avatar"));
        return row;
    }

    private Map<String, Object> acquiredSkillFromSquare(Map<String, Object> source) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("squareSkillId", source.get("skillId"));
        item.put("name", source.get("name"));
        item.put("avatar", source.get("avatar"));
        item.put("author", source.get("author"));
        item.put("desc", source.get("desc"));
        item.put("skillMarkdown", source.get("skillMarkdown"));
        item.put("objectPath", "builtin://" + source.get("skillId"));
        item.put("downloadUrl", "/user/api/v1/square/skill/download?skillId=" + source.get("skillId"));
        return item;
    }

    private Map<String, Object> acquiredSkillDetail(Map<String, Object> item, boolean includeVariables) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("skillId", item.get("skillId"));
        row.put("squareSkillId", item.get("squareSkillId"));
        row.put("name", item.get("name"));
        row.put("avatar", item.get("avatar"));
        row.put("author", item.get("author"));
        row.put("desc", item.get("desc"));
        row.put("skillMarkdown", item.get("skillMarkdown"));
        row.put("downloadUrl", item.get("downloadUrl"));
        if (includeVariables) {
            row.put("variables", skillVariables(item));
        }
        return row;
    }

    private Map<String, Object> squareSkillInfo(Map<String, Object> item, boolean shared) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("skillId", item.get("skillId"));
        row.put("name", item.get("name"));
        row.put("avatar", item.get("avatar"));
        row.put("author", item.get("author"));
        row.put("desc", item.get("desc"));
        row.put("isShared", shared);
        return row;
    }

    private Map<String, Object> squareBuiltinSkillInfo(Map<String, Object> item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("skillId", item.get("skillId"));
        row.put("name", item.get("name"));
        row.put("avatar", item.get("avatar"));
        row.put("author", item.get("author"));
        row.put("desc", item.get("desc"));
        return row;
    }

    private Map<String, Object> createSkillVariable(Map<String, Object> item, Map<String, Object> request) {
        Map<String, Object> variable = request != null && request.get("variable") instanceof Map
                ? castMap(request.get("variable"))
                : Collections.<String, Object>emptyMap();
        Map<String, Object> row = skillVariable(variable);
        listValue(item.get("variables")).add(row);
        return singleton("id", row.get("id"));
    }

    private void updateSkillVariable(String orgId, String variableId, Map<String, Object> request) {
        Map<String, Object> variable = request != null && request.get("variable") instanceof Map
                ? castMap(request.get("variable"))
                : Collections.<String, Object>emptyMap();
        for (Map<String, Object> owner : skillVariableOwners(orgId)) {
            for (Map<String, Object> row : listValue(owner.get("variables"))) {
                if (variableId.equals(text(row, "id"))) {
                    row.putAll(skillVariableFields(variable));
                    return;
                }
            }
        }
    }

    private void deleteSkillVariable(String orgId, String variableId) {
        for (Map<String, Object> owner : skillVariableOwners(orgId)) {
            listValue(owner.get("variables")).removeIf(row -> variableId.equals(text(row, "id")));
        }
    }

    private List<Map<String, Object>> skillVariableOwners(String orgId) {
        List<Map<String, Object>> owners = new ArrayList<>();
        for (Map<String, Object> item : customSkills.values()) {
            if (sameOrg(item, orgId)) {
                owners.add(item);
            }
        }
        for (Map<String, Object> item : acquiredSkills.values()) {
            if (sameOrg(item, orgId)) {
                owners.add(item);
            }
        }
        for (Map<String, Object> item : builtinSkillVariableStores.values()) {
            if (sameOrg(item, orgId)) {
                owners.add(item);
            }
        }
        return owners;
    }

    private Map<String, Object> skillVariable(Map<String, Object> variable) {
        Map<String, Object> row = skillVariableFields(variable);
        row.put("id", "var-" + variableSequence.getAndIncrement());
        return row;
    }

    private Map<String, Object> skillVariableFields(Map<String, Object> variable) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", defaultText(variable, "name", "Variable"));
        row.put("desc", defaultText(variable, "desc", ""));
        row.put("variableKey", defaultText(variable, "variableKey", ""));
        row.put("variableValue", defaultText(variable, "variableValue", ""));
        return row;
    }

    private List<Map<String, Object>> skillVariables(Map<String, Object> item) {
        return copyList(listValue(item.get("variables")));
    }

    private Map<String, Object> builtinSkillVariableItem(String orgId, String skillId) {
        String key = scoped(orgId, skillId);
        Map<String, Object> existing = builtinSkillVariableStores.get(key);
        if (existing != null) {
            return existing;
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("skillId", skillId);
        item.put("ownerOrgId", org(orgId));
        item.put("variables", new ArrayList<Map<String, Object>>());
        Map<String, Object> raced = builtinSkillVariableStores.putIfAbsent(key, item);
        return raced == null ? item : raced;
    }

    private boolean isAcquired(String orgId, String squareSkillId) {
        for (Map<String, Object> item : acquiredSkills.values()) {
            if (sameOrg(item, orgId) && squareSkillId.equals(text(item, "squareSkillId"))) {
                return true;
            }
        }
        return false;
    }

    private byte[] skillArchive(String skillId, String source) {
        String body = "SKILL.md\nname: " + skillId + "\nsource: " + source + "\n";
        return body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private Map<String, Object> conversationMessage(String role, String content,
                                                    List<Map<String, Object>> responseFiles) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("role", role);
        row.put("content", content);
        row.put("createdAt", now());
        row.put("responseFiles", responseFiles);
        return row;
    }

    private Map<String, Object> skillResponseFile(String query) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("skillId", "generated-" + compact(query));
        row.put("skillSaveId", "save-" + compact(query));
        row.put("name", "Generated Skill");
        row.put("desc", "Local deterministic skill draft.");
        row.put("author", "Wanwu");
        row.put("avatar", avatar("/imgs/skill.svg"));
        row.put("downloadUrl", "/user/api/v1/builtin/skill/download?skillId=builtin-summary");
        row.put("inResource", false);
        row.put("expiredAt", "2030-01-01 00:00:00");
        return row;
    }

    private List<Map<String, Object>> apiList(Object schema) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> action : actionsFromSchema(String.valueOf(schema == null ? "{}" : schema))) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", action.get("name"));
            row.put("desc", action.get("description"));
            row.put("method", action.get("method"));
            row.put("path", action.get("path"));
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> actionsFromSchema(String schema) {
        List<Map<String, Object>> actions = new ArrayList<>();
        try {
            JsonNode paths = JSON.readTree(schema).path("paths");
            if (paths.isObject()) {
                java.util.Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
                while (pathIt.hasNext()) {
                    Map.Entry<String, JsonNode> path = pathIt.next();
                    java.util.Iterator<Map.Entry<String, JsonNode>> methodIt = path.getValue().fields();
                    while (methodIt.hasNext()) {
                        Map.Entry<String, JsonNode> method = methodIt.next();
                        String httpMethod = method.getKey().toUpperCase(Locale.ROOT);
                        JsonNode operation = method.getValue();
                        String operationId = operation.path("operationId").asText("");
                        String name = blank(operationId)
                                ? httpMethod.toLowerCase(Locale.ROOT) + "_" + compact(path.getKey())
                                : operationId;
                        String desc = operation.path("summary").asText(operation.path("description").asText(name));
                        Map<String, Object> action = action(name, desc);
                        action.put("method", httpMethod);
                        action.put("path", path.getKey());
                        actions.add(action);
                    }
                }
            }
        } catch (Exception ignored) {
            actions.clear();
        }
        if (actions.isEmpty()) {
            Map<String, Object> action = action("invoke", "Invoke the configured API");
            action.put("method", "POST");
            action.put("path", "/invoke");
            actions.add(action);
        }
        return actions;
    }

    private List<Map<String, Object>> toolActions(String orgId, String toolId, String toolType) {
        if ("custom".equals(toolType)) {
            return actionsFromSchema(text(require(customTools, orgId, toolId, "custom tool"), "schema"));
        }
        return copyList(listValue(builtinTool(toolId).get("tools")));
    }

    private Map<String, Object> toolSelect(Map<String, Object> item, String type, String id) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("uniqueId", type + "_" + id);
        row.put("toolId", id);
        row.put("toolName", text(item, "name"));
        row.put("toolType", type);
        row.put("desc", firstText(item, "desc", "description"));
        row.put("needApiKeyInput", item.get("needApiKeyInput") == null ? false : item.get("needApiKeyInput"));
        row.put("apiKey", "");
        row.put("avatar", item.get("avatar"));
        return row;
    }

    private Map<String, Object> toolSquareInfo(Map<String, Object> item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("toolSquareId", item.get("toolSquareId"));
        row.put("avatar", item.get("avatar"));
        row.put("name", item.get("name"));
        row.put("desc", item.get("desc"));
        row.put("tags", item.get("tags"));
        return row;
    }

    private Map<String, Object> mcpInfo(Map<String, Object> item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("mcpId", item.get("mcpId"));
        row.put("mcpSquareId", item.get("mcpSquareId"));
        row.put("avatar", item.get("avatar"));
        row.put("name", item.get("name"));
        row.put("desc", item.get("desc"));
        row.put("from", item.get("from"));
        row.put("sseUrl", item.get("sseUrl"));
        row.put("streamableUrl", item.get("streamableUrl"));
        row.put("transport", item.get("transport"));
        return row;
    }

    private Map<String, Object> mcpDetail(Map<String, Object> item) {
        Map<String, Object> row = mcpInfo(item);
        row.put("summary", "Local Java MCP compatibility resource.");
        row.put("feature", "Lists tools and can be selected by agents.");
        row.put("scenario", "Development reproduction flow.");
        row.put("manual", "Create, inspect, and bind this MCP in the resource center.");
        row.put("detail", "This Java slice mirrors the Wanwu frontend contract with local deterministic tools.");
        row.put("tools", item.get("tools"));
        row.put("hasCustom", false);
        return row;
    }

    private Map<String, Object> mcpSelect(Map<String, Object> item, String type) {
        String id = "mcpserver".equals(type) ? text(item, "mcpServerId") : text(item, "mcpId");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("uniqueId", type + "_" + id);
        row.put("mcpId", id);
        row.put("mcpSquareId", firstText(item, "mcpSquareId"));
        row.put("name", item.get("name"));
        row.put("type", type);
        row.put("toolId", id);
        row.put("toolName", item.get("name"));
        row.put("toolType", type);
        row.put("description", firstText(item, "desc", "description"));
        row.put("serverFrom", firstText(item, "from"));
        row.put("serverUrl", firstText(item, "sseUrl"));
        row.put("streamableUrl", firstText(item, "streamableUrl"));
        row.put("transport", firstText(item, "transport"));
        row.put("avatar", item.get("avatar"));
        return row;
    }

    private Map<String, Object> serverTool(String orgId, Map<String, Object> request) {
        String id = text(request, "id");
        String type = defaultText(request, "type", "custom");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("mcpServerToolId", id("mcpservertool"));
        row.put("methodName", defaultText(request, "methodName", "invoke"));
        row.put("type", type);
        row.put("id", id);
        row.put("name", defaultText(request, "name", resolveToolName(orgId, id, type)));
        row.put("desc", defaultText(request, "desc", resolveToolDesc(orgId, id, type)));
        return row;
    }

    private String resolveToolName(String orgId, String id, String type) {
        if ("builtin".equals(type)) {
            return text(builtinTool(id), "name");
        }
        Map<String, Object> custom = customTools.get(scoped(orgId, id));
        return custom == null ? id : text(custom, "name");
    }

    private String resolveToolDesc(String orgId, String id, String type) {
        if ("builtin".equals(type)) {
            return text(builtinTool(id), "desc");
        }
        Map<String, Object> custom = customTools.get(scoped(orgId, id));
        return custom == null ? "" : text(custom, "description");
    }

    private Map<String, Object> mcpSquareInfo(Map<String, Object> item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("mcpSquareId", item.get("mcpSquareId"));
        row.put("avatar", item.get("avatar"));
        row.put("name", item.get("name"));
        row.put("desc", item.get("desc"));
        row.put("from", item.get("from"));
        row.put("category", item.get("category"));
        return row;
    }

    private Map<String, Object> mcpSquareDetail(Map<String, Object> item) {
        Map<String, Object> row = mcpSquareInfo(item);
        row.put("summary", item.get("summary"));
        row.put("feature", item.get("feature"));
        row.put("scenario", item.get("scenario"));
        row.put("manual", item.get("manual"));
        row.put("detail", item.get("detail"));
        row.put("sseUrl", item.get("sseUrl"));
        row.put("streamableUrl", item.get("streamableUrl"));
        row.put("transport", item.get("transport"));
        row.put("tools", item.get("tools"));
        row.put("hasCustom", false);
        return row;
    }

    private Map<String, Object> promptStreamPayload(String response) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", null);
        result.put("message", "success");
        result.put("response", response);
        result.put("finish", 1);
        result.put("usage", null);
        return result;
    }

    private List<Map<String, Object>> builtinTools() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> weather = new LinkedHashMap<>();
        weather.put("toolSquareId", "builtin-weather");
        weather.put("avatar", avatar("/imgs/toolImg.png"));
        weather.put("name", "Weather Tool");
        weather.put("desc", "Query deterministic local weather information.");
        weather.put("tags", Arrays.asList("builtin", "weather"));
        weather.put("needApiKeyInput", false);
        weather.put("tools", Collections.singletonList(action("get_weather", "Get weather by city name")));
        weather.put("detail", "Development built-in tool compatible with the original Wanwu resource center.");
        weather.put("schema", "{}");
        list.add(weather);

        Map<String, Object> search = new LinkedHashMap<>();
        search.put("toolSquareId", "builtin-search");
        search.put("avatar", avatar("/imgs/toolImg.png"));
        search.put("name", "Search Tool");
        search.put("desc", "Return local search-style snippets for frontend integration.");
        search.put("tags", Arrays.asList("builtin", "search"));
        search.put("needApiKeyInput", true);
        search.put("tools", Collections.singletonList(action("search", "Search local indexed content")));
        search.put("detail", "API-key capable built-in tool shell for the Java reproduction.");
        search.put("schema", "{}");
        list.add(search);
        return list;
    }

    private List<Map<String, Object>> builtinSkills() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(builtinSkillSeed("builtin-summary", "Summary Skill", "Wanwu",
                "Summarize documents into structured notes.",
                "# Summary Skill\n\nUse this skill to summarize long material into facts, conclusions, and action items."));
        list.add(builtinSkillSeed("builtin-review", "Review Skill", "Wanwu",
                "Review content and return risks, evidence, and next actions.",
                "# Review Skill\n\nUse this skill to inspect content, find risks, and produce concrete fixes."));
        return list;
    }

    private Map<String, Object> builtinSkillSeed(String skillId, String name, String author, String desc,
                                                 String markdown) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("skillId", skillId);
        item.put("avatar", avatar("/imgs/skill.svg"));
        item.put("name", name);
        item.put("author", author);
        item.put("desc", desc);
        item.put("skillMarkdown", markdown);
        item.put("downloadUrl", "/user/api/v1/builtin/skill/download?skillId=" + skillId);
        item.put("variables", new ArrayList<Map<String, Object>>());
        return item;
    }

    private List<Map<String, Object>> promptTemplates() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(promptTemplate("prompt-template-summary", "总结助手", "把材料整理成结构化摘要",
                "请总结以下内容，保留关键事实、结论和待办事项：\n{{input}}"));
        list.add(promptTemplate("prompt-template-review", "代码评审助手", "输出风险、证据和修复建议",
                "你是资深代码评审员。请按严重程度列出问题，并给出文件位置和修复建议：\n{{diff}}"));
        return list;
    }

    private Map<String, Object> promptTemplate(String id, String name, String desc, String prompt) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("templateId", id);
        item.put("avatar", avatar("/imgs/prompt.png"));
        item.put("name", name);
        item.put("desc", desc);
        item.put("prompt", prompt);
        item.put("updateAt", now());
        return item;
    }

    private List<Map<String, Object>> mcpSquares() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> browser = new LinkedHashMap<>();
        browser.put("mcpSquareId", "square-browser");
        browser.put("avatar", avatar("/imgs/mcp_active.svg"));
        browser.put("name", "Browser MCP");
        browser.put("desc", "Browse and inspect web pages.");
        browser.put("from", "Wanwu");
        browser.put("category", "search");
        browser.put("summary", "Browser automation MCP.");
        browser.put("feature", "Page open, extraction and structured tool calls.");
        browser.put("scenario", "Research and frontend smoke testing.");
        browser.put("manual", "Send to resource center, then select it in an agent.");
        browser.put("detail", "Local square seed for Java reproduction.");
        browser.put("sseUrl", "https://example.invalid/mcp/browser/sse");
        browser.put("streamableUrl", "https://example.invalid/mcp/browser/streamable");
        browser.put("transport", "sse");
        browser.put("tools", sampleMcpTools("browser"));
        list.add(browser);
        return list;
    }

    private Map<String, Object> builtinTool(String id) {
        for (Map<String, Object> item : builtinTools()) {
            if (text(item, "toolSquareId").equals(id)) {
                return item;
            }
        }
        throw new IllegalArgumentException("builtin tool not found: " + id);
    }

    private Map<String, Object> builtinSkill(String id) {
        for (Map<String, Object> item : builtinSkills()) {
            if (text(item, "skillId").equals(id)) {
                return item;
            }
        }
        throw new IllegalArgumentException("builtin skill not found: " + id);
    }

    private Map<String, Object> mcpSquare(String id) {
        for (Map<String, Object> item : mcpSquares()) {
            if (text(item, "mcpSquareId").equals(id)) {
                return item;
            }
        }
        throw new IllegalArgumentException("mcp square not found: " + id);
    }

    private Map<String, Object> promptTemplate(String id) {
        for (Map<String, Object> item : promptTemplates()) {
            if (text(item, "templateId").equals(id)) {
                return item;
            }
        }
        throw new IllegalArgumentException("prompt template not found: " + id);
    }

    private List<Map<String, Object>> sampleMcpTools(String seed) {
        return Arrays.asList(
                action("search_" + compact(defaultString(seed, "mcp")), "Search through " + defaultString(seed, "MCP")),
                action("fetch_detail", "Fetch detail from the MCP server")
        );
    }

    private Map<String, Object> action(String name, String description) {
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("type", "string");
        property.put("description", "Input text");
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("query", property);
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Collections.singletonList("query"));
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("name", name);
        action.put("description", description);
        action.put("inputSchema", schema);
        action.put("method", "POST");
        action.put("path", "/" + name);
        return action;
    }

    private Map<String, Object> findAction(List<Map<String, Object>> actions, String actionName) {
        for (Map<String, Object> action : actions) {
            if (text(action, "name").equals(actionName)) {
                return action;
            }
        }
        return actions.isEmpty() ? action("invoke", "Invoke") : actions.get(0);
    }

    private Map<String, Object> listResult(List<Map<String, Object>> list) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return result;
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    private synchronized void saveSnapshot() {
        if (mcpRecordMapper == null) {
            return;
        }
        McpRecordEntity entity = new McpRecordEntity();
        entity.setRecordType(TYPE_SNAPSHOT);
        entity.setRecordId(SNAPSHOT_ID);
        long now = System.currentTimeMillis();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        try {
            entity.setPayload(JSON.writeValueAsString(snapshot()));
            mcpRecordMapper.upsertRecord(entity);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to persist MCP snapshot", ex);
        }
    }

    private McpSnapshot snapshot() {
        McpSnapshot snapshot = new McpSnapshot();
        snapshot.customTools.putAll(customTools);
        snapshot.customMcps.putAll(customMcps);
        snapshot.mcpServers.putAll(mcpServers);
        snapshot.customPrompts.putAll(customPrompts);
        snapshot.customSkills.putAll(customSkills);
        snapshot.acquiredSkills.putAll(acquiredSkills);
        snapshot.builtinSkillVariableStores.putAll(builtinSkillVariableStores);
        snapshot.skillConversations.putAll(skillConversations);
        snapshot.builtinToolApiKeys.putAll(builtinToolApiKeys);
        snapshot.variableSequence = variableSequence.get();
        return snapshot;
    }

    private void applySnapshot(McpSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        restore(customTools, snapshot.customTools);
        restore(customMcps, snapshot.customMcps);
        restore(mcpServers, snapshot.mcpServers);
        restore(customPrompts, snapshot.customPrompts);
        restore(customSkills, snapshot.customSkills);
        restore(acquiredSkills, snapshot.acquiredSkills);
        restore(builtinSkillVariableStores, snapshot.builtinSkillVariableStores);
        restore(skillConversations, snapshot.skillConversations);
        builtinToolApiKeys.clear();
        if (snapshot.builtinToolApiKeys != null) {
            builtinToolApiKeys.putAll(snapshot.builtinToolApiKeys);
        }
        variableSequence.set(Math.max(1, snapshot.variableSequence));
        bumpVariableSequence(customSkills);
        bumpVariableSequence(acquiredSkills);
        bumpVariableSequence(builtinSkillVariableStores);
    }

    private void restore(ConcurrentMap<String, Map<String, Object>> target,
                         Map<String, Map<String, Object>> source) {
        target.clear();
        if (source != null) {
            target.putAll(source);
        }
    }

    private void bumpVariableSequence(Map<String, Map<String, Object>> store) {
        for (Map<String, Object> item : store.values()) {
            for (Map<String, Object> variable : listValue(item.get("variables"))) {
                String id = text(variable, "id");
                if (id.startsWith("var-")) {
                    try {
                        int value = Integer.parseInt(id.substring("var-".length())) + 1;
                        if (value > variableSequence.get()) {
                            variableSequence.set(value);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    private static final class McpSnapshot {
        public Map<String, Map<String, Object>> customTools =
                new LinkedHashMap<String, Map<String, Object>>();
        public Map<String, Map<String, Object>> customMcps =
                new LinkedHashMap<String, Map<String, Object>>();
        public Map<String, Map<String, Object>> mcpServers =
                new LinkedHashMap<String, Map<String, Object>>();
        public Map<String, Map<String, Object>> customPrompts =
                new LinkedHashMap<String, Map<String, Object>>();
        public Map<String, Map<String, Object>> customSkills =
                new LinkedHashMap<String, Map<String, Object>>();
        public Map<String, Map<String, Object>> acquiredSkills =
                new LinkedHashMap<String, Map<String, Object>>();
        public Map<String, Map<String, Object>> builtinSkillVariableStores =
                new LinkedHashMap<String, Map<String, Object>>();
        public Map<String, Map<String, Object>> skillConversations =
                new LinkedHashMap<String, Map<String, Object>>();
        public Map<String, String> builtinToolApiKeys = new LinkedHashMap<String, String>();
        public int variableSequence = 1;
    }

    private Map<String, Object> defaultApiAuth() {
        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("authType", "none");
        auth.put("apiKeyValue", "");
        auth.put("apiKeyHeader", "");
        auth.put("apiKeyHeaderPrefix", "");
        auth.put("apiKeyQueryParam", "");
        return auth;
    }

    private Map<String, Object> avatar(Map<String, Object> request) {
        if (request != null && request.get("avatar") instanceof Map) {
            return copy(castMap(request.get("avatar")));
        }
        return avatar("");
    }

    private Map<String, Object> avatar(String path) {
        Map<String, Object> avatar = new LinkedHashMap<>();
        avatar.put("key", "");
        avatar.put("path", path);
        return avatar;
    }

    private boolean sameOrg(Map<String, Object> item, String orgId) {
        return org(orgId).equals(defaultText(item, "ownerOrgId", DEFAULT_ORG));
    }

    private String scoped(String orgId, String id) {
        return org(orgId) + ":" + id;
    }

    private String org(String orgId) {
        return blank(orgId) ? DEFAULT_ORG : orgId;
    }

    private String id(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private boolean matches(Map<String, Object> item, String key, String name) {
        return blank(name) || text(item, key).toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT));
    }

    private String compact(String value) {
        String source = defaultString(value, "item").replaceAll("[^A-Za-z0-9_]+", "_");
        return blank(source) ? "item" : source.toLowerCase(Locale.ROOT);
    }

    private String defaultText(Map<String, Object> map, String key, String defaultValue) {
        String value = text(map, key);
        return blank(value) ? defaultValue : value;
    }

    private String defaultString(String value, String defaultValue) {
        return blank(value) ? defaultValue : value;
    }

    private String firstText(Map<String, Object> map, String... keys) {
        if (map == null) {
            return "";
        }
        for (String key : keys) {
            String value = text(map, key);
            if (!blank(value)) {
                return value;
            }
        }
        return "";
    }

    private String text(Map<String, Object> map, String key) {
        if (map == null || key == null || map.get(key) == null) {
            return "";
        }
        return String.valueOf(map.get(key));
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, Object> require(ConcurrentMap<String, Map<String, Object>> store, String orgId, String id,
                                        String name) {
        Map<String, Object> item = store.get(scoped(orgId, id));
        if (item == null) {
            throw new IllegalArgumentException(name + " not found: " + id);
        }
        return item;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listValue(Object value) {
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return new ArrayList<>();
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            result.add(String.valueOf(item));
        }
        return result;
    }

    private Map<String, Object> copy(Map<String, Object> source) {
        return new LinkedHashMap<>(source);
    }

    private List<Map<String, Object>> copyList(List<Map<String, Object>> source) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : source) {
            result.add(copy(item));
        }
        return result;
    }
}
