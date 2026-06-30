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

        ArgumentCaptor<KnowledgeRecordEntity> captor = ArgumentCaptor.forClass(KnowledgeRecordEntity.class);
        verify(mapper, atLeastOnce()).upsertRecord(captor.capture());
        KnowledgeRecordEntity last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("snapshot", last.getRecordType());
        assertEquals("state", last.getRecordId());
        assertTrue(last.getPayload().contains("Persist KB"));
        assertTrue(last.getPayload().contains("doc-persist"));
        assertTrue(last.getPayload().contains("Persist?"));
        assertTrue(last.getPayload().contains("Persist keyword"));
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

        ArgumentCaptor<KnowledgeRecordEntity> captor = ArgumentCaptor.forClass(KnowledgeRecordEntity.class);
        verify(sourceMapper, atLeastOnce()).upsertRecord(captor.capture());
        String payload = captor.getAllValues().get(captor.getAllValues().size() - 1).getPayload();

        KnowledgeRecordMapper restartMapper = mock(KnowledgeRecordMapper.class);
        when(restartMapper.selectByType(eq("snapshot")))
                .thenReturn(Collections.singletonList(record("snapshot", "state", payload)));
        KnowledgeServiceImpl restarted = new KnowledgeServiceImpl(restartMapper);

        List<Map<String, Object>> knowledgeList = listOfMaps(restarted.selectKnowledge("dev-admin", "default-org",
                selectKnowledge("Restart", 0, -1)).get("knowledgeList"));
        assertEquals(1, knowledgeList.size());
        assertEquals(knowledgeId, knowledgeList.get(0).get("knowledgeId"));
        assertTrue((Boolean) listOfMaps(knowledgeList.get(0).get("knowledgeTagList")).get(0).get("selected"));
        assertEquals(1, restarted.listDocs("dev-admin", "default-org", docList(knowledgeId)).get("total"));
        assertEquals(1, restarted.listQaPairs("dev-admin", "default-org",
                qaPairList(knowledgeId, "Restart", Collections.singletonList(-1))).get("total"));
        assertEquals(1, restarted.listKeywords("dev-admin", "default-org", keywordList("Restart")).get("total"));

        Map<String, Object> next = restarted.createKnowledge("dev-admin", "default-org",
                createKnowledge("Restart Next", 0));
        assertEquals("knowledge-1002", next.get("knowledgeId"));
        Map<String, Object> nextKeyword = restarted.createKeyword("dev-admin", "default-org",
                keywordCreate("Restart next keyword", "Restart next alias", (String) next.get("knowledgeId")));
        assertEquals(1002L, nextKeyword.get("id"));
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
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("question", question);
        request.put("knowledgeList", Collections.singletonList(singleton("knowledgeId", knowledgeId)));
        request.put("knowledgeMatchParams", singleton("topK", 5));
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
