# Analyze services with high missed branches
$csv = Import-Csv 'target\site\jacoco\jacoco.csv'

Write-Host "=== TOP SERVICES BY MISSED BRANCHES ==="
$csv | Where-Object { $_.BRANCH_MISSED -gt 5 -and $_.CLASS -match 'Service' } | `
    Sort-Object -Property {[int]$_.BRANCH_MISSED} -Descending | `
    Select-Object -First 15 | `
    ForEach-Object { 
        Write-Host "$($_.CLASS): $($_.BRANCH_MISSED) missed, $($_.BRANCH_COVERED) covered (Package: $($_.PACKAGE))"
    }

Write-Host ""
Write-Host "=== TOP CLASSES BY MISSED BRANCHES (All) ==="
$csv | Sort-Object -Property {[int]$_.BRANCH_MISSED} -Descending | `
    Select-Object -First 15 | `
    ForEach-Object { 
        Write-Host "$($_.CLASS): $($_.BRANCH_MISSED) missed, $($_.BRANCH_COVERED) covered"
    }
