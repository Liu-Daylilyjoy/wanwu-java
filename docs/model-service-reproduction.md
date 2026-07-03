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
  - `/appspace/workflow/model/select/{type}` with the same user/org scoped model-type query mapping used by the Go workflow selectors, including `/select/asr` -> `sync-asr`.
  - `/model/import/providers` and `/model/recommend`, including model-type-correct recommendation tags for LLM, embedding, rerank, sync-ASR, OCR, PDF Parser, and GUI.
  - `/model/experience/dialog`, `/model/experience/dialogs`, `/model/experience/dialog/records`.
  - `/model/experience/llm` as a local OpenAI-compatible SSE echo path that saves user and assistant records.
- `wanwu-service-bff` maps the original frontend paths under `/user/api/v1`.
- `wanwu-service-bff` also maps `/callback/v1/model/{modelId}` to `ModelService#getModel`, returning Go-style callback model metadata with API keys redacted to `useless-api-key` and `config.endpointUrl` rewritten to `/callback/v1/model/{modelId}`.
- Callback OCR, PDF Parser, GUI, and sync-ASR routes now return local Go-compatible success body shapes (`OcrResp`, `PdfParserResp`, `GuiResp`, `SyncAsrResp`) so downstream services can parse the expected top-level fields while real provider calls are still deferred.
- Docker Compose `full` profile includes `model` on ports `8082` and `20882`.

## Current Storage Boundary

The Java model service uses an in-memory repository seeded with Docker development models:

- DeepSeek LLM: `deepseek-chat`
- OpenAI-compatible embedding: `text-embedding-3-small`
- Jina rerank: `jina-reranker-v2-base-multilingual`
- Qwen ASR: `qwen3-asr-flash`
- YuanJing OCR: `unicom-ocr`
- YuanJing PDF Parser: `pdf-parser`
- YuanJing GUI Agent: `gui_agent_v1`

The same boundary now stores model experience dialogs by `sessionId` and records by `modelExperienceId`, mirroring the Go service's create-or-update and cascade-delete behavior. The BFF local model-experience SSE path checks global Safety Guard tables before echoing prompts and checks the generated local answer before saving and returning it, matching the frontend-visible Go input/output blocking rules while provider streaming is still local. The mutable model state is now durable through `model_service.model_records`, so Docker restarts preserve imported models, status changes, delete tombstones, model-experience dialogs, and records. It is still not the final Go-equivalent database model; later slices should normalize this compatibility table into imported-model and model-experience relational tables matching the original model service behavior.

## Still Missing

- Real external LLM provider streaming and token statistics for model experience; current SSE path is deterministic local echo for frontend compatibility.
- LLM inference validation parity beyond the current `validate-thinking` success stub.
- Real callback/OpenAPI model inference proxying beyond the current development callback model-info redaction contract and local OCR/PDF/GUI/ASR response bodies.
