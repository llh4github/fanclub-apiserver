# 构建阶段
FROM gradle:9.3.1-jdk25-graal-noble AS builder
WORKDIR /app
COPY .. .

# 定义构建参数
ARG APP_VERSION
ARG BUILD_TIME
ARG GIT_BRANCH
ARG GIT_COMMIT_ID
ARG GIT_COMMIT_TIME

# 设置资源目录变量以避免重复
ENV RESOURCES_DIR=src/main/resources

# 生成 git.properties 文件
RUN mkdir -p ${RESOURCES_DIR} && \
    echo "build.time=${BUILD_TIME}" > ${RESOURCES_DIR}/git.properties && \
    echo "app.version=${APP_VERSION}" >> ${RESOURCES_DIR}/git.properties && \
    echo "git.branch=${GIT_BRANCH}" >> ${RESOURCES_DIR}/git.properties && \
    echo "git.commit.id.abbrev=${GIT_COMMIT_ID}" >> ${RESOURCES_DIR}/git.properties && \
    echo "git.commit.time=${GIT_COMMIT_TIME}" >> ${RESOURCES_DIR}/git.properties

# 分阶段构建依赖缓存
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    gradle :nativeCompile -x test --no-daemon --parallel

# 运行阶段
FROM debian:bookworm-slim
WORKDIR /app

# 创建运行用户
RUN groupadd -r app && useradd -r -g app -s /bin/bash app
# 创建日志目录
RUN mkdir "logs"
# 创建 logs 目录并赋予 app 用户权限
RUN mkdir -p /app/logs && \
    chown -R app:app /app/logs

USER app:app

# 从构建阶段复制所有模块的构建产物
COPY --from=builder /app/build/native/nativeCompile/fanclub-vup  api-server

# 暴露应用端口
EXPOSE 8080
ENTRYPOINT ["./api-server", "--server.port=8080"]