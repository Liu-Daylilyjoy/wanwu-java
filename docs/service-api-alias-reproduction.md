# Service API Alias Reproduction

Date: 2026-07-01

## Source Evidence

- Go BFF registers assistant listing at `internal/bff-service/server/http/handler/router/v1/assistant.go` as `GET /appspace/assistant/list`.
- Go BFF registers RAG upload at `internal/bff-service/server/http/handler/router/v1/rag.go` as `POST /rag/upload`.
- The current frontend also calls `web/src/api/agent.js` with `${USER_API}/assistant/list`.
- The current frontend calls `web/src/api/chunkFile.js` with `${SERVICE_API}/rag/upload`.

## Java Reproduction

- `GET /user/api/v1/assistant/list` is mapped to the same Java handler as `/user/api/v1/appspace/assistant/list`.
- `POST /service/api/v1/rag/upload` is implemented as a thin compatibility controller using the existing frontend RAG upload response contract.
- The upload contract returns `data.fileList[]` with `fileIndex` and a data URL in `fileUrl`; when `markdown=true`, the data URL is wrapped as a Markdown image.
- Empty uploads return the same development error shape as the user-domain upload: `code=1001`, `msg=file is empty`.

## Remaining Gap

This slice reproduces the frontend-visible route shape only. Real Go parity still needs the RAG service upload pipeline, object storage lifecycle, content parsing, indexing status, and downstream retrieval integration.
