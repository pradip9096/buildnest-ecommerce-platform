---
title: Claude Code Memory Directory — Structure and Customization
category: tooling
tags: [claude-code, memory, persistence, feedback, claude-md]
keywords: [memory directory, feedback memory, user memory, project memory, reference memory, MEMORY.md]
objective: What is in Claude Code's persistent memory directory for this project, and how can the user shape it?
audience: BuildNest maintainer working with Claude Code across sessions
scope: general (applies to any Claude Code project; examples drawn from BuildNest)
source_conversations: [Session 2026-07-02]
last_updated: 2026-07-02
confidence: high
evidence_strength: strong
related_articles: [claude-code-progressive-disclosure.md]
status: published
---

# Claude Code Memory Directory — Structure and Customization

---

## Overview

Claude Code maintains a persistent, file-based memory directory per project:
`~/.claude/projects/<project-slug>/memory/`. It survives across conversations and lets
Claude build up context about the user, project, and working style over time, rather
than re-deriving it every session.

For BuildNest, this directory currently contains 7 files.

---

## The Memory Types

Claude Code recognizes four memory *types*, each with a distinct purpose:

| Type | Purpose | Example |
|---|---|---|
| `user` | Role, goals, responsibilities, knowledge of the person Claude is working with | Experience level, preferred communication style |
| `feedback` | Dos and don'ts explicitly corrected or confirmed by the user — guides future behavior | "Run Maven from `backend/`, not repo root" |
| `project` | Ongoing work, goals, decisions not derivable from code/git history | Current milestone, in-flight initiatives |
| `reference` | Pointers to where information lives in external systems | GitHub token scopes, project board URL |

These types are fixed by the memory system's design — Claude does not invent new types.

---

## Current Files (BuildNest)

| File | Type | Purpose |
|---|---|---|
| `MEMORY.md` | index (not a memory) | One-line pointers to every other file; always loaded into context automatically |
| `user_profile.md` | `user` | Role, experience level, working style — shapes tone and level of explanation |
| `feedback.md` | `feedback` | General dos/don'ts corrected or confirmed across sessions, each with a **Why** so edge cases can be judged rather than blindly followed |
| `feedback_failure_response.md` | `feedback` | A narrower, standing protocol split out from `feedback.md`: never weaken assertions or apply workarounds on test/build failure — always fix root cause |
| `project_state.md` | `project` | Snapshot of current milestone, open issue count, build/test status — the file most likely to go stale, so it should be verified against live state before being trusted |
| `github_setup.md` | `reference` | Repo name, token scopes/gotchas, project board number, labels, milestones |
| `engineering_standards.md` | `project` (mirrors CLAUDE.md) | Non-negotiable quality bar, reinforced as memory so it persists even through context summarization |

**Structural vs. organic:** the *shape* (an index file + typed memory files) is fixed by
the system. The *number and topics* of files grew organically, session by session, as
relevant information came up — nothing was scaffolded upfront. Splitting
`feedback_failure_response.md` out of `feedback.md` is an example: it became a standing
protocol worth its own file rather than one entry in a general list.

---

## How to Shape These Files

### 1. Explicit instruction (most reliable)
- *"Remember that..."* → saved immediately under whichever type fits.
- *"Forget that..."* → the entry is found and removed.

### 2. Correction or confirmation (implicit)
- Correcting Claude's approach ("don't do X", "do it this way instead") gets logged as
  a **Don't** in `feedback.md`.
- Confirming an unusual choice without pushback, or an explicit "yes, keep doing that,"
  gets logged as a **Do**. This is easy for Claude to miss silently — saying it out loud
  helps it register.

### 3. Direct file editing
The files are plain markdown with YAML frontmatter under
`~/.claude/projects/<project-slug>/memory/`. Nothing stops manual editing or deletion;
Claude picks up the changes next session.

### 4. `CLAUDE.md` vs. memory
`CLAUDE.md` (global or project) is loaded verbatim every session as **binding
instruction**. Memory is more like **advisory observation** — "things learned about
working with this user/project." For durable rules that must always apply, `CLAUDE.md`
is the stronger tool; for softer preferences and history, memory is more appropriate.

### 5. Ask for reorganization
Requests like "split this file by topic" or "this snapshot is stale, refresh it" are
valid — Claude can restructure memory files on request, not just append to them.

### What Claude deliberately does *not* save
Even on request, some content is excluded because it duplicates a better source of
truth and goes stale quickly:
- Code patterns, conventions, architecture, file paths — derivable by reading the repo
- Git history / who-changed-what — `git log` / `git blame` are authoritative
- Debugging solutions or fix recipes — the fix lives in the code/commit, not memory
- Anything already documented in `CLAUDE.md`
- Ephemeral, single-conversation task state

---

## When Memory Is Applied vs. Written

These happen on different cadences — worth distinguishing clearly:

### Applying existing memory — every turn, automatically
`MEMORY.md` and its linked files are loaded into context at conversation start. By the
time Claude responds to any given turn, `feedback.md`'s dos/don'ts and `user_profile.md`'s
working-style notes are already available — Claude is expected to act consistently with
them without being reminded each time. This is why, for example, Claude runs
`gh issue close` directly rather than telling the user to do it manually: that's
`feedback.md`'s "take action directly" entry being applied without being invoked.

### Writing new memory — reactive, not scheduled
This is **not** a per-turn scan. New memories are written when a specific signal occurs:

- The user explicitly says "remember this" / "forget this"
- The user **corrects** Claude's approach → logged as a **Don't** in `feedback.md`
- The user **confirms** an unusual choice, especially quietly (accepting something
  without pushback) → logged as a **Do**. This signal is easy to miss — saying it out
  loud ("good, keep doing that") makes it far more likely to register.
- Claude learns a durable project fact, user fact, or reference pointer not already
  captured

Claude does not scan every message for memory-worthy content — that would add noise
faster than value. Writing happens at natural checkpoints (task completion, the user's
reaction to something just done), not on a fixed interval. There is no built-in
enforcement mechanism for this — it depends entirely on Claude noticing the signal in
the moment during the live conversation. No existing Claude Code feature (hooks or
`/loop`) is wired to memory-writing today:

- **Hooks** fire deterministic shell commands on defined events (tool calls, session
  start/stop). They cannot perform the judgment call of "was this worth remembering,"
  so they are not a viable mechanism for enforcing memory capture as-is.
- **`/loop`** re-runs a prompt or command on an interval for repeatable *tasks* (e.g.,
  "check the deploy every 5 minutes"). It is not connected to memory-writing.

A hypothetical stronger guarantee — e.g., a periodic `/loop` invocation prompting
"review this session and write any memory entries that were missed" — is a workaround
one could set up manually. It is not a documented or existing capability; treat it as a
speculative idea, not a feature to rely on.

### Confirming a "remember" request actually gets stored
When the user says "remember X," Claude saves it via the two-step process — write the
typed memory file, then add a one-line pointer to `MEMORY.md` — checking first whether
an existing file already covers the topic, to avoid duplicating entries. The exclusion
list (see above) still applies even to explicit requests: Claude will decline to store
code patterns, git history, or debugging recipes even if asked, because those are
better sourced live from the repository and would go stale in memory.

---

## Knowledge Base vs. Learned Lessons — Where New Information Belongs

BuildNest has two separate persistent-knowledge trees, easy to conflate:

| | `docs/knowledge-base/` | `docs/wiki/learned-lessons/` |
|---|---|---|
| **Trigger** | Conceptual/reference question — "how does X work?" | A failure occurred — something broke and was root-caused |
| **Content** | Durable, reusable, no incident required | Non-obvious or high-cost failure mode + fix |
| **Example** | This article; progressive disclosure; hooks reference | `.env` not auto-loaded by local processes; PIT mutation survival patterns |
| **Audience** | Team members learning how a system/tool works | Team members hitting the same failure mode again |

A conversation that explains a concept (like "what is Claude's memory directory and how
do I shape it?") belongs in the knowledge base — nothing failed, so it fails the
learned-lessons bar of "non-obvious or high-cost failure." Conversely, a debugging
session that traces a bug to root cause (like the `.env` sourcing issue) belongs in
learned-lessons, not the knowledge base. Do not create a learned-lessons entry for a
purely explanatory conversation just because it happened in the same session as a bug fix.

---

## Transferring Memory Between Projects

Memory is scoped per-project (`~/.claude/projects/<project-slug>/memory/`), and there is
**no built-in export/import or cross-project sync feature**. Transfer between projects
is manual, and requires judgment about what's actually portable — a straight file copy
is usually wrong because most files mix portable and non-portable content.

### Portability by memory type

| Memory type | Portable across projects? | Why |
|---|---|---|
| `user` (e.g. `user_profile.md`) | **Yes, mostly** | Facts about the person — role, working style, communication preferences — don't change per-project |
| `feedback` (behavioral) | **Partially** | General rules ("take action directly instead of telling me to do it manually") apply everywhere; project-specific rules ("run Maven from `backend/`") would be actively wrong elsewhere |
| `project` | **No** | Specific to that codebase's current state by definition |
| `reference` | **No** | Points at that project's external systems (its repo, its tokens) |

### How to do it

1. **Manual copy + edit, not a straight copy.** Copy the relevant file(s) into the
   target project's memory directory, then strip anything specific to the source
   project. A mixed file like `feedback.md` typically needs splitting — portable
   general-behavior entries kept, project-specific entries dropped — not copied whole.
2. **Prefer promoting truly universal preferences to global `CLAUDE.md`
   (`~/.claude/CLAUDE.md`) instead of duplicating per-project memory.** Anything true
   across *all* projects (e.g., "no co-author lines in commits") belongs in one place
   that's loaded as binding instruction everywhere, rather than copy-pasted into every
   project's memory directory where it can drift out of sync.
3. **Update the target project's `MEMORY.md`.** A copied file with no index pointer
   won't be loaded — the index entry has to be added manually too.

There is no automated tooling for this today; it is manual file management, best
combined with a review pass to re-scope content rather than a blind copy.

---

## Practical Notes

- Memory files are **fully loaded into context every session** (see
  `claude-code-progressive-disclosure.md`) — unlike skills/agents which load lazily.
  Keep entries concise; every line is a recurring token cost.
- `MEMORY.md` entries are capped effectively at ~150 characters each and truncated past
  line 200 — it must stay an index, not a content dump.
- Point-in-time memories (like `project_state.md`) carry an implicit staleness risk.
  Before acting on a memory that names a specific fact, file, or number, verify it
  against current state (`git log`, `git status`, a fresh build) rather than trusting
  it as live truth.

---

## See Also

- `docs/knowledge-base/project/claude-code-progressive-disclosure.md` — token cost of memory files vs. other components
- `docs/wiki/learned-lessons/` — repo-wide technical/process lessons (distinct from Claude's private memory; checked into git, team-facing)
