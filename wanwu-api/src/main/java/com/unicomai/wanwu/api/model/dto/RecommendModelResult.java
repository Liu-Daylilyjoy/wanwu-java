package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecommendModelResult implements Serializable {

    private List<RecommendModelInfo> list = new ArrayList<RecommendModelInfo>();
    private long total;

    public RecommendModelResult() {
    }

    public RecommendModelResult(List<RecommendModelInfo> list, long total) {
        this.list = list == null ? new ArrayList<RecommendModelInfo>() : list;
        this.total = total;
    }

    public List<RecommendModelInfo> getList() {
        return list;
    }

    public void setList(List<RecommendModelInfo> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
