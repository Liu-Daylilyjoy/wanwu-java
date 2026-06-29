# Wanwu Java Skeleton Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Java 8 Maven multi-module skeleton that mirrors Wanwu's service boundaries while replacing Go gRPC with Dubbo.

**Architecture:** A parent Maven project manages dependency versions. `wanwu-api` defines Java RPC contracts, `wanwu-common-*` modules provide shared platform capabilities, and each `wanwu-service-*` module is an independently runnable Spring Boot service.

**Tech Stack:** Java 8 target, Spring Boot 2.7.18, Apache Dubbo 3.2.x, Nacos 2.x, MyBatis-Plus, Flyway, JUnit 5.

---

### Task 1: Build System And Docs

**Files:**
- Create: `pom.xml`
- Create: `.gitignore`
- Create: `README.md`
- Create: `docker-compose.yml`

- [x] **Step 1: Create the Maven parent**

Define group `com.unicomai.wanwu`, Java source/target `1.8`, Spring Boot dependency management, Dubbo, MyBatis-Plus, and all service/common modules.

- [x] **Step 2: Add developer docs**

Document prerequisites, local infrastructure, Maven build commands, and how to switch Dubbo from local mode to Nacos.

### Task 2: Shared Modules

**Files:**
- Create: `wanwu-common-core/pom.xml`
- Create: `wanwu-common-web/pom.xml`
- Create: `wanwu-common-rpc/pom.xml`
- Create: `wanwu-common-data/pom.xml`

- [x] **Step 1: Add core primitives**

Create response envelopes, error codes, business exceptions, trace IDs, service names, and tenant context.

- [x] **Step 2: Add web primitives**

Create shared health and exception handling that services can reuse by scanning `com.unicomai.wanwu`.

- [x] **Step 3: Add RPC and data primitives**

Create Dubbo constants and MyBatis-Plus/Flyway base configuration for future persistence modules.

### Task 3: RPC Contracts

**Files:**
- Create: `wanwu-api/pom.xml`
- Create: `wanwu-api/src/main/java/com/unicomai/wanwu/api/**`

- [x] **Step 1: Add service descriptor DTO**

Create a serializable DTO returned by every skeleton service.

- [x] **Step 2: Add one interface per service boundary**

Create Java Dubbo interfaces for bff, iam, model, knowledge, agent, assistant, app, mcp, operate, and rag.

### Task 4: Service Modules

**Files:**
- Create: `wanwu-service-*/pom.xml`
- Create: `wanwu-service-*/src/main/java/**`
- Create: `wanwu-service-*/src/main/resources/application.yml`

- [x] **Step 1: Add Spring Boot applications**

Each service has an application class with package scanning rooted at `com.unicomai.wanwu`.

- [x] **Step 2: Add Dubbo providers**

Each service implements its matching API interface with `@DubboService`.

- [x] **Step 3: Add REST service-info endpoints**

Each service exposes a small endpoint for local smoke tests.

### Task 5: Verification

**Files:**
- Create: `wanwu-common-core/src/test/java/com/unicomai/wanwu/common/core/model/ApiResponseTest.java`

- [x] **Step 1: Add a focused unit test**

Verify the shared response envelope preserves success state and payload.

- [x] **Step 2: Run Maven tests**

Run: `mvn clean test`

Expected: Maven compiles all modules and the common-core unit test passes. Verified with temporary Maven 3.9.9 because Maven is not installed on `PATH` in this workstation.
