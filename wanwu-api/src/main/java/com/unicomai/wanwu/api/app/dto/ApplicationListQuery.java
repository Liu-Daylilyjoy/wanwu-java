package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApplicationListQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String appType;
    private String name;
    private String userId;
    private String orgId;
    private String searchType;

    public ApplicationListQuery() {
    }

    public ApplicationListQuery(String appType, String name) {
        this(appType, name, "", "");
    }

    public ApplicationListQuery(String appType, String name, String userId, String orgId) {
        this.appType = appType;
        this.name = name;
        this.userId = userId;
        this.orgId = orgId;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
}
