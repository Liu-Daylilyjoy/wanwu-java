package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RagChatCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ragId;
    private String question;
    private boolean draft;
    private List<Map<String, Object>> history = new ArrayList<>();
    private List<Map<String, Object>> fileInfo = new ArrayList<>();
    private String userId;
    private String orgId;
    private String overrideResponse;

    public String getRagId() {
        return ragId;
    }

    public void setRagId(String ragId) {
        this.ragId = ragId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public List<Map<String, Object>> getHistory() {
        return history;
    }

    public void setHistory(List<Map<String, Object>> history) {
        this.history = history;
    }

    public List<Map<String, Object>> getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(List<Map<String, Object>> fileInfo) {
        this.fileInfo = fileInfo;
    }

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

    public String getOverrideResponse() {
        return overrideResponse;
    }

    public void setOverrideResponse(String overrideResponse) {
        this.overrideResponse = overrideResponse;
    }
}
