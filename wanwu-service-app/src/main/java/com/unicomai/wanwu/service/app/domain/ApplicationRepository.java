package com.unicomai.wanwu.service.app.domain;

import java.util.List;

public interface ApplicationRepository {

    AppRecord saveAssistant(AppRecord record);

    List<AppRecord> listAssistants(String userId, String orgId, String name);

    AppRecord findAssistant(String userId, String orgId, String assistantId);
}
