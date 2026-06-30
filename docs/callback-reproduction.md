# Callback Compatibility Reproduction

Date: 2026-06-30

## Go Source Baseline

Original Go files inspected:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\init.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\callback\router.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\callback.go`

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
- Workflow/chatflow callback lists and tool detail shells
- MCP callback detail shells
- Agent callback chat SSE shell
- RAG callback search and stream search shells
- WGA sandbox run/cleanup callback shells
- App record and skill callback shells
- v1 callback aliases for doc status, deploy info, category info, doc status init, and knowledge status

## Current Contract

This slice is a compatibility shell:

- Routes no longer return `Not Found`.
- Response envelopes follow the frontend/BFF success shape where Go handlers are frontend-facing.
- Model chat/embedding/rerank callbacks use OpenAI-compatible response shapes because those routes are typically consumed by external model adapters.
- Stream routes use `text/event-stream` with deterministic development payloads.
- Mutating callback routes echo status and request data for development observability.

## Remaining Gaps

- Real model provider invocation is not implemented.
- Real OCR/ASR/PDF parsing/GUI tasks are not implemented.
- Real RAG recall and knowledge-base stream search are not implemented.
- Real WGA sandbox execution and cleanup are not implemented.
- Doc/knowledge status callbacks do not yet mutate persisted MySQL-backed import/indexing state.
- Callback authentication, callback signature verification, and usage metrics are not reproduced yet.
