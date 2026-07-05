package com.unicomai.wanwu.api.perm.dto;

import java.io.Serializable;

public class CheckUserEnableResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String language;
    private Long lastUpdatePasswordAt;

    public CheckUserEnableResult() {
    }

    public CheckUserEnableResult(String language, Long lastUpdatePasswordAt) {
        this.language = language;
        this.lastUpdatePasswordAt = lastUpdatePasswordAt;
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
