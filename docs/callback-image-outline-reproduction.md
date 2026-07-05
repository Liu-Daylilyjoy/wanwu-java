# Callback Image Outline Reproduction

Date: 2026-07-05

## Go Source Baseline

Original Go files inspected:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\callback\router.go`
- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\model\request\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\model\response\callback.go`
- `D:\work\week3\wanwu\internal\bff-service\service\image_outline.go`
- `D:\work\week3\wanwu\internal\bff-service\service\image_outline_test.go`

Go registers `POST /callback/v1/image/outline`. The handler binds `ImageOutlineExtractReq` and delegates to `service.ExtractImageOutline`.

The response body uses `ImageOutlineExtractResp`:

- `message`
- `prompt`
- `markdown`
- `result`
- `mimeType`
- `url`
- `uri`
- `usage`

Go keeps `response_format` only for compatibility. Empty, `url`, and `b64_json` are accepted; unsupported values return an error. The checked Go tests show the route still returns markdown URL output even when `response_format=b64_json`.

## Java Reproduction

Java controller:

- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiController.java`

Implemented behavior:

- `POST /callback/v1/image/outline` no longer returns the old echo shell.
- The response now follows the Go field shape and omits `status/request/timestamp`.
- Unsupported `response_format` returns frontend error code `1001`.
- Missing `image` returns frontend error code `1001`.
- The returned `markdown`, `result[0]`, and `url` point to `/callback/v1/file/{fileId}`.
- `GET /callback/v1/file/{fileId}` serves the generated local PNG.

This is intentionally a deterministic Docker-development loop. It does not call DashScope Qwen image edit and does not upload to MinIO. The `usage.method` remains `qwen_image_edit` to preserve the Go response contract seen by callback consumers.

## Verification

Targeted red-green test:

```powershell
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -DfailIfNoTests=false -Dtest=WanwuCallbackApiControllerTest#imageOutlineCallbackReturnsGoCompatibleMarkdownResult test
```

