# Architecture Decision Records (ADR)

This directory holds BuildNest's Architecture Decision Records, using the
[MADR](https://adr.github.io/madr/) (Markdown Any Decision Records) format,
current version 4.0.0.

## When an ADR gets written here

Referenced from
[`development-workflow.md`](../../../../.claude/rules/common/development-workflow.md)'s
Sequence table:

- **Step `solution-options-adr`** — whenever a real architectural or design
  decision is made (a genuine trade-off between competing approaches, not a
  self-evident implementation choice), write it up as a new numbered ADR
  here, following the [template](adr-template.md). This is where the
  decision's actual content lives — `development-workflow.md`'s own Notes
  cell only points here, it does not restate ADR content inline.
- **Step `update-docs`** — whenever a `solution-options-adr` decision
  produced a new ADR file in the same issue's work, that file is one of the
  docs enumerated at closing time, alongside README/RTM/SRS/SDD/CHANGELOG/
  test-plan.md.

## Numbering and naming

`NNNN-short-kebab-case-title.md`, four-digit zero-padded, sequential —
e.g. `0001-split-multi-seller-cart-into-per-seller-orders.md`. `0000` is
reserved for this directory's own genesis ADR (the decision to use MADR
itself), following MADR's own convention.

## Status values

`proposed` | `accepted` | `rejected` | `deprecated` | `superseded by ADR-NNNN`

## Index

| ADR | Title | Status | Issue |
| :--- | :--- | :--- | :--- |
| [0000](0000-use-markdown-any-decision-records.md) | Use Markdown Any Decision Records | accepted | — |
| [0001](0001-district-matching-strategy-for-location-based-seller-buyer-matching.md) | District-matching strategy for location-based seller-buyer matching | accepted | #561 |
| [0002](0002-playwright-as-the-e2e-testing-tool-migrating-off-selenium.md) | Playwright as the E2E testing tool, migrating off Selenium | accepted | #117 |

New entries are added here in the same edit that adds the ADR file — this
index is not backfilled from git history retroactively; it starts tracking
from `0000` onward.
