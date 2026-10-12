#!/usr/bin/env bash

set -e

REPOSITORY_URL="https://github.com/anrwkd9902/spring-basic-roomescape-playground.git"
DEPLOY_BRANCH="step3"
APP_DIR="$HOME/roomescape"
RUN_DIR="$HOME/roomescape-run"

if [ -d "$APP_DIR/.git" ]; then
    echo "이미 Git 저장소가 존재합니다: $APP_DIR"
elif [ -e "$APP_DIR" ]; then
    echo "Git 저장소가 아닌 경로가 이미 존재합니다: $APP_DIR"
    exit 1
else
    git clone --branch "$DEPLOY_BRANCH" --single-branch "$REPOSITORY_URL" "$APP_DIR"
fi

mkdir -p "$RUN_DIR"
if [ ! -w "$RUN_DIR" ]; then
    echo "PID 파일을 저장할 디렉터리에 쓰기 권한이 없습니다: $RUN_DIR"
    exit 1
fi
echo "PID 파일 경로: $RUN_DIR/application.pid"

chmod +x "$APP_DIR/gradlew"
chmod +x "$APP_DIR/scripts/deploy.sh"

echo "초기 설정을 완료했습니다."
