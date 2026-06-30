package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppVersionInfo implements Serializable {

    private String version;
    private String desc;
    private String createdAt;
    private String publishType;

    public AppVersionInfo() {
    }

    public AppVersionInfo(String version, String desc, String createdAt, String publishType) {
        this.version = version;
        this.desc = desc;
        this.createdAt = createdAt;
        this.publishType = publishType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPublishType() {
        return publishType;
    }

    public void setPublishType(String publishType) {
        this.publishType = publishType;
    }
}
