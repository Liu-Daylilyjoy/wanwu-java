# AppService API Key Statistics Persistence

Date: 2026-07-01

## Go Source Baseline

The original Go backend records OpenAPI API Key usage in two layers:

- `internal/bff-service/server/http/middleware/api_key_record.go` captures API Key context, `METHOD-path`, status code, stream flag, elapsed time, request body, and response body.
- `proto/app-service/app-service.proto` exposes `RecordAPIKeyStatistic`, `GetAPIKeyStatistic`, `GetAPIKeyStatisticList`, and `GetAPIKeyStatisticRecord`.
- `internal/app-service/client/model/api_key_statistic.go` and `api_key_record.go` define daily aggregate and detail records.
- `internal/app-service/client/orm/api_key_statistic.go` combines Redis daily aggregation with MySQL detail storage.

## Java Reproduction

Java keeps the same BFF-to-AppService responsibility split, but uses direct MySQL writes for this slice:

- BFF `OpenApiUsageRecordFilter` records OpenAPI calls, captures JSON request bodies and non-stream JSON/text response bodies, and invokes `AppService.recordApiKeyStatistic`.
- AppService writes a detailed row to `api_key_records`.
- AppService upserts a daily aggregate row in `api_key_statistics`.
- The statistics dashboard reads AppService first for overview, trend, aggregate list, and detailed record endpoints.
- The previous BFF-local `OpenApiUsageMeter` remains as a fallback so OpenAPI responses are not affected if the statistic RPC is temporarily unavailable.

The direct MySQL aggregate write is intentional for Docker reproducibility. It preserves the frontend-visible behavior and persisted data model now, while leaving the Go Redis daily sync path as a later internal parity slice.

## Tables

`api_key_statistics` stores daily counters:

- `api_key_id`
- `method_path`
- `org_id`
- `user_id`
- `date`
- `total_call_count`
- `total_success_call_count`
- `total_fail_call_count`
- `total_stream_call_count`
- `total_stream_costs`
- `total_non_stream_call_count`
- `total_non_stream_costs`

`api_key_records` stores detailed records:

- `api_key_id`
- `method_path`
- `org_id`
- `user_id`
- `call_time`
- `request_body`
- `response_body`
- `status`
- `stream`
- `stream_costs`
- `non_stream_costs`

## Remaining Parity Work

- Replace direct aggregate writes with Redis daily aggregation and synchronization when Redis parity is introduced.
- Use real provider execution costs and true first-token stream costs instead of BFF request elapsed time for all runtime families.
- Export persisted API Key aggregate and record data as the original Excel format.
- Tighten API-key scoped authorization to match Go middleware exactly.
