package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppStatisticOverview implements Serializable {

    private StatisticOverviewItem callCount = new StatisticOverviewItem();
    private StatisticOverviewItem callFailure = new StatisticOverviewItem();
    private StatisticOverviewItem streamCount = new StatisticOverviewItem();
    private StatisticOverviewItem nonStreamCount = new StatisticOverviewItem();
    private StatisticOverviewItem avgStreamCosts = new StatisticOverviewItem();
    private StatisticOverviewItem avgNonStreamCosts = new StatisticOverviewItem();

    public StatisticOverviewItem getCallCount() {
        return callCount;
    }

    public void setCallCount(StatisticOverviewItem callCount) {
        this.callCount = callCount;
    }

    public StatisticOverviewItem getCallFailure() {
        return callFailure;
    }

    public void setCallFailure(StatisticOverviewItem callFailure) {
        this.callFailure = callFailure;
    }

    public StatisticOverviewItem getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(StatisticOverviewItem streamCount) {
        this.streamCount = streamCount;
    }

    public StatisticOverviewItem getNonStreamCount() {
        return nonStreamCount;
    }

    public void setNonStreamCount(StatisticOverviewItem nonStreamCount) {
        this.nonStreamCount = nonStreamCount;
    }

    public StatisticOverviewItem getAvgStreamCosts() {
        return avgStreamCosts;
    }

    public void setAvgStreamCosts(StatisticOverviewItem avgStreamCosts) {
        this.avgStreamCosts = avgStreamCosts;
    }

    public StatisticOverviewItem getAvgNonStreamCosts() {
        return avgNonStreamCosts;
    }

    public void setAvgNonStreamCosts(StatisticOverviewItem avgNonStreamCosts) {
        this.avgNonStreamCosts = avgNonStreamCosts;
    }
}
