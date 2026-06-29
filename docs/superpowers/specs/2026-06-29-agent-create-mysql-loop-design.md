# Agent Create MySQL Loop Design

Date: 2026-06-29

## Goal

Reproduce the smallest real business loop that the existing frontend can use without code changes:

1. Start the Java backend, frontend, Nacos, and MySQL with Docker Compose.
2. Log in through the frontend.
3. Open the agent app space.
4. Create an agent through the real frontend dialog.
5. Persist the created app in MySQL.
6. Refresh/restart and still list the created app from `/user/api/v1/appspace/assistant/list`.

## Original Go MySQL Integration

The Go app service initializes persistence in `cmd/app-service/main.go`:

- Load service config through Viper.
- Build a database connection through `pkg/db.New(config.Cfg().DB)`.
- Create the ORM client with `orm.NewClient(db)`.
- Run service bootstrap jobs.
- Start the gRPC server.

`pkg/db/client.go` selects the database implementation with `db.name`. For MySQL, TiDB, and OceanBase it uses the GORM MySQL driver and builds a DSN from address, user, password, and database. It enables `parseTime=true`, uses local time, configures open/idle pool sizes, disables GORM foreign-key migration constraints, and can enable SQL logging.

The app-service config uses the `app_service` schema with MySQL. The Go Docker Compose starts MySQL with an init script mounted under `/docker-entrypoint-initdb.d`; that script creates service schemas such as `iam_service`, `model_service`, `app_service`, `rag_service`, `assistant_service`, `knowledge_service`, `mcp_service`, `operate_service`, and `opencoze`.

The Go app-service model uses GORM auto-migration. For the app-space list/publish loop, the relevant table model has:

- `id`
- `created_at` and `updated_at` as millisecond timestamps
- `user_id`
- `org_id`
- `app_id`
- `app_type`
- `publish_type`

The Go `PublishApp` behavior is an upsert by `user_id`, `org_id`, `app_id`, and `app_type`; list filters by `user_id`, `org_id`, and `app_type`, ordered by descending id.

## Java Direction

The Java reproduction will not copy GORM auto-migration. It will use:

- Spring Boot 2.7.x on Java 8.
- Dubbo for service RPC.
- MyBatis-Plus for mapper/repository code.
- Flyway for explicit schema migrations.
- Docker Compose for the full runtime.

The first Java migration creates `apps` in the `app_service` schema:

```sql
CREATE TABLE apps (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  app_type VARCHAR(32) NOT NULL,
  publish_type VARCHAR(32) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_app_created_at (created_at),
  KEY idx_app_user_id (user_id),
  KEY idx_app_org_id (org_id),
  KEY idx_app_app_id (app_id),
  KEY idx_app_app_type (app_type),
  KEY idx_app_publish_type (publish_type),
  UNIQUE KEY uk_app_user_org_type_id (user_id, org_id, app_type, app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

The slice also adds `assistant_drafts` for the minimum frontend presentation and editor echo fields:

- `assistant_id`
- `name`
- `description`
- `avatar_key`
- `avatar_path`
- `category`
- `user_id`
- `org_id`
- millisecond `created_at` and `updated_at`

This deliberately keeps the Go-compatible app identity in `apps` while placing frontend-specific draft display fields in a separate table. Later assistant/editor migrations can deepen this table or split richer draft configuration into dedicated tables.

## Frontend Contract

No frontend code changes are allowed for this loop.

The current frontend uses:

- `GET /user/api/v1/appspace/assistant/list` for the agent app-space list.
- `POST /user/api/v1/assistant` for agent creation.
- `GET /user/api/v1/assistant/draft` after creation, because the frontend routes into the agent editor.
- Create success requires `res.code === 0` and `res.data.assistantId`.
- After creation, the frontend navigates to `/agent/test?id=<assistantId>` and marks the app list as changed.

The BFF therefore owns compatibility with the existing frontend request/response shape. The app service owns persisted app identity, listing, and minimum draft echo. The first implementation stores app-space identity and enough presentation fields for list/detail. Real model/chat execution, publishing, tool binding, and knowledge-base selection remain outside this slice.

To keep the existing editor page navigable without implementing unrelated domains, the BFF returns empty list placeholders for the editor's selector endpoints, including model, prompt, MCP, workflow, safe table, tool, skill, version list, knowledge select, and conversation draft history.

## Boundaries

In scope:

- MySQL service in one-command Docker Compose full startup.
- App-service datasource, Flyway migration, MyBatis-Plus mapper, repository, and Dubbo RPC behavior.
- BFF endpoint for frontend agent creation.
- BFF list and draft responses that render in the existing frontend.
- Empty selector placeholders needed by the create-success editor route.
- Docker and browser-level verification.

Out of scope:

- Full Go schema parity.
- Real model/chat execution.
- Upload/minio persistence.
- Redis-backed session hardening.
- Multi-tenant authorization beyond the dev user/org already reproduced in the IAM slice.

## Acceptance Criteria

- `docker compose --profile full up -d --build` starts frontend, BFF, IAM, app-service, Nacos, and MySQL.
- MySQL initializes the `app_service` schema and Flyway creates the `apps` and `assistant_drafts` tables.
- `POST /user/api/v1/assistant` returns `code: 0` with `data.assistantId`.
- `GET /user/api/v1/appspace/assistant/list` returns the created assistant in `data.list`.
- `GET /user/api/v1/assistant/draft` returns the created assistant's persisted name/description/avatar/category plus editor defaults.
- Restarting the Java services does not erase the created assistant.
- Playwright can exercise login, the real frontend create dialog, create-success editor route, and list refresh without frontend code changes or `/user/api/v1/*` errors.
