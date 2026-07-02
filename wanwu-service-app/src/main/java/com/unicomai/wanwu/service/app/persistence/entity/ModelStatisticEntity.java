package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("model_statistics")
public class ModelStatisticEntity {

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
    @TableField("model_id")
    private String modelId;
    private String model;
    private String provider;
    @TableField("model_type")
    private String modelType;
    private String date;
    @TableField("prompt_tokens")
    private Long promptTokens;
    @TableField("completion_tokens")
    private Long completionTokens;
    @TableField("total_tokens")
    private Long totalTokens;
    @TableField("first_token_latency")
    private Long firstTokenLatency;
    private Long costs;
    @TableField("call_count")
    private Long callCount;
    @TableField("stream_count")
    private Long streamCount;
    @TableField("non_stream_count")
    private Long nonStreamCount;
    @TableField("call_failure")
    private Long callFailure;
    @TableField("stream_failure")
    private Long streamFailure;
    @TableField("non_stream_failure")
    private Long nonStreamFailure;

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

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Long promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Long getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Long completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Long getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Long totalTokens) {
        this.totalTokens = totalTokens;
    }

    public Long getFirstTokenLatency() {
        return firstTokenLatency;
    }

    public void setFirstTokenLatency(Long firstTokenLatency) {
        this.firstTokenLatency = firstTokenLatency;
    }

    public Long getCosts() {
        return costs;
    }

    public void setCosts(Long costs) {
        this.costs = costs;
    }

    public Long getCallCount() {
        return callCount;
    }

    public void setCallCount(Long callCount) {
        this.callCount = callCount;
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

    public Long getCallFailure() {
        return callFailure;
    }

    public void setCallFailure(Long callFailure) {
        this.callFailure = callFailure;
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
}
