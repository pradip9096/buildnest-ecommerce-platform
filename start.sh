#!/usr/bin/env bash
# Starts BuildNest: infra (MySQL/Redis/Elasticsearch), backend, and frontend.
set -euo pipefail
set -m  # enable job control so each backgrounded subshell gets its own process
        # group — required for cleanup()'s `kill -- -$PID` to reach mvnw's/npm's
        # actual child process (java, node), not just the wrapper subshell

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

CLEANED_UP=0
cleanup() {
  [[ "$CLEANED_UP" -eq 1 ]] && return
  CLEANED_UP=1
  echo
  echo "Shutting down..."
  # Negative PID targets the whole process group, so mvnw's/npm's actual child
  # process (java, node) is killed too — killing only the wrapper subshell PID
  # leaves those orphaned and running.
  [[ -n "${BACKEND_PID:-}" ]] && kill -- "-${BACKEND_PID}" 2>/dev/null || true
  [[ -n "${FRONTEND_PID:-}" ]] && kill -- "-${FRONTEND_PID}" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

if [[ ! -f "$BACKEND_DIR/.env" ]]; then
  echo "backend/.env not found — copying from .env.example"
  cp "$BACKEND_DIR/.env.example" "$BACKEND_DIR/.env"
  echo "Review backend/.env before continuing if this is your first run."
fi

echo "Starting infrastructure (MySQL, Redis, Elasticsearch)..."
(cd "$BACKEND_DIR" && docker compose up -d mysql redis elasticsearch)

echo "Waiting for MySQL to be healthy..."
MYSQL_WAIT_ELAPSED=0
MYSQL_WAIT_TIMEOUT="${MYSQL_WAIT_TIMEOUT:-120}"
until [[ "$(cd "$BACKEND_DIR" && docker compose ps mysql --format '{{.Health}}')" == "healthy" ]]; do
  if [[ "$MYSQL_WAIT_ELAPSED" -ge "$MYSQL_WAIT_TIMEOUT" ]]; then
    echo "MySQL did not become healthy within ${MYSQL_WAIT_TIMEOUT}s."
    echo "Check backend/.env for unfilled placeholder values and run:"
    echo "  (cd \"$BACKEND_DIR\" && docker compose logs mysql)"
    exit 1
  fi
  sleep 2
  MYSQL_WAIT_ELAPSED=$((MYSQL_WAIT_ELAPSED + 2))
done
echo "MySQL is healthy."

echo "Starting backend (Spring Boot)..."
(trap - EXIT INT TERM; cd "$BACKEND_DIR" && set -o allexport && source .env && set +o allexport && ./mvnw spring-boot:run) &
BACKEND_PID=$!

echo "Waiting for backend to become healthy..."
BACKEND_PORT="${BACKEND_PORT:-8080}"
BACKEND_WAIT_ELAPSED=0
BACKEND_WAIT_TIMEOUT="${BACKEND_WAIT_TIMEOUT:-180}"
# Use the readiness group, not the aggregate /actuator/health — the aggregate
# includes third-party indicators (mail, elasticsearch) that commonly stay DOWN
# in local dev (no SMTP password, ES auth not configured) without the app
# itself being unready to serve requests.
until curl -sf "http://localhost:${BACKEND_PORT}/actuator/health/readiness" >/dev/null 2>&1; do
  if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
    echo "Backend process exited before becoming healthy — check the Spring Boot log output above."
    exit 1
  fi
  if [[ "$BACKEND_WAIT_ELAPSED" -ge "$BACKEND_WAIT_TIMEOUT" ]]; then
    echo "Backend did not become healthy within ${BACKEND_WAIT_TIMEOUT}s."
    exit 1
  fi
  sleep 2
  BACKEND_WAIT_ELAPSED=$((BACKEND_WAIT_ELAPSED + 2))
done
echo "Backend is healthy."

if [[ -d "$FRONTEND_DIR" && -f "$FRONTEND_DIR/package.json" ]]; then
  echo "Starting frontend (Vite dev server)..."
  (trap - EXIT INT TERM; cd "$FRONTEND_DIR" && npm run dev) &
  FRONTEND_PID=$!
fi

echo
echo "BuildNest is starting up. Press Ctrl+C to stop everything."
wait
