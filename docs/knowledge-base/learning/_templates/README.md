# Learning Templates

Reusable, standalone templates for producing this repo's `docs/knowledge-base/learning/`
content: a per-subject **outline skeleton** (sequencing only) followed by one **Module file**
per planned entry in that skeleton's Index (the actual teaching content). None of these
templates depend on this repo's other docs — each is safe to copy elsewhere.

This file does two jobs, kept in two separate sections below: **Orientation** (the workflow for
using these templates and the rules for amending them) and **Index** (the manifest — the
authoritative list of every file, as one-line rows). See
[Manifest and Surrogate Pattern for Index Files](../../project/manifest-and-surrogate-pattern-for-index-files.md)
and the [README-as-Manifest Blueprint](../../project/readme-manifest-blueprint.md) for why this
split exists.

---

## Orientation

### Workflow

1. Generate a subject's outline skeleton from `learning-outline-template.md` (via its Reuse
   Prompt, or the persistent Project setup in `chatgpt-project-setting-instrctions.md`). This
   produces titles and one-line scopes only — no Module content yet.
2. For each row in that skeleton's Index, author the real Module file yourself, one at a time,
   using `module-file-quick-template.md` (or `module-file-template.md` if you need the inline
   rule explanations again).
3. After writing each Module file, go back to the outline's Index row and replace the plain-text
   planned filename with a real link, and refresh `Last Updated` — this is how the outline tracks
   which Modules are actually written vs. still planned.

The outline skeleton's job is sequencing; a Module file's job is teaching. Don't blend the two —
the skeleton stays terse on purpose, and a Module file is where the real explanatory depth goes.

### Amending These Templates

This is the same **cross-reference mesh** problem this repo already names and tracks elsewhere
(see the wiki lesson [Cross-Reference Version Pointers Form a Mesh, Not a Single
Edge](../../../wiki/learned-lessons/cross-reference-version-pointers-form-a-mesh-not-a-single-edge.md),
written for the RTM/SRS/SDD/Test-Plan version-pointer set): a change to one file can require
updates to more than one sibling, not just the one edge the change happened to touch.

These four files aren't independent — each has at least one sibling whose content must move in
step with it:

- **`learning-outline-template.md`** ↔ **`chatgpt-project-setting-instrctions.md`** — the latter is
  a persistent-Project restatement of the former's Reuse Prompt. Any rule change to the Reuse
  Prompt (output shape, Step 0 wording, heading conventions, exclusion list, etc.) must be
  propagated to the ChatGPT instructions file in the same edit, or the two drift into producing
  different output for the same subject. `learning-outline-template.md`'s own Amendment Log
  already tracks this propagation explicitly — check it for "Propagated to the standalone ChatGPT
  Project instructions file" entries before assuming a rule only lives in one place.
- **`module-file-template.md`** ↔ **`module-file-quick-template.md`** — the quick template is the
  bare-skeleton twin of the fully-annotated one. Any change to the Module-file *shape* (Header/
  Body/Footer sections, See Also rules, what's optional vs. required) must be mirrored in both —
  the quick template just drops the guidance comments, it must never diverge on structure.
- **This README's Index table and Workflow section** — describes what each file is *for*. If a
  template gains or loses a section, a step, or a file-naming rule, update this README's relevant
  row/step in the same change, not as an afterthought once someone notices the description no
  longer matches.

**When amending:** the moment you change one file in a pair above, check its sibling for the same
fact before considering the edit done — don't wait for a later pass to notice the drift. Log the
change below: date, which file(s) you touched, and what changed (or point to the file's own
Amendment Log if it has one, per `learning-outline-template.md`'s own convention, rather than
duplicating the detail here).

#### Cross-Reference Log

| Date | Files touched | Change |
|---|---|---|
| 2026-08-03 | `README.md` (new) | Created this README describing all four templates and the outline→Module workflow; added the Amending-These-Templates/Cross-Reference-Log section |
| 2026-08-03 | `module-file-template.md`, `chatgpt-project-setting-instrctions.md` | Added a maintainer note to each pointing at its cross-reference partner (`module-file-quick-template.md`, `learning-outline-template.md` respectively). Kept `module-file-template.md`'s note self-contained (no path/filename citation to this README) since that file declares itself a copy-anywhere STANDALONE template — a repo-relative reference there would break that guarantee. `learning-outline-template.md` itself was left untouched for the same reason; its own Amendment Log already tracks propagation to the ChatGPT instructions file without naming this folder. |
| 2026-08-03 | `README.md` | Restructured into the repo's standard Orientation/Index two-section split (per the README-as-Manifest Blueprint), matching `docs/wiki/learned-lessons/README.md` and `docs/knowledge-base/project/README.md` — Workflow and Amending-These-Templates moved under Orientation, the file table renamed to Index. |
| 2026-08-03 | `docs/knowledge-base/project/readme-manifest-blueprint.md`, this file's Index, `docs/wiki/learned-lessons/README.md`, `docs/knowledge-base/project/README.md` | Added a `Sr. No.` column to the blueprint's Index skeleton (purely positional — answers "how many files/entries" from the last row's number, never used as a citable identifier) and propagated it to all three manifests conforming to the blueprint, so none of them silently drifted out of sync with the shape the blueprint now specifies. |
| 2026-08-03 | `docs/knowledge-base/learning/README.md`, `docs/knowledge-base/learning/git-github-ecosystem/README.md` | Follow-up sweep found two more manifests out of sync with the blueprint: `learning/README.md` had no `## Orientation` section at all (plain intro prose → `## Index` → a trailing `## Housekeeping` after the Index, instead of Housekeeping living under Orientation before it) and cited only the Manifest-and-Surrogate-Pattern article, not the Blueprint itself. Restructured it to match the other four conforming READMEs, added the missing Blueprint citation, and added `Sr. No.` to its Index (18 rows after also collapsing 3 flat `_templates/*.md` rows into one subfolder row now that `_templates/` has its own README). Added `Sr. No.` to `git-github-ecosystem/README.md`'s 20-row Index (its Orientation/Index structure was already correct — only the column was missing). |

---

## Index

The manifest — one row per template file, no prose beyond this table.

| Sr. No. | File | Purpose | Last Updated |
| :--- | :--- | :--- | :--- |
| 1 | [`learning-outline-template.md`](learning-outline-template.md) | The governing spec for one subject's outline skeleton: Claim/Caveat, Prerequisites, Levels of Module *titles only* (no content), a Mini Project, a Knowledge Dependency Graph, a Pareto Learning Order, and an Index table planning every Module's filename + one-line description. Includes its own "Reuse Prompt" for driving an LLM to generate the skeleton from just a subject name — one-off upload, one-off paste-inline, or a persistent ChatGPT Project setup. | 2026-07-30 |
| 2 | [`chatgpt-project-setting-instrctions.md`](chatgpt-project-setting-instrctions.md) | The persistent ChatGPT Project Instructions variant of that Reuse Prompt — paste once into a Project's Instructions field (with `learning-outline-template.md` uploaded to the Project's Sources) so every later chat only needs a bare subject name, with no re-pasting or re-uploading. | 2026-08-03 |
| 3 | [`module-file-template.md`](module-file-template.md) | The fully-annotated template for one Module's own file — read this copy once to learn the rules (Header → Body → Footer shape, Topic-structured prose, supporting devices, the self-authoring checklist), then switch to the quick version below for actual day-to-day writing. | 2026-08-03 |
| 4 | [`module-file-quick-template.md`](module-file-quick-template.md) | The bare Module-file skeleton with no guidance comments — copy this one when actually authoring a Module, fill in the placeholders, and paste your explanation into the single Body slot. | 2026-07-30 |
