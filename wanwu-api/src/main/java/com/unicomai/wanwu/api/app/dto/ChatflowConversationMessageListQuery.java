package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ChatflowConversationMessageListQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String chatflowId;
    private String conversationId;
    private int limit;
    private String userId;
    private String orgId;

    public String getChatflowId() {
        return chatflowId;
    }

    public void setChatflowId(String chatflowId) {
        this.chatflowId = chatflowId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
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
