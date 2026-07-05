package com.unicomai.wanwu.service.agent.rpc;

import com.unicomai.wanwu.api.agent.AgentService;
import com.unicomai.wanwu.api.assistant.AssistantService;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.LinkedHashMap;
import java.util.Map;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class AgentServiceImpl implements AgentService {

    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private AssistantService assistantService;

    public AgentServiceImpl() {
    }

    AgentServiceImpl(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.AGENT, "Agent Service", "agent");
    }

    @Override
    public Map<String, Object> createAgent(Map<String, Object> request) {
        return withAgentId(assistantService().assistantCreate(safe(request)));
    }

    @Override
    public void updateAgent(Map<String, Object> request) {
        assistantService().assistantUpdate(safe(request));
    }

    @Override
    public void updateAgentConfig(Map<String, Object> request) {
        assistantService().assistantConfigUpdate(safe(request));
    }

    @Override
    public void deleteAgent(Map<String, Object> request) {
        assistantService().assistantDelete(safe(request));
    }

    @Override
    public Map<String, Object> listAgents(Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<String, Object>(
                assistantService().getAssistantListMyAll(safe(request)));
        response.put("agentInfos", response.get("assistantInfos"));
        return response;
    }

    @Override
    public Map<String, Object> getAgent(Map<String, Object> request) {
        return withAgentId(assistantService().getAssistantInfo(safe(request)));
    }

    @Override
    public Map<String, Object> copyAgent(Map<String, Object> request) {
        return withAgentId(assistantService().assistantCopy(safe(request)));
    }

    @Override
    public Map<String, Object> publishAgent(Map<String, Object> request) {
        return assistantService().assistantSnapshotCreate(safe(request));
    }

    @Override
    public void updatePublishedAgent(Map<String, Object> request) {
        assistantService().assistantSnapshotUpdate(safe(request));
    }

    @Override
    public Map<String, Object> listAgentVersions(Map<String, Object> request) {
        return assistantService().assistantSnapshotList(safe(request));
    }

    @Override
    public void rollbackAgentDraft(Map<String, Object> request) {
        assistantService().assistantSnapshotRollback(safe(request));
    }

    @Override
    public Map<String, Object> createConversation(Map<String, Object> request) {
        return assistantService().conversationCreate(safe(request));
    }

    @Override
    public void deleteConversation(Map<String, Object> request) {
        assistantService().conversationDelete(safe(request));
    }

    @Override
    public Map<String, Object> listConversations(Map<String, Object> request) {
        return assistantService().getConversationList(safe(request));
    }

    @Override
    public Map<String, Object> listConversationDetails(Map<String, Object> request) {
        return assistantService().getConversationDetailList(safe(request));
    }

    @Override
    public Map<String, Object> chatAgent(Map<String, Object> request) {
        return assistantService().assistantConversionStream(safe(request));
    }

    private Map<String, Object> withAgentId(Map<String, Object> source) {
        Map<String, Object> response = new LinkedHashMap<String, Object>(safe(source));
        Object assistantId = response.get("assistantId");
        if (assistantId != null) {
            response.put("agentId", assistantId);
        }
        return response;
    }

    private Map<String, Object> safe(Map<String, Object> request) {
        return request == null ? new LinkedHashMap<String, Object>() : request;
    }

    private AssistantService assistantService() {
        if (assistantService == null) {
            throw new IllegalStateException("AssistantService is not available");
        }
        return assistantService;
    }
}
