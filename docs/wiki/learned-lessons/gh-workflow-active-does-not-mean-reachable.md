---
title: "gh workflow list 'active' Does Not Mean a Workflow Is Reachable"
category: tooling
tags: [github-actions, ci-cd, workflow-triggers]
keywords: [gh workflow list, paths-ignore, dead workflow, unreachable trigger, branch mismatch]
source_conversations: ["#336"]
last_updated: 2026-07-10
confidence: high
evidence_strength: direct-repo-verification
related_lessons: [github-actions-working-directory-default-only-applies-to-run-steps]
---

## What happened

While auditing `.github/workflows/` for #336 (adding `paths-ignore` to skip full CI on docs-only
PRs), `gh workflow list` reported `ci-cd.yml` ("BuildNest CI/CD Pipeline") as `active`, alongside
`ci.yml` and `ci-cd-pipeline.yml`. But `ci-cd.yml`'s `on:` trigger only fires on `push`/
`pull_request` to `main` or `develop` — and `git branch -a` confirmed neither branch exists in
this repo (only `master`). The workflow is enabled and would run if those branches existed, but
in practice it has never triggered and never will under the repo's actual branching convention.

## Why it matters

`active` in `gh workflow list` (and the green toggle in GitHub's UI) reflects whether the workflow
*file* is enabled — not whether its trigger conditions can ever actually match real repo state.
A workflow can be perfectly valid YAML, fully enabled, and permanently dead because its `branches:`
filter references branches that were renamed, never created, or deprecated (e.g. a `main`→`master`
rename, or a `develop` branch strategy that was dropped). Nothing surfaces this mismatch
automatically — no warning, no lint, no visual distinction from a working workflow in the Actions
tab list.

This matters for two reasons: (1) config changes made to a dead workflow (like this session's
`paths-ignore` addition) are logically correct but currently inert — worth noting explicitly
rather than silently fixing a file no one will ever see run; (2) a genuinely broken dead workflow
can sit unnoticed for a long time, the same "undocumented drift, not deliberate decision" pattern
already seen with the GitHub Projects board (`github-projects-board-retired-not-a-deliberate-original-decision.md`).

## How to apply

Before assuming a workflow file's behavior reflects live CI (e.g. when auditing all workflows for
a cross-cutting change, or when a workflow looks stale/legacy), check its `on: push/pull_request:
branches:` list against `git branch -a` (or the repo's actual default/long-lived branches) — not
just `gh workflow list`'s `active` status. If a workflow's trigger branches don't exist, flag it
explicitly (file a follow-up issue for removal/repair) rather than silently leaving it as
inert-but-present config that reads as active tooling to a future reader.
