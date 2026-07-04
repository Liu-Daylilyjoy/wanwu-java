package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class ChatflowConversationListQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String chatflowId;
    private int pageNo;
    private int pageSize;
    private String userId;
    private String orgId;

    public String getChatflowId() {
        return chatflowId;
    }

    public void setChatflowId(String chatflowId) {
        this.chatflowId = chatflowId;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
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
}
