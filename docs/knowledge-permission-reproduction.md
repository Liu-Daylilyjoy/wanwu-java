# Knowledge Permission Reproduction

Date: 2026-07-05

## Go Source Baseline

Checked source:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\knowledge.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\middleware\auth_knowledge.go`

Go permission levels:

- `KnowledgeView = 0`
- `KnowledgeEdit = 10`
- `KnowledgeGrant = 20`
- `KnowledgeSystem = 30`

Go `AuthKnowledge("knowledgeId", ...)` rejects missing `knowledgeId` with `knowledgeId is required`. `AuthKnowledgeIfHas` allows an empty field and only checks permission when the field is present.

## Java Development Parity

The Java frontend BFF now performs route-level `checkKnowledgeUserPermission` before entering the knowledge service for direct `knowledgeId` routes, including:

- Knowledge update/delete: system permission.
- Knowledge docs config/list/import/update-config/reimport/delete/export/upload-limit/import-tip: view or edit permission matching the Go router.
- Metadata, tag bind, permission grant pages, report pages, QA pair create/list/import/export/delete, export records, graph, and external knowledge update/delete.
- Optional `knowledgeId` behavior for Go `AuthKnowledgeIfHas` routes such as tag list and doc metadata update.

The Java BFF also mirrors Go's reference-resolving knowledge middleware:

- `AuthKnowledgeDoc("docId", ...)`: resolves `docId` through `KnowledgeService.resolveKnowledgeId` before checking view/edit permission.
- `AuthKnowledgeQAPair("qaPairId", ...)`: resolves `qaPairId` before checking edit permission on QA pair update/switch routes.
- `AuthKnowledgeRagName("knowledgeName", ...)`: resolves `knowledgeName` before checking view permission on `/knowledge/doc/by/name`.

The checks return the normal frontend failure envelope with code `1001` and prevent the downstream service call when permission is denied or when a required knowledge reference is missing.

## Remaining Gap

The knowledge service still uses a JSON snapshot compatibility repository rather than normalized Go-equivalent knowledge permission tables. Service-layer RBAC parity beyond the frontend/BFF route middleware remains a later normalization slice.
