---
title: Apply Every Applicable domain: Label, Not Just the Most Obvious One
category: process
tags: [github, issues, labels, taxonomy, queries]
keywords: [domain label, multi-label, filtered issue view, missing from query, domain frontend, domain auth]
source_conversations: [Session 2026-07-04]
last_updated: 2026-07-04
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/github-issue-hygiene.md
---

# Apply Every Applicable `domain:` Label, Not Just the Most Obvious One

## Problem

Issue #296 ("implement silent token refresh — `apiRefresh()` is never called") was filed with only `domain: auth`, because the *concern* is authentication. But the actual change lives entirely in frontend files (`AuthContext.tsx`, `api/auth.ts`) — it never touches the backend. When the user asked for a priority-ordered list of "open GitHub issues related to the frontend," a query filtered on `--label "domain: frontend"` silently omitted #296, even though it was frontend work sitting in the same batch of issues just filed moments earlier in the same session.

The root cause: domain labels in this repo (`domain: auth`, `domain: frontend`, `domain: product`, etc.) are not mutually exclusive categories — they describe *concerns*, and a single piece of work can sit at the intersection of two (auth logic implemented in frontend code, a product feature requiring a backend migration, etc.). Treating them as a single-select dropdown when filing an issue causes it to silently vanish from any filtered view that expects the other label.

## Rule

When filing or triaging an issue, ask "what layer does the code change actually live in?" separately from "what concern is this about?" — apply labels for both if they differ. A frontend file implementing auth logic gets both `domain: auth` and `domain: frontend`. A backend migration required for a frontend feature gets both `domain: product` (or whatever the feature domain is) and nothing frontend, since the code change is backend — but the reverse (frontend code for a backend-named concern) needs both.

Before closing out a batch of newly-filed issues, re-run the filtered view you expect stakeholders to use (e.g., `gh issue list --label "domain: frontend"`) and confirm every issue you just filed that touches that codebase actually appears in it — don't just trust the label you picked at filing time.
