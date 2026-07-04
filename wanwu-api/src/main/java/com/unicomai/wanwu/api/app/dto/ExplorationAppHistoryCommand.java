package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ExplorationAppHistoryCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String orgId;
    private String appId;
    private String appType;

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
}
