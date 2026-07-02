#!/usr/bin/env bash
# Starts BuildNest: infra (MySQL/Redis/Elasticsearch), backend, and frontend.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

cleanup() {
  echo
  echo "Shutting down..."
  [[ -n "${BACKEND_PID:-}" ]] && kill "$BACKEND_PID" 2>/dev/null || true
  [[ -n "${FRONTEND_PID:-}" ]] && kill "$FRONTEND_PID" 2>/dev/null || true
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
until [[ "$(cd "$BACKEND_DIR" && docker compose ps mysql --format '{{.Health}}')" == "healthy" ]]; do
  sleep 2
done
echo "MySQL is healthy."

echo "Starting backend (Spring Boot)..."
(cd "$BACKEND_DIR" && set -o allexport && source .env && set +o allexport && ./mvnw spring-boot:run) &
BACKEND_PID=$!

if [[ -d "$FRONTEND_DIR" && -f "$FRONTEND_DIR/package.json" ]]; then
  echo "Starting frontend (Vite dev server)..."
  (cd "$FRONTEND_DIR" && npm run dev) &
  FRONTEND_PID=$!
fi

echo
echo "BuildNest is starting up. Press Ctrl+C to stop everything."
wait
