package com.unicomai.wanwu.common.core.model;

import java.io.Serializable;

public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String code;
    private String message;
    private T data;
    private String traceId;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<T>();
        response.setSuccess(true);
        response.setCode(ErrorCode.OK.getCode());
        response.setMessage(ErrorCode.OK.getMessage());
        response.setData(data);
        response.setTraceId(TraceIds.current());
        return response;
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message) {
        ApiResponse<T> response = new ApiResponse<T>();
        response.setSuccess(false);
        response.setCode(errorCode.getCode());
        response.setMessage(message == null || message.trim().isEmpty() ? errorCode.getMessage() : message);
        response.setTraceId(TraceIds.current());
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
