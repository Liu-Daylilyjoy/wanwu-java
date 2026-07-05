package com.unicomai.wanwu.api.agent;

import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

public interface AgentService {

    ServiceDescriptor describe();

    Map<String, Object> createAgent(Map<String, Object> request);

    void updateAgent(Map<String, Object> request);

    void updateAgentConfig(Map<String, Object> request);

    void deleteAgent(Map<String, Object> request);

    Map<String, Object> listAgents(Map<String, Object> request);

    Map<String, Object> getAgent(Map<String, Object> request);

    Map<String, Object> copyAgent(Map<String, Object> request);

    Map<String, Object> publishAgent(Map<String, Object> request);

    void updatePublishedAgent(Map<String, Object> request);

    Map<String, Object> listAgentVersions(Map<String, Object> request);

    void rollbackAgentDraft(Map<String, Object> request);

    Map<String, Object> createConversation(Map<String, Object> request);

    void deleteConversation(Map<String, Object> request);

    Map<String, Object> listConversations(Map<String, Object> request);

    Map<String, Object> listConversationDetails(Map<String, Object> request);

    Map<String, Object> chatAgent(Map<String, Object> request);
}
