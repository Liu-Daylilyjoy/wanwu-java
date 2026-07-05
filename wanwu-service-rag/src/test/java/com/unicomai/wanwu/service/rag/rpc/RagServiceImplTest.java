package com.unicomai.wanwu.service.rag.rpc;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RagConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
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

public class RagServiceImplTest {

    private AppService appService;
    private RagServiceImpl service;

    @BeforeEach
    public void setUp() {
        appService = mock(AppService.class);
        service = new RagServiceImpl(appService);
    }

    @Test
    public void createRagMapsIdentityAndBriefToAppService() {
        when(appService.createRag(any(RagCreateCommand.class))).thenReturn(new RagCreateResult("rag-1"));

        Map<String, Object> result = service.createRag(map(
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "appBrief", map(
                        "name", "Knowledge Bot",
                        "description", "answers docs",
                        "avatar", map("key", "avatar-key", "path", "/avatars/rag.png"))));

        ArgumentCaptor<RagCreateCommand> captor = ArgumentCaptor.forClass(RagCreateCommand.class);
        verify(appService).createRag(captor.capture());
        RagCreateCommand command = captor.getValue();
        assertEquals("rag-1", result.get("ragId"));
        assertEquals("user-a", command.getUserId());
        assertEquals("org-a", command.getOrgId());
        assertEquals("Knowledge Bot", command.getName());
        assertEquals("answers docs", command.getDesc());
        assertEquals("avatar-key", command.getAvatarKey());
        assertEquals("/avatars/rag.png", command.getAvatarPath());
    }

    @Test
    public void updateRagConfigAcceptsGoProtoFieldNames() {
        service.updateRagConfig(map(
                "ragId", "rag-1",
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "modelConfig", map("modelId", "llm-1"),
                "QArerankConfig", map("modelId", "qa-rerank"),
                "QAknowledgeBaseConfig", map("perKnowledgeConfigs", Collections.emptyList()),
                "sensitiveConfig", map("enable", true)));

        ArgumentCaptor<RagConfigUpdateCommand> captor = ArgumentCaptor.forClass(RagConfigUpdateCommand.class);
        verify(appService).updateRagConfig(captor.capture());
        RagConfigUpdateCommand command = captor.getValue();
        assertEquals("rag-1", command.getRagId());
        assertEquals("user-a", command.getUserId());
        assertEquals("org-a", command.getOrgId());
        assertEquals("llm-1", command.getModelConfig().get("modelId"));
        assertEquals("qa-rerank", command.getQaRerankConfig().get("modelId"));
        assertTrue((Boolean) command.getSafetyConfig().get("enable"));
    }

    @Test
    public void chatRagDelegatesPublishedRequestAndReturnsSearchLists() {
        RagChatResult appResult = new RagChatResult();
        appResult.setRagId("rag-1");
        appResult.setQuestion("hello");
        appResult.setResponse("world");
        appResult.setCreatedAt(123L);
        appResult.setSearchList(Collections.singletonList(map("title", "doc")));
        when(appService.streamRagChat(any(RagChatCommand.class))).thenReturn(appResult);

        Map<String, Object> response = service.chatRag(map(
                "ragId", "rag-1",
                "question", "hello",
                "publish", 1,
                "identity", map("userId", "user-a", "orgId", "org-a"),
                "history", Collections.singletonList(map("query", "old", "response", "answer")),
                "fileInfoList", Collections.singletonList(map("fileName", "a.txt", "fileSize", 3L))));

        ArgumentCaptor<RagChatCommand> captor = ArgumentCaptor.forClass(RagChatCommand.class);
        verify(appService).streamRagChat(captor.capture());
        RagChatCommand command = captor.getValue();
        assertFalse(command.isDraft());
        assertEquals("user-a", command.getUserId());
        assertEquals(1, command.getHistory().size());
        assertEquals(1, command.getFileInfo().size());
        assertEquals("world", response.get("content"));
        assertEquals("doc", firstMap(response.get("searchList")).get("title"));
    }

    @Test
    public void listPublishRagHistoryReturnsGoStyleHistoryItems() {
        when(appService.listAppVersions(any(AppVersionQuery.class))).thenReturn(new AppVersionListResult(
                Arrays.asList(new AppVersionInfo("v0.0.1", "first publish", "2026-07-04 10:00:00", "private")),
                1));

        Map<String, Object> response = service.listPublishRagHistory(map(
                "ragId", "rag-1",
                "identity", map("userId", "user-a", "orgId", "org-a")));

        ArgumentCaptor<AppVersionQuery> captor = ArgumentCaptor.forClass(AppVersionQuery.class);
        verify(appService).listAppVersions(captor.capture());
        assertEquals("rag", captor.getValue().getAppType());
        assertEquals("rag-1", captor.getValue().getAppId());
        assertEquals(1L, response.get("total"));
        Map<String, Object> item = firstMap(response.get("historyList"));
        assertEquals("rag-1", item.get("ragId"));
        assertEquals("v0.0.1", item.get("version"));
        assertTrue((Long) item.get("createAt") > 0L);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstMap(Object value) {
        return ((List<Map<String, Object>>) value).get(0);
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }
}
