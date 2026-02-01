# CI/CD Setup & Configuration Guide

## Overview
This project has 6 GitHub Actions workflows configured for complete CI/CD automation:
- **CI**: Build, test, coverage validation
- **Security**: OWASP dependency check, CheckStyle, SpotBugs, SonarQube
- **Deploy**: Docker containerization & Kubernetes deployment
- **Performance**: Benchmark and load testing
- Additional: ci-cd.yml, ci-cd-pipeline.yml (legacy/alternative configs)

---

## 1. Required GitHub Secrets Configuration

Add these secrets to your GitHub repository (Settings → Secrets and variables → Actions):

### Deployment Secrets
```
DOCKER_USERNAME          # Docker Hub username (for image push)
DOCKER_PASSWORD          # Docker Hub personal access token
DOCKER_REGISTRY          # Registry URL (e.g., docker.io or ghcr.io)
REGISTRY_USERNAME        # Registry login username
REGISTRY_PASSWORD        # Registry login password/token
```

### Code Quality Secrets
```
SONAR_TOKEN              # SonarQube/SonarCloud authentication token
CODECOV_TOKEN            # Codecov.io token for coverage reports
```

### Deployment Environment Secrets
```
KUBECONFIG_STAGING       # Kubernetes config for staging (base64 encoded)
KUBECONFIG_PRODUCTION    # Kubernetes config for production (base64 encoded)
KUBE_NAMESPACE_STAGING   # Kubernetes namespace for staging
KUBE_NAMESPACE_PROD      # Kubernetes namespace for production
SLACK_WEBHOOK            # Optional: Slack notification webhook
```

### How to Add Secrets
1. Go to repo Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Name: (secret name from above)
4. Value: (your secret value)
5. Click "Add secret"

**Example - Docker credentials:**
```
Name: DOCKER_USERNAME
Value: your-docker-username

Name: DOCKER_PASSWORD
Value: your-docker-personal-access-token
```

---

## 2. Configure GitHub Environments (for deploy workflow)

Go to **Settings → Environments** and create two environments:

### Staging Environment
- Name: `staging`
- Protection rules: (optional - allow only from develop branch)
- Environment variables/secrets:
  - `DOCKER_TAG`: staging
  - `K8S_NAMESPACE`: buildnest-staging
  - `REPLICAS`: 2

### Production Environment
- Name: `production`
- Protection rules: **Require approvals** (recommended: 2+ reviewers)
- Environment variables/secrets:
  - `DOCKER_TAG`: latest
  - `K8S_NAMESPACE`: buildnest-production
  - `REPLICAS`: 3

---

## 3. Configure Branch Protection Rules

Go to **Settings → Branches → Branch protection rules**

Create rule for `master`/`main` branch:
- ✅ Require a pull request before merging
- ✅ Require status checks to pass:
  - `CI/CD Pipeline / build`
  - `CI/CD Pipeline / test`
  - `Security & Quality Scan / security-scan`
  - `Security & Quality Scan / code-quality`
- ✅ Require branches to be up to date
- ✅ Require conversation resolution
- ✅ Include administrators

---

## 4. Workflow Triggers & Schedules

### CI Workflow (ci.yml)
- **Triggers**: On push, pull request, manual (workflow_dispatch)
- **Branches**: main, master, develop
- **When it runs**:
  - Any push to specified branches
  - Any pull request against specified branches
  - Manual trigger via Actions tab

### Security Workflow (security.yml)
- **Triggers**: On push, pull request, weekly schedule
- **Schedule**: Every Sunday at 00:00 UTC
- **Runs**: Dependency check, CheckStyle, SpotBugs, SonarQube

### Deploy Workflow (deploy.yml)
- **Triggers**: Manual (workflow_dispatch)
- **Environment selection**: Choose staging or production
- **Runs**: Build Docker image, push to registry, deploy to K8s

### Performance Workflow (performance.yml)
- **Triggers**: Manual, weekly schedule
- **Schedule**: Every Sunday at 03:00 UTC
- **Runs**: Performance benchmarks, load tests

---

## 5. Code Coverage Setup

### Codecov Integration
1. Enable in repository: https://codecov.io/gh/your-username/buildnest-ecommerce-platform
2. Add `CODECOV_TOKEN` to GitHub Secrets (if private repo)
3. Coverage reports auto-upload on CI completion

### Viewing Coverage
- **Local**: `target/site/jacoco/index.html` (open in browser)
- **CI**: Check GitHub Actions artifacts
- **Codecov**: https://app.codecov.io/gh/your-username/repo

**Coverage Requirements**:
- Line: ≥ 93.88% ✅
- Method: ≥ 90.88% ✅
- Instruction: ≥ 89.71% (needs 0.29% improvement)
- Branch: 66.07% (architectural limitation)

---

## 6. Manual Workflow Triggers

### Run CI Pipeline
```bash
# Go to Actions tab → CI/CD Pipeline → Run workflow
# Or via GitHub CLI:
gh workflow run ci.yml -f branch=master
```

### Run Security Scan
```bash
# Actions tab → Security & Quality Scan → Run workflow
gh workflow run security.yml
```

### Trigger Deployment
```bash
# Actions tab → Deploy to Production → Run workflow
# Select environment: staging or production
gh workflow run deploy.yml -f environment=staging
```

### Run Performance Tests
```bash
# Actions tab → Performance & Load Testing → Run workflow
gh workflow run performance.yml
```

---

## 7. Artifact Retention & Management

Workflows store artifacts (test reports, coverage, performance results):
- **Default retention**: 30 days
- **Location**: GitHub Actions tab → Workflow run → Artifacts
- **Download**: Click artifact name to download

**Important artifacts**:
- `coverage-reports/` - JaCoCo coverage HTML
- `security-reports/` - OWASP, CheckStyle, SpotBugs
- `performance-reports/` - Benchmark results
- `test-results/` - JUnit test reports

---

## 8. Monitoring & Notifications

### GitHub Actions Notifications
- ✅ Enabled by default for all users
- Go to Settings (user) → Notifications → GitHub Actions

### Optional: Slack Notifications
Add to CI/CD workflow step:
```yaml
- name: Notify Slack
  if: failure()
  uses: slackapi/slack-github-action@v1.24
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK }}
    payload: |
      {
        "text": "❌ Build failed for ${{ github.repository }}"
      }
```

---

## 9. Troubleshooting

### Workflow Won't Trigger
- Check branch name matches workflow `on.branches`
- Verify `.github/workflows/*.yml` files exist
- Check GitHub Actions is enabled (Settings → Actions)

### Docker Push Fails
- Verify `DOCKER_USERNAME` and `DOCKER_PASSWORD` are correct
- Check Docker Hub token has push permissions
- Verify registry URL is correct

### Tests Fail in CI but Pass Locally
- Check Java version matches (should be 21)
- Verify Maven cache is working
- Check environment variables are set in workflow

### Coverage Reports Not Uploading
- Verify `CODECOV_TOKEN` is set (for private repos)
- Check `target/site/jacoco/jacoco.xml` exists after test
- Review codecov action step logs

---

## 10. Best Practices

### Commits
- ✅ Use conventional commits: `feat:`, `fix:`, `test:`, `ci:`, etc.
- ✅ Small, focused commits for easy rollback
- ✅ Include PR description linking to issues

### Pull Requests
- ✅ Create PR against `develop` or `master`
- ✅ Wait for all checks to pass
- ✅ Request reviews from 2+ maintainers
- ✅ Merge only after approval

### Deployments
- ✅ Deploy to staging first
- ✅ Run smoke tests after staging deploy
- ✅ Get approval before production deploy
- ✅ Monitor metrics after production deploy

### Testing
- ✅ Maintain ≥90% code coverage
- ✅ Write tests before features (TDD)
- ✅ Test edge cases and error paths
- ✅ Run locally before pushing

---

## 11. Next Steps

1. **Add GitHub Secrets** (Step 1 above)
2. **Configure Environments** (Step 2 above)
3. **Set Branch Protection** (Step 3 above)
4. **Test CI Pipeline**: Push a small change and monitor Actions tab
5. **Verify Coverage**: Check coverage reports in artifacts
6. **Set up Codecov**: Link GitHub to Codecov.io
7. **Document team processes**: Commit message conventions, deployment procedures

---

## 12. Workflow Reference

| Workflow | File | Trigger | Purpose |
|----------|------|---------|---------|
| CI/CD Pipeline | `ci.yml` | Push, PR, manual | Build, test, coverage |
| Security & Quality | `security.yml` | Push, PR, weekly | Vulnerability scan, code quality |
| Deploy to Production | `deploy.yml` | Manual | Docker build, K8s deploy |
| Performance Testing | `performance.yml` | Manual, weekly | Benchmarks, load tests |
| Legacy CI/CD | `ci-cd.yml` | Push, PR | Alternative build pipeline |
| Legacy Pipeline | `ci-cd-pipeline.yml` | Push, PR | Extended testing pipeline |

---

## Support

For issues:
1. Check workflow logs in Actions tab
2. Review step output for error messages
3. Check GitHub status: https://www.githubstatus.com
4. Review troubleshooting section above
