---
title: "Verifying Time-Based Elasticsearch ILM Policies Without Waiting the Real Duration"
category: infrastructure
tags: [elasticsearch, ilm, index-lifecycle-management, smoke-testing, docker-compose]
keywords: [ILM min_age verification, indices.lifecycle.poll_interval, wait-for-shard-history-leases stuck, ILM delete phase test, scratch policy verification technique]
source_conversations: [Session 2026-07-13, issue #326]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
root_cause: "an ILM policy's delete phase only becomes eligible to run once min_age elapses, and ILM re-evaluates eligible indices on a poll cycle (default 10 minutes) rather than instantly on min_age expiry, so a real 30-day policy cannot be end-to-end verified by simply waiting"
impact: low — this is a verification-technique gap, not a bug; without it, teams either skip live verification of ILM delete behavior entirely (config-review-only confidence) or wait unrealistic real time to confirm it
related_lessons:
  - docs/wiki/learned-lessons/smoke-sanity-and-regression-testing-vs-ci-test-suite-coverage.md
---

# Verifying Time-Based Elasticsearch ILM Policies Without Waiting the Real Duration

## Problem

Issue #326 required a live verification that a new Elasticsearch ILM (Index Lifecycle Management)
retention policy — delete-only, `min_age: 30d` — genuinely deletes indices once they age out, not
just that the policy JSON is syntactically valid and registered. Waiting the real 30 days is not
a viable verification step, but skipping verification entirely and trusting the config would
violate this repo's `smoke-sanity-regression-test` step: the whole point is that config review
alone can't catch runtime-infrastructure behavior.

A first attempt at forcing the delete action appeared to hang: after registering a scratch policy
with `min_age: 0s` and applying it directly to a test index, `GET <index>/_ilm/explain` showed the
index stuck at `phase: delete`, `step: wait-for-shard-history-leases` for over a minute with no
progress, even after calling `POST _ilm/start` and retrying the step.

## Why This Is Non-Obvious

`min_age: 0s` only makes an index *eligible* to enter the delete phase — it does not make ILM
re-evaluate that index immediately. Elasticsearch's ILM runner polls all managed indices on a
fixed interval (`indices.lifecycle.poll_interval`, default `10m`), and only re-checks phase
eligibility on that cadence. `wait-for-shard-history-leases` is a real, normal step inside the
delete action (waiting for retention leases on the index's shards to clear) — it is not evidence
of a bug, but on a single-node dev cluster with no ongoing writes it can look identically "stuck"
whether ILM just hasn't polled yet or something is genuinely wrong. `POST _ilm/start` only ensures
the ILM runner itself is active; it does not force an out-of-cycle poll, and `_ilm/retry` on a step
that hasn't errored is explicitly rejected (`"cannot retry an action ... that has not encountered
an error"`) — an easy dead end to hit while trying to force progress.

## Fix — Force the Poll Cycle, Not the Clock

Temporarily lower the ILM poll interval via a cluster setting, let it run through the already-met
`min_age` threshold, then restore the default:

```bash
# Force ILM to re-evaluate all managed indices every 1s instead of the default 10m
curl -u "$AUTH" -X PUT "http://localhost:9200/_cluster/settings" \
  -H 'Content-Type: application/json' \
  -d '{"transient":{"indices.lifecycle.poll_interval":"1s"}}'

# Give it a few polling cycles to actually execute the now-eligible delete step
sleep 15

# Confirm the index is genuinely gone (not just "phase: delete" in explain output)
curl -s -o /dev/null -w "%{http_code}\n" -u "$AUTH" "http://localhost:9200/<index-name>"
# expect 404

# Restore the default so production-realistic timing resumes
curl -u "$AUTH" -X PUT "http://localhost:9200/_cluster/settings" \
  -H 'Content-Type: application/json' \
  -d '{"transient":{"indices.lifecycle.poll_interval":null}}'
```

This proves the actual mechanism (the delete *action* removes the index) without waiting real
time for either `min_age` or the poll interval. It does **not** prove the scheduler's own timing
behavior — that's Elastic's own well-documented, uncontroversial implementation, out of scope to
independently verify. The thing worth testing is *your own policy configuration*, not Elasticsearch
itself.

## How to Apply

1. Use a disposable **scratch policy** (`min_age: 0s`) applied directly to a disposable **scratch
   or real-pipeline-created test index** — never lower the poll interval against the actual
   production policy, since that changes real cluster-wide timing behavior for every managed index
   while the setting is active.
2. If a delete-phase index appears stuck at a wait-step, check whether it's simply waiting on the
   next poll cycle before assuming a bug — `GET <index>/_ilm/explain` showing a stable
   `step_time_millis` with no error is the normal, working state, not a failure signal.
3. Always restore `indices.lifecycle.poll_interval` to its default (`null` via the transient
   settings API) immediately after verification — leaving it lowered wastes cluster resources on
   unnecessary constant re-evaluation in a long-running environment.
4. Clean up every scratch artifact (test index, scratch policy) before considering the
   verification complete — a leftover scratch policy blocks deletion of itself if still
   referenced by an index (`"Cannot delete policy ... It is in use by"`), which is itself a useful
   sanity check that the association is real.

This generalizes beyond BuildNest to any Elasticsearch ILM verification, and the underlying
technique (temporarily shortening a poll/check interval to observe time-gated behavior without
waiting real time) generalizes further to any scheduler-driven, interval-polled system.

## Related Articles

- [Smoke, Sanity, and Regression Testing vs. CI Test-Suite Coverage](../../knowledge-base/project/smoke-sanity-and-regression-testing-vs-ci-test-suite-coverage.md) — why this class of verification exists as a distinct step from CI's own test suite
