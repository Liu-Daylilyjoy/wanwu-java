package com.unicomai.wanwu.service.knowledge.rpc;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnowledgeServiceImplTest {

    private final KnowledgeServiceImpl service = new KnowledgeServiceImpl();

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
}
