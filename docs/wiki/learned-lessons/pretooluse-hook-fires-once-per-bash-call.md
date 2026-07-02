---
title: PreToolUse Hooks Fire Once Per Bash Tool Call, Not Per Shell Command
category: tooling
tags: [claude-code, hooks, pretooluse, bash, git-hooks, testing]
keywords: [PreToolUse fires before entire command, multi-line bash hook test false negative, git add git commit combined, hook staged state stale]
source_conversations: [Session 2026-07-02]
last_updated: 2026-07-02
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/git-checkout-vs-reset-order.md
---

# PreToolUse Hooks Fire Once Per Bash Tool Call, Not Per Shell Command

## Problem

While building a `PreToolUse` hook on the `Bash` matcher to block `git commit` when source changes were staged without a matching `CHANGELOG.md` update, the hook repeatedly failed to fire — commits went through even when the deny condition should have applied. Debug logging eventually showed `staged=[]` (empty) at the moment the hook ran, even though the test had just run `git add` on a source file.

The cause: the test script combined staging and committing into a **single Bash tool call**:

```bash
echo "// test" >> SomeFile.java
git add SomeFile.java
git commit -m "test"
```

`PreToolUse` evaluates once, before the entire Bash tool invocation executes — not before each individual line of a multi-line/chained shell script. At the moment the hook ran, `git add` had not yet executed, so `git diff --cached` was empty and the hook correctly (by its own logic) found nothing to deny.

## Fix

Split staging and committing into separate Bash tool calls, matching how commits are actually made in practice (they are rarely combined into one shell invocation). Once separated, the hook fired with the correct staged state and denied/allowed as designed.

## Rule

When testing a `PreToolUse`/`Bash` hook that inspects state mutated by an earlier command (staged files, a lockfile, a running process), never combine the state-mutating command and the triggering command in one Bash tool call during testing — the hook sees pre-invocation state, not mid-script state. Test with separate tool calls, one per real-world action.
