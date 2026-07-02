package com.unicomai.wanwu.service.app.domain;

import java.util.List;

public interface ApplicationRepository {

    AppRecord saveAssistant(AppRecord record);

    AppRecord updateAssistant(AppRecord record);

    List<AppRecord> listAssistants(String userId, String orgId, String name);

    AppRecord findAssistant(String userId, String orgId, String assistantId);

    AppRecord findAssistantByOrg(String orgId, String assistantId);

    boolean deleteAssistant(String userId, String orgId, String assistantId);

    List<String> listAssistantNamesByPrefix(String userId, String orgId, String prefix);

    AppRecord copyAssistant(AppRecord record, AssistantDraftConfigRecord config);

    AssistantDraftConfigRecord saveAssistantConfig(AssistantDraftConfigRecord record);

    AssistantDraftConfigRecord findAssistantConfig(String userId, String orgId, String assistantId);

    AssistantSnapshotRecord saveAssistantSnapshot(AssistantSnapshotRecord snapshot);

    List<AssistantSnapshotRecord> listAssistantSnapshots(String userId, String orgId, String assistantId);

    AssistantSnapshotRecord findLatestAssistantSnapshot(String userId, String orgId, String assistantId);

    AssistantSnapshotRecord findAssistantSnapshotByVersion(String userId, String orgId, String assistantId, String version);

    boolean updateLatestAssistantSnapshot(String userId, String orgId, String assistantId, String desc, long updatedAt);

    boolean updateAssistantPublishType(String userId, String orgId, String assistantId, String publishType, long updatedAt);

    boolean rollbackAssistant(AppRecord record, AssistantDraftConfigRecord config);

    AppRecord saveRag(AppRecord record);

    AppRecord updateRag(AppRecord record);

    List<AppRecord> listRags(String userId, String orgId, String name);

    AppRecord findRag(String userId, String orgId, String ragId);

    boolean deleteRag(String userId, String orgId, String ragId);

    List<String> listRagNamesByPrefix(String userId, String orgId, String prefix);

    AppRecord copyRag(AppRecord record, RagDraftConfigRecord config);

    RagDraftConfigRecord saveRagConfig(RagDraftConfigRecord record);

    RagDraftConfigRecord findRagConfig(String userId, String orgId, String ragId);

    RagSnapshotRecord saveRagSnapshot(RagSnapshotRecord snapshot);

    List<RagSnapshotRecord> listRagSnapshots(String userId, String orgId, String ragId);

    RagSnapshotRecord findLatestRagSnapshot(String userId, String orgId, String ragId);

    RagSnapshotRecord findRagSnapshotByVersion(String userId, String orgId, String ragId, String version);

    boolean updateLatestRagSnapshot(String userId, String orgId, String ragId, String desc, long updatedAt);

    boolean updateRagPublishType(String userId, String orgId, String ragId, String publishType, long updatedAt);

    boolean rollbackRag(AppRecord record, RagDraftConfigRecord config);

    AppRecord saveWorkflow(AppRecord record, WorkflowDraftRecord draft);

    List<AppRecord> listWorkflows(String userId, String orgId, String name);

    List<AppRecord> listWorkflows(String userId, String orgId, String name, String appType);

    AppRecord findWorkflow(String userId, String orgId, String workflowId);

    AppRecord findWorkflow(String userId, String orgId, String workflowId, String appType);

    WorkflowDraftRecord findWorkflowDraft(String userId, String orgId, String workflowId);

    boolean deleteWorkflow(String userId, String orgId, String workflowId);

    boolean deleteWorkflow(String userId, String orgId, String workflowId, String appType);

    List<String> listWorkflowNamesByPrefix(String userId, String orgId, String prefix);

    List<String> listWorkflowNamesByPrefix(String userId, String orgId, String prefix, String appType);

    AppRecord copyWorkflow(AppRecord record, WorkflowDraftRecord draft);

    WorkflowSnapshotRecord saveWorkflowSnapshot(WorkflowSnapshotRecord snapshot);

    List<WorkflowSnapshotRecord> listWorkflowSnapshots(String userId, String orgId, String workflowId);

    WorkflowSnapshotRecord findLatestWorkflowSnapshot(String userId, String orgId, String workflowId);

    WorkflowSnapshotRecord findWorkflowSnapshotByVersion(String userId, String orgId, String workflowId, String version);

    boolean updateLatestWorkflowSnapshot(String userId, String orgId, String workflowId, String desc, long updatedAt);

    boolean updateWorkflowPublishType(String userId, String orgId, String workflowId, String publishType, long updatedAt);

    boolean updateWorkflowPublishType(String userId, String orgId, String workflowId, String appType, String publishType, long updatedAt);

    boolean rollbackWorkflow(AppRecord record, WorkflowDraftRecord draft);

    ApiKeyRecord saveApiKey(ApiKeyRecord record);

    ApiKeyRecord updateApiKey(ApiKeyRecord record);

    ApiKeyRecord findApiKeyById(Long id);

    ApiKeyRecord findApiKeyByKey(String key);

    ApiKeyRecord findApiKeyByName(String userId, String orgId, String name);

    List<ApiKeyRecord> listApiKeys(String userId, String orgId, int offset, int limit);

    long countApiKeys(String userId, String orgId);

    boolean updateApiKeyStatus(Long id, boolean status, long updatedAt);

    boolean deleteApiKey(Long id);

    void recordApiKeyUsage(ApiKeyUsageRecord record, ApiKeyUsageAggregateRecord aggregate);

    ApiKeyUsageAggregateRecord sumApiKeyUsage(String userId,
                                              String orgId,
                                              String startDate,
                                              String endDate,
                                              List<String> apiKeyIds,
                                              List<String> methodPaths);

    List<ApiKeyUsageAggregateRecord> listApiKeyUsageTrend(String userId,
                                                          String orgId,
                                                          String startDate,
                                                          String endDate,
                                                          List<String> apiKeyIds,
                                                          List<String> methodPaths);

    List<ApiKeyUsageAggregateRecord> listApiKeyUsageAggregates(String userId,
                                                               String orgId,
                                                               String startDate,
                                                               String endDate,
                                                               List<String> apiKeyIds,
                                                               List<String> methodPaths,
                                                               int offset,
                                                               int limit);

    long countApiKeyUsageAggregates(String userId,
                                    String orgId,
                                    String startDate,
                                    String endDate,
                                    List<String> apiKeyIds,
                                    List<String> methodPaths);

    List<ApiKeyUsageRecord> listApiKeyUsageRecords(String userId,
                                                   String orgId,
                                                   String startDate,
                                                   String endDate,
                                                   List<String> apiKeyIds,
                                                   List<String> methodPaths,
                                                   int offset,
                                                   int limit);

    long countApiKeyUsageRecords(String userId,
                                 String orgId,
                                 String startDate,
                                 String endDate,
                                 List<String> apiKeyIds,
                                 List<String> methodPaths);

    List<String> listApiKeyUsageMethodPaths(String userId, String orgId);

    AppKeyRecord saveAppKey(AppKeyRecord record);

    List<AppKeyRecord> listAppKeys(String userId, String orgId, String appId, String appType);

    AppKeyRecord findAppKeyById(Long id);

    AppKeyRecord findAppKeyByKey(String apiKey);

    boolean deleteAppKey(Long id);

    AppUrlRecord saveAppUrl(AppUrlRecord record);

    AppUrlRecord updateAppUrl(AppUrlRecord record);

    AppUrlRecord findAppUrlById(String userId, String orgId, Long id);

    AppUrlRecord findAppUrlBySuffix(String suffix);

    AppUrlRecord findAppUrlByName(String userId, String orgId, String appId, String appType, String name);

    List<AppUrlRecord> listAppUrls(String userId, String orgId, String appId, String appType);

    boolean updateAppUrlStatus(String userId, String orgId, Long id, boolean status, long updatedAt);

    boolean deleteAppUrl(String userId, String orgId, Long id);

    AssistantConversationRecord saveConversation(AssistantConversationRecord record);

    AssistantConversationRecord findConversation(String userId, String orgId, String conversationId);

    AssistantConversationRecord findDraftConversation(String userId, String orgId, String assistantId);

    List<AssistantConversationRecord> listConversations(String userId,
                                                        String orgId,
                                                        String assistantId,
                                                        String conversationType,
                                                        int offset,
                                                        int limit);

    long countConversations(String userId, String orgId, String assistantId, String conversationType);

    boolean touchConversation(String userId, String orgId, String conversationId, long updatedAt);

    boolean deleteConversation(String userId, String orgId, String conversationId);

    AssistantConversationMessageRecord saveConversationMessage(AssistantConversationMessageRecord record);

    List<AssistantConversationMessageRecord> listConversationMessages(String userId,
                                                                     String orgId,
                                                                     String conversationId,
                                                                     int offset,
                                                                     int limit);

    long countConversationMessages(String userId, String orgId, String conversationId);

    boolean deleteConversationMessage(String userId, String orgId, String conversationId, String detailId);

    boolean deleteConversationMessages(String userId, String orgId, String conversationId);
}
