# 서버 배포

Linux/EC2 서버에 Git, JDK 17, Bash, `flock`(util-linux)을 설치하고,
애플리케이션을 실행할 일반 사용자로 배포합니다. 기본 서버 포트는 8080입니다.
EC2 보안 그룹의 접근 허용 범위는 서비스에 맞게 설정하세요.

## 최초 실행

저장소의 `deploy.sh`를 서버에 복사한 뒤 아래 명령을 실행합니다.
스크립트는 저장소가 없으면 클론하고, PID·로그 디렉터리를 생성합니다.
파일은 실행 사용자만 접근할 수 있는 권한으로 생성됩니다.

```bash
export BRANCH=step3 # 배포할 실제 브랜치로 변경
export APP_DIR="$HOME/roomescape"
export RUN_DIR="$HOME/roomescape-run"
export PID_FILE="$RUN_DIR/application.pid"
bash deploy.sh
```

기본 저장소는 `https://github.com/mint0326/spring-basic-roomescape-playground.git`입니다.
다른 저장소는 `REPO_URL`로 지정하세요. 비공개 저장소는 Git 인증을 미리 설정해야 합니다.
Gradle 최초 실행에는 Gradle 배포본과 의존성을 다운로드할 네트워크 연결이 필요합니다.
스크립트와 실행 파일은 저장소 밖에 보관하세요.

## 재배포

최초 실행과 같은 사용자와 환경 변수로 다음 명령을 실행합니다.

```bash
bash "$APP_DIR/deploy.sh"
# 필요한 경우 Spring Boot 인자 전달
bash "$APP_DIR/deploy.sh" --server.port=8080
```

순서는 `git pull --ff-only` → `clean build`(테스트 포함) → 기존 PID에 SIGTERM 전달
→ 새 JAR 실행입니다. pull 또는 빌드 실패 시 기존 애플리케이션은 계속 실행됩니다.
커밋하지 않은 변경이나 브랜치 불일치가 있으면 중단합니다.
동시에 여러 배포를 실행하지 않도록 잠금을 사용합니다.

기존 프로세스의 종료를 기본 30초 동안 기다립니다(`STOP_TIMEOUT`으로 변경 가능).
종료되지 않으면 배포를 중단하고, 강제 종료 여부는 운영자가 판단합니다.
PID 파일은 이 스크립트가 시작한 프로세스를 추적합니다. 기존에 수동 실행한
애플리케이션은 첫 배포 전에 직접 종료해야 합니다.

로그 확인: `tail -f "$RUN_DIR/application.log"`.
`LOG_FILE`로 로그 경로를 변경할 수 있습니다. 경로 변수는 모두 절대 경로를 사용하고,
PID와 로그는 저장소 및 `build` 디렉터리 밖에 두세요.
기동 5초 후 프로세스 생존을 확인하며 HTTP 준비 완료를 보장하지 않습니다.
이 방식은 교체 중 서비스 중단이 있으며 자동 롤백·재부팅 후 자동 실행은 제공하지 않습니다.

현재 기본 DB 설정은 메모리 H2와 `create-drop`이므로 재배포하면 데이터가 사라집니다.
데이터를 유지할 운영 환경에서는 영속 DB와 스키마 정책을 별도로 설정해야 합니다.
운영 JWT 비밀키는 `ROOMESCAPE_AUTH_JWT_SECRET` 환경 변수로 전달하세요.
