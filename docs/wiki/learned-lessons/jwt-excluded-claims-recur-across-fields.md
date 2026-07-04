---
title: "The Same JWT-Claim Bug Recurs Per Field — Audit All Consumers, Not Just the Reported One"
category: process
tags: [jwt, spring-security, auth-context, root-cause-recurrence]
keywords: [user.id resolves to 0, user.roles always empty, JWT does not carry claim, decode JWT payload anti-pattern, AuthContext tokenToUser tokenToRoles]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
related_lessons: []
---

# The Same JWT-Claim Bug Recurs Per Field — Audit All Consumers, Not Just the Reported One

## Problem

Two separate issues, filed and fixed independently, turned out to be the identical defect on two different fields:

- **#280**: `AuthContext.tokenToUser()` derived `user.id` by decoding the JWT payload (`payload['id'] ?? payload['userId'] ?? 0`). This project's own documented JWT design (`spring-security.md`) deliberately excludes `id` from the token — only `sub`/`iat`/`exp` are present. Result: `user.id` was always `0`.
- **#292** (found *while fixing #280*, filed separately, fixed in a later pass): the same file's `tokenToRoles()` did the identical thing for `user.roles`, decoding `payload['roles'] ?? payload['authorities'] ?? []` — also always empty, for the same reason. This one silently broke the entire admin dashboard (`user.roles.includes('ADMIN')` always false), a more severe symptom than #280's, sitting undetected because there was no admin account in the dev database to notice it with.

Both bugs lived in the same function-adjacent code, in the same file, discovered in the same session, three tasks apart — and were treated as two unrelated tickets until the second one was found by inspection while fixing the first.

## The generalizable pattern

When a bug's root cause is "code assumes a data source carries information it structurally does not" (here: the JWT payload), that root cause is not specific to the one field named in the bug report. **Every other field decoded from the same source is equally suspect**, and should be checked in the same pass — not left for a future bug report to rediscover independently.

## What would have caught this sooner

After fixing #280, a two-minute audit of `AuthContext.tsx` for every other `decodeJwtPayload(...)` call site (there was exactly one more — `tokenToRoles`) would have caught #292 immediately, before it needed its own separate investigation, GitHub issue, and fix cycle. The audit is cheap: grep the file (or the whole codebase) for the helper function's other call sites, and for any other `payload['...']` access pattern, the moment the first instance is confirmed to be a real bug.

## Applies beyond JWTs

The same principle generalizes to any "this data source doesn't actually contain X" root cause — a DTO missing a field, an API response shape assumption, a cache key pattern. The specific lesson here is JWT claims, but the checklist item is generic: **once you've proven a source doesn't carry claim A, check whether anything else in the same consumer reads claim B, C, ... from that same source before considering the bug class closed.**
