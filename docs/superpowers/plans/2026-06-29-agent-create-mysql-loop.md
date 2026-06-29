# Plan: Agent Create MySQL Loop

Date: 2026-06-29

## Context

We are implementing the next vertical slice of the Wanwu Java reproduction: a frontend-zero-change agent creation loop backed by MySQL.

The source Go app-service uses Viper config, GORM, MySQL schema initialization through Docker init scripts, and an app table that supports publish/list behavior. The Java reproduction will use explicit Flyway migrations and MyBatis-Plus instead of auto-migration.

## Steps

1. Capture the frontend contract.
   - Confirm the real frontend endpoints and response fields from source code.
   - If the running browser flow is available, observe the create request through Playwright network logs.

2. Write failing tests.
   - BFF test: `POST /user/api/v1/assistant` accepts the frontend body and returns `data.assistantId`.
   - BFF test: `/appspace/assistant/list` forwards name filtering and returns list payloads in the frontend shape.
   - App-service test: creating an assistant produces a persisted app-space row and list returns it newest first.

3. Implement API contracts.
   - Add app DTOs for create command/result and list query context.
   - Extend `AppService` with assistant creation.
   - Keep DTOs Java 8 compatible and serializable for Dubbo.

4. Implement app-service persistence.
   - Add `wanwu-common-data` dependency to app-service.
   - Add datasource/Flyway config.
   - Add Flyway migration for `apps` and `assistant_drafts`.
   - Add entity, mapper, repository, and service behavior.

5. Implement BFF compatibility.
   - Add `POST /user/api/v1/assistant`.
   - Add `GET /user/api/v1/assistant/draft` for the post-create editor route.
   - Add empty selector placeholders for editor-only dependencies not yet migrated.
   - Fill dev user/org defaults when frontend does not send explicit context.
   - Return `FrontendResponse` with exactly the fields the existing Vue code expects.

6. Wire Docker Compose.
   - Move MySQL into the `full` startup path.
   - Mount schema init SQL for service databases.
   - Make app-service wait for healthy MySQL and Nacos.
   - Pass datasource environment variables into app-service.

7. Verify.
   - Run the fastest Maven tests first.
   - Build app-service/BFF modules.
   - Start Docker Compose full profile.
   - Verify HTTP endpoints with `curl` or equivalent.
   - Use Playwright against `http://localhost:3000` for login, create agent, list/refresh.

8. Document and commit.
   - Update README with the MySQL-backed full startup behavior.
   - Commit and push the completed slice to `main`.

## Verification Result

- Docker Maven tests passed for `wanwu-service-bff` and `wanwu-service-app` with their dependencies.
- `docker compose --profile full config --quiet` passed.
- `docker compose --profile full` is running MySQL, Nacos, IAM, app-service, BFF, and web.
- Flyway created `apps`, `assistant_drafts`, and `flyway_schema_history` in `app_service`.
- HTTP create/list/draft endpoints returned the persisted assistant from MySQL.
- Playwright exercised the real frontend with zero frontend code changes:
  - login at `http://localhost:3000/aibase/login`
  - create agent through the dialog
  - enter the post-create editor route
  - return to app-space list
  - confirm the created agent is visible
  - confirm zero `/user/api/v1/*` 4xx/5xx responses

## Checkpoints

- Stop after red tests if the test setup reveals an incompatible module boundary.
- Stop after Docker startup if MySQL cannot initialize in the user's Docker environment.
- Do not alter frontend source unless the user explicitly changes the zero-modification constraint.
