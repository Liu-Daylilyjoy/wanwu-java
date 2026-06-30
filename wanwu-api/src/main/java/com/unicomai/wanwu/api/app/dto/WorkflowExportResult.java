package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class WorkflowExportResult implements Serializable {

    private String name;
    private String desc;
    private String schema;

    public WorkflowExportResult() {
    }

    public WorkflowExportResult(String name, String desc, String schema) {
        this.name = name;
        this.desc = desc;
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
