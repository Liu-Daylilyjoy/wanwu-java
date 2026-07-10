# RAG Runtime Architecture

Date: 2026-07-10

## Source Baseline

The Go `rag-service` is an orchestration service. It resolves the draft or published RAG configuration, builds knowledge and QA retrieval requests, and streams the ordered result of QA retrieval, knowledge retrieval, and model generation. Retrieval and model execution are delegated to runtime providers.

## Java Runtime Flow

The Java runtime keeps the same ownership boundaries:

1. `AppServiceImpl` resolves the draft or published RAG snapshot.
2. Input safety rules run before retrieval or provider execution.
3. `KnowledgeService` executes document and QA retrieval with the configured filters and ranking settings.
4. Retrieved document, QA, and graph cards are converted into numbered, bounded system references. Graph nodes, descriptions, and relationships are supplied to the model instead of the frontend-only graph placeholder.
5. Enabled history items are trimmed to the latest configured `maxHistory` turns; zero disables history, matching the Python RAG runtime. Image attachments are appended before the current user question.
6. `ModelService.invokeModel` resolves the active model and calls its OpenAI-compatible provider.
7. Provider SSE deltas are aggregated for persistence and retained as individual response chunks for frontend AG-UI SSE output.
8. Output safety rules run before the answer and evidence are persisted.

Model credentials remain in model-service configuration. RAG and BFF code pass only a model ID and inference payload through the internal Dubbo contract.

Document segments and QA pairs persist their embedding and embedding model ID in the knowledge snapshot. Text, vector, and weighted hybrid retrieval share one ranking path. Provider rerank updates both the final score and the evidence card; provider failures fall back to the recall score without failing the chat.

Knowledge imports parse text/Markdown/CSV, PDF, DOC/DOCX, XLS/XLSX, PPT/PPTX, HTML, ZIP, and TAR.GZ content before splitting and indexing. Archive parsing is bounded and never extracts files to disk. The RAG chat attachment contract intentionally remains image-only because that is the behavior of the Go runtime and unchanged frontend.

## Model Inference Contract

`ModelInvokeCommand` supports these operations:

- `chat` -> `/chat/completions`
- `embeddings` -> `/embeddings`
- `rerank` -> `/rerank`
- `multimodal-embeddings` -> `/multimodal-embeddings`
- `multimodal-rerank` -> `/multimodal-rerank`

The model service validates model state and model type before sending a request. Development placeholder keys are never sent to an upstream provider. Provider errors are returned as invocation failures without logging credentials.

## Offline Behavior

Docker development remains usable without provider credentials. If no active configured model can be invoked, RAG returns the strongest retrieved evidence directly. If retrieval is empty, it returns an explicit no-relevant-knowledge response. The old `Demo RAG response` path is removed.

## Verification

- `ModelServiceImplTest#invokeModelAggregatesOpenAiCompatibleChatStream`
- `ModelServiceImplTest#invokeModelReturnsEmbeddingAndRerankProviderResponses`
- `AppServiceImplTest#ragChatRetrievesEvidenceBeforeInvokingConfiguredModel`
- `AppServiceImplTest#ragChatAddsGraphEvidenceAndKeepsOnlyLatestConfiguredHistory`
- `AppServiceImplTest#ragChatOmitsHistoryWhenMaxHistoryIsZero`
- `KnowledgeServiceImplTest#knowledgeHitUsesConfiguredEmbeddingModelForVectorRetrieval`
- `KnowledgeServiceImplTest#knowledgeHitUsesConfiguredProviderRerankOrderAndScores`
- `KnowledgeServiceImplTest#documentImportExtractsPresentationLegacySpreadsheetAndZipArchive`
- `WanwuFrontendApiControllerTest#knowledgeDocImportPreservesPresentationAndArchiveBytes`
- Full `ModelServiceImplTest`
- Full `AppServiceImplTest`
- Frontend and OpenAPI RAG delegation/SSE tests
