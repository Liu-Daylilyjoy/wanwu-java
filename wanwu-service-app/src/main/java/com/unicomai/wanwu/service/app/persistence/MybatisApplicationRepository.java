package com.unicomai.wanwu.service.app.persistence;

import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationMessageRecord;
import com.unicomai.wanwu.service.app.domain.AssistantConversationRecord;
import com.unicomai.wanwu.service.app.domain.AssistantSnapshotRecord;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.AppUrlRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import com.unicomai.wanwu.service.app.persistence.entity.AppEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AppUrlEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantConversationEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantConversationMessageEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantDraftConfigEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantDraftEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantSnapshotEntity;
import com.unicomai.wanwu.service.app.persistence.mapper.AppMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AppUrlMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantConversationMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantConversationMessageMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantDraftConfigMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantDraftMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantSnapshotMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class MybatisApplicationRepository implements ApplicationRepository {

    private final AppMapper appMapper;
    private final AssistantDraftMapper assistantDraftMapper;
    private final AssistantDraftConfigMapper assistantDraftConfigMapper;
    private final AssistantSnapshotMapper assistantSnapshotMapper;
    private final AppUrlMapper appUrlMapper;
    private final AssistantConversationMapper assistantConversationMapper;
    private final AssistantConversationMessageMapper assistantConversationMessageMapper;

    public MybatisApplicationRepository(AppMapper appMapper,
                                        AssistantDraftMapper assistantDraftMapper,
                                        AssistantDraftConfigMapper assistantDraftConfigMapper,
                                        AssistantSnapshotMapper assistantSnapshotMapper,
                                        AppUrlMapper appUrlMapper,
                                        AssistantConversationMapper assistantConversationMapper,
                                        AssistantConversationMessageMapper assistantConversationMessageMapper) {
        this.appMapper = appMapper;
        this.assistantDraftMapper = assistantDraftMapper;
        this.assistantDraftConfigMapper = assistantDraftConfigMapper;
        this.assistantSnapshotMapper = assistantSnapshotMapper;
        this.appUrlMapper = appUrlMapper;
        this.assistantConversationMapper = assistantConversationMapper;
        this.assistantConversationMessageMapper = assistantConversationMessageMapper;
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

    private AssistantDraftConfigEntity newDefaultConfig(AppRecord record) {
        AssistantDraftConfigEntity entity = new AssistantDraftConfigEntity();
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setUserId(record.getUserId());
        entity.setOrgId(record.getOrgId());
        entity.setAssistantId(record.getAppId());
        entity.setPrologue("");
        entity.setInstructions("");
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
}
