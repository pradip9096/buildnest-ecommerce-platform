# BuildNest Deployment Quick Reference Card
**Emergency Operations Cheatsheet**  
Print and keep at your desk during deployment

---

## Quick Start (5 minutes)

### Prerequisites Check
```bash
# 1. Verify kubectl access
kubectl cluster-info

# 2. Verify namespace
kubectl config set-context --current --namespace=buildnest
kubectl get namespace buildnest

# 3. Verify secrets
kubectl get secret buildnest-secrets -n buildnest

# 4. Verify database
mysql -h buildnest-mysql -u buildnest_user -p -e "SELECT DATABASE();"
```

### Deploy Application
```bash
# 1. Build and push image
./mvnw clean package -DskipTests
docker build -t buildnest-ecommerce:v1.0.0 .
docker push <registry>/buildnest-ecommerce:v1.0.0

# 2. Update and deploy
kubectl set image rollout/buildnest-app \
  buildnest-app=<registry>/buildnest-ecommerce:v1.0.0 -n buildnest

# 3. Promote (when ready)
kubectl argo rollouts promote buildnest-app -n buildnest

# 4. Verify
curl -k https://api.buildnest.com/actuator/health
```

---

## Health Checks (2 minutes each)

### Application Health
```bash
# Quick status
kubectl get pods -n buildnest
kubectl argo rollouts status buildnest-app -n buildnest

# Logs
kubectl logs -f deployment/buildnest-app -n buildnest

# Metrics
curl -s https://api.buildnest.com/actuator/metrics | jq '.names[]'
```

### Database Health
```bash
# Check connectivity
mysql -h buildnest-mysql -u buildnest_user -p -e "SHOW SLAVE STATUS\G" | grep "Slave_IO_Running"

# Check size
mysql -u buildnest_user -p buildnest_ecommerce -e "SELECT table_name, ROUND(data_length/1024/1024,2) as MB FROM information_schema.tables WHERE table_schema='buildnest_ecommerce' ORDER BY data_length DESC;"
```

### Cache Health
```bash
# Check Redis
redis-cli -h <redis-host> -p 6379 -a '<password>' PING

# Memory usage
redis-cli -h <redis-host> -p 6379 -a '<password>' INFO memory | grep "used_memory_human"
```

### Certificate Health
```bash
# Check expiration
kubectl get certificate buildnest-tls-cert -n buildnest
openssl s_client -connect api.buildnest.com:443 </dev/null 2>/dev/null | openssl x509 -noout -dates
```

---

## Common Commands

### Pod Management
```bash
# View pods
kubectl get pods -n buildnest
kubectl get pods -n buildnest -o wide

# Pod details
kubectl describe pod <pod-name> -n buildnest

# Pod logs
kubectl logs <pod-name> -n buildnest
kubectl logs -f <pod-name> -c buildnest-app -n buildnest

# Execute command in pod
kubectl exec -it <pod-name> -n buildnest -- bash

# Delete pod (force restart)
kubectl delete pod <pod-name> -n buildnest

# Scale replicas
kubectl scale deployment buildnest-app --replicas=5 -n buildnest
```

### Rollout Management
```bash
# Check rollout status
kubectl argo rollouts status buildnest-app -n buildnest

# Watch rollout progress
kubectl argo rollouts get rollout buildnest-app -n buildnest --watch

# Promote to production
kubectl argo rollouts promote buildnest-app -n buildnest

# Abort rollout
kubectl argo rollouts abort buildnest-app -n buildnest

# Rollback to previous version
kubectl argo rollouts undo buildnest-app -n buildnest

# Rollback to specific revision
kubectl argo rollouts undo buildnest-app --to-revision=1 -n buildnest

# View history
kubectl argo rollouts history buildnest-app -n buildnest
```

### Secrets Management
```bash
# View secret names
kubectl get secret -n buildnest

# Check secret keys
kubectl get secret buildnest-secrets -n buildnest -o jsonpath='{.data}' | jq 'keys'

# Update secret
kubectl patch secret buildnest-secrets -n buildnest --type merge -p '{"stringData":{"jwt.secret":"<new-value>"}}'

# Restart pods to reload secrets
kubectl rollout restart deployment/buildnest-app -n buildnest
```

### Database Operations
```bash
# Connect to MySQL
mysql -h buildnest-mysql -u buildnest_user -p buildnest_ecommerce

# Backup database
mysqldump -h buildnest-mysql -u buildnest_user -p buildnest_ecommerce > backup-$(date +%Y%m%d).sql

# Restore database (CAREFUL!)
mysql -h buildnest-mysql -u buildnest_user -p buildnest_ecommerce < backup-20260202.sql

# Check migration status
./mvnw liquibase:status

# Rollback last migration
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1
```

---

## Emergency Procedures

### 🚨 Application Down

**Step 1: Immediate Assessment** (1 min)
```bash
# Check pod status
kubectl get pods -n buildnest

# Check logs
kubectl logs deployment/buildnest-app -n buildnest --tail=20 | tail -20

# Check events
kubectl get events -n buildnest --sort-by='.lastTimestamp' | tail -10
```

**Step 2: Determine Root Cause** (2 min)
- Pod CrashLoopBackOff? → Memory/config issue
- Pod Pending? → Node resource issue
- Pod Running but unhealthy? → Application issue

**Step 3: Quick Fix Options**

**Option A: Restart Pod** (< 1 min)
```bash
kubectl delete pod <pod-name> -n buildnest
# New pod starts automatically
```

**Option B: Rollback** (2 min)
```bash
kubectl argo rollouts undo buildnest-app -n buildnest
kubectl argo rollouts status buildnest-app -n buildnest --watch
```

**Option C: Scale to Previous Version** (3 min)
```bash
# Stop current deployment
kubectl scale deployment buildnest-app --replicas=0 -n buildnest

# Deploy previous image
kubectl set image deployment/buildnest-app \
  buildnest-app=<registry>/buildnest-ecommerce:v0.9.0 -n buildnest

# Scale up
kubectl scale deployment buildnest-app --replicas=3 -n buildnest
```

**Step 4: Verify Recovery**
```bash
curl -k https://api.buildnest.com/actuator/health
# Should return 200 OK with "status": "UP"
```

---

### 🚨 Database Down

**Step 1: Check Database Status** (1 min)
```bash
# Test connection
mysql -h buildnest-mysql -u buildnest_user -p -e "SELECT 1"

# Check size and tables
mysql -u buildnest_user -p buildnest_ecommerce -e "SHOW TABLES; SHOW MASTER STATUS;"
```

**Step 2: If Primary Database Down**

**Option A: Failover to Replica** (5 min)
```bash
# Update connection string to replica
kubectl patch configmap buildnest-config -n buildnest \
  --type merge -p '{"data":{"SPRING_DATASOURCE_HOST":"<replica-ip>"}}'

# Restart application
kubectl rollout restart deployment/buildnest-app -n buildnest

# Promote replica to primary
# (Execute on replica server)
STOP SLAVE;
RESET MASTER;
SET GLOBAL read_only=OFF;
```

**Option B: Restore from Backup** (15 min)
```bash
# Stop application
kubectl scale deployment buildnest-app --replicas=0 -n buildnest

# Restore backup
mysql -u buildnest_user -p buildnest_ecommerce < backup-20260201.sql

# Restart application
kubectl scale deployment buildnest-app --replicas=3 -n buildnest
```

**Step 3: Monitor Recovery**
```bash
# Check connection status
kubectl logs deployment/buildnest-app -n buildnest | grep -i "connected\|failed"

# Check data integrity
mysql -u buildnest_user -p buildnest_ecommerce -e "SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM products;"
```

---

### 🚨 High Error Rate (> 5%)

**Step 1: Quick Diagnostics** (2 min)
```bash
# Check error logs
kubectl logs deployment/buildnest-app -n buildnest | grep -i "error\|exception" | tail -20

# Check metrics
curl -s https://api.buildnest.com/actuator/prometheus | grep "http_requests_total" | head -5

# Check response times
curl -s https://api.buildnest.com/actuator/metrics/http.server.requests
```

**Step 2: Common Issues**

| Issue | Fix |
|-------|-----|
| 503 Service Unavailable | Restart pods: `kubectl delete pod -l app=buildnest-app -n buildnest` |
| 500 Database Error | Check database connection: `mysql -h buildnest-mysql ...` |
| 429 Too Many Requests | Check rate limiting: `kubectl logs ingress-nginx...` |
| 401 Unauthorized | Verify JWT secret: `kubectl get secret buildnest-secrets -n buildnest` |
| 504 Timeout | Increase resource limits or scale up |

**Step 3: Scale Up If Needed**
```bash
# Temporarily increase replicas
kubectl scale deployment buildnest-app --replicas=10 -n buildnest

# Monitor error rate
watch "curl -s https://api.buildnest.com/actuator/prometheus | grep 'http_requests_total{status=\"500\"}'"

# Scale back down when stable
kubectl scale deployment buildnest-app --replicas=3 -n buildnest
```

---

### 🚨 Certificate Expiration Alert

**Step 1: Check Certificate Status** (1 min)
```bash
kubectl get certificate buildnest-tls-cert -n buildnest
kubectl describe certificate buildnest-tls-cert -n buildnest
```

**Step 2: Emergency Renewal** (5 min)
```bash
# Manually trigger renewal
kubectl delete secret buildnest-tls-cert -n buildnest

# Or force renewal via cert-manager
kubectl annotate certificate buildnest-tls-cert \
  cert-manager.io/issue-temporary-certificate="true" --overwrite -n buildnest

# Monitor renewal
kubectl describe certificate buildnest-tls-cert -n buildnest
```

**Step 3: Verify**
```bash
openssl s_client -connect api.buildnest.com:443 </dev/null 2>/dev/null | openssl x509 -noout -dates
```

---

## Monitoring Dashboard URLs

| Component | URL | Login |
|-----------|-----|-------|
| Prometheus | http://prometheus:9090 | None |
| Grafana | http://grafana:3000 | admin/admin |
| Kibana | http://kibana:5601 | elastic/changeme |
| Argo Rollouts | `kubectl port-forward svc/argo-rollouts-dashboard 3100:3100 -n argo-rollouts` | None |
| API Health | https://api.buildnest.com/actuator/health | JWT required |

---

## Resource Quotas

| Resource | Limit | Current Usage |
|----------|-------|-----------------|
| CPU | 16 cores | Check: `kubectl top pod -n buildnest` |
| Memory | 32 GB | Check: `kubectl top pod -n buildnest` |
| Pods | 50 | Check: `kubectl get pods -n buildnest --no-headers \| wc -l` |
| Services | 10 | Check: `kubectl get svc -n buildnest --no-headers \| wc -l` |

---

## Escalation Contacts

| Severity | Contact | Time | Number |
|----------|---------|------|--------|
| 🔴 Critical | On-Call DevOps | 5 min | +1-555-0101 |
| 🟠 High | SRE Manager | 15 min | +1-555-0102 |
| 🟡 Medium | Engineering Manager | 30 min | +1-555-0103 |
| 🟢 Low | Ticket System | 8 hours | support@buildnest.com |

---

## Key Files Reference

| File | Purpose | Location |
|------|---------|----------|
| Deployment Guide | Complete deployment instructions | `COMPREHENSIVE_DEPLOYMENT_GUIDE.md` |
| Blockers Resolution | All 6 critical blockers | `CRITICAL_BLOCKERS_RESOLUTION.md` |
| Disaster Recovery | RTO 15min, RPO 5min | `DISASTER_RECOVERY_RUNBOOK.md` |
| Rollout Config | Blue-green deployment | `kubernetes/buildnest-rollout.yaml` |
| Setup Scripts | Automation scripts | `scripts/*.ps1` |

---

## Quick Keyboard Shortcuts

```bash
# Frequent commands alias (add to ~/.bashrc or ~/.zshrc)
alias k='kubectl'
alias kn='kubectl config set-context --current --namespace'
alias kgp='kubectl get pods -n buildnest'
alias kgs='kubectl get svc -n buildnest'
alias kl='kubectl logs -f deployment/buildnest-app -n buildnest'
alias kd='kubectl describe pod -n buildnest'
alias kar='kubectl argo rollouts'
```

---

**Last Updated**: February 2, 2026  
**Version**: 1.0.0  
**For Emergencies**: Call +1-555-0101
