package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplicationListResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Map<String, Object>> list = new ArrayList<>();

    public ApplicationListResult() {
    }

    public ApplicationListResult(List<Map<String, Object>> list) {
        this.list = list;
    }

    public List<Map<String, Object>> getList() {
        return list;
    }

    public void setList(List<Map<String, Object>> list) {
        this.list = list;
    }
}
