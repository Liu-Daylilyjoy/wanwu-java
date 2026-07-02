package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatisticChart implements Serializable {

    private String tableName;
    private List<StatisticLine> lines = new ArrayList<StatisticLine>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<StatisticLine> getLines() {
        return lines;
    }

    public void setLines(List<StatisticLine> lines) {
        this.lines = lines == null ? new ArrayList<StatisticLine>() : new ArrayList<StatisticLine>(lines);
    }
}
