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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertEquals(30, knowledge.get("permissionType"));
        assertEquals("2", map(knowledge.get("embeddingModelInfo")).get("modelId"));
        assertTrue((Boolean) listOfMaps(knowledge.get("knowledgeTagList")).get(0).get("selected"));

        service.checkKnowledgeUserPermission("dev-admin", "default-org", knowledgeId, 30);
        assertThrows(IllegalArgumentException.class, () ->
                service.checkKnowledgeUserPermission("dev-app", "default-org", knowledgeId, 0));

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
    public void knowledgeServiceExposesKnowledgeIdResolutionForBffMiddlewareParity() throws Exception {
        assertNotNull(com.unicomai.wanwu.api.knowledge.KnowledgeService.class.getMethod(
                "resolveKnowledgeId", String.class, String.class, Map.class));
    }

    @Test
    public void resolveKnowledgeIdFindsDirectDocQaPairAndNameReferences() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Resolve KB", 0)).get("knowledgeId");
        service.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-resolve", "Resolve.txt"));
        String qaPairId = (String) service.createQaPair("dev-admin", "default-org",
                qaPairCreate(knowledgeId, "What resolves?", "Knowledge IDs.")).get("qaPairId");

        assertEquals(knowledgeId, service.resolveKnowledgeId("dev-admin", "default-org",
                singleton("knowledgeId", knowledgeId)));
        assertEquals(knowledgeId, service.resolveKnowledgeId("dev-admin", "default-org",
                singleton("docId", "doc-resolve")));
        assertEquals(knowledgeId, service.resolveKnowledgeId("dev-admin", "default-org",
                singleton("qaPairId", qaPairId)));
        assertEquals(knowledgeId, service.resolveKnowledgeId("dev-admin", "default-org",
                singleton("knowledgeName", "Resolve KB")));
        assertThrows(IllegalArgumentException.class, () -> service.resolveKnowledgeId("dev-admin", "default-org",
                singleton("docId", "missing-doc")));
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
    public void qaImportParsesInlineCsvAndFallsBackToDocUrlName() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Imported QA", 1)).get("knowledgeId");

        service.importQaPairs("dev-admin", "default-org", qaImportContent(knowledgeId,
                "question,answer\n"
                        + "What is Wanwu?,An AI platform.\n"
                        + "\"How does CSV quoting work?\",\"Commas, quotes, and answers are preserved.\""));
        Map<String, Object> parsed = service.listQaPairs("dev-admin", "default-org",
                qaPairList(knowledgeId, "CSV quoting", Collections.singletonList(-1)));
        assertEquals(1, parsed.get("total"));
        Map<String, Object> parsedPair = listOfMaps(parsed.get("list")).get(0);
        assertEquals("How does CSV quoting work?", parsedPair.get("question"));
        assertEquals("Commas, quotes, and answers are preserved.", parsedPair.get("answer"));

        service.importQaPairs("dev-admin", "default-org", qaImportDocUrl(knowledgeId,
                "/service/api/v1/file/download/qa_import_template.csv"));
        Map<String, Object> fallback = service.listQaPairs("dev-admin", "default-org",
                qaPairList(knowledgeId, "Imported from qa_import_template.csv", Collections.singletonList(-1)));
        assertEquals(1, fallback.get("total"));

        Map<String, Object> hit = service.hitQaPairs("dev-admin", "default-org",
                qaHit(knowledgeId, "Wanwu"));
        assertEquals(1, listOfMaps(hit.get("searchList")).size());
    }

    @Test
    public void qaHitRanksQuestionMatchesBeforeAnswerMatches() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Ranking QA", 1)).get("knowledgeId");
        service.createQaPair("dev-admin", "default-org",
                qaPairCreate(knowledgeId, "How does recall work?", "Ranking is only in this answer."));
        Map<String, Object> stronger = service.createQaPair("dev-admin", "default-org",
                qaPairCreate(knowledgeId, "How does Ranking work?", "Question text should rank first."));

        Map<String, Object> request = qaHit(knowledgeId, "Ranking");
        map(request.get("knowledgeMatchParams")).put("topK", 1);
        Map<String, Object> hit = service.hitQaPairs("dev-admin", "default-org", request);
        List<Map<String, Object>> searchList = listOfMaps(hit.get("searchList"));

        assertEquals(1, searchList.size());
        assertEquals(stronger.get("qaPairId"), searchList.get(0).get("qaPairId"));
        assertEquals(1.0D, ((List<Double>) hit.get("score")).get(0));
    }

    @Test
    public void qaHitHonorsScoreThreshold() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Threshold QA", 1)).get("knowledgeId");
        service.createQaPair("dev-admin", "default-org",
                qaPairCreate(knowledgeId, "How does recall work?", "Threshold appears only in this answer."));
        Map<String, Object> stronger = service.createQaPair("dev-admin", "default-org",
                qaPairCreate(knowledgeId, "How does Threshold work?", "Question text should pass threshold."));

        Map<String, Object> request = qaHit(knowledgeId, "Threshold");
        map(request.get("knowledgeMatchParams")).put("score", 0.9D);
        Map<String, Object> hit = service.hitQaPairs("dev-admin", "default-org", request);
        List<Map<String, Object>> searchList = listOfMaps(hit.get("searchList"));

        assertEquals(1, searchList.size());
        assertEquals(stronger.get("qaPairId"), searchList.get(0).get("qaPairId"));
        assertEquals(1.0D, ((List<Double>) hit.get("score")).get(0));
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
    public void callbackStatusUpdatesPersistDocAndKnowledgeState() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Callback KB", 0)).get("knowledgeId");
        service.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-callback", "Callback.txt"));

        assertEquals(1, docStatus(knowledgeId, "doc-callback"));

        Map<String, Object> docStatus = new LinkedHashMap<String, Object>();
        docStatus.put("id", "doc-callback");
        docStatus.put("status", 31);
        service.updateCallbackDocStatus("", "", docStatus);
        assertEquals(31, docStatus(knowledgeId, "doc-callback"));

        service.initCallbackDocStatus("", "");
        assertEquals(5, docStatus(knowledgeId, "doc-callback"));

        Map<String, Object> finished = new LinkedHashMap<String, Object>();
        finished.put("id", "doc-callback");
        finished.put("status", 1);
        service.updateCallbackDocStatus("", "", finished);
        service.initCallbackDocStatus("", "");
        assertEquals(1, docStatus(knowledgeId, "doc-callback"));

        Map<String, Object> knowledgeStatus = new LinkedHashMap<String, Object>();
        knowledgeStatus.put("knowledgeId", knowledgeId);
        knowledgeStatus.put("reportStatus", 130);
        service.updateCallbackKnowledgeStatus("", "", knowledgeStatus);

        Map<String, Object> reports = service.listReports("dev-admin", "default-org", reportList(knowledgeId, 1, 10));
        assertEquals(130, reports.get("status"));
    }

    @Test
    public void reportBatchAddParsesInlineCsvAndKeepsFileUploadFallback() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Report Import KB", 0)).get("knowledgeId");

        service.batchAddReports("dev-admin", "default-org", reportBatchContent(knowledgeId,
                "title,content\n"
                        + "Alpha Report,Alpha imported report content.\n"
                        + "\"Beta, Report\",\"Beta imported report content with comma.\""));
        Map<String, Object> parsed = service.listReports("dev-admin", "default-org", reportList(knowledgeId, 1, 10));
        assertEquals(2, parsed.get("total"));
        assertEquals(2, parsed.get("lastImportStatus"));
        List<Map<String, Object>> reports = listOfMaps(parsed.get("list"));
        assertEquals("Alpha Report", reports.get(0).get("title"));
        assertEquals("Beta, Report", reports.get(1).get("title"));

        service.batchAddReports("dev-admin", "default-org", reportBatch(knowledgeId, "report-upload-id"));
        Map<String, Object> withFallback = service.listReports("dev-admin", "default-org",
                reportList(knowledgeId, 1, 10));
        assertEquals(3, withFallback.get("total"));
        assertEquals("Imported Community Report", listOfMaps(withFallback.get("list")).get(0).get("title"));
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
    public void docMetadataValuesFollowGoMetaValueContracts() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Meta Docs", 0)).get("knowledgeId");
        service.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-one", "One.txt"));
        service.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-two", "Two.txt"));

        service.updateDocMeta("dev-admin", "default-org", metaDefinition(knowledgeId, "source", "string"));
        Map<String, Object> meta = listOfMaps(service.selectMetaKeys("dev-admin", "default-org",
                singleton("knowledgeId", knowledgeId)).get("knowledgeMetaList")).get(0);
        String metaId = (String) meta.get("metaId");
        assertEquals("source", meta.get("metaKey"));

        service.updateMetaValues("dev-admin", "default-org",
                metaValueUpdate(knowledgeId, Collections.singletonList("doc-one"), metaId, "source", "manual"));
        service.updateMetaValues("dev-admin", "default-org",
                metaValueUpdate(knowledgeId, Collections.singletonList("doc-two"), metaId, "source", "imported"));

        Map<String, Object> values = service.listMetaValues("dev-admin", "default-org",
                metaValueList(knowledgeId, "doc-one", "doc-two"));
        List<Map<String, Object>> rows = listOfMaps(values.get("knowledgeMetaValues"));
        assertEquals("source", rows.get(0).get("metaKey"));
        assertEquals(java.util.Arrays.asList("manual", "imported"), rows.get(0).get("metaValue"));

        Map<String, Object> docs = service.listDocs("dev-admin", "default-org", docList(knowledgeId));
        assertEquals("manual", listOfMaps(listOfMaps(docs.get("list")).get(0).get("metaDataList")).get(0).get("metaValue"));

        Map<String, Object> segments = service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-one", ""));
        assertEquals("manual", listOfMaps(segments.get("metaDataList")).get(0).get("metaValue"));

        Map<String, Object> hit = service.hitKnowledge("dev-admin", "default-org",
                knowledgeHit(knowledgeId, "One.txt"));
        assertEquals("manual", listOfMaps(listOfMaps(hit.get("searchList")).get(0).get("metaDataList")).get(0).get("metaValue"));

        service.updateMetaValues("dev-admin", "default-org",
                metaValueDelete(knowledgeId, Collections.singletonList("doc-one"), metaId, "source"));
        Map<String, Object> afterDelete = service.listMetaValues("dev-admin", "default-org",
                metaValueList(knowledgeId, "doc-one", "doc-two"));
        assertEquals(Collections.singletonList("imported"),
                listOfMaps(afterDelete.get("knowledgeMetaValues")).get(0).get("metaValue"));
    }

    @Test
    public void documentImportSplitsInlineContentIntoSearchableSegments() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Parsed Docs", 0)).get("knowledgeId");
        String content = "Wanwu Java retrieval keeps frontend hit test useful.\n\n"
                + "Workflow nodes can call tools and models.\n\n"
                + "RAG retrieval uses persisted knowledge segments.";

        service.importDocs("dev-admin", "default-org",
                docImportWithContent(knowledgeId, "doc-parsed", "Parsed.txt", content));

        Map<String, Object> allSegments = service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-parsed", ""));
        assertEquals(3, allSegments.get("segmentTotalNum"));

        Map<String, Object> ragSegments = service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-parsed", "RAG retrieval"));
        assertEquals(1, ragSegments.get("segmentTotalNum"));
        Map<String, Object> ragSegment = listOfMaps(ragSegments.get("contentList")).get(0);
        assertEquals("RAG retrieval uses persisted knowledge segments.", ragSegment.get("content"));

        Map<String, Object> hit = service.hitKnowledge("dev-admin", "default-org",
                knowledgeHit(knowledgeId, "RAG retrieval"));
        List<Map<String, Object>> searchList = listOfMaps(hit.get("searchList"));
        assertEquals(1, searchList.size());
        assertEquals("Parsed.txt", searchList.get(0).get("title"));
        assertEquals("RAG retrieval uses persisted knowledge segments.", searchList.get(0).get("snippet"));
    }

    @Test
    public void reimportDocsRebuildsSegmentsFromSavedImportSourceAndUpdatedConfig() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Reimport Docs", 0)).get("knowledgeId");
        String content = "First imported paragraph.\n\nSecond imported paragraph.\n\nThird imported paragraph.";
        service.importDocs("dev-admin", "default-org",
                docImportWithContent(knowledgeId, "doc-reimport", "Reimport.txt", content));
        service.createDocSegment("dev-admin", "default-org",
                createSegment("doc-reimport", "Manual segment should be removed by reimport",
                        Collections.singletonList("manual")));

        assertEquals(4, service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-reimport", "")).get("segmentTotalNum"));

        Map<String, Object> reimportRequest = new LinkedHashMap<String, Object>();
        reimportRequest.put("knowledgeId", knowledgeId);
        reimportRequest.put("docIdList", Collections.singletonList("doc-reimport"));
        service.reimportDocs("dev-admin", "default-org", reimportRequest);

        assertEquals(3, service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-reimport", "")).get("segmentTotalNum"));
        assertEquals(0, service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-reimport", "Manual segment")).get("segmentTotalNum"));
        Map<String, Object> hit = service.hitKnowledge("dev-admin", "default-org",
                knowledgeHit(knowledgeId, "Second imported paragraph"));
        assertEquals("Second imported paragraph.", listOfMaps(hit.get("searchList")).get(0).get("snippet"));

        Map<String, Object> docSegment = new LinkedHashMap<String, Object>();
        docSegment.put("segmentMethod", "1");
        docSegment.put("maxSplitter", 500);
        docSegment.put("subMaxSplitter", 20);
        Map<String, Object> updateConfigRequest = new LinkedHashMap<String, Object>();
        updateConfigRequest.put("knowledgeId", knowledgeId);
        updateConfigRequest.put("docIdList", Collections.singletonList("doc-reimport"));
        updateConfigRequest.put("docSegment", docSegment);
        service.updateDocConfig("dev-admin", "default-org", updateConfigRequest);

        Map<String, Object> parentPage = service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-reimport", "First imported"));
        Map<String, Object> parent = listOfMaps(parentPage.get("contentList")).get(0);
        assertEquals(true, parent.get("isParent"));
        assertEquals(2, parent.get("childNum"));
        Map<String, Object> childPage = service.listDocChildSegments("dev-admin", "default-org",
                childList("doc-reimport", (String) parent.get("contentId")));
        assertEquals(2, listOfMaps(childPage.get("contentList")).size());
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
    public void knowledgeHitRanksAllCandidatesBeforeApplyingTopK() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Ranking KB", 0)).get("knowledgeId");
        service.importDocs("dev-admin", "default-org",
                docImportWithContent(knowledgeId, "doc-low", "RankingGuide.txt",
                        "This paragraph mentions general platform usage."));
        service.importDocs("dev-admin", "default-org",
                docImportWithContent(knowledgeId, "doc-high", "Guide.txt",
                        "Ranking signal should win even when imported later."));

        Map<String, Object> request = knowledgeHit(knowledgeId, "Ranking");
        map(request.get("knowledgeMatchParams")).put("topK", 1);

        Map<String, Object> hit = service.hitKnowledge("dev-admin", "default-org", request);
        List<Map<String, Object>> searchList = listOfMaps(hit.get("searchList"));

        assertEquals(1, searchList.size());
        assertEquals("Guide.txt", searchList.get(0).get("title"));
        assertEquals("Ranking signal should win even when imported later.", searchList.get(0).get("snippet"));
        assertEquals(1.0D, ((List<Double>) hit.get("score")).get(0));
    }

    @Test
    public void knowledgeHitRanksDocInfoFallbackWithSegmentCandidates() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Upload Ranking KB", 0)).get("knowledgeId");
        service.importDocs("dev-admin", "default-org",
                docImportWithContent(knowledgeId, "doc-low", "Existing.txt",
                        "This existing segment does not contain the query."));

        Map<String, Object> request = knowledgeHit(knowledgeId, "Upload Ranking");
        map(request.get("knowledgeMatchParams")).put("topK", 1);
        request.put("docInfoList", Collections.singletonList(docInfo("doc-upload", "Upload Ranking.pdf")));

        Map<String, Object> hit = service.hitKnowledge("dev-admin", "default-org", request);
        List<Map<String, Object>> searchList = listOfMaps(hit.get("searchList"));

        assertEquals(1, searchList.size());
        assertEquals("Upload Ranking.pdf", searchList.get(0).get("title"));
        assertEquals(0.80D, ((List<Double>) hit.get("score")).get(0));
    }

    @Test
    public void docChildSegmentsFollowFrontendAndGoContract() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Child KB", 0)).get("knowledgeId");
        service.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-child", "ChildGuide.txt"));
        service.createDocSegment("dev-admin", "default-org",
                createSegment("doc-child", "Parent section for child chunks.", Collections.singletonList("parent")));

        Map<String, Object> parentPage = service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-child", "Parent section"));
        Map<String, Object> parent = listOfMaps(parentPage.get("contentList")).get(0);
        String parentId = (String) parent.get("contentId");
        assertEquals(false, parent.get("isParent"));
        assertEquals(0, parent.get("childNum"));

        service.createDocChildSegment("dev-admin", "default-org",
                childCreate("doc-child", parentId, "Child insight one", "Child insight two"));
        Map<String, Object> childList = service.listDocChildSegments("dev-admin", "default-org",
                childList("doc-child", parentId));
        List<Map<String, Object>> children = listOfMaps(childList.get("contentList"));
        assertEquals(2, children.size());
        assertNotNull(children.get(0).get("childId"));
        assertEquals(1, children.get(0).get("childNum"));
        assertEquals(parentId, children.get(0).get("parentId"));

        Map<String, Object> parentAfterCreate = listOfMaps(service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-child", "Parent section")).get("contentList")).get(0);
        assertEquals(true, parentAfterCreate.get("isParent"));
        assertEquals(2, parentAfterCreate.get("childNum"));

        service.updateDocChildSegment("dev-admin", "default-org",
                childUpdate("doc-child", parentId, 1, "Updated child insight"));
        Map<String, Object> hit = service.hitKnowledge("dev-admin", "default-org",
                knowledgeHit(knowledgeId, "Updated child insight"));
        Map<String, Object> hitItem = listOfMaps(hit.get("searchList")).get(0);
        assertEquals("Parent section for child chunks.", hitItem.get("snippet"));
        assertEquals(2, listOfMaps(hitItem.get("childContentList")).size());
        assertEquals(2, ((List<Double>) hitItem.get("childScore")).size());
        assertEquals(0.95D, ((List<Double>) hit.get("score")).get(0));

        service.deleteDocChildSegment("dev-admin", "default-org", childDelete("doc-child", parentId, 1));
        List<Map<String, Object>> remaining = listOfMaps(service.listDocChildSegments("dev-admin", "default-org",
                childList("doc-child", parentId)).get("contentList"));
        assertEquals(1, remaining.size());
        assertEquals(1, remaining.get(0).get("childNum"));
        assertEquals("Child insight two", remaining.get(0).get("content"));
    }

    @Test
    public void knowledgeGraphReflectsLocalDocsSegmentsTagsAndKeywords() {
        String knowledgeId = (String) service.createKnowledge("dev-admin", "default-org",
                createKnowledge("Graph KB", 0)).get("knowledgeId");
        String tagId = (String) service.createTag("dev-admin", "default-org", singleton("tagName", "GraphTag"))
                .get("tagId");
        service.bindTags("dev-admin", "default-org", bindTags(knowledgeId, tagId));
        service.createKeyword("dev-admin", "default-org",
                keywordCreate("Graph keyword", "Graph alias", knowledgeId));
        service.importDocs("dev-admin", "default-org", docImport(knowledgeId, "doc-graph", "GraphGuide.txt"));
        service.createDocSegment("dev-admin", "default-org",
                createSegment("doc-graph", "Graph segment explains Wanwu relationships.",
                        Collections.singletonList("graph-label")));

        Map<String, Object> parent = listOfMaps(service.listDocSegments("dev-admin", "default-org",
                segmentList("doc-graph", "Graph segment")).get("contentList")).get(0);
        service.createDocChildSegment("dev-admin", "default-org",
                childCreate("doc-graph", (String) parent.get("contentId"), "Child graph fact"));

        Map<String, Object> result = service.getKnowledgeGraph("dev-admin", "default-org",
                singleton("knowledgeId", knowledgeId));
        assertEquals(1, result.get("total"));
        assertEquals(1, result.get("successCount"));

        Map<String, Object> graph = map(result.get("graph"));
        List<Map<String, Object>> nodes = listOfMaps(graph.get("nodes"));
        List<Map<String, Object>> edges = listOfMaps(graph.get("edges"));
        assertTrue(containsNode(nodes, "Knowledge: Graph KB", "knowledge"));
        assertTrue(containsNode(nodes, "Document: GraphGuide.txt", "document"));
        assertTrue(containsNode(nodes, "Tag: GraphTag", "tag"));
        assertTrue(containsNode(nodes, "Keyword: Graph keyword", "keyword"));
        assertTrue(containsNode(nodes, "Label: graph-label", "label"));
        assertTrue(containsNode(nodes, "Child Segment: Child graph fact", "child_segment"));
        assertTrue(containsEdge(edges, "Knowledge: Graph KB", "Document: GraphGuide.txt"));
        assertTrue(containsEdge(edges, "Document: GraphGuide.txt",
                "Segment: Graph segment explains Wanwu relationships."));
        assertTrue(containsEdge(edges, "Segment: Graph segment explains Wanwu relationships.",
                "Child Segment: Child graph fact"));
        assertTrue(stringList(map(graph.get("graph")).get("source_id")).contains("doc-graph"));
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

    private Map<String, Object> docImportWithContent(String knowledgeId, String docId, String docName, String content) {
        Map<String, Object> request = docImport(knowledgeId, docId, docName);
        map(listOfMaps(request.get("docInfoList")).get(0)).put("content", content);
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

    private Map<String, Object> metaDefinition(String knowledgeId, String metaKey, String metaValueType) {
        Map<String, Object> meta = new LinkedHashMap<String, Object>();
        meta.put("metaKey", metaKey);
        meta.put("metaValueType", metaValueType);
        meta.put("option", "add");
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("docId", "");
        request.put("metaDataList", Collections.singletonList(meta));
        return request;
    }

    private Map<String, Object> metaValueUpdate(String knowledgeId, List<String> docIds,
                                                String metaId, String metaKey, String metaValue) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("metaId", metaId);
        item.put("metaKey", metaKey);
        item.put("metaValue", metaValue);
        item.put("metaValueType", "string");
        item.put("option", "add");
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("docIdList", docIds);
        request.put("applyToSelected", true);
        request.put("metaValueList", Collections.singletonList(item));
        return request;
    }

    private Map<String, Object> metaValueDelete(String knowledgeId, List<String> docIds,
                                                String metaId, String metaKey) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("metaId", metaId);
        item.put("metaKey", metaKey);
        item.put("option", "delete");
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("docIdList", docIds);
        request.put("applyToSelected", true);
        request.put("metaValueList", Collections.singletonList(item));
        return request;
    }

    private Map<String, Object> metaValueList(String knowledgeId, String... docIds) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("docIdList", java.util.Arrays.asList(docIds));
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

    private Map<String, Object> childList(String docId, String parentId) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("contentId", parentId);
        return request;
    }

    private Map<String, Object> childCreate(String docId, String parentId, String... contents) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("parentId", parentId);
        request.put("content", java.util.Arrays.asList(contents));
        return request;
    }

    private Map<String, Object> childUpdate(String docId, String parentId, int childNum, String content) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("parentId", parentId);
        Map<String, Object> childChunk = new LinkedHashMap<String, Object>();
        childChunk.put("chunkNo", childNum);
        childChunk.put("content", content);
        request.put("childChunk", childChunk);
        return request;
    }

    private Map<String, Object> childDelete(String docId, String parentId, int childNum) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("docId", docId);
        request.put("parentId", parentId);
        request.put("ChildChunkNoList", Collections.singletonList(childNum));
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

    private Map<String, Object> qaImportContent(String knowledgeId, String content) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        Map<String, Object> doc = new LinkedHashMap<String, Object>();
        doc.put("docName", "qa.csv");
        doc.put("content", content);
        request.put("docInfoList", Collections.singletonList(doc));
        return request;
    }

    private Map<String, Object> qaImportDocUrl(String knowledgeId, String docUrl) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("docInfoList", Collections.singletonList(singleton("docUrl", docUrl)));
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

    private Map<String, Object> reportBatchContent(String knowledgeId, String content) {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("knowledgeId", knowledgeId);
        request.put("content", content);
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

    private int docStatus(String knowledgeId, String docId) {
        Map<String, Object> page = service.listDocs("dev-admin", "default-org", docList(knowledgeId));
        for (Map<String, Object> doc : listOfMaps(page.get("list"))) {
            if (docId.equals(doc.get("docId"))) {
                return (Integer) doc.get("status");
            }
        }
        throw new AssertionError("doc not found: " + docId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object value) {
        return (List<Map<String, Object>>) value;
    }

    private boolean containsNode(List<Map<String, Object>> nodes, String name, String type) {
        for (Map<String, Object> node : nodes) {
            if (name.equals(node.get("entity_name")) && type.equals(node.get("entity_type"))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsEdge(List<Map<String, Object>> edges, String source, String target) {
        for (Map<String, Object> edge : edges) {
            if (source.equals(edge.get("source_entity")) && target.equals(edge.get("target_entity"))) {
                return true;
            }
        }
        return false;
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
