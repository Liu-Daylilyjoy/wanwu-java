# OpenAPI Chatflow Session Reproduction

Date: 2026-07-10

## Source Evidence

- Go registers Chatflow OpenAPI routes in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- The handlers live in `internal/bff-service/server/http/handler/openapi/workflow.go` and cover conversation create/delete/list, message list, chat, and file upload.
- The Go response models include `OpenAPIChatflowConversationListResponse` and `OpenAPIChatflowGetConversationMessageListResponse`.
- Go persists the application/conversation association in BFF, but delegates graph execution and SSE events to the separately deployed `workflow-wanwu` runtime. That runtime source is not part of the Go repository.
- The Go frontend pages `web/src/views/workflowNew/index.vue` and `workflowRunNew/index.vue` embed that runtime as an iframe; node execution policy is therefore not implemented in the Go BFF.
- All bundled Go workflow templates use `settingOnError.processType=1` and optional `timeoutMs` (60 or 180 seconds). The repository contains no retry, default-output, or skip-policy contract.

## Java Reproduction

- `AppService` now exposes Chatflow OpenAPI conversation create/list/message-list/chat/delete methods.
- Chatflow OpenAPI conversations and turns are persisted through the existing `assistant_conversations` and `assistant_conversation_messages` tables with `conversation_type=chatflow_openapi` and the Chatflow `appId` stored in `assistant_id`.
- `OpenApiChatflowSessionStore` remains a BFF-local fallback when AppService is unavailable or a development request targets a Chatflow id that has not been created in Java yet.
- `POST /service/api/openapi/v1/chatflow/conversation` creates a scoped conversation and returns `conversation_id`, `conversationId`, `conversation_name`, and `uuid`.
- `POST /service/api/openapi/v1/chatflow/chat` executes the saved Chatflow graph, persists a workflow run, records the user/model turn, emits the documented `text/event-stream` event sequence, and records OpenAPI-source app statistics.
- Runtime input contains the OpenAPI `parameters` plus reserved aliases `query`, `Query`, `question`, `message`, and `input`, so existing start-node schemas can consume the current user message without frontend changes.
- LLM nodes invoke `ModelService` only when the owning application is a Chatflow. Workflow LLM behavior remains unchanged.
- A configured Chatflow model is mandatory at runtime: missing RPC services, empty provider responses, and provider exceptions fail the graph instead of returning a fabricated local answer.
- LLM configuration resolves Coze-style `llmParam` values for model id, prompt, system prompt, history switch/rounds, temperature, top-p, frequency penalty, and max tokens.
- Intent nodes invoke the configured model for Chatflow and parse a structured zero-based `classificationId` and `reason`; malformed successful output falls back to the deterministic parser, while provider invocation failures fail the graph.
- Knowledge nodes call `KnowledgeService`, and their prompt/search cards can be referenced by downstream LLM nodes. Aggregated cards are returned as `search_list` and persisted with the turn.
- Configured `type=1009` MCP nodes resolve the server/tool from `mcpInfoList`, pass only declared node inputs as arguments, and call `McpService.callMcpServerTool`. Unconfigured template nodes retain an explicit `providerFallback=true` local preview.
- Configured MCP nodes treat `isError=true`, null responses, and unavailable RPC services as graph failures. The local preview is used only by nodes with no server/tool configuration.
- When `enableChatHistory=true`, the configured number of persisted prior turns is inserted before the current prompt. Provider chunks and usage metadata are retained in node output.
- Model calls record provider/model attribution, prompt/completion/total tokens, first-token latency, duration, stream mode, and success/failure in the existing model-statistic aggregate.
- `settingOnError.processType=1` is enforced as fail-fast. `timeoutMs` is propagated through Chatflow LLM, intent, MCP, and HTTP nodes; BFF-to-App and App-to-provider Dubbo calls permit the bundled-template maximum of 180 seconds.
- The five code-node programs shipped in Go templates (JSON-to-CSV, log-level statistics, text length, category/value shaping, and example multi-output) execute through bounded Java implementations. Unknown Chatflow code is rejected with an explicit isolation error instead of executing arbitrary Python/JavaScript in the App process. Workflow behavior is unchanged.
- Chat responses expose `run_id`, `chunks`, `search_list`, and `node_events`; chunks, knowledge hits, and node events are also stored with the conversation message.
- SSE output follows the repository manual: `conversation.chat.created`, `conversation.chat.in_progress`, one `conversation.message.delta` per provider chunk, `conversation.message.completed`, `conversation.chat.completed`, and `done`. Provider token usage is normalized to `input_count`, `output_count`, and `token_count`.
- Graph failures are persisted as `workflow_runs.status=failed` with the scoped input and error output. BFF returns an explicit failed OpenAPI response and statistic instead of silently substituting a local fake answer.
- Relative URLs returned by Chatflow/OpenAPI file upload are resolved by AppService through `WANWU_BFF_INTERNAL_BASE_URL` (`http://bff:8080` in Compose), allowing document-parse nodes to consume the uploaded bytes across containers.
- Empty development canvases fall back to the current user query instead of the removed hard-coded `Chatflow response:` placeholder.
- `POST /service/api/openapi/v1/chatflow/conversation/message/list` returns Go-shaped message rows under `data`, with `has_more`, `first_id`, and `last_id`.
- `POST /service/api/openapi/v1/chatflow/conversation/list` returns `conversations`, `list`, and `total`.
- `DELETE /service/api/openapi/v1/chatflow/conversation` removes the persisted scoped conversation and its messages.

## Verification

- `AppServiceImplTest#chatflowOpenApiConversationsPersistMessagesAndListState` verifies persisted create, chat, message list, conversation list, and delete behavior.
- `AppServiceImplTest#chatflowOpenApiChatExecutesConfiguredModelWithConversationHistory` verifies start-to-LLM-to-end graph execution, provider chunks, model parameters, persisted answers, and second-turn history ordering.
- The same test verifies provider/model attribution and two-turn token/statistic aggregation.
- `AppServiceImplTest#chatflowConfiguredModelFailurePersistsFailedRunAndStatistic` verifies model failures produce a failed run, no assistant message, and failed stream statistics.
- `AppServiceImplTest#chatflowOpenApiChatGroundsModelWithKnowledgeNodeResults` verifies knowledge evidence reaches the Chatflow LLM and search cards reach the API result.
- `AppServiceImplTest#chatflowOpenApiChatExecutesConfiguredMcpNode` verifies real MCP dispatch and declared-argument isolation.
- `AppServiceImplTest#chatflowConfiguredMcpErrorPersistsFailedRun` verifies provider MCP errors cannot be mistaken for successful tool output.
- `AppServiceImplTest#chatflowOpenApiChatUsesConfiguredModelForIntentNode` verifies provider-backed intent classification.
- `AppServiceImplTest#chatflowOpenApiChatPersistsFailedGraphRuns` verifies failed-run diagnostics and no false assistant message.
- `AppServiceImplTest#chatflowDocumentNodeDownloadsOpenApiUploadedFileUrl` verifies cross-service upload consumption.
- `AppServiceImplTest#chatflowRejectsUndocumentedNodeErrorProcessType` verifies unsupported, source-undocumented failure policies are rejected explicitly.
- `AppServiceImplTest#chatflowRejectsUnknownCodeOutsideSafeBuiltInPatterns` verifies arbitrary code is not executed in-process; `workflowUnknownCodeRetainsLegacyPassThrough` protects the requested Workflow non-change.
- `ModelServiceImplTest#invokeModelHonorsCommandReadTimeout` verifies a real delayed HTTP provider is interrupted by the Chatflow-provided read timeout.
- `WanwuOpenApiControllerTest#chatflowOpenApiRoutesUseAppServiceConversationState` verifies the OpenAPI route family uses AppService first while preserving the Go-shaped response contracts.
- `WanwuOpenApiControllerTest#chatflowOpenApiChatRecordsFailureStatisticWhenServiceRejectsRequest` verifies Chatflow OpenAPI service failures are not hidden by the BFF fallback and are counted as failed stream calls.
- Full `AppServiceImplTest` regression passes on Java 8.

### Docker acceptance

```powershell
docker compose --profile full up -d --build
./scripts/chatflow-smoke.ps1
```

The smoke script creates a disposable Start-to-End Chatflow through the frontend API, creates an OpenAPI conversation, verifies the six SSE event types in order, reads the persisted user/assistant messages, and removes its test data. On 2026-07-10 it passed against `localhost:8080` with all backend containers healthy.

Browser acceptance also passed at `http://localhost:3000/aibase/appSpace/workflow?type=chatflow`: development login completed without a gateway error, the Chatflow tab rendered, and the zero-change frontend opened the Create Chatflow dialog without console errors.

## Scope Boundary

This work reproduces Chatflow and its zero-change frontend contract only. It deliberately does not expand Workflow behavior.

The Java runtime executes the graph nodes and real model, knowledge, HTTP, and MCP integrations represented by the Go repository, reproduces the documented SSE/session contract, persists failure diagnostics, attributes model usage, and honors the failure/timeout policy visible in source. Retry/default/skip semantics are not implemented because neither the Go BFF nor bundled schemas define such a contract. Arbitrary user code requires a separately deployed, tenant-isolated runner; until one is configured, Chatflow rejects it rather than weakening the Java service boundary.
