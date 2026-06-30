package com.unicomai.wanwu.api.app.dto;

import java.io.Serializable;

public class RagDetailQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ragId;
    private String version;
    private String userId;
    private String orgId;

    public RagDetailQuery() {
    }

    public RagDetailQuery(String ragId, String version, String userId, String orgId) {
        this.ragId = ragId;
        this.version = version;
        this.userId = userId;
        this.orgId = orgId;
    }

    public String getRagId() {
        return ragId;
    }

    public void setRagId(String ragId) {
        this.ragId = ragId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
