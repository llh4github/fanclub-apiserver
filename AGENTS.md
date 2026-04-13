# Agent

本文是AI编程的指导文档，所有AI编程工具都必须遵循本文的规范。

1. 你必须使用中文回答问题。
2. 所有函数体不得超过100行。
3. ORM框架使用 Jimmer, 使用文档链接 https://babyfish-ct.github.io/jimmer-doc/zh/docs/overview/welcome
4. kotlin 代码修改需通过编译。
5. 生成代码包含必要注释，重点解释为什么而非是什么。
6. 对于data class 和其他用于数据传输的类字段应加上简明的中文说明

## 项目结构

```
fanclub-apiserver/                              # 项目根目录
├── fanclub-apiserver/                    # 主应用服务模块
│   ├── src/main/kotlin/
│   │   ├── api/                         # REST API 接口层（anchor/sys/viewer）
│   │   ├── components/                  # Spring 组件配置
│   │   ├── dto/                         # 数据传输对象
│   │   ├── entity/                      # Jimmer 数据库实体
│   │   ├── service/                     # 业务逻辑层
│   │   ├── websocket/                   # WebSocket 处理器
│   │   └── FanclubVupApplication.kt    # 应用启动类
│   ├── src/main/resources/
│   │   ├── lua/                         # Redis Lua 脚本
│   │   ├── db/pgsql/                    # PostgreSQL 迁移脚本
│   │   └── application*.yaml            # 应用配置文件
│   └── build.gradle.kts                # 模块构建配置
├── fanclub-bilibili/                    # B站客户端模块
├── fanclub-common/                      # 公共模块（常量/异常/工具类）
├── fanclub-ksp/                         # KSP 代码生成模块
├── build.gradle.kts                     # 根项目构建配置
├── settings.gradle.kts                  # Gradle 设置文件
├── compose.yaml                         # Docker Compose 配置
└── README.md                            # 项目说明文档
```

## kotlin项目包管理风格
kotlin 代码模块目录结构遵循省略了公共根包的包结构。
例如：
```text

├── fanclub-apiserver/                    # 主应用服务模块
│   ├── src/main/kotlin/
│   │   ├── api/                         # REST API 接口层（按业务域划分）
│   │   ├── components/                  # Spring 组件配置
│   │   ├── consts/                      # 常量定义
│   │   ├── dto/                         # 数据传输对象
│   │   ├── entity/                      # Jimmer 数据库实体
│   │   ├── service/                     # 业务逻辑层
│   │   ├── websocket/                   # WebSocket 处理器
│   │   └── FanclubVupApplication.kt    # 应用启动类
```
1. FanclubVupApplication.kt启动类在`llh.fanclubvup.apiserver`包下，但它位`src/main/kotlin/`目录下。
2. 其余子包在主类（启动类）目录下依次创建。
3. 这是不创建公共空目录包，并非在类声明中省略公共类前缀。
