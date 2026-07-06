# Model Scope Permission Reproduction

Date: 2026-07-05

## Go Source Baseline

The Go model service lists models with `WithUserOrgOrPublicScope(userID, orgID)`:

- private models: `user_id = ? AND org_id = ? AND scope_type = private`
- public models: `scope_type = public`
- organization models: `org_id = ? AND scope_type = org`

Relevant checked source:

- `D:\work\week3\wanwu\internal\model-service\client\orm\sqlopt\sqloption.go`
- `D:\work\week3\wanwu\internal\model-service\server\grpc\model\model.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\middleware\auth_model.go`

The Go BFF also validates model IDs in sensitive routes with `AuthModelByModelId` and `AuthModelByUuid`.

## Java Development Parity

`ModelServiceImpl` now applies the same owner/scope visibility rule when:

- listing models,
- listing selectable active models by type,
- reading a model detail,
- checking model user permission.

This means `dev-app` can no longer see or read `dev-admin` private models through service calls, while public and same-organization models remain visible.

The Java BFF now also performs Go-style route-level model checks before saving:

- Assistant config: `modelConfig.modelId`, `rerankConfig.modelId`, `recommendConfig.modelConfig.modelId`.
- RAG config: `modelConfig.modelId`, `rerankConfig.modelId`, `qaRerankConfig.modelId`.

Those checks run before `AppService` persistence and return the normal frontend failure envelope when a referenced model is not visible to the current user/org.

## Remaining Gap

The Java BFF already calls `checkModelUserPermission` for several OpenAPI routes, Assistant config, and RAG config, but not every Go `AuthModelByModelId` route has an equivalent Java filter yet. Later slices still need route-level model authorization for prompt optimization/reason/evaluate, model experience, ASR stream, and knowledge hit/config routes.
