---
title: "Changing a Health Endpoint's Semantics Requires Grepping CI/CD Workflow Consumers, Not Just Application Code"
category: infrastructure
tags: [spring-boot, actuator, ci-cd, health-groups, github-actions, wait-loop]
keywords: [readiness probe semantics change, CI wait loop health check, github actions curl actuator health, master-only workflow invisible in PR CI]
source_conversations: [Session 2026-08-08/09, issue #123]
last_updated: 2026-08-09
confidence: high
evidence_strength: strong
root_cause: "A CI wait-loop script (ci-cd-pipeline.yml's Load Tests job) polled /actuator/health/readiness on the assumption — stated in its own comment — that readiness stayed UP regardless of Redis reachability; #123 changed that assumption by wiring db/redis/elasticsearch into the readiness group, and nothing in the application-code change process grepped .github/workflows/ for existing consumers of the endpoint's old behavior before merging"
impact: high — broke a CI job (ci-cd-pipeline.yml, push-to-master only) invisibly, since it never runs on pull_request and so never surfaced during #123's own PR CI; only caught by closure-time re-verification against fresh post-merge evidence
related_lessons:
  - docs/wiki/learned-lessons/spring-boot-health-group-fails-startup-on-nonexistent-indicator-reference.md
---

# Changing a Health Endpoint's Semantics Requires Grepping CI/CD Workflow Consumers, Not Just Application Code

## Problem

#123 wired `management.endpoint.health.group.readiness.include` to genuinely reflect
MySQL/Redis/Elasticsearch reachability — the correct real-world fix for Kubernetes readiness
probes. All application-code review (self-review, `java-reviewer` agent, PR CI) passed cleanly.
Closure-time re-verification against fresh post-merge CI on `master`, however, found
`ci-cd-pipeline.yml`'s `Load Tests` job timing out: its own startup wait-loop polled
`/actuator/health/readiness` with an inline comment explaining that it deliberately chose that
endpoint because readiness previously stayed UP "regardless of Redis/Elasticsearch reachability"
in that job's stripped-down environment (H2 only, no live Redis). #123's change made that comment
— and the script's behavior — silently wrong.

## Root Cause

This repo's `ci-cd-pipeline.yml` (`Full Test Matrix & Docker Publish`) only triggers on `push` to
`master`, not on `pull_request` (see `development-workflow.md`'s Workflow File Responsibilities
table). A regression in one of its own scripts is therefore **structurally invisible during a
PR's own CI run** — the exact same failure mode already documented for `deploy.yml`'s
`workflow_run`-gated triggering, but here affecting a *script's behavioral assumption* rather
than a trigger condition. `add-run-tests`/`code-review-ci` both passed because neither exercises
this workflow file at all; only `verify-post-merge`'s fresh-evidence re-check against real
post-merge CI caught it.

## Fix

Changed the wait-loop to poll `/actuator/health/liveness` instead — liveness only reflects
JVM/app-context health (`management.endpoint.health.group.liveness.include` is `livenessState`
only), matching what the wait loop actually needs ("is the app serving requests"), independent of
dependency reachability. A sibling wait script in the same file (the E2E job) already had a
`|| curl .../api/public/products` fallback and was unaffected — left untouched.

## Generalization

Before changing what a health/readiness/liveness endpoint's aggregate status *means* (adding or
removing a contributor from a health group, changing a `HealthIndicator`'s up/down logic), grep
**`.github/workflows/*.yml`** for existing `curl`/wait-loop consumers of that endpoint — not just
application code, controllers, or tests. A CI wait script's health-check choice encodes an
implicit assumption about the endpoint's current semantics the same way application code does,
but it's invisible to `grep -r` scoped to `src/`, and invisible to PR CI entirely if the
workflow file in question only triggers on `push`/`master`. This is a distinct failure shape
from `deploy.yml`'s already-documented "required check lives in a `paths-ignore`'d workflow"
gap — there, the workflow never runs at all; here, the workflow runs but the meaning of what
it's checking silently changed underneath it.
