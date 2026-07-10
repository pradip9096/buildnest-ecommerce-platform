---
title: "Verifying Liquibase Seed-Data Changesets When the Test Profile Uses ddl-auto=create-drop"
category: testing
tags: [liquibase, hibernate, ddl-auto, create-drop, h2, seed-data, spring-boot-test]
keywords: [liquibase seed data disappears in test, ddl-auto create-drop wipes changeset, SpringBootTest cannot verify seed row, Liquibase close cascades connection, hibernate schema generation after liquibase, mvn -q suppresses test summary on success]
source_conversations: [Session 2026-07-05]
last_updated: 2026-07-05
confidence: high
evidence_strength: strong
related_lessons:
  - docs/wiki/learned-lessons/env-sourcing-and-cache-pitfalls-fixing-liquibase-data.md
  - docs/wiki/learned-lessons/stale-test-classes-false-failures.md
---

# Verifying Liquibase Seed-Data Changesets When the Test Profile Uses ddl-auto=create-drop

## Problem

Added a Liquibase changeset (`20260704-013-seed-default-shipping-method`, #304) that inserts one row into `shipping_methods`. A `@SpringBootTest` asserting the row existed via `shippingMethodRepository.findAll()` failed with "found none" — even though DEBUG-level Liquibase logging proved the `INSERT` executed successfully ("New row inserted into shipping_methods... ran successfully in 10ms").

## Root cause

`application-test.properties` sets `spring.jpa.hibernate.ddl-auto=create-drop`. Spring Boot runs Liquibase migrations first, then Hibernate's schema generation step — which, under `create-drop`, **drops and recreates every entity-mapped table**, including any table also mapped by a JPA `@Entity` (here, `ShippingMethod`). The Liquibase-seeded row is wiped before any `@SpringBootTest` method body runs. Production uses `ddl-auto=validate` (schema-check only, no drop/recreate), so this is a test-profile-only interaction — the seed changeset itself was correct all along.

**Diagnostic path that worked:** don't trust "it fails, so the changeset is wrong" — rule that out first with direct evidence. Re-ran the single failing test with `-Dlogging.level.liquibase=DEBUG` (the default `logging.level.root=WARN` suppresses Liquibase's own success logs) to get a full execution trace. Seeing the `INSERT` succeed at the Liquibase layer redirected the investigation to "something after Liquibase is erasing it" rather than "the changeset is broken."

## Fix: a Spring-context-free Liquibase-only test

For changesets whose effect can't survive `@SpringBootTest`'s Hibernate schema regeneration, verify them by running Liquibase directly against a throwaway JDBC/H2 connection, bypassing Spring (and Hibernate) entirely:

```java
String jdbcUrl = "jdbc:h2:mem:shipping_seed_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
connection = DriverManager.getConnection(jdbcUrl, "sa", "");
Database database = DatabaseFactory.getInstance()
        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
Liquibase liquibase = new Liquibase(
        "db/changelog/db.changelog-master.xml",
        new ClassLoaderResourceAccessor(),
        database);
liquibase.update("");
// assert directly against `connection` afterward
```

This is a new, reusable pattern for this codebase — no precedent existed before. See `ShippingMethodSeedMigrationTest`.

## Gotcha 1: `Liquibase.close()` cascades to closing the shared JDBC connection

Wrapping the above in try-with-resources (`try (Liquibase liquibase = new Liquibase(...)) { ... }`) closes `liquibase` at the end of the block — which closes the underlying `Database`, which closes the wrapped `Connection`. If a later test method (or `@AfterEach`) needs that same `connection` for assertions, it fails with `JdbcSQLNonTransient: The object is already closed [90007-232]`.

**Fix:** don't close `liquibase` explicitly (or try-with-resources it) when the connection must outlive the migration call. Let `@AfterEach` close the raw `connection` once, after all assertions are done. Document why the `Liquibase` object is deliberately left unclosed — it reads as a resource leak to static analysis otherwise.

## Gotcha 2: `mvn -q test` gives no green-path confirmation text

`./mvnw -q test` suppresses Surefire's `Tests run: X, Failures: Y` summary lines entirely when everything passes — that output only appears on failure. Grepping the log for `"Tests run:"` or `"BUILD SUCCESS"` after a passing `-q` run finds nothing, which can look like the run never completed. The actual signal to check is the shell **exit code** (captured immediately after the command, not after a `tail`/`grep` pipe — see the exit-code-masking lesson) combined with the *absence* of `BUILD FAILURE` / `[ERROR]` / `Tests run:.*Failures: [1-9]` lines.
