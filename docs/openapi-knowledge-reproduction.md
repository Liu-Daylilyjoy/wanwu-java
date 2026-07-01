# OpenAPI Knowledge Service Proxy Reproduction

Date: 2026-07-01

## Source Evidence

- Go registers the public Knowledge OpenAPI group in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- The Go route group covers direct file upload, knowledge create/update/delete/select, document config/list/import/update/export/delete, export records, and knowledge hit testing.
- Go applies API-key context, knowledge authorization, and model authorization middleware before dispatching to the normal knowledge handlers.

## Java Reproduction

- `WanwuOpenApiController` now injects `KnowledgeService` alongside `AppService` and `ModelService`.
- Public `/service/api/openapi/v1/knowledge` create/update/delete/select routes now call the Java knowledge service with the OpenAPI user/org context.
- Document routes now proxy to `getDocConfig`, `listDocs`, `importDocs`, `updateDocConfig`, `exportDocs`, `getDocImportTip`, `listExportRecords`, `deleteDocs`, and `deleteExportRecord`.
- `POST /service/api/openapi/v1/knowledge/hit` now calls the local Java knowledge hit implementation, so OpenAPI callers can exercise the same deterministic document/segment match loop used by the frontend and RAG compatibility path.
- Create responses preserve `knowledgeId` and also expose `knowledge_id` for snake-case OpenAPI clients.

## Verification

- `WanwuOpenApiControllerTest#modelKnowledgeUploadOauthAndMcpShellsDoNotReturnNotFound` verifies create/select/doc-config/doc-import/hit route dispatch and user/org propagation.
- Docker Compose smoke creates a knowledge base through the public OpenAPI path, imports a development document descriptor, and verifies `/knowledge/hit` returns a real `searchList` item.

## Remaining Gap

This slice removes the OpenAPI Knowledge shell for the frontend-visible local loop. Full Go parity still needs exact API-key app authorization, normalized Go knowledge tables, real file parsing and indexing, vector/rerank retrieval, MinIO-backed async import/export, API usage metrics, and middleware-level authorization parity.
