# Wanwu Java

这是 `D:\work\week3\wanwu` 的 Java 8 微服务骨架版。当前阶段先复刻服务边界和可持续开发结构，不迁移 Go 业务实现。

## 技术基线

- Java 8 target
- Spring Boot 2.7.18
- Maven 多模块
- Apache Dubbo 3
- Nacos 2.x
- MyBatis-Plus + Flyway 基础模块

## 模块结构

```text
web                       原 Wanwu 前端源码，未做业务代码改造
wanwu-api                 Java Dubbo RPC 接口和 DTO
wanwu-common-core         统一响应、错误码、异常、trace、tenant 上下文
wanwu-common-web          健康检查、trace filter、全局异常处理
wanwu-common-rpc          Dubbo 公共约定
wanwu-common-data         MyBatis-Plus/Flyway 基础设施
wanwu-service-bff         BFF 服务骨架
wanwu-service-iam         IAM 服务骨架
wanwu-service-model       模型服务骨架
wanwu-service-knowledge   知识库服务骨架
wanwu-service-agent       Agent 服务骨架
wanwu-service-assistant   Assistant 服务骨架
wanwu-service-app         App 服务骨架
wanwu-service-mcp         MCP 服务骨架
wanwu-service-operate     运营服务骨架
wanwu-service-rag         RAG 服务骨架
```

## 前端

前端已从原项目 `D:\work\week3\wanwu\web` 原样迁入到 `web`，未改业务代码。仓库不提交 `node_modules`、`dist` 和 `.env.*`；如果需要本地开发，请按前端项目自己的 `package.json` 和锁文件安装依赖，并在本机提供对应环境变量文件。

## 本地构建

需要 Maven 3.8+ 和 JDK 8+。工程用 `source/target=1.8`，因此可以在更高版本 JDK 上编译 Java 8 字节码。

```powershell
mvn clean test
```

只启动 BFF 服务：

```powershell
mvn -pl wanwu-service-bff -am spring-boot:run
```

默认 `DUBBO_REGISTRY_ADDRESS` 是 `N/A`，服务可以先不依赖 Nacos 单独启动。启用 Nacos 时：

```powershell
$env:DUBBO_REGISTRY_ADDRESS = "nacos://127.0.0.1:8848"
mvn -pl wanwu-service-bff -am spring-boot:run
```

## 本地基础设施

复制 `.env.example` 为 `.env` 后，用环境变量填入本地数据库凭据。不要把 `.env` 提交到仓库。

```powershell
docker compose --env-file .env up -d nacos redis mysql
```

Nacos 控制台：

```text
http://127.0.0.1:8848/nacos
```

## 纯 Docker 启动 BFF

如果本机不安装 Java/Maven，只依赖 Docker，可以直接构建并启动 BFF 骨架服务。Compose 会同时启动 Nacos，BFF 默认注册到 `nacos://nacos:8848`：

```powershell
docker compose --profile app up --build bff
```

启动后访问：

```text
http://127.0.0.1:8080/internal/health
http://127.0.0.1:8080/api/bff/service-info
```

## Docker 一键启动前后端最小闭环

本阶段已经把前端也纳入 Compose。只需要 Docker，不要求本机安装 Java、Maven 或 Node：

```powershell
docker compose --profile full up -d --build
```

启动后访问前端：

```text
http://127.0.0.1:3000/aibase/login
```

开发环境内置登录信息：

```text
用户名：admin
密码：任意非空值，例如 x
验证码：1234
```

登录成功后会进入：

```text
http://127.0.0.1:3000/aibase/appSpace/agent
```

当前最小闭环已经接入 MySQL：IAM 返回开发账号、默认组织和权限；前端可零改动创建智能体；App 服务会把智能体写入 `app_service.apps` 和 `app_service.assistant_drafts`，并在列表和草稿详情中读回。

页面验收路径：

1. 登录后进入 `应用空间 -> 智能体`。
2. 点击创建按钮，新建一个智能体。
3. 创建成功后会进入智能体编辑页；当前模型、知识库、工具等下拉接口先返回空集合占位。
4. 回到智能体列表，可以看到刚创建的智能体。

命令行快速验收：

```powershell
$body = @{ category = 1; name = 'DockerAgent'; desc = 'Created through BFF'; avatar = @{ key = ''; path = '' } } | ConvertTo-Json -Depth 5
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/user/api/v1/assistant' -Headers @{ Authorization = 'Bearer dev-token' } -ContentType 'application/json' -Body $body
Invoke-RestMethod -Uri 'http://localhost:8080/user/api/v1/appspace/assistant/list?name=Docker' -Headers @{ Authorization = 'Bearer dev-token' }
```

查看 MySQL 表和 Flyway 版本：

```powershell
docker exec wanwu-java-mysql mysql -uroot -e "USE app_service; SHOW TABLES; SELECT version, description, success FROM flyway_schema_history;"
```

停止服务：

```powershell
docker compose --profile full down
```

## 健康检查

每个服务都有统一健康检查：

```text
GET /internal/health
```

每个服务也有一个本地服务信息接口：

```text
GET /api/{service}/service-info
```

BFF 示例：

```text
http://127.0.0.1:8080/api/bff/service-info
```

## 后续迁移建议

1. 先迁移 IAM 的用户、组织、权限领域模型，因为它会成为其他服务的身份基础。
2. 再迁移 Model、Knowledge、RAG 的核心数据模型和外部适配器。
3. Agent、Assistant、MCP 放在第三阶段，把编排能力建立在稳定的模型和知识库接口之上。
4. 每迁移一个服务，先补 `wanwu-api` 契约，再补领域模型、Mapper、Flyway migration、服务实现和测试。
