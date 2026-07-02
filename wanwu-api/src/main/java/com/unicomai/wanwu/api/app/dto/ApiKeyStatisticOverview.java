package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApiKeyStatisticOverview implements Serializable {

    private ApiKeyStatisticOverviewItem callCount = new ApiKeyStatisticOverviewItem();
    private ApiKeyStatisticOverviewItem callFailure = new ApiKeyStatisticOverviewItem();
    private ApiKeyStatisticOverviewItem avgStreamCosts = new ApiKeyStatisticOverviewItem();
    private ApiKeyStatisticOverviewItem avgNonStreamCosts = new ApiKeyStatisticOverviewItem();
    private ApiKeyStatisticOverviewItem streamCount = new ApiKeyStatisticOverviewItem();
    private ApiKeyStatisticOverviewItem nonStreamCount = new ApiKeyStatisticOverviewItem();

    public ApiKeyStatisticOverviewItem getCallCount() {
        return callCount;
    }

    public void setCallCount(ApiKeyStatisticOverviewItem callCount) {
        this.callCount = callCount;
    }

    public ApiKeyStatisticOverviewItem getCallFailure() {
        return callFailure;
    }

    public void setCallFailure(ApiKeyStatisticOverviewItem callFailure) {
        this.callFailure = callFailure;
    }

    public ApiKeyStatisticOverviewItem getAvgStreamCosts() {
        return avgStreamCosts;
    }

    public void setAvgStreamCosts(ApiKeyStatisticOverviewItem avgStreamCosts) {
        this.avgStreamCosts = avgStreamCosts;
    }

    public ApiKeyStatisticOverviewItem getAvgNonStreamCosts() {
        return avgNonStreamCosts;
    }

    public void setAvgNonStreamCosts(ApiKeyStatisticOverviewItem avgNonStreamCosts) {
        this.avgNonStreamCosts = avgNonStreamCosts;
    }

    public ApiKeyStatisticOverviewItem getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(ApiKeyStatisticOverviewItem streamCount) {
        this.streamCount = streamCount;
    }

    public ApiKeyStatisticOverviewItem getNonStreamCount() {
        return nonStreamCount;
    }

    public void setNonStreamCount(ApiKeyStatisticOverviewItem nonStreamCount) {
        this.nonStreamCount = nonStreamCount;
    }
}
