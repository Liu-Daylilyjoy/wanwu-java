package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModelListResult implements Serializable {

    private List<ModelInfo> list = new ArrayList<ModelInfo>();
    private long total;

    public ModelListResult() {
    }

    public ModelListResult(List<ModelInfo> list, long total) {
        this.list = list == null ? new ArrayList<ModelInfo>() : list;
        this.total = total;
    }

    public List<ModelInfo> getList() {
        return list;
    }

    public void setList(List<ModelInfo> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
