# RAG Chat Knowledge Hit Reproduction

Date: 2026-07-01

## Source Evidence

- Go BFF accepts RAG chat through `internal/bff-service/server/http/handler/router/v1/rag.go`.
- Go app request config models define `knowledgeBaseConfig` and `qaKnowledgeBaseConfig` in `internal/bff-service/model/request/appspace_common.go`.
- Each RAG knowledge config can carry the frontend `knowledgebases` array plus `config` object, or the Go proto `perKnowledgeConfigs` plus `globalConfig` shape from `proto/rag-service/rag-service.proto`; Go uses that same nested field name for both Knowledge and QA configs.
- Go RAG runtime builds knowledge and QA hit params in `internal/rag-service/service/rag-manage-service/rag_manage_sevice.go` and `rag_qa_hit_sevice.go`; `priorityMatch == 1` uses weighted scoring, while other values pass `rerankConfig.modelId` or `qaRerankConfig.modelId` as `rerankModelId`.
- The Go BFF stream converter emits AG-UI custom events named `rag_qa_start`, `rag_qa_search_list`, `rag_knowledge_start`, and `rag_search_list` during RAG chat.

## Java Reproduction

- `AppServiceImpl.streamRagChat` now reads the current draft config for draft chat and the published snapshot config for published chat.
- Configured `knowledgebases[].id`, `knowledgeList[].knowledgeId`, and Go `perKnowledgeConfigs[].knowledgeId` values are translated to the Java knowledge-service hit contract as `knowledgeList[].knowledgeId`.
- The existing `knowledgeBaseConfig.config` object or Go `globalConfig` object is passed through as `knowledgeMatchParams`, preserving frontend and proto settings such as `topK`, `threshold`, `matchType`, graph flags, and hybrid retrieval weights.
- RAG chat now applies the Go rerank switch when building local KnowledgeService requests: `rerankConfig.modelId` feeds knowledge hits and `qaRerankConfig.modelId` feeds QA hits when `priorityMatch != 1`, with `rerankMod=rerank_model`; weighted-score mode keeps `rerankMod=weighted_score`.
- Go proto per-knowledge rows are normalized by preserving `graphSwitch` and mapping `ragMetaFilter` to `metaDataFilterParams`.
- When a knowledge base is configured, Java calls `KnowledgeService.hitKnowledge`; when a QA knowledge base is configured, Java calls `KnowledgeService.hitQaPairs`.
- The returned `searchList` and `qaSearchList` are normalized with Go BFF-compatible `score`, `kb_name`, and `user_kb_name` fields, then placed on `RagChatResult`; the BFF emits `rag_qa_start -> rag_qa_search_list` and `rag_knowledge_start -> rag_search_list` AG-UI custom SSE events before the text response when local hits exist.
- The deterministic development response is enriched with the returned `prompt` or first hit text so manual smoke tests can see that the configured knowledge path was used.

## Tests

- `AppServiceImplTest#ragChatReturnsConfiguredKnowledgeHits` verifies config parsing, `id -> knowledgeId` translation, `topK` propagation, response enrichment, search-list return, top-level score propagation, and `rerank_info[0].score` fallback.
- `AppServiceImplTest#ragChatAcceptsGoProtoKnowledgeAndQaConfigShapes` verifies Go proto `perKnowledgeConfigs/globalConfig` and QA config shapes both trigger the Java knowledge hit loop.
- `AppServiceImplTest#ragChatPassesConfiguredRerankModelsToKnowledgeHits` verifies separate knowledge and QA rerank model IDs are propagated into the local hit requests.
- `WanwuFrontendApiControllerTest#ragChatDraftReturnsAgUiSseAndMapsFrontendRequest` verifies that frontend RAG draft chat emits the Go-style QA and knowledge start/search-list AG-UI event order over text/event-stream.

## Remaining Gap

This slice connects the Java RAG chat path to the local Java knowledge hit API and forwards the configured rerank model IDs into that local request contract. It does not reproduce the full Go RAG runtime yet. The remaining work includes real file parsing/indexing, vector/keyword/rerank retrieval execution parity, model generation, conversation memory, graph RAG behavior, callback-producing parser/indexer workers, object storage lifecycle, and streaming token generation from a provider model.
