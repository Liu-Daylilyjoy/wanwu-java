package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ModelStatisticOverview implements Serializable {

    private StatisticOverviewItem callCount = new StatisticOverviewItem();
    private StatisticOverviewItem callFailure = new StatisticOverviewItem();
    private StatisticOverviewItem totalTokens = new StatisticOverviewItem();
    private StatisticOverviewItem promptTokens = new StatisticOverviewItem();
    private StatisticOverviewItem completionTokens = new StatisticOverviewItem();
    private StatisticOverviewItem avgCosts = new StatisticOverviewItem();
    private StatisticOverviewItem avgFirstTokenLatency = new StatisticOverviewItem();

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

    public StatisticOverviewItem getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(StatisticOverviewItem totalTokens) {
        this.totalTokens = totalTokens;
    }

    public StatisticOverviewItem getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(StatisticOverviewItem promptTokens) {
        this.promptTokens = promptTokens;
    }

    public StatisticOverviewItem getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(StatisticOverviewItem completionTokens) {
        this.completionTokens = completionTokens;
    }

    public StatisticOverviewItem getAvgCosts() {
        return avgCosts;
    }

    public void setAvgCosts(StatisticOverviewItem avgCosts) {
        this.avgCosts = avgCosts;
    }

    public StatisticOverviewItem getAvgFirstTokenLatency() {
        return avgFirstTokenLatency;
    }

    public void setAvgFirstTokenLatency(StatisticOverviewItem avgFirstTokenLatency) {
        this.avgFirstTokenLatency = avgFirstTokenLatency;
    }
}
