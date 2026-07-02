package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class RecordModelStatisticCommand implements Serializable {

    private String userId;
    private String orgId;
    private String modelId;
    private String model;
    private String provider;
    private String modelType;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private long firstTokenLatency;
    private long costs;
    private boolean success = true;
    private boolean stream;
    private long callTime;

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

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public long getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(long promptTokens) {
        this.promptTokens = promptTokens;
    }

    public long getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(long completionTokens) {
        this.completionTokens = completionTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(long totalTokens) {
        this.totalTokens = totalTokens;
    }

    public long getFirstTokenLatency() {
        return firstTokenLatency;
    }

    public void setFirstTokenLatency(long firstTokenLatency) {
        this.firstTokenLatency = firstTokenLatency;
    }

    public long getCosts() {
        return costs;
    }

    public void setCosts(long costs) {
        this.costs = costs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public long getCallTime() {
        return callTime;
    }

    public void setCallTime(long callTime) {
        this.callTime = callTime;
    }
}
