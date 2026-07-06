# BFF User Context Resolution

Date: 2026-07-05

## Go Source Baseline

The Go BFF auth middleware resolves request auth into user and organization context before business handlers call service logic. The checked source path is:

- `D:\work\week3\wanwu\internal\bff-service\server\http\middleware\auth_user.go`

That flow is broader than a frontend permission menu: it provides request identity for ownership-scoped writes, list queries, and permission checks.

## Java Development Parity

`BffUserContextResolver` now centralizes the Docker development identity mapping used by BFF controllers:

| Input token | userId | orgId | username | admin |
| --- | --- | --- | --- | --- |
| empty or `dev-token` | `dev-admin` | `default-org` | `admin` | `true` |
| `dev-token-app` | `dev-app` | `default-org` | `app` | `false` |

The resolver accepts both `Bearer <token>` and plain token values. Controllers that accept explicit user/org headers, such as the General Agent compatibility controller, still let those headers override the development defaults.

## Covered BFF Callers

The shared resolver is now used by the main frontend controller plus the Resource, Skill, Statistic, Operation, Safety, Exploration, legacy Model Use, Setting, Template, Common, and General Agent compatibility controllers.

The most important behavior change is in `WanwuFrontendApiController`: app-token application writes no longer fall back to `dev-admin`; they pass `dev-app` and `default-org` to service commands.

## Remaining Gap

This is still a Docker development identity resolver. Full Go parity still needs token/session validation, multi-organization selection, dynamic IAM users created at runtime, exact role membership, request audit fields, and service-layer authorization across every Java service.
