# App and Assistant Service Reproduction

Date: 2026-07-01

## Covered Java Behavior

- Assistant draft lifecycle: create, update base info, update config, delete, copy.
- App publish lifecycle: publish, unpublish, latest version, version list, update release note/scope, rollback.
- Conversations: draft/published conversation create/list/detail/delete/clear and frontend SSE responses; the BFF now tries the configured assistant `modelConfig.modelId` through the same OpenAI-compatible upstream path used by model experience before delegating persistence to AppService, while AppService still falls back to deterministic local responses when no usable provider config exists. Configured assistant knowledge bases feed the same local `KnowledgeService.hitKnowledge` path used by RAG, conversation details persist the returned `searchList`, and OpenAPI Agent chat can expose it as Go-style `search_list`.
- OpenURL: app URL create/update/delete/status/list and public agent conversation compatibility.
- API keys and app keys: local persisted lifecycle matching the frontend management flows.
- RAG app lifecycle: create/update/delete/copy/list, draft config save/read, publish/unpublish/version list/version update/rollback, published detail read, draft/published AG-UI chat shell plus OpenAPI RAG chat with configured OpenAI-compatible model aggregation, MySQL-persisted chat snapshots, and multipart upload response compatibility.
- Workflow app lifecycle: create/list/copy/import/export/delete, Go-style export attachment headers, generic app publish/unpublish/version list/version update/rollback, MySQL-persisted local run snapshots with schema-aware output, Go-style process readback from persisted run records, node `outputs/output` declaration mapping with template variable rendering, Go-template numeric node basics (`1` start, `2` end, `3` LLM, `15` string concat, `32` variable merge), `sourceNodeID/targetNodeID` edges, `block-output` literal/ref input resolution, prior-node output propagation for later nodes, deterministic node trace output, and text/handle/object-style conditional edge execution, assistant workflow select from real created workflows, workflow tool select/action/tool-box compatibility backed by resource-center tools, `/workflow/api/workflow/parameter`, `/workflow/api/api/workflow/use`, `/workflow/api/api/workflow_api/get_process`, `/workflow/api/workflow/openapi_schema`, and `/api/bot/upload_file` avatar upload compatibility.
- Chatflow app lifecycle: create/list/copy/import/export/delete, Go-style export attachment headers, generic app publish/unpublish/version list/version update/rollback, local chatflow application list/detail plus conversation-delete compatibility for `/appspace/chatflow/*` and `/chatflow/*` frontend calls, and OpenAPI Chatflow conversation/message persistence for the public Chatflow route family.
- Template square compatibility: assistant and Workflow template list/detail/recommend/download data plus Workflow template download counts are backed by `app_templates`, with the BFF also merging read-only bundles generated from the 6 local Go Assistant templates and the 30 local Go Workflow templates.
- Safety guard lifecycle: sensitive word table create/list/detail/update/reply/delete/select, single word upload, uploaded XLSX/CSV file import, list, delete, and Agent/RAG local chat input blocking through `wanwu-service-app` SafetyService-backed configuration.
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
- `rag_chat_records`

Assistant draft/published stream calls and RAG draft/published chat calls now use the saved `modelConfig.modelId` in the BFF to attempt an OpenAI-compatible `stream:true` model call. The aggregated provider answer is passed into AppService as the base response for persistence. AppService then uses the saved `knowledgeBaseConfig` to call the Java knowledge compatibility service, normalizes returned hit cards with `score`, `kb_name`, and `user_kb_name`, appends local evidence when available, and stores the assistant conversation detail or RAG chat snapshot `searchList`. Safety input/output replacement still wins: blocked prompts or blocked generated output clear the local search list and return the configured safety reply. If model config is missing, inactive, uses a development placeholder key, or the upstream fails, the same deterministic local answer remains the fallback.

The BFF exposes the original frontend paths under `/user/api/v1/appspace/rag/*`, including list, draft detail, published detail, create, update, config update, copy, and delete. Generic app publish/version endpoints now accept `appType=rag` in addition to Agent-compatible app types. The RAG chat endpoints `/rag/chat/draft`, `/rag/chat`, and public `/service/api/openapi/v1/rag/chat` return the frontend/Go-compatible envelopes, try the configured OpenAI-compatible model before AppService persistence, persist each question/answer/search-list snapshot in `rag_chat_records`, and `/rag/upload` accepts multipart `files` and returns `fileList[{fileIndex,fileUrl}]` like the Go BFF.

Workflow and Chatflow draft and snapshot state is stored in Workflow-specific tables:

- `workflow_drafts`
- `workflow_snapshots`
- `workflow_run_records`

Template square development records are stored in:

- `app_templates`

The BFF exposes the original frontend paths under `/user/api/v1/appspace/workflow/*` for list, create, copy, import, export, and workflow-to-chatflow conversion. Export endpoints now return the Go attachment headers and filenames (`workflow_export.json` / `chatflow_export.json`) while keeping the exported `{name, desc, schema}` JSON body. It also maps `/user/api/v1/workflow/tool/select`, `/workflow/tool/action`, and `/workflow/tool/box` to the Java MCP resource service, returning the Go workflow editor field shapes (`toolId/toolName/actions`, action inputs/outputs, and snake_case tool-box metadata). Workflow run calls now generate a `workflow-run-*` id, return status/timing metadata through the frontend `/workflow/api/api/workflow/use` and `/user/api/v1/workflow/run` envelopes, and `/workflow/api/api/workflow_api/get_process` reads the persisted run snapshot back as Go-style `executeStatus/nodeResults/nodeEvents`. The local runner derives declared top-level `outputs` fields from saved schema and node-level `outputs/output` declarations, renders simple `{{input}}` and `${input}` template variables from runtime input/node context, resolves Go template `data.inputs.inputParameters` literal/ref values including `source=block-output` with `blockID/name`, follows `sourceNodeID/targetNodeID` graph edges, gives imported numeric node types deterministic local behavior for start/end/LLM/string-concat/variable-merge nodes, propagates each executed node output into later node inputs, includes deterministic local `steps`, `trace`, `nodeOutputs`, `edges`, and `edgeEvaluations` for schemas that contain nodes, follows text conditions such as `approved == true`, handle branches such as `sourceHandle=true`, and object-style conditions such as `{field, operator, value}` / `conditions[]`, and persists input/output/status/cost in `workflow_run_records`. Chatflow uses the same storage tables with `apps.app_type=chatflow`, plus `/user/api/v1/appspace/chatflow/*`, `/user/api/v1/appspace/chatflow/list` for the zero-change `getAppSpaceList('chatflow')` frontend path, chatflow-to-workflow conversion, `/user/api/v1/chatflow/application/list`, `/user/api/v1/chatflow/application/info`, and `/user/api/v1/chatflow/conversation/delete`. The conversion endpoints now persist the Go-equivalent `apps.app_type` switch in Java AppService/MySQL while preserving the frontend `workflow_id` contract. Public Chatflow OpenAPI conversations reuse the existing conversation tables with `conversation_type=chatflow_openapi`, so create/chat/list/message/delete state survives Docker restarts when AppService is available. Generic app publish/version/delete endpoints now accept `appType=workflow` and `appType=chatflow`. The separate frontend Workflow API prefix `/workflow/api` is served by `WanwuWorkflowApiController`, and Docker nginx proxies `/workflow/api/` plus `/api/` to BFF so the zero-change Vue frontend no longer sees gateway-level 404s for Workflow schema/use/avatar-upload calls.

## Original Go Mapping

- Go request contracts came from `internal/bff-service/model/request/assistant.go`.
- Go response contracts came from `internal/bff-service/model/response/assistant.go`.
- Go routes came from `internal/bff-service/server/http/handler/router/v1/assistant.go`, `tool.go`, `workflow.go`, `chatflow.go`, `rag.go`, `safety.go`, and openapi workflow handlers.
- Go RPC boundaries came from `proto/assistant-service/assistant-service.proto`, `proto/rag-service/rag-service.proto`, and the app-service safety gRPC package.

## Known Gaps

- No real MCP server execution or external tool invocation yet. Local OpenAPI schema parsing and MCP Server resource binding are covered by `wanwu-service-mcp`, but runtime protocol handling is still a later slice.
- Workflow and Chatflow now have persisted local app lifecycles and frontend-compatible import/export shells. Workflow run snapshots, Go-style process readback, schema-aware outputs, node declared-output mapping, Go-template numeric node basics, `block-output` input references, prior-node output propagation, deterministic node trace output, and text/handle/object-style conditional edge execution are persisted for development verification, while the actual visual editor engine, advanced branch semantics, Coze-compatible runtime, node validation, and advanced Workflow/Chatflow marketplace/template flows are still missing.
- Skill marketplace/custom/acquired skill flows are implemented in the resource service slice, not this app-service slice.
- Safety guard management, local uploaded-file word import, Agent/RAG local chat input/output replacement, and BFF Model Experience global input/output replacement are implemented for Java's local deterministic streams; Go-equivalent provider token-stream Aho-Corasick interception remains later.
- Prompt templates now have local resource-center list/detail/copy and deterministic optimize/reason/evaluate SSE compatibility. Assistant and Workflow template square rows are persisted as development marketplace seed data, with read-only Go local template bundles filling the original BFF config templates; real template publication/ranking/governance remains later.
- Assistant chat plus frontend/OpenAPI RAG chat now support configured OpenAI-compatible model generation as an aggregated answer before AppService persistence, then enrich from local knowledge hits when configured. Real vector/rerank retrieval, reasoning frames, token-by-token Assistant/RAG streaming, tool/MCP/multi-agent orchestration, and provider-specific adapters remain future slices.
