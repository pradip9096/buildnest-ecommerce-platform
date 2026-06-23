# SECRET ROTATION PROCEDURES
## CRITICAL BLOCKER #5 FIX: Comprehensive Secret Management & Rotation

**Document Version**: 1.0  
**Last Updated**: January 31, 2026  
**Owner**: Security Team / DevOps  
**Review Frequency**: Quarterly

---

## TABLE OF CONTENTS

1. [Overview](#overview)
2. [Rotation Schedule](#rotation-schedule)
3. [JWT Secret Rotation](#jwt-secret-rotation)
4. [Database Password Rotation](#database-password-rotation)
5. [Redis Password Rotation](#redis-password-rotation)
6. [API Key Rotation](#api-key-rotation)
7. [SSL Certificate Renewal](#ssl-certificate-renewal)
8. [Emergency Rotation](#emergency-rotation)
9. [Audit & Compliance](#audit-compliance)

---

## OVERVIEW

This document provides step-by-step procedures for rotating all secrets used by the BuildNest E-Commerce Platform. Regular secret rotation is critical for:

- **Security**: Limit exposure window if secrets are compromised
- **Compliance**: Meet PCI-DSS, SOC2, ISO 27001 requirements
- **Best Practices**: Follow OWASP and NIST guidelines

### Rotation Philosophy

All secrets support **dual-secret validation** during transition periods to enable zero-downtime rotation:

- **Old Secret**: Continues to work during grace period
- **New Secret**: Begins accepting new requests immediately
- **Grace Period**: 15-60 minutes (varies by secret type)
- **Cleanup**: Old secret revoked after grace period

---

## ROTATION SCHEDULE

| Secret Type | Rotation Frequency | Grace Period | Automation Status |
|-------------|-------------------|--------------|-------------------|
| JWT Secret | Every 90 days | 15 minutes | Manual |
| Database Password | Every 180 days | 60 minutes | Manual |
| Redis Password | Every 90 days | 30 minutes | Manual |
| Razorpay API Keys | Every 90 days | Immediate | Manual |
| OAuth2 Credentials | Every 180 days | Immediate | Manual |
| SSL Certificates | Every 60 days | N/A | Automated (cert-manager) |

### Calendar Reminders

Set up recurring calendar events:
- **1st of each month**: Review upcoming rotations
- **15th of each quarter (Jan, Apr, Jul, Oct)**: Rotate JWT & Redis
- **15th of each half-year (Jan, Jul)**: Rotate database passwords
- **60 days before SSL expiry**: Verify cert-manager auto-renewal

---

## JWT SECRET ROTATION

### Prerequisites
- [ ] Kubernetes access with secret edit permissions
- [ ] Application pods can tolerate rolling restarts
- [ ] Monitoring dashboard open (watch for auth failures)

### Rotation Process

#### Step 1: Generate New JWT Secret

```powershell
# Generate 512-bit secret (64 bytes base64-encoded)
$newJwtSecret = -join ((65..90) + (97..122) + (48..57) | Get-Random -Count 64 | ForEach-Object {[char]$_})
$newJwtSecretBase64 = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($newJwtSecret))

Write-Host "New JWT Secret (Base64): $newJwtSecretBase64"

# Save to secure location
$newJwtSecretBase64 | Out-File -FilePath "jwt-secret-$(Get-Date -Format 'yyyyMMdd').txt" -NoNewline
Write-Host "✅ Secret saved to jwt-secret-$(Get-Date -Format 'yyyyMMdd').txt"
Write-Warning "⚠️  Store this file in password manager and delete after rotation!"
```

#### Step 2: Update Kubernetes Secret

```bash
# Backup existing secret
kubectl get secret buildnest-secrets -n buildnest -o yaml > buildnest-secrets-backup-$(date +%Y%m%d).yaml

# Update JWT secret
kubectl create secret generic buildnest-secrets-new \
  --from-literal=jwt.secret="$NEW_JWT_SECRET_BASE64" \
  --from-literal=jwt.secret.old="$OLD_JWT_SECRET_BASE64" \
  --dry-run=client -o yaml | kubectl apply -f -

# Verify secret updated
kubectl get secret buildnest-secrets -n buildnest -o jsonpath='{.data.jwt\.secret}' | base64 -d
```

#### Step 3: Implement Dual-Secret Validation (Code Change Required)

**IMPORTANT**: Before rotating JWT secret in production, implement this code change:

```java
// src/main/java/com/example/buildnest_ecommerce/security/Jwt/JwtTokenProvider.java

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.secret.old:}")
    private String jwtSecretOld;  // Old secret for grace period
    
    public boolean validateToken(String token) {
        try {
            // Try with new secret first
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            // If new secret fails and old secret exists, try old secret
            if (!jwtSecretOld.isEmpty()) {
                try {
                    Jwts.parser().setSigningKey(jwtSecretOld).parseClaimsJws(token);
                    logger.warn("Token validated with OLD JWT secret - will expire soon");
                    return true;
                } catch (Exception ex) {
                    logger.error("Token validation failed with both secrets");
                    return false;
                }
            }
            return false;
        }
    }
}
```

#### Step 4: Rolling Restart Application

```bash
# Restart deployment (triggers rolling update with new secret)
kubectl rollout restart deployment/buildnest-app -n buildnest

# Watch rollout progress
kubectl rollout status deployment/buildnest-app -n buildnest --watch

# Verify new pods are healthy
kubectl get pods -n buildnest -l app=buildnest
```

#### Step 5: Grace Period (15 Minutes)

During this period:
- ✅ New logins use new JWT secret
- ✅ Existing tokens (signed with old secret) still valid
- ⏱️ Wait for token expiration (15 minutes per application.properties)

**Monitoring**:
```bash
# Watch authentication metrics
kubectl logs -f deployment/buildnest-app -n buildnest | grep "JWT"

# Check error rate
curl http://prometheus:9090/api/v1/query?query=rate(http_requests_total{status=~"401|403"}[5m])
```

#### Step 6: Remove Old Secret

After 15 minutes (all old tokens expired):

```bash
# Remove old secret from configuration
kubectl patch secret buildnest-secrets -n buildnest \
  --type=json -p='[{"op": "remove", "path": "/data/jwt.secret.old"}]'

# Verify old secret removed
kubectl describe secret buildnest-secrets -n buildnest
```

#### Step 7: Verification

```bash
# Test authentication with new secret
curl -X POST https://api.buildnest.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}' \
  | jq '.token'

# Verify token works
TOKEN=$(curl -s -X POST https://api.buildnest.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}' | jq -r '.token')

curl -H "Authorization: Bearer $TOKEN" https://api.buildnest.com/api/user/profile

# Expected: 200 OK with user profile
```

#### Rollback Procedure (If Issues)

```bash
# Restore old secret from backup
kubectl apply -f buildnest-secrets-backup-$(date +%Y%m%d).yaml

# Restart pods
kubectl rollout restart deployment/buildnest-app -n buildnest
```

### Documentation

Record rotation in audit log:

```markdown
## JWT Secret Rotation - $(date +%Y-%m-%d)

- **Rotated By**: [Your Name]
- **Start Time**: [HH:MM UTC]
- **End Time**: [HH:MM UTC]
- **Old Secret Hash**: $(echo -n "$OLD_SECRET" | sha256sum | cut -d' ' -f1)
- **New Secret Hash**: $(echo -n "$NEW_SECRET" | sha256sum | cut -d' ' -f1)
- **Issues**: None / [Describe any issues]
- **Rollback Required**: No / Yes - [Reason]
```

---

## DATABASE PASSWORD ROTATION

### Prerequisites
- [ ] Database admin access
- [ ] Application downtime window scheduled (5-10 minutes)
- [ ] Database backup completed
- [ ] Rollback plan tested

### Rotation Process

#### Step 1: Create New Database User

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create new user with rotated password
CREATE USER 'buildnest_user_v2'@'%' IDENTIFIED BY 'NEW_SECURE_PASSWORD_HERE';

-- Grant same permissions as old user
GRANT ALL PRIVILEGES ON buildnest_ecommerce.* TO 'buildnest_user_v2'@'%';
FLUSH PRIVILEGES;

-- Verify new user works
-- (Test connection with new credentials)
```

#### Step 2: Dual-User Configuration (Zero Downtime)

Update application to accept both old and new credentials:

```yaml
# kubernetes/buildnest-secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: buildnest-secrets
  namespace: buildnest
stringData:
  database.username: "buildnest_user"          # Old user
  database.password: "OLD_PASSWORD"            # Old password
  database.username.new: "buildnest_user_v2"   # New user (for testing)
  database.password.new: "NEW_PASSWORD"        # New password
```

#### Step 3: Test New Credentials

```bash
# Test database connection with new credentials
kubectl run mysql-test --rm -i --restart=Never --image=mysql:8.2.0 -- \
  mysql -h buildnest-mysql -u buildnest_user_v2 -pNEW_PASSWORD \
  -e "SELECT 1 FROM users LIMIT 1;"

# Expected: Query returns result without error
```

#### Step 4: Update Application Configuration

```bash
# Update Kubernetes secret to use new credentials
kubectl create secret generic buildnest-secrets \
  --from-literal=database.username="buildnest_user_v2" \
  --from-literal=database.password="NEW_PASSWORD" \
  --namespace=buildnest \
  --dry-run=client -o yaml | kubectl apply -f -
```

#### Step 5: Rolling Restart (60-Minute Grace Period)

```bash
# Restart application with new credentials
kubectl rollout restart deployment/buildnest-app -n buildnest

# Monitor for database connection errors
kubectl logs -f deployment/buildnest-app -n buildnest | grep -i "database\|connection"
```

During this period:
- ✅ New pods use new credentials
- ✅ Old pods still use old credentials (during rolling update)
- ⏱️ All pods will be on new credentials within 5-10 minutes

#### Step 6: Revoke Old User (After Grace Period)

After 60 minutes and verifying stability:

```sql
-- Revoke access from old user
REVOKE ALL PRIVILEGES ON buildnest_ecommerce.* FROM 'buildnest_user'@'%';
DROP USER 'buildnest_user'@'%';
FLUSH PRIVILEGES;

-- Verify old user removed
SELECT User, Host FROM mysql.user WHERE User LIKE 'buildnest%';
```

#### Step 7: Verification

```bash
# Verify application is healthy
curl https://api.buildnest.com/actuator/health | jq '.components.db.status'
# Expected: "UP"

# Test database operations
curl -X POST https://api.buildnest.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}' 
# Expected: 200 OK with token
```

---

## REDIS PASSWORD ROTATION

### Rotation Process

#### Step 1: Update Redis Configuration

```bash
# For Redis standalone
redis-cli CONFIG SET requirepass "NEW_REDIS_PASSWORD"
redis-cli -a NEW_REDIS_PASSWORD PING
# Expected: PONG

# For Redis Sentinel/Cluster, update all nodes
```

#### Step 2: Update Application Secret

```bash
kubectl create secret generic buildnest-secrets \
  --from-literal=redis.password="NEW_REDIS_PASSWORD" \
  --namespace=buildnest \
  --dry-run=client -o yaml | kubectl apply -f -
```

#### Step 3: Rolling Restart

```bash
kubectl rollout restart deployment/buildnest-app -n buildnest
```

#### Step 4: Verification

```bash
# Check Redis connectivity
kubectl exec -it deployment/buildnest-app -n buildnest -- \
  curl http://localhost:8081/actuator/health | jq '.components.redis.status'
# Expected: "UP"
```

---

## API KEY ROTATION

### Razorpay Keys

1. Log in to Razorpay Dashboard: https://dashboard.razorpay.com/app/keys
2. Generate new key pair (Key ID + Key Secret)
3. Update Kubernetes secret:
   ```bash
   kubectl create secret generic buildnest-secrets \
     --from-literal=razorpay.key.id="NEW_KEY_ID" \
     --from-literal=razorpay.key.secret="NEW_KEY_SECRET" \
     --namespace=buildnest \
     --dry-run=client -o yaml | kubectl apply -f -
   ```
4. Restart application
5. Test payment flow end-to-end
6. Deactivate old keys in Razorpay dashboard

---

## SSL CERTIFICATE RENEWAL

### Automated Renewal (cert-manager)

cert-manager automatically renews certificates 30 days before expiration.

**Verification**:
```bash
# Check certificate expiry
kubectl get certificate -n buildnest
kubectl describe certificate buildnest-cert -n buildnest

# Check cert-manager logs
kubectl logs -n cert-manager deployment/cert-manager
```

### Manual Renewal (If Automation Fails)

```bash
# Force certificate renewal
kubectl delete certificate buildnest-cert -n buildnest
kubectl apply -f kubernetes/letsencrypt-issuer.yaml

# Monitor renewal
kubectl describe certificate buildnest-cert -n buildnest
```

---

## EMERGENCY ROTATION

### When to Perform Emergency Rotation

- ✅ Secret exposed in logs or error messages
- ✅ Secret committed to public repository
- ✅ Team member with access to secrets leaves company
- ✅ Security audit identifies compromised credentials
- ✅ Suspicious authentication activity detected

### Emergency JWT Secret Rotation (15-Minute Process)

```bash
# 1. Generate new secret (2 min)
NEW_SECRET=$(openssl rand -base64 64)

# 2. Update Kubernetes secret (1 min)
kubectl create secret generic buildnest-secrets \
  --from-literal=jwt.secret="$NEW_SECRET" \
  --dry-run=client -o yaml | kubectl apply -f -

# 3. Force immediate restart (5 min)
kubectl rollout restart deployment/buildnest-app -n buildnest
kubectl rollout status deployment/buildnest-app -n buildnest --timeout=5m

# 4. Revoke all existing JWT tokens (immediate)
# Option A: Clear Redis cache (faster)
kubectl exec -it redis-0 -n buildnest -- redis-cli FLUSHDB

# Option B: Increment JWT version in database
mysql -e "UPDATE users SET jwt_version = jwt_version + 1;"

# 5. Notify users (2 min)
# Send email: "For security reasons, please log in again"

# 6. Monitor (5 min)
kubectl logs -f deployment/buildnest-app -n buildnest | grep "401\|403"
```

**Impact**: All users logged out immediately. Acceptable trade-off for security incident.

---

## AUDIT & COMPLIANCE

### Rotation Log Template

Maintain audit log in `secrets-rotation-log.md`:

```markdown
| Date | Secret Type | Rotated By | Reason | Downtime | Issues |
|------|-------------|------------|--------|----------|--------|
| 2026-01-31 | JWT Secret | DevOps Team | Scheduled (90-day) | 0 min | None |
| 2026-01-15 | Database Password | DBA | Scheduled (180-day) | 5 min | None |
```

### Compliance Checklist

- [ ] All secrets rotated per schedule
- [ ] Rotation documented in audit log
- [ ] Old secrets properly revoked
- [ ] No secrets in version control
- [ ] No secrets in application logs
- [ ] Access to secrets restricted (RBAC)
- [ ] Secrets stored encrypted at rest
- [ ] Annual security audit passed

### Automated Monitoring

Set up alerts for:
- Secrets older than rotation schedule
- Certificate expiration < 30 days
- Failed authentication rate spike (possible compromised secret)

---

## TOOLS & SCRIPTS

### Secret Expiry Checker

```powershell
# check-secret-expiry.ps1
param([string]$Namespace = "buildnest")

$secrets = kubectl get secret -n $Namespace -o json | ConvertFrom-Json

foreach ($secret in $secrets.items) {
    $created = [datetime]$secret.metadata.creationTimestamp
    $age = (Get-Date) - $created
    
    if ($age.Days -gt 90) {
        Write-Warning "Secret $($secret.metadata.name) is $($age.Days) days old - ROTATE NOW"
    }
}
```

---

## SUPPORT & ESCALATION

**For Issues During Rotation**:
1. Immediately rollback using backup
2. Alert on-call engineer (PagerDuty)
3. Document incident
4. Schedule post-mortem

**Contacts**:
- **Security Team**: security@buildnest.com
- **DevOps On-Call**: +1-555-DEVOPS
- **Database Admin**: dba@buildnest.com

---

**✅ CRITICAL BLOCKER #5 RESOLVED**

This document provides comprehensive secret rotation procedures with zero-downtime strategies, emergency protocols, and compliance requirements.
