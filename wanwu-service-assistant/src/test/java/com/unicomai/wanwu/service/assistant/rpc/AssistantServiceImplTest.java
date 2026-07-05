package com.unicomai.wanwu.service.assistant.rpc;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.GeneralAgentConversationStateCommand;
import com.unicomai.wanwu.api.mcp.McpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AssistantServiceImplTest {

    private AppService appService;
    private McpService mcpService;
    private AssistantServiceImpl service;

    @BeforeEach
    public void setUp() {
        appService = mock(AppService.class);
        mcpService = mock(McpService.class);
        service = new AssistantServiceImpl(appService, mcpService);
    }

    @Test
    public void assistantCreateMapsIdentityAndBriefToAppService() {
        when(appService.createAssistant(any(AssistantCreateCommand.class))).thenReturn(new AssistantCreateResult("assistant-1"));

        Map<String, Object> response = service.assistantCreate(map(
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "category", 2,
                "assistantBrief", map(
                        "name", "Support Agent",
                        "description", "answers tickets",
                        "avatar", map("key", "avatar-key", "path", "/avatars/agent.png"))));

        ArgumentCaptor<AssistantCreateCommand> captor = ArgumentCaptor.forClass(AssistantCreateCommand.class);
        verify(appService).createAssistant(captor.capture());
        AssistantCreateCommand command = captor.getValue();
        assertEquals("assistant-1", response.get("assistantId"));
        assertEquals("user-a", command.getUserId());
        assertEquals("org-a", command.getOrgId());
        assertEquals("Support Agent", command.getName());
        assertEquals("answers tickets", command.getDesc());
        assertEquals(2, command.getCategory());
        assertEquals("avatar-key", command.getAvatarKey());
        assertEquals("/avatars/agent.png", command.getAvatarPath());
    }

    @Test
    public void assistantConfigUpdateMapsCoreProtoFields() {
        service.assistantConfigUpdate(map(
                "assistantId", "assistant-1",
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "instructions", "be helpful",
                "prologue", "hello",
                "recommendQuestion", Collections.singletonList("What can you do?"),
                "modelConfig", map("modelId", "llm-1"),
                "memoryConfig", map("maxHistoryLength", 6)));

        ArgumentCaptor<AssistantConfigUpdateCommand> captor = ArgumentCaptor.forClass(AssistantConfigUpdateCommand.class);
        verify(appService).updateAssistantConfig(captor.capture());
        AssistantConfigUpdateCommand command = captor.getValue();
        assertEquals("assistant-1", command.getAssistantId());
        assertEquals("user-a", command.getUserId());
        assertEquals("be helpful", command.getInstructions());
        assertEquals("What can you do?", command.getRecommendQuestion().get(0));
        assertEquals("llm-1", command.getModelConfig().get("modelId"));
        assertEquals(6, command.getMemoryConfig().get("maxHistoryLength"));
    }

    @Test
    public void assistantSnapshotCreatePublishesAgentAndReturnsLatestSnapshot() {
        when(appService.getLatestAppVersion(any())).thenReturn(
                new AppVersionInfo("v0.0.1", "first publish", "2026-07-04 10:00:00", "private"));

        Map<String, Object> response = service.assistantSnapshotCreate(map(
                "assistantId", "assistant-1",
                "version", "v0.0.1",
                "desc", "first publish",
                "identity", map("userId", "user-a", "orgId", "org-a")));

        ArgumentCaptor<AppPublishCommand> captor = ArgumentCaptor.forClass(AppPublishCommand.class);
        verify(appService).publishApp(captor.capture());
        AppPublishCommand command = captor.getValue();
        assertEquals("assistant-1", command.getAppId());
        assertEquals("agent", command.getAppType());
        assertEquals("v0.0.1", command.getVersion());
        assertEquals("assistant-1:v0.0.1", response.get("snapshotId"));
        assertTrue((Long) response.get("createAt") > 0L);
    }

    @Test
    public void assistantConversionStreamDelegatesDraftRequest() {
        AssistantConversationStreamResult appResult = new AssistantConversationStreamResult();
        appResult.setAssistantId("assistant-1");
        appResult.setConversationId("conversation-1");
        appResult.setDetailId("detail-1");
        appResult.setPrompt("hello");
        appResult.setResponse("world");
        appResult.setCreatedAt(123L);
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class))).thenReturn(appResult);

        Map<String, Object> response = service.assistantConversionStream(map(
                "assistantId", "assistant-1",
                "conversationId", "conversation-1",
                "prompt", "hello",
                "draft", false,
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "fileInfo", Collections.singletonList(map("fileName", "a.txt"))));

        ArgumentCaptor<AssistantConversationStreamCommand> captor =
                ArgumentCaptor.forClass(AssistantConversationStreamCommand.class);
        verify(appService).streamAssistantConversation(captor.capture());
        AssistantConversationStreamCommand command = captor.getValue();
        assertFalse(command.isDraft());
        assertEquals("user-a", command.getUserId());
        assertEquals(1, command.getFileInfo().size());
        assertEquals("world", response.get("content"));
        assertEquals("detail-1", response.get("detailId"));
    }

    @Test
    public void esCompatibilityShellStoresSearchesAndDeletesJsonDocs() {
        service.saveToES(map("index_name", "assistant", "doc_json", "{\"conversationId\":\"c1\",\"text\":\"hello\"}"));
        service.saveToES(map("index_name", "assistant", "doc_json", "{\"conversationId\":\"c2\",\"text\":\"bye\"}"));

        Map<String, Object> firstSearch = service.searchFromES(map(
                "index_name", "assistant",
                "conditions", map("conversationId", "c1")));
        assertEquals(1L, firstSearch.get("total"));
        assertEquals("{\"conversationId\":\"c1\",\"text\":\"hello\"}", firstString(firstSearch.get("doc_json_list")));

        service.deleteFromES(map("index_name", "assistant", "conditions", map("conversationId", "c1")));
        Map<String, Object> secondSearch = service.searchFromES(map("index_name", "assistant"));
        assertEquals(1L, secondSearch.get("total"));
        assertEquals("{\"conversationId\":\"c2\",\"text\":\"bye\"}", firstString(secondSearch.get("doc_json_list")));
    }

    @Test
    public void customPromptCreateDelegatesToMcpService() {
        when(mcpService.createCustomPrompt(any(), any(), any())).thenReturn(map("customPromptId", "prompt-1"));

        Map<String, Object> response = service.customPromptCreate(map(
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "name", "Prompt",
                "prompt", "Be concise"));

        verify(mcpService).createCustomPrompt("user-a", "org-a", map(
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "name", "Prompt",
                "prompt", "Be concise"));
        assertEquals("prompt-1", response.get("customPromptId"));
    }

    @Test
    public void updateWgaConfigMapsGoListsToGeneralAgentConfig() {
        service.updateWgaConfig(map(
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "toolList", Collections.singletonList(map("toolId", "tool-1", "toolType", "custom"))));

        ArgumentCaptor<GeneralAgentConfigUpdateCommand> captor =
                ArgumentCaptor.forClass(GeneralAgentConfigUpdateCommand.class);
        verify(appService).updateGeneralAgentConfig(captor.capture());
        GeneralAgentConfigUpdateCommand command = captor.getValue();
        assertEquals("user-a", command.getUserId());
        assertEquals("org-a", command.getOrgId());
        assertEquals("tool-1", command.getConfig().get("toolList").get(0).get("toolId"));
    }

    @Test
    public void wgaConversationCreateSavesGeneralAgentState() {
        when(appService.saveGeneralAgentConversationState(any())).thenReturn(map("threadId", "thread-1"));

        Map<String, Object> response = service.wgaConversationCreate(map(
                "threadId", "thread-1",
                "prompt", "build workspace",
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "modelConfig", map("modelId", "llm-1")));

        ArgumentCaptor<GeneralAgentConversationStateCommand> captor =
                ArgumentCaptor.forClass(GeneralAgentConversationStateCommand.class);
        verify(appService).saveGeneralAgentConversationState(captor.capture());
        GeneralAgentConversationStateCommand command = captor.getValue();
        assertEquals("thread-1", response.get("threadId"));
        assertEquals("thread-1", command.getThreadId());
        assertEquals("build workspace", command.getTitle());
        assertEquals("llm-1", command.getModelConfig().get("modelId"));
    }

    @SuppressWarnings("unchecked")
    private String firstString(Object value) {
        return ((List<String>) value).get(0);
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }
}
