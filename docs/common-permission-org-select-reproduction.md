# Common Permission And Organization Select Reproduction

## Go Source

- Router: `internal/bff-service/server/http/handler/router/v1/common.go`
- Handlers: `internal/bff-service/server/http/handler/v1/common.go`

The Go BFF registers:

- `GET /user/api/v1/user/permission`
- `GET /user/api/v1/org/select`

The handlers delegate to IAM-backed service calls using the current user token context:

- `service.GetUserPermission(ctx, userId, orgId)`
- `service.GetOrgSelect(ctx, userId)`

## Java Reproduction

Java already exposes these routes from `WanwuFrontendApiController`. This slice hardens them so
frontend bootstrap does not fail when the IAM Dubbo provider is temporarily unavailable in Docker
development mode:

- `/user/permission` still prefers `IamService.permission(token)`.
- `/org/select` still prefers `IamService.selectOrganizations()`.
- If the IAM call throws or the reference is absent, the BFF returns deterministic development data.

The fallback keeps the same development account contract used by login and the frontend permission
filter:

- `dev-token`: admin permissions for implemented frontend route groups.
- `dev-token-app`: app-only permissions for `app`, `app.rag`, `app.workflow`, and `app.agent`.
- Both responses keep WGA/general-agent permissions hidden.
- Organization select returns the default Docker organization `default-org`.

## Verification

```powershell
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -Dtest=WanwuFrontendApiControllerTest#permissionAndOrgSelectFallbackWhenIamUnavailable -DfailIfNoTests=false test
```

## Remaining Gaps

This is a BFF compatibility fallback. Full Go parity for IAM still depends on the normalized
organization, role, route-tag, and user-permission tables rather than the current development JSON
compatibility repository.
