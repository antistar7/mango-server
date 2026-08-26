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
"

echo "==> Deploy complete"
ssh "$REMOTE" "ls -lh '$REMOTE_APP'"

echo "===> Setting Spring Profile: $PROFILE"

ssh "$REMOTE" "echo 'SPRING_PROFILES_ACTIVE=$PROFILE' > /home/mango/app/mango-server.env"

echo "===> Restarting server..."

ssh -t "$REMOTE" "sudo systemctl restart mango-server"

echo "===> Deploy complete: $PROFILE"
