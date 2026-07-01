# OpenAPI Agent Config Publish Reproduction

Date: 2026-07-01

## Source Evidence

- Go registers `PUT /service/api/openapi/v1/agent/config` and `POST /service/api/openapi/v1/agent/publish` in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- Go request models are `OpenAPIAgentConfigUpdateRequest` and `OpenAPIAgentPublishRequest` in `internal/bff-service/model/request/openapi.go`.
- Go handlers update the assistant draft configuration before publishing an agent app version.

## Java Reproduction

- `WanwuOpenApiController.updateAgentConfig` now maps OpenAPI JSON into `AssistantConfigUpdateCommand` and calls `AppService.updateAssistantConfig`.
- `assistantUuid`, `uuid`, and `assistantId` are accepted as assistant identifiers for local Java compatibility.
- Config objects such as `modelConfig`, `knowledgeBaseConfig`, `safetyConfig`, `visionConfig`, `memoryConfig`, `rerankConfig`, and `recommendConfig` are passed through as structured maps.
- When `knowledgeBaseConfig` is omitted, Java fills the same default local match settings used by the Go OpenAPI handler shape: mix matching, priority match, threshold `0.4`, and `topK = 5`.
- `WanwuOpenApiController.publishAgent` now maps OpenAPI JSON into `AppPublishCommand` with `appType = agent` and calls `AppService.publishApp`.

## Verification

- `WanwuOpenApiControllerTest#agentConfigAndPublishRoutesUseAppService` verifies route dispatch, user/org context propagation, config map preservation, recommendation questions, version, description, publish type, and app type mapping.

## Remaining Gap

This slice removes two OpenAPI agent shells. Full Go parity still needs exact UUID-to-appID conversion when Java app IDs diverge from public UUIDs, OpenAPI model authorization middleware parity, Go's pre-publish prologue/model checks, and public API usage metrics.
