# 智能体复制闭环设计说明

## 背景

删除闭环完成后，开发工作室卡片上的另一个高频操作是复制智能体。前端已经具备调用和跳转逻辑：复制成功后拿到新的 `assistantId`，跳转到 `/agent/test?id=...`。因此本阶段补齐 Java BFF/App 对 `POST /user/api/v1/assistant/copy` 的支持。

## 原 Go 项目链路

前端：

- `web/src/components/appList.vue` 中智能体卡片的 `copy` 命令调用 `copyAgentApp({ assistantId })`。
- `web/src/api/appspace.js` 将请求发送到 `POST ${USER_API}/assistant/copy`。
- 成功后读取 `res.data.assistantId` 并跳转到新智能体编辑页。

BFF：

- `internal/bff-service/server/http/handler/router/v1/assistant.go` 注册 `POST /assistant/copy`。
- `internal/bff-service/server/http/handler/v1/assistant.go` 使用 `AssistantIdRequest` 绑定请求体。
- `internal/bff-service/service/assistant.go` 调用 assistant-service `AssistantCopy` 并返回 `AssistantCreateResp`。

assistant-service：

- 先读取原智能体。
- 再读取 workflow、MCP、tool、多智能体关系、skill 等关联数据。
- ORM 复制时名称使用 `原名_数字`，数字取同前缀已有名称最大值加 1。

## Java 复刻契约

- `POST /user/api/v1/assistant/copy`
  - 请求体：`{ "assistantId": "..." }`
  - 成功：`{ "code": 0, "msg": "success", "data": { "assistantId": "..." } }`
  - 原智能体不存在：`{ "code": 1001, "msg": "assistant draft not found" }`

复制后：

- 原智能体仍存在。
- 新智能体列表名称为 `原名_1`、`原名_2` 递增。
- 新智能体 draft 回显复制过来的配置。

## 分层实现

- `wanwu-api`
  - 新增 `AssistantCopyCommand`。
  - `AppService` 增加 `copyAssistant`，返回 `AssistantCreateResult`。
- `wanwu-service-bff`
  - 增加 `POST /assistant/copy`。
  - 将请求体和当前用户上下文转换为 `AssistantCopyCommand`。
  - 将业务异常转换为前端统一失败响应。
- `wanwu-service-app`
  - 校验原智能体归属。
  - 生成新 `assistantId`。
  - 查询同前缀名称并生成下一个复制名称。
  - 复制基础信息和草稿配置。
- `wanwu-service-app` 持久化层
  - 复用现有 `saveAssistant` 写入 `apps`、`assistant_drafts` 和默认配置。
  - 随后 upsert 复制后的 `assistant_draft_configs`。

## 复刻范围

当前 Java 复刻已落地的智能体开发态数据包括：

- `apps`
- `assistant_drafts`
- `assistant_draft_configs`

Go 项目复制的 workflow、MCP、tool、skill、多智能体关系属于后续闭环；本阶段不虚构未落地表，也不返回假数据。

## 测试覆盖

- AppService：
  - 复制原智能体两次后生成 `原名_1`、`原名_2`。
  - 新智能体基础信息和配置与原智能体一致。
  - 原智能体不存在时返回 `assistant draft not found`。
- BFF：
  - `POST /assistant/copy` 正确映射 `assistantId`、`userId`、`orgId`。
  - 成功响应包含新 `assistantId`。
  - 缺失异常转换为 `code=1001`。

## 验收记录

已通过 Docker Java 8 Maven 测试：

```powershell
docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff,wanwu-service-app -am test
```

已完成 Docker Compose 和 HTTP 验收：

- `docker compose --profile full build app bff` 通过。
- `docker compose --profile full up -d --force-recreate --no-build` 通过。
- full profile 容器状态：`app`、`bff`、`iam`、`mysql`、`nacos` 均 healthy，`web` 正常运行。
- 前端容器访问 `http://localhost:3000/aibase/` 返回 200。
- HTTP 闭环通过：创建原智能体、保存配置、连续复制两次、列表查询、复制后 draft 回显均符合预期。
- 实际验证样例：`CopyLoop-20260630103706` 复制后生成 `CopyLoop-20260630103706_1` 和 `CopyLoop-20260630103706_2`，复制体 draft 的 `instructions` 为 `copy instructions`，`memoryConfig.maxHistoryLength` 为 `13`。
