package com.unicomai.wanwu.service.app.domain;

public class AppStatisticAggregateRecord {

    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String orgId;
    private String userId;
    private String appId;
    private String appType;
    private String date;
    private long callCount;
    private long callFailure;
    private long streamCount;
    private long streamFailure;
    private long streamCosts;
    private long nonStreamCount;
    private long nonStreamFailure;
    private long nonStreamCosts;
    private long webCallCount;
    private long webCallFailure;
    private long openapiCallCount;
    private long openapiCallFailure;
    private long webUrlCallCount;
    private long webUrlCallFailure;

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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
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

    public long getStreamFailure() {
        return streamFailure;
    }

    public void setStreamFailure(long streamFailure) {
        this.streamFailure = streamFailure;
    }

    public long getStreamCosts() {
        return streamCosts;
    }

    public void setStreamCosts(long streamCosts) {
        this.streamCosts = streamCosts;
    }

    public long getNonStreamCount() {
        return nonStreamCount;
    }

    public void setNonStreamCount(long nonStreamCount) {
        this.nonStreamCount = nonStreamCount;
    }

    public long getNonStreamFailure() {
        return nonStreamFailure;
    }

    public void setNonStreamFailure(long nonStreamFailure) {
        this.nonStreamFailure = nonStreamFailure;
    }

    public long getNonStreamCosts() {
        return nonStreamCosts;
    }

    public void setNonStreamCosts(long nonStreamCosts) {
        this.nonStreamCosts = nonStreamCosts;
    }

    public long getWebCallCount() {
        return webCallCount;
    }

    public void setWebCallCount(long webCallCount) {
        this.webCallCount = webCallCount;
    }

    public long getWebCallFailure() {
        return webCallFailure;
    }

    public void setWebCallFailure(long webCallFailure) {
        this.webCallFailure = webCallFailure;
    }

    public long getOpenapiCallCount() {
        return openapiCallCount;
    }

    public void setOpenapiCallCount(long openapiCallCount) {
        this.openapiCallCount = openapiCallCount;
    }

    public long getOpenapiCallFailure() {
        return openapiCallFailure;
    }

    public void setOpenapiCallFailure(long openapiCallFailure) {
        this.openapiCallFailure = openapiCallFailure;
    }

    public long getWebUrlCallCount() {
        return webUrlCallCount;
    }

    public void setWebUrlCallCount(long webUrlCallCount) {
        this.webUrlCallCount = webUrlCallCount;
    }

    public long getWebUrlCallFailure() {
        return webUrlCallFailure;
    }

    public void setWebUrlCallFailure(long webUrlCallFailure) {
        this.webUrlCallFailure = webUrlCallFailure;
    }
}
