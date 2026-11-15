#!/usr/bin/env bash

set -e

cd "$(dirname "$0")"

REPOSITORY_URL="https://github.com/MJW207/spring-basic-roomescape-playground.git"
DEPLOY_BRANCH="step3"

if [ ! -d ".git" ]; then
    PROJECT_DIR="spring-basic-roomescape-playground"

    if [ ! -d "$PROJECT_DIR/.git" ]; then
        git clone --branch "$DEPLOY_BRANCH" --single-branch "$REPOSITORY_URL" "$PROJECT_DIR"
    fi

    cd "$PROJECT_DIR"
fi

if [ "$(git branch --show-current)" != "$DEPLOY_BRANCH" ]; then
    echo "배포할 브랜치가 아니므로 중단합니다."
    exit 1
fi

RUN_DIR="$PWD/run"
PID_FILE="$RUN_DIR/application.pid"
LOG_FILE="$RUN_DIR/application.log"
APP_JAR="$RUN_DIR/application.jar"
STOP_TIMEOUT=30
STARTUP_CHECK_COUNT=30
CHECK_URL="http://127.0.0.1:8080/"

if ! command -v curl >/dev/null; then
    echo "서버 기동 확인에 필요한 curl이 없습니다."
    exit 1
fi

mkdir -p "$RUN_DIR"
chmod 700 "$RUN_DIR"

git pull --ff-only origin "$DEPLOY_BRANCH"

./gradlew clean bootJar

JAR_FILE=$(find build/libs -maxdepth 1 -type f -name "*.jar" ! -name "*-plain.jar" -print -quit)

if [ -z "$JAR_FILE" ]; then
    echo "실행할 JAR 파일을 찾을 수 없습니다."
    exit 1
fi

echo "실행할 JAR 파일: $JAR_FILE"

if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")

    if [[ ! "$OLD_PID" =~ ^[1-9][0-9]*$ ]]; then
        echo "PID 파일에 올바른 프로세스 번호가 없습니다."
        exit 1
    fi

    if kill -0 "$OLD_PID" 2>/dev/null; then
        PROCESS_COMMAND=$(ps -p "$OLD_PID" -o command=)

        if [[ "$PROCESS_COMMAND" != *"-jar $APP_JAR"* ]]; then
            echo "기록된 PID가 다른 프로그램을 가리켜 배포를 중단합니다."
            exit 1
        fi

        echo "기존 애플리케이션을 종료합니다. PID: $OLD_PID"
         kill "$OLD_PID"

         for ((elapsed = 0; elapsed < STOP_TIMEOUT; elapsed++)); do
             if ! kill -0 "$OLD_PID" 2>/dev/null; then
                 break
             fi

             sleep 1
         done

         if kill -0 "$OLD_PID" 2>/dev/null; then
             echo "기존 애플리케이션이 종료되지 않아 배포를 중단합니다."
             exit 1
         fi
    else
        echo "이전 PID에 해당하는 프로세스를 확인할 수 없습니다."
    fi
else
    echo "기록된 PID 파일이 없습니다."
fi

rm -f "$PID_FILE"

cp "$JAR_FILE" "$APP_JAR"

nohup java -jar "$APP_JAR" --spring.profiles.active=prod >> "$LOG_FILE" 2>&1 < /dev/null &

NEW_PID=$!
echo "$NEW_PID" > "$PID_FILE"

echo "새 애플리케이션 실행 요청. PID: $NEW_PID"
echo "로그 파일: $LOG_FILE"

for ((attempt = 0; attempt < STARTUP_CHECK_COUNT; attempt++)); do
    if ! kill -0 "$NEW_PID" 2>/dev/null; then
        rm -f "$PID_FILE"
        echo "새 애플리케이션이 종료됐습니다. 로그를 확인하세요: $LOG_FILE"
        exit 1
    fi

    if curl --fail --silent --output /dev/null --max-time 1 "$CHECK_URL"; then
        echo "서버의 HTTP 응답을 확인했습니다. PID: $NEW_PID"
        exit 0
    fi

    sleep 1
done

echo "서버의 HTTP 응답을 확인하지 못했습니다. 로그를 확인하세요: $LOG_FILE"
exit 1