#!/bin/bash

set -e

REMOTE="mango-server"
REMOTE_TMP="/home/mango/app/mango-server.jar.tmp"
REMOTE_APP="/home/mango/app/mango-server.jar"

# Spring Profile
PROFILE="${1:-prod}"

case "$PROFILE" in
  prod|dev)
    ;;
  *)
    echo "사용법: ./deploy.sh [prod|dev]"
    exit 1
    ;;
esac

echo "===> Profile: $PROFILE"

echo "==> Build"
./mvnw clean package -DskipTests

JAR=$(ls -t target/*.jar | head -1)

echo "==> JAR: $JAR"

echo "==> Uploading..."
scp "$JAR" "$REMOTE:$REMOTE_TMP"

echo "==> Installing..."
ssh "$REMOTE" "
    mv '$REMOTE_TMP' '$REMOTE_APP'
    chown mango:mango '$REMOTE_APP'
    mkdir -p /home/mango/app/logs
"

echo "==> Deploy complete"
ssh "$REMOTE" "ls -lh '$REMOTE_APP'"

# 프로파일과 자격 증명은 systemd가 읽는 /etc/mango/mango.env에 있다.
# 예전에는 여기서 /home/mango/app/mango-server.env에 프로파일을 기록했지만
# 그 파일은 유닛의 EnvironmentFile이 아니라 아무 효과가 없었다.
# 값이 비어 있으면 application.properties의 spring.profiles.default=prod가 쓰인다.
if [ "$PROFILE" != "prod" ]; then
  echo "!!! $PROFILE 프로파일로 띄우려면 서버의 /etc/mango/mango.env에"
  echo "!!! SPRING_PROFILES_ACTIVE=$PROFILE 를 직접 넣어야 한다."
fi

echo "===> Restarting server..."

ssh -t "$REMOTE" "sudo systemctl restart mango-server"

# 관리자 자격 증명(ADMIN_USERNAME/ADMIN_PASSWORD)이 없으면 기동에 실패한다.
# 배포가 조용히 사이트를 내리지 않도록 여기서 확인한다.
echo "===> Verifying..."

ssh "$REMOTE" '
    for i in $(seq 1 30); do
        curl -sf -o /dev/null http://127.0.0.1:8080/api/v1/cities && break
        sleep 2
    done

    if ! curl -sf -o /dev/null http://127.0.0.1:8080/api/v1/cities; then
        echo "!!! 기동 실패. 로그: sudo journalctl -u mango-server -n 50"
        exit 1
    fi

    ADMIN_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        http://127.0.0.1:8080/api/v1/admin/cities)

    if [ "$ADMIN_CODE" != "401" ]; then
        echo "!!! 관리자 API가 인증 없이 $ADMIN_CODE 를 반환한다. 인증 설정을 확인하라."
        exit 1
    fi

    echo "    공개 API 200, 관리자 API 401 — 정상"
'

echo "===> Deploy complete: $PROFILE"
