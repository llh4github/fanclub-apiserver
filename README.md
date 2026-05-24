# Fanclub API

## 项目介绍

Fanclub API 是一个基于 Fiber 框架开发的 RESTful API 服务，支持用户认证、验证码系统、B站数据采集、树洞功能等模块。

## 📚 技术栈

- **Go 1.26.2** - 开发语言
- **Fiber v3** - Web 框架
- **GORM v2** - ORM 数据库访问（使用代码生成）
- **PostgreSQL** - 主数据库
- **Redis** - 缓存和会话管理
- **Sonyflake** - 分布式 ID 生成器
- **Swagger** - API 文档自动生成
- **Zap** - 高性能日志库
- **Viper** - 配置管理
- **JWT** - 身份认证

## 项目结构

```
fanclub-apiserver/
├── api/                    # REST API 控制器层
│   ├── rest/              # API 处理器
│   └── wrapper/            # 响应包装器
├── bilibili/              # B站数据采集模块
│   ├── client.go          # B站 API 客户端
│   ├── ws.go             # WebSocket 连接管理
│   ├── handler_*.go      # 事件处理器
│   └── task_*.go        # 定时任务
├── cache/                 # Redis 缓存层
│   ├── base.go          # 缓存基础功能
│   └── luas/            # Lua 脚本
├── consts/                # 常量定义
│   ├── captcha.go       # 验证码场景常量
│   ├── audit_status.go  # 审核状态常量
│   └── role.go          # 角色常量
├── database/             # 数据库层
│   ├── model/           # 数据模型（修改后需运行代码生成）
│   ├── generated/       # GORM 生成的代码
│   └── query_helper.go  # 查询辅助函数
├── dto/                  # 数据传输对象
│   ├── req/             # 请求参数结构体
│   └── resp/           # 响应数据结构体
├── errs/                 # 错误处理
│   ├── code.go         # 错误码定义
│   └── wrap.go         # 错误包装
├── g/                    # 全局工具和配置
│   ├── config.go       # 配置加载
│   ├── db.go           # 数据库连接
│   ├── redis.go        # Redis 连接
│   ├── jwt.go          # JWT 工具
│   └── logger.go       # 日志工具
├── middleware/           # 中间件
│   ├── jwt_handler.go  # JWT 认证中间件
│   └── error_handler.go # 错误处理中间件
├── resources/            # 资源文件
│   └── V0.9__base_tables.sql # 数据库 Schema
├── scheduler/            # 定时任务调度器
├── services/             # 业务逻辑层
├── utils/                # 工具函数
│   ├── crypto.go       # 加密解密工具
│   ├── markdown.go     # Markdown 处理
│   └── bcrypt.go       # 密码加密
├── docs/                 # Swagger 文档（自动生成）
├── main.go               # 入口文件
└── config.toml           # 配置文件
```

## 🚀 安装与运行

### 1. 环境要求

- Go 1.26.2+
- PostgreSQL 17+
- Redis 7+

### 2. 克隆项目

```bash
git clone <repository-url>
cd fanclub-apiserver
```

### 3. 安装依赖

```bash
go mod tidy
```

### 4. 安装 Swag 工具

```bash
go install github.com/swaggo/swag/cmd/swag@latest
```

### 5. 生成 Swagger 文档

```bash
swag fmt && swag init
```

### 6. 启动服务

使用 VSCode 调试功能启动，参考 `.vscode/launch.json` 配置。

或命令行启动：

```bash
go run main.go
```

## API 文档

启动服务后访问 Swagger UI：

```
http://localhost:8080/swagger
```

## ORM 使用（GORM）

### 代码生成

修改 `database/model` 目录下的文件后，需要运行代码生成：

```bash
gorm gen -i ./database/model -o ./database/generated
```

### 类型安全查询

```go
// 使用 GORM Typed API
q := typed.G[model.SysUser](g.DB)
q.
    Select(
        generated.BaseModel.ID,
        generated.SysUser.Username,
        generated.SysUser.Password,
    ).
    Where(generated.SysUser.Username.Eq(username)).
    Scan(appCtx.C, &user)
```

### 分页查询

```go
result, err := database.Page[model.TreeholeSubmission](
    appCtx.C,
    page,
    pageSize,
    generated.TreeholeSubmission.TopicID.Eq(topicID),
)
```

## 验证码系统

### 支持的验证码类型

- **点选验证码**：用户点击指定位置
- **滑动验证码**：用户滑动拼图到正确位置

### 验证码场景

- `login` - 登录场景
- `submission` - 投稿场景

### API 端点

```
GET  /api/captcha/click          # 生成点选验证码
POST /api/captcha/click/verify   # 验证点选验证码
GET  /api/captcha/slide/generate # 生成滑动验证码
POST /api/captcha/slide/verify  # 验证滑动验证码
```

## 🐳 Docker 部署

### 构建镜像

```bash
./image-build.sh
```

### 启动服务

```bash
docker-compose -f compose.yaml up -d
```

或使用预构建镜像：

```bash
docker run -d -p 8080:8080 \
  -v $(pwd)/config-docker.toml:/app/config.toml \
  fanclub-apiserver:latest
```

## ⚙️ VSCode 配置

项目提供了 VSCode 调试配置，简化开发流程。

### launch.json - 调试配置

| 配置名称             | 说明                                   |
| -------------------- | -------------------------------------- |
| Run with Swag Init   | 启动前自动执行 `swag fmt && swag init` |
| Run with Go Generate | 启动前执行预处理任务                   |

### tasks.json - 任务配置

| 任务名称         | 命令                                                   | 说明                      |
| ---------------- | ------------------------------------------------------ | ------------------------- |
| swag-fmt-init    | `swag fmt && swag init`                                | 格式化并生成 Swagger 文档 |
| gorm-gen         | `gorm gen -i ./database/model -o ./database/generated` | 生成 GORM 类型安全代码    |
| pre-launch-tasks | 依赖 swag-fmt-init                                     | 预处理任务                |

### 使用方法

1. 按 `F5` 或点击调试配置名称启动
2. 修改 `database/model` 后，运行 `gorm-gen` 任务重新生成代码

## 开发规范

### 代码规范

- 使用 Go 1.26.2 语法
- 函数和方法必须有简明的注释
- 结构体字段使用 snake_case 的 JSON 标签
- 使用 `g.Error()` 等方法记录日志

### API 规范

- 请求参数放在 `dto/req` 包
- 响应数据放在 `dto/resp` 包
- 使用 Swagger 注解生成文档
- 返回统一的 JSON 响应格式

### 数据库规范

- 使用 GORM 代码生成
- 修改 model 后运行代码生成
- SQL 文件放在 `resources` 目录
- 使用雪花算法生成 ID

## 🔧 配置说明

主要配置项（`config.toml`）：

```toml
[server]
host = "0.0.0.0"
port = "8080"

[database]
host = "localhost"
port = 5432
user = "postgres"
password = "your-password"
dbname = "fanclub"

[redis]
host = "localhost"
port = 6379
password = ""
db = 0

[bilibili]
cookies = []  # B站 Cookies 列表
```

## 常见问题

### Q: 修改 Model 后编译报错？

A: 需要运行代码生成命令：

```bash
gorm gen -i ./database/model -o ./database/generated
```

### Q: Swagger 文档不更新？

A: 重新生成文档：

```bash
swag fmt && swag init
```

### Q: 如何添加新的验证码场景？

A: 在 `consts/captcha.go` 中添加新的常量值

## 📄 License

Apache License 2.0
