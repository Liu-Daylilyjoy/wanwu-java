# Agent Conversation Loop Design

## Goal

Reproduce the smallest agent conversation loop that the existing Vue frontend can use without code changes.

## Go Source Contract

The Go BFF exposes these agent conversation routes:

- `POST /user/api/v1/assistant/conversation`
- `DELETE /user/api/v1/assistant/conversation`
- `DELETE /user/api/v1/assistant/conversation/clear`
- `GET /user/api/v1/assistant/conversation/list`
- `GET /user/api/v1/assistant/conversation/detail`
- `POST /user/api/v1/assistant/stream`
- `POST /user/api/v1/assistant/stream/draft`
- `GET /user/api/v1/assistant/conversation/draft/detail`
- `DELETE /user/api/v1/assistant/conversation/draft`
- `POST /user/api/v1/assistant/question/recommend`

The current Vue code also has `POST /assistant/test/stream`, so Java will support it as an alias for draft stream.

In Go, conversation metadata is stored by assistant-service, while message details are written to ES. Java will keep both in MySQL for the reproduction to avoid introducing ES before the minimum agent loop is stable.

## Java Design

Add two MySQL tables:

- `assistant_conversations`: user/org/assistant scoped conversation metadata.
- `assistant_conversation_messages`: prompt/response details and frontend-compatible JSON fields.

The app-service owns this state because the current Java reproduction already keeps agent draft and snapshot state there. BFF stays a thin adapter for `/user/api/v1`.

## Behavior

- `draft` conversations back the agent editor/test page.
- `published` conversations back published agent chat.
- Draft stream creates or reuses one draft conversation per assistant when no conversation id is provided.
- Published stream requires a published snapshot.
- Stream output is deterministic and local: it echoes a safe response from current agent metadata instead of calling an external model.
- Every stream call writes a message detail row, so list/detail endpoints remain consistent after page refresh.
- Clear/delete operations are idempotent where the Go BFF treats empty draft history as success.

## Verification

Completed checks:

- Docker Maven module tests for `wanwu-service-app` and `wanwu-service-bff` passed in `maven:3.9.9-eclipse-temurin-8`.
- `git diff --check` passed with CRLF warnings only.
- `docker compose --profile full config` passed.
- `docker compose --profile full build app bff` passed.
- `docker compose --profile full up -d --force-recreate --no-build app bff` started healthy `app` and `bff` containers.
- HTTP create/config/draft-stream/draft-history/publish/conversation/stream/list/detail/clear/delete flow passed through BFF with frontend homepage status `200`.
- MySQL persistence check found `assistant_conversations` and `assistant_conversation_messages`; the persistence probe left `conv_count=3`, `msg_count=1`, and latest prompt `persist me`.

Acceptance identifiers:

- Full loop assistant: `assistant-2b904972468c4f1bbb37bce69c9433ce`.
- Draft loop: `conversation-d0dbd9dd066446e58dd33cfa257aa422`, `detail-c1f7bac6438146e6a5ce8ded024bb0bc`.
- Published loop: `conversation-67e17f0a49dd409d878debaaf20c81e2`, `detail-5f4f583c303b4e758edce57750c21fdb`.
- Persistence probe: `assistant-6e38aac94af5462dab9dc63d4a7e9d4c`, `conversation-cd1ef56e18a14621a7bbc9b161182e85`, `detail-03989e12ef4a4864a25375b8ff697b34`.
