package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppTypeConvertCommand implements Serializable {

    private String appId;
    private String oldAppType;
    private String newAppType;
    private String userId;
    private String orgId;

    public AppTypeConvertCommand() {
    }

    public AppTypeConvertCommand(String appId, String oldAppType, String newAppType, String userId, String orgId) {
        this.appId = appId;
        this.oldAppType = oldAppType;
        this.newAppType = newAppType;
        this.userId = userId;
        this.orgId = orgId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getOldAppType() {
        return oldAppType;
    }

    public void setOldAppType(String oldAppType) {
        this.oldAppType = oldAppType;
    }

    public String getNewAppType() {
        return newAppType;
    }

    public void setNewAppType(String newAppType) {
        this.newAppType = newAppType;
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
