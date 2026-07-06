# Callback Agent Chat Reproduction

Date: 2026-07-06

## Go Source Baseline

Inspected original Go files:

- `D:\work\week3\wanwu\internal\bff-service\server\http\handler\callback\agent_proxy.go`
- `D:\work\week3\wanwu\internal\bff-service\service\agent_proxy.go`
- `D:\work\week3\wanwu\docs\callback\swagger.yaml`

Go route:

- `POST /callback/v1/agent/{assistantId}/chat`

The Go handler accepts `{"input": "..."}` and calls `service.AgentChatProxy`. That service proxies
to agent-service stream chat, keeps only `eventType=0` data, aggregates text chunks, and returns a
standard JSON response envelope where `data` is the final answer string.

The route is documented as `application/json`, not `text/event-stream`.

## Java Reproduction

Implemented in:

- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiController.java`
- `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuCallbackApiControllerTest.java`

Java now:

- accepts Go `input` and legacy development aliases `query`, `prompt`, and `content`
- injects `assistantId`
- sets `stream=true` before calling `AgentService.chatAgent`
- extracts `response`, `content`, `data`, or `output` from the Java AgentService result
- returns `FrontendResponse<String>` with the answer string in `data`
- falls back to echoing input only when AgentService is unavailable

## Current Boundary

This reproduces the callback JSON contract and connects to Java's local AgentService facade. Real
agent provider streaming, tool execution, and exact Go event aggregation still depend on deeper
Agent/Assistant runtime parity.

## Verification

Executed with Docker Maven/JDK8:

```powershell
docker run --rm -v "${env:USERPROFILE}\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am -Dtest=WanwuCallbackApiControllerTest#agentCallbackReturnsGoJsonStringFromAgentService+workflowMcpRagSkillAndSandboxCallbacksReturnFrontendSafeResponses -DfailIfNoTests=false test
```
