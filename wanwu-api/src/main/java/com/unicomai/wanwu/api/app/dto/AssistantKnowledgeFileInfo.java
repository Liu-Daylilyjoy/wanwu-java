package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class AssistantKnowledgeFileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileId;
    private String id;
    private String fileName;
    private String fileNameAlias;
    private String name;
    private long size;
    private String status;
    private String url;
    private String contentType;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFile_name() {
        return fileNameAlias;
    }

    public void setFile_name(String fileNameAlias) {
        this.fileNameAlias = fileNameAlias;
    }

    public void setFileNameAlias(String fileNameAlias) {
        this.fileNameAlias = fileNameAlias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
