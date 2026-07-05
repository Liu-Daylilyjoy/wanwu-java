package com.unicomai.wanwu.api.perm.dto;

import java.io.Serializable;

public class CheckUserEnableCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String genTokenAt;

    public CheckUserEnableCommand() {
    }

    public CheckUserEnableCommand(String userId, String genTokenAt) {
        this.userId = userId;
        this.genTokenAt = genTokenAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGenTokenAt() {
        return genTokenAt;
    }

    public void setGenTokenAt(String genTokenAt) {
        this.genTokenAt = genTokenAt;
    }
}
