# Callback File Reproduction

Date: 2026-07-05

## Go Source Baseline

Original Go files inspected:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\callback\router.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\model\request\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\model\response\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\service\workflow_upload.go`
- `D:\work\week3\wanwu\pkg\util\file.go`

Go exposes:

- `POST /callback/v1/file/url/base64`
- `POST /callback/v1/file/upload/base64`

`file/url/base64` reads `fileUrl`, downloads the file bytes, converts them to base64, and optionally adds a `data:*;base64,` prefix using `addPrefix` and `customPrefix`.

`file/upload/base64` reads `file`, optional `fileName`, and optional `fileExt`, uploads decoded bytes to MinIO, and returns:

- `url`
- `uri`

## Java Reproduction

Java controller:

- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiController.java`

Implemented behavior:

- `file/url/base64` now reads bytes from either an HTTP URL or a local `/callback/v1/file/{fileId}` URL.
- `addPrefix` and `customPrefix` are honored with the same comma-normalization behavior as Go.
- `file/upload/base64` decodes base64, stores bytes in the local BFF upload store, and returns Go-style `url` and `uri`.
- Existing Java compatibility fields `fileId`, `file_id`, `fileName`, `file_name`, and `path` are retained for older frontend callers.
- `GET /callback/v1/file/{fileId}` serves uploaded bytes with a simple content type inferred from the file extension.

This remains a Docker-development storage loop. It does not upload callback files to MinIO yet, so returned `url` values are local BFF callback URLs instead of object-storage download URLs.

## Verification

Targeted red-green test:

```powershell
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -DfailIfNoTests=false -Dtest=WanwuCallbackApiControllerTest#fileCallbacksReadAndUploadBytesWithGoCompatibleFields test
```

