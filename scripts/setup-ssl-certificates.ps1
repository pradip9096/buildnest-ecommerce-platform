# BuildNest E-Commerce - SSL Certificate Setup Script
# This script automates SSL certificate generation and deployment

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('letsencrypt', 'self-signed', 'manual')]
    [string]$CertificateType = "letsencrypt",
    
    [Parameter(Mandatory=$false)]
    [string]$Domain = "api.buildnest.com",
    
    [Parameter(Mandatory=$false)]
    [string]$Email = "admin@buildnest.com",
    
    [Parameter(Mandatory=$false)]
    [string]$Namespace = "buildnest",
    
    [Parameter(Mandatory=$false)]
    [string]$CertPath,
    
    [Parameter(Mandatory=$false)]
    [string]$KeyPath,
    
    [Parameter(Mandatory=$false)]
    [string]$KeystorePassword,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,
    
    [Parameter(Mandatory=$false)]
    [switch]$Verbose
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "=====================================" -ForegroundColor Green
Write-Host "BuildNest SSL Certificate Setup" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""

# Function to install cert-manager
function Install-CertManager {
    Write-Host "Installing cert-manager..." -ForegroundColor Cyan
    
    $certManagerVersion = "v1.13.0"
    $certManagerUrl = "https://github.com/cert-manager/cert-manager/releases/download/$certManagerVersion/cert-manager.yaml"
    
    if ($DryRun) {
        Write-Host "  [DRY RUN] Would install cert-manager $certManagerVersion" -ForegroundColor Yellow
    } else {
        kubectl apply -f $certManagerUrl
        Write-Host "  ✓ cert-manager installed" -ForegroundColor Green
        
        Write-Host "  Waiting for cert-manager to be ready..." -ForegroundColor Cyan
        kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance=cert-manager -n cert-manager --timeout=180s
        Write-Host "  ✓ cert-manager is ready" -ForegroundColor Green
    }
}

# Function to create Let's Encrypt ClusterIssuer
function New-LetsEncryptIssuer {
    Write-Host "Creating Let's Encrypt ClusterIssuer..." -ForegroundColor Cyan
    
    $issuerYaml = @"
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
  labels:
    app: buildnest-ecommerce
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: $Email
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
---
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-staging
  labels:
    app: buildnest-ecommerce
spec:
  acme:
    server: https://acme-staging-v02.api.letsencrypt.org/directory
    email: $Email
    privateKeySecretRef:
      name: letsencrypt-staging
    solvers:
    - http01:
        ingress:
          class: nginx
"@
    
    if ($DryRun) {
        Write-Host "  [DRY RUN] Would create ClusterIssuers" -ForegroundColor Yellow
        if ($Verbose) {
            Write-Host $issuerYaml -ForegroundColor Gray
        }
    } else {
        $issuerYaml | kubectl apply -f -
        Write-Host "  ✓ ClusterIssuers created (production & staging)" -ForegroundColor Green
    }
}

# Function to create Ingress with TLS
function New-TlsIngress {
    Write-Host "Creating Ingress with TLS..." -ForegroundColor Cyan
    
    $ingressYaml = @"
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: buildnest-ingress
  namespace: $Namespace
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
  labels:
    app: buildnest-ecommerce
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - $Domain
    secretName: buildnest-tls
  rules:
  - host: $Domain
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: buildnest-service
            port:
              number: 8080
"@
    
    if ($DryRun) {
        Write-Host "  [DRY RUN] Would create Ingress" -ForegroundColor Yellow
        if ($Verbose) {
            Write-Host $ingressYaml -ForegroundColor Gray
        }
    } else {
        $ingressYaml | kubectl apply -f -
        Write-Host "  ✓ Ingress created with TLS configuration" -ForegroundColor Green
        Write-Host "  ⏳ Certificate issuance may take a few minutes..." -ForegroundColor Yellow
    }
}

# Function to generate self-signed certificate
function New-SelfSignedCertificate {
    Write-Host "Generating self-signed certificate..." -ForegroundColor Cyan
    
    $certDir = ".\certs"
    if (-not (Test-Path $certDir)) {
        New-Item -ItemType Directory -Path $certDir | Out-Null
    }
    
    $certFile = Join-Path $certDir "cert.pem"
    $keyFile = Join-Path $certDir "key.pem"
    $keystoreFile = Join-Path $certDir "keystore.p12"
    
    if (-not $KeystorePassword) {
        Write-Host "  Generating random keystore password..." -ForegroundColor Cyan
        $bytes = New-Object byte[] 32
        [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
        $KeystorePassword = [Convert]::ToBase64String($bytes).Substring(0, 24)
    }
    
    # Generate private key and certificate
    $opensslCmd = @"
openssl req -x509 -newkey rsa:4096 -nodes \
  -keyout $keyFile \
  -out $certFile \
  -days 365 \
  -subj "/C=US/ST=State/L=City/O=BuildNest/CN=$Domain"
"@
    
    if ($DryRun) {
        Write-Host "  [DRY RUN] Would generate self-signed certificate" -ForegroundColor Yellow
        Write-Host "  Command: $opensslCmd" -ForegroundColor Gray
    } else {
        Write-Host "  Generating RSA 4096-bit private key and certificate..." -ForegroundColor Cyan
        Invoke-Expression $opensslCmd.Replace("`n", "")
        Write-Host "  ✓ Certificate generated: $certFile" -ForegroundColor Green
        Write-Host "  ✓ Private key generated: $keyFile" -ForegroundColor Green
        
        # Create PKCS12 keystore
        Write-Host "  Creating PKCS12 keystore..." -ForegroundColor Cyan
        $keystoreCmd = "openssl pkcs12 -export -in $certFile -inkey $keyFile -out $keystoreFile -name buildnest -passout pass:$KeystorePassword"
        Invoke-Expression $keystoreCmd
        Write-Host "  ✓ Keystore created: $keystoreFile" -ForegroundColor Green
        
        # Create Kubernetes secret
        Write-Host "  Creating Kubernetes secret..." -ForegroundColor Cyan
        kubectl create secret generic buildnest-ssl-certs `
            --from-file=keystore.p12=$keystoreFile `
            --namespace=$Namespace `
            --dry-run=client -o yaml | kubectl apply -f -
        Write-Host "  ✓ Kubernetes secret created" -ForegroundColor Green
        
        # Save password to file
        $passwordFile = Join-Path $certDir "keystore-password.txt"
        @"
BuildNest SSL Keystore Password
Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Type: Self-Signed Certificate

KEYSTORE_PASSWORD=$KeystorePassword

Certificate: $certFile
Private Key: $keyFile
Keystore: $keystoreFile

IMPORTANT:
- Store this password securely
- Do NOT commit to version control
- Certificate expires in 365 days
"@ | Out-File -FilePath $passwordFile -Encoding UTF8
        
        Write-Host "  ✓ Keystore password saved to: $passwordFile" -ForegroundColor Yellow
    }
}

# Function to import manual certificate
function Import-ManualCertificate {
    if (-not $CertPath -or -not $KeyPath -or -not $KeystorePassword) {
        Write-Host "  ✗ Manual certificate requires: -CertPath, -KeyPath, -KeystorePassword" -ForegroundColor Red
        exit 1
    }
    
    if (-not (Test-Path $CertPath)) {
        Write-Host "  ✗ Certificate file not found: $CertPath" -ForegroundColor Red
        exit 1
    }
    
    if (-not (Test-Path $KeyPath)) {
        Write-Host "  ✗ Private key file not found: $KeyPath" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Importing manual certificate..." -ForegroundColor Cyan
    
    $keystoreFile = ".\buildnest-keystore.p12"
    
    # Create PKCS12 keystore
    Write-Host "  Creating PKCS12 keystore from provided certificate..." -ForegroundColor Cyan
    $keystoreCmd = "openssl pkcs12 -export -in $CertPath -inkey $KeyPath -out $keystoreFile -name buildnest -passout pass:$KeystorePassword"
    
    if ($DryRun) {
        Write-Host "  [DRY RUN] Would create keystore from manual certificate" -ForegroundColor Yellow
    } else {
        Invoke-Expression $keystoreCmd
        Write-Host "  ✓ Keystore created: $keystoreFile" -ForegroundColor Green
        
        # Create Kubernetes secret
        Write-Host "  Creating Kubernetes secret..." -ForegroundColor Cyan
        kubectl create secret generic buildnest-ssl-certs `
            --from-file=keystore.p12=$keystoreFile `
            --namespace=$Namespace `
            --dry-run=client -o yaml | kubectl apply -f -
        Write-Host "  ✓ Kubernetes secret created" -ForegroundColor Green
    }
}

# Main execution
Write-Host "Certificate Type: $CertificateType" -ForegroundColor White
Write-Host "Domain:           $Domain" -ForegroundColor White
Write-Host "Namespace:        $Namespace" -ForegroundColor White
Write-Host ""

# Check kubectl connectivity
Write-Host "[1/4] Checking Kubernetes connectivity..." -ForegroundColor Cyan
try {
    $null = kubectl cluster-info 2>&1
    Write-Host "  ✓ Connected to Kubernetes cluster" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Cannot connect to Kubernetes cluster" -ForegroundColor Red
    exit 1
}

# Create namespace if needed
Write-Host "[2/4] Creating namespace..." -ForegroundColor Cyan
if ($DryRun) {
    Write-Host "  [DRY RUN] Would create namespace: $Namespace" -ForegroundColor Yellow
} else {
    kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -
    Write-Host "  ✓ Namespace verified: $Namespace" -ForegroundColor Green
}

# Execute based on certificate type
Write-Host "[3/4] Setting up SSL certificate..." -ForegroundColor Cyan
switch ($CertificateType) {
    "letsencrypt" {
        Install-CertManager
        New-LetsEncryptIssuer
        New-TlsIngress
    }
    "self-signed" {
        New-SelfSignedCertificate
    }
    "manual" {
        Import-ManualCertificate
    }
}

# Verification
Write-Host "[4/4] Verification steps..." -ForegroundColor Cyan
if ($CertificateType -eq "letsencrypt") {
    Write-Host "  Check certificate status:" -ForegroundColor White
    Write-Host "    kubectl get certificate -n $Namespace" -ForegroundColor Gray
    Write-Host "    kubectl describe certificate buildnest-tls -n $Namespace" -ForegroundColor Gray
} else {
    Write-Host "  Check secret:" -ForegroundColor White
    Write-Host "    kubectl get secret buildnest-ssl-certs -n $Namespace" -ForegroundColor Gray
}

Write-Host "  Test HTTPS endpoint:" -ForegroundColor White
Write-Host "    curl -k https://$Domain/actuator/health" -ForegroundColor Gray
Write-Host "    openssl s_client -connect $Domain:443 -showcerts" -ForegroundColor Gray

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

if ($CertificateType -eq "letsencrypt") {
    Write-Host ""
    Write-Host "Let's Encrypt certificate will be issued automatically." -ForegroundColor Yellow
    Write-Host "This may take 2-5 minutes. Monitor with:" -ForegroundColor Yellow
    Write-Host "  kubectl get certificate -n $Namespace -w" -ForegroundColor Gray
} elseif ($CertificateType -eq "self-signed") {
    Write-Host ""
    Write-Host "⚠ Self-signed certificate generated for development/testing only!" -ForegroundColor Yellow
    Write-Host "⚠ Use Let's Encrypt for production deployments" -ForegroundColor Yellow
}
