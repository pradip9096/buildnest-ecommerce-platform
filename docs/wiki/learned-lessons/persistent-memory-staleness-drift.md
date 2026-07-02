---
title: Persistent Memory Silently Drifts Stale Across Milestone/Issue Boundaries
category: process
tags: [claude-code, memory, staleness, project-state, milestones, github-issues]
keywords: [project_state.md, github_setup.md, stale memory, memory review, milestone completion, issue count drift]
source_conversations: [Session 2026-07-02]
last_updated: 2026-07-02
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/github-issue-hygiene.md
---

# Persistent Memory Silently Drifts Stale Across Milestone/Issue Boundaries

## Problem

Claude Code's persistent per-project memory (`~/.claude/projects/<slug>/memory/`) is
written as a point-in-time snapshot, but nothing forces it to be refreshed when the
underlying state changes. Numeric facts — issue counts, milestone status, "remaining
work" lists — decay the moment the tracked reality moves on, and there is no automatic
invalidation.

On review of BuildNest's memory directory (2026-07-02), two files had drifted
independently, each in a different way:

- **`github_setup.md`** still listed M1 (8 open), M2 (8 open), and M3 (6 open) issues as
  active work. All three milestones had actually been **completed and tagged**
  (`v0.2.0`, `v0.3.0`, `v0.4.0`) well before this memory was last touched. The file's
  "total open issues: 99" was also stale — actual was 68.
- **`project_state.md`** listed issues **#271 and #272 as "REMAINING (next session
  starts here)"** — but both had been closed in the *same session* that produced the
  memory review, because the memory write predated the closure action later in that
  conversation.

Neither error was caught until an explicit request to "review files ... for staleness"
triggered cross-checking every numeric claim against live `gh issue list` /
`gh issue view` output.

## Why This Is Non-Obvious

Stale memory doesn't fail loudly. Unlike a broken build or a failing test, a wrong
issue count or a "still open" label on a closed issue produces no error — it just
quietly misinforms whatever decision comes next (e.g., "what should I work on next"
answered from a snapshot that's months out of date). The risk compounds because memory
is *trusted by design* — it exists specifically so Claude doesn't have to re-derive
context every session, which means there's an inherent temptation to treat it as live
truth rather than a cached observation.

## Root Causes

1. **No automatic trigger to refresh memory on milestone/issue closure.** Closing an
   issue via `gh issue close` does not touch memory at all — the two systems are
   completely decoupled unless a human or the agent explicitly connects them.
2. **Memory can be written mid-session, before the session's own actions complete.**
   If a memory update happens before a later action in the same conversation (e.g.,
   closing #271/#272), the memory is stale from the moment it's written, not just over
   time.
3. **No periodic staleness check is built into the workflow.** Nothing prompts a
   review; it only happens when explicitly requested.

## Mitigation

- **Verify before trusting.** Any memory file carrying a specific number, issue status,
  or file:line citation should be treated as a claim to verify, not a fact to act on —
  especially for project-state-type memories. Every memory file returned by the Read
  tool already carries a `<system-reminder>` noting its age and this exact caveat; the
  fix is to actually act on that reminder rather than reading past it.
- **Verify at the point of a factual claim, not just at read time.** If a memory says
  "42 issues open," running `gh issue list --state open | wc -l` (or the `gh` JSON
  equivalent) costs one tool call and eliminates the risk of acting on stale data.
- **Update memory as the last step of a session, not mid-session.** If a memory write
  happens before later actions in the same conversation change the state it describes,
  it's stale on arrival. Snapshot memory writes should happen after the session's
  mutating actions are done, or be explicitly re-verified before the session ends.
- **Periodic staleness reviews are worth doing proactively**, not only on request —
  particularly for memory files typed `project` (inherently time-bound) or `reference`
  (numeric facts about external systems), since those are the types most prone to drift.
  `user` and `feedback` (behavioral) memories are comparatively immune — they don't
  encode facts that expire the same way.

## Rule

Treat memory of type `project` or `reference` as a **cached observation with a known
age**, not live state. Before citing a specific count, status, or fact from memory in a
response the user will act on, cross-check it against the authoritative source (`git
log`, `gh issue list`, a fresh build) if the cost of being wrong is non-trivial.
