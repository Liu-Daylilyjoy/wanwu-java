package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.Map;

public class RagConfigUpdateCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ragId;
    private String userId;
    private String orgId;
    private Map<String, Object> modelConfig;
    private Map<String, Object> rerankConfig;
    private Map<String, Object> qaRerankConfig;
    private Map<String, Object> knowledgeBaseConfig;
    private Map<String, Object> qaKnowledgeBaseConfig;
    private Map<String, Object> safetyConfig;
    private Map<String, Object> visionConfig;

    public String getRagId() {
        return ragId;
    }

    public void setRagId(String ragId) {
        this.ragId = ragId;
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

    public Map<String, Object> getModelConfig() {
        return modelConfig;
    }

    public void setModelConfig(Map<String, Object> modelConfig) {
        this.modelConfig = modelConfig;
    }

    public Map<String, Object> getRerankConfig() {
        return rerankConfig;
    }

    public void setRerankConfig(Map<String, Object> rerankConfig) {
        this.rerankConfig = rerankConfig;
    }

    public Map<String, Object> getQaRerankConfig() {
        return qaRerankConfig;
    }

    public void setQaRerankConfig(Map<String, Object> qaRerankConfig) {
        this.qaRerankConfig = qaRerankConfig;
    }

    public Map<String, Object> getKnowledgeBaseConfig() {
        return knowledgeBaseConfig;
    }

    public void setKnowledgeBaseConfig(Map<String, Object> knowledgeBaseConfig) {
        this.knowledgeBaseConfig = knowledgeBaseConfig;
    }

    public Map<String, Object> getQaKnowledgeBaseConfig() {
        return qaKnowledgeBaseConfig;
    }

    public void setQaKnowledgeBaseConfig(Map<String, Object> qaKnowledgeBaseConfig) {
        this.qaKnowledgeBaseConfig = qaKnowledgeBaseConfig;
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
}
