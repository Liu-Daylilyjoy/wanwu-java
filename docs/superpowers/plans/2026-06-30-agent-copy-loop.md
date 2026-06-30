# 智能体复制闭环实施计划

## 目标

在前端零改动的前提下，补齐开发工作室智能体复制能力：

- 前端 `appList.vue` 调用 `copyAgentApp({ assistantId })`。
- `web/src/api/appspace.js` 将请求发送到 `POST /user/api/v1/assistant/copy`。
- BFF 返回 `{ assistantId: "..." }` 后，前端跳转到新智能体编辑页。
- 新智能体复制原智能体的基础信息、分类、头像和当前草稿配置。
- 新智能体名称按 Go 源码规则使用 `原名_1`、`原名_2` 递增。

## Go 源码依据

- `internal/bff-service/server/http/handler/v1/assistant.go`：`AssistantCopy` 绑定 `AssistantIdRequest`，路由为 `POST /assistant/copy`。
- `internal/bff-service/service/assistant.go`：BFF 调用 assistant-service `AssistantCopy` 并返回 `AssistantCreateResp`。
- `internal/assistant-service/server/grpc/assistant/assistant.go`：复制前读取原智能体和关联的 workflow、MCP、tool、多智能体关系、skill。
- `internal/assistant-service/client/orm/assistant.go`：复制时新名称为 `原名_数字`，数字取现有同前缀名称中的最大值加 1。

## 非目标

- 不实现 workflow、MCP、tool、skill、多智能体关系的完整复制，因为当前 Java 复刻还没有落地这些表和闭环。
- 不修改前端源码。
- 不改变创建、编辑、删除已有契约。

## 设计

- API 层新增 `AssistantCopyCommand`，复用 `AssistantCreateResult` 作为返回。
- AppService 新增 `copyAssistant`。
- AppServiceImpl 负责：
  - 校验 `assistantId`。
  - 按 `userId + orgId + assistantId` 查找原草稿。
  - 生成新 `assistantId`。
  - 按 `原名_数字` 生成新名称。
  - 复制基础字段和草稿配置。
- Repository 新增：
  - `listAssistantNamesByPrefix` 用于寻找现有复制名称。
  - `copyAssistant` 用事务写入 `apps`、`assistant_drafts`、`assistant_draft_configs`。
- BFF 新增 `POST /assistant/copy`，将前端请求体映射为 `AssistantCopyCommand`。

## 文件

- `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/AppService.java`
- `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/AssistantCopyCommand.java`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/rpc/AppServiceImpl.java`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/domain/ApplicationRepository.java`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/MybatisApplicationRepository.java`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/mapper/AppMapper.java`
- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiController.java`
- `wanwu-service-app/src/test/java/com/unicomai/wanwu/service/app/rpc/AppServiceImplTest.java`
- `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiControllerTest.java`
- `docs/superpowers/specs/2026-06-30-agent-copy-loop-design.md`

## 执行步骤

1. 写 AppService 和 BFF 失败测试，覆盖复制成功、名称递增、配置复制、原智能体不存在。
2. 运行 Docker Maven 测试，确认红灯来自缺失复制契约。
3. 增加 API DTO、Service 方法、Repository 方法和 MyBatis 查询。
4. 实现复制业务和 BFF `POST /assistant/copy`。
5. 运行 Maven 测试变绿。
6. 更新技术说明文档，运行 Docker Compose 构建/启动、HTTP 复制闭环和前端访问验收。
7. 提交并推送到 `main`。

## 验收命令

```powershell
docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff,wanwu-service-app -am test
docker compose --profile full build app bff
docker compose --profile full up -d --force-recreate --no-build
```

## 验收标准

- Maven 测试通过。
- `POST /user/api/v1/assistant/copy` 返回新的 `assistantId`。
- 新智能体在列表中名称为 `原名_1` 或递增后缀。
- 新智能体 draft 能回显复制过来的配置。
- 原智能体仍然存在且配置不变。
- 前端容器仍能通过 `http://localhost:3000/aibase/` 正常访问。
