package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;

public class ModelExperienceDialogRecordQuery implements Serializable {

    private String userId;
    private String orgId;
    private String modelExperienceId;
    private String sessionId;

    public ModelExperienceDialogRecordQuery() {
    }

    public ModelExperienceDialogRecordQuery(String userId, String orgId, String modelExperienceId, String sessionId) {
        this.userId = userId;
        this.orgId = orgId;
        this.modelExperienceId = modelExperienceId;
        this.sessionId = sessionId;
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

    public String getModelExperienceId() {
        return modelExperienceId;
    }

    public void setModelExperienceId(String modelExperienceId) {
        this.modelExperienceId = modelExperienceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
