# RAG Service Reproduction

## Go Source

- Go contract: `D:\work\week3\wanwu\proto\rag-service\rag-service.proto`.
- The Go `RagService` exposes 15 RPCs for chat, draft lifecycle, config, listing, copy, publish/version history, draft overwrite, and latest publish descriptions.

## Java Slice

`wanwu-api` now exposes the same service boundary through `RagService`. The methods intentionally accept and return `Map<String, Object>` because the reproduced Java app service already stores frontend-compatible RAG config maps and the Go proto contains nested app/knowledge/model config objects that are still evolving in the Java reproduction.

`wanwu-service-rag` now implements the RPC surface as a thin facade over `AppService`:

- `CreateRag`, `UpdateRag`, `DeleteRag`, `CopyRag`, `GetRagDetail`, `ListRag`, and `GetRagByIds` delegate to the existing RAG draft lifecycle.
- `UpdateRagConfig` accepts both Java lower-camel fields and Go proto fields such as `QArerankConfig`, `QAknowledgeBaseConfig`, and `sensitiveConfig`; AppService RAG chat now also consumes Go proto `perKnowledgeConfigs/globalConfig` knowledge and QA config shapes during local recall and forwards separate knowledge/QA rerank model IDs when weighted-score mode is not selected.
- `PublishRag` maps to `publishApp(appType=rag)`.
- `UpdatePublishRag` maps to `updateAppVersion(appType=rag)`.
- `ListPublishRagHistory`, `GetPublishRagDesc`, and `GetPublishRagDescBatch` map to app version queries and return Go-style `historyList`, `version`, `desc`, and `createAt` fields.
- `OverwriteRagDraft` maps to `rollbackAppVersion(appType=rag)`.
- `ChatRag` maps to `streamRagChat`, preserving `publish=1` as published mode, forwarding `history` plus `fileInfoList`, converting image file URLs into Go-style `attachmentList` rows, and returning Go-compatible normalized `searchList` entries with `score`, `kb_name`, and `user_kb_name`.

This avoids creating a second RAG persistence model. Drafts, configs, snapshots, publish status, and chat records remain owned by `wanwu-service-app`.

The frontend and public OpenAPI RAG chat paths also mirror the Go runtime's use of the configured model ID far enough for real development loops: BFF reads the draft/published RAG `modelConfig.modelId`, calls the Java OpenAI-compatible upstream path with `stream:true`, aggregates the provider deltas, and passes that answer into AppService before knowledge/QA enrichment, safety output replacement, and chat-record persistence. That upstream request now preserves `needHistory=true` RAG history as ordered OpenAI-compatible `user/assistant` messages before the current question. The enrichment step accepts both frontend `knowledgebases/config` and Go proto `perKnowledgeConfigs/globalConfig` shapes, preserving `graphSwitch`, metadata filters, weighted-score vs rerank-model mode, the configured `rerankConfig.modelId` / `qaRerankConfig.modelId`, and Go-style image attachments (`.png`, `.jpg`, `.jpeg`) for the Java knowledge hit request context. Frontend RAG SSE now emits Go-style QA and knowledge start/search-list AG-UI custom events before the text content. If the model config is absent, inactive, or unreachable, AppService keeps the deterministic local fallback so development Docker remains usable offline.

## Verification

- `RagServiceImplTest#createRagMapsIdentityAndBriefToAppService`
- `RagServiceImplTest#updateRagConfigAcceptsGoProtoFieldNames`
- `RagServiceImplTest#chatRagDelegatesPublishedRequestAndReturnsSearchLists`
- `RagServiceImplTest#listPublishRagHistoryReturnsGoStyleHistoryItems`
- `WanwuFrontendApiControllerTest#ragDraftChatUsesConfiguredOpenAiCompatibleModelBeforePersisting`
- `WanwuFrontendApiControllerTest#ragChatDraftReturnsAgUiSseAndMapsFrontendRequest`
- `WanwuOpenApiControllerTest#ragOpenApiChatUsesConfiguredOpenAiCompatibleModelBeforePersisting`
- `AppServiceImplTest#ragChatPersistsConfiguredModelUpstreamResponse`
- `AppServiceImplTest#ragChatAcceptsGoProtoKnowledgeAndQaConfigShapes`
- `AppServiceImplTest#ragChatPassesConfiguredRerankModelsToKnowledgeHits`
- `AppServiceImplTest#ragChatBuildsImageAttachmentListFromFileInfo`

## Remaining Gap

This slice reproduces the Go RPC boundary, state transitions, frontend/OpenAPI configured-model answer loop, rerank model ID propagation, and image attachment request shaping into local hit requests, not the full Go runtime. True retrieval/rerank execution orchestration, provider token-by-token RAG streaming, exact stream chunk sequencing, file parser/indexer callbacks, object-storage lifecycle, non-image attachment parsing, and provider-specific adapter behavior remain later RAG runtime work.
