package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatisticLine implements Serializable {

    private String lineName;
    private List<StatisticPoint> items = new ArrayList<StatisticPoint>();

    public StatisticLine() {
    }

    public StatisticLine(String lineName, List<StatisticPoint> items) {
        this.lineName = lineName;
        setItems(items);
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public List<StatisticPoint> getItems() {
        return items;
    }

    public void setItems(List<StatisticPoint> items) {
        this.items = items == null ? new ArrayList<StatisticPoint>() : new ArrayList<StatisticPoint>(items);
    }
}
