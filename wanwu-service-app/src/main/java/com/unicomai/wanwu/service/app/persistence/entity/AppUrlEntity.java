package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("app_urls")
public class AppUrlEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("created_at")
    private Long createdAt;
    @TableField("updated_at")
    private Long updatedAt;
    @TableField("user_id")
    private String userId;
    @TableField("org_id")
    private String orgId;
    @TableField("app_id")
    private String appId;
    @TableField("app_type")
    private String appType;
    private String name;
    private String description;
    @TableField("expired_at")
    private Long expiredAt;
    private String copyright;
    @TableField("copyright_enable")
    private Boolean copyrightEnable;
    @TableField("privacy_policy")
    private String privacyPolicy;
    @TableField("privacy_policy_enable")
    private Boolean privacyPolicyEnable;
    private String disclaimer;
    @TableField("disclaimer_enable")
    private Boolean disclaimerEnable;
    private String suffix;
    private Boolean status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Long expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public Boolean getCopyrightEnable() {
        return copyrightEnable;
    }

    public void setCopyrightEnable(Boolean copyrightEnable) {
        this.copyrightEnable = copyrightEnable;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public Boolean getPrivacyPolicyEnable() {
        return privacyPolicyEnable;
    }

    public void setPrivacyPolicyEnable(Boolean privacyPolicyEnable) {
        this.privacyPolicyEnable = privacyPolicyEnable;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public Boolean getDisclaimerEnable() {
        return disclaimerEnable;
    }

    public void setDisclaimerEnable(Boolean disclaimerEnable) {
        this.disclaimerEnable = disclaimerEnable;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
