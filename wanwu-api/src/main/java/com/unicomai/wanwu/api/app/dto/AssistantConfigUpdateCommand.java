package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class AssistantConfigUpdateCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assistantId;
    private String userId;
    private String orgId;
    private String prologue;
    private String instructions;
    private Map<String, Object> memoryConfig;
    private Map<String, Object> knowledgeBaseConfig;
    private Map<String, Object> modelConfig;
    private Map<String, Object> safetyConfig;
    private Map<String, Object> visionConfig;
    private Map<String, Object> rerankConfig;
    private Map<String, Object> recommendConfig;
    private List<String> recommendQuestion;

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

    public Map<String, Object> getMemoryConfig() {
        return memoryConfig;
    }

    public void setMemoryConfig(Map<String, Object> memoryConfig) {
        this.memoryConfig = memoryConfig;
    }

    public Map<String, Object> getKnowledgeBaseConfig() {
        return knowledgeBaseConfig;
    }

    public void setKnowledgeBaseConfig(Map<String, Object> knowledgeBaseConfig) {
        this.knowledgeBaseConfig = knowledgeBaseConfig;
    }

    public Map<String, Object> getModelConfig() {
        return modelConfig;
    }

    public void setModelConfig(Map<String, Object> modelConfig) {
        this.modelConfig = modelConfig;
    }

    public Map<String, Object> getSafetyConfig() {
        return safetyConfig;
    }

    public void setSafetyConfig(Map<String, Object> safetyConfig) {
        this.safetyConfig = safetyConfig;
    }

    public Map<String, Object> getVisionConfig() {
        return visionConfig;
    }

    public void setVisionConfig(Map<String, Object> visionConfig) {
        this.visionConfig = visionConfig;
    }

    public Map<String, Object> getRerankConfig() {
        return rerankConfig;
    }

    public void setRerankConfig(Map<String, Object> rerankConfig) {
        this.rerankConfig = rerankConfig;
    }

    public Map<String, Object> getRecommendConfig() {
        return recommendConfig;
    }

    public void setRecommendConfig(Map<String, Object> recommendConfig) {
        this.recommendConfig = recommendConfig;
    }

    public List<String> getRecommendQuestion() {
        return recommendQuestion;
    }

    public void setRecommendQuestion(List<String> recommendQuestion) {
        this.recommendQuestion = recommendQuestion;
    }
}
