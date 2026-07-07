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
- frontend `/workflow/api/api/workflow/use` compatibility run: `source=web`, `appType=workflow`
- frontend model experience LLM: model statistic with provider `usage` when available, otherwise deterministic local token estimate
- frontend assistant stream with configured model: model statistic with provider `usage` when the OpenAI-compatible stream reports it, otherwise deterministic local token estimate
- frontend RAG chat with configured model: model statistic with provider `usage` when the OpenAI-compatible stream reports it, otherwise deterministic local token estimate
- OpenAPI agent chat: `source=openapi`, `appType=agent`
- OpenAPI agent chat with configured model: model statistic with provider `usage` when the OpenAI-compatible stream reports it, otherwise deterministic local token estimate
- OpenAPI RAG chat: `source=openapi`, `appType=rag`
- OpenAPI RAG chat with configured model: model statistic with provider `usage` when the OpenAI-compatible stream reports it, otherwise deterministic local token estimate
- OpenAPI workflow run: `source=openapi`, `appType=workflow`
- OpenAPI Chatflow chat: `source=openapi`, `appType=chatflow`, including service-layer validation failures as failed stream calls
- public OpenURL assistant stream: `source=webURL`, `appType=agent`
- public OpenURL assistant stream with configured model: model statistic with provider `usage` when the OpenAI-compatible stream reports it, otherwise deterministic local token estimate

Frontend Assistant/RAG configured-model calls now use the same `OpenAiCompatibleChatClient` as OpenAPI and OpenURL, so stream aggregation, non-stream fallback, provider `usage` parsing, and local token estimation share one implementation. The model experience endpoint keeps its separate proxy because it must preserve dialog history and enabled inference parameters in the upstream request body.

Draft app calls are intentionally not recorded, matching Go's `AppStatisticSourceDraft` comment that draft versions do not contribute to app statistics.

## Frontend Contract

`WanwuStatisticApiController` now reads persistent AppService statistics for:

- `GET /user/api/v1/statistic/app`
- `GET /user/api/v1/statistic/app/list`
- `GET /user/api/v1/statistic/app/export`
- `GET /user/api/v1/statistic/model`
- `GET /user/api/v1/statistic/model/list`
- `GET /user/api/v1/statistic/model/export`

Frontend field names remain unchanged. For example, model overview still returns `totalTokensTotal`, `promptTokensTotal`, `completionTokensTotal`, `callCountTotal`, and `callFailureTotal`.

The export endpoints now emit xlsx workbooks from the same persisted aggregate rows used by the dashboard list endpoints. The Java reproduction uses a small BFF-local OOXML writer so the frontend receives Go-equivalent downloadable workbooks without introducing a new Excel dependency.

## Remaining Gaps

- Go's Redis daily hash plus hourly cron has not been reproduced; Java currently writes MySQL directly.
- Runtime costs are still development estimates; token counts use provider-reported `usage` for supported OpenAI-compatible model experience, frontend/OpenAPI/OpenURL assistant stream, frontend/OpenAPI RAG chat, and callback paths, with local estimates for deterministic fallbacks.
- The xlsx writer intentionally covers the simple single-sheet statistics export surface only; richer Excel styling can be added later if the frontend starts depending on it.

## Verification

Targeted tests:

```powershell
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-app -am -DfailIfNoTests=false -Dtest=AppServiceImplTest#appAndModelStatisticsPersistAggregates test
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -DfailIfNoTests=false -Dtest=WanwuStatisticApiControllerTest test
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -DfailIfNoTests=false -Dtest=WanwuOpenUrlApiControllerTest test
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am "-Dtest=WanwuOpenApiControllerTest#chatflowOpenApiChatRecordsFailureStatisticWhenServiceRejectsRequest" -DfailIfNoTests=false test
```
