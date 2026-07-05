# Agent Service Reproduction

## Source Boundary

The checked Go source does not expose a separate `proto/agent-service` package. Agent behavior is represented through the Assistant service, appspace BFF routes, OpenAPI Agent routes, and legacy model-use assistant aliases.

The Java project still has a `wanwu-service-agent` module, so this slice turns it from a describe-only shell into an Agent-compatible facade.

## Java Slice

`AgentService` now exposes core Agent operations as aliases over the reproduced Assistant boundary:

- Agent create/update/config/delete/list/detail/copy delegate to `AssistantService` draft lifecycle calls.
- Publish/update/list/rollback delegate to Assistant snapshot calls.
- Conversation create/delete/list/detail and chat delegate to Assistant conversation calls.
- Responses preserve `assistantId` while adding `agentId` or `agentInfos` aliases where useful for Agent callers.

This keeps Agent state in the existing Assistant/AppService persistence model and avoids inventing a second Agent store.

## Verification

- `AgentServiceImplTest#createAgentDelegatesToAssistantCreateAndAddsAgentIdAlias`
- `AgentServiceImplTest#listAgentsReturnsAgentInfosAlias`
- `AgentServiceImplTest#publishAndChatDelegateToAssistantService`

## Remaining Gap

This is a Java compatibility module, not a separate Go service reproduction. Real agent orchestration, tool execution, model streaming, and multi-agent runtime behavior remain owned by the Assistant/AppService runtime slices.
