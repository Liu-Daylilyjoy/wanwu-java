# OpenAPI Reproduction Notes

Date: 2026-07-04

## Go Source Baseline

The original Go BFF registers public routes in:

- `internal/bff-service/server/http/handler/router/openapi/router.go`
- `internal/bff-service/server/http/handler/openapi/openapi.go`
- `internal/bff-service/server/http/handler/openapi/agent.go`
- `internal/bff-service/server/http/handler/openapi/workflow.go`
- `internal/bff-service/server/http/handler/openapi/knowledge.go`
- `internal/bff-service/server/http/handler/openapi/model.go`
- `internal/bff-service/server/http/handler/openapi/mcp.go`
- `internal/bff-service/server/http/handler/openapi/oauth.go`

Go authenticates most routes with API Key middleware and stores user/org/api-key context on the request. The Java reproduction mirrors this at BFF level by requiring a token for protected OpenAPI routes, accepting `Authorization: Bearer`, `X-API-Key`, or `Api-Key`, rejecting missing, disabled, and expired keys with HTTP 401, and resolving real keys through `AppService.getApiKeyByKey`. Docker development tokens `dev-token` and `dev-token-app` still map to the known development accounts.

## Java Coverage

Implemented in `WanwuOpenApiController` under `/service/api/openapi/v1`:

- Agent create/delete/list/info plus config/publish through `AppService`.
- Agent published/draft conversation list/detail/delete/clear compatibility.
- Agent chat JSON plus legacy single-frame SSE response shapes; draft Agent chat now uses draft configuration/conversations, and Agent/RAG chat responses expose local knowledge hits through Go-style search-list fields while keeping the public response envelopes unchanged.
- Workflow run returns raw schema-aware workflow output JSON, persists a local workflow run snapshot through AppService/MySQL, and workflow/chatflow upload returns the raw signed URL text, matching Go's direct `ctx.Writer.Write` / `ctx.String` behavior instead of the normal BFF response envelope.
- Chatflow conversation/message/chat loop persisted through AppService/MySQL with a BFF-local fallback.
- Model list via `ModelService`.
- Knowledge management/doc/export/hit routes proxy to `KnowledgeService`.
- MCP SSE/message/streamable compatibility now validates the Go-style `?key=` app key through `AppService.getAppKeyByKey`, requires `appType=mcpserver`, emits the initialized SSE frame, and supports development JSON-RPC `initialize`, `ping`, `tools/list`, plus deterministic `tools/call` results from the Java `McpService` MCP Server tool bindings.
- Protected non-OAuth/non-MCP routes enforce shared OpenAPI API-key auth support. Missing tokens, disabled API keys, expired API keys, and malformed non-Bearer Authorization headers are rejected before business handlers; successful real keys carry their user/org/api-key id into downstream AppService calls and statistics.
- Agent config, Knowledge create/hit, and Knowledge CRUD/doc/export OpenAPI routes enforce the Go router's fine-grained `AuthModelByUuid` and `AuthKnowledge` checks at BFF level. Model UUID fields are resolved through `ModelService.listModelIdsByUuids` and checked with Go-style scope rules; knowledge routes require `knowledgeId` and validate explicit user permission levels including system owner permission.
- OAuth login, authorize, token, refresh, discovery, and userinfo development flow backed by managed IAM OAuth apps. The flow validates `clientId`, `clientSecret`, `redirectUri`, one-time authorization codes, refresh tokens, and Bearer access tokens; code and refresh-token state persists through `OperateService` into `operate_service.operate_records` with a BFF-local fallback.
- OAuth login, authorize, token, refresh, and userinfo visits are recorded into OperateService client statistics, including persisted per-day browse counts, so the zero-change Operation statistics page can show active-client/browse data during development.
- API-key-style OpenAPI routes are recorded by `OpenApiUsageRecordFilter`, persisted through `AppService` into MySQL, and surfaced in the App Observability API Key statistics page with a BFF-local fallback.

This slice prevents public OpenAPI routes from returning 404 and gives API Key pages a runnable local target. Agent config/publish now reaches the Java app-service draft/version loop, Agent chat now supports published JSON, published single-frame SSE, draft single-frame SSE, and Go-style `search_list` from local configured knowledge hits, RAG chat now keeps public JSON/SSE compatibility while persisting local chat snapshots through AppService/MySQL, Chatflow OpenAPI now persists development conversation/message state through AppService/MySQL with a BFF-local fallback, Knowledge OpenAPI now reaches the same Docker MySQL-backed Java knowledge compatibility service used by the frontend, protected OpenAPI routes now enforce Go-style API-key presence/status/expiration semantics, fine-grained OpenAPI model/knowledge permission middleware is covered for the Go router fields, workflow run/upload follows Go's raw response semantics while workflow run snapshots persist through AppService/MySQL, MCP OpenAPI routes now enforce Go-style appKey query/appType validation and expose bound MCP Server tools through local JSON-RPC initialize/list/call responses, OAuth authorization-code runtime is stateful for development use with HS256 access tokens, RS256 ID tokens, JWKS, OperateService/MySQL-persisted code and refresh-token records, and Operation client-statistic visit recording, and API Key statistics now reflect persisted OpenAPI calls. Deep parity remains for true model inference, true token streaming, workflow engine execution, knowledge indexing, real remote MCP protocol execution/proxying, exact Go Redis implementation parity for OAuth runtime state, PEM-backed OAuth key configuration, Redis daily statistic synchronization, and exact runtime cost behavior.

## Verification

- `WanwuOpenApiControllerTest`
- `WanwuStatisticApiControllerTest#openApiCallsAreVisibleInApiKeyStatistics`
- Docker Maven targeted test: `openapi-targeted-tests-ok`
