package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class WorkflowExportQuery implements Serializable {

    private String workflowId;
    private String version;
    private boolean published;
    private String userId;
    private String orgId;

    public WorkflowExportQuery() {
    }

    public WorkflowExportQuery(String workflowId, String version, boolean published, String userId, String orgId) {
        this.workflowId = workflowId;
        this.version = version;
        this.published = published;
        this.userId = userId;
        this.orgId = orgId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
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
