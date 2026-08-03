#!/bin/bash
# backup-db.sh — Automated MySQL backup for BuildNest (#121, OPS-03, SRS NFR-AVL-01)
#
# Dumps the buildnest_ecommerce database from the running `buildnest-mysql` Docker
# container, compresses it with a timestamped filename, and prunes backups older
# than the retention window. Intended to run daily via cron at 02:00 UTC.
#
# Usage:
#   ./backup-db.sh
#
# Required environment (loaded from backend/.env, matching docker-compose.yml):
#   MYSQL_ROOT_PASSWORD, MYSQL_DATABASE
#
# Optional overrides:
#   BACKUP_DIR       — where .sql.gz files are stored (default: backend/backups)
#   RETENTION_DAYS   — days to keep backups before deletion (default: 30)
#   MYSQL_CONTAINER   — Docker container name (default: buildnest-mysql)

set -euo pipefail
umask 077   # backups contain full customer PII (users, orders, payments) — never world-readable

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${BACKEND_DIR}/.env"

if [[ -f "${ENV_FILE}" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "${ENV_FILE}"
    set +a
fi

MYSQL_CONTAINER="${MYSQL_CONTAINER:-buildnest-mysql}"
BACKUP_DIR="${BACKUP_DIR:-${BACKEND_DIR}/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD must be set (via backend/.env or the environment)}"
: "${MYSQL_DATABASE:?MYSQL_DATABASE must be set (via backend/.env or the environment)}"

TIMESTAMP="$(date -u +%Y%m%d_%H%M%S)"
BACKUP_FILE="${BACKUP_DIR}/${MYSQL_DATABASE}_${TIMESTAMP}.sql.gz"
LOG_PREFIX="[backup-db $(date -u +%Y-%m-%dT%H:%M:%SZ)]"

mkdir -p "${BACKUP_DIR}"

echo "${LOG_PREFIX} Starting backup of '${MYSQL_DATABASE}' from container '${MYSQL_CONTAINER}'"

if ! docker exec -e MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" "${MYSQL_CONTAINER}" \
    mysqldump \
        -u root \
        --single-transaction \
        --routines \
        --triggers \
        --databases "${MYSQL_DATABASE}" \
    | gzip > "${BACKUP_FILE}"; then
    echo "${LOG_PREFIX} ERROR: mysqldump failed — removing partial backup file" >&2
    rm -f "${BACKUP_FILE}"
    exit 1
fi

if [[ ! -s "${BACKUP_FILE}" ]]; then
    echo "${LOG_PREFIX} ERROR: backup file is empty — treating as failure" >&2
    rm -f "${BACKUP_FILE}"
    exit 1
fi

BACKUP_SIZE="$(du -h "${BACKUP_FILE}" | cut -f1)"
echo "${LOG_PREFIX} Backup complete: ${BACKUP_FILE} (${BACKUP_SIZE})"

echo "${LOG_PREFIX} Pruning backups older than ${RETENTION_DAYS} days"
DELETED_COUNT=0
while IFS= read -r -d '' old_backup; do
    rm -f "${old_backup}"
    echo "${LOG_PREFIX} Deleted expired backup: ${old_backup}"
    DELETED_COUNT=$((DELETED_COUNT + 1))
done < <(find "${BACKUP_DIR}" -maxdepth 1 -name "${MYSQL_DATABASE}_*.sql.gz" -mtime "+${RETENTION_DAYS}" -print0)

echo "${LOG_PREFIX} Retention sweep complete: ${DELETED_COUNT} expired backup(s) removed"
echo "${LOG_PREFIX} Done"
