package com.unicomai.wanwu.api.assistant;

import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

public interface AssistantService {

    ServiceDescriptor describe();

    void saveToES(Map<String, Object> request);

    void deleteFromES(Map<String, Object> request);

    Map<String, Object> searchFromES(Map<String, Object> request);

    Map<String, Object> getAssistantByIds(Map<String, Object> request);

    Map<String, Object> assistantCreate(Map<String, Object> request);

    void assistantUpdate(Map<String, Object> request);

    void assistantConfigUpdate(Map<String, Object> request);

    void assistantDelete(Map<String, Object> request);

    Map<String, Object> getAssistantListMyAll(Map<String, Object> request);

    Map<String, Object> getAssistantInfo(Map<String, Object> request);

    Map<String, Object> getAssistantIdByUuid(Map<String, Object> request);

    Map<String, Object> assistantCopy(Map<String, Object> request);

    Map<String, Object> getAssistantDetailById(Map<String, Object> request);

    Map<String, Object> getMultiAssistantById(Map<String, Object> request);

    Map<String, Object> assistantSnapshotCreate(Map<String, Object> request);

    void assistantSnapshotUpdate(Map<String, Object> request);

    Map<String, Object> assistantSnapshotList(Map<String, Object> request);

    void assistantSnapshotRollback(Map<String, Object> request);

    Map<String, Object> assistantSnapshotInfo(Map<String, Object> request);

    Map<String, Object> assistantSnapshotLatest(Map<String, Object> request);

    Map<String, Object> assistantSnapshotLatestBatch(Map<String, Object> request);

    void assistantWorkFlowCreate(Map<String, Object> request);

    void assistantWorkFlowDelete(Map<String, Object> request);

    void assistantWorkFlowEnableSwitch(Map<String, Object> request);

    void assistantWorkFlowDeleteByWorkflowId(Map<String, Object> request);

    void assistantMCPCreate(Map<String, Object> request);

    void assistantMCPDelete(Map<String, Object> request);

    void assistantMCPEnableSwitch(Map<String, Object> request);

    Map<String, Object> assistantMCPGetList(Map<String, Object> request);

    void assistantMCPDeleteByMCPId(Map<String, Object> request);

    void assistantToolCreate(Map<String, Object> request);

    void assistantToolDelete(Map<String, Object> request);

    void assistantToolEnableSwitch(Map<String, Object> request);

    void assistantToolConfig(Map<String, Object> request);

    void assistantToolDeleteByToolId(Map<String, Object> request);

    void assistantSkillCreate(Map<String, Object> request);

    void assistantSkillDelete(Map<String, Object> request);

    void assistantSkillEnableSwitch(Map<String, Object> request);

    void multiAgentCreate(Map<String, Object> request);

    void multiAgentDelete(Map<String, Object> request);

    void multiAgentEnableSwitch(Map<String, Object> request);

    void multiAgentConfigUpdate(Map<String, Object> request);

    Map<String, Object> conversationCreate(Map<String, Object> request);

    void conversationDelete(Map<String, Object> request);

    void clearConversationES(Map<String, Object> request);

    Map<String, Object> getConversationIdByAssistantId(Map<String, Object> request);

    Map<String, Object> getConversationList(Map<String, Object> request);

    Map<String, Object> getConversationDetailList(Map<String, Object> request);

    Map<String, Object> assistantConversionStream(Map<String, Object> request);

    Map<String, Object> multiAssistantConversionStream(Map<String, Object> request);
}
