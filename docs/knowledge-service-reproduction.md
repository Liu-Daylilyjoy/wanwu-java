# Knowledge Service Reproduction Notes

Date: 2026-06-30

## Source Alignment

- Frontend callers: `web/src/api/knowledge.js`, `web/src/api/keyword.js`, `web/src/api/qaDatabase.js`, `web/src/views/knowledge/index.vue`, `web/src/views/knowledge/component/create.vue`, `web/src/components/externalAPIDrawer.vue`, `web/src/components/externalAPIDialog.vue`, `web/src/views/knowledge/knowledgeDatabase/doclist.vue`, `web/src/views/knowledge/component/communityReport/*`, `web/src/views/knowledge/keyword/*`, `tagDialog.vue`, `splitterDialog.vue`, and `power` views.
- Go BFF router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\knowledge.go`.
- Go request/response contracts: `internal\bff-service\model\request\knowledge*.go`, `internal\bff-service\model\response\knowledge*.go`.
- Go proto baseline: the `KnowledgeBase*` service groups in `D:\work\week3\wanwu\proto`.

## Java Coverage

- `wanwu-api` now exposes a Java `KnowledgeService` contract for the frontend-facing knowledge base surface.
- `wanwu-service-knowledge` implements a Docker MySQL-backed compatibility service for:
  - Knowledge base create/list/update/delete and local document-segment hit test.
  - Knowledge tag CRUD, bind, and bind-count.
  - Knowledge keyword create/list/detail/update/delete, knowledge-base association, and document-page keyword echo.
  - Knowledge splitter preset list and custom CRUD.
  - Document URL analysis, local document import/list/delete, document config, import tip, upload limit, default segment generation, segment create/update/delete/status/labels, and child segment create/list/update/delete.
  - Metadata key/value shells.
  - Permission owner/admin/user/org compatibility.
  - QA pair create/list/update/switch/delete, import-tip, local CSV export record, and local text hit.
  - Community report list/add/update/delete, deterministic local generate, batch-add placeholder, and frontend status fields.
  - External API create/list/update/delete, external dataset selection, external knowledge create/update/delete, and external knowledge list integration.
  - Graph compatibility shell plus QA/document export record list, delete, and local download compatibility.
- `wanwu-service-bff` maps the original frontend paths under `/user/api/v1/knowledge`.
- Docker Compose `full` profile includes `knowledge` on ports `8083` and `20883`, and BFF now depends on it.

## Current Storage Boundary

The Java knowledge service currently uses a Docker MySQL snapshot repository in `knowledge_service.knowledge_records`. It preserves the frontend contracts that matter for real navigation:

- List payload uses `knowledgeList`.
- Knowledge items include `knowledgeId`, `name`, `orgName`, `description`, `docCount`, `embeddingModelInfo`, `knowledgeTagList`, `permissionType`, `share`, `ragName`, `graphSwitch`, `category`, `llmModelId`, `external`, `externalKnowledgeInfo`, and `avatar`.
- Document pages include `list`, `total`, `pageNo`, `pageSize`, and `docKnowledgeInfo`.
- Permission pages return `knowledgeUserInfoList`, `knowOrgInfoList`, and `userInfoList`.

This is enough for the frontend to open the Knowledge module, create internal and external knowledge bases, bind tags, create and edit keyword mappings, view splitters, import local development document descriptors, see the document list, inspect and edit local segments and child segments, open permission-related panels, and manage community reports without backend 404s. Mutable knowledge, external API, external dataset mount state, tag, keyword, splitter, doc, segment, child segment, metadata, permission, QA-pair, and report state now survives Docker restarts. It is not the final Go-equivalent storage or indexing model.

The document knowledge hit path now follows the Go BFF and proto response shape for `prompt`, `searchList`, `score`, and `useGraph`. It runs a deterministic local match over enabled imported document segments and their child segments, fills frontend card fields (`title`, `snippet`, `knowledgeName`, `contentType = text`, `childContentList`, `childScore`, `score`, and `rerankInfo`), honors `topK` and score/threshold inputs, and ignores disabled parent segments. This gives `/knowledge/hit` a real frontend-visible loop while the true RAG/vector/rerank integration remains a later replacement.

The QA database path now also has a working local loop: create a QA knowledge base (`category = 1`), create/edit/delete QA pairs, switch them on or off, list them with name/status filters, export QA pairs into a persisted local CSV export record, and run a deterministic hit test over enabled finished pairs. This mirrors the frontend and Go BFF shape but does not yet reproduce the Go service's asynchronous import or retrieval engine.

The document path stores imported `docInfoList` entries in memory, derives a default segment for each imported document, supports URL basename analysis, lets the frontend create/update/delete/enable/disable/tag parent segments, create/list/update/delete child segments, and creates persisted local ZIP export records for selected documents. The implementation deliberately does not parse file bytes, auto-split parent/child chunks from uploads, or build embeddings yet.

The export-record path now follows the Go `knowledge_export_task` frontend contract for `exportRecordId`, `author`, `status`, `filePath`, `errorMsg`, `exportTime`, and `knowledgeName`. QA export and document export write to the same snapshot-backed record list; `/knowledge/export/record/list` paginates it, `/knowledge/export/record` deletes it, and `/knowledge/export/file/{exportRecordId}/{fileName}` returns local CSV/ZIP bytes for the unchanged frontend download button. The Go service runs asynchronous export tasks and uploads files to MinIO; the Java reproduction completes the local development export synchronously.

The community report path now has the frontend-visible loop from Go's `KnowledgeBaseReportService`: list returns `list`, `total`, pagination, `createdAt`, `status`, `canGenerate`, `canAddReport`, `generateLabel`, and `lastImportStatus`; single add, update, delete, generate, and batch-add calls mutate the same snapshot. `generate` creates or refreshes a deterministic development report, and `batch-add` records the uploaded file id as an imported report placeholder. The Go implementation delegates to graph/RAG report generation and CSV import tasks; those runtime integrations remain later work.

The external knowledge path now follows the Go BFF contract from `knowledge_external.go`: `/knowledge/external/api/select`, `/knowledge/external/api`, `/knowledge/external/select`, and `/knowledge/external` all dispatch to `KnowledgeService`. The Go service validates and lists Dify datasets over the external API; the Java Docker reproduction uses a persisted local candidate dataset list per external API so frontend flows do not depend on an external Dify server. Creating an external knowledge base mounts one candidate, creates a normal Wanwu `knowledgeId` with `external = 1`, and returns `externalKnowledgeInfo` in `/knowledge/select`. Updating or deleting external knowledge mutates the same snapshot and releases the candidate for reuse.

## Still Missing

- Normalized Go-equivalent MySQL tables for knowledge bases, tags, keywords, splitters, docs, metadata, permissions, QA pairs, reports, external APIs, and external knowledge.
- Real document upload byte handling, file parsing, automatic parent-child chunking, vector indexing/rerank retrieval, reimport, MinIO-backed asynchronous export files, and asynchronous callback status updates.
- Real QA file import parsing, MinIO-backed asynchronous export tasks, and vector/keyword/rerank retrieval for QA hit tests.
- Real keyword extraction/sync into the RAG engine, graph generation, graph-derived report generation, CSV report import parsing, Dify external API validation/listing, and RAG query integration.
- Callback status updates from asynchronous document processing.
