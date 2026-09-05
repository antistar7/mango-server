#!/bin/bash
# journald / nginx / syslog 보관 기간을 7일로 맞춘다.
# /etc 변경이 있어 root 또는 sudo가 필요하다.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ "$(id -u)" -ne 0 ]; then
  echo "root로 실행하세요. 예: sudo $0"
  exit 1
fi

install -d -m 0755 /etc/systemd/journald.conf.d
install -m 0644 "$SCRIPT_DIR/journald.conf" \
  /etc/systemd/journald.conf.d/99-mango-retention.conf

install -m 0644 "$SCRIPT_DIR/nginx" /etc/logrotate.d/nginx
install -m 0644 "$SCRIPT_DIR/rsyslog" /etc/logrotate.d/rsyslog

systemctl restart systemd-journald
journalctl --vacuum-time=7d

logrotate -f /etc/logrotate.d/nginx || true
logrotate -f /etc/logrotate.d/rsyslog || true

echo "로그 보관 기간을 7일로 맞췄습니다."
