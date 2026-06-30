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
import com.unicomai.wanwu.api.app.dto.AssistantUpdateCommand;
import com.unicomai.wanwu.api.app.dto.AppPublishCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionInfo;
import com.unicomai.wanwu.api.app.dto.AppVersionListResult;
import com.unicomai.wanwu.api.app.dto.AppVersionQuery;
import com.unicomai.wanwu.api.app.dto.AppVersionRollbackCommand;
import com.unicomai.wanwu.api.app.dto.AppVersionUpdateCommand;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

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

    Map<String, Object> getAssistantDraft(AssistantDetailQuery query);

    Map<String, Object> getPublishedAssistant(AssistantPublishedQuery query);

    AssistantConversationCreateResult createAssistantConversation(AssistantConversationCreateCommand command);

    void deleteAssistantConversation(AssistantConversationDeleteCommand command);

    void clearAssistantConversation(AssistantConversationDeleteCommand command);

    void deleteDraftAssistantConversation(AssistantConversationDeleteCommand command);

    AssistantConversationPageResult listAssistantConversations(AssistantConversationListQuery query);

    AssistantConversationPageResult listAssistantConversationDetails(AssistantConversationDetailQuery query);

    AssistantConversationPageResult listDraftAssistantConversationDetails(AssistantConversationListQuery query);

    AssistantConversationStreamResult streamAssistantConversation(AssistantConversationStreamCommand command);
}
