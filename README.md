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
  # okhttp3 的 metrics 配置
  metrics:
    # 是否在监控指标中包含host标签
    include-host: true
```


##### 2、使用示例

访问本地配置：

http://localhost:8080/actuator/prometheus

```
# HELP okhttp3_responses_body_end_total  
# TYPE okhttp3_responses_body_end_total counter
okhttp3_responses_body_end_total{application="spring-boot-actuator",} 444.0
# HELP okhttp3_connections_started_total  
# TYPE okhttp3_connections_started_total counter
okhttp3_connections_started_total{application="spring-boot-actuator",} 4.0
# TYPE okhttp3_pool_dispatcher_running_calls_count gauge
okhttp3_pool_dispatcher_running_calls_count{application="spring-boot-actuator",} 1.0
# HELP okhttp3_calls_end_total  
# TYPE okhttp3_calls_end_total counter
okhttp3_calls_end_total{application="spring-boot-actuator",} 222.0
# HELP tomcat_sessions_rejected_sessions_total  
# TYPE tomcat_sessions_rejected_sessions_total counter
tomcat_sessions_rejected_sessions_total{application="spring-boot-actuator",} 0.0
# HELP okhttp3_responses_headers_started_total  
# TYPE okhttp3_responses_headers_started_total counter
okhttp3_responses_headers_started_total{application="spring-boot-actuator",} 445.0
# HELP okhttp3_connections_released_total  
# TYPE okhttp3_connections_released_total counter
okhttp3_connections_released_total{application="spring-boot-actuator",} 444.0
# HELP okhttp3_requests_body_end_total  
# TYPE okhttp3_requests_body_end_total counter
okhttp3_requests_body_end_total{application="spring-boot-actuator",} 0.0
# HELP okhttp3_network_requests_submitted_total  
# TYPE okhttp3_network_requests_submitted_total counter
okhttp3_network_requests_submitted_total{application="spring-boot-actuator",} 445.0
# HELP okhttp3_requests_headers_end_total  
# TYPE okhttp3_requests_headers_end_total counter
okhttp3_requests_headers_end_total{application="spring-boot-actuator",} 445.0
# HELP okhttp3_dns_started_total  
# TYPE okhttp3_dns_started_total counter
okhttp3_dns_started_total{application="spring-boot-actuator",} 4.0
# HELP okhttp3_calls_duration_seconds  
# TYPE okhttp3_calls_duration_seconds summary
okhttp3_calls_duration_seconds_count{application="spring-boot-actuator",} 0.0
okhttp3_calls_duration_seconds_sum{application="spring-boot-actuator",} 0.0
# HELP okhttp3_calls_duration_seconds_max  
# TYPE okhttp3_calls_duration_seconds_max gauge
okhttp3_calls_duration_seconds_max{application="spring-boot-actuator",} 0.0
# HELP okhttp3_connections_acquired_total  
# TYPE okhttp3_connections_acquired_total counter
okhttp3_connections_acquired_total{application="spring-boot-actuator",} 445.0
# HELP okhttp3_pool_dispatcher_queued_calls_count Total number of queued calls 
# TYPE okhttp3_pool_dispatcher_queued_calls_count gauge
okhttp3_pool_dispatcher_queued_calls_count{application="spring-boot-actuator",} 0.0
# HELP okhttp3_calls_started_total  
# TYPE okhttp3_calls_started_total counter
okhttp3_calls_started_total{application="spring-boot-actuator",} 223.0
# HELP okhttp3_connections_end_total  
# TYPE okhttp3_connections_end_total counter
okhttp3_connections_end_total{application="spring-boot-actuator",} 4.0
# HELP okhttp3_responses_failed_total  
# TYPE okhttp3_responses_failed_total counter
okhttp3_responses_failed_total{application="spring-boot-actuator",} 0.0
# HELP okhttp3_requests_body_started_total  
# TYPE okhttp3_requests_body_started_total counter
okhttp3_requests_body_started_total{application="spring-boot-actuator",} 0.0
# HELP okhttp3_requests_failed_total  
# TYPE okhttp3_requests_failed_total counter
okhttp3_requests_failed_total{application="spring-boot-actuator",} 0.0
# HELP okhttp3_requests_headers_started_total  
# TYPE okhttp3_requests_headers_started_total counter
okhttp3_requests_headers_started_total{application="spring-boot-actuator",} 445.0
# HELP okhttp3_requests_seconds_max Timer of OkHttp operation
# TYPE okhttp3_requests_seconds_max gauge
okhttp3_requests_seconds_max{application="spring-boot-actuator",method="GET",status="302",target_host="baidu.com",target_port="443",target_scheme="https",uri="/",} 1.2050477
# HELP okhttp3_requests_seconds Timer of OkHttp operation
# TYPE okhttp3_requests_seconds summary
okhttp3_requests_seconds_count{application="spring-boot-actuator",method="GET",status="302",target_host="baidu.com",target_port="443",target_scheme="https",uri="/",} 222.0
okhttp3_requests_seconds_sum{application="spring-boot-actuator",method="GET",status="302",target_host="baidu.com",target_port="443",target_scheme="https",uri="/",} 13.3909726
# HELP okhttp3_connections_duration_seconds  
# TYPE okhttp3_connections_duration_seconds summary
okhttp3_connections_duration_seconds_count{application="spring-boot-actuator",} 0.0
okhttp3_connections_duration_seconds_sum{application="spring-boot-actuator",} 0.0
# HELP okhttp3_connections_duration_seconds_max  
# TYPE okhttp3_connections_duration_seconds_max gauge
okhttp3_connections_duration_seconds_max{application="spring-boot-actuator",} 0.0
# HELP okhttp3_connections_failed_total  
# TYPE okhttp3_connections_failed_total counter
okhttp3_connections_failed_total{application="spring-boot-actuator",} 0.0
# HELP okhttp3_pool_dispatcher_max_requests_perhost_total max requests of dispatcher by per host 
# TYPE okhttp3_pool_dispatcher_max_requests_perhost_total counter
okhttp3_pool_dispatcher_max_requests_perhost_total{application="spring-boot-actuator",} 5.0
# HELP okhttp3_responses_headers_end_total  
# TYPE okhttp3_responses_headers_end_total counter
okhttp3_responses_headers_end_total{application="spring-boot-actuator",} 444.0
# HELP okhttp3_pool_connection_count_connections The state of connections in the OkHttp connection pool
# TYPE okhttp3_pool_connection_count_connections gauge
okhttp3_pool_connection_count_connections{app="spring-boot-actuator",application="spring-boot-actuator",state="active",} 1.0
okhttp3_pool_connection_count_connections{app="spring-boot-actuator",application="spring-boot-actuator",state="idle",} 1.0
# HELP okhttp3_dns_duration_seconds  
# TYPE okhttp3_dns_duration_seconds summary
okhttp3_dns_duration_seconds_count{application="spring-boot-actuator",} 0.0
okhttp3_dns_duration_seconds_sum{application="spring-boot-actuator",} 0.0
# HELP okhttp3_dns_duration_seconds_max  
# TYPE okhttp3_dns_duration_seconds_max gauge
okhttp3_dns_duration_seconds_max{application="spring-boot-actuator",} 0.0
# HELP okhttp3_requests_body_bytes_max  
# TYPE okhttp3_requests_body_bytes_max gauge
okhttp3_requests_body_bytes_max{application="spring-boot-actuator",} 0.0
# HELP okhttp3_requests_body_bytes  
# TYPE okhttp3_requests_body_bytes summary
okhttp3_requests_body_bytes_count{application="spring-boot-actuator",} 0.0
okhttp3_requests_body_bytes_sum{application="spring-boot-actuator",} 0.0
# HELP okhttp3_network_requests_duration_seconds  
# TYPE okhttp3_network_requests_duration_seconds histogram
okhttp3_network_requests_duration_seconds{application="spring-boot-actuator",quantile="0.5",} 0.037224448
okhttp3_network_requests_duration_seconds{application="spring-boot-actuator",quantile="0.75",} 0.043515904
okhttp3_network_requests_duration_seconds{application="spring-boot-actuator",quantile="0.95",} 0.04980736
okhttp3_network_requests_duration_seconds{application="spring-boot-actuator",quantile="0.98",} 0.318242816
okhttp3_network_requests_duration_seconds{application="spring-boot-actuator",quantile="0.99",} 0.536346624
okhttp3_network_requests_duration_seconds{application="spring-boot-actuator",quantile="0.999",} 1.207435264
okhttp3_network_requests_duration_seconds_bucket{application="spring-boot-actuator",le="0.1",} 424.0
okhttp3_network_requests_duration_seconds_bucket{application="spring-boot-actuator",le="+Inf",} 444.0
okhttp3_network_requests_duration_seconds_count{application="spring-boot-actuator",} 444.0
okhttp3_network_requests_duration_seconds_sum{application="spring-boot-actuator",} 18.789
# HELP okhttp3_network_requests_duration_seconds_max  
okhttp3_network_requests_duration_seconds_max{application="spring-boot-actuator",} 1.204
# HELP okhttp3_network_requests_running_total  
okhttp3_network_requests_running_total{application="spring-boot-actuator",} 445.0
# HELP okhttp3_pool_dispatcher_max_requests_total max requests of dispatcher 
okhttp3_pool_dispatcher_max_requests_total{application="spring-boot-actuator",} 64.0
# HELP okhttp3_calls_failed_total  
okhttp3_calls_failed_total{application="spring-boot-actuator",} 0.0
# HELP okhttp3_dns_end_total  
okhttp3_dns_end_total{application="spring-boot-actuator",} 4.0
# HELP okhttp3_responses_body_started_total  
okhttp3_responses_body_started_total{application="spring-boot-actuator",} 444.0
# HELP okhttp3_responses_body_bytes_max  
okhttp3_responses_body_bytes_max{application="spring-boot-actuator",} 1142.0
# HELP okhttp3_responses_body_bytes  
okhttp3_responses_body_bytes_count{application="spring-boot-actuator",} 444.0
okhttp3_responses_body_bytes_sum{application="spring-boot-actuator",} 253524.0
# HELP okhttp3_network_requests_completed_total  
okhttp3_network_requests_completed_total{application="spring-boot-actuator",} 444.0
```

##### 3、Prometheus 集成


##### 4、Grafana 集成


## Jeebiz 技术社区

Jeebiz 技术社区 **微信公共号**、**小程序**，欢迎关注反馈意见和一起交流，关注公众号回复「Jeebiz」拉你入群。

|公共号|小程序|
|---|---|
| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/qrcode_for_gh_1d965ea2dfd1_344.jpg)| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/gh_09d7d00da63e_344.jpg)|
