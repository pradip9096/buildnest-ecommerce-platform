---
title: Cross-Reference Version Pointers Form a Mesh, Not a Single Edge
category: process
tags: [sdlc-docs, documentation, traceability, staleness]
keywords: [Related SRS, Related SDD, Related TP, Related RTM, Document Control, cross-reference, version drift, document suite summary]
source_conversations: [BuildNest session 38, 2026-07-17 — user-requested RTM/SRS/SDD/Test-Plan verification pass, PR #478]
last_updated: 2026-07-17
confidence: high
evidence_strength: direct-verification
related_lessons: [rtm-row-level-edits-dont-catch-document-level-aggregate-staleness.md]
root_cause: A per-issue fix to document A's content correctly bumps A's own Version and updates the one sibling cross-reference that issue's own diff happened to care about, but structurally never sweeps the reverse edge or a third document's edge to either — no single issue's scope legitimately includes "check every other document's pointer back to the one I just changed."
impact: medium — no functional/runtime consequence, but multiple formal SDLC documents actively misrepresented which version of their own siblings they were tracing against, for an extended period, despite each document individually being actively and correctly maintained
---

## What happened

BuildNest's 5 SDLC documents (RTM, SRS, SDD, Test Plan, SDP) cross-reference each other's version
numbers in their own Document Control header (`Related SRS: vX.Y`, etc.), and SDP additionally
keeps a full "Document Suite Summary" table (Appendix D) listing all 5 at once. This is an N-way
mesh of version pointers, not a single directional link.

Every prior fix to this mesh (BuildNest sessions 30/32/35/36, tracked under
`[defect-class: sdlc-doc-version-never-bumped]`) correctly updated *the one pointer relevant to
that issue's own change* — e.g. fixing SRS content bumps SRS's own version and updates RTM's
`Related SRS` field to match, since that's the specific edge the issue touched. But nothing ever
swept the reverse or sideways edges: SDD's own `Related SRS` drifted independently of RTM's;
Test Plan's `Related SRS`/`Related SDD` header fields were never touched even once since the
original 2026-06-19 baseline (still read v4.0/v3.0 while the real documents had moved to
v4.5/v3.4); SDP's entire Appendix D table listed every one of its 5 sibling documents at their
*original* baseline version, despite a full day of active, correct version bumps on 4 of those 5
documents that same session.

A user-requested "verify RTM/SRS/SDD/Test Plan against source" pass (not tied to any GitHub
issue) found 4 separate stale pointers across 4 documents in one sweep, despite each individual
document having been actively maintained and version-bumped multiple times that same day.

## Why this is easy to miss

The existing `sdlc-doc-version-never-bumped` defect-class rule already requires bumping *the
edited document's own* Version + Revision History whenever its content changes — but it never
named "and check every other document's pointer back to this one" as part of that requirement.
Each individual fix looked complete in isolation (the specific pointer that issue's own diff
touched was correctly updated) while the mesh as a whole kept drifting through every other edge
nobody happened to be looking at during that pass. The project's own Periodic SDLC Documentation
Sync (every 15 merged issues) also didn't originally name this — its Scope list enumerated only
per-document self-referential aggregate claims (Totals rows, Component Statistics counts, gate
values), never the cross-document mesh itself.

## Fix

When bumping any one SDLC document's Version, don't stop at updating the single cross-reference
the triggering issue happens to care about. Grep all 5 documents for `Related SRS`/`Related SDD`/
`Related TP`/`Related RTM` (and SDP's Appendix D table) and confirm every pointer *to* the
just-bumped document is current, not just the one edge that motivated the fix.

`development-workflow.md`'s Periodic SDLC Documentation Sync section now explicitly names this
cross-reference mesh sweep as part of its Scope, alongside the pre-existing per-document
aggregate-claim checks.

## Generalizes beyond BuildNest

Any project maintaining N documents that reference each other's version numbers has this same
structural gap — fixing edge A→B on the occasion B changes doesn't maintain edge C→A or the
reverse B→A unless something explicitly sweeps the whole mesh, not just the triggering edge.
