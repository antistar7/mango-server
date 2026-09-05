#!/bin/bash
# mango 계정으로 매일 돌려, 앱 로그 디렉터리에서 7일이 지난 파일을 지운다.

set -euo pipefail

LOG_DIR="${MANGO_LOG_DIR:-/home/mango/app/logs}"
KEEP_DAYS="${MANGO_LOG_KEEP_DAYS:-7}"

if [ ! -d "$LOG_DIR" ]; then
  exit 0
fi

find "$LOG_DIR" -type f -mtime +"$KEEP_DAYS" -delete
find "$LOG_DIR" -type f -name "*.gz" -mtime +"$KEEP_DAYS" -delete
