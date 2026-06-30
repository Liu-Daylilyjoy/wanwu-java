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
| `callback/router.go` | 31 | Missing |
| `openapi/router.go` | 11 | Missing |
| `openurl/router.go` | 11 | Mostly covered for Agent OpenURL; file upload routes missing |
| `v1/api_key.go` | 5 | Covered for create/update/delete/list/status |
| `v1/assistant.go` | 32 | Partially covered; tool/workflow/mcp/skill/multi-agent/template/select missing |
| `v1/callback.go` | 5 | Missing |
| `v1/common.go` | 48 | Partially covered; app-key and app-list covered for Agent; model select endpoints covered; user/file/doc selects still missing |
| `v1/explore.go` | 25 | Missing except overlapping published Agent conversation behavior |
| `v1/guest.go` | 13 | Missing |
| `v1/knowledge.go` | 79 | Partially covered; knowledge CRUD/list, tags, splitters, docs empty state/config/import-tip/upload-limit, metadata shell, permissions, QA pair CRUD/list/switch/hit/import-tip/export shell, graph/report/export empty contracts, and external stubs covered for frontend navigation; real doc import/indexing/report/RAG integration/external knowledge missing |
| `v1/mcp_square.go` | 3 | Missing |
| `v1/model.go` | 15 | Partially covered; model list/detail/import/update/delete/status, recommend, validate-thinking stub, provider list, select endpoints, and model experience dialog/list/records/delete/local SSE covered; ASR stream and real provider inference missing |
| `v1/oauth.go` | 5 | Missing |
| `v1/permission.go` | 23 | Partially covered; user/role/org list, role select, role template, org select/info read paths covered; writes and batch import missing |
| `v1/rag.go` | 10 | Missing |
| `v1/safety.go` | 9 | Missing |
| `v1/setting.go` | 3 | Missing |
| `v1/skill.go` | 28 | Missing |
| `v1/statistic.go` | 14 | Missing |
| `v1/statistic_client.go` | 1 | Missing |
| `v1/tool.go` | 39 | Missing |
| `v1/wga.go` | 31 | Missing |
| `v1/workflow.go` | 20 | Missing |

## Proto Service Groups

| Proto service | RPC count | Java status |
| --- | ---: | --- |
| `AppService` | 41 | Partially covered for publish/version/OpenURL/app keys/API keys; explore/statistics missing |
| `AssistantService` | 67 | Partially covered through Java `AppService`; assistant-side tool/skill/workflow/multi-agent/WGA missing |
| `IAMService` | 49 | Development login, frontend permission split, and IAM user/role/org read compatibility covered; persisted user/org/role/OAuth writes still missing |
| `KnowledgeBaseDocService` | 26 | Frontend-compatible list/config/import-tip/upload-limit/segment shell covered; real file import, parsing, chunk indexing, export, and reimport missing |
| `KnowledgeBaseKeywordsService` | 5 | Missing beyond empty metadata/list compatibility shells |
| `KnowledgeBasePermissionService` | 6 | Partially covered with owner/admin/user/org frontend compatibility |
| `KnowledgeBaseQAService` | 10 | Partially covered with in-memory QA pair create/update/delete/list/switch/import-tip/export shell and local text hit; file import parsing, real vector/keyword search, and persisted export records missing |
| `KnowledgeBaseReportService` | 6 | Empty list/mutation contracts covered; report generation missing |
| `KnowledgeBaseService` | 28 | Partially covered with in-memory CRUD/list/hit shell |
| `KnowledgeBaseSplitterService` | 4 | Partially covered with preset and custom splitter CRUD |
| `KnowledgeBaseTagService` | 6 | Partially covered with tag CRUD/bind/count |
| `MCPService` | 65 | Placeholder-level |
| `ModelService` | 16 | Partially covered with Java RPC contract and Docker in-memory repository for model management/select/recommend/provider flows plus model experience dialog/record persistence; real provider inference and callback APIs still missing |
| `OperateService` | 6 | Placeholder-level |
| `PermService` | 2 | Missing as independent service |
| `RagService` | 15 | Placeholder-level |
| `SafetyService` | 12 | Missing |

## Reproduction Order

1. Platform and compatibility shell: common response, auth context, file upload, appspace app list, app keys, API keys.
2. IAM: login, user/org/role/permission, OAuth app management.
3. Model: model import/list/select/status/delete and model experience dialog.
4. Knowledge: knowledge base CRUD, docs, QA, tags, splitters, permissions, reports, callback status updates.
5. MCP/tool/prompt/skill: custom tools, MCP servers/tools, prompt templates, built-in/custom/acquired skills.
6. RAG: draft/publish/chat/copy/version and knowledge integration.
7. Assistant full surface: workflows, MCP tools, custom tools, skills, multi-agent, templates, select endpoints.
8. Explore/guest/statistics/callback/openapi: marketplace, public API, API usage metrics, callback compatibility.
9. WGA/general agent and sandbox integrations.

## Development IAM Accounts

The Java IAM service currently exposes two Docker development accounts:

- `admin` / token `dev-token`: implemented stable permissions only: `permission`, `permission.user`, `permission.org`, `permission.role`, `model`, `model.model_management`, `app`, `app.agent`, `api_key`, `api_key.api_key_management`, `resource.knowledge`.
- `app` / token `dev-token-app`: `app` and `app.agent` only.

Unreproduced frontend modules are intentionally not exposed to avoid `Not Found` toasts from pages whose Java backend routes are not implemented yet. Permission management currently exposes read paths only; create/update/delete/status/batch operations still belong to the persisted IAM slice. Model Management is exposed because its Docker development backend is implemented and verified, but it still uses in-memory storage until the model persistence slice is promoted to MySQL. Knowledge is exposed because the frontend can now create/list/update/delete a knowledge base and enter the main empty-state detail views without 404s; document import/indexing, QA, report generation, external knowledge, and RAG integration remain later slices. The ontology agent menu has been removed from this Java reproduction scope. Details are tracked in `docs/development-login-accounts.md`. This is a development compatibility slice, not the final reproduced Go IAM/model/knowledge persistence model.

## Operating Rule

Each slice must:

- Start from Go source behavior and frontend API callers.
- Add failing tests first.
- Implement Java service/BFF/persistence as needed.
- Add or update technical notes.
- Run Docker Maven tests and Docker Compose smoke tests when the slice touches runtime routes.
- Commit and push independently.
