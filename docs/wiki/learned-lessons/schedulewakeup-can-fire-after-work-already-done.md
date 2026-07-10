---
title: "A Scheduled Wakeup Prompt Can Arrive After the Work It Describes Is Already Done"
category: tooling
tags: [schedulewakeup, background-tasks, task-notification, race-condition, redundant-work]
keywords: [scheduled wakeup stale, duplicate task notification, background task race, ScheduleWakeup fires late, re-doing completed work]
source_conversations: [Session 2026-07-03]
last_updated: 2026-07-03
confidence: medium
evidence_strength: moderate
related_lessons: []
---

# A Scheduled Wakeup Prompt Can Arrive After the Work It Describes Is Already Done

## Problem

While chaining three sequential PIT mutation-test runs for issue #276 (each run backgrounded via Bash `run_in_background`), a `ScheduleWakeup` was set after starting run 2 and again after starting run 3, each with instructions like "check on run N, if done start run N+1 / run the diff script." In both cases, the actual background task's `task-notification` arrived first, in real time, and was handled immediately in the normal flow of the conversation — run 3 was started, the diff script was run, findings were posted to #276, and the issue was closed, all before the corresponding scheduled wakeup fired.

The scheduled wakeup then fired anyway, arriving as a new user-turn instruction: "Check on PIT run 3 ... If done, run the diff script ... If still running, reschedule." By the time it arrived, run 3 had finished minutes earlier and the entire investigation was already complete and closed on GitHub.

## Why

`ScheduleWakeup` and the harness's `task-notification` mechanism are two independent triggers watching the same background task. The task notification is event-driven (fires the instant the task completes) and was fast enough to preempt the scheduled wakeup's fixed delay. The scheduled wakeup isn't automatically cancelled just because the condition it was polling for got resolved through the other channel — it still fires at its scheduled time regardless.

## Rule

When a scheduled wakeup arrives, don't assume its premise ("is X still running?") is still true — check first (e.g., whether the described GitHub issue is already closed, whether the referenced background task ID is already gone from any tracking) before re-running described steps. If the work is already done, say so plainly and point to what was already completed rather than silently redoing it or ignoring the mismatch.

If you know a task-notification will supersede a scheduled wakeup for the same background task, consider whether the wakeup is still worth scheduling — it may be simpler to skip `ScheduleWakeup` entirely for a task the harness is already going to notify on, and only use it as a fallback timeout for tasks or conditions the harness *can't* observe directly.
