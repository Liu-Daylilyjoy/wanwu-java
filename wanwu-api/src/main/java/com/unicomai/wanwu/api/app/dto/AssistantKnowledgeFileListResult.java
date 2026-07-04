package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AssistantKnowledgeFileListResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<AssistantKnowledgeFileInfo> list = new ArrayList<AssistantKnowledgeFileInfo>();
    private int total;

    public AssistantKnowledgeFileListResult() {
    }

    public AssistantKnowledgeFileListResult(List<AssistantKnowledgeFileInfo> list, int total) {
        this.list = list;
        this.total = total;
    }

    public List<AssistantKnowledgeFileInfo> getList() {
        return list;
    }

    public void setList(List<AssistantKnowledgeFileInfo> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
