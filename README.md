# Fanclub VUP - B 站虚拟主播数据监控系统

一个基于 Kotlin + Spring Boot 4 的 B站虚拟主播数据监控系统，支持实时弹幕统计、直播状态追踪、观众数据分析等功能。

## 📁 项目结构

```
fanclub-vup/
├── fanclub-apiserver/        # 主应用服务
│   ├── src/main/kotlin/
│   │   ├── api/             # REST API 接口层
│   │   ├── components/      # Spring 组件配置
│   │   ├── consts/          # 常量定义
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 数据库实体
│   │   ├── service/         # 业务逻辑层
│   │   ├── statistics/      # 数据统计模块
│   │   └── websocket/       # WebSocket 处理器
│   └── src/main/resources/
│       ├── lua/             # Redis Lua 脚本
│       └── db/mariadb/      # 数据库迁移脚本
│
├── fanclub-bilisdk/         # B 站 SDK 模块
│   ├── cache/               # 缓存管理
│   ├── dm/                  # 弹幕处理
│   ├── http/                # HTTP 客户端
│   └── scraper/             # 数据采集器
│
├── fanclub-common/          # 公共模块
│   ├── utils/               # 工具类
│   └── exceptions/          # 自定义异常
│
├── docker/                  # Docker 相关配置
├── compose.yaml            # Docker Compose 配置
└── build.gradle.kts        # Gradle 构建配置
```

## 🛠️ 技术栈与版本

### 核心框架

- **JDK**: 25 (GraalVM)
- **Kotlin**: 2.3.10
- **Spring Boot**: 4.0.3
- **GraalVM Native Image**: 0.11.4

### 主要依赖库

- **ORM**: [Jimmer](https://babyfish-ct.github.io/jimmer-doc/zh/docs/overview/welcome/)(Kotlin 优先的 ORM 框架)
- **数据库驱动**: MariaDB Java Client
- **缓存**: Redis 7.4.0 + Spring Data Redis
- **本地缓存**: Ehcache
- **JSON 处理**: Jackson (tools.jackson.module)
- **日志框架**: Log4j2 (spring-boot-starter-log4j2)
- **API 文档**: SpringDoc OpenAPI 3.0.2 + Swagger UI
- **ID 生成**: Yitter.IdGenerator 1.0.6

### 构建工具

- **Gradle**: 9.3.1
- **KSP**: 2.3.5 (Kotlin Symbol Processing)

## 🗄️ 外部服务依赖

### 必需服务

1. **MariaDB 11.8.5**
    - 端口：3306
    - 数据库：fanclub_dev
    - 字符集：utf8mb4_unicode_ci

2. **Redis Stack 7.4.0**
    - 端口：6379
    - 功能：
        - 缓存热点数据
        - 弹幕计数统计
        - JWT Token 管理
        - 布隆过滤器 (需要 RedisBloom 模块)

### 可选服务

- **Docker Compose**: 用于本地开发环境快速启动依赖服务

## 🚀 本地启动指南

### 前置要求

1. **JDK 25** (推荐使用 GraalVM 或 Oracle JDK)
2. **Docker & Docker Compose** (推荐)
3. **Git** (用于版本控制和 Git 信息生成)

### 方式一：使用 Docker  (推荐)

#### 1. 项目导入IDEA

使用 `FanclubSever-local-docker` 启动项目，这将自动:

- 启动ariaDB 数据库
- 启动Redis Stack
- 创建数据库结构

#### 2. 访问应用

- **应用地址**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator 端点**: http://localhost:8080/actuator

### 方式二：完全手动配置

如果不想使用 Docker，需要手动安装和配置。

1. 配置数据库和缓存服务
2. 修改配置文件
3. 启动应用

## 🔧 开发与调试

### IDE 配置

- **推荐 IDE**: IntelliJ IDEA
- **必要插件**:
    - Kotlin
    - Spring Boot
    - Gradle

**项目使用KSP，代码编译一次后才是“完整”的。**

### 构建 Native Image (可选)

```bash
# 需要安装 GraalVM
./gradlew :fanclub-apiserver:nativeCompile -x test

# 生成的可执行文件位于:
# fanclub-apiserver/build/native/nativeCompile/
```

或者使用已有脚本构建 Docker 镜像

```bash
image-build.sh
```

## 📝 注意事项

1. **Redis 配置**: Redis 7.x 必须安装 RedisBloom 模块
2. **数据库字符集**: 必须使用 `utf8mb4`，否则中文会出现乱码
3. **Flyway 迁移**: 生产环境建议设置 `spring.flyway.enabled=false`
4. **性能优化**: 生产环境建议关闭 SQL 显示
5. **KSP插件**: 代码需编译一次后才是“完整”的

## 📄 License

[Apache License](./LICENSE)
