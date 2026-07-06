# Frontend Permission Filter Reproduction

Date: 2026-07-05

## Go Source Baseline

The checked Go BFF protects frontend routes with `CheckUserPerm` in:

- `D:\work\week3\wanwu\internal\bff-service\server\http\middleware\auth_user.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\middleware\init.go`

The Go middleware finds the permission tags registered for the current route, calls the IAM permission service, and returns `403` when the user does not own any required tag. A small set of common routes is excluded, including user info, user permission, password update, and organization selection.

## Java Development Parity

`WanwuFrontendPermissionFilter` now adds the same frontend-facing guard for `/user/api/v1/**` routes.

Current permission mapping:

| Route family | Required permission |
| --- | --- |
| `/user`, `/user/batch`, `/org/user` | `permission.user` |
| `/role` | `permission.role` |
| `/org` | `permission.org` |
| `/custom` | `setting` |
| `/model` | `model.model_management` |
| `/api/key` | `api_key.api_key_management` |
| `/appspace/assistant`, `/assistant` | `app.agent` |
| `/appspace/rag`, `/rag` | `app.rag` |
| `/appspace/workflow`, `/appspace/chatflow`, `/workflow`, `/chatflow` | `app.workflow` |
| `/knowledge` | `resource.knowledge` |
| `/tool` | `resource.tool` |
| `/mcp` | `resource.mcp` |
| `/prompt` | `resource.prompt` |
| `/agent/skill`, `/builtin/skill`, `/square/skill` | `resource.skill` |
| `/safe` | `resource.safety` |
| `/oauth` | `operation.oauth` |
| `/statistic/client` | `operation.statistic_client` |
| `/statistic` | `app_observability.statistic` |
| `/exploration/app` | `exploration.app` |
| `/exploration/mcp` | `exploration.mcp` |
| `/exploration` | `exploration` |

Common routes stay open after login so the unchanged frontend can still load profile, permission, avatar, docs, file, and organization bootstrap data.

## Development Accounts

- `dev-token` keeps the full reproduced development permission set.
- `dev-token-app` keeps only `app`, `app.rag`, `app.workflow`, and `app.agent`.

The app-only account can enter Agent/RAG/Workflow/Chatflow application pages, but model, knowledge, resource, permission, operation, setting, exploration, statistic, and API-key management routes now return `403` from the BFF even if the user manually opens those URLs.

## Remaining Gap

This is a frontend-route guard, not the final Go RBAC reproduction. The normalized Go IAM tables, exact route-tag registration model, token-to-user/session semantics, audit behavior, and service-layer authorization checks still need later slices.
