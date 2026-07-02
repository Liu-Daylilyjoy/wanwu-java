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
- The filter calls `AppService.recordApiKeyStatistic` so runtime calls are persisted in MySQL:
  - `api_key_records` stores detailed request/response records.
  - `api_key_statistics` stores daily aggregate counters by `userId`, `orgId`, `apiKeyId`, `methodPath`, and date.
  - The aggregate write uses a MySQL upsert so repeated calls increment totals atomically.
- `OpenApiUsageMeter` still keeps a BFF-local rolling record buffer as a runtime fallback.
- `WanwuStatisticApiController` now prefers AppService/MySQL statistics and falls back to the local meter for:
  - `POST /user/api/v1/statistic/api`
  - `POST /user/api/v1/statistic/api/list`
  - `POST /user/api/v1/statistic/api/record`
  - `GET /user/api/v1/statistic/api/routes`

## Verification

- `WanwuStatisticApiControllerTest#openApiCallsAreVisibleInApiKeyStatistics` verifies that one OpenAPI RAG stream call appears in API Key overview, aggregate list, and detailed record responses.
- `AppServiceImplTest#apiKeyStatisticPersistsAggregatesAndRecords` verifies current/previous period aggregation, trend output, stream/non-stream costs, and detailed record data.
- `WanwuStatisticApiControllerTest#apiKeyStatisticsUsePersistentAppServiceWhenAvailable` verifies that the BFF reads AppService statistics before using the fallback meter.

## Remaining Gap

This is the runtime compatibility loop needed by the zero-change frontend. Full Go parity still needs the Redis daily aggregation and cron synchronization path, export from persisted data, real runtime cost values from provider execution, and exact authorization middleware behavior.
