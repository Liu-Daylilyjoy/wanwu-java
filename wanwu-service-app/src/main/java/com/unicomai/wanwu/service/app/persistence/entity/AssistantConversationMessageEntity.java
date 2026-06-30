package com.unicomai.wanwu.service.app.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("assistant_conversation_messages")
public class AssistantConversationMessageEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("created_at")
    private Long createdAt;
    @TableField("updated_at")
    private Long updatedAt;
    @TableField("user_id")
    private String userId;
    @TableField("org_id")
    private String orgId;
    @TableField("assistant_id")
    private String assistantId;
    @TableField("conversation_id")
    private String conversationId;
    @TableField("detail_id")
    private String detailId;
    private String prompt;
    @TableField("sys_prompt")
    private String sysPrompt;
    private String response;
    @TableField("response_list")
    private String responseListJson;
    @TableField("search_list")
    private String searchListJson;
    @TableField("request_files")
    private String requestFilesJson;
    @TableField("response_files")
    private String responseFilesJson;
    @TableField("sub_conversation_list")
    private String subConversationListJson;
    @TableField("file_size")
    private Long fileSize;
    @TableField("file_name")
    private String fileName;
    @TableField("qa_type")
    private Integer qaType;

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

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getSysPrompt() {
        return sysPrompt;
    }

    public void setSysPrompt(String sysPrompt) {
        this.sysPrompt = sysPrompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponseListJson() {
        return responseListJson;
    }

    public void setResponseListJson(String responseListJson) {
        this.responseListJson = responseListJson;
    }

    public String getSearchListJson() {
        return searchListJson;
    }

    public void setSearchListJson(String searchListJson) {
        this.searchListJson = searchListJson;
    }

    public String getRequestFilesJson() {
        return requestFilesJson;
    }

    public void setRequestFilesJson(String requestFilesJson) {
        this.requestFilesJson = requestFilesJson;
    }

    public String getResponseFilesJson() {
        return responseFilesJson;
    }

    public void setResponseFilesJson(String responseFilesJson) {
        this.responseFilesJson = responseFilesJson;
    }

    public String getSubConversationListJson() {
        return subConversationListJson;
    }

    public void setSubConversationListJson(String subConversationListJson) {
        this.subConversationListJson = subConversationListJson;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getQaType() {
        return qaType;
    }

    public void setQaType(Integer qaType) {
        this.qaType = qaType;
    }
}
