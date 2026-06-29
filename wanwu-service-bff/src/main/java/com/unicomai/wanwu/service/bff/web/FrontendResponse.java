package com.unicomai.wanwu.service.bff.web;

public class FrontendResponse<T> {

    private int code;
    private String msg;
    private T data;

    public FrontendResponse() {
    }

    private FrontendResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> FrontendResponse<T> ok(T data) {
        return new FrontendResponse<>(0, "success", data);
    }

    public static <T> FrontendResponse<T> failure(int code, String msg) {
        return new FrontendResponse<>(code, msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
