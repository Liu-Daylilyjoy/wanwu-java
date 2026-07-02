package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ModelStatisticItem implements Serializable {

    private String modelId;
    private String model;
    private String provider;
    private String orgId;
    private long callCount;
    private long callFailure;
    private double failureRate;
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;
    private double avgCosts;
    private double avgFirstTokenLatency;

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

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public long getCallCount() {
        return callCount;
    }

    public void setCallCount(long callCount) {
        this.callCount = callCount;
    }

    public long getCallFailure() {
        return callFailure;
    }

    public void setCallFailure(long callFailure) {
        this.callFailure = callFailure;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
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

    public double getAvgCosts() {
        return avgCosts;
    }

    public void setAvgCosts(double avgCosts) {
        this.avgCosts = avgCosts;
    }

    public double getAvgFirstTokenLatency() {
        return avgFirstTokenLatency;
    }

    public void setAvgFirstTokenLatency(double avgFirstTokenLatency) {
        this.avgFirstTokenLatency = avgFirstTokenLatency;
    }
}
