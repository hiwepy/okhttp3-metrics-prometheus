# okhttp3-metrics-prometheus

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-17-orange)](https://github.com/easy-4-java/okhttp3-metrics-prometheus) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](./LICENSE)

纯 Java OkHttp 指标模块：面向 Micrometer / Prometheus 的调用、调度器与缓存埋点

> **当前分支**：`feature/2.0.x`
> **版本**：`2.0.x.20260630-SNAPSHOT`
> **JDK 基线**：17
> **项目状态**：维护中（2.0.x 线）。尚未发布 Maven Central；制品通过 Aliyun Maven 仓库与 GitHub Releases 分发。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 能力与状态](#2-features--status)
- [3. 运行要求与兼容性](#3-requirements--compatibility)
- [4. 架构与模块](#4-architecture--modules)
- [5. 引入依赖](#5-installation)
- [6. 快速开始](#6-quick-start)
- [7. 配置](#7-configuration)
- [8. 核心用法](#8-core-usage)
- [9. 测试与构建](#9-testing--build)
- [10. 版本线与分支](#10-versioning--branches)
- [11. 贡献与许可证](#11-contributing--license)

## 1. 项目概述

### 1.1 是什么

**okhttp3-metrics-prometheus** 使用 Micrometer 对 OkHttp 客户端进行埋点，使 Prometheus（或任意 Micrometer 注册中心后端）可以采集调用生命周期、调度器与缓存指标。它是纯 Java 的「指标 sidecar」——**不依赖 Spring Boot**。

### 1.2 不是什么

- **不是 Spring Boot Starter**。自动装配位于独立的 `okhttp3-spring-boot-starter` 仓库。
- **不是 OkHttp 的替代品**。通过 `EventListener` 与 `Interceptor` 包装客户端，不 fork OkHttp。
- **不是日志模块**。它产出指标；日志仍由 SLF4J 负责。

### 1.3 典型使用场景

| 场景 | 推荐入口 | 结果 |
|---|---|---|
| 向 Prometheus 暴露 HTTP 调用指标 | `InstrumentedOkHttpClients.create(registry)` | `okhttp3.calls.*`、`okhttp3.requests.*` 计数器、计时器与直方图 |
| 为既有客户端埋点 | `InstrumentedOkHttpClients.create(registry, client, ...)` | 无需重建客户端即可获得同样指标 |
| 控制 URL 基数 | `UrlMapperEnum` + `create(..., urlMapper, ...)` | 控制按 URL 标签的基数 |
| 追加上下文标签 | `create(..., contextSpecificTags, ...)` | 由 `Request` / `Response` 派生额外 `KeyValue` 标签 |
| 监听器组合 | `NestedEventListener` | 将本监听器与自有 `EventListener` 组合 |

<a id="2-features--status"></a>
## 2. 能力与状态

| 能力 | 状态 | 说明 |
|---|:---:|---|
| 调用生命周期指标 | 可用 | `okhttp3.calls.started` / `calls.end` / `calls.failed` / `calls.duration` |
| DNS 指标 | 可用 | `okhttp3.dns.started` / `dns.end` / `dns.duration` |
| 连接指标 | 可用 | `okhttp3.connections.started/end/failed/duration/acquired/released` |
| 请求指标 | 可用 | `okhttp3.requests.headers.*`、`requests.body.*`、`requests.failed`、`requests.body.bytes` |
| 响应指标 | 可用 | `okhttp3.responses.headers.*`、`responses.body.*`、`responses.failed` |
| 调用超时计数器 | 可用 | `okhttp3.call.timeout.count` |
| 缓存指标 | 可用 | `OkHttpCacheMetrics` |
| 调度器指标 | 可用 | `OkHttpDispatcherMetrics` |
| 指标拦截器 | 可用 | `InstrumentedInterceptor(registry, tags)`，用于 `OkHttpClient.Builder.addInterceptor(...)` |
| Spring Boot 自动配置 | 不包含 | 见独立的 `okhttp3-spring-boot-starter` |

指标名前缀在 `OkHttp3Metrics`（`okhttp3`、`okhttp3.requests`、`okhttp3.pool`）与 `MetricNames` 中声明。

<a id="3-requirements--compatibility"></a>
## 3. 运行要求与兼容性

| 组件 | 版本 | 说明 |
|---|---:|---|
| JDK | 17+ | 2.0.x 线基线 |
| OkHttp | 4.12.0 | 埋点目标 |
| Micrometer core + observation | 1.14.5 | 指标原语 |
| SLF4J | 2.0.18 | 日志门面 |

版本线矩阵：

| 版本线 | 分支 | JDK | 版本模式 | 用途 |
|---|---|---:|---|---|
| 1.0.x | `feature/1.0.x` | 8 | `1.0.x.*` | 供 Boot 2.x Starter 与存量项目使用 |
| 2.0.x | `feature/2.0.x`（当前分支） | 17 | `2.0.x.*` | 供 Boot 3.x Starter 使用 |
| 3.0.x | `feature/3.0.x` | 21 | `3.0.x.*` | 供 Boot 4.x Starter / 新项目使用 |

<a id="4-architecture--modules"></a>
## 4. 架构与模块

```text
[ OkHttpClient (4.12.0) ]
        |
        | InstrumentedEventListener（EventListener）
        | InstrumentedInterceptor（Interceptor）
        v
+------------------------------------------+
| OkHttp3Metrics（MeterBinder）             |
|  okhttp3.calls.* / requests.* /           |
|  responses.* / dns.* / connections.*      |
| OkHttpCacheMetrics      缓存命中/写入     |
| OkHttpDispatcherMetrics 排队/运行中       |
+------------------------------------------+
        |
        v
[ Micrometer MeterRegistry ] -> [ Prometheus / ... ]
```

单模块库（打包类型 `jar`）。包结构（`okhttp3.metrics`）：

| 类 | 职责 |
|---|---|
| `OkHttp3Metrics` | 抽象 `MeterBinder`；指标名常量与绑定契约 |
| `InstrumentedOkHttpClients` | 工厂：新建或包装带埋点的 `OkHttpClient` |
| `InstrumentedEventListener` | 捕获调用生命周期的 `EventListener`（DNS、连接、请求、响应、失败） |
| `InstrumentedInterceptor` | 供 `addInterceptor(...)` 装配的 `Interceptor` 方案 |
| `NestedEventListener` | 组合多个 `EventListener` |
| `OkHttpCacheMetrics` / `OkHttpDispatcherMetrics` | 缓存与调度器 Gauge / Counter |
| `MetricNames` / `UrlMapperEnum` | 命名工具与 URL 基数策略 |
| `OKhttp3MetricsSpecificTagHandler` | 标签处理工具 |

<a id="5-installation"></a>
## 5. 引入依赖

Maven：

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>okhttp3-metrics-prometheus</artifactId>
    <version>2.0.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle：

```groovy
implementation 'io.github.easy4j:okhttp3-metrics-prometheus:2.0.x.20260630-SNAPSHOT'
```

快照版本需要启用对应快照仓库（`pom.xml` 中 `distributionManagement` 指向 Aliyun Maven 仓库）。

<a id="6-quick-start"></a>
## 6. 快速开始

```java
MeterRegistry registry = new SimpleMeterRegistry();

// 新建一个带埋点的客户端（无 Spring 参与）
OkHttpClient client = InstrumentedOkHttpClients.create(registry);

// 或为既有客户端埋点：
// OkHttpClient client = InstrumentedOkHttpClients.create(registry, myExistingClient);

String body = client.newCall(new Request.Builder()
        .url("https://httpbin.org/get").build())
        .execute().body().string();
```

**预期结果**：调用完成后，注册中心中出现 `okhttp3.calls.started`、`okhttp3.calls.end`、`okhttp3.requests.headers.end`、`okhttp3.connections.acquired` 等指标；配合 Prometheus 注册中心与抓取端点后，以 `okhttp3_*` 指标族对外暴露。

<a id="7-configuration"></a>
## 7. 配置

本库为纯 Java 库，无配置属性。埋点行为通过工厂参数控制：

| 参数 | 含义 |
|---|---|
| `MeterRegistry` | 指标绑定目标（Prometheus、Simple 等） |
| `OkHttpClient` | 待包装的既有客户端（可选，缺省自动创建） |
| `UrlMapperEnum` | URL 到标签的映射策略（默认 `ENCODED_PATH`），用于控制基数 |
| `includeHostTag` | 是否附加 `host` 标签 |
| `extraTagMap` / `requestTagKeys` | 静态与按请求附加的额外标签 |
| `contextSpecificTags` | 上下文标签 `BiFunction<Request, Response, KeyValue>` 列表 |
| `Collection<Tag>` | 传给 `InstrumentedInterceptor` 的标签 |

<a id="8-core-usage"></a>
## 8. 核心用法

### 8.1 基于拦截器的装配

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new InstrumentedInterceptor(registry,
                List.of(Tag.of("app", "checkout"))))
        .build();
```

### 8.2 自定义 URL 映射

```java
// ENCODED_PATH 保持每个编码路径一个序列；其他策略在基数与细节之间取舍，
// 可选策略见 UrlMapperEnum。
OkHttpClient client = InstrumentedOkHttpClients.create(
        registry, baseClient, UrlMapperEnum.ENCODED_PATH, true);
```

<a id="9-testing--build"></a>
## 9. 测试与构建

```bash
mvn clean verify
```

- 父 POM 通过 `maven-enforcer-plugin` 强制 Maven 与 JDK 17 基线。
- JaCoCo 在 `verify` 阶段执行 `prepare-agent`、`report` 与 `check`，行覆盖率规则为 **90%**（`haltOnFailure=false`）。
- 发布打包（`mvn -Prelease deploy`）附带 sources 与 javadoc 构件并执行 GPG 签名，对接 Sonatype Central Publishing；普通 `mvn deploy` 按版本后缀路由到 Aliyun Maven 仓库（见 `distributionManagement`）。
- `scripts/render-branch-pom.py` 按版本线重新生成分支专属 `pom.xml`（JDK 与依赖栈随线变化）。

<a id="10-versioning--branches"></a>
## 10. 版本线与分支

| 分支 | 版本模式 | JDK | 维护策略 |
|---|---|---|---|
| `feature/1.0.x` | `1.0.x.*` | 8 | 仅接受兼容性修复与 JDK 8 安全的依赖升级 |
| `feature/2.0.x`（当前分支） | `2.0.x.*` | 17 | JDK 17 线 |
| `feature/3.0.x` | `3.0.x.*` | 21 | JDK 21 线 |

<a id="11-contributing--license"></a>
## 11. 贡献与许可证

欢迎贡献。提交 Pull Request 前请执行 `mvn clean verify`，并说明兼容性、测试与迁移影响。本项目采用 [Apache License 2.0](LICENSE) 许可证。
