# okhttp3-metrics-prometheus

Okhttp 3.x Metrics For Prometheus

### 组件简介

 > 基于 okhttp 3.x + micrometer 的 Prometheus 指标采集组件，用于采集 okhttp 3.x 的连接池指标数据，包括：连接池活跃连接数、连接池空闲连接数、连接池最大连接数、连接池最大空闲连接数、连接
 
- 参考：https://github.com/raskasa/metrics-okhttp

### 使用说明

##### 1、Spring Boot 项目添加 Maven 依赖

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>okhttp3-metrics-prometheus</artifactId>
	<version>${project.version}</version>
</dependency>
```

##### 2、在`application.yml`文件中增加如下配置

```yaml
################################################################################################################
###okhttp3基本配置：
################################################################################################################
okhttp3:
  # 连接超时时间，默认 10s
  connect-timeout: 5s
  # 读取超时时间，默认 10s
  read-timeout: 30s
  # 写入超时时间，默认 10s
  write-timeout: 30s
  # 连接失败后是否重试
  retry-on-connection-failure: false
  # 打印日志级别：NONE、BASIC、HEADERS、BODY
  log-level: HEADERS
  pool:
    # 最大空闲连接梳数量，超出该值后，连接用完后会被关闭，最多只会保留idleConnectionCount个连接数量
    max-idle-connections: 48
    # 最大瞬时处理连接数量
    max-requests: 128
    # 每个请求地址最大瞬时处理连接数量
    max-requests-per-host: 24
```


##### 2、使用示例

访问本地配置：

http://localhost:8080/actuator/prometheus

```
# Connection connect max time
druid_connections_connect_max_time{application="druid-app",} 0.0
# Connection alive max time
druid_connections_alive_max_time{application="druid-app",} 0.0
# Connection alive min time
druid_connections_alive_min_time{application="druid-app",} 0.0
# Connection connect count
druid_connections_connect_count{application="druid-app",} 0.0
# Connection active count
druid_connections_active_count{application="druid-app",} 0.0
# Connection close count
druid_connections_close_count{application="druid-app",} 0.0
# Connection error count
druid_connections_error_count{application="druid-app",} 0.0
# Connection connect error count
druid_connections_connect_error_count{application="druid-app",} 0.0
# Connecting commit count
druid_connections_commit_count{application="druid-app",} 0.0
# Connection rollback count
druid_connections_rollback_count{application="druid-app",} 0.0
```

##### 3、Prometheus 集成


##### 4、Grafana 集成


## Jeebiz 技术社区

Jeebiz 技术社区 **微信公共号**、**小程序**，欢迎关注反馈意见和一起交流，关注公众号回复「Jeebiz」拉你入群。

|公共号|小程序|
|---|---|
| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/qrcode_for_gh_1d965ea2dfd1_344.jpg)| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/gh_09d7d00da63e_344.jpg)|
