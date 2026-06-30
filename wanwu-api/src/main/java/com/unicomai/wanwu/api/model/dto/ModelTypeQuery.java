package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;

public class ModelTypeQuery implements Serializable {

    private String userId;
    private String orgId;
    private String modelType;

    public ModelTypeQuery() {
    }

    public ModelTypeQuery(String userId, String orgId, String modelType) {
        this.userId = userId;
        this.orgId = orgId;
        this.modelType = modelType;
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

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
}
