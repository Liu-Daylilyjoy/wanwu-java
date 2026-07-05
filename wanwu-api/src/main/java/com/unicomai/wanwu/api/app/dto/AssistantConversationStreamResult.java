package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class AssistantConversationStreamResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assistantId;
    private String conversationId;
    private String detailId;
    private String prompt;
    private String response;
    private List<Map<String, Object>> searchList;
    private long createdAt;

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<Map<String, Object>> getSearchList() {
        return searchList;
    }

    public void setSearchList(List<Map<String, Object>> searchList) {
        this.searchList = searchList;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
