# Callback Compatibility Reproduction

Date: 2026-07-04

## Go Source Baseline

Original Go files inspected:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\init.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\callback\router.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\workflow.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\mcp.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\skill.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\skill_detail.go`

Go BFF registers public callback routes under `/callback/v1` and several internal callback/status aliases under the v1 API group.

## Java Coverage Added

Java controller:

- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiController.java`

Nginx gateway:

- `web/nginx.conf` now proxies `/callback/v1/` to the BFF service, so Docker Compose frontend access does not return gateway-level 404/502.

Tests:

- `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiControllerTest.java`

Covered route families:

- File callbacks: `/callback/v1/file/url/base64`, `/callback/v1/file/upload/base64`
- Image and tourism helper callbacks: `/callback/v1/image/outline`, `/callback/v1/tourism/poi/search`
- Model callback routes: info, chat completions, embeddings, multimodal embeddings, rerank, multimodal rerank, OCR, GUI, PDF parser, ASR
- Workflow/chatflow callback lists and MCP-backed workflow tool detail callbacks
- MCP callback details backed by Java `McpService`
- Agent callback chat SSE shell
- RAG callback search and stream search shells
- WGA sandbox run/cleanup callback shells
- App record shell plus skill detail/list callbacks backed by Java `McpService`, including Go-style `skillList` response bodies
- v1 callback aliases for doc status, deploy info, category info, doc status init, and knowledge status

## Current Contract

This slice is a compatibility shell:

- Routes no longer return `Not Found`.
- Response envelopes follow the frontend/BFF success shape where Go handlers are frontend-facing.
- File callbacks now read and upload real bytes through a local Docker-development store: `file/url/base64` supports `fileUrl/addPrefix/customPrefix`, and `file/upload/base64` returns Go-style `url/uri` while retaining Java compatibility ids.
- Tourism POI search now mirrors the Go local ranking service with `query/results`, category and keyword filtering, radius filtering, rating/distance sorting, and limit normalization.
- Model chat/embedding/rerank callbacks use OpenAI-compatible response shapes because those routes are typically consumed by external model adapters.
- Workflow tool, MCP, and Skill metadata callbacks read the same Docker MySQL-backed Java `McpService` resource snapshot used by the frontend, with deterministic fallback data when that service is unavailable.
- Image outline returns the Go `ImageOutlineExtractResp` shape (`message`, `prompt`, `markdown`, `result`, `mimeType`, `url`, `uri`, `usage`) and stores a local downloadable PNG under `/callback/v1/file/{fileId}` for Docker development.
- Stream routes use `text/event-stream` with deterministic development payloads.
- Mutating callback routes echo status and request data for development observability.

## Remaining Gaps

- Provider-specific model execution is still partial; configured OpenAI-compatible chat/embedding/rerank endpoints can proxy, while OCR/ASR/PDF/GUI remain local shells.
- Callback file upload/download is BFF-local, not MinIO/object-storage-backed yet.
- Image outline does not call DashScope Qwen image edit or MinIO yet; it returns a deterministic local PNG while preserving the Go response contract and `response_format` validation.
- Real OCR/ASR/PDF parsing/GUI tasks are not implemented.
- Real RAG recall and knowledge-base stream search are not implemented.
- Real WGA sandbox execution and cleanup are not implemented.
- Callback authentication, callback signature verification, and usage metrics are not reproduced yet.
