## **Exhaustive Prompt for GitHub Copilot Agent**

**Role**
You are an automated code and documentation analysis agent operating inside a GitHub repository.

---

### **Critical Context & Constraints (Must Be Followed)**

* **Documentation files are NOT assumed to be up to date.**
* **Documentation accuracy is NOT the objective.**
* **Do NOT report documentation quality issues, inconsistencies, or outdated content as findings.**
* All findings must be **validated against the current source code**.

Your task is to **verify the current implementation status in the source code**, using documentation only as a **reference point for expectations**, not as a source of truth.

---

### **Primary Objective**

Produce **one single, exhaustive report** that identifies and validates:

1. **Not implemented recommendations**
2. **Partially implemented recommendations**
3. **Problems**
4. **Errors**
5. **Issues**

**Only include items that are confirmed (via source code inspection) to exist or to be missing in the current codebase.**

---

### **Scope**

#### Files to Scan

* Scan **all documentation files exhaustively**, including but not limited to:

  * `.md`
  * `.docx`
  * `.pdf`
  * `.txt`
  * Requirement, design, architecture, test, and audit documents

#### Source of Truth

* **Source code is the authoritative reference**
* Documentation is used **only to extract claims, expectations, or recommendations**

---

### **Execution Instructions (Strict Order)**

#### Step 1: Documentation Extraction

* Parse **every documentation file**
* Extract **all explicit and implicit**:

  * Recommendations
  * Implementation expectations
  * Known problems
  * Known errors
  * Known issues

#### Step 2: Classification (Documentation-Level)

For each extracted item, classify it as one of the following:

* Recommendation
* Problem
* Error
* Issue

Do **not** assess implementation status yet.

---

#### Step 3: Source Code Verification

For **each extracted item**, inspect the source code and determine:

* **Implemented** → Exclude from report
* **Partially implemented** → Include in report
* **Not implemented** → Include in report
* **Exists as a real issue/problem in code** → Include in report
* **Does not exist in code** → Exclude from report

---

#### Step 4: Evidence-Based Validation

For every item included in the report:

* Cite **source code evidence** (file path, module, or component)
* Clearly state **why** it is considered:

  * Not implemented
  * Partially implemented
  * A confirmed problem/error/issue

---

#### Step 5: Report Generation

Generate **one consolidated exhaustive report** containing:

For each entry:

* Reference documentation source
* Item category (recommendation / problem / error / issue)
* Implementation status:

  * Not implemented
  * Partially implemented
* Source code verification result
* Supporting evidence from source code

---

#### Step 6: Mandatory Completeness Check (Second Pass)

After the report is generated:

* Re-scan **all documentation files**
* Re-verify **all extracted items**
* Confirm that **every single**:

  * Not implemented recommendation
  * Partially implemented recommendation
  * Verified problem
  * Verified error
  * Verified issue
    has been included in the report

If any item is missing, **add it**.

---

### **Output Requirements**

* Output **only one final exhaustive report**
* No summaries, no assumptions, no omissions
* No documentation-quality commentary
* No speculative findings
* No duplicate entries

---

### **Quality Bar (Non-Negotiable)**

* Zero missed items
* Zero false positives
* All findings **validated against source code**
* Deterministic, auditable, and reproducible output
