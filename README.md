# okhttp3-metrics-prometheus

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-17-orange)](https://github.com/easy-4-java/okhttp3-metrics-prometheus) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](./LICENSE)

Pure Java OkHttp metrics module: Micrometer / Prometheus instrumentation for OkHttp calls, dispatcher and cache
[简体中文](./README.zh-CN.md)

> **Current branch**: `feature/2.0.x`
> **Version**: `2.0.x.20260630-SNAPSHOT`
> **JDK baseline**: 17
> **Project status**: maintenance (2.0.x line). Not yet published to Maven Central; artifacts are distributed via the Aliyun Maven repository and GitHub Releases.

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage](#8-core-usage)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

### 1.1 What it is

**okhttp3-metrics-prometheus** instruments OkHttp clients with Micrometer meters so that Prometheus (or any Micrometer registry backend) can scrape call lifecycle, dispatcher and cache metrics. It is a pure Java "metrics sidecar" — **independent of Spring Boot**.

### 1.2 What it is not

- **Not a Spring Boot starter.** Auto-configuration lives in the separate `okhttp3-spring-boot-starter` repository.
- **Not a replacement for OkHttp.** It wraps clients via `EventListener` and `Interceptor`, without forking OkHttp.
- **Not a logging module.** It emits metrics; logging remains SLF4J's job.

### 1.3 Typical scenarios

| Scenario | Recommended entry | Result |
|---|---|---|
| Expose HTTP call metrics to Prometheus | `InstrumentedOkHttpClients.create(registry)` | `okhttp3.calls.*` / `okhttp3.requests.*` counters, timers and histograms |
| Instrument an existing client | `InstrumentedOkHttpClients.create(registry, client, ...)` | Same metrics without rebuilding the client |
| Custom URL cardinality control | `UrlMapperEnum` + `create(..., urlMapper, ...)` | Control per-URL tag cardinality |
| Add context-specific tags | `create(..., contextSpecificTags, ...)` | Extra `KeyValue` tags derived from `Request`/`Response` |
| Event-listener composition | `NestedEventListener` | Combine this listener with your own `EventListener`s |

<a id="2-features--status"></a>
## 2. Features & Status

| Capability | Status | Notes |
|---|:---:|---|
| Call lifecycle metrics | Available | `okhttp3.calls.started` / `calls.end` / `calls.failed` / `calls.duration` |
| DNS metrics | Available | `okhttp3.dns.started` / `dns.end` / `dns.duration` |
| Connection metrics | Available | `okhttp3.connections.started/end/failed/duration/acquired/released` |
| Request metrics | Available | `okhttp3.requests.headers.*` / `requests.body.*` / `requests.failed` / `requests.body.bytes` |
| Response metrics | Available | `okhttp3.responses.headers.*` / `responses.body.*` / `responses.failed` |
| Call timeout counter | Available | `okhttp3.call.timeout.count` |
| Cache metrics | Available | `OkHttpCacheMetrics` |
| Dispatcher metrics | Available | `OkHttpDispatcherMetrics` |
| Metrics interceptor | Available | `InstrumentedInterceptor(registry, tags)` for `OkHttpClient.Builder.addInterceptor(...)` |
| Spring Boot auto-configuration | Not included | See the separate `okhttp3-spring-boot-starter` |

Metric name prefixes are declared in `OkHttp3Metrics` (`okhttp3`, `okhttp3.requests`, `okhttp3.pool`) and `MetricNames`.

<a id="3-requirements--compatibility"></a>
## 3. Requirements & Compatibility

| Component | Version | Notes |
|---|---:|---|
| JDK | 17+ | 2.0.x line baseline |
| OkHttp | 4.12.0 | Instrumented target |
| Micrometer core + observation | 1.14.5 | Meter primitives |
| SLF4J | 2.0.18 | Logging facade |

Version-line matrix:

| Version line | Branch | JDK | Version pattern | Purpose |
|---|---|---:|---|---|
| 1.0.x | `feature/1.0.x` | 8 | `1.0.x.*` | For Boot 2.x starters and legacy projects |
| 2.0.x | `feature/2.0.x` (this branch) | 17 | `2.0.x.*` | For Boot 3.x starters |
| 3.0.x | `feature/3.0.x` | 21 | `3.0.x.*` | For Boot 4.x starters / new projects |

<a id="4-architecture--modules"></a>
## 4. Architecture & Modules

```text
[ OkHttpClient (4.12.0) ]
        |
        | InstrumentedEventListener (EventListener)
        | InstrumentedInterceptor (Interceptor)
        v
+------------------------------------------+
| OkHttp3Metrics (MeterBinder)              |
|  okhttp3.calls.* / requests.* /           |
|  responses.* / dns.* / connections.*      |
| OkHttpCacheMetrics     cache hits/puts    |
| OkHttpDispatcherMetrics  queued/running   |
+------------------------------------------+
        |
        v
[ Micrometer MeterRegistry ] -> [ Prometheus / ... ]
```

Single-module library (packaging `jar`). Package layout (`okhttp3.metrics`):

| Class | Responsibility |
|---|---|
| `OkHttp3Metrics` | Abstract `MeterBinder`; metric-name constants and binding contract |
| `InstrumentedOkHttpClients` | Factory: build or wrap an `OkHttpClient` with instrumentation |
| `InstrumentedEventListener` | `EventListener` capturing call lifecycle (DNS, connect, request, response, failure) |
| `InstrumentedInterceptor` | `Interceptor` alternative for `addInterceptor(...)` wiring |
| `NestedEventListener` | Composes multiple `EventListener`s |
| `OkHttpCacheMetrics` / `OkHttpDispatcherMetrics` | Cache and dispatcher gauges/counters |
| `MetricNames` / `UrlMapperEnum` | Name building helpers and URL-cardinality policy |
| `OKhttp3MetricsSpecificTagHandler` | Tag handling helpers |

<a id="5-installation"></a>
## 5. Installation

Maven:

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>okhttp3-metrics-prometheus</artifactId>
    <version>2.0.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.easy4j:okhttp3-metrics-prometheus:2.0.x.20260630-SNAPSHOT'
```

Snapshot builds require an enabled snapshot repository (Aliyun Maven snapshot repository per `distributionManagement` in `pom.xml`).

<a id="6-quick-start"></a>
## 6. Quick Start

```java
MeterRegistry registry = new SimpleMeterRegistry();

// Build a brand-new instrumented client (no Spring involved)
OkHttpClient client = InstrumentedOkHttpClients.create(registry);

// Or instrument an existing client:
// OkHttpClient client = InstrumentedOkHttpClients.create(registry, myExistingClient);

String body = client.newCall(new Request.Builder()
        .url("https://httpbin.org/get").build())
        .execute().body().string();
```

**Expected result**: after the call, the registry contains meters such as `okhttp3.calls.started`, `okhttp3.calls.end`, `okhttp3.requests.headers.end` and `okhttp3.connections.acquired`; with the Prometheus registry and a scrape endpoint, they appear under the `okhttp3_*` metric family.

<a id="7-configuration"></a>
## 7. Configuration

This is a pure Java library — no configuration properties. Instrumentation behavior is controlled through factory parameters:

| Parameter | Meaning |
|---|---|
| `MeterRegistry` | Where meters are bound (Prometheus, Simple, ...) |
| `OkHttpClient` | Existing client to wrap (optional; a default is created) |
| `UrlMapperEnum` | URL-to-tag mapping policy (default `ENCODED_PATH`) to control cardinality |
| `includeHostTag` | Whether to add a `host` tag |
| `extraTagMap` / `requestTagKeys` | Static and per-request extra tags |
| `contextSpecificTags` | `BiFunction<Request, Response, KeyValue>` list for context tags |
| `Collection<Tag>` | Tags passed to `InstrumentedInterceptor` |

<a id="8-core-usage"></a>
## 8. Core Usage

### 8.1 Interceptor-based wiring

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new InstrumentedInterceptor(registry,
                List.of(Tag.of("app", "checkout"))))
        .build();
```

### 8.2 Custom URL mapping

```java
// ENCODED_PATH keeps one series per encoded path; other strategies trade
// cardinality for detail. See UrlMapperEnum for available policies.
OkHttpClient client = InstrumentedOkHttpClients.create(
        registry, baseClient, UrlMapperEnum.ENCODED_PATH, true);
```

<a id="9-testing--build"></a>
## 9. Testing & Build

```bash
mvn clean verify
```

- The parent POM enforces Maven and JDK 17 baselines via `maven-enforcer-plugin`.
- JaCoCo runs `prepare-agent`, `report` and `check` on the `verify` phase with a **90% line-coverage** rule (`haltOnFailure=false`).
- Release packaging (`mvn -Prelease deploy`) attaches sources and javadoc jars, GPG-signs artifacts and is wired for Sonatype Central Publishing; plain `mvn deploy` routes SNAPSHOT/release artifacts to the Aliyun Maven repository per `distributionManagement`.
- `scripts/render-branch-pom.py` regenerates the branch-specific `pom.xml` (JDK / dependency stack per version line).

<a id="10-versioning--branches"></a>
## 10. Versioning & Branches

| Branch | Version pattern | JDK | Maintenance policy |
|---|---|---|---|
| `feature/1.0.x` | `1.0.x.*` | 8 | Compatibility fixes and JDK-8-safe dependency upgrades only |
| `feature/2.0.x` (this branch) | `2.0.x.*` | 17 | JDK 17 line |
| `feature/3.0.x` | `3.0.x.*` | 21 | JDK 21 line |

<a id="11-contributing--license"></a>
## 11. Contributing & License

Contributions are welcome. Run `mvn clean verify` before opening a pull request and describe compatibility, testing and migration impact. This project is licensed under the [Apache License 2.0](LICENSE).
