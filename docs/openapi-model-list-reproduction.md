# OpenAPI Model List Reproduction

Date: 2026-07-05

## Go Source Baseline

- Route: `GET /service/api/openapi/v1/model/list`.
- Handler: `internal/bff-service/server/http/handler/openapi/model.go`.
- Request fields: `modelType`, `provider`, `displayName`, `isActive`, `filterScope`, and `scopeType`.
- Go calls the normal model list service, then maps the full `ModelInfo` objects into `OpenAPIModelListItem`.
- The OpenAPI list item intentionally exposes only `uuid`, `displayName`, `provider`, `modelType`, `model`, and `scopeType`.

## Java Reproduction

- `WanwuOpenApiController.listModels` still delegates filtering to `ModelService.listModels` with the OpenAPI user/org context.
- The BFF now maps `ModelListResult` into the Go OpenAPI public field set.
- Internal fields such as `modelId`, `config`, `tags`, user/org ids, and edit metadata are no longer exposed through the OpenAPI route.

## Verification

- `WanwuOpenApiControllerTest#modelOpenApiListReturnsGoPublicFieldsOnly`

## Remaining Gap

Java currently passes the common OpenAPI filters used by the existing route. Full parity still needs explicit `isActive`, `filterScope`, and `scopeType` query propagation if OpenAPI clients rely on those optional Go parameters.
