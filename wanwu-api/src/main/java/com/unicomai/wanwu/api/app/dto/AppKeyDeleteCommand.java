package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppKeyDeleteCommand implements Serializable {

    private String apiId;
    private String userId;
    private String orgId;

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
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
