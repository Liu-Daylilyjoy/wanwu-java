package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApiKeyStatisticItem implements Serializable {

    private String apiKeyId;
    private String methodPath;
    private long callCount;
    private long callFailure;
    private double avgStreamCosts;
    private double avgNonStreamCosts;
    private long streamCount;
    private long nonStreamCount;

    public String getApiKeyId() {
        return apiKeyId;
    }

    public void setApiKeyId(String apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    public String getMethodPath() {
        return methodPath;
    }

    public void setMethodPath(String methodPath) {
        this.methodPath = methodPath;
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

    public double getAvgStreamCosts() {
        return avgStreamCosts;
    }

    public void setAvgStreamCosts(double avgStreamCosts) {
        this.avgStreamCosts = avgStreamCosts;
    }

    public double getAvgNonStreamCosts() {
        return avgNonStreamCosts;
    }

    public void setAvgNonStreamCosts(double avgNonStreamCosts) {
        this.avgNonStreamCosts = avgNonStreamCosts;
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
}
