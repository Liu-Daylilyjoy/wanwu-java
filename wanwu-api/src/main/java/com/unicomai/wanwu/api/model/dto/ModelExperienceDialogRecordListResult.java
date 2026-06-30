package com.unicomai.wanwu.api.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModelExperienceDialogRecordListResult implements Serializable {

    private List<ModelExperienceDialogRecordInfo> list = new ArrayList<ModelExperienceDialogRecordInfo>();
    private long total;

    public ModelExperienceDialogRecordListResult() {
    }

    public ModelExperienceDialogRecordListResult(List<ModelExperienceDialogRecordInfo> list, long total) {
        setList(list);
        this.total = total;
    }

    public List<ModelExperienceDialogRecordInfo> getList() {
        return list;
    }

    public void setList(List<ModelExperienceDialogRecordInfo> list) {
        this.list = list == null ? new ArrayList<ModelExperienceDialogRecordInfo>() : list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
