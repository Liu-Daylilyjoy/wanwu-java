# Model MySQL Persistence Slice

Date: 2026-06-30

## Go Source Baseline

Original files inspected:

- `D:\work\week3\wanwu\configs\microservice\model-service\configs\config.yaml`
- `D:\work\week3\wanwu\internal\model-service\client\model\model_imported.go`
- `D:\work\week3\wanwu\internal\model-service\client\model\model_experience_dialog.go`
- `D:\work\week3\wanwu\internal\model-service\client\model\model_experience_dialog_record.go`

The Go model service selects `db.name: mysql`, connects to the `model_service` schema, and uses GORM models for imported models and model-experience dialogs/records. Model rows carry provider, model type, model name, display name, icon path, active flag, provider config, description, publish date, scope, import source, user/org ownership, and millisecond timestamps.

## Java Coverage Added

This slice wires Java `wanwu-service-model` into Docker MySQL:

- `docker-compose.yml` passes `SPRING_DATASOURCE_*` to `model` and waits for healthy `mysql`.
- `bff` now waits for healthy `model`, reducing startup races for model pages/selectors.
- `wanwu-service-model` depends on `wanwu-common-data`.
- `wanwu-service-model/src/main/resources/db/migration/V1__create_model_records.sql` creates `model_records`.
- `ModelServiceImpl` loads persisted model records, model delete tombstones, model-experience dialogs, and model-experience records on startup.

## Persistence Shape

The current table is a compatibility snapshot table:

- `record_type`: model, model_deleted, dialog, records
- `record_id`: model id, dialog id, or `all` for the model-experience record list snapshot
- `payload`: JSON DTO/state payload
- `created_at` and `updated_at`: millisecond timestamps

The table preserves current frontend-compatible DTO behavior while providing restart durability. Later slices should normalize it into Go-equivalent model tables.

## Verified Behavior

- Unit tests cover JSON upsert/delete calls, startup reload, model ID sequence continuation, dialog reload, and experience-record reload.
- Docker smoke should create a model through `localhost:3000`, recreate model and BFF containers, verify the model still appears, and confirm rows in `model_service.model_records`.

## Remaining Gaps

- Normalize imported models, model-experience dialogs, and records into relational tables matching Go semantics.
- Implement real provider validation/inference callbacks.
- Persist provider-level credentials with encryption or secret references instead of plain development JSON.
- Add audit/history and stricter ownership enforcement.
