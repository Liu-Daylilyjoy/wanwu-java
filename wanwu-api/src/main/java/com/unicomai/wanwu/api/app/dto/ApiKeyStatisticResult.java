package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApiKeyStatisticResult implements Serializable {

    private ApiKeyStatisticOverview overview = new ApiKeyStatisticOverview();
    private ApiKeyStatisticTrend trend = new ApiKeyStatisticTrend();

    public ApiKeyStatisticOverview getOverview() {
        return overview;
    }

    public void setOverview(ApiKeyStatisticOverview overview) {
        this.overview = overview;
    }

    public ApiKeyStatisticTrend getTrend() {
        return trend;
    }

    public void setTrend(ApiKeyStatisticTrend trend) {
        this.trend = trend;
    }
}
