package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppStatisticListResult implements Serializable {

    private List<AppStatisticItem> list = new ArrayList<AppStatisticItem>();
    private long total;
    private int pageNo = 1;
    private int pageSize = 10;

    public List<AppStatisticItem> getList() {
        return list;
    }

    public void setList(List<AppStatisticItem> list) {
        this.list = list == null ? new ArrayList<AppStatisticItem>() : new ArrayList<AppStatisticItem>(list);
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
