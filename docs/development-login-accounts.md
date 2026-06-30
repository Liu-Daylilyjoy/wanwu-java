# Development Login Accounts

Date: 2026-06-30

The Docker development environment has two built-in IAM accounts. They are code-backed development accounts, not the final reproduced IAM user/role persistence model.

| Username | Token | Permission scope | Intended check |
| --- | --- | --- | --- |
| `admin` | `dev-token` | `permission`, `permission.user`, `permission.org`, `permission.role`, `model`, `model.model_management`, `app`, `app.agent`, `api_key`, `api_key.api_key_management`, `resource.knowledge` | Verify permission-management read views, Model Management, Knowledge, Agent, and API Key menus without exposing unreproduced modules. |
| `app` | `dev-token-app` | `app` and `app.agent` only | Verify the minimal Agent-only view. |

Password and captcha are development placeholders. Use any non-empty password, such as `x`, and captcha code `1234`.

Frontend permissions are intentionally narrower than `web/src/router/constants.js` while the Java reproduction is still incomplete. Unreproduced modules stay hidden until their backend slices are implemented, tested, and verified through Docker Compose. Permission management currently covers the read-only user, role, organization, role-select, and role-template views; write operations are still a later IAM persistence slice. Model Management currently covers the model list, detail, import/update/delete/status, provider list, recommendation list, and model select endpoints with a Docker development in-memory repository. Knowledge currently covers knowledge base create/list/update/delete, tag binding, splitter lists, document empty-state views, metadata shells, and permission compatibility using a Docker development in-memory repository. The ontology agent menu has been removed from this reproduction scope.

Current limitation: BFF business write operations still map request context to the shared development organization and user until the full IAM user/org/role persistence slice is reproduced.
