---
title: Pre-Implementation Premise Verification — Facts, Tool Execution Model, Live Infrastructure, and Test Identity
category: quality-engineering
tags: [premise-verification, infrastructure-readiness, test-identity, tool-execution-model, github-issues]
keywords: [verify issue premise before implementing, runtime behavior vs config, third-party tool execution model, live infrastructure reachability, dev server vs container, test identity setup, fully current fully stale partial]
objective: What has to be independently confirmed, and how, before implementation starts on a GitHub issue — beyond just re-reading the issue text?
audience: Anyone starting work on a filed issue in this repo, especially one touching infrastructure, a third-party tool's config semantics, or a role the seed data doesn't provide.
scope: general, with BuildNest-specific worked examples
source_conversations: [Session 2026-07-05 (#428), Session 2026-07-17 (#425), Session 2026-07-18 (#309 /critique-prompt extraction)]
last_updated: 2026-07-18
confidence: high
evidence_strength: strong
related_articles:
  - verify-issue-premises-against-repo-before-implementing.md
status: published
---

# Pre-Implementation Premise Verification

## What Is It?

A four-part check run once, before implementation begins on a filed issue, that goes beyond
re-reading the issue text: (1) verify claims about *runtime/execution behavior* against actual
recent output, not just config; (2) verify your own understanding of any third-party tool's
*execution model* via its documentation, treating it as provisional until an empirical check
confirms it; (3) confirm *live infrastructure* — including the application's own dev-server
processes, not just their container dependencies — is actually reachable now; (4) establish any
*test identity* the verification will need (an admin role the seed data doesn't provide) as part
of this same pass, not as a mid-task surprise. The output is a single stated verdict: the issue's
premise is **fully current** (implement as written), **fully stale** (confirm before closing), or
**partial** (name which specific criteria are already met vs. not).

## Why It Matters

Each of these four failure modes has independently cost real debugging time in this repo:

- **Runtime claims treated as config claims**: a workflow file being "active" or a job being
  "wired in" doesn't mean it actually executes or blocks anything — `gh workflow list` reporting
  `active` only means the file is enabled, not that its trigger branches exist (`ci-cd.yml`
  targeted `main`/`develop`, neither of which existed here). A `codeql-action/upload-sarif` step
  existing doesn't mean CodeQL's own scan engine ran — it's a generic SARIF uploader, confirmed
  only by reading the actual workflow steps, not the action name.
- **Tool execution model assumed instead of verified**: some tools have properties no doc
  surfaces until an empirical check does — build-dependent static analysis, or a config
  directive's precise inheritance/scoping rule. Trusting the top-level architectural choice (which
  base image, which library) without verifying each specific directive the plan actually relies on
  risks a change that looks right on paper and does something else at runtime.
- **Infrastructure assumed reachable**: a container being up (`docker ps` healthy) doesn't mean
  the application process that talks to it is running — `./mvnw spring-boot:run` doesn't read
  `.env` the way `docker compose` does, so a healthy MySQL container next to a backend process
  that silently connected to the wrong database has produced misleading "it's broken" symptoms
  more than once in this repo's history.
- **Test identity discovered as a mid-task blocker**: this repo's seed data provides no admin
  account by default. Discovering that fact mid-verification (after cart/category/product setup
  is already underway) costs a round trip that establishing the identity up front avoids entirely.

Bundling these four into one premise-check pass, rather than letting each surface separately mid-task,
is what actually prevents "discover unavailability mid-task" — the single most expensive shape of
premise failure, since it invalidates work already done in between.

## How It Works

### 1. Runtime/execution-behavior claims

For any claim that a step "runs," a gate "blocks," or a job "triggers" — verify against the actual
output of a recent execution (a `gh run list`/`gh api .../check-runs` query, a log line, a live
`curl`), not just the configuration file that's supposed to produce that behavior. A config file
describes intent; only an execution's actual output confirms the intent was realized.

### 2. Third-party tool execution model

Verify your own understanding of the tool's actual execution semantics via its documentation
before trusting that a planned change produces the intended effect — apply this not only to the
top-level architectural choice but to each specific config directive/flag the plan relies on.
Treat every part of this understanding as provisional until a later empirical check confirms it;
some execution properties (build-dependent analysis passes, context-dependent directive scoping)
are not documented anywhere a doc search will find, and only show up once the change actually runs.

### 3. Live infrastructure reachability

If verification will depend on live infrastructure (Docker, a running service, a dashboard),
confirm it's reachable *now*, during this premise-check, before any implementation step begins —
even when that infrastructure is the subject of the issue itself (a Dockerfile/container issue is
not exempt from this check just because fixing the infrastructure is the point). "Infrastructure"
explicitly includes the application's own dev-server processes (backend/frontend), not just their
container dependencies (DB/cache/search) — a healthy container says nothing about whether the app
process that talks to it is actually up, or pointed at the right instance.

### 4. Test identity

If verification will require an authenticated session against a role the seed data doesn't
provide (e.g. no admin account exists by default here), establish that identity as part of this
same premise-check pass — check `project_state.md`/domain lesson files first for an
already-documented shortcut (e.g. registering a scratch account and granting the role directly in
the dev database) before improvising one from scratch mid-verification.

### Stating the result

Conclude with one of three explicit verdicts, never a silent default to "implement as filed":

- **Fully current** — every criterion in the issue still holds; implement as written.
- **Fully stale** — the described defect/state no longer matches reality; confirm with the user
  before closing or reworking.
- **Partial** — name which specific criteria are already met vs. not; don't default to a full
  build when only part of the issue's scope is actually still open.

## When to Use It

Run this once, as its own premise-check pass, before `TaskCreate`/branching — not folded silently
into general "understanding the issue." It applies to every issue that makes a runtime/execution
claim, references a third-party tool's config behavior, depends on live infrastructure for its own
verification, or will need an authenticated role the seed data doesn't provide. It is distinct from
verifying an issue's stated *severity* (a separate axis — see
[Trace Every Consumer of Shared Data Before Trusting a Bug Report's Severity](../../wiki/learned-lessons/trace-every-consumer-of-shared-data-before-trusting-a-bug-reports-severity.md)),
which checks blast-radius rather than facts.

## Examples

- **#428** (admin category management UI): the `docker` CLI was missing from the WSL distro
  entirely, not just stopped — discovered and resolved via `AskUserQuestion` at premise-check time,
  before any implementation began, rather than mid-task.
- **#425** (product CRUD UI): live browser verification needed an admin session; the dev database
  had no `ROLE_ADMIN` row at all (a stale/reset volume) — a scratch account was registered and
  granted the role directly, following an already-documented shortcut pattern rather than
  improvising one mid-verification.
- **#318** (PMD CI gate): `gh workflow list` reported the workflow `active`, but its trigger
  branches (`main`/`develop`) didn't exist in this repo — the gate had zero run history despite
  looking wired in from the config alone; only `gh run list` against the actual workflow surfaced
  this.

## Synthesis

None of these four checks is expensive on its own — a `gh run list` query, a `curl`, a `docker ps`,
a scratch-account registration. What makes skipping any of them costly is timing: each one, if
skipped, tends to surface *mid-task* instead of *before* it, at a point where reversing course
costs more than the check itself would have. Bundling all four into one explicit premise-check
pass, concluded with a stated fully-current/fully-stale/partial verdict, is what converts four
independently-cheap checks into one reliably-run gate.

## Related Articles

- [Verify Issue Premises Against Repo Before Implementing](../../wiki/learned-lessons/verify-issue-premises-against-repo-before-implementing.md) — the narrower, factual-claims-only precedent this article generalizes beyond (file paths, gated metrics)
- [Trace Every Consumer of Shared Data Before Trusting a Bug Report's Severity](../../wiki/learned-lessons/trace-every-consumer-of-shared-data-before-trusting-a-bug-reports-severity.md) — the sibling check on the *severity* axis, distinct from this article's facts/infrastructure/identity axes
