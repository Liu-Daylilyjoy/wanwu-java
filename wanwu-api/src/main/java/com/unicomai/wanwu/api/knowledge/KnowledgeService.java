package com.unicomai.wanwu.api.knowledge;

import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

public interface KnowledgeService {

    ServiceDescriptor describe();

    Map<String, Object> selectKnowledge(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> createKnowledge(String userId, String orgId, Map<String, Object> request);

    void updateKnowledge(String userId, String orgId, Map<String, Object> request);

    void deleteKnowledge(String userId, String orgId, Map<String, Object> request);

    void checkKnowledgeUserPermission(String userId, String orgId, String knowledgeId, int permissionType);

    Map<String, Object> hitKnowledge(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listKeywords(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getKeyword(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> createKeyword(String userId, String orgId, Map<String, Object> request);

    void updateKeyword(String userId, String orgId, Map<String, Object> request);

    void deleteKeyword(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listTags(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> createTag(String userId, String orgId, Map<String, Object> request);

    void updateTag(String userId, String orgId, Map<String, Object> request);

    void deleteTag(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> countTagBindings(String userId, String orgId, Map<String, Object> request);

    void bindTags(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listSplitters(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> createSplitter(String userId, String orgId, Map<String, Object> request);

    void updateSplitter(String userId, String orgId, Map<String, Object> request);

    void deleteSplitter(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listDocs(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getDocConfig(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getDocImportTip(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getDocUploadLimit(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listDocSegments(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listDocChildSegments(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> analyzeDocUrls(String userId, String orgId, Map<String, Object> request);

    void importDocs(String userId, String orgId, Map<String, Object> request);

    void updateDocConfig(String userId, String orgId, Map<String, Object> request);

    void reimportDocs(String userId, String orgId, Map<String, Object> request);

    void deleteDocs(String userId, String orgId, Map<String, Object> request);

    void updateDocMeta(String userId, String orgId, Map<String, Object> request);

    void batchUpdateDocMeta(String userId, String orgId, Map<String, Object> request);

    void updateMetaValues(String userId, String orgId, Map<String, Object> request);

    void updateCallbackDocStatus(String userId, String orgId, Map<String, Object> request);

    void initCallbackDocStatus(String userId, String orgId);

    void updateCallbackKnowledgeStatus(String userId, String orgId, Map<String, Object> request);

    void updateDocSegmentStatus(String userId, String orgId, Map<String, Object> request);

    void updateDocSegmentLabels(String userId, String orgId, Map<String, Object> request);

    void createDocSegment(String userId, String orgId, Map<String, Object> request);

    void batchCreateDocSegment(String userId, String orgId, Map<String, Object> request);

    void deleteDocSegment(String userId, String orgId, Map<String, Object> request);

    void updateDocSegment(String userId, String orgId, Map<String, Object> request);

    void createDocChildSegment(String userId, String orgId, Map<String, Object> request);

    void updateDocChildSegment(String userId, String orgId, Map<String, Object> request);

    void deleteDocChildSegment(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> selectMetaKeys(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listMetaValues(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getKnowledgeGraph(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listKnowledgeOrgs(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listKnowledgeUsers(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listUsersWithoutPermit(String userId, String orgId, Map<String, Object> request);

    void addKnowledgeUsers(String userId, String orgId, Map<String, Object> request);

    void editKnowledgeUser(String userId, String orgId, Map<String, Object> request);

    void deleteKnowledgeUser(String userId, String orgId, Map<String, Object> request);

    void transferKnowledgeAdmin(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listReports(String userId, String orgId, Map<String, Object> request);

    void generateReport(String userId, String orgId, Map<String, Object> request);

    void deleteReport(String userId, String orgId, Map<String, Object> request);

    void updateReport(String userId, String orgId, Map<String, Object> request);

    void addReport(String userId, String orgId, Map<String, Object> request);

    void batchAddReports(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listExternalApis(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> createExternalApi(String userId, String orgId, Map<String, Object> request);

    void updateExternalApi(String userId, String orgId, Map<String, Object> request);

    void deleteExternalApi(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listExternalKnowledge(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> createExternalKnowledge(String userId, String orgId, Map<String, Object> request);

    void updateExternalKnowledge(String userId, String orgId, Map<String, Object> request);

    void deleteExternalKnowledge(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listExportRecords(String userId, String orgId, Map<String, Object> request);

    void deleteExportRecord(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> exportDocs(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getExportRecordFile(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getDocByName(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> createQaPair(String userId, String orgId, Map<String, Object> request);

    void updateQaPair(String userId, String orgId, Map<String, Object> request);

    void updateQaPairSwitch(String userId, String orgId, Map<String, Object> request);

    void deleteQaPairs(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> listQaPairs(String userId, String orgId, Map<String, Object> request);

    void importQaPairs(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> getQaImportTip(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> exportQaPairs(String userId, String orgId, Map<String, Object> request);

    Map<String, Object> hitQaPairs(String userId, String orgId, Map<String, Object> request);
}
