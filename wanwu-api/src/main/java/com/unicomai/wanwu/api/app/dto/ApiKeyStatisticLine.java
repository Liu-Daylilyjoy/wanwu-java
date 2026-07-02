package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApiKeyStatisticLine implements Serializable {

    private String lineName;
    private List<ApiKeyStatisticPoint> items = new ArrayList<ApiKeyStatisticPoint>();

    public ApiKeyStatisticLine() {
    }

    public ApiKeyStatisticLine(String lineName, List<ApiKeyStatisticPoint> items) {
        this.lineName = lineName;
        setItems(items);
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public List<ApiKeyStatisticPoint> getItems() {
        return items;
    }

    public void setItems(List<ApiKeyStatisticPoint> items) {
        this.items = items == null ? new ArrayList<ApiKeyStatisticPoint>() : new ArrayList<ApiKeyStatisticPoint>(items);
    }
}
