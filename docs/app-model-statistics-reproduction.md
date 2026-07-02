# App and Model Statistics Reproduction

Date: 2026-07-02

## Go Source Reference

Original Go contracts:

- `proto/app-service/app-service.proto`
  - `RecordAppStatistic`, `GetAppStatistic`, `GetAppStatisticList`
  - `RecordModelStatistic`, `GetModelStatistic`, `GetModelStatisticList`
- `internal/app-service/client/model/app_statistic.go`
- `internal/app-service/client/model/model_statistic.go`
- `internal/app-service/client/orm/app_statistic.go`
- `internal/app-service/client/orm/model_statistic.go`
- `internal/app-service/client/orm/statistic_cron.go`

Go records runtime deltas into Redis hashes by day, then an hourly cron folds the latest 30 days into MySQL `app_statistic` and `model_statistic` tables. The Java reproduction writes the same daily aggregate dimensions directly to MySQL with atomic upsert. This keeps Docker/local runtime deterministic and avoids requiring the Redis cron before the dashboard can show real data.

## Java Implementation

New AppService DTOs:

- `RecordAppStatisticCommand`, `AppStatisticQuery`, `AppStatisticPageQuery`
- `AppStatisticResult`, `AppStatisticOverview`, `AppStatisticTrend`, `AppStatisticItem`, `AppStatisticListResult`
- `RecordModelStatisticCommand`, `ModelStatisticQuery`, `ModelStatisticPageQuery`
- `ModelStatisticResult`, `ModelStatisticOverview`, `ModelStatisticTrend`, `ModelStatisticItem`, `ModelStatisticListResult`
- generic chart DTOs: `StatisticChart`, `StatisticLine`, `StatisticPoint`, `StatisticOverviewItem`

New persistence:

- `app_statistics`
  - unique key: `(org_id, user_id, app_id, app_type, date)`
  - counters: call/failure, stream/non-stream, web/openapi/webURL sources
- `model_statistics`
  - unique key: `(org_id, user_id, model_id, provider, date)`
  - counters: calls/failures, stream/non-stream, prompt/completion/total tokens, first-token latency, non-stream costs

Runtime recording currently covers:

- frontend published assistant stream: `source=web`, `appType=agent`
- frontend published RAG chat: `source=web`, `appType=rag`
- frontend workflow run: `source=web`, `appType=workflow`
- frontend model experience LLM: model statistic with deterministic local token estimate
- OpenAPI agent chat: `source=openapi`, `appType=agent`
- OpenAPI RAG chat: `source=openapi`, `appType=rag`
- OpenAPI workflow run: `source=openapi`, `appType=workflow`

Draft app calls are intentionally not recorded, matching Go's `AppStatisticSourceDraft` comment that draft versions do not contribute to app statistics.

## Frontend Contract

`WanwuStatisticApiController` now reads persistent AppService statistics for:

- `GET /user/api/v1/statistic/app`
- `GET /user/api/v1/statistic/app/list`
- `GET /user/api/v1/statistic/model`
- `GET /user/api/v1/statistic/model/list`

Frontend field names remain unchanged. For example, model overview still returns `totalTokensTotal`, `promptTokensTotal`, `completionTokensTotal`, `callCountTotal`, and `callFailureTotal`.

## Remaining Gaps

- Go's Redis daily hash plus hourly cron has not been reproduced; Java currently writes MySQL directly.
- Runtime costs and token counts are development estimates for local deterministic responses, not provider-reported metrics.
- Public OpenURL `webURL` app statistic source is not wired yet.
- CSV endpoints still return minimal placeholder exports, not Go-equivalent Excel exports.

## Verification

Targeted tests:

```powershell
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-app -am -DfailIfNoTests=false -Dtest=AppServiceImplTest#appAndModelStatisticsPersistAggregates test
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -DfailIfNoTests=false -Dtest=WanwuStatisticApiControllerTest test
```
