#!/usr/bin/env bash

set -euo pipefail

BRANCH="week10"
BASE_DIR="$HOME/roomescape"
APP_DIR="$BASE_DIR/app"
PID_FILE="$BASE_DIR/app.pid"
JAR_FILE="$BASE_DIR/roomescape.jar"
LOG_FILE="$BASE_DIR/logs/app.log"
STOP_TIMEOUT_SECONDS=30
HEALTH_CHECK_URL="http://localhost:8080/"
HEALTH_CHECK_TIMEOUT_SECONDS=60

# 저장소에서 최신 코드를 pull 받음
echo "[deploy] 최신 코드를 받습니다. (branch: $BRANCH)"
cd "$APP_DIR"
git fetch origin "$BRANCH"
git checkout "$BRANCH"
git pull --ff-only origin "$BRANCH"

# 프로젝트 빌드
echo "[deploy] 프로젝트를 빌드합니다."
./gradlew clean bootJar

BUILT_JAR="$(find build/libs -maxdepth 1 -name "*.jar" ! -name "*-plain.jar" | head -n 1)"
if [[ -z "$BUILT_JAR" ]]; then
    echo "[deploy] 빌드된 jar 파일을 찾을 수 없습니다."
    exit 1
fi

# 기존에 실행 중인 애플리케이션의 PID를 찾아 종료
if [[ ! -f "$PID_FILE" ]]; then
    echo "[deploy] 실행 중인 애플리케이션이 없습니다. (PID 파일 없음)"
else
    PID="$(cat "$PID_FILE")"
    if ! kill -0 "$PID" 2> /dev/null; then
        echo "[deploy] PID $PID 프로세스가 이미 종료되어 있습니다."
    else
        echo "[deploy] 실행 중인 애플리케이션(PID $PID)을 종료합니다."
        kill "$PID"
        for _ in $(seq 1 "$STOP_TIMEOUT_SECONDS"); do
            kill -0 "$PID" 2> /dev/null || break
            sleep 1
        done
        if kill -0 "$PID" 2> /dev/null; then
            echo "[deploy] ${STOP_TIMEOUT_SECONDS}초 안에 종료되지 않아 강제 종료합니다."
            kill -9 "$PID"
        fi
        echo "[deploy] 종료되었습니다."
    fi
    rm -f "$PID_FILE"
fi

# 빌드된 애플리케이션 실행
if curl -fs -o /dev/null --max-time 2 "$HEALTH_CHECK_URL"; then
    echo "[deploy] 이미 다른 애플리케이션이 응답하고 있습니다. ($HEALTH_CHECK_URL) 먼저 종료하세요."
    exit 1
fi

cp "$BUILT_JAR" "$JAR_FILE"
echo "[deploy] 애플리케이션을 실행합니다. (로그: $LOG_FILE)"
nohup java -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
APP_PID=$!
echo "$APP_PID" > "$PID_FILE"

for _ in $(seq 1 "$HEALTH_CHECK_TIMEOUT_SECONDS"); do
    if ! kill -0 "$APP_PID" 2> /dev/null; then
        echo "[deploy] 애플리케이션이 실행 중에 종료되었습니다. 로그를 확인하세요."
        tail -n 30 "$LOG_FILE"
        exit 1
    fi
    if curl -fs -o /dev/null --max-time 2 "$HEALTH_CHECK_URL"; then
        echo "[deploy] 실행되었습니다. (PID $APP_PID)"
        exit 0
    fi
    sleep 1
done

echo "[deploy] ${HEALTH_CHECK_TIMEOUT_SECONDS}초 안에 응답하지 않습니다. 로그를 확인하세요."
tail -n 30 "$LOG_FILE"
exit 1
