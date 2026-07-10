---
title: "Check In-Flight Sibling PRs Before Filing a 'New' Finding as a Separate Issue"
category: process
tags: [github-issues, duplicate-work, ci-investigation, owasp, cve]
keywords: [duplicate issue, sibling PR, in-flight work, CVE gate failure, root cause investigation]
source_conversations: ["#335", "#337", "#338", "#341"]
last_updated: 2026-07-10
confidence: high
evidence_strength: direct-repo-verification
related_lessons: [verify-issue-premises-against-repo-before-implementing]
---

## What happened

While investigating a stuck NVD API key warning across three parallel PRs (#335, #337, #338),
fixing the key surfaced a real, previously-hidden CI failure: `dependency-check-maven`'s
`failBuildOnCVSS=7` gate failing on 16 dependencies (several CVSS 9.0+, one at 10.0). This was
filed immediately as a new Critical issue (#341) with a full findings table.

Only afterward — while doing the final diff self-review before merging #335 — did it become clear
that #335 (already open, already in this same investigation's working set) was the actual fix for
exactly this problem: real dependency version bumps, 5 individually-verified suppression entries,
and a fixed suppression-wiring bug, closing #332. The CVSS list in #341 was line-for-line the same
set #335 already resolved. #341 was filed without first checking whether any of the three PRs
already in flight addressed it.

## Why it matters

The three PRs (#335/#337/#338) were being tracked together in the same session specifically
because they were running in parallel. That context should have been the first thing checked once
a new failure surfaced mid-investigation — not an afterthought during merge review. Filing a
duplicate issue isn't just wasted issue-tracker noise: it also means the "critical, unresolved"
framing was wrong for the several hours between filing and discovering the fix already existed,
which could have prompted urgency (rushed patching, escalation) that wasn't warranted.

## How to apply

Before filing a new issue for a finding discovered mid-investigation, check the other branches/PRs
already active in the same working set — `gh pr list`, or specifically `gh pr diff <n>` /
`gh pr view <n> --json body` on any sibling PR whose scope could plausibly overlap (same
dependency tree, same subsystem, same CVE family) — before writing up the finding as new. This is
a specific case of the more general rule in
[Verify Issue Premises Against Repo Before Implementing](verify-issue-premises-against-repo-before-implementing.md):
that lesson is about checking the *current repo state*; this one is about checking *other in-flight
work* the same session already has open, which a plain repo-state check won't surface since the
fix isn't merged yet. If a fix is in flight but unmerged, note that in the new issue (or don't file
one at all) rather than treating "not yet in the codebase" as "not yet addressed."
