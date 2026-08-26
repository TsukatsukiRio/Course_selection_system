# Nacos 与 Sentinel 配置说明

## 1. Nacos 注册中心

各业务服务在 `application.yml` 中配置：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

启动后自动注册，网关通过 `lb://服务名` 负载均衡调用：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
```

### 1.1 共享配置（可选）

在 Nacos 控制台「配置管理→配置列表」中创建共享配置
`smart-campus-common.yaml`（Data ID 与各服务 application.name 对应），
示例内容：

```yaml
jwt:
  secret: smart-campus-course-selection-jwt-secret-2026-xwt
  expire-hours: 24
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```

服务引入方式（application.yml 中增加）：

```yaml
spring:
  config:
    import:
      - optional:nacos:smart-campus-common.yaml
```

`optional:` 前缀表示 Nacos 不可用时服务仍可启动，保证本地开发不受影响。

## 2. Sentinel 限流与熔断

### 2.1 已内置的保护点

| 资源名 | 位置 | 保护行为 |
| ---- | ---- | ---- |
| `select-course` | selection-service 选课接口 | 触发限流返回 429「当前选课人数较多，请稍后重试」；异常降级返回「选课服务繁忙」 |
| `recommend-list` | recommend-service 推荐接口 | 限流或熔断时返回热门课程兜底列表 |

Feign 调用已启用 Sentinel 支持（`feign.sentinel.enabled: true`），
课程服务不可用时选课服务返回友好提示而非长时间等待。

### 2.2 Sentinel Dashboard（可选）

下载 sentinel-dashboard-1.8.x.jar 后启动：

```bash
java -Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858 -jar sentinel-dashboard-1.8.6.jar
```

访问 http://localhost:8858（默认账号 sentinel/sentinel），
在各服务的 `application.yml` 中加入 Dashboard 地址：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8858
        port: 8719
```

### 2.3 推荐限流/熔断规则（演示）

在 Sentinel Dashboard 中对 `select-course` 资源添加规则：

- **流控规则**：QPS 阈值 200，流控效果「快速失败」；
- **熔断规则**：慢调用比例阈值 0.5、比例阈值 1000ms、最小请求数 5、统计时长 1000ms。

选课高峰期流量超过阈值时，接口立即返回 429 与友好提示，
后端服务不被击垮，验证「限流保障」需求（UC-12）。

### 2.4 网关层限流（可选）

gateway-service 已引入 `spring-cloud-alibaba-sentinel-gateway`，
可在网关按 API 路径配置限流。示例（Nacos 配置）：

```yaml
spring:
  cloud:
    sentinel:
      scg:
        fallback:
          mode: response
          response-status: 429
          response-body: '{"code":429,"message":"请求过于频繁","data":null}'
```

配合 Sentinel 网关流控规则（API 分组）即可实现网关层统一限流。
