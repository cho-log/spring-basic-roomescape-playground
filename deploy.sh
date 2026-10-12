#!/bin/bash

# 스크립트 내부 변수, 자주 쓰는 경로와 이름을 모아둔 것
APP_DIR="$HOME/app"
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$APP_DIR/app.log"
BRANCH="step8"

main() {
    set -e
    cd "$APP_DIR"

    # 1. 저장소에서 최신 코드를 pull
    git pull origin "$BRANCH"

    # 2. 프로젝트 빌드
    ./gradlew clean build -x test

    # 3. 기존에 실행 중인 애플리케이션의 PID를 찾아 종료
    if [ -s "$PID_FILE" ]; then
        OLD_PID=$(cat "$PID_FILE")
        if kill -0 "$OLD_PID" 2>/dev/null; then
            echo "기존 프로세스 종료: $OLD_PID"
            kill "$OLD_PID"
            for i in $(seq 1 10); do
                kill -0 "$OLD_PID" 2>/dev/null || break
                sleep 1
            done
            if kill -0 "$OLD_PID" 2>/dev/null; then
                kill -9 "$OLD_PID"
            fi
        fi
    fi

    # 4. 빌드된 애플리케이션 실행
    JAR=$(ls build/libs/*.jar | grep -v plain | head -n 1)
    nohup java -jar "$JAR" > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    echo "배포 완료. PID: $(cat "$PID_FILE")"
}

main "$@"; exit $?
