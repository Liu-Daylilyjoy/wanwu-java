package com.unicomai.wanwu.service.app.persistence;

import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.domain.ApplicationRepository;
import com.unicomai.wanwu.service.app.persistence.entity.AppEntity;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantDraftEntity;
import com.unicomai.wanwu.service.app.persistence.mapper.AppMapper;
import com.unicomai.wanwu.service.app.persistence.mapper.AssistantDraftMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class MybatisApplicationRepository implements ApplicationRepository {

    private final AppMapper appMapper;
    private final AssistantDraftMapper assistantDraftMapper;

    public MybatisApplicationRepository(AppMapper appMapper, AssistantDraftMapper assistantDraftMapper) {
        this.appMapper = appMapper;
        this.assistantDraftMapper = assistantDraftMapper;
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

        record.setId(app.getId());
        return record;
    }

    @Override
    public List<AppRecord> listAssistants(String userId, String orgId, String name) {
        return appMapper.selectAssistantRecords(userId, orgId, name);
    }

    @Override
    public AppRecord findAssistant(String userId, String orgId, String assistantId) {
        return appMapper.selectAssistantRecord(userId, orgId, assistantId);
    }
}
