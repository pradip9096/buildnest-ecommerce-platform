# DISASTER RECOVERY RUNBOOK
## CRITICAL BLOCKER #6 FIX: BuildNest E-Commerce Platform

**Document Version**: 1.0  
**Last Updated**: January 31, 2026  
**Owner**: DevOps / SRE Team  
**Review Frequency**: Monthly  
**Emergency Contact**: +1-555-DEVOPS

---

## 🚨 EMERGENCY CONTACTS

| Role | Name | Phone | Email | Availability |
|------|------|-------|-------|--------------|
| **DevOps Lead** | [NAME] | +1-555-DEV-001 | devops-lead@buildnest.com | 24/7 |
| **Database Admin** | [NAME] | +1-555-DBA-001 | dba@buildnest.com | 24/7 |
| **Security Lead** | [NAME] | +1-555-SEC-001 | security@buildnest.com | Business Hours |
| **Product Owner** | [NAME] | +1-555-PRD-001 | product@buildnest.com | Business Hours |
| **CEO** | [NAME] | +1-555-CEO-001 | ceo@buildnest.com | Critical Only |

**Escalation Chain**: DevOps → Database Admin → DevOps Lead → CEO

---

## 📋 RECOVERY OBJECTIVES

### Service Level Objectives (SLO)

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Availability** | 99.9% | 43.2 minutes downtime/month |
| **RTO (Recovery Time)** | 15 minutes | Time from incident to service restored |
| **RPO (Recovery Point)** | 5 minutes | Maximum data loss acceptable |
| **MTTR (Mean Time to Recovery)** | 30 minutes | Average recovery time |

### Critical Services

1. **API Gateway** (Priority 1) - Customer-facing endpoints
2. **Database** (Priority 1) - All persistent data
3. **Redis Cache** (Priority 2) - Session and cache data
4. **Payment Gateway** (Priority 1) - Transaction processing
5. **Elasticsearch** (Priority 3) - Logging and analytics

---

## 🔍 INCIDENT DETECTION

### Automated Alerts

Monitor these Prometheus alerts:

```yaml
# High Error Rate
- alert: HighErrorRate
  expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
  severity: critical
  
# Service Down
- alert: ServiceDown
  expr: up{job="buildnest-app"} == 0
  severity: critical
  
# Database Connectivity
- alert: DatabaseDown
  expr: mysql_up == 0
  severity: critical
  
# High Response Time
- alert: HighLatency
  expr: http_request_duration_seconds{quantile="0.95"} > 2
  severity: warning
```

### Manual Detection

Users may report:
- "Website not loading"
- "Payment failed"
- "Cannot log in"
- "Slow performance"

**First Response**: Acknowledge incident in PagerDuty within 2 minutes.

---

## SCENARIO 1: DATABASE FAILURE

### 🔴 CRITICAL - RTO: 15 minutes | RPO: 5 minutes

### Symptoms
- Application errors: "Connection refused" or "Too many connections"
- Health check failing: `/actuator/health/db` returns DOWN
- Database pod in CrashLoopBackOff

### Detection
```bash
# Check database health
kubectl get pods -n buildnest | grep mysql
kubectl logs -f mysql-0 -n buildnest --tail=100

# Check application logs
kubectl logs -f deployment/buildnest-app -n buildnest | grep -i "database\|mysql"

# Check database connectivity
kubectl exec -it mysql-0 -n buildnest -- mysql -u root -p -e "SELECT 1;"
```

### Root Cause Analysis
1. **Disk Full**: Check PVC usage
2. **Memory Pressure**: Check pod resources
3. **Corrupted Data**: Check MySQL error log
4. **Network Issue**: Check service/endpoint

### Recovery Steps

#### Step 1: Immediate Triage (2 minutes)

```bash
# Check database pod status
kubectl describe pod mysql-0 -n buildnest

# Check persistent volume
kubectl get pvc -n buildnest
kubectl describe pvc mysql-pvc -n buildnest

# Check database error logs
kubectl logs mysql-0 -n buildnest --previous
```

#### Step 2: Attempt Quick Restart (3 minutes)

```bash
# Restart database pod
kubectl delete pod mysql-0 -n buildnest

# Wait for pod to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n buildnest --timeout=180s

# Test connectivity
kubectl exec -it mysql-0 -n buildnest -- mysql -u root -p -e "SELECT 1;"
```

**If successful**: Skip to Step 7 (Verification)

#### Step 3: Failover to Standby (5 minutes)

**Option A: Promote Read Replica**
```bash
# If using MySQL replication
kubectl exec -it mysql-standby -n buildnest -- mysql -u root -p

# On standby MySQL
STOP SLAVE;
RESET SLAVE ALL;

# Update application to point to standby
kubectl set env deployment/buildnest-app \
  SPRING_DATASOURCE_URL="jdbc:mysql://mysql-standby:3306/buildnest_ecommerce" \
  -n buildnest
```

**Option B: Restore from Backup**
```bash
# List available backups
aws s3 ls s3://buildnest-backups/mysql/ --recursive | grep dump

# Download latest backup
aws s3 cp s3://buildnest-backups/mysql/buildnest-2026-01-31-0200.sql.gz /tmp/

# Restore to new database instance
gunzip < /tmp/buildnest-2026-01-31-0200.sql.gz | \
  kubectl exec -i mysql-0 -n buildnest -- mysql -u root -p buildnest_ecommerce
```

#### Step 4: Run Liquibase Migrations (2 minutes)

```bash
# Apply any pending migrations
kubectl exec -it deployment/buildnest-app -n buildnest -- \
  /app/mvnw liquibase:update
```

#### Step 5: Verify Data Integrity (2 minutes)

```bash
# Check table counts
kubectl exec -it mysql-0 -n buildnest -- mysql -u root -p -e "
  USE buildnest_ecommerce;
  SELECT 'users' as table_name, COUNT(*) as count FROM users
  UNION ALL
  SELECT 'products', COUNT(*) FROM product
  UNION ALL
  SELECT 'orders', COUNT(*) FROM orders;
"

# Check latest records
kubectl exec -it mysql-0 -n buildnest -- mysql -u root -p -e "
  USE buildnest_ecommerce;
  SELECT created_at, COUNT(*) 
  FROM orders 
  WHERE created_at > NOW() - INTERVAL 1 HOUR 
  GROUP BY created_at;
"
```

#### Step 6: Restart Application (2 minutes)

```bash
# Rolling restart to reconnect to database
kubectl rollout restart deployment/buildnest-app -n buildnest

# Wait for rollout
kubectl rollout status deployment/buildnest-app -n buildnest --timeout=120s
```

#### Step 7: Verification (3 minutes)

```bash
# Check application health
curl https://api.buildnest.com/actuator/health | jq '.components.db.status'
# Expected: "UP"

# Test database operations
curl -X POST https://api.buildnest.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'
# Expected: 200 OK with JWT token

# Test order creation
curl -X POST https://api.buildnest.com/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}]}'
# Expected: 201 Created
```

### Post-Recovery Actions

1. **Document Data Loss Window**:
   ```bash
   # Check last successful transaction before failure
   kubectl exec -it mysql-0 -n buildnest -- mysql -u root -p -e "
     SELECT MAX(created_at) FROM orders;
   "
   # Compare with backup timestamp
   ```

2. **Enable Binary Logging** (if not already):
   ```sql
   # Edit MySQL config
   [mysqld]
   log_bin = /var/log/mysql/mysql-bin.log
   expire_logs_days = 7
   max_binlog_size = 100M
   ```

3. **Schedule Post-Mortem** within 24 hours

### Rollback Procedure

If recovery fails:
1. Revert to previous database backup
2. Accept data loss for time window
3. Notify users of data loss period
4. Investigate root cause before retry

---

## SCENARIO 2: APPLICATION DEPLOYMENT FAILURE

### 🟡 HIGH - RTO: 10 minutes | RPO: 0 minutes

### Symptoms
- New deployment stuck in "Progressing" state
- Pods in CrashLoopBackOff or ImagePullBackOff
- Health checks failing on new version
- Increasing error rate in production

### Detection
```bash
# Check rollout status
kubectl rollout status deployment/buildnest-app -n buildnest

# Check pod events
kubectl get events -n buildnest --sort-by='.lastTimestamp' | tail -20

# Check pod logs
kubectl logs -f deployment/buildnest-app -n buildnest --all-containers=true
```

### Recovery Steps

#### Option 1: Abort Blue-Green Deployment (Argo Rollouts)

```bash
# Abort rollout (keeps old version active)
kubectl argo rollouts abort buildnest-app -n buildnest

# Verify old version still serving traffic
kubectl get svc buildnest-active -n buildnest -o jsonpath='{.spec.selector}'

# Scale down new version
kubectl argo rollouts undo buildnest-app -n buildnest
```

#### Option 2: Rollback Standard Deployment

```bash
# Check deployment history
kubectl rollout history deployment/buildnest-app -n buildnest

# Rollback to previous version
kubectl rollout undo deployment/buildnest-app -n buildnest

# Or rollback to specific revision
kubectl rollout undo deployment/buildnest-app --to-revision=3 -n buildnest

# Watch rollback progress
kubectl rollout status deployment/buildnest-app -n buildnest --watch
```

#### Option 3: Emergency Rollback (Manual Image Change)

```bash
# Set image to last known good version
kubectl set image deployment/buildnest-app \
  buildnest-app=buildnest/ecommerce:v1.0.5 \
  -n buildnest

# Force immediate rollout
kubectl rollout restart deployment/buildnest-app -n buildnest
```

### Verification

```bash
# Check all pods are running
kubectl get pods -n buildnest -l app=buildnest

# Test application endpoints
curl https://api.buildnest.com/actuator/health
curl https://api.buildnest.com/api/products?page=0&size=10

# Check error rate
curl http://prometheus:9090/api/v1/query?query=rate(http_requests_total{status=~"5.."}[5m])
```

### Post-Recovery

1. Analyze why new version failed
2. Fix issues in development
3. Re-run full test suite before next deployment
4. Consider adding more pre-deployment smoke tests

---

## SCENARIO 3: REDIS CLUSTER FAILURE

### 🟡 MEDIUM - RTO: 10 minutes | RPO: Acceptable (cache data)

### Symptoms
- Degraded application performance
- Session loss (users logged out)
- Rate limiting not working
- Cache miss rate 100%

### Detection
```bash
# Check Redis health
kubectl get pods -n buildnest | grep redis
kubectl logs -f redis-0 -n buildnest

# Check Redis connectivity
kubectl exec -it redis-0 -n buildnest -- redis-cli PING
```

### Recovery Steps

#### Step 1: Restart Redis Pod

```bash
kubectl delete pod redis-0 -n buildnest
kubectl wait --for=condition=ready pod -l app=redis -n buildnest --timeout=120s
```

#### Step 2: Verify Redis is Accessible

```bash
kubectl exec -it redis-0 -n buildnest -- redis-cli PING
# Expected: PONG

kubectl exec -it redis-0 -n buildnest -- redis-cli INFO | grep connected_clients
```

#### Step 3: Application Auto-Reconnects

Application should auto-reconnect due to Resilience4j circuit breaker:
```properties
resilience4j.circuitbreaker.instances.redis-circuit-breaker.wait-duration-in-open-state=60000
```

#### Step 4: Clear Circuit Breaker (if needed)

```bash
# Restart application to reset circuit breaker
kubectl rollout restart deployment/buildnest-app -n buildnest
```

### Verification

```bash
# Check cache is working
curl https://api.buildnest.com/api/categories | jq length
curl https://api.buildnest.com/api/categories | jq length  # Should be faster (cached)

# Check rate limiting
for i in {1..10}; do 
  curl -o /dev/null -s -w "%{http_code}\n" https://api.buildnest.com/api/auth/login \
    -X POST -d '{"username":"test","password":"wrong"}'
done
# Expected: 429 after 3 attempts
```

### Impact Assessment

- **No data loss** (cache can be rebuilt)
- **User sessions lost** (users must log in again)
- **Performance degraded** until cache warms up (5-10 minutes)

---

## SCENARIO 4: COMPLETE CLUSTER FAILURE

### 🔴 CRITICAL - RTO: 60 minutes | RPO: 5 minutes

### Symptoms
- All pods unreachable
- Kubernetes API server not responding
- Entire region/availability zone down

### Recovery Steps

#### Option 1: Failover to DR Cluster (if configured)

```bash
# Switch DNS to DR cluster
aws route53 change-resource-record-sets \
  --hosted-zone-id Z1234567890ABC \
  --change-batch file://dns-failover-to-dr.json

# Verify DR cluster is healthy
kubectl --context=dr-cluster get pods -n buildnest

# Start traffic to DR cluster
# Expected: Service restored in 15 minutes
```

#### Option 2: Rebuild Cluster from Scratch

**Time Estimate**: 60 minutes

1. **Provision new Kubernetes cluster** (20 min)
   ```bash
   terraform apply -var="environment=disaster-recovery"
   ```

2. **Deploy infrastructure** (15 min)
   ```bash
   kubectl apply -f kubernetes/namespace.yaml
   kubectl apply -f kubernetes/buildnest-secrets.yaml
   kubectl apply -f kubernetes/mysql-statefulset.yaml
   kubectl apply -f kubernetes/redis-statefulset.yaml
   ```

3. **Restore database from backup** (15 min)
   ```bash
   aws s3 cp s3://buildnest-backups/mysql/latest.sql.gz /tmp/
   gunzip < /tmp/latest.sql.gz | kubectl exec -i mysql-0 -n buildnest -- mysql -u root -p
   ```

4. **Deploy application** (10 min)
   ```bash
   kubectl apply -f kubernetes/buildnest-deployment.yaml
   kubectl apply -f kubernetes/buildnest-service.yaml
   kubectl apply -f kubernetes/buildnest-ingress.yaml
   ```

5. **Update DNS** to point to new cluster

### Verification

Full end-to-end testing:
```bash
# Test authentication
curl -X POST https://api.buildnest.com/api/auth/login

# Test product search
curl https://api.buildnest.com/api/products/search?q=hammer

# Test order creation
curl -X POST https://api.buildnest.com/api/orders

# Monitor error rate for 30 minutes
```

---

## SCENARIO 5: SECURITY BREACH

### 🔴 CRITICAL - Immediate Action Required

### Symptoms
- Unauthorized access detected
- Secrets leaked in logs or public repository
- Suspicious database queries
- Unusual API traffic patterns

### Immediate Actions (5 minutes)

1. **Isolate affected services**:
   ```bash
   # Scale down compromised deployment
   kubectl scale deployment/buildnest-app --replicas=0 -n buildnest
   ```

2. **Rotate ALL secrets** (see SECRET_ROTATION_PROCEDURES.md):
   ```bash
   # Emergency JWT rotation
   kubectl create secret generic buildnest-secrets \
     --from-literal=jwt.secret="$(openssl rand -base64 64)" \
     --dry-run=client -o yaml | kubectl apply -f -
   
   # Revoke all active sessions
   kubectl exec -it redis-0 -n buildnest -- redis-cli FLUSHALL
   ```

3. **Enable audit logging**:
   ```bash
   kubectl logs -f deployment/buildnest-app -n buildnest > security-incident-$(date +%Y%m%d-%H%M%S).log
   ```

4. **Notify security team** immediately

5. **Preserve evidence**:
   ```bash
   kubectl get events -n buildnest > events-$(date +%Y%m%d).log
   kubectl logs deployment/buildnest-app -n buildnest --all-containers > logs-$(date +%Y%m%d).log
   ```

### Investigation

- Review audit logs for suspicious activity
- Check database for data exfiltration
- Review IAM permissions and access logs
- Engage external security consultant if needed

---

## BACKUP & RESTORE PROCEDURES

### Automated Backups

#### Database Backups

```bash
# Backup schedule (via CronJob)
# Daily full backup: 2:00 AM UTC
# Hourly incremental: Every hour
# Retention: 30 days full, 7 days incremental

# Manual backup
kubectl exec -it mysql-0 -n buildnest -- \
  mysqldump -u root -p buildnest_ecommerce \
  --single-transaction --routines --triggers \
  | gzip > buildnest-backup-$(date +%Y%m%d-%H%M%S).sql.gz

# Upload to S3
aws s3 cp buildnest-backup-*.sql.gz s3://buildnest-backups/mysql/
```

#### Application Configuration Backups

```bash
# Backup all Kubernetes resources
kubectl get all,cm,secret -n buildnest -o yaml > k8s-backup-$(date +%Y%m%d).yaml

# Backup Terraform state
terraform state pull > terraform-state-backup-$(date +%Y%m%d).json
```

### Restore Procedures

#### Restore Database

```bash
# Download backup
aws s3 cp s3://buildnest-backups/mysql/buildnest-2026-01-31-0200.sql.gz /tmp/

# Restore to database
gunzip < /tmp/buildnest-2026-01-31-0200.sql.gz | \
  kubectl exec -i mysql-0 -n buildnest -- mysql -u root -p buildnest_ecommerce

# Run migrations to catch up to current schema
kubectl exec -it deployment/buildnest-app -n buildnest -- \
  ./mvnw liquibase:update
```

#### Point-in-Time Recovery (if binary logs enabled)

```bash
# Restore full backup
# Then apply binary logs from backup time to desired point

# Find binary log position
kubectl exec -it mysql-0 -n buildnest -- mysql -u root -p -e "SHOW MASTER STATUS;"

# Apply binary logs
kubectl exec -it mysql-0 -n buildnest -- \
  mysqlbinlog /var/log/mysql/mysql-bin.000001 \
  --start-position=12345 --stop-position=67890 | \
  mysql -u root -p buildnest_ecommerce
```

---

## COMMUNICATION TEMPLATES

### Incident Notification (Internal)

```
SUBJECT: [P1] BuildNest Production Incident - Database Failure

INCIDENT: Database failure detected at 14:32 UTC
STATUS: Investigating
IMPACT: Full service outage
ETA: 15 minutes to recovery
ACTIONS: Attempting database restart, prepared to restore from backup
NEXT UPDATE: 14:45 UTC

Incident Commander: [Your Name]
War Room: https://zoom.us/j/emergency
```

### Customer Communication

```
SUBJECT: BuildNest Service Disruption - We're Working on It

Dear BuildNest Customers,

We're currently experiencing technical difficulties affecting our website and mobile app. Our engineering team is actively working to restore service.

IMPACT: Unable to place orders or access account
ESTIMATED RESOLUTION: 15 minutes
STATUS UPDATES: https://status.buildnest.com

We sincerely apologize for the inconvenience.

BuildNest Team
```

### Post-Incident Report Template

```markdown
# Post-Incident Report - [Date]

## Incident Summary
- **Date**: YYYY-MM-DD
- **Duration**: X minutes
- **Severity**: P1/P2/P3
- **Impact**: [Description]

## Timeline
- 14:30 - Incident detected
- 14:32 - Team alerted
- 14:35 - Root cause identified
- 14:45 - Fix deployed
- 14:50 - Service restored
- 15:00 - Monitoring confirmed stable

## Root Cause
[Detailed technical explanation]

## Resolution
[Steps taken to resolve]

## Data Loss
- RPO Achievement: 5 minutes (target: 5 minutes) ✅
- Orders affected: 23
- Data recovery: 100%

## Action Items
- [ ] Improve monitoring for early detection
- [ ] Add automated failover
- [ ] Update runbook with lessons learned
- [ ] Schedule team training

## Sign-Off
- DevOps Lead: [Name]
- DBA: [Name]
- Product Owner: [Name]
```

---

## DISASTER RECOVERY TESTING

### Quarterly DR Drill Schedule

| Quarter | Scenario | Duration | Pass/Fail |
|---------|----------|----------|-----------|
| Q1 2026 | Database Failover | 15 min | Pending |
| Q2 2026 | Application Rollback | 10 min | Pending |
| Q3 2026 | Complete Cluster Rebuild | 60 min | Pending |
| Q4 2026 | Security Breach Response | 30 min | Pending |

### DR Drill Checklist

- [ ] Schedule drill with 1-week notice
- [ ] Notify team (but don't specify exact time)
- [ ] Execute scenario
- [ ] Time each recovery step
- [ ] Document issues encountered
- [ ] Update runbook based on learnings
- [ ] Share results with team

---

## MONITORING & HEALTH CHECKS

### Key Metrics to Monitor

```bash
# Application Health
curl https://api.buildnest.com/actuator/health

# Database Health
kubectl exec -it mysql-0 -n buildnest -- mysql -u root -p -e "SELECT 1;"

# Redis Health
kubectl exec -it redis-0 -n buildnest -- redis-cli PING

# Response Time
curl -o /dev/null -s -w "Response time: %{time_total}s\n" https://api.buildnest.com

# Error Rate (via Prometheus)
curl http://prometheus:9090/api/v1/query?query=rate(http_requests_total{status=~"5.."}[5m])
```

### Dashboard URLs

- **Grafana**: https://grafana.buildnest.com
- **Prometheus**: https://prometheus.buildnest.com
- **Kibana**: https://kibana.buildnest.com
- **PagerDuty**: https://buildnest.pagerduty.com

---

## APPENDIX: QUICK REFERENCE COMMANDS

### Check System Status
```bash
kubectl get pods -n buildnest
kubectl get svc -n buildnest
kubectl top pods -n buildnest
kubectl get events -n buildnest --sort-by='.lastTimestamp'
```

### Emergency Rollback
```bash
kubectl rollout undo deployment/buildnest-app -n buildnest
kubectl rollout status deployment/buildnest-app -n buildnest
```

### View Logs
```bash
kubectl logs -f deployment/buildnest-app -n buildnest --all-containers
kubectl logs mysql-0 -n buildnest --tail=100
kubectl logs redis-0 -n buildnest --tail=100
```

### Database Quick Check
```bash
kubectl exec -it mysql-0 -n buildnest -- mysql -u root -p -e "
  SELECT table_name, table_rows 
  FROM information_schema.tables 
  WHERE table_schema='buildnest_ecommerce';
"
```

---

## DOCUMENT MAINTENANCE

**Review Schedule**: Monthly on 1st business day  
**Last Reviewed**: January 31, 2026  
**Next Review**: March 1, 2026  

**Changelog**:
- 2026-01-31: Initial creation - CRITICAL BLOCKER #6 resolved
- [Future updates here]

---

**✅ CRITICAL BLOCKER #6 RESOLVED**

This runbook provides comprehensive disaster recovery procedures with defined RTO/RPO, detailed recovery steps for critical scenarios, and emergency communication templates.

---

**END OF DISASTER RECOVERY RUNBOOK**
