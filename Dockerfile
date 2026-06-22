# 第一阶段：编译
FROM golang:1.26.2-alpine AS builder

WORKDIR /app

# 配置GOPROXY加速依赖下载
ENV GOPROXY=https://goproxy.cn,direct

# 复制go.mod和go.sum文件
COPY go.mod go.sum ./

# 下载依赖
RUN go mod download

# 复制所有源代码
COPY . .

# 生成 swagger 文档（确保 docs 包在构建时存在）
RUN go install github.com/swaggo/swag/cmd/swag@latest && swag init

# 编译
ARG VERSION=dev
ARG GIT_BRANCH=unknown
ARG GIT_COMMIT=unknown
ARG BUILD_TIME=unknown

RUN CGO_ENABLED=0 GOOS=linux go build \
  -a -installsuffix cgo \
  -ldflags "-X 'fanclub-apiserver/g.Version=$VERSION' -X 'fanclub-apiserver/g.Branch=$GIT_BRANCH' -X 'fanclub-apiserver/g.GitCommit=$GIT_COMMIT' -X 'fanclub-apiserver/g.BuildTime=$BUILD_TIME'" \
  -o app .

# 第二阶段：运行
FROM alpine:3.22.4 AS runtime

# 重新定义构建参数，以便在运行镜像阶段使用
ARG VERSION=dev
ARG GIT_BRANCH=unknown
ARG GIT_COMMIT=unknown
ARG BUILD_TIME=unknown

# 添加编译信息标签
LABEL version=$VERSION
LABEL git.branch=$GIT_BRANCH
LABEL git.commit=$GIT_COMMIT
LABEL build.time=$BUILD_TIME

WORKDIR /app

# 安装时区数据（解决 unknown time zone 问题）
RUN apk add --no-cache tzdata wget

# 创建logs目录
RUN mkdir -p logs

# 复制编译后的可执行文件
COPY --from=builder /app/app .

# 日志目录挂载点
VOLUME ["/app/logs"]

# 配置文件挂载点
VOLUME ["/app/config.toml"]

# 暴露端口
EXPOSE 8080

# 运行应用（使用环境变量设定端口）
ENV SERVER_PORT=8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD wget -qO- http://localhost:${SERVER_PORT}/livez || exit 1

CMD ["./app"]
