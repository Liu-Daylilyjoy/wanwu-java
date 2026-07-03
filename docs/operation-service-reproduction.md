# Operation Management Reproduction

Date: 2026-06-30

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
- Go IAM proto boundary: `proto\iam-service\iam-service.proto` OAuth RPCs.
- Go statistic client handler/service/response:
  - `internal\bff-service\server\http\handler\v1\statistic_client.go`
  - `internal\bff-service\service\statistic_client.go`
  - `internal\bff-service\model\response\statistic.go`
- Go `v1/statistic_client.go` router registration is currently commented out, but the frontend still calls `/statistic/client`.

## Covered Java Behavior

- `wanwu-service-bff` exposes the original OAuth management routes:
  - `POST /user/api/v1/oauth/app`
  - `GET /user/api/v1/oauth/app/list`
  - `PUT /user/api/v1/oauth/app`
  - `PUT /user/api/v1/oauth/app/status`
  - `DELETE /user/api/v1/oauth/app`
- `wanwu-service-bff` exposes `GET /user/api/v1/statistic/client` with the frontend-required `overview` and `trend` response shape.
- `wanwu-service-bff` exposes a development OAuth authorization-code runtime under `/service/api/openapi/v1/oauth/*`:
  - `/oauth/login` redirects to the zero-change frontend OAuth confirmation route.
  - `/oauth/code/authorize` validates the managed OAuth app, development user token, redirect URI, and emits a one-time code through a 302 callback.
  - `/oauth/code/token` validates `clientSecret` and exchanges the code for HS256 access tokens, RS256 ID tokens, and refresh tokens.
  - `/oauth/code/token/refresh` rotates refresh/access tokens.
  - `/oauth/userinfo` resolves a Bearer access token to the development user profile.
  - `/oauth/jwks` exposes the generated RSA public key used by the development ID token signer.
- `wanwu-api` extends `IamService` with OAuth app management operations.
- `wanwu-service-iam` stores OAuth apps in Docker development in-memory state with generated `clientId` and `clientSecret`.
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
- OAuth authorize/code/token/refresh/userinfo works through `/service/api/openapi/v1/oauth/*` using the managed app credentials.
- `/statistic/client` returns `overview.cumulativeClient`, `overview.additionClient`, `overview.activeClient`, `overview.browse`, and `trend.client`/`trend.browse`.

## Current Boundary

This slice is a frontend-compatible Operation Management loop. It prevents the zero-change frontend operation page from being hidden or receiving backend 404s.

It does not yet implement:

- MySQL persistence for OAuth apps.
- Redis-backed code/refresh-token storage.
- PEM-backed OAuth key configuration instead of the generated development RSA key.
- Real client installation statistics in OperateService.
- Redis-backed global browse statistics.
- Full App Observability dashboard routes under `v1/statistic.go`.
