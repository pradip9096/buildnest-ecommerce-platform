# GitHub Secrets and Environments - Complete Setup

## 🔐 Part 1: Add Repository Secrets

**Location**: Settings → Secrets and variables → Actions → "New repository secret"

Add these 3 required secrets:

### Secret 1: DOCKER_USERNAME
```
Name:  DOCKER_USERNAME
Value: your-docker-hub-username
Example: pradip9096
```

### Secret 2: DOCKER_PASSWORD
```
Name:  DOCKER_PASSWORD
Value: your-docker-personal-access-token
```
**How to get Docker PAT**:
1. Go to https://hub.docker.com/settings/security
2. Click "New Access Token"
3. Name: "buildnest-cicd"
4. Permissions: Read & Write
5. Copy the token and paste here

### Secret 3: CODECOV_TOKEN
```
Name:  CODECOV_TOKEN
Value: your-codecov-token
```
**How to get Codecov token**:
1. Go to https://codecov.io
2. Connect your GitHub repo
3. Go to Settings → Upload token
4. Copy and paste here

---

## 🌍 Part 2: Create GitHub Environments

**Location**: Settings → Environments

### Environment 1: STAGING

**Configuration**:
```
Name: staging
Protection rules: NONE (no approvals)
```

**Secrets/Variables**:
```
K8S_NAMESPACE      = buildnest-staging
DOCKER_TAG         = staging
REPLICAS           = 2
```

### Environment 2: PRODUCTION

**Configuration**:
```
Name: production
Protection rules: YES (require approval)
  - Minimum reviewers: 2
  - Required branches: master, main
```

**Secrets/Variables**:
```
K8S_NAMESPACE      = buildnest-production
DOCKER_TAG         = latest
REPLICAS           = 3
```

---

## ✅ Verification Checklist

After setup, verify in GitHub:

- [ ] Repository Secrets visible: Settings → Secrets → 3 secrets listed
  - [ ] DOCKER_USERNAME ✓
  - [ ] DOCKER_PASSWORD ✓
  - [ ] CODECOV_TOKEN ✓

- [ ] Staging Environment visible: Settings → Environments → staging
  - [ ] K8S_NAMESPACE: buildnest-staging
  - [ ] DOCKER_TAG: staging
  - [ ] REPLICAS: 2

- [ ] Production Environment visible: Settings → Environments → production
  - [ ] K8S_NAMESPACE: buildnest-production
  - [ ] DOCKER_TAG: latest
  - [ ] REPLICAS: 3
  - [ ] Require 2 reviewers: ✓
  - [ ] Restrict to master/main: ✓

---

## 🧪 Test the Setup

After configuration, test with:

```bash
# 1. Create a test branch
git checkout -b test/env-setup

# 2. Make a small change
echo "# Test" >> README.md
git add README.md
git commit -m "test: verify environment setup"
git push origin test/env-setup

# 3. Watch GitHub Actions
# Go to Actions tab and verify workflow runs
# Check that it can access environment variables
```

---

## 📋 Complete Setup Summary

| Item | Status | Done |
|------|--------|------|
| DOCKER_USERNAME secret | Required | ☐ |
| DOCKER_PASSWORD secret | Required | ☐ |
| CODECOV_TOKEN secret | Required | ☐ |
| Staging environment | Required | ☐ |
| Production environment | Required | ☐ |
| Staging variables | Required | ☐ |
| Production variables | Required | ☐ |
| Production reviewers | Required | ☐ |
| Production branch protection | Optional | ☐ |

---

## 🆘 Common Issues

### Secret not available in workflow
- **Cause**: Secret name doesn't match workflow reference
- **Fix**: Check exact spelling in workflow and GitHub UI

### Workflow can't access environment
- **Cause**: Environment name misspelled or doesn't exist
- **Fix**: Verify environment name in Settings → Environments

### Deployment requires approval but can't find reviewers
- **Cause**: Reviewers not configured properly
- **Fix**: Set minimum 2 reviewers in environment protection rules

---

## 📞 Next Steps

1. ✅ Add repository secrets (3 items)
2. ✅ Create staging environment
3. ✅ Create production environment
4. ⏳ Set branch protection rules
5. ⏳ Test with sample deployment

**Total time**: ~20 minutes

---

## 🔗 Quick Links

- **GitHub Repository**: https://github.com/pradip9096/buildnest-ecommerce-platform
- **Docker Hub**: https://hub.docker.com
- **Codecov**: https://codecov.io
- **GitHub Docs**: https://docs.github.com/en/actions/deployment/targeting-different-environments/using-environments-for-deployment
