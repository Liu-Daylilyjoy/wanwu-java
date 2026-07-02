package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("api_key_records")
public class ApiKeyUsageRecordEntity {

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
    @TableField("call_time")
    private Long callTime;
    @TableField("response_status")
    private String responseStatus;
    @TableField("is_stream")
    private Boolean stream;
    @TableField("stream_costs")
    private Long streamCosts;
    @TableField("non_stream_costs")
    private Long nonStreamCosts;
    @TableField("request_body")
    private String requestBody;
    @TableField("response_body")
    private String responseBody;
    private String date;

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

    public Long getCallTime() {
        return callTime;
    }

    public void setCallTime(Long callTime) {
        this.callTime = callTime;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
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

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
