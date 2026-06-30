package com.unicomai.wanwu.service.app.domain;

public class RagDraftConfigRecord {

    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String userId;
    private String orgId;
    private String ragId;
    private String modelConfigJson;
    private String rerankConfigJson;
    private String qaRerankConfigJson;
    private String knowledgeBaseConfigJson;
    private String qaKnowledgeBaseConfigJson;
    private String safetyConfigJson;
    private String visionConfigJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
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

    public String getRagId() {
        return ragId;
    }

    public void setRagId(String ragId) {
        this.ragId = ragId;
    }

    public String getModelConfigJson() {
        return modelConfigJson;
    }

    public void setModelConfigJson(String modelConfigJson) {
        this.modelConfigJson = modelConfigJson;
    }

    public String getRerankConfigJson() {
        return rerankConfigJson;
    }

    public void setRerankConfigJson(String rerankConfigJson) {
        this.rerankConfigJson = rerankConfigJson;
    }

    public String getQaRerankConfigJson() {
        return qaRerankConfigJson;
    }

    public void setQaRerankConfigJson(String qaRerankConfigJson) {
        this.qaRerankConfigJson = qaRerankConfigJson;
    }

    public String getKnowledgeBaseConfigJson() {
        return knowledgeBaseConfigJson;
    }

    public void setKnowledgeBaseConfigJson(String knowledgeBaseConfigJson) {
        this.knowledgeBaseConfigJson = knowledgeBaseConfigJson;
    }

    public String getQaKnowledgeBaseConfigJson() {
        return qaKnowledgeBaseConfigJson;
    }

    public void setQaKnowledgeBaseConfigJson(String qaKnowledgeBaseConfigJson) {
        this.qaKnowledgeBaseConfigJson = qaKnowledgeBaseConfigJson;
    }

    public String getSafetyConfigJson() {
        return safetyConfigJson;
    }

    public void setSafetyConfigJson(String safetyConfigJson) {
        this.safetyConfigJson = safetyConfigJson;
    }

    public String getVisionConfigJson() {
        return visionConfigJson;
    }

    public void setVisionConfigJson(String visionConfigJson) {
        this.visionConfigJson = visionConfigJson;
    }
}
