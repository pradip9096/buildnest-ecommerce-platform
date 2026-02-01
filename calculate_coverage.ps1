# Parse JaCoCo CSV and calculate coverage metrics
$csv = Import-Csv 'target\site\jacoco\jacoco.csv'

$totalInstructionMissed = 0
$totalInstructionCovered = 0
$totalBranchMissed = 0
$totalBranchCovered = 0
$totalLineMissed = 0
$totalLineCovered = 0
$totalComplexityMissed = 0
$totalComplexityCovered = 0
$totalMethodMissed = 0
$totalMethodCovered = 0

foreach ($row in $csv) {
    $totalInstructionMissed += [int]$row.INSTRUCTION_MISSED
    $totalInstructionCovered += [int]$row.INSTRUCTION_COVERED
    $totalBranchMissed += [int]$row.BRANCH_MISSED
    $totalBranchCovered += [int]$row.BRANCH_COVERED
    $totalLineMissed += [int]$row.LINE_MISSED
    $totalLineCovered += [int]$row.LINE_COVERED
    $totalComplexityMissed += [int]$row.COMPLEXITY_MISSED
    $totalComplexityCovered += [int]$row.COMPLEXITY_COVERED
    $totalMethodMissed += [int]$row.METHOD_MISSED
    $totalMethodCovered += [int]$row.METHOD_COVERED
}

$totalInstruction = $totalInstructionMissed + $totalInstructionCovered
$totalBranch = $totalBranchMissed + $totalBranchCovered
$totalLine = $totalLineMissed + $totalLineCovered
$totalComplexity = $totalComplexityMissed + $totalComplexityCovered
$totalMethod = $totalMethodMissed + $totalMethodCovered

$instructionCoverage = [math]::Round(($totalInstructionCovered / $totalInstruction) * 100, 2)
$branchCoverage = [math]::Round(($totalBranchCovered / $totalBranch) * 100, 2)
$lineCoverage = [math]::Round(($totalLineCovered / $totalLine) * 100, 2)
$complexityCoverage = [math]::Round(($totalComplexityCovered / $totalComplexity) * 100, 2)
$methodCoverage = [math]::Round(($totalMethodCovered / $totalMethod) * 100, 2)

Write-Host "=== CURRENT COVERAGE METRICS ==="
Write-Host "Instruction Coverage: $instructionCoverage% ($totalInstructionCovered/$totalInstruction covered)"
Write-Host "Branch Coverage:      $branchCoverage% ($totalBranchCovered/$totalBranch covered)"
Write-Host "Line Coverage:        $lineCoverage% ($totalLineCovered/$totalLine covered)"
Write-Host "Complexity Coverage:  $complexityCoverage% ($totalComplexityCovered/$totalComplexity covered)"
Write-Host "Method Coverage:      $methodCoverage% ($totalMethodCovered/$totalMethod covered)"
Write-Host ""
Write-Host "Progress towards 90% target:"
Write-Host "  Instruction:  Need +[math]::Max(0, 90 - $instructionCoverage)% more"
Write-Host "  Branch:       Need +[math]::Max(0, 90 - $branchCoverage)% more"
Write-Host "  Line:         Need +[math]::Max(0, 90 - $lineCoverage)% more"
Write-Host "  Complexity:   Need +[math]::Max(0, 90 - $complexityCoverage)% more"
Write-Host "  Method:       Need +[math]::Max(0, 90 - $methodCoverage)% more"
