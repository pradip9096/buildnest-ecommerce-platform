from __future__ import annotations

from pathlib import Path
import xml.etree.ElementTree as ET


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    reports_dir = root / "target" / "surefire-reports"
    if not reports_dir.exists():
        print("Surefire reports not found.")
        return 0

    total_tests = 0
    total_time = 0.0
    slowest_test = ("", 0.0)

    for xml_file in reports_dir.glob("TEST-*.xml"):
        try:
            tree = ET.parse(xml_file)
            suite = tree.getroot()
        except ET.ParseError:
            continue

        for testcase in suite.findall("testcase"):
            name = testcase.get("name", "")
            classname = testcase.get("classname", "")
            time_str = testcase.get("time", "0")
            try:
                time_val = float(time_str)
            except ValueError:
                time_val = 0.0

            total_tests += 1
            total_time += time_val
            if time_val > slowest_test[1]:
                slowest_test = (f"{classname}.{name}", time_val)

    avg_time = (total_time / total_tests) if total_tests else 0.0

    lines = [
        "Test Performance Summary",
        "========================",
        f"Total tests: {total_tests}",
        f"Total time (s): {total_time:.3f}",
        f"Average time per test (s): {avg_time:.3f}",
        f"Slowest test: {slowest_test[0] or 'N/A'} ({slowest_test[1]:.3f}s)",
    ]

    output_dir = root / "target" / "test-performance"
    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "summary.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")

    print("\n".join(lines))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
