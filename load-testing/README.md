# BuildNest E-Commerce Platform - Load Testing Guide
**Version**: 1.0.0  
**Last Updated**: 2026-01-31  
**Tool**: Apache JMeter 5.6.3

---

## Overview

This directory contains comprehensive load testing configurations for the BuildNest E-Commerce Platform. The test suite simulates realistic user behavior patterns to validate production readiness.

---

## Test Scenarios

### 1. **Product Search & Browse** (60% of users - 600 concurrent)
- Search for products with various keywords
- View product details
- Browse categories
- Simulate realistic browsing patterns with think time

### 2. **Add to Cart & Checkout** (25% of users - 250 concurrent)
- User authentication (login with JWT)
- Add products to cart
- View cart contents
- Proceed through checkout flow

### 3. **User Registration & Login** (10% of users - 100 concurrent)
- Register new user accounts
- Login with credentials
- Token-based authentication

### 4. **Admin Operations** (5% of users - 50 concurrent)
- Admin authentication
- View all orders
- Access analytics dashboard
- Monitor system metrics

---

## Load Profile

| Metric | Value |
|--------|-------|
| **Total Concurrent Users** | 1,000 |
| **Test Duration** | 15 minutes (900 seconds) |
| **Ramp-up Time** | 2 minutes (120 seconds) |
| **Think Time** | 2-10 seconds (varies by scenario) |
| **Expected Throughput** | 10,000+ requests/minute |

---

## Success Criteria

| Metric | Target | Critical Threshold |
|--------|--------|-------------------|
| **P50 Response Time** | < 200ms | < 300ms |
| **P95 Response Time** | < 500ms | < 750ms |
| **P99 Response Time** | < 1000ms | < 1500ms |
| **Error Rate** | < 0.1% | < 1.0% |
| **Throughput** | > 10,000 req/min | > 8,000 req/min |
| **CPU Usage** | < 70% | < 85% |
| **Memory Usage** | < 80% | < 90% |

---

## Prerequisites

### 1. Install Apache JMeter

**Windows**:
```powershell
# Download JMeter 5.6.3
Invoke-WebRequest -Uri "https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.zip" -OutFile "jmeter.zip"
Expand-Archive -Path "jmeter.zip" -DestinationPath "C:\Tools"
$env:PATH += ";C:\Tools\apache-jmeter-5.6.3\bin"
```

**Linux/macOS**:
```bash
# Download and extract JMeter
wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xzf apache-jmeter-5.6.3.tgz
export PATH=$PATH:~/apache-jmeter-5.6.3/bin
```

### 2. Verify Installation

```bash
jmeter --version
# Expected output: Apache JMeter 5.6.3
```

### 3. Prepare Test Environment

1. **Application Running**: Ensure BuildNest application is running at `http://localhost:8080`
2. **Database Seeded**: Populate database with test data (products, categories)
3. **Test Users Created**: Create test users listed in `test-users.csv`
4. **Monitoring Active**: Enable Prometheus metrics at `/actuator/prometheus`

---

## Running Load Tests

### Option 1: Command Line (Non-GUI Mode - Recommended)

**Basic Test**:
```bash
jmeter -n -t load-testing/buildnest-load-test.jmx \
  -l load-testing/results/test-results.jtl \
  -e -o load-testing/results/html-report
```

**Custom Parameters**:
```bash
jmeter -n -t load-testing/buildnest-load-test.jmx \
  -Jhost=staging.buildnest.com \
  -Jport=443 \
  -Jprotocol=https \
  -Jthreads=500 \
  -Jrampup=60 \
  -Jduration=600 \
  -l load-testing/results/test-results.jtl \
  -e -o load-testing/results/html-report
```

**PowerShell Script** (Windows):
```powershell
# Run load test with production-like settings
& jmeter.bat -n `
  -t "load-testing\buildnest-load-test.jmx" `
  -Jhost=localhost `
  -Jport=8080 `
  -Jprotocol=http `
  -Jthreads=1000 `
  -Jrampup=120 `
  -Jduration=900 `
  -l "load-testing\results\test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').jtl" `
  -e -o "load-testing\results\html-report-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
```

### Option 2: GUI Mode (Development/Debugging Only)

```bash
jmeter -t load-testing/buildnest-load-test.jmx
```

**Note**: GUI mode is NOT recommended for actual load testing due to resource overhead.

---

## Configuration Parameters

All parameters can be overridden via command line using `-J` flag:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `base.url` | `http://localhost:8080` | Full base URL |
| `host` | `localhost` | Application hostname |
| `port` | `8080` | Application port |
| `protocol` | `http` | Protocol (http/https) |
| `threads` | `1000` | Total concurrent users |
| `rampup` | `120` | Ramp-up time in seconds |
| `duration` | `900` | Test duration in seconds |
| `admin.password` | `Admin@1234` | Admin user password |

**Example**:
```bash
jmeter -n -t load-testing/buildnest-load-test.jmx \
  -Jhost=production.buildnest.com \
  -Jport=443 \
  -Jprotocol=https \
  -Jthreads=2000 \
  -Jduration=1800 \
  -l results.jtl
```

---

## Interpreting Results

### HTML Report

After test completion, open `load-testing/results/html-report/index.html` in a browser.

**Key Sections**:
1. **Dashboard**: Overview of test execution
2. **Statistics**: Request statistics (throughput, response time)
3. **Response Times**: P50, P95, P99 percentiles
4. **Throughput**: Requests per second over time
5. **Errors**: Error rate and types

### Success/Failure Determination

**✅ Test PASSED if**:
- P95 response time < 500ms
- P99 response time < 1000ms
- Error rate < 0.1%
- Throughput > 10,000 req/min
- No OOMKilled pods or container restarts

**❌ Test FAILED if**:
- P95 response time > 750ms (critical)
- Error rate > 1.0% (critical)
- Database connection pool exhaustion
- Memory usage > 90% (OOMKill risk)

---

## Test Data Management

### User Credentials

Edit `load-testing/test-users.csv` to add test users:

```csv
username,password,email
testuser1,Test@1234,test1@example.com
testuser2,Test@1234,test2@example.com
```

**Create Test Users** (SQL):
```sql
INSERT INTO users (username, email, password, first_name, last_name, role)
VALUES 
  ('user1', 'user1@buildnest.com', '$2a$10$...', 'Test', 'User1', 'USER'),
  ('user2', 'user2@buildnest.com', '$2a$10$...', 'Test', 'User2', 'USER');
```

### Seeding Test Data

Before running load tests, seed the database with realistic data:

```bash
# Run migration script to populate products/categories
.\test-database-migrations.ps1 -SeedTestData
```

---

## Monitoring During Load Tests

### 1. Application Metrics (Prometheus)

```bash
# Monitor HTTP request rate
curl http://localhost:8080/actuator/prometheus | grep http_server_requests_seconds_count

# Monitor JVM memory
curl http://localhost:8080/actuator/prometheus | grep jvm_memory_used_bytes
```

### 2. Database Metrics

```sql
-- Active connections
SHOW STATUS LIKE 'Threads_connected';

-- Slow queries
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;

-- Connection pool utilization
SELECT * FROM information_schema.processlist WHERE USER = 'buildnest';
```

### 3. Redis Metrics

```bash
# Monitor Redis latency
redis-cli --latency

# Monitor memory usage
redis-cli INFO memory | grep used_memory_human

# Monitor commands/sec
redis-cli INFO stats | grep instantaneous_ops_per_sec
```

### 4. Kubernetes Metrics

```bash
# Pod CPU/Memory usage
kubectl top pods -n production

# HPA autoscaling status
kubectl get hpa -n production

# Check for OOMKilled pods
kubectl get pods -n production | grep OOMKilled
```

---

## Troubleshooting

### Issue: High Error Rate

**Symptoms**: Error rate > 1%

**Diagnosis**:
1. Check application logs: `kubectl logs -n production -l app=buildnest-ecommerce --tail=100`
2. Check for 5xx errors: `grep "500\|502\|503" load-testing/results/test-results.jtl`
3. Verify database connectivity: `curl http://localhost:8080/actuator/health`

**Solutions**:
- Increase database connection pool size
- Scale application replicas: `kubectl scale deployment buildnest-ecommerce --replicas=5`
- Reduce concurrent users: `-Jthreads=500`

### Issue: High Response Time

**Symptoms**: P95 > 750ms

**Diagnosis**:
1. Identify slow endpoints: Review HTML report "Response Times" section
2. Check database query performance: `SHOW FULL PROCESSLIST;`
3. Check Redis latency: `redis-cli --latency`

**Solutions**:
- Optimize slow database queries (add indexes)
- Increase cache TTL for frequently accessed data
- Tune HikariCP pool: `spring.datasource.hikari.maximum-pool-size=30`

### Issue: Connection Refused Errors

**Symptoms**: `java.net.ConnectException: Connection refused`

**Diagnosis**:
- Application not running: `curl http://localhost:8080/actuator/health`
- Port mismatch: Verify `-Jport=8080` matches application port
- Firewall blocking: Check Windows Firewall / iptables

**Solutions**:
- Start application: `./mvnw spring-boot:run`
- Verify port: `netstat -ano | findstr 8080` (Windows) or `lsof -i :8080` (Linux)

### Issue: Out of Memory

**Symptoms**: JMeter crashes or pods are OOMKilled

**Diagnosis**:
- JMeter heap size too small
- Application memory leak
- Too many concurrent threads

**Solutions**:
- Increase JMeter heap: `export HEAP="-Xms2g -Xmx4g"` (before running jmeter)
- Reduce concurrent users: `-Jthreads=500`
- Increase application memory limit in Kubernetes deployment

---

## CI/CD Integration

### GitHub Actions Workflow

Add to `.github/workflows/load-test.yml`:

```yaml
name: Load Test

on:
  workflow_dispatch:
    inputs:
      target_environment:
        description: 'Target environment (staging/production)'
        required: true
        default: 'staging'
      duration:
        description: 'Test duration in seconds'
        required: true
        default: '900'

jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Install JMeter
        run: |
          wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz
          tar -xzf apache-jmeter-5.6.3.tgz
          echo "$PWD/apache-jmeter-5.6.3/bin" >> $GITHUB_PATH
      
      - name: Run load test
        run: |
          jmeter -n -t load-testing/buildnest-load-test.jmx \
            -Jhost=${{ secrets.STAGING_HOST }} \
            -Jport=443 \
            -Jprotocol=https \
            -Jduration=${{ github.event.inputs.duration }} \
            -l load-testing/results/test-results.jtl \
            -e -o load-testing/results/html-report
      
      - name: Upload results
        uses: actions/upload-artifact@v4
        with:
          name: load-test-results
          path: load-testing/results/
      
      - name: Evaluate results
        run: |
          # Parse results and fail if thresholds exceeded
          ERROR_RATE=$(grep -c "false" load-testing/results/test-results.jtl || echo 0)
          TOTAL_SAMPLES=$(wc -l < load-testing/results/test-results.jtl)
          ERROR_PERCENT=$(echo "scale=2; $ERROR_RATE / $TOTAL_SAMPLES * 100" | bc)
          
          if (( $(echo "$ERROR_PERCENT > 1.0" | bc -l) )); then
            echo "Load test FAILED: Error rate ${ERROR_PERCENT}% exceeds 1.0%"
            exit 1
          fi
```

---

## Best Practices

### 1. Test Environment Isolation

- Use dedicated staging environment
- Avoid testing on production
- Ensure consistent baseline (same data, same configuration)

### 2. Gradual Load Increase

- Start with 100 users, verify stability
- Increase to 500 users, verify metrics
- Full load (1,000 users) only after validation

### 3. Realistic Think Time

- Include 2-10 second delays between requests
- Simulates real user behavior (reading, decision-making)
- Prevents unrealistic hammering of API

### 4. Monitor During Tests

- Keep Grafana/Prometheus dashboards open
- Watch for CPU/memory spikes
- Check database connection pool utilization

### 5. Results Analysis

- Compare against previous baselines
- Identify performance regressions
- Document optimization improvements

---

## Results Checklist

After running load tests, complete this checklist:

- [ ] P95 response time < 500ms
- [ ] P99 response time < 1000ms
- [ ] Error rate < 0.1%
- [ ] Throughput > 10,000 req/min
- [ ] CPU usage < 70% average
- [ ] Memory usage < 80% average
- [ ] No OOMKilled pods
- [ ] Database connection pool < 80% utilization
- [ ] Redis latency < 5ms average
- [ ] No 5xx errors in application logs

---

## Next Steps

1. **Baseline Testing**: Run initial load test to establish baseline metrics
2. **Optimization**: Address any performance bottlenecks identified
3. **Retest**: Verify improvements with subsequent load tests
4. **Stress Testing**: Gradually increase load beyond 1,000 users to find breaking point
5. **Soak Testing**: Run 24-hour test to identify memory leaks
6. **Spike Testing**: Sudden load increase to test autoscaling

---

## Support

For issues or questions:
- Review application logs: `kubectl logs -n production -l app=buildnest-ecommerce`
- Check PRODUCTION_READINESS_ASSESSMENT.md
- Consult DISASTER_RECOVERY_RUNBOOK.md for incident response

---

**Document Version**: 1.0.0  
**Last Updated**: 2026-01-31  
**Maintained By**: BuildNest DevOps Team
