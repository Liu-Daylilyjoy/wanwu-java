package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApiKeyDeleteCommand implements Serializable {

    private String keyId;
    private String userId;
    private String orgId;

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
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
