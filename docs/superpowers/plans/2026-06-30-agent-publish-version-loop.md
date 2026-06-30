# Agent Publish Version Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Docker-verifiable agent publish/version/rollback loop that the current frontend can call unchanged.

**Architecture:** Keep agent draft writes in the existing AppService module and add an `assistant_snapshots` table for published versions. BFF remains a thin contract adapter from Vue's `/user/api/v1` routes to typed AppService DTOs.

**Tech Stack:** Java 8, Spring Boot, Dubbo, MyBatis-Plus, Flyway, MySQL, Maven in Docker.

---

### Task 1: Contract And Red Tests

**Files:**
- Modify: `wanwu-service-app/src/test/java/com/unicomai/wanwu/service/app/rpc/AppServiceImplTest.java`
- Modify: `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiControllerTest.java`

- [x] **Step 1: Add failing service tests**

Cover publish snapshot creation, version monotonic validation, latest version update, version list order, rollback, unpublish, and published snapshot detail.

- [x] **Step 2: Add failing BFF tests**

Cover `POST/DELETE /appspace/app/publish`, `GET/PUT /appspace/app/version`, `GET /appspace/app/version/list`, `POST /appspace/app/version/rollback`, and `GET /assistant`.

- [x] **Step 3: Run red**

Run:

```powershell
docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff,wanwu-service-app -am test
```

Expected: FAIL because publish/version DTOs and methods are missing.

### Task 2: API And Persistence

**Files:**
- Create: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AppPublishCommand.java`
- Create: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AppVersionQuery.java`
- Create: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AppVersionInfo.java`
- Create: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AppVersionListResult.java`
- Create: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AppVersionUpdateCommand.java`
- Create: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AppVersionRollbackCommand.java`
- Create: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AssistantPublishedQuery.java`
- Modify: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/AppService.java`
- Create: `wanwu-service-app/src/main/resources/db/migration/V3__create_assistant_snapshots.sql`
- Create: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/domain/AssistantSnapshotRecord.java`
- Create: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/entity/AssistantSnapshotEntity.java`
- Create: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/mapper/AssistantSnapshotMapper.java`
- Modify: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/domain/ApplicationRepository.java`
- Modify: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/MybatisApplicationRepository.java`
- Modify: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/mapper/AppMapper.java`

- [x] **Step 1: Add typed DTOs and AppService methods**

Expose publish, unpublish, latest, list, update, rollback, and published-detail operations.

- [x] **Step 2: Add `assistant_snapshots` persistence**

Store version metadata and snapshot JSON keyed by user/org/assistant/version.

### Task 3: Service And BFF Implementation

**Files:**
- Modify: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/rpc/AppServiceImpl.java`
- Modify: `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiController.java`

- [x] **Step 1: Implement agent-only publish/version behavior**

Validate `agent`, version format, increasing version, and publish scope.

- [x] **Step 2: Implement rollback**

Restore draft base fields and config from selected snapshot JSON.

- [x] **Step 3: Map frontend routes**

Replace the version-list stub with real service calls and add `GET /assistant` for published preview.

### Task 4: Verification And Commit

- [x] **Step 1: Run green tests**

Run the same Docker Maven command. Expected: PASS.

- [x] **Step 2: Run Docker Compose and HTTP acceptance**

Build/recreate `app` and `bff`, then verify create/config/publish/latest/list/update/preview/rollback/unpublish over HTTP.

- [ ] **Step 3: Commit and push**

Run:

```powershell
git add <changed files>
git commit -m "feat: add agent publish version loop"
git push origin main
```
