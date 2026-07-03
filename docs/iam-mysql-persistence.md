# IAM MySQL Persistence Slice

Date: 2026-07-03

## Original Go Integration

Original files inspected:

- `D:\work\week3\wanwu\docker-compose.yaml`
- `D:\work\week3\wanwu\configs\middleware\mysql\initdb.d`
- `D:\work\week3\wanwu\configs\microservice\iam-service\configs\config.yaml`
- `D:\work\week3\wanwu\internal\iam-service\client\model\user.go`

The Go project starts MySQL from Docker Compose, mounts SQL init scripts under `/docker-entrypoint-initdb.d`, and creates service-specific schemas such as `iam_service`, `model_service`, `app_service`, `rag_service`, `assistant_service`, `knowledge_service`, `mcp_service`, and `operate_service`.

The IAM service config selects `db.name: mysql`, connects to the `iam_service` schema, and uses GORM models with indexed fields and millisecond timestamps. The same config shape also supports Postgres, TiDB, and OceanBase, but the default compose path is MySQL.

## Java Implementation

This slice wires Java IAM into the existing Docker MySQL service:

- `docker-compose.yml` now passes `SPRING_DATASOURCE_*` to `iam` and waits for healthy `mysql`.
- `bff` now waits for healthy `iam`, reducing startup races during login and permission calls.
- `wanwu-service-iam` depends on `wanwu-common-data` for MyBatis-Plus, Flyway, and MySQL driver support.
- `wanwu-service-iam/src/main/resources/db/migration/V1__create_iam_records.sql` creates `iam_records`.
- `IamServiceImpl` loads persisted records on startup and saves user, role, org, and OAuth app mutations. Platform custom settings have moved to `wanwu-service-operate`; IAM keeps only legacy fallback methods for older tests and compatibility.

## Persistence Shape

The current table is intentionally a compatibility snapshot table:

- `record_type`: user, role, org, oauth
- `record_id`: stable business id such as `user-1`, `role-1`, `org-1`, or `oauth-client-1`
- `payload`: JSON response-shaped record
- `created_at` and `updated_at`: millisecond timestamps

This is not the final Go-equivalent IAM relational model. It gives Docker Compose a real durable boundary now, while preserving the current frontend-compatible response maps. Later slices can replace individual record types with normalized tables without changing BFF routes.

## Verified Behavior

- Unit tests cover JSON upsert/delete calls and startup reload with sequence continuation.
- Docker smoke should create a user/role/org through `localhost:3000`, restart `iam` and `bff`, and verify the records still appear.

## Remaining Gaps

- Normalize IAM users, roles, orgs, role bindings, and org memberships into relational tables matching Go semantics.
- Persist password hashes, token version timestamps, invitation workflows, and email verification state.
- Implement real Excel import rows and failures instead of one deterministic imported development user.
- Add audit logs and permission enforcement beyond current frontend route exposure.
