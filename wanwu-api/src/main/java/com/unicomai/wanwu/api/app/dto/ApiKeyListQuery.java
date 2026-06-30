package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApiKeyListQuery implements Serializable {

    private int pageNo;
    private int pageSize;
    private String userId;
    private String orgId;

    public ApiKeyListQuery() {
    }

    public ApiKeyListQuery(int pageNo, int pageSize, String userId, String orgId) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.userId = userId;
        this.orgId = orgId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
