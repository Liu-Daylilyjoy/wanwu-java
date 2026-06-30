# Development Login Accounts

Date: 2026-06-30

The Docker development environment has two built-in IAM accounts. They are code-backed development accounts, not the final reproduced IAM user/role persistence model.

| Username | Token | Permission scope | Intended check |
| --- | --- | --- | --- |
| `admin` | `dev-token` | All frontend permissions from `web/src/router/constants.js` | Verify every menu currently exposed by the copied frontend router. |
| `app` | `dev-token-app` | `app` and `app.agent` only | Verify the minimal Agent-only view. |

Password and captcha are development placeholders. Use any non-empty password, such as `x`, and captcha code `1234`.

Current limitation: BFF business write operations still map request context to the shared development organization and user until the full IAM user/org/role persistence slice is reproduced.
