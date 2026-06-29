package com.unicomai.wanwu.api.iam.dto;

import java.io.Serializable;

public class OrganizationOption implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    public OrganizationOption() {
    }

    public OrganizationOption(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
