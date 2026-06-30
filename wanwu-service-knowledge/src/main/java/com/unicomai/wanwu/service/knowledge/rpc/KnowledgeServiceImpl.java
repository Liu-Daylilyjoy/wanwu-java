package com.unicomai.wanwu.service.knowledge.rpc;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final String DEFAULT_USER_ID = "dev-admin";
    private static final String DEFAULT_ORG_ID = "default-org";
    private static final String DEFAULT_ORG_NAME = "Default Organization";
    private static final String CREATED_AT = "2026-06-30 00:00:00";
    private static final int PERMISSION_READ = 0;
    private static final int PERMISSION_EDIT = 10;
    private static final int PERMISSION_ADMIN = 20;
    private static final int CATEGORY_KNOWLEDGE = 0;
    private static final int QA_STATUS_FINISHED = 2;
    private static final int EXTERNAL_ALL = -1;
    private static final int EXTERNAL_INTERNAL = 0;
    private static final int DOC_STATUS_FINISHED = 1;

    private final Map<String, KnowledgeState> knowledgeBases = new LinkedHashMap<String, KnowledgeState>();
    private final Map<String, TagState> tags = new LinkedHashMap<String, TagState>();
    private final Map<String, SplitterState> splitters = new LinkedHashMap<String, SplitterState>();
    private final Map<String, List<String>> tagIdsByKnowledgeId = new LinkedHashMap<String, List<String>>();
    private final Map<String, List<DocState>> docsByKnowledgeId = new LinkedHashMap<String, List<DocState>>();
    private final Map<String, List<QaPairState>> qaPairsByKnowledgeId = new LinkedHashMap<String, List<QaPairState>>();
    private final Map<String, QaPairState> qaPairsById = new LinkedHashMap<String, QaPairState>();
    private final Map<String, List<Map<String, Object>>> metasByKnowledgeId = new LinkedHashMap<String, List<Map<String, Object>>>();
    private final Map<String, List<PermissionState>> permissionsByKnowledgeId = new LinkedHashMap<String, List<PermissionState>>();

    private final AtomicLong nextKnowledgeId = new AtomicLong(1000);
    private final AtomicLong nextTagId = new AtomicLong(1000);
    private final AtomicLong nextSplitterId = new AtomicLong(1000);
    private final AtomicLong nextDocId = new AtomicLong(1000);
    private final AtomicLong nextQaPairId = new AtomicLong(1000);
    private final AtomicLong nextMetaId = new AtomicLong(1000);
    private final AtomicLong nextPermissionId = new AtomicLong(1000);

    public KnowledgeServiceImpl() {
        seedSplitters();
    }

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    @Override
    public synchronized Map<String, Object> selectKnowledge(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String name = string(safe.get("name"));
        int category = intValue(safe.get("category"), CATEGORY_KNOWLEDGE);
        int external = intValue(safe.get("external"), EXTERNAL_ALL);
        Set<String> requiredTagIds = new LinkedHashSet<String>(stringList(safe.get("tagId")));

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (KnowledgeState knowledge : knowledgeBases.values()) {
            if (!matchesOwner(orgId, knowledge)) {
                continue;
            }
            if (!isBlank(name) && !containsIgnoreCase(knowledge.name, name)) {
                continue;
            }
            if (category >= 0 && category != knowledge.category) {
                continue;
            }
            if (external != EXTERNAL_ALL && external != knowledge.external) {
                continue;
            }
            if (!requiredTagIds.isEmpty() && !tagIds(knowledge.knowledgeId).containsAll(requiredTagIds)) {
                continue;
            }
            result.add(toKnowledgeInfo(knowledge, userId));
        }
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("knowledgeList", result);
        return response;
    }

    @Override
    public synchronized Map<String, Object> createKnowledge(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String name = string(safe.get("name"));
        if (isBlank(name)) {
            throw new IllegalArgumentException("knowledge name cannot be empty");
        }
        String knowledgeId = "knowledge-" + nextKnowledgeId.incrementAndGet();
        KnowledgeState knowledge = new KnowledgeState();
        knowledge.knowledgeId = knowledgeId;
        knowledge.userId = defaultIfBlank(userId, DEFAULT_USER_ID);
        knowledge.orgId = defaultIfBlank(orgId, DEFAULT_ORG_ID);
        knowledge.orgName = DEFAULT_ORG_NAME;
        knowledge.name = name;
        knowledge.description = string(safe.get("description"));
        knowledge.category = intValue(safe.get("category"), CATEGORY_KNOWLEDGE);
        knowledge.embeddingModelId = embeddingModelId(safe.get("embeddingModelInfo"));
        knowledge.graphSwitch = graphSwitch(safe.get("knowledgeGraph")) ? 1 : 0;
        knowledge.llmModelId = graphModelId(safe.get("knowledgeGraph"));
        knowledge.avatar = avatar(safe.get("avatar"));
        knowledge.external = EXTERNAL_INTERNAL;
        knowledge.createdAt = CREATED_AT;
        knowledge.updatedAt = CREATED_AT;
        knowledgeBases.put(knowledgeId, knowledge);
        tagIdsByKnowledgeId.put(knowledgeId, new ArrayList<String>());
        docsByKnowledgeId.put(knowledgeId, new ArrayList<DocState>());
        qaPairsByKnowledgeId.put(knowledgeId, new ArrayList<QaPairState>());
        metasByKnowledgeId.put(knowledgeId, new ArrayList<Map<String, Object>>());
        permissionsByKnowledgeId.put(knowledgeId, new ArrayList<PermissionState>());
        addOwnerPermission(knowledgeId, knowledge.userId, knowledge.orgId);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("knowledgeId", knowledgeId);
        return response;
    }

    @Override
    public synchronized void updateKnowledge(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        KnowledgeState knowledge = existingKnowledge(string(safe.get("knowledgeId")));
        String name = string(safe.get("name"));
        if (isBlank(name)) {
            throw new IllegalArgumentException("knowledge name cannot be empty");
        }
        knowledge.name = name;
        knowledge.description = string(safe.get("description"));
        knowledge.avatar = avatar(safe.get("avatar"));
        knowledge.updatedAt = CREATED_AT;
    }

    @Override
    public synchronized void deleteKnowledge(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        existingKnowledge(knowledgeId);
        knowledgeBases.remove(knowledgeId);
        tagIdsByKnowledgeId.remove(knowledgeId);
        docsByKnowledgeId.remove(knowledgeId);
        for (QaPairState pair : qaPairs(knowledgeId)) {
            qaPairsById.remove(pair.qaPairId);
        }
        qaPairsByKnowledgeId.remove(knowledgeId);
        metasByKnowledgeId.remove(knowledgeId);
        permissionsByKnowledgeId.remove(knowledgeId);
    }

    @Override
    public Map<String, Object> hitKnowledge(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("prompt", "");
        result.put("searchList", Collections.emptyList());
        result.put("score", Collections.emptyList());
        result.put("useGraph", Boolean.FALSE);
        return result;
    }

    @Override
    public synchronized Map<String, Object> listTags(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        String tagName = string(safe.get("tagName"));
        List<String> selected = isBlank(knowledgeId) ? Collections.<String>emptyList() : tagIds(knowledgeId);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (TagState tag : tags.values()) {
            if (!isBlank(tagName) && !containsIgnoreCase(tag.tagName, tagName)) {
                continue;
            }
            result.add(toTagInfo(tag, selected.contains(tag.tagId)));
        }
        return singleton("knowledgeTagList", result);
    }

    @Override
    public synchronized Map<String, Object> createTag(String userId, String orgId, Map<String, Object> request) {
        String tagName = string(safe(request).get("tagName"));
        if (isBlank(tagName)) {
            throw new IllegalArgumentException("tagName cannot be empty");
        }
        String tagId = "tag-" + nextTagId.incrementAndGet();
        TagState tag = new TagState();
        tag.tagId = tagId;
        tag.tagName = tagName;
        tag.userId = defaultIfBlank(userId, DEFAULT_USER_ID);
        tag.orgId = defaultIfBlank(orgId, DEFAULT_ORG_ID);
        tags.put(tagId, tag);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("tagId", tagId);
        result.put("knowledgeId", tagId);
        return result;
    }

    @Override
    public synchronized void updateTag(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        TagState tag = existingTag(string(safe.get("tagId")));
        String tagName = string(safe.get("tagName"));
        if (isBlank(tagName)) {
            throw new IllegalArgumentException("tagName cannot be empty");
        }
        tag.tagName = tagName;
    }

    @Override
    public synchronized void deleteTag(String userId, String orgId, Map<String, Object> request) {
        String tagId = string(safe(request).get("tagId"));
        existingTag(tagId);
        tags.remove(tagId);
        for (List<String> ids : tagIdsByKnowledgeId.values()) {
            ids.remove(tagId);
        }
    }

    @Override
    public synchronized Map<String, Object> countTagBindings(String userId, String orgId, Map<String, Object> request) {
        String tagId = string(safe(request).get("tagId"));
        int count = 0;
        for (List<String> ids : tagIdsByKnowledgeId.values()) {
            if (ids.contains(tagId)) {
                count++;
            }
        }
        return singleton("tagBindCount", count);
    }

    @Override
    public synchronized void bindTags(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        List<String> target = tagIds(knowledgeId);
        target.clear();
        for (String tagId : stringList(safe.get("tagIdList"))) {
            if (tags.containsKey(tagId)) {
                target.add(tagId);
            }
        }
    }

    @Override
    public synchronized Map<String, Object> listSplitters(String userId, String orgId, Map<String, Object> request) {
        String splitterName = string(safe(request).get("splitterName"));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (SplitterState splitter : splitters.values()) {
            if (!isBlank(splitterName) && !containsIgnoreCase(splitter.splitterName, splitterName)
                    && !containsIgnoreCase(splitter.splitterValue, splitterName)) {
                continue;
            }
            result.add(toSplitterInfo(splitter));
        }
        return singleton("knowledgeSplitterList", result);
    }

    @Override
    public synchronized Map<String, Object> createSplitter(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String splitterName = string(safe.get("splitterName"));
        String splitterValue = string(safe.get("splitterValue"));
        if (isBlank(splitterName)) {
            throw new IllegalArgumentException("splitterName cannot be empty");
        }
        String splitterId = "splitter-" + nextSplitterId.incrementAndGet();
        SplitterState splitter = new SplitterState();
        splitter.splitterId = splitterId;
        splitter.splitterName = splitterName;
        splitter.splitterValue = defaultIfBlank(splitterValue, splitterName);
        splitter.type = "custom";
        splitters.put(splitterId, splitter);
        return singleton("splitterId", splitterId);
    }

    @Override
    public synchronized void updateSplitter(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        SplitterState splitter = existingSplitter(string(safe.get("splitterId")));
        if ("preset".equals(splitter.type)) {
            return;
        }
        splitter.splitterName = defaultIfBlank(string(safe.get("splitterName")), splitter.splitterName);
        splitter.splitterValue = defaultIfBlank(string(safe.get("splitterValue")), splitter.splitterValue);
    }

    @Override
    public synchronized void deleteSplitter(String userId, String orgId, Map<String, Object> request) {
        SplitterState splitter = existingSplitter(string(safe(request).get("splitterId")));
        if (!"preset".equals(splitter.type)) {
            splitters.remove(splitter.splitterId);
        }
    }

    @Override
    public synchronized Map<String, Object> listDocs(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        KnowledgeState knowledge = existingKnowledge(knowledgeId);
        String docName = string(safe.get("docName"));
        Set<String> docIdList = new LinkedHashSet<String>(stringList(safe.get("docIdList")));
        List<DocState> docs = docs(knowledgeId);
        List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();
        for (DocState doc : docs) {
            if (!isBlank(docName) && !containsIgnoreCase(doc.docName, docName)) {
                continue;
            }
            if (!docIdList.isEmpty() && !docIdList.contains(doc.docId)) {
                continue;
            }
            filtered.add(toDocInfo(doc));
        }
        int pageNo = intValue(safe.get("pageNo"), 1);
        int pageSize = intValue(safe.get("pageSize"), 10);
        Map<String, Object> result = page(filtered, pageNo, pageSize);
        result.put("docKnowledgeInfo", toDocKnowledgeInfo(knowledge));
        return result;
    }

    @Override
    public Map<String, Object> getDocConfig(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("docImportType", 0);
        result.put("docSegment", docSegment());
        result.put("docAnalyzer", Collections.singletonList("text"));
        result.put("parserModelId", "");
        result.put("asrModelId", "");
        result.put("multimodalModelId", "");
        result.put("docPreprocess", Collections.emptyList());
        return result;
    }

    @Override
    public synchronized Map<String, Object> getDocImportTip(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        KnowledgeState knowledge = existingKnowledge(knowledgeId);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("msg", "");
        result.put("uploadstatus", 2);
        result.put("knowledgeId", knowledgeId);
        result.put("knowledgeName", knowledge.name);
        return result;
    }

    @Override
    public Map<String, Object> getDocUploadLimit(String userId, String orgId, Map<String, Object> request) {
        List<Map<String, Object>> limits = new ArrayList<Map<String, Object>>();
        limits.add(uploadLimit("document", 50, "txt", "pdf", "doc", "docx", "md", "xlsx", "xls", "csv"));
        limits.add(uploadLimit("image", 20, "png", "jpg", "jpeg", "bmp", "webp"));
        limits.add(uploadLimit("video", 200, "mp4", "mov", "avi"));
        return singleton("uploadLimitList", limits);
    }

    @Override
    public synchronized Map<String, Object> listDocSegments(String userId, String orgId, Map<String, Object> request) {
        String docId = string(safe(request).get("docId"));
        DocState doc = findDoc(docId);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("fileName", doc == null ? "" : doc.docName);
        result.put("pageTotal", 0);
        result.put("segmentTotalNum", 0);
        result.put("maxSegmentSize", 0);
        result.put("segmentType", "0");
        result.put("uploadTime", doc == null ? CREATED_AT : doc.uploadTime);
        result.put("splitter", "");
        result.put("metaDataList", Collections.emptyList());
        result.put("contentList", Collections.emptyList());
        result.put("segmentImportStatus", "");
        result.put("segmentMethod", doc == null ? "0" : doc.segmentMethod);
        result.put("docAnalyzerText", Collections.singletonList(singleton("text", "text")));
        return result;
    }

    @Override
    public Map<String, Object> listDocChildSegments(String userId, String orgId, Map<String, Object> request) {
        return singleton("contentList", Collections.emptyList());
    }

    @Override
    public Map<String, Object> analyzeDocUrls(String userId, String orgId, Map<String, Object> request) {
        List<Map<String, Object>> urls = new ArrayList<Map<String, Object>>();
        for (String url : stringList(safe(request).get("urlList"))) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("url", url);
            item.put("fileName", url);
            item.put("fileSize", 0);
            urls.add(item);
        }
        return singleton("urlList", urls);
    }

    @Override
    public synchronized void importDocs(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        String segmentMethod = string(map(safe.get("docSegment")).get("segmentMethod"));
        if (isBlank(segmentMethod)) {
            segmentMethod = "0";
        }
        for (Object raw : list(safe.get("docInfoList"))) {
            Map<String, Object> docInfo = map(raw);
            DocState doc = new DocState();
            doc.docId = defaultIfBlank(string(docInfo.get("docId")), "doc-" + nextDocId.incrementAndGet());
            doc.docName = defaultIfBlank(string(docInfo.get("docName")), doc.docId);
            doc.docType = defaultIfBlank(string(docInfo.get("docType")), fileType(doc.docName));
            doc.knowledgeId = knowledgeId;
            doc.uploadTime = CREATED_AT;
            doc.status = DOC_STATUS_FINISHED;
            doc.fileSize = longValue(docInfo.get("docSize"), 0L);
            doc.segmentMethod = segmentMethod;
            doc.author = defaultIfBlank(userId, DEFAULT_USER_ID);
            doc.graphStatus = 0;
            docs(knowledgeId).add(doc);
        }
    }

    @Override
    public void updateDocConfig(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void reimportDocs(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public synchronized void deleteDocs(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        List<String> docIds = stringList(safe(request).get("docIdList"));
        List<DocState> docs = docs(knowledgeId);
        for (int i = docs.size() - 1; i >= 0; i--) {
            if (docIds.contains(docs.get(i).docId)) {
                docs.remove(i);
            }
        }
    }

    @Override
    public synchronized void updateDocMeta(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        List<Map<String, Object>> metas = metas(knowledgeId);
        for (Object raw : list(safe.get("metaDataList"))) {
            Map<String, Object> meta = new LinkedHashMap<String, Object>(map(raw));
            String option = string(meta.get("option"));
            if ("add".equals(option)) {
                meta.put("metaId", "meta-" + nextMetaId.incrementAndGet());
                metas.add(meta);
            } else if ("update".equals(option)) {
                replaceMeta(metas, meta);
            } else if ("delete".equals(option)) {
                removeMeta(metas, string(meta.get("metaId")));
            }
        }
    }

    @Override
    public void batchUpdateDocMeta(String userId, String orgId, Map<String, Object> request) {
        updateDocMeta(userId, orgId, request);
    }

    @Override
    public void updateDocSegmentStatus(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void updateDocSegmentLabels(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void createDocSegment(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void batchCreateDocSegment(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void deleteDocSegment(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void updateDocSegment(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void createDocChildSegment(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void updateDocChildSegment(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void deleteDocChildSegment(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public synchronized Map<String, Object> selectMetaKeys(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> meta : metas(knowledgeId)) {
            result.add(new LinkedHashMap<String, Object>(meta));
        }
        return singleton("knowledgeMetaList", result);
    }

    @Override
    public synchronized Map<String, Object> listMetaValues(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> meta : metas(knowledgeId)) {
            Map<String, Object> value = new LinkedHashMap<String, Object>();
            value.put("metaId", meta.get("metaId"));
            value.put("metaKey", meta.get("metaKey"));
            value.put("metaValue", Collections.emptyList());
            value.put("metaValueType", meta.get("metaValueType"));
            result.add(value);
        }
        return singleton("knowledgeMetaValues", result);
    }

    @Override
    public Map<String, Object> getKnowledgeGraph(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> graph = new LinkedHashMap<String, Object>();
        graph.put("directed", true);
        graph.put("multigraph", false);
        graph.put("graph", singleton("source_id", Collections.emptyList()));
        graph.put("nodes", Collections.emptyList());
        graph.put("edges", Collections.emptyList());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("processingCount", 0);
        result.put("successCount", 0);
        result.put("failCount", 0);
        result.put("total", 0);
        result.put("graph", graph);
        return result;
    }

    @Override
    public Map<String, Object> listKnowledgeOrgs(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> org = new LinkedHashMap<String, Object>();
        org.put("orgId", DEFAULT_ORG_ID);
        org.put("orgName", DEFAULT_ORG_NAME);
        return singleton("knowOrgInfoList", Collections.singletonList(org));
    }

    @Override
    public synchronized Map<String, Object> listKnowledgeUsers(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (PermissionState permission : permissions(knowledgeId)) {
            result.add(toPermissionInfo(permission));
        }
        return singleton("knowledgeUserInfoList", result);
    }

    @Override
    public synchronized Map<String, Object> listUsersWithoutPermit(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        Set<String> permitted = new LinkedHashSet<String>();
        for (PermissionState permission : permissions(knowledgeId)) {
            permitted.add(permission.userId);
        }
        List<Map<String, Object>> users = new ArrayList<Map<String, Object>>();
        if (!permitted.contains("dev-app")) {
            Map<String, Object> app = new LinkedHashMap<String, Object>();
            app.put("userId", "dev-app");
            app.put("userName", "app");
            users.add(app);
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("orgId", DEFAULT_ORG_ID);
        result.put("orgName", DEFAULT_ORG_NAME);
        result.put("userInfoList", users);
        return result;
    }

    @Override
    public synchronized void addKnowledgeUsers(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        for (Object raw : list(safe.get("knowledgeUserList"))) {
            Map<String, Object> user = map(raw);
            PermissionState permission = new PermissionState();
            permission.permissionId = "perm-" + nextPermissionId.incrementAndGet();
            permission.userId = string(user.get("userId"));
            permission.userName = "dev-app".equals(permission.userId) ? "app" : permission.userId;
            permission.orgId = defaultIfBlank(string(user.get("orgId")), DEFAULT_ORG_ID);
            permission.orgName = DEFAULT_ORG_NAME;
            permission.permissionType = intValue(user.get("permissionType"), PERMISSION_READ);
            permission.transfer = false;
            permissions(knowledgeId).add(permission);
        }
    }

    @Override
    public synchronized void editKnowledgeUser(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> knowledgeUser = map(safe(request).get("knowledgeUser"));
        PermissionState permission = findPermission(string(knowledgeUser.get("permissionId")));
        if (permission != null) {
            permission.permissionType = intValue(knowledgeUser.get("permissionType"), permission.permissionType);
        }
    }

    @Override
    public synchronized void deleteKnowledgeUser(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        String permissionId = string(safe(request).get("permissionId"));
        List<PermissionState> permissions = permissions(knowledgeId);
        for (int i = permissions.size() - 1; i >= 0; i--) {
            if (permissionId.equals(permissions.get(i).permissionId)) {
                permissions.remove(i);
            }
        }
    }

    @Override
    public synchronized void transferKnowledgeAdmin(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        Map<String, Object> knowledgeUser = map(safe.get("knowledgeUser"));
        for (PermissionState permission : permissions(knowledgeId)) {
            permission.permissionType = PERMISSION_EDIT;
            permission.transfer = false;
        }
        PermissionState permission = new PermissionState();
        permission.permissionId = "perm-" + nextPermissionId.incrementAndGet();
        permission.userId = string(knowledgeUser.get("userId"));
        permission.userName = "dev-app".equals(permission.userId) ? "app" : permission.userId;
        permission.orgId = defaultIfBlank(string(knowledgeUser.get("orgId")), DEFAULT_ORG_ID);
        permission.orgName = DEFAULT_ORG_NAME;
        permission.permissionType = PERMISSION_ADMIN;
        permission.transfer = true;
        permissions(knowledgeId).add(permission);
    }

    @Override
    public Map<String, Object> listReports(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("list", Collections.emptyList());
        result.put("total", 0);
        result.put("pageNo", intValue(safe(request).get("pageNo"), 1));
        result.put("pageSize", intValue(safe(request).get("pageSize"), 10));
        return result;
    }

    @Override
    public void generateReport(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void deleteReport(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void updateReport(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void addReport(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public void batchAddReports(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public Map<String, Object> listExportRecords(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> result = page(Collections.<Map<String, Object>>emptyList(),
                intValue(safe(request).get("pageNo"), 1),
                intValue(safe(request).get("pageSize"), 10));
        return result;
    }

    @Override
    public void deleteExportRecord(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public synchronized Map<String, Object> getDocByName(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeName = string(safe.get("knowledgeName"));
        String docName = string(safe.get("docName"));
        for (KnowledgeState knowledge : knowledgeBases.values()) {
            if (!knowledge.name.equals(knowledgeName)) {
                continue;
            }
            for (DocState doc : docs(knowledge.knowledgeId)) {
                if (doc.docName.equals(docName)) {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("knowledgeId", knowledge.knowledgeId);
                    result.put("knowledgeName", knowledge.name);
                    result.put("id", doc.docId);
                    result.put("type", doc.docType);
                    result.put("name", doc.docName);
                    result.put("disable", false);
                    return result;
                }
            }
        }
        return new LinkedHashMap<String, Object>();
    }

    @Override
    public synchronized Map<String, Object> createQaPair(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        String qaPairId = "qa-" + nextQaPairId.incrementAndGet();
        QaPairState pair = new QaPairState();
        pair.qaPairId = qaPairId;
        pair.knowledgeId = knowledgeId;
        pair.question = string(safe.get("question")).trim();
        pair.answer = string(safe.get("answer")).trim();
        pair.userId = defaultIfBlank(userId, DEFAULT_USER_ID);
        pair.author = displayUserName(pair.userId);
        pair.uploadTime = CREATED_AT;
        pair.status = QA_STATUS_FINISHED;
        pair.enabled = true;
        pair.errorMsg = "";
        qaPairs(knowledgeId).add(pair);
        qaPairsById.put(qaPairId, pair);
        return singleton("qaPairId", qaPairId);
    }

    @Override
    public synchronized void updateQaPair(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        QaPairState pair = existingQaPair(string(safe.get("qaPairId")));
        pair.question = string(safe.get("question")).trim();
        pair.answer = string(safe.get("answer")).trim();
    }

    @Override
    public synchronized void updateQaPairSwitch(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        QaPairState pair = existingQaPair(string(safe.get("qaPairId")));
        pair.enabled = booleanValue(safe.get("switch"), pair.enabled);
    }

    @Override
    public synchronized void deleteQaPairs(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        Set<String> ids = new LinkedHashSet<String>(stringList(safe.get("QAPairIdList")));
        if (ids.isEmpty()) {
            ids.addAll(stringList(safe.get("qaPairIdList")));
        }
        List<QaPairState> pairs = qaPairs(knowledgeId);
        for (int i = pairs.size() - 1; i >= 0; i--) {
            QaPairState pair = pairs.get(i);
            if (ids.contains(pair.qaPairId)) {
                pairs.remove(i);
                qaPairsById.remove(pair.qaPairId);
            }
        }
    }

    @Override
    public synchronized Map<String, Object> listQaPairs(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        KnowledgeState knowledge = existingKnowledge(knowledgeId);
        String name = string(safe.get("name")).trim();
        String metaValue = string(safe.get("metaValue")).trim();
        List<Integer> statuses = intList(safe.get("status"));
        boolean allStatuses = statuses.isEmpty() || statuses.contains(-1);
        List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();
        for (QaPairState pair : qaPairs(knowledgeId)) {
            if (!isBlank(name) && !containsIgnoreCase(pair.question, name) && !containsIgnoreCase(pair.answer, name)) {
                continue;
            }
            if (!allStatuses && !statuses.contains(pair.status)) {
                continue;
            }
            if (!isBlank(metaValue) && !qaMetaContains(pair, metaValue)) {
                continue;
            }
            filtered.add(toQaPairInfo(pair));
        }
        Map<String, Object> result = page(filtered,
                intValue(safe.get("pageNo"), 1),
                intValue(safe.get("pageSize"), 10));
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        info.put("knowledgeId", knowledge.knowledgeId);
        info.put("knowledgeName", knowledge.name);
        result.put("qaKnowledgeInfo", info);
        return result;
    }

    @Override
    public synchronized void importQaPairs(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        for (Object rawDoc : list(safe.get("docInfoList"))) {
            Map<String, Object> doc = map(rawDoc);
            String docName = defaultIfBlank(string(doc.get("docName")), string(doc.get("name")));
            if (isBlank(docName)) {
                continue;
            }
            Map<String, Object> create = new LinkedHashMap<String, Object>();
            create.put("knowledgeId", knowledgeId);
            create.put("question", "Imported from " + docName);
            create.put("answer", "");
            createQaPair(userId, orgId, create);
        }
    }

    @Override
    public synchronized Map<String, Object> getQaImportTip(String userId, String orgId, Map<String, Object> request) {
        KnowledgeState knowledge = existingKnowledge(string(safe(request).get("knowledgeId")));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("msg", "");
        result.put("uploadstatus", QA_STATUS_FINISHED);
        result.put("knowledgeId", knowledge.knowledgeId);
        result.put("knowledgeName", knowledge.name);
        return result;
    }

    @Override
    public synchronized Map<String, Object> exportQaPairs(String userId, String orgId, Map<String, Object> request) {
        KnowledgeState knowledge = existingKnowledge(string(safe(request).get("knowledgeId")));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("knowledgeId", knowledge.knowledgeId);
        result.put("knowledgeName", knowledge.name);
        result.put("recordCreated", true);
        result.put("fileUrl", "");
        result.put("downloadUrl", "");
        return result;
    }

    @Override
    public synchronized Map<String, Object> hitQaPairs(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String question = string(safe.get("question"));
        int topK = intValue(map(safe.get("knowledgeMatchParams")).get("topK"), 5);
        Set<String> knowledgeIds = new LinkedHashSet<String>();
        for (Object raw : list(safe.get("knowledgeList"))) {
            String knowledgeId = string(map(raw).get("knowledgeId"));
            if (!isBlank(knowledgeId)) {
                knowledgeIds.add(knowledgeId);
            }
        }
        if (knowledgeIds.isEmpty()) {
            knowledgeIds.addAll(qaPairsByKnowledgeId.keySet());
        }

        List<Map<String, Object>> searchList = new ArrayList<Map<String, Object>>();
        List<Double> scores = new ArrayList<Double>();
        for (String knowledgeId : knowledgeIds) {
            KnowledgeState knowledge = knowledgeBases.get(knowledgeId);
            if (knowledge == null) {
                continue;
            }
            for (QaPairState pair : qaPairs(knowledgeId)) {
                if (!pair.enabled || pair.status != QA_STATUS_FINISHED) {
                    continue;
                }
                if (!isBlank(question) && !containsIgnoreCase(pair.question, question)
                        && !containsIgnoreCase(pair.answer, question)) {
                    continue;
                }
                searchList.add(toQaHitInfo(pair, knowledge));
                scores.add(1.0D);
                if (searchList.size() >= topK) {
                    return qaHitResult(searchList, scores);
                }
            }
        }
        return qaHitResult(searchList, scores);
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.KNOWLEDGE, "Knowledge Service", "knowledge");
    }

    private void seedSplitters() {
        putPresetSplitter("preset-paragraph", "paragraph", "\n\n");
        putPresetSplitter("preset-line", "line", "\n");
        putPresetSplitter("preset-sentence", "sentence", "。");
    }

    private void putPresetSplitter(String id, String name, String value) {
        SplitterState splitter = new SplitterState();
        splitter.splitterId = id;
        splitter.splitterName = name;
        splitter.splitterValue = value;
        splitter.type = "preset";
        splitters.put(id, splitter);
    }

    private void addOwnerPermission(String knowledgeId, String userId, String orgId) {
        PermissionState permission = new PermissionState();
        permission.permissionId = "perm-" + nextPermissionId.incrementAndGet();
        permission.userId = userId;
        permission.userName = "dev-admin".equals(userId) ? "admin" : userId;
        permission.orgId = orgId;
        permission.orgName = DEFAULT_ORG_NAME;
        permission.permissionType = PERMISSION_ADMIN;
        permission.transfer = true;
        permissions(knowledgeId).add(permission);
    }

    private Map<String, Object> toKnowledgeInfo(KnowledgeState knowledge, String userId) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("knowledgeId", knowledge.knowledgeId);
        result.put("name", knowledge.name);
        result.put("orgName", knowledge.orgName);
        result.put("description", knowledge.description);
        result.put("docCount", docs(knowledge.knowledgeId).size());
        result.put("embeddingModelInfo", singleton("modelId", knowledge.embeddingModelId));
        result.put("knowledgeTagList", tagListForKnowledge(knowledge.knowledgeId));
        result.put("createUserId", knowledge.userId);
        result.put("createAt", knowledge.createdAt);
        result.put("permissionType", permissionForUser(knowledge.knowledgeId, userId));
        result.put("share", false);
        result.put("ragName", knowledge.name);
        result.put("graphSwitch", knowledge.graphSwitch);
        result.put("category", knowledge.category);
        result.put("llmModelId", knowledge.llmModelId);
        result.put("updatedAt", knowledge.updatedAt);
        result.put("external", knowledge.external);
        result.put("externalKnowledgeInfo", null);
        result.put("avatar", new LinkedHashMap<String, Object>(knowledge.avatar));
        return result;
    }

    private Map<String, Object> toDocKnowledgeInfo(KnowledgeState knowledge) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("knowledgeId", knowledge.knowledgeId);
        result.put("knowledgeName", knowledge.name);
        result.put("graphSwitch", knowledge.graphSwitch);
        result.put("showGraphReport", knowledge.graphSwitch == 1);
        result.put("description", knowledge.description);
        result.put("keywords", Collections.emptyList());
        result.put("embeddingModel", modelInfo(knowledge.embeddingModelId, "Text Embedding Small", "embedding"));
        result.put("llmModelId", knowledge.llmModelId);
        result.put("category", knowledge.category);
        result.put("avatar", new LinkedHashMap<String, Object>(knowledge.avatar));
        return result;
    }

    private Map<String, Object> modelInfo(String modelId, String displayName, String modelType) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("modelId", modelId);
        result.put("displayName", displayName);
        result.put("modelType", modelType);
        result.put("tags", Collections.singletonList(singleton("text", modelType)));
        return result;
    }

    private List<Map<String, Object>> tagListForKnowledge(String knowledgeId) {
        List<String> selected = tagIds(knowledgeId);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (TagState tag : tags.values()) {
            result.add(toTagInfo(tag, selected.contains(tag.tagId)));
        }
        return result;
    }

    private Map<String, Object> toTagInfo(TagState tag, boolean selected) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("tagId", tag.tagId);
        result.put("tagName", tag.tagName);
        result.put("selected", selected);
        return result;
    }

    private Map<String, Object> toSplitterInfo(SplitterState splitter) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("splitterId", splitter.splitterId);
        result.put("splitterName", splitter.splitterName);
        result.put("splitterValue", splitter.splitterValue);
        result.put("type", splitter.type);
        return result;
    }

    private Map<String, Object> toDocInfo(DocState doc) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("docId", doc.docId);
        result.put("docName", doc.docName);
        result.put("docType", doc.docType);
        result.put("knowledgeId", doc.knowledgeId);
        result.put("uploadTime", doc.uploadTime);
        result.put("status", doc.status);
        result.put("errorMsg", "");
        result.put("fileSize", doc.fileSize);
        result.put("segmentMethod", doc.segmentMethod);
        result.put("author", doc.author);
        result.put("graphStatus", doc.graphStatus);
        result.put("graphErrMsg", "");
        result.put("isMultimodal", false);
        return result;
    }

    private Map<String, Object> toQaPairInfo(QaPairState pair) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("qaPairId", pair.qaPairId);
        result.put("knowledgeId", pair.knowledgeId);
        result.put("question", pair.question);
        result.put("answer", pair.answer);
        result.put("metaDataList", new ArrayList<Map<String, Object>>(pair.metaDataList));
        result.put("author", pair.author);
        result.put("uploadTime", pair.uploadTime);
        result.put("status", pair.status);
        result.put("switch", pair.enabled);
        result.put("errorMsg", pair.errorMsg);
        return result;
    }

    private Map<String, Object> toQaHitInfo(QaPairState pair, KnowledgeState knowledge) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("title", pair.question);
        result.put("question", pair.question);
        result.put("answer", pair.answer);
        result.put("qaPairId", pair.qaPairId);
        result.put("qaBase", knowledge.name);
        result.put("qaId", knowledge.knowledgeId);
        result.put("contentType", "qa");
        return result;
    }

    private Map<String, Object> qaHitResult(List<Map<String, Object>> searchList, List<Double> scores) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("searchList", searchList);
        result.put("score", scores);
        return result;
    }

    private Map<String, Object> toPermissionInfo(PermissionState permission) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("userId", permission.userId);
        result.put("userName", permission.userName);
        result.put("orgId", permission.orgId);
        result.put("orgName", permission.orgName);
        result.put("permissionType", permission.permissionType);
        result.put("permissionId", permission.permissionId);
        result.put("transfer", permission.transfer);
        return result;
    }

    private Map<String, Object> docSegment() {
        Map<String, Object> segment = new LinkedHashMap<String, Object>();
        segment.put("segmentMethod", "0");
        segment.put("segmentType", "0");
        segment.put("splitter", Collections.emptyList());
        segment.put("maxSplitter", 500);
        segment.put("overlap", 0);
        segment.put("subSplitter", Collections.emptyList());
        segment.put("subMaxSplitter", 200);
        return segment;
    }

    private Map<String, Object> uploadLimit(String fileType, int maxSize, String... ext) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("fileType", fileType);
        result.put("maxSize", maxSize);
        result.put("extList", Arrays.asList(ext));
        return result;
    }

    private Map<String, Object> page(List<Map<String, Object>> all, int pageNo, int pageSize) {
        int safePageNo = pageNo <= 0 ? 1 : pageNo;
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int from = Math.min((safePageNo - 1) * safePageSize, all.size());
        int to = Math.min(from + safePageSize, all.size());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("list", new ArrayList<Map<String, Object>>(all.subList(from, to)));
        result.put("total", all.size());
        result.put("pageNo", safePageNo);
        result.put("pageSize", safePageSize);
        return result;
    }

    private KnowledgeState existingKnowledge(String knowledgeId) {
        if (isBlank(knowledgeId) || !knowledgeBases.containsKey(knowledgeId)) {
            throw new IllegalArgumentException("knowledge not found");
        }
        return knowledgeBases.get(knowledgeId);
    }

    private TagState existingTag(String tagId) {
        if (isBlank(tagId) || !tags.containsKey(tagId)) {
            throw new IllegalArgumentException("tag not found");
        }
        return tags.get(tagId);
    }

    private SplitterState existingSplitter(String splitterId) {
        if (isBlank(splitterId) || !splitters.containsKey(splitterId)) {
            throw new IllegalArgumentException("splitter not found");
        }
        return splitters.get(splitterId);
    }

    private DocState findDoc(String docId) {
        if (isBlank(docId)) {
            return null;
        }
        for (List<DocState> docs : docsByKnowledgeId.values()) {
            for (DocState doc : docs) {
                if (doc.docId.equals(docId)) {
                    return doc;
                }
            }
        }
        return null;
    }

    private QaPairState existingQaPair(String qaPairId) {
        QaPairState pair = qaPairsById.get(qaPairId);
        if (pair == null) {
            throw new IllegalArgumentException("qa pair not found: " + qaPairId);
        }
        return pair;
    }

    private PermissionState findPermission(String permissionId) {
        if (isBlank(permissionId)) {
            return null;
        }
        for (List<PermissionState> permissions : permissionsByKnowledgeId.values()) {
            for (PermissionState permission : permissions) {
                if (permissionId.equals(permission.permissionId)) {
                    return permission;
                }
            }
        }
        return null;
    }

    private int permissionForUser(String knowledgeId, String userId) {
        String safeUser = defaultIfBlank(userId, DEFAULT_USER_ID);
        int permissionType = PERMISSION_READ;
        for (PermissionState permission : permissions(knowledgeId)) {
            if (safeUser.equals(permission.userId)) {
                permissionType = Math.max(permissionType, permission.permissionType);
            }
        }
        return permissionType;
    }

    private boolean matchesOwner(String orgId, KnowledgeState knowledge) {
        return isBlank(orgId) || orgId.equals(knowledge.orgId);
    }

    private List<String> tagIds(String knowledgeId) {
        List<String> tags = tagIdsByKnowledgeId.get(knowledgeId);
        if (tags == null) {
            tags = new ArrayList<String>();
            tagIdsByKnowledgeId.put(knowledgeId, tags);
        }
        return tags;
    }

    private List<DocState> docs(String knowledgeId) {
        List<DocState> docs = docsByKnowledgeId.get(knowledgeId);
        if (docs == null) {
            docs = new ArrayList<DocState>();
            docsByKnowledgeId.put(knowledgeId, docs);
        }
        return docs;
    }

    private List<QaPairState> qaPairs(String knowledgeId) {
        List<QaPairState> pairs = qaPairsByKnowledgeId.get(knowledgeId);
        if (pairs == null) {
            pairs = new ArrayList<QaPairState>();
            qaPairsByKnowledgeId.put(knowledgeId, pairs);
        }
        return pairs;
    }

    private List<Map<String, Object>> metas(String knowledgeId) {
        List<Map<String, Object>> metas = metasByKnowledgeId.get(knowledgeId);
        if (metas == null) {
            metas = new ArrayList<Map<String, Object>>();
            metasByKnowledgeId.put(knowledgeId, metas);
        }
        return metas;
    }

    private List<PermissionState> permissions(String knowledgeId) {
        List<PermissionState> permissions = permissionsByKnowledgeId.get(knowledgeId);
        if (permissions == null) {
            permissions = new ArrayList<PermissionState>();
            permissionsByKnowledgeId.put(knowledgeId, permissions);
        }
        return permissions;
    }

    private void replaceMeta(List<Map<String, Object>> metas, Map<String, Object> meta) {
        String metaId = string(meta.get("metaId"));
        for (int i = 0; i < metas.size(); i++) {
            if (metaId.equals(metas.get(i).get("metaId"))) {
                metas.set(i, meta);
                return;
            }
        }
    }

    private void removeMeta(List<Map<String, Object>> metas, String metaId) {
        for (int i = metas.size() - 1; i >= 0; i--) {
            if (metaId.equals(metas.get(i).get("metaId"))) {
                metas.remove(i);
            }
        }
    }

    private boolean qaMetaContains(QaPairState pair, String expected) {
        for (Map<String, Object> meta : pair.metaDataList) {
            for (Object value : meta.values()) {
                if (containsIgnoreCase(string(value), expected)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String embeddingModelId(Object raw) {
        String modelId = string(map(raw).get("modelId"));
        return defaultIfBlank(modelId, "2");
    }

    private boolean graphSwitch(Object raw) {
        Object value = map(raw).get("switch");
        return value instanceof Boolean && (Boolean) value;
    }

    private String graphModelId(Object raw) {
        return string(map(raw).get("llmModelId"));
    }

    private Map<String, Object> avatar(Object raw) {
        Map<String, Object> source = map(raw);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("key", string(source.get("key")));
        result.put("path", string(source.get("path")));
        return result;
    }

    private String fileType(String docName) {
        int dot = docName == null ? -1 : docName.lastIndexOf('.');
        return dot >= 0 ? docName.substring(dot + 1).toLowerCase(Locale.ENGLISH) : "";
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(key, value);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safe(Map<String, Object> request) {
        return request == null ? new LinkedHashMap<String, Object>() : request;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object raw) {
        if (raw instanceof Map) {
            return (Map<String, Object>) raw;
        }
        return new LinkedHashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    private List<Object> list(Object raw) {
        if (raw instanceof List) {
            return (List<Object>) raw;
        }
        return Collections.emptyList();
    }

    private List<String> stringList(Object raw) {
        List<String> result = new ArrayList<String>();
        if (!(raw instanceof List)) {
            return result;
        }
        for (Object item : (List<?>) raw) {
            String value = string(item);
            if (!isBlank(value)) {
                result.add(value);
            }
        }
        return result;
    }

    private String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private List<Integer> intList(Object raw) {
        List<Integer> result = new ArrayList<Integer>();
        if (!(raw instanceof List)) {
            return result;
        }
        for (Object item : (List<?>) raw) {
            result.add(intValue(item, 0));
        }
        return result;
    }

    private boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return fallback;
    }

    private long longValue(Object value, long fallback) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private boolean containsIgnoreCase(String source, String expected) {
        return !isBlank(source) && !isBlank(expected)
                && source.toLowerCase(Locale.ENGLISH).contains(expected.toLowerCase(Locale.ENGLISH));
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String displayUserName(String userId) {
        if ("dev-admin".equals(userId)) {
            return "admin";
        }
        if ("dev-app".equals(userId)) {
            return "app";
        }
        return defaultIfBlank(userId, DEFAULT_USER_ID);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class KnowledgeState {
        private String knowledgeId;
        private String userId;
        private String orgId;
        private String orgName;
        private String name;
        private String description;
        private int category;
        private String embeddingModelId;
        private int graphSwitch;
        private String llmModelId;
        private int external;
        private Map<String, Object> avatar;
        private String createdAt;
        private String updatedAt;
    }

    private static final class TagState {
        private String tagId;
        private String tagName;
        private String userId;
        private String orgId;
    }

    private static final class SplitterState {
        private String splitterId;
        private String splitterName;
        private String splitterValue;
        private String type;
    }

    private static final class DocState {
        private String docId;
        private String docName;
        private String docType;
        private String knowledgeId;
        private String uploadTime;
        private int status;
        private long fileSize;
        private String segmentMethod;
        private String author;
        private int graphStatus;
    }

    private static final class QaPairState {
        private String qaPairId;
        private String knowledgeId;
        private String question;
        private String answer;
        private String userId;
        private String author;
        private String uploadTime;
        private int status;
        private boolean enabled;
        private String errorMsg;
        private final List<Map<String, Object>> metaDataList = new ArrayList<Map<String, Object>>();
    }

    private static final class PermissionState {
        private String permissionId;
        private String userId;
        private String userName;
        private String orgId;
        private String orgName;
        private int permissionType;
        private boolean transfer;
    }
}
