# 0004. Grafana Tempo as the distributed tracing backend

* Status: accepted
* Date: 2026-08-08
* Issue: #108

## Context and Problem Statement

#108 (OBS-02, SRS NFR-OPS-04, RTM MON-05) requires distributed tracing across HTTP requests and
async tasks, exported to "Zipkin (or Grafana Tempo if available)" per the issue body. Neither
backend was already deployed — `backend/docker-compose.yml` runs Prometheus + Grafana for metrics
(#122) but has no trace store. This is a genuine architectural choice, not a foregone one: no
prior instance of this decision exists in the repo's ADR index or issue history (checked via
`gh issue list --search "tracing OR zipkin OR tempo OR observability"` and a grep of the ADR
directory — first tracing-backend decision made in this repo).

## Decision Drivers

* Existing observability stack: Prometheus + Grafana are already deployed and are the team's
  established dashboard surface (#122)
* Avoid a second, disconnected monitoring UI if a better-integrated option exists
* Keep the exporter path standard (OTLP) so the choice of backend doesn't dictate the app's own
  tracing library — `micrometer-tracing-bridge-otel` exports OTLP regardless of which backend
  receives it

## Considered Options

* Grafana Tempo
* Zipkin

## Decision Outcome

Chosen option: "Grafana Tempo", confirmed with the user via `AskUserQuestion` (no prior repo
precedent to defer to). Tempo registers as a native Grafana datasource alongside the existing
Prometheus datasource, giving one dashboard surface for both metrics and traces, including
trace-to-metrics correlation (`tracesToMetrics`/`serviceMap` datasource linking, see
`backend/grafana/provisioning/datasources/tempo.yml`) — Zipkin would have added a second,
disconnected UI the team would need to separately learn and cross-reference against Grafana.

### Consequences

* Good, because traces are queryable from the same Grafana instance already bookmarked for
  metrics dashboards — no context-switch to a second tool for a checkout-flow investigation that
  spans both.
* Good, because the OTLP receiver Tempo exposes is the same protocol Zipkin's OTLP-compatible
  receiver would have used — switching backends later only means repointing
  `TEMPO_OTLP_ENDPOINT`, not changing application code.
* Bad, because Tempo's local single-binary storage backend (used here for dev/staging) has no
  built-in replication or object-store durability — acceptable for this issue's scope (a local
  dev/CI stack), but a production deployment would need to swap `storage.trace.backend` to an
  object store (s3/gcs/azure), which is out of scope for #108.

## Pros and Cons of the Options

### Grafana Tempo

* Good, because it integrates as a native Grafana datasource, correlating with the existing
  Prometheus metrics already dashboarded.
* Good, because it's part of the same Grafana Labs stack already in use (Prometheus + Grafana),
  reducing the number of distinct vendor/tool conventions the team has to track.
* Bad, because it requires standing up an additional service (Tempo itself) plus datasource
  provisioning config, more moving parts than a single Zipkin container.

### Zipkin

* Good, because it is a single, self-contained container with its own dedicated UI and a
  long-established Spring Boot integration path (`micrometer-tracing-bridge-zipkin` /
  `spring-boot-starter-zipkin` idioms are extremely well documented).
* Bad, because it adds a second, disconnected UI outside the Grafana dashboard the team already
  uses for metrics — a checkout-flow investigation spanning both metrics and traces would require
  switching tools and manually correlating timestamps.
