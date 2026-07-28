---
title: GitHub Sub-Issues REST API's parent field is `parent_issue_url`, not `.parent.number`
category: technical
date: 2026-07-28
root_cause: Assumed the parent-issue field on `GET /repos/{owner}/{repo}/issues/{n}` mirrored the nested-object shape of the `sub_issues` endpoint's own child list, without checking the actual response.
impact: A GitHub Action (#600) built to auto-close a parent epic once all its sub-issues close would have silently no-op'd on every single invocation — `.parent.number` is always empty on the real payload, so the workflow would report "no parent" for every issue, every time, with no error to signal the mistake.
---

## What happened

Building `.github/workflows/epic-auto-close.yml` (#600), the first draft read the closed issue's
parent via `gh api repos/{owner}/{repo}/issues/{n} --jq '.parent.number'`. This is a reasonable
guess by analogy with `GET .../issues/{n}/sub_issues`, which does return an array of full child
issue objects (each with its own `.number`). The actual field on the **parent-issue direction** is
different: `parent_issue_url`, a URL string (e.g.
`https://api.github.com/repos/{owner}/{repo}/issues/552`), not a nested object.

```bash
$ gh api repos/OWNER/REPO/issues/559 --jq '.parent'
null
$ gh api repos/OWNER/REPO/issues/559 --jq '.parent_issue_url'
https://api.github.com/repos/OWNER/REPO/issues/552
```

`.parent` genuinely exists on the payload but is always `null` — it isn't a typo-shaped 404, it's a
real field that just never carries the data one would expect from its name. `basename` on the URL
extracts the numeric parent ID cleanly.

## Why this is dangerous

The mistake produces **zero errors at any layer** — `jq`'s `// empty` silently degrades a missing
field to an empty string, the shell's `-z` check reads that as "no parent," and the workflow exits
0 having done nothing. A CI job that always reports green while never performing its actual job is
exactly the failure shape already documented in this repo for `sonar:sonar` (#350) and PMD (#320)
— "the check ran and passed" is not evidence it did anything.

## How it was caught

Before pushing, the logic was dry-run manually against real, already-known data (issue #559, a
real sub-issue of the already-closed epic #552) using the exact `gh api`/`jq` commands the
workflow itself uses, outside of Actions entirely. The dry run returned an empty parent for a case
known to have one, which is what surfaced the wrong field name — a live Actions run reporting
"nothing to do" would have looked identical to a correctly-firing no-parent case and given no
signal anything was wrong.

## Generalizable lesson

When consuming a REST API field whose exact shape isn't already confirmed in this session, print
and inspect the actual payload (`gh api ... | python3 -m json.tool` or an unfiltered `--jq '.'`)
before writing the extraction logic that depends on it — don't infer a nested-object shape by
analogy with a *different, related* endpoint's response shape, even when they cover the same
conceptual relationship (parent/child sub-issues here). This generalizes beyond GitHub's API to
any REST/GraphQL API pair where a "forward" and "reverse" direction of the same relationship are
exposed as separate endpoints or fields — their shapes are not guaranteed to be symmetric.
