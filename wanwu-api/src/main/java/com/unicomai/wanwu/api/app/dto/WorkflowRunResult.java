package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.Map;

public class WorkflowRunResult implements Serializable {

    private String workflowId;
    private Map<String, Object> output;

    public WorkflowRunResult() {
    }

    public WorkflowRunResult(String workflowId, Map<String, Object> output) {
        this.workflowId = workflowId;
        this.output = output;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }
}
