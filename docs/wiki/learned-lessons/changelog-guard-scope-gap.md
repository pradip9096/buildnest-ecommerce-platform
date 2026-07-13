---
title: changelog-guard Hook Only Covers backend/src/main and frontend/src
category: process
tags: [changelog, hooks, claude-code, coverage-gap, tooling]
keywords: [changelog-guard.sh scope, start.sh not covered, hook path filter too narrow, CHANGELOG missed]
source_conversations: [Session 2026-07-02]
last_updated: 2026-07-02
confidence: medium
evidence_strength: moderate
root_cause: "the changelog-guard PreToolUse hook's path filter only watches backend/src/main/** and frontend/src/**, so commits to root-level scripts, CI config, or other infra files never trigger the CHANGELOG check at all"
impact: low — a missed CHANGELOG entry, caught and fixed manually the same session
related_lessons:
  - docs/wiki/learned-lessons/pretooluse-hook-fires-once-per-bash-call.md
---

# `changelog-guard` Hook Only Covers `backend/src/main` and `frontend/src`

## Problem

The `PreToolUse` hook built to enforce CHANGELOG discipline (`.claude/hooks/changelog-guard.sh`) only denies a `git commit` when staged changes touch `backend/src/main/**` or `frontend/src/**` without a staged `CHANGELOG.md` change. A commit fixing two real bugs in `start.sh` (a repo-root dev convenience script, not under either watched path) landed without the hook prompting for a CHANGELOG entry at all — the user had to separately ask "update the CHANGELOG" afterward for it to get recorded.

This is a scope gap, not a hook malfunction: the hook did exactly what it was configured to do. But "notable changes" (per the CHANGELOG's own stated purpose) clearly includes things like `start.sh`, CI config, Liquibase changesets outside `src/main`, and other tooling/infra files that live outside the two watched path prefixes.

## Fix

None applied yet — noting the gap rather than widening the hook unilaterally, since broadening the path filter changes what blocks future commits and deserves explicit sign-off (same reasoning that applied when originally scoping the hook).

## Rule

Do not assume the `changelog-guard` hook's silence means a change doesn't warrant a CHANGELOG entry — it only watches `backend/src/main/**` and `frontend/src/**`. For changes to root-level scripts, CI/CD config, Liquibase changesets, or other infra files, manually check whether the change is "notable" and add the entry proactively; the hook will not prompt for it.
