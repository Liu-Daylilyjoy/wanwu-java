# 智能体草稿编辑闭环设计说明

## 背景

当前阶段的目标是让原 Wanwu 前端在不改源码的情况下，真实访问 Java BFF/App 服务，完成智能体草稿编辑的最小业务闭环。上一阶段已经完成创建、列表、详情默认回显和 MySQL 持久化；本阶段补齐编辑保存。

## 接口契约

- `PUT /user/api/v1/assistant`
  - 保存基础信息：`assistantId`、`name`、`desc`、`category`、`avatar.key`、`avatar.path`。
  - 成功返回 `{ code: 0, msg: "success", data: {} }`。
  - 智能体不存在或参数错误时返回 `{ code: 1001, msg: "..." }`。

- `PUT /user/api/v1/assistant/config`
  - 保存编辑器配置：`prologue`、`instructions`、`memoryConfig`、`knowledgeBaseConfig`、`modelConfig`、`safetyConfig`、`visionConfig`、`rerankConfig`、`recommendConfig`、`recommendQuestion`。
  - 服务端只做存在性和 JSON 可序列化校验，不做深层业务规则校验。
  - 不隐式创建智能体；必须已有 `assistant_drafts` 且属于当前用户/组织。

- `GET /user/api/v1/assistant/draft`
  - 回显基础信息和保存后的配置。
  - 对旧数据兼容：没有配置行时返回前端可工作的默认配置。

- `GET /user/api/v1/appspace/assistant/list`
  - 基础信息更新后，列表反映新的名称、描述、头像和分类。

## 持久化

基础信息继续使用：

- `apps`
- `assistant_drafts`

新增当前草稿配置表：

- `assistant_draft_configs`

配置表字段包括：

- 文本字段：`prologue`、`instructions`
- JSON 字段：`memory_config`、`knowledge_base_config`、`model_config`、`safety_config`、`vision_config`、`rerank_config`、`recommend_config`、`recommend_questions`

创建智能体时会插入一条默认配置行；旧数据没有配置行时，服务层按默认值回显，首次保存配置时 upsert 创建。

## 分层

- `wanwu-api` 暴露 Dubbo 契约和 DTO。
- `wanwu-service-bff` 适配前端请求体和统一响应。
- `wanwu-service-app` 负责归属校验、默认配置、JSON 序列化和 MySQL 持久化。
- 前端 `web/` 保持零改动。

## 验收

- Docker Maven 测试通过。
- Docker Compose full profile 可启动，App 服务 Flyway 从 v1 迁移到 v2。
- HTTP 验收完成：创建、基础信息更新、配置保存、draft/list 回显均为 `code=0`。
- Playwright 前端验收完成：真实页面打开 `/aibase/agent/test?id=...`，通过 UI 修改系统提示词和开场白，前端自动调用 `PUT /assistant/config`，刷新后仍能回显。
