package com.unicomai.wanwu.api.iam.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoginResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uid;
    private String username;
    private String userCategory;
    private String token;
    private long expiresAt;
    private Boolean isUpdatePassword;
    private List<OrganizationOption> orgs = new ArrayList<>();
    private Map<String, Object> orgPermission;
    private Map<String, Object> custom;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserCategory() {
        return userCategory;
    }

    public void setUserCategory(String userCategory) {
        this.userCategory = userCategory;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsUpdatePassword() {
        return isUpdatePassword;
    }

    public void setIsUpdatePassword(Boolean isUpdatePassword) {
        this.isUpdatePassword = isUpdatePassword;
    }

    public List<OrganizationOption> getOrgs() {
        return orgs;
    }

    public void setOrgs(List<OrganizationOption> orgs) {
        this.orgs = orgs;
    }

    public Map<String, Object> getOrgPermission() {
        return orgPermission;
    }

    public void setOrgPermission(Map<String, Object> orgPermission) {
        this.orgPermission = orgPermission;
    }

    public Map<String, Object> getCustom() {
        return custom;
    }

    public void setCustom(Map<String, Object> custom) {
        this.custom = custom;
    }
}
