# OpenAPI Reproduction Notes

Date: 2026-06-30

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

- Agent create/delete/list/info/config/publish compatibility.
- Agent published/draft conversation list/detail/delete/clear compatibility.
- Agent chat and RAG chat non-stream response shapes.
- Workflow run plus workflow/chatflow upload response shapes.
- Chatflow conversation/message/chat compatibility shells.
- Model list via `ModelService`.
- Knowledge management/doc/export/hit compatibility shells.
- MCP SSE/message/streamable compatibility shells.
- OAuth JWKS, login, authorize, token, refresh, discovery, and userinfo development shells.

This slice prevents public OpenAPI routes from returning 404 and gives API Key pages a runnable local target. Deep parity remains for true model inference, workflow engine execution, knowledge indexing, MCP protocol runtime, OAuth signing, and API usage metric persistence.

## Verification

- `WanwuOpenApiControllerTest`
- Docker Maven targeted test: `openapi-targeted-tests-ok`
