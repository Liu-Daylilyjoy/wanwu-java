package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("rag_chat_records")
public class RagChatEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private String userId;
    private String orgId;
    private String ragId;
    private String chatId;
    private Boolean draft;
    private String question;
    private String response;
    private String historyJson;
    private String fileInfoJson;
    private String searchListJson;
    private String qaSearchListJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
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

    public String getRagId() {
        return ragId;
    }

    public void setRagId(String ragId) {
        this.ragId = ragId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
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

    public String getHistoryJson() {
        return historyJson;
    }

    public void setHistoryJson(String historyJson) {
        this.historyJson = historyJson;
    }

    public String getFileInfoJson() {
        return fileInfoJson;
    }

    public void setFileInfoJson(String fileInfoJson) {
        this.fileInfoJson = fileInfoJson;
    }

    public String getSearchListJson() {
        return searchListJson;
    }

    public void setSearchListJson(String searchListJson) {
        this.searchListJson = searchListJson;
    }

    public String getQaSearchListJson() {
        return qaSearchListJson;
    }

    public void setQaSearchListJson(String qaSearchListJson) {
        this.qaSearchListJson = qaSearchListJson;
    }
}
