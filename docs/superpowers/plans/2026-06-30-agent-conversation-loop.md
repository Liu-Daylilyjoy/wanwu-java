# Agent Conversation Loop Implementation Plan

> Agent worker note: execute this plan autonomously and commit after verification.

**Goal:** Add a Docker-verifiable agent conversation loop that the current frontend can call unchanged.

**Architecture:** Store conversation metadata and details in MySQL under app-service. Keep BFF as a frontend route adapter.

**Tech Stack:** Java 8, Spring Boot, Dubbo, MyBatis-Plus, Flyway, MySQL, Maven in Docker.

---

### Task 1: Contract And Red Tests

**Files:**
- Modify: `wanwu-service-app/src/test/java/com/unicomai/wanwu/service/app/rpc/AppServiceImplTest.java`
- Modify: `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiControllerTest.java`

- [x] **Step 1: Add failing service tests**

Cover conversation create/list/detail, stream persistence, draft history reuse, single-message clear, and full conversation delete.

- [x] **Step 2: Add failing BFF tests**

Cover `/assistant/conversation*`, `/assistant/stream`, `/assistant/stream/draft`, `/assistant/test/stream`, and draft conversation routes.

- [x] **Step 3: Run red**

Run:

```powershell
docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff,wanwu-service-app -am test
```

Expected: FAIL because conversation DTOs and methods are missing.

Result: failed as expected before implementation because `AssistantConversation*` DTOs and AppService methods did not exist.

### Task 2: API And Persistence

**Files:**
- Modify: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/AppService.java`
- Create DTOs under `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/`
- Create domain records and MyBatis entities/mappers under `wanwu-service-app`
- Create: `wanwu-service-app/src/main/resources/db/migration/V4__create_assistant_conversations.sql`

- [x] **Step 1: Add typed conversation DTOs**

Expose create, list/detail query, delete/clear, stream command/result, and page result.

- [x] **Step 2: Add MySQL persistence**

Store conversation metadata and message detail rows with frontend-compatible JSON payload fields.

### Task 3: Service And BFF Implementation

**Files:**
- Modify: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/rpc/AppServiceImpl.java`
- Modify: `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiController.java`

- [x] **Step 1: Implement draft and published conversation behavior**

Validate assistant ownership, reuse draft conversation, require published snapshot for published stream, and persist details.

- [x] **Step 2: Implement frontend routes and SSE**

Return normal JSON for list/detail/delete and `text/event-stream` for stream endpoints.

### Task 4: Verification And Commit

- [x] **Step 1: Run green tests**

Run Docker Maven module tests.

- [x] **Step 2: Run Docker Compose and HTTP acceptance**

Build/recreate `app` and `bff`, then verify the full conversation loop over HTTP.

- [ ] **Step 3: Commit and push**

Run:

```powershell
git add <changed files>
git commit -m "feat: add agent conversation loop"
git push origin main
```

Verification results captured before commit:

- `docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff,wanwu-service-app -am test`: passed.
- `git diff --check`: passed with CRLF warnings only.
- `docker compose --profile full config`: passed.
- `docker compose --profile full build app bff`: passed.
- `docker compose --profile full up -d --force-recreate --no-build app bff`: app and bff became healthy.
- HTTP acceptance through BFF: `ACCEPTANCE_OK assistantId=assistant-2b904972468c4f1bbb37bce69c9433ce draftConversation=conversation-d0dbd9dd066446e58dd33cfa257aa422 draftDetail=detail-c1f7bac6438146e6a5ce8ded024bb0bc publishedConversation=conversation-67e17f0a49dd409d878debaaf20c81e2 publishedDetail=detail-5f4f583c303b4e758edce57750c21fdb frontend=200`.
- MySQL persistence probe: `assistant_conversations` and `assistant_conversation_messages` exist; `conv_count=3`, `msg_count=1`, latest detail `detail-03989e12ef4a4864a25375b8ff697b34` stores prompt `persist me`.
