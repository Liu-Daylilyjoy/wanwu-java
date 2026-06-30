# Development Login Accounts

Date: 2026-06-30

The Docker development environment has two built-in IAM accounts. They are code-backed development accounts, not the final reproduced IAM user/role persistence model.

| Username | Token | Permission scope | Intended check |
| --- | --- | --- | --- |
| `admin` | `dev-token` | `app`, `app.agent`, `api_key`, `api_key.api_key_management` | Verify the implemented Agent and API Key menus without exposing unreproduced modules. |
| `app` | `dev-token-app` | `app` and `app.agent` only | Verify the minimal Agent-only view. |

Password and captcha are development placeholders. Use any non-empty password, such as `x`, and captcha code `1234`.

Frontend permissions are intentionally narrower than `web/src/router/constants.js` while the Java reproduction is still incomplete. Unreproduced modules stay hidden until their backend slices are implemented, tested, and verified through Docker Compose. The ontology agent menu has been removed from this reproduction scope.

Current limitation: BFF business write operations still map request context to the shared development organization and user until the full IAM user/org/role persistence slice is reproduced.
