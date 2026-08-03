# 0003. SSH + Docker Compose against GHCR images as the deployment mechanism

* Status: accepted
* Date: 2026-08-03
* Issue: #120

## Context and Problem Statement

#120 asks for a real `deploy.yml` workflow that builds and pushes Docker images, deploys to
staging/production with a no-downtime rolling/blue-green strategy, notifies on deploy, and gates
production behind manual approval. The existing `deploy.yml` (built for the earlier, duplicate
#217) already had this shape stubbed out, but its `Push to Container Registry` and `Deploy to
Kubernetes` steps were both hardcoded `if: false` placeholders — no registry, no cluster, no real
target ever configured. This repo has no Kubernetes cluster, no cloud account, and no existing CI
secret set for any deployment target. What should the real (not stubbed) deployment mechanism be,
given that constraint?

## Decision Drivers

* No Kubernetes cluster or cloud provider account exists for this project — inventing one would
  mean fabricating infrastructure that doesn't exist, producing a workflow that looks complete but
  deploys nowhere real (the exact failure mode #120 was filed to fix).
* `docker-compose.prod.yml` (#119/#671) already exists, is merged, and models the full production
  stack (MySQL, Redis, Elasticsearch, backend, frontend, nginx-proxy with TLS) — reusing it avoids
  a second, parallel deployment definition.
* A no-downtime rollout requirement, without a cluster's built-in rolling-update primitive,
  needs a mechanism achievable with plain Docker Compose on a single host.
* A genuine manual-approval gate needs to be enforced by GitHub itself (auditable, blocking),
  not by a workflow step that merely asks for confirmation and can be bypassed by re-running.

## Considered Options

* Kubernetes (`kubectl set image` against a real cluster) — the shape the old stub gestured at
* A cloud provider's managed deployment API (AWS ECS/App Runner, Azure Container Apps, etc.)
* SSH into a host running `docker-compose.prod.yml`, pull pre-built images from GHCR, and
  `docker compose up -d` for a rolling per-service restart

## Decision Outcome

Chosen option: **SSH + Docker Compose against GHCR-hosted images**, because it's the only option
that doesn't require infrastructure this project doesn't have. Kubernetes and the managed
cloud-API options both require a real cluster/account and credentials that don't exist yet;
choosing either now would mean either fabricating fake credentials (producing an untestable,
non-functional workflow) or blocking #120 entirely on infrastructure procurement outside this
issue's scope. The SSH+Compose approach:

* Uses GHCR (`ghcr.io`) with the workflow's own built-in `GITHUB_TOKEN` for push/pull — no
  external registry account or credential to fabricate.
* Reuses `docker-compose.prod.yml` unmodified in structure — only adds an `image:` field
  alongside each service's existing `build:` field, so local `docker compose build` still works
  for dev, while a deploy host instead runs `docker compose pull` to fetch the CI-built image
  (see `docker-compose.prod.yml`'s and `.env.prod.example`'s own inline comments for the
  `IMAGE_TAG`/`BACKEND_IMAGE`/`FRONTEND_IMAGE` mechanism).
* Achieves a rolling restart via `docker compose up -d --no-deps backend frontend` — each
  container is replaced independently; MySQL/Redis/Elasticsearch/nginx-proxy are untouched, so
  the proxy keeps serving through the brief backend/frontend restart window. This is a narrower
  guarantee than a true zero-downtime blue-green swap (no second, parallel backend instance to
  cut over to) — acceptable given the AC's stated alternative, "Blue-green **or** rolling
  deployment strategy," and given no load balancer/second host exists to blue-green against.
* Uses a GitHub `environment: production` with required-reviewer protection rules as the manual
  approval gate — a native, auditable GitHub mechanism, not a workflow-internal confirmation step
  that could be bypassed by re-running the job. This requires a one-time manual step in this
  repo's own Settings > Environments UI (adding required reviewers) — a human-only action outside
  `git`/`gh`'s reach, called out explicitly in the issue's closing documentation rather than
  assumed configured.

### Consequences

* Good, because it's real and testable today — no fabricated credentials, no workflow step that
  will forever report `if: false`.
* Good, because it reuses #119/#671's existing compose stack rather than inventing a second
  deployment definition to keep in sync.
* Bad, because a genuine zero-downtime blue-green cutover (two parallel environments + traffic
  shift) isn't achievable with a single Compose host — documented above as a deliberate,
  narrower-than-ideal trade-off, not a silent gap.
* Bad, because `STAGING_SSH_HOST`/`STAGING_SSH_KEY`/`STAGING_SSH_USER` and their `PROD_*`
  counterparts are placeholder secrets the repo owner must still provision (a real host to SSH
  into) before either deploy job can run successfully — the workflow is real and correct, but
  inert until that one-time infrastructure step happens outside this issue's scope.
* If a real Kubernetes cluster or managed cloud target is provisioned later, this ADR should be
  superseded rather than silently reworked — the SSH+Compose steps would need to be replaced
  wholesale, not patched.
