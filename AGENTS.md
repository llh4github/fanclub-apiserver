# Agent

本文是AI编程的指导文档，所有AI编程工具都必须遵循本文的规范。

1. 你必须使用中文回答问题。
2. 所有函数体不得超过100行。
3. ORM框架使用 Jimmer, 使用文档链接 https://babyfish-ct.github.io/jimmer-doc/zh/docs/overview/welcome
4. kotlin 代码修改需通过编译。
5. 生成代码包含必要注释，重点解释为什么而非是什么。
6. 对于data class 和其他用于数据传输的类字段应加上简明的中文说明
7. 根目录下的prompt文件夹为AI的 skill、rule 提示词 

## 项目结构

```
fanclub-apiserver/                              # 项目根目录
├── fanclub-apiserver/                    # 主应用服务模块
│   ├── src/main/kotlin/
│   │   ├── api/                         # REST API 接口层（按业务域划分）
│   │   │   ├── anchor/                  # 主播相关接口
│   │   │   ├── sys/                     # 系统管理接口
│   │   │   └── viewer/                  # 观众相关接口
│   │   ├── components/                  # Spring 组件配置
│   │   │   ├── RedisConfig.kt          # Redis 配置
│   │   │   ├── WebSocketConfig.kt      # WebSocket 配置
│   │   │   └── ...                      # 其他组件配置
│   │   ├── consts/                      # 常量定义
│   │   ├── dto/                         # 数据传输对象
│   │   │   ├── anchor/                  # 主播相关 DTO
│   │   │   ├── sys/                     # 系统相关 DTO
│   │   │   └── viewer/                  # 观众相关 DTO
│   │   ├── entity/                      # Jimmer 数据库实体
│   │   │   ├── anchor/                  # 主播实体
│   │   │   ├── sys/                     # 系统实体
│   │   │   └── viewer/                  # 观众实体
│   │   ├── service/                     # 业务逻辑层
│   │   │   ├── AnchorService.kt        # 主播服务
│   │   │   ├── ViewerService.kt        # 观众服务
│   │   │   └── ...                      # 其他业务服务
│   │   ├── statistics/                  # 数据统计模块
│   │   │   └── DanmuStatistics.kt      # 弹幕统计
│   │   ├── websocket/                   # WebSocket 处理器
│   │   │   ├── DanmuWebSocketHandler.kt # 弹幕 WebSocket
│   │   │   └── ...
│   │   └── FanclubVupApplication.kt    # 应用启动类
│   ├── src/main/resources/
│   │   ├── lua/                         # Redis Lua 脚本
│   │   │   ├── statistics_danmu.lua    # 弹幕统计脚本
│   │   │   └── ...
│   │   ├── db/pgsql/                    # PostgreSQL 迁移脚本
│   │   ├── application.yaml            # 主配置文件
│   │   ├── application-dev.yaml        # 开发环境配置
│   │   ├── application-docker.yaml     # Docker 环境配置
│   │   └── log4j2-spring.xml           # Log4j2 日志配置
│   └── build.gradle.kts                # 模块构建配置
│
├── fanclub-bilisdk/                     # B站 SDK 模块
│   ├── src/main/kotlin/
│   │   ├── cache/                       # 缓存管理
│   │   │   ├── BiliSignCacheManager.kt # 签名缓存管理器
│   │   │   └── PersistentCookieJarManager.kt # Cookie 持久化管理
│   │   ├── consts/                      # SDK 常量
│   │   │   ├── BiliApiUrls.kt          # B站 API URL 集合
│   │   │   └── ScraperConst.kt         # 爬虫常量
│   │   ├── dm/                          # 弹幕处理
│   │   │   ├── CommandProcessor.kt     # 弹幕命令解析器
│   │   │   └── ...
│   │   ├── dto/                         # 响应数据模型
│   │   │   ├── danmu/                   # 弹幕相关 DTO
│   │   │   │   ├── DanmuInfoData.kt    # 弹幕服务器信息
│   │   │   │   └── HostServer.kt       # 服务器主机信息
│   │   │   ├── UserInfoResponse.kt     # 用户信息响应
│   │   │   ├── GuardPageResponse.kt    # 舰长列表响应
│   │   │   └── ...
│   │   ├── enums/                       # 枚举类型
│   │   ├── event/                       # Spring 事件
│   │   ├── http/                        # HTTP 客户端
│   │   ├── props/                       # 配置属性
│   │   │   └── BiliScraperProp.kt      # 爬虫配置
│   │   ├── scraper/                     # 数据采集器
│   │   │   ├── BiliScraperClient.kt    # B站爬虫客户端
│   │   │   ├── BiliDanmuWebSocketHandler.kt # 弹幕 WebSocket 处理器
│   │   │   ├── BiliWsMsgBizHandler.kt  # 弹幕消息业务处理器
│   │   │   └── BiliWsAuthFetcher.kt    # WebSocket 认证获取器
│   │   ├── utils/                       # 工具类
│   │   └── BiliSdkAutoConfig.kt        # SDK 自动配置类
│   └── build.gradle.kts                # 模块构建配置
│
├── fanclub-common/                      # 公共模块
│   ├── src/main/kotlin/
│   │   ├── consts/                      # 通用常量
│   │   ├── exceptions/                  # 自定义异常
│   │   │   └── AppRuntimeException.kt  # 应用运行时异常
│   │   ├── utils/                       # 通用工具类
│   │   │   ├── Md5Utils.kt             # MD5 加密工具
│   │   │   └── ...
│   │   ├── ext.kt                       # Kotlin 扩展函数
│   │   └── package.kt                   # 包级函数和常量
│   └── build.gradle.kts                # 模块构建配置
│
├── fanclub-ksp/                         # KSP 代码生成模块
│   ├── src/main/kotlin/
│   │   ├── annon/                       # 自定义注解
│   │   ├── processor/                   # KSP 处理器
│   │   └── KspRegister.kt              # KSP 注册器
│   └── build.gradle.kts                # 模块构建配置
│
├── build.gradle.kts                     # 根项目构建配置
├── settings.gradle.kts                  # Gradle 设置文件
├── compose.yaml                         # Docker Compose 配置
└── README.md                            # 项目说明文档
```

### 模块说明

#### 1. **fanclub-apiserver** - 主应用服务

- **职责**：提供 REST API、WebSocket 服务、业务逻辑处理
- **技术栈**：Spring Boot 4.0.3 + Jimmer ORM + Log4j2
- **核心功能**：
    - 主播数据管理
    - 观众数据分析
    - 实时弹幕统计
    - 直播状态监控
    - WebSocket 实时推送

#### 2. **fanclub-bilisdk** - B站 SDK 模块

- **职责**：封装 B站 API 调用、弹幕 WebSocket 连接、数据采集
- **核心组件**：
    - `BiliScraperClient`：HTTP 接口调用客户端
    - `BiliDanmuWebSocketHandler`：弹幕 WebSocket 连接管理
    - `CommandProcessor`：弹幕消息解析器
    - 缓存管理：WBI 签名、Cookie 持久化
- **关键流程**：
    - 获取弹幕服务器配置 → 建立 WebSocket 连接 → 发送认证包 → 心跳保活 → 接收弹幕消息

#### 3. **fanclub-common** - 公共模块

- **职责**：提供通用工具类、异常定义、扩展函数
- **被依赖**：所有其他模块
- **无外部依赖**：保持轻量级

#### 4. **fanclub-ksp** - KSP 代码生成模块

- **职责**：编译时代码生成（Jimmer 实体元数据等）
- **触发时机**：Gradle 编译时自动执行
- **注意**：代码需编译一次后才"完整"

### 数据流向

```
用户请求 → API 层 → Service 层 → Jimmer ORM → PostgreSQL
                                    ↓
                              Redis 缓存 (Lua 脚本)
                                    ↓
B站数据采集 ← BiliScraperClient ← WebSocket/HTTP
                                    ↓
                              弹幕统计 → Redis/Ehcache
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
