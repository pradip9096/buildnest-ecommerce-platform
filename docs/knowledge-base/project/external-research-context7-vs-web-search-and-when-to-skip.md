# External Research: context7 vs. Web Search, and When to Skip It

**Category:** documentation
**Last Updated:** 2026-07-12

## What this step answers (and doesn't)

`development-workflow.md`'s `external-research` step covers **"how do I build this"** — the
official API surface of a library/framework/SDK/CLI already decided on, and the idiomatic
pattern for wiring it up. It does **not** cover **"which approach should I build"** —
feasibility comparisons, tradeoff discussions, prior art across competing designs. That's
`solution-options-adr`'s job. Folding both into one step recreates a mixed-scope Notes cell
that has to be split apart later (this file's own `init-context`/`external-research` split,
2026-07-11, was exactly this pattern once already).

## Tool selection

- **`context7`** — the official docs/API surface of a *named* library, framework, SDK, or CLI.
  Use when the question is "what's the exact signature/config/flag for X." This is the
  authoritative-source case: the vendor's own current documentation, not a blog post that may
  be stale relative to the pinned version.
- **Web search** — the idiomatic *implementation pattern* once the API itself is already known.
  Use for "how do people typically wire up X for Y use case," known gotchas, common mistakes,
  and community consensus that context7 (API reference) doesn't carry. This is also where a
  version-specific behavior change that isn't yet reflected in cached knowledge gets caught —
  see the mysqld_exporter `DATA_SOURCE_NAME` removal in
  [Smoke, Sanity, and Regression Testing vs. CI Test-Suite Coverage](smoke-sanity-and-regression-testing-vs-ci-test-suite-coverage.md)
  for a case where the documented-in-memory config method had been silently dropped by a newer
  pinned version — the kind of drift a fresh web search (not stale training-data recall) is
  positioned to catch.

## When to skip

Skip when the repo already demonstrates the pattern (an existing controller/service pair to
model a new one on) or the approach is already well-understood. Do **not** skip merely because
the feature "sounds standard" without actually checking — "sounds standard" is exactly the
overconfidence this step exists to catch; a library's API can look familiar and still have
version-specific quirks that only research (or a live test, see the smoke-test article above)
would surface.

## Sourcing a Claude Code capability, as distinct from the application feature

If what's being researched is a Claude Code capability itself — a plugin, agent, or hook — that
is a different question from implementing the *application* feature, and belongs in
[Claude Code Extension Mechanisms](claude-code-extension-mechanisms.md) (hooks, MCP servers,
slash commands, skills, and plugin marketplaces), not this article.

## See also

- `development-workflow.md`'s `external-research` step (Sequence table) and `solution-options-adr`
  (the sibling step for feasibility/design-comparison research)
- [Research/Discovery Phase Before Software Implementation](research-discovery-phase-before-software-implementation.md)
  — the broader pre-implementation research taxonomy this step is one piece of
