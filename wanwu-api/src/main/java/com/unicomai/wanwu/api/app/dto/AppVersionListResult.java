package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppVersionListResult implements Serializable {

    private List<AppVersionInfo> list = new ArrayList<>();
    private int total;

    public AppVersionListResult() {
    }

    public AppVersionListResult(List<AppVersionInfo> list, int total) {
        this.list = list == null ? new ArrayList<AppVersionInfo>() : list;
        this.total = total;
    }

    public List<AppVersionInfo> getList() {
        return list;
    }

    public void setList(List<AppVersionInfo> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
