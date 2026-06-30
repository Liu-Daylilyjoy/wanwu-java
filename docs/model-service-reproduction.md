# Model Service Reproduction Notes

Date: 2026-06-30

## Source Alignment

- Frontend callers: `web/src/api/modelAccess.js`, `web/src/api/modelExprience.js`, `web/src/views/modelAccess/index.vue`, `web/src/views/modelAccess/components/createDialog.vue`, `web/src/views/modelExprience/ConversationPane.vue`, `web/src/views/modelExprience/components/ModelChatPane.vue`.
- Go BFF router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\model.go`.
- Go BFF handler/service: `internal\bff-service\server\http\handler\v1\model.go`, `internal\bff-service\server\http\handler\v1\model_experience.go`, `internal\bff-service\service\model.go`, `internal\bff-service\service\model_experience.go`.
- Go ORM baseline: `internal\model-service\client\orm\model_experience.go`.
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
  - `/model/experience/dialog`, `/model/experience/dialogs`, `/model/experience/dialog/records`.
  - `/model/experience/llm` as a local OpenAI-compatible SSE echo path that saves user and assistant records.
- `wanwu-service-bff` maps the original frontend paths under `/user/api/v1`.
- Docker Compose `full` profile includes `model` on ports `8082` and `20882`.

## Current Storage Boundary

The Java model service uses an in-memory repository seeded with Docker development models:

- DeepSeek LLM: `deepseek-chat`
- OpenAI-compatible embedding: `text-embedding-3-small`
- Jina rerank: `jina-reranker-v2-base-multilingual`

The same in-memory boundary now stores model experience dialogs by `sessionId` and records by `modelExperienceId`, mirroring the Go service's create-or-update and cascade-delete behavior. This is enough for real frontend navigation, model select dependencies, model-management CRUD smoke tests, and the model experience history loop. It is not the final Go-equivalent database model. The later persistence slice should move this repository behind MySQL tables matching the original model service behavior.

## Still Missing

- Real external LLM provider streaming and token statistics for model experience; current SSE path is deterministic local echo for frontend compatibility.
- LLM inference validation parity beyond the current `validate-thinking` success stub.
- Callback/OpenAPI model inference endpoints.
