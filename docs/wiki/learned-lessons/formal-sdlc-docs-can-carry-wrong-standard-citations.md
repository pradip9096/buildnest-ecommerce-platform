---
title: "Formal SDLC Documents Can Carry Wrong Standard Citations, Copy-Pasted Across Files"
category: process
tags: [rtm, srs, owasp-asvs, standard-citation, documentation-accuracy]
keywords: [OWASP ASVS, requirements traceability matrix, software requirements specification, citation verification, compliance claim]
source_conversations: ["#327", "#339"]
last_updated: 2026-07-10
confidence: high
evidence_strength: direct-repo-verification
root_cause: "an unverified OWASP ASVS control citation was written once, then copy-pasted across the RTM and SRS, where the duplication itself was mistaken for corroboration instead of propagated error"
impact: high — a false compliance claim (wrong control, wrong timing value) sat in formal traceability docs an audit or future engineer could rely on as ground truth
related_lessons: [verify-issue-premises-against-repo-before-implementing]
---

## What happened

While completing the Definition of Done review for #327 (password reset email fix), the RTM row
for FR-AUTH-08 was checked against the actual code as a required step (auth is a risk-modifier
domain). It read: "tokens expire 15 min; OWASP ASVS 2.1.8." Neither half was true: the code's
actual default is 1 hour, and ASVS 2.1.8 is the **password strength meter** control — completely
unrelated to reset token expiration. The exact same wrong citation and wrong timing had also been
copy-pasted into `software-requirements-specification.md`'s equivalent row, and appears a third
time (unfixed, deliberately) in a dated, versioned "Controlled" report as a historical snapshot.

Verification (via direct fetch of `OWASP/ASVS`'s `0x11-V2-Authentication.md`) found the actually
applicable control is **2.5.6** ("secure recovery mechanism"), which doesn't even specify a timing
value — the closest ASVS control that does specify a number (2.7.2, 10 minutes) is scoped to
out-of-band verifiers/OTPs, not password reset links, so citing it directly would have been another
misapplication.

## Why it matters

A specific-sounding citation like "OWASP ASVS 2.1.8 compliant" reads as strong evidence of security
diligence — precise, sourced, standard-backed. That specificity is exactly what makes a wrong one
dangerous: it doesn't look like a guess, so it doesn't get double-checked, and once it's copied into
a second formal document (RTM → SRS), the duplication itself reads as corroboration rather than
propagated error. This is a compliance-integrity risk, not just a doc-quality nit — an audit or a
future engineer trusting the RTM's citation as ground truth would be relying on a false claim.

## How to apply

Any time a requirement/traceability document cites a specific standard control number as evidence
of compliance (OWASP ASVS, NIST, ISO, PCI-DSS, etc.), verify the control's actual text against the
primary source before trusting or propagating it — the same rule `~/.claude/CLAUDE.md`'s
"Verifying Standards" section already requires for citing standards in conversation applies with
full force to text already sitting in the repo's own formal docs, which is easy to assume was
already verified when it was written. When a wrong citation is found in one document, grep the rest
of the repo (RTM, SRS, gap-analysis reports, ADRs) for the same string — a citation error rarely
occurs in exactly one place once cross-document copying is in play. Dated, versioned "Controlled"
snapshot reports (matching this repo's CHANGELOG-style append-only convention) are the one
exception: correct the error going forward in living documents, but don't retroactively rewrite a
frozen historical report to reflect a later correction — note the error and move on.
