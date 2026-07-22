---
title: "SonarCloud's Log-Injection Rule (S5145) Can Fail the New-Code Gate Through a Pre-Existing Sink, and This Repo's SecureLogger Doesn't Cover It"
category: technical
tags: [sonarcloud, spotbugs, security, log-injection, javasecurity-s5145, np-null-on-some-path, quality-gate, new-code]
keywords: [S5145, log forging, CRLF injection, SecureLogger, new_security_rating, quality gate new code, NP_NULL_ON_SOME_PATH, defensive null check side effect]
source_conversations: ["#554"]
last_updated: 2026-07-22
confidence: high
evidence_strength: direct-repo-verification
root_cause: "SonarCloud's per-PR Quality Gate evaluates security rating only over the New Code diff; a pre-existing, already-shared log sink (NotificationServiceImpl.send()'s log.debug) had never failed the gate for any prior caller, but adding one new caller that routes admin/user-supplied strings through it re-triggers the taint analysis and fails the gate on code the PR didn't touch at the sink itself — the vulnerability was already latent, just never scored. Fixing it with a null-safety ternary then handed SpotBugs's dataflow analyzer proof that the same variable could be null, retroactively flagging a separate, pre-existing unguarded dereference of it later in the same method. A regex-based sanitizer for the original finding also didn't clear it, since SonarJava's javasecurity taint engine only recognizes a curated allowlist of sanitizer patterns, not arbitrary code — removing the tainted value from the sink was the only fix that actually registered"
impact: medium — blocked PR #568's merge on a required check (Code Quality Analysis) across three separate root causes in the same incident, each requiring its own investigation rather than a single obvious fix
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

## Follow-on: the S5145 fix itself introduced a real SpotBugs NPE finding

The first fix for `SellerServiceImpl.updateVerificationStatus`'s own log-injection sink used a
ternary purely to keep the log call null-safe:

```java
log.info("Admin: updating seller id={} verification to={}",
        sellerId, newStatus == null ? null
                : newStatus.replaceAll("[\r\n]", "_"));
```

CI's next run then failed a *different*, genuinely new check: SpotBugs (`NP_NULL_ON_SOME_PATH`,
High) — "Possible null pointer dereference of `newStatus`... Known null at line 103" — pointing
at the very next line, `newStatus.toUpperCase()`, which had never been guarded. This dereference
predates this PR (it was `#553`/`#554`-era code, unguarded from the start), but SpotBugs's
dataflow analysis had never flagged it before, because *nothing in the method previously proved
to the analyzer that `newStatus` could be null on some path*. Writing `newStatus == null ? ... :
...` for an unrelated reason (making one log line null-safe) is exactly that proof — it
retroactively turns a latent, previously-invisible NPE risk into a flagged one, on a completely
different line than the one just edited.

**Fix**: don't null-safe a symptom (the log call) while leaving the actual unguarded dereference
untouched — fail fast at the top of the method instead:

```java
if (newStatus == null) {
    throw new IllegalArgumentException("Invalid verification status: null");
}
log.info("Admin: updating seller id={} verification to={}",
        sellerId, newStatus.replaceAll("[\r\n]", "_"));
```

This also gave PIT a real, testable branch (`updateVerificationStatus_nullStatus_throwsIllegalArgument`),
recovering the mutation-score dip the log-injection ternary itself introduced (PIT mutates the
ternary's own `==` check, and a fixture that never calls the method with a null `newStatus` can't
kill that mutant).

**Generalizes**: adding a defensive null-check to satisfy one static-analysis tool (or just to be
"safe") on a variable that is dereferenced unconditionally *elsewhere in the same method* is not
free — it can hand a different, unrelated dataflow analyzer (or a mutation-testing tool) exactly
the signal it needed to flag every other unguarded use of that variable as newly provable. When a
null-check is added, grep the rest of the method for other uses of the same variable before
assuming the check is a self-contained, no-side-effect fix.

## Second follow-on: a custom regex "sanitizer" does not clear SonarJava's own taint rule

The `sanitizeForLog()` helper above (`value.replaceAll("[\r\n]", "_")`) is a real, functioning
fix for CRLF log-forging — but the *next* SonarCloud analysis still reported the exact same
`javasecurity:S5145` issue, on the exact same line, as if nothing had changed. Re-querying the
SonarCloud API directly (`api/issues/search?...&types=VULNERABILITY`) confirmed it was the same
issue, not a new one elsewhere.

**Why**: SonarJava's security-rule taint engine (the `javasecurity:*` rule family, distinct from
the general-purpose `java:*` rules) only clears a taint flow when the sanitizing call matches a
small, curated internal list of recognized sanitizer methods/patterns (specific JDK/library
encoders, a handful of known-safe idioms) — not any code that is *functionally* a sanitizer.
Calling `.replaceAll(...)` on the tainted value doesn't break the flow from the engine's
perspective, because `String.replaceAll` isn't on that recognized list; the engine still treats
the return value as tainted.

**Fix**: don't try to guess a sanitizer shape the engine will accept — remove the tainted value
from the log statement entirely:

```java
// Before (still flagged despite the regex "sanitizer")
log.debug("Email sent to={} subject={}", sanitizeForLog(to), sanitizeForLog(subject));

// After — no tainted data reaches the sink at all
log.debug("Email sent successfully");
```

**Generalizes**: for any SonarQube/SonarCloud `javasecurity:*` (or equivalent taint-based)
finding, verify a fix actually cleared the specific issue via the tool's own API/dashboard before
assuming a "reasonable-looking" mitigation (regex strip, custom escaping, a bespoke validator)
satisfies it — these rules check against a recognized-sanitizer allowlist, not general code
correctness, and a fix that is genuinely secure in practice can still fail to register as such.
When in doubt, the fastest reliable fix is removing the tainted value from the sink rather than
trying to launder it through a transformation the analyzer might not recognize.
