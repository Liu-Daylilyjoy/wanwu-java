package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.Map;

public class AssistantResourceCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assistantId;
    private String userId;
    private String orgId;
    private String resourceId;
    private String resourceType;
    private String actionName;
    private String desc;
    private Boolean enable;
    private Map<String, Object> toolConfig;

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Map<String, Object> getToolConfig() {
        return toolConfig;
    }

    public void setToolConfig(Map<String, Object> toolConfig) {
        this.toolConfig = toolConfig;
    }
}
