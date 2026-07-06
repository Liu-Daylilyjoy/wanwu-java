# Callback RAG Reproduction

Date: 2026-07-06

## Go Source Baseline

Inspected original Go/Python files:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\callback\router.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\wga_knowledge.go`
- `D:\work\week3\wanwu\internal\bff-service\model\request\knowledge.go`
- `D:\work\week3\wanwu\internal\bff-service\service\rag_knowledge_service.go`
- `D:\work\week3\wanwu\rag\rag_open_source\rag_core\run.py`

Go callback routes:

- `POST /callback/v1/rag/search-knowledge-base`
- `POST /callback/v1/rag/search-QA-base`
- `POST /callback/v1/wga/rag/search-knowledge-base`
- `POST /callback/v1/rag/knowledge/stream/search`

The Go BFF receives callback requests with RAG-native fields such as `knowledgeIdList`,
`knowledge_base_info`, `QABaseInfo`, `topK`, `threshold`, `use_graph`, `retrieve_method`,
`rerank_mod`, `rerank_model_id`, `weights`, and metadata filters. It then calls local or
external RAG services and returns `prompt`, `searchList`, `score`, and for knowledge hits
`use_graph`.

The WGA callback narrows the request to `knowledgeIdList` and `question`, then applies Go
defaults: `topK=5`, `threshold=0.4`, `retrieve_method=hybrid_search`,
`rerank_mod=weighted_score`, `vector_weight=0.2`, and `text_weight=0.8`.

## Java Reproduction

Implemented in:

- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiController.java`
- `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiControllerTest.java`

The callback layer now adapts Go request fields into the Java `KnowledgeService` contract:

- `knowledgeIdList`, `knowledge_base_info[].kb_id`, and `QABaseInfo[].QAId` become
  `knowledgeList[].knowledgeId`.
- `topK`, `threshold`, `score`, `use_graph`, rerank, retrieve, weight, and metadata filter fields
  become `knowledgeMatchParams`.
- `X-uid` is honored for WGA user identity, matching the Go handler.
- Knowledge search calls `KnowledgeService.hitKnowledge`.
- QA search calls `KnowledgeService.hitQaPairs`.

The response keeps Java camelCase fields and adds Go callback aliases:

- `useGraph` plus `use_graph`
- `knowledgeName` plus `kb_name`
- `userKbName` plus `user_kb_name`
- `metaDataList` plus `meta_data`
- `childContentList` plus `child_content_list`
- `childSnippet` plus `child_snippet`
- `childScore` plus `child_score`
- `contentType` plus `content_type`
- `rerankInfo` plus `rerank_info`
- `fileUrl` plus `file_url`

## Current Boundary

This slice makes the callback RAG search routes usable by the frontend and by internal callback
callers without changing frontend code. It does not reproduce the full Python RAG/vector/rerank
runtime yet. Search quality is bounded by the current Java local `KnowledgeService` document and
QA hit implementations.

`/callback/v1/rag/knowledge/stream/search` remains a deterministic development SSE shell and is
not yet connected to provider generation.

## Verification

Executed with Docker Maven/JDK8:

```powershell
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -Dtest=WanwuCallbackApiControllerTest#ragCallbacksAdaptGoRequestsToKnowledgeServiceAndReturnGoAliases -DfailIfNoTests=false test
```
