package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("assistant_draft_configs")
public class AssistantDraftConfigEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String userId;
    private String orgId;
    private String assistantId;
    private String prologue;
    private String instructions;
    @TableField("memory_config")
    private String memoryConfigJson;
    @TableField("knowledge_base_config")
    private String knowledgeBaseConfigJson;
    @TableField("model_config")
    private String modelConfigJson;
    @TableField("safety_config")
    private String safetyConfigJson;
    @TableField("vision_config")
    private String visionConfigJson;
    @TableField("rerank_config")
    private String rerankConfigJson;
    @TableField("recommend_config")
    private String recommendConfigJson;
    @TableField("recommend_questions")
    private String recommendQuestionsJson;
    @TableField("workflow_infos")
    private String workflowInfosJson;
    @TableField("mcp_infos")
    private String mcpInfosJson;
    @TableField("tool_infos")
    private String toolInfosJson;
    @TableField("skill_infos")
    private String skillInfosJson;
    @TableField("multi_agent_infos")
    private String multiAgentInfosJson;

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

    public String getWorkflowInfosJson() {
        return workflowInfosJson;
    }

    public void setWorkflowInfosJson(String workflowInfosJson) {
        this.workflowInfosJson = workflowInfosJson;
    }

    public String getMcpInfosJson() {
        return mcpInfosJson;
    }

    public void setMcpInfosJson(String mcpInfosJson) {
        this.mcpInfosJson = mcpInfosJson;
    }

    public String getToolInfosJson() {
        return toolInfosJson;
    }

    public void setToolInfosJson(String toolInfosJson) {
        this.toolInfosJson = toolInfosJson;
    }

    public String getSkillInfosJson() {
        return skillInfosJson;
    }

    public void setSkillInfosJson(String skillInfosJson) {
        this.skillInfosJson = skillInfosJson;
    }

    public String getMultiAgentInfosJson() {
        return multiAgentInfosJson;
    }

    public void setMultiAgentInfosJson(String multiAgentInfosJson) {
        this.multiAgentInfosJson = multiAgentInfosJson;
    }
}
