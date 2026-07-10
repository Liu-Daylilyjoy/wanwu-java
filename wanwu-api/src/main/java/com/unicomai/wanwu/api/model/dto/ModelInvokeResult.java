package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelInvokeResult implements Serializable {

    private String content;
    private List<String> chunks = new ArrayList<String>();
    private Map<String, Object> response = new LinkedHashMap<String, Object>();
    private Map<String, Object> usage = new LinkedHashMap<String, Object>();

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getChunks() {
        return chunks;
    }

    public void setChunks(List<String> chunks) {
        this.chunks = chunks == null ? new ArrayList<String>() : chunks;
    }

    public Map<String, Object> getResponse() {
        return response;
    }

    public void setResponse(Map<String, Object> response) {
        this.response = response == null ? new LinkedHashMap<String, Object>() : response;
    }

    public Map<String, Object> getUsage() {
        return usage;
    }

    public void setUsage(Map<String, Object> usage) {
        this.usage = usage == null ? new LinkedHashMap<String, Object>() : usage;
    }
}
