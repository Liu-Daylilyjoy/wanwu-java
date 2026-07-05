# OpenAPI Agent Chat Reproduction

Date: 2026-07-05

## Go Source Baseline

- Routes are registered in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- Published chat is handled by `internal/bff-service/server/http/handler/openapi/openapi.go#ChatAgent`.
- Draft chat is handled by `internal/bff-service/server/http/handler/openapi/openapi.go#DraftChatAgent`.
- Request/response contracts are defined in `internal/bff-service/model/request/openapi.go` and `internal/bff-service/model/response/openapi.go`.

Go behavior used for this slice:

- `POST /agent/chat` accepts `uuid`, `conversation_id`, `query`, `stream`, and `file_info`.
- `stream=true` returns `text/event-stream`; non-stream mode aggregates the assistant stream into one JSON response.
- `POST /agent/chat/draft` uses draft assistant configuration, auto-resolves a draft conversation when `conversation_id` is omitted, and streams the response.
- Agent responses expose `response`, `gen_file_url_list`, `search_list`, `history`, `usage`, and `finish`.
- `file_info` currently accepts at most one file.

## Java Reproduction

- `WanwuOpenApiController` now separates `/agent/chat` and `/agent/chat/draft` instead of sending both through published mode.
- Published chat still supports JSON by default, and `stream=true` returns the same legacy single-frame SSE envelope used by the OpenAPI RAG compatibility route.
- Draft chat sets `AssistantConversationStreamCommand.draft = true` and returns single-frame SSE, letting `AppServiceImpl.resolveConversation` reuse or create the draft conversation when no `conversation_id` is supplied.
- `AssistantConversationStreamResult` now carries `searchList`; `AppServiceImpl.streamAssistantConversation` fills it from configured local knowledge hits.
- The BFF maps `searchList` to Go-style `search_list[{kb_name,title,snippet}]`.
- The OpenAPI Agent route enforces the Go one-file `file_info` limit.

## Verification

- `WanwuOpenApiControllerTest#chatRagAndWorkflowRoutesMapToExistingAppService`
- `WanwuOpenApiControllerTest#agentOpenApiStreamReturnsLegacySseWithSearchList`
- `WanwuOpenApiControllerTest#draftAgentOpenApiUsesDraftConversationAndSse`

## Remaining Gap

This still emits a completed single-frame SSE response from the local deterministic AppService path. Full parity needs true provider token streaming, exact Go chunk sequencing, exact usage/cost fields, generated file URL handling, and API-key scoped app authorization.
