# Resource Service Reproduction

Date: 2026-06-30

## Original Go Mapping

- Frontend callers: `web/src/api/mcp.js`, `web/src/api/templateSquare.js`, `web/src/api/promptTemplate.js`, `web/src/views/tool`, `web/src/views/mcpManagementPublic`, and `web/src/components/createApp/createPrompt.vue`.
- Go BFF router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\tool.go`, `mcp_square.go`, and the prompt routes in `explore.go`.
- Go request/response contracts: `internal\bff-service\model\request\tool.go`, `mcp.go`, `mcp_server.go`, `prompt.go`; responses in the matching `response` files.

## Covered Java Behavior

- `wanwu-api` now exposes a Java `McpService` contract for resource-center Tool, MCP, MCP Server, MCP square, Prompt, and assistant select/action queries.
- `wanwu-service-mcp` implements a Docker development in-memory repository for:
  - Custom Tool create/list/detail/update/delete.
  - OpenAPI schema parsing into frontend action rows and MCP-compatible `name/description/inputSchema` tools.
  - Built-in Tool square list/detail and API-key shell.
  - Custom MCP create/list/detail/update/delete and tool listing.
  - MCP Server create/list/detail/update/delete, server tool bind/edit/delete, and OpenAPI-tool bind shell.
  - MCP square list/detail/recommend seed data.
  - Custom Prompt create/list/detail/update/delete/copy.
  - Prompt template list/detail/copy-to-custom.
  - Prompt optimize/reason/evaluate SSE compatibility with deterministic local responses.
- `wanwu-service-bff` exposes the original frontend paths under `/user/api/v1/tool`, `/user/api/v1/mcp`, and `/user/api/v1/prompt`.
- Docker Compose `full` profile includes `mcp` on ports `8087` and `20887`, and BFF waits for it.

## Verification

Executed in Docker with Java 8:

- `mvn -q -pl wanwu-service-iam,wanwu-service-bff,wanwu-service-mcp -am -DfailIfNoTests=false test`
- `docker compose --profile full build mcp`
- `docker compose --profile full build iam bff`
- `docker compose --profile full up -d --no-build knowledge iam mcp bff web`

Frontend-entry smoke test through `http://localhost:3000/user/api/v1`:

- Login as `admin` returned token `dev-token` and 17 implemented permissions, including `resource.tool`, `resource.mcp`, and `resource.prompt`.
- Created a custom Tool with an OpenAPI schema and verified `/tool/custom/list`, `/tool/select`, and `/tool/action/list?toolType=custom`.
- Created an MCP Server and verified `/mcp/server/list`.
- Created a custom Prompt and verified `/prompt/custom/list`.
- Verified `/prompt/optimize` returns an SSE `data:` frame with `finish`.

## Current Boundary

This slice is a frontend-compatible management loop. It prevents zero-change frontend resource pages from receiving backend 404s and lets Agent configuration select real locally-created Tool/MCP resources.

It does not yet implement:

- MySQL persistence for resource records.
- Real remote MCP protocol runtime, SSE/streamable proxying, or OpenAPI invocation.
- Built-in/acquired/custom Skill resource flows.
- Prompt optimization through a real model provider.
- Callback/OpenAPI runtime endpoints for MCP Server clients.
