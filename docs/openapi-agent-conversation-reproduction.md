# OpenAPI Agent Conversation Reproduction

Date: 2026-07-05

## Go Source Baseline

- Routes are registered in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- Conversation handlers live in `internal/bff-service/server/http/handler/openapi/openapi.go` and `internal/bff-service/server/http/handler/openapi/agent.go`.
- `OpenAPIAgentCreateConversationResponse` in `internal/bff-service/model/response/openapi.go` returns `conversation_id`.
- List/detail routes return Go `PageResult` with `ConversationInfo` and `ConversationDetailInfo`, whose conversation fields are camelCase.

## Java Reproduction

- `POST /service/api/openapi/v1/agent/conversation` still calls `AppService.createAssistantConversation` with `conversationType=published`.
- The BFF now maps the internal `conversationId` result to Go-style `conversation_id`.
- The response also keeps `conversationId` as a compatibility alias for existing Java/front-end callers.
- List/detail/delete/clear continue to reuse the AppService conversation APIs because their page and item field shapes already match the Go response structs.

## Verification

- `WanwuOpenApiControllerTest#agentManagementAndConversationRoutesUseOpenApiContext`

## Remaining Gap

Full Go parity still needs exact public UUID-to-internal assistant id conversion when those identifiers diverge, API-key scoped app authorization, and exact normalized Go persistence tables.
