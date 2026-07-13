---
title: "Spring's Self-Invocation Proxy Gap Applies to Every AOP Annotation, Not Just `@Transactional` — SonarCloud `java:S6809` Catches It for `@Cacheable`"
category: technical
tags: [spring, aop, proxy, self-invocation, cacheable, transactional, sonarcloud, static-analysis]
keywords: [self-invocation bypasses proxy, this.method() ignores Cacheable, java S6809, call cacheable method via injected dependency, CGLIB proxy same-class call]
source_conversations: ["Session 2026-07-13, issue #84, PR #370"]
last_updated: 2026-07-13
confidence: high
evidence_strength: strong
root_cause: "Spring's default proxy-based AOP (JDK dynamic proxy or CGLIB) only intercepts calls that arrive through the proxy object; a method calling another method on 'this' within the same class bypasses the proxy entirely, so any AOP-driven annotation on the callee (not just @Transactional) is silently a no-op for that call path — the mechanism is generic to Spring proxy AOP, not specific to any one annotation"
impact: medium — ProductServiceImpl.getRelatedProducts called getProductById via 'this', silently skipping getProductById's own @Cacheable on every invocation; not a correctness bug (data was still right) but a real, undetected caching inefficiency that would have shipped unnoticed if SonarCloud's PR analysis hadn't flagged it
related_lessons:
  - docs/wiki/learned-lessons/checkstyle-ratchet-counts-whole-file-not-diff-add-javadoc-per-new-method.md
  - docs/wiki/learned-lessons/testing-cacheable-proxy-behavior-needs-cache-type-override-and-has-a-scope-limit.md
---

# Spring's Self-Invocation Proxy Gap Applies to Every AOP Annotation, Not Just `@Transactional` — SonarCloud `java:S6809` Catches It for `@Cacheable`

## Problem

`.claude/rules/spring/jpa.md` already documents the self-invocation gap, but scoped narrowly:

> Never call a `@Transactional` method from within the same class — Spring's proxy does not
> intercept self-calls, so the transaction annotation is silently ignored

PR #370 (#84) wrote `ProductServiceImpl.getRelatedProducts` (itself `@Cacheable`) to fetch the
source product via `getProductById(productId)` — a plain `this`-scoped call to a sibling method
on the same class, which is also `@Cacheable`. This compiled cleanly, passed all unit tests
(Mockito mocks the repository directly, so it can't observe proxy behavior at all), and looked
correct on review, since the existing repo rule only warns about `@Transactional`.

SonarCloud's PR analysis (`java:S6809`, `CRITICAL`) caught it: "Call cacheable methods via an
injected dependency instead of directly via `this`." The mechanism is identical to the
`@Transactional` case the repo already knows about — Spring's proxy (JDK dynamic proxy or CGLIB)
only intercepts calls that arrive *through the proxy object*. A same-class method call never goes
through the proxy, so **any** AOP-driven annotation on the callee is silently inert for that call
path: `@Cacheable`, `@Async`, `@Retryable`, `@PreAuthorize`, not just `@Transactional`. The repo's
existing wording under-scoped a general Spring mechanism as if it were transaction-specific.

## Rule

Treat the self-invocation proxy gap as a property of **Spring proxy-based AOP itself**, not of any
one annotation:

1. Never call an AOP-annotated method (`@Transactional`, `@Cacheable`, `@Async`, `@Retryable`,
   `@PreAuthorize`, etc.) from another method in the *same class* via a plain `this`-scoped call —
   the annotation silently does nothing for that call path.
2. If the callee's own annotation isn't actually needed for that specific call (as here — the
   related-products query doesn't need `getProductById`'s per-product cache, only its own
   `relatedProducts` cache), the simplest fix is to bypass the annotated method entirely and call
   the underlying repository/dependency directly, matching how this file's other methods
   (`deleteProduct`, `updateProductImage`) already fetch a product without going through
   `getProductById`.
3. If the callee's own annotation genuinely is needed, the standard fixes are: inject the bean
   into itself (`@Lazy` self-injection) and call through that reference, or move the annotated
   method to a separate collaborator class and call it via the injected dependency.
4. Unit tests with mocked dependencies (Mockito `@InjectMocks`) cannot catch this class of bug —
   mocking bypasses the real Spring proxy entirely, so the self-call "works" in the test the same
   way it would in production, annotation or not. Only a real Spring context test or a static
   analyzer (SonarCloud/SonarQube `java:S6809` for `@Cacheable`, similar rules exist for
   `@Transactional`/`@Async`) can catch it. This repo's SonarCloud integration is wired
   non-blocking in CI (per `development-workflow.md`'s CI Failure Handling), so a finding like this
   will not fail the build — it must be triaged by hand during PR self-review, exactly as this one
   was.

`.claude/rules/spring/jpa.md`'s existing rule should be read as the `@Transactional`-specific
instance of this general pattern, not the full scope of it.
