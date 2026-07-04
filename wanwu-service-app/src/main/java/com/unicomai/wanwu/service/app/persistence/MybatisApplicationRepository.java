package com.unicomai.wanwu.service.app.persistence;

import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationMessageRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationRecord;
import com.unicomai.wanwu.service.app.domain.AssistantActionRecord;
import com.unicomai.wanwu.service.app.domain.AssistantKnowledgeFileRecord;
import com.unicomai.wanwu.service.app.domain.AssistantSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyUsageAggregateRecord;
import com.unicomai.wanwu.service.app.domain.ApiKeyUsageRecord;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.AppFavoriteRecord;
import com.unicomai.wanwu.service.app.domain.AppHistoryRecord;
import com.unicomai.wanwu.service.app.domain.AppKeyRecord;
import com.unicomai.wanwu.service.app.domain.AppStatisticAggregateRecord;
import com.unicomai.wanwu.service.app.domain.AppUrlRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import com.unicomai.wanwu.service.app.domain.GeneralAgentConfigRecord;
import com.unicomai.wanwu.service.app.domain.GeneralAgentConversationRecord;
import com.unicomai.wanwu.service.app.domain.ModelStatisticAggregateRecord;
import com.unicomai.wanwu.service.app.domain.RagDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.RagSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowDraftRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowRunRecord;
import com.unicomai.wanwu.service.app.domain.WorkflowSnapshotRecord;
import com.unicomai.wanwu.service.app.persistence.entity.ApiKeyEntity;
import com.unicomai.wanwu.service.app.persistence.entity.ApiKeyUsageAggregateEntity;
import com.unicomai.wanwu.service.app.persistence.entity.ApiKeyUsageRecordEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AppEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AppFavoriteEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AppHistoryEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AppKeyEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AppStatisticEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AppUrlEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantConversationEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantConversationMessageEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantActionEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantDraftConfigEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantDraftEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantKnowledgeFileEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantSnapshotEntity;
import com.unicomai.wanwu.service.app.persistence.entity.GeneralAgentConfigEntity;
import com.unicomai.wanwu.service.app.persistence.entity.GeneralAgentConversationEntity;
import com.unicomai.wanwu.service.app.persistence.entity.RagDraftConfigEntity;
import com.unicomai.wanwu.service.app.persistence.entity.RagDraftEntity;
import com.unicomai.wanwu.service.app.persistence.entity.RagSnapshotEntity;
import com.unicomai.wanwu.service.app.persistence.entity.ModelStatisticEntity;
import com.unicomai.wanwu.service.app.persistence.entity.WorkflowDraftEntity;
import com.unicomai.wanwu.service.app.persistence.entity.WorkflowRunEntity;
import com.unicomai.wanwu.service.app.persistence.entity.WorkflowSnapshotEntity;
import com.unicomai.wanwu.service.app.persistence.mapper.ApiKeyMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.ApiKeyUsageAggregateMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.ApiKeyUsageRecordMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AppMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AppFavoriteMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AppHistoryMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AppKeyMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AppStatisticMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AppUrlMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantConversationMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantConversationMessageMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantActionMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantDraftConfigMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantDraftMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantKnowledgeFileMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantSnapshotMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.GeneralAgentConfigMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.GeneralAgentConversationMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.RagDraftConfigMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.RagDraftMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.RagSnapshotMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.ModelStatisticMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.WorkflowDraftMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.WorkflowRunMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.WorkflowSnapshotMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class MybatisApplicationRepository implements ApplicationRepository {

    private final AppMapper appMapper;
    private final AssistantDraftMapper assistantDraftMapper;
    private final AssistantDraftConfigMapper assistantDraftConfigMapper;
    private final AssistantSnapshotMapper assistantSnapshotMapper;
    private final RagDraftMapper ragDraftMapper;
    private final RagDraftConfigMapper ragDraftConfigMapper;
    private final RagSnapshotMapper ragSnapshotMapper;
    private final WorkflowDraftMapper workflowDraftMapper;
    private final WorkflowSnapshotMapper workflowSnapshotMapper;
    private final WorkflowRunMapper workflowRunMapper;
    private final ApiKeyMapper apiKeyMapper;
    private final ApiKeyUsageAggregateMapper apiKeyUsageAggregateMapper;
    private final ApiKeyUsageRecordMapper apiKeyUsageRecordMapper;
    private final AppStatisticMapper appStatisticMapper;
    private final ModelStatisticMapper modelStatisticMapper;
    private final AppKeyMapper appKeyMapper;
    private final AppUrlMapper appUrlMapper;
    private final AppFavoriteMapper appFavoriteMapper;
    private final AppHistoryMapper appHistoryMapper;
    private final AssistantConversationMapper assistantConversationMapper;
    private final AssistantConversationMessageMapper assistantConversationMessageMapper;
    private final AssistantActionMapper assistantActionMapper;
    private final AssistantKnowledgeFileMapper assistantKnowledgeFileMapper;
    private final GeneralAgentConfigMapper generalAgentConfigMapper;
    private final GeneralAgentConversationMapper generalAgentConversationMapper;

    public MybatisApplicationRepository(AppMapper appMapper,
                                        AssistantDraftMapper assistantDraftMapper,
                                        AssistantDraftConfigMapper assistantDraftConfigMapper,
                                        AssistantSnapshotMapper assistantSnapshotMapper,
                                        RagDraftMapper ragDraftMapper,
                                        RagDraftConfigMapper ragDraftConfigMapper,
                                        RagSnapshotMapper ragSnapshotMapper,
                                        WorkflowDraftMapper workflowDraftMapper,
                                        WorkflowSnapshotMapper workflowSnapshotMapper,
                                        WorkflowRunMapper workflowRunMapper,
                                        ApiKeyMapper apiKeyMapper,
                                        ApiKeyUsageAggregateMapper apiKeyUsageAggregateMapper,
                                        ApiKeyUsageRecordMapper apiKeyUsageRecordMapper,
                                        AppStatisticMapper appStatisticMapper,
                                        ModelStatisticMapper modelStatisticMapper,
                                        AppKeyMapper appKeyMapper,
                                        AppUrlMapper appUrlMapper,
                                        AppFavoriteMapper appFavoriteMapper,
                                        AppHistoryMapper appHistoryMapper,
                                        AssistantConversationMapper assistantConversationMapper,
                                        AssistantConversationMessageMapper assistantConversationMessageMapper,
                                        AssistantActionMapper assistantActionMapper,
                                        AssistantKnowledgeFileMapper assistantKnowledgeFileMapper,
                                        GeneralAgentConfigMapper generalAgentConfigMapper,
                                        GeneralAgentConversationMapper generalAgentConversationMapper) {
        this.appMapper = appMapper;
        this.assistantDraftMapper = assistantDraftMapper;
        this.assistantDraftConfigMapper = assistantDraftConfigMapper;
        this.assistantSnapshotMapper = assistantSnapshotMapper;
        this.ragDraftMapper = ragDraftMapper;
        this.ragDraftConfigMapper = ragDraftConfigMapper;
        this.ragSnapshotMapper = ragSnapshotMapper;
        this.workflowDraftMapper = workflowDraftMapper;
        this.workflowSnapshotMapper = workflowSnapshotMapper;
        this.workflowRunMapper = workflowRunMapper;
        this.apiKeyMapper = apiKeyMapper;
        this.apiKeyUsageAggregateMapper = apiKeyUsageAggregateMapper;
        this.apiKeyUsageRecordMapper = apiKeyUsageRecordMapper;
        this.appStatisticMapper = appStatisticMapper;
        this.modelStatisticMapper = modelStatisticMapper;
        this.appKeyMapper = appKeyMapper;
        this.appUrlMapper = appUrlMapper;
        this.appFavoriteMapper = appFavoriteMapper;
        this.appHistoryMapper = appHistoryMapper;
        this.assistantConversationMapper = assistantConversationMapper;
        this.assistantConversationMessageMapper = assistantConversationMessageMapper;
        this.assistantActionMapper = assistantActionMapper;
        this.assistantKnowledgeFileMapper = assistantKnowledgeFileMapper;
        this.generalAgentConfigMapper = generalAgentConfigMapper;
        this.generalAgentConversationMapper = generalAgentConversationMapper;
    }

    @Override
    @Transactional
    public AppRecord saveAssistant(AppRecord record) {
        AppEntity app = new AppEntity();
        app.setCreatedAt(record.getCreatedAt());
        app.setUpdatedAt(record.getUpdatedAt());
        app.setUserId(record.getUserId());
        app.setOrgId(record.getOrgId());
        app.setAppId(record.getAppId());
        app.setAppType(record.getAppType());
        app.setPublishType(record.getPublishType());
        appMapper.insert(app);

        AssistantDraftEntity draft = new AssistantDraftEntity();
        draft.setCreatedAt(record.getCreatedAt());
        draft.setUpdatedAt(record.getUpdatedAt());
        draft.setUserId(record.getUserId());
        draft.setOrgId(record.getOrgId());
        draft.setAssistantId(record.getAppId());
        draft.setName(record.getName());
        draft.setDescription(record.getDesc());
        draft.setAvatarKey(record.getAvatarKey());
        draft.setAvatarPath(record.getAvatarPath());
        draft.setCategory(record.getCategory());
        assistantDraftMapper.insert(draft);
        assistantDraftConfigMapper.upsert(newDefaultConfig(record));

        record.setId(app.getId());
        return record;
    }

    @Override
    @Transactional
    public AppRecord updateAssistant(AppRecord record) {
        appMapper.updateAssistantUpdatedAt(record.getUserId(), record.getOrgId(), record.getAppId(), record.getUpdatedAt());
        int updated = assistantDraftMapper.updateDraft(record);
        return updated > 0 ? record : null;
    }

    @Override
    public List<AppRecord> listAssistants(String userId, String orgId, String name) {
        return appMapper.selectAssistantRecords(userId, orgId, name);
    }

    @Override
    public AppRecord findAssistant(String userId, String orgId, String assistantId) {
        return appMapper.selectAssistantRecord(userId, orgId, assistantId);
    }

    @Override
    public AppRecord findAssistantByOrg(String orgId, String assistantId) {
        return appMapper.selectAssistantRecordByOrg(orgId, assistantId);
    }

    @Override
    @Transactional
    public boolean deleteAssistant(String userId, String orgId, String assistantId) {
        appUrlMapper.deleteByAssistant(userId, orgId, assistantId);
        assistantConversationMessageMapper.deleteByAssistant(userId, orgId, assistantId);
        assistantConversationMapper.deleteByAssistant(userId, orgId, assistantId);
        assistantActionMapper.deleteByAssistant(userId, orgId, assistantId);
        assistantKnowledgeFileMapper.deleteByAssistant(userId, orgId, assistantId);
        assistantSnapshotMapper.deleteByAssistant(userId, orgId, assistantId);
        assistantDraftConfigMapper.deleteByAssistant(userId, orgId, assistantId);
        int draftDeleted = assistantDraftMapper.deleteDraft(userId, orgId, assistantId);
        int appDeleted = appMapper.deleteAssistantApp(userId, orgId, assistantId);
        return draftDeleted > 0 && appDeleted > 0;
    }

    @Override
    public List<String> listAssistantNamesByPrefix(String userId, String orgId, String prefix) {
        return appMapper.selectAssistantNamesByPrefix(userId, orgId, prefix);
    }

    @Override
    @Transactional
    public AppRecord copyAssistant(AppRecord record, AssistantDraftConfigRecord config) {
        saveAssistant(record);
        assistantDraftConfigMapper.upsert(toEntity(config));
        return record;
    }

    @Override
    @Transactional
    public AssistantDraftConfigRecord saveAssistantConfig(AssistantDraftConfigRecord record) {
        assistantDraftConfigMapper.upsert(toEntity(record));
        appMapper.updateAssistantUpdatedAt(record.getUserId(), record.getOrgId(), record.getAssistantId(), record.getUpdatedAt());
        return record;
    }

    @Override
    public AssistantDraftConfigRecord findAssistantConfig(String userId, String orgId, String assistantId) {
        return toRecord(assistantDraftConfigMapper.selectByAssistant(userId, orgId, assistantId));
    }

    @Override
    public AssistantSnapshotRecord saveAssistantSnapshot(AssistantSnapshotRecord snapshot) {
        AssistantSnapshotEntity entity = toEntity(snapshot);
        assistantSnapshotMapper.insert(entity);
        snapshot.setId(entity.getId());
        return snapshot;
    }

    @Override
    public List<AssistantSnapshotRecord> listAssistantSnapshots(String userId, String orgId, String assistantId) {
        return toSnapshotRecords(assistantSnapshotMapper.selectByAssistant(userId, orgId, assistantId));
    }

    @Override
    public AssistantSnapshotRecord findLatestAssistantSnapshot(String userId, String orgId, String assistantId) {
        return toRecord(assistantSnapshotMapper.selectLatest(userId, orgId, assistantId));
    }

    @Override
    public AssistantSnapshotRecord findAssistantSnapshotByVersion(String userId,
                                                                 String orgId,
                                                                 String assistantId,
                                                                 String version) {
        return toRecord(assistantSnapshotMapper.selectByVersion(userId, orgId, assistantId, version));
    }

    @Override
    public boolean updateLatestAssistantSnapshot(String userId,
                                                 String orgId,
                                                 String assistantId,
                                                 String desc,
                                                 long updatedAt) {
        AssistantSnapshotEntity latest = assistantSnapshotMapper.selectLatest(userId, orgId, assistantId);
        if (latest == null) {
            return false;
        }
        return assistantSnapshotMapper.updateLatestDescription(latest.getId(), desc, updatedAt) > 0;
    }

    @Override
    public boolean updateAssistantPublishType(String userId,
                                              String orgId,
                                              String assistantId,
                                              String publishType,
                                              long updatedAt) {
        return appMapper.updateAssistantPublishType(userId, orgId, assistantId, publishType, updatedAt) > 0;
    }

    @Override
    @Transactional
    public boolean rollbackAssistant(AppRecord record, AssistantDraftConfigRecord config) {
        appMapper.updateAssistantUpdatedAt(record.getUserId(), record.getOrgId(), record.getAppId(), record.getUpdatedAt());
        int updated = assistantDraftMapper.updateDraft(record);
        if (updated <= 0) {
            return false;
        }
        assistantDraftConfigMapper.upsert(toEntity(config));
        return true;
    }

    @Override
    @Transactional
    public AppRecord saveRag(AppRecord record) {
        AppEntity app = new AppEntity();
        app.setCreatedAt(record.getCreatedAt());
        app.setUpdatedAt(record.getUpdatedAt());
        app.setUserId(record.getUserId());
        app.setOrgId(record.getOrgId());
        app.setAppId(record.getAppId());
        app.setAppType(record.getAppType());
        app.setPublishType(record.getPublishType());
        appMapper.insert(app);

        RagDraftEntity draft = new RagDraftEntity();
        draft.setCreatedAt(record.getCreatedAt());
        draft.setUpdatedAt(record.getUpdatedAt());
        draft.setUserId(record.getUserId());
        draft.setOrgId(record.getOrgId());
        draft.setRagId(record.getAppId());
        draft.setName(record.getName());
        draft.setDescription(record.getDesc());
        draft.setAvatarKey(record.getAvatarKey());
        draft.setAvatarPath(record.getAvatarPath());
        draft.setCategory(record.getCategory());
        ragDraftMapper.insert(draft);
        ragDraftConfigMapper.upsert(newDefaultRagConfig(record));

        record.setId(app.getId());
        return record;
    }

    @Override
    @Transactional
    public AppRecord updateRag(AppRecord record) {
        appMapper.updateRagUpdatedAt(record.getUserId(), record.getOrgId(), record.getAppId(), record.getUpdatedAt());
        int updated = ragDraftMapper.updateDraft(record);
        return updated > 0 ? record : null;
    }

    @Override
    public List<AppRecord> listRags(String userId, String orgId, String name) {
        return appMapper.selectRagRecords(userId, orgId, name);
    }

    @Override
    public AppRecord findRag(String userId, String orgId, String ragId) {
        return appMapper.selectRagRecord(userId, orgId, ragId);
    }

    @Override
    @Transactional
    public boolean deleteRag(String userId, String orgId, String ragId) {
        appUrlMapper.deleteByAssistant(userId, orgId, ragId);
        ragSnapshotMapper.deleteByRag(userId, orgId, ragId);
        ragDraftConfigMapper.deleteByRag(userId, orgId, ragId);
        int draftDeleted = ragDraftMapper.deleteDraft(userId, orgId, ragId);
        int appDeleted = appMapper.deleteRagApp(userId, orgId, ragId);
        return draftDeleted > 0 && appDeleted > 0;
    }

    @Override
    public List<String> listRagNamesByPrefix(String userId, String orgId, String prefix) {
        return appMapper.selectRagNamesByPrefix(userId, orgId, prefix);
    }

    @Override
    @Transactional
    public AppRecord copyRag(AppRecord record, RagDraftConfigRecord config) {
        saveRag(record);
        if (config != null) {
            ragDraftConfigMapper.upsert(toEntity(config));
        }
        return record;
    }

    @Override
    @Transactional
    public RagDraftConfigRecord saveRagConfig(RagDraftConfigRecord record) {
        ragDraftConfigMapper.upsert(toEntity(record));
        appMapper.updateRagUpdatedAt(record.getUserId(), record.getOrgId(), record.getRagId(), record.getUpdatedAt());
        return record;
    }

    @Override
    public RagDraftConfigRecord findRagConfig(String userId, String orgId, String ragId) {
        return toRecord(ragDraftConfigMapper.selectByRag(userId, orgId, ragId));
    }

    @Override
    public RagSnapshotRecord saveRagSnapshot(RagSnapshotRecord snapshot) {
        RagSnapshotEntity entity = toEntity(snapshot);
        ragSnapshotMapper.insert(entity);
        snapshot.setId(entity.getId());
        return snapshot;
    }

    @Override
    public List<RagSnapshotRecord> listRagSnapshots(String userId, String orgId, String ragId) {
        return toRagSnapshotRecords(ragSnapshotMapper.selectByRag(userId, orgId, ragId));
    }

    @Override
    public RagSnapshotRecord findLatestRagSnapshot(String userId, String orgId, String ragId) {
        return toRecord(ragSnapshotMapper.selectLatest(userId, orgId, ragId));
    }

    @Override
    public RagSnapshotRecord findRagSnapshotByVersion(String userId, String orgId, String ragId, String version) {
        return toRecord(ragSnapshotMapper.selectByVersion(userId, orgId, ragId, version));
    }

    @Override
    public boolean updateLatestRagSnapshot(String userId, String orgId, String ragId, String desc, long updatedAt) {
        RagSnapshotEntity latest = ragSnapshotMapper.selectLatest(userId, orgId, ragId);
        if (latest == null) {
            return false;
        }
        return ragSnapshotMapper.updateLatestDescription(latest.getId(), desc, updatedAt) > 0;
    }

    @Override
    public boolean updateRagPublishType(String userId, String orgId, String ragId, String publishType, long updatedAt) {
        return appMapper.updateRagPublishType(userId, orgId, ragId, publishType, updatedAt) > 0;
    }

    @Override
    @Transactional
    public boolean rollbackRag(AppRecord record, RagDraftConfigRecord config) {
        appMapper.updateRagUpdatedAt(record.getUserId(), record.getOrgId(), record.getAppId(), record.getUpdatedAt());
        int updated = ragDraftMapper.updateDraft(record);
        if (updated <= 0) {
            return false;
        }
        ragDraftConfigMapper.upsert(toEntity(config));
        return true;
    }

    @Override
    @Transactional
    public AppRecord saveWorkflow(AppRecord record, WorkflowDraftRecord draftRecord) {
        AppEntity app = new AppEntity();
        app.setCreatedAt(record.getCreatedAt());
        app.setUpdatedAt(record.getUpdatedAt());
        app.setUserId(record.getUserId());
        app.setOrgId(record.getOrgId());
        app.setAppId(record.getAppId());
        app.setAppType(record.getAppType());
        app.setPublishType(record.getPublishType());
        appMapper.insert(app);

        WorkflowDraftEntity draft = toWorkflowDraftEntity(record, draftRecord);
        workflowDraftMapper.insert(draft);

        record.setId(app.getId());
        draftRecord.setId(draft.getId());
        return record;
    }

    @Override
    public List<AppRecord> listWorkflows(String userId, String orgId, String name) {
        return listWorkflows(userId, orgId, name, "workflow");
    }

    @Override
    public List<AppRecord> listWorkflows(String userId, String orgId, String name, String appType) {
        return appMapper.selectWorkflowRecords(userId, orgId, name, appType);
    }

    @Override
    public List<AppFavoriteRecord> listAppFavorites(String userId, String appType) {
        List<AppFavoriteEntity> entities = appFavoriteMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AppFavoriteEntity>()
                        .eq("user_id", userId)
                        .eq(appType != null && !appType.isEmpty(), "app_type", appType)
                        .orderByDesc("updated_at"));
        return toAppFavoriteRecords(entities);
    }

    @Override
    public void saveAppFavorite(AppFavoriteRecord record) {
        AppFavoriteEntity existing = appFavoriteMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AppFavoriteEntity>()
                        .eq("user_id", record.getUserId())
                        .eq("app_id", record.getAppId())
                        .eq("app_type", record.getAppType())
                        .last("LIMIT 1"));
        if (existing == null) {
            appFavoriteMapper.insert(toEntity(record));
            return;
        }
        existing.setUpdatedAt(record.getUpdatedAt());
        appFavoriteMapper.updateById(existing);
    }

    @Override
    public boolean deleteAppFavorite(String userId, String appId, String appType) {
        int deleted = appFavoriteMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AppFavoriteEntity>()
                        .eq("user_id", userId)
                        .eq("app_id", appId)
                        .eq("app_type", appType));
        return deleted > 0;
    }

    @Override
    public List<AppHistoryRecord> listAppHistories(String userId, String appType, long startUpdatedAt) {
        List<AppHistoryEntity> entities = appHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AppHistoryEntity>()
                        .eq("user_id", userId)
                        .eq(appType != null && !appType.isEmpty(), "app_type", appType)
                        .ge(startUpdatedAt > 0L, "updated_at", startUpdatedAt)
                        .orderByDesc("updated_at"));
        return toAppHistoryRecords(entities);
    }

    @Override
    public void saveAppHistory(AppHistoryRecord record) {
        AppHistoryEntity existing = appHistoryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AppHistoryEntity>()
                        .eq("user_id", record.getUserId())
                        .eq("app_id", record.getAppId())
                        .eq("app_type", record.getAppType())
                        .last("LIMIT 1"));
        if (existing == null) {
            appHistoryMapper.insert(toEntity(record));
            return;
        }
        existing.setUpdatedAt(record.getUpdatedAt());
        appHistoryMapper.updateById(existing);
    }

    @Override
    public AppRecord findWorkflow(String userId, String orgId, String workflowId) {
        return findWorkflow(userId, orgId, workflowId, "workflow");
    }

    @Override
    public AppRecord findWorkflow(String userId, String orgId, String workflowId, String appType) {
        return appMapper.selectWorkflowRecord(userId, orgId, workflowId, appType);
    }

    @Override
    public WorkflowDraftRecord findWorkflowDraft(String userId, String orgId, String workflowId) {
        WorkflowDraftEntity entity = workflowDraftMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WorkflowDraftEntity>()
                .eq("user_id", userId)
                .eq("org_id", orgId)
                .eq("workflow_id", workflowId)
                .last("LIMIT 1"));
        return toRecord(entity);
    }

    @Override
    @Transactional
    public boolean deleteWorkflow(String userId, String orgId, String workflowId) {
        return deleteWorkflow(userId, orgId, workflowId, "workflow");
    }

    @Override
    @Transactional
    public boolean deleteWorkflow(String userId, String orgId, String workflowId, String appType) {
        appUrlMapper.deleteByAssistant(userId, orgId, workflowId);
        workflowRunMapper.deleteByWorkflow(userId, orgId, workflowId);
        workflowSnapshotMapper.deleteByWorkflow(userId, orgId, workflowId);
        int draftDeleted = workflowDraftMapper.deleteDraft(userId, orgId, workflowId);
        int appDeleted = appMapper.deleteWorkflowApp(userId, orgId, workflowId, appType);
        return draftDeleted > 0 && appDeleted > 0;
    }

    @Override
    public List<String> listWorkflowNamesByPrefix(String userId, String orgId, String prefix) {
        return listWorkflowNamesByPrefix(userId, orgId, prefix, "workflow");
    }

    @Override
    public List<String> listWorkflowNamesByPrefix(String userId, String orgId, String prefix, String appType) {
        return appMapper.selectWorkflowNamesByPrefix(userId, orgId, prefix, appType);
    }

    @Override
    @Transactional
    public AppRecord copyWorkflow(AppRecord record, WorkflowDraftRecord draft) {
        saveWorkflow(record, draft);
        return record;
    }

    @Override
    public WorkflowSnapshotRecord saveWorkflowSnapshot(WorkflowSnapshotRecord snapshot) {
        WorkflowSnapshotEntity entity = toEntity(snapshot);
        workflowSnapshotMapper.insert(entity);
        snapshot.setId(entity.getId());
        return snapshot;
    }

    @Override
    public List<WorkflowSnapshotRecord> listWorkflowSnapshots(String userId, String orgId, String workflowId) {
        return toWorkflowSnapshotRecords(workflowSnapshotMapper.selectByWorkflow(userId, orgId, workflowId));
    }

    @Override
    public WorkflowSnapshotRecord findLatestWorkflowSnapshot(String userId, String orgId, String workflowId) {
        return toRecord(workflowSnapshotMapper.selectLatest(userId, orgId, workflowId));
    }

    @Override
    public WorkflowSnapshotRecord findWorkflowSnapshotByVersion(String userId, String orgId, String workflowId, String version) {
        return toRecord(workflowSnapshotMapper.selectByVersion(userId, orgId, workflowId, version));
    }

    @Override
    public boolean updateLatestWorkflowSnapshot(String userId, String orgId, String workflowId, String desc, long updatedAt) {
        WorkflowSnapshotEntity latest = workflowSnapshotMapper.selectLatest(userId, orgId, workflowId);
        if (latest == null) {
            return false;
        }
        return workflowSnapshotMapper.updateLatestDescription(latest.getId(), desc, updatedAt) > 0;
    }

    @Override
    public boolean updateWorkflowPublishType(String userId, String orgId, String workflowId, String publishType, long updatedAt) {
        return updateWorkflowPublishType(userId, orgId, workflowId, "workflow", publishType, updatedAt);
    }

    @Override
    public boolean updateWorkflowPublishType(String userId, String orgId, String workflowId, String appType, String publishType, long updatedAt) {
        return appMapper.updateWorkflowPublishType(userId, orgId, workflowId, appType, publishType, updatedAt) > 0;
    }

    @Override
    @Transactional
    public boolean rollbackWorkflow(AppRecord record, WorkflowDraftRecord draft) {
        appMapper.updateWorkflowUpdatedAt(record.getUserId(), record.getOrgId(), record.getAppId(), record.getAppType(), record.getUpdatedAt());
        int updated = workflowDraftMapper.updateDraft(record);
        if (updated <= 0) {
            return false;
        }
        WorkflowDraftEntity entity = workflowDraftMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<WorkflowDraftEntity>()
                .eq("user_id", record.getUserId())
                .eq("org_id", record.getOrgId())
                .eq("workflow_id", record.getAppId())
                .last("LIMIT 1"));
        if (entity != null) {
            entity.setSchemaJson(draft.getSchemaJson());
            entity.setUpdatedAt(draft.getUpdatedAt());
            workflowDraftMapper.updateById(entity);
        }
        return true;
    }

    @Override
    public WorkflowRunRecord saveWorkflowRun(WorkflowRunRecord record) {
        WorkflowRunEntity entity = toEntity(record);
        workflowRunMapper.insert(entity);
        record.setId(entity.getId());
        return record;
    }

    @Override
    public List<WorkflowRunRecord> listWorkflowRuns(String userId, String orgId, String workflowId, int limit) {
        int safeLimit = limit <= 0 ? 20 : Math.min(limit, 100);
        return toWorkflowRunRecords(workflowRunMapper.selectByWorkflow(userId, orgId, workflowId, safeLimit));
    }

    @Override
    public ApiKeyRecord saveApiKey(ApiKeyRecord record) {
        ApiKeyEntity entity = toEntity(record);
        apiKeyMapper.insert(entity);
        record.setId(entity.getId());
        return record;
    }

    @Override
    public ApiKeyRecord updateApiKey(ApiKeyRecord record) {
        int updated = apiKeyMapper.updateConfig(toEntity(record));
        return updated > 0 ? record : null;
    }

    @Override
    public ApiKeyRecord findApiKeyById(Long id) {
        return toRecord(apiKeyMapper.selectByIdValue(id));
    }

    @Override
    public ApiKeyRecord findApiKeyByKey(String key) {
        return toRecord(apiKeyMapper.selectByKey(key));
    }

    @Override
    public ApiKeyRecord findApiKeyByName(String userId, String orgId, String name) {
        return toRecord(apiKeyMapper.selectByScopedName(userId, orgId, name));
    }

    @Override
    public List<ApiKeyRecord> listApiKeys(String userId, String orgId, int offset, int limit) {
        return toApiKeyRecords(apiKeyMapper.selectPage(userId, orgId, offset, limit));
    }

    @Override
    public long countApiKeys(String userId, String orgId) {
        return apiKeyMapper.countByUser(userId, orgId);
    }

    @Override
    public boolean updateApiKeyStatus(Long id, boolean status, long updatedAt) {
        return apiKeyMapper.updateStatus(id, status, updatedAt) > 0;
    }

    @Override
    public boolean deleteApiKey(Long id) {
        return apiKeyMapper.deleteByIdValue(id) > 0;
    }

    @Override
    @Transactional
    public void recordApiKeyUsage(ApiKeyUsageRecord record, ApiKeyUsageAggregateRecord aggregate) {
        apiKeyUsageRecordMapper.insert(toEntity(record));
        apiKeyUsageAggregateMapper.upsertDelta(toEntity(aggregate));
    }

    @Override
    public ApiKeyUsageAggregateRecord sumApiKeyUsage(String userId,
                                                     String orgId,
                                                     String startDate,
                                                     String endDate,
                                                     List<String> apiKeyIds,
                                                     List<String> methodPaths) {
        return toRecord(apiKeyUsageAggregateMapper.selectSum(
                userId, orgId, startDate, endDate, apiKeyIds, methodPaths));
    }

    @Override
    public List<ApiKeyUsageAggregateRecord> listApiKeyUsageTrend(String userId,
                                                                 String orgId,
                                                                 String startDate,
                                                                 String endDate,
                                                                 List<String> apiKeyIds,
                                                                 List<String> methodPaths) {
        return toApiKeyUsageAggregateRecords(apiKeyUsageAggregateMapper.selectTrend(
                userId, orgId, startDate, endDate, apiKeyIds, methodPaths));
    }

    @Override
    public List<ApiKeyUsageAggregateRecord> listApiKeyUsageAggregates(String userId,
                                                                      String orgId,
                                                                      String startDate,
                                                                      String endDate,
                                                                      List<String> apiKeyIds,
                                                                      List<String> methodPaths,
                                                                      int offset,
                                                                      int limit) {
        return toApiKeyUsageAggregateRecords(apiKeyUsageAggregateMapper.selectGroupedPage(
                userId, orgId, startDate, endDate, apiKeyIds, methodPaths, offset, limit));
    }

    @Override
    public long countApiKeyUsageAggregates(String userId,
                                           String orgId,
                                           String startDate,
                                           String endDate,
                                           List<String> apiKeyIds,
                                           List<String> methodPaths) {
        return apiKeyUsageAggregateMapper.countGrouped(userId, orgId, startDate, endDate, apiKeyIds, methodPaths);
    }

    @Override
    public List<ApiKeyUsageRecord> listApiKeyUsageRecords(String userId,
                                                          String orgId,
                                                          String startDate,
                                                          String endDate,
                                                          List<String> apiKeyIds,
                                                          List<String> methodPaths,
                                                          int offset,
                                                          int limit) {
        return toApiKeyUsageRecords(apiKeyUsageRecordMapper.selectPage(
                userId, orgId, startDate, endDate, apiKeyIds, methodPaths, offset, limit));
    }

    @Override
    public long countApiKeyUsageRecords(String userId,
                                        String orgId,
                                        String startDate,
                                        String endDate,
                                        List<String> apiKeyIds,
                                        List<String> methodPaths) {
        return apiKeyUsageRecordMapper.countRecords(userId, orgId, startDate, endDate, apiKeyIds, methodPaths);
    }

    @Override
    public List<String> listApiKeyUsageMethodPaths(String userId, String orgId) {
        return apiKeyUsageAggregateMapper.selectMethodPaths(userId, orgId);
    }

    @Override
    public void recordAppStatistic(AppStatisticAggregateRecord aggregate) {
        appStatisticMapper.upsertDelta(toEntity(aggregate));
    }

    @Override
    public AppStatisticAggregateRecord sumAppStatistic(String userId,
                                                       String orgId,
                                                       String startDate,
                                                       String endDate,
                                                       List<String> appIds,
                                                       String appType) {
        return toRecord(appStatisticMapper.selectSum(userId, orgId, startDate, endDate, appIds, appType));
    }

    @Override
    public List<AppStatisticAggregateRecord> listAppStatisticTrend(String userId,
                                                                   String orgId,
                                                                   String startDate,
                                                                   String endDate,
                                                                   List<String> appIds,
                                                                   String appType) {
        return toAppStatisticRecords(appStatisticMapper.selectTrend(userId, orgId, startDate, endDate, appIds, appType));
    }

    @Override
    public List<AppStatisticAggregateRecord> listAppStatisticAggregates(String userId,
                                                                        String orgId,
                                                                        String startDate,
                                                                        String endDate,
                                                                        List<String> appIds,
                                                                        String appType,
                                                                        int offset,
                                                                        int limit) {
        return toAppStatisticRecords(appStatisticMapper.selectGroupedPage(
                userId, orgId, startDate, endDate, appIds, appType, offset, limit));
    }

    @Override
    public long countAppStatisticAggregates(String userId,
                                            String orgId,
                                            String startDate,
                                            String endDate,
                                            List<String> appIds,
                                            String appType) {
        return appStatisticMapper.countGrouped(userId, orgId, startDate, endDate, appIds, appType);
    }

    @Override
    public void recordModelStatistic(ModelStatisticAggregateRecord aggregate) {
        modelStatisticMapper.upsertDelta(toEntity(aggregate));
    }

    @Override
    public ModelStatisticAggregateRecord sumModelStatistic(String userId,
                                                           String orgId,
                                                           String startDate,
                                                           String endDate,
                                                           List<String> modelIds,
                                                           String modelType) {
        return toRecord(modelStatisticMapper.selectSum(userId, orgId, startDate, endDate, modelIds, modelType));
    }

    @Override
    public List<ModelStatisticAggregateRecord> listModelStatisticTrend(String userId,
                                                                       String orgId,
                                                                       String startDate,
                                                                       String endDate,
                                                                       List<String> modelIds,
                                                                       String modelType) {
        return toModelStatisticRecords(modelStatisticMapper.selectTrend(
                userId, orgId, startDate, endDate, modelIds, modelType));
    }

    @Override
    public List<ModelStatisticAggregateRecord> listModelStatisticAggregates(String userId,
                                                                           String orgId,
                                                                           String startDate,
                                                                           String endDate,
                                                                           List<String> modelIds,
                                                                           String modelType,
                                                                           int offset,
                                                                           int limit) {
        return toModelStatisticRecords(modelStatisticMapper.selectGroupedPage(
                userId, orgId, startDate, endDate, modelIds, modelType, offset, limit));
    }

    @Override
    public long countModelStatisticAggregates(String userId,
                                              String orgId,
                                              String startDate,
                                              String endDate,
                                              List<String> modelIds,
                                              String modelType) {
        return modelStatisticMapper.countGrouped(userId, orgId, startDate, endDate, modelIds, modelType);
    }

    @Override
    public AppKeyRecord saveAppKey(AppKeyRecord record) {
        AppKeyEntity entity = toEntity(record);
        appKeyMapper.insert(entity);
        record.setId(entity.getId());
        return record;
    }

    @Override
    public List<AppKeyRecord> listAppKeys(String userId, String orgId, String appId, String appType) {
        return toAppKeyRecords(appKeyMapper.selectByApp(userId, orgId, appId, appType));
    }

    @Override
    public AppKeyRecord findAppKeyById(Long id) {
        return toRecord(appKeyMapper.selectByIdValue(id));
    }

    @Override
    public AppKeyRecord findAppKeyByKey(String apiKey) {
        return toRecord(appKeyMapper.selectByApiKey(apiKey));
    }

    @Override
    public boolean deleteAppKey(Long id) {
        return appKeyMapper.deleteByIdValue(id) > 0;
    }

    @Override
    public AppUrlRecord saveAppUrl(AppUrlRecord record) {
        AppUrlEntity entity = toEntity(record);
        appUrlMapper.insert(entity);
        record.setId(entity.getId());
        return record;
    }

    @Override
    public AppUrlRecord updateAppUrl(AppUrlRecord record) {
        int updated = appUrlMapper.updateConfig(toEntity(record));
        return updated > 0 ? record : null;
    }

    @Override
    public AppUrlRecord findAppUrlById(String userId, String orgId, Long id) {
        return toRecord(appUrlMapper.selectByScopedId(userId, orgId, id));
    }

    @Override
    public AppUrlRecord findAppUrlBySuffix(String suffix) {
        return toRecord(appUrlMapper.selectBySuffix(suffix));
    }

    @Override
    public AppUrlRecord findAppUrlByName(String userId, String orgId, String appId, String appType, String name) {
        return toRecord(appUrlMapper.selectByName(userId, orgId, appId, appType, name));
    }

    @Override
    public List<AppUrlRecord> listAppUrls(String userId, String orgId, String appId, String appType) {
        return toAppUrlRecords(appUrlMapper.selectByApp(userId, orgId, appId, appType));
    }

    @Override
    public boolean updateAppUrlStatus(String userId, String orgId, Long id, boolean status, long updatedAt) {
        return appUrlMapper.updateStatus(userId, orgId, id, status, updatedAt) > 0;
    }

    @Override
    public boolean deleteAppUrl(String userId, String orgId, Long id) {
        return appUrlMapper.deleteByScopedId(userId, orgId, id) > 0;
    }

    @Override
    public AssistantConversationRecord saveConversation(AssistantConversationRecord record) {
        AssistantConversationEntity entity = toEntity(record);
        assistantConversationMapper.insert(entity);
        record.setId(entity.getId());
        return record;
    }

    @Override
    public AssistantConversationRecord findConversation(String userId, String orgId, String conversationId) {
        return toRecord(assistantConversationMapper.selectByConversationId(userId, orgId, conversationId));
    }

    @Override
    public AssistantConversationRecord findDraftConversation(String userId, String orgId, String assistantId) {
        return toRecord(assistantConversationMapper.selectDraftByAssistant(userId, orgId, assistantId));
    }

    @Override
    public List<AssistantConversationRecord> listConversations(String userId,
                                                               String orgId,
                                                               String assistantId,
                                                               String conversationType,
                                                               int offset,
                                                               int limit) {
        return toConversationRecords(assistantConversationMapper.selectPage(
                userId, orgId, assistantId, conversationType, offset, limit));
    }

    @Override
    public long countConversations(String userId, String orgId, String assistantId, String conversationType) {
        return assistantConversationMapper.countByAssistant(userId, orgId, assistantId, conversationType);
    }

    @Override
    public boolean touchConversation(String userId, String orgId, String conversationId, long updatedAt) {
        return assistantConversationMapper.touch(userId, orgId, conversationId, updatedAt) > 0;
    }

    @Override
    @Transactional
    public boolean deleteConversation(String userId, String orgId, String conversationId) {
        assistantConversationMessageMapper.deleteByConversation(userId, orgId, conversationId);
        return assistantConversationMapper.deleteByConversationId(userId, orgId, conversationId) > 0;
    }

    @Override
    public AssistantConversationMessageRecord saveConversationMessage(AssistantConversationMessageRecord record) {
        AssistantConversationMessageEntity entity = toEntity(record);
        assistantConversationMessageMapper.insert(entity);
        record.setId(entity.getId());
        return record;
    }

    @Override
    public List<AssistantConversationMessageRecord> listConversationMessages(String userId,
                                                                             String orgId,
                                                                             String conversationId,
                                                                             int offset,
                                                                             int limit) {
        return toMessageRecords(assistantConversationMessageMapper.selectPage(
                userId, orgId, conversationId, offset, limit));
    }

    @Override
    public long countConversationMessages(String userId, String orgId, String conversationId) {
        return assistantConversationMessageMapper.countByConversation(userId, orgId, conversationId);
    }

    @Override
    public boolean deleteConversationMessage(String userId, String orgId, String conversationId, String detailId) {
        return assistantConversationMessageMapper.deleteDetail(userId, orgId, conversationId, detailId) > 0;
    }

    @Override
    public boolean deleteConversationMessages(String userId, String orgId, String conversationId) {
        return assistantConversationMessageMapper.deleteByConversation(userId, orgId, conversationId) > 0;
    }

    @Override
    public AssistantKnowledgeFileRecord saveAssistantKnowledgeFile(AssistantKnowledgeFileRecord record) {
        AssistantKnowledgeFileEntity entity = toEntity(record);
        assistantKnowledgeFileMapper.insert(entity);
        record.setId(entity.getId());
        return record;
    }

    @Override
    public List<AssistantKnowledgeFileRecord> listAssistantKnowledgeFiles(String userId,
                                                                          String orgId,
                                                                          String assistantId) {
        return toAssistantKnowledgeFileRecords(
                assistantKnowledgeFileMapper.selectByAssistant(userId, orgId, assistantId));
    }

    @Override
    public long countAssistantKnowledgeFiles(String userId, String orgId, String assistantId) {
        return assistantKnowledgeFileMapper.countByAssistant(userId, orgId, assistantId);
    }

    @Override
    public boolean deleteAssistantKnowledgeFile(String userId, String orgId, String assistantId, String fileId) {
        return assistantKnowledgeFileMapper.deleteByAssistantFile(userId, orgId, assistantId, fileId) > 0;
    }

    @Override
    public boolean deleteAssistantKnowledgeFile(String userId, String orgId, String fileId) {
        return assistantKnowledgeFileMapper.deleteByFile(userId, orgId, fileId) > 0;
    }

    @Override
    public boolean deleteAssistantKnowledgeFiles(String userId, String orgId, String assistantId) {
        return assistantKnowledgeFileMapper.deleteByAssistant(userId, orgId, assistantId) > 0;
    }

    @Override
    public AssistantActionRecord saveAssistantAction(AssistantActionRecord record) {
        AssistantActionEntity entity = toEntity(record);
        AssistantActionEntity existing = assistantActionMapper.selectByAction(
                record.getUserId(), record.getOrgId(), record.getActionId());
        if (existing == null) {
            assistantActionMapper.insert(entity);
        } else {
            entity.setId(existing.getId());
            entity.setCreatedAt(existing.getCreatedAt());
            assistantActionMapper.updateByAction(entity);
        }
        record.setId(entity.getId());
        return record;
    }

    @Override
    public AssistantActionRecord findAssistantAction(String userId, String orgId, String actionId) {
        return toRecord(assistantActionMapper.selectByAction(userId, orgId, actionId));
    }

    @Override
    public List<AssistantActionRecord> listAssistantActions(String userId, String orgId, String assistantId) {
        return toAssistantActionRecords(assistantActionMapper.selectByAssistant(userId, orgId, assistantId));
    }

    @Override
    public boolean deleteAssistantAction(String userId, String orgId, String actionId) {
        return assistantActionMapper.deleteByAction(userId, orgId, actionId) > 0;
    }

    @Override
    public boolean deleteAssistantActions(String userId, String orgId, String assistantId) {
        return assistantActionMapper.deleteByAssistant(userId, orgId, assistantId) > 0;
    }

    @Override
    public GeneralAgentConfigRecord saveGeneralAgentConfig(GeneralAgentConfigRecord record) {
        GeneralAgentConfigEntity entity = toEntity(record);
        GeneralAgentConfigEntity existing = generalAgentConfigMapper.selectByScope(
                record.getUserId(), record.getOrgId());
        if (existing == null) {
            generalAgentConfigMapper.insert(entity);
        } else {
            entity.setId(existing.getId());
            entity.setCreatedAt(existing.getCreatedAt());
            generalAgentConfigMapper.updateByScope(entity);
        }
        record.setId(entity.getId());
        return record;
    }

    @Override
    public GeneralAgentConfigRecord findGeneralAgentConfig(String userId, String orgId) {
        return toRecord(generalAgentConfigMapper.selectByScope(userId, orgId));
    }

    @Override
    public GeneralAgentConversationRecord saveGeneralAgentConversation(GeneralAgentConversationRecord record) {
        GeneralAgentConversationEntity entity = toEntity(record);
        GeneralAgentConversationEntity existing = generalAgentConversationMapper.selectByThread(
                record.getUserId(), record.getOrgId(), record.getThreadId());
        if (existing == null) {
            generalAgentConversationMapper.insert(entity);
        } else {
            entity.setId(existing.getId());
            entity.setCreatedAt(existing.getCreatedAt());
            generalAgentConversationMapper.updateByThread(entity);
        }
        record.setId(entity.getId());
        return record;
    }

    @Override
    public GeneralAgentConversationRecord findGeneralAgentConversation(String userId, String orgId, String threadId) {
        return toRecord(generalAgentConversationMapper.selectByThread(userId, orgId, threadId));
    }

    @Override
    public GeneralAgentConversationRecord findGeneralAgentConversationByPreview(String userId,
                                                                               String orgId,
                                                                               String previewId) {
        return toRecord(generalAgentConversationMapper.selectByPreview(userId, orgId, previewId));
    }

    @Override
    public List<GeneralAgentConversationRecord> listGeneralAgentConversations(String userId, String orgId) {
        return toGeneralAgentConversationRecords(generalAgentConversationMapper.selectByScope(userId, orgId));
    }

    @Override
    public boolean deleteGeneralAgentConversation(String userId, String orgId, String threadId) {
        return generalAgentConversationMapper.deleteByThread(userId, orgId, threadId) > 0;
    }

    private AssistantDraftConfigEntity newDefaultConfig(AppRecord record) {
        AssistantDraftConfigEntity entity = new AssistantDraftConfigEntity();
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setAssistantId(record.getAppId());
        entity.setPrologue("");
        entity.setInstructions("");
        entity.setWorkflowInfosJson("[]");
        entity.setMcpInfosJson("[]");
        entity.setToolInfosJson("[]");
        entity.setSkillInfosJson("[]");
        entity.setMultiAgentInfosJson("[]");
        return entity;
    }

    private AssistantDraftConfigEntity toEntity(AssistantDraftConfigRecord record) {
        AssistantDraftConfigEntity entity = new AssistantDraftConfigEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setAssistantId(record.getAssistantId());
        entity.setPrologue(record.getPrologue());
        entity.setInstructions(record.getInstructions());
        entity.setMemoryConfigJson(record.getMemoryConfigJson());
        entity.setKnowledgeBaseConfigJson(record.getKnowledgeBaseConfigJson());
        entity.setModelConfigJson(record.getModelConfigJson());
        entity.setSafetyConfigJson(record.getSafetyConfigJson());
        entity.setVisionConfigJson(record.getVisionConfigJson());
        entity.setRerankConfigJson(record.getRerankConfigJson());
        entity.setRecommendConfigJson(record.getRecommendConfigJson());
        entity.setRecommendQuestionsJson(record.getRecommendQuestionsJson());
        entity.setWorkflowInfosJson(record.getWorkflowInfosJson());
        entity.setMcpInfosJson(record.getMcpInfosJson());
        entity.setToolInfosJson(record.getToolInfosJson());
        entity.setSkillInfosJson(record.getSkillInfosJson());
        entity.setMultiAgentInfosJson(record.getMultiAgentInfosJson());
        return entity;
    }

    private AssistantDraftConfigRecord toRecord(AssistantDraftConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        AssistantDraftConfigRecord record = new AssistantDraftConfigRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setAssistantId(entity.getAssistantId());
        record.setPrologue(entity.getPrologue());
        record.setInstructions(entity.getInstructions());
        record.setMemoryConfigJson(entity.getMemoryConfigJson());
        record.setKnowledgeBaseConfigJson(entity.getKnowledgeBaseConfigJson());
        record.setModelConfigJson(entity.getModelConfigJson());
        record.setSafetyConfigJson(entity.getSafetyConfigJson());
        record.setVisionConfigJson(entity.getVisionConfigJson());
        record.setRerankConfigJson(entity.getRerankConfigJson());
        record.setRecommendConfigJson(entity.getRecommendConfigJson());
        record.setRecommendQuestionsJson(entity.getRecommendQuestionsJson());
        record.setWorkflowInfosJson(entity.getWorkflowInfosJson());
        record.setMcpInfosJson(entity.getMcpInfosJson());
        record.setToolInfosJson(entity.getToolInfosJson());
        record.setSkillInfosJson(entity.getSkillInfosJson());
        record.setMultiAgentInfosJson(entity.getMultiAgentInfosJson());
        return record;
    }

    private RagDraftConfigEntity newDefaultRagConfig(AppRecord record) {
        RagDraftConfigEntity entity = new RagDraftConfigEntity();
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setRagId(record.getAppId());
        return entity;
    }

    private RagDraftConfigEntity toEntity(RagDraftConfigRecord record) {
        RagDraftConfigEntity entity = new RagDraftConfigEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setRagId(record.getRagId());
        entity.setModelConfigJson(record.getModelConfigJson());
        entity.setRerankConfigJson(record.getRerankConfigJson());
        entity.setQaRerankConfigJson(record.getQaRerankConfigJson());
        entity.setKnowledgeBaseConfigJson(record.getKnowledgeBaseConfigJson());
        entity.setQaKnowledgeBaseConfigJson(record.getQaKnowledgeBaseConfigJson());
        entity.setSafetyConfigJson(record.getSafetyConfigJson());
        entity.setVisionConfigJson(record.getVisionConfigJson());
        return entity;
    }

    private RagDraftConfigRecord toRecord(RagDraftConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        RagDraftConfigRecord record = new RagDraftConfigRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setRagId(entity.getRagId());
        record.setModelConfigJson(entity.getModelConfigJson());
        record.setRerankConfigJson(entity.getRerankConfigJson());
        record.setQaRerankConfigJson(entity.getQaRerankConfigJson());
        record.setKnowledgeBaseConfigJson(entity.getKnowledgeBaseConfigJson());
        record.setQaKnowledgeBaseConfigJson(entity.getQaKnowledgeBaseConfigJson());
        record.setSafetyConfigJson(entity.getSafetyConfigJson());
        record.setVisionConfigJson(entity.getVisionConfigJson());
        return record;
    }

    private AssistantSnapshotEntity toEntity(AssistantSnapshotRecord record) {
        AssistantSnapshotEntity entity = new AssistantSnapshotEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setAssistantId(record.getAssistantId());
        entity.setVersion(record.getVersion());
        entity.setDesc(record.getDesc());
        entity.setCategory(record.getCategory());
        entity.setAssistantInfoJson(record.getAssistantInfoJson());
        entity.setAssistantConfigJson(record.getAssistantConfigJson());
        return entity;
    }

    private List<AssistantSnapshotRecord> toSnapshotRecords(List<AssistantSnapshotEntity> entities) {
        List<AssistantSnapshotRecord> records = new java.util.ArrayList<>();
        for (AssistantSnapshotEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private AssistantSnapshotRecord toRecord(AssistantSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }
        AssistantSnapshotRecord record = new AssistantSnapshotRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setAssistantId(entity.getAssistantId());
        record.setVersion(entity.getVersion());
        record.setDesc(entity.getDesc());
        record.setCategory(entity.getCategory());
        record.setAssistantInfoJson(entity.getAssistantInfoJson());
        record.setAssistantConfigJson(entity.getAssistantConfigJson());
        return record;
    }

    private RagSnapshotEntity toEntity(RagSnapshotRecord record) {
        RagSnapshotEntity entity = new RagSnapshotEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setRagId(record.getRagId());
        entity.setVersion(record.getVersion());
        entity.setDesc(record.getDesc());
        entity.setCategory(record.getCategory());
        entity.setRagInfoJson(record.getRagInfoJson());
        entity.setRagConfigJson(record.getRagConfigJson());
        return entity;
    }

    private List<RagSnapshotRecord> toRagSnapshotRecords(List<RagSnapshotEntity> entities) {
        List<RagSnapshotRecord> records = new java.util.ArrayList<>();
        for (RagSnapshotEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private RagSnapshotRecord toRecord(RagSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }
        RagSnapshotRecord record = new RagSnapshotRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setRagId(entity.getRagId());
        record.setVersion(entity.getVersion());
        record.setDesc(entity.getDesc());
        record.setCategory(entity.getCategory());
        record.setRagInfoJson(entity.getRagInfoJson());
        record.setRagConfigJson(entity.getRagConfigJson());
        return record;
    }

    private WorkflowDraftEntity toWorkflowDraftEntity(AppRecord record, WorkflowDraftRecord draftRecord) {
        WorkflowDraftEntity entity = new WorkflowDraftEntity();
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setWorkflowId(record.getAppId());
        entity.setName(record.getName());
        entity.setDescription(record.getDesc());
        entity.setAvatarKey(record.getAvatarKey());
        entity.setAvatarPath(record.getAvatarPath());
        entity.setCategory(record.getCategory());
        entity.setSchemaJson(draftRecord == null ? null : draftRecord.getSchemaJson());
        return entity;
    }

    private WorkflowDraftRecord toRecord(WorkflowDraftEntity entity) {
        if (entity == null) {
            return null;
        }
        WorkflowDraftRecord record = new WorkflowDraftRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setWorkflowId(entity.getWorkflowId());
        record.setSchemaJson(entity.getSchemaJson());
        return record;
    }

    private WorkflowSnapshotEntity toEntity(WorkflowSnapshotRecord record) {
        WorkflowSnapshotEntity entity = new WorkflowSnapshotEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setWorkflowId(record.getWorkflowId());
        entity.setVersion(record.getVersion());
        entity.setDesc(record.getDesc());
        entity.setCategory(record.getCategory());
        entity.setWorkflowInfoJson(record.getWorkflowInfoJson());
        entity.setWorkflowSchemaJson(record.getWorkflowSchemaJson());
        return entity;
    }

    private List<WorkflowSnapshotRecord> toWorkflowSnapshotRecords(List<WorkflowSnapshotEntity> entities) {
        List<WorkflowSnapshotRecord> records = new java.util.ArrayList<>();
        for (WorkflowSnapshotEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private WorkflowSnapshotRecord toRecord(WorkflowSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }
        WorkflowSnapshotRecord record = new WorkflowSnapshotRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setWorkflowId(entity.getWorkflowId());
        record.setVersion(entity.getVersion());
        record.setDesc(entity.getDesc());
        record.setCategory(entity.getCategory());
        record.setWorkflowInfoJson(entity.getWorkflowInfoJson());
        record.setWorkflowSchemaJson(entity.getWorkflowSchemaJson());
        return record;
    }

    private WorkflowRunEntity toEntity(WorkflowRunRecord record) {
        WorkflowRunEntity entity = new WorkflowRunEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setFinishedAt(record.getFinishedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setWorkflowId(record.getWorkflowId());
        entity.setRunId(record.getRunId());
        entity.setStatus(record.getStatus());
        entity.setInputJson(record.getInputJson());
        entity.setOutputJson(record.getOutputJson());
        entity.setCostMillis(record.getCostMillis());
        return entity;
    }

    private List<WorkflowRunRecord> toWorkflowRunRecords(List<WorkflowRunEntity> entities) {
        List<WorkflowRunRecord> records = new java.util.ArrayList<>();
        for (WorkflowRunEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private WorkflowRunRecord toRecord(WorkflowRunEntity entity) {
        if (entity == null) {
            return null;
        }
        WorkflowRunRecord record = new WorkflowRunRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setFinishedAt(entity.getFinishedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setWorkflowId(entity.getWorkflowId());
        record.setRunId(entity.getRunId());
        record.setStatus(entity.getStatus());
        record.setInputJson(entity.getInputJson());
        record.setOutputJson(entity.getOutputJson());
        record.setCostMillis(entity.getCostMillis());
        return record;
    }

    private AppFavoriteEntity toEntity(AppFavoriteRecord record) {
        AppFavoriteEntity entity = new AppFavoriteEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setAppId(record.getAppId());
        entity.setAppType(record.getAppType());
        return entity;
    }

    private List<AppFavoriteRecord> toAppFavoriteRecords(List<AppFavoriteEntity> entities) {
        List<AppFavoriteRecord> records = new java.util.ArrayList<>();
        for (AppFavoriteEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private AppFavoriteRecord toRecord(AppFavoriteEntity entity) {
        if (entity == null) {
            return null;
        }
        AppFavoriteRecord record = new AppFavoriteRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setAppId(entity.getAppId());
        record.setAppType(entity.getAppType());
        return record;
    }

    private AppHistoryEntity toEntity(AppHistoryRecord record) {
        AppHistoryEntity entity = new AppHistoryEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setAppId(record.getAppId());
        entity.setAppType(record.getAppType());
        return entity;
    }

    private List<AppHistoryRecord> toAppHistoryRecords(List<AppHistoryEntity> entities) {
        List<AppHistoryRecord> records = new java.util.ArrayList<>();
        for (AppHistoryEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private AppHistoryRecord toRecord(AppHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        AppHistoryRecord record = new AppHistoryRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setAppId(entity.getAppId());
        record.setAppType(entity.getAppType());
        return record;
    }

    private ApiKeyEntity toEntity(ApiKeyRecord record) {
        ApiKeyEntity entity = new ApiKeyEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setOrgId(record.getOrgId());
        entity.setUserId(record.getUserId());
        entity.setKey(record.getKey());
        entity.setDescription(record.getDescription());
        entity.setName(record.getName());
        entity.setStatus(record.getStatus());
        entity.setExpiredAt(record.getExpiredAt());
        return entity;
    }

    private List<ApiKeyRecord> toApiKeyRecords(List<ApiKeyEntity> entities) {
        List<ApiKeyRecord> records = new java.util.ArrayList<>();
        for (ApiKeyEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private ApiKeyRecord toRecord(ApiKeyEntity entity) {
        if (entity == null) {
            return null;
        }
        ApiKeyRecord record = new ApiKeyRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setOrgId(entity.getOrgId());
        record.setUserId(entity.getUserId());
        record.setKey(entity.getKey());
        record.setDescription(entity.getDescription());
        record.setName(entity.getName());
        record.setStatus(entity.getStatus());
        record.setExpiredAt(entity.getExpiredAt());
        return record;
    }

    private ApiKeyUsageAggregateEntity toEntity(ApiKeyUsageAggregateRecord record) {
        ApiKeyUsageAggregateEntity entity = new ApiKeyUsageAggregateEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setOrgId(record.getOrgId());
        entity.setUserId(record.getUserId());
        entity.setApiKeyId(record.getApiKeyId());
        entity.setMethodPath(record.getMethodPath());
        entity.setDate(record.getDate());
        entity.setCallCount(record.getCallCount());
        entity.setCallFailure(record.getCallFailure());
        entity.setStreamCount(record.getStreamCount());
        entity.setNonStreamCount(record.getNonStreamCount());
        entity.setStreamFailure(record.getStreamFailure());
        entity.setNonStreamFailure(record.getNonStreamFailure());
        entity.setStreamCosts(record.getStreamCosts());
        entity.setNonStreamCosts(record.getNonStreamCosts());
        return entity;
    }

    private List<ApiKeyUsageAggregateRecord> toApiKeyUsageAggregateRecords(List<ApiKeyUsageAggregateEntity> entities) {
        List<ApiKeyUsageAggregateRecord> records = new java.util.ArrayList<>();
        for (ApiKeyUsageAggregateEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private ApiKeyUsageAggregateRecord toRecord(ApiKeyUsageAggregateEntity entity) {
        ApiKeyUsageAggregateRecord record = new ApiKeyUsageAggregateRecord();
        if (entity == null) {
            return record;
        }
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setOrgId(entity.getOrgId());
        record.setUserId(entity.getUserId());
        record.setApiKeyId(entity.getApiKeyId());
        record.setMethodPath(entity.getMethodPath());
        record.setDate(entity.getDate());
        record.setCallCount(value(entity.getCallCount()));
        record.setCallFailure(value(entity.getCallFailure()));
        record.setStreamCount(value(entity.getStreamCount()));
        record.setNonStreamCount(value(entity.getNonStreamCount()));
        record.setStreamFailure(value(entity.getStreamFailure()));
        record.setNonStreamFailure(value(entity.getNonStreamFailure()));
        record.setStreamCosts(value(entity.getStreamCosts()));
        record.setNonStreamCosts(value(entity.getNonStreamCosts()));
        return record;
    }

    private ApiKeyUsageRecordEntity toEntity(ApiKeyUsageRecord record) {
        ApiKeyUsageRecordEntity entity = new ApiKeyUsageRecordEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setOrgId(record.getOrgId());
        entity.setUserId(record.getUserId());
        entity.setApiKeyId(record.getApiKeyId());
        entity.setMethodPath(record.getMethodPath());
        entity.setCallTime(record.getCallTime());
        entity.setResponseStatus(record.getResponseStatus());
        entity.setStream(record.isStream());
        entity.setStreamCosts(record.getStreamCosts());
        entity.setNonStreamCosts(record.getNonStreamCosts());
        entity.setRequestBody(record.getRequestBody());
        entity.setResponseBody(record.getResponseBody());
        entity.setDate(record.getDate());
        return entity;
    }

    private List<ApiKeyUsageRecord> toApiKeyUsageRecords(List<ApiKeyUsageRecordEntity> entities) {
        List<ApiKeyUsageRecord> records = new java.util.ArrayList<>();
        for (ApiKeyUsageRecordEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private ApiKeyUsageRecord toRecord(ApiKeyUsageRecordEntity entity) {
        ApiKeyUsageRecord record = new ApiKeyUsageRecord();
        if (entity == null) {
            return record;
        }
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setOrgId(entity.getOrgId());
        record.setUserId(entity.getUserId());
        record.setApiKeyId(entity.getApiKeyId());
        record.setMethodPath(entity.getMethodPath());
        record.setCallTime(value(entity.getCallTime()));
        record.setResponseStatus(entity.getResponseStatus());
        record.setStream(Boolean.TRUE.equals(entity.getStream()));
        record.setStreamCosts(value(entity.getStreamCosts()));
        record.setNonStreamCosts(value(entity.getNonStreamCosts()));
        record.setRequestBody(entity.getRequestBody());
        record.setResponseBody(entity.getResponseBody());
        record.setDate(entity.getDate());
        return record;
    }

    private AppStatisticEntity toEntity(AppStatisticAggregateRecord record) {
        AppStatisticEntity entity = new AppStatisticEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setOrgId(record.getOrgId());
        entity.setUserId(record.getUserId());
        entity.setAppId(record.getAppId());
        entity.setAppType(record.getAppType());
        entity.setDate(record.getDate());
        entity.setCallCount(record.getCallCount());
        entity.setCallFailure(record.getCallFailure());
        entity.setStreamCount(record.getStreamCount());
        entity.setStreamFailure(record.getStreamFailure());
        entity.setStreamCosts(record.getStreamCosts());
        entity.setNonStreamCount(record.getNonStreamCount());
        entity.setNonStreamFailure(record.getNonStreamFailure());
        entity.setNonStreamCosts(record.getNonStreamCosts());
        entity.setWebCallCount(record.getWebCallCount());
        entity.setWebCallFailure(record.getWebCallFailure());
        entity.setOpenapiCallCount(record.getOpenapiCallCount());
        entity.setOpenapiCallFailure(record.getOpenapiCallFailure());
        entity.setWebUrlCallCount(record.getWebUrlCallCount());
        entity.setWebUrlCallFailure(record.getWebUrlCallFailure());
        return entity;
    }

    private List<AppStatisticAggregateRecord> toAppStatisticRecords(List<AppStatisticEntity> entities) {
        List<AppStatisticAggregateRecord> records = new java.util.ArrayList<>();
        for (AppStatisticEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private AppStatisticAggregateRecord toRecord(AppStatisticEntity entity) {
        AppStatisticAggregateRecord record = new AppStatisticAggregateRecord();
        if (entity == null) {
            return record;
        }
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setOrgId(entity.getOrgId());
        record.setUserId(entity.getUserId());
        record.setAppId(entity.getAppId());
        record.setAppType(entity.getAppType());
        record.setDate(entity.getDate());
        record.setCallCount(value(entity.getCallCount()));
        record.setCallFailure(value(entity.getCallFailure()));
        record.setStreamCount(value(entity.getStreamCount()));
        record.setStreamFailure(value(entity.getStreamFailure()));
        record.setStreamCosts(value(entity.getStreamCosts()));
        record.setNonStreamCount(value(entity.getNonStreamCount()));
        record.setNonStreamFailure(value(entity.getNonStreamFailure()));
        record.setNonStreamCosts(value(entity.getNonStreamCosts()));
        record.setWebCallCount(value(entity.getWebCallCount()));
        record.setWebCallFailure(value(entity.getWebCallFailure()));
        record.setOpenapiCallCount(value(entity.getOpenapiCallCount()));
        record.setOpenapiCallFailure(value(entity.getOpenapiCallFailure()));
        record.setWebUrlCallCount(value(entity.getWebUrlCallCount()));
        record.setWebUrlCallFailure(value(entity.getWebUrlCallFailure()));
        return record;
    }

    private ModelStatisticEntity toEntity(ModelStatisticAggregateRecord record) {
        ModelStatisticEntity entity = new ModelStatisticEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setOrgId(record.getOrgId());
        entity.setUserId(record.getUserId());
        entity.setModelId(record.getModelId());
        entity.setModel(record.getModel());
        entity.setProvider(record.getProvider());
        entity.setModelType(record.getModelType());
        entity.setDate(record.getDate());
        entity.setPromptTokens(record.getPromptTokens());
        entity.setCompletionTokens(record.getCompletionTokens());
        entity.setTotalTokens(record.getTotalTokens());
        entity.setFirstTokenLatency(record.getFirstTokenLatency());
        entity.setCosts(record.getCosts());
        entity.setCallCount(record.getCallCount());
        entity.setStreamCount(record.getStreamCount());
        entity.setNonStreamCount(record.getNonStreamCount());
        entity.setCallFailure(record.getCallFailure());
        entity.setStreamFailure(record.getStreamFailure());
        entity.setNonStreamFailure(record.getNonStreamFailure());
        return entity;
    }

    private List<ModelStatisticAggregateRecord> toModelStatisticRecords(List<ModelStatisticEntity> entities) {
        List<ModelStatisticAggregateRecord> records = new java.util.ArrayList<>();
        for (ModelStatisticEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private ModelStatisticAggregateRecord toRecord(ModelStatisticEntity entity) {
        ModelStatisticAggregateRecord record = new ModelStatisticAggregateRecord();
        if (entity == null) {
            return record;
        }
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setOrgId(entity.getOrgId());
        record.setUserId(entity.getUserId());
        record.setModelId(entity.getModelId());
        record.setModel(entity.getModel());
        record.setProvider(entity.getProvider());
        record.setModelType(entity.getModelType());
        record.setDate(entity.getDate());
        record.setPromptTokens(value(entity.getPromptTokens()));
        record.setCompletionTokens(value(entity.getCompletionTokens()));
        record.setTotalTokens(value(entity.getTotalTokens()));
        record.setFirstTokenLatency(value(entity.getFirstTokenLatency()));
        record.setCosts(value(entity.getCosts()));
        record.setCallCount(value(entity.getCallCount()));
        record.setStreamCount(value(entity.getStreamCount()));
        record.setNonStreamCount(value(entity.getNonStreamCount()));
        record.setCallFailure(value(entity.getCallFailure()));
        record.setStreamFailure(value(entity.getStreamFailure()));
        record.setNonStreamFailure(value(entity.getNonStreamFailure()));
        return record;
    }

    private AppKeyEntity toEntity(AppKeyRecord record) {
        AppKeyEntity entity = new AppKeyEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setOrgId(record.getOrgId());
        entity.setUserId(record.getUserId());
        entity.setAppId(record.getAppId());
        entity.setAppType(record.getAppType());
        entity.setApiKey(record.getApiKey());
        return entity;
    }

    private List<AppKeyRecord> toAppKeyRecords(List<AppKeyEntity> entities) {
        List<AppKeyRecord> records = new java.util.ArrayList<>();
        for (AppKeyEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private AppKeyRecord toRecord(AppKeyEntity entity) {
        if (entity == null) {
            return null;
        }
        AppKeyRecord record = new AppKeyRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setOrgId(entity.getOrgId());
        record.setUserId(entity.getUserId());
        record.setAppId(entity.getAppId());
        record.setAppType(entity.getAppType());
        record.setApiKey(entity.getApiKey());
        return record;
    }

    private AppUrlEntity toEntity(AppUrlRecord record) {
        AppUrlEntity entity = new AppUrlEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setAppId(record.getAppId());
        entity.setAppType(record.getAppType());
        entity.setName(record.getName());
        entity.setDescription(record.getDescription());
        entity.setExpiredAt(record.getExpiredAt());
        entity.setCopyright(record.getCopyright());
        entity.setCopyrightEnable(record.getCopyrightEnable());
        entity.setPrivacyPolicy(record.getPrivacyPolicy());
        entity.setPrivacyPolicyEnable(record.getPrivacyPolicyEnable());
        entity.setDisclaimer(record.getDisclaimer());
        entity.setDisclaimerEnable(record.getDisclaimerEnable());
        entity.setSuffix(record.getSuffix());
        entity.setStatus(record.getStatus());
        return entity;
    }

    private List<AppUrlRecord> toAppUrlRecords(List<AppUrlEntity> entities) {
        List<AppUrlRecord> records = new java.util.ArrayList<>();
        for (AppUrlEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private AppUrlRecord toRecord(AppUrlEntity entity) {
        if (entity == null) {
            return null;
        }
        AppUrlRecord record = new AppUrlRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setAppId(entity.getAppId());
        record.setAppType(entity.getAppType());
        record.setName(entity.getName());
        record.setDescription(entity.getDescription());
        record.setExpiredAt(entity.getExpiredAt());
        record.setCopyright(entity.getCopyright());
        record.setCopyrightEnable(entity.getCopyrightEnable());
        record.setPrivacyPolicy(entity.getPrivacyPolicy());
        record.setPrivacyPolicyEnable(entity.getPrivacyPolicyEnable());
        record.setDisclaimer(entity.getDisclaimer());
        record.setDisclaimerEnable(entity.getDisclaimerEnable());
        record.setSuffix(entity.getSuffix());
        record.setStatus(entity.getStatus());
        return record;
    }

    private AssistantConversationEntity toEntity(AssistantConversationRecord record) {
        AssistantConversationEntity entity = new AssistantConversationEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setAssistantId(record.getAssistantId());
        entity.setConversationId(record.getConversationId());
        entity.setConversationType(record.getConversationType());
        entity.setTitle(record.getTitle());
        return entity;
    }

    private List<AssistantConversationRecord> toConversationRecords(List<AssistantConversationEntity> entities) {
        List<AssistantConversationRecord> records = new java.util.ArrayList<>();
        for (AssistantConversationEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private AssistantConversationRecord toRecord(AssistantConversationEntity entity) {
        if (entity == null) {
            return null;
        }
        AssistantConversationRecord record = new AssistantConversationRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setAssistantId(entity.getAssistantId());
        record.setConversationId(entity.getConversationId());
        record.setConversationType(entity.getConversationType());
        record.setTitle(entity.getTitle());
        return record;
    }

    private AssistantConversationMessageEntity toEntity(AssistantConversationMessageRecord record) {
        AssistantConversationMessageEntity entity = new AssistantConversationMessageEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setAssistantId(record.getAssistantId());
        entity.setConversationId(record.getConversationId());
        entity.setDetailId(record.getDetailId());
        entity.setPrompt(record.getPrompt());
        entity.setSysPrompt(record.getSysPrompt());
        entity.setResponse(record.getResponse());
        entity.setResponseListJson(record.getResponseListJson());
        entity.setSearchListJson(record.getSearchListJson());
        entity.setRequestFilesJson(record.getRequestFilesJson());
        entity.setResponseFilesJson(record.getResponseFilesJson());
        entity.setSubConversationListJson(record.getSubConversationListJson());
        entity.setFileSize(record.getFileSize());
        entity.setFileName(record.getFileName());
        entity.setQaType(record.getQaType());
        return entity;
    }

    private List<AssistantConversationMessageRecord> toMessageRecords(List<AssistantConversationMessageEntity> entities) {
        List<AssistantConversationMessageRecord> records = new java.util.ArrayList<>();
        for (AssistantConversationMessageEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private AssistantConversationMessageRecord toRecord(AssistantConversationMessageEntity entity) {
        if (entity == null) {
            return null;
        }
        AssistantConversationMessageRecord record = new AssistantConversationMessageRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setAssistantId(entity.getAssistantId());
        record.setConversationId(entity.getConversationId());
        record.setDetailId(entity.getDetailId());
        record.setPrompt(entity.getPrompt());
        record.setSysPrompt(entity.getSysPrompt());
        record.setResponse(entity.getResponse());
        record.setResponseListJson(entity.getResponseListJson());
        record.setSearchListJson(entity.getSearchListJson());
        record.setRequestFilesJson(entity.getRequestFilesJson());
        record.setResponseFilesJson(entity.getResponseFilesJson());
        record.setSubConversationListJson(entity.getSubConversationListJson());
        record.setFileSize(entity.getFileSize());
        record.setFileName(entity.getFileName());
        record.setQaType(entity.getQaType());
        return record;
    }

    private AssistantKnowledgeFileEntity toEntity(AssistantKnowledgeFileRecord record) {
        AssistantKnowledgeFileEntity entity = new AssistantKnowledgeFileEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setAssistantId(record.getAssistantId());
        entity.setFileId(record.getFileId());
        entity.setFileName(record.getFileName());
        entity.setFileSize(record.getFileSize());
        entity.setContentType(record.getContentType());
        entity.setStatus(record.getStatus());
        entity.setUrl(record.getUrl());
        return entity;
    }

    private List<AssistantKnowledgeFileRecord> toAssistantKnowledgeFileRecords(
            List<AssistantKnowledgeFileEntity> entities) {
        List<AssistantKnowledgeFileRecord> records = new java.util.ArrayList<>();
        for (AssistantKnowledgeFileEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private AssistantKnowledgeFileRecord toRecord(AssistantKnowledgeFileEntity entity) {
        if (entity == null) {
            return null;
        }
        AssistantKnowledgeFileRecord record = new AssistantKnowledgeFileRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setAssistantId(entity.getAssistantId());
        record.setFileId(entity.getFileId());
        record.setFileName(entity.getFileName());
        record.setFileSize(entity.getFileSize());
        record.setContentType(entity.getContentType());
        record.setStatus(entity.getStatus());
        record.setUrl(entity.getUrl());
        return record;
    }

    private AssistantActionEntity toEntity(AssistantActionRecord record) {
        AssistantActionEntity entity = new AssistantActionEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setAssistantId(record.getAssistantId());
        entity.setActionId(record.getActionId());
        entity.setName(record.getName());
        entity.setPayload(record.getPayloadJson());
        return entity;
    }

    private AssistantActionRecord toRecord(AssistantActionEntity entity) {
        if (entity == null) {
            return null;
        }
        AssistantActionRecord record = new AssistantActionRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setAssistantId(entity.getAssistantId());
        record.setActionId(entity.getActionId());
        record.setName(entity.getName());
        record.setPayloadJson(entity.getPayload());
        return record;
    }

    private List<AssistantActionRecord> toAssistantActionRecords(List<AssistantActionEntity> entities) {
        List<AssistantActionRecord> records = new java.util.ArrayList<>();
        for (AssistantActionEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private GeneralAgentConfigEntity toEntity(GeneralAgentConfigRecord record) {
        GeneralAgentConfigEntity entity = new GeneralAgentConfigEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setConfigJson(record.getConfigJson());
        return entity;
    }

    private GeneralAgentConfigRecord toRecord(GeneralAgentConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        GeneralAgentConfigRecord record = new GeneralAgentConfigRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setConfigJson(entity.getConfigJson());
        return record;
    }

    private GeneralAgentConversationEntity toEntity(GeneralAgentConversationRecord record) {
        GeneralAgentConversationEntity entity = new GeneralAgentConversationEntity();
        entity.setId(record.getId());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setThreadId(record.getThreadId());
        entity.setTitle(record.getTitle());
        entity.setSkillConversation(record.getSkillConversation());
        entity.setSkillId(record.getSkillId());
        entity.setPreviewId(record.getPreviewId());
        entity.setModelConfigJson(record.getModelConfigJson());
        entity.setRunsJson(record.getRunsJson());
        return entity;
    }

    private GeneralAgentConversationRecord toRecord(GeneralAgentConversationEntity entity) {
        if (entity == null) {
            return null;
        }
        GeneralAgentConversationRecord record = new GeneralAgentConversationRecord();
        record.setId(entity.getId());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setUserId(entity.getUserId());
        record.setOrgId(entity.getOrgId());
        record.setThreadId(entity.getThreadId());
        record.setTitle(entity.getTitle());
        record.setSkillConversation(entity.getSkillConversation());
        record.setSkillId(entity.getSkillId());
        record.setPreviewId(entity.getPreviewId());
        record.setModelConfigJson(entity.getModelConfigJson());
        record.setRunsJson(entity.getRunsJson());
        return record;
    }

    private List<GeneralAgentConversationRecord> toGeneralAgentConversationRecords(
            List<GeneralAgentConversationEntity> entities) {
        List<GeneralAgentConversationRecord> records = new java.util.ArrayList<>();
        for (GeneralAgentConversationEntity entity : entities) {
            records.add(toRecord(entity));
        }
        return records;
    }

    private long value(Long value) {
        return value == null ? 0L : value;
    }
}
