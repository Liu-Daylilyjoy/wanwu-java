package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationPageResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantPublishedQuery;
import com.unicomai.wanwu.api.app.dto.AppUrlInfo;
import com.unicomai.wanwu.api.app.dto.AppUrlSuffixQuery;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WanwuOpenUrlApiControllerTest {

    private final AppService appService = mock(AppService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WanwuOpenUrlApiController(appService))
            .build();

    @Test
    public void openUrlInfoReturnsPublishedAssistantAndUrlInfo() throws Exception {
        when(appService.getAppUrlBySuffix(any(AppUrlSuffixQuery.class))).thenReturn(appUrlInfo("assistant-001"));
        Map<String, Object> assistant = new LinkedHashMap<>();
        assistant.put("assistantId", "assistant-001");
        assistant.put("name", "PublishedAgent");
        assistant.put("desc", "published desc");
        when(appService.getPublishedAssistant(any(AssistantPublishedQuery.class))).thenReturn(assistant);

        mockMvc.perform(get("/openurl/v1/agent/suffix-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.assistant.name").value("PublishedAgent"))
                .andExpect(jsonPath("$.data.appUrlInfo.suffix").value("suffix-001"));

        mockMvc.perform(get("/service/url/openurl/v1/agent/suffix-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assistant.assistantId").value("assistant-001"));

        verify(appService, times(2)).getAppUrlBySuffix(any(AppUrlSuffixQuery.class));
        verify(appService, times(2)).getPublishedAssistant(any(AssistantPublishedQuery.class));
    }

    @Test
    public void openUrlConversationAndStreamUseClientIdentityFromHeader() throws Exception {
        when(appService.getAppUrlBySuffix(any(AppUrlSuffixQuery.class))).thenReturn(appUrlInfo("assistant-001"));
        when(appService.createAssistantConversation(any(AssistantConversationCreateCommand.class)))
                .thenReturn(new AssistantConversationCreateResult("conversation-001"));
        AssistantConversationStreamResult streamResult = new AssistantConversationStreamResult();
        streamResult.setAssistantId("assistant-001");
        streamResult.setConversationId("conversation-001");
        streamResult.setDetailId("detail-001");
        streamResult.setResponse("Public answer.");
        when(appService.streamAssistantConversation(any(AssistantConversationStreamCommand.class))).thenReturn(streamResult);

        mockMvc.perform(post("/service/url/openurl/v1/agent/suffix-001/conversation")
                        .header("X-Client-ID", "client-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello public\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.conversationId").value("conversation-001"));

        mockMvc.perform(post("/service/url/openurl/v1/agent/suffix-001/stream")
                        .header("X-Client-ID", "client-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conversationId\":\"conversation-001\",\"prompt\":\"continue public\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("Public answer.")))
                .andExpect(content().string(containsString("\"conversationId\":\"conversation-001\"")));

        org.mockito.ArgumentCaptor<AssistantConversationCreateCommand> createCaptor =
                forClass(AssistantConversationCreateCommand.class);
        verify(appService).createAssistantConversation(createCaptor.capture());
        assertEquals("assistant-001", createCaptor.getValue().getAssistantId());
        assertEquals("hello public", createCaptor.getValue().getPrompt());
        assertEquals("published", createCaptor.getValue().getConversationType());
        assertEquals("client-001", createCaptor.getValue().getUserId());
        assertEquals("default-org", createCaptor.getValue().getOrgId());

        org.mockito.ArgumentCaptor<AssistantConversationStreamCommand> streamCaptor =
                forClass(AssistantConversationStreamCommand.class);
        verify(appService).streamAssistantConversation(streamCaptor.capture());
        assertEquals("assistant-001", streamCaptor.getValue().getAssistantId());
        assertEquals("conversation-001", streamCaptor.getValue().getConversationId());
        assertEquals("continue public", streamCaptor.getValue().getPrompt());
        assertEquals(false, streamCaptor.getValue().isDraft());
        assertEquals("client-001", streamCaptor.getValue().getUserId());
    }

    @Test
    public void openUrlConversationHistoryRoutesUsePublicIdentity() throws Exception {
        when(appService.getAppUrlBySuffix(any(AppUrlSuffixQuery.class))).thenReturn(appUrlInfo("assistant-001"));
        when(appService.listAssistantConversations(any(AssistantConversationListQuery.class)))
                .thenReturn(new AssistantConversationPageResult(Collections.emptyList(), 0, 1, 1000));
        when(appService.listAssistantConversationDetails(any(AssistantConversationDetailQuery.class)))
                .thenReturn(new AssistantConversationPageResult(Collections.emptyList(), 0, 1, 1000));

        mockMvc.perform(get("/service/url/openurl/v1/agent/suffix-001/conversation/list")
                        .header("X-Client-ID", "client-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list").isArray());

        mockMvc.perform(get("/service/url/openurl/v1/agent/suffix-001/conversation/detail")
                        .header("X-Client-ID", "client-001")
                        .param("conversationId", "conversation-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        verify(appService).listAssistantConversations(any(AssistantConversationListQuery.class));
        verify(appService).listAssistantConversationDetails(any(AssistantConversationDetailQuery.class));
    }

    private AppUrlInfo appUrlInfo(String assistantId) {
        AppUrlInfo info = new AppUrlInfo();
        info.setUrlId("1");
        info.setAppId(assistantId);
        info.setAppType("agent");
        info.setName("Public demo");
        info.setCreatedAt("2026-06-29 10:00:00");
        info.setExpiredAt("2026-07-01 12:30:00");
        info.setSuffix("suffix-001");
        info.setStatus(true);
        info.setUserId("dev-admin");
        info.setOrgId("default-org");
        info.setDescription("open desc");
        return info;
    }
}
