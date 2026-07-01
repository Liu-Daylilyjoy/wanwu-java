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

- User info is derived from the built-in development tokens `dev-token` and `dev-token-app`.
- Password and avatar update routes return success-compatible responses for frontend workflows.
- Email register, password reset, and two-stage email-login routes now exist with deterministic development responses. They keep the frontend route contract available while the platform custom flags still advertise email register/reset/login as disabled.
- `POST /base/login/email` returns the temporary development token for `admin` or `app`; `POST/PUT /user/login` returns the full login-session shape, including organization, language, and the same permission split as the built-in development accounts.
- Avatar upload stores the image under the BFF local temp directory and returns `key/path` like the Go avatar API.
- Language select returns `zh` and `en` entries with `zh` as default.
- Doc Center returns deterministic local Markdown seeds for menu, search, entry, and Markdown content.

## Current Boundary

- User profile persistence, password policy enforcement, real email delivery, email-code verification, and avatar object-storage lifecycle are still future IAM/Object Storage slices.
- The email auth endpoints are development compatibility shells, not a completed reproduction of the Go IAM email RPC flow.
- The Doc Center uses in-code Markdown seeds, not the Go static manual directory and riot search index.
- This slice is intended to eliminate frontend `Not Found` errors and provide stable UI navigation while deeper persistence is reproduced.

## Verification

- `WanwuCommonApiControllerTest` covers user info, language, password, email register/reset/login compatibility, avatar upload/download/update, doc-center entry, menu, Markdown, and search contracts.
