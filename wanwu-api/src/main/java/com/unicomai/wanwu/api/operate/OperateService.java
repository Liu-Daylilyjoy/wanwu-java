package com.unicomai.wanwu.api.operate;

import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

public interface OperateService {

    ServiceDescriptor describe();

    void createSystemCustomTab(String userId, String orgId, String mode, Map<String, Object> request);

    void createSystemCustomLogin(String userId, String orgId, String mode, Map<String, Object> request);

    void createSystemCustomHome(String userId, String orgId, String mode, Map<String, Object> request);

    Map<String, Object> getSystemCustom(String mode);

    void addClientRecord(String clientId);

    Map<String, Object> getClientStatistic(String startDate, String endDate);

    void saveOAuthCode(String code, Map<String, Object> payload);

    Map<String, Object> consumeOAuthCode(String code);

    void saveOAuthRefreshToken(String refreshToken, Map<String, Object> payload);

    Map<String, Object> consumeOAuthRefreshToken(String refreshToken);
}
