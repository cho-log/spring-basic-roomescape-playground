#!/usr/bin/env bash

set -Eeuo pipefail

APP_DIR="${HOME}/roomescape/repository"
DEPLOY_BRANCH="step3"
PID_FILE="${HOME}/roomescape/run/application.pid"
LOG_FILE="${HOME}/roomescape/logs/application.log"
DATA_DIR="${HOME}/roomescape/data"
SERVER_PORT="${SERVER_PORT:-8080}"
READINESS_URL="http://127.0.0.1:${SERVER_PORT}/actuator/health/readiness"
STARTUP_TIMEOUT=60

if ! command -v curl >/dev/null 2>&1; then
  echo "배포 상태 확인에 curl이 필요합니다. curl을 설치한 뒤 다시 실행해 주세요."
  exit 1
fi

PID_DIR="$(dirname "${PID_FILE}")"
LOG_DIR="$(dirname "${LOG_FILE}")"

if [[ ! -d "${PID_DIR}" || ! -d "${LOG_DIR}" || ! -d "${DATA_DIR}" ]]; then
  echo "배포 디렉터리가 없습니다. setup.sh를 먼저 실행해 주세요."
  exit 1
fi

if [[ ! -w "${DATA_DIR}" || ! -x "${DATA_DIR}" ]]; then
  echo "데이터 디렉터리에 접근하거나 기록할 수 없습니다: ${DATA_DIR}"
  exit 1
fi

# 저장소에서 최신 코드 pull
if [[ ! -d "${APP_DIR}/.git" ]]; then
  echo "Git 저장소를 찾을 수 없습니다: ${APP_DIR}"
  exit 1
fi

cd "${APP_DIR}"

CURRENT_BRANCH="$(git branch --show-current)"

if [[ "${CURRENT_BRANCH}" != "${DEPLOY_BRANCH}" ]]; then
  echo "배포 브랜치가 아닙니다. 현재 브랜치: ${CURRENT_BRANCH}"
  exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
  echo "저장소에 커밋되지 않은 변경 사항이 있어 배포를 중단합니다."
  exit 1
fi

git pull --ff-only origin "${DEPLOY_BRANCH}"

# 프로젝트 빌드
./gradlew clean build

JAR_FILE="$(find "${APP_DIR}/build/libs" \
  -maxdepth 1 \
  -type f \
  -name "*.jar" \
  ! -name "*-plain.jar" \
  -print \
  -quit)"

if [[ -z "${JAR_FILE}" ]]; then
  echo "실행할 JAR 파일을 찾을 수 없습니다."
  exit 1
fi

# 기존 애플리케이션 종료
if [[ -f "${PID_FILE}" ]]; then
  OLD_PID="$(cat "${PID_FILE}")"

  if [[ "${OLD_PID}" =~ ^[0-9]+$ ]] && kill -0 "${OLD_PID}" 2>/dev/null; then
    PROCESS_COMMAND="$(ps -p "${OLD_PID}" -o command= 2>/dev/null || true)"

    if [[ "${PROCESS_COMMAND}" != *"${APP_DIR}/build/libs/"* ]]; then
      echo "PID가 현재 애플리케이션이 아닌 다른 프로세스를 가리킵니다: ${OLD_PID}"
      exit 1
    fi

    echo "기존 애플리케이션을 종료합니다: ${OLD_PID}"
    kill "${OLD_PID}"

    for _ in {1..10}; do
      if ! kill -0 "${OLD_PID}" 2>/dev/null; then
        break
      fi

      sleep 1
    done

    if kill -0 "${OLD_PID}" 2>/dev/null; then
      echo "기존 애플리케이션이 종료되지 않았습니다."
      exit 1
    fi
  else
    echo "실행 중인 기존 애플리케이션이 없습니다."
  fi

  rm -f "${PID_FILE}"
fi

# 빌드된 애플리케이션 실행
export ROOMESCAPE_DB_PATH="${DATA_DIR}/database"
nohup java -jar "${JAR_FILE}" \
  --spring.profiles.active=prod \
  --server.port="${SERVER_PORT}" \
  >> "${LOG_FILE}" 2>&1 &

NEW_PID=$!
echo "${NEW_PID}" > "${PID_FILE}"

DEADLINE=$((SECONDS + STARTUP_TIMEOUT))

while (( SECONDS < DEADLINE )); do
  if ! kill -0 "${NEW_PID}" 2>/dev/null; then
    rm -f "${PID_FILE}"
    echo "애플리케이션이 준비되기 전에 종료됐습니다. ${LOG_FILE}"
    exit 1
  fi

  REMAINING=$((DEADLINE - SECONDS))
  REQUEST_TIMEOUT=$((REMAINING < 2 ? REMAINING : 2))
  if (( REQUEST_TIMEOUT <= 0 )); then
    break
  fi

  HTTP_STATUS="$(curl --silent --output /dev/null --write-out '%{http_code}' \
    --noproxy '*' --connect-timeout 1 --max-time "${REQUEST_TIMEOUT}" \
    "${READINESS_URL}" || true)"

  if [[ "${HTTP_STATUS}" == "200" ]] && kill -0 "${NEW_PID}" 2>/dev/null; then
    echo "애플리케이션 준비가 완료됐습니다: ${NEW_PID}"
    echo "로그 파일: ${LOG_FILE}"
    exit 0
  fi

  sleep 1
done

echo "${STARTUP_TIMEOUT}초 안에 애플리케이션이 준비되지 않아 배포 확인에 실패했습니다."
echo "PID: ${NEW_PID}, 로그 파일: ${LOG_FILE}"
exit 1
