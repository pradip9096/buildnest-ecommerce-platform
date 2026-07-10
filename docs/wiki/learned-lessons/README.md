# Learned Lessons Repository

Reusable, actionable lessons extracted from BuildNest development sessions.
Each file is a standalone reference — read it when you hit the relevant situation.

## Definition

A **lesson learned** is a generalized, evidence-based insight — captured from a real
experience, positive or negative — that is likely to provide long-term value beyond the
original discussion. Three formulations, all converging on the same bar:

> "A lesson learned is knowledge or understanding gained by experience. The experience
> may be positive, as in a successful test or mission, or negative, as in a mishap or
> failure... A lesson must be significant in that it has a real or assumed impact on
> operations; valid in that is factually and technically correct; and applicable in
> that it identifies a specific design, process, or decision that reduces or
> eliminates the potential for failures and mishaps, or reinforces a positive result."

> "Generalizations based on evaluation experiences with projects, programs, or
> policies that abstract from the specific circumstances to broader situations.
> Frequently, lessons highlight strengths or weaknesses in preparation, design, and
> implementation that affect performance, outcome, and impact."

> "A lesson learned is a generalized, evidence-based insight that captures knowledge,
> best practices, decision-making rationale, recurring patterns, successful
> approaches, common pitfalls, implementation experiences, troubleshooting
> strategies, design considerations, workflows, methodologies, or process
> improvements that are likely to provide long-term value beyond the original
> discussion."

Source: [Wikipedia — Lessons learned](https://en.wikipedia.org/wiki/Lessons_learned)

**The three-part admission bar for a file in this directory** (synthesized from all
three definitions above):

- **Significant** — it has a real or assumed impact on how work gets done here; a
  one-off typo or a preference with no downstream consequence doesn't qualify.
- **Valid** — verified against the actual failure/fix, not assumed or reconstructed
  from memory; factually and technically correct.
- **Generalized/applicable** — abstracted past the one specific occurrence into a rule
  that applies the next time the same class of situation comes up, not just a log of
  "what happened on 2026-07-04."

This is a stricter bar than "anything mildly useful I noticed" — see the Contributing
section below for the single-occurrence threshold this implies in practice.

## Index

| File | Topic | Category | Last Updated |
|---|---|---|---|
| [pit-mutation-testing-patterns.md](pit-mutation-testing-patterns.md) | Three common PIT mutation survival patterns and their fixes: lambda null-return, setter-removal hidden by DTO, and `targetTests` naming exclusion | testing | 2026-07-01 |
| [shell-pipeline-exit-code-masking.md](shell-pipeline-exit-code-masking.md) | Pipelines ending in `tail`/`grep` always exit 0 — masks build failures; use `$PIPESTATUS` or `set -o pipefail` | tooling | 2026-07-01 |
| [github-issue-hygiene.md](github-issue-hygiene.md) | Root causes of stale issue accumulation and a repeatable closure protocol | process | 2026-07-01 |
| [dotenv-not-auto-loaded-by-local-processes.md](dotenv-not-auto-loaded-by-local-processes.md) | Docker Compose auto-loads `.env`; locally-run processes (`mvnw`, `npm run dev`) do not — must be sourced explicitly | tooling | 2026-07-02 |
| [persistent-memory-staleness-drift.md](persistent-memory-staleness-drift.md) | Claude's persistent memory (issue counts, milestone status) silently goes stale with no auto-refresh — verify before citing project-state/reference memory as fact | process | 2026-07-02 |
| [stale-test-classes-false-failures.md](stale-test-classes-false-failures.md) | `mvn test` without `clean` can run orphaned `.class` files for test sources already deleted in an earlier commit, causing unrelated `NoClassDefFoundError` failures | testing | 2026-07-02 |
| [pretooluse-hook-fires-once-per-bash-call.md](pretooluse-hook-fires-once-per-bash-call.md) | `PreToolUse` hooks fire once before the whole Bash tool call, not per line — combining a staging command and a triggering command in one call gives the hook stale state | tooling | 2026-07-02 |
| [git-checkout-vs-reset-order.md](git-checkout-vs-reset-order.md) | `git checkout -- <file>` restores from the index, not HEAD — on an already-staged file it's a no-op; use `git restore --staged --worktree` or `reset` before `checkout --` | tooling | 2026-07-02 |
| [changelog-guard-scope-gap.md](changelog-guard-scope-gap.md) | The `changelog-guard` PreToolUse hook only watches `backend/src/main` and `frontend/src` — root scripts, CI config, and other infra files won't trigger it, even though they're "notable changes" | process | 2026-07-02 |
| [webmvctest-scans-filters-and-interceptors.md](webmvctest-scans-filters-and-interceptors.md) | `@WebMvcTest` scans every `Filter`/`HandlerInterceptor`/`WebMvcConfigurer` bean app-wide, not just the target controller — expect cascading `@MockBean` requirements when converting from `@SpringBootTest` | testing | 2026-07-02 |
| [git-add-fails-atomically-on-bad-pathspec.md](git-add-fails-atomically-on-bad-pathspec.md) | `git add valid1 badpath valid2` stages nothing at all, not just the bad path — check `git status --short` after any multi-path `add` before committing | tooling | 2026-07-02 |
| [gh-cli-path-inconsistent-in-bash-tool.md](gh-cli-path-inconsistent-in-bash-tool.md) | A `127` from `gh` in one Bash call doesn't mean it's uninstalled — verify with `which gh` before switching to the GitHub MCP server, whose write ops have failed auth while reads succeeded | tooling | 2026-07-03 |
| [verify-issue-premises-against-repo-before-implementing.md](verify-issue-premises-against-repo-before-implementing.md) | Issue #275 named the wrong CI workflow file and the wrong gated metric (test strength vs. mutation score) — verify an issue's technical claims against the actual repo/config before implementing, even self-authored issues | process | 2026-07-03 |
| [schedulewakeup-can-fire-after-work-already-done.md](schedulewakeup-can-fire-after-work-already-done.md) | A `ScheduleWakeup` for a background task can fire and arrive after the task's own `task-notification` already triggered and completed the work — check whether the premise still holds before redoing anything | tooling | 2026-07-03 |
| [env-sourcing-and-cache-pitfalls-fixing-liquibase-data.md](env-sourcing-and-cache-pitfalls-fixing-liquibase-data.md) | Six stacked pitfalls fixing data via Liquibase locally: unquoted `&` truncates `.env` values under `bash source`, a duplicate key silently overrides, `ddl-auto=update` + disabled Liquibase causes silent schema drift across three tables, a direct DB write doesn't evict `@Cacheable` Redis entries, and editing an already-applied changeset needs an explicit-confirmation checksum reconciliation | tooling | 2026-07-04 |
| [allargs-constructor-positional-test-fragility.md](allargs-constructor-positional-test-fragility.md) | Adding one field to a Lombok `@AllArgsConstructor` entity/DTO breaks every positional `new X(...)` test call site after that field — prefer builders or setter-based construction in tests | testing | 2026-07-04 |
| [react-router-searchparams-not-resynced-on-navigation.md](react-router-searchparams-not-resynced-on-navigation.md) | Reading `useSearchParams` only in a `useState` initializer hydrates a fresh page load but leaves state stale on browser back/forward — needs an explicit effect keyed on the param values | tooling | 2026-07-04 |
| [jwt-excluded-claims-recur-across-fields.md](jwt-excluded-claims-recur-across-fields.md) | `user.id` (#280) and `user.roles` (#292) were the same root cause on two different fields, filed as separate issues — once a "JWT doesn't carry claim A" bug is confirmed, audit the same file for every other claim decoded the same way before closing the bug class | process | 2026-07-04 |
| [github-issue-multi-domain-labeling.md](github-issue-multi-domain-labeling.md) | An issue about auth logic living in a frontend file needs both `domain: auth` and `domain: frontend` — single-domain labeling makes it silently vanish from a filtered view expecting the other label | process | 2026-07-04 |
| [vitest-needs-triple-slash-reference-in-vite-config.md](vitest-needs-triple-slash-reference-in-vite-config.md) | Colocating Vitest's `test` block inside `vite.config.ts`'s `defineConfig` needs `/// <reference types="vitest/config" />` as the first line, or the `test` key isn't type-recognized | testing | 2026-07-04 |
| [rtl-cleanup-needs-explicit-aftereach-with-globals-false.md](rtl-cleanup-needs-explicit-aftereach-with-globals-false.md) | With Vitest `globals: false`, React Testing Library's automatic unmount-after-each-test never fires — needs an explicit `afterEach(cleanup)` in the shared setup file or later tests see leftover DOM from earlier ones | testing | 2026-07-04 |
| [tailwind-v4-semantic-color-alias-for-recolor-refactors.md](tailwind-v4-semantic-color-alias-for-recolor-refactors.md) | A "unify the brand color, one place to change it later" task needs a `@theme` alias (`--color-primary-50: var(--color-indigo-50)`, etc.) pointing at the chosen palette — a plain find-replace of the color name doesn't satisfy the "one place" requirement | tooling | 2026-07-04 |
| [fake-timers-cleanup-belongs-in-aftereach-not-test-body.md](fake-timers-cleanup-belongs-in-aftereach-not-test-body.md) | `vi.useRealTimers()` as the last line of a test body never runs if that test fails or times out first — the leftover fake-timer state then cascades into the *next* test's unrelated timeout failure; put the cleanup in `afterEach` instead | testing | 2026-07-04 |
| [verifying-a-stale-premise-can-surface-a-bigger-bug.md](verifying-a-stale-premise-can-surface-a-bigger-bug.md) | Verifying #249's stale "backend expects JSON" premise required reading the whole `changePassword` endpoint — which surfaced an unrelated, unfiled IDOR (client-supplied `userId`, no ownership check) standing next to the filed `priority: low` cosmetic bug | process | 2026-07-04 |
| [github-actions-working-directory-default-only-applies-to-run-steps.md](github-actions-working-directory-default-only-applies-to-run-steps.md) | `defaults.run.working-directory` only affects `run:` steps, not `uses:` steps — #329's `report-summary` job failed only on its `run:` step, not the preceding `actions/download-artifact` step | tooling | 2026-07-09 |
| [real-secret-pasted-into-env-example-instead-of-env.md](real-secret-pasted-into-env-example-instead-of-env.md) | A real NVD API key was pasted into the committed `.env.example` instead of the gitignored `.env` — twice in one session; never committed, but the fix and prevention rule generalize | process | 2026-07-09 |

## Related Knowledge Base Articles

- [Quality Gate Ratchet Pattern](../../knowledge-base/project/quality-gate-ratchet-pattern.md) — fitness functions, monotonic improvement constraint, PIT threshold schedule
- [.env.example (Committed Template) vs .env (Local Secrets)](../../knowledge-base/project/env-example-template-vs-env-local-secrets.md) — the durable mechanism behind the real-secret-pasted-into-env-example lesson above

## Contributing

- One topic per file; consolidate closely related patterns into the same file
- Frontmatter is required: title, category, tags, keywords, source\_conversations, last\_updated, confidence, evidence\_strength, related\_lessons
- Update this README when adding or significantly updating a file
- Do not document a lesson from a single occurrence unless the failure mode is non-obvious or high-cost
