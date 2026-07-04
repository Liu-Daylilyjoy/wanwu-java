package com.unicomai.wanwu.service.app.domain;

public class AppTemplateRecord {

    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String templateType;
    private String templateId;
    private String category;
    private String name;
    private String desc;
    private String avatarJson;
    private String author;
    private Integer downloadCount;
    private String summary;
    private String feature;
    private String scenario;
    private String note;
    private String prologue;
    private String instructions;
    private String recommendQuestionsJson;
    private String workflowInstruction;
    private String schemaJson;

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

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAvatarJson() {
        return avatarJson;
    }

    public void setAvatarJson(String avatarJson) {
        this.avatarJson = avatarJson;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public String getRecommendQuestionsJson() {
        return recommendQuestionsJson;
    }

    public void setRecommendQuestionsJson(String recommendQuestionsJson) {
        this.recommendQuestionsJson = recommendQuestionsJson;
    }

    public String getWorkflowInstruction() {
        return workflowInstruction;
    }

    public void setWorkflowInstruction(String workflowInstruction) {
        this.workflowInstruction = workflowInstruction;
    }

    public String getSchemaJson() {
        return schemaJson;
    }

    public void setSchemaJson(String schemaJson) {
        this.schemaJson = schemaJson;
    }
}
