---
title: A Backend-Only Sub-Issue Chain Can Leave an Entity Relationship Unexposed via Any DTO Until the Linked Frontend Issue Hits It
category: process
tags: [dto-design, cross-layer, sub-issues, api-contract]
keywords: [OrderGroup, OrderResponseDTO, orderGroupId, FR-SEL-06, DTO field exposure, cross-layer verification]
source_conversations: ["#581"]
last_updated: 2026-07-26
confidence: medium
evidence_strength: single-occurrence, judged non-obvious enough to document
related_lessons: []
root_cause: A multi-sub-issue feature (#578/#579/#580) built the schema, service logic, and API endpoints for a new entity relationship (Order.orderGroup), and each sub-issue's own acceptance criteria were satisfied without any of them needing to expose that relationship through a response DTO — only the deliberately-deferred, separately-filed frontend issue actually required reading it.
impact: medium — not a defect (nothing shipped broken), but a day-one design gap that only surfaces once implementation starts on the dependent issue, after the "backend complete" milestone has already been recorded (RTM marked FR-SEL-06 ✅ Implemented before the field existed anywhere in a DTO)
---

## What happened

FR-SEL-06 (seller-scoped order management) was deliberately split into three backend
sub-issues (#578 schema, #579 checkout-split logic, #580 seller-scoped API) plus one
linked frontend follow-up (#581), filed at the same time per `verify-implementation-readiness`'s
cross-layer coverage check. Each backend sub-issue added and tested `Order.orderGroup`
(the JPA relationship) correctly — but none of them had a reason to add `orderGroupId` to
`OrderResponseDTO`, since none of their own acceptance criteria required a caller to read it.
RTM's FR-SEL-06 row was marked `✅ Implemented` after #580 closed, describing the requirement
as backend-complete.

Only when #581 (the frontend issue, explicitly scoped as "once backend sub-orders exist")
started implementation did it become clear the backend didn't actually expose the one field
the whole frontend feature depends on. This was caught immediately (via
`verify-implementation-readiness`'s cross-layer check reading the actual DTO/entity before
building UI against it) and folded into #581 as a small additive change — but it could just
as easily have been discovered only after the frontend was half-built, or missed until a
live QA pass.

## Why this is worth documenting

The gap wasn't a coding mistake in any of #578/#579/#580 — each did exactly what its own
acceptance criteria asked. The failure mode is structural: **splitting a feature into
backend-first sub-issues, with a "frontend once backend exists" issue filed separately,
creates an implicit assumption that the backend fully anticipates what the frontend will
need to read — but nothing in the backend sub-issues' own acceptance criteria forces that
check**, since a DTO field with no current reader has no test that would fail for its
absence.

## How to apply

When splitting a feature into backend-sub-issues + a linked frontend issue:
- At the point the *last* backend sub-issue closes (or RTM is marked `✅ Implemented`),
  explicitly ask: "does every DTO a frontend caller will need already expose every field the
  linked frontend issue's acceptance criteria implies?" — not just "does the entity/schema
  support it."
- If the linked frontend issue's body describes a UI concept (e.g. "one purchase, N
  shipments") that requires reading a relationship, grep the actual response DTO for that
  field *before* closing the backend chain, not after the frontend issue starts.
- If found late (as here), the fix is usually small and additive (a nullable field, mapped
  in the existing `mapToResponseDTO`-style method) — treat it as the same-concern case in
  Mid-Implementation Scope Discovery, not a reason to reopen the closed backend issues.
