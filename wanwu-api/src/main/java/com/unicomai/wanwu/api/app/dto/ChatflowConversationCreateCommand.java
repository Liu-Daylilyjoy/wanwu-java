package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ChatflowConversationCreateCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String chatflowId;
    private String conversationName;
    private String userId;
    private String orgId;

    public String getChatflowId() {
        return chatflowId;
    }

    public void setChatflowId(String chatflowId) {
        this.chatflowId = chatflowId;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
