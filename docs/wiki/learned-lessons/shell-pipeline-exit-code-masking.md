---
title: Shell Pipeline Exit Code Masking
category: tooling
tags: [shell, bash, zsh, pipestatus, exit-code, maven, ci]
keywords: [pipeline exit code, PIPESTATUS, tail, grep, tee, mvnw, build failure masked]
source_conversations: [Session 2026-07-01]
last_updated: 2026-07-01
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/github-issue-hygiene.md
---

# Shell Pipeline Exit Code Masking

## Problem

When a command is piped into another command, the shell exit code is the exit code of the **last** command in the pipeline, not the first. A failing build therefore appears to succeed:

```bash
./mvnw verify -q 2>&1 | tail -20
echo $?   # always 0 — tail's exit code, not Maven's
```

This applies to any pipeline ending in `tail`, `grep`, `head`, `wc`, `less`, or similar utilities that succeed regardless of their input.

## Fixes

### Option 1 — `$PIPESTATUS` (bash/zsh)

```bash
./mvnw verify -q 2>&1 | tail -20
echo ${PIPESTATUS[0]}   # Maven's exit code
```

`$PIPESTATUS` is an array of exit codes for each pipeline stage. Index 0 is the leftmost command.

To propagate the failure:

```bash
./mvnw verify -q 2>&1 | tail -20; exit ${PIPESTATUS[0]}
```

### Option 2 — `tee` preserves the upstream exit code path

```bash
./mvnw verify 2>&1 | tee /tmp/build.log | tail -20
```

`tee` still exits 0. Use `$PIPESTATUS[0]` afterward, or enable `pipefail`.

### Option 3 — `set -o pipefail` (preferred for scripts)

```bash
set -o pipefail
./mvnw verify -q 2>&1 | tail -20
```

With `pipefail`, the pipeline exit code is the rightmost non-zero exit in the pipeline. Scripts should set this at the top to prevent silent failures.

## Rule

Never use `| tail`, `| grep`, or similar in a CI script or Makefile target without either `set -o pipefail` or explicit `$PIPESTATUS` capture. A green CI step that masked a build failure is harder to debug than a red one.
