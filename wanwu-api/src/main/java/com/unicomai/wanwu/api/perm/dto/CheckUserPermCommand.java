package com.unicomai.wanwu.api.perm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CheckUserPermCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String genTokenAt;
    private String orgId;
    private List<String> oneOfPerms = new ArrayList<String>();

    public CheckUserPermCommand() {
    }

    public CheckUserPermCommand(String userId, String genTokenAt, String orgId, List<String> oneOfPerms) {
        this.userId = userId;
        this.genTokenAt = genTokenAt;
        this.orgId = orgId;
        this.oneOfPerms = oneOfPerms;
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

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public List<String> getOneOfPerms() {
        return oneOfPerms;
    }

    public void setOneOfPerms(List<String> oneOfPerms) {
        this.oneOfPerms = oneOfPerms;
    }
}
