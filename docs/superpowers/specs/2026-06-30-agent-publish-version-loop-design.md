# Agent Publish Version Loop Design

## Goal

Reproduce the smallest agent publish/version loop that the existing Vue frontend can call without code changes.

## Go Source Contract

The original Go BFF registers:

- `POST /user/api/v1/appspace/app/publish`
- `DELETE /user/api/v1/appspace/app/publish`
- `GET /user/api/v1/appspace/app/version`
- `PUT /user/api/v1/appspace/app/version`
- `GET /user/api/v1/appspace/app/version/list`
- `POST /user/api/v1/appspace/app/version/rollback`
- `GET /user/api/v1/assistant` for published snapshot preview

For `appType=agent`, Go creates an assistant snapshot through assistant-service and then calls app-service `PublishApp` to store `publish_type`. Version history comes from `assistant_snapshots`; publish scope comes from app-service `apps`. Rollback reads the selected snapshot and overwrites the draft assistant plus related configs.

## Java Design

The current Java reproduction stores agent draft base data in `apps` plus `assistant_drafts`, and draft config in `assistant_draft_configs`. This slice adds `assistant_snapshots` and keeps `apps.publish_type` as the frontend-visible publish marker for the existing Java schema.

The snapshot stores the frontend editor shape as JSON. This keeps rollback simple and avoids inventing workflow/MCP/skill tables before those domains are reproduced.

Supported scope is intentionally `agent` only. Other app types return a frontend error instead of fake success.

## Behavior

- New agent drafts start with empty `publishType`, matching the frontend's draft/published distinction.
- Publishing validates `vX.Y.Z` and requires the new version to be greater than the latest snapshot.
- If the frontend sends no version, Java assigns the next patch version, starting at `v1.0.0`.
- Latest version returns `{version, desc, createdAt, publishType}`.
- Version list returns newest first.
- Updating version changes the latest snapshot description and current publish scope.
- Unpublish clears `publishType` but keeps snapshots.
- Rollback restores draft base fields and config from the selected snapshot.

## Verification

Completed checks:

- Docker Maven module tests for `wanwu-service-app` and `wanwu-service-bff`.
- `git diff --check`.
- `docker compose --profile full config`.
- `docker compose --profile full build app bff`.
- `docker compose --profile full up -d --force-recreate --no-build`.
- HTTP create/config/publish/latest/list/update/preview/rollback/unpublish flow through BFF.
- Frontend `/aibase/` still responds from Docker Compose.
- MySQL persistence check confirmed `assistant_snapshots` rows are written and `apps.publish_type` reflects publish/unpublish state.

HTTP acceptance evidence from the Docker stack:

```text
latest=v1.0.0/updated release/public
rollbackName=PublishLoop-20260630111030
unpublishedType='' cardVersion=v1.0.0
```
