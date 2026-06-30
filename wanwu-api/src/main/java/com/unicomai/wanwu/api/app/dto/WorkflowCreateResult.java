package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class WorkflowCreateResult implements Serializable {

    private String workflowId;

    public WorkflowCreateResult() {
    }

    public WorkflowCreateResult(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getWorkflow_id() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
}
