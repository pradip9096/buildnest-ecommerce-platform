---
title: "SonarCloud's Log-Injection Rule (S5145) Can Fail the New-Code Gate Through a Pre-Existing Sink, and This Repo's SecureLogger Doesn't Cover It"
category: technical
tags: [sonarcloud, security, log-injection, javasecurity-s5145, quality-gate, new-code]
keywords: [S5145, log forging, CRLF injection, SecureLogger, new_security_rating, quality gate new code]
source_conversations: ["#554"]
last_updated: 2026-07-22
confidence: high
evidence_strength: direct-repo-verification
root_cause: "SonarCloud's per-PR Quality Gate evaluates security rating only over the New Code diff; a pre-existing, already-shared log sink (NotificationServiceImpl.send()'s log.debug) had never failed the gate for any prior caller, but adding one new caller that routes admin/user-supplied strings through it re-triggers the taint analysis and fails the gate on code the PR didn't touch at the sink itself — the vulnerability was already latent, just never scored"
impact: medium — blocked PR #568's merge on a required check (Code Quality Analysis) until root-caused and fixed at the shared sink, not just the new call site
related_lessons:
  - pit-mutation-testing-patterns.md
  - sonarcloud-new-code-gate-reattributes-pre-existing-findings-on-whole-file-rewrap.md
---

# SonarCloud's Log-Injection Rule (S5145) Can Fail the New-Code Gate Through a Pre-Existing Sink, and This Repo's SecureLogger Doesn't Cover It

## What happened

#554 added `NotificationServiceImpl.sendSellerVerificationDecision(email, businessName,
approved, rejectionReason)`, which passes admin-supplied (`rejectionReason`) and
account-derived (`email`, `businessName`) strings through to the existing shared `send()`
helper's `log.debug("Email sent to={} subject={}", to, subject)` call. SonarCloud's
`javasecurity:S5145` (log injection / CRLF log-forging) rule flagged this as a new
`VULNERABILITY`, dropping `new_security_rating` from A to B and failing the PR's Quality Gate
— a required, branch-protected check (`Code Quality Analysis`).

Two things made this non-obvious:

1. **The sink itself (`send()`'s `log.debug`) was not new code.** Every prior notification
   method (`sendOrderConfirmation`, `sendPasswordResetEmail`, etc.) already funnels a
   user-controlled `email` through the exact same `log.debug` call, and none of them had ever
   failed the gate. SonarCloud's PR analysis scores security rating over the *New Code* period
   only — adding one new caller into an old, shared sink is enough to newly trigger the taint
   flow's evaluation, even though the sink's own line didn't change. The vulnerability was
   already latent for every existing caller; it just had never been scored until a new call path
   walked through it.
2. **This repo already has a `SecureLogger` utility** (`util/SecureLogger.java`,
   `#1.6 MEDIUM - Sensitive Data Logging`) that masks emails/card numbers/SSNs/passwords/API
   keys via regex before logging — but it does not strip CR/LF characters, which is what S5145
   specifically checks for (forged/injected log entries via newline characters), a distinct
   concern from PII exposure. Skimming the class name alone ("secure logging already exists
   here") would have been a false reassurance that this class of finding was already handled.

## Root cause

New-code security-gate evaluation is scoped to the diff, but taint-flow analysis walks the
whole call graph — a PR only needs to add *one new edge* into an already-tainted sink to fail
the gate, regardless of how old that sink's own source line is.

## Fix

Sanitized at the actual sink rather than only the new call site, so every existing caller gets
the same protection going forward:

```java
log.debug("Email sent to={} subject={}",
        sanitizeForLog(to), sanitizeForLog(subject));
...
private static String sanitizeForLog(String value) {
    return value == null ? null : value.replaceAll("[\r\n]", "_");
}
```

Also fixed `SellerServiceImpl.updateVerificationStatus`'s own `log.info(..., newStatus)`, which
logs the raw request-body status string directly.

Distinct from [SonarCloud's "New Code" Quality Gate Is Git-Blame-Based](sonarcloud-new-code-gate-reattributes-pre-existing-findings-on-whole-file-rewrap.md): that lesson is about a **false positive** (a pre-existing finding wrongly re-scored as new via git-blame reattribution from an unrelated rewrap). This one is a **genuine new vulnerability** — the taint flow is real, the fix is real, and it should not be treated as "pre-existing, safe to merge past" just because the sink line itself predates this PR. Check which shape you're actually looking at (query the rule against `master`, per that lesson's own verification method) before assuming either.

## Generalizes beyond this repo

- When a static-analysis PR gate fails on a call into a pre-existing helper/sink, check whether
  the sink itself needs fixing (protecting all callers) rather than special-casing the new call
  site — the "new code" framing describes what triggered the check, not the correct scope of
  the fix.
- A named "secure logging" utility's scope should be verified against the *specific* rule that
  fired, not assumed from its name — PII masking and log-injection/CRLF-forging are two distinct
  concerns, and a utility built for one does not imply coverage of the other.
