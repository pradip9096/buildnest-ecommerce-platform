---
title: git checkout -- Restores From the Index, Not HEAD, on Already-Staged Files
category: tooling
tags: [git, checkout, reset, staging-area, cleanup]
keywords: [git checkout -- does not revert staged change, git reset before checkout, test artifact left staged, cleanup order matters]
source_conversations: [Session 2026-07-02]
last_updated: 2026-07-02
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/pretooluse-hook-fires-once-per-bash-call.md
---

# `git checkout -- <file>` Restores From the Index, Not HEAD, on Already-Staged Files

## Problem

While cleaning up after a scripted test (append a test line, `git add` it, then revert), the cleanup command was:

```bash
git checkout -- CHANGELOG.md
git reset CHANGELOG.md
```

This left the test line (`test line`) still present in the file after both commands ran — the "revert" silently did nothing.

`git checkout -- <file>` restores the **working tree** from the **index** (staging area), not from `HEAD`. If a file has already been `git add`ed with the modification, the working tree already matches the index — `checkout --` has nothing to change, because as far as it's concerned the working tree is already "correct" (equal to what's staged). The modification survives in both the index and the working tree. Running `git reset` afterward only unstages the file; it does not touch the working-tree content, which still carries the leftover edit.

This produced two separate incidents in one session: leftover `test line` appended to `CHANGELOG.md` and a `note` line in a lessons-learned README, both requiring a second manual cleanup pass with `git restore` (unstage + revert working tree in one step, or `git reset` before `git checkout --` in the correct order).

## Fix

To fully discard a staged modification, either:

```bash
git restore --staged --worktree <file>   # single command, correct in one step
```

or, if using the older two-step form, **unstage first, then restore**:

```bash
git reset -- <file>       # unstage (index -> matches HEAD... but worktree still has the edit)
git restore -- <file>     # or: git checkout -- <file>  (now restores worktree from index, which now matches HEAD)
```

The two-step form only works in `reset`-then-`checkout` order. `checkout`-then-`reset` (the order used in the failing case) does not work.

## Rule

To fully discard a staged test/scratch change, use `git restore --staged --worktree <file>` (one command, no ordering pitfall) rather than composing `checkout --` and `reset` by hand. If composing manually, `reset` must come before `checkout --`/`restore` — never the reverse.
