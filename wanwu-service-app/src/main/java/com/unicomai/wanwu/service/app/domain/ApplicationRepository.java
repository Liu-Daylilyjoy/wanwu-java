package com.unicomai.wanwu.service.app.domain;

import java.util.List;

public interface ApplicationRepository {

    AppRecord saveAssistant(AppRecord record);

    AppRecord updateAssistant(AppRecord record);

    List<AppRecord> listAssistants(String userId, String orgId, String name);

    AppRecord findAssistant(String userId, String orgId, String assistantId);

    boolean deleteAssistant(String userId, String orgId, String assistantId);

    List<String> listAssistantNamesByPrefix(String userId, String orgId, String prefix);

    AppRecord copyAssistant(AppRecord record, AssistantDraftConfigRecord config);

    AssistantDraftConfigRecord saveAssistantConfig(AssistantDraftConfigRecord record);

    AssistantDraftConfigRecord findAssistantConfig(String userId, String orgId, String assistantId);
}
