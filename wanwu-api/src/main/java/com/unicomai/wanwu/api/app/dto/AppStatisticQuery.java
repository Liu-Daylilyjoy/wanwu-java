package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppStatisticQuery implements Serializable {

    private String userId;
    private String orgId;
    private String startDate;
    private String endDate;
    private List<String> appIds = new ArrayList<String>();
    private String appType;

    public AppStatisticQuery() {
    }

    public AppStatisticQuery(String userId,
                             String orgId,
                             String startDate,
                             String endDate,
                             List<String> appIds,
                             String appType) {
        this.userId = userId;
        this.orgId = orgId;
        this.startDate = startDate;
        this.endDate = endDate;
        setAppIds(appIds);
        this.appType = appType;
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

    public List<String> getAppIds() {
        return appIds;
    }

    public void setAppIds(List<String> appIds) {
        this.appIds = appIds == null ? new ArrayList<String>() : new ArrayList<String>(appIds);
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }
}
