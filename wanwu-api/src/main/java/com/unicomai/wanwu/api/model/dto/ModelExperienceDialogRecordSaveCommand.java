package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;

public class ModelExperienceDialogRecordSaveCommand implements Serializable {

    private String userId;
    private String orgId;
    private String modelExperienceId;
    private String modelId;
    private String sessionId;
    private String originalContent;
    private String handledContent;
    private String reasoningContent;
    private String role;

    public ModelExperienceDialogRecordSaveCommand() {
    }

    public ModelExperienceDialogRecordSaveCommand(String userId, String orgId, String modelExperienceId,
                                                  String modelId, String sessionId, String originalContent,
                                                  String handledContent, String reasoningContent, String role) {
        this.userId = userId;
        this.orgId = orgId;
        this.modelExperienceId = modelExperienceId;
        this.modelId = modelId;
        this.sessionId = sessionId;
        this.originalContent = originalContent;
        this.handledContent = handledContent;
        this.reasoningContent = reasoningContent;
        this.role = role;
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
