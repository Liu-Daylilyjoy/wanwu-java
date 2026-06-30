# Agent OpenURL Loop Design

## Goal

Recreate the original Go project's agent OpenURL capability in the Java remake so the existing frontend can create a public access URL, load the published agent by suffix, create a public conversation, stream a response, and read persisted conversation history without frontend code changes.

## Source Alignment

The original Go project integrates OpenURL in two layers:

- BFF user management APIs under `/user/api/v1/appspace/app/openurl*`.
- Public APIs under `/openurl/v1/agent/{suffix}*`.
- The frontend calls `/service/url/openurl/v1`, so the Java BFF also exposes this alias and nginx proxies it to BFF.
- App service stores `AppUrl` in MySQL with app id/type, legal text, expiry, random suffix, owner identity, status, and description.
- Public OpenURL access checks `status` and `expiredAt` before loading the assistant snapshot.

## Java Architecture

- `wanwu-api` owns OpenURL DTOs and `AppService` contracts.
- `wanwu-service-app` owns validation, suffix generation, MySQL persistence, and OpenURL status/expiry checks.
- `wanwu-service-bff` maps frontend payloads to service commands and exposes user/public HTTP routes.
- Public conversations reuse the existing assistant conversation persistence with `X-Client-ID` as user identity and OpenURL owner org as organization identity.

## Minimal Closed Loop

1. Publish an agent snapshot.
2. Create an OpenURL for that published agent.
3. List the OpenURL and receive a frontend-copyable `/service/url/openurl/v1/agent/{suffix}` path.
4. Load public OpenURL agent info by suffix.
5. Create and stream a public conversation.
6. Read/delete/clear public conversation history.

## Deferred Scope

OpenURL file upload and merge routes remain out of this slice. They require the file subsystem and are not necessary for the smallest frontend-visible business loop.
