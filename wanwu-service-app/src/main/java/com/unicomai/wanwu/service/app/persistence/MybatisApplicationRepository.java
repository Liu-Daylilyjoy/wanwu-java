package com.unicomai.wanwu.service.app.persistence;

import com.unicomai.wanwu.service.app.domain.AssistantDraftConfigRecord;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import com.unicomai.wanwu.service.app.persistence.entity.AppEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantDraftConfigEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantDraftEntity;
import com.unicomai.wanwu.service.app.persistence.mapper.AppMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantDraftConfigMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantDraftMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class MybatisApplicationRepository implements ApplicationRepository {

    private final AppMapper appMapper;
    private final AssistantDraftMapper assistantDraftMapper;
    private final AssistantDraftConfigMapper assistantDraftConfigMapper;

    public MybatisApplicationRepository(AppMapper appMapper,
                                        AssistantDraftMapper assistantDraftMapper,
                                        AssistantDraftConfigMapper assistantDraftConfigMapper) {
        this.appMapper = appMapper;
        this.assistantDraftMapper = assistantDraftMapper;
        this.assistantDraftConfigMapper = assistantDraftConfigMapper;
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
    @Transactional
    public boolean deleteAssistant(String userId, String orgId, String assistantId) {
        assistantDraftConfigMapper.deleteByAssistant(userId, orgId, assistantId);
        int draftDeleted = assistantDraftMapper.deleteDraft(userId, orgId, assistantId);
        int appDeleted = appMapper.deleteAssistantApp(userId, orgId, assistantId);
        return draftDeleted > 0 && appDeleted > 0;
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
}
