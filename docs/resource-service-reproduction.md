# Resource Service Reproduction

Date: 2026-07-01

## Original Go Mapping

- Frontend callers: `web/src/api/mcp.js`, `web/src/api/templateSquare.js`, `web/src/api/promptTemplate.js`, `web/src/api/skill.js`, `web/src/api/skillSquare.js`, `web/src/views/tool`, `web/src/views/mcpManagementPublic`, and `web/src/components/createApp/createPrompt.vue`.
- Go BFF router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\tool.go`, `workflow.go`, `mcp_square.go`, `skill.go`, `skill_square.go`, and the prompt routes in `explore.go`.
- Go request/response contracts: `internal\bff-service\model\request\tool.go`, `tool_box.go`, `mcp.go`, `mcp_server.go`, `prompt.go`, `skill.go`, `skill_square.go`; responses in the matching `response` files, especially `workflow_tool.go` and `tool_box.go` for Workflow editor tool selection.
- Go remote MCP tool discovery: `internal\bff-service\pkg\mcp-util\mcp_client.go` creates an SSE or streamable client and calls `ListTools`; `internal\bff-service\service\mcp.go` wires `/mcp/tool/list` to that remote discovery path.
- Go public MCP Server execution: `internal\bff-service\pkg\mcp-util\mcp_server_util.go` maps bound OpenAPI operations into MCP tools and handles `tools/call` by applying Go-style argument prefixes and API-key auth before making the HTTP request.

## Covered Java Behavior

- `wanwu-api` now exposes a Java `McpService` contract for resource-center Tool, MCP, MCP Server, MCP square, Prompt, Skill, Skill square, and assistant select/action queries.
- `wanwu-service-mcp` implements a Docker MySQL-backed compatibility repository for:
  - Custom Tool create/list/detail/update/delete.
  - OpenAPI schema parsing into frontend action rows and MCP-compatible `name/description/inputSchema` tools.
  - Built-in Tool square list/detail and API-key shell.
  - Custom MCP create/list/detail/update/delete and tool listing.
  - Streamable and SSE custom MCP tool discovery through JSON-RPC `initialize` plus `tools/list`, with local sample-tool fallback when the remote endpoint is offline.
  - MCP Server create/list/detail/update/delete, server tool bind/edit/delete, and OpenAPI-tool bind shell.
  - MCP Server `tools/call` execution for bound built-in Weather/Search tools with deterministic local `content/structuredContent` results.
  - MCP square list/detail/recommend seed data.
  - Custom Prompt create/list/detail/update/delete/copy.
  - Prompt template list/detail/copy-to-custom.
  - Prompt optimize/reason/evaluate SSE compatibility, using configured OpenAI-compatible `modelId` upstream responses when available and deterministic local responses as fallback.
  - Custom Skill ZIP import/check/list/detail/delete, including `SKILL.md` front matter extraction for `name` and `description`, kebab-case validation, returning the imported markdown in custom Skill detail, and persisting the original ZIP package bytes in the Docker MySQL snapshot.
  - Custom/Built-in/Acquired Skill variable config create/update/delete.
  - Built-in Skill list/detail/download seed data, with download returning a real ZIP containing `SKILL.md` front matter instead of a text placeholder.
  - Skill select endpoint for Agent configuration.
  - Skill square list/detail/share/download seed data and acquired Skill list/detail/delete.
  - Skill conversation create/list/detail/delete/clear/chat/save with deterministic local SSE responses.
- `wanwu-service-bff` exposes the original frontend paths under `/user/api/v1/tool`, `/user/api/v1/mcp`, `/user/api/v1/prompt`, `/user/api/v1/agent/skill`, `/user/api/v1/agent/acquired/skill`, and `/user/api/v1/square/skill`.
- `wanwu-service-bff` also exposes `/user/api/v1/workflow/tool/select`, `/workflow/tool/action`, and `/workflow/tool/box`, adapting the same MCP Tool data into the Go Workflow editor response shapes.
- `wanwu-service-bff` exposes the public MCP Server OpenAPI endpoints with app-key validation plus JSON-RPC `initialize`, `ping`, `tools/list`, and `tools/call`. `tools/call` now delegates to `McpService.callMcpServerTool`; bound custom OpenAPI tools and direct OpenAPI schema tools execute real HTTP requests with query/header/path/body argument mapping and Go-style API-key auth, bound built-in Weather/Search tools execute through the Java local runtime, while unsupported bindings keep the deterministic compatibility fallback.
- Docker Compose `full` profile includes `mcp` on ports `8087` and `20887`, and BFF waits for it.

## Verification

Executed in Docker with Java 8:

- `mvn -q -pl wanwu-service-iam,wanwu-service-bff,wanwu-service-mcp -am -DfailIfNoTests=false test`
- `docker compose --profile full build mcp bff iam`
- `docker compose --profile full up -d --no-build mcp bff iam web`

Frontend-entry smoke test through `http://localhost:3000/user/api/v1`:

- Login as `admin` returned token `dev-token` and 18 implemented permissions, including `resource.tool`, `resource.mcp`, `resource.prompt`, and `resource.skill`.
- Login as `app` returned token `dev-token-app` and only `app`, `app.rag`, `app.workflow`, `app.agent`.
- Created a custom Tool with an OpenAPI schema and verified `/tool/custom/list`, `/tool/select`, `/tool/action/list?toolType=custom`, and Workflow editor tool select/action/tool-box compatibility.
- Created an MCP Server and verified `/mcp/server/list`.
- Verified `/mcp/tool/list?transport=streamable&serverUrl=...` reaches a local streamable MCP JSON-RPC server and returns the remote `tools/list` result.
- Verified `/mcp/tool/list?transport=sse&serverUrl=...` reads a local SSE endpoint event, posts JSON-RPC `tools/list` to the message endpoint, and returns the remote tool schema.
- Bound built-in Weather/Search tools to MCP Servers and verified `tools/call` returns MCP-compatible local `content`, `structuredContent.response`, and `isError=false`.
- Verified `/service/api/openapi/v1/mcp/server/message` `tools/call` delegates to the MCP service runtime, and verified the service runtime calls local OpenAPI HTTP servers for both custom-tool and direct-schema bindings with `query-*` arguments, JSON body arguments, bearer auth, and query auth.
- Created a custom Prompt and verified `/prompt/custom/list`.
- Verified `/prompt/optimize` returns an SSE `data:` frame with `finish`, and uses a configured OpenAI-compatible model upstream before falling back to the local response.
- Checked and created a custom Skill from a ZIP package containing `SKILL.md`, verified front matter parsing, added a variable config, and verified `/agent/skill/custom/list`, `/agent/skill/custom/detail`, and `/agent/skill/select`.
- Verified built-in Skill list/detail/download for `builtin-summary`, including unzipping the downloaded package and reading `SKILL.md`.
- Verified imported custom Skill packages are saved in the MCP snapshot and remain downloadable after service restart.
- Verified Skill square list, share-to-resource, acquired Skill list, and acquired Skill config.
- Verified Skill conversation create/chat SSE/save through `/agent/skill/conversation/*`.

## Current Boundary

This slice is a frontend-compatible management loop. It prevents zero-change frontend resource pages and Workflow editor tool panels from receiving backend 404s, and lets Agent/Workflow configuration select real locally-created Tool/MCP/Skill resources. Mutable custom Tool, MCP, MCP Server/tool, Prompt, Skill variable, acquired Skill, built-in Tool API-key, Skill package bytes, and Skill conversation state now survives Docker restarts through `mcp_service.mcp_records`. Custom MCP `streamable` and `sse` endpoints now try the real remote `tools/list` path before falling back to local development sample tools. Public MCP Server clients can now call bound built-in Weather/Search tools, bound custom OpenAPI tools, and direct OpenAPI schema tools through `tools/call`. Prompt optimize/reason/evaluate now honors configured OpenAI-compatible models before falling back to the local response. Custom Skill ZIP checks and creates now parse the uploaded package's `SKILL.md` front matter using the same user-visible constraints as the Go `ExtractSkillMarkdownFromZip` path, and Skill downloads now return real ZIP packages.

It does not yet implement:

- Normalized Go-equivalent MySQL tables for resource records.
- Full remote MCP runtime, streamable/SSE stateful proxying beyond `tools/list`, and provider-backed built-in tool execution beyond the local Weather/Search runtime.
- Real Skill package execution and package-to-runtime installation.
- Real Skill conversation generation through a model provider.
- Provider-specific prompt optimization beyond OpenAI-compatible chat completions.
- Callback MCP runtime parity beyond the public OpenAPI custom-tool execution path.
