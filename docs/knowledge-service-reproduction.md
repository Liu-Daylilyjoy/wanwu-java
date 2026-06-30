# Knowledge Service Reproduction Notes

Date: 2026-06-30

## Source Alignment

- Frontend callers: `web/src/api/knowledge.js`, `web/src/api/qaDatabase.js`, `web/src/views/knowledge/index.vue`, `web/src/views/knowledge/component/create.vue`, `web/src/views/knowledge/knowledgeDatabase/doclist.vue`, `tagDialog.vue`, `splitterDialog.vue`, and `power` views.
- Go BFF router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\knowledge.go`.
- Go request/response contracts: `internal\bff-service\model\request\knowledge*.go`, `internal\bff-service\model\response\knowledge*.go`.
- Go proto baseline: the `KnowledgeBase*` service groups in `D:\work\week3\wanwu\proto`.

## Java Coverage

- `wanwu-api` now exposes a Java `KnowledgeService` contract for the frontend-facing knowledge base surface.
- `wanwu-service-knowledge` implements an in-memory compatibility service for:
  - Knowledge base create/list/update/delete and hit shell.
  - Knowledge tag CRUD, bind, and bind-count.
  - Knowledge splitter preset list and custom CRUD.
  - Document list empty state, document config, import tip, upload limit, segment list shell, URL analysis shell, import shell, reimport shell, and delete shell.
  - Metadata key/value shells.
  - Permission owner/admin/user/org compatibility.
  - QA pair create/list/update/switch/delete, import-tip, export shell, and local text hit.
  - Graph, report, export record, and external knowledge compatibility shells.
- `wanwu-service-bff` maps the original frontend paths under `/user/api/v1/knowledge`.
- Docker Compose `full` profile includes `knowledge` on ports `8083` and `20883`, and BFF now depends on it.

## Current Storage Boundary

The Java knowledge service currently uses an in-memory repository. It preserves the frontend contracts that matter for real navigation:

- List payload uses `knowledgeList`.
- Knowledge items include `knowledgeId`, `name`, `orgName`, `description`, `docCount`, `embeddingModelInfo`, `knowledgeTagList`, `permissionType`, `share`, `ragName`, `graphSwitch`, `category`, `llmModelId`, `external`, `externalKnowledgeInfo`, and `avatar`.
- Document pages include `list`, `total`, `pageNo`, `pageSize`, and `docKnowledgeInfo`.
- Permission pages return `knowledgeUserInfoList`, `knowOrgInfoList`, and `userInfoList`.

This is enough for the frontend to open the Knowledge module, create a knowledge base, bind tags, view splitters, enter the document empty state, and open permission-related panels without backend 404s. It is not the final Go-equivalent storage or indexing model.

The QA database path now also has a working local loop: create a QA knowledge base (`category = 1`), create/edit/delete QA pairs, switch them on or off, list them with name/status filters, and run a deterministic hit test over enabled finished pairs. This mirrors the frontend and Go BFF shape but does not yet reproduce the Go service's asynchronous import or retrieval engine.

## Still Missing

- MySQL persistence for knowledge bases, tags, splitters, docs, metadata, permissions, QA pairs, reports, and external knowledge.
- Real document upload, file parsing, chunking, vector indexing, reimport, and export.
- Real QA file import parsing, persisted export records, and vector/keyword/rerank retrieval for QA hit tests.
- Real keyword extraction, graph generation, report generation, and RAG query integration.
- Callback status updates from asynchronous document processing.
