package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApiKeyStatisticChart implements Serializable {

    private String tableName;
    private List<ApiKeyStatisticLine> lines = new ArrayList<ApiKeyStatisticLine>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ApiKeyStatisticLine> getLines() {
        return lines;
    }

    public void setLines(List<ApiKeyStatisticLine> lines) {
        this.lines = lines == null ? new ArrayList<ApiKeyStatisticLine>() : new ArrayList<ApiKeyStatisticLine>(lines);
    }
}
