package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApiKeyStatisticRecordResult implements Serializable {

    private List<ApiKeyStatisticRecordItem> list = new ArrayList<ApiKeyStatisticRecordItem>();
    private long total;
    private int pageNo;
    private int pageSize;

    public List<ApiKeyStatisticRecordItem> getList() {
        return list;
    }

    public void setList(List<ApiKeyStatisticRecordItem> list) {
        this.list = list == null ? new ArrayList<ApiKeyStatisticRecordItem>() : new ArrayList<ApiKeyStatisticRecordItem>(list);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
