package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppUrlInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String urlId;
    private String appId;
    private String appType;
    private String name;
    private String createdAt;
    private String expiredAt;
    private String copyright;
    private boolean copyrightEnable;
    private String privacyPolicy;
    private boolean privacyPolicyEnable;
    private String disclaimer;
    private boolean disclaimerEnable;
    private String suffix;
    private boolean status;
    private String userId;
    private String orgId;
    private String description;

    public String getUrlId() {
        return urlId;
    }

    public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(String expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public boolean isCopyrightEnable() {
        return copyrightEnable;
    }

    public void setCopyrightEnable(boolean copyrightEnable) {
        this.copyrightEnable = copyrightEnable;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public boolean isPrivacyPolicyEnable() {
        return privacyPolicyEnable;
    }

    public void setPrivacyPolicyEnable(boolean privacyPolicyEnable) {
        this.privacyPolicyEnable = privacyPolicyEnable;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public boolean isDisclaimerEnable() {
        return disclaimerEnable;
    }

    public void setDisclaimerEnable(boolean disclaimerEnable) {
        this.disclaimerEnable = disclaimerEnable;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
