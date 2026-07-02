package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppStatisticResult implements Serializable {

    private AppStatisticOverview overview = new AppStatisticOverview();
    private AppStatisticTrend trend = new AppStatisticTrend();

    public AppStatisticOverview getOverview() {
        return overview;
    }

    public void setOverview(AppStatisticOverview overview) {
        this.overview = overview;
    }

    public AppStatisticTrend getTrend() {
        return trend;
    }

    public void setTrend(AppStatisticTrend trend) {
        this.trend = trend;
    }
}
