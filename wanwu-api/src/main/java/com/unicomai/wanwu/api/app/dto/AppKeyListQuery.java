package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppKeyListQuery implements Serializable {

    private String appId;
    private String appType;
    private String userId;
    private String orgId;

    public AppKeyListQuery() {
    }

    public AppKeyListQuery(String appId, String appType, String userId, String orgId) {
        this.appId = appId;
        this.appType = appType;
        this.userId = userId;
        this.orgId = orgId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
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
