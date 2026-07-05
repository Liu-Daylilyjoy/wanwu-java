# Operation Management Reproduction

Date: 2026-07-05

## Original Go Mapping

- Frontend callers:
  - `web/src/api/permission/oauth.js`
  - `web/src/api/permission/statistic.js`
  - `web/src/views/operation/index.vue`
  - `web/src/views/permission/oauth/index.vue`
  - `web/src/views/permission/statistics/index.vue`
- Go BFF OAuth router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\oauth.go`.
- Go BFF OAuth handler/service/request/response:
  - `internal\bff-service\server\http\handler\v1\oauth.go`
  - `internal\bff-service\service\oauth.go`
  - `internal\bff-service\model\request\oauth.go`
  - `internal\bff-service\model\response\oauth.go`
- Go OpenAPI OAuth runtime state:
  - `internal\bff-service\pkg\oauth2-util\code.go`
  - `internal\bff-service\pkg\oauth2-util\refresh_token.go`
  - The Go implementation stores authorization codes and refresh tokens in Redis and consumes them on exchange/rotation.
- Go IAM proto boundary: `proto\iam-service\iam-service.proto` OAuth RPCs.
- Go statistic client handler/service/response:
  - `internal\bff-service\server\http\handler\v1\statistic_client.go`
  - `internal\bff-service\service\statistic_client.go`
  - `internal\bff-service\model\response\statistic.go`
- Go `v1/statistic_client.go` router registration is currently commented out, but the frontend still calls `/statistic/client`.
- Go Operate custom system configuration boundary is covered separately by the setting page:
  - `proto\operate-service\operate-service.proto`
  - `internal\operate-service\server\grpc\operate\system_custom.go`

## Covered Java Behavior

- `wanwu-service-bff` exposes the original OAuth management routes:
  - `POST /user/api/v1/oauth/app`
  - `GET /user/api/v1/oauth/app/list`
  - `PUT /user/api/v1/oauth/app`
  - `PUT /user/api/v1/oauth/app/status`
  - `DELETE /user/api/v1/oauth/app`
- `wanwu-service-bff` exposes `GET /user/api/v1/statistic/client` with the frontend-required `overview` and `trend` response shape. The Java development slice now prefers `OperateService.getClientStatistic` for cumulative/new/active client data and daily browse statistics, with the older BFF-local counter retained only as an availability fallback.
- `wanwu-service-operate` is now a real Docker service in the `full` profile and provides the OperateService custom system configuration RPCs used by the platform setting page.
- `wanwu-service-operate` also implements `addClientRecord` and `getClientStatistic` with `operate_service.operate_records` snapshot persistence. OpenAPI OAuth login/authorize/token/refresh/userinfo visits synchronously record the managed OAuth client into OperateService, increment persisted visit counts and per-day browse totals, and use a BFF-local fallback when the provider is unavailable.
- `wanwu-service-operate` persists OpenAPI OAuth authorization codes and refresh tokens into `operate_service.operate_records`. The Java BFF consumes those records once through OperateService, keeps the Go-style short authorization-code window and 7-day refresh-token window, and falls back to local memory only if the provider is unavailable.
- `wanwu-service-bff` exposes a development OAuth authorization-code runtime under `/service/api/openapi/v1/oauth/*`:
  - `/oauth/login` redirects to the zero-change frontend OAuth confirmation route.
  - `/oauth/code/authorize` validates the managed OAuth app, development user token, redirect URI, and emits a one-time code through a 302 callback.
  - `/oauth/code/token` validates `clientSecret` and exchanges the code for HS256 access tokens, RS256 ID tokens, and refresh tokens.
  - `/oauth/code/token/refresh` rotates refresh/access tokens.
  - `/oauth/userinfo` resolves a Bearer access token to the development user profile.
  - `/oauth/jwks` exposes the generated RSA public key used by the development ID token signer.
- `wanwu-api` extends `IamService` with OAuth app management operations.
- `wanwu-service-iam` stores OAuth apps in the Docker development MySQL JSON compatibility repository with generated `clientId` and `clientSecret`.
- The admin development account now exposes `operation`, `operation.oauth`, and `operation.statistic_client` so the zero-change frontend can display Operation Management.

## OAuth Field Mapping

| Frontend/Go field | Java field |
| --- | --- |
| `clientId` | `clientId` |
| `name` | `name` |
| `desc` | `desc` |
| `redirectUri` | `redirectUri` |
| `clientSecret` | `clientSecret` |
| `status` | `status` |

## Verification

Executed in Docker with Java 8:

- `mvn -q -pl wanwu-service-bff,wanwu-service-iam -am "-Dtest=WanwuFrontendApiControllerTest,IamServiceImplTest" -DfailIfNoTests=false test`
- BFF contract test: `WanwuFrontendApiControllerTest` covers OAuth management routes, operation permissions, and `/statistic/client`.
- IAM service test: `IamServiceImplTest` covers operation permissions and OAuth app create/list/update/status/delete.

Frontend-entry smoke target:

- `http://localhost:3000/user/api/v1/base/login` returns operation permissions for `admin`.
- OAuth app create/list/update/status/delete works through `/oauth/app*`.
- OAuth authorize/code/token/refresh/userinfo works through `/service/api/openapi/v1/oauth/*` using the managed app credentials, with code and refresh-token state surviving BFF restarts when OperateService/MySQL is available.
- `/statistic/client` returns `overview.cumulativeClient`, `overview.additionClient`, `overview.activeClient`, `overview.browse`, and `trend.client`/`trend.browse`; the client values are non-zero when OAuth clients have visited the development OAuth runtime, and browse increases with repeated OAuth runtime visits in the requested date range.

## Current Boundary

This slice is a frontend-compatible Operation Management loop. It prevents the zero-change frontend operation page from being hidden or receiving backend 404s.

It does not yet implement:

- Exact Go Redis implementation parity for code/refresh-token storage; Java development state is persisted in `operate_service.operate_records` instead.
- PEM-backed OAuth key configuration instead of the generated development RSA key.
- Exact Go `client_records` / `client_daily_records` normalized table parity and Redis-backed daily aggregation jobs.
- Exact Redis-backed global browse aggregation beyond the Java persisted daily visit counters.
- Full App Observability dashboard routes under `v1/statistic.go`.
