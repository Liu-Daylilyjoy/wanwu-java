package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssistantConversationPageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Map<String, Object>> list = new ArrayList<>();
    private long total;
    private int pageNo;
    private int pageSize;

    public AssistantConversationPageResult() {
    }

    public AssistantConversationPageResult(List<Map<String, Object>> list, long total, int pageNo, int pageSize) {
        this.list = list;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public List<Map<String, Object>> getList() {
        return list;
    }

    public void setList(List<Map<String, Object>> list) {
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
