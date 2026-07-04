package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.Map;

public class WorkflowRunResult implements Serializable {

    private String workflowId;
    private String runId;
    private String status;
    private Map<String, Object> output;
    private long createdAt;
    private long finishedAt;
    private long costMillis;

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

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(long finishedAt) {
        this.finishedAt = finishedAt;
    }

    public long getCostMillis() {
        return costMillis;
    }

    public void setCostMillis(long costMillis) {
        this.costMillis = costMillis;
    }
}
