package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;

public class ModelListQuery implements Serializable {

    private String userId;
    private String orgId;
    private String modelType;
    private String provider;
    private String displayName;
    private String filterScope;
    private String scopeType;

    public ModelListQuery() {
    }

    public ModelListQuery(String userId, String orgId, String modelType, String provider, String displayName, String filterScope, String scopeType) {
        this.userId = userId;
        this.orgId = orgId;
        this.modelType = modelType;
        this.provider = provider;
        this.displayName = displayName;
        this.filterScope = filterScope;
        this.scopeType = scopeType;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFilterScope() {
        return filterScope;
    }

    public void setFilterScope(String filterScope) {
        this.filterScope = filterScope;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }
}
