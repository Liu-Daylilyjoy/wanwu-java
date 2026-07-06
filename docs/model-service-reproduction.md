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
  - `/model/import/providers` and `/model/recommend`, including model-type-correct recommendation tags for LLM, embedding, rerank, multimodal embedding/rerank, sync-ASR, OCR, PDF Parser, and GUI.
  - `/model/experience/dialog`, `/model/experience/dialogs`, `/model/experience/dialog/records`.
  - `/model/experience/llm` as a Go-style frontend SSE path that saves user and assistant records, tries configured OpenAI-compatible `stream:true` upstreams first, and falls back to local echo when no usable provider config exists.
- `wanwu-service-bff` maps the original frontend paths under `/user/api/v1`.
- `wanwu-service-bff` also maps `/callback/v1/model/{modelId}` to `ModelService#getModel`, returning Go-style callback model metadata with API keys redacted to `useless-api-key` and `config.endpointUrl` rewritten to the callback model proxy URL. The default Docker URL is `http://bff:8080/callback/v1/model/{modelId}` and can be overridden with `WANWU_CALLBACK_MODEL_BASE_URL`, the Go-compatible `WANWU_CALLBACK_LLM_BASE_URL`, or the JVM property `wanwu.callback.model-base-url`.
- `/callback/v1/model/{modelId}/chat/completions`, `/embeddings`, and `/rerank` now mirror the Go callback path at a minimum usable level: when `ModelService#getModel` returns a configured model with `endpointUrl` or `inferUrl` plus a non-development `apiKey`, the BFF rejects inactive models and Go-style `model` mismatches, forwards OpenAI-compatible chat JSON requests, converts chat user `image_url.url` values plus multimodal embeddings/rerank file URL fields to data URLs before proxying, records chat stream/non-stream plus embeddings/rerank `usage` into model statistics, passes through chat SSE when `stream:true`, and forwards embeddings/rerank JSON requests to the matching upstream endpoint. Missing model service, missing credentials, development redacted keys, and upstream failures still fall back to the deterministic local response so Docker development remains self-contained.
- Callback OCR, PDF Parser, GUI, and sync-ASR routes now return local Go-compatible success body shapes (`OcrResp`, `PdfParserResp`, `GuiResp`, `SyncAsrResp`) so downstream services can parse the expected top-level fields while real provider calls are still deferred.
- `/asr/stream` under `/user/api/v1` now has a Docker development `text/event-stream` route that emits `asr.connected` and `asr.closed` events for frontend route compatibility.
- Docker Compose `full` profile includes `model` on ports `8082` and `20882`.

## Current Storage Boundary

The Java model service uses an in-memory repository seeded with Docker development models:

- DeepSeek LLM: `deepseek-chat`
- OpenAI-compatible embedding: `text-embedding-3-small`
- Jina rerank: `jina-reranker-v2-base-multilingual`
- Qwen ASR: `qwen3-asr-flash`
- Qwen multimodal embedding: `qwen-vl-multimodal-embedding`
- Qwen multimodal rerank: `qwen-vl-multimodal-rerank`
- YuanJing OCR: `unicom-ocr`
- YuanJing PDF Parser: `pdf-parser`
- YuanJing GUI Agent: `gui_agent_v1`

The same boundary now stores model experience dialogs by `sessionId` and records by `modelExperienceId`, mirroring the Go service's create-or-update and cascade-delete behavior. The BFF model-experience SSE path checks global Safety Guard tables before generation and checks the generated answer before saving and returning it. When a model has `endpointUrl` or `inferUrl` plus a non-development `apiKey`, it builds OpenAI-compatible messages from saved dialog records plus the current user prompt, maps enabled Go-style inference parameters (`temperature`, `topP`, penalties, `maxTokens`, and `thinkingEnable`) into the upstream payload, first calls `/chat/completions` with `stream:true` and passes through `text/event-stream` chunks while aggregating answer/reasoning/usage for persistence and statistics. If the provider does not return an event stream, Java falls back to the existing non-stream OpenAI-compatible request and wraps the returned answer as frontend SSE; otherwise it keeps the deterministic local echo fallback. The mutable model state is now durable through `model_service.model_records`, so Docker restarts preserve imported models, status changes, delete tombstones, model-experience dialogs, and records. It is still not the final Go-equivalent database model; later slices should normalize this compatibility table into imported-model and model-experience relational tables matching the original model service behavior.

## Still Missing

- Provider-specific model adapters, token-level sensitive-word interception during live streaming, and provider-specific cost calculation; the current provider-backed model experience path supports OpenAI-compatible `stream:true` plus non-stream fallback.
- Real XunFei ASR WebSocket proxying behind `/asr/stream`; the current route is a deterministic development event stream only.
- LLM inference validation parity beyond the current `validate-thinking` success stub.
- Callback inference parity beyond OpenAI-compatible chat stream/non-stream with image URL data conversion plus embeddings/rerank JSON proxying, multimodal file field conversion, and usage statistics: provider-specific adapters and callback auth/signature checks remain missing.
