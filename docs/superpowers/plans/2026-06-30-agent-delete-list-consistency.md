# 智能体删除与列表一致性实施计划

## 目标

在前端零改动的前提下，补齐开发工作室卡片删除闭环：

- 前端 `appList.vue` 调用 `DELETE /user/api/v1/appspace/app`，请求体为 `appId` 和 `appType`。
- 当 `appType=agent` 时，Java BFF 调用 AppService 删除智能体草稿。
- 删除后 `GET /user/api/v1/appspace/assistant/list` 不再返回该智能体。
- 删除后 `GET /user/api/v1/assistant/draft` 返回前端可识别失败响应。
- MySQL 中 `apps`、`assistant_drafts`、`assistant_draft_configs` 三类开发态数据保持一致清理。

## Go 源码依据

- `internal/bff-service/server/http/handler/v1/app.go`：`DeleteAppSapceApp` 绑定 `DeleteAppSpaceAppRequest`，路由为 `DELETE /appspace/app`。
- `internal/bff-service/service/appspace.go`：先调用 app-service `DeleteApp` 清理发布/应用空间数据，再按 `appType` 分发；`agent` 分支调用 assistant-service `AssistantDelete`。
- `internal/app-service/client/orm/app.go`：`DeleteApp` 事务删除 `ApiKey`、`App`、`AppHistory`、`AppFavorite`、`AppUrl`。
- `internal/assistant-service/client/orm/assistant.go`：`DeleteAssistant` 事务删除 assistant 主表以及 workflow、MCP、tool、多智能体关系、snapshot、conversation。
- `web/src/components/appList.vue` 和 `web/src/api/appspace.js`：智能体卡片删除复用统一 `deleteApp(params)`，不是独立 `/assistant` 删除口。

## 非目标

- 不实现 RAG、workflow、chatflow 的真实删除。
- 不实现发布态版本、API Key、收藏、OpenUrl 的完整业务表，因为当前 Java 复刻还没有这些闭环。
- 不修改前端源码。
- 不引入新的数据库中间件或兼容 Java 8 以外的运行方式。

## 设计

- API 层新增 `AssistantDeleteCommand`，暴露 `AppService.deleteAssistant`。
- BFF 新增 `DELETE /appspace/app`，只对 `appType=agent` 执行真实删除；非 agent 返回明确失败，避免误报已删除。
- BFF 可保留并补充 `DELETE /assistant` 作为兼容入口，但前端验收以 `/appspace/app` 为准。
- AppService 删除前先按 `userId + orgId + assistantId` 校验归属，缺失时抛出 `assistant draft not found`。
- Repository 使用事务删除 `assistant_draft_configs`、`assistant_drafts`、`apps`，以草稿和 app 主记录删除结果判断是否成功。
- `assistantDraft` 对业务异常返回 `{ code: 1001, msg: "assistant draft not found" }`，避免删除后详情接口抛 500。

## 文件

- `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/AppService.java`
- `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AssistantDeleteCommand.java`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/rpc/AppServiceImpl.java`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/domain/ApplicationRepository.java`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/MybatisApplicationRepository.java`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/mapper/*Mapper.java`
- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiController.java`
- `wanwu-service-app/src/test/java/com/unicomai/wanwu/service/app/rpc/AppServiceImplTest.java`
- `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiControllerTest.java`
- `docs/superpowers/specs/2026-06-30-agent-delete-list-consistency-design.md`

## 执行步骤

1. 写 AppService 和 BFF 失败测试，覆盖删除成功、删除缺失、通用 `/appspace/app` 请求映射、删除后 draft 失败响应。
2. 运行 Docker Maven 测试，确认红灯来自缺少删除契约和实现。
3. 增加 API DTO、Service 方法、Repository 方法和 MyBatis 删除语句。
4. 实现 AppService 删除校验和 BFF DELETE 入口。
5. 运行 Maven 测试变绿。
6. 更新技术说明文档，运行 Docker Compose 一键启动、HTTP 创建/删除/列表/draft 验收和前端页面访问验收。
7. 提交并推送到 `main`。

## 验收命令

```powershell
docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff,wanwu-service-app -am test
docker compose --profile full build app bff
docker compose --profile full up -d --force-recreate app bff web iam
```

## 验收标准

- Maven 测试通过。
- `DELETE /user/api/v1/appspace/app` 删除 agent 返回 `code=0`。
- 删除后同名列表查询 `total=0`。
- 删除后 draft 查询返回 `code=1001`，消息为 `assistant draft not found`。
- MySQL 中目标 `assistantId` 对应的 `apps`、`assistant_drafts`、`assistant_draft_configs` 记录均清空。
- 前端 `/aibase/app/agent` 或当前工作室页面能加载列表，删除后触发刷新且不再显示该卡片。
