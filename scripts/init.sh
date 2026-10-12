#!/usr/bin/env bash

set -euo pipefail

REPO_URL="https://github.com/ThreeeJ/spring-basic-roomescape-playground.git"
BRANCH="week10"
BASE_DIR="$HOME/roomescape"
APP_DIR="$BASE_DIR/app"
PID_FILE="$BASE_DIR/app.pid"
LOG_FILE="$BASE_DIR/logs/app.log"

# 필요한 명령어 설치 여부 확인
for command in git java curl; do
    if ! command -v "$command" > /dev/null; then
        echo "[init] $command 명령을 찾을 수 없습니다. 먼저 설치하세요."
        exit 1
    fi
done

# Git 저장소 존재 여부 확인 및 클론
if [[ -d "$APP_DIR/.git" ]]; then
    echo "[init] Git 저장소가 이미 있습니다. ($APP_DIR)"
elif [[ -e "$APP_DIR" ]]; then
    echo "[init] $APP_DIR 경로가 이미 있지만 Git 저장소가 아닙니다."
    exit 1
else
    echo "[init] Git 저장소를 클론합니다. ($REPO_URL, branch: $BRANCH)"
    mkdir -p "$(dirname "$APP_DIR")"
    git clone --branch "$BRANCH" --single-branch "$REPO_URL" "$APP_DIR"
fi

# PID 파일 경로 설정 확인
PID_DIR="$(dirname "$PID_FILE")"
mkdir -p "$PID_DIR"

if [[ -e "$PID_FILE" && ! -f "$PID_FILE" ]]; then
    echo "[init] PID 파일 경로를 사용할 수 없습니다. ($PID_FILE)"
    exit 1
fi
if [[ ! -w "$PID_DIR" ]]; then
    echo "[init] PID 파일 경로에 쓸 수 없습니다. ($PID_DIR)"
    exit 1
fi

echo "[init] PID 파일 경로: $PID_FILE"

# 필요한 디렉토리 및 파일 권한 설정
mkdir -p "$(dirname "$LOG_FILE")"
chmod u+x "$APP_DIR/gradlew" "$APP_DIR"/scripts/*.sh

echo "[init] 로그 디렉토리와 실행 권한을 설정했습니다."
echo "[init] 초기 설정이 끝났습니다. 배포하려면 $APP_DIR/scripts/deploy.sh 를 실행하세요."
