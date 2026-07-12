# Content Extraction: DRY/SSOT as the Decision Principle, Not Size

**Category:** documentation
**Last Updated:** 2026-07-12

## What is it

When deciding whether to pull a piece of inline content out of a document (a rule file, a config
comment, a README section) into its own linked article, several familiar principles all seem to
apply at once: DRY, Single Source of Truth, Separation of Concerns, modular documentation,
abstraction, progressive disclosure, indirection. Naming all of them makes the pattern sound
overdetermined — like six or seven independent forces all pointing the same way. They aren't
independent. Most of that list describes the *shape* of the result once you've already decided to
extract; only one or two of them actually do the deciding.

## The underlying concept

**Content modularization** is a reasonable umbrella term for the overall pattern (breaking large
bodies of information into smaller, independently managed, reusable units — what the structured-
authoring world calls "topic-based authoring"). But naming the umbrella doesn't tell you *when*
to reach for it. That's a narrower question, and it has a narrower answer.

**DRY (Don't Repeat Yourself) / Single Source of Truth is the actual decision principle.** The
test is: does this content describe something that would need to be copy-pasted, not just linked,
into a second document someday? If yes — if it's a fact, technique, or definition that's true
independent of the document it currently sits in — it belongs in one canonical place, referenced
from everywhere it's relevant. If no — if the content is inherently *about* this document's own
local structure (a cross-reference to a sibling section, a rule specific to this table's own step
boundaries) — inlining it is correct, and extracting it would just add an indirection with no
reuse benefit.

**Separation of Concerns is the complementary half of the same judgment, from the other side.**
Where DRY/SSOT asks "does this content need to stay in sync across places it might get copied,"
SoC asks "does this section still belong to the *container* document's stated purpose." A rule
file's job is to say what to do in this specific context; the moment a paragraph starts explaining
*how something works in general*, independent of that context, it's drifted outside the
container's actual concern, whether or not it would ever literally be duplicated elsewhere.

**Everything else on the familiar list is a consequence, not a cause:**

- **Modular documentation** and **indirection** describe the *mechanism* — a self-contained
  target, referenced by a link — not the *decision* to use it.
- **Abstraction** and **progressive disclosure** describe the *reader-facing effect* — a shorter
  main document, detail available on demand — which is a benefit you get once you've extracted,
  not a test for whether extraction was warranted.

Listing all of these as parallel "principles at work" makes the pattern sound like it has many
independent justifications. It has one real justification (avoid content that must stay in sync
existing in more than one place) and one framing question (does this still belong to the
container's own purpose). The rest is what that justification looks like once acted on.

## Why size is not the trigger

It's tempting to operationalize "duplication risk" as "length," since bloated sections are what a
scan actually surfaces. But length is a symptom used to go *looking*, not the condition itself.
Concrete counter-example: in `development-workflow.md`'s Sequence table, `create-project-board`'s
Notes cell is the single longest row in the table (a multi-sentence deprecation story about this
repo's own abandoned GitHub Project board) and correctly stays inline — because that content is
entirely specific to this repository's own history, with no reuse value anywhere else. Meanwhile
two much shorter rows (`external-research`, and separately a CORS rule in `spring-security.md`)
were genuinely worth extracting, because their content — context7-vs-web-search tool selection,
a Spring Security precedence fact — is true in any codebase, not just this one.

A hard character-count cap would have flagged the wrong row and missed both right ones.

## When to use it

Reach for extraction when a paragraph, table cell, or comment block satisfies the DRY/SSOT test
above: the content is standalone and would remain true and useful if copied into an unrelated
project. Leave it inline when the content is a tight, doc-local cross-reference or a fact whose
entire relevance is specific to the surrounding document's own structure or history.

## Examples

- **Extracted** (generalizable): a Spring Security framework precedence fact
  ([Spring Security's `corsConfigurationSource` Shadows `WebMvcConfigurer` CORS Entirely](spring-security-cors-configurationsource-precedence-over-webmvc.md)),
  a general research-tool-selection technique
  ([External Research: context7 vs. Web Search](external-research-context7-vs-web-search-and-when-to-skip.md)),
  a general testing-methodology distinction
  ([Smoke, Sanity, and Regression Testing vs. CI Test-Suite Coverage](smoke-sanity-and-regression-testing-vs-ci-test-suite-coverage.md)).
- **Correctly left inline** (doc-local): `development-workflow.md`'s `create-project-board` row
  (a fact about this repo's own GitHub Project history), and its `prioritize-issues`/
  `solution-options-adr` rows (each just a one-sentence boundary statement relative to a sibling
  step in the same table).

## Synthesis

Content modularization is the right umbrella name for the pattern, but "modularize when content
gets long" is the wrong operational rule hiding under that name. The operational rule is DRY/SSOT
(would this need to be copy-pasted, not linked, elsewhere) plus SoC (does it still belong to this
document's own stated purpose) — everything else commonly cited alongside them (abstraction,
progressive disclosure, indirection, modular documentation) is a description of what the result
looks like, not a reason to produce it.

## See also

- [Extracting Inline Content Out of Other Project Docs](README.md#extracting-inline-content-out-of-other-project-docs-rule-files-etc)
  — the mechanical how-to (where the article goes, how to leave the pointer, index/log updates)
  that this article supplies the *why* for
