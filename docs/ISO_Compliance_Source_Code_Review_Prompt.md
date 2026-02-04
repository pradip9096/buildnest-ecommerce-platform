## Enhanced Prompt (ISO/IEC/IEEE-Enforced)

**Task:**
Scan the entire source codebase and generate an **EXHAUSTIVE RECOMMENDATION REPORT** strictly aligned with international ISO/IEC/IEEE standards.

---

### **Prompt**

> Scan the complete source code repository (including backend, infrastructure-as-code, scripts, and configuration files).
>
> Perform a **systematic, evidence-based analysis** and generate an **Exhaustive Recommendation Report** by enforcing the following international standards:
>
> **Standards to be applied**
>
> * **ISO/IEC/IEEE 29148** – Requirements Engineering (traceability, completeness, consistency, and verifiability of requirements vs implementation)
> * **ISO/IEC/IEEE 29119 (all relevant parts)** – Software Testing (testability gaps, missing test levels, test coverage risks)
> * **ISO/IEC 25010** – Software Product Quality Model (functional suitability, performance efficiency, reliability, security, maintainability, portability)
> * **ISO/IEC 15939** – Software Measurement (missing metrics, weak indicators, non-measurable quality attributes)
> * **ISO/IEC 27001 / 27002** – Information Security Controls (secure coding, secrets management, access control, logging, auditability)
> * **ISO/IEC 12207** – Software Life Cycle Processes (process nonconformance reflected in code structure)
> * **ISO/IEC 15288** – System Life Cycle alignment (system boundaries, interfaces, dependencies)
> * **ISO/IEC 330xx (SPICE)** – Process capability weaknesses observable from the codebase
>
> ---
>
> ### **Analysis Requirements**
>
> * Do **not assume correctness**; rely only on **observable evidence in the code**
> * Identify:
>
>   * Violations
>   * Partial compliance
>   * Missing artifacts implied by the code (tests, configs, docs, controls)
> * Map each finding to:
>
>   * Relevant **standard clause**
>   * **Risk level** (Critical / High / Medium / Low)
>   * **Impact** (Security, Quality, Maintainability, Compliance, Operability)
>
> ---
>
> ### **Recommendation Report MUST include**
>
> 1. **Executive Summary**
>
>    * Overall compliance posture
>    * Top systemic risks
> 2. **Detailed Findings (Per Standard)**
>
>    * Clause reference
>    * Observed code evidence
>    * Gap description
> 3. **Actionable Recommendations**
>
>    * Precise technical remediation steps
>    * Priority and effort estimation
> 4. **Traceability Matrix**
>
>    * Code area → Standard → Recommendation
> 5. **Compliance Readiness Assessment**
>
>    * Audit readiness status
>    * Blocking vs non-blocking gaps
>
> ---
>
> ### **Constraints**
>
> * Be exhaustive, not summary-level
> * Avoid generic best practices unless explicitly mapped to a standard
> * Recommendations must be **implementable**, not theoretical
> * Output must be suitable for **formal audit, certification, or regulatory review**
