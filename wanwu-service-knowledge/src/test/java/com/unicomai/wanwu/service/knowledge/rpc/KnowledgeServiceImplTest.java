package com.unicomai.wanwu.service.knowledge.rpc;

import com.unicomai.wanwu.service.knowledge.persistence.entity.KnowledgeRecordEntity;
import com.unicomai.wanwu.service.knowledge.persistence.mapper.KnowledgeRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KnowledgeServiceImplTest {

    private KnowledgeServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new KnowledgeServiceImpl();
    }

    @Test
    public void knowledgeLifecycleFollowsFrontendAndGoContract() {
        Map<String, Object> created = service.createKnowledge("dev-admin", "default-org", createKnowledge("Dev KB", 0));
        String knowledgeId = (String) created.get("knowledgeId");
        assertNotNull(knowledgeId);

        Map<String, Object> tagCreated = service.createTag("dev-admin", "default-org", singleton("tagName", "Backend"));
        String tagId = (String) tagCreated.get("tagId");
        service.bindTags("dev-admin", "default-org", bindTags(knowledgeId, tagId));

        Map<String, Object> list = service.selectKnowledge("dev-admin", "default-org", selectKnowledge("Dev", 0, -1));
        List<Map<String, Object>> knowledgeList = listOfMaps(list.get("knowledgeList"));
        assertEquals(1, knowledgeList.size());
        Map<String, Object> knowledge = knowledgeList.get(0);
        assertEquals(knowledgeId, knowledge.get("knowledgeId"));
        assertEquals("Dev KB", knowledge.get("name"));
        assertEquals(0, knowledge.get("category"));
        assertEquals(0, knowledge.get("external"));
        assertEquals(20, knowledge.get("permissionType"));
        assertEquals("2", map(knowledge.get("embeddingModelInfo")).get("modelId"));
        assertTrue((Boolean) listOfMaps(knowledge.get("knowledgeTagList")).get(0).get("selected"));

        Map<String, Object> tagList = service.listTags("dev-admin", "default-org", tagQuery(knowledgeId, ""));
        assertEquals(1, listOfMaps(tagList.get("knowledgeTagList")).size());
        assertEquals(1, service.countTagBindings("dev-admin", "default-org", singleton("tagId", tagId)).get("tagBindCount"));

        Map<String, Object> docPage = service.listDocs("dev-admin", "default-org", docList(knowledgeId));
        assertEquals(0, docPage.get("total"));
        assertEquals("Dev KB", map(docPage.get("docKnowledgeInfo")).get("knowledgeName"));
        assertEquals("2", map(map(docPage.get("docKnowledgeInfo")).get("embeddingModel")).get("modelId"));

        Map<String, Object> docConfig = service.getDocConfig("dev-admin", "default-org", singleton("knowledgeId", knowledgeId));
        assertEquals("0", map(docConfig.get("docSegment")).get("segmentMethod"));

        service.updateKnowledge("dev-admin", "default-org", updateKnowledge(knowledgeId, "Dev KB Updated"));
        assertEquals("Dev KB Updated", listOfMaps(service.selectKnowledge("dev-admin", "default-org",
                selectKnowledge("Updated", 0, -1)).get("knowledgeList")).get(0).get("name"));

        service.deleteKnowledge("dev-admin", "default-org", singleton("knowledgeId", knowledgeId));
        assertEquals(0, listOfMaps(service.selectKnowledge("dev-admin", "default-org",
                selectKnowledge("", 0, -1)).get("knowledgeList")).size());
    }

    @Test
    public void keywordLifecycleFollowsFrontendAndGoContract() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Keyword KB", 0)).get("knowledgeId");
        String otherKnowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Other KB", 0)).get("knowledgeId");

        Map<String, Object> created = service.createKeyword("dev-admin", "default-org",
                keywordCreate("question", "document", knowledgeId));
        assertEquals(1001L, created.get("id"));

        Map<String, Object> list = service.listKeywords("dev-admin", "default-org", keywordList("question"));
        assertEquals(1, list.get("total"));
        assertEquals(1, list.get("pageNo"));
        Map<String, Object> keyword = listOfMaps(list.get("list")).get(0);
        assertEquals(1001L, keyword.get("id"));
        assertEquals("question", keyword.get("name"));
        assertEquals("document", keyword.get("alias"));
        assertEquals("Keyword KB", stringList(keyword.get("knowledgeBaseNames")).get(0));

        Map<String, Object> detail = service.getKeyword("dev-admin", "default-org", singleton("id", 1001));
        assertEquals("Keyword KB", stringList(detail.get("knowledgeBaseNames")).get(0));
        Map<String, Object> docs = service.listDocs("dev-admin", "default-org", docList(knowledgeId));
        assertEquals("question", listOfMaps(map(docs.get("docKnowledgeInfo")).get("keywords")).get(0).get("name"));

        service.updateKeyword("dev-admin", "default-org",
                keywordUpdate(1001L, "question-updated", "document-updated", otherKnowledgeId));
        Map<String, Object> updated = service.getKeyword("dev-admin", "default-org", singleton("id", 1001));
        assertEquals("question-updated", updated.get("name"));
        assertEquals("Other KB", stringList(updated.get("knowledgeBaseNames")).get(0));

        service.deleteKeyword("dev-admin", "default-org", singleton("id", 1001));
        assertEquals(0, service.listKeywords("dev-admin", "default-org", keywordList("")).get("total"));
    }

    @Test
    public void qaPairLifecycleFollowsFrontendAndGoContract() {
        Map<String, Object> createdKnowledge = service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Dev QA", 1));
        String knowledgeId = (String) createdKnowledge.get("knowledgeId");

        Map<String, Object> createdPair = service.createQaPair("dev-admin", "default-org",
                qaPairCreate(knowledgeId, "What is Wanwu?", "An AI platform."));
        String qaPairId = (String) createdPair.get("qaPairId");
        assertNotNull(qaPairId);

        Map<String, Object> list = service.listQaPairs("dev-admin", "default-org",
                qaPairList(knowledgeId, "Wanwu", Collections.singletonList(2)));
        assertEquals(1, list.get("total"));
        assertEquals("Dev QA", map(list.get("qaKnowledgeInfo")).get("knowledgeName"));
        Map<String, Object> pair = listOfMaps(list.get("list")).get(0);
        assertEquals(qaPairId, pair.get("qaPairId"));
        assertEquals("What is Wanwu?", pair.get("question"));
        assertEquals("An AI platform.", pair.get("answer"));
        assertEquals(2, pair.get("status"));
        assertTrue((Boolean) pair.get("switch"));
        assertEquals("admin", pair.get("author"));

        service.updateQaPair("dev-admin", "default-org",
                qaPairUpdate(qaPairId, "What is Wanwu Java?", "A Java reproduction."));
        service.updateQaPairSwitch("dev-admin", "default-org", qaPairSwitch(qaPairId, false));

        Map<String, Object> updatedList = service.listQaPairs("dev-admin", "default-org",
                qaPairList(knowledgeId, "Java", Collections.singletonList(-1)));
        Map<String, Object> updatedPair = listOfMaps(updatedList.get("list")).get(0);
        assertEquals("What is Wanwu Java?", updatedPair.get("question"));
        assertFalse((Boolean) updatedPair.get("switch"));

        service.updateQaPairSwitch("dev-admin", "default-org", qaPairSwitch(qaPairId, true));
        Map<String, Object> hit = service.hitQaPairs("dev-admin", "default-org", qaHit(knowledgeId, "Wanwu Java"));
        assertEquals(1, listOfMaps(hit.get("searchList")).size());
        assertEquals(qaPairId, listOfMaps(hit.get("searchList")).get(0).get("qaPairId"));

        Map<String, Object> tip = service.getQaImportTip("dev-admin", "default-org", singleton("knowledgeId", knowledgeId));
        assertEquals(2, tip.get("uploadstatus"));
        assertEquals("Dev QA", tip.get("knowledgeName"));

        service.deleteQaPairs("dev-admin", "default-org", qaPairDelete(knowledgeId, qaPairId));
        assertEquals(0, service.listQaPairs("dev-admin", "default-org",
                qaPairList(knowledgeId, "", Collections.singletonList(-1))).get("total"));
    }

    @Test
    public void exportRecordsFollowQaAndDocFrontendContract() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Export KB", 0)).get("knowledgeId");
        service.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-export", "Guide.txt"));
        service.createQaPair("dev-admin", "default-org",
                qaPairCreate(knowledgeId, "What is exported?", "QA pairs and docs."));

        Map<String, Object> qaExport = service.exportQaPairs("dev-admin", "default-org",
                singleton("knowledgeId", knowledgeId));
        assertEquals(true, qaExport.get("recordCreated"));
        assertTrue(((String) qaExport.get("downloadUrl")).endsWith(".csv"));

        Map<String, Object> docExportRequest = new LinkedHashMap<String, Object>();
        docExportRequest.put("knowledgeId", knowledgeId);
        docExportRequest.put("docIdList", Collections.singletonList("doc-export"));
        Map<String, Object> docExport = service.exportDocs("dev-admin", "default-org", docExportRequest);
        assertEquals(true, docExport.get("recordCreated"));
        assertTrue(((String) docExport.get("downloadUrl")).endsWith(".zip"));

        Map<String, Object> records = service.listExportRecords("dev-admin", "default-org",
                exportRecordList(knowledgeId, 1, 10));
        assertEquals(2, records.get("total"));
        Map<String, Object> latest = listOfMaps(records.get("list")).get(0);
        assertEquals(2, latest.get("status"));
        assertEquals("admin", latest.get("author"));
        assertEquals("Export KB", latest.get("knowledgeName"));
        assertTrue(((String) latest.get("filePath")).contains("/knowledge/export/file/"));

        Map<String, Object> qaFile = service.getExportRecordFile("dev-admin", "default-org",
                singleton("exportRecordId", qaExport.get("exportRecordId")));
        assertEquals("text/csv;charset=UTF-8", qaFile.get("contentType"));
        assertTrue(((String) qaFile.get("content")).contains("What is exported?"));

        Map<String, Object> docFile = service.getExportRecordFile("dev-admin", "default-org",
                singleton("exportRecordId", docExport.get("exportRecordId")));
        assertEquals("application/zip", docFile.get("contentType"));
        assertFalse(((String) docFile.get("contentBase64")).isEmpty());

        service.deleteExportRecord("dev-admin", "default-org",
                singleton("exportRecordId", qaExport.get("exportRecordId")));
        assertEquals(1, service.listExportRecords("dev-admin", "default-org",
                exportRecordList(knowledgeId, 1, 10)).get("total"));
    }

    @Test
    public void reportLifecycleFollowsFrontendAndGoContract() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Report KB", 0)).get("knowledgeId");

        Map<String, Object> empty = service.listReports("dev-admin", "default-org", reportList(knowledgeId, 1, 10));
        assertEquals(0, empty.get("total"));
        assertEquals(0, empty.get("status"));
        assertEquals(true, empty.get("canGenerate"));
        assertEquals(true, empty.get("canAddReport"));
        assertEquals(-1, empty.get("lastImportStatus"));

        service.generateReport("dev-admin", "default-org", singleton("knowledgeId", knowledgeId));
        Map<String, Object> generated = service.listReports("dev-admin", "default-org", reportList(knowledgeId, 1, 10));
        assertEquals(1, generated.get("total"));
        assertEquals(2, generated.get("status"));
        Map<String, Object> generatedReport = listOfMaps(generated.get("list")).get(0);
        assertEquals("Generated Community Report", generatedReport.get("title"));
        assertTrue(((String) generatedReport.get("content")).contains("Report KB"));

        service.addReport("dev-admin", "default-org", reportCreate(knowledgeId, "Manual", "Manual content"));
        Map<String, Object> withManual = service.listReports("dev-admin", "default-org", reportList(knowledgeId, 1, 1));
        assertEquals(2, withManual.get("total"));
        Map<String, Object> manual = listOfMaps(withManual.get("list")).get(0);
        assertEquals("Manual", manual.get("title"));

        String contentId = (String) manual.get("contentId");
        service.updateReport("dev-admin", "default-org",
                reportUpdate(knowledgeId, contentId, "Manual Updated", "Updated content"));
        Map<String, Object> updatedPage = service.listReports("dev-admin", "default-org", reportList(knowledgeId, 1, 10));
        assertEquals("Manual Updated", listOfMaps(updatedPage.get("list")).get(0).get("title"));

        service.batchAddReports("dev-admin", "default-org", reportBatch(knowledgeId, "file-report-csv"));
        Map<String, Object> imported = service.listReports("dev-admin", "default-org", reportList(knowledgeId, 1, 10));
        assertEquals(3, imported.get("total"));
        assertEquals(2, imported.get("lastImportStatus"));
        assertEquals("Imported Community Report", listOfMaps(imported.get("list")).get(0).get("title"));

        service.deleteReport("dev-admin", "default-org", reportDelete(knowledgeId, contentId));
        Map<String, Object> afterDelete = service.listReports("dev-admin", "default-org", reportList(knowledgeId, 1, 10));
        assertEquals(2, afterDelete.get("total"));
    }

    @Test
    public void externalKnowledgeLifecycleFollowsFrontendAndGoContract() {
        Map<String, Object> createdApi = service.createExternalApi("dev-admin", "default-org",
                externalApiCreate("Dify Dev", "https://dify.example/v1", "dev-key"));
        String externalApiId = (String) createdApi.get("externalApiId");
        assertEquals("external-api-1001", externalApiId);

        Map<String, Object> apiList = service.listExternalApis("dev-admin", "default-org",
                Collections.<String, Object>emptyMap());
        Map<String, Object> api = listOfMaps(apiList.get("externalApiList")).get(0);
        assertEquals("Dify Dev", api.get("name"));
        assertEquals("https://dify.example/v1", api.get("baseUrl"));
        assertFalse(api.containsKey("externalAPIId"));

        Map<String, Object> selectable = service.listExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeList(externalApiId));
        List<Map<String, Object>> candidates = listOfMaps(selectable.get("externalKnowledgeList"));
        assertEquals(2, candidates.size());
        assertFalse(candidates.get(0).containsKey("externalAPIId"));
        String firstExternalKnowledgeId = (String) candidates.get(0).get("externalKnowledgeId");

        Map<String, Object> createdKnowledge = service.createExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeCreate("External KB", externalApiId, firstExternalKnowledgeId));
        String knowledgeId = (String) createdKnowledge.get("knowledgeId");
        assertEquals("knowledge-1001", knowledgeId);

        Map<String, Object> afterMount = service.listExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeList(externalApiId));
        assertEquals(1, listOfMaps(afterMount.get("externalKnowledgeList")).size());

        Map<String, Object> selected = listOfMaps(service.selectKnowledge("dev-admin", "default-org",
                selectKnowledge("External", 0, 1)).get("knowledgeList")).get(0);
        assertEquals(1, selected.get("external"));
        assertEquals(3, selected.get("docCount"));
        Map<String, Object> externalInfo = map(selected.get("externalKnowledgeInfo"));
        assertEquals(externalApiId, externalInfo.get("externalApiId"));
        assertEquals("dify", externalInfo.get("externalSource"));
        assertEquals(firstExternalKnowledgeId, externalInfo.get("externalKnowledgeId"));

        service.updateExternalApi("dev-admin", "default-org",
                externalApiUpdate(externalApiId, "Dify Updated", "https://dify.example/v2", "dev-key-2"));
        Map<String, Object> withUpdatedApi = listOfMaps(service.selectKnowledge("dev-admin", "default-org",
                selectKnowledge("External", 0, 1)).get("knowledgeList")).get(0);
        assertEquals("Dify Updated", map(withUpdatedApi.get("externalKnowledgeInfo")).get("externalApiName"));

        String secondExternalKnowledgeId = (String) listOfMaps(afterMount.get("externalKnowledgeList"))
                .get(0).get("externalKnowledgeId");
        service.updateExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeUpdate(knowledgeId, "External KB Updated", externalApiId, secondExternalKnowledgeId));
        Map<String, Object> updated = listOfMaps(service.selectKnowledge("dev-admin", "default-org",
                selectKnowledge("Updated", 0, 1)).get("knowledgeList")).get(0);
        assertEquals("External KB Updated", updated.get("name"));
        assertEquals(5, updated.get("docCount"));
        assertEquals(secondExternalKnowledgeId, map(updated.get("externalKnowledgeInfo")).get("externalKnowledgeId"));

        service.deleteExternalKnowledge("dev-admin", "default-org", singleton("knowledgeId", knowledgeId));
        assertEquals(0, listOfMaps(service.selectKnowledge("dev-admin", "default-org",
                selectKnowledge("", 0, 1)).get("knowledgeList")).size());
        assertEquals(2, listOfMaps(service.listExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeList(externalApiId)).get("externalKnowledgeList")).size());

        service.deleteExternalApi("dev-admin", "default-org", singleton("externalApiId", externalApiId));
        assertEquals(0, listOfMaps(service.listExternalApis("dev-admin", "default-org",
                Collections.<String, Object>emptyMap()).get("externalApiList")).size());
    }

    @Test
    public void documentImportAndSegmentsFollowFrontendAndGoContract() {
        Map<String, Object> created = service.createKnowledge("dev-admin", "default-org", createKnowledge("Dev Docs", 0));
        String knowledgeId = (String) created.get("knowledgeId");

        Map<String, Object> analyzed = service.analyzeDocUrls("dev-admin", "default-org",
                urlAnalysis(knowledgeId, "https://example.com/files/guide.txt"));
        assertEquals("guide.txt", listOfMaps(analyzed.get("urlList")).get(0).get("fileName"));

        service.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-guide", "Guide.txt"));
        Map<String, Object> docPage = service.listDocs("dev-admin", "default-org", docList(knowledgeId));
        assertEquals(1, docPage.get("total"));
        Map<String, Object> doc = listOfMaps(docPage.get("list")).get(0);
        assertEquals("doc-guide", doc.get("docId"));
        assertEquals("Guide.txt", doc.get("docName"));
        assertEquals(1, doc.get("status"));
        assertEquals("admin", doc.get("author"));

        Map<String, Object> firstSegments = service.listDocSegments("dev-admin", "default-org", segmentList("doc-guide", ""));
        assertEquals(1, firstSegments.get("segmentTotalNum"));
        Map<String, Object> defaultSegment = listOfMaps(firstSegments.get("contentList")).get(0);
        assertEquals(true, defaultSegment.get("available"));
        assertTrue(((String) defaultSegment.get("content")).contains("Guide.txt"));

        service.createDocSegment("dev-admin", "default-org",
                createSegment("doc-guide", "Extra segment", Collections.singletonList("manual")));
        Map<String, Object> twoSegments = service.listDocSegments("dev-admin", "default-org", segmentList("doc-guide", "Extra"));
        assertEquals(1, twoSegments.get("segmentTotalNum"));
        Map<String, Object> extraSegment = listOfMaps(twoSegments.get("contentList")).get(0);
        assertEquals("Extra segment", extraSegment.get("content"));
        assertEquals("manual", ((List) extraSegment.get("labels")).get(0));

        String contentId = (String) extraSegment.get("contentId");
        service.updateDocSegment("dev-admin", "default-org", updateSegment("doc-guide", contentId, "Updated segment"));
        service.updateDocSegmentStatus("dev-admin", "default-org", segmentStatus("doc-guide", contentId, "false"));
        Map<String, Object> updatedSegments = service.listDocSegments("dev-admin", "default-org", segmentList("doc-guide", "Updated"));
        Map<String, Object> updated = listOfMaps(updatedSegments.get("contentList")).get(0);
        assertEquals("Updated segment", updated.get("content"));
        assertEquals(false, updated.get("available"));

        service.deleteDocSegment("dev-admin", "default-org", deleteSegment("doc-guide", contentId));
        assertEquals(1, service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-guide", "")).get("segmentTotalNum"));

        service.deleteDocs("dev-admin", "default-org", deleteDocs(knowledgeId, "doc-guide"));
        assertEquals(0, service.listDocs("dev-admin", "default-org", docList(knowledgeId)).get("total"));
        assertEquals(0, service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-guide", "")).get("segmentTotalNum"));
    }

    @Test
    public void knowledgeHitReturnsImportedDocumentSegmentsForFrontendContract() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Hit KB", 0)).get("knowledgeId");
        service.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-hit", "Guide.txt"));
        service.createDocSegment("dev-admin", "default-org",
                createSegment("doc-hit", "Wanwu Java retrieval keeps frontend hit test useful.",
                        Collections.singletonList("retrieval")));

        Map<String, Object> hit = service.hitKnowledge("dev-admin", "default-org",
                knowledgeHit(knowledgeId, "Wanwu Java retrieval"));
        List<Map<String, Object>> searchList = listOfMaps(hit.get("searchList"));
        assertEquals(1, searchList.size());
        assertEquals("Guide.txt", searchList.get(0).get("title"));
        assertEquals("Hit KB", searchList.get(0).get("knowledgeName"));
        assertEquals("text", searchList.get(0).get("contentType"));
        assertEquals("Wanwu Java retrieval keeps frontend hit test useful.", searchList.get(0).get("snippet"));
        assertEquals(1.0D, ((List<Double>) hit.get("score")).get(0));
        assertEquals(false, hit.get("useGraph"));
        assertTrue(((String) hit.get("prompt")).contains("Wanwu Java retrieval keeps frontend hit test useful."));

        String contentId = (String) searchList.get(0).get("contentId");
        service.updateDocSegmentStatus("dev-admin", "default-org", segmentStatus("doc-hit", contentId, "false"));
        Map<String, Object> disabledHit = service.hitKnowledge("dev-admin", "default-org",
                knowledgeHit(knowledgeId, "Wanwu Java retrieval"));
        assertEquals(0, listOfMaps(disabledHit.get("searchList")).size());
    }

    @Test
    public void mutableKnowledgeStateIsPersistedAsSnapshotRecord() {
        KnowledgeRecordMapper mapper = mock(KnowledgeRecordMapper.class);
        KnowledgeServiceImpl persistent = new KnowledgeServiceImpl(mapper);

        Map<String, Object> created = persistent.createKnowledge("dev-admin", "default-org",
                createKnowledge("Persist KB", 0));
        String knowledgeId = (String) created.get("knowledgeId");
        Map<String, Object> tag = persistent.createTag("dev-admin", "default-org", singleton("tagName", "Persist"));
        persistent.bindTags("dev-admin", "default-org", bindTags(knowledgeId, (String) tag.get("tagId")));
        persistent.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-persist", "Persist.txt"));
        persistent.createQaPair("dev-admin", "default-org",
                qaPairCreate(knowledgeId, "Persist?", "Yes."));
        persistent.createKeyword("dev-admin", "default-org",
                keywordCreate("Persist keyword", "Persist alias", knowledgeId));
        persistent.addReport("dev-admin", "default-org",
                reportCreate(knowledgeId, "Persist report", "Persist report body"));
        persistent.exportQaPairs("dev-admin", "default-org", singleton("knowledgeId", knowledgeId));
        String externalApiId = (String) persistent.createExternalApi("dev-admin", "default-org",
                externalApiCreate("Persist Dify", "https://persist.example/v1", "persist-key")).get("externalApiId");
        String externalKnowledgeId = (String) listOfMaps(persistent.listExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeList(externalApiId)).get("externalKnowledgeList")).get(0).get("externalKnowledgeId");
        persistent.createExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeCreate("Persist External KB", externalApiId, externalKnowledgeId));

        ArgumentCaptor<KnowledgeRecordEntity> captor = ArgumentCaptor.forClass(KnowledgeRecordEntity.class);
        verify(mapper, atLeastOnce()).upsertRecord(captor.capture());
        KnowledgeRecordEntity last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("snapshot", last.getRecordType());
        assertEquals("state", last.getRecordId());
        assertTrue(last.getPayload().contains("Persist KB"));
        assertTrue(last.getPayload().contains("doc-persist"));
        assertTrue(last.getPayload().contains("Persist?"));
        assertTrue(last.getPayload().contains("Persist keyword"));
        assertTrue(last.getPayload().contains("Persist report"));
        assertTrue(last.getPayload().contains("export-1001"));
        assertTrue(last.getPayload().contains("Persist Dify"));
        assertTrue(last.getPayload().contains("Persist External KB"));
    }

    @Test
    public void persistedSnapshotIsLoadedAndSequencesContinueAfterRestart() {
        KnowledgeRecordMapper sourceMapper = mock(KnowledgeRecordMapper.class);
        KnowledgeServiceImpl source = new KnowledgeServiceImpl(sourceMapper);
        Map<String, Object> created = source.createKnowledge("dev-admin", "default-org",
                createKnowledge("Restart KB", 0));
        String knowledgeId = (String) created.get("knowledgeId");
        Map<String, Object> tag = source.createTag("dev-admin", "default-org", singleton("tagName", "Restart"));
        source.bindTags("dev-admin", "default-org", bindTags(knowledgeId, (String) tag.get("tagId")));
        source.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-restart", "Restart.txt"));
        source.createQaPair("dev-admin", "default-org",
                qaPairCreate(knowledgeId, "Restart?", "Loaded."));
        source.createKeyword("dev-admin", "default-org",
                keywordCreate("Restart keyword", "Restart alias", knowledgeId));
        source.addReport("dev-admin", "default-org",
                reportCreate(knowledgeId, "Restart report", "Restart report body"));
        source.exportQaPairs("dev-admin", "default-org", singleton("knowledgeId", knowledgeId));
        String externalApiId = (String) source.createExternalApi("dev-admin", "default-org",
                externalApiCreate("Restart Dify", "https://restart.example/v1", "restart-key")).get("externalApiId");
        String externalKnowledgeId = (String) listOfMaps(source.listExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeList(externalApiId)).get("externalKnowledgeList")).get(0).get("externalKnowledgeId");
        String externalWanwuKnowledgeId = (String) source.createExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeCreate("Restart External KB", externalApiId, externalKnowledgeId)).get("knowledgeId");

        ArgumentCaptor<KnowledgeRecordEntity> captor = ArgumentCaptor.forClass(KnowledgeRecordEntity.class);
        verify(sourceMapper, atLeastOnce()).upsertRecord(captor.capture());
        String payload = captor.getAllValues().get(captor.getAllValues().size() - 1).getPayload();

        KnowledgeRecordMapper restartMapper = mock(KnowledgeRecordMapper.class);
        when(restartMapper.selectByType(eq("snapshot")))
                .thenReturn(Collections.singletonList(record("snapshot", "state", payload)));
        KnowledgeServiceImpl restarted = new KnowledgeServiceImpl(restartMapper);

        List<Map<String, Object>> knowledgeList = listOfMaps(restarted.selectKnowledge("dev-admin", "default-org",
                selectKnowledge("Restart KB", 0, -1)).get("knowledgeList"));
        assertEquals(1, knowledgeList.size());
        assertEquals(knowledgeId, knowledgeList.get(0).get("knowledgeId"));
        assertTrue((Boolean) listOfMaps(knowledgeList.get(0).get("knowledgeTagList")).get(0).get("selected"));
        assertEquals(1, restarted.listDocs("dev-admin", "default-org", docList(knowledgeId)).get("total"));
        assertEquals(1, restarted.listQaPairs("dev-admin", "default-org",
                qaPairList(knowledgeId, "Restart", Collections.singletonList(-1))).get("total"));
        assertEquals(1, restarted.listKeywords("dev-admin", "default-org", keywordList("Restart")).get("total"));
        assertEquals(1, restarted.listReports("dev-admin", "default-org", reportList(knowledgeId, 1, 10)).get("total"));
        assertEquals(1, restarted.listExportRecords("dev-admin", "default-org",
                exportRecordList(knowledgeId, 1, 10)).get("total"));
        List<Map<String, Object>> externalKnowledgeList = listOfMaps(restarted.selectKnowledge("dev-admin", "default-org",
                selectKnowledge("Restart External", 0, 1)).get("knowledgeList"));
        assertEquals(1, externalKnowledgeList.size());
        assertEquals(externalWanwuKnowledgeId, externalKnowledgeList.get(0).get("knowledgeId"));
        assertEquals("Restart Dify", map(externalKnowledgeList.get(0).get("externalKnowledgeInfo")).get("externalApiName"));
        assertEquals(1, listOfMaps(restarted.listExternalKnowledge("dev-admin", "default-org",
                externalKnowledgeList(externalApiId)).get("externalKnowledgeList")).size());

        Map<String, Object> next = restarted.createKnowledge("dev-admin", "default-org",
                createKnowledge("Restart Next", 0));
        assertEquals("knowledge-1003", next.get("knowledgeId"));
        Map<String, Object> nextKeyword = restarted.createKeyword("dev-admin", "default-org",
                keywordCreate("Restart next keyword", "Restart next alias", (String) next.get("knowledgeId")));
        assertEquals(1002L, nextKeyword.get("id"));
        restarted.addReport("dev-admin", "default-org",
                reportCreate((String) next.get("knowledgeId"), "Restart next report", "Restart next report body"));
        assertEquals("report-1002", listOfMaps(restarted.listReports("dev-admin", "default-org",
                reportList((String) next.get("knowledgeId"), 1, 10)).get("list")).get(0).get("contentId"));
        restarted.exportQaPairs("dev-admin", "default-org", singleton("knowledgeId", (String) next.get("knowledgeId")));
        assertEquals("export-1002", listOfMaps(restarted.listExportRecords("dev-admin", "default-org",
                exportRecordList((String) next.get("knowledgeId"), 1, 10)).get("list")).get(0).get("exportRecordId"));
        Map<String, Object> nextExternalApi = restarted.createExternalApi("dev-admin", "default-org",
                externalApiCreate("Restart Next Dify", "https://restart.example/v2", "restart-key-2"));
        assertEquals("external-api-1002", nextExternalApi.get("externalApiId"));
        verify(restartMapper, atLeastOnce()).upsertRecord(any(KnowledgeRecordEntity.class));
    }

    private Map<String, Object> createKnowledge(String name, int category) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("name", name);
        request.put("description", "development documents");
        request.put("embeddingModelInfo", singleton("modelId", "2"));
        request.put("knowledgeGraph", singleton("switch", false));
        request.put("category", category);
        request.put("avatar", singleton("path", ""));
        return request;
    }

    private Map<String, Object> updateKnowledge(String knowledgeId, String name) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("name", name);
        request.put("description", "updated");
        request.put("avatar", singleton("path", ""));
        return request;
    }

    private Map<String, Object> selectKnowledge(String name, int category, int external) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("name", name);
        request.put("category", category);
        request.put("external", external);
        request.put("tagId", Collections.emptyList());
        return request;
    }

    private Map<String, Object> docList(String knowledgeId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("pageNo", 1);
        request.put("pageSize", 10);
        return request;
    }

    private Map<String, Object> docImport(String knowledgeId, String docId, String docName) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("docInfoList", Collections.singletonList(docInfo(docId, docName)));
        Map<String, Object> segment = new LinkedHashMap<String, Object>();
        segment.put("segmentMethod", "0");
        segment.put("segmentType", "0");
        segment.put("maxSplitter", 500);
        request.put("docSegment", segment);
        request.put("docAnalyzer", Collections.singletonList("text"));
        return request;
    }

    private Map<String, Object> docInfo(String docId, String docName) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("docName", docName);
        request.put("docType", "txt");
        request.put("docSize", 42);
        return request;
    }

    private Map<String, Object> deleteDocs(String knowledgeId, String docId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("docIdList", Collections.singletonList(docId));
        return request;
    }

    private Map<String, Object> urlAnalysis(String knowledgeId, String url) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("urlList", Collections.singletonList(url));
        return request;
    }

    private Map<String, Object> segmentList(String docId, String keyword) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("keyword", keyword);
        request.put("pageNo", 1);
        request.put("pageSize", 10);
        return request;
    }

    private Map<String, Object> createSegment(String docId, String content, List<String> labels) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("content", content);
        request.put("labels", labels);
        return request;
    }

    private Map<String, Object> updateSegment(String docId, String contentId, String content) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("contentId", contentId);
        request.put("content", content);
        return request;
    }

    private Map<String, Object> segmentStatus(String docId, String contentId, String status) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("contentId", contentId);
        request.put("contentStatus", status);
        return request;
    }

    private Map<String, Object> deleteSegment(String docId, String contentId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("contentId", contentId);
        return request;
    }

    private Map<String, Object> tagQuery(String knowledgeId, String tagName) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("tagName", tagName);
        return request;
    }

    private Map<String, Object> bindTags(String knowledgeId, String tagId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("tagIdList", Collections.singletonList(tagId));
        return request;
    }

    private Map<String, Object> keywordCreate(String name, String alias, String knowledgeId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("name", name);
        request.put("alias", alias);
        request.put("knowledgeBaseIds", Collections.singletonList(knowledgeId));
        return request;
    }

    private Map<String, Object> keywordUpdate(long id, String name, String alias, String knowledgeId) {
        Map<String, Object> request = keywordCreate(name, alias, knowledgeId);
        request.put("id", id);
        return request;
    }

    private Map<String, Object> keywordList(String name) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("name", name);
        request.put("pageNo", 1);
        request.put("pageSize", 10);
        return request;
    }

    private Map<String, Object> qaPairCreate(String knowledgeId, String question, String answer) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("question", question);
        request.put("answer", answer);
        return request;
    }

    private Map<String, Object> qaPairUpdate(String qaPairId, String question, String answer) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("qaPairId", qaPairId);
        request.put("question", question);
        request.put("answer", answer);
        return request;
    }

    private Map<String, Object> qaPairSwitch(String qaPairId, boolean enabled) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("qaPairId", qaPairId);
        request.put("switch", enabled);
        return request;
    }

    private Map<String, Object> qaPairDelete(String knowledgeId, String qaPairId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("QAPairIdList", Collections.singletonList(qaPairId));
        return request;
    }

    private Map<String, Object> qaPairList(String knowledgeId, String name, List<Integer> status) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("name", name);
        request.put("status", status);
        request.put("pageNo", 1);
        request.put("pageSize", 10);
        return request;
    }

    private Map<String, Object> qaHit(String knowledgeId, String question) {
        return knowledgeHit(knowledgeId, question);
    }

    private Map<String, Object> knowledgeHit(String knowledgeId, String question) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("question", question);
        request.put("knowledgeList", Collections.singletonList(singleton("knowledgeId", knowledgeId)));
        request.put("knowledgeMatchParams", singleton("topK", 5));
        return request;
    }

    private Map<String, Object> reportList(String knowledgeId, int pageNo, int pageSize) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("pageNo", pageNo);
        request.put("pageSize", pageSize);
        return request;
    }

    private Map<String, Object> exportRecordList(String knowledgeId, int pageNo, int pageSize) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("pageNo", pageNo);
        request.put("pageSize", pageSize);
        return request;
    }

    private Map<String, Object> reportCreate(String knowledgeId, String title, String content) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("title", title);
        request.put("content", content);
        return request;
    }

    private Map<String, Object> reportUpdate(String knowledgeId, String contentId, String title, String content) {
        Map<String, Object> request = reportCreate(knowledgeId, title, content);
        request.put("contentId", contentId);
        return request;
    }

    private Map<String, Object> reportDelete(String knowledgeId, String contentId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("contentId", contentId);
        return request;
    }

    private Map<String, Object> reportBatch(String knowledgeId, String fileUploadId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("fileUploadId", fileUploadId);
        return request;
    }

    private Map<String, Object> externalApiCreate(String name, String baseUrl, String apiKey) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("name", name);
        request.put("description", "external api");
        request.put("baseUrl", baseUrl);
        request.put("apiKey", apiKey);
        return request;
    }

    private Map<String, Object> externalApiUpdate(String externalApiId, String name, String baseUrl, String apiKey) {
        Map<String, Object> request = externalApiCreate(name, baseUrl, apiKey);
        request.put("externalApiId", externalApiId);
        return request;
    }

    private Map<String, Object> externalKnowledgeList(String externalApiId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("externalApiId", externalApiId);
        return request;
    }

    private Map<String, Object> externalKnowledgeCreate(String name, String externalApiId, String externalKnowledgeId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("name", name);
        request.put("description", "external knowledge");
        request.put("externalSource", "dify");
        request.put("externalApiId", externalApiId);
        request.put("externalKnowledgeId", externalKnowledgeId);
        request.put("avatar", singleton("path", ""));
        return request;
    }

    private Map<String, Object> externalKnowledgeUpdate(String knowledgeId, String name, String externalApiId,
                                                        String externalKnowledgeId) {
        Map<String, Object> request = externalKnowledgeCreate(name, externalApiId, externalKnowledgeId);
        request.put("knowledgeId", knowledgeId);
        return request;
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(key, value);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object value) {
        return (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
        return (List<String>) value;
    }

    private KnowledgeRecordEntity record(String type, String id, String payload) {
        KnowledgeRecordEntity record = new KnowledgeRecordEntity();
        record.setRecordType(type);
        record.setRecordId(id);
        record.setPayload(payload);
        record.setCreatedAt(1L);
        record.setUpdatedAt(1L);
        return record;
    }
}
