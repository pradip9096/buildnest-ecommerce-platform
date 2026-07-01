# Q&A: How to Check the Quality of a Question

> **Category:** Pedagogy & Knowledge Management
> **Tags:** `question-quality`, `bloom-taxonomy`, `pedagogy`, `learning-design`, `knowledge-management`, `critical-thinking`
> **Level:** Beginner → Intermediate
> **Last Updated:** 2026-07-01

---

## Table of Contents

- [Question](#question)
- [Why It Matters](#why-it-matters)
- [The 6 Dimensions of Question Quality](#the-6-dimensions-of-question-quality)
- [Framework 1 — Bloom's Cognitive Level Check](#framework-1--blooms-cognitive-level-check)
- [Framework 2 — The PICO Precision Test](#framework-2--the-pico-precision-test)
- [Framework 3 — The 5 Anti-Patterns (Question Smells)](#framework-3--the-5-anti-patterns-question-smells)
- [Framework 4 — The Essential Question Test](#framework-4--the-essential-question-test-wiggins--mctighe)
- [Practical Checklist — Quick Scan](#practical-checklist--quick-scan)
- [Summary](#summary)
- [Bottom Line](#bottom-line)
- [Related Topics](#related-topics)

---

## Question

**How do you check the quality of a question?**

---

## Why It Matters

> A poor question produces a poor answer — even from an expert. The question is the *specification*; the answer is the *implementation*. Garbage in, garbage out.

Question quality is a prerequisite for:
- Effective knowledge transfer and learning
- Productive code or design reviews
- Useful AI-assisted outputs
- Sound engineering requirements elicitation
- Defensible research and analysis

---

## The 6 Dimensions of Question Quality

Think of these as **quality attributes** — a question should satisfy all six:

| Dimension | Test to apply | Failure symptom |
|---|---|---|
| **1. Clarity** | Can it be understood in exactly one way? | Multiple valid interpretations exist |
| **2. Precision** | Is the scope well-bounded? | Too vague ("Tell me everything about X") or too narrow to matter |
| **3. Answerability** | Can it actually be answered? | Unanswerable, circular, or based on false premises |
| **4. Singularity** | Does it ask exactly one thing? | Multiple questions bundled into one sentence |
| **5. Cognitive Level** | Does it target the right depth? | Too shallow (rote recall) or too advanced for the prerequisites |
| **6. Context Sufficiency** | Does it carry enough background to constrain the answer? | Answer depends entirely on unstated assumptions |

---

## Framework 1 — Bloom's Cognitive Level Check

Ask: *"What mental operation does this question require?"*

| Level | Trigger words | Example question |
|---|---|---|
| **Remember** | Define, List, Name | "What is code coverage?" |
| **Understand** | Explain, Describe, Summarise | "Why does high coverage not guarantee good tests?" |
| **Apply** | Use, Demonstrate, Calculate | "What is the mutation score if 7 of 10 mutants are killed?" |
| **Analyze** | Compare, Differentiate, Examine | "What is the difference between branch and condition coverage?" |
| **Evaluate** | Judge, Justify, Critique | "Should we require 100% mutation score in CI?" |
| **Create** | Design, Formulate, Construct | "Design a testing strategy for a payment service." |

> **Red flag:** A question that *sounds* analytical but only requires recall. E.g., "What are the differences between X and Y?" is **Remember** if the learner just memorised a table, but **Analyze** if they must derive the comparison themselves.

---

## Framework 2 — The PICO Precision Test

Originally from medical research — universally applicable to any knowledge domain.

| Element | Meaning | Applied to testing questions |
|---|---|---|
| **P**opulation | Who/what is the subject? | "For a Java REST API…" |
| **I**ntervention | What is being evaluated? | "…using JaCoCo branch coverage…" |
| **C**omparison | Compared to what? | "…vs. PITest mutation score…" |
| **O**utcome | What are we measuring? | "…which better predicts defect escape rate?" |

**Weak question:**
> *"Is code coverage good?"*

**PICO-refined question:**
> *"For a Java Spring Boot REST API with 80% JaCoCo branch coverage, does a mutation score below 60% predict a significantly higher post-release defect rate than a score above 80%?"*

---

## Framework 3 — The 5 Anti-Patterns (Question Smells)

| Anti-pattern | Example | Problem |
|---|---|---|
| **Leading question** | "Isn't TDD always better?" | Presupposes the answer; closes inquiry |
| **Loaded question** | "Why does PIT slow down bad CI pipelines?" | Embeds an unverified assumption |
| **Compound question** | "What is coverage and how does PIT work and when should I use TDD?" | Cannot be answered coherently as one unit |
| **False dichotomy** | "Should I use coverage OR mutation testing?" | Excludes the valid answer: both |
| **Unanswerable question** | "What is the perfect mutation score?" | No objective answer exists; needs a constraint |

---

## Framework 4 — The Essential Question Test (Wiggins & McTighe)

A *great* pedagogical question passes all four checks:

```
1. OPEN-ENDED    → Has no single, obvious, correct answer
2. GENERATIVE   → Sparks further inquiry and sub-questions
3. TRANSFERABLE → Applies beyond the immediate topic
4. CENTRAL      → Gets at the core idea, not a peripheral detail
```

**Example — does "What is code coverage?" pass?**

| Check | Result | Reason |
|---|---|---|
| Open-ended | ❌ No | Has one correct definition |
| Generative | ❌ Weak | Doesn't naturally lead to deeper questions |
| Transferable | ⚠️ Partial | Concept applies broadly, but phrasing is narrow |
| Central | ✅ Yes | Gets at a core QA concern |

**Improved essential question:**
> *"When is a test suite good enough?"*

| Check | Result |
|---|---|
| Open-ended | ✅ Debatable — no single right answer |
| Generative | ✅ Leads to coverage, mutation, property-based, formal verification… |
| Transferable | ✅ Applies to any project, language, or domain |
| Central | ✅ Core concern of all quality assurance work |

---

## Practical Checklist — Quick Scan

Run this before asking or writing any question:

```
☐ 1. Can I state it in one sentence?
☐ 2. Is there exactly one question mark?
☐ 3. Does it assume the reader knows what I know? (context gap)
☐ 4. What cognitive level does it target — is that intentional?
☐ 5. What would a wrong answer look like? (if you can't define it, the question is unclear)
☐ 6. Does the question contain its own answer? (leading)
☐ 7. Is the scope narrow enough to answer in the available time/space?
☐ 8. Are all terms in the question defined or commonly understood?
```

---

## Summary

| Quality Criterion | Quick test |
|---|---|
| **Clarity** | One interpretation only |
| **Precision** | Bounded scope — not "everything about X" |
| **Singularity** | One question mark |
| **Cognitive fit** | Targets the right Bloom's level deliberately |
| **Context sufficiency** | No critical unstated assumptions |
| **Answerability** | A wrong answer can be defined |
| **Non-leading** | Doesn't embed the expected answer |
| **Essential** | Opens inquiry, doesn't just retrieve a fact |

---

## Bottom Line

> A high-quality question is one where the *asker* has done sufficient thinking that the *answerer* can do precise, productive thinking. The question is the contract between them.

---

## Related Topics

- Bloom's Revised Taxonomy (Anderson & Krathwohl, 2001)
- Understanding by Design — Wiggins & McTighe
- PICO Framework — evidence-based research methodology
- Socratic questioning techniques
- Requirements elicitation in software engineering
- Prompt engineering for AI-assisted development
