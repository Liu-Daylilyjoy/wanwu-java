package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AssistantConversationCreateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String conversationId;

    public AssistantConversationCreateResult() {
    }

    public AssistantConversationCreateResult(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
