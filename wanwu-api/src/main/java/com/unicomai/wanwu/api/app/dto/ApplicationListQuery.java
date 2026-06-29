package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApplicationListQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String appType;
    private String name;

    public ApplicationListQuery() {
    }

    public ApplicationListQuery(String appType, String name) {
        this.appType = appType;
        this.name = name;
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
}
