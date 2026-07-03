# Changelog

All notable changes to this project are documented in this file.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Issue numbers reference the [GitHub issue tracker](https://github.com/pradip9096/buildnest-ecommerce-platform/issues).

Pre-1.0 convention: MINOR increments represent completed milestones; PATCH increments represent hotfixes within a milestone. `1.0.0` will mark the first production-ready release (end of M5).

---

## [Unreleased] — M4: Feature Development

### Removed
- `OrderProcessingComprehensiveTest` — duplicated `OrderServiceImplTest`'s Mockito-based unit coverage and `OrderServiceIntegrationTest`'s `@DataJpaTest` repository-level assertions; unique scenarios migrated into the two canonical classes before deletion (#259)
- 7 misleadingly named top-level `@DataJpaTest` classes — `RBACTest`, `AdminDashboardTest`, `AnalyticsReportingTest`, `ApiIntegrationTest`, `PaymentProcessingTest`, `InventoryManagementTest`, `CategoryManagementTest` — each tested only basic JPA persistence mechanics while its name implied coverage of RBAC enforcement, admin dashboards, analytics, API contracts, payment processing, inventory management, or category management respectively; `ApiIntegrationTest` asserted tautologies (e.g. `page >= 0`) and tested no actual API contract; every persistence scenario they covered is already tested more thoroughly by dedicated classes (`AuthenticationAuthorizationSecurityTest`, `AdminProductControllerIntegrationTest`, `SalesAnalyticsServiceImplTest`/`AdminAnalyticsServiceTest`, the payment test suite, `InventoryServiceImplTest`, `CategoryRepositoryTest`); no unique coverage lost (#255)

### Changed
- `AdminInventoryControllerTest` converted from `@SpringBootTest` (full application context, including database/Redis/Elasticsearch config) to `@WebMvcTest(AdminInventoryController.class)` with `@MockBean InventoryService` — all 22 existing scenarios pass with a much cheaper test context; `AdminInventoryControllerIntegrationTest` (real H2) is unchanged (#259)
- PIT mutation score raised from 75% (1,237/1,658) to 77% (1,284/1,658), bumping the CI gate to ≥77% (#277); no-coverage mutations reduced from 72 to 68; test strength improved from 78% to 81%
  - `AdminServiceImplTest`: `ArgumentCaptor<User>` on `deleteUser` asserting `isDeleted=true` and `deletedAt≠null`; `ArgumentCaptor` on `updateUser`/`updateUserByAdmin` asserting `updatedAt≠null`; `ex.getMessage()` assertions on all not-found paths to kill lambda null-return mutations
  - `CartServiceImplTest`: full `CartItemResponseDTO` field assertions (cartItemId, productId, productName, quantity, price, itemTotal); `ArgumentCaptor<Cart>` on `save()` for new-cart path asserting user and items set; new test `testAddToCart_newItem_setsAllFieldsOnCartItem` with `ArgumentCaptor<CartItem>` asserting all fields; `testGetCartTotal` tightened from `assertTrue(total >= 0)` to `assertEquals(200.0, total)`
  - `UserServiceImplTest`: `ArgumentCaptor<User>` on `save()` in `updateUserProfile` asserting `updatedAt≠null`; field assertions on all three name/phone fields; `assertNotNull(existing.getDeletedAt())` in `testDeleteUser`
  - Three test files renamed via `git mv` so PIT `targetTests` pattern includes them: `RedisCheckoutSessionStoreTest` → `RedisCheckoutSessionStoreImplTest`, `EmailTemplateRenderingTest` → `EmailTemplateRenderingServiceTest`, `OrderProcessingComprehensiveTest` → `OrderProcessingComprehensiveServiceTest`
- PIT mutation score raised from 69% (1,122/1,637) to 75% (1,237/1,658), clearing the ≥75% CI gate (#277); no-coverage mutations reduced from 167 to 72; test strength improved from 76% to 78%
  - `NotificationServiceImplTest`: `ArgumentCaptor<Context>` assertions on all `ctx.setVariable()` calls; `MimeMessage` recipient/subject assertions to detect `setTo`/`setSubject` removal mutations
  - `InventoryServiceImplTest`: added `@Mock InventoryAuditLogRepository` (was null, blocking `adjustStock`); 22 new tests covering `reserveStock`, `releaseReservation`, `releaseExpiredReservations`, `adjustStock`, `getInventoryStatus`, `getAllInventorySummary`
  - `InventoryReportServiceTest`: replaced size-only assertions with specific map-field checks; added sorting-order tests for all three sorted result sets
  - `InventoryThresholdManagementServiceTest`: state assertions after `setProductThreshold`/`setCategoryThreshold`; new cache-repopulation test for `getProductThreshold` cache-miss path
  - `CheckoutServiceImplTest`: added 4 missing `@Mock` fields (was causing 57 no-coverage mutations); 12 new multi-step checkout tests; strengthened subtotal/order-number/status assertions
  - Scheduler tests renamed `*Test` → `*ImplTest` (`TokenCleanupScheduler`, `InventoryReservationCleanupJob`, `InventoryMonitoringScheduler`) so PIT `targetTests` pattern includes them
  - `OrderSpecificationTest` renamed to `OrderSpecificationImplTest` for same reason

### Added
- CI: `ci-cd-pipeline.yml` `integration-tests` job now parses `target/pit-reports/mutations.xml` and posts a PR comment with PIT mutation results (killed/survived/no-coverage counts, top 5 classes by surviving/uncovered mutants); pass/fail indicator is derived from **mutation score** (`killed / total generated`, including no-coverage mutants) — the metric `pom.xml`'s `mutationThreshold` actually gates on — with test strength (`killed / reached`) shown alongside for visibility only, since it is not gated; section omitted gracefully when `mutations.xml` is absent (e.g. PIT skipped). Note: issue #275 as filed named `ci.yml` as the target file and test strength as the gated metric — both were incorrect. `ci.yml` never runs `mvn verify`/PIT (only `clean test jacoco:report`), so `mutations.xml` never exists there; PIT only executes in `ci-cd-pipeline.yml`. Gating on test strength would also have produced a pass/fail indicator inconsistent with the actual `mutationThreshold` gate, which evaluates mutation score (see `pom.xml` pitest-maven plugin comment, added during #277) (#275)
- `PitNamingConventionTest` — ArchUnit rule in the `architecture` package enforcing that service-package test classes use `*ImplTest` or `*ServiceTest` suffixes; plain `*Test` classes are silently excluded from PIT's `targetTests` pattern and kill zero mutations while appearing in JaCoCo coverage; the rule fails the build at PR time with a rename suggestion (#278)
- React product detail page at `/products/:id`: image gallery (main + thumbnail strip), star rating (half-star aware, sm/md/lg sizes), quantity selector (clamped to stock), paginated reviews section (summary box with distribution bars + review list), related products grid (same-category, max 4); SEO meta (`document.title`, og:title/description/image); skeleton loader and 404 error state; add-to-cart placeholder (deferred to FE-06, #95) (FE-02, #93)
- `src/api/products.ts` — `fetchProductById(id)` (`GET /api/public/products/{id}`) typed API client (#93)
- `src/api/reviews.ts` — `fetchReviews(productId, page, size)` (`GET /api/products/{id}/reviews`) and `fetchReviewSummary(productId)` (`GET /api/products/{id}/reviews/summary`) typed API clients (#93)
- `src/hooks/useProduct.ts` — single-product data-fetching hook with cancellation-flag pattern (#93)
- `src/hooks/useReviews.ts` — parallel fetch of review list + summary with cancellation (#93)
- `src/components/product/StarRating.tsx` — half-star-aware rating display, amber-400 filled stars, three size variants (#93)
- `src/components/product/ImageGallery.tsx` — main image large display with thumbnail strip; graceful single-image degradation (#93)
- `src/components/product/QuantitySelector.tsx` — +/− buttons clamped to `[1, stockQuantity]` (#93)
- `src/components/product/ReviewsSection.tsx` — average rating + distribution bars summary box, paginated review list (#93)
- `src/components/product/RelatedProducts.tsx` — same-category product grid (max 4); uses `linkable={false}` on `ProductCard` to avoid anchor-in-anchor HTML nesting (#93)
- `src/pages/ProductDetailPage.tsx` — orchestrates gallery, rating, quantity, add-to-cart, reviews, and related products; uses `useParams<{ id: string }>()` for route param (#93)
- `ReviewUser`, `Review`, `ReviewSummary`, `PagedResponse<T>` TypeScript interfaces in `src/types/index.ts` (#93)
- React product listing page at `/`: TypeScript + Tailwind CSS v4 responsive grid (`grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4`); keyword search via form submit; client-side category multi-filter, sort (relevance / price asc-desc / newest), and pagination; loading skeleton (12 animated cards), per-card error and empty states; Vite dev-server proxy (`/api` → `:8080`) eliminates CORS in development (FE-01, #92)
- `src/types/index.ts` — shared TypeScript interfaces (`Product`, `Category`, `ApiResponse<T>`, `ProductFilters`, `SortOption`) for the React frontend (#92)
- `src/api/products.ts` — `fetchProducts()` (`GET /api/public/products`) and `searchProducts(keyword)` (`GET /api/public/products/search?keyword=`) typed API clients (#92)
- `src/api/categories.ts` — `fetchCategories()` (`GET /api/public/categories`) typed API client (#92)
- `src/hooks/useProducts.ts` — data-fetching hook with cancellation-flag pattern (async fetch in `useEffect`; returns `{ products, total, loading, error }`) (#92)
- `src/hooks/useCategories.ts` — category data-fetching hook with graceful empty-array fallback (#92)
- `src/components/product/ProductCard.tsx` — product card with category label, discount strikethrough price, out-of-stock badge, and placeholder emoji (#92)
- `src/components/product/ProductGrid.tsx` — responsive product grid with empty-state message (#92)
- `src/components/product/LoadingSkeleton.tsx` — 12-card animated skeleton loader (#92)
- `src/components/filters/CategorySidebar.tsx` — multi-select category filter checkboxes with "Clear filters" button (#92)
- `src/components/filters/SortDropdown.tsx` — sort order selector (relevance / price asc / price desc / newest) (#92)
- `src/components/common/Pagination.tsx` — prev/next + numbered page buttons with ellipsis via `buildPageRange` helper (#92)
- `src/components/common/ErrorMessage.tsx` — error display component with optional retry callback (#92)
- `src/pages/ProductListingPage.tsx` — orchestrates header (logo + search), toolbar (count + sort), sidebar (categories), grid, and pagination; manages filter state and page resets on keyword/category change (#92)
- Full-text product search at `GET /api/v2/products/search`: delegates to Elasticsearch when `elasticsearch.enabled=true` (multi-field match across name^3, description, categoryName with fuzziness; category/price/stock filters applied); falls back to JPA `advancedSearch` when ES is disabled (SRCH-01, #74)
- `ProductSearchService` interface + `ProductSearchServiceImpl` (`@ConditionalOnProperty("elasticsearch.enabled","true")`): `search`, `indexProduct`, `deleteFromIndex`, `reindexAll` with Resilience4j circuit-breaker protection and graceful degradation on `CallNotPermittedException` (SRCH-01/SRCH-02, #74/#75)
- `ProductDocument` Elasticsearch document (`@Document(indexName="products")`): name (Text, standard analyzer, boost×3), description (Text), categoryId/categoryName (Keyword), price/discountPrice (Double), inStock/isActive (Boolean), sku/imageUrl (Keyword), createdAt (Date) (#74)
- `ProductElasticsearchRepository`: `fullTextSearch` (multi_match native query), `findByCategoryIdAndIsActiveTrue`, `findByIsActiveTrue` (#74)
- `ProductCreatedEvent`, `ProductUpdatedEvent`, `ProductDeletedEvent` — Spring `ApplicationEvent` subclasses carrying the affected `Product` or `productId` (SRCH-02, #75)
- `ProductIndexEventListener` (`@ConditionalOnProperty("elasticsearch.enabled","true")`): `@Async @EventListener` handlers for all three product events; calls `ProductSearchService.indexProduct` / `deleteFromIndex` (#75)
- `POST /api/v1/admin/search/reindex` (admin only): triggers `ProductSearchService.reindexAll()`; returns 503 if Elasticsearch is disabled (SRCH-02, #75)
- `ProductSearchServiceTest` — 22 unit tests: full-text query routing, category routing, all-active routing, price-range post-filter, indexProduct mapping, deleteFromIndex, reindexAll; circuit breaker OPEN paths for all 4 methods, repository exception paths, blank-query fallback routing, inStock filter excluding out-of-stock documents, toDocument null-field branches (null category, null price/discountPrice, null/zero stockQuantity) (#74/#75, #264)
- `ProductIndexEventListenerTest` — 3 unit tests: created/updated/deleted event dispatch (#75)
- `AdminSearchControllerIntegrationTest` — 4 integration tests: admin 200, 403, 401, service-exception 500 (#75)
- `AdminSearchControllerTest` — 2 unit tests: `Optional.empty()` (Elasticsearch disabled) → 503, `Optional.of(mock)` → 200 with `reindexAll` invocation; the 503 path is structurally unreachable from the integration test because `TestElasticsearchConfig` always provides the bean (#75, #268)
- Payment refund endpoint `POST /api/v1/admin/orders/{id}/refund` (admin only): accepts `{ amount, reason }`, validates amount ≤ original payment, calls Razorpay refund API, sets Payment status to `REFUNDED` (full) or `PARTIALLY_REFUNDED` (partial), auditable (PAY-02, #61)
- `PaymentServiceImpl.processRefund`: find-by-order-id → SUCCESS-status guard → amount guard → gateway call → status + `refundedAmount` + `refundReason` + `refundInitiatedAt` update (#61)
- `RefundRequest` payload — `@NotNull @DecimalMin("0.01") Double amount`, optional `String reason` (#61)
- `PaymentRepository.findByOrderId` — for refund lookup by internal order ID (#61)
- `GlobalExceptionHandler` now handles `PaymentProcessingException` → HTTP 400 (#61)
- Liquibase changeset `20260629-009` — adds `refunded_amount`, `refund_reason`, `refund_initiated_at` columns to `payment` table and extends MySQL status ENUM with `PARTIALLY_REFUNDED` (PAY-02, #61)
- `PaymentRefundServiceTest` — 5 unit tests: full refund, partial refund, excess amount, non-SUCCESS status, payment not found, gateway failure (#61)
- `AdminPaymentRefundControllerIntegrationTest` — 8 integration tests: full/partial refund, excess amount (400), non-SUCCESS (400), missing field (400), no payment (404), 403, 401 (#61)
- Razorpay webhook endpoint at `POST /api/v1/webhooks/payment` (public; no JWT): validates `X-Razorpay-Signature` via HMAC-SHA256, handles `payment.captured` (Payment→SUCCESS, Order→PAID) and `payment.failed` (Payment→FAILED, Order→PAYMENT_FAILED) with idempotency protection; returns 200 on success, 401 on invalid signature (PAY-01, #60)
- `PaymentServiceImpl.processWebhookEvent`: signature validation → JSON parse → find-by-razorpay-order-id → idempotency check → status update + `OrderService.updateOrderStatus` + `DomainEventPublisher` (#60)
- `razorpay.webhook.secret` property (`${RAZORPAY_WEBHOOK_SECRET:test_webhook_secret}`) in `application.properties` (#60)
- `PaymentWebhookServiceTest` — 7 unit tests: captured event, failed event, invalid signature, idempotency skip, payment not found, unknown event, malformed JSON (#60)
- `PaymentWebhookControllerIntegrationTest` — 4 integration tests: valid webhook → 200, invalid signature → 401, missing header → 401, public access without JWT → 200 (#60)
- Shipping method and cost calculation service: `GET /api/v1/checkout/shipping-options?postalCode=` returns all active methods with `calculatedCost = baseCost + (costPerKg × totalWeightKg × zoneMultiplier)`; zone derived from postal code prefix hash into configurable `app.shipping.zone-multipliers` list (SHIP-01, #87)
- `ShippingConfig` (`@ConfigurationProperties("app.shipping")`): `defaultWeightPerItemKg` (default 0.5 kg/unit) and `zoneMultipliers` (default [1.0, 1.5, 2.0]) — fully configurable without code changes (#87)
- `AdminShippingController` at `GET/POST/PUT/DELETE /api/v1/admin/shipping-methods`: full CRUD with `@Auditable` on all mutating operations; DELETE is a soft-deactivate (`isActive=false`) (#87)
- `ShippingServiceImplTest` — 16 unit tests covering cost formula, zone resolver boundary cases (null/blank postal, short postal, hash consistency), empty-cart guard, and admin CRUD (#87)
- `AdminShippingControllerIntegrationTest` — 11 integration tests covering list, create (201 + isActive assertion), update, soft-delete (DB `isActive=false` verified), validation (400), role enforcement (403), 404, and GET /{id} (found → 200, not-found → 404) (#87, #268)
- Multi-step checkout flow at `POST /api/v1/checkout/{address,shipping,payment,confirm}`: address selection → shipping method → payment initiation (delegates to `PaymentService`) → order confirmation; session stored in Redis with 30-minute TTL; out-of-order step returns 409 Conflict (CHK-01, #76)
- `CheckoutSessionStore` interface + `RedisCheckoutSessionStore` implementation (`StringRedisTemplate` + Jackson; key `checkout:session:{userId}`; 30-min TTL) for Redis-backed checkout session management (#76)
- `CheckoutSession` / `CheckoutStep` — serializable session POJO and step enum (`PENDING_SHIPPING → PENDING_PAYMENT → PENDING_CONFIRM`) (#76)
- `ShippingMethod` JPA entity + `ShippingMethodRepository` mapping the `shipping_methods` table (created in #104/#87): name, baseCost, costPerKg, estimatedDaysMin/Max, isActive (#76)
- `AddressRepository` — for checkout address ownership validation (#76)
- `SetAddressRequest` + `SelectShippingRequest` — validated request payloads for checkout step 1 and step 2 (#76)
- `CheckoutSessionDTO` — response DTO exposing current session step, addressId, shippingMethodId, shippingCost, orderId, razorpayOrderId (#76)
- `CheckoutFlowIntegrationTest` — 13 integration tests covering happy paths, invalid step transitions (409), wrong-owner address (404), inactive shipping method (404), unauthenticated access (401), and GET /shipping-options (authenticated with active cart → 200, unauthenticated → 401); `@MockBean CheckoutSessionStore` and `@MockBean PaymentService` used to avoid Redis/Razorpay in tests (CHK-01, #76, #262)
- Inventory management admin endpoints: `GET /api/v1/admin/inventory` (list all with `InventoryDTO` — productId, productName, qty, reservedQty, availableQty, status) and `PATCH /api/v1/admin/inventory/{productId}` (delta-based adjustment with required reason; validates result ≥ 0; `@Auditable`) (ADM-06, #72)
- `InventoryAuditLog` JPA entity + `InventoryAuditLogRepository` mapping the `inventory_audit_log` table (created in #104); every `PATCH` adjustment writes a record with before/change/after quantities, reason, actor, and `referenceType=MANUAL` (#72)
- `InventoryDTO` — list-view DTO for admin inventory endpoint (#72)
- `AdjustInventoryRequest` — `{ @NotNull Integer delta, @NotBlank String reason }` request payload (#72)
- `AdminInventoryControllerIntegrationTest` — 9 integration tests covering list, positive/negative delta, below-zero guard (400), missing reason (400), 404, role enforcement (403), and audit log record verification (ADM-06, #72)
- Order management admin endpoints at `GET/PATCH /api/v1/admin/orders` — list with status/userId/dateFrom/dateTo filters + pagination, full detail with nested items, forward-only status transitions (PENDING→CONFIRMED→SHIPPED→DELIVERED; any→CANCELLED) enforced server-side; `PATCH` is `@Auditable`; customer notification dispatched on each transition (ADM-03, #69)
- `OrderSpecification` — composable `JpaSpecificationExecutor`-based filter for admin order listing (#69)
- `OrderSpecificationTest` — 13 unit tests: fetch-join guard (entity query adds join, count query skips it to avoid JPA exception), `isDeleted=false` predicate always applied, all 4 optional filters (status, userId, dateFrom, dateTo) present and absent, all filters combined; raw `@Mock Path isDeletedPath` used to satisfy `cb.isFalse(Expression<Boolean>)` type signature (#69, #265)
- `AdminOrderDetailDTO` + `OrderItemDTO` — full admin order view with nested item lines (#69)
- `UpdateOrderStatusRequest` — validated request payload for status transitions (`@NotBlank status`, optional `cancellationReason`) (#69)
- `NotificationServiceImpl` — stub implementation of `INotificationService`; logs all calls; real email delivery wired in #62 (#69)
- `NotificationServiceImplTest` — 14 unit tests covering all dispatch methods (`sendOrderConfirmation`, `sendShipmentNotification` both overloads, `sendPasswordResetEmail`, `sendVerificationEmail`), null-user/null-email early-exit guards, null optional fields, shipping address context branch, anonymous `MailException` subclass, and `MessagingException` → `MailException` wrapping in `sendEmail`; `@Value` fields injected via `ReflectionTestUtils`; real `MimeMessage` used for success paths, mocked for error paths (#69, #263)
- `AdminOrderControllerIntegrationTest` — 13 integration tests covering list, filter, detail, valid/invalid transitions, and role enforcement (ADM-03, #69)
- Product CRUD admin endpoints at `POST/GET/PUT/DELETE /api/v1/admin/products` — all `@PreAuthorize("hasRole('ADMIN')")`, all `@Auditable`; image upload via `POST /api/v1/admin/products/{id}/images` (multipart/form-data, 10 MB cap, JPEG/PNG/WebP/GIF only) (ADM-01, #67)
- `StorageService` interface + `LocalStorageService` implementation (UUID-keyed filenames, configurable `app.storage.location`, static serving via `/uploads/**`) (#67)
- `StorageConfig` (`WebMvcConfigurer`) serving uploaded files from the configured storage directory (#67)
- `Product.updateProductImage` service method for atomic image URL updates (#67)
- `AdminProductControllerIntegrationTest` — 11 integration tests covering create, read, update, soft-delete, image upload, and role-enforcement (403/401) (ADM-01, #67)
- `/api/v1/admin/**` URL-level `hasRole("ADMIN")` rules added to `SecurityConfig` and `TestSecurityConfig` (was missing; only `/api/admin/**` was covered) (#67)
- Liquibase XML master orchestrator (`db.changelog-master.xml`) replacing direct SQL master reference; enables per-entity XML changeset files and clean include-based composition (#104)

- M4 test coverage gap remediation — pushed all packages above the JaCoCo 85% instruction-coverage gate; issues #258, #261–#265, #267–#269:
  - `RateLimiterServiceTest` +7 tests: circuit breaker OPEN path (`isAllowed` returns `true`), null increment, null TTL, `resetRateLimit` on error and on OPEN breaker (#258)
  - `ProductReviewServiceImplTest` +12 tests: duplicate-review guard, user/product not-found paths for `createReview`/`updateReview`/`deleteReview`/`markAsHelpful`, empty rating distribution, purchase-false path, all delegation methods (`getReviews`, `getReviewSummary`, `getTopHelpfulReviews`, `getReviewsByUser`) (#258)
  - `HttpsEnforcementFilterTest` (new) — 6 tests: enforcement disabled passes through, secure connection passes, insecure without header → 403, `X-Forwarded-Proto: https` overrides socket → pass, `X-Forwarded-Proto: http` overrides secure socket → 403, case-insensitive header comparison (#261)
  - `RedisCheckoutSessionStoreTest` (new) — 6 tests: `save` serialises with correct key (`checkout:session:{userId}`) and 30-minute TTL, serialisation failure → `IllegalStateException`, `find` absent/valid/corrupted JSON (corrupted deletes the key), `delete` removes key (#262)
  - `ProductControllerV2Test` +1 test: `searchProducts` when `Optional<ProductSearchService>` is present delegates to ES service and never calls JPA fallback (#264)
  - `ApiSunsetInterceptorTest` expanded 6 → 12 tests: `replacedBy` non-empty adds `X-API-Replaced-By` header, `warningDays=1000000` triggers approaching-sunset warning permanently without date rot, class-level `@ApiSunset` annotation applies headers, expired sunset with `replacedBy` includes replacement in 410 body, writer exception does not propagate, `enforce=false` past-sunset logs warn and allows request (#267)
  - `AdminServiceImplTest` +6 tests: `getUserById` not-found, `updateUser(Long, User)` (method was 0% covered), `updateUser` not-found, `updateUserByAdmin` not-found, `deleteUser` not-found, `convertToDto` with null roles (#268)
  - `AdminProductControllerTest` +3 tests: `createProduct` exception → 400, `updateProduct` exception → 400, `uploadProductImage` general `Exception` (second catch block) → 500 (#268)
  - `InventoryThresholdManagementServiceTest` +13 tests: not-found paths for all 6 methods (`setProductThreshold`, `setCategoryThreshold`, `getProductThreshold`, `getCategoryThreshold`, `useProductCategoryThreshold`, `getEffectiveThreshold`), `getCategoryThreshold` cache-miss with null category threshold returns 0, `getEffectiveThreshold` with `useCategoryThreshold=true` but no category falls back to product threshold (#269)
  - `InventoryReservationCleanupJobTest` (new) — 2 tests: success delegation and exception-swallowing; `InventoryReservationIntegrationTest` tests `InventoryServiceImpl`, not the scheduler (#269)
  - `InventoryMonitoringScheduler` confirmed at practical ceiling (83.3%): only uncovered lines are the catch block of `generateDailyInventorySummary()` whose try block contains only `log.info()` — dead code by design, no artificial test written (#269)
- `RazorpayClientAdapterTest` +3 tests: `createOrder` exception → `RuntimeException`, `refundPayment` exception → `PaymentProcessingException`, `fetchPaymentDetails` exception → `RuntimeException`; `initClient()` failure path is at practical ceiling (requires PowerMock-style constructor interception not available in Mockito) (#266)
- `ConstraintValidatorsTest` (new — 34 tests, `validator` package) + `EmailPhoneValidatorTest` +3 tests (`validation` package): full coverage of all 7 package-private `ConstraintValidator` implementations — `EmailValidator`, `PasswordStrengthValidator`, `PhoneNumberValidator`, `PostalCodeValidator`, `PriceValidator`, `QuantityValidator`, `SKUValidator`; each covers null, valid, boundary, and invalid-format cases; both null-passthrough and length/range guard branches are now killed by PIT (#260)

### Fixed
- `start.sh`: `cleanup()` could fire multiple times on Ctrl+C — backgrounded subshells inherit the parent's trap table, so each ran its own copy on exit; added a `CLEANED_UP` guard and `trap - EXIT INT TERM` inside each subshell to clear the inherited traps
- `start.sh`: MySQL health-wait loop had no timeout and would spin forever with no error if MySQL never became healthy (e.g. an unfilled `.env` copied fresh from `.env.example`, the exact state the script itself creates on first run); added a 120s timeout with a `docker compose logs mysql` pointer
- `SecurityConfig` and `TestSecurityConfig`: add `POST /api/auth/refresh` to `permitAll()` — was returning 401 when access token expired, forcing clients into a broken loop
- `SecurityConfig` and `TestSecurityConfig`: split blanket `/api/password/**` permit into explicit `/forgot` and `/reset` (public) and `/change` (requires `USER` or `ADMIN`) — unauthenticated callers could previously invoke the password-change endpoint
- `CheckoutController.calculateTotal` (`GET /api/checkout/calculate-total/{cartId}`): add `@PreAuthorize("hasRole('USER')")` — the only method in the controller missing the annotation its siblings already carried
- `WebhookAdminController`: add class-level `@PreAuthorize("hasRole('ADMIN')")` — the only controller under `/api/admin/**` without an authorization annotation
- `CacheConfig.cacheManager` now injects the Spring-managed `ObjectMapper` bean into `GenericJackson2JsonRedisSerializer` (all 9 cache regions: products, categories, auditLogs, userPermissions, inventoryItems, rateLimitStats, orders, users, and default); previously each region created its own bare `ObjectMapper` instance that could not handle Hibernate lazy-proxy types, causing silent cache-write failures for JPA entities
- `application.properties`: add `spring.jpa.properties.hibernate.type.preferred_boolean_jdbc_type=TINYINT` so Hibernate 6.x schema validation correctly maps `Boolean` entity fields to MySQL `TINYINT(1)` (MySQL `BOOLEAN` synonym) instead of `BIT(1)`, eliminating column-type mismatch errors on startup
- `application.properties`: externalize `spring.jpa.hibernate.ddl-auto` via `${SPRING_JPA_DDL_AUTO:validate}` to allow environment-specific override without code changes

### Changed
- `App.tsx`: added `/products/:id` route wired to `ProductDetailPage` (FE-02, #93)
- `src/components/product/ProductCard.tsx`: added `linkable?: boolean` prop (default `true`); when `false` renders the card div without a wrapping `<Link>`, enabling parent-controlled navigation in `RelatedProducts` (#93)
- `SecurityConfig` and `TestSecurityConfig`: add `GET /api/products/*/reviews`, `/reviews/summary`, and `/reviews/top-helpful` to `permitAll()` (required for unauthenticated product detail page) (#93)
- `App.tsx` migrated from JSX stub to full TypeScript: wraps `ProductListingPage` in `BrowserRouter` + `Routes` (react-router-dom) (FE-01, #92)
- `main.tsx` migrated from `.jsx` to `.tsx`: null-checked root element, no `.jsx` import extension (#92)
- `vite.config.js` replaced by `vite.config.ts`: added `@tailwindcss/vite` plugin and `/api` proxy to `http://localhost:8080` (#92)
- `src/index.css` replaced with Tailwind v4 import (`@import "tailwindcss"`) (#92)
- `eslint.config.js`: added `typescript-eslint`; disabled `react-hooks/set-state-in-effect` rule (hooks v7 incorrectly flags valid async data-fetching patterns documented by the React team) (#92)
- `ProductControllerV2.searchProducts` now routes to `ProductSearchService` (ES) when the bean is present, falling back to `ProductService.advancedSearch` (JPA) when absent; uses `Optional<ProductSearchService>` injection (#74)
- `ProductServiceImpl.advancedSearch` replaced in-memory stream filter with proper JPA `ProductRepository.advancedSearch` query (fixes O(n) memory load and incorrect pagination); `findByCategory` and `getProductsByCategory` likewise replaced with repository-level queries (#74)
- `ProductServiceImpl` now publishes `ProductCreatedEvent`, `ProductUpdatedEvent`, `ProductDeletedEvent` from `createProduct`, `updateProduct`, `deleteProduct` via `DomainEventPublisher` (#75)
- `TestElasticsearchConfig` extended with `ProductSearchService` and `ProductElasticsearchRepository` mocks so integration tests remain ES-free (#74/#75)
- `Payment` entity extended with `refundedAmount`, `refundReason`, `refundInitiatedAt` fields; status comment updated to include `PARTIALLY_REFUNDED` (#61)
- `AdminOrderController` now injects `PaymentService` and exposes `POST /{id}/refund` (#61)
- `RazorpayClientAdapter.refundPayment` throws `PaymentProcessingException` instead of generic `RuntimeException` (#61)
- `Order.OrderStatus` enum extended with `PAID` and `PAYMENT_FAILED` to represent webhook-confirmed payment outcomes (#60)
- `OrderServiceImpl.VALID_TRANSITIONS` extended: `PAID→{SHIPPED, CANCELLED}`, `PAYMENT_FAILED→{CANCELLED}` (#60)
- `PaymentRepository` extended with `Optional<Payment> findByRazorpayOrderId(String)` for idempotent webhook lookup (#60)
- `PaymentServiceImpl` refactored to explicit constructor injection (`@Lazy OrderService` to avoid circular dependency); `processWebhookEvent` added; `RazorpayClientAdapter.refundPayment` fixed to call `razorpayClient.payments.refund()` (was a no-op stub) (#60)
- `/api/v1/webhooks/**` added to `permitAll()` in both `SecurityConfig` and `TestSecurityConfig` (#60)
- `MultiStepCheckoutController` extended with `GET /api/v1/checkout/shipping-options` delegating to `ShippingService` (#87)
- `CheckoutService` extended with 4 new methods (`setAddress`, `selectShipping`, `initiatePayment`, `confirmCheckout`); `CheckoutServiceImpl` extended with implementations + `AddressRepository`, `ShippingMethodRepository`, `PaymentService`, `CheckoutSessionStore` injected via constructor (#76)
- `AdminInventoryController` migrated from `/api/admin/inventory` to `/api/v1/admin/inventory`; `GET /` and `PATCH /{productId}` added; legacy sub-path endpoints retained for backward compatibility (ADM-06, #72)
- `InventoryRepository` extended with `@EntityGraph({"product"}) findAll()` to prevent N+1 queries on admin list (#72)
- `InventoryServiceImpl` extended with `getAllInventorySummary()` and `adjustStock()` (delta validation, audit write, status recalculation, `LowStockWarningEvent` if threshold crossed) (#72)
- `AdminInventoryControllerTest` URL references updated from `/api/admin/inventory` to `/api/v1/admin/inventory` (#72)
- `AdminOrderController` rewritten at `/api/v1/admin/orders` (was `/api/admin/orders`); removed try/catch anti-pattern; delegates all error handling to `GlobalExceptionHandler` (ADM-03, #69)
- `OrderRepository` extended with `JpaSpecificationExecutor<Order>` for specification-based admin queries (#69)
- `OrderServiceImpl` extended with `adminUpdateOrderStatus` (transition validation + notification) and `getAdminOrders` / `getAdminOrderDetail` admin methods (#69)
- `AdminProductController` base path corrected from `/api/admin/products` to `/api/v1/admin/products` (ADM-01, #67)
- `deleteProduct` changed from hard delete (`deleteById`) to soft delete (`isActive = false`) (ADM-01, #67)
- `AuditAspectIntegrationTest` and `InputValidationSecurityTest` URL references updated to `/api/v1/admin/products` (#67)
- Liquibase changeset `addresses` table: user address book with user FK, default-flag, address-type, and covering indexes (USR-01, #78, #104)
- Liquibase changeset `product_variants` table: size/colour variants per product with SKU uniqueness, price adjustment, and stock quantity (PROD-01, #81, #104)
- Liquibase changeset `product_images` table: multiple ordered images per product with primary-flag (PROD-02, #82, #104)
- Liquibase changeset `product_tags` + `product_tag_map` join table: many-to-many product tagging with slug uniqueness (PROD-03, #83, #104)
- Liquibase changeset `coupons` table: PERCENTAGE/FIXED_AMOUNT discount codes with usage limits, validity windows, and min-order threshold (CHK-02, #77, #104)
- Liquibase changeset `shipping_methods` table: base cost, per-kg cost, and estimated delivery day range (SHIP-01, #87, #104)
- Liquibase changeset `return_requests` table: order return/refund flow with status lifecycle (PENDING → APPROVED/REJECTED → REFUNDED) and admin notes (RET-01, #88, #104)
- Liquibase changeset `inventory_audit_log` table: full inventory change audit trail with change type, before/after quantities, reference type/ID, and actor FK (ADM-06/INV-01, #72, #73, #104)

---

## [0.4.0] — 2026-06-24 (M3: Technical Debt Reduction)

### Changed
- Upgrade Elasticsearch, Kibana, and Logstash Docker images from 8.10.2 to 8.17.6 (#236)
- Relocate git repository root from `backend/` to `BuildNest/` project root to bring frontend and CI/CD files under version control (#233)
- Harden Content Security Policy: remove `'unsafe-inline'` from `script-src` and `style-src` on all API paths; introduce dedicated `@Order(1)` `SecurityFilterChain` scoping `'unsafe-inline'` to Swagger UI documentation paths only; add `frame-ancestors 'none'` and `form-action 'self'` to main chain (SEC-14, #237)
- Implement explicit circuit breaker fallbacks: add `elasticsearchCircuitBreaker` bean; protect all ES repository calls in `ElasticsearchIngestionService` (async writes skip silently on CB OPEN; reads return empty list); distinguish `CallNotPermittedException` from transient failures in `RateLimiterService` — CB OPEN logged at DEBUG, not WARN (#238)
- Raise JaCoCo instruction coverage gate from 50% to 55% — actual coverage 94.57%, no package below 55% (#239)
- Restore PIT mutation score from 73% to 85%: add boundary-value tests for HTTP status code classification, CB-OPEN fallback tests for all ES read/write paths, and non-empty return assertions on all read methods to kill surviving "replaced return value with emptyList" mutations (#240)
- Audit and enforce `@Auditable` AOP coverage on all admin endpoints: verify all `@PostMapping`, `@PutMapping`, `@DeleteMapping` in `admin/` carry `@Auditable`; add `AuditAspectIntegrationTest` (TC-AUDIT-001 to TC-AUDIT-006) proving audit entries capture user ID, action, entity type, and IP address through the full HTTP → AOP → AuditLogService pipeline; fix timing-sensitive `UptimeMonitoringServiceTest.uptimeMetricsFailSlaWhenDowntimeHigh` by replacing `Thread.sleep` with deterministic clock back-dating via `ReflectionTestUtils` (SRS ADM-06, RTM AUDIT-01, #59)

### Fixed
- `ElasticsearchConfig.clientConfiguration()` now passes injected credentials via `.withBasicAuth()` — previously credentials were declared but never forwarded to the client builder, causing HTTP 401 against any secured cluster (#236)
- Add explicit `FetchType.LAZY` to `Category.products` and `Order.orderItems` associations to eliminate N+1 query risk (DC-08, #54)

---

## [0.3.0] — 2026-06-22 (M2: Quality Foundation)

### Added
- OWASP Dependency-Check Maven plugin (`owasp` profile, CVSS ≥ 7.0 fails build) (#53)
- Integration tests for rate-limiting behaviour using Bucket4j/Redis (#51)
- Comprehensive unit tests for `CartService` and `WishlistService` — 15 and 14 tests respectively (#50)
- Comprehensive unit tests for `OrderServiceImpl` including edge cases (#49)
- Comprehensive unit tests for `ProductServiceImpl` including pagination edge cases (#48)
- Edge-case unit tests for `AuthServiceImpl` (#47)
- All 62 required environment variables documented across 16 sections in `.env.example` (#52)

### Changed
- Raise JaCoCo instruction coverage gate from 40% to 50% (#46)

### Fixed
- `ProductServiceImpl.advancedSearch()` and `findByCategory()` pagination bug corrected as part of test expansion (#48)

---

## [0.2.0] — 2026-06-14 (M1: Stabilisation)

### Fixed
- Add missing `RoleRepository` mock in `AuthServiceImplTest` — test context failed to load (#38)
- Add `@Tag("e2e")` to `OrderApiTest` so it is correctly excluded from the unit-test profile (#40)
- Correct HTTP status assertions in `testRoleHierarchyEnforcement` — expected 403, was asserting 200 (#41)
- Correct XSS test assertion in `InputValidationSecurityTest`: 401 → 400 (#42)
- Correct file-upload test assertions in `InputValidationSecurityTest`: 401 → 415 (#43)

---

## [0.1.0] — 2026-06 (Pre-M1: Foundation)

### Added
- Spring Boot 3.5 / Java 21 backend: REST API, JWT auth (access + refresh tokens), Spring Security RBAC with `Permission` entities
- JPA entities: `User`, `Product`, `Category`, `Order`, `OrderItem`, `Cart`, `CartItem`, `Wishlist`, `Inventory`, `Payment`, `ProductReview`, `AuditLog`, `Role`, `Permission`, `RefreshToken`, `PasswordResetToken`, `WebhookSubscription`
- Liquibase-managed schema migrations (DDL auto = `validate`)
- Redis-backed rate limiting (Bucket4j) and caching (`@Cacheable`)
- Elasticsearch 8.x integration for audit log ingestion, metrics collection, and alerting
- `@Auditable` AOP aspect for declarative audit logging
- `ApiSunsetInterceptor` adding deprecation headers to v1 product endpoints
- Dual API versioning: `ProductControllerV1` and `ProductControllerV2`
- Docker Compose stack: MySQL 8.2, Redis 7, Elasticsearch, Kibana, Logstash, Prometheus
- GitHub Actions CI/CD: build, test, JaCoCo coverage, CodeQL, OWASP Dependency-Check
- Structured Logback/Logstash JSON logging pipeline to Elasticsearch
- Resilience4j circuit breaker configuration
- SDLC documentation suite: SRS (ISO/IEC/IEEE 29148), SDD (IEEE 1016), RTM, Test Plan (ISO 29119-3), SDP, V&V Report, BRD, ICD, CSD, metrics and quality reports
- React 19 / Vite frontend scaffold (stub; real UI not yet built)
