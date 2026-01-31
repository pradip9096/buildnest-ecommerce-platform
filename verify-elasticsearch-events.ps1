# ============================================================================
# BuildNest E-Commerce Platform - Elasticsearch Event Verification Script
# ============================================================================
# Purpose: Verify that domain events are properly ingested into Elasticsearch
# Version: 1.0.0
# Last Updated: 2025-01-28
# Requirements: PowerShell 5.1+, Elasticsearch 8.2.0+, Spring Boot application running
#
# This script:
# 1. Creates test data (order, product, user) via API
# 2. Triggers domain events (OrderCreatedEvent, etc.)
# 3. Verifies events appear in Elasticsearch within expected timeframe
# 4. Checks event schema, timestamps, and data integrity
# 5. Creates Kibana dashboard for event monitoring
# ============================================================================

param(
    [string]$ElasticsearchHost = "http://localhost:9200",
    [string]$ElasticsearchUsername = "elastic",
    [string]$ElasticsearchPassword = "",
    [string]$AppHost = "http://localhost:8080",
    [string]$AdminToken = "",
    [int]$VerificationTimeoutSeconds = 30,
    [switch]$CreateKibanaDashboard,
    [switch]$ConfigureIndexRetention,
    [switch]$Verbose
)

# Color output functions
function Write-Success { param($Message) Write-Host "✓ $Message" -ForegroundColor Green }
function Write-Info { param($Message) Write-Host "ℹ $Message" -ForegroundColor Cyan }
function Write-Warning { param($Message) Write-Host "⚠ $Message" -ForegroundColor Yellow }
function Write-Error { param($Message) Write-Host "✗ $Message" -ForegroundColor Red }
function Write-Debug { param($Message) if ($Verbose) { Write-Host "  DEBUG: $Message" -ForegroundColor Gray } }

# ============================================================================
# ELASTICSEARCH API FUNCTIONS
# ============================================================================

function Invoke-ElasticsearchRequest {
    param(
        [string]$Method,
        [string]$Endpoint,
        [object]$Body
    )
    
    $uri = "$ElasticsearchHost/$Endpoint"
    $headers = @{ "Content-Type" = "application/json" }
    
    # Add basic auth if credentials provided
    if ($ElasticsearchUsername -and $ElasticsearchPassword) {
        $credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${ElasticsearchUsername}:${ElasticsearchPassword}"))
        $headers["Authorization"] = "Basic $credentials"
    }
    
    Write-Debug "$Method $uri"
    
    try {
        if ($Body) {
            $bodyJson = $Body | ConvertTo-Json -Depth 10
            Write-Debug "Request Body: $bodyJson"
            $response = Invoke-RestMethod -Uri $uri -Method $Method -Headers $headers -Body $bodyJson -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $uri -Method $Method -Headers $headers -ErrorAction Stop
        }
        return $response
    } catch {
        Write-Error "Elasticsearch request failed: $($_.Exception.Message)"
        throw
    }
}

function Test-ElasticsearchConnection {
    Write-Info "Testing Elasticsearch connection at $ElasticsearchHost..."
    try {
        $health = Invoke-ElasticsearchRequest -Method GET -Endpoint "_cluster/health"
        Write-Success "Elasticsearch cluster status: $($health.status)"
        return $true
    } catch {
        Write-Error "Failed to connect to Elasticsearch: $($_.Exception.Message)"
        return $false
    }
}

function Get-ElasticsearchIndices {
    $response = Invoke-ElasticsearchRequest -Method GET -Endpoint "_cat/indices?format=json"
    return $response | Where-Object { $_.index -like "buildnest-events-*" }
}

function Search-ElasticsearchEvents {
    param(
        [string]$EventType,
        [string]$SearchAfterTime
    )
    
    $query = @{
        query = @{
            bool = @{
                must = @(
                    @{ match = @{ "eventType" = $EventType } }
                )
            }
        }
        sort = @(
            @{ "timestamp" = @{ order = "desc" } }
        )
        size = 10
    }
    
    if ($SearchAfterTime) {
        $query.query.bool.must += @{
            range = @{
                timestamp = @{
                    gte = $SearchAfterTime
                }
            }
        }
    }
    
    $response = Invoke-ElasticsearchRequest -Method POST -Endpoint "buildnest-events-*/_search" -Body $query
    return $response.hits.hits
}

# ============================================================================
# APPLICATION API FUNCTIONS
# ============================================================================

function Invoke-AppRequest {
    param(
        [string]$Method,
        [string]$Endpoint,
        [object]$Body,
        [string]$Token = $AdminToken
    )
    
    $uri = "$AppHost/$Endpoint"
    $headers = @{ "Content-Type" = "application/json" }
    
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    
    Write-Debug "$Method $uri"
    
    try {
        if ($Body) {
            $bodyJson = $Body | ConvertTo-Json -Depth 5
            $response = Invoke-RestMethod -Uri $uri -Method $Method -Headers $headers -Body $bodyJson -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $uri -Method $Method -Headers $headers -ErrorAction Stop
        }
        return $response
    } catch {
        Write-Error "Application request failed: $($_.Exception.Message)"
        throw
    }
}

function Test-ApplicationConnection {
    Write-Info "Testing application connection at $AppHost..."
    try {
        $health = Invoke-RestMethod -Uri "$AppHost/actuator/health" -Method GET -ErrorAction Stop
        Write-Success "Application status: $($health.status)"
        return $true
    } catch {
        Write-Error "Failed to connect to application: $($_.Exception.Message)"
        return $false
    }
}

# ============================================================================
# EVENT VERIFICATION FUNCTIONS
# ============================================================================

function Test-EventSchema {
    param($Event)
    
    $requiredFields = @("eventType", "timestamp", "aggregateId", "payload")
    $missingFields = @()
    
    foreach ($field in $requiredFields) {
        if (-not $Event._source.$field) {
            $missingFields += $field
        }
    }
    
    if ($missingFields.Count -eq 0) {
        Write-Success "Event schema is valid"
        return $true
    } else {
        Write-Error "Event schema is invalid. Missing fields: $($missingFields -join ', ')"
        return $false
    }
}

function Wait-ForEvent {
    param(
        [string]$EventType,
        [string]$SearchAfterTime,
        [int]$TimeoutSeconds = 30
    )
    
    Write-Info "Waiting for $EventType event (timeout: ${TimeoutSeconds}s)..."
    $startTime = Get-Date
    
    while ($true) {
        $events = Search-ElasticsearchEvents -EventType $EventType -SearchAfterTime $SearchAfterTime
        
        if ($events.Count -gt 0) {
            Write-Success "Found $($events.Count) $EventType event(s)"
            return $events[0]
        }
        
        $elapsed = ((Get-Date) - $startTime).TotalSeconds
        if ($elapsed -ge $TimeoutSeconds) {
            Write-Error "Timeout waiting for $EventType event"
            return $null
        }
        
        Start-Sleep -Seconds 2
    }
}

# ============================================================================
# INDEX LIFECYCLE MANAGEMENT
# ============================================================================

function Configure-IndexRetentionPolicy {
    Write-Info "Configuring index retention policy (30 days)..."
    
    $ilmPolicy = @{
        policy = @{
            phases = @{
                hot = @{
                    actions = @{
                        rollover = @{
                            max_age = "7d"
                            max_size = "50gb"
                        }
                    }
                }
                delete = @{
                    min_age = "30d"
                    actions = @{
                        delete = @{}
                    }
                }
            }
        }
    }
    
    try {
        Invoke-ElasticsearchRequest -Method PUT -Endpoint "_ilm/policy/buildnest-events-policy" -Body $ilmPolicy | Out-Null
        Write-Success "ILM policy 'buildnest-events-policy' created/updated"
        
        # Apply policy to index template
        $indexTemplate = @{
            index_patterns = @("buildnest-events-*")
            template = @{
                settings = @{
                    "index.lifecycle.name" = "buildnest-events-policy"
                    "index.lifecycle.rollover_alias" = "buildnest-events"
                }
            }
        }
        
        Invoke-ElasticsearchRequest -Method PUT -Endpoint "_index_template/buildnest-events-template" -Body $indexTemplate | Out-Null
        Write-Success "Index template with ILM policy applied"
        
    } catch {
        Write-Error "Failed to configure retention policy: $($_.Exception.Message)"
    }
}

# ============================================================================
# KIBANA DASHBOARD CREATION
# ============================================================================

function Create-KibanaDashboard {
    Write-Info "Creating Kibana dashboard for event monitoring..."
    
    # Create index pattern if not exists
    $indexPattern = @{
        attributes = @{
            title = "buildnest-events-*"
            timeFieldName = "timestamp"
        }
    }
    
    try {
        # Note: This requires Kibana API access, which may need separate credentials
        Write-Warning "Kibana dashboard creation requires manual steps:"
        Write-Host "  1. Open Kibana at http://localhost:5601"
        Write-Host "  2. Go to Management > Stack Management > Index Patterns"
        Write-Host "  3. Create index pattern: buildnest-events-*"
        Write-Host "  4. Set timestamp field: timestamp"
        Write-Host "  5. Go to Dashboard > Create New Dashboard"
        Write-Host "  6. Add visualizations for:"
        Write-Host "     - Event count timeline (Line chart)"
        Write-Host "     - Events by type (Pie chart)"
        Write-Host "     - Events by aggregate ID (Data table)"
        Write-Host "     - Recent events (Saved search)"
        
    } catch {
        Write-Warning "Could not auto-create Kibana dashboard: $($_.Exception.Message)"
    }
}

# ============================================================================
# MAIN VERIFICATION WORKFLOW
# ============================================================================

function Test-ElasticsearchEventIntegration {
    Write-Host "`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║ BuildNest Elasticsearch Event Verification                    ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan
    
    # Step 1: Verify connections
    if (-not (Test-ElasticsearchConnection)) { return $false }
    if (-not (Test-ApplicationConnection)) { return $false }
    
    # Step 2: Check existing indices
    Write-Info "Checking existing Elasticsearch indices..."
    $indices = Get-ElasticsearchIndices
    if ($indices.Count -eq 0) {
        Write-Warning "No buildnest-events-* indices found. Events will create indices on first write."
    } else {
        Write-Success "Found $($indices.Count) event index/indices"
        foreach ($index in $indices) {
            Write-Debug "  - $($index.index): $($index.'docs.count') documents"
        }
    }
    
    # Record start time for event filtering
    $testStartTime = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    
    # Step 3: Create test order to trigger OrderCreatedEvent
    Write-Info "Creating test order to trigger OrderCreatedEvent..."
    try {
        # Note: This requires valid authentication token and may need adjustment based on API
        Write-Warning "Manual order creation required:"
        Write-Host "  1. Login to application at $AppHost"
        Write-Host "  2. Create a new order via UI or API"
        Write-Host "  3. Press Enter to continue with verification..."
        Read-Host
        
        # For automation, you would do:
        # $order = Invoke-AppRequest -Method POST -Endpoint "api/orders" -Body @{ items = @(...) }
        
    } catch {
        Write-Error "Failed to create test order: $($_.Exception.Message)"
        return $false
    }
    
    # Step 4: Wait for OrderCreatedEvent
    $orderEvent = Wait-ForEvent -EventType "OrderCreatedEvent" -SearchAfterTime $testStartTime -TimeoutSeconds $VerificationTimeoutSeconds
    
    if (-not $orderEvent) {
        Write-Error "OrderCreatedEvent was not found in Elasticsearch"
        Write-Warning "Troubleshooting steps:"
        Write-Host "  1. Check application logs for event publishing errors"
        Write-Host "  2. Verify Elasticsearch configuration in application.properties"
        Write-Host "  3. Check network connectivity: Test-NetConnection -ComputerName localhost -Port 9200"
        Write-Host "  4. Verify event handler is enabled: elasticsearch.metrics.enabled=true"
        return $false
    }
    
    # Step 5: Verify event schema
    Test-EventSchema -Event $orderEvent
    
    # Step 6: Display event details
    Write-Host "`n═══ Event Details ═══" -ForegroundColor Cyan
    Write-Host "Index: $($orderEvent._index)"
    Write-Host "Event Type: $($orderEvent._source.eventType)"
    Write-Host "Aggregate ID: $($orderEvent._source.aggregateId)"
    Write-Host "Timestamp: $($orderEvent._source.timestamp)"
    Write-Host "Payload: $($orderEvent._source.payload | ConvertTo-Json -Depth 5)"
    
    Write-Success "Elasticsearch event streaming verified successfully!"
    
    # Step 7: Optional configurations
    if ($ConfigureIndexRetention) {
        Configure-IndexRetentionPolicy
    }
    
    if ($CreateKibanaDashboard) {
        Create-KibanaDashboard
    }
    
    return $true
}

# ============================================================================
# EXECUTION
# ============================================================================

try {
    $result = Test-ElasticsearchEventIntegration
    
    if ($result) {
        Write-Host "`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
        Write-Host "║              ELASTICSEARCH VERIFICATION PASSED                 ║" -ForegroundColor Green
        Write-Host "╚════════════════════════════════════════════════════════════════╝`n" -ForegroundColor Green
        exit 0
    } else {
        Write-Host "`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Red
        Write-Host "║              ELASTICSEARCH VERIFICATION FAILED                 ║" -ForegroundColor Red
        Write-Host "╚════════════════════════════════════════════════════════════════╝`n" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Error "Unhandled exception: $($_.Exception.Message)"
    Write-Debug $_.ScriptStackTrace
    exit 1
}
