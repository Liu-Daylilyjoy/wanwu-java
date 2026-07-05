# Template Compatibility Reproduction

Date: 2026-06-30

## Go Source Baseline

The original Go BFF exposes two template surfaces used by the unchanged frontend:

- `v1/assistant.go`: `GET /assistant/template/list`, `GET /assistant/template`, `POST /assistant/template`.
- `v1/guest.go`: `GET /workflow/template/list`, `GET /workflow/template/detail`, `GET /workflow/template/recommend`, `GET /workflow/template/download`.
- `v1/workflow.go`: `POST /workflow/template` for copying a workflow template into App Space.

The frontend callers are:

- `web/src/api/appspace.js`: assistant template list, detail, and copy.
- `web/src/api/templateSquare.js`: workflow template list, detail, recommend, download, and copy.
- `web/src/views/templateSquare/*.vue`: expects `templateId`, `downloadLink.url`, `summary`, `feature`, `scenario`, and `note`.
- `web/src/components/createApp/createWorkflow.vue`: expects `POST /workflow/template` to return `workflow_id`.

## Java Reproduction

`WanwuTemplateApiController` provides the frontend-compatible BFF contracts under `/user/api/v1`. Template rows now come first from `AppService.listAppTemplates` / `AppService.getAppTemplate`, backed by the `app_templates` MySQL table seeded by Flyway. The controller then merges read-only classpath bundles generated from the original Go BFF configuration:

- Assistant bundle: `configs/microservice/bff-service/configs/assistant_template_config.yaml`, currently 6 local tourism Assistant templates.
- Workflow bundle: `configs/microservice/bff-service/configs/workflow_template_config.yaml` plus the original `workflow-template/*/*.json` schemas, currently 30 local Workflow templates.

This lets the unchanged frontend see the Go local template square data even when those templates are not yet persisted as database rows.

Assistant templates:

- `GET /assistant/template/list`: returns persisted template rows plus missing Go local Assistant templates with `assistantTemplateId`, `appType`, `category`, `avatar`, `name`, `desc`, and detail fields.
- `GET /assistant/template`: returns a single template by `assistantTemplateId`, including Go fields such as `recommendQuestion` and `workFlowInstruction`.
- `POST /assistant/template`: creates an assistant through `AppService.createAssistant`, then applies template prologue, instructions, and recommend questions through `AppService.updateAssistantConfig`.

Workflow templates:

- `GET /workflow/template/list`: returns persisted template cards plus missing Go local template cards and `downloadLink.url`.
- `GET /workflow/template/detail`: returns detail fields used by the template detail page.
- `GET /workflow/template/recommend`: returns related template cards excluding the current template.
- `GET /workflow/template/download`: records the Workflow template download count through `AppService.recordAppTemplateDownload`, then returns the template schema JSON as an attachment.
- `POST /workflow/template`: creates a workflow through `AppService.createWorkflow` using either a persisted template schema or the bundled Go template schema, then returns both `workflow_id` and `workflowId`.

## Current Boundary

This slice removes frontend `Not Found` failures for Template Square and assistant template creation without changing frontend code. It is still a development compatibility shell:

- AppService/MySQL rows remain the write-side source of truth for download counts and custom seed records; the Go bundles are read-only BFF fallbacks for original local templates.
- Workflow template copy can now create a workflow draft from the original Go template schema; execution is still limited by the Java local workflow runtime.
- Real marketplace ranking, authorship governance, and template publication remain future slices.

## Verification

- `WanwuFrontendApiControllerTest.templateRoutesReturnFrontendContractsAndCopyApps` covers all eight frontend template routes, the Go `cultural_tourism_research_agent` Assistant bundled template list/detail/copy path, and the Go `policy_assistant` Workflow bundled template list/detail/copy path.
- `AppServiceImplTest#appTemplatesUseRepositoryRecords` covers AppService mapping from repository records to frontend template fields.
- Docker Maven targeted and affected-module tests must pass before each commit.
- Docker Compose smoke verifies browser-facing `/user/api/v1` routes through the frontend proxy.
