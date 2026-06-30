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

    AssistantSnapshotRecord saveAssistantSnapshot(AssistantSnapshotRecord snapshot);

    List<AssistantSnapshotRecord> listAssistantSnapshots(String userId, String orgId, String assistantId);

    AssistantSnapshotRecord findLatestAssistantSnapshot(String userId, String orgId, String assistantId);

    AssistantSnapshotRecord findAssistantSnapshotByVersion(String userId, String orgId, String assistantId, String version);

    boolean updateLatestAssistantSnapshot(String userId, String orgId, String assistantId, String desc, long updatedAt);

    boolean updateAssistantPublishType(String userId, String orgId, String assistantId, String publishType, long updatedAt);

    boolean rollbackAssistant(AppRecord record, AssistantDraftConfigRecord config);

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
