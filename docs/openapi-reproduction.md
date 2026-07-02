# OpenAPI Reproduction Notes

Date: 2026-07-01

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

Go authenticates most routes with API Key middleware and stores user/org/api-key context on the request. The Java reproduction mirrors this at BFF level by accepting `Authorization: Bearer`, `X-API-Key`, or `Api-Key`. Docker development tokens `dev-token` and `dev-token-app` map to the known development accounts; other keys are resolved through `AppService.getApiKeyByKey`.

## Java Coverage

Implemented in `WanwuOpenApiController` under `/service/api/openapi/v1`:

- Agent create/delete/list/info plus config/publish through `AppService`.
- Agent published/draft conversation list/detail/delete/clear compatibility.
- Agent chat and RAG chat non-stream response shapes.
- Workflow run plus workflow/chatflow upload response shapes.
- Chatflow conversation/message/chat local session loop.
- Model list via `ModelService`.
- Knowledge management/doc/export/hit routes proxy to `KnowledgeService`.
- MCP SSE/message/streamable compatibility shells.
- OAuth JWKS, login, authorize, token, refresh, discovery, and userinfo development shells.
- API-key-style OpenAPI routes are recorded by `OpenApiUsageRecordFilter`, persisted through `AppService` into MySQL, and surfaced in the App Observability API Key statistics page with a BFF-local fallback.

This slice prevents public OpenAPI routes from returning 404 and gives API Key pages a runnable local target. Agent config/publish now reaches the Java app-service draft/version loop, Chatflow OpenAPI now keeps a local conversation/message loop, Knowledge OpenAPI now reaches the same Docker MySQL-backed Java knowledge compatibility service used by the frontend, and API Key statistics now reflect persisted OpenAPI calls. Deep parity remains for true model inference, workflow engine execution, knowledge indexing, MCP protocol runtime, OAuth signing, Redis daily statistic synchronization, and exact API-key authorization behavior.

## Verification

- `WanwuOpenApiControllerTest`
- `WanwuStatisticApiControllerTest#openApiCallsAreVisibleInApiKeyStatistics`
- Docker Maven targeted test: `openapi-targeted-tests-ok`
