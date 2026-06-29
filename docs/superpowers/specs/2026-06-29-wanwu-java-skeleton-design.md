# Wanwu Java Skeleton Design

## Goal

Create a Java 8 replacement skeleton for the Go-based Wanwu backend in `D:\work\week3\wanwu`, optimized for long-term incremental development instead of a one-shot line-by-line migration.

## Decisions

- Use Maven multi-module as the repository build system.
- Target Java 8 and Spring Boot 2.7.x.
- Replace Go gRPC/protobuf compatibility with a Java-native RPC layer: Apache Dubbo 3.
- Use Nacos 2.x for service discovery and future configuration management.
- Provide MyBatis-Plus and Flyway as the data-access baseline, but keep services startable without a database by default.
- Preserve the Go project's service boundaries as Java modules: bff, iam, model, knowledge, agent, assistant, app, mcp, operate, and rag.

## Architecture

The root Maven project owns dependency versions, compiler settings, and module composition. `wanwu-api` contains plain Java Dubbo service contracts and shared DTOs. `wanwu-common-core` contains cross-service primitives such as response envelopes, errors, tenant context, and trace IDs. `wanwu-common-web` contributes shared HTTP health and exception handling. `wanwu-common-rpc` holds Dubbo conventions. `wanwu-common-data` holds MyBatis-Plus and Flyway infrastructure for services that later need persistence.

Each `wanwu-service-*` module is an independent Spring Boot application. It exposes a small REST service-info endpoint for local inspection and a Dubbo provider implementation for its service contract. Nacos is configured by environment variable so local development can run without a registry first, then switch to Nacos when `DUBBO_REGISTRY_ADDRESS=nacos://127.0.0.1:8848` is provided.

## Scope

This first version intentionally builds the durable shape of the system: build files, module boundaries, bootstrapping, health endpoints, Dubbo contracts, provider implementations, infrastructure compose file, and developer documentation. It does not migrate the original Go business logic, database schema, frontend, RAG algorithms, or sandbox runtime.

## Testing And Verification

The skeleton includes a focused unit test for the shared response model. The intended verification command is `mvn clean test` from the repository root. The current workstation does not have Maven on `PATH`, so verification was run with a temporary Maven 3.9.9 download under `%TEMP%`; the full reactor build passed.
