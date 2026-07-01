# OpenAPI API Key Statistics Reproduction

Date: 2026-07-01

## Go Source Evidence

- Go OpenAPI routes in `internal/bff-service/server/http/handler/router/openapi/router.go` attach `middleware.APIKeyRecord(...)` after `AuthOpenAPIKey()` for Agent, RAG, Workflow, Chatflow, Model, and Knowledge OpenAPI routes.
- `internal/bff-service/server/http/middleware/api_key_record.go` records `apiKeyId`, `METHOD-path`, HTTP status, stream/non-stream flag, elapsed costs, JSON request body, and non-stream response body.
- `proto/app-service/app-service.proto` defines `GetAPIKeyStatistic`, `GetAPIKeyStatisticList`, `GetAPIKeyStatisticRecord`, and `RecordAPIKeyStatistic`.
- Go AppService stores daily aggregate stats plus detailed records in `internal/app-service/client/orm/api_key_statistic.go`.

## Java Reproduction

- `OpenApiUsageRecordFilter` now records Java OpenAPI calls under `/service/api/openapi/v1/**`.
- The filter mirrors Go's route boundary:
  - Records API-key-style OpenAPI routes.
  - Skips MCP and OAuth public runtime routes, which are not wired through Go `APIKeyRecord`.
  - Uses `METHOD-/service/api/openapi/v1/...` as `methodPath`.
  - Treats `/chatflow/chat` as stream and detects `stream=true` from JSON request bodies for request-driven routes.
- `OpenApiUsageMeter` keeps a BFF-local rolling record buffer and aggregates by `userId`, `orgId`, `apiKeyId`, `methodPath`, and date.
- `WanwuStatisticApiController` now uses that meter for:
  - `POST /user/api/v1/statistic/api`
  - `POST /user/api/v1/statistic/api/list`
  - `POST /user/api/v1/statistic/api/record`
  - `GET /user/api/v1/statistic/api/routes`

## Verification

- `WanwuStatisticApiControllerTest#openApiCallsAreVisibleInApiKeyStatistics` verifies that one OpenAPI RAG stream call appears in API Key overview, aggregate list, and detailed record responses.

## Remaining Gap

This is the runtime compatibility loop needed by the zero-change frontend. Full Go parity still needs AppService RPC methods for API Key statistics, Redis daily aggregation, MySQL persistence for aggregate/detail tables, export from persisted data, and exact authorization middleware behavior.
