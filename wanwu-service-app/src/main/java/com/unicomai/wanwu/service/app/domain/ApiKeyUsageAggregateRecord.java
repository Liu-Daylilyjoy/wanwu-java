package com.unicomai.wanwu.service.app.domain;

public class ApiKeyUsageAggregateRecord {

    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String orgId;
    private String userId;
    private String apiKeyId;
    private String methodPath;
    private String date;
    private long callCount;
    private long callFailure;
    private long streamCount;
    private long nonStreamCount;
    private long streamFailure;
    private long nonStreamFailure;
    private long streamCosts;
    private long nonStreamCosts;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public long getStreamCosts() {
        return streamCosts;
    }

    public void setStreamCosts(long streamCosts) {
        this.streamCosts = streamCosts;
    }

    public long getNonStreamCosts() {
        return nonStreamCosts;
    }

    public void setNonStreamCosts(long nonStreamCosts) {
        this.nonStreamCosts = nonStreamCosts;
    }
}
