---
title: A Real Secret Was Pasted Into .env.example (Committed) Instead of .env (Gitignored) — Twice
category: process
tags: [dotenv, env-example, secrets-management, gitignore, security]
keywords: [.env.example real secret, NVD_API_KEY leaked, secret in committed file, .env vs .env.example]
source_conversations: [Session 2026-07-09]
last_updated: 2026-07-09
confidence: high
evidence_strength: strong
root_cause: "the near-identical filenames .env.example and .env invite confusion under time pressure, with nothing in the manual-paste editing workflow flagging that a real value was written into the tracked template rather than the gitignored file — it recurred a second time in the same session after the first fix"
impact: high — a real API key was staged into a committed, tracked file twice; no actual exposure occurred only because it was caught before being pushed, and a committed instance would have required rotation
related_lessons:
  - docs/wiki/learned-lessons/dotenv-not-auto-loaded-by-local-processes.md
---

# A Real Secret Was Pasted Into .env.example (Committed) Instead of .env (Gitignored) — Twice

## Problem

After generating a real NVD API key for OWASP dependency-check, the actual key value was pasted
directly into `backend/.env.example` — a tracked, committed file — instead of the gitignored
`backend/.env`. This happened twice in the same session: the first time was caught and fixed
(blanked the value, moved it to `.env`), but the value reappeared in `.env.example` shortly after,
requiring a second fix.

Both times, `git status` still showed the change as uncommitted, so the key was never actually
pushed to GitHub — no real exposure occurred. But had either edit been committed, the key would
have been permanently recoverable from git history even after being removed from a later commit,
requiring rotation.

## Root Cause

The two filenames are easy to confuse under time pressure: `.env.example` reads, at a glance, like
"the file where an example/real value for this variable goes." Its actual purpose is the opposite
— it's the template that must **never** contain a real value; only the untracked `.env` should.
Nothing in the editing workflow itself (manually pasting a value into a file open in an IDE)
flags which of the two nearly-identically-named files is the wrong target.

## Rule

- Before committing any change to `.env.example`, verify every variable that should be a
  placeholder actually is: `grep '^VAR_NAME=' backend/.env.example` should print an empty value.
  Do this explicitly, not by visual scan — the file has 200+ lines and a one-line diff is easy to
  miss on review.
- The real value for any secret belongs only in `backend/.env` (gitignored — confirm with
  `git check-ignore -v backend/.env` if in doubt) or the relevant CI secret store (e.g.
  `gh secret set VAR_NAME --repo <owner>/<repo>`), never in the tracked example file.
- If a real secret is ever found committed (not just staged) to `.env.example` or any other
  tracked file, treat it as compromised and rotate it — removing it from a later commit does not
  remove it from git history.
- See [.env.example (Committed Template) vs .env (Local Secrets) — Why the Split Exists](../../knowledge-base/project/env-example-template-vs-env-local-secrets.md)
  for the full mechanism this lesson is a specific instance of.
