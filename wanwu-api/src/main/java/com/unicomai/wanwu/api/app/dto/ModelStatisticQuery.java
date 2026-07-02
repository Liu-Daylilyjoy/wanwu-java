package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModelStatisticQuery implements Serializable {

    private String userId;
    private String orgId;
    private String startDate;
    private String endDate;
    private List<String> modelIds = new ArrayList<String>();
    private String modelType;

    public ModelStatisticQuery() {
    }

    public ModelStatisticQuery(String userId,
                               String orgId,
                               String startDate,
                               String endDate,
                               List<String> modelIds,
                               String modelType) {
        this.userId = userId;
        this.orgId = orgId;
        this.startDate = startDate;
        this.endDate = endDate;
        setModelIds(modelIds);
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<String> getModelIds() {
        return modelIds;
    }

    public void setModelIds(List<String> modelIds) {
        this.modelIds = modelIds == null ? new ArrayList<String>() : new ArrayList<String>(modelIds);
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
}
