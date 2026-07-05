package com.unicomai.wanwu.api.perm.dto;

import java.io.Serializable;

public class CheckUserPermResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isAdmin;
    private Boolean isSystem;
    private String language;
    private Long lastUpdatePasswordAt;

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getLastUpdatePasswordAt() {
        return lastUpdatePasswordAt;
    }

    public void setLastUpdatePasswordAt(Long lastUpdatePasswordAt) {
        this.lastUpdatePasswordAt = lastUpdatePasswordAt;
    }
}
