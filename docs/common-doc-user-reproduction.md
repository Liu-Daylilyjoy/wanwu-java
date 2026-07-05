# Common User And Doc Center Compatibility

Date: 2026-07-01

## Go Source Baseline

The Go BFF common router exposes user and documentation helpers under `/user/api/v1`:

- `GET /user/info`
- `PUT /user/password`
- `POST /base/register/email/code`
- `POST /base/register/email`
- `POST /base/password/email/code`
- `POST /base/password/email`
- `POST /base/login/email`
- `POST /user/login/email/code`
- `POST /user/login`
- `PUT /user/login`
- `POST /avatar`
- `PUT /user/avatar`
- `GET /base/language/select`
- `PUT /user/language`
- `GET /doc_center`
- `GET /doc_center/menu`
- `GET /doc_center/markdown`
- `GET /doc_center/search`

The unchanged frontend uses these routes from `web/src/api/user.js` and `web/src/api/docs.js` for Personal Center, language switching, the documentation menu, documentation search, and the documentation download dialog.

## Java Reproduction

`WanwuCommonApiController` adds a development-compatible BFF shell:

- User info is read from Java `IamService` when available, with built-in development token fallback for isolated BFF tests.
- Password, avatar, and language update routes now proxy to Java `IamService`; avatar/language changes are visible through both `/user/info` and `/user/permission`, and password changes advance a non-secret development `passwordVersion`.
- Email register, password reset, and two-stage email-login routes now issue the Docker development code `123456`, validate and consume it once, and call Java `IamService` for registration, password reset, or first-login password changes when IAM is available. They keep the frontend route contract available while the platform custom flags still advertise email register/reset/login as disabled.
- `POST /base/login/email` returns the temporary development token for `admin` or `app`; `POST/PUT /user/login` returns the full login-session shape, including organization, language, and the same permission split as the built-in development accounts.
- Avatar upload stores the image under the BFF local temp directory and returns `key/path` like the Go avatar API.
- Language select returns `zh` and `en` entries with `zh` as default.
- Doc Center indexes the Go `static/manual` asset tree from `wanwu-service-bff/src/main/resources/static/manual` for menu, search, entry, and Markdown content. It builds Go-style directory menus, rewrites relative Markdown images to Go's `../../../user/api/v1/static/manual/...` prefix, and serves classpath resources through `/user/api/v1/static/manual/**`.

## Current Boundary

- User profile reads and avatar/language/password-version updates now survive Docker restarts through the IAM JSON compatibility repository. Password hashing/policy enforcement and the development email-code validation path are covered through IAM where available; SMTP delivery, exact email policy parity, and avatar object-storage lifecycle are still future IAM/Object Storage slices.
- The email auth endpoints are development compatibility shells, not a completed reproduction of the Go IAM email delivery and verification RPC flow.
- The Java repo now carries 725 Go manual files, including 123 Markdown documents and 92.6MB of static assets, excluding the `10.本体智能体` manual directory because ontology-agent functionality is intentionally removed from this reproduction scope.
- Search remains a deterministic Java substring scan rather than Go's riot search engine, so ranking/tokenization is not exact Go parity yet.
- This slice is intended to eliminate frontend `Not Found` errors and provide stable UI navigation while deeper persistence is reproduced.

## Verification

- `IamServiceImplTest` covers common profile persistence for language, avatar, and password-version updates.
- `WanwuCommonApiControllerTest` covers user info, IAM-backed language/password/avatar proxying, email register/reset/login compatibility, avatar upload/download/update, doc-center entry, menu, Markdown, and search contracts.
- `WanwuStaticDocsControllerTest` covers frontend static CSV/XLSX templates plus `/user/api/v1/static/manual/**` classpath resource serving and traversal rejection.
