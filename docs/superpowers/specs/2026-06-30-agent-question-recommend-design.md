# Agent Question Recommend Design

## Goal

Close the frontend call to `POST /user/api/v1/assistant/question/recommend` without changing Vue code.

## Go Source Contract

The Go BFF registers `POST /assistant/question/recommend` and binds:

- `query`
- `assistantId`
- `conversationId`
- `trial`

It resolves draft assistant info when `trial=true`, published assistant info otherwise, then streams OpenAI-style chat completion chunks. The Vue chat component reads `choices[0].delta.content`, `choices[0].contentType`, and `choices[0].finish_reason`.

## Java Design

Java BFF will keep this as a thin compatibility endpoint:

- Validate `assistantId` and `query`.
- Resolve draft or published assistant through the existing `AppService` read methods.
- Return `text/event-stream` with OpenAI-style chunk JSON.
- Use deterministic local suggestions instead of calling an external model.

This keeps the agent chat page free of 404s while preserving the future slot for a real model-backed recommendation service.

## Verification

Completed checks:

- BFF MVC tests cover draft and published recommendation routes.
- Docker Maven module tests for `wanwu-service-bff` passed.
- `git diff --check` passed with CRLF warnings only.
- Docker Compose BFF image build passed and the recreated BFF container became healthy.
- HTTP acceptance against Docker Compose BFF passed for both `trial=true` draft recommendation and `trial=false` published recommendation.

Acceptance result:

- `RECOMMEND_ACCEPTANCE_OK assistantId=assistant-23577fa4618f4da480657be939cc9f06 draftBytes=833 publishedBytes=836`
