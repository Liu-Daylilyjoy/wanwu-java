# App and Assistant Service Reproduction

Date: 2026-06-30

## Covered Java Behavior

- Assistant draft lifecycle: create, update base info, update config, delete, copy.
- App publish lifecycle: publish, unpublish, latest version, version list, update release note/scope, rollback.
- Conversations: draft/published conversation create/list/detail/delete/clear and deterministic local SSE responses.
- OpenURL: app URL create/update/delete/status/list and public agent conversation compatibility.
- API keys and app keys: local persisted lifecycle matching the frontend management flows.
- RAG app lifecycle: create/update/delete/copy/list, draft config save/read, publish/unpublish/version list/version update/rollback, published detail read, draft/published AG-UI chat shell, and multipart upload response compatibility.
- Workflow app lifecycle: create/list/copy/import/export/delete, generic app publish/unpublish/version list/version update/rollback, local run shell, assistant workflow select from real created workflows, `/workflow/api/workflow/parameter`, `/workflow/api/api/workflow/use`, `/workflow/api/workflow/openapi_schema`, and `/api/bot/upload_file` avatar upload compatibility.
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

These fields are returned through `GET /assistant/draft` as `workFlowInfos`, `mcpInfos`, `toolInfos`, `skillInfos`, and `multiAgentInfos`, matching the original Go BFF response shape.

RAG draft state is stored in the RAG-specific tables:

- `rag_drafts`
- `rag_draft_configs`
- `rag_snapshots`

The BFF exposes the original frontend paths under `/user/api/v1/appspace/rag/*`, including list, draft detail, published detail, create, update, config update, copy, and delete. Generic app publish/version endpoints now accept `appType=rag` in addition to Agent-compatible app types. The RAG chat endpoints `/rag/chat/draft` and `/rag/chat` return the AG-UI SSE event sequence consumed by the current frontend, while `/rag/upload` accepts multipart `files` and returns `fileList[{fileIndex,fileUrl}]` like the Go BFF.

Workflow draft and snapshot state is stored in Workflow-specific tables:

- `workflow_drafts`
- `workflow_snapshots`

The BFF exposes the original frontend paths under `/user/api/v1/appspace/workflow/*` for list, create, copy, import, export, and convert-shell compatibility. Generic app publish/version/delete endpoints now accept `appType=workflow`. The separate frontend Workflow API prefix `/workflow/api` is served by `WanwuWorkflowApiController`, and Docker nginx proxies `/workflow/api/` plus `/api/` to BFF so the zero-change Vue frontend no longer sees gateway-level 404s for Workflow schema/use/avatar-upload calls.

## Original Go Mapping

- Go request contracts came from `internal/bff-service/model/request/assistant.go`.
- Go response contracts came from `internal/bff-service/model/response/assistant.go`.
- Go routes came from `internal/bff-service/server/http/handler/router/v1/assistant.go`, `tool.go`, `workflow.go`, `rag.go`, and openapi workflow handlers.
- Go RPC boundaries came from `proto/assistant-service/assistant-service.proto` and `proto/rag-service/rag-service.proto`.

## Known Gaps

- No real MCP server execution, OpenAPI schema parsing, or external tool invocation yet.
- Workflow now has a persisted local app lifecycle and frontend-compatible import/export/run shell. The actual visual editor engine, graph execution, Coze-compatible runtime, node validation, and advanced Workflow marketplace/template flows are still missing.
- Skill marketplace/custom/acquired skill flows are not implemented in this slice.
- Prompt templates and assistant templates remain separate future slices.
- RAG chat currently returns a deterministic local answer after validating draft/published RAG existence. Real retrieval, QA hit handling, knowledge search lists, reasoning frames, and model generation remain future slices.
