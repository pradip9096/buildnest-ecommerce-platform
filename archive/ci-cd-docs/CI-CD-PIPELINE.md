# 🔄 CI/CD Pipeline Documentation

## Overview

This project uses GitHub Actions to automate build, test, security scanning, and deployment processes. The pipeline ensures code quality, test coverage, and security standards are met before deployment.

## 📋 Workflows

### 1. **CI/CD Pipeline** (`ci.yml`)
**Triggers**: Push to main/master/develop, Pull Requests, Manual, Weekly schedule

**Jobs**:
- **Build & Test**: Compiles code, runs tests, generates JaCoCo coverage
- **Quality Gates**: Validates coverage thresholds
- **Notify Results**: Generates summary reports

**Key Features**:
- ✅ JDK 21 with Maven caching
- ✅ JaCoCo code coverage reports
- ✅ Codecov integration
- ✅ Pull request comments with coverage analysis
- ✅ Automated test result publishing
- ✅ Coverage validation gates

**Coverage Thresholds**:
| Metric | Threshold |
|--------|-----------|
| Line | 90% |
| Method | 90% |
| Instruction | 90% |
| Branch | 90% |
| Class | 90% |

### 2. **Deployment Pipeline** (`deploy.yml`)
**Triggers**: CI/CD Pipeline success, Manual with environment selection

**Jobs**:
- **Deploy Application**: Builds Docker image, pushes to registry, deploys to K8s

**Environments**:
- Staging (default)
- Production

**Configuration Needed**:
```yaml
# Add to GitHub Secrets:
- REGISTRY_USERNAME: Docker registry username
- REGISTRY_PASSWORD: Docker registry password
- KUBECONFIG: Kubernetes configuration
```

### 3. **Security & Quality Scan** (`security.yml`)
**Triggers**: Push to main/develop, Pull Requests, Weekly schedule

**Jobs**:
- **Dependency Check**: OWASP dependency vulnerabilities
- **Security Scan**: Maven security plugins
- **Code Quality**: CheckStyle, SpotBugs, SonarQube

**Reports Generated**:
- SARIF (Security scanning)
- Dependency check reports
- CheckStyle violations
- SpotBugs findings

### 4. **Performance Testing** (`performance.yml`)
**Triggers**: Manual, Weekly schedule

**Jobs**:
- **Performance Test**: Benchmark suite execution
- **Load Test**: JMeter load testing (optional)

## 🚀 Getting Started

### Prerequisites
- GitHub repository with Actions enabled
- Java 21 installed (handled by Actions)
- Maven wrapper in project root

### Local Testing

```bash
# Run full pipeline locally (simulated)
chmod +x mvnw
./mvnw clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Enabling Workflows

1. **Enable in Repository Settings**:
   - Settings → Actions → Runner access → Allow all actions

2. **Configure Secrets** (if needed):
   ```
   Settings → Secrets and variables → Actions
   ```

3. **Workflows automatically activate** when pushed to repository

## 📊 Coverage Reports

### Viewing Results

1. **In GitHub Actions**:
   - Actions tab → Select workflow run
   - View summary or download artifacts

2. **Codecov Integration**:
   - Coverage reports sent to codecov.io
   - View at: `https://codecov.io/gh/pradip9096/buildnest-ecommerce-platform`

3. **Local Generation**:
   ```bash
   ./mvnw test jacoco:report
   open target/site/jacoco/index.html
   ```

### Interpreting Coverage

```
Green ✅  : Coverage meets/exceeds threshold
Yellow ⚠️  : Coverage approaching threshold
Red ❌    : Coverage below threshold
```

## 🔐 Security Configuration

### Enable OWASP Dependency Check
```bash
./mvnw verify -Dowasp.dependencycheck.skip=false
```

### Enable SonarQube
1. Create SonarCloud account
2. Add `SONAR_TOKEN` to GitHub Secrets
3. Uncomment SonarQube steps in `security.yml`

### CVE Scanning
Automatically checks dependencies for known vulnerabilities via SARIF integration.

## 📈 Performance & Load Testing

### Run Locally
```bash
python scripts/test_performance_metrics.py
```

### JMeter Load Testing
```bash
jmeter -n -t load-testing/buildnest-load-test.jmx -l results.jtl
```

## 🔧 Customization

### Modify Coverage Thresholds

Edit `ci.yml` in the "Generate Coverage Report" step:
```python
thresholds = {
    "INSTRUCTION": 90.0,  # Modify here
    "LINE": 90.0,
    "BRANCH": 90.0,
    "METHOD": 90.0,
    "CLASS": 90.0
}
```

### Add Custom Notifications

Edit notification step to send to Slack, Teams, etc.:
```yaml
- name: Notify Results
  uses: slackapi/slack-github-action@v1
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK }}
```

### Conditional Deployment

Modify `deploy.yml` triggers:
```yaml
if: |
  github.ref == 'refs/heads/master' &&
  needs.build.result == 'success' &&
  needs.quality-gates.result == 'success'
```

## 📝 Artifacts Generated

| Artifact | Purpose | Retention |
|----------|---------|-----------|
| coverage-reports | JaCoCo HTML/XML | 30 days |
| test-reports | JUnit XML | 30 days |
| jacoco-xml | CodeCov format | 30 days |
| security-reports | OWASP/SpotBugs | 30 days |
| quality-reports | CheckStyle/SonarQube | 30 days |
| performance-reports | Benchmarks | 30 days |

## 🐛 Troubleshooting

### Build Fails
- Check Java version: Should be 21
- Verify Maven wrapper: `ls -la mvnw`
- Review test logs in Actions tab

### Coverage Below Threshold
- Run locally: `./mvnw clean test jacoco:report`
- Review coverage report: `target/site/jacoco/index.html`
- Add tests for uncovered code paths

### Deployment Issues
1. Verify Docker image builds locally
2. Check Kubernetes configuration
3. Review environment secrets

## 📞 Support

For issues or questions:
1. Check workflow logs in Actions tab
2. Review error messages in step output
3. Consult GitHub Actions documentation

## 🔗 Useful Links

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/)
- [Maven Documentation](https://maven.apache.org/)
- [Codecov](https://codecov.io/)
- [SonarQube](https://www.sonarsource.com/sonarqube/)

---

**Last Updated**: February 1, 2026
**Status**: ✅ Active
**Java Version**: 21
**Build Tool**: Maven
