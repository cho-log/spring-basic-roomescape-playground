#!/bin/bash
set -e

REPO_URL="https://github.com/jh-lee0/spring-basic-roomescape-playground"
BRANCH="step8"
APP_DIR="$HOME/app"
PID_FILE="$APP_DIR/app.pid"

# 1. Git 저장소 존재 여부 확인 및 클론
if [ -d "$APP_DIR/.git" ]; then
    echo "저장소가 이미 있습니다: $APP_DIR"
else
    git clone -b "$BRANCH" "$REPO_URL" "$APP_DIR"
fi

# 2. PID 파일 경로 설정 확인
mkdir -p "$(dirname "$PID_FILE")"
touch "$PID_FILE"
echo "PID 파일 경로: $PID_FILE"

# 3. 필요한 디렉토리 및 파일 권한 설정
chmod +x "$APP_DIR/gradlew"
[ -f "$APP_DIR/deploy.sh" ] && chmod +x "$APP_DIR/deploy.sh"

echo "초기 설정 완료"
