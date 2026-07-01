# App and Assistant Service Reproduction

Date: 2026-07-01

## Covered Java Behavior

- Assistant draft lifecycle: create, update base info, update config, delete, copy.
- App publish lifecycle: publish, unpublish, latest version, version list, update release note/scope, rollback.
- Conversations: draft/published conversation create/list/detail/delete/clear and deterministic local SSE responses.
- OpenURL: app URL create/update/delete/status/list and public agent conversation compatibility.
- API keys and app keys: local persisted lifecycle matching the frontend management flows.
- RAG app lifecycle: create/update/delete/copy/list, draft config save/read, publish/unpublish/version list/version update/rollback, published detail read, draft/published AG-UI chat shell, and multipart upload response compatibility.
- Workflow app lifecycle: create/list/copy/import/export/delete, generic app publish/unpublish/version list/version update/rollback, local run shell, assistant workflow select from real created workflows, workflow tool select/action/tool-box compatibility backed by resource-center tools, `/workflow/api/workflow/parameter`, `/workflow/api/api/workflow/use`, `/workflow/api/workflow/openapi_schema`, and `/api/bot/upload_file` avatar upload compatibility.
- Chatflow app lifecycle: create/list/copy/import/export/delete, generic app publish/unpublish/version list/version update/rollback, and local chatflow application list/detail plus conversation-delete compatibility for `/appspace/chatflow/*` and `/chatflow/*` frontend calls.
- Safety guard lifecycle: sensitive word table create/list/detail/update/reply/delete/select and sensitive word upload/list/delete through `wanwu-service-app` SafetyService.
- Assistant extension bindings:
  - `POST/DELETE/PUT /assistant/tool/workflow`
  - `POST/DELETE/PUT /assistant/tool/mcp`
  - `POST/DELETE/PUT /assistant/tool`
  - `PUT /assistant/tool/config`
  - `POST/DELETE/PUT /assistant/skill`
  - `POST/DELETE/PUT /assistant/multi-agent`
  - `PUT /assistant/multi-agent/config`
  - `GET /assistant/select`, `/tool/select`, `/tool/action/list`, `/tool/action/detail`, `/mcp/select`, `/mcp/action/list`, `/workflow/select`

The binding state is stored with the assistant draft config in JSON columns:

- `workflow_infos`
- `mcp_infos`
- `tool_infos`
- `skill_infos`
- `multi_agent_infos`

These fields are returned through `GET /assistant/draft` as `workFlowInfos`, `mcpInfos`, `toolInfos`, `skillInfos`, and `multiAgentInfos`, matching the original Go BFF response shape. Tool and MCP select/action endpoints now read from `wanwu-service-mcp` when available, with the previous AppService local compatibility data kept only as a test fallback.

RAG draft state is stored in the RAG-specific tables:

- `rag_drafts`
- `rag_draft_configs`
- `rag_snapshots`

The BFF exposes the original frontend paths under `/user/api/v1/appspace/rag/*`, including list, draft detail, published detail, create, update, config update, copy, and delete. Generic app publish/version endpoints now accept `appType=rag` in addition to Agent-compatible app types. The RAG chat endpoints `/rag/chat/draft` and `/rag/chat` return the AG-UI SSE event sequence consumed by the current frontend, while `/rag/upload` accepts multipart `files` and returns `fileList[{fileIndex,fileUrl}]` like the Go BFF.

Workflow and Chatflow draft and snapshot state is stored in Workflow-specific tables:

- `workflow_drafts`
- `workflow_snapshots`

The BFF exposes the original frontend paths under `/user/api/v1/appspace/workflow/*` for list, create, copy, import, export, and convert-shell compatibility. It also maps `/user/api/v1/workflow/tool/select`, `/workflow/tool/action`, and `/workflow/tool/box` to the Java MCP resource service, returning the Go workflow editor field shapes (`toolId/toolName/actions`, action inputs/outputs, and snake_case tool-box metadata). Chatflow uses the same storage tables with `apps.app_type=chatflow`, plus `/user/api/v1/appspace/chatflow/*`, `/user/api/v1/chatflow/application/list`, `/user/api/v1/chatflow/application/info`, and `/user/api/v1/chatflow/conversation/delete`. Generic app publish/version/delete endpoints now accept `appType=workflow` and `appType=chatflow`. The separate frontend Workflow API prefix `/workflow/api` is served by `WanwuWorkflowApiController`, and Docker nginx proxies `/workflow/api/` plus `/api/` to BFF so the zero-change Vue frontend no longer sees gateway-level 404s for Workflow schema/use/avatar-upload calls.

## Original Go Mapping

- Go request contracts came from `internal/bff-service/model/request/assistant.go`.
- Go response contracts came from `internal/bff-service/model/response/assistant.go`.
- Go routes came from `internal/bff-service/server/http/handler/router/v1/assistant.go`, `tool.go`, `workflow.go`, `chatflow.go`, `rag.go`, `safety.go`, and openapi workflow handlers.
- Go RPC boundaries came from `proto/assistant-service/assistant-service.proto`, `proto/rag-service/rag-service.proto`, and the app-service safety gRPC package.

## Known Gaps

- No real MCP server execution or external tool invocation yet. Local OpenAPI schema parsing and MCP Server resource binding are covered by `wanwu-service-mcp`, but runtime protocol handling is still a later slice.
- Workflow and Chatflow now have persisted local app lifecycles and frontend-compatible import/export shells. The actual visual editor engine, graph execution, Coze-compatible runtime, node validation, and advanced Workflow/Chatflow marketplace/template flows are still missing.
- Skill marketplace/custom/acquired skill flows are implemented in the resource service slice, not this app-service slice.
- Safety guard management is implemented, but Agent/RAG/Model chat streams do not yet perform real sensitive word interception.
- Prompt templates now have local resource-center list/detail/copy and deterministic optimize/reason/evaluate SSE compatibility. Assistant templates remain a future slice.
- RAG chat currently returns a deterministic local answer after validating draft/published RAG existence. Real retrieval, QA hit handling, knowledge search lists, reasoning frames, and model generation remain future slices.
