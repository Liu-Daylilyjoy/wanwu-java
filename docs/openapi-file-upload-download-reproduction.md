# OpenAPI File Upload Download Reproduction

Date: 2026-07-05

## Go Source Baseline

- OpenAPI upload routes are registered in `internal/bff-service/server/http/handler/router/openapi/router.go`.
- Workflow and Chatflow uploads return a raw URL string.
- Direct upload returns a response body containing file identifiers/URL fields.
- In the Go deployment, these URLs are backed by the file/object storage layer.

## Java Reproduction

- `WanwuOpenApiController` returns local OpenAPI download URLs under `/service/api/openapi/v1/file/download/{fileId}`.
- Uploaded multipart bytes are stored in a BFF-local in-memory map keyed by the generated `openapi-file-*` id.
- `GET /service/api/openapi/v1/file/download/{fileId}` now returns the uploaded bytes with an attachment filename and the original content type when available.
- This closes the Docker development loop for Workflow/Chatflow/OpenAPI direct uploads without introducing MinIO or external object storage.

## Verification

- `WanwuOpenApiControllerTest#openApiUploadDownloadUrlReturnsUploadedBytes`

## Remaining Gap

The Java implementation is not durable across BFF restarts and does not reproduce object-storage lifecycle, signed URLs, or large-file streaming. Those remain future persistence/object-storage slices.
