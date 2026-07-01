# Q&A: Software Testing Fundamentals and Quality Metrics

> **Category:** Testing & Quality Assurance
> **Tags:** `java`, `testing`, `jacoco`, `pitest`, `code-coverage`, `mutation-testing`, `ci-cd`, `static-analysis`, `dynamic-analysis`, `test-techniques`, `bva`, `equivalence-partitioning`, `fuzzing`, `dast`, `test-levels`, `testing-pyramid`
> **Level:** Beginner → Advanced
> **Last Updated:** 2026-07-01 (gap-fix revision: 14 knowledge and logical gaps resolved)

> [!NOTE]
> This document is organised **pedagogically**, not chronologically. Questions progress from foundational concepts to advanced application, following a strict prerequisite-before-dependent ordering aligned with Bloom's Revised Taxonomy: **Remember → Understand → Apply → Analyze → Evaluate → Create**.

---

## Table of Contents

- [Foundational Definitions](#foundational-definitions)

### Q1: What is the difference between a static and dynamic solution for defects?
- [Short Answer](#short-answer)
- [Detailed Answer](#detailed-answer)
  - [1. Static Solutions](#1-static-solutions-static-testing--analysis)
  - [2. Dynamic Solutions](#2-dynamic-solutions-dynamic-testing--analysis)
    - [Test Levels and the Testing Pyramid](#test-levels-and-the-testing-pyramid)
  - [Summary Comparison](#summary-comparison)
- [Bottom Line](#bottom-line)

### Q2: What are the primary Black-Box and White-Box test design techniques used in industry?
- [Short Answer](#short-answer-1)
- [Detailed Answer](#detailed-answer-1)
  - [1. Famous Black-Box Techniques](#1-famous-black-box-techniques)
    - [Boundary Value Analysis (BVA)](#boundary-value-analysis-bva)
    - [Equivalence Partitioning (EP)](#equivalence-partitioning-ep)
    - [Decision Table Testing](#decision-table-testing)
    - [State Transition Testing](#state-transition-testing)
    - [Error Guessing](#error-guessing)
  - [2. Famous White-Box Techniques](#2-famous-white-box-techniques)
    - [Statement Coverage](#statement-coverage)
    - [Branch / Decision Coverage](#branch--decision-coverage)
    - [Condition Coverage](#condition-coverage)
    - [Path Coverage](#path-coverage)
    - [Mutation Testing](#mutation-testing-pointer-to-q6)
  - [Where These Techniques Sit in the Broader Picture](#where-these-techniques-sit-in-the-broader-picture)
- [Bottom Line](#bottom-line-1)

### Q3: Where do Code Coverage and PIT Mutation Score fit within the broader taxonomy of Test Adequacy Criteria?
- [Short Answer](#short-answer-2)
- [Detailed Answer](#detailed-answer-2)
  - [The Umbrella Term](#the-umbrella-term)
  - [Full Taxonomy of Test Adequacy Criteria](#full-taxonomy-of-test-adequacy-criteria)
  - [Tier 1 — Specification-Based Testing](#tier-1--specification-based-testing-black-box)
  - [Tier 2 — Property-Based Testing](#tier-2--property-based-testing)
  - [Tier 3 — Fuzzing](#tier-3--fuzzing-automated-chaos-input)
  - [Tier 4 — Static Analysis](#tier-4--static-analysis-no-tests-required)
  - [Tier 5 — Formal Verification](#tier-5--formal-verification-mathematical-proof)
  - [The Confidence Stack](#the-confidence-stack)
  - [Summary Table](#summary-table)
- [Bottom Line](#bottom-line-2)

### Q4: What is dynamic analysis?
- [Short Answer](#short-answer-3)
- [Detailed Answer](#detailed-answer-3)
  - [What Does Dynamic Analysis Look For?](#what-does-dynamic-analysis-look-for)
  - [Common Types of Dynamic Analysis](#common-types-of-dynamic-analysis)
  - [SAST vs DAST — Security Testing Counterparts](#sast-vs-dast--security-testing-counterparts)
- [Bottom Line](#bottom-line-3)

### Q5: What are the primary applications of Fuzzing, Profiling, and DAST in dynamic analysis?
- [Short Answer](#short-answer-4)
- [Detailed Answer](#detailed-answer-4)
  - [1. Fuzzing (Fuzz Testing)](#1-fuzzing-fuzz-testing)
  - [2. Profiling](#2-profiling)
  - [3. DAST (Dynamic Application Security Testing)](#3-dast-dynamic-application-security-testing)
- [Bottom Line](#bottom-line-4)

### Q6: What is the difference between Java Code Coverage and PIT Mutation Score?
- [Short Answer](#short-answer-5)
- [Detailed Answer](#detailed-answer-5)
  - [🧱 Foundation: What Problem Are They Solving?](#-foundation-what-problem-are-they-solving)
  - [1. Code Coverage — Did my tests visit the code?](#1-code-coverage----did-my-tests-visit-the-code)
    - [Coverage Type Hierarchy](#coverage-type-hierarchy)
  - [2. PIT Mutation Score — Do my tests actually catch bugs?](#2-pit-mutation-score----do-my-tests-actually-catch-bugs)
  - [3. Side-by-Side Comparison](#3-side-by-side-comparison)
  - [4. The Analogy](#4-the-analogy)
  - [5. The Hierarchy of Trust](#5-the-hierarchy-of-trust)
  - [6. Practical Guidance](#6-practical-guidance)
    - [Equivalent Mutants — Why 100% Score Is Impossible](#equivalent-mutants--why-100-score-is-impossible)
  - [7. Key Takeaways (Pareto 80/20)](#7-key-takeaways-pareto-8020)
- [Bottom Line](#bottom-line-5)

### Q7: How do source code structures directly influence Code Coverage and Mutation Score?
- [Short Answer](#short-answer-6)
- [Detailed Answer](#detailed-answer-6)
  - [1. Source Code → Code Coverage](#1-source-code--code-coverage)
  - [2. Source Code → PIT Mutation Score](#2-source-code--pit-mutation-score)
  - [3. The Unified Picture](#3-the-unified-picture)
  - [4. Key Insight: Same Source Code, Different Lenses](#4-key-insight-same-source-code-different-lenses)
- [Bottom Line](#bottom-line-6)

### Q8: What are the different engineering workflows for applying source code, tests, and quality metrics?
- [Short Answer](#short-answer-7)
- [Detailed Answer](#detailed-answer-7)
  - [The Four Workflows Side by Side](#the-four-workflows-side-by-side)
  - [Why Order Matters: The Hidden Risk of the Classic Approach](#why-order-matters-the-hidden-risk-of-the-classic-approach)
  - [Where Coverage and PIT Fit — Regardless of Order](#where-coverage-and-pit-fit--regardless-of-order)
  - [The Recommended Pragmatic Workflow](#the-recommended-pragmatic-workflow)
  - [Role of Each Tool in the Workflow](#role-of-each-tool-in-the-workflow)
- [Bottom Line](#bottom-line-7)

- [Related Topics](#related-topics)

---

## Foundational Definitions

Before any test technique or metric makes sense, three foundational terms must be precise. These definitions align with IEEE 1044 and the ISTQB standard glossary.

| Term | Definition | Example |
|---|---|---|
| **Error** | A human action that produces an incorrect result | Developer misunderstands a requirement and writes wrong logic |
| **Defect (Bug)** | A flaw in a work product — the error as it exists in code, a document, or a design | `return age > 18` instead of `return age >= 18` in source code |
| **Failure** | Observable deviation of the system's actual behaviour from its expected behaviour at runtime | `isAdult(18)` returns `false` when it should return `true` |

**The causal chain:**

```
Human Error  →  introduces  →  Defect in code  →  when executed  →  Failure observed
```

**Important caveats:**
- Not every defect causes a failure (e.g., defect in dead code that is never reached)
- Not every failure has a single root error (complex interactions can cause emergent failures)
- Detecting a **failure** is the goal of dynamic testing; detecting a **defect** is the goal of static analysis

**What is a test?**
A test is a triple: `(Input, Execution, Oracle)` where the *oracle* is the rule that determines whether the output is correct. Without an assertion (the oracle), there is no test — only execution.

---

## Q1: What is the difference between a static and dynamic solution for defects?

---

### Short Answer

| | Static Solution | Dynamic Solution |
|---|---|---|
| **Code executed?** | ❌ No | ✅ Yes |
| **When** | Early — during coding/design | Later — during/after build |
| **Focus** | Structure, syntax, standards | Functional behaviour, runtime |
| **Cost to fix** | Cheaper (caught early) | More expensive (caught later) |
| **Prevents** | Defects being built in | Failures reaching users |

Neither is better — both are essential. A robust strategy uses **both** in combination.

> **Dynamic Analysis is explored in depth in Q4**, which deepens the dynamic side specifically and introduces the full taxonomy of dynamic techniques.

---

### Detailed Answer

#### 1. Static Solutions (Static Testing / Analysis)

Static solutions find defects **without executing the code**. You examine code, design, or requirements *at rest*.

**How it works:** Checks structure, syntax, and semantics against predefined rules and standards.

**Examples:**

| Type | Examples |
|---|---|
| **Manual** | Code reviews, peer reviews, pair programming, design walkthroughs |
| **Automated** | Linting (ESLint, Pylint), static analyzers (SonarQube), SAST tools |

**What it finds:**
- Syntax errors, dead code, unused variables
- Security vulnerabilities (via SAST)
- Deviations from coding standards
- Logical flaws — *before the code is ever run*

**Benefits:** Defects found very early → much cheaper and faster to fix. Points exactly to the offending line.

---

#### 2. Dynamic Solutions (Dynamic Testing / Analysis)

Dynamic solutions find defects by **executing the software** and observing its behaviour.

**How it works:** Run the application, provide inputs, check if actual outputs match expected results (the oracle).

**Examples:**

| Type | Examples |
|---|---|
| **Automated Testing** | Unit tests, Integration tests, End-to-End (E2E) tests |
| **Manual Testing** | QA engineers clicking through the application |
| **Performance/Security** | Load testing, stress testing, DAST |

**What it finds:**
- Runtime errors, memory leaks
- Performance bottlenecks
- Incorrect business logic
- UI/UX issues

**Benefits:** Validates software works as intended from a user's perspective. Catches complex integration failures that static analysis cannot see.

---

#### Test Levels and the Testing Pyramid

Dynamic testing is not monolithic — it is structured into **four levels**, each with a different scope and purpose.

| Level | Scope | Goal | Tools (Java) |
|---|---|---|---|
| **Unit** | Single class or method, in isolation | Verify individual logic units | JUnit 5, Mockito |
| **Integration** | Interaction between components or services | Verify components work together | Spring Test, Testcontainers |
| **System** | The entire deployed application | Verify end-to-end behaviour | Selenium, Playwright, REST Assured |
| **Acceptance (UAT)** | Business requirements validation | Confirm software meets user needs | Cucumber, FitNesse |

**The Testing Pyramid** (Mike Cohn, 2009) prescribes the optimal *distribution* of tests across levels:

```
          ▲
         /E\
        / 2 \
       / E   \        ← Few: slow, expensive, brittle
      /───────\
     /         \
    / Integration\    ← Moderate: medium speed and cost
   /─────────────\
  /               \
 /   Unit Tests    \  ← Many: fast, cheap, deterministic
/───────────────────\
```

> **Anti-pattern — The Ice Cream Cone:** When E2E tests dominate, the suite is slow, flaky, and expensive to maintain. Always bias toward the pyramid base.

---

#### Summary Comparison

| Feature | Static Solution | Dynamic Solution |
|---|---|---|
| **Execution** | Code is NOT executed | Code IS executed |
| **When it happens** | Early (coding/design phase) | Later (build/test phase) |
| **Focus** | Structural integrity, syntax, standards | Functional behaviour, performance, runtime |
| **Cost to fix** | Generally cheaper (caught early) | More expensive (caught later) |
| **What it prevents** | Defects from being built into the app | Failures from reaching the user |

### Bottom Line

> Use **static solutions** to catch obvious flaws and enforce standards early.
> Use **dynamic solutions** to ensure the running application behaves correctly under real-world conditions.
> Together they form a complete defect-prevention strategy.

---

## Q2: What are the primary Black-Box and White-Box test design techniques used in industry?

---

### Short Answer

Test techniques split into two families:

| Family | Based on | Practised by |
|---|---|---|
| **Black-Box** | Inputs & outputs only (ignores internal code) | QA engineers, business analysts |
| **White-Box** | Internal code structure | Developers writing unit tests |

The two most universally used daily techniques are **Boundary Value Analysis** and **Equivalence Partitioning**.

---

### Detailed Answer

#### 1. Famous Black-Box Techniques

##### Boundary Value Analysis (BVA)

Bugs overwhelmingly cluster at boundary edges. For a password field accepting 8–15 characters, BVA dictates testing:

```
❌ 7  (just below min)
✅ 8  (exact min)
✅ 9  (just above min)
   ...
✅ 14 (just below max)
✅ 15 (exact max)
❌ 16 (just above max)
```

You skip the middle (e.g., 11) — if 8 and 15 work, 11 almost certainly works.

---

##### Equivalence Partitioning (EP)

Reduce redundant tests by dividing inputs into groups where the system behaves identically. For an age field accepting 18–65:

```
Partition 1: Under 18  → Invalid  → test with: 16
Partition 2: 18 to 65  → Valid    → test with: 30
Partition 3: Over 65   → Invalid  → test with: 70
```

Only one value from each partition is needed — testing 30 *and* 25 *and* 40 is redundant.

---

##### Decision Table Testing

Used for complex business logic with multiple condition combinations. Maps all input combinations to expected outputs:

| Is User Premium? | Cart > $50? | Offer Free Shipping? |
|---|---|---|
| ✅ Yes | ✅ Yes | ✅ Yes |
| ✅ Yes | ❌ No | ✅ Yes |
| ❌ No | ✅ Yes | ✅ Yes |
| ❌ No | ❌ No | ❌ No |

Ensures no logical combination is missed.

---

##### State Transition Testing

Used when an application moves through defined states. Example — ATM:

```
[Idle] → Card Inserted → [Waiting for PIN] → PIN Entered → [Authenticated]
                                           → Wrong PIN   → [Locked] (after 3 attempts)
```

Tests both **valid transitions** (normal flow) and **invalid triggers** (e.g., withdrawing cash before entering PIN).

---

##### Error Guessing

> [!IMPORTANT]
> Error Guessing is **experience-based and non-systematic**. Unlike BVA, EP, Decision Tables, and State Transition — which are algorithmic and repeatable — Error Guessing relies entirely on the tester's intuition and domain knowledge. It cannot be taught as a step-by-step procedure; it improves only with experience.

A tester "guesses" where developers likely made mistakes:

- Submitting forms with emojis or special characters
- Pasting 10,000 characters into a name field
- Clicking "Submit" twice in rapid succession
- Leaving required fields empty
- Entering negative numbers where only positive are expected

Use Error Guessing to *supplement* systematic techniques, never to *replace* them.

---

#### 2. Famous White-Box Techniques

Used by developers writing unit tests, looking directly at the code. These form a strict **coverage hierarchy** — each level subsumes the one before it.

##### Statement Coverage

Ensure every single **line** of code executes at least once.

```java
public int max(int a, int b) {
    if (a > b) return a;   // line 1
    return b;               // line 2
}
// Need: one test where a > b, one where b >= a
```

**Weakness:** A line can be executed without the outcome being verified — see "critical flaw" in Q6.

---

##### Branch / Decision Coverage

Ensure every **if/else/switch branch** executes — a strict superset of statement coverage.

```java
if (user.isAdmin()) {
    grantAccess();   // branch: true
} else {
    denyAccess();    // branch: false
}
// Need: one test with admin user, one with non-admin user
```

> Branch coverage is what JaCoCo primarily measures and reports.

**Relationship:** 100% branch coverage guarantees 100% statement coverage. The converse is false.

---

##### Condition Coverage

Ensure every **boolean sub-expression** within a compound condition independently evaluates to both `true` and `false`. This is a strict superset of branch coverage.

```java
// Compound condition: two sub-expressions
if (a > 0 && b > 0) { ... }

// Branch coverage only needs: one true path, one false path
// Condition coverage requires:
//   a > 0 = true,  b > 0 = true   → (1,1) overall true
//   a > 0 = false, b > 0 = true   → (0,1) first sub-expression false
//   a > 0 = true,  b > 0 = false  → (1,0) second sub-expression false
```

**Why it matters:** Branch coverage can pass with `(a=1,b=1)` and `(a=-1,b=-1)` — but never independently tests the second sub-expression.

**Variant — MC/DC (Modified Condition/Decision Coverage):** Required by DO-178C for safety-critical avionics software. Demonstrates that each condition independently affects the overall decision outcome.

---

##### Path Coverage

Ensure every **distinct execution path** through the code is exercised.

```java
// For 3 independent boolean conditions:
// Possible paths = 2³ = 8 paths
// For n conditions: 2ⁿ paths — exponential growth
```

**Practical reality:** Path coverage is theoretically complete but **infeasible for non-trivial code**. A function with 10 independent conditions would require 1,024 tests. Use MC/DC as the practical alternative for safety-critical systems.

**Relationship:** 100% path coverage guarantees 100% condition coverage. The converse is false.

---

##### Mutation Testing *(pointer to Q6)*

A white-box technique where automated tools inject artificial defects ("mutants") into source code and verify that tests detect them. This measures *test effectiveness*, not just *code reachability*.

> **Fully explained in Q6**, which covers Code Coverage vs PIT Mutation Score in depth.

---

#### Where These Techniques Sit in the Broader Picture

```
Black-Box Techniques          White-Box Techniques (Coverage Hierarchy)
(behaviour, requirements)     (code structure — each row ⊃ all above it)
─────────────────────────     ──────────────────────────────────────────
BVA                           Statement Coverage          (weakest)
Equivalence Partitioning      Branch/Decision Coverage  ← JaCoCo
Decision Tables               Condition / MC/DC Coverage
State Transition              Path Coverage               (strongest, infeasible)
Error Guessing (heuristic)    Mutation Testing          ← PITest → see Q6
```

### Bottom Line

> **BVA and Equivalence Partitioning** are the two most universally applied techniques — every engineer and QA professional relies on them daily.
> White-box techniques form a strict hierarchy: achieving a higher level guarantees all lower levels, but the cost increases significantly.

---

## Q3: Where do Code Coverage and PIT Mutation Score fit within the broader taxonomy of Test Adequacy Criteria?

---

### Short Answer

> [!NOTE]
> **Code Coverage** and **PIT Mutation Score** are two specific metrics introduced fully in **Q6**. This section classifies where they fit within the broader landscape of test quality measurement.

Both fall under the umbrella of **Test Adequacy Criteria** (also called **Test Sufficiency Criteria**) — formal measures that answer:

> *"Have we tested enough?"*

They belong to the broader field of **Software Test Quality Metrics**. And yes — several more powerful techniques exist beyond them.

---

### Detailed Answer

#### The Umbrella Term

```
Software Test Quality Metrics
        │
        ├── Structural Metrics      ← Code Coverage lives here
        │     (Did tests reach the code?)
        │
        └── Fault-Based Metrics     ← PIT Mutation Score lives here
              (Do tests detect injected faults?)
```

#### Full Taxonomy of Test Adequacy Criteria

```
TEST ADEQUACY CRITERIA
│
├── 1. STRUCTURAL (White-Box)         ← Coverage family
│       Line, Branch, Condition,
│       Path, MC/DC coverage
│
├── 2. FAULT-BASED                    ← PIT Mutation family
│       Mutation testing,
│       Error seeding
│
├── 3. SPECIFICATION-BASED            ← Beyond Coverage + PIT
│       (Black-Box)
│
├── 4. PROPERTY-BASED                 ← Beyond Coverage + PIT
│
└── 5. FORMAL / MATHEMATICAL          ← Beyond Coverage + PIT
```

---

#### What Exists Beyond Code Coverage and PIT

##### Tier 1 — Specification-Based Testing *(Black-Box)*

Tests behaviour against *requirements*, not code structure. No source code needed.

| Technique | What it checks | Tool example |
|---|---|---|
| **Equivalence Partitioning** | Groups of inputs that behave identically | Manual / Cucumber |
| **Boundary Value Analysis** | Edge values of input ranges | Manual / JUnit params |
| **Decision Table Testing** | Combinations of business rules | FitNesse |
| **Model-Based Testing** | Behaviour matches a formal state machine | GraphWalker |

> **Key limitation of Coverage + PIT:** they tell you nothing about *unwritten* requirements. A feature you forgot to code has 0% coverage — but that gap is invisible.

##### Tier 2 — Property-Based Testing

Instead of fixed examples, you define **invariants** that must hold for *all* generated inputs.

```java
// Classic test (example-based)
assertEquals(5, add(2, 3));

// Property-based test
// "for ALL integers a, b: add(a,b) == add(b,a)"
@Property
void commutative(@ForAll int a, @ForAll int b) {
    assertEquals(add(a, b), add(b, a));
}
```

- **Tool (Java):** `jqwik`, QuickCheck (Haskell origin)
- **Catches:** Entire classes of bugs coverage and PIT cannot — because they only test what *you* thought to write

##### Tier 3 — Fuzzing (Automated Chaos Input)

Bombards the system with **random, malformed, or unexpected inputs** to find crashes, hangs, and security holes.

```
Input: "hello"      → OK
Input: ""           → OK
Input: null         → NullPointerException 💥  ← fuzz found it
Input: "A" × 10000 → StackOverflow 💥          ← fuzz found it
```

- **Tool (Java):** Jazzer, AFL
- **Strength:** Finds stability and security defects no human would think to test

##### Tier 4 — Static Analysis *(No Tests Required)*

Analyses source code **without running it** — catches bugs, smells, and vulnerabilities at compile time.

| Tool | What it detects |
|---|---|
| **SpotBugs** | Null dereferences, resource leaks, threading bugs |
| **PMD** | Code style violations, dead code, inefficiencies |
| **SonarQube** | Aggregate quality: bugs + smells + security hotspots |
| **Checkstyle** | Formatting and structural conventions |

> **Important:** Static Analysis is **orthogonal** to the dynamic testing techniques above — it operates *before* tests run and catches a fundamentally different class of defects. It complements dynamic testing; it does not compete with it.

##### Tier 5 — Formal Verification *(Mathematical Proof)*

Mathematically **proves** correctness — no tests needed, no counterexamples.

```
Theorem: ∀ n ∈ ℤ, sort(list) returns list in non-decreasing order
Proof: ...
```

- **Tools:** TLA+, Alloy, Coq, Dafny
- **Used in:** Safety-critical systems (aerospace, cryptography, finance protocols)
- **Caveat:** Extremely expensive; requires formal specification of requirements

---

#### The Confidence Stack

The following stack ranks techniques by the strength of their correctness guarantee for dynamic defects. **Static Analysis is orthogonal** (see note). Fuzzing and Mutation Testing operate at the same level but on **different defect dimensions** — they are complementary, not ordered.

```
HIGHEST CONFIDENCE (hardest, most expensive)
│
│  ✦ Formal Verification        — proves correctness mathematically
│  ✦ Property-Based Testing     — proves invariants across infinite inputs
│  ✦ PIT Mutation Score  ┐      — proves tests catch semantic bugs
│  ✦ Fuzzing             ┘      — proves stability under adversarial input
│                               (complementary at this level, not ranked)
│  ✦ Code Coverage              — proves tests reached the code
│
LOWEST CONFIDENCE (easiest, most common)

─────────────────────────────────────────────────────────────────────────
Note: Static Analysis operates in a separate dimension entirely.
It runs BEFORE tests, on code at rest, and catches structural/syntactic
defects that dynamic techniques cannot see. It neither ranks above nor
below this stack — it is complementary to the entire stack.
─────────────────────────────────────────────────────────────────────────
```

#### Summary Table

| Question | Answered by |
|---|---|
| Did tests *reach* the code? | **Code Coverage** |
| Do tests *catch* bugs in the code? | **PIT Mutation Score** |
| Is the app *stable* under adversarial input? | **Fuzzing** |
| Do tests cover *all input space*? | **Property-Based Testing** |
| Does code match *requirements* not written as tests? | **Specification-Based Testing** |
| Is the code *provably* correct? | **Formal Verification** |
| Are there structural/syntactic flaws before runtime? | **Static Analysis** |

### Bottom Line

> Coverage and PIT are **necessary but not sufficient**. Each tier above catches a category of defects the tiers below it *cannot see*. A mature quality strategy layers multiple techniques rather than relying on any single metric.

---

## Q4: What is dynamic analysis?

---

> [!NOTE]
> **Q1 introduced the Static vs Dynamic dichotomy** at a high level. This section deepens the dynamic side specifically — explaining what dynamic analysis looks for, what forms it takes, and how its two security-testing sub-types (SAST and DAST) relate to each other.

### Short Answer

> **Dynamic analysis** is the process of testing and evaluating an application **while it is actively running**.

The key contrast:

| | | Analogy |
|---|---|---|
| **Static Analysis** | Examines source code *without* running it | Proofreading a recipe to see if you missed an ingredient |
| **Dynamic Analysis** | Executes code and observes behaviour in real-time | Tasting the soup while it's cooking to see if it needs more salt |

---

### Detailed Answer

#### What Does Dynamic Analysis Look For?

Because it watches the program "in motion," dynamic analysis is uniquely suited to catch:

| Category | What it detects |
|---|---|
| **Memory Issues** | Memory leaks (RAM eaten and not released), buffer overflows |
| **Concurrency/Threading Bugs** | Race conditions, deadlocks — only surface under specific execution timings |
| **Performance Bottlenecks** | Which specific query or function slows the app under real user load |
| **Security Vulnerabilities** | SQL injection, XSS, authentication bypasses when real malicious data is fed in |
| **Logic Errors** | Whether the app produces correct output for specific inputs |

#### Common Types of Dynamic Analysis

You likely already use dynamic analysis without calling it that. The table below is an overview; **Q5 provides a deep-dive into Fuzzing, Profiling, and DAST specifically**.

| Type | Description | Tools |
|---|---|---|
| **Unit / Integration / E2E Testing** | Run test scripts to execute code and verify output | JUnit, Jest, Playwright |
| **Performance / Load Testing** | Measure how the running system handles traffic | k6, JMeter, Gatling |
| **Fuzzing (Fuzz Testing)** | Throw random/invalid data at a running program to find crashes | Jazzer, AFL |
| **Profiling** | Monitor CPU and memory usage as the app runs | JProfiler, VisualVM |
| **DAST** | Automated security tools that attack your running web app | OWASP ZAP, Burp Suite |

---

#### SAST vs DAST — Security Testing Counterparts

Q1 introduced **SAST (Static Application Security Testing)**. Q5 explains **DAST (Dynamic Application Security Testing)**. They are natural counterparts and must be used together for complete security coverage.

| Dimension | SAST | DAST |
|---|---|---|
| **Code executed?** | ❌ No | ✅ Yes |
| **Source code access?** | ✅ Required | ❌ Not required |
| **When in pipeline?** | Build time (shift-left) | Runtime (after deployment) |
| **Perspective** | Inside-out (code reviewer) | Outside-in (attacker) |
| **Finds** | Insecure coding patterns, hardcoded secrets, injection-prone code paths | Exploitable runtime vulnerabilities, misconfigurations, authentication bypasses |
| **False positives** | Higher (flags patterns, not confirmed exploits) | Lower (confirms actual exploitability) |
| **Tools** | SonarQube SAST, Checkmarx, Semgrep | OWASP ZAP, Burp Suite, Veracode DAST |

> **Bottom line for security:** SAST finds *how code is written insecurely*. DAST finds *how the running application can actually be attacked*. Neither alone is sufficient — a mature pipeline uses both.

### Bottom Line

> Dynamic analysis is the umbrella for any technique that requires **running the code** to find defects. It complements static analysis and catches failures that only appear under real execution conditions.

---

## Q5: What are the primary applications of Fuzzing, Profiling, and DAST in dynamic analysis?

---

### Short Answer

All three are **types of dynamic analysis** — they require the application to be running — but each hunts a completely different category of defect.

| Technique | Goal | Finds |
|---|---|---|
| **Fuzzing** | Make the app crash with garbage input | Buffer overflows, unhandled exceptions, memory leaks |
| **Profiling** | Find where resources are wasted | CPU bottlenecks, memory bloat, N+1 query problems |
| **DAST** | Hack the app from the outside | SQL injection, XSS, security misconfigurations |

---

### Detailed Answer

#### 1. Fuzzing (Fuzz Testing)

**The Goal:** Make the application crash by throwing garbage at it.

**How it works:**
A fuzzer is an automated program that continuously bombards your running application with invalid, unexpected, massive, or completely random data. Instead of testing "happy paths" (like entering a normal email address), a fuzzer will enter 10,000 random emojis, negative numbers where positive ones are expected, or malformed JSON files.

**What it finds:**

| Issue | Example |
|---|---|
| **Buffer overflows** | Input field expects 50 chars, gets 50,000 — does it overwrite memory and crash? |
| **Unhandled Exceptions** | Does the server crash when it receives a corrupted image file? |
| **Memory leaks** | Does the system slowly bleed memory when repeatedly handling broken requests? |

> 🎯 **Analogy:** Handing a toddler a complex remote control and letting them smash buttons at random to see if the TV breaks.

**Tools (Java):** Jazzer, AFL

---

#### 2. Profiling

**The Goal:** Understand exactly where your application is spending its time and resources.

**How it works:**
You attach a profiler to your running application. As it executes, the profiler records in microscopic detail: how many milliseconds were spent inside function A, how many times function B was called, and how much RAM function C consumed.

**What it finds:**

| Issue | Example |
|---|---|
| **CPU Bottlenecks** | A specific loop is consuming 90% of processing time |
| **Memory Bloat** | Specific objects are eating all available server RAM |
| **Inefficiencies** | An API call executes 50 database queries when it should execute 1 (N+1 problem) |

> 🎯 **Analogy:** Putting a heart-rate monitor and GPS tracker on a marathon runner to see exactly when their heart rate spikes and at which mile they slow down.

**Tools (Java):** JProfiler, VisualVM, async-profiler

---

#### 3. DAST (Dynamic Application Security Testing)

**The Goal:** Hack your application from the outside, exactly like a real attacker would.

> [!CAUTION]
> Running DAST tools against any system you do not **own** or have **explicit written authorisation** to test constitutes unauthorised computer access. This is illegal under the Computer Fraud and Abuse Act (US), the Computer Misuse Act (UK), IT Act 2000 (India), and equivalent legislation in most jurisdictions. Always obtain written permission from the system owner before running DAST. In CI/CD pipelines, DAST must target dedicated test environments only — never production without explicit, documented approval.

**How it works:**
A DAST tool communicates with your running web application over HTTP/HTTPS — **without access to your source code**. It crawls your website, finds all inputs, forms, and API endpoints, and automatically attempts thousands of known hacking techniques against them.

**What it finds:**

| Vulnerability | Example attack attempted |
|---|---|
| **SQL Injection** | Puts `' OR 1=1 --` into your login box to bypass authentication |
| **Cross-Site Scripting (XSS)** | Injects malicious JavaScript into comment sections to see if browsers execute it |
| **Misconfigurations** | Checks if the server exposes sensitive files, uses weak encryption, or is missing security headers |

> 🎯 **Analogy:** Hiring a security guard to walk around the outside of your house, rattling doorknobs, prying at windows, and trying to guess your garage code — without ever going inside.

**Tools:** OWASP ZAP, Burp Suite, Veracode DAST

---

### Bottom Line

> These three techniques cover orthogonal risk dimensions:
> - **Fuzzing** → *stability* under unexpected inputs
> - **Profiling** → *efficiency* under real load
> - **DAST** → *security* against external attackers (with mandatory authorisation)
>
> A production-grade quality strategy should employ all three.

---

## Q6: What is the difference between Java Code Coverage and PIT Mutation Score?

---

### Short Answer

| | Code Coverage | PIT Mutation Score |
|---|---|---|
| **Measures** | Whether tests *executed* the code | Whether tests *verify* the code correctly |
| **Key question** | "Was this line/branch reached?" | "Would my tests catch a bug here?" |
| **Tool (Java)** | JaCoCo, Cobertura | PITest |
| **Can be gamed?** | ✅ Yes — test without assertions | ❌ Much harder to game |
| **Speed** | ⚡ Fast | 🐢 Slow |

---

### Detailed Answer

#### 🧱 Foundation: What Problem Are They Solving?

Both metrics answer one core question:

> **"How good are my tests?"**

But they measure *fundamentally different things*.

---

#### 1. Code Coverage — *"Did my tests visit the code?"*

##### What it measures

Code coverage tracks **which lines/branches of production code were executed** during test runs.

##### Types (simple → thorough)

| Type | What it checks | Example passes if… |
|---|---|---|
| **Line coverage** | Was this line run? | `int x = a + b;` was reached |
| **Branch coverage** | Was each if/else path taken? | Both `true` and `false` of `if (x > 0)` were executed |
| **Condition coverage** | Was each boolean sub-expression tested? | `a && b` tested with `a=false` and `a=true,b=false` |
| **Method/Class coverage** | Was this method/class called? | `calculateTax()` was invoked |

##### Coverage Type Hierarchy

These types form a **strict subset hierarchy** — satisfying a stronger criterion automatically satisfies all weaker ones:

```
Statement ⊂ Branch ⊂ Condition ⊂ Path
(weakest)                        (strongest — often infeasible)

Guarantees flow downward:
  100% Path      ⟹ 100% Condition
  100% Condition ⟹ 100% Branch
  100% Branch    ⟹ 100% Statement
  (but NOT vice versa in any direction)
```

**Practical implication:** JaCoCo targets Branch coverage. If your JaCoCo report shows 100% branch coverage, you automatically have 100% statement coverage — but you may still have condition-level and path-level gaps.

##### Common Java tools

- **JaCoCo** (most popular, measures line + branch), Cobertura, OpenClover

##### The critical flaw

```java
public int add(int a, int b) {
    return a + b;
}

@Test
void test_add() {
    add(2, 3); // ← no assertion! No oracle!
}
```

- ☑️ **100% line coverage** — the line was executed
- ❌ **The test proves nothing** — no oracle was applied; the result was never verified

> **Coverage tells you tests *ran* the code. It says nothing about whether they *check* the code correctly.**

---

#### 2. PIT Mutation Score — *"Do my tests actually catch bugs?"*

##### What it measures

PIT (PITest) uses **mutation testing**: it automatically introduces small, deliberate defects ("mutants") into your code and checks whether your tests **detect and kill** those mutants.

##### How it works — step by step

```
Original code:          Mutant (PIT auto-generates):
return a + b;    →      return a - b;   ← arithmetic mutation
if (x > 0)       →      if (x >= 0)    ← boundary mutation
return true;     →      return false;  ← return value mutation
```

For each mutant:
- PIT runs your full test suite
- **Mutant killed** → at least one test **failed** ✅ (good — your test caught the defect)
- **Mutant survived** → all tests **passed** ❌ (bad — your test missed the defect)

##### The score formula

```
Mutation Score = (Killed Mutants / Total Mutants) × 100%
```

##### Worked example

```java
// Code under test
public boolean isAdult(int age) {
    return age >= 18;
}

// Test
@Test
void test_isAdult() {
    assertTrue(isAdult(20));  // passes ✓
}
```

PIT generates mutant: `return age > 18`

- `isAdult(20)` still returns `true` → test still passes → **mutant survives** ⚠️
- Your test never checked the boundary case `isAdult(18)` → **gap exposed**
- Fix: add `assertTrue(isAdult(18))` → mutant now killed ✅

---

#### 3. Side-by-Side Comparison

| Dimension | Code Coverage | PIT Mutation Score |
|---|---|---|
| **What it measures** | Code *execution* | Test *effectiveness* |
| **Question answered** | "Was this code reached?" | "Would tests catch a defect here?" |
| **Can be gamed?** | ✅ Yes — test without assertions | ❌ Much harder to game |
| **Speed** | ⚡ Fast | 🐢 Slow (runs tests N×mutants times) |
| **False confidence risk** | High | Low |
| **Beginner-friendly?** | ✅ Yes | ⚠️ More setup/interpretation needed |
| **Tool (Java)** | JaCoCo | PITest |
| **Output granularity** | Line/branch % | Per-mutant survived/killed report |

---

#### 4. The Analogy

> Imagine testing a smoke alarm:
>
> **Code coverage** = "I pressed the test button and it beeped" ✓
> **Mutation score** = "I actually filled the room with smoke — did it go off?" ✓✓

Coverage confirms the button works. Mutation testing confirms the alarm actually detects danger.

---

#### 5. The Hierarchy of Trust

```
High Coverage + High Mutation Score  ← trustworthy test suite       ✅
High Coverage + Low Mutation Score   ← dangerous false confidence   ⚠️
Low Coverage  + Any Mutation Score   ← untested code exists, fix coverage first
```

---

#### 6. Practical Guidance

##### When to use each

- **Always use coverage** as a baseline gate (e.g., fail CI below 80%)
- **Use PIT** for critical/complex business logic (financial calculations, auth, state machines)
- **Don't require 100% mutation score** — some mutants are inherently unkillable (see below)

##### Equivalent Mutants — Why 100% Score Is Impossible

An **equivalent mutant** is a mutation that produces *identical behaviour* to the original for *all possible inputs*. No test can ever distinguish the original from an equivalent mutant, so it can never be killed.

```java
// Original:
public boolean isPositive(int x) {
    return x > 0;
}

// PIT-generated mutant: x > 0  changed to  x >= 1
public boolean isPositive(int x) {
    return x >= 1;   // semantically identical for all integer inputs
}
// For integers, there is no value between 0 and 1.
// x > 0 and x >= 1 are mathematically equivalent on ℤ.
// This mutant can NEVER be killed. It is an equivalent mutant.
```

**Formal note:** Detecting equivalent mutants in the general case is **undecidable** (a consequence of Rice's Theorem). PITest may report these as "survived," artificially depressing your mutation score. This is why a mutation score ceiling of 80–90% is generally considered excellent in practice.

##### CI pipeline integration (Maven)

```xml
<!-- pom.xml — JaCoCo + PITest -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <!-- provides line/branch coverage -->
</plugin>

<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <!-- provides mutation score -->
</plugin>
```

```bash
# Run coverage report
mvn test jacoco:report

# Run mutation coverage report
mvn org.pitest:pitest-maven:mutationCoverage
```

---

#### 7. Key Takeaways (Pareto 80/20)

| # | Insight |
|---|---|
| 1 | Coverage = *did tests run the code*; PIT = *do tests verify the code* |
| 2 | 100% coverage with 0% assertions = worthless tests (no oracle applied) |
| 3 | Mutation testing is expensive — apply it surgically to critical paths |
| 4 | Use **both**: coverage catches missing tests, PIT catches weak assertions |
| 5 | A surviving mutant is a **missing test case** — it tells you *exactly* what to write next |
| 6 | 100% mutation score is theoretically impossible due to equivalent mutants — target 80–90% |

---

### Bottom Line

> Code coverage is a *necessary but insufficient* condition for test quality.
> PIT mutation score is a *stronger signal* — it measures whether your tests can distinguish correct code from defective code.

---

## Q7: How do source code structures directly influence Code Coverage and Mutation Score?

---

### Short Answer

> **Source code is the *subject* being measured. Tests are the *instrument*. Coverage and PIT score are the *readings*.**

```
Source Code ──► [Test Suite] ──► Coverage Report
                               ──► PIT Mutation Report
```

Both metrics are *properties of the relationship* between your source code and your tests — neither exists in isolation.

---

### Detailed Answer

#### 1. Source Code → Code Coverage

Coverage is a **map of your source code's execution paths** drawn by tests.

```java
// Source code — 3 branches exist
public String classify(int n) {
    if (n > 0) return "positive";   // branch A
    if (n < 0) return "negative";   // branch B
    return "zero";                   // branch C
}
```

```
Tests run:     classify(5)   → branch A executed ✅
               classify(-1)  → branch B executed ✅
               (zero never tested)

Branch Coverage = 2/3 = 66%   ← a property of the source code + tests together
```

**What source code structure affects coverage:**

| Source Code Element | Coverage Impact |
|---|---|
| Dead code (unreachable) | Can never reach 100% line coverage |
| Complex nested conditions | Requires many tests to achieve branch coverage |
| Long methods | Single missed branch lowers % more significantly |
| Exception handling paths | Often forgotten, reducing coverage |

#### 2. Source Code → PIT Mutation Score

PIT **operates directly on the source code's bytecode** — it clones, corrupts, and re-tests it.

```
Your source code
       │
       ▼
PIT reads bytecode
       │
       ├─► Mutant 1: change `>=` to `>`   → run tests → killed/survived
       ├─► Mutant 2: change `+` to `-`    → run tests → killed/survived
       └─► Mutant 3: negate return value  → run tests → killed/survived
```

**Source code properties that directly influence mutation score:**

| Source Code Property | Effect on PIT |
|---|---|
| **Operators used** (`+`, `>`, `&&`) | More operators = more mutants generated |
| **Boundary conditions** (`>=`, `<=`) | Classic survival hotspot if tests lack edge cases |
| **Return values** (`true/false`, `null`) | PIT always mutates these — weak tests expose gaps |
| **Conditionals** | Each `if` generates multiple mutants |
| **Complexity (cyclomatic)** | Higher complexity = exponentially more mutants |

#### 3. The Unified Picture

```
                        ┌─────────────────────────────┐
                        │        SOURCE CODE           │
                        │  (the system under test)     │
                        └──────────┬──────────────────┘
                                   │
               ┌───────────────────┼────────────────────┐
               ▼                                        ▼
   ┌─────────────────────┐              ┌───────────────────────────┐
   │    TESTS EXECUTE IT │              │  PIT MUTATES IT, THEN     │
   │    (JaCoCo watches) │              │  TESTS EXECUTE EACH CLONE │
   └──────────┬──────────┘              └──────────────┬────────────┘
              │                                        │
              ▼                                        ▼
   ┌─────────────────────┐              ┌───────────────────────────┐
   │  COVERAGE REPORT    │              │  MUTATION REPORT          │
   │  "Which lines of    │              │  "Which mutations did      │
   │   source were run?" │              │   tests fail to detect?"  │
   └─────────────────────┘              └───────────────────────────┘
```

#### 4. Key Insight: Same Source Code, Different Lenses

Given *identical* source code and *identical* tests:

| Scenario | Coverage says | PIT says |
|---|---|---|
| Test calls method, no assertion | ✅ 100% line covered | ❌ All mutants survive |
| Test checks happy path only | ✅ High branch coverage | ⚠️ Boundary mutants survive |
| Test covers all branches + asserts | ✅ High coverage | ✅ High mutation score |

- **Coverage** reads the *execution trace* of source code.
- **PIT** reads the *semantic meaning* of source code's logic.

A surviving PIT mutant **pinpoints the exact line in your source code** where tests are semantically blind:

```java
// Source code line 42:
return price * quantity * (1 - discount);
// PIT mutated * to +, tests didn't catch it →
// your test never verified the mathematical relationship in this expression
```

Coverage would show this line as ✅ covered — PIT reveals it's ❌ untrusted.

### Bottom Line

| | Role of Source Code |
|---|---|
| **Coverage** | Source code is the *map* — coverage colors it in as tests traverse paths |
| **PIT** | Source code is the *specimen* — PIT dissects its logic by introducing controlled damage |

Both metrics are **meaningless without source code**. Source code is the shared substrate — coverage measures its *reachability*, PIT measures its *correctness under test*.

---

## Q8: What are the different engineering workflows for applying source code, tests, and quality metrics?

---

### Short Answer

Yes — that describes the **Classic (Code-First)** approach, and it works. But it is not the only valid order, it carries a known risk, and it does not address the most common real-world scenario: **inheriting a codebase with no tests**.

---

### Detailed Answer

#### The Four Workflows Side by Side

```
CLASSIC               TEST-FIRST (TDD)      ITERATIVE             LEGACY (No Tests)
(Code-First)                                (Most Real Teams)     (Characterisation)
──────────────        ────────────────      ─────────────────     ──────────────────
1. Write source       1. Write a failing    1. Write test         1. Run coverage
2. Write tests           test              2. Write source           (expect ~0%)
3. Run coverage       2. Write just        3. Run coverage +     2. Write characterisation
4. Run PIT               enough source        PIT                   tests (capture current
5. Fill gaps             to pass it        4. Improve tests          behaviour, even if buggy)
                      3. Refactor          5. Repeat             3. Run PIT to validate
                      4. Repeat                                      characterisation tests
                                                                 4. Refactor one module
                                                                    at a time, guarded
                                                                    by existing tests
                                                                 5. Raise coverage gate
                                                                    incrementally
```

> **Characterisation Testing** (Michael Feathers, *Working Effectively with Legacy Code*): Tests written to document *current* behaviour — not necessarily *correct* behaviour. Their purpose is to prevent regression while you gain understanding of the codebase. They are gradually replaced by intention-revealing tests as understanding grows.

---

#### Why Order Matters: The Hidden Risk of the Classic Approach

When you write source code *first*, you unconsciously write tests that **match what the code does** — not what it *should* do.

```
Source code has a defect:   return age > 18;   (should be >=)

You write test after:       assertTrue(isAdult(20));  ← passes even with the defect

Coverage: ✅ 100%     PIT: ❌ boundary mutant survives
```

> **You test the implementation, not the requirement.**

---

#### Where Coverage and PIT Fit — Regardless of Order

```
                    ┌─────────────────────────────────────────┐
                    │           FEEDBACK LOOP                  │
                    │                                          │
  Source Code ◄─────┤  Coverage reveals: untouched paths      │
  Tests        ◄────┤  PIT reveals:      weak assertions       │
                    └─────────────────────────────────────────┘
```

They are **diagnostic tools**, not one-time checkboxes. Run them repeatedly as code evolves.

---

#### The Recommended Pragmatic Workflow

```
1. WRITE TEST (for a requirement)
        ↓
2. WRITE SOURCE CODE (to satisfy it)
        ↓
3. RUN COVERAGE → find untested paths → add tests
        ↓
4. RUN PIT → find weak assertions → strengthen tests
        ↓
5. REFACTOR source code safely (tests now guard you)
        ↓
        └──── repeat for next requirement ────┘
```

---

#### Role of Each Tool in the Workflow

| Tool | When to run | What it tells you to do |
|---|---|---|
| **Coverage** | After writing tests | "These paths have no tests yet — add them" |
| **PIT** | After coverage is acceptable | "These tests exist but won't catch defects — strengthen assertions" |

### Bottom Line

> The *ideal* is: **tests before (or alongside) source code**, with Coverage and PIT as **continuous feedback** — not a final checkpoint.
>
> For **legacy codebases**, start with characterisation tests to establish a safety net before refactoring.
>
> Coverage and PIT are most powerful when treated as a **loop**, not a finish line.

---

## Related Topics

- [PITest official documentation](https://pitest.org)
- [JaCoCo official documentation](https://www.jacoco.org/jacoco/)
- [jqwik — Property-Based Testing for Java](https://jqwik.net)
- [OWASP ZAP — DAST tool](https://www.zaproxy.org)
- Michael Feathers — *Working Effectively with Legacy Code* (Characterisation Testing)
- Mike Cohn — *Succeeding with Agile* (Testing Pyramid)
- ISTQB Foundation Level Syllabus — test techniques and test levels
- IEEE 1044 — Standard Classification for Software Anomalies (Error/Defect/Failure)
- DO-178C — Software Considerations in Airborne Systems (MC/DC coverage)
- Test-Driven Development (TDD) — Kent Beck
- Assertion best practices in JUnit 5
- MC/DC coverage — avionics standard DO-178C
