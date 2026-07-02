package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class RecordAppStatisticCommand implements Serializable {

    private String userId;
    private String orgId;
    private String appId;
    private String appType;
    private boolean success = true;
    private boolean stream;
    private long streamCosts;
    private long nonStreamCosts;
    private String source;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getCallTime() {
        return callTime;
    }

    public void setCallTime(long callTime) {
        this.callTime = callTime;
    }
}
