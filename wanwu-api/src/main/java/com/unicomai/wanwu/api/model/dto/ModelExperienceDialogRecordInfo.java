package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;

public class ModelExperienceDialogRecordInfo implements Serializable {

    private String modelExperienceId;
    private String modelId;
    private String sessionId;
    private String originalContent;
    private String handledContent;
    private String reasoningContent;
    private String role;

    public String getModelExperienceId() {
        return modelExperienceId;
    }

    public void setModelExperienceId(String modelExperienceId) {
        this.modelExperienceId = modelExperienceId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getHandledContent() {
        return handledContent;
    }

    public void setHandledContent(String handledContent) {
        this.handledContent = handledContent;
    }

    public String getReasoningContent() {
        return reasoningContent;
    }

    public void setReasoningContent(String reasoningContent) {
        this.reasoningContent = reasoningContent;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
