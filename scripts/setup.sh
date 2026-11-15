#!/usr/bin/env bash

set -Eeuo pipefail

# Git 저장소 존재 여부 확인 및 클론
REPOSITORY_URL="https://github.com/oroi2009/spring-basic-roomescape-playground.git"
DEPLOY_BRANCH="step3"
APP_DIR="${HOME}/roomescape/repository"

if [[ -d "${APP_DIR}/.git" ]]; then
  echo "이미 Git 저장소가 존재합니다."
elif [[ -e "${APP_DIR}" ]]; then
  echo "Git 저장소가 아닙니다."
  exit 1
else
  mkdir -p "$(dirname "${APP_DIR}")"
  git clone \
      --branch "${DEPLOY_BRANCH}" \
      --single-branch \
      "${REPOSITORY_URL}" \
      "${APP_DIR}"
fi

# PID 파일 경로 설정 확인
PID_FILE="${HOME}/roomescape/run/application.pid"
PID_DIR="$(dirname "${PID_FILE}")"

if [[ -e "${PID_FILE}" && ! -f "${PID_FILE}" ]]; then
  echo "PID 파일 경로를 사용할 수 없습니다."
  exit 1
fi

echo "PID 파일 경로: ${PID_FILE}"

# 필요한 디렉토리 및 파일 권한 설정
LOG_DIR="${HOME}/roomescape/logs"
DATA_DIR="${HOME}/roomescape/data"

if ! mkdir -p "${PID_DIR}" "${LOG_DIR}" "${DATA_DIR}"; then
  echo "필요한 디렉터리를 생성하지 못했습니다."
  exit 1
fi

chmod u+rwx "${PID_DIR}" "${LOG_DIR}"
chmod 700 "${DATA_DIR}"
chmod u+x "${APP_DIR}/gradlew"
chmod u+x "${APP_DIR}/scripts/setup.sh"
chmod u+x "${APP_DIR}/scripts/deploy.sh"

echo "디렉터리와 실행 권한 설정을 완료했습니다."
