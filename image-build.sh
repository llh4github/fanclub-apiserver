#!/bin/bash

# 读取版本号
if [ -f VERSION ]; then
    VERSION=$(cat VERSION)
else
    VERSION="0.0.0"
fi

# 获取 git 信息
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
BUILD_TIME=$(date +"%Y-%m-%d %H:%M:%S")

# 设置命名空间，默认为空字符串
NAMESPACE="${NAMESPACE:-}"
if [ -n "$NAMESPACE" ]; then
    NAMESPACE="${NAMESPACE}/"
fi

# 是否推送镜像，默认为 false
PUSH="${PUSH:-false}"

# 构建 Docker 镜像
docker build \
    --build-arg VERSION="$VERSION" \
    --build-arg GIT_BRANCH="$GIT_BRANCH" \
    --build-arg GIT_COMMIT="$GIT_COMMIT" \
    --build-arg BUILD_TIME="$BUILD_TIME" \
    -t "${NAMESPACE}fanclub-apiserver:$VERSION" \
    -t "${NAMESPACE}fanclub-apiserver:latest" .

# 获取构建命令的退出码
build_exit_code=$?

# 根据退出码判断构建结果
if [ $build_exit_code -eq 0 ]; then
    # 构建成功时显示构建信息
    echo "构建完成：${NAMESPACE}fanclub-apiserver:$VERSION, Git 分支: $GIT_BRANCH, Git 提交: $GIT_COMMIT, 构建时间: $BUILD_TIME"
    
    # 如果设置了推送标志，则推送镜像
    if [ "$PUSH" = "true" ]; then
        echo "开始推送镜像..."
        docker push "${NAMESPACE}fanclub-apiserver:latest"
        docker push "${NAMESPACE}fanclub-apiserver:${VERSION}"
        echo "镜像推送完成"
    fi
    
    docker image prune -f
else
    # 构建失败时提示
    echo "构建失败，退出码: $build_exit_code，请检查错误信息"
    exit $build_exit_code
fi
