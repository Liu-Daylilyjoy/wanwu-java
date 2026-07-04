package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AssistantKnowledgeFileUploadCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String orgId;
    private String assistantId;
    private List<AssistantKnowledgeFileUploadItem> files = new ArrayList<AssistantKnowledgeFileUploadItem>();

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

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
    }

    public List<AssistantKnowledgeFileUploadItem> getFiles() {
        return files;
    }

    public void setFiles(List<AssistantKnowledgeFileUploadItem> files) {
        this.files = files;
    }
}
