package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AssistantActionInfoQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String orgId;
    private String actionId;

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

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
}
