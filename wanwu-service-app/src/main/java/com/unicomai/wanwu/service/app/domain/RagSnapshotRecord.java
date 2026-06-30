package com.unicomai.wanwu.service.app.domain;

public class RagSnapshotRecord {

    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String userId;
    private String orgId;
    private String ragId;
    private String version;
    private String desc;
    private Integer category;
    private String ragInfoJson;
    private String ragConfigJson;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getRagInfoJson() {
        return ragInfoJson;
    }

    public void setRagInfoJson(String ragInfoJson) {
        this.ragInfoJson = ragInfoJson;
    }

    public String getRagConfigJson() {
        return ragConfigJson;
    }

    public void setRagConfigJson(String ragConfigJson) {
        this.ragConfigJson = ragConfigJson;
    }
}
