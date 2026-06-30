package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModelExperienceDialogListResult implements Serializable {

    private List<ModelExperienceDialogInfo> list = new ArrayList<ModelExperienceDialogInfo>();
    private long total;

    public ModelExperienceDialogListResult() {
    }

    public ModelExperienceDialogListResult(List<ModelExperienceDialogInfo> list, long total) {
        setList(list);
        this.total = total;
    }

    public List<ModelExperienceDialogInfo> getList() {
        return list;
    }

    public void setList(List<ModelExperienceDialogInfo> list) {
        this.list = list == null ? new ArrayList<ModelExperienceDialogInfo>() : list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
