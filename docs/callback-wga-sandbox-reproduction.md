# Callback WGA Sandbox Reproduction

Date: 2026-07-06

## Go Source Baseline

Inspected original Go files:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\wga_sandbox.go`
- `D:\work\week3\wanwu\internal\bff-service\model\request\wga_sandbox.go`
- `D:\work\week3\wanwu\internal\bff-service\service\wga_sandbox.go`
- `D:\work\week3\wanwu\pkg\sse-util\sse_client.go`

Go callback routes:

- `POST /callback/v1/wga/sandbox/run` returns `text/event-stream`.
- `POST /callback/v1/wga/sandbox/cleanup` returns the standard JSON response envelope.

Go run requests include `threadId`, `runId`, `model`, `instruction`, `overallTask`, `messages`,
`tools`, `skills`, `mcps`, `inputDir`, `outputDir`, `enableThinking`, `skipCleanup`, and
`agentName`. The Go service resolves the model, configures an OpenCode runner, streams sandbox
output lines, and appends `data: [DONE]`.

## Java Reproduction

Implemented in:

- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiController.java`
- `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiControllerTest.java`

Java now separates the two routes:

- `run` emits `text/event-stream`.
- `cleanup` returns `FrontendResponse`.

The development run event includes:

- `code`, `message`, `threadId`, `runId`, `status`, and `finish`
- `data.output`
- `data.summary` with non-sensitive request counts and ids
- trailing `data: [DONE]`

The summary intentionally does not echo full model configuration or credentials.

## Current Boundary

This is a local development compatibility stream, not the real WGA sandbox runtime. It does not
launch OpenCode, attach MCP servers, execute tools, mount skill directories, write workspace files,
or clean remote sandbox containers. Those remain later reproduction slices.

## Verification

Executed with Docker Maven/JDK8:

```powershell
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -Dtest=WanwuCallbackApiControllerTest#workflowMcpRagSkillAndSandboxCallbacksReturnFrontendSafeResponses -DfailIfNoTests=false test
```
