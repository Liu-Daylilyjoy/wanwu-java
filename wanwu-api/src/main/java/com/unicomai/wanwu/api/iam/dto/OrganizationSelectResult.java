package com.unicomai.wanwu.api.iam.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrganizationSelectResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<OrganizationOption> select = new ArrayList<>();

    public OrganizationSelectResult() {
    }

    public OrganizationSelectResult(List<OrganizationOption> select) {
        this.select = select;
    }

    public List<OrganizationOption> getSelect() {
        return select;
    }

    public void setSelect(List<OrganizationOption> select) {
        this.select = select;
    }
}
