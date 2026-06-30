# IAM Permission Management Reproduction

Date: 2026-06-30

## Go Source Baseline

Original Go file inspected:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\permission.go`

The Go BFF exposes three permission-management route families:

- `permission.user`: user create, batch import, update, delete, list, status, role select, users outside org, add org user, and admin password reset.
- `permission.org`: organization create, update, delete, info, list, and status.
- `permission.role`: role template, create, update, delete, info, list, and status.

## Java Coverage Added

API contract:

- `wanwu-api/src/main/java/com/unicomai/wanwu/api/iam/IamService.java`

IAM implementation:

- `wanwu-service-iam/src/main/java/com/unicomai/wanwu/service/iam/rpc/IamServiceImpl.java`

BFF routes:

- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiController.java`

Tests:

- `wanwu-service-iam/src/test/java/com/unicomai/wanwu/service/iam/rpc/IamServiceImplTest.java`
- `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiControllerTest.java`

Covered routes:

- `POST /user/api/v1/user`
- `POST /user/api/v1/user/batch`
- `PUT /user/api/v1/user`
- `DELETE /user/api/v1/user`
- `GET /user/api/v1/user/list`
- `PUT /user/api/v1/user/status`
- `GET /user/api/v1/org/other/select`
- `GET /user/api/v1/role/select`
- `POST /user/api/v1/org/user`
- `POST /user/api/v1/org`
- `PUT /user/api/v1/org`
- `DELETE /user/api/v1/org`
- `GET /user/api/v1/org/info`
- `GET /user/api/v1/org/list`
- `PUT /user/api/v1/org/status`
- `GET /user/api/v1/role/template`
- `POST /user/api/v1/role`
- `PUT /user/api/v1/role`
- `DELETE /user/api/v1/role`
- `GET /user/api/v1/role/info`
- `GET /user/api/v1/role/list`
- `PUT /user/api/v1/role/status`

`PUT /user/api/v1/user/admin/password` was already covered by the common user compatibility controller and is intentionally not duplicated to avoid Spring mapping conflicts.

## Current Contract

This slice provides a Docker development IAM repository. After the MySQL persistence slice, mutable records are also stored in the `iam_service.iam_records` compatibility table:

- Created users, roles, and organizations are visible through later list/detail calls and survive Docker service restarts.
- Status changes affect later list/detail payloads.
- Deletion removes non-built-in development records.
- The built-in `admin` and `app` accounts remain protected seed records.
- Role permissions are normalized from either string arrays or frontend route/permission maps.
- `user/batch` accepts the frontend multipart upload route and records a deterministic imported development user.

## Remaining Gaps

- Normalized Go-equivalent IAM relational tables are still missing; the current durable boundary is a JSON compatibility table.
- Real password hashing, reset rules, and login policy are not reproduced.
- Real Excel parsing and per-row import errors are not reproduced.
- Hierarchical organization constraints and cross-org permission checks are minimal.
- Audit logs, invitation workflow, and notification side effects are not reproduced.
