# 智能体删除与列表一致性设计说明

## 背景

当前复刻已经完成智能体创建、列表、草稿回显、基础信息编辑、配置保存和 MySQL 持久化。原前端的开发工作室卡片删除不调用独立智能体接口，而是复用应用空间统一删除接口。因此本阶段补齐 `DELETE /user/api/v1/appspace/app`，让前端在零改动下完成智能体删除。

## 原 Go 项目链路

前端：

- `web/src/components/appList.vue` 的 `handleDelete` 组装 `{ appId, appType }`。
- `web/src/api/appspace.js` 的 `deleteApp` 发送 `DELETE ${USER_API}/appspace/app`，请求体透传。

BFF：

- `internal/bff-service/server/http/handler/router/v1/common.go` 注册 `DELETE /appspace/app`。
- `internal/bff-service/server/http/handler/v1/app.go` 绑定 `DeleteAppSpaceAppRequest`，字段为 `appId`、`appType`。
- `internal/bff-service/service/appspace.go` 先调用 app-service `DeleteApp`，再按 `appType` 分发领域删除；`agent` 分支调用 assistant-service `AssistantDelete`。

下游服务：

- app-service 删除发布态/应用空间关联数据，包括 app、history、favorite、api key、open url。
- assistant-service 删除 assistant 主记录以及 workflow、MCP、tool、多智能体关系、snapshot、conversation 等相关数据。

## Java 复刻契约

本阶段 Java 服务提供两个入口：

- `DELETE /user/api/v1/appspace/app`
  - 请求体：`{ "appId": "...", "appType": "agent" }`
  - 成功：`{ "code": 0, "msg": "success", "data": {} }`
  - 智能体不存在：`{ "code": 1001, "msg": "assistant draft not found" }`
  - 非 agent 类型：`{ "code": 1001, "msg": "unsupported app type" }`
- `DELETE /user/api/v1/assistant`
  - 请求体：`{ "assistantId": "..." }`
  - 作为兼容入口保留，当前前端验收不依赖它。

删除后：

- `GET /user/api/v1/appspace/assistant/list` 不再返回该智能体。
- `GET /user/api/v1/assistant/draft?assistantId=...` 返回 `code=1001`，不抛 500。

## 分层实现

- `wanwu-api`
  - 新增 `AssistantDeleteCommand`。
  - `AppService` 增加 `deleteAssistant`。
- `wanwu-service-bff`
  - 适配 `/appspace/app` 请求体到 `AssistantDeleteCommand`。
  - 只对 `appType=agent` 执行真实删除。
  - 捕获 `IllegalArgumentException` 并转换为前端统一失败响应。
- `wanwu-service-app`
  - `AppServiceImpl` 负责参数校验、默认开发用户/组织、归属校验。
  - `ApplicationRepository` 增加删除方法。
  - MyBatis 仓储使用事务清理开发态三表。

## 持久化一致性

当前 Java 复刻尚未实现 Go 项目完整发布态和开放 URL 表，因此本阶段删除范围聚焦已经落地的开发态闭环：

- `apps`
- `assistant_drafts`
- `assistant_draft_configs`

删除顺序为：

1. `assistant_draft_configs`
2. `assistant_drafts`
3. `apps`

配置表允许不存在，用于兼容旧数据；`assistant_drafts` 和 `apps` 必须同时删除成功，否则服务层返回 `assistant draft not found`。

## 测试覆盖

- AppService：
  - 删除已存在智能体后，列表为空、draft 查询失败、配置记录消失。
  - 删除不存在智能体返回 `assistant draft not found`。
- BFF：
  - `DELETE /appspace/app` 正确映射 `appId/appType` 到 `AssistantDeleteCommand`。
  - 下游业务异常转换为 `code=1001`。
  - 删除后 draft 缺失异常转换为前端失败响应。

## 验收记录

已通过 Docker Java 8 Maven 测试：

```powershell
docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff,wanwu-service-app -am test
```

已完成 Docker Compose 验收：

- `docker compose --profile full config` 通过。
- `docker compose --profile full build app bff web` 通过。
- `docker compose --profile full up -d --force-recreate --no-build` 已拉起 full profile。
- 容器状态：`app`、`bff`、`iam`、`mysql`、`nacos` 均 healthy，`web` 正常运行并暴露 `3000`。
- HTTP 闭环通过：创建智能体、保存配置、删除、列表查询、draft 缺失查询均符合预期。
- MySQL 验收通过：目标 `assistantId` 在 `apps`、`assistant_drafts`、`assistant_draft_configs` 中计数均为 0。
- 前端验收通过：Playwright 已打开 `http://localhost:3000/aibase/` 并截图到 `output/playwright/agent-delete-frontend.png`，页面正常渲染登录页。
