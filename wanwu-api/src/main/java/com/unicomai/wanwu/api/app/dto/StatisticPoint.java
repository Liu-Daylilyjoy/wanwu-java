package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class StatisticPoint implements Serializable {

    private String key;
    private double value;

    public StatisticPoint() {
    }

    public StatisticPoint(String key, double value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
