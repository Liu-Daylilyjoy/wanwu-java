# Wanwu Backend Reproduction Gap Matrix

Date: 2026-06-30

## Scope Baseline

The original Go backend is a multi-service system, not just the Agent loop.

Measured from `D:\work\week3\wanwu`:

- HTTP routes registered by Go BFF: 461
- Proto services: 17
- Proto RPC methods: about 358
- Gorm model structs under service `client/model`: 98
- Runtime services: bff, iam, app, assistant, agent, model, knowledge, rag, mcp, operate, plus callback and sandbox sidecars

Measured from this Java repo before the full reproduction pass:

- `wanwu-service-app`: 25 Java files, 1 test file
- `wanwu-service-bff`: 6 Java files, 2 test files
- Most other service modules: service-info controller + placeholder Dubbo implementation only
- Real business coverage: Agent create/edit/delete/copy/publish/version/conversation/recommend/OpenURL

## Route Groups From Go BFF

| Group | Go routes | Java status |
| --- | ---: | --- |
| `callback/router.go` | 31 | Partially covered; `/callback/v1` file/model/workflow/MCP/chatflow/agent/RAG/WGA/app/skill route shapes now return stable development responses and stream shells through Docker Nginx, while real provider execution, RAG recall, sandbox runtime, auth/signature checks, and callback metrics remain missing |
| `openapi/router.go` | 11 | Partially covered; public `/service/api/openapi/v1` agent/conversation/chat/RAG/workflow/chatflow/model/knowledge/MCP/OAuth route shapes now exist with API-key/dev-token context; real inference, workflow engine, OAuth signing, MCP runtime, knowledge indexing, and API usage metrics remain missing |
| `openurl/router.go` | 11 | Mostly covered for Agent OpenURL plus anonymous file upload/merge/clean compatibility; deeper public runtime parity remains partial |
| `v1/api_key.go` | 5 | Covered for create/update/delete/list/status |
| `v1/assistant.go` | 32 | Partially covered; assistant CRUD/config/copy/publish/version/conversation/OpenURL plus local tool/workflow/MCP/skill/multi-agent binding/select compatibility and assistant template list/detail/copy covered; real assistant-side orchestration still missing |
| `v1/callback.go` | 5 | Partially covered; doc status, deploy info, category info, doc status init, and knowledge status aliases now exist for `/user/api/v1/api/*` and `/api/*`, while real persisted import/indexing status mutation remains missing |
| `v1/chatflow.go` | 10 | Partially covered; Chatflow appspace create/list/copy/import/export/delete, workflow-to-chatflow convert shell, generic publish/version integration, chatflow application list/detail, and conversation-delete compatibility covered through Java AppService; real Coze project conversation runtime and chatflow execution remain missing |
| `v1/common.go` | 48 | Partially covered; app-key, app-list, model selects, user info/language/password/avatar compatibility, local file upload/check/merge/clean/delete/direct-upload compatibility, and doc-center shell covered; several select helpers and real IAM/file/doc persistence still missing |
| `v1/explore.go` | 25 | Partially covered; exploration app list/favorite/history contracts, prompt template, MCP square, and Skill square frontend routes are covered; marketplace history recording, ranking, and deeper published app runtime remain partial |
| `v1/guest.go` | 13 | Partially covered; workflow template list/detail/recommend/download contracts covered for Template Square; other guest/public routes still missing |
| `v1/knowledge.go` | 79 | Partially covered with Docker MySQL snapshot persistence; knowledge CRUD/list, tags, splitters, local doc import/list/delete/url-analysis/segment CRUD, config/import-tip/upload-limit, metadata shell, permissions, QA pair CRUD/list/switch/hit/import-tip/export shell, graph/report/export empty contracts, and external stubs covered for frontend navigation; normalized Go tables, real file parsing/vector indexing/report/RAG integration/external knowledge missing |
| `v1/mcp_square.go` | 3 | Partially covered; local MCP square list/detail/recommend contracts return deterministic seed data |
| `v1/model.go` | 15 | Partially covered; model list/detail/import/update/delete/status, recommend, validate-thinking stub, provider list, select endpoints, and model experience dialog/list/records/delete/local SSE covered; ASR stream and real provider inference missing |
| `v1/oauth.go` | 5 | Partially covered; operation OAuth app create/list/update/delete/status covered with a Docker development in-memory IAM repository; OAuth authorize/token runtime remains missing |
| `v1/permission.go` | 23 | Mostly covered for Docker development; user/role/org create/update/delete/list/info/status, role select/template, org user add, users-outside-org, and multipart batch-import compatibility are covered with a MySQL-backed JSON compatibility repository; admin password reset is covered by the common user controller; normalized Go-equivalent IAM tables, Excel parsing, invitations, audit logs, and real permission enforcement remain missing |
| `v1/rag.go` | 10 | Partially covered; RAG app draft/create/update/config/copy/delete/list/publish/version, AG-UI draft/published chat shell, and multipart upload compatibility covered for frontend access; real RAG retrieval/generation still missing |
| `v1/safety.go` | 9 | Partially covered with Docker MySQL snapshot persistence; sensitive word table create/list/detail/update/reply/delete/select and word upload/list/delete covered for frontend safety guard management; normalized tables, real chat-stream sensitive word interception, and file Excel parsing missing |
| `v1/setting.go` | 3 | Partially covered; custom tab/login/home writes and `/base/custom` readback covered for the frontend platform setting page; Operate-service persistence and asset storage missing |
| `v1/skill.go` | 28 | Partially covered; custom/built-in/acquired Skill resource CRUD/config, Skill square list/detail/share/download, Skill select, and local Skill conversation SSE/save shell covered; real skill package parsing, LLM generation, and persistence missing |
| `v1/statistic.go` | 14 | Partially covered; app/model/API Key dashboard select, overview, trend, list, record, and export compatibility routes return frontend-safe development data; real Operate/App aggregation, cron sync, and Excel export remain missing |
| `v1/statistic_client.go` | 1 | Partially covered; `/statistic/client` returns the frontend-compatible overview/trend contract with zeroed development data; Operate/Redis statistics aggregation missing |
| `v1/tool.go` | 39 | Partially covered with Docker MySQL snapshot persistence; custom tool CRUD/list/detail/schema parsing, built-in tool square/detail/API-key shell, MCP import/server/tool binding/OpenAPI-tool shell, assistant tool/MCP select/action endpoints, prompt custom/template CRUD/copy and local SSE optimize/reason/evaluate covered; real remote MCP/tool execution and normalized resource tables still missing |
| `v1/wga.go` | 31 | Missing |
| `v1/workflow.go` | 20 | Partially covered; Workflow appspace create/list/copy/import/export/delete, template copy, generic publish/version integration, local run shell, assistant workflow select, `/workflow/api` parameter/use/openapi-schema compatibility, and avatar upload route covered; full visual workflow editor engine and Coze execution still missing |

## Proto Service Groups

| Proto service | RPC count | Java status |
| --- | ---: | --- |
| `AppService` | 41 | Partially covered for assistant CRUD/config, publish/version/OpenURL, app keys/API keys, persisted local assistant resource bindings, RAG app lifecycle/chat shell, Workflow app lifecycle/import/export/run shell, Chatflow lifecycle/application shell, Safety guard management shell, and frontend statistics dashboard compatibility; real RAG orchestration, real Workflow/Chatflow engine execution, real statistics aggregation, and runtime sensitive-word interception missing |
| `AssistantService` | 67 | Partially covered through Java `AppService`; local workflow/MCP/tool/skill/multi-agent binding state and assistant template copy covered, while real assistant-side orchestration, prompt, WGA, and skill conversations remain missing |
| `IAMService` | 49 | Development login, frontend permission split, IAM user/role/org read-write compatibility, platform custom setting readback, and OAuth app management covered with a Docker MySQL JSON compatibility repository; normalized IAM/OAuth schema parity, real password policy, Excel import parsing, invitations, audit logs, and Operate custom config storage still missing |
| `KnowledgeBaseDocService` | 26 | Partially covered with Docker MySQL snapshot-backed doc import/list/delete/url-analysis/default segment and segment create/update/delete/status/labels; real file parsing, chunk indexing, export records, reimport, child segment persistence, and async status callbacks missing |
| `KnowledgeBaseKeywordsService` | 5 | Partially covered with Docker MySQL snapshot-backed create/list/detail/update/delete, knowledge association, and document-page keyword echo; normalized `knowledge_keywords` table and RAG keyword sync remain missing |
| `KnowledgeBasePermissionService` | 6 | Partially covered with owner/admin/user/org frontend compatibility |
| `KnowledgeBaseQAService` | 10 | Partially covered with Docker MySQL snapshot-backed QA pair create/update/delete/list/switch/import-tip/export shell and local text hit; file import parsing, real vector/keyword search, and persisted export records missing |
| `KnowledgeBaseReportService` | 6 | Empty list/mutation contracts covered; report generation missing |
| `KnowledgeBaseService` | 28 | Partially covered with Docker MySQL snapshot-backed CRUD/list/hit shell |
| `KnowledgeBaseSplitterService` | 4 | Partially covered with preset and custom splitter CRUD |
| `KnowledgeBaseTagService` | 6 | Partially covered with tag CRUD/bind/count |
| `MCPService` | 65 | Partially covered with Java RPC contract and Docker MySQL snapshot resource repository for custom tools, built-in tools, custom MCP, MCP Server tool binding, MCP square seeds, custom prompts/templates, local prompt SSE, custom/built-in/acquired skills, Skill square, and Skill conversation shell; real MCP protocol runtime, external API invocation, real skill package execution, and normalized tables missing |
| `ModelService` | 16 | Partially covered with Java RPC contract and Docker MySQL JSON compatibility repository for model management/select/recommend/provider flows plus model experience dialog/record persistence; normalized Go-equivalent model tables, real provider inference, encrypted credentials, and callback APIs still missing |
| `OperateService` | 6 | Placeholder-level |
| `PermService` | 2 | Missing as independent service |
| `RagService` | 15 | Partially covered through Java `AppService` and BFF for appspace RAG lifecycle/config/publish/version/copy, frontend AG-UI chat shell, and upload response shape; standalone retrieval/QA/knowledge stream behavior still missing |
| `SafetyService` | 12 | Partially covered with Java RPC contract and Docker MySQL snapshot repository for table CRUD/reply/select and word upload/list/delete; normalized Go tables, Excel parsing, global enforcement, and chat-stream interception missing |

## Reproduction Order

1. Platform and compatibility shell: common response, auth context, file upload, appspace app list, app keys, API keys.
2. IAM: login, user/org/role/permission, OAuth app management. User/org/role/OAuth/custom-setting state now survives Docker restarts through MySQL; normalize IAM tables, real password policy, Excel import, invitations, and audit logs next.
3. Model: model import/list/select/status/delete and model experience dialog.
4. Knowledge: knowledge base CRUD, docs, QA, tags, splitters, permissions, reports, callback status updates.
5. MCP/tool/prompt/skill: custom tools, MCP servers/tools, prompt templates, built-in/custom/acquired skills. Tool/MCP/Prompt/Skill management now has a Docker MySQL snapshot-backed frontend-compatible loop; real MCP execution, skill package execution, and normalized tables remain next.
6. Safety: sensitive word table/word management now has a Docker MySQL snapshot-backed loop; normalized tables, runtime chat-stream interception, and Excel parsing remain next.
7. Setting: platform tab/login/home custom config now has a frontend-compatible write/read loop; Operate persistence remains next.
8. Operation: OAuth app management and client-statistic frontend contracts are covered; OAuth runtime and real Operate/Redis statistics remain next.
9. Exploration square: app list/favorite/history and MCP/Prompt/Skill square frontend contracts are covered; real marketplace ranking/history remain next.
10. Statistics dashboard: app/model/API Key select, overview, trend, list, record, and export compatibility are covered; real Operate/App aggregation remains next.
11. Templates: assistant template and workflow template list/detail/recommend/download/copy contracts are covered; real marketplace persistence, ranking, and template governance remain next.
12. File upload: chunk upload/check/list/merge/clean/delete, direct upload, proxy upload, inferpub upload, and OpenURL anonymous upload contracts are covered with local BFF storage; MinIO/object lifecycle remains next.
13. Common user/doc center: user info, password/avatar/language compatibility, and doc-center menu/markdown/search/entry are covered with development data; real IAM profile persistence and static manual indexing remain next.
14. RAG: draft/publish/chat/copy/version and knowledge integration. Management lifecycle, frontend chat shell, and upload response shape are covered; retrieval/search-list generation remains next.
15. Assistant full surface: Workflow app lifecycle is now covered as a local shell; continue with deeper runtime orchestration and select endpoints.
16. Guest/callback/openapi: public API compatibility shells are covered for the main Go OpenAPI and callback route families; API usage metrics runtime, real public execution, callback auth/signature checks, provider execution, and persisted status mutation remain next.
17. WGA/general agent and sandbox integrations remain out of the current exposed frontend scope unless explicitly reintroduced.

## Development IAM Accounts

The Java IAM service currently exposes two Docker development accounts:

- `admin` / token `dev-token`: implemented stable permissions only: `permission`, `permission.user`, `permission.org`, `permission.role`, `setting`, `model`, `model.model_management`, `app`, `app.rag`, `app.workflow`, `app.agent`, `api_key`, `api_key.api_key_management`, `resource`, `resource.knowledge`, `resource.tool`, `resource.mcp`, `resource.prompt`, `resource.skill`, `resource.safety`, `operation`, `operation.oauth`, `operation.statistic_client`, `exploration`, `exploration.app`, `exploration.mcp`, `exploration.template`, `exploration.skill`, `app_observability`, `app_observability.statistic`.
- `app` / token `dev-token-app`: `app`, `app.rag`, `app.workflow`, and `app.agent` only.

Unreproduced frontend modules are intentionally not exposed to avoid `Not Found` toasts from pages whose Java backend routes are not implemented yet. Permission management now has a Docker development user/org/role write loop, including create/update/delete/status and multipart batch-import compatibility; those mutable IAM/OAuth/custom-setting records survive restarts through `iam_service.iam_records`, while normalized Go-equivalent IAM tables, real Excel parsing, invitations, and audit logs remain later. Common user/profile routes currently cover development user info, password/avatar/language success contracts, admin password reset compatibility, and Doc Center seed content; real IAM profile persistence and static manual indexing remain later. The `setting` permission is exposed because the platform setting page can now write custom tab/login/home config and read it back through `/base/custom`. Operation is exposed because OAuth app management and client-statistic contracts are now available for the operation management page. Exploration is exposed because app square list/favorite/history, MCP square, prompt template square, Skill square, assistant template, and workflow template contracts are available. App Observation is exposed because the statistics dashboard can now load app/model/API Key selects, overview cards, trend charts, tables, details, and export buttons without 404s; real aggregation remains later. Model Management is exposed because model CRUD/status/select/provider/recommend and model-experience history now use a Docker MySQL compatibility repository; normalized model tables and real provider inference remain later. Knowledge is exposed because the frontend can now create/list/update/delete a knowledge base and use knowledge tags, splitters, docs, segments, permissions, and QA pairs through a Docker MySQL snapshot repository; normalized Go knowledge tables, document import/indexing, report generation, external knowledge, and real RAG retrieval integration remain later slices. Tool, MCP, Prompt, and Skill are exposed because the resource center now has Docker MySQL snapshot-backed Java management loops and frontend-compatible select/action/config/conversation contracts. Safety is exposed because the safety center now has a Docker MySQL snapshot-backed Java management loop. File upload is exposed through `/service/api/v1` and OpenURL aliases with BFF-local development storage; MinIO parity remains later. Public OpenAPI is exposed through `/service/api/openapi/v1` with development API-key context and compatibility shells for agent, RAG, workflow, chatflow, model, knowledge, MCP, and OAuth routes; true public runtime parity remains later. RAG is exposed because management plus draft/published chat shell and upload response shape are now available. Workflow and Chatflow are exposed because appspace lifecycle, template copy, import/export, publish/version, local run shell, `/workflow/api` compatibility, chatflow application compatibility, and avatar upload compatibility are now available. The ontology agent menu has been removed from this Java reproduction scope. Details are tracked in `docs/development-login-accounts.md`. This is a development compatibility slice, not the final reproduced Go IAM/model/knowledge/MCP/Safety persistence model.

## Operating Rule

Each slice must:

- Start from Go source behavior and frontend API callers.
- Add failing tests first.
- Implement Java service/BFF/persistence as needed.
- Add or update technical notes.
- Run Docker Maven tests and Docker Compose smoke tests when the slice touches runtime routes.
- Commit and push independently.
