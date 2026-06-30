# Knowledge MySQL Persistence Slice

Date: 2026-06-30

## Go Source Baseline

Original files inspected:

- `D:\work\week3\wanwu\configs\microservice\knowledge-service\configs\config.yaml`
- `D:\work\week3\wanwu\internal\knowledge-service\client\model\knowledge.go`
- `D:\work\week3\wanwu\internal\knowledge-service\client\model\knowledge_doc.go`
- `D:\work\week3\wanwu\internal\knowledge-service\client\model\knowledge_qa_pair.go`
- `D:\work\week3\wanwu\internal\knowledge-service\client\model\knowledge_tag.go`
- `D:\work\week3\wanwu\internal\knowledge-service\client\model\knowledge_tag_relation.go`
- `D:\work\week3\wanwu\internal\knowledge-service\client\model\knowledge_keywords.go`
- `D:\work\week3\wanwu\internal\knowledge-service\client\model\knowledge_splitter.go`
- `D:\work\week3\wanwu\internal\knowledge-service\client\model\knowledge_permission.go`

The Go knowledge service selects `db.name: mysql`, connects to the `knowledge_service` schema, and uses GORM tables for knowledge bases, documents, QA pairs, tags, tag relations, splitters, permissions, metadata, tasks, reports, and external knowledge records. It also integrates MinIO, Kafka, and RAG HTTP services for document import, indexing, graph/report generation, and async callbacks.

## Java Coverage Added

- `docker-compose.yml` passes `SPRING_DATASOURCE_*` to `knowledge` and waits for healthy `mysql`.
- `wanwu-service-knowledge` depends on `wanwu-common-data`.
- `wanwu-service-knowledge/src/main/resources/db/migration/V1__create_knowledge_records.sql` creates `knowledge_records`.
- `KnowledgeServiceImpl` loads and saves one JSON snapshot containing the current frontend-compatible knowledge, tag, keyword, splitter, document, segment, metadata, permission, QA-pair, report, external knowledge, and export-record state.

## Persistence Shape

The current table is a compatibility snapshot table:

- `record_type`: `snapshot`
- `record_id`: `state`
- `payload`: JSON snapshot of the current Java compatibility state and ID sequences
- `created_at` and `updated_at`: millisecond timestamps

This keeps the zero-frontend-change routes durable across Docker restarts without prematurely inventing the final relational schema.

## Verified Behavior

- Unit tests cover snapshot upsert, startup reload, restored knowledge/tag/keyword/doc/QA/report/external/export-record state, local export-file retrieval, and sequence continuation.
- Docker smoke should create a knowledge base through `localhost:3000`, recreate knowledge and BFF containers, verify the knowledge base still appears, and confirm a row exists in `knowledge_service.knowledge_records`.

## Remaining Gaps

- Normalize the snapshot into Go-equivalent tables such as `knowledge_base`, `knowledge_doc`, `knowledge_qa_pair`, `knowledge_keywords`, `knowledge_tag`, `knowledge_tag_relation`, `knowledge_splitter`, and `knowledge_permission`.
- Implement real file byte ingestion, MinIO object lifecycle, document parsing, chunking, vector indexing, reimport, child segments, asynchronous MinIO export tasks, and async status callbacks.
- Implement real QA import/export parsing, asynchronous MinIO export task execution, vector/keyword/rerank retrieval, keyword extraction/sync, graph generation, report generation, and RAG query integration.
