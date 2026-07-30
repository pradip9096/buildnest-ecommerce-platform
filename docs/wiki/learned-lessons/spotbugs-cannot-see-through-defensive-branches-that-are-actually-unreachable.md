---
title: "SpotBugs Flags a toString()-on-Array Call Even Inside an instanceof-Guarded Fallback Branch It Can Never Reach"
category: technical
tags: [spotbugs, static-analysis, java, code-quality, xml]
keywords: [DMI_INVOKING_TOSTRING_ON_ARRAY, spotbugs threshold, xml comment double dash, unreachable branch static analysis]
source_conversations: ["#317"]
last_updated: 2026-07-14
confidence: high
evidence_strength: direct-repo-verification
root_cause: "SpotBugs's bytecode-level dataflow analysis flags a toString() call on any variable that is ever assigned an array anywhere in the method, even when a guard (instanceof pattern match, prior null-check) makes that specific call site provably unreachable for the array case — the analysis is not precise enough to eliminate the unreachable branch"
impact: low — cost one extra edit/verify cycle while fixing #317's DMI_INVOKING_TOSTRING_ON_ARRAY finding in AuditAspect, caught immediately by re-running spotbugs:spotbugs rather than assuming the first fix worked
related_lessons: []
---

# SpotBugs Flags a toString()-on-Array Call Even Inside an instanceof-Guarded Fallback Branch It Can Never Reach

## What happened

Fixing #317's `DMI_INVOKING_TOSTRING_ON_ARRAY` finding in `AuditAspect.auditMethod` (calling
`.toString()` on `oldValue`, an `Object`-typed variable whose only assignment is
`joinPoint.getArgs()` — an `Object[]`), the first fix used a pattern-match guard:

```java
oldValue instanceof Object[] args ? Arrays.toString(args)
        : oldValue != null ? oldValue.toString() : null
```

Re-running `spotbugs:spotbugs` still reported the same finding, on the same line — the `else`
branch's `oldValue.toString()`. Logically that branch can only execute when `oldValue` is *not*
`instanceof Object[]`, and the only non-null value `oldValue` ever holds *is* an `Object[]`, so the
branch is genuinely dead code. SpotBugs's dataflow analysis doesn't reason about `instanceof`
narrowing at that level of precision — it sees "this variable is assigned an array somewhere in
the method, and `.toString()` is called on it somewhere in the method," and flags every call site
that matches, guarded or not.

The actual fix was to remove the ambiguity entirely rather than defend against a case that can't
happen: since `oldValue` can only be `null` or `Object[]`, cast directly —
`oldValue != null ? Arrays.toString((Object[]) oldValue) : null` — leaving no call site where
`toString()` is invoked on a variable SpotBugs can see holding an array.

A second, unrelated snag hit in the same fix: the `pom.xml` config comment explaining the new
threshold used `--` as a prose separator (`"...findings only -- Medium/Low..."`), which is illegal
inside an XML comment (`--` may only appear as the closing `-->` delimiter) — Maven failed to parse
the POM at all with a cryptic `ModelParseException` pointing at the comment's byte offset, not at
the actual `--`. Caught immediately since the very next command (`spotbugs:check`) couldn't even
resolve the project; fixed by using the file's existing em-dash (`—`) convention instead.

## Why it matters

Both are small, but both would have shipped a false sense of "fixed" if the verification step had
been skipped: the first fix looked correct by inspection (compiles, adds a null/type guard) but
didn't actually clear the finding; re-running the real tool immediately after a fix — not just
compiling — is what caught it. This matches `testing.md`'s V&V "Execute verification" discipline
generalized to static analysis, not just tests: a fix for a static-analysis finding isn't done
until the same analyzer confirms it, not just until the code compiles and looks right.

## How to apply

- When fixing a SpotBugs (or similar bytecode/dataflow-based static analysis) finding with a
  defensive branch (`instanceof` guard, null check) rather than eliminating the ambiguous type
  entirely, **re-run the actual analyzer** (`spotbugs:spotbugs` or `spotbugs:check`, not just
  `compile`) before considering the fix done — a guard that's logically correct can still leave
  the flagged call site reachable from the tool's coarser point of view.
- Prefer eliminating the source of ambiguity (a precise cast, a properly-typed field) over a
  defensive branch when a variable genuinely has only one possible non-null runtime type — it's
  both clearer to a human reader and more likely to actually satisfy a bytecode-level analyzer.
- XML comments (`<!-- ... -->`) cannot contain a literal `--` anywhere in the body, only as the
  closing delimiter — a `--` used as a prose em-dash substitute inside a comment will fail to
  parse, and the parser error typically points at the wrong location (the comment's start or the
  following token, not the actual `--`). Use `—` (em dash) or `-` (single hyphen) instead.
