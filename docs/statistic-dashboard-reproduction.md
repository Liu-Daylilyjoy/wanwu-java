# Statistic Dashboard Reproduction

Date: 2026-06-30

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

`WanwuStatisticApiController` is a BFF compatibility controller. It keeps the frontend contract stable while real Operate/App statistics aggregation is still being reproduced.

- App select/list data is sourced from `AppService.listApplications`.
- Model list data is sourced from `ModelService.listModels`.
- API Key select/list data is sourced from `AppService.listApiKeys`.
- Overview and trend metrics are zero-valued development data with the same field names the Vue dashboard reads.
- Export routes return CSV bytes so export buttons no longer hit 404. They are intentionally not a full Excel reproduction yet.

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

## Remaining Work

- Persist and aggregate real model/app/API usage records.
- Reproduce the Go cron/statistics synchronization path.
- Replace CSV compatibility exports with true Excel exports.
- Connect statistics to OpenAPI/runtime invocation paths instead of deterministic zero data.
