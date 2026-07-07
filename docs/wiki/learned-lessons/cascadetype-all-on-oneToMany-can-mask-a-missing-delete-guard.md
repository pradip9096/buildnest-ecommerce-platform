# CascadeType.ALL on a @OneToMany can mask a missing delete guard — and get baked into a test as "correct"

While implementing ADM-02 (#68, admin category CRUD), `Category.products` was mapped
`@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)`. The
issue's acceptance criteria required category deletion to be *blocked* while products still
reference it — but as mapped, deleting a `Category` would cascade-delete every `Product` in it
instead, silently doing the opposite of what was asked.

A pre-existing repository-level test, `CategoryRepositoryTest.testDeleteCategoryWithProducts`,
had encoded this cascade-delete as the expected, correct behavior — its own `@DisplayName` was
"Delete category with products (cascade)" and it asserted the product *was* deleted afterward.

**Why this matters:** removing the cascade (the correct fix) broke that pre-existing test, which
initially looked like a regression. Per [[tests-that-assert-the-bug-are-not-protected-by-the-no-weaken-assertions-rule]],
a failing test that asserts *the old, wrong behavior* is not a signal to revert the fix or weaken
the assertion — it's a signal the test itself encodes a bug and needs rewriting to assert the
new, correct behavior. In this case: rewrote the test to assert that a repository-level delete of
a category with products throws (Hibernate's own flush-time consistency check rejects it, as a
defense-in-depth backstop below the service-layer guard), and that neither row is actually deleted.

**How to apply:** Before accepting `cascade = CascadeType.ALL` (or `CascadeType.REMOVE`) on any
`@OneToMany`, check whether the parent side is ever expected to be deletable independently of its
children — if the AC or business rule says "block deletion while children exist," cascade=ALL is
the wrong mapping regardless of what today's tests assert. When a fix to production code breaks
an existing test, read what that test is actually asserting before treating the failure as a
regression — it may be testing the exact bug being fixed.
