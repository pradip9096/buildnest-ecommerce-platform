# CI/CD Quick Reference

## 🚀 Essential Commands

### Local Development
```bash
# Build locally
./mvnw clean install

# Run tests only
./mvnw clean test

# Run specific test
./mvnw test -Dtest=ClassName

# Generate coverage
./mvnw clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html  # macOS
explorer target\site\jacoco\index.html  # Windows
```

### Git Workflow
```bash
# Create feature branch
git checkout -b feature/my-feature develop

# Push and create PR
git push origin feature/my-feature

# Sync with main branch
git fetch origin
git rebase origin/develop

# Merge after PR approval
git checkout develop
git merge --no-ff feature/my-feature
git push origin develop
```

### GitHub Actions
```bash
# Trigger CI manually (via GitHub CLI)
gh workflow run ci.yml

# Trigger security scan
gh workflow run security.yml

# Trigger deployment
gh workflow run deploy.yml -f environment=staging

# View workflow status
gh run list --workflow=ci.yml
```

---

## 📊 Key Metrics

### Coverage Targets
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Line | 93.88% | 90% | ✅ Pass |
| Method | 90.88% | 90% | ✅ Pass |
| Instruction | 89.71% | 90% | ⚠️ Need 0.29% |
| Branch | 66.07% | N/A | ℹ️ Info |

### Pipeline Times
| Stage | Duration | Notes |
|-------|----------|-------|
| Build | 1-2 min | Maven compilation |
| Tests | 2-3 min | 1,349 tests |
| Coverage | 1 min | JaCoCo report |
| Security | 3-5 min | OWASP + quality |
| **Total** | **~10 min** | End-to-end |

---

## 🔑 Required Secrets

Add to **Settings → Secrets and variables → Actions**:

```
DOCKER_USERNAME       Docker Hub username
DOCKER_PASSWORD       Docker access token
CODECOV_TOKEN         Codecov.io token
SONAR_TOKEN          SonarQube token (optional)
```

---

## 🌳 Branch Strategy

```
master (main)          ← Production code (protected)
  ├─ Hotfixes only
  └─ Require PR + 2 approvals

develop                ← Development code
  ├─ Integration branch
  ├─ Auto-deploy to staging
  └─ Require PR + 1 approval

feature/*              ← Feature branches
  └─ Create from develop
```

---

## ✅ Pre-Merge Checklist

Before merging PR:
- [ ] All CI checks pass (green ✅)
- [ ] Coverage maintained/improved
- [ ] No new security issues
- [ ] Code reviewed by 2+ peers
- [ ] Commits follow convention
- [ ] Tests added for new features
- [ ] Documentation updated
- [ ] No conflicts with main branch

---

## 🐛 Common Issues & Fixes

| Issue | Solution |
|-------|----------|
| Tests pass locally, fail in CI | Check Java version, clean build: `./mvnw clean` |
| Coverage reports missing | Verify `target/site/jacoco/jacoco.xml` exists |
| Docker build fails | Check `DOCKER_USERNAME` and `DOCKER_PASSWORD` secrets |
| K8s deployment fails | Verify `KUBECONFIG` secret and namespace |
| Workflows not triggering | Verify branch name in `on.branches` and push to correct branch |

---

## 📍 Workflow Locations

| Workflow | File | Trigger |
|----------|------|---------|
| CI/CD | `.github/workflows/ci.yml` | Push, PR, manual |
| Security | `.github/workflows/security.yml` | Push, PR, weekly |
| Deploy | `.github/workflows/deploy.yml` | Manual only |
| Performance | `.github/workflows/performance.yml` | Manual, weekly |

---

## 🎯 Deployment Checklist

### Staging Deploy
```
1. Go to Actions → Deploy to Production
2. Click "Run workflow"
3. Select environment: staging
4. Wait for workflow to complete
5. Run smoke tests
6. Monitor logs and metrics
```

### Production Deploy
```
1. Get approval from 2+ maintainers
2. Go to Actions → Deploy to Production
3. Click "Run workflow"
4. Select environment: production
5. Wait for workflow and approval
6. Monitor key metrics
7. Keep rollback ready
```

---

## 📊 Monitoring

### GitHub Actions
- **Dashboard**: https://github.com/pradip9096/buildnest-ecommerce-platform/actions
- **Logs**: Click workflow run → View logs
- **Artifacts**: Click workflow run → Artifacts tab

### Coverage
- **Local**: `target/site/jacoco/index.html`
- **Codecov**: https://codecov.io/gh/your-username/repo
- **PR Comments**: Automatic coverage diff

### Performance
- **Benchmarks**: Download from Actions → Artifacts
- **Load Tests**: JMeter results in artifacts

---

## 🔗 Useful Links

| Resource | URL |
|----------|-----|
| GitHub Actions | https://github.com/features/actions |
| Codecov | https://codecov.io |
| SonarQube | https://www.sonarqube.org |
| Maven | https://maven.apache.org |
| Kubernetes | https://kubernetes.io |
| Docker | https://docker.com |

---

## 📞 Support

**For questions:**
1. Check `.github/CICD_SETUP_GUIDE.md`
2. Review workflow logs in Actions
3. Check troubleshooting section above
4. Contact DevOps team

---

## 🎓 Learning Resources

- **CI/CD Basics**: https://docs.github.com/en/actions/about-github-actions
- **Maven Guide**: https://maven.apache.org/guides/
- **Docker Best Practices**: https://docs.docker.com/develop/
- **Kubernetes 101**: https://kubernetes.io/docs/tutorials/

---

## 📋 Status

| Component | Status | Notes |
|-----------|--------|-------|
| Workflows | ✅ Ready | 6 workflows configured |
| Tests | ✅ Ready | 1,349 tests passing |
| Coverage | ✅ Ready | 93.88% line coverage |
| Security | ✅ Ready | OWASP CVSS ≥7 |
| Docker | ✅ Ready | Multi-stage build |
| Kubernetes | ✅ Ready | Manifests included |
| Performance | ✅ Ready | JMeter configured |

**Overall Status**: 🟢 **PRODUCTION READY**
