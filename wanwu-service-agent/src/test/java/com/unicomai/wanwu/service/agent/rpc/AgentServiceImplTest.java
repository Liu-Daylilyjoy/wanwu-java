package com.unicomai.wanwu.service.agent.rpc;

import com.unicomai.wanwu.api.assistant.AssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AgentServiceImplTest {

    private AssistantService assistantService;
    private AgentServiceImpl service;

    @BeforeEach
    public void setUp() {
        assistantService = mock(AssistantService.class);
        service = new AgentServiceImpl(assistantService);
    }

    @Test
    public void createAgentDelegatesToAssistantCreateAndAddsAgentIdAlias() {
        Map<String, Object> request = map("assistantBrief", map("name", "Agent"));
        when(assistantService.assistantCreate(request)).thenReturn(map("assistantId", "agent-1"));

        Map<String, Object> response = service.createAgent(request);

        verify(assistantService).assistantCreate(request);
        assertEquals("agent-1", response.get("assistantId"));
        assertEquals("agent-1", response.get("agentId"));
    }

    @Test
    public void listAgentsReturnsAgentInfosAlias() {
        Map<String, Object> request = map("name", "Agent");
        when(assistantService.getAssistantListMyAll(request)).thenReturn(map(
                "assistantInfos", Collections.singletonList(map("assistantId", "agent-1")),
                "total", 1L));

        Map<String, Object> response = service.listAgents(request);

        verify(assistantService).getAssistantListMyAll(request);
        assertEquals(response.get("assistantInfos"), response.get("agentInfos"));
        assertEquals(1L, response.get("total"));
    }

    @Test
    public void publishAndChatDelegateToAssistantService() {
        Map<String, Object> publishRequest = map("assistantId", "agent-1", "version", "v0.0.1");
        when(assistantService.assistantSnapshotCreate(publishRequest)).thenReturn(map("snapshotId", "agent-1:v0.0.1"));
        assertEquals("agent-1:v0.0.1", service.publishAgent(publishRequest).get("snapshotId"));
        verify(assistantService).assistantSnapshotCreate(publishRequest);

        Map<String, Object> chatRequest = map("assistantId", "agent-1", "prompt", "hello");
        when(assistantService.assistantConversionStream(chatRequest)).thenReturn(map("content", "world"));
        assertEquals("world", service.chatAgent(chatRequest).get("content"));
        verify(assistantService).assistantConversionStream(chatRequest);
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }
}
