#!/bin/bash
set -euo pipefail

APP_DIR="$HOME/roomescape"
REPO_URL="https://github.com/KDYoung5606/spring-basic-roomescape-playground.git"
BRANCH="step7-10"
PID_FILE="$APP_DIR/app.pid"

if [ -d "$APP_DIR/.git" ]; then
  echo "저장소가 이미 존재합니다: $APP_DIR"
else
  git clone --branch "$BRANCH" "$REPO_URL" "$APP_DIR"
fi

mkdir -p "$(dirname "$PID_FILE")"
echo "PID 파일 경로: $PID_FILE"

chmod +x "$APP_DIR/gradlew" "$APP_DIR/scripts/deploy.sh"
echo "초기 설정이 완료되었습니다."
