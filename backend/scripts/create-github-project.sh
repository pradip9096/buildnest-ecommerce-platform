#!/usr/bin/env bash
# create-github-project.sh
# Creates all GitHub labels, milestones, project, and 98 issues for BuildNest Platform.
# Idempotent: safe to re-run. Requires gh CLI authenticated with project scope.
set -euo pipefail

# ── Token: use stored gh token (has project scope); drop any injected GITHUB_TOKEN ──
unset GITHUB_TOKEN

REPO="pradip9096/buildnest-ecommerce-platform"
PROJECT_NAME="BuildNest Platform"
LOG_FILE="$(dirname "$0")/create-github-project.log"
ISSUE_URLS=()

log() { echo "[$(date '+%Y-%m-%dT%H:%M:%S')] $*" | tee -a "$LOG_FILE"; }

# ── Helpers ────────────────────────────────────────────────────────────────────

create_label() {
  local name="$1" color="$2" description="$3"
  gh label create "$name" --repo "$REPO" --color "$color" --description "$description" --force
  log "LABEL OK: $name"
}

create_milestone() {
  local title="$1" due="$2" description="$3"
  if gh api "repos/$REPO/milestones" --paginate | grep -qF "\"title\":\"$title\""; then
    log "MILESTONE EXISTS: $title"
  else
    gh api "repos/$REPO/milestones" -X POST \
      -f title="$title" -f due_on="${due}T23:59:59Z" -f description="$description"
    log "MILESTONE CREATED: $title"
  fi
}

create_issue() {
  local title="$1" milestone_title="$2" labels="$3" body="$4"
  if gh issue list --repo "$REPO" --limit 500 --state all | grep -qF "$title"; then
    log "ISSUE EXISTS: $title"
    local url
    url=$(gh issue list --repo "$REPO" --limit 500 --state all --json title,url \
      | python3 -c "import sys,json; issues=json.load(sys.stdin); print(next(i['url'] for i in issues if i['title']==sys.argv[1]))" "$title")
    ISSUE_URLS+=("$url")
    return
  fi
  local url
  url=$(gh issue create \
    --repo "$REPO" \
    --title "$title" \
    --milestone "$milestone_title" \
    --label "$labels" \
    --body "$body")
  ISSUE_URLS+=("$url")
  log "ISSUE CREATED: $title"
}

ensure_project() {
  local existing
  existing=$(gh project list --owner "pradip9096" --format json 2>/dev/null \
    | python3 -c "import sys,json; ps=json.load(sys.stdin); items=[p for p in ps.get('projects',[]) if p['title']=='$PROJECT_NAME']; print(items[0]['number'] if items else '')" 2>/dev/null || true)
  if [[ -n "$existing" ]]; then
    log "PROJECT EXISTS: $PROJECT_NAME (#$existing)"
    echo "$existing"
  else
    local number
    number=$(gh project create --owner "pradip9096" --title "$PROJECT_NAME" --format json 2>/dev/null \
      | python3 -c "import sys,json; print(json.load(sys.stdin)['number'])" 2>/dev/null || true)
    if [[ -n "$number" ]]; then
      log "PROJECT CREATED: $PROJECT_NAME (#$number)"
      echo "$number"
    else
      log "WARNING: Could not create/find project — token may need 'project' scope. Run: gh auth refresh -s project,read:project"
      echo ""
    fi
  fi
}

add_to_project() {
  local project_num="$1" url="$2"
  if [[ -z "$project_num" ]]; then
    log "SKIP PROJECT ADD (no project number): $url"
    return
  fi
  gh project item-add "$project_num" --owner "pradip9096" --url "$url" 2>/dev/null \
    && log "ADDED TO PROJECT: $url" \
    || log "ALREADY IN PROJECT (or skipped): $url"
}

# ── Labels ─────────────────────────────────────────────────────────────────────
log "=== PHASE: Labels ==="

# Type labels
create_label "type: bug"            "d73a4a" "Defect or incorrect behaviour"
create_label "type: feature"        "0075ca" "New capability or user story"
create_label "type: test"           "e4e669" "Test coverage or quality gate"
create_label "type: chore"          "cfd3d7" "Maintenance, refactor, dependency update"
create_label "type: docs"           "0052cc" "Documentation"
create_label "type: security"       "b60205" "Security hardening or vulnerability fix"
create_label "type: performance"    "f9d0c4" "Performance optimisation"
create_label "type: infrastructure" "bfd4f2" "CI/CD, DevOps, infrastructure"
create_label "type: config"         "fef2c0" "Configuration or environment"

# Priority labels
create_label "priority: critical"   "b60205" "Must ship in current milestone; blocks release"
create_label "priority: high"       "e4312b" "Important; target current milestone"
create_label "priority: medium"     "fbca04" "Standard priority"
create_label "priority: low"        "0e8a16" "Nice to have"

# Phase labels
create_label "phase: M1"  "1d76db" "Milestone 1 — Stabilisation"
create_label "phase: M2"  "0052cc" "Milestone 2 — Quality Foundation"
create_label "phase: M3"  "5319e7" "Milestone 3 — Technical Debt Reduction"
create_label "phase: M4"  "006b75" "Milestone 4 — Feature Development"
create_label "phase: M5"  "0e8a16" "Milestone 5 — Production Readiness"

# Domain labels
create_label "domain: auth"          "c5def5" "Authentication & authorisation"
create_label "domain: product"       "bfd4f2" "Product catalogue"
create_label "domain: order"         "d4c5f9" "Order management"
create_label "domain: cart"          "f9d0c4" "Cart & wishlist"
create_label "domain: payment"       "fef2c0" "Payment processing"
create_label "domain: inventory"     "c2e0c6" "Inventory management"
create_label "domain: search"        "e6f5d0" "Elasticsearch / search"
create_label "domain: notification"  "ffdacc" "Email / push notifications"
create_label "domain: analytics"     "d1bcf9" "Analytics & reporting"
create_label "domain: observability" "bfd4f2" "Monitoring, logging, tracing"
create_label "domain: frontend"      "fef2c0" "React / Vite UI"
create_label "domain: ci-cd"         "cfd3d7" "Pipelines & automation"

# ── Milestones ─────────────────────────────────────────────────────────────────
log "=== PHASE: Milestones ==="

create_milestone "M1 — Stabilisation"          "2026-07-04" "Fix 6 test defects; unblock CI green build. Phase gate: all tests pass, coverage ≥40%."
create_milestone "M2 — Quality Foundation"     "2026-07-18" "Raise JaCoCo gate to 50%, fix partial implementations, address CI/pom.xml gate discrepancy."
create_milestone "M3 — Technical Debt Reduction" "2026-08-01" "Resolve design gaps (DC-08 lazy fetch, SEC-14 CSP), upgrade Elasticsearch to 8.17+."
create_milestone "M4 — Feature Development"    "2026-10-24" "Implement all 20 pending features: payment E2E, notifications, analytics, admin, frontend MVP."
create_milestone "M5 — Production Readiness"   "2026-11-21" "Security hardening, 70% coverage, PIT mutation ≥75%, full observability, production deployment."

# ── GitHub Project ─────────────────────────────────────────────────────────────
log "=== PHASE: Project ==="
PROJECT_NUM=$(ensure_project)

# ── Issues — M1: Stabilisation (8 issues) ─────────────────────────────────────
log "=== PHASE: Issues — M1 ==="

create_issue \
  "[fix] auth: add missing @Mock RoleRepository in AuthServiceImplTest (DEF-001)" \
  "M1 — Stabilisation" \
  "type: bug,priority: critical,phase: M1,domain: auth" \
  "## Description
AuthServiceImplTest fails with NullPointerException because \`roleRepository\` field is declared but not annotated with \`@Mock\`, so Mockito never injects the mock.

## Affected File
\`src/test/java/com/example/buildnest_ecommerce/service/auth/AuthServiceImplTest.java\`

## Root Cause
Missing \`@Mock RoleRepository roleRepository;\` field declaration in the test class.

## Acceptance Criteria
- [ ] \`@Mock RoleRepository roleRepository;\` added to \`AuthServiceImplTest\`
- [ ] \`./mvnw test -Dtest=AuthServiceImplTest\` passes with zero failures
- [ ] No other test in \`service/auth/\` regresses

## Effort Estimate
0.25 h

## References
- RGAR §5, DEF-001
- RTM row AUTH-01"

create_issue \
  "[fix] test: add @Tag(\"e2e\") to ProductApiTest (DEF-002)" \
  "M1 — Stabilisation" \
  "type: bug,priority: critical,phase: M1,domain: product" \
  "## Description
\`ProductApiTest\` is an end-to-end integration test but is missing \`@Tag(\"e2e\")\`. The CI pipeline's \`integration-tests\` job filters on this tag; without it the test is excluded from the e2e run and included in the unit-test run, where it fails due to missing application context.

## Affected File
\`src/test/java/com/example/buildnest_ecommerce/controller/user/ProductApiTest.java\`

## Acceptance Criteria
- [ ] \`@Tag(\"e2e\")\` annotation added at class level
- [ ] \`./mvnw test -Dtest=ProductApiTest\` passes
- [ ] CI \`integration-tests\` job picks up the test correctly

## Effort Estimate
0.25 h

## References
- RGAR §5, DEF-002
- RTM row PROD-API-01"

create_issue \
  "[fix] test: add @Tag(\"e2e\") to OrderApiTest (DEF-003)" \
  "M1 — Stabilisation" \
  "type: bug,priority: critical,phase: M1,domain: order" \
  "## Description
Same root cause as DEF-002. \`OrderApiTest\` is missing \`@Tag(\"e2e\")\`, causing it to run in the wrong test phase and fail.

## Affected File
\`src/test/java/com/example/buildnest_ecommerce/controller/user/OrderApiTest.java\`

## Acceptance Criteria
- [ ] \`@Tag(\"e2e\")\` annotation added at class level
- [ ] \`./mvnw test -Dtest=OrderApiTest\` passes
- [ ] No other order tests regress

## Effort Estimate
0.25 h

## References
- RGAR §5, DEF-003
- RTM row ORD-API-01"

create_issue \
  "[fix] security: correct HTTP 401→403 assertion in AuthenticationAuthorizationSecurityTest:246 (DEF-004)" \
  "M1 — Stabilisation" \
  "type: bug,priority: critical,phase: M1,domain: auth" \
  "## Description
Line 246 of \`AuthenticationAuthorizationSecurityTest\` asserts \`equalTo(401)\` for a forbidden-resource scenario. Spring Security returns 403 (Forbidden) when the user is authenticated but lacks the required role/permission. The assertion must be corrected to \`equalTo(403)\`.

## Affected File
\`src/test/java/com/example/buildnest_ecommerce/security/AuthenticationAuthorizationSecurityTest.java\` — line 246

## Acceptance Criteria
- [ ] Assertion changed from \`equalTo(401)\` to \`equalTo(403)\` at line 246
- [ ] Test class passes: \`./mvnw test -Dtest=AuthenticationAuthorizationSecurityTest\`
- [ ] All other assertions in the file remain unchanged

## Effort Estimate
0.25 h

## References
- RGAR §5, DEF-004
- SRS SEC-05"

create_issue \
  "[fix] security: correct HTTP 401→400 assertion in InputValidationSecurityTest:168 (DEF-005)" \
  "M1 — Stabilisation" \
  "type: bug,priority: critical,phase: M1,domain: auth" \
  "## Description
Line 168 of \`InputValidationSecurityTest\` asserts \`equalTo(401)\` for a malformed-input scenario. The endpoint returns 400 (Bad Request) for invalid input before authentication is evaluated. The assertion must be corrected to \`equalTo(400)\`.

## Affected File
\`src/test/java/com/example/buildnest_ecommerce/security/InputValidationSecurityTest.java\` — line 168

## Acceptance Criteria
- [ ] Assertion changed from \`equalTo(401)\` to \`equalTo(400)\` at line 168
- [ ] \`./mvnw test -Dtest=InputValidationSecurityTest\` passes
- [ ] No other assertions regress

## Effort Estimate
0.25 h

## References
- RGAR §5, DEF-005
- SRS SEC-06"

create_issue \
  "[fix] security: correct HTTP 401→415 assertion in InputValidationSecurityTest:303 (DEF-006)" \
  "M1 — Stabilisation" \
  "type: bug,priority: critical,phase: M1,domain: auth" \
  "## Description
Line 303 of \`InputValidationSecurityTest\` asserts \`equalTo(401)\` for an unsupported-media-type scenario. The endpoint returns 415 (Unsupported Media Type). The assertion must be corrected.

## Affected File
\`src/test/java/com/example/buildnest_ecommerce/security/InputValidationSecurityTest.java\` — line 303

## Acceptance Criteria
- [ ] Assertion changed from \`equalTo(401)\` to \`equalTo(415)\` at line 303
- [ ] \`./mvnw test -Dtest=InputValidationSecurityTest\` passes
- [ ] Full security test suite green

## Effort Estimate
0.25 h

## References
- RGAR §5, DEF-006
- SRS SEC-06"

create_issue \
  "[chore] ci-cd: verify all tests pass after DEF-001–006 fixes and confirm CI green (M1 gate)" \
  "M1 — Stabilisation" \
  "type: chore,priority: critical,phase: M1,domain: ci-cd" \
  "## Description
After all six DEF-001–006 fixes are merged, run the full test suite and verify the CI pipeline reaches a green build. This issue tracks the M1 milestone gate sign-off.

## Acceptance Criteria
- [ ] \`./mvnw test\` exits 0 with zero failures
- [ ] CI \`build-and-test\` workflow is green on \`master\`
- [ ] JaCoCo report shows ≥40% instruction coverage (existing gate in pom.xml)
- [ ] No skipped tests remain that were previously failing

## Effort Estimate
0.5 h

## References
- SDP §6.3 M1 Gate Checklist
- RGAR §16"

create_issue \
  "[chore] ci-cd: align JaCoCo gate between pom.xml (40%) and ci.yml quality-gates job (90%)" \
  "M1 — Stabilisation" \
  "type: config,priority: high,phase: M1,domain: ci-cd" \
  "## Description
The \`quality-gates\` job in \`.github/workflows/ci.yml\` enforces 90% coverage, while \`pom.xml\` sets the JaCoCo minimum to 40%. This discrepancy means the CI gate will never pass until coverage reaches 90%, which is not achievable in M1. The CI gate must be lowered to 40% to match the current pom.xml target; it will be raised progressively through M2–M5.

## Acceptance Criteria
- [ ] \`ci.yml\` quality-gates job coverage threshold changed to 40%
- [ ] Progressive thresholds documented: M2→50%, M3→55%, M4→60%, M5→70%
- [ ] CI pipeline green after change

## Effort Estimate
0.5 h

## References
- RGAR §11.2
- SDP §9 (CI/CD)"

# ── Issues — M2: Quality Foundation (8 issues) ────────────────────────────────
log "=== PHASE: Issues — M2 ==="

create_issue \
  "[test] coverage: raise JaCoCo instruction coverage gate to 50% (M2 target)" \
  "M2 — Quality Foundation" \
  "type: test,priority: high,phase: M2,domain: ci-cd" \
  "## Description
Raise the JaCoCo minimum instruction coverage from 40% (M1) to 50% as per the SDP progressive gate schedule. Update both pom.xml and ci.yml simultaneously to keep them in sync.

## Acceptance Criteria
- [ ] \`pom.xml\` JaCoCo rule: \`<minimum>0.50</minimum>\`
- [ ] \`ci.yml\` quality-gates threshold: 50%
- [ ] \`./mvnw verify\` passes at or above 50%
- [ ] Coverage report published to \`target/site/jacoco/index.html\`

## Effort Estimate
2 h (writing tests to reach threshold)

## References
- SDP §9, Table 9-1
- RGAR §6 (partial: TIR-03)"

create_issue \
  "[test] auth: expand unit test coverage for AuthServiceImpl edge cases" \
  "M2 — Quality Foundation" \
  "type: test,priority: high,phase: M2,domain: auth" \
  "## Description
Increase unit test coverage for \`AuthServiceImpl\` to cover: expired tokens, revoked refresh tokens, duplicate registration, password reset token expiry, and concurrent login scenarios.

## Acceptance Criteria
- [ ] ≥10 new test methods added to \`AuthServiceImplTest\`
- [ ] Branch coverage for \`AuthServiceImpl\` ≥80%
- [ ] All new tests pass in isolation and in suite

## Effort Estimate
4 h

## References
- SRS AUTH-01 to AUTH-08
- RTM AUTH rows"

create_issue \
  "[test] product: expand unit test coverage for ProductServiceImpl" \
  "M2 — Quality Foundation" \
  "type: test,priority: medium,phase: M2,domain: product" \
  "## Description
Add tests for: product not found, category mismatch, out-of-stock handling, image upload validation, and pagination edge cases.

## Acceptance Criteria
- [ ] ≥8 new test methods in \`ProductServiceImplTest\`
- [ ] Branch coverage for \`ProductServiceImpl\` ≥75%
- [ ] All tests pass

## Effort Estimate
3 h

## References
- SRS PROD-01 to PROD-10"

create_issue \
  "[test] order: expand unit test coverage for OrderServiceImpl" \
  "M2 — Quality Foundation" \
  "type: test,priority: medium,phase: M2,domain: order" \
  "## Description
Add tests for: order cancellation, partial fulfillment, status transition validation, and concurrent order placement.

## Acceptance Criteria
- [ ] ≥8 new test methods in \`OrderServiceImplTest\`
- [ ] Branch coverage for \`OrderServiceImpl\` ≥75%
- [ ] All tests pass

## Effort Estimate
3 h

## References
- SRS ORD-01 to ORD-08"

create_issue \
  "[test] cart: add unit tests for CartService and WishlistService" \
  "M2 — Quality Foundation" \
  "type: test,priority: medium,phase: M2,domain: cart" \
  "## Description
CartService and WishlistService lack unit tests. Add coverage for: add/remove item, quantity update, cart merge on login, wishlist move-to-cart.

## Acceptance Criteria
- [ ] \`CartServiceImplTest\` created with ≥6 test methods
- [ ] \`WishlistServiceImplTest\` created with ≥4 test methods
- [ ] All tests pass

## Effort Estimate
3 h

## References
- SRS CART-01 to CART-06, WISH-01 to WISH-04"

create_issue \
  "[test] security: add integration tests for rate limiting behaviour" \
  "M2 — Quality Foundation" \
  "type: test,priority: medium,phase: M2,domain: auth" \
  "## Description
Verify Bucket4j rate limiting correctly returns 429 after threshold is exceeded for login and public product endpoints. Tests must use the H2/test profile (no Redis required).

## Acceptance Criteria
- [ ] \`RateLimitIntegrationTest\` created
- [ ] Test for login endpoint: >N requests → 429
- [ ] Test for product list endpoint: >N requests → 429
- [ ] Rate limit headers (\`X-RateLimit-Remaining\`, \`Retry-After\`) verified

## Effort Estimate
2 h

## References
- SRS NFR-SEC-05
- RTM NFR-RATELIMIT-01"

create_issue \
  "[chore] config: document all required environment variables with types and examples" \
  "M2 — Quality Foundation" \
  "type: docs,priority: medium,phase: M2,domain: ci-cd" \
  "## Description
\`backend/.env.example\` is incomplete. Every environment variable consumed by the application must be listed with: variable name, type, example value, required/optional flag, and security classification (secret vs non-secret).

## Acceptance Criteria
- [ ] All env vars from \`application.yml\` and \`application-test.yml\` documented in \`.env.example\`
- [ ] Security-critical vars marked clearly (JWT_SECRET, DB_PASSWORD, etc.)
- [ ] \`.env.example\` passes a lint check (no actual secrets)

## Effort Estimate
1 h

## References
- SDP Appendix B
- RGAR §13"

create_issue \
  "[chore] ci-cd: add dependency vulnerability scan (OWASP Dependency-Check) to CI pipeline" \
  "M2 — Quality Foundation" \
  "type: infrastructure,priority: medium,phase: M2,domain: ci-cd" \
  "## Description
Add OWASP Dependency-Check Maven plugin to the CI pipeline. Fail the build if any dependency has a CVSS score ≥7.0. Publish the HTML report as a CI artifact.

## Acceptance Criteria
- [ ] \`dependency-check-maven\` plugin added to \`pom.xml\` (scope: verify)
- [ ] CI workflow step added to run \`./mvnw verify -Powasp\`
- [ ] Report artifact uploaded in \`ci.yml\`
- [ ] Build fails correctly on high-severity CVE in test

## Effort Estimate
2 h

## References
- SDP §10 (Quality Assurance)
- SRS NFR-SEC-01"

# ── Issues — M3: Technical Debt Reduction (6 issues) ─────────────────────────
log "=== PHASE: Issues — M3 ==="

create_issue \
  "[fix] jpa: add explicit FetchType.LAZY to Category.products and Order.orderItems (DC-08)" \
  "M3 — Technical Debt Reduction" \
  "type: chore,priority: high,phase: M3,domain: product" \
  "## Description
\`Category.products\` (OneToMany) and \`Order.orderItems\` (OneToMany) use default fetch type, which is EAGER for some JPA providers and causes N+1 query issues in production-like loads. Explicit \`FetchType.LAZY\` must be set.

## Affected Files
- \`src/main/java/com/example/buildnest_ecommerce/model/entity/Category.java\`
- \`src/main/java/com/example/buildnest_ecommerce/model/entity/Order.java\`

## Acceptance Criteria
- [ ] \`@OneToMany(fetch = FetchType.LAZY)\` set on \`Category.products\`
- [ ] \`@OneToMany(fetch = FetchType.LAZY)\` set on \`Order.orderItems\`
- [ ] Existing integration tests pass (no LazyInitializationException)
- [ ] \`@Transactional\` added to any service method that iterates these collections

## Effort Estimate
2 h

## References
- RGAR §6, DC-08
- SRS NFR-PERF-02"

create_issue \
  "[security] csp: remove unsafe-inline from Content-Security-Policy header (SEC-14)" \
  "M3 — Technical Debt Reduction" \
  "type: security,priority: high,phase: M3,domain: auth" \
  "## Description
The current CSP header includes \`unsafe-inline\` for scripts and styles, which negates XSS protection. A nonce-based or hash-based CSP must replace it before M5 production readiness.

## Acceptance Criteria
- [ ] \`unsafe-inline\` removed from \`script-src\` and \`style-src\` directives
- [ ] Nonce or hash mechanism implemented for inline scripts/styles (if any)
- [ ] CSP validated with browser DevTools and \`securityheaders.com\` equivalent check
- [ ] No CSP violations in application logs after change

## Effort Estimate
4 h

## References
- RGAR §6, SEC-14
- SRS SEC-12, OWASP CSP Cheat Sheet"

create_issue \
  "[chore] elasticsearch: upgrade from 8.10 (EOL Oct 2024) to 8.17+ (M3 prerequisite)" \
  "M3 — Technical Debt Reduction" \
  "type: infrastructure,priority: critical,phase: M3,domain: search" \
  "## Description
Elasticsearch 8.10 reached end-of-life in October 2024 and no longer receives security patches. The application must be upgraded to 8.17+ before Phase 2 feature development begins (M4). This includes updating the Docker Compose image, the Spring Boot Elasticsearch client dependency, and any index mapping changes.

## Acceptance Criteria
- [ ] \`docker-compose.yml\` Elasticsearch image updated to \`8.17\` (or latest 8.x)
- [ ] \`pom.xml\` Elasticsearch client version aligned
- [ ] All Elasticsearch integration tests pass against 8.17
- [ ] Existing index mappings verified compatible (no breaking changes)
- [ ] \`ElasticsearchConfig\` updated if API changes require it

## Effort Estimate
4 h

## References
- RGAR §13.1
- SDP RISK-08"

create_issue \
  "[chore] resilience: verify and configure Circuit Breaker fallbacks for external service calls" \
  "M3 — Technical Debt Reduction" \
  "type: chore,priority: medium,phase: M3,domain: observability" \
  "## Description
Resilience4j Circuit Breaker is configured but fallback methods for payment gateway and notification service calls are not verified to be in place. Missing fallbacks cause cascading failures.

## Acceptance Criteria
- [ ] All \`@CircuitBreaker\` annotated methods have a corresponding \`fallbackMethod\`
- [ ] Fallback methods return graceful degradation responses (not exceptions)
- [ ] Integration test verifies fallback is invoked when external call fails
- [ ] Circuit breaker metrics exposed via Actuator

## Effort Estimate
3 h

## References
- SRS NFR-AVL-03
- RTM NFR-RESILIENCE-01"

create_issue \
  "[test] coverage: raise JaCoCo instruction coverage gate to 55% (M3 target)" \
  "M3 — Technical Debt Reduction" \
  "type: test,priority: high,phase: M3,domain: ci-cd" \
  "## Description
Raise the JaCoCo minimum instruction coverage from 50% (M2) to 55% as per the SDP progressive gate schedule.

## Acceptance Criteria
- [ ] \`pom.xml\` JaCoCo rule: \`<minimum>0.55</minimum>\`
- [ ] \`ci.yml\` quality-gates threshold: 55%
- [ ] \`./mvnw verify\` passes

## Effort Estimate
3 h (additional tests)

## References
- SDP §9, Table 9-1"

create_issue \
  "[chore] audit: verify AuditLog AOP aspect captures all admin and mutating operations" \
  "M3 — Technical Debt Reduction" \
  "type: chore,priority: medium,phase: M3,domain: analytics" \
  "## Description
The \`@Auditable\` AOP aspect exists but coverage of admin and mutating endpoints is not verified. All admin endpoints and all state-changing user endpoints must produce audit log entries.

## Acceptance Criteria
- [ ] All \`@PostMapping\`, \`@PutMapping\`, \`@DeleteMapping\`, \`@PatchMapping\` in \`admin/\` have \`@Auditable\`
- [ ] Integration test verifies audit entry is created for each audited operation
- [ ] Audit log entries include: user, action, resource, timestamp, IP address

## Effort Estimate
3 h

## References
- SRS ADM-06
- RTM AUDIT-01"

# ── Issues — M4: Feature Development (49 issues) ─────────────────────────────
log "=== PHASE: Issues — M4 ==="

create_issue \
  "[feature] payment: implement Stripe/payment-gateway E2E integration (PAY-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: critical,phase: M4,domain: payment" \
  "## Description
Payment processing is partially stubbed. Implement full Stripe (or configured gateway) E2E flow: create payment intent, confirm payment, handle webhook events (payment_intent.succeeded, payment_intent.payment_failed), update order status.

## Acceptance Criteria
- [ ] \`PaymentServiceImpl\` integrates with Stripe API via \`stripe-java\` SDK
- [ ] Webhook endpoint \`POST /api/v1/webhooks/payment\` validates Stripe signature
- [ ] Order status transitions to PAID on success, PAYMENT_FAILED on failure
- [ ] Idempotency key used for all payment intents
- [ ] Refund flow implemented
- [ ] Integration test with Stripe test mode keys passes

## Effort Estimate
12 h

## References
- SRS PAY-01 to PAY-06
- RTM PAY rows"

create_issue \
  "[feature] payment: implement payment refund API endpoint (PAY-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: payment" \
  "## Description
Admin must be able to issue full or partial refunds. Implement \`POST /api/v1/admin/orders/{id}/refund\` with amount validation, gateway refund call, and order/payment status update.

## Acceptance Criteria
- [ ] Endpoint accepts \`{ amount, reason }\` body
- [ ] Validates amount ≤ original payment amount
- [ ] Calls payment gateway refund API
- [ ] Updates Payment entity status to REFUNDED/PARTIALLY_REFUNDED
- [ ] Audit log entry created
- [ ] Unit and integration tests pass

## Effort Estimate
4 h

## References
- SRS PAY-04"

create_issue \
  "[feature] notification: implement email notification service with template engine (NOTIF-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: notification" \
  "## Description
\`NotificationService\` exists but email sending is not implemented. Integrate Spring Mail + Thymeleaf templates for: order confirmation, shipping update, password reset, registration welcome.

## Acceptance Criteria
- [ ] Spring Mail configured via \`MAIL_HOST\`, \`MAIL_PORT\`, \`MAIL_USERNAME\`, \`MAIL_PASSWORD\` env vars
- [ ] Thymeleaf HTML templates created for all 4 email types
- [ ] Emails sent asynchronously (\`@Async\`)
- [ ] Retry on transient SMTP failure (max 3 attempts)
- [ ] Unit tests mock JavaMailSender; integration test verifies template rendering

## Effort Estimate
6 h

## References
- SRS NOTIF-01 to NOTIF-04
- RTM NOTIF rows"

create_issue \
  "[feature] notification: implement in-app push notification support (NOTIF-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: notification" \
  "## Description
Add WebSocket or SSE-based in-app notification channel for real-time order status updates visible in the frontend.

## Acceptance Criteria
- [ ] SSE endpoint \`GET /api/v1/users/notifications/stream\` implemented
- [ ] Order status change events pushed via SSE
- [ ] Connection secured (JWT validated)
- [ ] Frontend (M4 stub) can connect and receive events
- [ ] Tests verify event emission on order update

## Effort Estimate
5 h

## References
- SRS NOTIF-05"

create_issue \
  "[feature] analytics: implement sales dashboard API (ANL-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: analytics" \
  "## Description
Implement \`GET /api/v1/admin/analytics/sales\` returning: total revenue, orders count, average order value, top products, top categories — all filterable by date range.

## Acceptance Criteria
- [ ] Endpoint accepts \`?from=&to=\` ISO-8601 date parameters
- [ ] Returns aggregated metrics from MySQL (JPA query or native query)
- [ ] Response cached with 5-minute TTL (\`@Cacheable\`)
- [ ] Paginated top-products list (max 20)
- [ ] Integration test with H2

## Effort Estimate
5 h

## References
- SRS ANL-01
- RTM ANL-01"

create_issue \
  "[feature] analytics: implement user behaviour analytics API (ANL-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: analytics" \
  "## Description
Track and expose user behaviour metrics: page views per product, cart abandonment rate, conversion funnel. Data ingested to Elasticsearch; queried via \`ElasticsearchIngestionService\`.

## Acceptance Criteria
- [ ] \`UserEventService\` records PRODUCT_VIEW, ADD_TO_CART, CHECKOUT_STARTED events to Elasticsearch
- [ ] \`GET /api/v1/admin/analytics/behaviour\` returns aggregated metrics
- [ ] Events are async (non-blocking to main request thread)
- [ ] Integration test verifies Elasticsearch ingestion

## Effort Estimate
6 h

## References
- SRS ANL-02"

create_issue \
  "[feature] analytics: implement inventory analytics and low-stock alerts (ANL-03)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: analytics" \
  "## Description
\`GET /api/v1/admin/analytics/inventory\` returns: stock levels by product, low-stock items (below configurable threshold), stockout risk, reorder recommendations. Low-stock threshold triggers alert via notification service.

## Acceptance Criteria
- [ ] Endpoint returns stock summary with low-stock flag
- [ ] Low-stock threshold configurable via \`inventory.low-stock-threshold\` property
- [ ] Alert sent when any product crosses threshold (single alert per crossing, not per request)
- [ ] Unit tests for threshold logic

## Effort Estimate
4 h

## References
- SRS ANL-03, INV-04"

create_issue \
  "[feature] admin: implement product CRUD admin endpoints (ADM-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: product" \
  "## Description
Complete admin product management: create product, update product details, update pricing, manage product images (upload/delete), soft-delete product.

## Acceptance Criteria
- [ ] \`POST /api/v1/admin/products\` — create with full validation
- [ ] \`PUT /api/v1/admin/products/{id}\` — update fields
- [ ] \`DELETE /api/v1/admin/products/{id}\` — soft delete (sets \`active=false\`)
- [ ] Image upload to configured storage (local/S3)
- [ ] \`@Auditable\` on all mutating endpoints
- [ ] Integration tests for all endpoints

## Effort Estimate
6 h

## References
- SRS ADM-01
- RTM ADM-01"

create_issue \
  "[feature] admin: implement category management admin endpoints (ADM-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: product" \
  "## Description
Admin CRUD for product categories including hierarchical category support (parent/child).

## Acceptance Criteria
- [ ] \`POST /api/v1/admin/categories\` — create with parent reference
- [ ] \`PUT /api/v1/admin/categories/{id}\` — update name, description, parent
- [ ] \`DELETE /api/v1/admin/categories/{id}\` — prevent delete if products exist in category
- [ ] \`@Auditable\` on mutating endpoints
- [ ] Integration tests pass

## Effort Estimate
4 h

## References
- SRS ADM-02"

create_issue \
  "[feature] admin: implement order management admin endpoints (ADM-03)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: order" \
  "## Description
Admin endpoints to view, filter, and update orders: list all orders with filter/sort/pagination, view order detail, update order status (PROCESSING→SHIPPED→DELIVERED), cancel order with reason.

## Acceptance Criteria
- [ ] \`GET /api/v1/admin/orders\` with filter params (\`status\`, \`userId\`, \`dateFrom\`, \`dateTo\`)
- [ ] \`GET /api/v1/admin/orders/{id}\` — full detail with items and payment
- [ ] \`PATCH /api/v1/admin/orders/{id}/status\` — status transition with validation
- [ ] Status transition rules enforced (cannot go backward)
- [ ] Notification sent to customer on status change
- [ ] Audit log on each status change

## Effort Estimate
5 h

## References
- SRS ADM-03, ORD-05"

create_issue \
  "[feature] admin: implement user management admin endpoints (ADM-04)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: auth" \
  "## Description
Admin endpoints to list, view, enable/disable, and assign roles to users.

## Acceptance Criteria
- [ ] \`GET /api/v1/admin/users\` — paginated list with search
- [ ] \`GET /api/v1/admin/users/{id}\` — full profile
- [ ] \`PATCH /api/v1/admin/users/{id}/status\` — enable/disable account
- [ ] \`PATCH /api/v1/admin/users/{id}/roles\` — assign/revoke roles
- [ ] Cannot disable own admin account
- [ ] \`@Auditable\` on all mutating endpoints
- [ ] Integration tests pass

## Effort Estimate
4 h

## References
- SRS ADM-04"

create_issue \
  "[feature] admin: implement audit log viewer admin endpoint (ADM-05)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: analytics" \
  "## Description
Admin endpoint to query audit logs stored in Elasticsearch with filtering by user, action type, date range, and resource ID.

## Acceptance Criteria
- [ ] \`GET /api/v1/admin/audit-logs\` with filter params
- [ ] Returns paginated results from Elasticsearch
- [ ] Results include: timestamp, user, action, resource, IP, outcome
- [ ] Non-admin access returns 403
- [ ] Integration test with Elasticsearch test container (or mock)

## Effort Estimate
3 h

## References
- SRS ADM-05, ADM-06"

create_issue \
  "[feature] admin: implement inventory management admin endpoints (ADM-06)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: inventory" \
  "## Description
Admin endpoints to view and adjust inventory levels, with full audit trail.

## Acceptance Criteria
- [ ] \`GET /api/v1/admin/inventory\` — list all with stock levels
- [ ] \`PATCH /api/v1/admin/inventory/{productId}\` — adjust quantity (delta, not absolute)
- [ ] Adjustment reason required
- [ ] Stock history tracked in \`InventoryAudit\` table (Liquibase changeset required)
- [ ] \`@Auditable\` on mutations
- [ ] Unit and integration tests

## Effort Estimate
5 h

## References
- SRS ADM-06, INV-01 to INV-04"

create_issue \
  "[feature] inventory: implement real-time inventory reservation during checkout (INV-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: inventory" \
  "## Description
Prevent overselling by reserving inventory when checkout is initiated and releasing reservation if payment fails or times out.

## Acceptance Criteria
- [ ] \`CheckoutService\` reserves inventory before payment
- [ ] Reservation times out after 15 minutes (scheduled job or TTL)
- [ ] Payment failure releases reservation
- [ ] Concurrent checkout handled with optimistic locking on \`Inventory.quantity\`
- [ ] Unit tests for reservation and release flows

## Effort Estimate
5 h

## References
- SRS INV-02, CHK-03"

create_issue \
  "[feature] search: implement full-text product search via Elasticsearch (SRCH-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: search" \
  "## Description
Implement \`GET /api/v1/products/search?q=&category=&minPrice=&maxPrice=&sort=\` backed by Elasticsearch for full-text search with faceted filtering.

## Acceptance Criteria
- [ ] Full-text search across product name, description, tags
- [ ] Filter by category, price range, availability
- [ ] Sort by relevance, price, rating, newest
- [ ] Paginated results
- [ ] Elasticsearch index created/updated via Liquibase or init script
- [ ] Integration test with Elasticsearch test container

## Effort Estimate
6 h

## References
- SRS SRCH-01 to SRCH-04"

create_issue \
  "[feature] search: implement product search index sync on product create/update/delete (SRCH-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: search" \
  "## Description
Elasticsearch index must stay in sync with MySQL product data. Implement synchronous index update on product create/update/delete, and a bulk re-index admin endpoint.

## Acceptance Criteria
- [ ] \`ProductEventListener\` (Spring ApplicationEvent) updates Elasticsearch on product change
- [ ] \`POST /api/v1/admin/search/reindex\` triggers full re-index
- [ ] Index update failures are logged and retried (Resilience4j)
- [ ] Unit test verifies index update is called on product save

## Effort Estimate
4 h

## References
- SRS SRCH-02"

create_issue \
  "[feature] checkout: implement multi-step checkout flow (CHK-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: order" \
  "## Description
Implement stateful checkout flow: address selection → shipping method → payment → confirmation. Each step validated before proceeding.

## Acceptance Criteria
- [ ] \`POST /api/v1/checkout/address\` — set delivery address
- [ ] \`POST /api/v1/checkout/shipping\` — select shipping method with cost calculation
- [ ] \`POST /api/v1/checkout/payment\` — initiate payment (delegates to PaymentService)
- [ ] \`POST /api/v1/checkout/confirm\` — finalise order
- [ ] Checkout session stored in Redis with 30-minute TTL
- [ ] Invalid step order returns 409 Conflict
- [ ] Integration tests for happy path and invalid transitions

## Effort Estimate
8 h

## References
- SRS CHK-01 to CHK-05"

create_issue \
  "[feature] checkout: implement coupon/discount code application at checkout (CHK-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: order" \
  "## Description
Allow customers to apply discount codes at checkout. Validate code, calculate discount, apply to order total.

## Acceptance Criteria
- [ ] \`Coupon\` entity and Liquibase changeset
- [ ] \`POST /api/v1/checkout/coupon\` — apply coupon to active checkout session
- [ ] Validate: code exists, not expired, usage limit not exceeded, minimum order value met
- [ ] Single coupon per order
- [ ] Admin endpoint to create/deactivate coupons
- [ ] Unit tests for validation rules

## Effort Estimate
5 h

## References
- SRS CHK-06"

create_issue \
  "[feature] user: implement user address book management (USR-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: auth" \
  "## Description
Users need to manage multiple delivery addresses. Implement CRUD for user address book with default address support.

## Acceptance Criteria
- [ ] \`Address\` entity and Liquibase changeset
- [ ] \`GET/POST/PUT/DELETE /api/v1/users/addresses\`
- [ ] Max 5 addresses per user
- [ ] One address can be marked as default
- [ ] Address used in checkout (pre-populated)
- [ ] Unit and integration tests

## Effort Estimate
3 h

## References
- SRS USR-03"

create_issue \
  "[feature] user: implement order history and order detail endpoints (USR-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: order" \
  "## Description
Customer-facing order history and detail viewing. Paginated list and detailed view of individual orders with items, pricing, status, and tracking.

## Acceptance Criteria
- [ ] \`GET /api/v1/users/orders\` — paginated, filterable by status
- [ ] \`GET /api/v1/users/orders/{id}\` — full detail
- [ ] User can only see their own orders (enforced in service layer)
- [ ] Tracking number included when available
- [ ] Integration tests verify ownership enforcement

## Effort Estimate
3 h

## References
- SRS USR-02, ORD-06"

create_issue \
  "[feature] user: implement product review and rating system (REV-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: product" \
  "## Description
Allow customers to submit, edit, and delete reviews for purchased products. Aggregate ratings displayed on product detail.

## Acceptance Criteria
- [ ] \`POST /api/v1/users/products/{id}/reviews\` — requires verified purchase
- [ ] \`PUT/DELETE /api/v1/users/reviews/{id}\` — own reviews only
- [ ] \`GET /api/v1/products/{id}/reviews\` — public, paginated
- [ ] Star rating 1–5 validated
- [ ] Aggregate rating \`(avg, count)\` returned in \`ProductDTO\`
- [ ] Admin can remove reviews (\`DELETE /api/v1/admin/reviews/{id}\`)
- [ ] Integration tests for purchase verification

## Effort Estimate
5 h

## References
- SRS REV-01 to REV-04"

create_issue \
  "[feature] product: implement product variant support (size, colour) (PROD-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: product" \
  "## Description
Products need size/colour/material variants with independent inventory tracking per variant.

## Acceptance Criteria
- [ ] \`ProductVariant\` entity with Liquibase changeset
- [ ] Variants linked to parent \`Product\`
- [ ] Each variant has own inventory
- [ ] \`ProductDTO\` includes variant list
- [ ] Cart items reference specific variant
- [ ] Admin can manage variants via admin product endpoint
- [ ] Integration tests for variant CRUD

## Effort Estimate
7 h

## References
- SRS PROD-05"

create_issue \
  "[feature] product: implement product image management (upload, reorder, delete) (PROD-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: product" \
  "## Description
Products can have multiple images. Support upload, reorder by display position, and delete. Images stored in configured storage backend (local filesystem or S3).

## Acceptance Criteria
- [ ] \`ProductImage\` entity with \`displayOrder\` field, Liquibase changeset
- [ ] \`POST /api/v1/admin/products/{id}/images\` — multipart upload
- [ ] \`PATCH /api/v1/admin/products/{id}/images/reorder\` — accepts ordered ID list
- [ ] \`DELETE /api/v1/admin/products/{id}/images/{imageId}\`
- [ ] Storage backend abstracted behind \`StorageService\` interface
- [ ] Tests mock \`StorageService\`

## Effort Estimate
4 h

## References
- SRS PROD-06"

create_issue \
  "[feature] product: implement product tagging and filtering (PROD-03)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: product" \
  "## Description
Add tag-based product classification. Admin assigns tags; customers filter by tag on product list.

## Acceptance Criteria
- [ ] \`ProductTag\` entity (many-to-many with \`Product\`), Liquibase changeset
- [ ] \`GET /api/v1/products?tag=\` filter parameter
- [ ] Admin CRUD for tags
- [ ] Tags included in Elasticsearch index (search integration)
- [ ] Integration tests

## Effort Estimate
3 h

## References
- SRS PROD-07"

create_issue \
  "[feature] product: implement related products recommendation (PROD-04)" \
  "M4 — Feature Development" \
  "type: feature,priority: low,phase: M4,domain: product" \
  "## Description
\`GET /api/v1/products/{id}/related\` returns up to 8 related products based on same category and shared tags.

## Acceptance Criteria
- [ ] Returns products from same category first, then same tags
- [ ] Excludes the source product
- [ ] Excludes inactive/out-of-stock products
- [ ] Result cached (5-minute TTL)
- [ ] Unit test for ranking logic

## Effort Estimate
2 h

## References
- SRS PROD-08"

create_issue \
  "[feature] cart: implement persistent cart (survives logout/login) (CART-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: cart" \
  "## Description
Cart must persist across sessions. Anonymous cart (Redis) should merge with database cart on login.

## Acceptance Criteria
- [ ] Anonymous cart stored in Redis with session key
- [ ] On login, anonymous cart merged with user cart (quantity summed, capped at max)
- [ ] Cart persisted in MySQL for logged-in users
- [ ] Cart items referencing deleted products automatically removed
- [ ] Integration test verifies merge behaviour

## Effort Estimate
4 h

## References
- SRS CART-03"

create_issue \
  "[feature] wishlist: implement wishlist share link (WISH-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: low,phase: M4,domain: cart" \
  "## Description
Users can generate a public share link for their wishlist viewable by unauthenticated users.

## Acceptance Criteria
- [ ] \`POST /api/v1/users/wishlist/share\` generates a short token
- [ ] \`GET /api/v1/wishlist/shared/{token}\` returns public view (no PII)
- [ ] Token expires after 7 days
- [ ] Token stored in Redis
- [ ] Unit test for token generation and expiry

## Effort Estimate
2 h

## References
- SRS WISH-05"

create_issue \
  "[feature] shipping: implement shipping method and cost calculation service (SHIP-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: order" \
  "## Description
Calculate shipping cost based on delivery address zone, total weight, and selected shipping method (Standard, Express, Same-day).

## Acceptance Criteria
- [ ] \`ShippingMethod\` entity and Liquibase changeset
- [ ] \`GET /api/v1/checkout/shipping-options\` returns available methods with costs for active cart
- [ ] Cost calculation based on weight + zone (configurable rate matrix)
- [ ] Admin can manage shipping methods and rate matrix
- [ ] Unit tests for cost calculation

## Effort Estimate
5 h

## References
- SRS SHIP-01 to SHIP-03"

create_issue \
  "[feature] returns: implement order return and refund request flow (RET-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: order" \
  "## Description
Customers can request returns within the return window. Admin reviews and approves/rejects. Approved returns trigger refund.

## Acceptance Criteria
- [ ] \`ReturnRequest\` entity and Liquibase changeset
- [ ] \`POST /api/v1/users/orders/{id}/returns\` — create return request (within 30 days of delivery)
- [ ] \`GET /api/v1/admin/returns\` — admin list with filter
- [ ] \`PATCH /api/v1/admin/returns/{id}/status\` — approve/reject
- [ ] Approved return → triggers PaymentService refund
- [ ] Inventory restored on approved return
- [ ] Integration tests for full flow

## Effort Estimate
6 h

## References
- SRS RET-01 to RET-03"

create_issue \
  "[feature] webhook: implement webhook subscription and delivery for order events (WH-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: notification" \
  "## Description
\`WebhookSubscription\` entity exists but delivery is not implemented. Deliver HTTP POST events to subscriber URLs for: ORDER_PLACED, ORDER_SHIPPED, ORDER_DELIVERED, PAYMENT_RECEIVED.

## Acceptance Criteria
- [ ] \`WebhookDeliveryService\` sends signed HTTP POST to subscriber URL
- [ ] HMAC-SHA256 signature in \`X-BuildNest-Signature\` header
- [ ] Retry up to 3 times on HTTP 5xx or timeout
- [ ] Delivery status logged (success/failed attempt count)
- [ ] Admin endpoint to list and deactivate webhooks
- [ ] Integration test verifies delivery and signature

## Effort Estimate
5 h

## References
- SRS WH-01 to WH-03"

create_issue \
  "[feature] auth: implement OAuth2 social login (Google) (AUTH-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: auth" \
  "## Description
Allow customers to register/login via Google OAuth2. Link social identity to existing account if email matches.

## Acceptance Criteria
- [ ] Spring Security OAuth2 Client configured for Google
- [ ] \`/oauth2/authorize/google\` initiates flow
- [ ] On callback, create or link user account
- [ ] JWT issued on successful OAuth2 login (same as password login)
- [ ] Unit test for account linking logic

## Effort Estimate
5 h

## References
- SRS AUTH-09"

create_issue \
  "[feature] auth: implement two-factor authentication (TOTP) (AUTH-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: auth" \
  "## Description
Optional TOTP-based 2FA for user accounts. QR code provisioning, TOTP verification at login, recovery codes.

## Acceptance Criteria
- [ ] \`POST /api/v1/users/2fa/enable\` — generates TOTP secret and QR code
- [ ] \`POST /api/v1/users/2fa/verify\` — verifies TOTP before enabling
- [ ] Login flow: if 2FA enabled, require TOTP code after password
- [ ] 8 one-time recovery codes generated on enable
- [ ] \`POST /api/v1/users/2fa/disable\` — requires TOTP verification
- [ ] Unit tests for TOTP validation

## Effort Estimate
6 h

## References
- SRS AUTH-10"

create_issue \
  "[feature] frontend: implement React product listing page (FE-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: frontend" \
  "## Description
Build the product listing page in React 19/Vite: grid layout, search bar, category filter sidebar, sort dropdown, pagination.

## Acceptance Criteria
- [ ] Fetches from \`GET /api/v1/products\` with filter params
- [ ] Responsive grid (1/2/3/4 columns by breakpoint)
- [ ] Category filter sidebar (multiselect)
- [ ] Sort by: relevance, price asc/desc, newest
- [ ] Pagination component
- [ ] Loading skeleton and error state
- [ ] TypeScript, no ESLint errors

## Effort Estimate
6 h

## References
- SRS UI-01"

create_issue \
  "[feature] frontend: implement React product detail page (FE-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: frontend" \
  "## Description
Product detail page: image gallery, variant selector, add-to-cart, reviews section, related products.

## Acceptance Criteria
- [ ] Image gallery with thumbnail navigation
- [ ] Variant selector updates price and stock display
- [ ] Add-to-cart button (quantity selector)
- [ ] Reviews section with pagination
- [ ] Related products row
- [ ] SEO meta tags (\`<title>\`, \`og:*\`)

## Effort Estimate
5 h

## References
- SRS UI-02"

create_issue \
  "[feature] frontend: implement React cart and checkout pages (FE-03)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: frontend" \
  "## Description
Cart page and multi-step checkout flow UI (address → shipping → payment → confirmation).

## Acceptance Criteria
- [ ] Cart page: item list, quantity controls, remove, coupon code field, total
- [ ] Checkout stepper: address form, shipping selector, payment form (Stripe Elements), confirmation
- [ ] Form validation (client-side + server error display)
- [ ] Order confirmation page with order number

## Effort Estimate
8 h

## References
- SRS UI-03"

create_issue \
  "[feature] frontend: implement React user account dashboard (FE-04)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: frontend" \
  "## Description
User account dashboard: profile, address book, order history, wishlist, 2FA settings.

## Acceptance Criteria
- [ ] Tabbed layout (Profile / Addresses / Orders / Wishlist / Security)
- [ ] Order history with status badges and detail modal
- [ ] Address CRUD form
- [ ] Wishlist with move-to-cart
- [ ] 2FA enable/disable flow

## Effort Estimate
6 h

## References
- SRS UI-04"

create_issue \
  "[feature] frontend: implement React admin dashboard (FE-05)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: frontend" \
  "## Description
Admin dashboard: sales charts, inventory table, order management table, user list, audit log viewer.

## Acceptance Criteria
- [ ] Protected route (ADMIN role only)
- [ ] Sales chart (Recharts or similar) with date range picker
- [ ] Orders table with status filter and status-update action
- [ ] Inventory table with adjustment modal
- [ ] User list with enable/disable action
- [ ] Audit log table with filters

## Effort Estimate
8 h

## References
- SRS UI-05"

create_issue \
  "[feature] frontend: implement authentication pages (login, register, password reset) (FE-06)" \
  "M4 — Feature Development" \
  "type: feature,priority: high,phase: M4,domain: frontend" \
  "## Description
Auth flow UI: login page, registration page, forgot-password page, reset-password page, email verification page.

## Acceptance Criteria
- [ ] Login: email/password form + Google OAuth button
- [ ] Registration: full validation (password strength, email format)
- [ ] Forgot password: email input → sends reset link
- [ ] Reset password: token from URL, new password form
- [ ] Email verification: token from URL → auto-verify
- [ ] JWT stored in httpOnly cookie (not localStorage)

## Effort Estimate
5 h

## References
- SRS UI-06, AUTH-01–AUTH-05"

create_issue \
  "[test] integration: add integration tests for payment flow (M4)" \
  "M4 — Feature Development" \
  "type: test,priority: high,phase: M4,domain: payment" \
  "## Description
Integration tests for the complete payment flow using Stripe test mode: create intent, confirm, webhook event handling, order status update, refund.

## Acceptance Criteria
- [ ] \`PaymentIntegrationTest\` with H2 + Stripe test mode
- [ ] Tests: successful payment, failed payment, refund
- [ ] Webhook signature validation tested
- [ ] All tests pass in CI

## Effort Estimate
4 h

## References
- SRS PAY-01 to PAY-06"

create_issue \
  "[test] integration: add integration tests for checkout flow (M4)" \
  "M4 — Feature Development" \
  "type: test,priority: high,phase: M4,domain: order" \
  "## Description
Integration tests covering the full multi-step checkout: address → shipping → payment → confirmation, including error paths.

## Acceptance Criteria
- [ ] \`CheckoutIntegrationTest\` using H2 + MockMvc
- [ ] Happy path: full checkout completes with order created
- [ ] Error paths: invalid step order, insufficient inventory, failed payment
- [ ] All tests pass

## Effort Estimate
3 h

## References
- SRS CHK-01 to CHK-05"

create_issue \
  "[test] integration: add integration tests for search and Elasticsearch (M4)" \
  "M4 — Feature Development" \
  "type: test,priority: medium,phase: M4,domain: search" \
  "## Description
Integration tests for Elasticsearch search functionality using a test container.

## Acceptance Criteria
- [ ] \`SearchIntegrationTest\` using Testcontainers Elasticsearch
- [ ] Tests: keyword search, category filter, price range filter, pagination
- [ ] Index sync test: product create → searchable
- [ ] All tests pass

## Effort Estimate
3 h

## References
- SRS SRCH-01 to SRCH-04"

create_issue \
  "[test] coverage: raise JaCoCo instruction coverage gate to 60% (M4 target)" \
  "M4 — Feature Development" \
  "type: test,priority: high,phase: M4,domain: ci-cd" \
  "## Description
Raise the JaCoCo minimum instruction coverage from 55% (M3) to 60% as per the SDP progressive gate schedule. New M4 feature code must have corresponding tests to meet this threshold.

## Acceptance Criteria
- [ ] \`pom.xml\` JaCoCo rule: \`<minimum>0.60</minimum>\`
- [ ] \`ci.yml\` quality-gates threshold: 60%
- [ ] \`./mvnw verify\` passes

## Effort Estimate
5 h (additional tests for M4 features)

## References
- SDP §9, Table 9-1"

create_issue \
  "[feature] api: implement API versioning strategy for v2 product endpoints (API-01)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: product" \
  "## Description
\`ProductControllerV2\` exists but the v1→v2 migration path and deprecation timeline are not formally implemented. Ensure sunset headers are correct, V2 differences are documented, and the deprecation date is set.

## Acceptance Criteria
- [ ] \`ApiSunsetInterceptor\` correctly adds \`Sunset\` and \`Deprecation\` headers to all v1 product endpoints
- [ ] \`Sunset\` date set to M5 completion date (2026-11-21)
- [ ] V2 endpoint differences documented in OpenAPI spec
- [ ] Integration test verifies deprecation headers on v1 responses

## Effort Estimate
2 h

## References
- SRS NFR-MAINT-04"

create_issue \
  "[feature] api: generate and publish OpenAPI 3.1 specification (API-02)" \
  "M4 — Feature Development" \
  "type: feature,priority: medium,phase: M4,domain: ci-cd" \
  "## Description
All endpoints must be documented in an OpenAPI 3.1 spec generated from \`springdoc-openapi\`. Published as part of CI and accessible at \`/swagger-ui.html\` in development.

## Acceptance Criteria
- [ ] \`springdoc-openapi-starter-webmvc-ui\` added to \`pom.xml\`
- [ ] All controllers annotated with \`@Operation\`, \`@ApiResponse\`, \`@Parameter\` where needed
- [ ] \`/v3/api-docs\` returns valid OpenAPI 3.1 JSON
- [ ] CI step generates and saves \`openapi.json\` as artifact
- [ ] Swagger UI accessible in dev profile

## Effort Estimate
4 h

## References
- SRS NFR-MAINT-01"

create_issue \
  "[chore] db: create Liquibase changesets for all M4 new entities" \
  "M4 — Feature Development" \
  "type: chore,priority: critical,phase: M4,domain: ci-cd" \
  "## Description
All new entities introduced in M4 (ProductVariant, ProductImage, Address, Coupon, ShippingMethod, ReturnRequest, InventoryAudit, ProductTag) require Liquibase changesets before code merges. DDL-auto is set to \`validate\` — missing changesets will cause startup failure.

## Acceptance Criteria
- [ ] One changeset file per new entity (naming: \`YYYYMMDD-NNN-description.xml\`)
- [ ] Each changeset includes: table creation, indices, foreign keys, and rollback tag
- [ ] \`./mvnw spring-boot:run\` starts successfully with all new changesets applied
- [ ] Changeset IDs are unique across all changelog files

## Effort Estimate
4 h

## References
- SDP §7.2 (Database Management)
- All M4 entity feature issues"

create_issue \
  "[feature] performance: implement query result caching strategy with Redis (PERF-01)" \
  "M4 — Feature Development" \
  "type: performance,priority: medium,phase: M4,domain: product" \
  "## Description
Cache frequently read, rarely changing data: product list, category tree, top products. Define TTLs appropriate to data volatility and implement cache eviction on write.

## Acceptance Criteria
- [ ] \`@Cacheable\` applied to: product list (TTL 5 min), category tree (TTL 30 min), product detail (TTL 10 min)
- [ ] \`@CacheEvict\` applied on product create/update/delete
- [ ] Cache key strategy avoids collisions with filters/pagination
- [ ] Integration test verifies cache hit reduces DB calls
- [ ] Cache miss degrades gracefully (no exception)

## Effort Estimate
3 h

## References
- SRS NFR-PERF-01"

create_issue \
  "[feature] performance: add database index optimisation for high-traffic queries (PERF-02)" \
  "M4 — Feature Development" \
  "type: performance,priority: medium,phase: M4,domain: ci-cd" \
  "## Description
Analyse query plans for the top 10 queries by frequency. Add missing indices via Liquibase changesets where missing.

## Acceptance Criteria
- [ ] Query analysis documented (EXPLAIN output or Hibernate statistics)
- [ ] Missing indices identified and added
- [ ] Liquibase changesets for all new indices
- [ ] Page load latency for product list \`<200ms\` (P95) under light load

## Effort Estimate
3 h

## References
- SRS NFR-PERF-02"

create_issue \
  "[feature] observability: implement structured JSON logging (OBS-01)" \
  "M4 — Feature Development" \
  "type: infrastructure,priority: medium,phase: M4,domain: observability" \
  "## Description
Replace default Spring Boot logback pattern with structured JSON output compatible with Logstash/ELK ingestion. Include: timestamp, level, service, traceId, spanId, message, exception.

## Acceptance Criteria
- [ ] \`logstash-logback-encoder\` added to \`pom.xml\`
- [ ] \`logback-spring.xml\` configured for JSON output in non-dev profiles
- [ ] Human-readable output retained for \`local\` profile
- [ ] \`traceId\` and \`spanId\` from Micrometer Tracing included in log context
- [ ] Kibana can parse log entries (verified manually)

## Effort Estimate
2 h

## References
- SRS NFR-OPS-03
- RTM MON-03"

create_issue \
  "[feature] observability: implement distributed tracing with Micrometer + Zipkin/Tempo (OBS-02)" \
  "M4 — Feature Development" \
  "type: infrastructure,priority: medium,phase: M4,domain: observability" \
  "## Description
Add distributed tracing to propagate trace context across service calls. Export traces to Zipkin (or Grafana Tempo if available).

## Acceptance Criteria
- [ ] \`micrometer-tracing-bridge-otel\` and OTLP exporter added
- [ ] Trace context propagated on all HTTP requests and async tasks
- [ ] Traces visible in Zipkin/Tempo for a sample checkout flow
- [ ] Sampling rate configurable via \`management.tracing.sampling.probability\`

## Effort Estimate
3 h

## References
- SRS NFR-OPS-04
- RTM MON-05"

create_issue \
  "[feature] observability: implement custom business metrics with Micrometer (OBS-03)" \
  "M4 — Feature Development" \
  "type: infrastructure,priority: medium,phase: M4,domain: observability" \
  "## Description
Emit custom Micrometer metrics for: orders per minute, payment success rate, cart abandonment rate, search query latency. Expose via \`/actuator/prometheus\`.

## Acceptance Criteria
- [ ] \`MeterRegistry\` injected and counters/timers/gauges registered
- [ ] Metrics visible at \`/actuator/prometheus\`
- [ ] Grafana dashboard JSON file committed to \`docs/monitoring/\`
- [ ] Metric names follow Prometheus naming conventions

## Effort Estimate
3 h

## References
- SRS NFR-OPS-05
- RTM MON-05"

# ── Issues — M5: Production Readiness (27 issues) ─────────────────────────────
log "=== PHASE: Issues — M5 ==="

create_issue \
  "[security] hardening: replace unsafe-inline CSP with nonce-based policy (SEC-01)" \
  "M5 — Production Readiness" \
  "type: security,priority: critical,phase: M5,domain: auth" \
  "## Description
Final verification that CSP \`unsafe-inline\` removed in M3 (SEC-14 fix) is fully effective with no regressions from M4 feature additions. Revalidate with OWASP ZAP passive scan.

## Acceptance Criteria
- [ ] OWASP ZAP scan shows no CSP \`unsafe-inline\` warnings
- [ ] All frontend scripts and styles use nonce or are external
- [ ] CSP header validated on production build

## Effort Estimate
2 h

## References
- RGAR §6, SEC-14
- SRS SEC-12"

create_issue \
  "[security] hardening: complete OWASP Top 10 security assessment (SEC-02)" \
  "M5 — Production Readiness" \
  "type: security,priority: critical,phase: M5,domain: auth" \
  "## Description
Perform OWASP Top 10 (2021) assessment against the full application. Document findings and remediate all Critical and High findings before M5 gate.

## Acceptance Criteria
- [ ] OWASP ZAP active scan against staging environment
- [ ] All A01–A10 categories assessed
- [ ] Zero Critical findings
- [ ] High findings have remediation plan with timeline
- [ ] Assessment report stored in \`docs/SDLC-docs/reports/security-assessment.md\`

## Effort Estimate
8 h

## References
- SRS SEC-01 to SEC-15
- SDP §10.2"

create_issue \
  "[security] hardening: implement HTTP security headers audit and enforcement (SEC-03)" \
  "M5 — Production Readiness" \
  "type: security,priority: high,phase: M5,domain: auth" \
  "## Description
Verify all required security headers are present and correctly configured: HSTS, X-Frame-Options, X-Content-Type-Options, Referrer-Policy, Permissions-Policy.

## Acceptance Criteria
- [ ] All headers scored A+ on securityheaders.com equivalent check
- [ ] HSTS max-age ≥31536000 with includeSubDomains
- [ ] X-Frame-Options: DENY
- [ ] X-Content-Type-Options: nosniff
- [ ] Integration test verifies all headers on every response

## Effort Estimate
2 h

## References
- SRS SEC-11, SEC-12"

create_issue \
  "[security] jwt: reduce JWT access token lifetime from 15 min to configurable, document rotation (SEC-04)" \
  "M5 — Production Readiness" \
  "type: security,priority: medium,phase: M5,domain: auth" \
  "## Description
JWT access token lifetime should be configurable and documented. Verify refresh token rotation is working correctly in production configuration.

## Acceptance Criteria
- [ ] \`jwt.expiration\` property documented in \`.env.example\` with recommended value
- [ ] Refresh token rotation tested end-to-end
- [ ] Expired access token correctly rejected with 401
- [ ] Token blacklist for logout verified working

## Effort Estimate
2 h

## References
- SRS AUTH-07
- SDP §3.3 Auth Flow"

create_issue \
  "[security] secrets: audit all configuration for hardcoded secrets or default values (SEC-05)" \
  "M5 — Production Readiness" \
  "type: security,priority: critical,phase: M5,domain: auth" \
  "## Description
Scan all configuration files, YAML, and Java for hardcoded secrets, weak defaults, or values that should be externalized. All security-critical values must have no default.

## Acceptance Criteria
- [ ] \`trufflehog\` or \`gitleaks\` scan passes with zero findings
- [ ] All \`@Value\` annotations for secrets have no default value (e.g., \`\${JWT_SECRET}\` not \`\${JWT_SECRET:default}\`)
- [ ] No credentials in \`application.yml\` or \`application-test.yml\`
- [ ] CI step runs secret scan on every push

## Effort Estimate
3 h

## References
- SDP Appendix B
- RGAR §11"

create_issue \
  "[test] mutation: implement PIT mutation testing with ≥75% mutation score (TIR-05)" \
  "M5 — Production Readiness" \
  "type: test,priority: high,phase: M5,domain: ci-cd" \
  "## Description
Configure PIT (Pitest) mutation testing for \`service.*\` and \`security.*\` packages. Achieve ≥75% mutation score as per SDP TIR-05.

## Acceptance Criteria
- [ ] \`pitest-maven\` plugin added to \`pom.xml\`
- [ ] Target packages: \`com.example.buildnest_ecommerce.service.*\` and \`com.example.buildnest_ecommerce.security.*\`
- [ ] \`./mvnw test-compile org.pitest:pitest-maven:mutationCoverage\` passes with ≥75% score
- [ ] PIT HTML report published as CI artifact
- [ ] CI gate fails below 75%

## Effort Estimate
6 h

## References
- SDP §9, TIR-05
- RGAR §6"

create_issue \
  "[test] coverage: raise JaCoCo instruction coverage gate to 70% (M5 target)" \
  "M5 — Production Readiness" \
  "type: test,priority: high,phase: M5,domain: ci-cd" \
  "## Description
Raise the JaCoCo minimum instruction coverage from 60% (M4) to 70% as per the SDP final gate.

## Acceptance Criteria
- [ ] \`pom.xml\` JaCoCo rule: \`<minimum>0.70</minimum>\`
- [ ] \`ci.yml\` quality-gates threshold: 70%
- [ ] \`./mvnw verify\` passes

## Effort Estimate
8 h (additional tests to reach 70%)

## References
- SDP §9, Table 9-1"

create_issue \
  "[test] e2e: implement full end-to-end test suite with Playwright (M5)" \
  "M5 — Production Readiness" \
  "type: test,priority: high,phase: M5,domain: ci-cd" \
  "## Description
Implement Playwright E2E tests for the complete user journey: register → browse → search → add-to-cart → checkout → order confirmation → order history.

## Acceptance Criteria
- [ ] Playwright configured in \`frontend/\` with TypeScript
- [ ] Tests run against staging environment
- [ ] Happy path E2E test passes
- [ ] Critical error paths tested (payment failure, out of stock)
- [ ] CI runs E2E tests on \`master\` push

## Effort Estimate
8 h

## References
- SDP §8.3 (Test Types)
- SRS UI-01 to UI-06"

create_issue \
  "[test] performance: implement load test with k6 (P95 < 200ms at 100 concurrent users) (PERF-TEST-01)" \
  "M5 — Production Readiness" \
  "type: test,priority: high,phase: M5,domain: ci-cd" \
  "## Description
Implement k6 load test for product listing and checkout critical path. Pass criterion: P95 response time < 200ms at 100 concurrent users with 0% error rate.

## Acceptance Criteria
- [ ] k6 test script at \`backend/scripts/load-test.js\`
- [ ] Tests: product list, product detail, search, checkout
- [ ] Run against staging: \`k6 run --vus 100 --duration 5m load-test.js\`
- [ ] P95 < 200ms, error rate < 0.1%
- [ ] Results stored in \`docs/SDLC-docs/reports/load-test-results.md\`

## Effort Estimate
5 h

## References
- SRS NFR-PERF-01
- SDP §10.3"

create_issue \
  "[infrastructure] deployment: create production Docker Compose configuration (OPS-01)" \
  "M5 — Production Readiness" \
  "type: infrastructure,priority: high,phase: M5,domain: ci-cd" \
  "## Description
Create a production-grade Docker Compose configuration (or Kubernetes manifests) for all services: backend, frontend (nginx), MySQL, Redis, Elasticsearch.

## Acceptance Criteria
- [ ] \`docker-compose.prod.yml\` at repository root
- [ ] All services configured with resource limits
- [ ] Environment variables injected from \`.env\` (no hardcoded values)
- [ ] Health checks on all services
- [ ] MySQL data persisted to named volume
- [ ] Nginx configured for frontend with SSL termination (self-signed for compose, LetsEncrypt for cloud)

## Effort Estimate
5 h

## References
- SDP §7.4 (Environment Management)"

create_issue \
  "[infrastructure] deployment: implement CI/CD deployment workflow (OPS-02)" \
  "M5 — Production Readiness" \
  "type: infrastructure,priority: high,phase: M5,domain: ci-cd" \
  "## Description
Implement \`deploy.yml\` GitHub Actions workflow that builds Docker images, pushes to registry, and deploys to staging/production environments on tagged release.

## Acceptance Criteria
- [ ] \`deploy.yml\` workflow triggers on \`v*\` tag push
- [ ] Builds and pushes backend and frontend Docker images to GitHub Container Registry
- [ ] Deploys to staging environment (SSH or cloud provider API)
- [ ] Blue-green or rolling deployment strategy (no downtime)
- [ ] Deployment notification (Slack or GitHub Release)
- [ ] Manual approval gate before production deploy

## Effort Estimate
8 h

## References
- SDP §9 (CI/CD Pipelines)"

create_issue \
  "[infrastructure] backup: implement automated database backup and restore procedure (OPS-03)" \
  "M5 — Production Readiness" \
  "type: infrastructure,priority: high,phase: M5,domain: ci-cd" \
  "## Description
Automated daily MySQL backups with verified restore procedure. RPO ≤4h, RTO ≤1h.

## Acceptance Criteria
- [ ] Backup script at \`backend/scripts/backup-db.sh\` (mysqldump + gzip + timestamp)
- [ ] Cron schedule: daily at 02:00 UTC
- [ ] Backups retained for 30 days
- [ ] Restore procedure documented and tested
- [ ] Restore RTO validated < 1h in DR drill

## Effort Estimate
3 h

## References
- SRS NFR-AVL-01
- SDP §7.3"

create_issue \
  "[infrastructure] monitoring: configure Grafana dashboards and alerting rules (OPS-04)" \
  "M5 — Production Readiness" \
  "type: infrastructure,priority: high,phase: M5,domain: observability" \
  "## Description
Configure Grafana dashboards for: application metrics (requests/s, latency, errors), JVM metrics, MySQL metrics, Redis metrics. Configure alert rules for SLA breach conditions.

## Acceptance Criteria
- [ ] Grafana dashboard JSON files committed to \`docs/monitoring/dashboards/\`
- [ ] Alert rules for: P95 latency >500ms, error rate >1%, memory >85%, disk >80%
- [ ] Alerts routed to configurable notification channel (email/Slack)
- [ ] Dashboards provisioned automatically in Docker Compose

## Effort Estimate
5 h

## References
- SRS NFR-OPS-05
- RTM MON-01 to MON-05"

create_issue \
  "[infrastructure] monitoring: implement health check endpoint and readiness/liveness probes (OPS-05)" \
  "M5 — Production Readiness" \
  "type: infrastructure,priority: medium,phase: M5,domain: observability" \
  "## Description
Enhance Spring Boot Actuator health endpoints for Kubernetes readiness/liveness probes. Custom health indicators for MySQL, Redis, and Elasticsearch connectivity.

## Acceptance Criteria
- [ ] \`/actuator/health/liveness\` returns UP when JVM is healthy
- [ ] \`/actuator/health/readiness\` returns UP only when MySQL + Redis + Elasticsearch are reachable
- [ ] Custom health indicators for each dependency
- [ ] Health endpoints excluded from authentication
- [ ] Integration test verifies health endpoint response structure

## Effort Estimate
2 h

## References
- SRS NFR-OPS-06"

create_issue \
  "[infrastructure] deployment: create Dockerfile for backend (multi-stage, non-root) (OPS-06)" \
  "M5 — Production Readiness" \
  "type: infrastructure,priority: high,phase: M5,domain: ci-cd" \
  "## Description
Production-grade Dockerfile for the Spring Boot backend: multi-stage build, non-root user, minimal base image (distroless or alpine JRE), JVM tuning for container environment.

## Acceptance Criteria
- [ ] Multi-stage: Maven build stage + JRE runtime stage
- [ ] Non-root user in runtime stage
- [ ] Base image: Eclipse Temurin 21-jre-alpine or Google Distroless Java 21
- [ ] JVM flags: \`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0\`
- [ ] Image builds successfully: \`docker build -t buildnest-backend .\`
- [ ] Container starts and passes health check

## Effort Estimate
2 h

## References
- SDP §7.4"

create_issue \
  "[infrastructure] deployment: create Dockerfile for frontend (nginx, non-root) (OPS-07)" \
  "M5 — Production Readiness" \
  "type: infrastructure,priority: high,phase: M5,domain: frontend" \
  "## Description
Production Dockerfile for React/Vite frontend: multi-stage build, nginx serving, non-root, gzip compression, security headers.

## Acceptance Criteria
- [ ] Multi-stage: Node build + nginx runtime
- [ ] \`nginx.conf\` configured: gzip, cache headers, SPA fallback (\`try_files\`)
- [ ] Non-root nginx (port 8080)
- [ ] Security headers set in nginx (CSP, HSTS, etc.)
- [ ] Image builds and serves \`npm run build\` output

## Effort Estimate
2 h

## References
- SDP §7.4"

create_issue \
  "[docs] runbook: create production runbook for common operational procedures (OPS-08)" \
  "M5 — Production Readiness" \
  "type: docs,priority: medium,phase: M5,domain: ci-cd" \
  "## Description
Create an operational runbook covering: application startup/shutdown, health check validation, database backup/restore, log retrieval, common alert responses, rollback procedure.

## Acceptance Criteria
- [ ] Runbook at \`docs/SDLC-docs/operations/runbook.md\`
- [ ] Covers: startup, shutdown, health check, backup/restore, log access, rollback
- [ ] Each procedure: prerequisites, step-by-step commands, expected output, troubleshooting
- [ ] Validated by dry-run on staging environment

## Effort Estimate
4 h

## References
- SDP §12 (Operational Readiness)"

create_issue \
  "[docs] api: publish final OpenAPI 3.1 specification to GitHub Pages (API-DOC-01)" \
  "M5 — Production Readiness" \
  "type: docs,priority: medium,phase: M5,domain: ci-cd" \
  "## Description
Publish the generated OpenAPI 3.1 spec to GitHub Pages using Swagger UI. Automate publication in the release workflow.

## Acceptance Criteria
- [ ] GitHub Pages configured for the repository
- [ ] \`swagger-ui\` static files committed to \`docs/api/\` or published via gh-pages branch
- [ ] Published at \`https://pradip9096.github.io/buildnest-ecommerce-platform/\`
- [ ] Release workflow updates the published spec on each tag

## Effort Estimate
2 h

## References
- SRS NFR-MAINT-01"

create_issue \
  "[chore] compliance: complete GDPR data audit and privacy implementation (COMP-01)" \
  "M5 — Production Readiness" \
  "type: chore,priority: high,phase: M5,domain: auth" \
  "## Description
Identify all PII stored and implement GDPR-required features: data export (right to access), account deletion (right to erasure), consent tracking.

## Acceptance Criteria
- [ ] PII inventory documented: fields, tables, retention period
- [ ] \`GET /api/v1/users/data-export\` returns all user data as JSON
- [ ] \`DELETE /api/v1/users/account\` soft-deletes account and anonymises PII after 30 days
- [ ] Consent recorded at registration (checkbox + timestamp)
- [ ] Privacy policy linked from registration page

## Effort Estimate
5 h

## References
- SRS NFR-COMP-01 to NFR-COMP-03"

create_issue \
  "[chore] compliance: accessibility audit (WCAG 2.1 AA) for frontend (COMP-02)" \
  "M5 — Production Readiness" \
  "type: chore,priority: medium,phase: M5,domain: frontend" \
  "## Description
Run accessibility audit against all frontend pages. Remediate all WCAG 2.1 AA Level violations.

## Acceptance Criteria
- [ ] axe-core automated scan passes on all pages (zero critical violations)
- [ ] Manual keyboard navigation verified for checkout flow
- [ ] Screen reader test for product listing and checkout
- [ ] Colour contrast ratio ≥4.5:1 for all text
- [ ] Report stored in \`docs/SDLC-docs/reports/accessibility-audit.md\`

## Effort Estimate
4 h

## References
- SRS NFR-COMP-04"

create_issue \
  "[test] regression: full regression test pass before M5 production gate (REG-01)" \
  "M5 — Production Readiness" \
  "type: test,priority: critical,phase: M5,domain: ci-cd" \
  "## Description
Run the complete test suite (unit, integration, E2E, security scan, load test) and confirm all gates pass before production deployment authorisation.

## Acceptance Criteria
- [ ] \`./mvnw verify\` — zero failures, coverage ≥70%
- [ ] PIT mutation score ≥75%
- [ ] Playwright E2E — all scenarios pass
- [ ] k6 load test — P95 < 200ms at 100 VUs
- [ ] OWASP ZAP scan — zero Critical findings
- [ ] Secret scan — zero findings
- [ ] All CI workflows green on \`master\`

## Effort Estimate
3 h (coordination)

## References
- SDP Appendix C (M5 Gate Checklist)
- RGAR §16"

create_issue \
  "[chore] dependency: update all dependencies to latest stable versions (DEP-01)" \
  "M5 — Production Readiness" \
  "type: chore,priority: medium,phase: M5,domain: ci-cd" \
  "## Description
Before production release, update all Maven and npm dependencies to their latest stable versions. Run full test suite after update.

## Acceptance Criteria
- [ ] \`./mvnw versions:display-dependency-updates\` reviewed and actioned
- [ ] \`npm outdated\` reviewed and actioned in \`frontend/\`
- [ ] No breaking changes introduced
- [ ] Full test suite passes after updates
- [ ] OWASP Dependency-Check passes after updates

## Effort Estimate
3 h

## References
- SDP §10.4"

create_issue \
  "[chore] config: finalise all production environment variables and secrets rotation procedure (CFG-01)" \
  "M5 — Production Readiness" \
  "type: config,priority: critical,phase: M5,domain: ci-cd" \
  "## Description
All production secrets must be managed via GitHub Actions Secrets or a secrets manager. Document secrets rotation procedure.

## Acceptance Criteria
- [ ] All secrets stored in GitHub Actions Secrets (not in repo)
- [ ] Secrets rotation procedure documented in runbook
- [ ] JWT_SECRET: minimum 512-bit random value
- [ ] MySQL, Redis passwords: minimum 32-character random values
- [ ] Stripe keys: live keys configured (test keys only in staging)

## Effort Estimate
2 h

## References
- SDP Appendix B
- RGAR §11"

create_issue \
  "[chore] review: conduct pre-production code review and architecture walkthrough (REVIEW-01)" \
  "M5 — Production Readiness" \
  "type: chore,priority: high,phase: M5,domain: ci-cd" \
  "## Description
Conduct a structured pre-production architecture walkthrough covering: security controls, data flows, error handling, observability, scalability, and compliance.

## Acceptance Criteria
- [ ] Architecture review checklist completed (based on SDP §10)
- [ ] All Critical and High findings remediated
- [ ] Review documented in \`docs/SDLC-docs/reports/architecture-review.md\`
- [ ] Sign-off from project stakeholders

## Effort Estimate
4 h

## References
- SDP §10.5
- ISO/IEC/IEEE 12207:2017 §6.3.5"

create_issue \
  "[feature] frontend: implement SEO metadata and sitemap generation (FE-SEO-01)" \
  "M5 — Production Readiness" \
  "type: feature,priority: medium,phase: M5,domain: frontend" \
  "## Description
Ensure all public pages have correct SEO metadata (title, description, Open Graph, Twitter Card). Generate sitemap.xml for product and category pages.

## Acceptance Criteria
- [ ] React Helmet or equivalent manages \`<title>\` and \`<meta>\` per page
- [ ] Open Graph tags on product detail pages
- [ ] \`sitemap.xml\` generated at build time listing all category and product pages
- [ ] \`robots.txt\` configured correctly (allow crawl, disallow admin)
- [ ] Lighthouse SEO score ≥90

## Effort Estimate
3 h

## References
- SRS NFR-MAINT-05"

create_issue \
  "[feature] frontend: implement Progressive Web App (PWA) support (FE-PWA-01)" \
  "M5 — Production Readiness" \
  "type: feature,priority: low,phase: M5,domain: frontend" \
  "## Description
Add PWA manifest and service worker for offline browsing of previously viewed products and app install capability.

## Acceptance Criteria
- [ ] \`manifest.json\` with app name, icons, theme colour
- [ ] Service worker caches product list and detail pages
- [ ] Offline mode shows cached content with offline indicator
- [ ] Lighthouse PWA score ≥70
- [ ] Install prompt works on mobile

## Effort Estimate
4 h

## References
- SRS NFR-MAINT-06"

create_issue \
  "[chore] internationalisation: implement i18n foundation for multi-language support (I18N-01)" \
  "M5 — Production Readiness" \
  "type: chore,priority: low,phase: M5,domain: frontend" \
  "## Description
Add i18n foundation (react-i18next) with English as primary language and structure for adding additional locales in future.

## Acceptance Criteria
- [ ] \`react-i18next\` installed and configured
- [ ] All UI strings extracted to \`public/locales/en/translation.json\`
- [ ] Language detection from browser \`Accept-Language\` header
- [ ] Language switcher component (English only initially)
- [ ] Build passes with no hardcoded strings in components

## Effort Estimate
4 h

## References
- SRS NFR-MAINT-07"

# ── Create GitHub Project ──────────────────────────────────────────────────────
log "=== PHASE: Add issues to project ==="

TOTAL=${#ISSUE_URLS[@]}
log "Total issue URLs collected: $TOTAL"

for url in "${ISSUE_URLS[@]}"; do
  add_to_project "$PROJECT_NUM" "$url"
done

# ── Summary ────────────────────────────────────────────────────────────────────
log "=== COMPLETE ==="
log "Project: $PROJECT_NAME (#$PROJECT_NUM)"
log "Issues created/confirmed: $TOTAL"
log "Log file: $LOG_FILE"
echo ""
echo "GitHub Project: https://github.com/users/pradip9096/projects/$PROJECT_NUM"
