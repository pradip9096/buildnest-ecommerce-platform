---
title: Committed Directly to master Despite development-workflow.md's Mandatory create-branch Step
category: process
tags: [git-workflow, create-branch, development-workflow, self-check-gap]
keywords: [git commit on master, forgot to create branch, create-branch Mandatory, git reset --hard origin/master recovery, branch after the fact]
source_conversations: ["#651"]
last_updated: 2026-08-02
confidence: high
evidence_strength: verified — reproduced live in this session, caught and recovered before push
root_cause: "the task-list seeded at the start of #651's session tracked the Proactive Recurrence Scan, sibling-precedent check, and sequence-table enumeration as their own TaskCreate items, but never seeded create-branch itself as a checkable item -- despite development-workflow.md listing it as Mandatory at every tier with no exceptions -- so nothing forced a check immediately before the first `git commit` call"
impact: "medium -- caught before push (git log origin/master..master showed the stray commit), recovered cleanly via git branch + git reset --hard origin/master + git checkout, but had it been pushed it would have required a force-push or a messier history rewrite to fix"
related_lessons: []
---

## What happened

While implementing #651 (a Redis cache-corruption bug fix), the session ran through the
Proactive Recurrence Scan, tier statement, and sequence-table enumeration correctly, then went
straight from `implement-code` to `git commit` — skipping `create-branch` entirely. The commit
landed directly on `master`. It was only caught because a review step (git status / diff review
before the PR) surfaced `git branch --show-current` returning `master`.

## Root cause

`create-branch` is listed in `development-workflow.md`'s Sequence table as **Mandatory** at every
tier, with an explicit note: "severity does not exempt an issue from having its own branch and
PR." But unlike the file's own `sequence-table-partial-enumeration` or `sibling-precedent-scope`
checks — both of which are *artifact-shaped* (a `TaskCreate` item, a stated N/A) and have been
progressively hardened after repeated failures as pure prose — `create-branch` had no equivalent
seeding requirement anywhere in this session's task list. Nothing forced a check immediately
before the first commit.

## The fix (this instance)

Recovered without a force-push, since the commit hadn't been pushed yet:

```bash
git branch fix/651-product-cache-serialization-corruption   # keep the commit
git reset --hard origin/master                              # move master back
git checkout fix/651-product-cache-serialization-corruption # continue work on the branch
```

If the commit had already been pushed to `origin/master`, this would have required either a
force-push (risky, needs explicit user sign-off per this repo's own git-safety rules) or leaving
the commit on `master` and accepting the process violation — a materially worse outcome than
catching it pre-push.

## The generalizable rule

**A `git commit` call is itself a natural forcing point to verify the current branch is not
`master`/`main`**, the same way `git status` is already checked before any destructive operation
per this session's own standing instructions. Before running `git commit` for issue-driven work in
a repo whose workflow mandates per-issue branches, run `git branch --show-current` (or equivalent)
and confirm it is not the default branch — cheap, mechanical, and catches this class of mistake
before it requires any recovery at all rather than after the fact.

## Recurrence tracking

This is a 1st occurrence — no existing `[defect-class: ...]` tag in `development-workflow.md`
covers "the create-branch step itself was skipped" (as distinct from `git-workflow.md`'s Merge
Strategy section, which covers branch *hygiene* after a branch already exists). Whether this
warrants promoting a `[defect-class: create-branch-not-checked]` tag into
`development-workflow.md`'s own Amendment Log — matching the artifact-shaped-requirements pattern
already used for `sequence-table-partial-enumeration`/`sibling-precedent-scope` — is a judgment
call for whoever next reviews this lesson; flagged here rather than silently promoted, since a
single occurrence doesn't yet establish a recurring pattern.
