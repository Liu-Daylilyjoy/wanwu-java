package com.unicomai.wanwu.api.mcp;

import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

public interface McpService {

    ServiceDescriptor describe();

    Map<String, Object> createCustomTool(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getCustomTool(String userId, String orgId, String customToolId);

    void updateCustomTool(String userId, String orgId, Map<String, Object> request);

    void deleteCustomTool(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listCustomTools(String userId, String orgId, String name);

    Map<String, Object> parseCustomToolSchema(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listToolSquares(String userId, String orgId, String name);

    Map<String, Object> getToolSquare(String userId, String orgId, String toolSquareId);

    void updateToolSquareApiKey(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listToolSelect(String userId, String orgId, String name);

    Map<String, Object> listToolActions(String userId, String orgId, String toolId, String toolType);

    Map<String, Object> getToolActionDetail(String userId, String orgId, String toolId, String toolType,
                                            String actionName);

    Map<String, Object> createMcp(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getMcp(String userId, String orgId, String mcpId);

    void updateMcp(String userId, String orgId, Map<String, Object> request);

    void deleteMcp(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listMcps(String userId, String orgId, String name);

    Map<String, Object> listMcpTools(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> createMcpServer(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getMcpServer(String userId, String orgId, String mcpServerId);

    void updateMcpServer(String userId, String orgId, Map<String, Object> request);

    void deleteMcpServer(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listMcpServers(String userId, String orgId, String name);

    Map<String, Object> createMcpServerTool(String userId, String orgId, Map<String, Object> request);

    void updateMcpServerTool(String userId, String orgId, Map<String, Object> request);

    void deleteMcpServerTool(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> createMcpServerOpenApiTool(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listMcpSelect(String userId, String orgId, String name);

    Map<String, Object> listMcpActions(String userId, String orgId, String toolId, String toolType);

    Map<String, Object> listMcpSquares(String userId, String orgId, String name);

    Map<String, Object> getMcpSquare(String userId, String orgId, String mcpSquareId);

    Map<String, Object> recommendMcpSquares(String userId, String orgId, String mcpSquareId);

    Map<String, Object> createCustomPrompt(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getCustomPrompt(String userId, String orgId, String customPromptId);

    void updateCustomPrompt(String userId, String orgId, Map<String, Object> request);

    void deleteCustomPrompt(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listCustomPrompts(String userId, String orgId, String name);

    Map<String, Object> copyCustomPrompt(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listPromptTemplates(String userId, String orgId, String name);

    Map<String, Object> getPromptTemplate(String userId, String orgId, String templateId);

    Map<String, Object> createPromptByTemplate(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> optimizePrompt(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> reasonPrompt(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> evaluatePrompt(String userId, String orgId, Map<String, Object> request);
}
