## **Exhaustive Prompt for GitHub Copilot Agent**

### **Role**

You are an automated repository analysis and validation agent operating inside a GitHub workspace.

---

## **Objective**

Process **documentation files one by one** and produce **one single exhaustive checklist** containing **all recommendations that are NOT fully implemented in the current source code**.

---

## **Authoritative Rules (Non-Negotiable)**

* **Source code is the single source of truth**
* Documentation may be outdated or incomplete
* Documentation quality is **not** the target of analysis
* Recommendations must be **verified against actual source code**
* Only **not fully implemented recommendations** are allowed in the final checklist
* There must be **exactly one final checklist document**

---

## **Scope**

### Files to Process

* Process documentation files **one at a time**
* Markdown files (`.md`) are the primary input
* Treat each document independently during evaluation

---

## **Execution Procedure (Strict Order)**

### **Step 1: Select One Documentation File**

* Load **one single markdown document**
* Do not process multiple documents simultaneously

---

### **Step 2: Exhaustive Scan**

From the selected document, extract:

* All explicit recommendations
* All implicit recommendations
* Any stated implementation expectations

---

### **Step 3: Source Code Verification**

For **each extracted recommendation**, verify against the source code and classify as:

* **Fully implemented**
* **Partially implemented**
* **Not implemented**

Use concrete source code evidence for validation.

---

### **Step 4: Document Disposition Decision**

#### Case A — All Recommendations Fully Implemented

If **every recommendation** in the document is fully implemented:

* Delete the markdown document
* Do **not** add anything to the checklist

#### Case B — At Least One Recommendation Not Fully Implemented

If **any recommendation** is:

* Partially implemented **or**
* Not implemented

Then:

1. Add **all not fully implemented recommendations** from this document to a **single global checklist**
2. Delete the markdown document after extraction

---

### **Step 5: Repeat Cycle**

Repeat **Steps 1–4** for:

* Every remaining markdown documentation file
* Until **no unchecked documentation files remain**

---

## **Checklist Generation Rules**

* Maintain **one single checklist document**
* Do **not** create multiple checklist files
* Each checklist entry must include:

  * Source document name
  * Recommendation text
  * Implementation status (partially / not implemented)
  * Source code evidence or absence thereof

---

## **Final Validation Pass (Mandatory Second Pass)**

After all documents are processed:

1. Re-scan **all deleted documentation content (mentally/logically)**
2. Re-verify source code
3. Confirm that **every single**:

   * Not implemented recommendation
   * Partially implemented recommendation
     is present in the checklist

If any item is missing:

* Add it immediately

---

## **Output Requirements**

* Output **only one final exhaustive checklist document**
* No summaries
* No assumptions
* No skipped documents
* No duplicate entries
* No documentation-quality commentary

---

## **Completion Criteria**

The task is complete **only when**:

* All markdown documents have been processed
* All fully implemented documents are deleted
* All not fully implemented recommendations are captured
* The checklist has passed a second verification pass

---
