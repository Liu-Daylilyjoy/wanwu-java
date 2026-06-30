# Safety Service Reproduction

Date: 2026-06-30

## Original Go Mapping

- Frontend callers: `web/src/api/safety.js`, `web/src/views/safety/*`, and `web/src/components/setSafety.vue`.
- Go BFF router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\safety.go`.
- Shared selector route: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\common.go` registers `/safe/sensitive/table/select`.
- Go request/response contracts: `internal\bff-service\model\request\safety.go` and `internal\bff-service\model\response\safety.go`.
- Go app-service boundary: `internal\app-service\server\grpc\safety\service.go`.

## Covered Java Behavior

- `wanwu-api` exposes `SafetyService` for sensitive word table and word management.
- `wanwu-service-app` implements a Docker MySQL snapshot-backed repository for:
  - Sensitive word table create/list/detail/update/reply/delete.
  - Personal table select for Agent/RAG safety guard configuration.
  - Sensitive word upload/list/delete.
- `wanwu-service-bff` exposes the original frontend paths under `/user/api/v1/safe/sensitive/*`.
- IAM exposes `resource.safety` only for the admin development account.

## Verification

Executed in Docker with Java 8:

- `mvn -q -pl wanwu-service-iam,wanwu-service-bff,wanwu-service-app -am -DfailIfNoTests=false test`
- BFF contract test: `WanwuFrontendApiControllerTest` now covers all Safety HTTP paths.
- Service test: `SafetyServiceImplTest` covers table create/update/reply/select, word upload/list/delete, and table delete.

Frontend-entry smoke target:

- `http://localhost:3000/user/api/v1/base/login` returns `resource.safety` for `admin`.
- `/safe/sensitive/table` creates and edits a table.
- `/safe/sensitive/table/select` returns personal tables for Agent/RAG configuration.
- `/safe/sensitive/word` and `/safe/sensitive/word/list` manage table words.

## Current Boundary

This slice is a frontend-compatible management loop. It prevents the zero-change frontend Safety Guard page and Agent/RAG safety selector from receiving backend 404s, and table/word changes now survive Docker restarts through `app_service.safety_records`.

It does not yet implement:

- Normalized Go-equivalent MySQL tables for sensitive tables and words.
- Excel parsing for uploaded sensitive word files.
- Global table enforcement across chat streams.
- Aho-Corasick sensitive-word interception in Agent/RAG/Model SSE flows.
