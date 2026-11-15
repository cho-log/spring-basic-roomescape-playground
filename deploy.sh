set -euo pipefail

PORT=8080
PROFILE=prod
LOG_FILE=app.log

cd "$(dirname "$(readlink -f "$0")")"

sudo apt-get update
sudo apt-get upgrade -y

java_major() {
  command -v java >/dev/null 2>&1 || return 0
  java -version 2>&1 | awk -F'"' '/version/ {split($2, v, "."); print v[1]}'
}

if [ "$(java_major)" = "17" ]; then
else
  sudo apt-get install -y openjdk-17-jdk-headless
  export PATH="/usr/lib/jvm/java-17-openjdk-$(dpkg --print-architecture)/bin:$PATH"
fi
JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
export JAVA_HOME
java -version

chmod +x gradlew
./gradlew clean test bootJar --no-daemon

JAR=$(find build/libs -maxdepth 1 -name '*-SNAPSHOT.jar' ! -name '*-plain.jar' -print -quit)
if [ -z "$JAR" ]; then
  exit 1
fi

if "$JAVA_HOME/bin/jar" tf "$JAR" | grep '^BOOT-INF/lib/h2-' >/dev/null;
else
  exit 1
fi

port_pids() {
  sudo ss -Hltnp "sport = :$PORT" | grep -oE 'pid=[0-9]+' | cut -d= -f2 | sort -u || true
}

is_running() {
  for pid in "$@"; do
    ps -p "$pid" >/dev/null 2>&1 && return 0
  done
  return 1
}

PIDS=$(port_pids)
if [ -n "$PIDS" ]; then
  sudo kill -TERM $PIDS || true

  sleep 1

  if is_running $PIDS; then
    sudo kill -KILL $PIDS || true
    sleep 1
  fi
fi

if [ -n "$(port_pids)" ];
  echo "애플리케이션 배포 문제 발생. 배포 중단"
  exit 1
fi

nohup "$JAVA_HOME/bin/java" -jar "$JAR" --spring.profiles.active="$PROFILE" > "$LOG_FILE" 2>&1 &
APP_PID=$!
