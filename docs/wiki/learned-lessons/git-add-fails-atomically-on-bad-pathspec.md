---
title: "`git add path1 path2 badpath` Stages Nothing, Not Just the Valid Paths"
category: tooling
tags: [git, add, pathspec, staging, atomic-failure]
keywords: [git add partial failure, git add did not match any files, one file already staged deleted path, incomplete commit from multi-arg add]
source_conversations: [Session 2026-07-02]
last_updated: 2026-07-02
confidence: high
evidence_strength: strong
root_cause: "git add fails atomically across the whole invocation when any one pathspec doesn't match, so valid paths in the same call are silently left unstaged, not just the bad one"
impact: medium — produced an incomplete first commit missing three of four intended files, requiring a second commit to carry the rest of the same logical change
related_lessons:
  - docs/wiki/learned-lessons/git-checkout-vs-reset-order.md
---

# `git add path1 path2 badpath` Stages Nothing, Not Just the Valid Paths

## Problem

Running `git add fileA fileB fileC` where `fileC` doesn't exist (e.g. it was already deleted and staged by an earlier `git rm`, so it's no longer present on disk to be re-added) fails with `fatal: pathspec 'fileC' did not match any files` and **stages none of the paths in that invocation** — not just the bad one. `fileA` and `fileB` are left unstaged even though they were valid, existing paths.

This produced an incomplete commit in this session: a `git add` call listed a deleted-and-already-staged test file alongside three other modified/new files. The deleted file was already in the index from a prior `git rm`, so it appeared in the resulting commit; the other three files were silently *not* staged by the failed `git add` call and were absent from the commit, requiring a second commit to carry the rest of the same logical change.

## Fix

Verified directly: `git add valid1.txt does-not-exist.txt valid2.txt` exits 128 and `git status` shows `valid1.txt`/`valid2.txt` still unstaged afterward — confirming the failure is atomic across the whole invocation, not per-path.

After any multi-path `git add`, check `git status --short` (or the command's own exit code) before committing — do not assume a `fatal: pathspec` error only dropped the one bad path.

## Rule

Never assume a multi-argument `git add` partially succeeded when one pathspec errors. Re-run `git status --short` after the `add` and before the `commit` to confirm every intended file actually shows as staged (`M `/`A `/`D ` in the first column, not the second). This is especially easy to trigger right after a `git rm` of one of the files in the same batch — the removed path is no longer on disk to match.
