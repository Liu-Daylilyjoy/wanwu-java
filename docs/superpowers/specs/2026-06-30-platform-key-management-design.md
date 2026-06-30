# Platform Key Management Design

Date: 2026-06-30

## Goal

Reproduce the first platform compatibility slice from the Go backend so the unchanged frontend can manage:

- `/user/api/v1/appspace/app/list`
- `/user/api/v1/api/key`
- `/user/api/v1/api/key/list`
- `/user/api/v1/api/key/status`
- `/user/api/v1/appspace/app/key`
- `/user/api/v1/appspace/app/key/list`

## Go Source Baseline

The Go BFF exposes API key and app key routes under `internal/bff-service/server/http/handler/v1`.

Observed request and response fields:

- OpenAPI key create/update/delete/status use `keyId`, `name`, `desc`, `expiredAt`, `status`.
- OpenAPI key list returns `keyId`, `key`, `creator`, `name`, `desc`, `expiredAt`, `createdAt`, `status`.
- App key create/list/delete use `appId`, `appType`, `apiId`.
- App key list returns `apiId`, `apiKey`, `createdAt`.

Observed persistence model:

- OpenAPI keys map to Go `OpenApiKey` and table `open_api_keys`.
- App keys map to Go `ApiKey` and table `api_keys`.
- Both are scoped by `user_id` and `org_id`.

## Java Design

The Java slice follows the existing app service pattern:

- DTOs live in `wanwu-api`.
- Domain records and repository contracts live in `wanwu-service-app/domain`.
- MyBatis Plus entities and mapper methods live in `wanwu-service-app/persistence`.
- BFF routes only map frontend fields and user context, then call `AppService`.

Dubbo registration is configured with `dubbo.application.register-mode=instance` across services. This keeps service discovery application-oriented and avoids Nacos rejecting oversized interface metadata as `AppService` grows during the full backend reproduction.

Service rules:

- API key names are unique per `userId + orgId`.
- API key `expiredAt` uses date-only format `yyyy-MM-dd`; blank means permanent.
- API key create defaults `status` to true.
- API key update/delete/status check ownership before mutation.
- App key create mirrors the Go ORM behavior and does not require app existence validation.
- Generated keys are 32-character UUID-derived opaque tokens and checked for collisions across both key tables.

## Database

Flyway migration `V6__create_api_keys.sql` creates:

- `open_api_keys`
- `api_keys`

The tables keep the Go column names so later OpenAPI/callback compatibility can reuse them directly.

## Test Coverage

Added service tests:

- Generic appspace app list returns agent cards.
- API key create/list/update/status/delete lifecycle.
- App key create/list/delete lifecycle.

Added BFF tests:

- `/appspace/app/list` maps to `AppService.listApplications`.
- `/api/key*` routes preserve frontend request and response shape.
- `/appspace/app/key*` routes preserve frontend request and response shape.

## Docker Acceptance

Runtime acceptance on Docker Compose full profile passed:

- `app`, `bff`, `iam`, `mysql`, and `nacos` containers were healthy.
- `web` served `/aibase/login` with HTTP 200.
- HTTP flow passed through BFF for app list, API key create/list/update/status/delete, and app key create/list/delete.
- MySQL contained `open_api_keys` and `api_keys`.
- Flyway history included version `6 create api keys`.
