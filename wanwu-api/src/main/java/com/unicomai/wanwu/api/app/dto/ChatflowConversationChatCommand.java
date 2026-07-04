package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChatflowConversationChatCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String chatflowId;
    private String conversationId;
    private String query;
    private Map<String, Object> parameters = new LinkedHashMap<>();
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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters == null ? new LinkedHashMap<String, Object>() : new LinkedHashMap<>(parameters);
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
