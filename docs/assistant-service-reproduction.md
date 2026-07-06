# Assistant Service Reproduction

## Go Source

- Go contract: `D:\work\week3\wanwu\proto\assistant-service\assistant-service.proto`.
- The Go `AssistantService` is a large boundary covering Assistant lifecycle, snapshot versions, resource bindings, conversations, ES helpers, custom prompts, skill conversations, and WGA configuration/conversations.

## Java Slice

`wanwu-api` now exposes the core Assistant RPC surface through `AssistantService` using `Map<String, Object>` request/response values. This mirrors the current Java compatibility style and lets the service accept Go proto-shaped fields while the deeper Java DTO model continues to evolve.

`wanwu-service-assistant` now delegates core behavior to `AppService` instead of creating a second Assistant persistence model:

- ES compatibility helpers: `SaveToES`, `DeleteFromES`, and `SearchFromES` are implemented as an in-memory development shell for conversation/search callers.
- Assistant draft lifecycle: create, update, config update, delete, list, get by ids, info/detail, uuid lookup, and copy.
- Snapshot lifecycle: publish, latest, list, update latest description, rollback, info, and latest batch.
- Resource bindings: workflow, MCP, tool, skill, and multi-agent create/delete/enable/config methods map to the existing AppService resource-binding commands.
- Conversation loop: create, delete, clear, get-or-create by assistant, list, detail list, and stream map to AppService's persisted development conversation implementation. Frontend BFF stream calls now attempt the configured assistant model through the OpenAI-compatible upstream path before persistence; AppService enriches the resulting answer from configured knowledge bases through `KnowledgeService.hitKnowledge` and stores the returned `searchList` on conversation details.
- Custom prompt RPCs map to `McpService` custom prompt CRUD/list/copy.
- Skill conversation RPCs map to `McpService` skill conversation create/delete/list.
- WGA config and conversation RPCs map to `AppService` GeneralAgent config/conversation state.

The service returns Go-style wrapper keys such as `assistantInfos`, `snapshotId`, `createAt`, `data`, `conversationId`, and `content` while preserving the Java frontend-compatible details inside the payload.

## Verification

- `AssistantServiceImplTest#assistantCreateMapsIdentityAndBriefToAppService`
- `AssistantServiceImplTest#assistantConfigUpdateMapsCoreProtoFields`
- `AssistantServiceImplTest#assistantSnapshotCreatePublishesAgentAndReturnsLatestSnapshot`
- `AssistantServiceImplTest#assistantConversionStreamDelegatesDraftRequest`
- `AppServiceImplTest#assistantDraftStreamReturnsConfiguredKnowledgeHits`
- `AssistantServiceImplTest#esCompatibilityShellStoresSearchesAndDeletesJsonDocs`
- `AssistantServiceImplTest#customPromptCreateDelegatesToMcpService`
- `AssistantServiceImplTest#updateWgaConfigMapsGoListsToGeneralAgentConfig`
- `AssistantServiceImplTest#wgaConversationCreateSavesGeneralAgentState`

## Remaining Gap

This slice establishes the standalone Java RPC boundary and keeps state owned by `wanwu-service-app` or `wanwu-service-mcp`. Exact ES/index persistence, token-by-token assistant orchestration, tool/MCP execution, WGA sandbox execution, and multi-agent runtime behavior remain later slices.
