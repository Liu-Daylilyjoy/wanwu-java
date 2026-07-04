# WGA / WanwuBot Reproduction Notes

Date: 2026-07-01

## Go Source Alignment

The Go BFF registers `v1/wga.go` under `/service/api/v1/general/agent`. The frontend caller in `web/src/api/generalAgent.js` expects the same base path and consumes:

- global config and resource selectors,
- conversation create/list/detail/config/check,
- AG-UI style SSE chat events,
- workspace tree, preview, and download endpoints,
- Skill conversation create/import/convert/refresh/chat/preview endpoints,
- human-in-the-loop question reply and reject endpoints.

## Java Slice

`WanwuGeneralAgentApiController` reproduces those frontend-visible contracts without requiring frontend changes. Resource selection delegates to the existing Java `AppService`, `McpService`, and `KnowledgeService` where possible. Global config is persisted through Java `AppService`/MySQL in `general_agent_configs`; WGA conversation, Skill preview, run, and generated workspace snapshot state is persisted through Java `AppService`/MySQL in `general_agent_conversations`, with an in-memory cache only used as the current request/runtime acceleration layer.

The admin IAM account now exposes `wga` and `wga.wanwu_bot`. `wga.openclaw` and ontology permissions are intentionally not exposed.

## Current Behavior

- `/general/agent/sub/list` returns WanwuBot, General, Data Analysis, and Skill Chat modes, excluding the ontology agent.
- `/general/agent/config` stores tool/MCP/workflow/skill/assistant/knowledge config per development user/org scope in MySQL and always strips ontology entries. PUT follows the Go route's replace semantics, so missing sections are saved as empty lists.
- `/general/agent/resource/select` aggregates assistant, MCP, workflow, skill, and knowledge selector lists.
- `/general/agent/conversation` create/delete/list/detail/config and Skill preview detail now round-trip through AppService/MySQL conversation snapshots keyed by user/org/thread.
- `/general/agent/conversation/chat` and `/general/agent/skill/conversation/chat` stream `RUN_STARTED`, `TEXT_MESSAGE_*`, `ACTIVITY_SNAPSHOT`, and `RUN_FINISHED` events that the frontend aggregator can render.
- workspace preview/download returns a generated `answer.md` artifact for each persisted chat run.

## Remaining Gaps

- Real WGA sandbox runtime and sub-agent orchestration.
- Durable real workspace files and artifact object storage beyond the current generated run snapshot.
- OpenClaw route exposure and backend implementation.
- Ontology agent backend and menu exposure.
- Normalized Go-equivalent WGA runtime persistence model beyond snapshot JSON columns.
