package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("app_statistics")
public class AppStatisticEntity {

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
    @TableField("app_id")
    private String appId;
    @TableField("app_type")
    private String appType;
    private String date;
    @TableField("call_count")
    private Long callCount;
    @TableField("call_failure")
    private Long callFailure;
    @TableField("stream_count")
    private Long streamCount;
    @TableField("stream_failure")
    private Long streamFailure;
    @TableField("stream_costs")
    private Long streamCosts;
    @TableField("non_stream_count")
    private Long nonStreamCount;
    @TableField("non_stream_failure")
    private Long nonStreamFailure;
    @TableField("non_stream_costs")
    private Long nonStreamCosts;
    @TableField("web_call_count")
    private Long webCallCount;
    @TableField("web_call_failure")
    private Long webCallFailure;
    @TableField("openapi_call_count")
    private Long openapiCallCount;
    @TableField("openapi_call_failure")
    private Long openapiCallFailure;
    @TableField("web_url_call_count")
    private Long webUrlCallCount;
    @TableField("web_url_call_failure")
    private Long webUrlCallFailure;

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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
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

    public Long getStreamFailure() {
        return streamFailure;
    }

    public void setStreamFailure(Long streamFailure) {
        this.streamFailure = streamFailure;
    }

    public Long getStreamCosts() {
        return streamCosts;
    }

    public void setStreamCosts(Long streamCosts) {
        this.streamCosts = streamCosts;
    }

    public Long getNonStreamCount() {
        return nonStreamCount;
    }

    public void setNonStreamCount(Long nonStreamCount) {
        this.nonStreamCount = nonStreamCount;
    }

    public Long getNonStreamFailure() {
        return nonStreamFailure;
    }

    public void setNonStreamFailure(Long nonStreamFailure) {
        this.nonStreamFailure = nonStreamFailure;
    }

    public Long getNonStreamCosts() {
        return nonStreamCosts;
    }

    public void setNonStreamCosts(Long nonStreamCosts) {
        this.nonStreamCosts = nonStreamCosts;
    }

    public Long getWebCallCount() {
        return webCallCount;
    }

    public void setWebCallCount(Long webCallCount) {
        this.webCallCount = webCallCount;
    }

    public Long getWebCallFailure() {
        return webCallFailure;
    }

    public void setWebCallFailure(Long webCallFailure) {
        this.webCallFailure = webCallFailure;
    }

    public Long getOpenapiCallCount() {
        return openapiCallCount;
    }

    public void setOpenapiCallCount(Long openapiCallCount) {
        this.openapiCallCount = openapiCallCount;
    }

    public Long getOpenapiCallFailure() {
        return openapiCallFailure;
    }

    public void setOpenapiCallFailure(Long openapiCallFailure) {
        this.openapiCallFailure = openapiCallFailure;
    }

    public Long getWebUrlCallCount() {
        return webUrlCallCount;
    }

    public void setWebUrlCallCount(Long webUrlCallCount) {
        this.webUrlCallCount = webUrlCallCount;
    }

    public Long getWebUrlCallFailure() {
        return webUrlCallFailure;
    }

    public void setWebUrlCallFailure(Long webUrlCallFailure) {
        this.webUrlCallFailure = webUrlCallFailure;
    }
}
