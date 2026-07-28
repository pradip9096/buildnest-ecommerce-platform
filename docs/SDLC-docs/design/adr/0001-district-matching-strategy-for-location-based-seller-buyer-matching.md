# 0001. District-matching strategy for location-based seller-buyer matching

* Status: accepted
* Date: 2026-07-28
* Issue: #561

## Context and Problem Statement

SRS v5.0 §3.2.12 (FG-12, Location-Based Matching) and SDD v4.0 §4.5.6 defined FR-LOC-01–04
but deliberately left two open questions unresolved, blocking implementation of #562/#563/#564:

* **OQ-01**: Is district matching strict (buyer sees only same-district sellers) or
  radius-based (seller declares which nearby districts they'll deliver to)? This determines
  the `Seller`–`District` entity relationship (`N:1` vs. `N:M`) and the Elasticsearch query
  shape used by FR-LOC-03/FR-LOC-04.
* **OQ-02**: Is district determined from a fixed, admin-maintained reference table, or
  free-text/geocoded from address?

`Seller.district_id` already exists in the schema as a nullable column with no FK constraint
(#553), explicitly deferred pending this decision (SDD Revision 4.1, RTM Revision 1.26).

## Decision Drivers

* Real-world delivery reach — a seller near a district boundary plausibly delivers to more
  than one district; a strict same-district rule would under-serve them and their buyers,
  especially in sparsely-populated districts with few or no local sellers.
* Repo precedent — this is the first architectural decision going through the ADR process
  as a standalone `solution-options-adr` issue (per the sibling-precedent check run this
  session), so there's no existing similar decision to match; the choice must stand on its
  own merits.
* Operational simplicity — avoid a new external dependency (geocoding service) unless the
  matching strategy actually requires the finer-grained precision only geocoding provides.
* Consistency with the existing `spring/resilience4j.md`/`spring/elasticsearch.md` conventions
  — any new external dependency (e.g. geocoding) would need its own circuit breaker and
  graceful-degradation fallback, adding real implementation surface.

## Considered Options

* **OQ-01**: Strict same-district (`Seller ──[N:1]──► District`) vs. radius/seller-declared
  (`Seller ──[N:M]──► District`)
* **OQ-02**: Fixed, admin-maintained reference table vs. free-text/geocoded from address

## Decision Outcome

Chosen: **radius/seller-declared matching (N:M)** for OQ-01, and **fixed, admin-maintained
reference table** for OQ-02.

* OQ-01 — "Radius / seller-declared (N:M)", because it better reflects real-world seller
  delivery reach and avoids under-serving buyers in sparsely-populated districts, at the
  accepted cost of a join table and a `terms`-query filter instead of an exact match.
* OQ-02 — "Fixed reference table", because it requires no new external dependency, no
  geocoding cost or failure mode, and matches the `districts(id, name UNIQUE)` table SDD
  §4.5.1/§4.5.2 already sketched — SDD's own simplicity/no-new-dependency driver wins over
  geocoding's finer-grained proximity, which nothing in FR-LOC-01–04 actually requires.

### Consequences

* Good, because sellers can serve multiple adjacent districts, better matching real delivery
  patterns and giving buyers in under-served districts more seller options.
* Good, because a fixed reference table needs no new circuit breaker/fallback wiring — no
  geocoding API to protect against per `resilience4j.md`'s dependency-tier conventions.
* Bad, because the entity model is a join table (`seller_districts`) rather than a single FK
  column — `Seller.district_id` (added in #553) must be migrated: the existing nullable
  column is replaced by a join table, requiring a data migration path for any seller rows
  already carrying a value (none exist yet, since FG-12 is unimplemented — a straightforward
  Liquibase `dropColumn` + `createTable` migration, no backfill needed).
* Bad, because the Elasticsearch query for FR-LOC-03/FR-LOC-04 becomes a `terms` filter
  against a seller's declared district list rather than a single exact-match `term` filter —
  marginally more expensive per SDD §4.5.6's own analysis, but not a materially different
  Elasticsearch capability.
* Bad, because the admin must seed and maintain the `districts` reference table — no
  self-service district creation from user-entered addresses.

## Pros and Cons of the Options

### OQ-01: Strict same-district (N:1)

* Good, because it is the simpler entity model (single FK) and query (exact `term` match) —
  SDD §4.5.6 itself called this "the simpler of the two options."
* Bad, because it under-serves buyers in districts with few or no local sellers, and doesn't
  reflect that many sellers can and do deliver beyond their home district.

### OQ-01: Radius / seller-declared (N:M) — chosen

* Good, because it matches real-world delivery reach and gives sellers more customers.
* Bad, because it requires a join table and a `terms` query instead of a simple FK/exact match.

### OQ-02: Fixed reference table — chosen

* Good, because it needs no external dependency, no geocoding cost, and matches SDD's
  existing `districts(id, name UNIQUE)` sketch.
* Bad, because the admin must seed and maintain the district list, and it can't express
  proximity finer than a whole district.

### OQ-02: Free-text / geocoded from address

* Good, because district assignment is automatic and could support true radius-in-km
  matching in the future.
* Bad, because it adds an external geocoding dependency, its own cost and failure mode, and
  a new resilience/circuit-breaker surface — none of which FR-LOC-01–04 actually requires.
