# 智能体草稿编辑闭环实施计划

## 目标

在前端零改动的前提下，让当前 Wanwu 前端能真实完成智能体草稿的编辑闭环：

- `PUT /user/api/v1/assistant` 保存基础信息。
- `PUT /user/api/v1/assistant/config` 保存草稿配置。
- `GET /user/api/v1/assistant/draft` 回显基础信息和配置。
- `GET /user/api/v1/appspace/assistant/list` 反映基础信息更新。
- MySQL 持久化当前草稿配置，Docker Compose 一键启动后可验收。

## 非目标

- 不实现发布、版本、删除。
- 不实现真实模型列表、真实对话运行、真实知识库/工具/MCP 选择器。
- 不修改前端源码，不增加新的前端自动保存逻辑。
- 不做配置历史，只保存当前草稿配置。

## 设计

- App 服务继续作为业务源头，BFF 只做前端协议适配。
- 基础信息仍落在 `apps` 与 `assistant_drafts`。
- 新增 `assistant_draft_configs` 表保存配置 JSON 和少量独立文本字段。
- 配置更新要求智能体已存在且属于当前用户/组织；不存在时返回前端可识别失败响应。
- 兼容旧数据：没有配置行的旧草稿，`GET /assistant/draft` 返回默认配置；首次 `PUT /assistant/config` 创建配置行。

## 文件

- `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/AppService.java`
- `wanwu-api/src/main/java/com/unicomai/wanwu/api/app/dto/*`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/rpc/AppServiceImpl.java`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/domain/*`
- `wanwu-service-app/src/main/java/com/unicomai/wanwu/service/app/persistence/*`
- `wanwu-service-app/src/main/resources/db/migration/V2__create_assistant_draft_configs.sql`
- `wanwu-service-app/src/test/java/com/unicomai/wanwu/service/app/rpc/AppServiceImplTest.java`
- `wanwu-service-bff/src/main/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiController.java`
- `wanwu-service-bff/src/test/java/com/unicomai/wanwu/service/bff/web/WanwuFrontendApiControllerTest.java`
- `README.md`

## 执行步骤

1. 先写 AppService 与 BFF 的失败测试，覆盖基础信息更新、配置保存回显、不存在智能体失败。
2. 增加 API DTO 和 `AppService` 方法。
3. 增加配置实体、Mapper、Repository 方法和 Flyway V2 迁移。
4. 在 `AppServiceImpl` 中实现校验、默认配置兼容、JSON 序列化/反序列化。
5. 在 BFF 中接入两个 PUT 接口，并将业务参数错误转为前端失败响应。
6. 运行 Docker Maven 测试、Compose 启动、HTTP 与浏览器验收。
7. 更新 README，提交并推送到 `main`。

## 验收命令

```powershell
docker run --rm -v "$env:USERPROFILE\.m2:/root/.m2" -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-8 mvn -q -pl wanwu-service-bff,wanwu-service-app -am test
docker compose --profile full config
docker compose --profile full up -d
```

## 验收标准

- Maven 测试通过。
- Flyway 能在 MySQL 上创建 V2 表。
- 前端创建智能体后，可进入编辑页并保存草稿配置。
- 重新获取 draft 能看到保存后的 `instructions`、`prologue` 和配置 JSON。
- 列表能显示更新后的名称、描述、头像和分类。
