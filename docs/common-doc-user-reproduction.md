# Common User And Doc Center Compatibility

Date: 2026-06-30

## Go Source Baseline

The Go BFF common router exposes user and documentation helpers under `/user/api/v1`:

- `GET /user/info`
- `PUT /user/password`
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
- Avatar upload stores the image under the BFF local temp directory and returns `key/path` like the Go avatar API.
- Language select returns `zh` and `en` entries with `zh` as default.
- Doc Center returns deterministic local Markdown seeds for menu, search, entry, and Markdown content.

## Current Boundary

- User profile persistence, password policy enforcement, email verification, and avatar object-storage lifecycle are still future IAM/Object Storage slices.
- The Doc Center uses in-code Markdown seeds, not the Go static manual directory and riot search index.
- This slice is intended to eliminate frontend `Not Found` errors and provide stable UI navigation while deeper persistence is reproduced.

## Verification

- `WanwuCommonApiControllerTest` covers user info, language, password, avatar upload/download/update, doc-center entry, menu, Markdown, and search contracts.
