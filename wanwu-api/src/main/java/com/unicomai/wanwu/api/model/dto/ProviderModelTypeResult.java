package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProviderModelTypeResult implements Serializable {

    private List<ProviderModelTypeInfo> list = new ArrayList<ProviderModelTypeInfo>();
    private long total;

    public ProviderModelTypeResult() {
    }

    public ProviderModelTypeResult(List<ProviderModelTypeInfo> list, long total) {
        this.list = list == null ? new ArrayList<ProviderModelTypeInfo>() : list;
        this.total = total;
    }

    public List<ProviderModelTypeInfo> getList() {
        return list;
    }

    public void setList(List<ProviderModelTypeInfo> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
