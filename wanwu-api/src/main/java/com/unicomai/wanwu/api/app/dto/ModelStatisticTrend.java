package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ModelStatisticTrend implements Serializable {

    private StatisticChart modelCalls = new StatisticChart();
    private StatisticChart tokensUsage = new StatisticChart();

    public StatisticChart getModelCalls() {
        return modelCalls;
    }

    public void setModelCalls(StatisticChart modelCalls) {
        this.modelCalls = modelCalls;
    }

    public StatisticChart getTokensUsage() {
        return tokensUsage;
    }

    public void setTokensUsage(StatisticChart tokensUsage) {
        this.tokensUsage = tokensUsage;
    }
}
