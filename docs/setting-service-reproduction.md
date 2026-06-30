# Setting Service Reproduction

Date: 2026-06-30

## Original Go Mapping

- Frontend caller: `web/src/api/setInfo.js` posts to `/user/api/v1/custom/{tab|login|home}`.
- Frontend page: `web/src/views/infoSetting/index.vue` writes platform custom settings and reads them from `/base/custom`.
- Go BFF router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\setting.go`.
- Go BFF handler and request contracts:
  - `internal\bff-service\server\http\handler\v1\setting.go`
  - `internal\bff-service\model\request\setting.go`
- Go service boundary: `internal\bff-service\service\setting.go` delegates to `OperateService`.
- Go Operate persistence boundary:
  - `proto\operate-service\operate-service.proto`
  - `internal\operate-service\server\grpc\operate\system_custom.go`
  - `internal\operate-service\client\orm\system_custom.go`

## Covered Java Behavior

- `wanwu-service-bff` exposes the three original frontend write routes:
  - `POST /user/api/v1/custom/tab`
  - `POST /user/api/v1/custom/login`
  - `POST /user/api/v1/custom/home`
- `wanwu-api` extends `IamService` with development custom setting mutations.
- `wanwu-service-iam` stores custom tab, login, and home config in the Docker development in-memory state and merges it into `platformConfig()`, which backs `/user/api/v1/base/custom` and login custom data.
- The admin development account now exposes the `setting` permission so the zero-change frontend can display the platform setting tab inside `web/src/views/permission/index.vue`.

## Field Mapping

| Frontend/Go request | Java readback field |
| --- | --- |
| `tabLogo` | `custom.tab.logo` |
| `tabTitle` | `custom.tab.title` |
| `loginBg` | `custom.login.background` |
| `loginLogo` | `custom.login.logo` |
| `loginWelcomeText` | `custom.login.welcomeText` |
| `loginButtonColor` | `custom.login.loginButtonColor` |
| `homeLogo` | `custom.home.logo` |
| `homeName` | `custom.home.title` |
| `homeBgColor` | `custom.home.backgroundColor` |

## Verification

Executed in Docker with Java 8:

- `mvn -q -pl wanwu-service-bff,wanwu-service-iam -am "-Dtest=WanwuFrontendApiControllerTest,IamServiceImplTest" -DfailIfNoTests=false test`
- BFF contract test: `WanwuFrontendApiControllerTest` covers all three setting write routes and the admin login permission shape.
- IAM service test: `IamServiceImplTest` covers setting permission exposure and custom config write/readback.

Frontend-entry smoke target:

- `http://localhost:3000/user/api/v1/base/login` returns `setting` for `admin`.
- `POST /custom/tab`, `/custom/login`, and `/custom/home` return `code: 0`.
- `GET /base/custom` returns the custom tab/login/home values written through the setting routes.

## Current Boundary

This slice is a frontend-compatible platform setting loop. It prevents the zero-change frontend setting page from hiding the tab or receiving backend 404s when saving platform custom settings.

It does not yet implement:

- The independent Java `OperateService` RPC contract.
- MySQL persistence for system custom config.
- Uploaded asset storage or URL/path resolution beyond preserving the avatar maps sent by the frontend.
- Mode-specific custom config beyond the single Docker development default mode.
