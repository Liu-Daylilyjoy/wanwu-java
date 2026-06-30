package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class RagCreateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ragId;

    public RagCreateResult() {
    }

    public RagCreateResult(String ragId) {
        this.ragId = ragId;
    }

    public String getRagId() {
        return ragId;
    }

    public void setRagId(String ragId) {
        this.ragId = ragId;
    }
}
