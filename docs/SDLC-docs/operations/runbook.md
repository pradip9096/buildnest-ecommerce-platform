# BuildNest Production Runbook

## Document Control

| Field | Value |
| :--- | :--- |
| Document | Production Operations Runbook |
| Version | 1.0 |
| Status | Draft — pending staging dry-run validation (see [Validation Status](#validation-status)) |
| Related SDP | [Software Development Plan](../project-planning/software-development-plan.md) |
| Related RTM | [Requirements Traceability Matrix](../requirement-engineering/requirements-traceability-matrix.md) — no dedicated OPS-08 row exists (this is a `type: docs` issue with no new specified system behavior; documents existing OBS-05/REL-05 behavior instead — same no-dedicated-OPS-row pattern as RTM revisions 1.48/1.49) |
| Related SRS | [Software Requirements Specification](../requirement-engineering/software-requirements-specification.md) — OBS-05 (health probes, documented in §2), REL-05 (backup/restore, documented in §3) |

### Revision History

| Version | Date | Author | Description |
| :--- | :--- | :--- | :--- |
| 1.0 | 2026-08-09 | Project Manager | Initial runbook for #126 (OPS-08): startup/shutdown, health checks, backup/restore, log access, alert response, rollback |

> **Note on this issue's own reference**: #126 cites "SDP §12 (Operational Readiness)" — SDP §12 is actually "Configuration Management Plan"; no section titled "Operational Readiness" exists in the SDP. This runbook instead draws its operational facts directly from the running configuration (`docker-compose.yml`, `kubernetes/`, `application*.properties`) rather than a stale document citation.

---

## Scope

This runbook covers the procedures an on-call engineer needs for BuildNest's real infrastructure: the local Docker Compose stack (`backend/docker-compose.yml`) for development, and the SSH + Docker Compose + GHCR mechanism (`docker-compose.prod.yml`, `.github/workflows/deploy.yml`, [ADR-0003](../design/adr/0003-ssh-docker-compose-plus-ghcr-as-the-deployment-mechanism.md)) that actually deploys `staging`/`production`. The `backend/kubernetes/` manifests are **not** the active deployment mechanism — they predate ADR-0003 and were superseded before ever being used; sections referencing them are marked accordingly and retained only for completeness. Procedures against the real `staging`/`production` GitHub Environments have not been dry-run this session (no SSH access available) — see [Validation Status](#validation-status).

---

## 1. Application Startup

### 1.1 Local / Staging (Docker Compose)

**Prerequisites:**
- `backend/.env` populated from `backend/.env.example`
- Docker and Docker Compose installed

**Steps:**
```bash
cd backend
cp .env.example .env   # first time only — then fill in real values
docker compose up -d mysql redis elasticsearch
# wait for healthchecks (each service has one — see §2.2)
./mvnw spring-boot:run
```

**Expected output:** Spring Boot startup log ending in `Started BuildnestEcommerceApplication in N.NNN seconds`. `SecurityConfig.validateHttpsInProduction()` runs a `@PostConstruct` fail-fast check — if `production` profile is active without SSL configured, startup aborts immediately with a clear error rather than starting insecurely.

**Troubleshooting:**
| Symptom | Cause | Fix |
| :--- | :--- | :--- |
| `Schema-validation: missing table/column` | Liquibase changelog not applied, or `ddl-auto` mismatch | Confirm `spring.jpa.hibernate.ddl-auto=validate` and that Liquibase ran (`db.changelog-master.xml` includes are current) — never switch to `update` |
| Startup hangs on MySQL/Redis/ES connection | Dependency container not healthy yet | `docker compose ps` — wait for `healthy`, not just `Up` |
| `@PostConstruct` HTTPS validation failure | `production` profile active, no keystore configured | Set `server.ssl.enabled=true` with a valid keystore path/password, or don't activate `production` locally |

### 1.2 Staging / Production (SSH + Docker Compose + GHCR — the real deployment mechanism)

Per [ADR-0003](../design/adr/0003-ssh-docker-compose-plus-ghcr-as-the-deployment-mechanism.md): this repo has no Kubernetes cluster or cloud account, so `.github/workflows/deploy.yml` (#120, OPS-02) deploys by SSH-ing into the target host and running Docker Compose against pre-built GHCR images — **not** the `backend/kubernetes/` manifests (those predate ADR-0003 and are not wired into any real deployment path; see §1.3 for their status).

**Automatic (normal path):** a push to `staging` or a `v*` tag push to `main` triggers `deploy.yml`, which builds+pushes both images to `ghcr.io` tagged with the commit SHA, exports that as `IMAGE_TAG`, then over SSH runs:
```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod pull backend frontend
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --no-deps backend frontend
```
This is a rolling per-service restart, not a full-stack restart — MySQL/Redis/Elasticsearch/`nginx-proxy` are untouched (`--no-deps`), so the proxy keeps serving through the brief backend/frontend restart window. Production requires the `production` GitHub Environment's required-reviewer approval gate before the SSH step runs.

**Manual (on the target host, if CI is unavailable):**
```bash
cd /path/to/buildnest   # wherever docker-compose.prod.yml + .env.prod live on the host
echo "$GHCR_PAT" | docker login ghcr.io -u <user> --password-stdin   # PAT via stdin, never a CLI arg
docker compose -f docker-compose.prod.yml --env-file .env.prod pull backend frontend
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --no-deps backend frontend
```

**Expected output:** `deploy.yml`'s own health-check step (immediately after the restart) curls the readiness endpoint (§2.1) through `nginx-proxy` — a workflow failure at that step means the new containers came up but aren't passing health checks; don't consider the deploy complete until that step is green.

### 1.3 Kubernetes Manifests (`backend/kubernetes/`) — not the active deployment mechanism

These manifests exist in the repo but, per ADR-0003, were superseded before ever being used for a real deployment — there is no Kubernetes cluster this project deploys to. **Known defect if ever revived:** the `livenessProbe`/`readinessProbe`/`startupProbe` in `buildnest-deployment.yaml` target container port `8081` (named `management`), but neither `application.properties` nor `application-production.properties` sets `management.server.port=8081` — actuator actually serves on `server.port` (`8080`). All three probes would fail with connection-refused if this manifest were ever applied. Tracked as follow-up #708 — do not apply this manifest without resolving that first.

---

## 2. Health Check Validation

### 2.1 Application Health Endpoints

Spring Boot Actuator is wired with real dependency checks (`management.endpoint.health.probes.enabled=true`, `application.properties:203-222`):

| Endpoint | Purpose | Checks |
| :--- | :--- | :--- |
| `GET /actuator/health/liveness` | Is the JVM/app context alive? | `livenessState` only — never blocks on external dependencies |
| `GET /actuator/health/readiness` | Can this instance serve traffic? | `readinessState`, MySQL (`db`), Redis — configurable via `HEALTH_READINESS_GROUP` env var |
| `GET /actuator/health` | Full detail (dev only — `show-details=always` locally, `when-authorized` in production) | All registered indicators: db, redis, circuit breakers, disk space |

**Validation command (local):**
```bash
curl -s http://localhost:8080/actuator/health/readiness | python3 -m json.tool
```
**Expected output:** `{"status": "UP", "components": {"db": {"status": "UP"}, "redis": {"status": "UP"}, "readinessState": {"status": "UP"}}}`. A `DOWN` component means that specific dependency is unreachable — see §2.2 for the corresponding infrastructure check.

**Troubleshooting:** if `/actuator/health/liveness` is `UP` but `/actuator/health/readiness` is `DOWN`, the JVM is healthy but a dependency isn't — check §2.2 before restarting the app (a restart won't fix a downstream outage).

### 2.2 Infrastructure Healthchecks (Docker Compose)

```bash
docker compose ps
```
Each service (`elasticsearch`, `mysql`, `redis`) has its own `healthcheck` block (`docker-compose.yml`):

| Service | Check | Interval |
| :--- | :--- | :--- |
| MySQL | `mysqladmin ping -h localhost` | 10s, 5 retries |
| Redis | `redis-cli ping` | 10s, 5 retries |
| Elasticsearch | `curl -s http://elastic:$ELASTIC_PASSWORD@localhost:9200` | 10s, 5 retries |

**Troubleshooting:** a container stuck `starting` past 5 retries (~50s) means the healthcheck command itself is failing — `docker compose logs <service>` for the actual startup error, not just the healthcheck status.

### 2.3 Actuator Prometheus Endpoint (Monitoring Credential)

`GET /actuator/prometheus` is exposed via a dedicated `@Order(0)` security chain (`actuatorMonitoringSecurityFilterChain`, `SecurityConfig.java`) with its own Basic Auth credential (`monitoring` / `${monitoring.password}`) — deliberately isolated from real user accounts (see `spring-security.md`). Never use an admin user's credentials to scrape this endpoint.

```bash
curl -u monitoring:$MONITORING_PASSWORD http://localhost:8080/actuator/prometheus | head
```

---

## 3. Database Backup and Restore

Real, existing tooling — `backend/scripts/backup-db.sh` / `restore-db.sh` (#121, OPS-03, REL-05) — not a manual `mysqldump` procedure. Daily `mysqldump --single-transaction --routines --triggers`, gzip-compressed, 30-day retention (cron-scheduled at 02:00 UTC), umask `077` (backups contain full customer PII — users, orders, payments — and must never be world-readable).

### 3.1 Backup

**Prerequisites:** `backend/.env` populated (`MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`); `buildnest-mysql` container healthy (§2.2).

**Steps:**
```bash
cd backend
./scripts/backup-db.sh
# optional overrides: BACKUP_DIR, RETENTION_DAYS, MYSQL_CONTAINER
```

**Expected output:** `[backup-db ...] Backup complete: <path>/<db>_<timestamp>.sql.gz (<size>)`, followed by a retention-sweep line reporting how many expired backups were deleted. The script itself fails loudly and removes any partial file on a `mysqldump` error or an unexpectedly empty dump (`set -euo pipefail`) — it does not silently produce a corrupt backup.

**Troubleshooting:**
| Symptom | Cause | Fix |
| :--- | :--- | :--- |
| `MYSQL_ROOT_PASSWORD must be set` | `backend/.env` missing or not sourced | Confirm `backend/.env` exists and matches `docker-compose.yml`'s values |
| `mysqldump failed` | Container unreachable or credentials wrong | `docker compose ps` — confirm `buildnest-mysql` is `healthy`, then re-check `MYSQL_ROOT_PASSWORD` |

**Known gap (REL-05, RTM 🟢 partial):** daily-only cadence gives an actual RPO of up to ~24h, not the SRS-required ≤5 min — point-in-time recovery (binlog/replication) is tracked separately (see RTM REL-05's follow-up), not part of this script's current scope.

### 3.2 Restore

**Destructive — overwrites the target database.** The script itself guards this: outside a non-interactive/CI context it requires typing the database name to confirm; set `RESTORE_YES=1` only for an already-confirmed, scripted DR drill.

**Steps:**
```bash
cd backend
./scripts/restore-db.sh --latest                      # most recent backup in BACKUP_DIR
# or: ./scripts/restore-db.sh backups/buildnest_ecommerce_20260809_020000.sql.gz
```

**Expected output:** `[restore-db ...] Restore complete in <N>s` — the live DR drill referenced in RTM REL-05 measured ~30s, well under the SRS's 15-minute RTO target. Verify via a known-row-count query afterward (e.g. `docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" buildnest-mysql mysql -u root -e "SELECT COUNT(*) FROM users;" "$MYSQL_DATABASE"`).

**Troubleshooting:**
| Symptom | Cause | Fix |
| :--- | :--- | :--- |
| `refusing to restore a non-.sql.gz file` | Wrong file passed, or an uncompressed `.sql` | The script only accepts its own `backup-db.sh` output format — gzip it first if restoring an ad hoc dump |
| Confirmation prompt loops/fails in CI | Running non-interactively without `RESTORE_YES=1` | Set `RESTORE_YES=1` explicitly for scripted DR drills — never as a default |

---

## 4. Log Retrieval

### 4.1 Application Logs (Local File)

`logging.file.name=logs/buildnest-ecommerce.log` (`application.properties:87`), structured JSON (`logging.pattern.console`).

```bash
tail -f backend/logs/buildnest-ecommerce.log | python3 -m json.tool 2>/dev/null
```

### 4.2 Staging / Production (SSH + Docker Compose)

```bash
# On the deploy host, in the docker-compose.prod.yml directory:
docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f backend
docker compose -f docker-compose.prod.yml --env-file .env.prod logs --tail=200 backend   # after a restart
```

### 4.3 Kubernetes Pod Logs — not the active deployment target

```bash
kubectl logs -f deployment/buildnest-app -n buildnest --all-containers
kubectl logs deployment/buildnest-app -n buildnest --previous   # after a crash/restart
```
Per §1.3, no live cluster exists — retained for completeness only.

### 4.4 Centralized Logs (Kibana / Elasticsearch)

Audit logs and metrics are ingested into daily-rotated indices (`audit-logs-{yyyy-MM-dd}`, see `spring/elasticsearch.md`) via `ElasticsearchIngestionService`, gated by `elasticsearch.enabled`.

```bash
docker compose up -d kibana   # depends on elasticsearch being healthy first
```
Open `http://localhost:5601`, credentials from `backend/.env` (`ELASTICSEARCH_USERNAME`/`ELASTICSEARCH_PASSWORD`). Search index pattern `audit-logs-*` for a specific user/action, or `app-logs-*` for application log lines shipped via Logstash.

**Troubleshooting:** if Kibana shows no indices, confirm `elasticsearch.enabled=true` is actually set — this feature flag gates every ES bean (`@ConditionalOnProperty(matchIfMissing = false)`), so ingestion silently no-ops when unset rather than erroring.

---

## 5. Common Alert Responses

Alerting is driven by `ElasticsearchAlertingService`, a `@Scheduled(fixedDelay = 60000)` loop reading recent metrics and sending a webhook (`elasticsearch.alert.webhook-url`) when a threshold is breached (`application.properties:251-255`):

| Alert | Threshold (default) | First response |
| :--- | :--- | :--- |
| CPU | `ELASTICSEARCH_ALERT_CPU_THRESHOLD` (80%) | Check `/actuator/metrics/system.cpu.usage`; if sustained, check for a runaway query or N+1 loop (`spring/jpa.md`) before scaling |
| Memory | `ELASTICSEARCH_ALERT_MEMORY_THRESHOLD` (90%) | Check `/actuator/metrics/jvm.memory.used`; a steady climb with no drop after GC suggests a leak — capture a heap dump before restarting |
| Error rate | `ELASTICSEARCH_ALERT_ERROR_RATE_THRESHOLD` (5%) | Check `/actuator/prometheus` `http_server_requests_seconds_count{status=~"5.."}` by endpoint; cross-reference recent deploys (§6) as the first suspect |
| `RolloutStuck` / `RolloutDegraded` (Argo Rollouts — not the active deployment mechanism, see §1.3) | Progressing/Degraded phase | `kubectl argo rollouts get rollout buildnest-app -n buildnest` for the specific step that's stuck; see §6.3 |
| Circuit breaker OPEN (Redis/DB/Elasticsearch) | N/A — logged at `DEBUG`, not alerted directly | Expected graceful-degradation behavior per `spring/resilience4j.md` — confirm the underlying dependency (not the breaker itself) before treating as an incident |

A circuit breaker in the `OPEN` state is **not itself an incident** — it's the system correctly protecting itself. Investigate the dependency it's protecting (MySQL/Redis/Elasticsearch reachability, §2.2) rather than the breaker.

---

## 6. Rollback Procedure

### 6.1 Staging / Production (SSH + Docker Compose — the real mechanism, ADR-0003)

There is no blue-green swap or cluster-native rollback here (§1.2) — rolling back means re-pulling and restarting the previous GHCR image tag on the same host:

```bash
# On the deploy host, in the docker-compose.prod.yml directory:
# `docker compose pull` takes service names, not a service:tag suffix — the tag is
# pinned via the IMAGE_TAG variable that docker-compose.prod.yml's image: field reads.
IMAGE_TAG=<previous-tag> docker compose -f docker-compose.prod.yml --env-file .env.prod pull backend frontend
IMAGE_TAG=<previous-tag> docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --no-deps backend frontend
```

Find the previous good tag via the GHCR package history or `git tag --sort=-creatordate | head` (production deploys trigger on `v*` tag pushes — see §1.2).

**Expected outcome:** confirm via `/actuator/health/readiness` (§2.1) through `nginx-proxy` and error-rate metrics (§5) before considering the rollback complete — a clean `docker compose up -d` exit code does not by itself prove the previous image is healthy under current load.

**Troubleshooting:** if error rates don't recover after rollback, the previous version may share the same root cause (e.g. a bad config value in `.env.prod` applied to both) — check §5's error-rate guidance for the actual failing dependency before assuming the image rollback itself is broken.

### 6.2 Local / Staging (Docker Compose, dev stack)

```bash
docker compose down
git checkout <last-known-good-tag>   # e.g. baseline/m4-YYYY-MM-DD, per SDP §12.3
docker compose up -d
```

### 6.3 Kubernetes / Argo Rollouts manifests — not the active rollback mechanism

`buildnest-rollout.yaml` documents an Argo Rollouts blue-green flow (`kubectl argo rollouts undo`, traffic-selector patching) and SDP RISK-09 (`software-development-plan.md:857`) references it, but per ADR-0003 (§1.2/§1.3) this project has no live Kubernetes cluster — these commands have no real target to run against today. Retained here only so the procedure is documented if/when a cluster is provisioned; do not treat this as the current rollback path.

---

## Validation Status

**Not yet validated by a dry-run on a staging environment.** This runbook's local Docker Compose procedures (§1.1, §2.2, §4.1) and the `backup-db.sh`/`restore-db.sh` commands (§3) were cross-checked against the real scripts and `docker-compose.yml`/`application.properties` configuration, but not executed end-to-end against the actual `staging` GitHub Environment (§1.2/§4.2/§6.1) as a single pass — that requires SSH access to a real staging host this session does not have. The Kubernetes procedures (§1.3/§4.3/§6.3) are documented for completeness only; per ADR-0003 no live cluster exists to validate them against, and #708 (management-port probe mismatch) would block them regardless if one were ever provisioned. Acceptance criterion "Validated by dry-run on staging environment" is **not yet met** — tracked as a follow-up validation pass once staging SSH access is available, rather than closing this criterion against unexecuted procedures.

### Sibling-precedent note

A related runbook, `docs/operations/secrets-rotation-procedure.md` (#132, CFG-01), already exists at a different top-level path (`docs/operations/`, not `docs/SDLC-docs/operations/`). This runbook follows #126's own acceptance criterion path (`docs/SDLC-docs/operations/runbook.md`) rather than matching that precedent — the inconsistency is noted here rather than silently left for a future reader to rediscover; unifying the two under one convention is a documentation-organization decision better made deliberately in its own follow-up than as a side effect of this issue.
