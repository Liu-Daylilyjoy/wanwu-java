package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("rag_draft_configs")
public class RagDraftConfigEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String userId;
    private String orgId;
    private String ragId;
    @TableField("model_config")
    private String modelConfigJson;
    @TableField("rerank_config")
    private String rerankConfigJson;
    @TableField("qa_rerank_config")
    private String qaRerankConfigJson;
    @TableField("knowledge_base_config")
    private String knowledgeBaseConfigJson;
    @TableField("qa_knowledge_base_config")
    private String qaKnowledgeBaseConfigJson;
    @TableField("safety_config")
    private String safetyConfigJson;
    @TableField("vision_config")
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
