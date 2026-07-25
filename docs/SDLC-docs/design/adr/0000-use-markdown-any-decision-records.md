# 0000. Use Markdown Any Decision Records

* Status: accepted
* Date: 2026-07-26
* Issue: —

## Context and Problem Statement

BuildNest's `solution-options-adr` workflow step (`development-workflow.md`)
already requires real architectural decisions to be written up, but had no
dedicated file location or format — decisions have historically lived
scattered across Amendment Log prose, PR descriptions, and knowledge-base
articles, with no single place a reader can browse to see "what decisions
were made and why" independent of which document happened to record one.

## Decision Drivers

* Needs to be plain Markdown, consistent with every other SDLC doc in this
  repo (RTM/SRS/SDD/Test Plan/SDP are all Markdown, not a proprietary tool)
* Should be a recognized, tool-agnostic convention rather than an invented
  one-off format, so it's legible to anyone familiar with ADRs generally
* Low overhead — this repo already has extensive process ceremony
  (`development-workflow.md`'s own Amendment Log discipline); the ADR
  format itself should not add more

## Considered Options

* MADR (Markdown Any Decision Records)
* Michael Nygard's original ADR format (Title/Status/Context/Decision/
  Consequences, no separate "Considered Options" section)
* No dedicated format — keep recording decisions inline in Amendment Log
  entries and PR descriptions, as already happening

## Decision Outcome

Chosen option: "MADR", because it is the most widely recognized Markdown
ADR convention, is actively maintained (v4.0.0), and its "Considered
Options" / "Decision Drivers" sections map directly onto the trade-off
comparisons `solution-options-adr` already asks for (e.g. #578's
per-seller-order-splitting decision, #453's location-matching design) —
adopting it gives those write-ups a consistent shape instead of ad hoc
prose structure each time.

### Consequences

* Good, because future architectural decisions get one canonical,
  browsable location (this directory's index) instead of being scattered
* Good, because the format's "Considered Options" section directly
  encodes what `solution-options-adr`'s sibling-precedent check already
  requires recording (what was compared, what was rejected, why)
* Bad, because it adds one more artifact type to `update-docs`'s closing
  enumeration — mitigated by making it conditional: only when a
  `solution-options-adr` decision actually produced a new ADR in that
  issue's own work, not on every issue

## Pros and Cons of the Options

### MADR

* Good, because it is a maintained, versioned standard (adr.github.io)
* Good, because its minimal template's mandatory sections (Context and
  Problem Statement, Decision Drivers, Considered Options, Decision
  Outcome) are lightweight enough not to add meaningful ceremony
* Bad, because it is one more convention contributors need to learn

### Nygard's original format

* Good, because it is simpler (no separate options/drivers breakdown)
* Bad, because it doesn't have a dedicated place to record what
  alternatives were rejected and why — exactly the content
  `solution-options-adr`'s sibling-precedent check already needs recorded

### No dedicated format (status quo)

* Good, because it requires no new convention or directory
* Bad, because it is the status quo this ADR exists to fix — decisions
  are unrecoverable except by searching Amendment Log prose or PR history
