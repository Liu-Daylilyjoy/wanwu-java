package com.unicomai.wanwu.api.safety;

import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

public interface SafetyService {

    ServiceDescriptor describe();

    Map<String, Object> createSensitiveWordTable(String userId, String orgId, Map<String, Object> request);

    void updateSensitiveWordTable(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getSensitiveWordTable(String userId, String orgId, String tableId);

    void updateSensitiveWordTableReply(String userId, String orgId, Map<String, Object> request);

    void deleteSensitiveWordTable(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listSensitiveWordTables(String userId, String orgId, String type);

    Map<String, Object> selectSensitiveWordTables(String userId, String orgId);

    Map<String, Object> listSensitiveWords(String userId, String orgId, String tableId, int pageNo, int pageSize);

    void uploadSensitiveWord(String userId, String orgId, Map<String, Object> request);

    void deleteSensitiveWord(String userId, String orgId, Map<String, Object> request);
}
