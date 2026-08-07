---
title: A Service-Layer Integration Test Does Not Count as Controller Coverage for SonarCloud's new_coverage Gate
category: testing
tags: [sonarcloud, coverage, new-code-gate, controller, quality-gate]
keywords: [new_coverage, quality gate FAILED, controller 0% coverage, integration test calls service directly, patch coverage, codecov/patch]
source_conversations: [#88]
last_updated: 2026-08-07
confidence: high
evidence_strength: strong
root_cause: SonarCloud's new_coverage metric is computed from JaCoCo line-execution data per class file — a controller class's lines are only marked covered if execution actually passes through that class, not merely through the service it delegates to; a test that calls the service directly (bypassing the controller entirely) contributes zero coverage to the controller regardless of how thoroughly it exercises the underlying business logic.
impact: medium — PR #695 (#88) had a real SpringBootTest integration test (ReturnRequestIT) exercising the full create/approve/reject flow through the service layer, plus 10+ service-layer unit tests, and still failed SonarCloud's new_coverage gate (74.5% < 80% required) and codecov/patch, because the two new controller classes (AdminReturnController, UserOrderController's new endpoint) had 0% and 48% new-code coverage respectively — the integration test's thoroughness at the service layer gave no signal that the controller layer itself was untested
related_lessons: []
---

# A Service-Layer Integration Test Does Not Count as Controller Coverage for SonarCloud's new_coverage Gate

## Problem

When implementing a new feature that spans controller → service → repository, it's natural to
write:

- Unit tests for the service (mocked dependencies)
- One real integration test (`@SpringBootTest`) proving the full flow persists correctly end-to-end

Both of these can be thorough and pass every assertion, yet SonarCloud's `Code Quality Analysis`
CI check (and `codecov/patch`) still fails with `new_coverage` below the required threshold
(this repo's `Sonar way` gate requires ≥80% new-code coverage). The reason: if the integration
test calls the service directly —

```java
@Autowired
private ReturnService returnService;
...
returnService.createReturnRequest(userId, orderId, reason);  // bypasses the controller
```

— rather than driving the request through the real HTTP layer (`TestRestTemplate`, `MockMvc`, or
an actual running server), the controller class that would normally receive that request is never
executed at all during the test run. JaCoCo (and, downstream, SonarCloud) mark every line in that
controller method as uncovered, because coverage is measured per class from actual bytecode
execution — it has no way to credit a controller for "the thing it calls got tested."

On #88, `AdminReturnController` had 0/15 new lines covered and `UserOrderController`'s new
`createReturnRequest` endpoint had 13/25 uncovered, despite `ReturnServiceImpl` itself sitting at
~93% new-code coverage from the same PR's tests.

## Fix

Add a controller-level unit test alongside the service-level tests — this repo's existing
pattern is a plain (non-Spring-context) unit test that `mock()`s the service dependency and calls
the controller's methods directly:

```java
@Test
void updateReturnStatus_valid_returns200() {
    ReturnService returnService = mock(ReturnService.class);
    AdminReturnController controller = new AdminReturnController(returnService);
    when(returnService.updateReturnStatus(eq(1L), eq("APPROVED"), eq("notes")))
            .thenReturn(dto);

    var response = controller.updateReturnStatus(1L, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
}
```

This executes the controller class's own lines (parameter binding, status-code branches, error
mapping) directly, closing the coverage gap without needing a full Spring context or MockMvc.

## How to apply

Whenever a new feature adds both a controller method and a service method, write a coverage plan
that explicitly lists **which layer each planned test exercises** before implementing — don't
assume a strong integration test at one layer implies coverage at every layer it happens to call
through. If the integration test's own setup calls the service directly (common when the test's
purpose is proving persistence/transaction behavior, not the HTTP contract), that is a deliberate
choice that leaves the controller layer's own coverage as a separate, still-open item — treat it
as such rather than discovering the gap only when CI's coverage gate fails.
