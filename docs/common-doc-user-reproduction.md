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
- Email register, password reset, and two-stage email-login routes now exist with deterministic development responses. They keep the frontend route contract available while the platform custom flags still advertise email register/reset/login as disabled.
- `POST /base/login/email` returns the temporary development token for `admin` or `app`; `POST/PUT /user/login` returns the full login-session shape, including organization, language, and the same permission split as the built-in development accounts.
- Avatar upload stores the image under the BFF local temp directory and returns `key/path` like the Go avatar API.
- Language select returns `zh` and `en` entries with `zh` as default.
- Doc Center indexes Markdown files from `wanwu-service-bff/src/main/resources/static/manual` for menu, search, entry, and Markdown content, serves classpath resources through `/user/api/v1/static/manual/**`, and keeps deterministic local Markdown seeds as a fallback.

## Current Boundary

- User profile reads and avatar/language/password-version updates now survive Docker restarts through the IAM JSON compatibility repository. Real password hashing/policy enforcement, email delivery, email-code verification, and avatar object-storage lifecycle are still future IAM/Object Storage slices.
- The email auth endpoints are development compatibility shells, not a completed reproduction of the Go IAM email RPC flow.
- The Doc Center uses a lightweight Java classpath Markdown index rather than Go's full `static/manual` asset directory and riot search engine. The static manual route now exists for Markdown images and attachments, but the current repository intentionally includes lightweight Markdown seeds; copying the full 99MB Go manual asset tree remains a separate asset-porting slice.
- This slice is intended to eliminate frontend `Not Found` errors and provide stable UI navigation while deeper persistence is reproduced.

## Verification

- `IamServiceImplTest` covers common profile persistence for language, avatar, and password-version updates.
- `WanwuCommonApiControllerTest` covers user info, IAM-backed language/password/avatar proxying, email register/reset/login compatibility, avatar upload/download/update, doc-center entry, menu, Markdown, and search contracts.
- `WanwuStaticDocsControllerTest` covers frontend static CSV/XLSX templates plus `/user/api/v1/static/manual/**` classpath resource serving and traversal rejection.
