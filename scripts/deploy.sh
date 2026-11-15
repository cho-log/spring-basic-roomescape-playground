#!/bin/bash
set -euo pipefail

APP_DIR="$HOME/roomescape"
BRANCH="step7-10"
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$APP_DIR/app.log"

cd "$APP_DIR"

git pull origin "$BRANCH"

./gradlew clean bootJar

JAR=$(ls build/libs/*.jar)
echo "빌드 완료: $JAR"

if [ -f "$PID_FILE" ]; then
  OLD_PID=$(cat "$PID_FILE")
  if kill -0 "$OLD_PID"; then
    echo "실행 중인 애플리케이션을 종료합니다. PID: $OLD_PID"
    kill "$OLD_PID"
    for i in {1..10}; do
      kill -0 "$OLD_PID" || break
      sleep 1
    done
    if kill -0 "$OLD_PID"; then
      echo "애플리케이션이 종료되지 않았습니다. 배포를 중단합니다."
      exit 1
    fi
  fi
fi

nohup java -jar "$JAR" > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"
echo "애플리케이션을 시작했습니다. PID: $(cat "$PID_FILE")"
