package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;

public class ProviderListQuery implements Serializable {

    private String provider;
    private String modelType;

    public ProviderListQuery() {
    }

    public ProviderListQuery(String provider, String modelType) {
        this.provider = provider;
        this.modelType = modelType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
}
