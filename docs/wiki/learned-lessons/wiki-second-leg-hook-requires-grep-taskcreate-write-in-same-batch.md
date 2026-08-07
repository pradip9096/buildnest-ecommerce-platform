---
title: The wiki-second-leg Recurrence-Scan Hook Requires Grep+TaskCreate in the Same Tool-Call Batch as the Write, Not Merely a Recent Prior One
category: process
tags: [hooks, work-on-issue, recurrence-scan, wiki-lessons, task-create]
keywords: [wiki-second-leg-not-regrepped, PreToolUse hook, Write blocked, TaskCreate before Write, same batch, tool-call batch]
source_conversations: [#88]
last_updated: 2026-08-07
confidence: high
evidence_strength: strong
related_lessons: []
root_cause: The `[defect-class: wiki-second-leg-not-regrepped]` hook that gates new gate-governed-file `Write` calls checks for a qualifying `TaskCreate` (wiki-lessons re-grep) as part of the *same* tool-call batch that contains the `Write` — a `TaskCreate` satisfying the same requirement made in an earlier turn, even seconds before, does not count, and does not carry over across separate `Write` calls even within one batch.
impact: medium — repeated silently-failing attempts waste turns; the correct pattern (grep, TaskCreate, Write, once per file, all three calls in one message) must be repeated for every single new file, not amortized across a batch of several new files
---

# The wiki-second-leg Recurrence-Scan Hook Requires Grep+TaskCreate in the Same Tool-Call Batch as the Write, Not Merely a Recent Prior One

## What happened

While implementing #88 (order return/refund flow), several new CheckStyle-governed Java files
needed to be created. `development-workflow.md`'s Proactive Recurrence Scan requires a
"second leg" — re-grepping `docs/wiki/learned-lessons/README.md`'s Index — before writing any
new file governed by a static-analysis gate, seeded as a `TaskCreate` item per the
`[defect-class: wiki-second-leg-not-regrepped]` tier-2 requirement.

The natural approach was to run the grep once, create one `TaskCreate` task summarizing the
disposition, and then batch several `Write` calls for the day's new files in the same response.
This failed repeatedly: the `PreToolUse` hook blocked every `Write` after the first one in the
batch with the same `wiki-second-leg-not-regrepped` error, even though a qualifying `TaskCreate`
had just been made in the same message. A second attempt — creating a fresh `TaskCreate` in a
*separate* turn immediately before the `Write` — also failed.

## What actually satisfies the hook

The hook only passes when, within **one single tool-call batch** (one assistant turn), all three
happen together and in this shape:

1. A `Bash` call running the actual `grep -n ...` command against the wiki-lessons README
2. A `TaskCreate` call whose subject/description names the specific file about to be written
3. The `Write` call for that one new file

This must be repeated **per file**, not per batch — a batch containing grep + TaskCreate + five
`Write` calls only lets the first `Write` through; the remaining four are blocked with the
identical error, even though the grep and TaskCreate genuinely just ran. There is no way to
front-load the check for multiple files at once.

## Why this is non-obvious

The hook's own error message ("re-run the wiki-lessons Index grep... and seed a TaskCreate item")
reads as a one-time-per-session or one-time-per-batch precondition, not a strict
one-grep-one-task-one-write coupling re-verified on every single gated tool call. Nothing in the
message states the same-batch, one-file-at-a-time granularity; it was only discovered empirically
after ~4 failed attempts with progressively more literal compliance (verbose `grep -n` output,
exact phrase "Wiki-lessons re-grep" in the subject, full grep output pasted into the description)
before the actual variable — batch/call locality, not phrasing or verbosity — was identified as
the blocker.

## How to apply

When creating multiple new gate-governed files in one issue's implementation, budget one
grep+TaskCreate+Write triplet per file, all three calls in the same tool-call batch, rather than
trying to front-load the recurrence-scan check once for the whole set. This is slower but is the
only pattern that reliably passes the hook.
