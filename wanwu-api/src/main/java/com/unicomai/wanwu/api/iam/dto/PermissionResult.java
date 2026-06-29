package com.unicomai.wanwu.api.iam.dto;

import java.io.Serializable;
import java.util.Map;

public class PermissionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> orgPermission;
    private Boolean isUpdatePassword;
    private Map<String, Object> avatar;
    private Map<String, Object> language;

    public Map<String, Object> getOrgPermission() {
        return orgPermission;
    }

    public void setOrgPermission(Map<String, Object> orgPermission) {
        this.orgPermission = orgPermission;
    }

    public Boolean getIsUpdatePassword() {
        return isUpdatePassword;
    }

    public void setIsUpdatePassword(Boolean isUpdatePassword) {
        this.isUpdatePassword = isUpdatePassword;
    }

    public Map<String, Object> getAvatar() {
        return avatar;
    }

    public void setAvatar(Map<String, Object> avatar) {
        this.avatar = avatar;
    }

    public Map<String, Object> getLanguage() {
        return language;
    }

    public void setLanguage(Map<String, Object> language) {
        this.language = language;
    }
}
