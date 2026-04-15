# 构建阶段
FROM gradle:9.4.1-jdk25-graal-noble AS builder
WORKDIR /app
COPY .. .

ENV SPRING_DOCKER_COMPOSE_ENABLED=false
# 定义构建参数
ARG APP_VERSION
ARG BUILD_TIME
ARG GIT_BRANCH
ARG GIT_COMMIT_ID
ARG GIT_COMMIT_TIME

# 设置资源目录变量以避免重复
ENV RESOURCES_DIR=fanclub-apiserver/src/main/resources

# 生成 git.properties 文件
RUN mkdir -p ${RESOURCES_DIR} && \
    echo "build.time=${BUILD_TIME}" > ${RESOURCES_DIR}/git.properties && \
    echo "app.version=${APP_VERSION}" >> ${RESOURCES_DIR}/git.properties && \
    echo "git.branch=${GIT_BRANCH}" >> ${RESOURCES_DIR}/git.properties && \
    echo "git.commit.id.abbrev=${GIT_COMMIT_ID}" >> ${RESOURCES_DIR}/git.properties && \
    echo "git.commit.time=${GIT_COMMIT_TIME}" >> ${RESOURCES_DIR}/git.properties

# 分阶段构建依赖缓存
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    gradle :fanclub-apiserver:nativeCompile -x test --no-daemon --parallel

# 运行阶段
FROM debian:bookworm-slim
WORKDIR /app

# 安装 AWT 所需依赖
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig \
    libfreetype6 \
    libxext6 \
    libxrender1 \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

ARG APP_VERSION
ARG GIT_COMMIT_ID

LABEL maintainer="lilinhong_coding@foxmail.com" \
      license="Apache-2.0" \
      git_commit_id=${GIT_COMMIT_ID} \
      version=${APP_VERSION} \
      description="A api server."
# 设置禁用 Flyway 的环境变量
ENV SPRING_FLYWAY_ENABLED=false
ENV SPRING_DOCKER_COMPOSE_ENABLED=false
ENV SPRING_APPLICATION_VERSION=${APP_VERSION}
# 创建日志目录
RUN mkdir "logs"

# 从构建阶段复制所有模块的构建产物
COPY --from=builder /app/fanclub-apiserver/build/native/nativeCompile/fanclub-apiserver  api-server

# 暴露应用端口
EXPOSE 8080
ENTRYPOINT ["./api-server", "--server.port=8080"]
