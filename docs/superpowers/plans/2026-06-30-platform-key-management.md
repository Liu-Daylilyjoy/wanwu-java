# Platform Key Management Execution Plan

Date: 2026-06-30

## Slice

Complete the platform compatibility loop for frontend-visible app listing, OpenAPI keys, and app keys.

## Completed

- Added API DTOs for OpenAPI key and app key commands/results.
- Extended `AppService` with app list, API key, and app key methods.
- Added domain records, MyBatis entities, mappers, and repository methods.
- Added Flyway migration for `open_api_keys` and `api_keys`.
- Implemented `AppServiceImpl` business rules from the Go source.
- Added BFF routes for `/appspace/app/list`, `/api/key*`, and `/appspace/app/key*`.
- Switched Dubbo services to application-level instance registration to support growing service interfaces on Nacos.
- Added service and BFF tests before production implementation.

## Verification

Docker Maven command:

```powershell
docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -pl wanwu-service-bff,wanwu-service-app -am test
```

Result:

- Build success.
- BFF tests: 40 passed.
- App service tests: 29 passed.

Docker Compose runtime:

```powershell
docker compose --profile full config --quiet
docker compose --profile full build app bff iam
docker compose --profile full up -d --force-recreate --no-build iam app bff web
```

Result:

- `app`, `bff`, `iam`, `mysql`, `nacos` healthy.
- Frontend `/aibase/login` returned HTTP 200.
- BFF HTTP acceptance passed with `ACCEPTANCE_OK assistantId=assistant-8642d01f717d4add882ed3dc89ba6a04 apiKeyId=1 appApiId=1 frontend=200`.
- MySQL confirmed `open_api_keys`, `api_keys`, and Flyway `V6__create_api_keys.sql`.

## Next Slice

Move from platform key management to IAM/permission reproduction or model management, depending on frontend route pressure. The gap matrix currently makes IAM the next highest-leverage backend foundation.
