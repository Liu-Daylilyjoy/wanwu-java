package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AssistantCreateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assistantId;

    public AssistantCreateResult() {
    }

    public AssistantCreateResult(String assistantId) {
        this.assistantId = assistantId;
    }

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
    }
}
