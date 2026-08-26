# 智能校园微服务课程选退课系统

> 基于 Spring Cloud 微服务架构的智能校园课程选退课系统，覆盖学生选课退课、教师授课管理、教务管理统计全流程，内置 AI 课程推荐与完整微服务治理体系。

## 项目介绍

本系统以高校教务场景中的“课程选退课”业务为核心，采用前后端分离与微服务架构设计：

- **后端**：Java 17 + Spring Boot 3 + Spring Cloud + Spring Cloud Alibaba，按业务域拆分为网关与 5 个独立微服务，通过 Nacos 注册发现、OpenFeign 服务调用、Sentinel 限流熔断实现服务治理；
- **前端**：Vue 3 + Vite + Element Plus + Pinia + ECharts 单页应用，通过网关统一访问后端；
- **数据**：MySQL 7 张核心表（用户 / 公告 / 课程 / 教学计划 / 选课记录 / 学生成绩 / 选课配置），MyBatis-Plus 持久层；
- **安全**：JWT 无状态认证 + BCrypt 密码加密 + 登录失败锁定策略，网关统一鉴权、角色校验与内部接口防护。

## 功能特性

### 学生端
- **注册登录**：学号注册、JWT 登录，连续失败 5 次锁定账号 30 分钟
- **首页推荐**：基于兴趣标签的内容推荐 + 选课行为协同过滤 + 热门课程兜底（推荐服务异常时自动降级）
- **课程检索**：按课程名称、编号、教师、院系、类型、学分组合检索，查看课程详情与教学计划
- **选课**：自动检测时间冲突、先修课程、课程容量、选课门数（默认 5 门）与学分（默认 30）上限、选课时间窗口
- **退课**：退课截止时间控制、课程名额自动恢复、选课历史记录
- **课表**：周视图课表与课程安排
- **个人中心**：资料修改、密码修改

### 教师端
- **我的课程**：查看授课课程与选课学生名单
- **成绩管理**：学生成绩录入与查询
- **教学计划**：提交教学计划，由管理员审核

### 管理员端
- **用户管理**：学生 CSV 批量导入、教师 / 管理员创建、启禁用、重置密码
- **课程管理**：课程增删改、开停课状态维护、教学计划审核
- **选课参数**：选课 / 退课时间窗口、门数与学分上限配置
- **选课记录**：全量选退课记录分页查询
- **公告管理**：系统公告发布与下架
- **数据统计**：选课趋势、课程热度排行、院系选课率、课程类型分布、时段热力与 CSV 导出

### 微服务治理
- Nacos 服务注册发现，网关基于 `lb://` 的负载均衡路由
- 网关全局 JWT 鉴权、按路径前缀的角色校验、内部接口保护、跨域处理
- Sentinel：选课接口限流、推荐服务熔断降级（热门课程兜底）
- OpenFeign 服务间调用、统一响应结构、全局异常处理

## 技术栈

| 层次 | 技术 |
| ---- | ---- |
| 后端 | Java 17、Spring Boot 3.0.2、Spring Cloud 2022.0.0、Spring Cloud Alibaba 2022.0.0.0 |
| 微服务组件 | Nacos 2.x（注册/配置）、Spring Cloud Gateway（网关）、OpenFeign（服务调用）、Sentinel（限流熔断） |
| 持久层 | MyBatis-Plus 3.5.5 + MySQL 8.0（兼容 5.7） |
| 前端 | Vue 3 + Element Plus + Vite + Pinia + ECharts |
| 认证 | JWT（HS256，24 小时有效期）+ BCrypt 密码加密 |

## 模块结构

| 模块 | 端口 | 职责 |
| ---- | ---- | ---- |
| common-core | - | 统一响应 R、业务异常、JWT 工具、常量 |
| common-web | - | 全局异常处理、通用配置 |
| gateway-service | 8080 | 统一入口：路由转发、JWT 鉴权、角色校验、跨域 |
| user-service | 8081 | 注册登录、用户管理、公告管理、内部用户查询 |
| course-service | 8082 | 课程 CRUD/检索、教学计划、成绩录入、名额原子扣减 |
| selection-service | 8083 | 选课/退课、时间冲突检测、先修检测、课表、选课配置 |
| recommend-service | 8084 | AI 推荐（内容 + 协同过滤 + 热门兜底，Sentinel 熔断降级） |
| statistics-service | 8085 | 选课趋势、课程热度、院系选课率、类型分布、时段热力、CSV 导出 |

## 项目目录

```
Course_selection_system
├── common-core                 # 公共模块：统一响应、异常、JWT、常量
├── common-web                  # 公共模块：全局异常处理
├── gateway-service             # 网关 8080
├── user-service                # 用户服务 8081
├── course-service              # 课程服务 8082
├── selection-service           # 选课服务 8083
├── recommend-service           # 推荐服务 8084
├── statistics-service          # 统计服务 8085
├── frontend                    # Vue3 前端（Vite + Element Plus + Pinia + ECharts）
├── sql/init_smart_campus.sql   # 数据库初始化脚本（建表 + 种子数据）
├── docs/                       # 部署说明、接口清单、Nacos 与 Sentinel 配置
├── 一键启动.cmd                # Windows 一键启动脚本
├── 一键停止.cmd                # Windows 一键停止脚本
└── pom.xml                     # Maven 父工程
```

## 快速开始

### 环境要求
- JDK 17+、Maven 3.8+
- MySQL 5.7 / 8.0
- Nacos 2.x（本地示例：`O:\nacos-server-2.3.2\nacos`）
- Node.js 18+

### 1. 初始化数据库
```bash
mysql -u root -p < sql/init_smart_campus.sql
```
> 各服务默认数据源账号密码为 `root/root`，如不一致请同步修改对应 `application.yml`。

### 2. 后端构建
```bash
mvn clean package -DskipTests
```

### 3. 启动后端（完整微服务模式）
1. 启动 Nacos 单机：`O:\nacos-server-2.3.2\nacos\bin\startup.cmd -m standalone`
2. 依次启动 5 个业务服务，再启动网关（各自新开窗口）：
   ```bash
   java -jar user-service/target/user-service-1.0.0.jar
   java -jar course-service/target/course-service-1.0.0.jar
   java -jar selection-service/target/selection-service-1.0.0.jar
   java -jar recommend-service/target/recommend-service-1.0.0.jar
   java -jar statistics-service/target/statistics-service-1.0.0.jar
   java -jar gateway-service/target/gateway-service-1.0.0.jar
   ```
3. 本地直连模式（不依赖 Nacos）：各服务加 `--spring.profiles.active=local` 启动。

### 4. 启动前端
```bash
cd frontend
npm.cmd install
npm.cmd run dev
```
浏览器访问 http://localhost:5173（Vite 将 `/api` 代理到网关 http://localhost:8080）。

### Windows 一键启停
双击 `一键启动.cmd`：按 Nacos → 业务服务 → 网关 → 前端 的顺序自动启动并打开浏览器；
双击 `一键停止.cmd`：停止前端、网关、业务服务与 Nacos（MySQL 保留运行）。

## 默认账号（由初始化脚本写入）

| 角色 | 账号 | 密码 |
| ---- | ---- | ---- |
| 管理员 | admin | Admin123 |
| 教师 | T1001 / T1002 / T1003 | Pass1234 |
| 学生 | 20210001 ~ 20210005 | Pass1234 |

## 文档

- `docs/部署说明.md`：环境要求、两种运行模式、端口规划、常见问题
- `docs/接口清单.md`：全部微服务接口与鉴权方式
- `docs/nacos与sentinel配置.md`：Nacos 与 Sentinel 规则说明
