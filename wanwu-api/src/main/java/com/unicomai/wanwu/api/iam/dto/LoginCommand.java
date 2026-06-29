package com.unicomai.wanwu.api.iam.dto;

import java.io.Serializable;

public class LoginCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String key;
    private String code;

    public LoginCommand() {
    }

    public LoginCommand(String username, String password, String key, String code) {
        this.username = username;
        this.password = password;
        this.key = key;
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
