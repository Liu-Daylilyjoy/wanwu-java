package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApiKeyStatisticOverviewItem implements Serializable {

    private double value;
    private double periodOverPeriod;

    public ApiKeyStatisticOverviewItem() {
    }

    public ApiKeyStatisticOverviewItem(double value, double periodOverPeriod) {
        this.value = value;
        this.periodOverPeriod = periodOverPeriod;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getPeriodOverPeriod() {
        return periodOverPeriod;
    }

    public void setPeriodOverPeriod(double periodOverPeriod) {
        this.periodOverPeriod = periodOverPeriod;
    }
}
