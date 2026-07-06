---
title: Push vs. Pull Request vs. CI, and Commit Batch Sizing
category: git-workflow
tags: [git, push, pull-request, ci, commit-batching, code-review, workflow]
keywords: [direct push, PR gate, CI trigger, workflow_run, deploy gating, batch size, unpushed commits]
objective: Clarify how push, PR, and CI relate to each other, when direct push to master is acceptable in BuildNest, and how large a commit batch should get before pushing.
audience: engineers working on BuildNest who are deciding between direct push and PR-based workflows
scope: BuildNest-specific CI trigger configuration; general reasoning applicable to any repo
source_conversations: [Session 2026-07-06]
last_updated: 2026-07-06
confidence: high
evidence_strength: strong
related_articles:
  - docs/knowledge-base/project/quality-gate-ratchet-pattern.md
status: published
---

# Push vs. Pull Request vs. CI, and Commit Batch Sizing

## What Each Term Means

| Term | Role |
|---|---|
| **Push** | The mechanism that gets commits onto a branch. No review step by itself. |
| **Pull Request (PR)** | An optional staging step *before* commits land on a target branch — push to a side branch, then propose merging it. Adds a review/discussion gate and a durable record tied to the change. |
| **CI** | Automated validation (build, test, security scan) that runs in response to a push or a PR event. |

These are independent axes. A repo can run CI on direct pushes, on PRs, or both — the presence of a PR doesn't create CI, and CI doesn't require a PR.

## How They Interact in BuildNest

Checked `.github/workflows/*.yml` triggers directly (2026-07-06):

| Workflow | Triggers on |
|---|---|
| `ci-cd-pipeline.yml` | push to `main`/`master`/`develop`, PR to same, `workflow_dispatch`, weekly schedule |
| `ci-cd.yml` | push and PR to `main`/`develop` |
| `ci.yml` | push and PR to `main`/`master`/`develop`, `workflow_dispatch`, weekly schedule |
| `security.yml` | push and PR to `main`/`master`/`develop`, weekly schedule |
| `deploy.yml` | `workflow_run` — fires only after `ci-cd-pipeline.yml` succeeds on `master` |
| `performance.yml` | `workflow_dispatch` and weekly schedule only — not tied to push or PR |

**Key finding:** CI coverage is identical whether you push directly to `master` or open a PR — every relevant workflow triggers on both events. So in this repo, the choice between push and PR does not change *whether* CI runs.

What it changes is *when* you see the result relative to the change landing:

- **Direct push**: the commit is already on `master` when CI runs. If CI fails, `master` is broken until fixed — and since `deploy.yml` chains off a *successful* `ci-cd-pipeline.yml` run on `master`, a passing-then-later-found-bad state has no gate between "CI green" and "deploy triggers."
- **PR**: CI runs against the side branch first. You see red/green before merging, so `master` stays green and `deploy.yml` never sees a commit that wasn't already validated pre-merge.

### Practical Rule for This Repo

- Direct push: fine for low-risk, easily-revertable changes (docs, chores, non-production config).
- Route through a PR: for anything security/auth-related, or anything likely to reach `master` and trigger `deploy.yml` — so CI results are visible *before* the change is live, not after.

## Commit Batch Size Before Pushing

There is no fixed ideal number of commits per push — it's a tradeoff, not a constant.

| Batch size | Tradeoff |
|---|---|
| Small (1–3 commits) | Minimal blast radius per push; CI failure pinpoints a small diff; less unpushed work at risk if local disk is lost; easy to revert in isolation. |
| Large (10+ commits) | Convenient locally; but CI failure requires bisecting across many commits to find the culprit; more unpushed work at risk; PR reviewers face a large diff instead of a reviewable increment. |

**Rule of thumb:** push at the end of each logical unit of work (one issue/feature/fix — whether that's 1 commit or 5), or after roughly a day has passed, whichever comes first. The commit count in a batch should be a side effect of "one coherent unit of work," not a target chosen up front.

### Case Study (this repo, 2026-07-06)

Local `master` had accumulated 22 unpushed commits, including a security-sensitive change (SEC-15: JWT localStorage → httpOnly cookies, CSRF re-enabled, #282). This was flagged as past a reasonable ceiling: security-relevant fixes benefit from CI/security-scan validation soon after being written, and 22 commits sitting only on local disk with no remote copy and no CI run is unnecessary risk for a change of that sensitivity. The batch was pushed directly (`git push`) since it was solo work headed to `master` with no PR gate in use at the time.

## References

- `.github/workflows/*.yml` — BuildNest's own CI trigger configuration (source of truth for this article's CI claims).
- BuildNest issue #282 — SEC-15, the security change used as the case study above.
