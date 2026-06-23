from __future__ import annotations

import re
from pathlib import Path
from typing import Iterable, Set


def extract_ids(pattern: str, text: str) -> Set[str]:
    return set(re.findall(pattern, text))


def read_text(path: Path) -> str:
    if not path.exists():
        return ""
    return path.read_text(encoding="utf-8", errors="ignore")


def collect_test_sources(root: Path) -> str:
    tests_root = root / "src" / "test" / "java"
    if not tests_root.exists():
        return ""
    contents: list[str] = []
    for java_file in tests_root.rglob("*.java"):
        contents.append(read_text(java_file))
    return "\n".join(contents)


def write_report(output_path: Path, lines: Iterable[str]) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    root = Path(__file__).resolve().parents[1]

    stm_path = root / "STANDARDS_TRACEABILITY_MATRIX.md"
    risk_register_path = root / "RISK_REGISTER.md"
    test_cases_ref_path = root / "TEST_CASES_REFERENCE.md"
    testing_guide_path = root / "TESTING_GUIDE.md"

    stm_text = read_text(stm_path)
    risk_register_text = read_text(risk_register_path)
    test_cases_ref_text = read_text(test_cases_ref_path)
    testing_guide_text = read_text(testing_guide_path)
    test_sources_text = collect_test_sources(root)

    risk_id_pattern = r"\bR-[A-Z]+-\d+\b"
    tc_id_pattern = r"\bTC-[A-Z0-9-]+\b"

    risks_in_register = extract_ids(risk_id_pattern, risk_register_text)
    risks_in_stm = extract_ids(risk_id_pattern, stm_text)
    tcs_in_stm = extract_ids(tc_id_pattern, stm_text)

    tcs_in_docs = (
        extract_ids(tc_id_pattern, test_cases_ref_text)
        | extract_ids(tc_id_pattern, testing_guide_text)
    )
    tcs_in_tests = extract_ids(tc_id_pattern, test_sources_text)
    tcs_known = tcs_in_docs | tcs_in_tests

    missing_risks = sorted(risks_in_register - risks_in_stm)
    missing_tcs = sorted(tcs_in_stm - tcs_known)

    report_lines = [
        "Traceability Verification Summary",
        "================================",
        f"Risks in register: {len(risks_in_register)}",
        f"Risks in STM: {len(risks_in_stm)}",
        f"Test cases in STM: {len(tcs_in_stm)}",
        f"Test cases found in docs/tests: {len(tcs_known)}",
        "",
        "Missing Risk IDs in STM:",
        "- None" if not missing_risks else "- " + "\n- ".join(missing_risks),
        "",
        "Missing Test Case IDs in docs/tests:",
        "- None" if not missing_tcs else "- " + "\n- ".join(missing_tcs),
    ]

    report_path = root / "target" / "traceability" / "traceability_report.txt"
    write_report(report_path, report_lines)

    summary = f"Traceability check completed. Missing risks: {len(missing_risks)}; Missing test cases: {len(missing_tcs)}."
    print(summary)

    if missing_risks or missing_tcs:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
