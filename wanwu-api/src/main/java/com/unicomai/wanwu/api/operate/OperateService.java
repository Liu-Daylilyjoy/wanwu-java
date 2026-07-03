package com.unicomai.wanwu.api.operate;

import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

public interface OperateService {

    ServiceDescriptor describe();

    void createSystemCustomTab(String userId, String orgId, String mode, Map<String, Object> request);

    void createSystemCustomLogin(String userId, String orgId, String mode, Map<String, Object> request);

    void createSystemCustomHome(String userId, String orgId, String mode, Map<String, Object> request);

    Map<String, Object> getSystemCustom(String mode);
}
