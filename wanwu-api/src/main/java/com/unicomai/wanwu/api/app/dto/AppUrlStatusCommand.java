package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppUrlStatusCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String urlId;
    private boolean status;
    private String userId;
    private String orgId;

    public String getUrlId() {
        return urlId;
    }

    public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
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
