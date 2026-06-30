# Agent OpenURL Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a frontend-zero-change OpenURL loop for published agents.

**Architecture:** Add typed OpenURL commands/results to `wanwu-api`, store URL configs in app-service MySQL through the existing repository pattern, and expose both management and public BFF routes. Public chat reuses the existing assistant conversation stream and persistence paths.

**Tech Stack:** Java 8, Maven, Spring Boot, Dubbo, MyBatis Plus, Flyway, MySQL, Docker Compose, Vue/nginx frontend proxy.

---

### Task 1: Red Tests

**Files:**
- Modify: `wanwu-service-app/src/test/java/com/unicomai/wanwu/service/app/rpc/AppServiceImplTest.java`
- Modify: `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiControllerTest.java`
- Create: `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuOpenUrlApiControllerTest.java`

- [ ] Add tests for OpenURL create/list/status/expiry behavior.
- [ ] Add tests for frontend management routes.
- [ ] Add tests for public OpenURL info, conversation, and stream routes.
- [ ] Run Docker Maven and verify the tests fail because contracts and controllers are missing.

### Task 2: Service Contract And Persistence

**Files:**
- Create: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AppUrl*.java`
- Modify: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/AppService.java`
- Create: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/domain/AppUrlRecord.java`
- Create: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/entity/AppUrlEntity.java`
- Create: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/mapper/AppUrlMapper.java`
- Create: `wanwu-service-app/src/main/resources/db/migration/V5__create_app_urls.sql`
- Modify: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/domain/ApplicationRepository.java`
- Modify: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/MybatisApplicationRepository.java`

- [ ] Define OpenURL commands, query, and response DTO.
- [ ] Add repository methods for save/update/delete/list/find-by-suffix/status.
- [ ] Add MySQL table and mapper methods.
- [ ] Cascade OpenURL deletion when deleting an assistant.

### Task 3: App Service Logic

**Files:**
- Modify: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/rpc/AppServiceImpl.java`
- Modify: `wanwu-service-app/src/test/java/com/unicomai/wanwu/service/app/rpc/AppServiceImplTest.java`

- [ ] Require an existing published assistant snapshot before creating an OpenURL.
- [ ] Generate unique random suffixes.
- [ ] Persist legal text, description, expiry, owner, and enabled status.
- [ ] Enforce disabled and expired URL checks for public suffix lookup.
- [ ] Run app-service tests until green.

### Task 4: BFF Routes And Frontend Proxy

**Files:**
- Modify: `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiController.java`
- Create: `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuOpenUrlApiController.java`
- Modify: `web/nginx.conf`
- Modify: BFF tests from Task 1

- [ ] Add `/appspace/app/openurl*` management endpoints.
- [ ] Add `/openurl/v1/agent/{suffix}*` public endpoints.
- [ ] Add `/service/url/openurl/v1/agent/{suffix}*` alias for the current frontend constants.
- [ ] Proxy `/service/url/openurl/v1/` from nginx to BFF.
- [ ] Run BFF tests until green.

### Task 5: Docker Acceptance And Commit

**Files:**
- All files changed in this slice.

- [ ] Run Docker Maven tests for `wanwu-service-bff,wanwu-service-app`.
- [ ] Run `git diff --check`.
- [ ] Build `app`, `bff`, and `web` images.
- [ ] Start/recreate Docker Compose services.
- [ ] Exercise HTTP flow: create assistant, publish, create OpenURL, load public info, create public conversation, stream response, read history.
- [ ] Commit as `feat: add agent openurl loop`.
- [ ] Push `main` to origin.
