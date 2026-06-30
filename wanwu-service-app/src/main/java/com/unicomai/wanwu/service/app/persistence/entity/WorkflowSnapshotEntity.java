package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("workflow_snapshots")
public class WorkflowSnapshotEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String userId;
    private String orgId;
    private String workflowId;
    private String version;
    @TableField("snapshot_desc")
    private String desc;
    private Integer category;
    @TableField("workflow_info_json")
    private String workflowInfoJson;
    @TableField("workflow_schema_json")
    private String workflowSchemaJson;

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

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
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

    public String getWorkflowInfoJson() {
        return workflowInfoJson;
    }

    public void setWorkflowInfoJson(String workflowInfoJson) {
        this.workflowInfoJson = workflowInfoJson;
    }

    public String getWorkflowSchemaJson() {
        return workflowSchemaJson;
    }

    public void setWorkflowSchemaJson(String workflowSchemaJson) {
        this.workflowSchemaJson = workflowSchemaJson;
    }
}
