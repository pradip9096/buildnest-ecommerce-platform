---
title: JaCoCo report and check executions can silently diverge in scope
category: testing
tags: [jacoco, coverage, maven, ci, silent-drift]
last_updated: 2026-07-06
---

# JaCoCo Report and Check Executions Can Silently Diverge in Scope

## What happened

BuildNest's `pom.xml` has two separate JaCoCo executions: a `report` execution (phase `test`, generates the human-visible HTML/CSV) and a `jacoco-check` execution (phase `verify`, in the `ci` profile, enforces the 85% coverage gate). Each execution can carry its own `<excludes>` config, which is *not* automatically kept in sync with the plugin's global default `<configuration>`.

Someone updated the global default excludes to stop hiding `model.entity`/`model.elasticsearch` (comment: "now have comprehensive tests"), intending to bring them into the enforced gate. But the `report` execution's own exclude list was never updated to match. Result: the check gate correctly evaluated `model.entity` and failed at 63% coverage — but the visible report excluded that package entirely, so nobody could see *why* by looking at the coverage report. The failure and the diagnostic data were pointing at two different scopes.

## Why this is non-obvious

Maven executions of the same plugin don't inherit each other's `<configuration>` — each execution either defines its own or falls back to the plugin-level default, independently. Editing "the excludes" in one place gives no warning that a sibling execution has its own copy that's now stale. There's no lint or build-time check for this drift; it only surfaces as a confusing mismatch between "the report looks fine" and "the gate fails."

## How to apply

- When a JaCoCo (or any multi-execution plugin) config has more than one `<excludes>`/`<includes>` block across executions, treat them as a single source of truth conceptually — grep for all occurrences before changing scope, not just the one you're looking at.
- If a report and an enforcement gate use the same underlying data but different scope, verify they agree by literally comparing packages present in each output — don't assume shared plugin identity implies shared config.
- When a `check` goal fails with no visible cause in the report, don't trust the report's completeness — rerun the specific bound execution directly (e.g. `./mvnw -P ci org.jacoco:jacoco-maven-plugin:VERSION:check@jacoco-check`) to get the actual rule-violation message, since default `-q` verify runs and even non-quiet runs may bury it.

## Related
- [[verify-issue-premises-against-repo-before-implementing]]
- `docs/knowledge-base/project/quality-gate-ratchet-pattern.md`
