package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AssistantKnowledgeFileUploadResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> list = new ArrayList<String>();
    private List<AssistantKnowledgeFileInfo> fileList = new ArrayList<AssistantKnowledgeFileInfo>();
    private int total;

    public AssistantKnowledgeFileUploadResult() {
    }

    public AssistantKnowledgeFileUploadResult(List<String> list,
                                              List<AssistantKnowledgeFileInfo> fileList,
                                              int total) {
        this.list = list;
        this.fileList = fileList;
        this.total = total;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public List<AssistantKnowledgeFileInfo> getFileList() {
        return fileList;
    }

    public void setFileList(List<AssistantKnowledgeFileInfo> fileList) {
        this.fileList = fileList;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
