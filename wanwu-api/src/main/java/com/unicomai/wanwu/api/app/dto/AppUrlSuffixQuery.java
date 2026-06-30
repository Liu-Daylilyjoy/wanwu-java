package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AppUrlSuffixQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String suffix;

    public AppUrlSuffixQuery() {
    }

    public AppUrlSuffixQuery(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
