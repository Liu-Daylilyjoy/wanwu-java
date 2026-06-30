package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ChatflowApplicationInfoQuery implements Serializable {

    private String intelligenceId;
    private Long intelligenceType;
    private String userId;
    private String orgId;

    public String getIntelligenceId() {
        return intelligenceId;
    }

    public void setIntelligenceId(String intelligenceId) {
        this.intelligenceId = intelligenceId;
    }

    public Long getIntelligenceType() {
        return intelligenceType;
    }

    public void setIntelligenceType(Long intelligenceType) {
        this.intelligenceType = intelligenceType;
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
