#
# Copyright (c) 2026 llh
# Contact: lilinhong_coding@foxmail.com
#

# 读取 VERSION 文件中的版本号作为默认值
if [ -f "VERSION" ]; then
   VERSION=$(cat VERSION | tr -d '\r' | xargs)
else
   VERSION="latest"
fi

# 设置命名空间，默认为空字符串
NAMESPACE="${NAMESPACE:-}"
if [ -n "$NAMESPACE" ]; then
    NAMESPACE="${NAMESPACE}/"
fi

# 设置构建类型，默认为 native（可选值：native, jar）
BUILD_TYPE="${BUILD_TYPE:-native}"

# 根据构建类型选择 Dockerfile
if [ "$BUILD_TYPE" = "jar" ]; then
    DOCKERFILE="Dockerfile-jar"
    echo "📦 使用 JAR 模式构建镜像"
else
    DOCKERFILE="Dockerfile"
    echo "⚡ 使用 Native Image 模式构建镜像"
fi

echo "build docker images with version: $VERSION"
echo "build type: $BUILD_TYPE"
echo "dockerfile: $DOCKERFILE"

docker build --build-arg BUILD_TIME="$(date -u +"%Y-%m-%dT%H:%M:%SZ")" \
    --build-arg GIT_BRANCH="$(git rev-parse --abbrev-ref HEAD)" \
    --build-arg APP_VERSION="$VERSION" \
    --build-arg GIT_COMMIT_ID="$(git rev-parse --short HEAD)" \
    --build-arg GIT_COMMIT_TIME="$(git show -s --format=%ci HEAD)" \
    -f "$DOCKERFILE" \
    -t ${NAMESPACE}fanclub-apiserver:$VERSION -t ${NAMESPACE}fanclub-apiserver:latest .

echo "build docker images done."
docker image prune -f || true