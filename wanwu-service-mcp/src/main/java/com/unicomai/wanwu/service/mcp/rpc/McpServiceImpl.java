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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class McpServiceImpl implements McpService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String DEFAULT_ORG = "default-org";
    private static final String TYPE_SNAPSHOT = "snapshot";
    private static final String SNAPSHOT_ID = "state";
    private static final int MCP_REMOTE_TIMEOUT_MILLIS = 2000;
    private static final Pattern SKILL_NAME_PATTERN =
            Pattern.compile("^[\\p{L}][\\p{L}\\p{N}]*(?:-[\\p{L}\\p{N}]+)*$");

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
        item.put("tools", resolveMcpTools(item, text(item, "name")));
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
        updated.put("tools", resolveMcpTools(updated, text(updated, "name")));
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
            Map<String, Object> item = require(customMcps, orgId, mcpId, "mcp");
            return singleton("tools", resolveMcpTools(item, text(item, "name")));
        }
        Map<String, Object> probe = mcpBase(request);
        String serverUrl = firstText(request, "serverUrl", "streamableUrl", "sseUrl");
        if ("streamable".equalsIgnoreCase(text(probe, "transport")) && blank(text(probe, "streamableUrl"))) {
            probe.put("streamableUrl", serverUrl);
        } else if (blank(text(probe, "sseUrl"))) {
            probe.put("sseUrl", serverUrl);
        }
        return singleton("tools", resolveMcpTools(probe, serverUrl));
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
            create.put("schema", text(request, "schema"));
            if (request.get("apiAuth") instanceof Map) {
                create.put("apiAuth", copy(castMap(request.get("apiAuth"))));
            }
            Map<String, Object> tool = serverTool(orgId, create);
            listValue(server.get("tools")).add(tool);
            created.add(tool);
        }
        saveSnapshot();
        return listResult(created);
    }

    @Override
    public Map<String, Object> callMcpServerTool(String userId, String orgId, String mcpServerId,
                                                 Map<String, Object> request) {
        Map<String, Object> server = require(mcpServers, orgId, mcpServerId, "mcp server");
        String name = firstText(request, "name", "toolName", "methodName");
        Map<String, Object> arguments = mapValue(request == null ? null : request.get("arguments"));
        Map<String, Object> tool = findServerTool(server, name);
        if (tool.isEmpty()) {
            return mcpCallResult(mcpServerId, name, arguments, tool,
                    "MCP tool not found: " + defaultString(name, "unknown"), true,
                    Collections.<String, Object>emptyMap());
        }
        if ("custom".equals(text(tool, "type"))) {
            try {
                return executeCustomMcpServerTool(orgId, mcpServerId, tool, name, arguments);
            } catch (Exception ex) {
                return mcpCallResult(mcpServerId, name, arguments, tool,
                        "MCP tool call failed: " + ex.getMessage(), true,
                        Collections.<String, Object>emptyMap());
            }
        }
        if ("openapi".equals(text(tool, "type"))) {
            try {
                return executeOpenApiMcpServerTool(mcpServerId, tool, name, arguments,
                        text(tool, "schema"), mapValue(tool.get("apiAuth")));
            } catch (Exception ex) {
                return mcpCallResult(mcpServerId, name, arguments, tool,
                        "MCP tool call failed: " + ex.getMessage(), true,
                        Collections.<String, Object>emptyMap());
            }
        }
        if ("builtin".equals(text(tool, "type"))) {
            return executeBuiltinMcpServerTool(mcpServerId, tool, name, arguments);
        }
        String text = "Executed MCP tool " + defaultString(name, "unknown")
                + " on server " + defaultString(mcpServerId, "")
                + " with arguments " + arguments;
        if (!blank(text(tool, "desc"))) {
            text = text + ". " + text(tool, "desc");
        }
        return mcpCallResult(mcpServerId, name, arguments, tool, text, false,
                Collections.<String, Object>emptyMap());
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
        String zipUrl = text(request, "zipUrl");
        if (!blank(zipUrl)) {
            SkillPackage skillPackage = parseSkillPackage(zipUrl);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("name", skillPackage.name);
            result.put("desc", skillPackage.description);
            return result;
        }
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
        return skillArchive(builtinSkill(skillId), "builtin");
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
        item.put("skillPackageBase64", Base64.getEncoder().encodeToString(skillArchive(source, "square")));
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
        Map<String, Object> builtin = findBuiltinSkill(skillId);
        if (builtin != null) {
            return skillArchive(builtin, "square");
        }
        Map<String, Object> custom = customSkills.get(scoped(orgId, skillId));
        if (custom != null) {
            return skillArchive(custom, "custom");
        }
        Map<String, Object> acquired = acquiredSkills.get(scoped(orgId, skillId));
        if (acquired != null) {
            return skillArchive(acquired, "acquired");
        }
        throw new IllegalArgumentException("square skill not found: " + skillId);
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
        String zipUrl = defaultText(request, "zipUrl", "");
        SkillPackage skillPackage = parseSkillPackageForCreate(zipUrl, request);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("avatar", avatar(request));
        item.put("name", skillPackage == null ? defaultText(request, "name", "Imported Skill") : skillPackage.name);
        item.put("author", defaultText(request, "author", "Wanwu"));
        item.put("desc", skillPackage == null ? defaultText(request, "desc", "Imported local skill package.")
                : skillPackage.description);
        item.put("zipUrl", zipUrl);
        item.put("objectPath", zipUrl);
        item.put("skillMarkdown", skillPackage == null ? defaultText(request, "skillMarkdown", "")
                : skillPackage.markdown);
        if (skillPackage != null) {
            item.put("skillPackageBase64", Base64.getEncoder().encodeToString(skillPackage.zipData));
        }
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
        row.put("skillMarkdown", item.get("skillMarkdown"));
        row.put("variables", skillVariables(item));
        return row;
    }

    private SkillPackage parseSkillPackageForCreate(String zipUrl, Map<String, Object> request) {
        if (blank(zipUrl)) {
            return null;
        }
        try {
            return parseSkillPackage(zipUrl);
        } catch (IllegalArgumentException ex) {
            if (!blank(text(request, "name")) && !blank(text(request, "desc"))) {
                return null;
            }
            throw ex;
        }
    }

    private SkillPackage parseSkillPackage(String zipUrl) {
        byte[] zipData = loadSkillZip(zipUrl);
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory() && "SKILL.md".equals(baseName(entry.getName()))) {
                    String markdown = new String(readBytes(zip), StandardCharsets.UTF_8);
                    SkillFrontMatter frontMatter = parseSkillFrontMatter(markdown);
                    return new SkillPackage(zipData, markdown, frontMatter.name, frontMatter.description);
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to read zip file: " + ex.getMessage(), ex);
        }
        throw new IllegalArgumentException("SKILL.md file not found in the zip archive");
    }

    private byte[] loadSkillZip(String zipUrl) {
        if (zipUrl.startsWith("http://") || zipUrl.startsWith("https://")) {
            return downloadSkillZip(zipUrl);
        }
        if (zipUrl.startsWith("file:")) {
            try (InputStream stream = new URL(zipUrl).openStream()) {
                return readBytes(stream);
            } catch (IOException ex) {
                throw new IllegalArgumentException("failed to read zip file: " + ex.getMessage(), ex);
            }
        }
        File file = new File(zipUrl);
        if (!file.isFile()) {
            throw new IllegalArgumentException("skill zip file not found: " + zipUrl);
        }
        try (InputStream stream = new FileInputStream(file)) {
            return readBytes(stream);
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to read zip file: " + ex.getMessage(), ex);
        }
    }

    private byte[] downloadSkillZip(String zipUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(zipUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(MCP_REMOTE_TIMEOUT_MILLIS);
            connection.setReadTimeout(MCP_REMOTE_TIMEOUT_MILLIS);
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            byte[] body = stream == null ? new byte[0] : readBytes(stream);
            if (status < 200 || status >= 300) {
                throw new IllegalArgumentException("download skill zip err: http " + status + " "
                        + new String(body, StandardCharsets.UTF_8));
            }
            return body;
        } catch (IOException ex) {
            throw new IllegalArgumentException("download skill zip err: " + ex.getMessage(), ex);
        }
    }

    private SkillFrontMatter parseSkillFrontMatter(String markdown) {
        String content = markdown == null ? "" : markdown.trim();
        if (!content.startsWith("---")) {
            throw new IllegalArgumentException("SKILL.md file must start with front matter delimiters");
        }
        String rest = content.substring(3);
        int endIndex = rest.indexOf("\n---");
        if (endIndex < 0) {
            throw new IllegalArgumentException("SKILL.md file must end with front matter delimiters");
        }
        Map<String, String> frontMatter = parseSimpleYaml(rest.substring(0, endIndex));
        String name = frontMatter.get("name");
        String description = frontMatter.get("description");
        if (blank(name) || blank(description)) {
            throw new IllegalArgumentException(
                    "SKILL.md file must contain both name and description in front matter");
        }
        if (!SKILL_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("SKILL.md file name must be in kebab-case");
        }
        return new SkillFrontMatter(name, description);
    }

    private Map<String, String> parseSimpleYaml(String source) {
        Map<String, String> values = new LinkedHashMap<>();
        String[] lines = source.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            String value = stripYamlScalar(line.substring(separator + 1).trim());
            values.put(key, value);
        }
        return values;
    }

    private String stripYamlScalar(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private String baseName(String name) {
        String normalized = defaultString(name, "").replace('\\', '/');
        int index = normalized.lastIndexOf('/');
        return index < 0 ? normalized : normalized.substring(index + 1);
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

    private byte[] skillArchive(Map<String, Object> item, String source) {
        byte[] stored = storedSkillPackage(item);
        if (stored.length > 0) {
            return stored;
        }
        String skillId = text(item, "skillId");
        String desc = defaultText(item, "desc", "Wanwu skill package.");
        String markdown = defaultText(item, "skillMarkdown",
                "# " + defaultText(item, "name", skillId) + "\n\n" + desc);
        return zipSkillArchive(skillId, source, desc, markdown);
    }

    private byte[] storedSkillPackage(Map<String, Object> item) {
        String encoded = text(item, "skillPackageBase64");
        if (blank(encoded)) {
            return new byte[0];
        }
        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException ex) {
            return new byte[0];
        }
    }

    private byte[] zipSkillArchive(String skillId, String source, String desc, String markdown) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.putNextEntry(new ZipEntry(skillId + "/SKILL.md"));
            zip.write(skillMarkdownWithFrontMatter(skillId, source, desc, markdown).getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("failed to build skill zip: " + ex.getMessage(), ex);
        }
    }

    private String skillMarkdownWithFrontMatter(String skillId, String source, String desc, String markdown) {
        String body = defaultString(markdown, "").trim();
        if (body.startsWith("---")) {
            return body + "\n";
        }
        return "---\nname: " + skillId + "\ndescription: " + desc + "\nsource: " + source
                + "\n---\n" + defaultString(markdown, "") + "\n";
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
        if (!blank(text(request, "schema"))) {
            row.put("schema", text(request, "schema"));
        }
        if (request.get("apiAuth") instanceof Map) {
            row.put("apiAuth", copy(castMap(request.get("apiAuth"))));
        }
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
        Map<String, Object> item = findBuiltinSkill(id);
        if (item != null) {
            return item;
        }
        throw new IllegalArgumentException("builtin skill not found: " + id);
    }

    private Map<String, Object> findBuiltinSkill(String id) {
        for (Map<String, Object> item : builtinSkills()) {
            if (text(item, "skillId").equals(id)) {
                return item;
            }
        }
        return null;
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

    private List<Map<String, Object>> resolveMcpTools(Map<String, Object> item, String fallbackSeed) {
        List<Map<String, Object>> remote = fetchRemoteMcpTools(item);
        if (!remote.isEmpty()) {
            return remote;
        }
        List<Map<String, Object>> stored = listValue(item.get("tools"));
        if (!stored.isEmpty()) {
            return copyList(stored);
        }
        return sampleMcpTools(fallbackSeed);
    }

    private List<Map<String, Object>> fetchRemoteMcpTools(Map<String, Object> item) {
        if (!"streamable".equalsIgnoreCase(text(item, "transport"))) {
            return Collections.emptyList();
        }
        String url = firstText(item, "streamableUrl", "serverUrl");
        if (blank(url)) {
            return Collections.emptyList();
        }
        try {
            String sessionId = initializeStreamableMcp(url);
            return parseRemoteTools(callStreamableMcp(url, mcpRpcBody("tools/list"), sessionId).body);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private String initializeStreamableMcp(String url) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("protocolVersion", "2024-11-05");
            params.put("capabilities", new LinkedHashMap<String, Object>());
            Map<String, Object> clientInfo = new LinkedHashMap<>();
            clientInfo.put("name", "wanwu-java");
            clientInfo.put("version", "0.1.0");
            params.put("clientInfo", clientInfo);
            return callStreamableMcp(url, mcpRpcBody("initialize", params), "").sessionId;
        } catch (Exception ex) {
            return "";
        }
    }

    private String mcpRpcBody(String method) throws IOException {
        return mcpRpcBody(method, Collections.<String, Object>emptyMap());
    }

    private String mcpRpcBody(String method, Map<String, Object> params) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("jsonrpc", "2.0");
        body.put("id", 1);
        body.put("method", method);
        body.put("params", params);
        return JSON.writeValueAsString(body);
    }

    private RemoteMcpResponse callStreamableMcp(String url, String body, String sessionId) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(MCP_REMOTE_TIMEOUT_MILLIS);
        connection.setReadTimeout(MCP_REMOTE_TIMEOUT_MILLIS);
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        if (!blank(sessionId)) {
            connection.setRequestProperty("Mcp-Session-Id", sessionId);
        }
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(bytes);
        }
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        String responseBody = stream == null ? "" : readFully(stream);
        if (status < 200 || status >= 300) {
            throw new IOException("mcp streamable http " + status + ": " + responseBody);
        }
        return new RemoteMcpResponse(responseBody, defaultString(connection.getHeaderField("Mcp-Session-Id"), ""));
    }

    private String readFully(InputStream stream) throws IOException {
        return new String(readBytes(stream), StandardCharsets.UTF_8);
    }

    private byte[] readBytes(InputStream stream) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((read = stream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private List<Map<String, Object>> parseRemoteTools(String body) throws IOException {
        if (blank(body)) {
            return Collections.emptyList();
        }
        JsonNode root = JSON.readTree(body);
        JsonNode tools = root.path("result").path("tools");
        if (!tools.isArray()) {
            tools = root.path("tools");
        }
        if (!tools.isArray()) {
            tools = root.path("data").path("tools");
        }
        if (!tools.isArray()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode tool : tools) {
            if (blank(tool.path("name").asText(""))) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> row = JSON.convertValue(tool, Map.class);
            result.add(row);
        }
        return result;
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

    private Map<String, Object> findServerTool(Map<String, Object> server, String name) {
        for (Map<String, Object> tool : listValue(server.get("tools"))) {
            String methodName = defaultString(text(tool, "methodName"), text(tool, "name"));
            if (methodName.equals(name)) {
                return tool;
            }
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> executeCustomMcpServerTool(String orgId, String mcpServerId,
                                                           Map<String, Object> serverTool, String name,
                                                           Map<String, Object> arguments) throws IOException {
        Map<String, Object> customTool = require(customTools, orgId, text(serverTool, "id"), "custom tool");
        return executeOpenApiMcpServerTool(mcpServerId, serverTool, name, arguments,
                text(customTool, "schema"), mapValue(customTool.get("apiAuth")));
    }

    private Map<String, Object> executeOpenApiMcpServerTool(String mcpServerId, Map<String, Object> serverTool,
                                                            String name, Map<String, Object> arguments,
                                                            String schema, Map<String, Object> apiAuth)
            throws IOException {
        OpenApiOperation operation = openApiOperation(schema, name);
        HttpToolResponse response = callOpenApiOperation(operation, apiAuth, arguments);
        Object parsed = parseJsonValue(response.body);
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("status", response.status);
        extra.put("url", response.url);
        extra.put("response", parsed);
        return mcpCallResult(mcpServerId, name, arguments, serverTool, response.body,
                response.status < 200 || response.status >= 300, extra);
    }

    private Map<String, Object> executeBuiltinMcpServerTool(String mcpServerId, Map<String, Object> serverTool,
                                                            String name, Map<String, Object> arguments) {
        Map<String, Object> builtin = builtinTool(text(serverTool, "id"));
        String actionName = defaultString(name, text(findAction(listValue(builtin.get("tools")), name), "name"));
        Map<String, Object> response;
        if ("builtin-weather".equals(text(builtin, "toolSquareId")) || "get_weather".equals(actionName)) {
            response = builtinWeatherResponse(arguments);
        } else if ("builtin-search".equals(text(builtin, "toolSquareId")) || "search".equals(actionName)) {
            response = builtinSearchResponse(arguments);
        } else {
            response = new LinkedHashMap<>();
            response.put("tool", text(builtin, "name"));
            response.put("action", actionName);
            response.put("arguments", arguments);
            response.put("message", "Builtin tool executed by Wanwu Java local runtime.");
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("builtinToolId", text(builtin, "toolSquareId"));
        extra.put("response", response);
        return mcpCallResult(mcpServerId, actionName, arguments, serverTool, jsonText(response), false, extra);
    }

    private Map<String, Object> builtinWeatherResponse(Map<String, Object> arguments) {
        String city = firstText(arguments, "city", "query-city", "location", "query", "input");
        city = blank(city) ? "Beijing" : city;
        int hash = city.toLowerCase(Locale.ROOT).hashCode() & 0x7fffffff;
        String[] conditions = {"sunny", "cloudy", "overcast", "light rain"};
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("city", city);
        response.put("condition", conditions[hash % conditions.length]);
        response.put("temperatureCelsius", 18 + hash % 15);
        response.put("humidity", 45 + hash % 35);
        response.put("source", "wanwu-java-local");
        return response;
    }

    private Map<String, Object> builtinSearchResponse(Map<String, Object> arguments) {
        String query = firstText(arguments, "query", "keyword", "q", "input");
        query = blank(query) ? "wanwu" : query;
        List<Map<String, Object>> results = new ArrayList<>();
        results.add(searchResult("Wanwu resource center", "Local resource metadata matched: " + query,
                "/user/api/v1/tool/select", 0.91));
        results.add(searchResult("Wanwu knowledge base", "Local knowledge snippet matched: " + query,
                "/user/api/v1/knowledge/hit", 0.84));
        results.add(searchResult("Wanwu application runtime", "Local application runtime trace matched: " + query,
                "/service/api/openapi/v1/app", 0.77));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("results", results);
        response.put("source", "wanwu-java-local");
        return response;
    }

    private Map<String, Object> searchResult(String title, String snippet, String url, double score) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", title);
        result.put("snippet", snippet);
        result.put("url", url);
        result.put("score", score);
        return result;
    }

    private OpenApiOperation openApiOperation(String schema, String operationId) throws IOException {
        JsonNode root = JSON.readTree(defaultString(schema, "{}"));
        String baseUrl = root.path("servers").path(0).path("url").asText("");
        JsonNode paths = root.path("paths");
        if (!paths.isObject()) {
            throw new IOException("openapi paths empty");
        }
        java.util.Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        while (pathIt.hasNext()) {
            Map.Entry<String, JsonNode> path = pathIt.next();
            java.util.Iterator<Map.Entry<String, JsonNode>> methodIt = path.getValue().fields();
            while (methodIt.hasNext()) {
                Map.Entry<String, JsonNode> method = methodIt.next();
                JsonNode operation = method.getValue();
                if (operationId.equals(operation.path("operationId").asText(""))) {
                    return new OpenApiOperation(baseUrl, path.getKey(),
                            method.getKey().toUpperCase(Locale.ROOT), operation);
                }
            }
        }
        throw new IOException("operationId not found: " + operationId);
    }

    private HttpToolResponse callOpenApiOperation(OpenApiOperation operation, Map<String, Object> apiAuth,
                                                   Map<String, Object> arguments) throws IOException {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        Map<String, String> headerParams = new LinkedHashMap<>();
        Map<String, Object> bodyParams = new LinkedHashMap<>();
        String path = operation.path;

        JsonNode parameters = operation.operation.path("parameters");
        if (parameters.isArray()) {
            for (JsonNode parameter : parameters) {
                String in = parameter.path("in").asText("");
                String name = parameter.path("name").asText("");
                Object value = argumentValue(arguments, in + "-" + name, name);
                if (value == null || blank(name)) {
                    continue;
                }
                if ("path".equals(in)) {
                    path = path.replace("{" + name + "}", urlEncode(value));
                } else if ("query".equals(in)) {
                    queryParams.put(name, value);
                } else if ("header".equals(in)) {
                    headerParams.put(name, String.valueOf(value));
                }
            }
        }

        JsonNode requestBody = operation.operation.path("requestBody").path("content");
        if (requestBody.isObject()) {
            java.util.Iterator<JsonNode> mediaTypes = requestBody.elements();
            while (mediaTypes.hasNext()) {
                JsonNode properties = mediaTypes.next().path("schema").path("properties");
                if (properties.isObject()) {
                    java.util.Iterator<String> names = properties.fieldNames();
                    while (names.hasNext()) {
                        String property = names.next();
                        Object value = argumentValue(arguments, property);
                        if (value != null) {
                            bodyParams.put(property, value);
                        }
                    }
                    break;
                }
            }
        }
        if (bodyParams.isEmpty() && !"GET".equals(operation.method)) {
            bodyParams.putAll(arguments);
        }

        applyApiAuth(apiAuth, headerParams, queryParams);
        String url = appendQuery(joinUrl(operation.baseUrl, path), queryParams);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(operation.method);
        connection.setConnectTimeout(MCP_REMOTE_TIMEOUT_MILLIS);
        connection.setReadTimeout(MCP_REMOTE_TIMEOUT_MILLIS);
        connection.setRequestProperty("Accept", "application/json");
        for (Map.Entry<String, String> header : headerParams.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
        if (!bodyParams.isEmpty() && !"GET".equals(operation.method)) {
            byte[] bytes = JSON.writeValueAsBytes(bodyParams);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(bytes);
            }
        }
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        return new HttpToolResponse(status, stream == null ? "" : readFully(stream), url);
    }

    private void applyApiAuth(Map<String, Object> apiAuth, Map<String, String> headers,
                              Map<String, Object> queryParams) {
        String authType = text(apiAuth, "authType");
        if ("api_key_query".equals(authType)) {
            String name = text(apiAuth, "apiKeyQueryParam");
            if (!blank(name)) {
                queryParams.put(name, text(apiAuth, "apiKeyValue"));
            }
            return;
        }
        if (!"api_key_header".equals(authType)) {
            return;
        }
        String header = defaultString(text(apiAuth, "apiKeyHeader"), "Authorization");
        String value = text(apiAuth, "apiKeyValue");
        String prefix = text(apiAuth, "apiKeyHeaderPrefix");
        if ("basic".equals(prefix)) {
            value = "Basic " + value;
        } else if ("bearer".equals(prefix)) {
            value = "Bearer " + value;
        }
        headers.put(header, value);
    }

    private Object argumentValue(Map<String, Object> arguments, String... keys) {
        for (String key : keys) {
            if (arguments.containsKey(key)) {
                return arguments.get(key);
            }
        }
        return null;
    }

    private String joinUrl(String baseUrl, String path) {
        String base = defaultString(baseUrl, "");
        String suffix = defaultString(path, "");
        if (base.endsWith("/") && suffix.startsWith("/")) {
            return base.substring(0, base.length() - 1) + suffix;
        }
        if (!base.endsWith("/") && !suffix.startsWith("/")) {
            return base + "/" + suffix;
        }
        return base + suffix;
    }

    private String appendQuery(String url, Map<String, Object> queryParams) {
        if (queryParams.isEmpty()) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        builder.append(url.contains("?") ? "&" : "?");
        boolean first = true;
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            if (!first) {
                builder.append("&");
            }
            builder.append(urlEncode(entry.getKey())).append("=").append(urlEncode(entry.getValue()));
            first = false;
        }
        return builder.toString();
    }

    private String urlEncode(Object value) {
        try {
            return URLEncoder.encode(String.valueOf(value == null ? "" : value), "UTF-8").replace("+", "%20");
        } catch (Exception ex) {
            return "";
        }
    }

    private Object parseJsonValue(String body) {
        if (blank(body)) {
            return "";
        }
        try {
            return JSON.readValue(body, Object.class);
        } catch (IOException ignored) {
            return body;
        }
    }

    private String jsonText(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    private Map<String, Object> mcpCallResult(String mcpServerId, String name, Map<String, Object> arguments,
                                              Map<String, Object> tool, String text, boolean isError,
                                              Map<String, Object> extraStructuredContent) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "text");
        content.put("text", defaultString(text, ""));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", Collections.singletonList(content));
        Map<String, Object> structuredContent = new LinkedHashMap<>();
        structuredContent.put("mcpServerId", defaultString(mcpServerId, ""));
        structuredContent.put("name", defaultString(name, "unknown"));
        structuredContent.put("arguments", arguments);
        if (!tool.isEmpty()) {
            structuredContent.put("toolId", firstText(tool, "id", "toolId", "mcpServerToolId"));
            structuredContent.put("toolType", firstText(tool, "type", "toolType"));
            structuredContent.put("description", defaultString(text(tool, "desc"), text(tool, "description")));
        }
        structuredContent.putAll(extraStructuredContent);
        result.put("structuredContent", structuredContent);
        result.put("isError", isError);
        return result;
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

    private static final class RemoteMcpResponse {
        private final String body;
        private final String sessionId;

        private RemoteMcpResponse(String body, String sessionId) {
            this.body = body;
            this.sessionId = sessionId;
        }
    }

    private static final class OpenApiOperation {
        private final String baseUrl;
        private final String path;
        private final String method;
        private final JsonNode operation;

        private OpenApiOperation(String baseUrl, String path, String method, JsonNode operation) {
            this.baseUrl = baseUrl;
            this.path = path;
            this.method = method;
            this.operation = operation;
        }
    }

    private static final class HttpToolResponse {
        private final int status;
        private final String body;
        private final String url;

        private HttpToolResponse(int status, String body, String url) {
            this.status = status;
            this.body = body;
            this.url = url;
        }
    }

    private static final class SkillPackage {
        private final byte[] zipData;
        private final String markdown;
        private final String name;
        private final String description;

        private SkillPackage(byte[] zipData, String markdown, String name, String description) {
            this.zipData = zipData;
            this.markdown = markdown;
            this.name = name;
            this.description = description;
        }
    }

    private static final class SkillFrontMatter {
        private final String name;
        private final String description;

        private SkillFrontMatter(String name, String description) {
            this.name = name;
            this.description = description;
        }
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
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
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
