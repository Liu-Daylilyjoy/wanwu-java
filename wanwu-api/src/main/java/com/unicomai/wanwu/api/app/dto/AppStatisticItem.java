package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppStatisticItem implements Serializable {

    private String appId;
    private String appType;
    private String orgId;
    private long callCount;
    private long callFailure;
    private double failureRate;
    private long streamCount;
    private long nonStreamCount;
    private double avgStreamCosts;
    private double avgNonStreamCosts;

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
}
