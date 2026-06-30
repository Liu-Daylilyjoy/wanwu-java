package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;

public class ModelStatusCommand implements Serializable {

    private String userId;
    private String orgId;
    private String modelId;
    private Boolean isActive;

    public ModelStatusCommand() {
    }

    public ModelStatusCommand(String userId, String orgId, String modelId, Boolean active) {
        this.userId = userId;
        this.orgId = orgId;
        this.modelId = modelId;
        isActive = active;
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

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
