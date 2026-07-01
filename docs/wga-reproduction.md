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

`WanwuGeneralAgentApiController` reproduces those frontend-visible contracts without requiring frontend changes. Resource selection delegates to the existing Java `AppService`, `McpService`, and `KnowledgeService` where possible. Conversation, Skill preview, and workspace state are held in a deterministic in-memory development model so the current Docker Compose environment can exercise the UI end to end.

The admin IAM account now exposes `wga` and `wga.wanwu_bot`. `wga.openclaw` and ontology permissions are intentionally not exposed.

## Current Behavior

- `/general/agent/sub/list` returns WanwuBot, General, Data Analysis, and Skill Chat modes, excluding the ontology agent.
- `/general/agent/config` stores tool/MCP/workflow/skill/assistant/knowledge config per development user/org scope and always strips ontology entries.
- `/general/agent/resource/select` aggregates assistant, MCP, workflow, skill, and knowledge selector lists.
- `/general/agent/conversation/chat` and `/general/agent/skill/conversation/chat` stream `RUN_STARTED`, `TEXT_MESSAGE_*`, `ACTIVITY_SNAPSHOT`, and `RUN_FINISHED` events that the frontend aggregator can render.
- workspace preview/download returns a generated `answer.md` artifact for each chat run.

## Remaining Gaps

- Real WGA sandbox runtime and sub-agent orchestration.
- Durable workspace files and artifact storage.
- OpenClaw route exposure and backend implementation.
- Ontology agent backend and menu exposure.
- Normalized Go-equivalent WGA persistence model.
