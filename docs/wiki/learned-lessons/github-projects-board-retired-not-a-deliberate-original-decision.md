---
title: GitHub Projects Board Was Retired by Silent Drift, Not a Deliberate Original Decision
category: process
tags: [github-projects, process-documentation, workflow-drift, milestones]
keywords: [github project board unused, project #9, deprecated project board, why not use github projects]
source_conversations: [Session 2026-07-09]
last_updated: 2026-07-09
confidence: high
evidence_strength: strong
related_lessons: []
---

# GitHub Projects Board Was Retired by Silent Drift, Not a Deliberate Original Decision

## Problem

`development-workflow.md` (written 2026-07-07) stated that GitHub Projects are "not currently
used in this repo — issues + milestones are the tracking mechanism," phrased as if this were a
standing design decision. Asked directly why the repo "deliberately" avoids Projects, there was
no recorded rationale to point to.

Checking the actual project board (`gh project item-list 9 --owner pradip9096`) showed 232 items,
with every issue **#1 through #292** present and every issue **#293 onward** (40+ issues,
including #329/#330/#332) missing. Issue #292 was created 2026-07-04T06:07 and #293 at
2026-07-04T12:46 — same day. The board was actively maintained right up to that point, then
stopped cold with no issue, commit, or note explaining why, and was never resumed. The
workflow-file's "not currently used" line was written three days *after* that drop-off — accurate
as a present-tense snapshot, but worded in a way that implied an intentional original policy that
never actually existed.

## Root Cause

A supplementary tracking surface (a board mirroring information already present in
issues+labels+milestones) has no forcing function keeping it in sync — nothing breaks if it stops
being updated, and no error surfaces to signal the drift. It silently diverges from reality until
someone happens to check it, at which point the honest history has often already been lost from
memory/session context and gets reconstructed as "we decided not to use this," when what actually
happened was "no one updated it for one session and it was never picked back up."

## Rule

- When a process file states that a tool/practice is "not used" or "not currently applicable,"
  don't take that as evidence the choice was deliberate — check whether it was ever used and, if
  so, when and why it stopped. A drop-off with no accompanying rationale is drift, not policy.
- Before recommending a secondary tracking surface (a board, a dashboard, a duplicate log) that
  mirrors data already captured elsewhere (milestones, labels), weigh whether it will realistically
  be kept in sync without a forcing function — this repo's own history is direct evidence that,
  absent one, it silently stops.
- Retiring a secondary surface once discovered unused should be an explicit act (mark it
  deprecated in the process doc, state why, avoid resuming it reflexively) rather than either
  silently reviving it or leaving ambiguous "optional/unused" phrasing that erases the real history
  for the next person who asks "why."
