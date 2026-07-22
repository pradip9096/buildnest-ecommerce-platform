---
title: "@RequestParam Bean Validation Throws Before the Method Body Runs, Bypassing Its Own try/catch"
category: technical
tags: [spring, validation, exception-handling, rest-api]
keywords: [ConstraintViolationException, RequestParam, Validated, Min, bean validation, GlobalExceptionHandler, try-catch]
source_conversations: ["#487"]
last_updated: 2026-07-22
confidence: high
evidence_strength: verified — reproduced via a real @WebMvcTest hitting the endpoint with a negative value, confirmed the ConstraintViolationException log line and 400 response only appeared after the new handler was added
related_lessons: []
root_cause: "Method-parameter constraint annotations (e.g. @Min on a @RequestParam) are enforced by a method-level AOP validation interceptor that runs before the annotated method's body executes at all — so the ConstraintViolationException it throws is never seen by a try/catch inside that method, and instead propagates straight to whatever @ExceptionHandler (or the framework default) is registered for it."
impact: medium — without a registered handler, the endpoint returns 500 Internal Server Error instead of 400 Bad Request on invalid input, silently worse than doing no validation at all from a client's perspective
---

## What happened

Fixing #487 (adding `@Min(0)` to `AdminInventoryController.addStock()`/`updateStock()`'s
`@RequestParam Integer quantity`, to reject negative stock values) required also adding
`@Validated` at the class level — Spring only enforces constraint annotations on plain
(non-`@RequestBody`) method parameters when the containing bean is `@Validated`.

Both endpoint methods already had their own `try { ... } catch (Exception e) { return 400 }`
wrapping the whole body. It would be reasonable to assume this catches everything, including the
new validation failure. **It doesn't.**

## Why

`@Validated` at the class level installs a method-level AOP interceptor
(`MethodValidationInterceptor`) around every public method. This interceptor validates the
method's arguments *before* the target method is ever invoked. If a constraint fails, it throws
`ConstraintViolationException` from the interceptor itself — the annotated method's body, and
therefore its `try/catch`, never runs at all.

This repo had never used a bean-validation constraint directly on a `@RequestParam`/`@PathVariable`
before (only `@Valid @RequestBody` DTOs, which use a different validation path —
`MethodArgumentNotValidException`, already handled). So `GlobalExceptionHandler` had no
`@ExceptionHandler(ConstraintViolationException.class)` — the exception fell through to the
generic `catch (Exception e)` handler, returning **500 Internal Server Error** instead of the
intended 400.

## The fix

Add an explicit handler:

```java
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ErrorResponse> handleConstraintViolationException(
        ConstraintViolationException ex, WebRequest request) {
    // ... return 400, mirroring the existing MethodArgumentNotValidException handler
}
```

## Generalizes beyond this repo

Any Spring MVC/WebFlux controller adding a bean-validation constraint (`@Min`, `@NotBlank`,
`@Pattern`, etc.) directly to a `@RequestParam`/`@PathVariable`/`@RequestHeader` for the first
time needs both:

1. `@Validated` on the controller class (constraints on plain parameters are silently ignored
   without it — no error, the validation just never runs).
2. A registered `@ExceptionHandler(ConstraintViolationException.class)` (or an equivalent
   `@ControllerAdvice` handler) — otherwise the failure mode is *worse* than having no validation
   at all: the client sees a 500 instead of a clean 400, and any per-method `try/catch(Exception)`
   in the same class gives a false sense that it's already covered.

Before adding a method-parameter constraint annotation for the first time in a codebase, grep for
an existing `ConstraintViolationException` handler — if none exists, add one in the same change,
don't assume the generic exception handler is an adequate fallback.
