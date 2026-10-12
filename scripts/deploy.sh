#!/usr/bin/env bash

set -e

APP_DIR="$HOME/roomescape"
RUN_DIR="$HOME/roomescape-run"
PID_FILE="$RUN_DIR/application.pid"
LOG_FILE="$RUN_DIR/application.log"
DEPLOY_BRANCH="step3"
SERVER_PORT="${SERVER_PORT:-8080}"
CHECK_URL="http://127.0.0.1:${SERVER_PORT}/"
STOP_TIMEOUT=30
STARTUP_TIMEOUT=30

if ! command -v curl >/dev/null 2>&1; then
    echo "서버 기동 확인에 curl이 필요합니다."
    exit 1
fi

if [ ! -d "$APP_DIR/.git" ] || [ ! -d "$RUN_DIR" ]; then
    echo "초기 설정이 되어 있지 않습니다. setup.sh를 먼저 실행해 주세요."
    exit 1
fi

cd "$APP_DIR"

git pull --ff-only origin "$DEPLOY_BRANCH"

./gradlew clean bootJar

JAR_FILE=$(find build/libs -maxdepth 1 -type f -name "*.jar" ! -name "*-plain.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "실행할 JAR 파일을 찾을 수 없습니다."
    exit 1
fi

if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")

    if [ -n "$OLD_PID" ] && kill -0 "$OLD_PID" 2>/dev/null; then
        echo "기존 애플리케이션을 종료합니다. PID: $OLD_PID"
        kill "$OLD_PID"

        for i in $(seq 1 "$STOP_TIMEOUT"); do
            kill -0 "$OLD_PID" 2>/dev/null || break
            sleep 1
        done

        if kill -0 "$OLD_PID" 2>/dev/null; then
            echo "기존 애플리케이션이 ${STOP_TIMEOUT}초 안에 종료되지 않아 배포를 중단합니다."
            exit 1
        fi
    else
        echo "실행 중인 기존 애플리케이션이 없습니다."
    fi

    rm -f "$PID_FILE"
else
    echo "PID 파일이 없습니다. 첫 배포로 보고 진행합니다."
fi

nohup java -jar "$JAR_FILE" --server.port="$SERVER_PORT" > "$LOG_FILE" 2>&1 &
NEW_PID=$!
echo "$NEW_PID" > "$PID_FILE"
echo "새 애플리케이션을 실행했습니다. PID: $NEW_PID"

for i in $(seq 1 "$STARTUP_TIMEOUT"); do
    if ! kill -0 "$NEW_PID" 2>/dev/null; then
        rm -f "$PID_FILE"
        echo "애플리케이션이 시작 중에 종료됐습니다. 로그를 확인하세요: $LOG_FILE"
        exit 1
    fi

    if curl --fail --silent --output /dev/null --max-time 1 "$CHECK_URL"; then
        echo "배포 완료: 서버가 응답합니다. ($CHECK_URL)"
        exit 0
    fi

    sleep 1
done

echo "${STARTUP_TIMEOUT}초 안에 서버가 응답하지 않았습니다. 로그를 확인하세요: $LOG_FILE"
exit 1
