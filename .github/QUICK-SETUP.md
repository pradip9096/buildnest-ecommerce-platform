# ⚡ Quick Action Guide - GitHub Environments

## 🎯 What to Do RIGHT NOW

### 🟢 Step 1: Add GitHub Secrets (10 minutes)

Go to: https://github.com/pradip9096/buildnest-ecommerce-platform/settings/secrets/actions

**Add 3 secrets** by clicking "New repository secret":

1. **DOCKER_USERNAME**
   - Value: Your Docker Hub username

2. **DOCKER_PASSWORD**
   - Value: Your Docker Personal Access Token
   - Get it: https://hub.docker.com/settings/security → New Access Token

3. **CODECOV_TOKEN**
   - Value: Your Codecov token
   - Get it: https://codecov.io → Connect repo → Copy token

### 🔵 Step 2: Create Staging Environment (5 minutes)

Go to: https://github.com/pradip9096/buildnest-ecommerce-platform/settings/environments

**Click "New environment"**:
- Name: `staging`
- Click "Configure environment"
- No protection rules needed
- Add variables:
  - `K8S_NAMESPACE` = `buildnest-staging`
  - `DOCKER_TAG` = `staging`
  - `REPLICAS` = `2`

### 🔴 Step 3: Create Production Environment (5 minutes)

Go to: https://github.com/pradip9096/buildnest-ecommerce-platform/settings/environments

**Click "New environment"**:
- Name: `production`
- Click "Configure environment"
- **Check** "Required reviewers"
  - Add 2+ team members
- **Check** "Restrict to deployment branches"
  - Select: `master` or `main`
- Add variables:
  - `K8S_NAMESPACE` = `buildnest-production`
  - `DOCKER_TAG` = `latest`
  - `REPLICAS` = `3`

---

## ⏱️ Total Time: ~20 minutes

**Status**:
- [ ] Secrets added (10 min)
- [ ] Staging environment created (5 min)
- [ ] Production environment created (5 min)

---

## ✅ Verification

After completing above, verify:

```
Settings → Secrets and variables → Actions
  ✓ DOCKER_USERNAME
  ✓ DOCKER_PASSWORD
  ✓ CODECOV_TOKEN

Settings → Environments
  ✓ staging (with 3 variables)
  ✓ production (with 3 variables + 2 reviewers)
```

---

## 🚀 After Verification

**You're ready to**:
1. Test the CI/CD pipeline
2. Deploy to staging
3. Deploy to production (with approvals)

**Test with**:
```bash
git checkout -b test/pipeline
echo "# Test" >> README.md
git add README.md
git commit -m "test: verify pipeline"
git push origin test/pipeline
# → Watch Actions tab
```

---

## 📚 Documentation

- `.github/SECRETS-AND-ENVIRONMENTS.md` - Complete setup guide
- `.github/ENVIRONMENTS-SETUP.md` - Step-by-step with screenshots
- `CICD-QUICK-REFERENCE.md` - Daily usage guide

---

## 🆘 Help

- Not sure about Docker token? See: https://docs.docker.com/docker-hub/access-tokens/
- Not sure about Codecov? See: https://codecov.io/account/settings
- GitHub help: https://docs.github.com/en/actions/deployment/targeting-different-environments

---

**Ready?** → Start with Step 1 above!
