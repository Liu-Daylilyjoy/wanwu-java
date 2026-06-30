package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;

public class ModelExperienceDialogListQuery implements Serializable {

    private String userId;
    private String orgId;

    public ModelExperienceDialogListQuery() {
    }

    public ModelExperienceDialogListQuery(String userId, String orgId) {
        this.userId = userId;
        this.orgId = orgId;
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
