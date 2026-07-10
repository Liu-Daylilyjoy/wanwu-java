# OpenAPI Chatflow Session Reproduction

Date: 2026-07-10

## Source Evidence

- Go registers Chatflow OpenAPI routes in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- The handlers live in `internal/bff-service/server/http/handler/openapi/workflow.go` and cover conversation create/delete/list, message list, chat, and file upload.
- The Go response models include `OpenAPIChatflowConversationListResponse` and `OpenAPIChatflowGetConversationMessageListResponse`.
- Go persists the application/conversation association in BFF, but delegates graph execution and SSE events to the separately deployed `workflow-wanwu` runtime. That runtime source is not part of the Go repository.

## Java Reproduction

- `AppService` now exposes Chatflow OpenAPI conversation create/list/message-list/chat/delete methods.
- Chatflow OpenAPI conversations and turns are persisted through the existing `assistant_conversations` and `assistant_conversation_messages` tables with `conversation_type=chatflow_openapi` and the Chatflow `appId` stored in `assistant_id`.
- `OpenApiChatflowSessionStore` remains a BFF-local fallback when AppService is unavailable or a development request targets a Chatflow id that has not been created in Java yet.
- `POST /service/api/openapi/v1/chatflow/conversation` creates a scoped conversation and returns `conversation_id`, `conversationId`, `conversation_name`, and `uuid`.
- `POST /service/api/openapi/v1/chatflow/chat` executes the saved Chatflow graph, persists a workflow run, records the user/model turn, emits the documented `text/event-stream` event sequence, and records OpenAPI-source app statistics.
- Runtime input contains the OpenAPI `parameters` plus reserved aliases `query`, `Query`, `question`, `message`, and `input`, so existing start-node schemas can consume the current user message without frontend changes.
- LLM nodes invoke `ModelService` only when the owning application is a Chatflow. Workflow LLM behavior remains unchanged.
- LLM configuration resolves Coze-style `llmParam` values for model id, prompt, system prompt, history switch/rounds, temperature, top-p, frequency penalty, and max tokens.
- Intent nodes invoke the configured model for Chatflow and parse a structured zero-based `classificationId` and `reason`; invalid or unavailable provider output falls back to the existing deterministic classifier.
- Knowledge nodes call `KnowledgeService`, and their prompt/search cards can be referenced by downstream LLM nodes. Aggregated cards are returned as `search_list` and persisted with the turn.
- Configured `type=1009` MCP nodes resolve the server/tool from `mcpInfoList`, pass only declared node inputs as arguments, and call `McpService.callMcpServerTool`. Unconfigured template nodes retain an explicit `providerFallback=true` local preview.
- When `enableChatHistory=true`, the configured number of persisted prior turns is inserted before the current prompt. Provider chunks and usage metadata are retained in node output.
- Chat responses expose `run_id`, `chunks`, `search_list`, and `node_events`; chunks, knowledge hits, and node events are also stored with the conversation message.
- SSE output follows the repository manual: `conversation.chat.created`, `conversation.chat.in_progress`, one `conversation.message.delta` per provider chunk, `conversation.message.completed`, `conversation.chat.completed`, and `done`. Provider token usage is normalized to `input_count`, `output_count`, and `token_count`.
- Empty development canvases fall back to the current user query instead of the removed hard-coded `Chatflow response:` placeholder.
- `POST /service/api/openapi/v1/chatflow/conversation/message/list` returns Go-shaped message rows under `data`, with `has_more`, `first_id`, and `last_id`.
- `POST /service/api/openapi/v1/chatflow/conversation/list` returns `conversations`, `list`, and `total`.
- `DELETE /service/api/openapi/v1/chatflow/conversation` removes the persisted scoped conversation and its messages.

## Verification

- `AppServiceImplTest#chatflowOpenApiConversationsPersistMessagesAndListState` verifies persisted create, chat, message list, conversation list, and delete behavior.
- `AppServiceImplTest#chatflowOpenApiChatExecutesConfiguredModelWithConversationHistory` verifies start-to-LLM-to-end graph execution, provider chunks, model parameters, persisted answers, and second-turn history ordering.
- `AppServiceImplTest#chatflowOpenApiChatGroundsModelWithKnowledgeNodeResults` verifies knowledge evidence reaches the Chatflow LLM and search cards reach the API result.
- `AppServiceImplTest#chatflowOpenApiChatExecutesConfiguredMcpNode` verifies real MCP dispatch and declared-argument isolation.
- `AppServiceImplTest#chatflowOpenApiChatUsesConfiguredModelForIntentNode` verifies provider-backed intent classification.
- `WanwuOpenApiControllerTest#chatflowOpenApiRoutesUseAppServiceConversationState` verifies the OpenAPI route family uses AppService first while preserving the Go-shaped response contracts.
- `WanwuOpenApiControllerTest#chatflowOpenApiChatRecordsFailureStatisticWhenServiceRejectsRequest` verifies Chatflow OpenAPI service failures are not hidden by the BFF fallback and are counted as failed stream calls.
- Full `AppServiceImplTest` regression passes on Java 8.

## Remaining Gap

The Java runtime now executes the supported graph nodes and real model, knowledge, HTTP, and MCP integrations and reproduces the documented multi-frame SSE contract. Remaining Chatflow parity work is published-versus-draft OpenAPI selection and authorization, general-purpose code-node isolation, file upload consumption, failure-node policies, and exact provider/runtime usage attribution.
