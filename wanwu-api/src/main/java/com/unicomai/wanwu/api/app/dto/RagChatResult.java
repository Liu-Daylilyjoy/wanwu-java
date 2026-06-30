package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RagChatResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ragId;
    private String question;
    private String response;
    private List<Map<String, Object>> searchList = new ArrayList<>();
    private List<Map<String, Object>> qaSearchList = new ArrayList<>();
    private long createdAt;

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

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<Map<String, Object>> getSearchList() {
        return searchList;
    }

    public void setSearchList(List<Map<String, Object>> searchList) {
        this.searchList = searchList;
    }

    public List<Map<String, Object>> getQaSearchList() {
        return qaSearchList;
    }

    public void setQaSearchList(List<Map<String, Object>> qaSearchList) {
        this.qaSearchList = qaSearchList;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
