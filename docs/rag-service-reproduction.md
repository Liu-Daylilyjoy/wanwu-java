# RAG Service Reproduction

## Go Source

- Go contract: `D:\work\week3\wanwu\proto\rag-service\rag-service.proto`.
- The Go `RagService` exposes 15 RPCs for chat, draft lifecycle, config, listing, copy, publish/version history, draft overwrite, and latest publish descriptions.

## Java Slice

`wanwu-api` now exposes the same service boundary through `RagService`. The methods intentionally accept and return `Map<String, Object>` because the reproduced Java app service already stores frontend-compatible RAG config maps and the Go proto contains nested app/knowledge/model config objects that are still evolving in the Java reproduction.

`wanwu-service-rag` now implements the RPC surface as a thin facade over `AppService`:

- `CreateRag`, `UpdateRag`, `DeleteRag`, `CopyRag`, `GetRagDetail`, `ListRag`, and `GetRagByIds` delegate to the existing RAG draft lifecycle.
- `UpdateRagConfig` accepts both Java lower-camel fields and Go proto fields such as `QArerankConfig`, `QAknowledgeBaseConfig`, and `sensitiveConfig`.
- `PublishRag` maps to `publishApp(appType=rag)`.
- `UpdatePublishRag` maps to `updateAppVersion(appType=rag)`.
- `ListPublishRagHistory`, `GetPublishRagDesc`, and `GetPublishRagDescBatch` map to app version queries and return Go-style `historyList`, `version`, `desc`, and `createAt` fields.
- `OverwriteRagDraft` maps to `rollbackAppVersion(appType=rag)`.
- `ChatRag` maps to `streamRagChat`, preserving `publish=1` as published mode and forwarding `history` plus `fileInfoList`.

This avoids creating a second RAG persistence model. Drafts, configs, snapshots, publish status, and chat records remain owned by `wanwu-service-app`.

## Verification

- `RagServiceImplTest#createRagMapsIdentityAndBriefToAppService`
- `RagServiceImplTest#updateRagConfigAcceptsGoProtoFieldNames`
- `RagServiceImplTest#chatRagDelegatesPublishedRequestAndReturnsSearchLists`
- `RagServiceImplTest#listPublishRagHistoryReturnsGoStyleHistoryItems`

## Remaining Gap

This slice reproduces the Go RPC boundary and state transitions, not the full Go runtime. True retrieval/rerank/model orchestration, provider token streaming, exact stream chunk sequencing, file parser/indexer callbacks, and object-storage lifecycle remain later RAG runtime work.
