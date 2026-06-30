# App and Assistant Service Reproduction

Date: 2026-06-30

## Covered Java Behavior

- Assistant draft lifecycle: create, update base info, update config, delete, copy.
- App publish lifecycle: publish, unpublish, latest version, version list, update release note/scope, rollback.
- Conversations: draft/published conversation create/list/detail/delete/clear and deterministic local SSE responses.
- OpenURL: app URL create/update/delete/status/list and public agent conversation compatibility.
- API keys and app keys: local persisted lifecycle matching the frontend management flows.
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

## Original Go Mapping

- Go request contracts came from `internal/bff-service/model/request/assistant.go`.
- Go response contracts came from `internal/bff-service/model/response/assistant.go`.
- Go routes came from `internal/bff-service/server/http/handler/router/v1/assistant.go`, `tool.go`, and `workflow.go`.
- Go RPC boundaries came from `proto/assistant-service/assistant-service.proto`.

## Known Gaps

- No real MCP server execution, OpenAPI schema parsing, or external tool invocation yet.
- Workflow select currently exposes development compatibility data; Coze/workflow import/export/execution is still missing.
- Skill marketplace/custom/acquired skill flows are not implemented in this slice.
- Prompt templates and assistant templates remain separate future slices.
