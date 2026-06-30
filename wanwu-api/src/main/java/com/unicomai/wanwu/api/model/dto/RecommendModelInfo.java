package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendModelInfo implements Serializable {

    private String model;
    private String displayName;
    private List<Map<String, Object>> tags = new ArrayList<Map<String, Object>>();
    private String visionSupport;
    private String functionCalling;
    private String thinkingSupport;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Map<String, Object>> getTags() {
        return tags;
    }

    public void setTags(List<Map<String, Object>> tags) {
        this.tags = tags == null ? new ArrayList<Map<String, Object>>() : tags;
    }

    public String getVisionSupport() {
        return visionSupport;
    }

    public void setVisionSupport(String visionSupport) {
        this.visionSupport = visionSupport;
    }

    public String getFunctionCalling() {
        return functionCalling;
    }

    public void setFunctionCalling(String functionCalling) {
        this.functionCalling = functionCalling;
    }

    public String getThinkingSupport() {
        return thinkingSupport;
    }

    public void setThinkingSupport(String thinkingSupport) {
        this.thinkingSupport = thinkingSupport;
    }
}
