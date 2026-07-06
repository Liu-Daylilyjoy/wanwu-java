# General Agent Menu Removal

## Goal

The frontend contains a `generalAgent` / WanwuBot page, but this Java reproduction no longer exposes it because the current acceptance scope does not need the ontology/general-agent feature. The frontend code remains unchanged; the menu is hidden by removing the `wga` permissions from IAM responses.

## Go Source Reference

- Go registers WanwuBot permissions through the BFF route/permission layer with `wga` and `wga.wanwu_bot`.
- The Java reproduction had mirrored those permissions for the admin account, which made the unchanged frontend render the General Agent menu and then call `/general-agent/...` style APIs.

## Java Behavior

- `IamServiceImpl` no longer grants `wga` or `wga.wanwu_bot` to the default admin account.
- `roleTemplate` no longer returns the WanwuBot route node.
- The app-only account is unchanged and still has only `app`, `app.rag`, `app.workflow`, and `app.agent`.
- Existing WGA callback compatibility endpoints are left in place for backend callback tests, but they are no longer reachable from the normal frontend menu.

## Verification

- `IamServiceImplTest` asserts the admin permission list excludes `wga` and has 30 permissions.
- BFF login/permission tests assert the permission array stops after `app_observability.statistic`.
