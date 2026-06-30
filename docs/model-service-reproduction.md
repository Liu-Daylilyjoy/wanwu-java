# Model Service Reproduction Notes

Date: 2026-06-30

## Source Alignment

- Frontend callers: `web/src/api/modelAccess.js`, `web/src/views/modelAccess/index.vue`, `web/src/views/modelAccess/components/createDialog.vue`.
- Go BFF router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\model.go`.
- Go BFF handler/service: `internal\bff-service\server\http\handler\v1\model.go`, `internal\bff-service\service\model.go`.
- Go RPC baseline: `D:\work\week3\wanwu\proto\model-service\model-service.proto`.

## Java Coverage

- `wanwu-api` now exposes typed model DTOs under `com.unicomai.wanwu.api.model.dto`.
- `wanwu-service-model` implements:
  - `POST /model` equivalent through `ModelService#importModel`.
  - `PUT /model` equivalent through `updateModel`.
  - `DELETE /model` equivalent through `deleteModel`.
  - `PUT /model/status` equivalent through `changeModelStatus`.
  - `GET /model`, `/model/list`, `/model/select/{type}`.
  - `/model/import/providers` and `/model/recommend`.
- `wanwu-service-bff` maps the original frontend paths under `/user/api/v1`.
- Docker Compose `full` profile includes `model` on ports `8082` and `20882`.

## Current Storage Boundary

The Java model service uses an in-memory repository seeded with Docker development models:

- DeepSeek LLM: `deepseek-chat`
- OpenAI-compatible embedding: `text-embedding-3-small`
- Jina rerank: `jina-reranker-v2-base-multilingual`

This is enough for real frontend navigation, model select dependencies, and model-management CRUD smoke tests. It is not the final Go-equivalent database model. The later persistence slice should move this repository behind MySQL tables matching the original model service behavior.

## Still Missing

- Model experience dialog persistence and record APIs.
- LLM streaming/inference validation parity beyond the current `validate-thinking` success stub.
- Callback/OpenAPI model inference endpoints.
