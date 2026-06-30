package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;

public class ModelExperienceDialogDeleteCommand implements Serializable {

    private String userId;
    private String orgId;
    private String modelExperienceId;

    public ModelExperienceDialogDeleteCommand() {
    }

    public ModelExperienceDialogDeleteCommand(String userId, String orgId, String modelExperienceId) {
        this.userId = userId;
        this.orgId = orgId;
        this.modelExperienceId = modelExperienceId;
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
}
