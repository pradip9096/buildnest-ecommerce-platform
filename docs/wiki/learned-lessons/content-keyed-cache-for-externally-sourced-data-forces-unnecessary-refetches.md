---
title: "A Content-Keyed Cache for Externally-Sourced Data Forces Unnecessary Full Refetches — Key on Time Instead"
category: tooling
tags: [github-actions, actions-cache, owasp, nvd, cache-invalidation, ci-performance]
keywords: [cache key design, hashFiles pom.xml, external dataset caching, restore-keys, actions/cache, NVD database]
source_conversations: ["#342", "#343"]
last_updated: 2026-07-10
confidence: high
evidence_strength: direct-repo-verification
root_cause: "the NVD database cache was keyed on hashFiles('**/pom.xml') even though the cached content is externally-sourced and unrelated to pom.xml, so every dependency edit minted a fresh cache key with nothing to restore"
impact: medium — each of three parallel PRs independently paid a ~1.5-2 hour full resync cost before the keying bug was found
related_lessons: [check-sibling-branches-before-filing-a-duplicate-issue]
---

## What happened

`ci.yml`'s "Cache NVD database" step keyed the `actions/cache` entry on `hashFiles('**/pom.xml')`.
The NVD CVE database is an external dataset with no relationship to this project's own dependency
versions — it updates on its own schedule regardless of what's in `pom.xml`. But because the cache
*key* was tied to `pom.xml`'s content, every dependency change (a version bump, a suppression
edit) produced a brand-new cache key with nothing to restore, forcing `dependency-check-maven` to
redo a full multi-hour historical sync from scratch. This was invisible until three separate PRs
in the same investigation (#335, #337, #338) each independently paid the same ~1.5-2 hour cost —
paradoxically, the PRs *fixing* the CVE dependency versions were the ones most likely to trigger
the slow path, since they were guaranteed to touch `pom.xml`.

The fix: rekey on a daily date string instead, with an OS-scoped `restore-keys` prefix fallback.
Verified empirically — two consecutive commits that each touched `pom.xml` both hit the same-day
cache (confirmed via the literal log line `Cache hit for: nvd-db-Linux-<date>`), cutting job time
from ~20 minutes (cold cache) to ~1 minute (warm cache) each time.

## Why it matters

The instinct to key a cache on `hashFiles('**/<manifest>')` is usually correct — for caches whose
*validity* actually depends on that manifest's content (e.g. a `node_modules` or `.m2` dependency
cache, where a changed lockfile genuinely means different content is needed). It's the wrong
instinct for a cache holding data from an independent external source. The manifest-hash pattern
is common enough in CI templates/boilerplate that it gets copy-pasted into new cache steps without
re-asking "does this cache's validity actually track this file's content, or just happen to share
a workflow with it?"

## How to apply

Before keying any `actions/cache` (or equivalent) entry on a file hash, ask: does the *cached
content itself* change when that file changes, or is the cached content actually independent and
just being fetched/used *during* a step that also happens to depend on that file? If the cache
holds:
- **Project-derived artifacts** (installed packages matching a lockfile, compiled output matching
  source) — a content hash is correct; the cache genuinely becomes stale when the file changes.
- **Externally-sourced data** (a vulnerability database, a public dataset, a third-party API
  response cache) — key on **time** (daily/weekly, matching how often the *source* actually
  updates) with a prefix-based `restore-keys` fallback, not on any local file's content. Confirm
  `restore-keys` prefix-matching actually returns the most-recently-created match (per GitHub's own
  `actions/cache` docs) before relying on it as a fallback.
