package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.List;

public class AppStatisticPageQuery extends AppStatisticQuery implements Serializable {

    private int pageNo = 1;
    private int pageSize = 10;

    public AppStatisticPageQuery() {
    }

    public AppStatisticPageQuery(String userId,
                                 String orgId,
                                 String startDate,
                                 String endDate,
                                 List<String> appIds,
                                 String appType,
                                 int pageNo,
                                 int pageSize) {
        super(userId, orgId, startDate, endDate, appIds, appType);
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
