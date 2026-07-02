package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ApiKeyStatisticRecordItem implements Serializable {

    private String apiKeyId;
    private String methodPath;
    private long callTime;
    private String responseStatus;
    private long streamCosts;
    private long nonStreamCosts;
    private String requestBody;
    private String responseBody;

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

    public long getCallTime() {
        return callTime;
    }

    public void setCallTime(long callTime) {
        this.callTime = callTime;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public long getStreamCosts() {
        return streamCosts;
    }

    public void setStreamCosts(long streamCosts) {
        this.streamCosts = streamCosts;
    }

    public long getNonStreamCosts() {
        return nonStreamCosts;
    }

    public void setNonStreamCosts(long nonStreamCosts) {
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
}
