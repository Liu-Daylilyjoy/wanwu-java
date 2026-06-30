package com.unicomai.wanwu.service.app.domain;

public class AssistantDraftConfigRecord {

    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String userId;
    private String orgId;
    private String assistantId;
    private String prologue;
    private String instructions;
    private String memoryConfigJson;
    private String knowledgeBaseConfigJson;
    private String modelConfigJson;
    private String safetyConfigJson;
    private String visionConfigJson;
    private String rerankConfigJson;
    private String recommendConfigJson;
    private String recommendQuestionsJson;

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

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
    }

    public String getPrologue() {
        return prologue;
    }

    public void setPrologue(String prologue) {
        this.prologue = prologue;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getMemoryConfigJson() {
        return memoryConfigJson;
    }

    public void setMemoryConfigJson(String memoryConfigJson) {
        this.memoryConfigJson = memoryConfigJson;
    }

    public String getKnowledgeBaseConfigJson() {
        return knowledgeBaseConfigJson;
    }

    public void setKnowledgeBaseConfigJson(String knowledgeBaseConfigJson) {
        this.knowledgeBaseConfigJson = knowledgeBaseConfigJson;
    }

    public String getModelConfigJson() {
        return modelConfigJson;
    }

    public void setModelConfigJson(String modelConfigJson) {
        this.modelConfigJson = modelConfigJson;
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

    public String getRerankConfigJson() {
        return rerankConfigJson;
    }

    public void setRerankConfigJson(String rerankConfigJson) {
        this.rerankConfigJson = rerankConfigJson;
    }

    public String getRecommendConfigJson() {
        return recommendConfigJson;
    }

    public void setRecommendConfigJson(String recommendConfigJson) {
        this.recommendConfigJson = recommendConfigJson;
    }

    public String getRecommendQuestionsJson() {
        return recommendQuestionsJson;
    }

    public void setRecommendQuestionsJson(String recommendQuestionsJson) {
        this.recommendQuestionsJson = recommendQuestionsJson;
    }
}
