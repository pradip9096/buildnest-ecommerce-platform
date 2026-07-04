# Learned Lessons Repository

Reusable, actionable lessons extracted from BuildNest development sessions.
Each file is a standalone reference — read it when you hit the relevant situation.

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

## Related Knowledge Base Articles

- [Quality Gate Ratchet Pattern](../../knowledge-base/project/quality-gate-ratchet-pattern.md) — fitness functions, monotonic improvement constraint, PIT threshold schedule

## Contributing

- One topic per file; consolidate closely related patterns into the same file
- Frontmatter is required: title, category, tags, keywords, source\_conversations, last\_updated, confidence, evidence\_strength, related\_lessons
- Update this README when adding or significantly updating a file
- Do not document a lesson from a single occurrence unless the failure mode is non-obvious or high-cost
