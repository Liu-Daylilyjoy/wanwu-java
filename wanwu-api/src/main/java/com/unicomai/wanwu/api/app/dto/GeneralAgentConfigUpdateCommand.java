package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneralAgentConfigUpdateCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String orgId;
    private Map<String, List<Map<String, Object>>> config =
            new LinkedHashMap<String, List<Map<String, Object>>>();

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

    public Map<String, List<Map<String, Object>>> getConfig() {
        return config;
    }

    public void setConfig(Map<String, List<Map<String, Object>>> config) {
        this.config = config;
    }
}
