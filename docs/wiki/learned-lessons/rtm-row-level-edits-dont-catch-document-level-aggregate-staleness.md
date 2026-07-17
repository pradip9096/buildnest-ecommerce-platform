---
title: "Row-Level RTM Edits Don't Catch Document-Level Aggregate Staleness (Blocked Verdicts, Totals Arithmetic)"
category: process
tags: [rtm, documentation, staleness, traceability, aggregate-facts, arithmetic-consistency]
keywords: [RTM Phase 1 blocked, open defects section stale, coverage summary totals wrong, TIR-01 TIR-04 MNT-03 already fixed, requirements traceability matrix audit]
source_conversations: [Session 2026-07-17, issue #428 retrospective request → #452/#453]
last_updated: 2026-07-17
confidence: high
evidence_strength: strong
root_cause: "development-workflow.md's update-docs step only prompts checking whether an issue's own scope requires an RTM update to the row(s) it touches — nothing prompts a periodic check of the RTM's own document-level aggregate claims (a 'Phase 1 is blocked' verdict, a Totals row's arithmetic) that no single issue's row edit would ever surface or falsify"
impact: high — the RTM's own headline claim ('Phase 1 is blocked', 6 open defects) was false for an unknown but clearly extended period (multiple sessions had already correctly fixed individual rows like FR-ADM-09/PRT-01/DC-06 without ever revisiting this section), and a stale 'blocked' verdict in a document whose stated purpose is compliance evidence is exactly the kind of claim a reader would trust without independently re-deriving it
related_lessons:
  - docs/wiki/learned-lessons/persistent-memory-staleness-drift.md
  - docs/wiki/learned-lessons/verify-issue-premises-against-repo-before-implementing.md
---

# Row-Level RTM Edits Don't Catch Document-Level Aggregate Staleness (Blocked Verdicts, Totals Arithmetic)

## What Happened

Asked to "verify the RTM is up to date with the source code" after closing #428 (a routine, targeted follow-up to a normal `update-docs` check). Instead of just spot-checking the row #428 had touched, a fuller pass found two large-scale staleness problems that had nothing to do with #428 specifically:

1. **§9/§11/§12 ("Open Defects Blocking Phase 1 Exit")** claimed all 4 TIR requirements plus MNT-03 were unresolved defects, with a document-level verdict: *"Phase 1 is blocked."* Direct source inspection showed all 6 listed defects (DEF-001 through DEF-006) were already fixed — `@Tag("e2e")` present, `RoleRepository` mocked, both security-test assertions correct. This verdict had been false since some point well before this session; multiple prior sessions had already correctly fixed individual, unrelated RTM rows (`FR-ADM-09`, `PRT-01`, `DC-06`) without ever revisiting this section.
2. **§3's Coverage Summary "Totals" row didn't even sum to its own category rows** — an internal arithmetic inconsistency (off by 1-2, in ways unrelated to any specific requirement's status) that had nothing to do with any code change at all. It was just never checked.

## Why It Matters

`development-workflow.md`'s `update-docs` step (and its `[defect-class: update-docs-changelog-only]` hardening) asks: *does this issue's own scope require updating the RTM row(s) it touches?* That's a real, necessary check — but it is scoped to the issue at hand. Nothing in that per-issue check ever prompts revisiting the RTM's **document-level** claims: a blocked/unblocked verdict, a Totals row's arithmetic, a section header's blanket assertion ("the frontend is a stub"). These are exactly the kind of fact that no single row-level edit would ever surface or falsify — you only find them by reading the *whole* document occasionally and cross-checking its own internal consistency, not by editing the row your current issue happens to touch.

This is the same category of gap as `definition-of-done.md` item 6's "aggregate fact" verification requirement (README/SDP-level counts going stale while per-item rows get updated correctly) — but that item is scoped to *external* aggregate facts (a README claiming N issues closed). This is the same failure mode occurring **within a single document against itself** (the RTM's own summary table not matching its own category rows, its own defects list not matching its own... status column two sections earlier).

## How It Works / Recognition Cue

The signal that this kind of staleness exists: **a document has both granular, frequently-edited rows (which get properly maintained because they're each issue's specific scope) and coarser, rarely-edited aggregate/summary sections (which no single issue's scope ever touches).** The granular rows drift toward accuracy over time because they're maintained one at a time; the aggregate sections drift toward staleness over the same time, because nothing ever revisits them specifically. The longer a document lives and the more targeted edits it receives, the *larger* this gap gets — each correctly-fixed row is indirect evidence the document is being maintained, which is exactly what makes a stale aggregate claim easy to miss (the document doesn't look neglected).

## How to Apply

- When asked to "verify X document is up to date" (not just "update the row for issue #N"), explicitly check **document-level** claims separately from row-level ones: section headers asserting a blanket state ("stub," "not yet implemented," "blocked"), summary/Totals rows (recompute them independently rather than trusting the stated number), and any narrative verdict that isn't tied to a single row.
- Recompute a Totals/summary row from its own underlying rows directly (e.g., in Python/a script) rather than trusting it was ever correct — it's cheap to verify and catches exactly this class of drift.
- When fixing a document-level claim that's clearly wrong but whose full scope is large (e.g., an entire 30-row frontend section that's stale, not just the summary verdict), don't silently do a shallow "path correction" pass — split it into (a) the narrow, mechanical, already-verified fix (defects that are provably resolved) and (b) a separate follow-up for genuine per-row re-derivation (does each requirement's *actual* behavior match its text, not just does the cited file exist) — conflating the two risks a shallow fix that looks complete but leaves the real gaps (a different technical approach than planned, not just a different file path) invisible.

## Synthesis

Row-level maintenance discipline and document-level consistency are two different properties, and improving one does not improve the other — a document can have every individual row perfectly current while its own summary/verdict about itself is stale, because nothing about editing a row ever exercises the summary. Treat "is this document accurate" and "is this document internally self-consistent" as two separate questions to check, not one.
