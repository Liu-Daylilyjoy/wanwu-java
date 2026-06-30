package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApiKeyPageResult implements Serializable {

    private List<ApiKeyInfo> list = new ArrayList<>();
    private long total;
    private int pageNo;
    private int pageSize;

    public ApiKeyPageResult() {
    }

    public ApiKeyPageResult(List<ApiKeyInfo> list, long total, int pageNo, int pageSize) {
        this.list = list == null ? new ArrayList<ApiKeyInfo>() : list;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public List<ApiKeyInfo> getList() {
        return list;
    }

    public void setList(List<ApiKeyInfo> list) {
        this.list = list;
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
