# Frontend Compat Full Compose Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a zero-frontend-change minimum Wanwu business loop: login to `/aibase/appSpace/agent` with an empty assistant list through Docker Compose.

**Architecture:** Nginx serves the copied Vue frontend and proxies `/user/api/*` to the Java BFF. The BFF exposes the original frontend API contract and uses Dubbo through Nacos to call IAM and App services. IAM and App use in-memory development data for this slice.

**Tech Stack:** Java 8, Spring Boot 2.7, Apache Dubbo 3, Nacos, Maven, Vue 2 static build, Nginx, Docker Compose, Playwright.

---

### Task 1: Contract Tests

**Files:**
- Create: `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiControllerTest.java`
- Create: `wanwu-service-iam/src/test/java/com/unicomai/wanwu/service/iam/rpc/IamServiceImplTest.java`
- Create: `wanwu-service-app/src/test/java/com/unicomai/wanwu/service/app/rpc/AppServiceImplTest.java`

- [x] Write tests for login response shape, captcha response shape, permission/org/custom shape, and empty assistant list.
- [x] Run `mvn -pl wanwu-service-bff,wanwu-service-iam,wanwu-service-app -am test` and confirm tests fail because the new contracts are missing.

### Task 2: API DTOs and RPC Interfaces

**Files:**
- Modify: `wanwu-api/src/main/java/com/unicomai/wanwu/api/iam/IamService.java`
- Modify: `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/AppService.java`
- Create DTOs under `wanwu-api/src/main/java/com/unicomai/wanwu/api/iam/dto/`
- Create DTOs under `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/`

- [x] Add serializable request/result DTOs for captcha, login, permission, organization select, and assistant list.
- [x] Add IAM RPC methods for `captcha`, `login`, `permission`, `selectOrganizations`, and `platformConfig`.
- [x] Add App RPC method for `listAssistants`.

### Task 3: In-Memory Service Implementations

**Files:**
- Modify: `wanwu-service-iam/src/main/java/com/unicomai/wanwu/service/iam/rpc/IamServiceImpl.java`
- Modify: `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/rpc/AppServiceImpl.java`

- [x] Implement developer login for `admin` with non-empty password and non-empty captcha.
- [x] Return permissions `app` and `app.agent`, default org `default-org`, `isUpdatePassword=true`, and 2FA disabled in platform config.
- [x] Return `{ list: [] }` for assistant list.
- [x] Run service tests and confirm green.

### Task 4: BFF Frontend Compatibility Layer

**Files:**
- Create: `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/FrontendResponse.java`
- Create: `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiController.java`

- [x] Expose `/user/api/v1/base/captcha`.
- [x] Expose `/user/api/v1/base/login`.
- [x] Expose `/user/api/v1/user/permission`.
- [x] Expose `/user/api/v1/org/select`.
- [x] Expose `/user/api/v1/base/custom`.
- [x] Expose `/user/api/v1/appspace/assistant/list`.
- [x] Run BFF tests and confirm green.

### Task 5: Full Docker Compose

**Files:**
- Create: `web/Dockerfile`
- Create: `web/nginx.conf`
- Modify: `docker-compose.yml`
- Modify: `.dockerignore`

- [x] Build Vue static assets in Docker with `VUE_APP_BASE_PATH=`.
- [x] Serve static assets at `/aibase` with Nginx.
- [x] Proxy `/user/api/*` to `bff:8080`.
- [x] Add `full` profile for `web`, `bff`, `iam`, `app`, and `nacos`.

### Task 6: Verification and Docs

**Files:**
- Modify: `README.md`

- [x] Run Maven tests.
- [x] Run `docker compose --profile full up -d --build`.
- [x] Verify HTTP endpoints for captcha, login, and assistant list.
- [x] Use Playwright to log in at `http://127.0.0.1:3000/aibase/login` and assert navigation to `/aibase/appSpace/agent`.
- [x] Update README with one-command startup and dev credentials.
- [ ] Commit and push to `origin/main`.
