# Setting Service Reproduction

Date: 2026-07-03

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
- `wanwu-api` exposes the Go-equivalent `OperateService` custom system configuration methods:
  - `createSystemCustomTab`
  - `createSystemCustomLogin`
  - `createSystemCustomHome`
  - `getSystemCustom`
- `wanwu-service-operate` stores custom tab, login, and home config in `operate_service.operate_records` through Flyway/MyBatis and merges non-empty fields by mode, matching the Go `mergeCustomFields` behavior.
- `wanwu-service-bff` writes setting updates to `OperateService` and reads `/user/api/v1/base/custom` from `OperateService`, with the zero-change frontend response shape preserved.
- `POST /user/api/v1/avatar` now stores uploaded images in BFF-local development storage using Go-style `custom-upload/avatar/{prefix}/{id}.ext` keys and returns `/v1/cache/avatar/{key}` paths. `/user/api/v1/cache/avatar/**` serves both normal avatar cache paths and setting-page `custom/` cache paths, so the unchanged frontend `avatarSrc()` helper can preview uploaded platform assets.
- `OperateService` normalizes setting-page avatar maps to Go-style custom cache paths (`/v1/cache/avatar/custom/{key}`) on write and readback, including legacy Java download paths already stored in the compatibility repository.
- `docker-compose.yml` includes `wanwu-service-operate` in the `full` profile and makes BFF wait for it before becoming healthy.
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

- `mvn -q -pl wanwu-service-bff,wanwu-service-operate -am "-Dtest=WanwuFrontendApiControllerTest,OperateServiceImplTest" -DfailIfNoTests=false test`
- BFF contract tests: `WanwuFrontendApiControllerTest` covers all three setting write routes, `/base/custom`, and the admin login permission shape; `WanwuCommonApiControllerTest` covers Go-style avatar upload keys plus normal and custom cache avatar readback.
- Operate service test: `OperateServiceImplTest` covers setting config write/readback, custom avatar path normalization, mode isolation, and non-empty field merge behavior.

Frontend-entry smoke target:

- `http://localhost:3000/user/api/v1/base/login` returns `setting` for `admin`.
- `POST /custom/tab`, `/custom/login`, and `/custom/home` return `code: 0`.
- `GET /base/custom` returns the custom tab/login/home values written through the setting routes.
- `POST /avatar` returns `/v1/cache/avatar/...`; `GET /cache/avatar/...` and `GET /cache/avatar/custom/...` both serve the uploaded image from the Java development store.

## Current Boundary

This slice is a frontend-compatible platform setting loop. It prevents the zero-change frontend setting page from hiding the tab, receiving backend 404s, or showing broken local previews after saving uploaded platform assets.

It does not yet implement:

- MinIO/object-storage lifecycle parity for uploaded setting assets.
- Full Go light/dark theme seed config from YAML; Java accepts mode-specific records but only seeds the default development shell.
