package com.unicomai.wanwu.api.iam.dto;

import java.io.Serializable;

public class CaptchaResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
    private String b64;

    public CaptchaResult() {
    }

    public CaptchaResult(String key, String b64) {
        this.key = key;
        this.b64 = b64;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getB64() {
        return b64;
    }

    public void setB64(String b64) {
        this.b64 = b64;
    }
}
