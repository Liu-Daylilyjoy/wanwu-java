package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AssistantDetailQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assistantId;
    private String userId;
    private String orgId;

    public AssistantDetailQuery() {
    }

    public AssistantDetailQuery(String assistantId, String userId, String orgId) {
        this.assistantId = assistantId;
        this.userId = userId;
        this.orgId = orgId;
    }

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
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
