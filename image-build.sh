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

echo "build docker images with version: $VERSION"

docker build --build-arg BUILD_TIME="$(date -u +"%Y-%m-%dT%H:%M:%SZ")" \
    --build-arg GIT_BRANCH="$(git rev-parse --abbrev-ref HEAD)" \
    --build-arg APP_VERSION="$VERSION" \
    --build-arg GIT_COMMIT_ID="$(git rev-parse --short HEAD)" \
    --build-arg GIT_COMMIT_TIME="$(git show -s --format=%ci HEAD)" \
    -t fanclub-apiserver:$VERSION -t fanclub-apiserver:latest .

echo "build docker images done."
docker image prune -f || true