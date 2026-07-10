---
title: ".env.example (Committed Template) vs .env (Local Secrets) — Why the Split Exists"
category: tooling
tags: [dotenv, env-example, secrets-management, gitignore, environment-variables, configuration]
keywords: [.env.example, .env, environment variable template, secret leaked in git, gitignore .env, config template vs real values]
objective: "What is the .env / .env.example split for, and how do I avoid putting a real secret in the wrong one?"
audience: "Any contributor configuring local environment variables or adding a new configurable secret to the project"
scope: general
source_conversations: [Session 2026-07-09]
last_updated: 2026-07-09
confidence: high
evidence_strength: strong
related_articles:
  - docs/wiki/learned-lessons/dotenv-not-auto-loaded-by-local-processes.md
status: published
---

# .env.example (Committed Template) vs .env (Local Secrets) — Why the Split Exists

## What Is It?

`.env.example` and `.env` are two files serving the same configuration surface with opposite
git-tracking status. `.env.example` is **committed to version control** — it lists every
environment variable the application uses, with placeholder or blank values for anything secret.
`.env` is **gitignored** — it holds the real values (passwords, API keys, JWT secrets) that a
running process actually reads, and it is created locally by copying the example
(`cp .env.example .env`) and filling it in.

They are not two independent files with similar names; `.env` is meant to be a filled-in copy of
`.env.example`, kept in sync in *shape* (which variables exist) but never in *content* (real
secret values only ever go in the untracked copy).

## Why It Matters

The split exists to solve one specific problem: a project needs to document *what* configuration
it requires so any contributor can set up their environment, without ever committing the *actual*
values of secrets to git history — where they'd be permanently recoverable (via `git log`,
forks, or a public repo) even after being removed from the current file. Deleting a secret from
the current version of a tracked file does not remove it from history; a leaked secret in a
committed file must be treated as compromised and rotated, not just edited away.

`backend/.env.example` in this repo makes the distinction explicit in its own header comment
(`Class: SECRET (never commit) | non-secret` per variable), and BuildNest's own
`.claude/rules/common/security.md` states the same rule at a higher level: "NEVER hardcode
secrets in source code" and "ALWAYS use environment variables." The `.env`/`.env.example` split
is the concrete mechanism that rule points to.

## How It Works

```
backend/.env.example    ← tracked, committed, safe to view in any PR/fork
                           every variable listed; secrets shown as `KEY=` (blank)
                           non-secrets shown with real usable defaults
        │
        │  cp .env.example .env
        ▼
backend/.env            ← gitignored (see backend/.gitignore), local-only
                           secrets filled in with real values
                           read by Docker Compose automatically, and by
                           locally-run processes only if explicitly sourced
                           (see dotenv-not-auto-loaded-by-local-processes.md)
```

`git check-ignore -v backend/.env` confirms the ignore rule is active; there is no equivalent
ignore rule for `.env.example` — and there shouldn't be, since it needs to be visible to anyone
cloning the repo.

Each variable in `.env.example` carries an inline comment documenting its `Type`, `Default`,
`Class` (`SECRET` or `non-secret`), and `Req` (`REQUIRED` or `optional`) — this is what lets a
contributor fill in `.env` correctly without needing to read application source code to discover
what each variable does.

## When to Use It

- **Adding a new configurable secret** (an API key, a new service credential): add a documented,
  **blank** entry to `.env.example` (with the `Class: SECRET` comment), and separately add the
  real value only to your own local `.env`. Never combine these into one edit.
- **Onboarding to the project**: `cp .env.example .env`, then fill in every `REQUIRED` /
  `SECRET` field with real values — the app will fail to start on missing required secrets by
  design (e.g. `JWT_SECRET` has no default).
- **CI/CD**: neither file is used directly — CI reads secrets from the platform's own secret
  store (e.g. a GitHub Actions repository secret set via `gh secret set`), referenced in workflow
  YAML as `${{ secrets.VAR_NAME }}`. `.env.example` documents that this is where the value comes
  from in CI, via a comment, but does not supply it.
- **Before every commit that touches `.env.example`**: re-check the diff for anything that isn't
  a placeholder. A one-line `grep '^SECRET_VAR_NAME=' backend/.env.example` should always show an
  empty value.

## Examples

Real example from this repo (2026-07-09, `source_conversations` above): after generating an NVD
API key for OWASP dependency-check (`pom.xml`'s `nvdApiKey` property, read from
`${env.NVD_API_KEY}`), the real key value was pasted directly into `backend/.env.example` —
**twice**, in the same session, on two separate edits. Both times, `git status` still showed only
`.env.example` as modified (not yet committed), so nothing was actually pushed to GitHub — but
had either edit been committed, the key would have been exposed in the repo's public history and
required rotation. Both times, the fix was the same: blank the value back out in `.env.example`,
and instead append `NVD_API_KEY=<real-value>` directly to the gitignored `backend/.env`
(verified untracked via `git check-ignore -v backend/.env`). The GitHub Actions side was handled
separately and correctly from the start, via `gh secret set NVD_API_KEY --repo <owner>/<repo>`,
which stores the value in GitHub's own encrypted secret store — never touching either `.env`
file.

The recurrence (same mistake, twice, same session) is itself the signal that the two files' names
are easy to confuse under time pressure — `.env.example` reads, at a glance, like "the file where
the example/real value for this variable goes," when it actually means the opposite: "the
template that must never contain one."

## Synthesis

The `.env`/`.env.example` split is a narrow, mechanical answer to a broad problem — how do you
document required configuration without ever making a secret's real value part of a project's
permanent, forkable history? The convention only works if the boundary is respected in both
directions: `.env.example` must stay free of real values (or every future clone/fork leaks
whatever's currently pasted there), and `.env` must stay out of git (which `.gitignore` already
enforces mechanically). The naming similarity between the two files is the main practical risk —
worth a deliberate double-check (`grep` for a blank value) before committing any change to the
example file, rather than trusting visual inspection alone under time pressure.

## Quick Reference

| Question | Answer |
|---|---|
| Which file is committed to git? | `.env.example` only |
| Which file has real secret values? | `.env` only |
| Where does CI get secrets from? | Platform secret store (e.g. `gh secret set`), not either file |
| How do I create my local `.env`? | `cp .env.example .env`, then fill in real values |
| How do I add a new secret variable? | Blank/documented entry in `.env.example` + real value in local `.env` — never the same edit |
| How do I check `.env.example` is safe to commit? | `grep '^VAR_NAME=' backend/.env.example` should show an empty value |
| What if a real secret was committed to `.env.example`? | Treat it as compromised — rotate it — deleting it from the file does not remove it from git history |

## Related Articles

- [.env Files Are Not Auto-Loaded by Locally-Run Processes](../../wiki/learned-lessons/dotenv-not-auto-loaded-by-local-processes.md) — the companion loading-mechanism question (this article covers *which file holds real values*; that one covers *which processes actually read `.env` and how*)
