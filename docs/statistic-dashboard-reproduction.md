# Statistic Dashboard Reproduction

Date: 2026-07-01

## Original Go Surface

The Go BFF registers the app observability dashboard under `v1/statistic.go` with the permission `app_observability.statistic`.

Covered routes in this Java slice:

- `GET /user/api/v1/statistic/app/select`
- `GET /user/api/v1/statistic/app`
- `GET /user/api/v1/statistic/app/list`
- `GET /user/api/v1/statistic/app/export`
- `GET /user/api/v1/statistic/model`
- `GET /user/api/v1/statistic/model/list`
- `GET /user/api/v1/statistic/model/export`
- `GET /user/api/v1/statistic/api/select`
- `GET /user/api/v1/statistic/api/routes`
- `POST /user/api/v1/statistic/api`
- `POST /user/api/v1/statistic/api/list`
- `POST /user/api/v1/statistic/api/record`
- `POST /user/api/v1/statistic/api/list/export`
- `POST /user/api/v1/statistic/api/record/export`

## Java Design

`WanwuStatisticApiController` is a BFF compatibility controller. It keeps the frontend contract stable while reproducing the Go statistic dashboard in Java.

- App select/list data is sourced from `AppService.listApplications`.
- Model list data is sourced from `ModelService.listModels`.
- API Key select/list data is sourced from `AppService.listApiKeys`.
- API Key overview, trend, aggregate list, detailed records, and exports now prefer AppService/MySQL runtime statistics written by `OpenApiUsageRecordFilter`.
- App/model overview, trend, aggregate lists, and exports now prefer AppService/MySQL runtime statistics written by frontend, OpenAPI, and OpenURL runtime paths.
- `OpenApiUsageMeter` remains as a BFF-local fallback if the AppService statistic path is temporarily unavailable.
- Export routes return single-sheet xlsx workbooks compatible with the Go excelize export contract.

The admin development account now exposes:

- `app_observability`
- `app_observability.statistic`

The app-only account stays restricted to application-development permissions.

## Verification

Docker Maven targeted tests:

```powershell
docker run --rm -v ${env:USERPROFILE}\.m2:/root/.m2 -v ${PWD}:/workspace -w /workspace maven:3.9.9-eclipse-temurin-8 /bin/sh -lc 'mvn -q -pl wanwu-service-bff,wanwu-service-iam -am "-Dtest=WanwuFrontendApiControllerTest,IamServiceImplTest" -DfailIfNoTests=false test'
```

The controller is also included in the Docker Compose BFF smoke path for:

- Login permissions include `app_observability.statistic`.
- `/statistic/app`, `/statistic/model`, and `/statistic/api` return overview/trend contracts.
- App/model/API lists return frontend-compatible `list/total/pageNo/pageSize`.
- OpenAPI API Key statistics record runtime calls through `OpenApiUsageRecordFilter`; see `docs/openapi-api-key-statistics-reproduction.md`.
- App/model statistics record frontend, OpenAPI, and OpenURL runtime calls through AppService/MySQL; see `docs/app-model-statistics-reproduction.md`.

## Remaining Work

- Reproduce the Go Redis daily hash plus cron/statistics synchronization path.
- Replace deterministic local token/cost estimates with exact provider-reported runtime metrics.
- Replace the direct MySQL API Key aggregate write with the Go-equivalent Redis daily aggregation plus synchronization job when Redis parity is introduced.
