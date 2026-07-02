package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppStatisticTrend implements Serializable {

    private StatisticChart callTrend = new StatisticChart();

    public StatisticChart getCallTrend() {
        return callTrend;
    }

    public void setCallTrend(StatisticChart callTrend) {
        this.callTrend = callTrend;
    }
}
