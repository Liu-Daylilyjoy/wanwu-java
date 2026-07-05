# Static Docs Reproduction

Date: 2026-07-01

## Source Behavior

The Go BFF registers static files for the frontend with:

`apiV1.Static("/static", "./configs/microservice/bff-service/static")`

Because the frontend is mounted under `/user/api/v1`, template downloads use paths such as:

- `/user/api/v1/static/docs/users.xlsx`
- `/user/api/v1/static/docs/sensitive.xlsx`
- `/user/api/v1/static/docs/graph_schema.xlsx`
- `/user/api/v1/static/docs/url_import_template.xlsx`
- `/user/api/v1/static/docs/qa_import_template.xlsx`
- `/user/api/v1/static/docs/qa_pair_template.csv`
- `/user/api/v1/static/docs/report.csv`
- `/user/api/v1/static/docs/segment.csv`

These are opened directly by browser actions in permission, knowledge, QA, community-report, chunk, and safety pages.

## Java Compatibility Strategy

`WanwuStaticDocsController` serves only a fixed whitelist of frontend-visible docs under `/user/api/v1/static/docs/{fileName}`.

- CSV templates are generated as UTF-8 `text/csv`.
- XLSX templates are generated as minimal valid OOXML ZIP files with one worksheet. The Safety Guard sensitive-word template uses the same type-column matrix shape that the local import parser accepts.
- The user batch-import template now uses the Go BFF header contract (`用户名/密码/单位/电话/角色/备注`) and includes a sample row that passes the Java IAM import validator.
- Unknown names return 404.
- The implementation does not read arbitrary local files and does not depend on the original Go workspace being present inside the Docker image.

This is a frontend compatibility slice. The generated templates are development-safe placeholders, not exact binary copies of the original Go static assets.

## Verification

- `WanwuStaticDocsControllerTest` covers all frontend-referenced template names, content types, XLSX ZIP entries, content disposition headers, no-cache headers, Go-compatible user-import template headers/sample row, and unknown-file 404 behavior.
