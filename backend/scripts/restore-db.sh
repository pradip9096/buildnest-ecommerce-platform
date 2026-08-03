#!/bin/bash
# restore-db.sh — Restore a BuildNest MySQL backup (#121, OPS-03, SRS NFR-AVL-01)
#
# Restores a .sql.gz backup produced by backup-db.sh into the running
# `buildnest-mysql` Docker container. Destructive — overwrites the target database.
#
# Usage:
#   ./restore-db.sh <path-to-backup.sql.gz>
#   ./restore-db.sh --latest        # restore the most recent backup in BACKUP_DIR
#
# Required environment (loaded from backend/.env, matching docker-compose.yml):
#   MYSQL_ROOT_PASSWORD, MYSQL_DATABASE

set -euo pipefail

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
LOG_PREFIX="[restore-db $(date -u +%Y-%m-%dT%H:%M:%SZ)]"

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD must be set (via backend/.env or the environment)}"
: "${MYSQL_DATABASE:?MYSQL_DATABASE must be set (via backend/.env or the environment)}"

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 <path-to-backup.sql.gz> | --latest" >&2
    exit 1
fi

if [[ "$1" == "--latest" ]]; then
    BACKUP_FILE="$(find "${BACKUP_DIR}" -maxdepth 1 -name "${MYSQL_DATABASE}_*.sql.gz" -printf '%T@ %p\n' 2>/dev/null | sort -rn | head -n1 | cut -d' ' -f2-)"
    if [[ -z "${BACKUP_FILE}" ]]; then
        echo "${LOG_PREFIX} ERROR: no backups found in ${BACKUP_DIR}" >&2
        exit 1
    fi
else
    BACKUP_FILE="$1"
fi

if [[ ! -f "${BACKUP_FILE}" ]]; then
    echo "${LOG_PREFIX} ERROR: backup file not found: ${BACKUP_FILE}" >&2
    exit 1
fi

if [[ "${BACKUP_FILE}" != *.sql.gz ]]; then
    echo "${LOG_PREFIX} ERROR: refusing to restore a non-.sql.gz file: ${BACKUP_FILE}" >&2
    exit 1
fi

echo "${LOG_PREFIX} Restoring '${MYSQL_DATABASE}' into container '${MYSQL_CONTAINER}' from ${BACKUP_FILE}"
echo "${LOG_PREFIX} WARNING: this overwrites the current contents of '${MYSQL_DATABASE}'"

if [[ -t 0 && "${RESTORE_YES:-}" != "1" ]]; then
    read -r -p "Type the database name (${MYSQL_DATABASE}) to confirm this destructive restore: " CONFIRM
    if [[ "${CONFIRM}" != "${MYSQL_DATABASE}" ]]; then
        echo "${LOG_PREFIX} Confirmation did not match — aborting" >&2
        exit 1
    fi
fi

START_TIME=$(date +%s)

if ! gunzip -c "${BACKUP_FILE}" | docker exec -i -e MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" "${MYSQL_CONTAINER}" \
    mysql -u root; then
    echo "${LOG_PREFIX} ERROR: restore failed" >&2
    exit 1
fi

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo "${LOG_PREFIX} Restore complete in ${ELAPSED}s"
