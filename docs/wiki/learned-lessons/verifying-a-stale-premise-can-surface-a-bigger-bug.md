---
title: Verifying a Stale Issue Premise Can Surface a More Serious Bug Than the One Filed
category: process
tags: [github-issues, security, idor, code-review, investigation]
keywords: [stale issue premise, priority miscalibration, IDOR discovery, investigate adjacent code, low priority mislabeled, verify before implementing]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
root_cause: "PasswordResetController.changePassword accepted userId as a client-supplied @RequestParam with no check that it matched the authenticated caller, an IDOR that was only found because verifying a low-priority issue's stale premise required reading the whole method rather than just enough to confirm/refute the stated claim"
impact: high — an unauthenticated ownership check let any authenticated user change another user's password given that user's current password, a genuine IDOR sitting next to what was filed as a cosmetic low-priority issue
related_lessons:
  - docs/wiki/learned-lessons/verify-issue-premises-against-repo-before-implementing.md
---

# Verifying a Stale Issue Premise Can Surface a More Serious Bug Than the One Filed

## Problem

Issue #249 was filed as `priority: low` — "SecurityTab password change sends credentials as URL params instead of request body," with a stated fix: switch the frontend to send a JSON body, because "the backend expects a JSON body."

Checking the actual backend before implementing (per the standing practice of verifying an issue's technical claims — see the related lesson) revealed the premise was wrong: `PasswordResetController.changePassword` used `@RequestParam` for all three fields, so the existing query-param call already matched what the backend expected. Applying the issue's literal fix without a backend change would have broken the endpoint.

Reading that controller method to understand *why* it used `@RequestParam` — the minimum needed to correct the issue's premise — surfaced something the issue never mentioned at all: `userId` was one of those three client-supplied parameters, with no check anywhere that it matched the authenticated caller. Any authenticated user could pass a different `userId` and, given that user's current password, change it. A real IDOR, sitting one line away from the "low priority" cosmetic bug that was actually filed.

## Fix

Stopping at "here's why the issue's stated cause was wrong" would have missed the IDOR entirely — a technically correct but materially incomplete review. The fix that mattered was reading the *whole* method (and the service method it called) rather than just enough of it to explain the premise mismatch, then treating the finding as a scope question for the user rather than silently expanding or silently ignoring it.

## Rule

When verifying a GitHub issue's technical premise against the actual code (per the standing practice), don't stop reading once you've confirmed or refuted the specific claim in the issue. Read the whole function/endpoint/service method you're already looking at — the investigation is already paid for, and adjacent bugs in the same code are cheap to spot while you're there but expensive to discover later as a separate incident. Priority labels on filed issues reflect the filer's understanding *at filing time*, which may predate any real investigation — a `priority: low` frontend cosmetic bug can be standing directly next to a `priority: critical` backend vulnerability that nobody has looked at yet. Don't let the filed label anchor how carefully you read the surrounding code.
