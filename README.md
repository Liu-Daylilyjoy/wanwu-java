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

如果本机不安装 Java/Maven，只依赖 Docker，可以直接构建并启动 BFF 骨架服务：

```powershell
docker compose --profile app up --build bff
```

启动后访问：

```text
http://127.0.0.1:8080/internal/health
http://127.0.0.1:8080/api/bff/service-info
```

如果要让 Dubbo 注册到 Nacos，先启动 Nacos，再设置注册中心地址：

```powershell
docker compose up -d nacos
$env:DUBBO_REGISTRY_ADDRESS = "nacos://nacos:8848"
docker compose --profile app up --build bff
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
