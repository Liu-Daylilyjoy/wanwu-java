package com.unicomai.wanwu.service.app.domain;

public class ModelStatisticAggregateRecord {

    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String orgId;
    private String userId;
    private String modelId;
    private String model;
    private String provider;
    private String modelType;
    private String date;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private long firstTokenLatency;
    private long costs;
    private long callCount;
    private long streamCount;
    private long nonStreamCount;
    private long callFailure;
    private long streamFailure;
    private long nonStreamFailure;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public long getCallCount() {
        return callCount;
    }

    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    public long getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(long streamCount) {
        this.streamCount = streamCount;
    }

    public long getNonStreamCount() {
        return nonStreamCount;
    }

    public void setNonStreamCount(long nonStreamCount) {
        this.nonStreamCount = nonStreamCount;
    }

    public long getCallFailure() {
        return callFailure;
    }

    public void setCallFailure(long callFailure) {
        this.callFailure = callFailure;
    }

    public long getStreamFailure() {
        return streamFailure;
    }

    public void setStreamFailure(long streamFailure) {
        this.streamFailure = streamFailure;
    }

    public long getNonStreamFailure() {
        return nonStreamFailure;
    }

    public void setNonStreamFailure(long nonStreamFailure) {
        this.nonStreamFailure = nonStreamFailure;
    }
}
