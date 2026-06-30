# Agent Question Recommend Loop Implementation Plan

> Agent worker note: execute autonomously and commit after verification.

**Goal:** Add the missing frontend-compatible recommendation question SSE route.

---

### Task 1: Contract Tests

**Files:**
- Modify: `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiControllerTest.java`

- [x] **Step 1: Add draft recommendation SSE test**

Assert `POST /user/api/v1/assistant/question/recommend` returns `text/event-stream` with OpenAI-style `choices[0].delta.content` and looks up draft assistant info when `trial=true`.

- [x] **Step 2: Add published recommendation SSE test**

Assert the same endpoint looks up published assistant info when `trial=false`.

- [x] **Step 3: Run red**

Run:

```powershell
docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am test
```

Expected: FAIL because the endpoint is not implemented.

Result: failed as expected with HTTP 404 for both recommendation tests.

### Task 2: BFF Implementation

**Files:**
- Modify: `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiController.java`

- [x] **Step 1: Add request mapping**

Expose `POST /assistant/question/recommend` with `text/event-stream`.

- [x] **Step 2: Add deterministic SSE response**

Generate local follow-up questions from the user query and send an OpenAI-style answer chunk plus a stop chunk.

### Task 3: Verification And Commit

- [x] **Step 1: Run green tests**
- [x] **Step 2: Run Docker HTTP acceptance**
- [ ] **Step 3: Commit and push**

Verification results captured before commit:

- Red: Docker Maven BFF tests failed with two 404 assertions for `/assistant/question/recommend`.
- Green: `docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff -am test` passed.
- `git diff --check` passed with CRLF warnings only.
- `docker compose --profile full build bff` passed; image build Maven package reported `BUILD SUCCESS`.
- `docker compose --profile full up -d --force-recreate --no-deps --no-build bff` restarted BFF and health check reached `healthy`.
- HTTP acceptance: `RECOMMEND_ACCEPTANCE_OK assistantId=assistant-23577fa4618f4da480657be939cc9f06 draftBytes=833 publishedBytes=836`.
