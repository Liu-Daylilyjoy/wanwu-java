package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AssistantPublishedQuery implements Serializable {

    private String assistantId;
    private String version;
    private String userId;
    private String orgId;

    public AssistantPublishedQuery() {
    }

    public AssistantPublishedQuery(String assistantId, String version, String userId, String orgId) {
        this.assistantId = assistantId;
        this.version = version;
        this.userId = userId;
        this.orgId = orgId;
    }

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
