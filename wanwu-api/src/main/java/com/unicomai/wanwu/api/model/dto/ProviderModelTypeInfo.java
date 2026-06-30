package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProviderModelTypeInfo implements Serializable {

    private String key;
    private String name;
    private List<ModelTypeInfo> children = new ArrayList<ModelTypeInfo>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ModelTypeInfo> getChildren() {
        return children;
    }

    public void setChildren(List<ModelTypeInfo> children) {
        this.children = children == null ? new ArrayList<ModelTypeInfo>() : children;
    }
}
