package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApiKeyStatisticQuery implements Serializable {

    private String userId;
    private String orgId;
    private String startDate;
    private String endDate;
    private List<String> apiKeyIds = new ArrayList<String>();
    private List<String> methodPaths = new ArrayList<String>();

    public ApiKeyStatisticQuery() {
    }

    public ApiKeyStatisticQuery(String userId,
                                String orgId,
                                String startDate,
                                String endDate,
                                List<String> apiKeyIds,
                                List<String> methodPaths) {
        this.userId = userId;
        this.orgId = orgId;
        this.startDate = startDate;
        this.endDate = endDate;
        setApiKeyIds(apiKeyIds);
        setMethodPaths(methodPaths);
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

    public List<String> getApiKeyIds() {
        return apiKeyIds;
    }

    public void setApiKeyIds(List<String> apiKeyIds) {
        this.apiKeyIds = apiKeyIds == null ? new ArrayList<String>() : new ArrayList<String>(apiKeyIds);
    }

    public List<String> getMethodPaths() {
        return methodPaths;
    }

    public void setMethodPaths(List<String> methodPaths) {
        this.methodPaths = methodPaths == null ? new ArrayList<String>() : new ArrayList<String>(methodPaths);
    }
}
