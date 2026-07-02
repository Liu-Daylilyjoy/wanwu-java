package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApiKeyStatisticTrend implements Serializable {

    private ApiKeyStatisticChart apiCalls = new ApiKeyStatisticChart();

    public ApiKeyStatisticChart getApiCalls() {
        return apiCalls;
    }

    public void setApiCalls(ApiKeyStatisticChart apiCalls) {
        this.apiCalls = apiCalls;
    }
}
