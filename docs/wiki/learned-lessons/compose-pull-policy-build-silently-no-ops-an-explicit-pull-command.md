---
title: "Docker Compose's `pull_policy: build` Silently No-ops an Explicit `docker compose pull`"
category: infrastructure
tags: [docker-compose, ghcr, ci-cd, deployment]
keywords: [pull_policy build skipped pull, docker compose pull does nothing, compose image and build both defined, ghcr pull skipped, deploy workflow pull policy]
source_conversations: [Session 2026-08-03, issue #120]
last_updated: 2026-08-03
confidence: high
evidence_strength: strong
root_cause: "pull_policy: build tells Compose to prefer building over pulling for ANY command that would fetch the image, not just the implicit pre-up pull — including an explicit `docker compose pull` invocation, which silently reports the image as \"Skipped\" instead of erroring or fetching it"
impact: medium — would have made a CI-driven deploy pipeline's core mechanism (pull the CI-built image) a permanent no-op, invisible until an actual deploy ran and served stale/local content instead of the intended release
related_lessons:
  - dockerfile-vs-docker-compose-and-prebuilt-images.md
---

# Docker Compose's `pull_policy: build` silently no-ops an explicit `docker compose pull`

## What happened

Building #120's deploy workflow, `docker-compose.prod.yml`'s backend/frontend services were given
both an `image:` field (pointing at a GHCR tag) and their existing `build:` field, so a deploy
host could run `docker compose pull` to fetch the CI-built image while local dev could still run
`docker compose build` unchanged. A code-review pass raised a real concern: with both `image:` and
`build:` present and no explicit `pull_policy`, Compose's default (`missing`) means `docker compose
up` can attempt an implicit registry pull before falling back to a local build — a behavior change
from before, when `build:` was the only source of truth.

The suggested fix was to add `pull_policy: build` to make `up`/`build` never attempt a registry
pull. Applying it and re-testing with `docker compose pull backend --dry-run` showed:

```
Image ghcr.io/pradip9096/buildnest-backend:latest Skipped
```

`pull_policy: build` doesn't just affect the implicit pull inside `up` — it makes **every**
pull-related Compose command prefer building, including an *explicit* `docker compose pull`
invocation. Since the entire deploy mechanism being built was "CI builds and pushes the image,
then the deploy host runs `docker compose pull` to fetch it," this setting would have made that
pull a permanent, silent no-op — the deploy host would never actually receive the new image,
serving whatever was already running (or attempting a local build it usually can't do, since
production hosts don't necessarily have the source tree).

## Why this is non-obvious

`pull_policy` reads as if it only scopes the *default*, implicit pull-before-up behavior that
having both `image:` and `build:` introduces — not as something that overrides a command whose
entire purpose is stated explicitly (`docker compose pull`, not `docker compose up`). Reading the
compose-spec field name and its stated purpose ("policy for pulling image during container
creation") does not obviously extend to "also silently disables the standalone pull subcommand."

## The fix

Don't set `pull_policy: build` on a service definition that a deploy pipeline relies on
`docker compose pull` to actually fetch a new image for. If the underlying concern (implicit
pull during `docker compose up` in a context with no registry access) doesn't actually apply —
e.g. the compose file in question is a deploy-only file, never invoked bare by local dev without
first running an explicit `pull` — the concern is moot and the field should be omitted entirely,
verified via `docker compose pull <service> --dry-run` before trusting either behavior.
