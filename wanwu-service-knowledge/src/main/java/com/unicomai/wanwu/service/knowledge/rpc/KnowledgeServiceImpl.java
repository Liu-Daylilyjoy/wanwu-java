package com.unicomai.wanwu.service.knowledge.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.knowledge.KnowledgeService;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import com.unicomai.wanwu.service.knowledge.persistence.entity.KnowledgeRecordEntity;
import com.unicomai.wanwu.service.knowledge.persistence.mapper.KnowledgeRecordMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private static final int REPORT_STATUS_PENDING = 0;
    private static final int REPORT_STATUS_GENERATED = 2;
    private static final int REPORT_IMPORT_NONE = -1;
    private static final int REPORT_IMPORT_SUCCESS = 2;
    private static final int EXTERNAL_ALL = -1;
    private static final int EXTERNAL_INTERNAL = 0;
    private static final int EXTERNAL_KNOWLEDGE = 1;
    private static final String EXTERNAL_PROVIDER_DIFY = "dify";
    private static final int DOC_STATUS_FINISHED = 1;
    private static final int EXPORT_STATUS_SUCCESS = 2;
    private static final String EXPORT_TYPE_QA = "qa";
    private static final String EXPORT_TYPE_DOC = "doc";
    private static final String TYPE_SNAPSHOT = "snapshot";
    private static final String SNAPSHOT_ID = "state";
    private static final ObjectMapper JSON = new ObjectMapper();

    static {
        JSON.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    private final Map<String, KnowledgeState> knowledgeBases = new LinkedHashMap<String, KnowledgeState>();
    private final Map<String, TagState> tags = new LinkedHashMap<String, TagState>();
    private final Map<String, SplitterState> splitters = new LinkedHashMap<String, SplitterState>();
    private final Map<String, List<String>> tagIdsByKnowledgeId = new LinkedHashMap<String, List<String>>();
    private final Map<String, List<DocState>> docsByKnowledgeId = new LinkedHashMap<String, List<DocState>>();
    private final Map<String, List<SegmentState>> segmentsByDocId = new LinkedHashMap<String, List<SegmentState>>();
    private final Map<String, List<QaPairState>> qaPairsByKnowledgeId = new LinkedHashMap<String, List<QaPairState>>();
    private final Map<String, QaPairState> qaPairsById = new LinkedHashMap<String, QaPairState>();
    private final Map<String, List<ReportState>> reportsByKnowledgeId = new LinkedHashMap<String, List<ReportState>>();
    private final Map<String, List<ExportRecordState>> exportRecordsByKnowledgeId =
            new LinkedHashMap<String, List<ExportRecordState>>();
    private final Map<String, ExportRecordState> exportRecordsById = new LinkedHashMap<String, ExportRecordState>();
    private final Map<String, ExternalApiState> externalApis = new LinkedHashMap<String, ExternalApiState>();
    private final Map<String, List<ExternalKnowledgeState>> externalKnowledgeByApiId =
            new LinkedHashMap<String, List<ExternalKnowledgeState>>();
    private final Map<String, KeywordState> keywords = new LinkedHashMap<String, KeywordState>();
    private final Map<String, List<Map<String, Object>>> metasByKnowledgeId = new LinkedHashMap<String, List<Map<String, Object>>>();
    private final Map<String, List<PermissionState>> permissionsByKnowledgeId = new LinkedHashMap<String, List<PermissionState>>();

    private final AtomicLong nextKnowledgeId = new AtomicLong(1000);
    private final AtomicLong nextTagId = new AtomicLong(1000);
    private final AtomicLong nextSplitterId = new AtomicLong(1000);
    private final AtomicLong nextDocId = new AtomicLong(1000);
    private final AtomicLong nextSegmentId = new AtomicLong(1000);
    private final AtomicLong nextQaPairId = new AtomicLong(1000);
    private final AtomicLong nextReportId = new AtomicLong(1000);
    private final AtomicLong nextExportRecordId = new AtomicLong(1000);
    private final AtomicLong nextExternalApiId = new AtomicLong(1000);
    private final AtomicLong nextExternalKnowledgeId = new AtomicLong(1000);
    private final AtomicLong nextKeywordId = new AtomicLong(1000);
    private final AtomicLong nextMetaId = new AtomicLong(1000);
    private final AtomicLong nextPermissionId = new AtomicLong(1000);

    @Autowired(required = false)
    private KnowledgeRecordMapper knowledgeRecordMapper;

    public KnowledgeServiceImpl() {
        seedSplitters();
    }

    KnowledgeServiceImpl(KnowledgeRecordMapper knowledgeRecordMapper) {
        this.knowledgeRecordMapper = knowledgeRecordMapper;
        seedSplitters();
        loadPersistedSnapshot();
    }

    @PostConstruct
    synchronized void loadPersistedSnapshot() {
        if (knowledgeRecordMapper == null) {
            return;
        }
        List<KnowledgeRecordEntity> records = knowledgeRecordMapper.selectByType(TYPE_SNAPSHOT);
        if (records == null || records.isEmpty()) {
            return;
        }
        KnowledgeRecordEntity record = records.get(records.size() - 1);
        try {
            KnowledgeSnapshot snapshot = JSON.readValue(record.getPayload(), KnowledgeSnapshot.class);
            applySnapshot(snapshot);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load persisted knowledge snapshot", ex);
        }
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
        knowledge.docCount = 0;
        knowledge.createdAt = CREATED_AT;
        knowledge.updatedAt = CREATED_AT;
        knowledgeBases.put(knowledgeId, knowledge);
        tagIdsByKnowledgeId.put(knowledgeId, new ArrayList<String>());
        docsByKnowledgeId.put(knowledgeId, new ArrayList<DocState>());
        qaPairsByKnowledgeId.put(knowledgeId, new ArrayList<QaPairState>());
        reportsByKnowledgeId.put(knowledgeId, new ArrayList<ReportState>());
        exportRecordsByKnowledgeId.put(knowledgeId, new ArrayList<ExportRecordState>());
        metasByKnowledgeId.put(knowledgeId, new ArrayList<Map<String, Object>>());
        permissionsByKnowledgeId.put(knowledgeId, new ArrayList<PermissionState>());
        addOwnerPermission(knowledgeId, knowledge.userId, knowledge.orgId);
        saveSnapshot();
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
        saveSnapshot();
    }

    @Override
    public synchronized void deleteKnowledge(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        KnowledgeState knowledge = existingKnowledge(knowledgeId);
        if (knowledge.external == EXTERNAL_KNOWLEDGE) {
            unmountExternalKnowledge(knowledge);
        }
        knowledgeBases.remove(knowledgeId);
        tagIdsByKnowledgeId.remove(knowledgeId);
        for (DocState doc : docs(knowledgeId)) {
            segmentsByDocId.remove(doc.docId);
        }
        docsByKnowledgeId.remove(knowledgeId);
        for (QaPairState pair : qaPairs(knowledgeId)) {
            qaPairsById.remove(pair.qaPairId);
        }
        qaPairsByKnowledgeId.remove(knowledgeId);
        reportsByKnowledgeId.remove(knowledgeId);
        for (ExportRecordState record : exportRecords(knowledgeId)) {
            exportRecordsById.remove(record.exportRecordId);
        }
        exportRecordsByKnowledgeId.remove(knowledgeId);
        for (KeywordState keyword : keywords.values()) {
            keyword.knowledgeBaseIds.remove(knowledgeId);
        }
        metasByKnowledgeId.remove(knowledgeId);
        permissionsByKnowledgeId.remove(knowledgeId);
        saveSnapshot();
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
    public synchronized Map<String, Object> listKeywords(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String name = string(safe.get("name"));
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (KeywordState keyword : keywords.values()) {
            if (!matchesOwner(orgId, keyword)) {
                continue;
            }
            if (!isBlank(name) && !containsIgnoreCase(keyword.name, name) && !containsIgnoreCase(keyword.alias, name)) {
                continue;
            }
            rows.add(toKeywordInfo(keyword));
        }
        Map<String, Object> result = page(rows, intValue(safe.get("pageNo"), 1), intValue(safe.get("pageSize"), 10));
        result.put("pageNum", result.get("pageNo"));
        return result;
    }

    @Override
    public synchronized Map<String, Object> getKeyword(String userId, String orgId, Map<String, Object> request) {
        KeywordState keyword = existingKeyword(string(safe(request).get("id")));
        if (!matchesOwner(orgId, keyword)) {
            throw new IllegalArgumentException("keyword not found");
        }
        return toKeywordInfo(keyword);
    }

    @Override
    public synchronized Map<String, Object> createKeyword(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String name = string(safe.get("name"));
        String alias = string(safe.get("alias"));
        if (isBlank(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        if (isBlank(alias)) {
            throw new IllegalArgumentException("alias cannot be empty");
        }
        List<String> knowledgeBaseIds = validateKnowledgeIds(safe.get("knowledgeBaseIds"));
        ensureUniqueKeywordName(orgId, name, null);
        long id = nextKeywordId.incrementAndGet();
        KeywordState keyword = new KeywordState();
        keyword.id = id;
        keyword.userId = defaultIfBlank(userId, DEFAULT_USER_ID);
        keyword.orgId = defaultIfBlank(orgId, DEFAULT_ORG_ID);
        keyword.name = name;
        keyword.alias = alias;
        keyword.knowledgeBaseIds.addAll(knowledgeBaseIds);
        keyword.updatedAt = CREATED_AT;
        keywords.put(String.valueOf(id), keyword);
        saveSnapshot();
        return singleton("id", id);
    }

    @Override
    public synchronized void updateKeyword(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        KeywordState keyword = existingKeyword(string(safe.get("id")));
        if (!matchesOwner(orgId, keyword)) {
            throw new IllegalArgumentException("keyword not found");
        }
        String name = string(safe.get("name"));
        String alias = string(safe.get("alias"));
        if (isBlank(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        if (isBlank(alias)) {
            throw new IllegalArgumentException("alias cannot be empty");
        }
        ensureUniqueKeywordName(orgId, name, String.valueOf(keyword.id));
        keyword.name = name;
        keyword.alias = alias;
        keyword.knowledgeBaseIds.clear();
        keyword.knowledgeBaseIds.addAll(validateKnowledgeIds(safe.get("knowledgeBaseIds")));
        keyword.updatedAt = CREATED_AT;
        saveSnapshot();
    }

    @Override
    public synchronized void deleteKeyword(String userId, String orgId, Map<String, Object> request) {
        KeywordState keyword = existingKeyword(string(safe(request).get("id")));
        if (!matchesOwner(orgId, keyword)) {
            throw new IllegalArgumentException("keyword not found");
        }
        keywords.remove(String.valueOf(keyword.id));
        saveSnapshot();
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
        saveSnapshot();
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
        saveSnapshot();
    }

    @Override
    public synchronized void deleteTag(String userId, String orgId, Map<String, Object> request) {
        String tagId = string(safe(request).get("tagId"));
        existingTag(tagId);
        tags.remove(tagId);
        for (List<String> ids : tagIdsByKnowledgeId.values()) {
            ids.remove(tagId);
        }
        saveSnapshot();
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
        saveSnapshot();
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
        saveSnapshot();
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
        saveSnapshot();
    }

    @Override
    public synchronized void deleteSplitter(String userId, String orgId, Map<String, Object> request) {
        SplitterState splitter = existingSplitter(string(safe(request).get("splitterId")));
        if (!"preset".equals(splitter.type)) {
            splitters.remove(splitter.splitterId);
            saveSnapshot();
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
        Map<String, Object> safe = safe(request);
        String docId = string(safe.get("docId"));
        String keyword = string(safe.get("keyword"));
        DocState doc = findDoc(docId);
        List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();
        if (doc != null) {
            for (SegmentState segment : segments(docId)) {
                if (!isBlank(keyword) && !containsIgnoreCase(segment.content, keyword)) {
                    continue;
                }
                filtered.add(toSegmentInfo(segment));
            }
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("fileName", doc == null ? "" : doc.docName);
        result.put("pageTotal", 1);
        result.put("segmentTotalNum", filtered.size());
        result.put("maxSegmentSize", 500);
        result.put("segmentType", "0");
        result.put("uploadTime", doc == null ? CREATED_AT : doc.uploadTime);
        result.put("splitter", "");
        result.put("metaDataList", Collections.emptyList());
        result.put("contentList", page(filtered,
                intValue(safe.get("pageNo"), 1),
                intValue(safe.get("pageSize"), 10)).get("list"));
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
            item.put("fileName", fileNameFromUrl(url));
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
            doc.author = displayUserName(defaultIfBlank(userId, DEFAULT_USER_ID));
            doc.graphStatus = 0;
            docs(knowledgeId).add(doc);
            createDefaultSegment(doc);
        }
        saveSnapshot();
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
                segmentsByDocId.remove(docs.get(i).docId);
                docs.remove(i);
            }
        }
        saveSnapshot();
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
        saveSnapshot();
    }

    @Override
    public void batchUpdateDocMeta(String userId, String orgId, Map<String, Object> request) {
        updateDocMeta(userId, orgId, request);
    }

    @Override
    public synchronized void updateDocSegmentStatus(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String docId = string(safe.get("docId"));
        boolean all = booleanValue(safe.get("all"), false);
        boolean available = segmentAvailable(safe.get("contentStatus"));
        if (all) {
            for (SegmentState segment : segments(docId)) {
                segment.available = available;
            }
            saveSnapshot();
            return;
        }
        SegmentState segment = findSegment(docId, string(safe.get("contentId")));
        if (segment != null) {
            segment.available = available;
            saveSnapshot();
        }
    }

    @Override
    public synchronized void updateDocSegmentLabels(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        SegmentState segment = findSegment(string(safe.get("docId")), string(safe.get("contentId")));
        if (segment != null) {
            segment.labels = stringList(safe.get("labels"));
            saveSnapshot();
        }
    }

    @Override
    public synchronized void createDocSegment(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String docId = string(safe.get("docId"));
        if (findDoc(docId) == null) {
            throw new IllegalArgumentException("doc not found: " + docId);
        }
        SegmentState segment = new SegmentState();
        segment.contentId = "segment-" + nextSegmentId.incrementAndGet();
        segment.docId = docId;
        segment.content = string(safe.get("content"));
        segment.available = true;
        segment.contentNum = segments(docId).size() + 1;
        segment.labels = stringList(safe.get("labels"));
        segment.parent = false;
        segment.childNum = 0;
        segments(docId).add(segment);
        saveSnapshot();
    }

    @Override
    public void batchCreateDocSegment(String userId, String orgId, Map<String, Object> request) {
    }

    @Override
    public synchronized void deleteDocSegment(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        List<SegmentState> segments = segments(string(safe.get("docId")));
        String contentId = string(safe.get("contentId"));
        for (int i = segments.size() - 1; i >= 0; i--) {
            if (contentId.equals(segments.get(i).contentId)) {
                segments.remove(i);
            }
        }
        renumberSegments(segments);
        saveSnapshot();
    }

    @Override
    public synchronized void updateDocSegment(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        SegmentState segment = findSegment(string(safe.get("docId")), string(safe.get("contentId")));
        if (segment != null) {
            segment.content = string(safe.get("content"));
            saveSnapshot();
        }
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
        saveSnapshot();
    }

    @Override
    public synchronized void editKnowledgeUser(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> knowledgeUser = map(safe(request).get("knowledgeUser"));
        PermissionState permission = findPermission(string(knowledgeUser.get("permissionId")));
        if (permission != null) {
            permission.permissionType = intValue(knowledgeUser.get("permissionType"), permission.permissionType);
            saveSnapshot();
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
        saveSnapshot();
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
        saveSnapshot();
    }

    @Override
    public synchronized Map<String, Object> listReports(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        List<ReportState> reports = reports(knowledgeId);
        List<Map<String, Object>> all = new ArrayList<Map<String, Object>>();
        int lastImportStatus = REPORT_IMPORT_NONE;
        for (ReportState report : reports) {
            all.add(toReportInfo(report));
            if (report.imported) {
                lastImportStatus = REPORT_IMPORT_SUCCESS;
            }
        }
        Map<String, Object> result = page(all,
                intValue(safe.get("pageNo"), 1),
                intValue(safe.get("pageSize"), 10));
        result.put("createdAt", latestReportCreatedAt(reports));
        result.put("status", reports.isEmpty() ? REPORT_STATUS_PENDING : REPORT_STATUS_GENERATED);
        result.put("canGenerate", Boolean.TRUE);
        result.put("canAddReport", Boolean.TRUE);
        result.put("generateLabel", reports.isEmpty() ? "" : "Regenerate");
        result.put("lastImportStatus", lastImportStatus);
        return result;
    }

    @Override
    public synchronized void generateReport(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        KnowledgeState knowledge = existingKnowledge(knowledgeId);
        ReportState generated = null;
        for (ReportState report : reports(knowledgeId)) {
            if (report.generated) {
                generated = report;
                break;
            }
        }
        if (generated == null) {
            generated = newReport(knowledgeId, "Generated Community Report",
                    generatedReportContent(knowledge), false, true);
            reports(knowledgeId).add(0, generated);
        } else {
            generated.title = "Generated Community Report";
            generated.content = generatedReportContent(knowledge);
            generated.createdAt = nowMillis();
        }
        saveSnapshot();
    }

    @Override
    public synchronized void deleteReport(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        String contentId = string(safe.get("contentId"));
        List<ReportState> reports = reports(knowledgeId);
        for (int i = reports.size() - 1; i >= 0; i--) {
            if (contentId.equals(reports.get(i).contentId)) {
                reports.remove(i);
            }
        }
        saveSnapshot();
    }

    @Override
    public synchronized void updateReport(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        ReportState report = existingReport(knowledgeId, string(safe.get("contentId")));
        String title = string(safe.get("title")).trim();
        String content = string(safe.get("content")).trim();
        if (isBlank(title) || isBlank(content)) {
            throw new IllegalArgumentException("report title and content cannot be empty");
        }
        report.title = title;
        report.content = content;
        report.createdAt = nowMillis();
        saveSnapshot();
    }

    @Override
    public synchronized void addReport(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        String title = string(safe.get("title")).trim();
        String content = string(safe.get("content")).trim();
        if (isBlank(title) || isBlank(content)) {
            throw new IllegalArgumentException("report title and content cannot be empty");
        }
        reports(knowledgeId).add(0, newReport(knowledgeId, title, content, false, false));
        saveSnapshot();
    }

    @Override
    public synchronized void batchAddReports(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        String fileUploadId = string(safe.get("fileUploadId"));
        if (isBlank(fileUploadId)) {
            throw new IllegalArgumentException("fileUploadId cannot be empty");
        }
        reports(knowledgeId).add(0, newReport(knowledgeId,
                "Imported Community Report",
                "Imported from fileUploadId: " + fileUploadId,
                true,
                false));
        saveSnapshot();
    }

    @Override
    public synchronized Map<String, Object> listExternalApis(String userId, String orgId, Map<String, Object> request) {
        List<String> requestedIds = stringList(safe(request).get("externalApiIds"));
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ExternalApiState api : externalApis.values()) {
            if (!matchesOwner(orgId, api)) {
                continue;
            }
            if (!requestedIds.isEmpty() && !requestedIds.contains(api.externalApiId)) {
                continue;
            }
            result.add(toExternalApiInfo(api));
        }
        return singleton("externalApiList", result);
    }

    @Override
    public synchronized Map<String, Object> createExternalApi(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String name = string(safe.get("name")).trim();
        String baseUrl = string(safe.get("baseUrl")).trim();
        String apiKey = string(safe.get("apiKey")).trim();
        if (isBlank(name) || isBlank(baseUrl) || isBlank(apiKey)) {
            throw new IllegalArgumentException("external api name, baseUrl and apiKey cannot be empty");
        }
        ExternalApiState api = new ExternalApiState();
        api.externalApiId = "external-api-" + nextExternalApiId.incrementAndGet();
        api.userId = defaultIfBlank(userId, DEFAULT_USER_ID);
        api.orgId = defaultIfBlank(orgId, DEFAULT_ORG_ID);
        api.name = name;
        api.description = string(safe.get("description"));
        api.baseUrl = baseUrl;
        api.apiKey = apiKey;
        api.provider = EXTERNAL_PROVIDER_DIFY;
        externalApis.put(api.externalApiId, api);
        seedExternalKnowledgeCandidates(api);
        saveSnapshot();
        return singleton("externalApiId", api.externalApiId);
    }

    @Override
    public synchronized void updateExternalApi(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        ExternalApiState api = existingExternalApi(string(safe.get("externalApiId")));
        String name = string(safe.get("name")).trim();
        String baseUrl = string(safe.get("baseUrl")).trim();
        String apiKey = string(safe.get("apiKey")).trim();
        if (isBlank(name) || isBlank(baseUrl) || isBlank(apiKey)) {
            throw new IllegalArgumentException("external api name, baseUrl and apiKey cannot be empty");
        }
        api.name = name;
        api.description = string(safe.get("description"));
        api.baseUrl = baseUrl;
        api.apiKey = apiKey;
        for (KnowledgeState knowledge : knowledgeBases.values()) {
            Map<String, Object> info = knowledge.externalKnowledgeInfo;
            if (info != null && api.externalApiId.equals(string(info.get("externalApiId")))) {
                info.put("externalApiName", api.name);
                info.put("externalApiUrl", api.baseUrl);
                info.put("externalApiKey", api.apiKey);
            }
        }
        saveSnapshot();
    }

    @Override
    public synchronized void deleteExternalApi(String userId, String orgId, Map<String, Object> request) {
        String externalApiId = string(safe(request).get("externalApiId"));
        existingExternalApi(externalApiId);
        externalApis.remove(externalApiId);
        externalKnowledgeByApiId.remove(externalApiId);
        saveSnapshot();
    }

    @Override
    public synchronized Map<String, Object> listExternalKnowledge(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        ExternalApiState api = existingExternalApi(string(safe.get("externalApiId")));
        seedExternalKnowledgeCandidates(api);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ExternalKnowledgeState externalKnowledge : externalKnowledge(api.externalApiId)) {
            if (externalKnowledge.mounted) {
                continue;
            }
            result.add(toExternalKnowledgeInfo(api, externalKnowledge));
        }
        return singleton("externalKnowledgeList", result);
    }

    @Override
    public synchronized Map<String, Object> createExternalKnowledge(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String name = string(safe.get("name")).trim();
        if (isBlank(name)) {
            throw new IllegalArgumentException("knowledge name cannot be empty");
        }
        ensureUniqueKnowledgeName(orgId, name, "");
        ExternalApiState api = existingExternalApi(string(safe.get("externalApiId")));
        ExternalKnowledgeState externalKnowledge = existingExternalKnowledge(
                api.externalApiId, string(safe.get("externalKnowledgeId")));
        String knowledgeId = "knowledge-" + nextKnowledgeId.incrementAndGet();
        KnowledgeState knowledge = new KnowledgeState();
        knowledge.knowledgeId = knowledgeId;
        knowledge.userId = defaultIfBlank(userId, DEFAULT_USER_ID);
        knowledge.orgId = defaultIfBlank(orgId, DEFAULT_ORG_ID);
        knowledge.orgName = DEFAULT_ORG_NAME;
        knowledge.name = name;
        knowledge.description = string(safe.get("description"));
        knowledge.category = CATEGORY_KNOWLEDGE;
        knowledge.embeddingModelId = "";
        knowledge.graphSwitch = 0;
        knowledge.llmModelId = "";
        knowledge.avatar = avatar(safe.get("avatar"));
        knowledge.external = EXTERNAL_KNOWLEDGE;
        knowledge.docCount = externalKnowledge.docCount;
        knowledge.externalKnowledgeInfo = externalKnowledgeInfo(api, externalKnowledge,
                defaultIfBlank(string(safe.get("externalSource")), EXTERNAL_PROVIDER_DIFY));
        knowledge.createdAt = CREATED_AT;
        knowledge.updatedAt = CREATED_AT;
        knowledgeBases.put(knowledgeId, knowledge);
        tagIdsByKnowledgeId.put(knowledgeId, new ArrayList<String>());
        docsByKnowledgeId.put(knowledgeId, new ArrayList<DocState>());
        qaPairsByKnowledgeId.put(knowledgeId, new ArrayList<QaPairState>());
        reportsByKnowledgeId.put(knowledgeId, new ArrayList<ReportState>());
        exportRecordsByKnowledgeId.put(knowledgeId, new ArrayList<ExportRecordState>());
        metasByKnowledgeId.put(knowledgeId, new ArrayList<Map<String, Object>>());
        permissionsByKnowledgeId.put(knowledgeId, new ArrayList<PermissionState>());
        addOwnerPermission(knowledgeId, knowledge.userId, knowledge.orgId);
        mountExternalKnowledge(externalKnowledge, knowledgeId);
        saveSnapshot();
        return singleton("knowledgeId", knowledgeId);
    }

    @Override
    public synchronized void updateExternalKnowledge(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        KnowledgeState knowledge = existingKnowledge(string(safe.get("knowledgeId")));
        if (knowledge.external != EXTERNAL_KNOWLEDGE) {
            throw new IllegalArgumentException("knowledge is not external");
        }
        String name = string(safe.get("name")).trim();
        if (isBlank(name)) {
            throw new IllegalArgumentException("knowledge name cannot be empty");
        }
        ensureUniqueKnowledgeName(orgId, name, knowledge.knowledgeId);
        ExternalApiState api = existingExternalApi(string(safe.get("externalApiId")));
        ExternalKnowledgeState externalKnowledge = existingExternalKnowledge(
                api.externalApiId, string(safe.get("externalKnowledgeId")));
        unmountExternalKnowledge(knowledge);
        mountExternalKnowledge(externalKnowledge, knowledge.knowledgeId);
        knowledge.name = name;
        knowledge.description = string(safe.get("description"));
        knowledge.docCount = externalKnowledge.docCount;
        knowledge.externalKnowledgeInfo = externalKnowledgeInfo(api, externalKnowledge,
                defaultIfBlank(string(safe.get("externalSource")), EXTERNAL_PROVIDER_DIFY));
        knowledge.updatedAt = CREATED_AT;
        saveSnapshot();
    }

    @Override
    public synchronized void deleteExternalKnowledge(String userId, String orgId, Map<String, Object> request) {
        String knowledgeId = string(safe(request).get("knowledgeId"));
        KnowledgeState knowledge = existingKnowledge(knowledgeId);
        if (knowledge.external != EXTERNAL_KNOWLEDGE) {
            throw new IllegalArgumentException("knowledge is not external");
        }
        deleteKnowledge(userId, orgId, request);
    }

    @Override
    public synchronized Map<String, Object> listExportRecords(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String knowledgeId = string(safe.get("knowledgeId"));
        existingKnowledge(knowledgeId);
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (ExportRecordState record : exportRecords(knowledgeId)) {
            if (!matchesOwner(orgId, record)) {
                continue;
            }
            rows.add(toExportRecordInfo(record));
        }
        return page(rows,
                intValue(safe.get("pageNo"), 1),
                intValue(safe.get("pageSize"), 10));
    }

    @Override
    public synchronized void deleteExportRecord(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        String exportRecordId = string(safe.get("exportRecordId"));
        ExportRecordState record = exportRecordsById.remove(exportRecordId);
        if (record == null) {
            return;
        }
        List<ExportRecordState> records = exportRecords(record.knowledgeId);
        for (int i = records.size() - 1; i >= 0; i--) {
            if (exportRecordId.equals(records.get(i).exportRecordId)) {
                records.remove(i);
            }
        }
        saveSnapshot();
    }

    @Override
    public synchronized Map<String, Object> exportDocs(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        KnowledgeState knowledge = existingKnowledge(string(safe.get("knowledgeId")));
        List<String> docIds = stringList(safe.get("docIdList"));
        ExportRecordState record = createExportRecord(
                knowledge,
                defaultIfBlank(userId, DEFAULT_USER_ID),
                defaultIfBlank(orgId, DEFAULT_ORG_ID),
                EXPORT_TYPE_DOC,
                "",
                buildDocExportZipBase64(knowledge, docIds),
                "zip",
                "application/zip");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("recordCreated", true);
        result.put("exportRecordId", record.exportRecordId);
        result.put("fileUrl", record.filePath);
        result.put("downloadUrl", record.filePath);
        return result;
    }

    @Override
    public synchronized Map<String, Object> getExportRecordFile(String userId, String orgId, Map<String, Object> request) {
        ExportRecordState record = existingExportRecord(string(safe(request).get("exportRecordId")));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("exportRecordId", record.exportRecordId);
        result.put("fileName", record.fileName);
        result.put("contentType", record.contentType);
        result.put("content", record.content);
        result.put("contentBase64", record.contentBase64);
        return result;
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
        saveSnapshot();
        return singleton("qaPairId", qaPairId);
    }

    @Override
    public synchronized void updateQaPair(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        QaPairState pair = existingQaPair(string(safe.get("qaPairId")));
        pair.question = string(safe.get("question")).trim();
        pair.answer = string(safe.get("answer")).trim();
        saveSnapshot();
    }

    @Override
    public synchronized void updateQaPairSwitch(String userId, String orgId, Map<String, Object> request) {
        Map<String, Object> safe = safe(request);
        QaPairState pair = existingQaPair(string(safe.get("qaPairId")));
        pair.enabled = booleanValue(safe.get("switch"), pair.enabled);
        saveSnapshot();
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
        saveSnapshot();
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
        ExportRecordState record = createExportRecord(
                knowledge,
                defaultIfBlank(userId, DEFAULT_USER_ID),
                defaultIfBlank(orgId, DEFAULT_ORG_ID),
                EXPORT_TYPE_QA,
                buildQaExportContent(knowledge),
                "",
                "csv",
                "text/csv;charset=UTF-8");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("knowledgeId", knowledge.knowledgeId);
        result.put("knowledgeName", knowledge.name);
        result.put("recordCreated", true);
        result.put("exportRecordId", record.exportRecordId);
        result.put("fileUrl", record.filePath);
        result.put("downloadUrl", record.filePath);
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
        result.put("docCount", Math.max(knowledge.docCount, docs(knowledge.knowledgeId).size()));
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
        result.put("externalKnowledgeInfo", knowledge.external == EXTERNAL_KNOWLEDGE
                ? new LinkedHashMap<String, Object>(safe(knowledge.externalKnowledgeInfo))
                : null);
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
        result.put("keywords", keywordsForKnowledge(knowledge.knowledgeId));
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

    private List<Map<String, Object>> keywordsForKnowledge(String knowledgeId) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (KeywordState keyword : keywords.values()) {
            if (keyword.knowledgeBaseIds.contains(knowledgeId)) {
                result.add(toKeywordInfo(keyword));
            }
        }
        return result;
    }

    private Map<String, Object> toKeywordInfo(KeywordState keyword) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", keyword.id);
        result.put("name", keyword.name);
        result.put("alias", keyword.alias);
        result.put("knowledgeBaseIds", new ArrayList<String>(keyword.knowledgeBaseIds));
        result.put("knowledgeBaseNames", knowledgeNames(keyword.knowledgeBaseIds));
        result.put("updatedAt", keyword.updatedAt);
        return result;
    }

    private List<String> knowledgeNames(List<String> knowledgeBaseIds) {
        List<String> result = new ArrayList<String>();
        for (String knowledgeId : safeList(knowledgeBaseIds)) {
            KnowledgeState knowledge = knowledgeBases.get(knowledgeId);
            if (knowledge != null) {
                result.add(knowledge.name);
            }
        }
        return result;
    }

    private List<String> validateKnowledgeIds(Object raw) {
        List<String> result = new ArrayList<String>();
        for (String knowledgeId : stringList(raw)) {
            KnowledgeState knowledge = existingKnowledge(knowledgeId);
            if (!result.contains(knowledge.knowledgeId)) {
                result.add(knowledge.knowledgeId);
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("knowledgeBaseIds cannot be empty");
        }
        return result;
    }

    private void ensureUniqueKeywordName(String orgId, String name, String currentId) {
        for (KeywordState keyword : keywords.values()) {
            if (!matchesOwner(orgId, keyword)) {
                continue;
            }
            if (!String.valueOf(keyword.id).equals(currentId) && name.equals(keyword.name)) {
                throw new IllegalArgumentException("keyword already exists");
            }
        }
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

    private Map<String, Object> toSegmentInfo(SegmentState segment) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("content", segment.content);
        result.put("available", segment.available);
        result.put("contentId", segment.contentId);
        result.put("contentNum", segment.contentNum);
        result.put("labels", new ArrayList<String>(segment.labels));
        result.put("isParent", segment.parent);
        result.put("childNum", segment.childNum);
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

    private Map<String, Object> toExportRecordInfo(ExportRecordState record) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("exportRecordId", record.exportRecordId);
        result.put("author", record.author);
        result.put("status", record.status);
        result.put("filePath", record.filePath);
        result.put("errorMsg", record.errorMsg);
        result.put("exportTime", record.exportTime);
        result.put("userId", record.userId);
        result.put("knowledgeName", record.knowledgeName);
        return result;
    }

    private ExportRecordState createExportRecord(KnowledgeState knowledge, String userId, String orgId,
                                                 String exportType, String content, String contentBase64,
                                                 String extension, String contentType) {
        ExportRecordState record = new ExportRecordState();
        record.exportRecordId = "export-" + nextExportRecordId.incrementAndGet();
        record.knowledgeId = knowledge.knowledgeId;
        record.knowledgeName = knowledge.name;
        record.exportType = exportType;
        record.userId = userId;
        record.orgId = orgId;
        record.author = displayUserName(userId);
        record.status = EXPORT_STATUS_SUCCESS;
        record.errorMsg = "";
        record.exportTime = CREATED_AT;
        record.fileName = exportFileName(knowledge.name, exportType, record.exportRecordId, extension);
        record.filePath = "/user/api/v1/knowledge/export/file/" + record.exportRecordId + "/" + record.fileName;
        record.content = content;
        record.contentBase64 = contentBase64;
        record.contentType = contentType;
        exportRecords(knowledge.knowledgeId).add(0, record);
        exportRecordsById.put(record.exportRecordId, record);
        saveSnapshot();
        return record;
    }

    private String buildQaExportContent(KnowledgeState knowledge) {
        StringBuilder csv = new StringBuilder();
        csv.append("knowledgeName,question,answer,status\n");
        for (QaPairState pair : qaPairs(knowledge.knowledgeId)) {
            csv.append(csvCell(knowledge.name)).append(',')
                    .append(csvCell(pair.question)).append(',')
                    .append(csvCell(pair.answer)).append(',')
                    .append(pair.status).append('\n');
        }
        if (qaPairs(knowledge.knowledgeId).isEmpty()) {
            csv.append(csvCell(knowledge.name)).append(",,,").append(QA_STATUS_FINISHED).append('\n');
        }
        return csv.toString();
    }

    private String buildDocExportZipBase64(KnowledgeState knowledge, List<String> docIds) {
        Set<String> selectedDocIds = new LinkedHashSet<String>(docIds);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(out);
            try {
                int written = 0;
                for (DocState doc : docs(knowledge.knowledgeId)) {
                    if (!selectedDocIds.isEmpty() && !selectedDocIds.contains(doc.docId)) {
                        continue;
                    }
                    zip.putNextEntry(new ZipEntry(exportEntryName(doc.docName)));
                    String body = "knowledgeId: " + knowledge.knowledgeId + "\n"
                            + "knowledgeName: " + knowledge.name + "\n"
                            + "docId: " + doc.docId + "\n"
                            + "docName: " + doc.docName + "\n";
                    zip.write(body.getBytes(StandardCharsets.UTF_8));
                    zip.closeEntry();
                    written++;
                }
                if (written == 0) {
                    zip.putNextEntry(new ZipEntry("README.txt"));
                    zip.write(("knowledgeId: " + knowledge.knowledgeId + "\n"
                            + "knowledgeName: " + knowledge.name + "\n").getBytes(StandardCharsets.UTF_8));
                    zip.closeEntry();
                }
            } finally {
                zip.close();
            }
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to build knowledge doc export", ex);
        }
    }

    private String csvCell(String value) {
        String safeValue = string(value).replace("\"", "\"\"");
        return "\"" + safeValue + "\"";
    }

    private String exportFileName(String knowledgeName, String exportType, String exportRecordId, String extension) {
        String safeName = defaultIfBlank(knowledgeName, "knowledge").replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        String safeExtension = defaultIfBlank(extension, "txt").replace(".", "");
        return safeName + "_" + exportType + "_" + exportRecordId + "." + safeExtension;
    }

    private String exportEntryName(String docName) {
        String safeName = defaultIfBlank(docName, "document.txt").replace('\\', '_').replace('/', '_');
        return safeName.isEmpty() ? "document.txt" : safeName;
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

    private KeywordState existingKeyword(String keywordId) {
        if (isBlank(keywordId) || !keywords.containsKey(keywordId)) {
            throw new IllegalArgumentException("keyword not found");
        }
        return keywords.get(keywordId);
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

    private SegmentState findSegment(String docId, String contentId) {
        if (isBlank(docId) || isBlank(contentId)) {
            return null;
        }
        for (SegmentState segment : segments(docId)) {
            if (contentId.equals(segment.contentId)) {
                return segment;
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

    private ReportState existingReport(String knowledgeId, String contentId) {
        for (ReportState report : reports(knowledgeId)) {
            if (contentId.equals(report.contentId)) {
                return report;
            }
        }
        throw new IllegalArgumentException("report not found: " + contentId);
    }

    private ExportRecordState existingExportRecord(String exportRecordId) {
        ExportRecordState record = exportRecordsById.get(exportRecordId);
        if (record == null) {
            throw new IllegalArgumentException("export record not found: " + exportRecordId);
        }
        return record;
    }

    private ExternalApiState existingExternalApi(String externalApiId) {
        if (isBlank(externalApiId) || !externalApis.containsKey(externalApiId)) {
            throw new IllegalArgumentException("external api not found");
        }
        return externalApis.get(externalApiId);
    }

    private ExternalKnowledgeState existingExternalKnowledge(String externalApiId, String externalKnowledgeId) {
        for (ExternalKnowledgeState externalKnowledge : externalKnowledge(externalApiId)) {
            if (externalKnowledgeId.equals(externalKnowledge.externalKnowledgeId)) {
                return externalKnowledge;
            }
        }
        throw new IllegalArgumentException("external knowledge not found");
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

    private boolean matchesOwner(String orgId, KeywordState keyword) {
        return isBlank(orgId) || orgId.equals(keyword.orgId);
    }

    private boolean matchesOwner(String orgId, ExternalApiState api) {
        return isBlank(orgId) || orgId.equals(api.orgId);
    }

    private boolean matchesOwner(String orgId, ExportRecordState record) {
        return isBlank(orgId) || orgId.equals(record.orgId);
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

    private List<SegmentState> segments(String docId) {
        List<SegmentState> segments = segmentsByDocId.get(docId);
        if (segments == null) {
            segments = new ArrayList<SegmentState>();
            segmentsByDocId.put(docId, segments);
        }
        return segments;
    }

    private List<QaPairState> qaPairs(String knowledgeId) {
        List<QaPairState> pairs = qaPairsByKnowledgeId.get(knowledgeId);
        if (pairs == null) {
            pairs = new ArrayList<QaPairState>();
            qaPairsByKnowledgeId.put(knowledgeId, pairs);
        }
        return pairs;
    }

    private List<ReportState> reports(String knowledgeId) {
        List<ReportState> reports = reportsByKnowledgeId.get(knowledgeId);
        if (reports == null) {
            reports = new ArrayList<ReportState>();
            reportsByKnowledgeId.put(knowledgeId, reports);
        }
        return reports;
    }

    private List<ExportRecordState> exportRecords(String knowledgeId) {
        List<ExportRecordState> records = exportRecordsByKnowledgeId.get(knowledgeId);
        if (records == null) {
            records = new ArrayList<ExportRecordState>();
            exportRecordsByKnowledgeId.put(knowledgeId, records);
        }
        return records;
    }

    private List<ExternalKnowledgeState> externalKnowledge(String externalApiId) {
        List<ExternalKnowledgeState> result = externalKnowledgeByApiId.get(externalApiId);
        if (result == null) {
            result = new ArrayList<ExternalKnowledgeState>();
            externalKnowledgeByApiId.put(externalApiId, result);
        }
        return result;
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

    private void createDefaultSegment(DocState doc) {
        List<SegmentState> segments = segments(doc.docId);
        if (!segments.isEmpty()) {
            return;
        }
        SegmentState segment = new SegmentState();
        segment.contentId = "segment-" + nextSegmentId.incrementAndGet();
        segment.docId = doc.docId;
        segment.content = "Imported document: " + doc.docName;
        segment.available = true;
        segment.contentNum = 1;
        segment.labels = new ArrayList<String>();
        segment.parent = false;
        segment.childNum = 0;
        segments.add(segment);
    }

    private void renumberSegments(List<SegmentState> segments) {
        for (int i = 0; i < segments.size(); i++) {
            segments.get(i).contentNum = i + 1;
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

    private ReportState newReport(String knowledgeId, String title, String content, boolean imported, boolean generated) {
        ReportState report = new ReportState();
        report.contentId = "report-" + nextReportId.incrementAndGet();
        report.knowledgeId = knowledgeId;
        report.title = title;
        report.content = content;
        report.createdAt = nowMillis();
        report.imported = imported;
        report.generated = generated;
        return report;
    }

    private Map<String, Object> toReportInfo(ReportState report) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("contentId", report.contentId);
        result.put("title", report.title);
        result.put("content", report.content);
        return result;
    }

    private String latestReportCreatedAt(List<ReportState> reports) {
        String latest = "";
        for (ReportState report : reports) {
            if (!isBlank(report.createdAt) && (isBlank(latest) || report.createdAt.compareTo(latest) > 0)) {
                latest = report.createdAt;
            }
        }
        return latest;
    }

    private String generatedReportContent(KnowledgeState knowledge) {
        int docCount = docs(knowledge.knowledgeId).size();
        int qaCount = qaPairs(knowledge.knowledgeId).size();
        return "Development community report for " + knowledge.name
                + ". Documents: " + docCount
                + ", QA pairs: " + qaCount + ".";
    }

    private void seedExternalKnowledgeCandidates(ExternalApiState api) {
        List<ExternalKnowledgeState> candidates = externalKnowledge(api.externalApiId);
        if (!candidates.isEmpty()) {
            return;
        }
        candidates.add(newExternalKnowledgeCandidate(api, "Development Dataset", 3));
        candidates.add(newExternalKnowledgeCandidate(api, "Operations Dataset", 5));
    }

    private ExternalKnowledgeState newExternalKnowledgeCandidate(ExternalApiState api, String suffix, int docCount) {
        ExternalKnowledgeState candidate = new ExternalKnowledgeState();
        candidate.externalKnowledgeId = "external-knowledge-" + nextExternalKnowledgeId.incrementAndGet();
        candidate.externalKnowledgeName = api.name + " " + suffix;
        candidate.externalApiId = api.externalApiId;
        candidate.provider = api.provider;
        candidate.userId = api.userId;
        candidate.orgId = api.orgId;
        candidate.docCount = docCount;
        candidate.mounted = false;
        candidate.mountedKnowledgeId = "";
        return candidate;
    }

    private Map<String, Object> toExternalApiInfo(ExternalApiState api) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("externalApiId", api.externalApiId);
        result.put("name", api.name);
        result.put("description", api.description);
        result.put("baseUrl", api.baseUrl);
        result.put("apiKey", api.apiKey);
        return result;
    }

    private Map<String, Object> toExternalKnowledgeInfo(ExternalApiState api, ExternalKnowledgeState externalKnowledge) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("externalKnowledgeId", externalKnowledge.externalKnowledgeId);
        result.put("externalKnowledgeName", externalKnowledge.externalKnowledgeName);
        result.put("externalApiId", api.externalApiId);
        result.put("externalApiName", api.name);
        result.put("externalApiUrl", api.baseUrl);
        result.put("externalApiKey", api.apiKey);
        result.put("externalSource", defaultIfBlank(externalKnowledge.provider, EXTERNAL_PROVIDER_DIFY));
        result.put("provider", defaultIfBlank(externalKnowledge.provider, EXTERNAL_PROVIDER_DIFY));
        result.put("docCount", externalKnowledge.docCount);
        result.put("retrievalModelInfo", retrievalModelInfo());
        return result;
    }

    private Map<String, Object> externalKnowledgeInfo(ExternalApiState api, ExternalKnowledgeState externalKnowledge,
                                                      String externalSource) {
        Map<String, Object> result = toExternalKnowledgeInfo(api, externalKnowledge);
        result.put("externalSource", defaultIfBlank(externalSource, EXTERNAL_PROVIDER_DIFY));
        result.put("provider", defaultIfBlank(externalSource, EXTERNAL_PROVIDER_DIFY));
        return result;
    }

    private Map<String, Object> retrievalModelInfo() {
        Map<String, Object> rerankingModel = new LinkedHashMap<String, Object>();
        rerankingModel.put("rerankingProviderName", "");
        rerankingModel.put("rerankingModelName", "");
        Map<String, Object> keywordSetting = new LinkedHashMap<String, Object>();
        keywordSetting.put("keywordWeight", 0.3D);
        Map<String, Object> vectorSetting = new LinkedHashMap<String, Object>();
        vectorSetting.put("vectorWeight", 0.7D);
        vectorSetting.put("embeddingModelName", "development-embedding");
        vectorSetting.put("embeddingProviderName", "wanwu-java");
        Map<String, Object> weights = new LinkedHashMap<String, Object>();
        weights.put("weightType", "customized");
        weights.put("keywordSetting", keywordSetting);
        weights.put("vectorSetting", vectorSetting);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("searchMethod", "hybrid_search");
        result.put("rerankingEnable", Boolean.FALSE);
        result.put("rerankingMode", "");
        result.put("rerankingModel", rerankingModel);
        result.put("weights", weights);
        result.put("topK", 3);
        result.put("scoreThresholdEnabled", Boolean.FALSE);
        result.put("scoreThreshold", 0D);
        return result;
    }

    private void mountExternalKnowledge(ExternalKnowledgeState externalKnowledge, String knowledgeId) {
        externalKnowledge.mounted = true;
        externalKnowledge.mountedKnowledgeId = knowledgeId;
    }

    private void unmountExternalKnowledge(KnowledgeState knowledge) {
        if (knowledge.externalKnowledgeInfo == null) {
            return;
        }
        String externalApiId = string(knowledge.externalKnowledgeInfo.get("externalApiId"));
        String externalKnowledgeId = string(knowledge.externalKnowledgeInfo.get("externalKnowledgeId"));
        for (ExternalKnowledgeState externalKnowledge : externalKnowledge(externalApiId)) {
            if (externalKnowledgeId.equals(externalKnowledge.externalKnowledgeId)
                    && knowledge.knowledgeId.equals(externalKnowledge.mountedKnowledgeId)) {
                externalKnowledge.mounted = false;
                externalKnowledge.mountedKnowledgeId = "";
            }
        }
    }

    private void ensureUniqueKnowledgeName(String orgId, String name, String currentKnowledgeId) {
        for (KnowledgeState knowledge : knowledgeBases.values()) {
            if (!matchesOwner(orgId, knowledge)) {
                continue;
            }
            if (!knowledge.knowledgeId.equals(currentKnowledgeId)
                    && knowledge.category == CATEGORY_KNOWLEDGE
                    && name.equals(knowledge.name)) {
                throw new IllegalArgumentException("knowledge already exists");
            }
        }
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

    private String fileNameFromUrl(String url) {
        if (isBlank(url)) {
            return "";
        }
        String clean = url;
        int query = clean.indexOf('?');
        if (query >= 0) {
            clean = clean.substring(0, query);
        }
        int hash = clean.indexOf('#');
        if (hash >= 0) {
            clean = clean.substring(0, hash);
        }
        int slash = clean.lastIndexOf('/');
        return slash >= 0 && slash < clean.length() - 1 ? clean.substring(slash + 1) : clean;
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

    private boolean segmentAvailable(Object value) {
        String raw = string(value);
        if ("false".equalsIgnoreCase(raw) || "0".equals(raw) || "disable".equalsIgnoreCase(raw)) {
            return false;
        }
        if ("true".equalsIgnoreCase(raw) || "1".equals(raw) || "enable".equalsIgnoreCase(raw)) {
            return true;
        }
        return booleanValue(value, true);
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

    private String nowMillis() {
        return String.valueOf(System.currentTimeMillis());
    }

    private void saveSnapshot() {
        if (knowledgeRecordMapper == null) {
            return;
        }
        KnowledgeRecordEntity entity = new KnowledgeRecordEntity();
        entity.setRecordType(TYPE_SNAPSHOT);
        entity.setRecordId(SNAPSHOT_ID);
        long now = System.currentTimeMillis();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        try {
            entity.setPayload(JSON.writeValueAsString(snapshot()));
            knowledgeRecordMapper.upsertRecord(entity);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to persist knowledge snapshot", ex);
        }
    }

    private KnowledgeSnapshot snapshot() {
        KnowledgeSnapshot snapshot = new KnowledgeSnapshot();
        snapshot.knowledgeBases.putAll(knowledgeBases);
        snapshot.tags.putAll(tags);
        snapshot.splitters.putAll(splitters);
        snapshot.tagIdsByKnowledgeId.putAll(tagIdsByKnowledgeId);
        snapshot.docsByKnowledgeId.putAll(docsByKnowledgeId);
        snapshot.segmentsByDocId.putAll(segmentsByDocId);
        snapshot.qaPairsByKnowledgeId.putAll(qaPairsByKnowledgeId);
        snapshot.qaPairsById.putAll(qaPairsById);
        snapshot.reportsByKnowledgeId.putAll(reportsByKnowledgeId);
        snapshot.exportRecordsByKnowledgeId.putAll(exportRecordsByKnowledgeId);
        snapshot.exportRecordsById.putAll(exportRecordsById);
        snapshot.externalApis.putAll(externalApis);
        snapshot.externalKnowledgeByApiId.putAll(externalKnowledgeByApiId);
        snapshot.keywords.putAll(keywords);
        snapshot.metasByKnowledgeId.putAll(metasByKnowledgeId);
        snapshot.permissionsByKnowledgeId.putAll(permissionsByKnowledgeId);
        snapshot.nextKnowledgeId = nextKnowledgeId.get();
        snapshot.nextTagId = nextTagId.get();
        snapshot.nextSplitterId = nextSplitterId.get();
        snapshot.nextDocId = nextDocId.get();
        snapshot.nextSegmentId = nextSegmentId.get();
        snapshot.nextQaPairId = nextQaPairId.get();
        snapshot.nextReportId = nextReportId.get();
        snapshot.nextExportRecordId = nextExportRecordId.get();
        snapshot.nextExternalApiId = nextExternalApiId.get();
        snapshot.nextExternalKnowledgeId = nextExternalKnowledgeId.get();
        snapshot.nextKeywordId = nextKeywordId.get();
        snapshot.nextMetaId = nextMetaId.get();
        snapshot.nextPermissionId = nextPermissionId.get();
        return snapshot;
    }

    private void applySnapshot(KnowledgeSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        knowledgeBases.clear();
        knowledgeBases.putAll(safeMap(snapshot.knowledgeBases));
        tags.clear();
        tags.putAll(safeMap(snapshot.tags));
        splitters.clear();
        splitters.putAll(safeMap(snapshot.splitters));
        if (splitters.isEmpty()) {
            seedSplitters();
        }
        tagIdsByKnowledgeId.clear();
        tagIdsByKnowledgeId.putAll(safeMap(snapshot.tagIdsByKnowledgeId));
        docsByKnowledgeId.clear();
        docsByKnowledgeId.putAll(safeMap(snapshot.docsByKnowledgeId));
        segmentsByDocId.clear();
        segmentsByDocId.putAll(safeMap(snapshot.segmentsByDocId));
        qaPairsByKnowledgeId.clear();
        qaPairsByKnowledgeId.putAll(safeMap(snapshot.qaPairsByKnowledgeId));
        qaPairsById.clear();
        qaPairsById.putAll(safeMap(snapshot.qaPairsById));
        rebuildQaPairIndex();
        reportsByKnowledgeId.clear();
        reportsByKnowledgeId.putAll(safeMap(snapshot.reportsByKnowledgeId));
        exportRecordsByKnowledgeId.clear();
        exportRecordsByKnowledgeId.putAll(safeMap(snapshot.exportRecordsByKnowledgeId));
        exportRecordsById.clear();
        exportRecordsById.putAll(safeMap(snapshot.exportRecordsById));
        rebuildExportRecordIndex();
        externalApis.clear();
        externalApis.putAll(safeMap(snapshot.externalApis));
        externalKnowledgeByApiId.clear();
        externalKnowledgeByApiId.putAll(safeMap(snapshot.externalKnowledgeByApiId));
        keywords.clear();
        keywords.putAll(safeMap(snapshot.keywords));
        metasByKnowledgeId.clear();
        metasByKnowledgeId.putAll(safeMap(snapshot.metasByKnowledgeId));
        permissionsByKnowledgeId.clear();
        permissionsByKnowledgeId.putAll(safeMap(snapshot.permissionsByKnowledgeId));

        restoreSequence(nextKnowledgeId, snapshot.nextKnowledgeId);
        restoreSequence(nextTagId, snapshot.nextTagId);
        restoreSequence(nextSplitterId, snapshot.nextSplitterId);
        restoreSequence(nextDocId, snapshot.nextDocId);
        restoreSequence(nextSegmentId, snapshot.nextSegmentId);
        restoreSequence(nextQaPairId, snapshot.nextQaPairId);
        restoreSequence(nextReportId, snapshot.nextReportId);
        restoreSequence(nextExportRecordId, snapshot.nextExportRecordId);
        restoreSequence(nextExternalApiId, snapshot.nextExternalApiId);
        restoreSequence(nextExternalKnowledgeId, snapshot.nextExternalKnowledgeId);
        restoreSequence(nextKeywordId, snapshot.nextKeywordId);
        restoreSequence(nextMetaId, snapshot.nextMetaId);
        restoreSequence(nextPermissionId, snapshot.nextPermissionId);
        for (KnowledgeState knowledge : knowledgeBases.values()) {
            bumpSequence(nextKnowledgeId, knowledge.knowledgeId, "knowledge-");
        }
        for (TagState tag : tags.values()) {
            bumpSequence(nextTagId, tag.tagId, "tag-");
        }
        for (SplitterState splitter : splitters.values()) {
            bumpSequence(nextSplitterId, splitter.splitterId, "splitter-");
        }
        for (List<DocState> docs : docsByKnowledgeId.values()) {
            for (DocState doc : safeList(docs)) {
                bumpSequence(nextDocId, doc.docId, "doc-");
            }
        }
        for (List<SegmentState> segments : segmentsByDocId.values()) {
            for (SegmentState segment : safeList(segments)) {
                bumpSequence(nextSegmentId, segment.contentId, "segment-");
            }
        }
        for (QaPairState pair : qaPairsById.values()) {
            bumpSequence(nextQaPairId, pair.qaPairId, "qa-");
        }
        for (List<ReportState> reports : reportsByKnowledgeId.values()) {
            for (ReportState report : safeList(reports)) {
                bumpSequence(nextReportId, report.contentId, "report-");
            }
        }
        for (ExportRecordState record : exportRecordsById.values()) {
            bumpSequence(nextExportRecordId, record.exportRecordId, "export-");
        }
        for (ExternalApiState api : externalApis.values()) {
            bumpSequence(nextExternalApiId, api.externalApiId, "external-api-");
        }
        for (List<ExternalKnowledgeState> externalKnowledgeList : externalKnowledgeByApiId.values()) {
            for (ExternalKnowledgeState externalKnowledge : safeList(externalKnowledgeList)) {
                bumpSequence(nextExternalKnowledgeId, externalKnowledge.externalKnowledgeId, "external-knowledge-");
            }
        }
        for (KeywordState keyword : keywords.values()) {
            if (keyword.id > nextKeywordId.get()) {
                nextKeywordId.set(keyword.id);
            }
        }
        for (List<Map<String, Object>> metas : metasByKnowledgeId.values()) {
            for (Map<String, Object> meta : safeList(metas)) {
                bumpSequence(nextMetaId, string(meta.get("metaId")), "meta-");
            }
        }
        for (List<PermissionState> permissions : permissionsByKnowledgeId.values()) {
            for (PermissionState permission : safeList(permissions)) {
                bumpSequence(nextPermissionId, permission.permissionId, "perm-");
            }
        }
    }

    private void rebuildQaPairIndex() {
        for (List<QaPairState> pairs : qaPairsByKnowledgeId.values()) {
            for (QaPairState pair : safeList(pairs)) {
                if (pair != null && !isBlank(pair.qaPairId)) {
                    qaPairsById.put(pair.qaPairId, pair);
                }
            }
        }
    }

    private void rebuildExportRecordIndex() {
        for (List<ExportRecordState> records : exportRecordsByKnowledgeId.values()) {
            for (ExportRecordState record : safeList(records)) {
                if (record != null && !isBlank(record.exportRecordId)) {
                    exportRecordsById.put(record.exportRecordId, record);
                }
            }
        }
    }

    private void restoreSequence(AtomicLong sequence, long value) {
        sequence.set(Math.max(1000L, value));
    }

    private void bumpSequence(AtomicLong sequence, String id, String prefix) {
        if (isBlank(id) || !id.startsWith(prefix)) {
            return;
        }
        try {
            long value = Long.parseLong(id.substring(prefix.length()));
            if (value > sequence.get()) {
                sequence.set(value);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private <K, V> Map<K, V> safeMap(Map<K, V> source) {
        return source == null ? Collections.<K, V>emptyMap() : source;
    }

    private <T> List<T> safeList(List<T> source) {
        return source == null ? Collections.<T>emptyList() : source;
    }

    private static final class KnowledgeSnapshot {
        private Map<String, KnowledgeState> knowledgeBases = new LinkedHashMap<String, KnowledgeState>();
        private Map<String, TagState> tags = new LinkedHashMap<String, TagState>();
        private Map<String, SplitterState> splitters = new LinkedHashMap<String, SplitterState>();
        private Map<String, List<String>> tagIdsByKnowledgeId = new LinkedHashMap<String, List<String>>();
        private Map<String, List<DocState>> docsByKnowledgeId = new LinkedHashMap<String, List<DocState>>();
        private Map<String, List<SegmentState>> segmentsByDocId = new LinkedHashMap<String, List<SegmentState>>();
        private Map<String, List<QaPairState>> qaPairsByKnowledgeId = new LinkedHashMap<String, List<QaPairState>>();
        private Map<String, QaPairState> qaPairsById = new LinkedHashMap<String, QaPairState>();
        private Map<String, List<ReportState>> reportsByKnowledgeId =
                new LinkedHashMap<String, List<ReportState>>();
        private Map<String, List<ExportRecordState>> exportRecordsByKnowledgeId =
                new LinkedHashMap<String, List<ExportRecordState>>();
        private Map<String, ExportRecordState> exportRecordsById =
                new LinkedHashMap<String, ExportRecordState>();
        private Map<String, ExternalApiState> externalApis = new LinkedHashMap<String, ExternalApiState>();
        private Map<String, List<ExternalKnowledgeState>> externalKnowledgeByApiId =
                new LinkedHashMap<String, List<ExternalKnowledgeState>>();
        private Map<String, KeywordState> keywords = new LinkedHashMap<String, KeywordState>();
        private Map<String, List<Map<String, Object>>> metasByKnowledgeId =
                new LinkedHashMap<String, List<Map<String, Object>>>();
        private Map<String, List<PermissionState>> permissionsByKnowledgeId =
                new LinkedHashMap<String, List<PermissionState>>();
        private long nextKnowledgeId;
        private long nextTagId;
        private long nextSplitterId;
        private long nextDocId;
        private long nextSegmentId;
        private long nextQaPairId;
        private long nextReportId;
        private long nextExportRecordId;
        private long nextExternalApiId;
        private long nextExternalKnowledgeId;
        private long nextKeywordId;
        private long nextMetaId;
        private long nextPermissionId;
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
        private int docCount;
        private Map<String, Object> externalKnowledgeInfo;
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

    private static final class SegmentState {
        private String contentId;
        private String docId;
        private String content;
        private boolean available;
        private int contentNum;
        private List<String> labels = new ArrayList<String>();
        private boolean parent;
        private int childNum;
    }

    private static final class KeywordState {
        private long id;
        private String userId;
        private String orgId;
        private String name;
        private String alias;
        private final List<String> knowledgeBaseIds = new ArrayList<String>();
        private String updatedAt;
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

    private static final class ReportState {
        private String contentId;
        private String knowledgeId;
        private String title;
        private String content;
        private String createdAt;
        private boolean imported;
        private boolean generated;
    }

    private static final class ExportRecordState {
        private String exportRecordId;
        private String knowledgeId;
        private String knowledgeName;
        private String exportType;
        private String userId;
        private String orgId;
        private String author;
        private int status;
        private String fileName;
        private String filePath;
        private String contentType;
        private String content;
        private String contentBase64;
        private String errorMsg;
        private String exportTime;
    }

    private static final class ExternalApiState {
        private String externalApiId;
        private String userId;
        private String orgId;
        private String name;
        private String description;
        private String baseUrl;
        private String apiKey;
        private String provider;
    }

    private static final class ExternalKnowledgeState {
        private String externalKnowledgeId;
        private String externalKnowledgeName;
        private String externalApiId;
        private String provider;
        private String userId;
        private String orgId;
        private int docCount;
        private boolean mounted;
        private String mountedKnowledgeId;
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
