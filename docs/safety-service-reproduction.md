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
  - Sensitive word single upload, BFF-local XLSX/CSV file import, list, and delete.
- `wanwu-service-app` now applies Safety Guard input checks to local Agent and RAG chat shells:
  - global sensitive tables are always considered.
  - configured personal tables are considered only when `safetyConfig.enable=true`.
  - a matched word returns the table reply as the local chat response.
- `wanwu-service-bff` applies the Go Model Experience input rule: local model-experience prompts check global sensitive tables before the deterministic SSE echo path and return the table reply as OpenAI-style SSE when matched.
- Local Java Agent/RAG and Model Experience generated outputs are checked against the same effective sensitive tables and replaced with the matched table reply before the frontend SSE frame is returned.
- `wanwu-service-bff` exposes the original frontend paths under `/user/api/v1/safe/sensitive/*`.
- For file imports, the BFF reads the unchanged frontend upload result from the local upload store, converts first-sheet `.xlsx` or UTF-8 text content into request `content`, and sends it through the Java Safety RPC boundary.
- IAM exposes `resource.safety` only for the admin development account.

## Verification

Executed in Docker with Java 8:

- `mvn -q -pl wanwu-service-iam,wanwu-service-bff,wanwu-service-app -am -DfailIfNoTests=false test`
- BFF contract test: `WanwuFrontendApiControllerTest` now covers all Safety HTTP paths and uploaded XLSX handoff.
- Service test: `SafetyServiceImplTest` covers table create/update/reply/select, word single/file upload/list/delete, and table delete.
- App service test: `AppServiceImplTest` covers Agent and RAG local chat input blocking through configured sensitive tables.
- App service tests: `AppServiceImplTest#assistantDraftStreamReplacesSensitiveGeneratedOutputWithTableReply` and `AppServiceImplTest#ragChatReplacesSensitiveGeneratedOutputWithTableReply` cover local Agent/RAG output replacement.
- BFF test: `WanwuFrontendApiControllerTest#modelExperienceLlmBlocksGlobalSensitiveInput` covers Model Experience global table input blocking.
- BFF test: `WanwuFrontendApiControllerTest#modelExperienceLlmReplacesSensitiveGeneratedOutput` covers Model Experience local output replacement and saved assistant content.

Frontend-entry smoke target:

- `http://localhost:3000/user/api/v1/base/login` returns `resource.safety` for `admin`.
- `/safe/sensitive/table` creates and edits a table.
- `/safe/sensitive/table/select` returns personal tables for Agent/RAG configuration.
- `/safe/sensitive/word` and `/safe/sensitive/word/list` manage single and uploaded-file table words.

## Current Boundary

This slice is a frontend-compatible management loop. It prevents the zero-change frontend Safety Guard page and Agent/RAG safety selector from receiving backend 404s, and table/word changes now survive Docker restarts through `app_service.safety_records`. Uploaded sensitive-word files now work in the local Docker path without changing the frontend, Agent/RAG local chat inputs and generated outputs honor the selected guard table reply, and Model Experience checks global tables before and after local SSE echo generation.

It does not yet implement:

- Normalized Go-equivalent MySQL tables for sensitive tables and words.
- Go-equivalent MinIO object lifecycle and richer `excelize` workbook parity for uploaded sensitive word files.
- Go-equivalent Aho-Corasick sliding-window interception for real provider token streams.
