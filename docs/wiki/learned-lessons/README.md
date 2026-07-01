# Learned Lessons Repository

Reusable, actionable lessons extracted from BuildNest development sessions.
Each file is a standalone reference — read it when you hit the relevant situation.

## Index

| File | Topic | Category | Last Updated |
|---|---|---|---|
| [pit-mutation-testing-patterns.md](pit-mutation-testing-patterns.md) | Three common PIT mutation survival patterns and their fixes: lambda null-return, setter-removal hidden by DTO, and `targetTests` naming exclusion | testing | 2026-07-01 |
| [shell-pipeline-exit-code-masking.md](shell-pipeline-exit-code-masking.md) | Pipelines ending in `tail`/`grep` always exit 0 — masks build failures; use `$PIPESTATUS` or `set -o pipefail` | tooling | 2026-07-01 |
| [github-issue-hygiene.md](github-issue-hygiene.md) | Root causes of stale issue accumulation and a repeatable closure protocol | process | 2026-07-01 |

## Related Knowledge Base Articles

- [Quality Gate Ratchet Pattern](../../knowledge-base/project/quality-gate-ratchet-pattern.md) — fitness functions, monotonic improvement constraint, PIT threshold schedule

## Contributing

- One topic per file; consolidate closely related patterns into the same file
- Frontmatter is required: title, category, tags, keywords, source\_conversations, last\_updated, confidence, evidence\_strength, related\_lessons
- Update this README when adding or significantly updating a file
- Do not document a lesson from a single occurrence unless the failure mode is non-obvious or high-cost
