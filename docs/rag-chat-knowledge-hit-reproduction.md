# RAG Chat Knowledge Hit Reproduction

Date: 2026-07-01

## Source Evidence

- Go BFF accepts RAG chat through `internal/bff-service/server/http/handler/router/v1/rag.go`.
- Go app request config models define `knowledgeBaseConfig` and `qaKnowledgeBaseConfig` in `internal/bff-service/model/request/appspace_common.go`.
- Each RAG knowledge config carries a `knowledgebases` array and a `config` object. The frontend writes the same shape from `web/src/views/rag/index.vue`.
- The frontend consumes AG-UI custom events named `rag_search_list` and `rag_qa_search_list` during RAG chat.

## Java Reproduction

- `AppServiceImpl.streamRagChat` now reads the current draft config for draft chat and the published snapshot config for published chat.
- Configured `knowledgebases[].id` values are translated to the Java knowledge-service hit contract as `knowledgeList[].knowledgeId`.
- The existing `knowledgeBaseConfig.config` object is passed through as `knowledgeMatchParams`, preserving frontend settings such as `topK`, `threshold`, `matchType`, and graph flags.
- When a knowledge base is configured, Java calls `KnowledgeService.hitKnowledge`; when a QA knowledge base is configured, Java calls `KnowledgeService.hitQaPairs`.
- The returned `searchList` and `qaSearchList` are placed on `RagChatResult`, and the BFF emits them as AG-UI custom SSE events before the text response.
- The deterministic development response is enriched with the returned `prompt` or first hit text so manual smoke tests can see that the configured knowledge path was used.

## Tests

- `AppServiceImplTest#ragChatReturnsConfiguredKnowledgeHits` verifies config parsing, `id -> knowledgeId` translation, `topK` propagation, response enrichment, and search-list return.
- `WanwuFrontendApiControllerTest#ragChatDraftReturnsAgUiSseAndMapsFrontendRequest` verifies that frontend RAG draft chat emits `rag_search_list` over text/event-stream.

## Remaining Gap

This slice connects the Java RAG chat path to the local Java knowledge hit API. It does not reproduce the full Go RAG runtime yet. The remaining work includes real file parsing/indexing, vector/keyword/rerank retrieval parity, model generation, conversation memory, graph RAG behavior, callback status mutation, object storage lifecycle, and streaming token generation from a provider model.
