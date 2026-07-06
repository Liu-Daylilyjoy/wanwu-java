# MCP MySQL Persistence Slice

Date: 2026-06-30

## Go Source Baseline

Original files inspected:

- `D:\work\week3\wanwu\configs\microservice\mcp-service\configs\config.yaml`
- `D:\work\week3\wanwu\internal\mcp-service\client\model\custom_tool.go`
- `D:\work\week3\wanwu\internal\mcp-service\client\model\mcp.go`
- `D:\work\week3\wanwu\internal\mcp-service\client\model\mcp_server.go`
- `D:\work\week3\wanwu\internal\mcp-service\client\model\mcp_server_tool.go`
- `D:\work\week3\wanwu\internal\mcp-service\client\model\custom_skill.go`
- `D:\work\week3\wanwu\internal\mcp-service\client\model\builtin_skill_var.go`
- `D:\work\week3\wanwu\internal\mcp-service\client\model\acquired_skill.go`

The Go MCP service selects `db.name: mysql`, connects to the `mcp_service` schema, and uses ORM/model layers for custom tools, custom MCP records, MCP servers and bound tools, custom skills, acquired skills, skill variables, and skill publishing state.

## Java Coverage Added

- `docker-compose.yml` passes `SPRING_DATASOURCE_*` to `mcp` and waits for healthy `mysql`.
- `wanwu-service-mcp` depends on `wanwu-common-data`.
- `wanwu-service-mcp/src/main/resources/db/migration/V1__create_mcp_records.sql` creates `mcp_records`.
- `McpServiceImpl` loads and saves one JSON snapshot containing the current resource-center custom tools, custom MCPs, MCP servers/tools, custom prompts, custom/acquired/builtin skill variables, skill conversations, and built-in tool API keys.
- Custom MCP `transport=streamable` records now try to refresh their tool list from the configured remote endpoint using JSON-RPC `initialize` plus `tools/list`; successful tool rows are stored in the snapshot, while offline endpoints retain the development fallback tools.

## Persistence Shape

The current table is a compatibility snapshot table:

- `record_type`: `snapshot`
- `record_id`: `state`
- `payload`: JSON snapshot of the current Java compatibility state
- `created_at` and `updated_at`: millisecond timestamps

This makes the zero-frontend-change resource pages durable through Docker restarts while preserving the current frontend-compatible route surface.

## Verified Behavior

- Unit tests cover snapshot upsert, startup reload, restored Tool/Prompt/Skill state, and variable sequence continuation.
- Unit tests cover streamable MCP remote `tools/list` discovery against a local HTTP MCP JSON-RPC test server.
- Docker smoke should create a custom Tool through `localhost:3000`, recreate MCP and BFF containers, verify the Tool still appears, and confirm a row exists in `mcp_service.mcp_records`.

## Remaining Gaps

- Normalize the snapshot into Go-equivalent tables for custom tools, MCPs, MCP servers, server tools, skills, skill variables, and publishing state.
- Implement full remote MCP runtime, SSE discovery, stateful streamable proxying beyond `tools/list`, OpenAPI invocation, and callback/OpenAPI runtime endpoints.
- Implement real skill package parsing, validation, storage, execution, and model-backed prompt/skill generation.
