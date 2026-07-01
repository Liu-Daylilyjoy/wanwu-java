# OpenAPI Chatflow Local Session Reproduction

Date: 2026-07-01

## Source Evidence

- Go registers Chatflow OpenAPI routes in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- The handlers live in `internal/bff-service/server/http/handler/openapi/workflow.go` and cover conversation create/delete/list, message list, chat, and file upload.
- The Go response models include `OpenAPIChatflowConversationListResponse` and `OpenAPIChatflowGetConversationMessageListResponse`.

## Java Reproduction

- `OpenApiChatflowSessionStore` provides a small BFF-local state holder for development OpenAPI conversations.
- `POST /service/api/openapi/v1/chatflow/conversation` creates a scoped conversation and returns `conversation_id`, `conversationId`, `conversation_name`, and `uuid`.
- `POST /service/api/openapi/v1/chatflow/chat` records a user message and deterministic assistant response, then returns a legacy `text/event-stream` frame.
- `POST /service/api/openapi/v1/chatflow/conversation/message/list` returns Go-shaped message rows under `data`, with `has_more`, `first_id`, and `last_id`.
- `POST /service/api/openapi/v1/chatflow/conversation/list` returns `conversations`, `list`, and `total`.
- `DELETE /service/api/openapi/v1/chatflow/conversation` removes the scoped local conversation.

## Verification

- `WanwuOpenApiControllerTest#chatflowOpenApiRoutesKeepLocalConversationState` verifies create, chat, message list, conversation list, and delete behavior.

## Remaining Gap

This is a local development Chatflow loop, not the Go Coze runtime. Full parity still needs real project execution, streaming event sequencing, durable conversation persistence, published-app authorization, workflow node execution, and API usage metrics.
