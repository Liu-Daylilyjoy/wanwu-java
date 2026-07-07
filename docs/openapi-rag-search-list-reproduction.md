# OpenAPI RAG Search List Reproduction

Date: 2026-07-01

## Source Evidence

- Go registers `POST /service/api/openapi/v1/rag/chat` in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- Go OpenAPI RAG non-stream mode calls the same RAG stream pipeline, aggregates `data.output`, and keeps the legacy JSON response shape in `internal/bff-service/server/http/handler/openapi/openapi.go`.
- The Go response model has `OpenAPIRagChatResponse.data.searchList` in `internal/bff-service/model/response/openapi.go`, where each item exposes `kb_name`, `title`, and `snippet`.

## Java Reproduction

- `WanwuOpenApiController.chatRag` already routes published OpenAPI RAG calls into `AppService.streamRagChat`.
- The controller now accepts either `query` or `prompt` as the question field.
- The controller now maps `file_info` or `fileInfo` into `RagChatCommand.fileInfo`.
- `openApiRagChat` now normalizes Java knowledge-hit cards into Go-style `data.searchList[{kb_name,title,snippet}]`, while preserving `data.output`, `msg_id`, `history`, and `finish`. `qaSearchList` remains available as a Java compatibility extension for local QA recall.
- When the request contains `stream=true`, Java returns a legacy `text/event-stream` response with a single `data: {...}` JSON frame followed by `data: [DONE]`.
- Published OpenAPI RAG calls reuse `AppService.streamRagChat`, so the same question/answer/history/fileInfo/search-list snapshot is stored in `rag_chat_records` when AppService/MySQL is available.
- When the published RAG has a configured OpenAI-compatible `modelConfig.modelId`, Java forwards `needHistory=true` history items as ordered `user/assistant` messages before the current question.
- Service-layer validation failures on `POST /service/api/openapi/v1/rag/chat` keep the existing `400` JSON response and now record `source=openapi`, `appType=rag`, `success=false` app statistics.

## Tests

- `WanwuOpenApiControllerTest#chatRagAndWorkflowRoutesMapToExistingAppService` verifies published-mode RAG command mapping and asserts that `data.searchList[0]` exposes Go-style `kb_name`, `title`, and `snippet`.
- `WanwuOpenApiControllerTest#ragOpenApiStreamReturnsLegacySseWithSearchList` verifies `stream=true` legacy SSE compatibility with the same normalized search-list shape.
- `WanwuOpenApiControllerTest#ragOpenApiChatUsesConfiguredOpenAiCompatibleModelBeforePersisting` verifies configured-model RAG calls include ordered history messages before persistence.
- `WanwuOpenApiControllerTest#ragOpenApiChatRecordsFailureStatisticWhenServiceRejectsRequest` verifies failed OpenAPI RAG calls are counted in AppService statistics.

## Remaining Gap

The Java OpenAPI RAG endpoint now supports a legacy SSE envelope for `stream=true`, but it still emits one completed frame rather than provider token-by-token chunks. Full parity still needs real model streaming, exact upstream rag-service chunk sequencing, exact provider cost attribution, and stricter API-key scoped app authorization.
