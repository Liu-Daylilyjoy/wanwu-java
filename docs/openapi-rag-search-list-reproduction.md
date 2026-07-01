# OpenAPI RAG Search List Reproduction

Date: 2026-07-01

## Source Evidence

- Go registers `POST /service/api/openapi/v1/rag/chat` in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- Go OpenAPI RAG non-stream mode calls the same RAG stream pipeline, aggregates `data.output`, and keeps the legacy JSON response shape in `internal/bff-service/server/http/handler/openapi/openapi.go`.
- The Go response model has `OpenAPIRagChatResponse.data.searchList` in `internal/bff-service/model/response/openapi.go`.

## Java Reproduction

- `WanwuOpenApiController.chatRag` already routes published OpenAPI RAG calls into `AppService.streamRagChat`.
- The controller now accepts either `query` or `prompt` as the question field.
- The controller now maps `file_info` or `fileInfo` into `RagChatCommand.fileInfo`.
- `openApiRagChat` now returns `data.searchList` and `data.qaSearchList` from `RagChatResult`, while preserving `data.output`, `msg_id`, `history`, and `finish`.

## Tests

- `WanwuOpenApiControllerTest#chatRagAndWorkflowRoutesMapToExistingAppService` verifies published-mode RAG command mapping and asserts that `data.searchList[0].title` is returned to OpenAPI callers.

## Remaining Gap

The Java OpenAPI RAG endpoint is still non-streaming JSON only. Go also supports legacy SSE streaming when `stream=true`. Full parity still needs provider token streaming, exact legacy SSE line format, statistics recording, and API-key scoped app authorization.
