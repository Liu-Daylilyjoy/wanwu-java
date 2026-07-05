# OpenAPI Knowledge Export Record Reproduction

Date: 2026-07-05

## Go Source Baseline

- `internal/bff-service/server/http/handler/openapi/knowledge.go` binds `POST /knowledge/doc/export` to `service.ExportKnowledgeDoc` and responds with an empty success envelope.
- Export records are read later through `GET /knowledge/export/record/list`.
- `KnowledgeExportRecordListReq` carries `knowledgeId`, `pageNo`, and `pageSize`.
- Export record rows expose `exportRecordId`, `author`, `exportTime`, `filePath`, `status`, `errorMsg`, and `knowledgeName`.

## Java Reproduction

- `WanwuOpenApiController#exportDocs` now keeps the Go async-task contract: it calls `KnowledgeService.exportDocs` but returns an empty data object instead of exposing internal `exportRecordId` or `fileUrl`.
- `GET /service/api/openapi/v1/knowledge/export/record/list` now forwards `pageNo` and `pageSize` to `KnowledgeService.listExportRecords`.
- Export record file paths returned from the Java frontend-style service path are rewritten to `/service/api/openapi/v1/knowledge/export/file/{exportRecordId}/{fileName}`.
- `GET /service/api/openapi/v1/knowledge/export/file/{exportRecordId}/{fileName}` resolves the file through `KnowledgeService.getExportRecordFile` and streams the local CSV/ZIP bytes with attachment headers.

## Verification

- `WanwuOpenApiControllerTest#openApiKnowledgeExportFollowsGoAsyncRecordContract`

## Remaining Gap

The local Java loop produces the export file synchronously through the snapshot-backed knowledge service. Go creates asynchronous export tasks and stores files in MinIO, so object-storage lifecycle, task progress timing, and signed/external download URLs remain later parity work.
