package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AssistantKnowledgeFileUploadItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileName;
    private long size;
    private String contentType;

    public AssistantKnowledgeFileUploadItem() {
    }

    public AssistantKnowledgeFileUploadItem(String fileName, long size, String contentType) {
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
