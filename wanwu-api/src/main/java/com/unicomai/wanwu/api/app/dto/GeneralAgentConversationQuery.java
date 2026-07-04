package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class GeneralAgentConversationQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String orgId;
    private String threadId;
    private String previewId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getPreviewId() {
        return previewId;
    }

    public void setPreviewId(String previewId) {
        this.previewId = previewId;
    }
}
