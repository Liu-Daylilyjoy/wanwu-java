package com.unicomai.wanwu.common.core.model;

public enum ErrorCode {

    OK("0", "success"),
    BAD_REQUEST("400", "bad request"),
    UNAUTHORIZED("401", "unauthorized"),
    FORBIDDEN("403", "forbidden"),
    NOT_FOUND("404", "not found"),
    CONFLICT("409", "conflict"),
    INTERNAL_ERROR("500", "internal error"),
    SERVICE_UNAVAILABLE("503", "service unavailable");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
