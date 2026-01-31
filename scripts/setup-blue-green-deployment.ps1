# BuildNest E-Commerce - Blue-Green Deployment Setup Script
# This script installs Argo Rollouts and configures blue-green deployment

param(
    [Parameter(Mandatory=$false)]
    [string]$Namespace = "buildnest",
    
    [Parameter(Mandatory=$false)]
    [string]$ArgoRolloutsVersion = "v1.6.4",
    
    [Parameter(Mandatory=$false)]
    [switch]$InstallArgoRollouts,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,
    
    [Parameter(Mandatory=$false)]
    [switch]$Verbose
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "=====================================" -ForegroundColor Green
Write-Host "Blue-Green Deployment Setup" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""

# Check kubectl connectivity
Write-Host "[1/6] Checking Kubernetes connectivity..." -ForegroundColor Cyan
try {
    $null = kubectl cluster-info 2>&1
    Write-Host "  ✓ Connected to Kubernetes cluster" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Cannot connect to Kubernetes cluster" -ForegroundColor Red
    exit 1
}

# Install Argo Rollouts
if ($InstallArgoRollouts) {
    Write-Host "[2/6] Installing Argo Rollouts..." -ForegroundColor Cyan
    
    $argoRolloutsUrl = "https://github.com/argoproj/argo-rollouts/releases/download/$ArgoRolloutsVersion/install.yaml"
    
    if ($DryRun) {
        Write-Host "  [DRY RUN] Would install Argo Rollouts from: $argoRolloutsUrl" -ForegroundColor Yellow
    } else {
        kubectl create namespace argo-rollouts --dry-run=client -o yaml | kubectl apply -f -
        kubectl apply -n argo-rollouts -f $argoRolloutsUrl
        Write-Host "  ✓ Argo Rollouts installed" -ForegroundColor Green
        
        Write-Host "  Waiting for Argo Rollouts to be ready..." -ForegroundColor Cyan
        kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argo-rollouts -n argo-rollouts --timeout=180s
        Write-Host "  ✓ Argo Rollouts is ready" -ForegroundColor Green
    }
    
    # Install kubectl plugin
    Write-Host "  Installing kubectl-argo-rollouts plugin..." -ForegroundColor Cyan
    if ($IsWindows) {
        Write-Host "  Download from: https://github.com/argoproj/argo-rollouts/releases/download/$ArgoRolloutsVersion/kubectl-argo-rollouts-windows-amd64" -ForegroundColor Yellow
        Write-Host "  Place in PATH as: kubectl-argo-rollouts.exe" -ForegroundColor Yellow
    } else {
        if (-not $DryRun) {
            $pluginUrl = "https://github.com/argoproj/argo-rollouts/releases/download/$ArgoRolloutsVersion/kubectl-argo-rollouts-linux-amd64"
            curl -LO $pluginUrl
            chmod +x kubectl-argo-rollouts-linux-amd64
            sudo mv kubectl-argo-rollouts-linux-amd64 /usr/local/bin/kubectl-argo-rollouts
            Write-Host "  ✓ kubectl-argo-rollouts plugin installed" -ForegroundColor Green
        }
    }
} else {
    Write-Host "[2/6] Skipping Argo Rollouts installation (--InstallArgoRollouts not specified)" -ForegroundColor Yellow
}

# Create namespace
Write-Host "[3/6] Creating namespace..." -ForegroundColor Cyan
if ($DryRun) {
    Write-Host "  [DRY RUN] Would create namespace: $Namespace" -ForegroundColor Yellow
} else {
    kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -
    Write-Host "  ✓ Namespace created/verified: $Namespace" -ForegroundColor Green
}

# Apply Rollout configuration
Write-Host "[4/6] Applying Rollout configuration..." -ForegroundColor Cyan
if ($DryRun) {
    Write-Host "  [DRY RUN] Would apply: kubernetes/buildnest-rollout.yaml" -ForegroundColor Yellow
} else {
    kubectl apply -f .\kubernetes\buildnest-rollout.yaml
    Write-Host "  ✓ Rollout configuration applied" -ForegroundColor Green
}

# Create ServiceAccount and RBAC
Write-Host "[5/6] Creating ServiceAccount and RBAC..." -ForegroundColor Cyan

$rbacYaml = @"
apiVersion: v1
kind: ServiceAccount
metadata:
  name: buildnest-app
  namespace: $Namespace
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: buildnest-app
  namespace: $Namespace
rules:
- apiGroups: [""]
  resources: ["secrets", "configmaps"]
  verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: buildnest-app
  namespace: $Namespace
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: buildnest-app
subjects:
- kind: ServiceAccount
  name: buildnest-app
  namespace: $Namespace
"@

if ($DryRun) {
    Write-Host "  [DRY RUN] Would create ServiceAccount and RBAC" -ForegroundColor Yellow
} else {
    $rbacYaml | kubectl apply -f -
    Write-Host "  ✓ ServiceAccount and RBAC created" -ForegroundColor Green
}

# Verification
Write-Host "[6/6] Verifying installation..." -ForegroundColor Cyan
if (-not $DryRun) {
    $rolloutStatus = kubectl get rollout buildnest-app -n $Namespace -o jsonpath='{.status.phase}' 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Rollout status: $rolloutStatus" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Rollout not yet deployed" -ForegroundColor Yellow
    }
    
    $activeService = kubectl get service buildnest-active -n $Namespace -o name 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Active service created" -ForegroundColor Green
    }
    
    $previewService = kubectl get service buildnest-preview -n $Namespace -o name 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Preview service created" -ForegroundColor Green
    }
}

# Summary
Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
Write-Host "Usage Examples:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Deploy new version:" -ForegroundColor White
Write-Host "   kubectl argo rollouts set image buildnest-app buildnest-app=pradip9096/buildnest-ecommerce:v1.2.0 -n $Namespace" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Monitor rollout:" -ForegroundColor White
Write-Host "   kubectl argo rollouts get rollout buildnest-app -n $Namespace --watch" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Test preview service:" -ForegroundColor White
Write-Host "   kubectl port-forward svc/buildnest-preview 8080:8080 -n $Namespace" -ForegroundColor Gray
Write-Host "   curl http://localhost:8080/actuator/health" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Promote to production:" -ForegroundColor White
Write-Host "   kubectl argo rollouts promote buildnest-app -n $Namespace" -ForegroundColor Gray
Write-Host ""
Write-Host "5. Rollback:" -ForegroundColor White
Write-Host "   kubectl argo rollouts undo buildnest-app -n $Namespace" -ForegroundColor Gray
Write-Host ""
Write-Host "6. Abort rollout:" -ForegroundColor White
Write-Host "   kubectl argo rollouts abort buildnest-app -n $Namespace" -ForegroundColor Gray
Write-Host ""
Write-Host "7. View rollout history:" -ForegroundColor White
Write-Host "   kubectl argo rollouts history buildnest-app -n $Namespace" -ForegroundColor Gray
Write-Host ""
