#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

# Linux/EC2: Git, JDK 17, flock(util-linux)가 필요합니다.
REPO_URL="${REPO_URL:-https://github.com/mint0326/spring-basic-roomescape-playground.git}"
BRANCH="${BRANCH:-step3}"
APP_DIR="${APP_DIR:-$HOME/roomescape}"
RUN_DIR="${RUN_DIR:-$HOME/roomescape-run}"
PID_FILE="${PID_FILE:-$RUN_DIR/application.pid}"
LOG_FILE="${LOG_FILE:-$RUN_DIR/application.log}"
STOP_TIMEOUT="${STOP_TIMEOUT:-30}"

fail() { echo "배포 실패: $*" >&2; exit 1; }
for command in git java flock; do
    command -v "$command" >/dev/null || fail "$command 명령이 필요합니다."
done
[[ "$APP_DIR" = /* && "$RUN_DIR" = /* && "$PID_FILE" = /* && "$LOG_FILE" = /* ]] \
    || fail "APP_DIR, RUN_DIR, PID_FILE, LOG_FILE은 절대 경로여야 합니다."
[[ "$STOP_TIMEOUT" =~ ^[1-9][0-9]*$ ]] || fail "STOP_TIMEOUT은 양의 정수여야 합니다."

mkdir -p "$RUN_DIR" "$(dirname "$PID_FILE")" "$(dirname "$LOG_FILE")"
[[ -w "$(dirname "$PID_FILE")" ]] || fail "PID 파일 디렉터리에 쓰기 권한이 없습니다."
[[ ! -e "$PID_FILE" || -w "$PID_FILE" ]] || fail "PID 파일에 쓰기 권한이 없습니다."
touch "$LOG_FILE"
exec 9>"$RUN_DIR/deploy.lock"
flock -n 9 || fail "다른 배포가 실행 중입니다."

if [[ ! -d "$APP_DIR/.git" ]]; then
    [[ ! -e "$APP_DIR" ]] || fail "$APP_DIR 경로가 이미 존재하지만 Git 저장소가 아닙니다."
    mkdir -p "$(dirname "$APP_DIR")"
    git clone --branch "$BRANCH" --single-branch "$REPO_URL" "$APP_DIR"
fi
cd "$APP_DIR"
[[ "$(git branch --show-current)" = "$BRANCH" ]] || fail "저장소의 현재 브랜치가 $BRANCH 가 아닙니다."
[[ -z "$(git status --porcelain)" ]] || fail "저장소에 커밋하지 않은 변경이 있습니다."
git pull --ff-only origin "$BRANCH"

echo "프로젝트를 빌드합니다."
bash ./gradlew clean build
shopt -s nullglob
jars=()
for jar in "$APP_DIR"/build/libs/*.jar; do
    [[ "$jar" = *-plain.jar ]] || jars+=("$jar")
done
[[ ${#jars[@]} -eq 1 ]] || fail "실행 가능한 JAR을 하나만 찾을 수 있어야 합니다."
APP_JAR="$RUN_DIR/application.jar"
cp "${jars[0]}" "$APP_JAR.next"
trap 'rm -f "$APP_JAR.next"' EXIT

if [[ -s "$PID_FILE" ]]; then
    pid=$(cat "$PID_FILE")
    [[ "$pid" =~ ^[1-9][0-9]*$ ]] || fail "PID 파일 내용이 올바르지 않습니다."
    if kill -0 "$pid" 2>/dev/null; then
        # 오래된 PID가 다른 프로세스에 재사용되었다면 종료하지 않습니다.
        args=$(ps -p "$pid" -o args=) || fail "PID $pid 프로세스를 확인할 수 없습니다."
        [[ "$args" = *"-jar $APP_JAR"* ]] || fail "PID $pid 는 이 애플리케이션의 프로세스가 아닙니다."
        echo "기존 애플리케이션(PID $pid)을 종료합니다."
        kill "$pid"
        for ((i = 0; i < STOP_TIMEOUT; i++)); do
            kill -0 "$pid" 2>/dev/null || break
            sleep 1
        done
        if kill -0 "$pid" 2>/dev/null; then
            fail "종료 대기 시간을 초과했습니다. 기존 프로세스를 확인하세요."
        fi
    fi
fi
rm -f "$PID_FILE"
mv "$APP_JAR.next" "$APP_JAR"

echo "새 애플리케이션을 실행합니다."
# 전달한 인자는 Spring Boot 실행 인자로 사용됩니다. JVM 설정은 JAVA_TOOL_OPTIONS로 전달합니다.
nohup java -jar "$APP_JAR" "$@" >>"$LOG_FILE" 2>&1 </dev/null 9>&- &
pid=$!
printf '%s\n' "$pid" >"$PID_FILE"
sleep 5
if ! kill -0 "$pid" 2>/dev/null; then
    rm -f "$PID_FILE"
    fail "애플리케이션이 종료되었습니다. 로그를 확인하세요: $LOG_FILE"
fi
echo "애플리케이션 실행 중: PID $pid / 로그: $LOG_FILE"
