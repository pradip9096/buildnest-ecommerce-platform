# GitHub Actions `working-directory` default only applies to `run:` steps

A workflow- or job-level `defaults.run.working-directory` override affects **only `run:` steps**
(shell commands). It has no effect on `uses:` steps (third-party/marketplace actions) — those
resolve their own working directory internally, ignoring the default entirely.

## Why this matters

In #329, the `report-summary` job in `ci-cd-pipeline.yml` inherited a workflow-level
`working-directory: backend` default with no checkout step in that job, so `backend/` never
existed on the runner. The job had two steps:

1. `uses: actions/download-artifact@v4` — **unaffected** by the default, ran fine
2. `run: echo ... >> $GITHUB_STEP_SUMMARY` — **affected**, failed with
   `No such file or directory` trying to `cd` into `backend/`

Reading the failure in isolation, it's easy to assume the whole job — including the
`download-artifact` step — is broken by the missing checkout. It isn't; only `run:` steps are
sensitive to the default. This matters when picking a fix: overriding `working-directory` at the
job level (rather than adding a checkout step) is sufficient and correct here specifically because
the only working-directory-sensitive step doesn't need repo content — it just writes to
`$GITHUB_STEP_SUMMARY`.

## How to apply

When diagnosing a `working-directory`-related CI failure, check whether the failing step is a
`run:` or `uses:` step before deciding the fix. A job-level `defaults.run.working-directory`
override is a minimal, safe fix when: (a) only `run:` steps in that job are affected, and (b) none
of those steps actually need the directory you're overriding away from.

This generalizes beyond BuildNest — it's a property of GitHub Actions itself, not this repo's
workflow file.
