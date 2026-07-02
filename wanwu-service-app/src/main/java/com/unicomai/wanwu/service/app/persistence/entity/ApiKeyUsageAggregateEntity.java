package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("api_key_statistics")
public class ApiKeyUsageAggregateEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("created_at")
    private Long createdAt;
    @TableField("updated_at")
    private Long updatedAt;
    @TableField("org_id")
    private String orgId;
    @TableField("user_id")
    private String userId;
    @TableField("api_key_id")
    private String apiKeyId;
    @TableField("method_path")
    private String methodPath;
    private String date;
    @TableField("call_count")
    private Long callCount;
    @TableField("call_failure")
    private Long callFailure;
    @TableField("stream_count")
    private Long streamCount;
    @TableField("non_stream_count")
    private Long nonStreamCount;
    @TableField("stream_failure")
    private Long streamFailure;
    @TableField("non_stream_failure")
    private Long nonStreamFailure;
    @TableField("stream_costs")
    private Long streamCosts;
    @TableField("non_stream_costs")
    private Long nonStreamCosts;

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

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApiKeyId() {
        return apiKeyId;
    }

    public void setApiKeyId(String apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    public String getMethodPath() {
        return methodPath;
    }

    public void setMethodPath(String methodPath) {
        this.methodPath = methodPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getCallCount() {
        return callCount;
    }

    public void setCallCount(Long callCount) {
        this.callCount = callCount;
    }

    public Long getCallFailure() {
        return callFailure;
    }

    public void setCallFailure(Long callFailure) {
        this.callFailure = callFailure;
    }

    public Long getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(Long streamCount) {
        this.streamCount = streamCount;
    }

    public Long getNonStreamCount() {
        return nonStreamCount;
    }

    public void setNonStreamCount(Long nonStreamCount) {
        this.nonStreamCount = nonStreamCount;
    }

    public Long getStreamFailure() {
        return streamFailure;
    }

    public void setStreamFailure(Long streamFailure) {
        this.streamFailure = streamFailure;
    }

    public Long getNonStreamFailure() {
        return nonStreamFailure;
    }

    public void setNonStreamFailure(Long nonStreamFailure) {
        this.nonStreamFailure = nonStreamFailure;
    }

    public Long getStreamCosts() {
        return streamCosts;
    }

    public void setStreamCosts(Long streamCosts) {
        this.streamCosts = streamCosts;
    }

    public Long getNonStreamCosts() {
        return nonStreamCosts;
    }

    public void setNonStreamCosts(Long nonStreamCosts) {
        this.nonStreamCosts = nonStreamCosts;
    }
}
