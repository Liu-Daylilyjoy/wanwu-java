package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModelInvokeCommand implements Serializable {

    private String userId;
    private String orgId;
    private String modelId;
    private String operation;
    private int timeoutMillis;
    private Map<String, Object> payload = new LinkedHashMap<String, Object>();

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

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload == null ? new LinkedHashMap<String, Object>() : payload;
    }
}
