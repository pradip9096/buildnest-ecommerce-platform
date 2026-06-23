# BuildNest E-Commerce - Production Secrets Setup Script
# This script generates and applies all required Kubernetes secrets for production deployment

param(
    [Parameter(Mandatory=$false)]
    [string]$Namespace = "buildnest",
    
    [Parameter(Mandatory=$false)]
    [string]$Environment = "production",
    
    [Parameter(Mandatory=$true)]
    [string]$DatabasePassword,
    
    [Parameter(Mandatory=$true)]
    [string]$RedisPassword,
    
    [Parameter(Mandatory=$true)]
    [string]$KeystorePassword,
    
    [Parameter(Mandatory=$true)]
    [string]$KeystorePath,
    
    [Parameter(Mandatory=$true)]
    [string]$RazorpayKeyId,
    
    [Parameter(Mandatory=$true)]
    [string]$RazorpayKeySecret,
    
    [Parameter(Mandatory=$false)]
    [string]$GoogleClientId,
    
    [Parameter(Mandatory=$false)]
    [string]$GoogleClientSecret,
    
    [Parameter(Mandatory=$false)]
    [string]$PagerDutyServiceKey,
    
    [Parameter(Mandatory=$false)]
    [string]$SlackWebhookUrl,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,
    
    [Parameter(Mandatory=$false)]
    [switch]$Verbose
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Function to generate secure JWT secret
function New-JwtSecret {
    Write-Host "Generating 512-bit JWT secret..." -ForegroundColor Cyan
    $bytes = New-Object byte[] 64
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $secret = [Convert]::ToBase64String($bytes)
    return $secret
}

# Function to validate password strength
function Test-PasswordStrength {
    param([string]$Password)
    
    if ($Password.Length -lt 16) {
        throw "Password must be at least 16 characters long"
    }
    
    $hasUpper = $Password -cmatch '[A-Z]'
    $hasLower = $Password -cmatch '[a-z]'
    $hasDigit = $Password -match '\d'
    $hasSpecial = $Password -match '[!@#$%^&*(),.?":{}|<>]'
    
    if (-not ($hasUpper -and $hasLower -and $hasDigit -and $hasSpecial)) {
        throw "Password must contain uppercase, lowercase, digit, and special character"
    }
    
    return $true
}

# Function to encode file to base64
function ConvertTo-Base64File {
    param([string]$FilePath)
    
    if (-not (Test-Path $FilePath)) {
        throw "File not found: $FilePath"
    }
    
    $bytes = [System.IO.File]::ReadAllBytes($FilePath)
    return [Convert]::ToBase64String($bytes)
}

Write-Host "=====================================" -ForegroundColor Green
Write-Host "BuildNest Production Secrets Setup" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""

# Validate inputs
Write-Host "[1/8] Validating inputs..." -ForegroundColor Cyan
try {
    Test-PasswordStrength -Password $DatabasePassword
    Test-PasswordStrength -Password $RedisPassword
    Test-PasswordStrength -Password $KeystorePassword
    Write-Host "  ✓ Password strength validation passed" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Password validation failed: $_" -ForegroundColor Red
    exit 1
}

# Check if keystore file exists
if (-not (Test-Path $KeystorePath)) {
    Write-Host "  ✗ Keystore file not found: $KeystorePath" -ForegroundColor Red
    Write-Host "  Generate keystore with: openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12" -ForegroundColor Yellow
    exit 1
}
Write-Host "  ✓ Keystore file found" -ForegroundColor Green

# Check kubectl connectivity
Write-Host "[2/8] Checking Kubernetes connectivity..." -ForegroundColor Cyan
try {
    $null = kubectl cluster-info 2>&1
    Write-Host "  ✓ Connected to Kubernetes cluster" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Cannot connect to Kubernetes cluster" -ForegroundColor Red
    exit 1
}

# Create namespace if it doesn't exist
Write-Host "[3/8] Creating namespace..." -ForegroundColor Cyan
if ($DryRun) {
    Write-Host "  [DRY RUN] Would create namespace: $Namespace" -ForegroundColor Yellow
} else {
    kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -
    Write-Host "  ✓ Namespace created/verified: $Namespace" -ForegroundColor Green
}

# Generate JWT secret
Write-Host "[4/8] Generating JWT secret..." -ForegroundColor Cyan
$jwtSecret = New-JwtSecret
Write-Host "  ✓ JWT secret generated (512-bit)" -ForegroundColor Green

# Encode keystore to base64
Write-Host "[5/8] Encoding SSL keystore..." -ForegroundColor Cyan
$keystoreBase64 = ConvertTo-Base64File -FilePath $KeystorePath
Write-Host "  ✓ Keystore encoded to base64" -ForegroundColor Green

# Create main secrets
Write-Host "[6/8] Creating main application secrets..." -ForegroundColor Cyan
$secretYaml = @"
apiVersion: v1
kind: Secret
metadata:
  name: buildnest-secrets
  namespace: $Namespace
  labels:
    app: buildnest-ecommerce
    environment: $Environment
    created-by: setup-script
    created-date: "$(Get-Date -Format 'yyyy-MM-dd')"
type: Opaque
stringData:
  jwt.secret: "$jwtSecret"
  spring.datasource.url: "jdbc:mysql://production-mysql-service:3306/buildnest_ecommerce?useSSL=true&requireSSL=true"
  spring.datasource.username: "buildnest_user"
  spring.datasource.password: "$DatabasePassword"
  spring.redis.host: "production-redis-service"
  spring.redis.port: "6379"
  spring.redis.password: "$RedisPassword"
  server.ssl.enabled: "true"
  server.ssl.key-store: "/app/certs/keystore.p12"
  server.ssl.key-store-password: "$KeystorePassword"
  server.ssl.key-store-type: "PKCS12"
  razorpay.key.id: "$RazorpayKeyId"
  razorpay.key.secret: "$RazorpayKeySecret"
"@

# Add optional Google OAuth2 credentials
if ($GoogleClientId -and $GoogleClientSecret) {
    $secretYaml += @"

  spring.security.oauth2.client.registration.google.client-id: "$GoogleClientId"
  spring.security.oauth2.client.registration.google.client-secret: "$GoogleClientSecret"
"@
    Write-Host "  ✓ Google OAuth2 credentials included" -ForegroundColor Green
}

if ($DryRun) {
    Write-Host "  [DRY RUN] Would create secret: buildnest-secrets" -ForegroundColor Yellow
    if ($Verbose) {
        Write-Host $secretYaml -ForegroundColor Gray
    }
} else {
    $secretYaml | kubectl apply -f -
    Write-Host "  ✓ Main application secrets created" -ForegroundColor Green
}

# Create SSL certificate secret
Write-Host "[7/8] Creating SSL certificate secret..." -ForegroundColor Cyan
$sslSecretYaml = @"
apiVersion: v1
kind: Secret
metadata:
  name: buildnest-ssl-certs
  namespace: $Namespace
  labels:
    app: buildnest-ecommerce
    environment: $Environment
type: Opaque
data:
  keystore.p12: $keystoreBase64
"@

if ($DryRun) {
    Write-Host "  [DRY RUN] Would create secret: buildnest-ssl-certs" -ForegroundColor Yellow
} else {
    $sslSecretYaml | kubectl apply -f -
    Write-Host "  ✓ SSL certificate secret created" -ForegroundColor Green
}

# Create monitoring secrets (if provided)
Write-Host "[8/8] Creating monitoring secrets..." -ForegroundColor Cyan
if ($PagerDutyServiceKey) {
    $pagerdutyYaml = @"
apiVersion: v1
kind: Secret
metadata:
  name: alertmanager-pagerduty
  namespace: monitoring
  labels:
    app: prometheus
type: Opaque
stringData:
  pagerduty-service-key: "$PagerDutyServiceKey"
"@
    
    if ($DryRun) {
        Write-Host "  [DRY RUN] Would create secret: alertmanager-pagerduty" -ForegroundColor Yellow
    } else {
        kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -
        $pagerdutyYaml | kubectl apply -f -
        Write-Host "  ✓ PagerDuty secret created" -ForegroundColor Green
    }
}

if ($SlackWebhookUrl) {
    $slackYaml = @"
apiVersion: v1
kind: Secret
metadata:
  name: alertmanager-slack
  namespace: monitoring
  labels:
    app: prometheus
type: Opaque
stringData:
  slack-webhook-url: "$SlackWebhookUrl"
  slack-channel-critical: "#buildnest-critical"
  slack-channel-alerts: "#buildnest-alerts"
"@
    
    if ($DryRun) {
        Write-Host "  [DRY RUN] Would create secret: alertmanager-slack" -ForegroundColor Yellow
    } else {
        $slackYaml | kubectl apply -f -
        Write-Host "  ✓ Slack webhook secret created" -ForegroundColor Green
    }
}

if (-not $PagerDutyServiceKey -and -not $SlackWebhookUrl) {
    Write-Host "  ⚠ No monitoring secrets provided (PagerDuty/Slack)" -ForegroundColor Yellow
}

# Summary
Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Summary" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Namespace:           $Namespace" -ForegroundColor White
Write-Host "Environment:         $Environment" -ForegroundColor White
Write-Host "Secrets created:     buildnest-secrets, buildnest-ssl-certs" -ForegroundColor White
Write-Host "JWT Secret:          Generated (512-bit)" -ForegroundColor White
Write-Host "Database:            Configured" -ForegroundColor White
Write-Host "Redis:               Configured" -ForegroundColor White
Write-Host "SSL:                 Configured" -ForegroundColor White
Write-Host "Razorpay:            Configured" -ForegroundColor White
Write-Host "Google OAuth2:       $(if($GoogleClientId){'Configured'}else{'Not configured'})" -ForegroundColor White
Write-Host "PagerDuty:           $(if($PagerDutyServiceKey){'Configured'}else{'Not configured'})" -ForegroundColor White
Write-Host "Slack:               $(if($SlackWebhookUrl){'Configured'}else{'Not configured'})" -ForegroundColor White

if ($DryRun) {
    Write-Host ""
    Write-Host "⚠ DRY RUN MODE - No changes were made" -ForegroundColor Yellow
    Write-Host "Remove -DryRun flag to apply changes" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "✓ All secrets created successfully!" -ForegroundColor Green
}

# Verification steps
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Verify secrets:" -ForegroundColor White
Write-Host "   kubectl get secrets -n $Namespace" -ForegroundColor Gray
Write-Host "2. Update deployment to use secrets:" -ForegroundColor White
Write-Host "   kubectl apply -f kubernetes/buildnest-deployment.yaml" -ForegroundColor Gray
Write-Host "3. Test application startup:" -ForegroundColor White
Write-Host "   kubectl logs -f deployment/buildnest-app -n $Namespace" -ForegroundColor Gray
Write-Host ""

# Save JWT secret to secure file (optional)
$secureOutputPath = ".\buildnest-jwt-secret-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
if (-not $DryRun) {
    @"
BuildNest JWT Secret
Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Environment: $Environment

JWT_SECRET=$jwtSecret

IMPORTANT: 
- Store this file securely (password manager, vault)
- Do NOT commit to version control
- Delete after storing securely
"@ | Out-File -FilePath $secureOutputPath -Encoding UTF8

    Write-Host "JWT secret saved to: $secureOutputPath" -ForegroundColor Yellow
    Write-Host "⚠ Store this file securely and delete after saving to vault!" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
