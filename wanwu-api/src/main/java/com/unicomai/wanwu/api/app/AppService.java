package com.unicomai.wanwu.api.app;

import com.unicomai.wanwu.api.app.dto.AssistantConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCopyCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationListQuery;
import com.unicomai.wanwu.api.app.dto.AssistantConversationPageResult;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamCommand;
import com.unicomai.wanwu.api.app.dto.AssistantConversationStreamResult;
import com.unicomai.wanwu.api.app.dto.AssistantDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.AssistantPublishedQuery;
import com.unicomai.wanwu.api.app.dto.AssistantResourceCommand;
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationInfoQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationChatCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationCreateCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteByIdCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationDeleteCommand;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationListQuery;
import com.unicomai.wanwu.api.app.dto.ChatflowConversationMessageListQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyCreateCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyDeleteCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyInfo;
import com.unicomai.wanwu.api.app.dto.ApiKeyListQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyPageResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatusCommand;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticListResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticQuery;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticRecordResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyStatisticResult;
import com.unicomai.wanwu.api.app.dto.ApiKeyUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyCreateCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AppKeyInfo;
import com.unicomai.wanwu.api.app.dto.AppKeyListQuery;
import com.unicomai.wanwu.api.app.dto.AppStatisticListResult;
import com.unicomai.wanwu.api.app.dto.AppStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.AppStatisticQuery;
import com.unicomai.wanwu.api.app.dto.AppStatisticResult;
import com.unicomai.wanwu.api.app.dto.AppUrlCreateCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlDeleteCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlInfo;
import com.unicomai.wanwu.api.app.dto.AppUrlListQuery;
import com.unicomai.wanwu.api.app.dto.AppUrlStatusCommand;
import com.unicomai.wanwu.api.app.dto.AppUrlSuffixQuery;
import com.unicomai.wanwu.api.app.dto.AppUrlUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.AppVersionRollbackCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.app.dto.ExplorationAppFavoriteCommand;
import com.unicomai.wanwu.api.app.dto.ExplorationAppHistoryCommand;
import com.unicomai.wanwu.api.app.dto.ModelStatisticListResult;
import com.unicomai.wanwu.api.app.dto.ModelStatisticPageQuery;
import com.unicomai.wanwu.api.app.dto.ModelStatisticQuery;
import com.unicomai.wanwu.api.app.dto.ModelStatisticResult;
import com.unicomai.wanwu.api.app.dto.RagConfigUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RagCopyCommand;
import com.unicomai.wanwu.api.app.dto.RagChatCommand;
import com.unicomai.wanwu.api.app.dto.RagChatResult;
import com.unicomai.wanwu.api.app.dto.RagCreateCommand;
import com.unicomai.wanwu.api.app.dto.RagCreateResult;
import com.unicomai.wanwu.api.app.dto.RagDeleteCommand;
import com.unicomai.wanwu.api.app.dto.RagDetailQuery;
import com.unicomai.wanwu.api.app.dto.RagUpdateCommand;
import com.unicomai.wanwu.api.app.dto.RecordApiKeyStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RecordAppStatisticCommand;
import com.unicomai.wanwu.api.app.dto.RecordModelStatisticCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCopyCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowCreateResult;
import com.unicomai.wanwu.api.app.dto.WorkflowDeleteCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowExportQuery;
import com.unicomai.wanwu.api.app.dto.WorkflowExportResult;
import com.unicomai.wanwu.api.app.dto.WorkflowImportCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunCommand;
import com.unicomai.wanwu.api.app.dto.WorkflowRunResult;
import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;
import java.util.List;

public interface AppService {

    ServiceDescriptor describe();

    AssistantCreateResult createAssistant(AssistantCreateCommand command);

    void updateAssistant(AssistantUpdateCommand command);

    void updateAssistantConfig(AssistantConfigUpdateCommand command);

    void deleteAssistant(AssistantDeleteCommand command);

    AssistantCreateResult copyAssistant(AssistantCopyCommand command);

    void publishApp(AppPublishCommand command);

    void unpublishApp(AppPublishCommand command);

    AppVersionInfo getLatestAppVersion(AppVersionQuery query);

    AppVersionListResult listAppVersions(AppVersionQuery query);

    void updateAppVersion(AppVersionUpdateCommand command);

    void rollbackAppVersion(AppVersionRollbackCommand command);

    ApplicationListResult listAssistants(ApplicationListQuery query);

    ApplicationListResult listApplications(ApplicationListQuery query);

    void changeExplorationAppFavorite(ExplorationAppFavoriteCommand command);

    void recordAppHistory(ExplorationAppHistoryCommand command);

    Map<String, Object> getAssistantDraft(AssistantDetailQuery query);

    Map<String, Object> getPublishedAssistant(AssistantPublishedQuery query);

    RagCreateResult createRag(RagCreateCommand command);

    void updateRag(RagUpdateCommand command);

    void updateRagConfig(RagConfigUpdateCommand command);

    void deleteRag(RagDeleteCommand command);

    RagCreateResult copyRag(RagCopyCommand command);

    Map<String, Object> getRagDraft(RagDetailQuery query);

    Map<String, Object> getPublishedRag(RagDetailQuery query);

    RagChatResult streamRagChat(RagChatCommand command);

    WorkflowCreateResult createWorkflow(WorkflowCreateCommand command);

    WorkflowCreateResult importWorkflow(WorkflowImportCommand command);

    WorkflowCreateResult copyWorkflow(WorkflowCopyCommand command);

    void deleteWorkflow(WorkflowDeleteCommand command);

    WorkflowExportResult exportWorkflow(WorkflowExportQuery query);

    WorkflowRunResult runWorkflow(WorkflowRunCommand command);

    WorkflowCreateResult createChatflow(WorkflowCreateCommand command);

    WorkflowCreateResult importChatflow(WorkflowImportCommand command);

    WorkflowCreateResult copyChatflow(WorkflowCopyCommand command);

    void deleteChatflow(WorkflowDeleteCommand command);

    WorkflowExportResult exportChatflow(WorkflowExportQuery query);

    Map<String, Object> listChatflowApplications(ChatflowApplicationListQuery query);

    Map<String, Object> getChatflowApplication(ChatflowApplicationInfoQuery query);

    void deleteChatflowConversation(ChatflowConversationDeleteCommand command);

    Map<String, Object> createChatflowOpenApiConversation(ChatflowConversationCreateCommand command);

    Map<String, Object> listChatflowOpenApiConversations(ChatflowConversationListQuery query);

    Map<String, Object> listChatflowOpenApiConversationMessages(ChatflowConversationMessageListQuery query);

    Map<String, Object> chatflowOpenApiChat(ChatflowConversationChatCommand command);

    void deleteChatflowOpenApiConversation(ChatflowConversationDeleteByIdCommand command);

    void addAssistantWorkflow(AssistantResourceCommand command);

    void deleteAssistantWorkflow(AssistantResourceCommand command);

    void switchAssistantWorkflow(AssistantResourceCommand command);

    void addAssistantMcp(AssistantResourceCommand command);

    void deleteAssistantMcp(AssistantResourceCommand command);

    void switchAssistantMcp(AssistantResourceCommand command);

    void addAssistantTool(AssistantResourceCommand command);

    void deleteAssistantTool(AssistantResourceCommand command);

    void switchAssistantTool(AssistantResourceCommand command);

    void configureAssistantTool(AssistantResourceCommand command);

    void addAssistantSkill(AssistantResourceCommand command);

    void deleteAssistantSkill(AssistantResourceCommand command);

    void switchAssistantSkill(AssistantResourceCommand command);

    void addAssistantAgent(AssistantResourceCommand command);

    void deleteAssistantAgent(AssistantResourceCommand command);

    void switchAssistantAgent(AssistantResourceCommand command);

    void updateAssistantAgentConfig(AssistantResourceCommand command);

    Map<String, Object> listAssistantToolSelect(String userId, String orgId);

    Map<String, Object> listAssistantToolActions(AssistantResourceCommand command);

    Map<String, Object> getAssistantToolActionDetail(AssistantResourceCommand command);

    Map<String, Object> listAssistantMcpSelect(String userId, String orgId);

    Map<String, Object> listAssistantMcpActions(AssistantResourceCommand command);

    Map<String, Object> listAssistantWorkflowSelect(String userId, String orgId, String name);

    AssistantConversationCreateResult createAssistantConversation(AssistantConversationCreateCommand command);

    void deleteAssistantConversation(AssistantConversationDeleteCommand command);

    void clearAssistantConversation(AssistantConversationDeleteCommand command);

    void deleteDraftAssistantConversation(AssistantConversationDeleteCommand command);

    AssistantConversationPageResult listAssistantConversations(AssistantConversationListQuery query);

    AssistantConversationPageResult listAssistantConversationDetails(AssistantConversationDetailQuery query);

    AssistantConversationPageResult listDraftAssistantConversationDetails(AssistantConversationListQuery query);

    AssistantConversationStreamResult streamAssistantConversation(AssistantConversationStreamCommand command);

    ApiKeyInfo createApiKey(ApiKeyCreateCommand command);

    void updateApiKey(ApiKeyUpdateCommand command);

    void deleteApiKey(ApiKeyDeleteCommand command);

    void updateApiKeyStatus(ApiKeyStatusCommand command);

    ApiKeyPageResult listApiKeys(ApiKeyListQuery query);

    ApiKeyInfo getApiKeyByKey(String key);

    void recordApiKeyStatistic(RecordApiKeyStatisticCommand command);

    ApiKeyStatisticResult getApiKeyStatistic(ApiKeyStatisticQuery query);

    ApiKeyStatisticListResult listApiKeyStatistics(ApiKeyStatisticPageQuery query);

    ApiKeyStatisticRecordResult listApiKeyStatisticRecords(ApiKeyStatisticPageQuery query);

    void recordAppStatistic(RecordAppStatisticCommand command);

    AppStatisticResult getAppStatistic(AppStatisticQuery query);

    AppStatisticListResult listAppStatistics(AppStatisticPageQuery query);

    void recordModelStatistic(RecordModelStatisticCommand command);

    ModelStatisticResult getModelStatistic(ModelStatisticQuery query);

    ModelStatisticListResult listModelStatistics(ModelStatisticPageQuery query);

    AppKeyInfo createAppKey(AppKeyCreateCommand command);

    void deleteAppKey(AppKeyDeleteCommand command);

    List<AppKeyInfo> listAppKeys(AppKeyListQuery query);

    AppKeyInfo getAppKeyByKey(String appKey);

    void createAppUrl(AppUrlCreateCommand command);

    void updateAppUrl(AppUrlUpdateCommand command);

    void deleteAppUrl(AppUrlDeleteCommand command);

    void updateAppUrlStatus(AppUrlStatusCommand command);

    List<AppUrlInfo> listAppUrls(AppUrlListQuery query);

    AppUrlInfo getAppUrlBySuffix(AppUrlSuffixQuery query);
}
