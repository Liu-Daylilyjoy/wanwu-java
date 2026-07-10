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

The Docker `full` profile starts the standalone RAG Dubbo service on HTTP port `8089` and Dubbo port `20889`. BFF startup waits for its health check, so one-command startup validates the same service topology used for continued development instead of leaving the RAG module outside Compose.

The Java IAM debug port defaults to host `18081` (`WANWU_IAM_HTTP_PORT`) while remaining `8081` inside Compose. This lets the Java stack run beside the original Go stack, whose Nginx commonly occupies host port `8081`.

`Dockerfile.service` shares a BuildKit Maven repository cache across service image builds and uses the repository mirror in `configs/maven/settings.xml`. This prevents every service image from downloading the same dependency graph and adds a stable Docker-only path for networks where Maven Central is slow; host Maven configuration is unchanged.

The frontend, public OpenAPI, and standalone RAG service now share the AppService-owned runtime sequence: input safety, document/QA retrieval, grounded prompt construction, configured model invocation, output safety, and chat persistence. `ModelService.invokeModel` owns OpenAI-compatible provider execution and preserves upstream SSE deltas as response chunks; the frontend emits one AG-UI `TEXT_MESSAGE_CONTENT` event per chunk. Enabled RAG history is sent as ordered `user/assistant` messages, image uploads ending in `.png`, `.jpg`, or `.jpeg` are sent as OpenAI-compatible `image_url` parts, and retrieved knowledge/QA evidence is present in the system message before the current question. If the model is absent, inactive, or unreachable, AppService returns the strongest local evidence or an explicit no-relevant-knowledge response; the old demo response has been removed.

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
- `KnowledgeServiceImplTest#knowledgeHitAppliesGoStyleMetadataFilters`
- `KnowledgeServiceImplTest#qaHitAppliesGoStyleMetadataFilters`
- `KnowledgeServiceImplTest#knowledgeHitReturnsLocalRerankInfoWhenRerankModeIsRequested`
- `KnowledgeServiceImplTest#qaHitReturnsLocalRerankInfoWhenRerankModeIsRequested`
- `KnowledgeServiceImplTest#knowledgeHitAddsGraphCardWhenUseGraphIsEnabled`
- `KnowledgeServiceImplTest#knowledgeHitUsesRequestGraphSwitchForGraphCard`
- `KnowledgeServiceImplTest#knowledgeHitUsesTokenOverlapForNaturalQuestions`
- `KnowledgeServiceImplTest#qaHitUsesTokenOverlapForNaturalQuestions`
- `docker compose --profile full config --quiet`
- `powershell -ExecutionPolicy Bypass -File scripts/rag-smoke.ps1`

## Remaining Gap

The unchanged frontend now has a complete functional RAG loop: lifecycle/config/publish, document and QA import, multi-format parsing, persisted indexing, text/vector/hybrid retrieval, provider rerank, graph evidence, grounded model generation, safety checks, AG-UI/OpenAPI SSE output, MySQL persistence, and one-command Docker startup are all exercised end to end.

Remaining production-parity work is infrastructure rather than a broken frontend flow: asynchronous parser/indexer workers, object-storage lifecycle, OCR/ASR and multimodal document parsers, transport-level live token forwarding across Dubbo, provider-generated graph extraction/community reasoning, and non-OpenAI-specific adapters. Non-image chat attachments are intentionally not listed as a gap because the checked Go RAG runtime also accepts only image chat attachments; other files enter through Knowledge import.
