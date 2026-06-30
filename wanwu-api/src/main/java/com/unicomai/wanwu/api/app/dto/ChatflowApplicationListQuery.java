package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ChatflowApplicationListQuery implements Serializable {

    private String workflowId;
    private String userId;
    private String orgId;

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
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
