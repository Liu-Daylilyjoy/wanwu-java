package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ModelStatisticResult implements Serializable {

    private ModelStatisticOverview overview = new ModelStatisticOverview();
    private ModelStatisticTrend trend = new ModelStatisticTrend();

    public ModelStatisticOverview getOverview() {
        return overview;
    }

    public void setOverview(ModelStatisticOverview overview) {
        this.overview = overview;
    }

    public ModelStatisticTrend getTrend() {
        return trend;
    }

    public void setTrend(ModelStatisticTrend trend) {
        this.trend = trend;
    }
}
