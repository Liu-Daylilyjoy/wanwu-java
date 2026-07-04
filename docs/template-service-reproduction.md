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

`WanwuTemplateApiController` provides the frontend-compatible BFF contracts under `/user/api/v1`. Template rows now come from `AppService.listAppTemplates` / `AppService.getAppTemplate`, backed by the `app_templates` MySQL table seeded by Flyway. The controller keeps the previous in-process template seeds only as a development fallback while AppService is unavailable.

Assistant templates:

- `GET /assistant/template/list`: returns persisted template rows with `assistantTemplateId`, `appType`, `category`, `avatar`, `name`, `desc`, and detail fields.
- `GET /assistant/template`: returns a single template by `assistantTemplateId`.
- `POST /assistant/template`: creates an assistant through `AppService.createAssistant`, then applies template prologue, instructions, and recommend questions through `AppService.updateAssistantConfig`.

Workflow templates:

- `GET /workflow/template/list`: returns persisted template cards and `downloadLink.url`.
- `GET /workflow/template/detail`: returns detail fields used by the template detail page.
- `GET /workflow/template/recommend`: returns related template cards excluding the current template.
- `GET /workflow/template/download`: returns the template schema JSON as an attachment.
- `POST /workflow/template`: creates a workflow through `AppService.createWorkflow` and returns both `workflow_id` and `workflowId`.

## Current Boundary

This slice removes frontend `Not Found` failures for Template Square and assistant template creation without changing frontend code. It is still a development compatibility shell:

- Template records are Flyway-seeded development records in `app_templates`, not the full Go marketplace publication/governance model.
- Workflow template copy creates a workflow draft with a simple schema, not a full visual runtime graph imported from the Go template service.
- Real marketplace ranking, download counts, authorship governance, and template publication remain future slices.

## Verification

- `WanwuFrontendApiControllerTest.templateRoutesReturnFrontendContractsAndCopyApps` covers all eight frontend template routes.
- `AppServiceImplTest#appTemplatesUseRepositoryRecords` covers AppService mapping from repository records to frontend template fields.
- Docker Maven targeted and affected-module tests must pass before each commit.
- Docker Compose smoke verifies browser-facing `/user/api/v1` routes through the frontend proxy.
