package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;
import java.util.List;

public class ApiKeyStatisticPageQuery extends ApiKeyStatisticQuery implements Serializable {

    private int pageNo = 1;
    private int pageSize = 10;

    public ApiKeyStatisticPageQuery() {
    }

    public ApiKeyStatisticPageQuery(String userId,
                                    String orgId,
                                    String startDate,
                                    String endDate,
                                    List<String> apiKeyIds,
                                    List<String> methodPaths,
                                    int pageNo,
                                    int pageSize) {
        super(userId, orgId, startDate, endDate, apiKeyIds, methodPaths);
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
