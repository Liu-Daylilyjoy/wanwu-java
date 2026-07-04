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
- Agent chat and RAG chat non-stream response shapes.
- Workflow run plus workflow/chatflow upload response shapes.
- Chatflow conversation/message/chat loop persisted through AppService/MySQL with a BFF-local fallback.
- Model list via `ModelService`.
- Knowledge management/doc/export/hit routes proxy to `KnowledgeService`.
- MCP SSE/message/streamable compatibility shells now validate the Go-style `?key=` app key through `AppService.getAppKeyByKey`, require `appType=mcpserver`, and bind the resolved MCP server id into the development shell response.
- Protected non-OAuth/non-MCP routes enforce shared OpenAPI API-key auth support. Missing tokens, disabled API keys, expired API keys, and malformed non-Bearer Authorization headers are rejected before business handlers; successful real keys carry their user/org/api-key id into downstream AppService calls and statistics.
- OAuth login, authorize, token, refresh, discovery, and userinfo development flow backed by managed IAM OAuth apps. The flow validates `clientId`, `clientSecret`, `redirectUri`, one-time authorization codes, refresh tokens, and Bearer access tokens; code and refresh-token state persists through `OperateService` into `operate_service.operate_records` with a BFF-local fallback.
- OAuth login, authorize, token, refresh, and userinfo visits are recorded into OperateService client statistics, with BFF-local browse compatibility, so the zero-change Operation statistics page can show active-client/browse data during development.
- API-key-style OpenAPI routes are recorded by `OpenApiUsageRecordFilter`, persisted through `AppService` into MySQL, and surfaced in the App Observability API Key statistics page with a BFF-local fallback.

This slice prevents public OpenAPI routes from returning 404 and gives API Key pages a runnable local target. Agent config/publish now reaches the Java app-service draft/version loop, Chatflow OpenAPI now persists development conversation/message state through AppService/MySQL with a BFF-local fallback, Knowledge OpenAPI now reaches the same Docker MySQL-backed Java knowledge compatibility service used by the frontend, protected OpenAPI routes now enforce Go-style API-key presence/status/expiration semantics, MCP OpenAPI routes now enforce Go-style appKey query/appType validation, OAuth authorization-code runtime is stateful for development use with HS256 access tokens, RS256 ID tokens, JWKS, OperateService/MySQL-persisted code and refresh-token records, and Operation client-statistic visit recording, and API Key statistics now reflect persisted OpenAPI calls. Deep parity remains for true model inference, workflow engine execution, knowledge indexing, real MCP protocol runtime, fine-grained Go AuthKnowledge/AuthModel permission parity, exact Go Redis implementation parity for OAuth runtime state, PEM-backed OAuth key configuration, Redis daily statistic synchronization, and exact runtime cost behavior.

## Verification

- `WanwuOpenApiControllerTest`
- `WanwuStatisticApiControllerTest#openApiCallsAreVisibleInApiKeyStatistics`
- Docker Maven targeted test: `openapi-targeted-tests-ok`
