package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class GeneralAgentConversationListQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String orgId;

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
