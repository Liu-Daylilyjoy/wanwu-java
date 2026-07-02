package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.List;

public class ModelStatisticPageQuery extends ModelStatisticQuery implements Serializable {

    private int pageNo = 1;
    private int pageSize = 10;

    public ModelStatisticPageQuery() {
    }

    public ModelStatisticPageQuery(String userId,
                                   String orgId,
                                   String startDate,
                                   String endDate,
                                   List<String> modelIds,
                                   String modelType,
                                   int pageNo,
                                   int pageSize) {
        super(userId, orgId, startDate, endDate, modelIds, modelType);
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
