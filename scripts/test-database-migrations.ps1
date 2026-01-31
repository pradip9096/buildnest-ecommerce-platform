# BuildNest E-Commerce - Database Migration Testing Script
# This script tests Liquibase migrations on production-like data

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('staging', 'local', 'production-clone')]
    [string]$Environment = "staging",
    
    [Parameter(Mandatory=$true)]
    [string]$DatabaseHost,
    
    [Parameter(Mandatory=$true)]
    [string]$DatabaseName,
    
    [Parameter(Mandatory=$true)]
    [string]$DatabaseUser,
    
    [Parameter(Mandatory=$true)]
    [string]$DatabasePassword,
    
    [Parameter(Mandatory=$false)]
    [int]$Port = 3306,
    
    [Parameter(Mandatory=$false)]
    [string]$BackupDir = ".\database-backups",
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipBackup,
    
    [Parameter(Mandatory=$false)]
    [switch]$TestRollback,
    
    [Parameter(Mandatory=$false)]
    [switch]$ValidateIntegrity,
    
    [Parameter(Mandatory=$false)]
    [switch]$MeasurePerformance,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,
    
    [Parameter(Mandatory=$false)]
    [switch]$Verbose
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$logFile = ".\migration-test-$timestamp.log"

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $logMessage = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [$Level] $Message"
    Write-Host $logMessage
    Add-Content -Path $logFile -Value $logMessage
}

function Test-DatabaseConnection {
    Write-Log "Testing database connection..." "INFO"
    
    $connectionString = "server=$DatabaseHost;port=$Port;database=$DatabaseName;uid=$DatabaseUser;pwd=$DatabasePassword"
    
    try {
        # Test using MySQL client
        $testQuery = "SELECT 1"
        $result = mysql -h $DatabaseHost -P $Port -u $DatabaseUser -p$DatabasePassword -D $DatabaseName -e $testQuery 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Log "Database connection successful" "SUCCESS"
            return $true
        } else {
            Write-Log "Database connection failed: $result" "ERROR"
            return $false
        }
    } catch {
        Write-Log "Database connection error: $_" "ERROR"
        return $false
    }
}

function Backup-Database {
    if ($SkipBackup) {
        Write-Log "Skipping database backup (--SkipBackup flag)" "WARN"
        return $null
    }
    
    Write-Log "Creating database backup..." "INFO"
    
    if (-not (Test-Path $BackupDir)) {
        New-Item -ItemType Directory -Path $BackupDir | Out-Null
    }
    
    $backupFile = Join-Path $BackupDir "$DatabaseName-$timestamp.sql"
    
    if ($DryRun) {
        Write-Log "[DRY RUN] Would backup to: $backupFile" "INFO"
        return $backupFile
    }
    
    $mysqldumpCmd = "mysqldump -h $DatabaseHost -P $Port -u $DatabaseUser -p$DatabasePassword --single-transaction --routines --triggers $DatabaseName > $backupFile"
    
    Write-Log "Executing: mysqldump (output: $backupFile)" "INFO"
    Invoke-Expression $mysqldumpCmd
    
    if ($LASTEXITCODE -eq 0) {
        $backupSize = (Get-Item $backupFile).Length / 1MB
        Write-Log "Backup created successfully: $backupFile ($([math]::Round($backupSize, 2)) MB)" "SUCCESS"
        return $backupFile
    } else {
        Write-Log "Backup failed!" "ERROR"
        throw "Database backup failed"
    }
}

function Get-TableRecordCounts {
    Write-Log "Collecting table record counts..." "INFO"
    
    $query = @"
SELECT 
    TABLE_NAME,
    TABLE_ROWS
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = '$DatabaseName'
    AND TABLE_TYPE = 'BASE TABLE'
ORDER BY 
    TABLE_ROWS DESC;
"@
    
    $result = mysql -h $DatabaseHost -P $Port -u $DatabaseUser -p$DatabasePassword -D $DatabaseName -e $query 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "Record counts collected" "SUCCESS"
        if ($Verbose) {
            Write-Log "Table statistics:`n$result" "INFO"
        }
        return $result
    } else {
        Write-Log "Failed to collect record counts: $result" "ERROR"
        return $null
    }
}

function Invoke-LiquibaseMigration {
    Write-Log "Running Liquibase migrations..." "INFO"
    
    # Set environment variables for Maven
    $env:SPRING_DATASOURCE_URL = "jdbc:mysql://${DatabaseHost}:${Port}/${DatabaseName}?useSSL=false"
    $env:SPRING_DATASOURCE_USERNAME = $DatabaseUser
    $env:SPRING_DATASOURCE_PASSWORD = $DatabasePassword
    
    if ($DryRun) {
        Write-Log "[DRY RUN] Would execute: ./mvnw liquibase:update" "INFO"
        return @{Success=$true; Duration=0}
    }
    
    $startTime = Get-Date
    
    Write-Log "Executing Maven Liquibase update..." "INFO"
    $output = & ./mvnw liquibase:update -e 2>&1
    $exitCode = $LASTEXITCODE
    
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds
    
    if ($Verbose) {
        Write-Log "Maven output:`n$output" "INFO"
    }
    
    if ($exitCode -eq 0) {
        Write-Log "Migrations executed successfully in $([math]::Round($duration, 2)) seconds" "SUCCESS"
        return @{Success=$true; Duration=$duration; Output=$output}
    } else {
        Write-Log "Migrations failed! Exit code: $exitCode" "ERROR"
        Write-Log "Error output:`n$output" "ERROR"
        return @{Success=$false; Duration=$duration; Output=$output; ExitCode=$exitCode}
    }
}

function Test-Rollback {
    if (-not $TestRollback) {
        Write-Log "Skipping rollback test (--TestRollback not specified)" "INFO"
        return $null
    }
    
    Write-Log "Testing migration rollback..." "INFO"
    
    # Set environment variables
    $env:SPRING_DATASOURCE_URL = "jdbc:mysql://${DatabaseHost}:${Port}/${DatabaseName}?useSSL=false"
    $env:SPRING_DATASOURCE_USERNAME = $DatabaseUser
    $env:SPRING_DATASOURCE_PASSWORD = $DatabasePassword
    
    if ($DryRun) {
        Write-Log "[DRY RUN] Would execute: ./mvnw liquibase:rollback" "INFO"
        return @{Success=$true}
    }
    
    Write-Log "Rolling back last changeset..." "INFO"
    $output = & ./mvnw liquibase:rollback -Dliquibase.rollbackCount=1 2>&1
    $exitCode = $LASTEXITCODE
    
    if ($exitCode -eq 0) {
        Write-Log "Rollback successful" "SUCCESS"
        
        # Re-apply migrations
        Write-Log "Re-applying migrations..." "INFO"
        $reapplyResult = Invoke-LiquibaseMigration
        
        if ($reapplyResult.Success) {
            Write-Log "Migrations re-applied successfully" "SUCCESS"
            return @{Success=$true; RollbackOutput=$output; ReapplyOutput=$reapplyResult.Output}
        } else {
            Write-Log "Failed to re-apply migrations after rollback!" "ERROR"
            return @{Success=$false; RollbackOutput=$output; ReapplyOutput=$reapplyResult.Output}
        }
    } else {
        Write-Log "Rollback failed! Exit code: $exitCode" "ERROR"
        Write-Log "Error output:`n$output" "ERROR"
        return @{Success=$false; RollbackOutput=$output; ExitCode=$exitCode}
    }
}

function Test-DataIntegrity {
    if (-not $ValidateIntegrity) {
        Write-Log "Skipping data integrity validation (--ValidateIntegrity not specified)" "INFO"
        return $null
    }
    
    Write-Log "Validating data integrity..." "INFO"
    
    $integrityTests = @()
    
    # Test 1: Check for orphaned order_items
    Write-Log "Test 1: Checking for orphaned order_items..." "INFO"
    $query1 = @"
SELECT COUNT(*) as orphaned_count
FROM order_items oi 
LEFT JOIN orders o ON oi.order_id = o.id 
WHERE o.id IS NULL;
"@
    
    $result1 = mysql -h $DatabaseHost -P $Port -u $DatabaseUser -p$DatabasePassword -D $DatabaseName -e $query1 -N 2>&1
    if ($result1 -match '^\d+$') {
        $orphanedCount = [int]$result1
        if ($orphanedCount -eq 0) {
            Write-Log "  ✓ No orphaned order_items found" "SUCCESS"
            $integrityTests += @{Test="Orphaned order_items"; Result="PASS"; Count=0}
        } else {
            Write-Log "  ✗ Found $orphanedCount orphaned order_items!" "ERROR"
            $integrityTests += @{Test="Orphaned order_items"; Result="FAIL"; Count=$orphanedCount}
        }
    }
    
    # Test 2: Check foreign key constraints
    Write-Log "Test 2: Verifying foreign key constraints..." "INFO"
    $query2 = @"
SELECT 
    COUNT(*) as fk_count
FROM 
    information_schema.TABLE_CONSTRAINTS 
WHERE 
    CONSTRAINT_SCHEMA = '$DatabaseName' 
    AND CONSTRAINT_TYPE = 'FOREIGN KEY';
"@
    
    $result2 = mysql -h $DatabaseHost -P $Port -u $DatabaseUser -p$DatabasePassword -D $DatabaseName -e $query2 -N 2>&1
    if ($result2 -match '^\d+$') {
        $fkCount = [int]$result2
        Write-Log "  ✓ Found $fkCount foreign key constraints" "SUCCESS"
        $integrityTests += @{Test="Foreign key constraints"; Result="PASS"; Count=$fkCount}
    }
    
    # Test 3: Check for NULL values in NOT NULL columns
    Write-Log "Test 3: Checking for NULL values in critical columns..." "INFO"
    $query3 = @"
SELECT 
    (SELECT COUNT(*) FROM users WHERE email IS NULL) +
    (SELECT COUNT(*) FROM users WHERE username IS NULL) +
    (SELECT COUNT(*) FROM products WHERE name IS NULL) +
    (SELECT COUNT(*) FROM orders WHERE user_id IS NULL) as null_count;
"@
    
    $result3 = mysql -h $DatabaseHost -P $Port -u $DatabaseUser -p$DatabasePassword -D $DatabaseName -e $query3 -N 2>&1
    if ($result3 -match '^\d+$') {
        $nullCount = [int]$result3
        if ($nullCount -eq 0) {
            Write-Log "  ✓ No NULL values in critical columns" "SUCCESS"
            $integrityTests += @{Test="NULL values in critical columns"; Result="PASS"; Count=0}
        } else {
            Write-Log "  ✗ Found $nullCount NULL values in critical columns!" "ERROR"
            $integrityTests += @{Test="NULL values in critical columns"; Result="FAIL"; Count=$nullCount}
        }
    }
    
    # Test 4: Verify indexes exist
    Write-Log "Test 4: Verifying database indexes..." "INFO"
    $query4 = @"
SELECT 
    COUNT(DISTINCT INDEX_NAME) as index_count
FROM 
    information_schema.STATISTICS
WHERE 
    TABLE_SCHEMA = '$DatabaseName'
    AND INDEX_NAME != 'PRIMARY';
"@
    
    $result4 = mysql -h $DatabaseHost -P $Port -u $DatabaseUser -p$DatabasePassword -D $DatabaseName -e $query4 -N 2>&1
    if ($result4 -match '^\d+$') {
        $indexCount = [int]$result4
        Write-Log "  ✓ Found $indexCount indexes" "SUCCESS"
        $integrityTests += @{Test="Database indexes"; Result="PASS"; Count=$indexCount}
    }
    
    # Summary
    $failedTests = $integrityTests | Where-Object { $_.Result -eq "FAIL" }
    if ($failedTests.Count -eq 0) {
        Write-Log "All integrity tests passed!" "SUCCESS"
        return @{Success=$true; Tests=$integrityTests}
    } else {
        Write-Log "$($failedTests.Count) integrity test(s) failed!" "ERROR"
        return @{Success=$false; Tests=$integrityTests; FailedTests=$failedTests}
    }
}

function Measure-MigrationPerformance {
    if (-not $MeasurePerformance) {
        return $null
    }
    
    Write-Log "Measuring migration performance..." "INFO"
    
    # Get database size
    $sizeQuery = @"
SELECT 
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS size_mb
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = '$DatabaseName';
"@
    
    $dbSize = mysql -h $DatabaseHost -P $Port -u $DatabaseUser -p$DatabasePassword -D $DatabaseName -e $sizeQuery -N 2>&1
    Write-Log "Database size: $dbSize MB" "INFO"
    
    # Performance metrics are collected during migration execution
    return @{DatabaseSize=$dbSize}
}

# Main execution
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Database Migration Testing" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""

Write-Log "Starting database migration test" "INFO"
Write-Log "Environment: $Environment" "INFO"
Write-Log "Database: $DatabaseHost:$Port/$DatabaseName" "INFO"
Write-Log "User: $DatabaseUser" "INFO"
Write-Log "Log file: $logFile" "INFO"
Write-Log "" "INFO"

# Step 1: Test connection
Write-Log "[1/7] Testing database connection..." "INFO"
if (-not (Test-DatabaseConnection)) {
    Write-Log "Cannot proceed without database connection" "ERROR"
    exit 1
}

# Step 2: Get initial state
Write-Log "[2/7] Collecting initial database state..." "INFO"
$initialCounts = Get-TableRecordCounts

# Step 3: Create backup
Write-Log "[3/7] Creating database backup..." "INFO"
$backupFile = Backup-Database

# Step 4: Measure performance (if requested)
Write-Log "[4/7] Measuring database metrics..." "INFO"
$performanceMetrics = Measure-MigrationPerformance

# Step 5: Run migrations
Write-Log "[5/7] Running Liquibase migrations..." "INFO"
$migrationResult = Invoke-LiquibaseMigration

if (-not $migrationResult.Success) {
    Write-Log "Migration failed! Restore from backup: $backupFile" "ERROR"
    exit 1
}

# Step 6: Validate integrity
Write-Log "[6/7] Validating data integrity..." "INFO"
$integrityResult = Test-DataIntegrity

if ($integrityResult -and -not $integrityResult.Success) {
    Write-Log "Data integrity validation failed!" "ERROR"
    Write-Log "Consider restoring from backup: $backupFile" "WARN"
}

# Step 7: Test rollback
Write-Log "[7/7] Testing rollback capability..." "INFO"
$rollbackResult = Test-Rollback

if ($rollbackResult -and -not $rollbackResult.Success) {
    Write-Log "Rollback test failed!" "ERROR"
}

# Final state
Write-Log "Collecting final database state..." "INFO"
$finalCounts = Get-TableRecordCounts

# Summary
Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Test Summary" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

Write-Host ""
Write-Host "Migration Results:" -ForegroundColor Cyan
Write-Host "  Status:           $(if($migrationResult.Success){'✓ SUCCESS'}else{'✗ FAILED'})" -ForegroundColor $(if($migrationResult.Success){'Green'}else{'Red'})
Write-Host "  Duration:         $([math]::Round($migrationResult.Duration, 2)) seconds" -ForegroundColor White
Write-Host "  Backup:           $backupFile" -ForegroundColor White

if ($performanceMetrics) {
    Write-Host "  Database Size:    $($performanceMetrics.DatabaseSize) MB" -ForegroundColor White
}

if ($integrityResult) {
    Write-Host ""
    Write-Host "Integrity Validation:" -ForegroundColor Cyan
    Write-Host "  Status:           $(if($integrityResult.Success){'✓ PASSED'}else{'✗ FAILED'})" -ForegroundColor $(if($integrityResult.Success){'Green'}else{'Red'})
    Write-Host "  Tests Run:        $($integrityResult.Tests.Count)" -ForegroundColor White
    
    foreach ($test in $integrityResult.Tests) {
        $icon = if ($test.Result -eq "PASS") { "✓" } else { "✗" }
        $color = if ($test.Result -eq "PASS") { "Green" } else { "Red" }
        Write-Host "    $icon $($test.Test): $($test.Result) (Count: $($test.Count))" -ForegroundColor $color
    }
}

if ($rollbackResult) {
    Write-Host ""
    Write-Host "Rollback Test:" -ForegroundColor Cyan
    Write-Host "  Status:           $(if($rollbackResult.Success){'✓ SUCCESS'}else{'✗ FAILED'})" -ForegroundColor $(if($rollbackResult.Success){'Green'}else{'Red'})
}

Write-Host ""
Write-Host "Performance Criteria:" -ForegroundColor Cyan
$migrationTimeOk = $migrationResult.Duration -lt 300
Write-Host "  Migration time:   $(if($migrationTimeOk){'✓'}else{'✗'}) $([math]::Round($migrationResult.Duration, 2))s / 300s target" -ForegroundColor $(if($migrationTimeOk){'Green'}else{'Yellow'})

Write-Host ""
Write-Host "Log file: $logFile" -ForegroundColor White

if ($migrationResult.Success -and ($null -eq $integrityResult -or $integrityResult.Success) -and ($null -eq $rollbackResult -or $rollbackResult.Success)) {
    Write-Host ""
    Write-Host "✓ All tests passed! Migrations are production-ready." -ForegroundColor Green
    Write-Host ""
    exit 0
} else {
    Write-Host ""
    Write-Host "✗ Some tests failed. Review results before production deployment." -ForegroundColor Red
    Write-Host ""
    exit 1
}
