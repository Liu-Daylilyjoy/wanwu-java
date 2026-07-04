# Model Use API Compatibility

Date: 2026-07-01

## Source Baseline

The frontend defines `MODEL_API = /use/model/api/v1` in `web/src/utils/requestConstants.js`.
The Go deployment proxies `/use/model/api/` through Nginx to `bff-service:6668`, and the frontend calls this family from:

- `web/src/api/chat.js` for legacy assistant management, assistant knowledge files, conversation history, ChatLLM/CUBM, and file confirmation.
- `web/src/api/cubm.js` for ChatLLM/CUBM conversations and assistant action helpers.
- `web/src/api/modelExprience.js` for model experience dialog history, detail records, and file extraction.

These routes are frontend-visible even though most of the current Java reproduction already uses the newer `/user/api/v1` route family.

## Java Reproduction

`WanwuModelUseApiController` adds a compatibility layer for the legacy proxy family:

- `web/nginx.conf` now forwards `/use/model/api/` to the BFF container, matching the original Go deployment proxy shape.
- Assistant create/update/delete/publish/list/info routes delegate to the existing Java `AppService` development assistant lifecycle where possible.
- Assistant conversation create/list/detail/delete delegates to the existing Java assistant conversation shell.
- Model experience dialog create/list/delete/detail aliases delegate to `ModelService`, so `/use/model/api/v1/model/experience/*` and `/user/api/v1/model/experience/*` share the same development repository.
- ChatLLM/CUBM conversation create/list/detail/delete now delegates to `AppService`, using the existing `assistant_conversations` and `assistant_conversation_messages` MySQL-backed repository with an internal `model_use_chatllm` conversation type.
- Assistant knowledge-file upload/list/delete now delegates to `AppService` and persists file binding metadata in the MySQL-backed `assistant_knowledge_files` table while preserving the legacy frontend response fields (`fileId`, `id`, `fileName`, `file_name`, `name`, `size`, `status`, `url`).
- Assistant action routes still return deterministic local development state.
- Legacy file routes (`/use/model/api/v1/file/batch/upload`, `/use/model/api/v1/file/confirmPath`, and `/service/api/v1/model/expansion/file/batch/upload`) return frontend-compatible upload/confirmation contracts.

## Current Boundary

- This is a route and data-shape compatibility slice, not a reproduced model-use inference service.
- ChatLLM/CUBM replies, assistant action execution, auto-create generation, and file extraction are deterministic shells.
- Assistant, model experience, ChatLLM/CUBM conversations, and assistant knowledge-file metadata persistence reuse Java development repositories; actual uploaded file content/object-storage lifecycle for legacy model-use knowledge files remains a later MinIO/runtime parity slice.

## Verification

- `AppServiceImplTest#legacyChatLlmConversationUsesPersistentConversationRepository` covers the persisted ChatLLM/CUBM service loop.
- `AppServiceImplTest#legacyAssistantKnowledgeFilesUsePersistentRepository` covers the persisted assistant knowledge-file metadata loop.
- `WanwuModelUseApiControllerTest` covers legacy assistant lifecycle, assistant conversations, assistant knowledge-file route delegation, ChatLLM/CUBM route mapping, model experience aliases, file extraction, file confirmation, and batch upload aliases.
