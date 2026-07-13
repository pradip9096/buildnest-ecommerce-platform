---
title: "`.claude/` Is Gitignored — Rule Files There Are Local-Only, Not Version-Controlled"
category: process
tags: [claude-code, gitignore, dotclaude, version-control, tooling-config]
keywords: [.claude gitignored, rule files not committed, git check-ignore, git ls-files .claude, local-only config]
source_conversations: [Session 2026-07-07]
last_updated: 2026-07-07
confidence: high
evidence_strength: strong
root_cause: "the system prompt's framing of project .claude/ files as \"checked into the codebase\" was trusted without verifying against this repo's own .gitignore, which explicitly excludes .claude/ under a comment marking it local-only"
impact: medium — an entire session's rule-file work (development-workflow.md, git-workflow.md amendments) was built on the false assumption it was shared/durable; no data loss occurred since the files still existed locally, but the assumption was silently wrong until directly checked
related_lessons: []
---

# `.claude/` is gitignored — rule files there are local-only, not version-controlled

An entire session was spent building out `.claude/rules/common/development-workflow.md` (necessity
tags, severity-based step merging, amendment log) and amending `git-workflow.md`, operating the
whole time on the assumption that these were durable, shared project files — matching how the
system prompt itself describes project `.claude/` files as "project instructions, checked into the
codebase."

**Why this was wrong:** `git status` never showed these files as modified/untracked, which should
have been the tell. Checking directly (`git check-ignore -v`, `git ls-files .claude/`) confirmed
`.gitignore` excludes `.claude/` entirely, under a section explicitly commented "AI agent
directories and system prompt files ... local use only, not committed." Every file under
`.claude/rules/` — not just the two touched this session, all of them (`jpa.md`, `liquibase.md`,
`spring-security.md`, etc.) — exists only on this local machine. A fresh clone of this repo, or a
teammate, would never see any of it.

**How to apply:** Before treating any `.claude/`-adjacent file as durable/shareable project
config, verify it's actually tracked (`git ls-files <path>` returns something, or `git status`
would show edits to it) rather than assuming from the system prompt's framing or from the file's
own location inside the repo directory tree. "Lives inside the project folder" is not the same
claim as "is version-controlled" — this repo's own `.gitignore` deliberately separates the two for
this exact directory. If a `.claude/rules/` file accumulates genuinely durable, team-relevant
decisions (as `development-workflow.md` now does), that's worth surfacing to the user as a
decision point — keep it local-only by design, or carve out a tracked exception — rather than
silently assuming either answer.
