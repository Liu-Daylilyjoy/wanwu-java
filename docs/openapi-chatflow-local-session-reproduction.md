# OpenAPI Chatflow Session Reproduction

Date: 2026-07-01

## Source Evidence

- Go registers Chatflow OpenAPI routes in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- The handlers live in `internal/bff-service/server/http/handler/openapi/workflow.go` and cover conversation create/delete/list, message list, chat, and file upload.
- The Go response models include `OpenAPIChatflowConversationListResponse` and `OpenAPIChatflowGetConversationMessageListResponse`.

## Java Reproduction

- `AppService` now exposes Chatflow OpenAPI conversation create/list/message-list/chat/delete methods.
- Chatflow OpenAPI conversations and deterministic development turns are persisted through the existing `assistant_conversations` and `assistant_conversation_messages` tables with `conversation_type=chatflow_openapi` and the Chatflow `appId` stored in `assistant_id`.
- `OpenApiChatflowSessionStore` remains a BFF-local fallback when AppService is unavailable or a development request targets a Chatflow id that has not been created in Java yet.
- `POST /service/api/openapi/v1/chatflow/conversation` creates a scoped conversation and returns `conversation_id`, `conversationId`, `conversation_name`, and `uuid`.
- `POST /service/api/openapi/v1/chatflow/chat` records a user message and deterministic assistant response, emits one legacy `text/event-stream` frame, and records OpenAPI-source app statistics. Service-layer validation failures now return `400` JSON and still record `success=false`, `stream=true`, `source=openapi` app statistics, matching the Go `ChatflowChat` deferred statistic behavior.
- `POST /service/api/openapi/v1/chatflow/conversation/message/list` returns Go-shaped message rows under `data`, with `has_more`, `first_id`, and `last_id`.
- `POST /service/api/openapi/v1/chatflow/conversation/list` returns `conversations`, `list`, and `total`.
- `DELETE /service/api/openapi/v1/chatflow/conversation` removes the persisted scoped conversation and its messages.

## Verification

- `AppServiceImplTest#chatflowOpenApiConversationsPersistMessagesAndListState` verifies persisted create, chat, message list, conversation list, and delete behavior.
- `WanwuOpenApiControllerTest#chatflowOpenApiRoutesUseAppServiceConversationState` verifies the OpenAPI route family uses AppService first while preserving the Go-shaped response contracts.
- `WanwuOpenApiControllerTest#chatflowOpenApiChatRecordsFailureStatisticWhenServiceRejectsRequest` verifies Chatflow OpenAPI service failures are not hidden by the BFF fallback and are counted as failed stream calls.

## Remaining Gap

This is still a deterministic development Chatflow loop, not the Go Coze runtime. Full parity still needs real project execution, streaming event sequencing, published-app authorization, workflow node execution, and exact provider/runtime usage attribution.
