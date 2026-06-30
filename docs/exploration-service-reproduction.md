# Exploration Square Reproduction

Date: 2026-06-30

## Original Go Mapping

- Frontend callers:
  - `web/src/api/explore.js`
  - `web/src/api/workflow.js` for `getExplorationFlowList`
  - `web/src/views/exploreSquare/index.vue`
  - `web/src/components/appList.vue`
- Go BFF router: `D:\work\week3\wanwu\internal\bff-service\server\http\handler\router\v1\explore.go`.
- Related already-covered square routes:
  - MCP square routes under `internal\bff-service\server\http\handler\router\v1\mcp_square.go`
  - Prompt template routes under `explore.go` and `tool.go`
  - Skill square routes under `explore.go` and `skill.go`

## Covered Java Behavior

- `wanwu-service-bff` now exposes:
  - `GET /user/api/v1/exploration/app/list`
  - `POST /user/api/v1/exploration/app/favorite`
  - `GET /user/api/v1/exploration/app/history`
- App square list reuses `AppService.listApplications` and enriches rows with frontend-required `isFavorite`, `user`, `avatar`, `createdAt`, and `publishType` defaults.
- Favorite state is kept in the Docker development BFF memory for immediate frontend readback.
- Existing resource controllers already expose MCP square, prompt template square, and Skill square routes.
- The admin development account now exposes `exploration`, `exploration.app`, `exploration.mcp`, `exploration.template`, and `exploration.skill`.

## Verification

Executed in Docker with Java 8:

- `mvn -q -pl wanwu-service-bff,wanwu-service-iam -am "-Dtest=WanwuFrontendApiControllerTest,IamServiceImplTest" -DfailIfNoTests=false test`
- BFF contract test: `WanwuFrontendApiControllerTest` covers app square list/favorite/history contracts and square permissions.
- IAM service test: `IamServiceImplTest` covers square permissions and role-template children.

Frontend-entry smoke target:

- `http://localhost:3000/user/api/v1/base/login` returns exploration permissions for `admin`.
- `/exploration/app/list` returns app cards with `isFavorite` and `user.userName`.
- `/exploration/app/favorite` toggles favorite state.
- `/exploration/app/history` returns an empty frontend-compatible list.

## Current Boundary

This slice is a frontend-compatible square navigation loop. It prevents the zero-change frontend square menu and app-square page from being hidden or receiving backend 404s.

It does not yet implement:

- Persistent marketplace favorite/history storage.
- Real marketplace ranking, recommendation, or browse-history recording.
- Chatflow marketplace routes.
- Full public template workflow square routes.
- Deeper published app runtime beyond the Agent/RAG/Workflow shells already reproduced in earlier slices.
