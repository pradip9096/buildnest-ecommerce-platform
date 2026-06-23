# GitHub Environments Setup Guide

## 📍 How to Create GitHub Environments

### Location in GitHub UI
1. Go to your repository: https://github.com/pradip9096/buildnest-ecommerce-platform
2. Click **Settings** (top right)
3. In left sidebar, scroll down and click **Environments**
4. You should see "New environment" button

---

## 🟦 Create STAGING Environment

### Step 1: Click "New environment"
- Button location: Right side of page

### Step 2: Enter Environment Name
```
Environment name: staging
```
- Click **Configure environment**

### Step 3: Configure Protection Rules (Optional)
- Leave unchecked (no approvals needed for staging)

### Step 4: Add Environment Secrets
Click "Add environment secret" for each:

**Secret 1: K8S_NAMESPACE**
```
Name:  K8S_NAMESPACE
Value: buildnest-staging
```

**Secret 2: DOCKER_TAG**
```
Name:  DOCKER_TAG
Value: staging
```

**Secret 3: REPLICAS**
```
Name:  REPLICAS
Value: 2
```

**Secret 4: KUBECONFIG_STAGING** (if using K8s)
```
Name:  KUBECONFIG_STAGING
Value: (base64 encoded kubeconfig file)
```

### Step 5: Click "Save"

---

## 🟥 Create PRODUCTION Environment

### Step 1: Click "New environment"
- Button location: Right side of page

### Step 2: Enter Environment Name
```
Environment name: production
```
- Click **Configure environment**

### Step 3: Configure Protection Rules (REQUIRED)
✅ **Check**: "Required reviewers"
- Add reviewers: (select team members who can approve deployments)
- Minimum: 2 reviewers recommended

✅ **Check**: "Restrict to deployment branches"
- Select branch: `main` or `master`

### Step 4: Add Environment Secrets
Click "Add environment secret" for each:

**Secret 1: K8S_NAMESPACE**
```
Name:  K8S_NAMESPACE
Value: buildnest-production
```

**Secret 2: DOCKER_TAG**
```
Name:  DOCKER_TAG
Value: latest
```

**Secret 3: REPLICAS**
```
Name:  REPLICAS
Value: 3
```

**Secret 4: KUBECONFIG_PRODUCTION** (if using K8s)
```
Name:  KUBECONFIG_PRODUCTION
Value: (base64 encoded kubeconfig file)
```

### Step 5: Click "Save"

---

## ✅ Verification Checklist

After creating both environments, verify:

- [ ] **Staging environment exists** and is visible in Settings → Environments
- [ ] **Production environment exists** with 2+ reviewer requirement
- [ ] **Environment secrets are set** for both environments
- [ ] **Deployment branches are configured** for production
- [ ] **Can see environment in deploy workflow** (Actions tab)

---

## 🔑 Reference: Environment Variables vs Secrets

### Staging Environment Variables
| Variable | Value | Purpose |
|----------|-------|---------|
| K8S_NAMESPACE | buildnest-staging | Kubernetes namespace |
| DOCKER_TAG | staging | Docker image tag |
| REPLICAS | 2 | Pod replicas |

### Production Environment Variables
| Variable | Value | Purpose |
|----------|-------|---------|
| K8S_NAMESPACE | buildnest-production | Kubernetes namespace |
| DOCKER_TAG | latest | Docker image tag |
| REPLICAS | 3 | Pod replicas |

---

## 🔗 Using Environments in Workflows

Once created, workflows can reference environment variables:

```yaml
# Example from deploy.yml
deploy-staging:
  environment: staging
  runs-on: ubuntu-latest
  steps:
    - run: echo "Deploying to ${{ vars.K8S_NAMESPACE }}"
    - run: echo "Using image tag: ${{ vars.DOCKER_TAG }}"
```

---

## 📸 Screenshots Guide

### Step 1: Click Settings
```
Repository Page
   ├─ Code
   ├─ Issues
   ├─ Pull requests
   └─ Settings ← CLICK HERE
```

### Step 2: Navigate to Environments
```
Settings Left Sidebar
   ├─ General
   ├─ Collaborators & teams
   ├─ ... (scroll down)
   └─ Environments ← CLICK HERE
```

### Step 3: New Environment Button
```
Environments Page
   ┌─────────────────────────────┐
   │ [New environment] button    │  ← CLICK HERE
   └─────────────────────────────┘
```

---

## ⏱️ Time Estimate
- **Creating staging**: ~3 minutes
- **Creating production**: ~5 minutes
- **Total**: ~8 minutes

---

## 🆘 Troubleshooting

### "New environment" button not visible
- Check you're in the repository (not organization)
- Check you have admin permissions
- Try refreshing the page

### Can't see Environments menu
- Scroll down in left sidebar
- Make sure you're in Settings (not repository root)

### Environment not showing in workflow
- Refresh browser
- Check environment name matches workflow exactly
- Verify environment is public (not private)

---

## ✨ What's Next

After creating environments:
1. ✅ Add GitHub Secrets (DOCKER_USERNAME, DOCKER_PASSWORD)
2. ✅ **Create GitHub Environments (DONE)**
3. ⏳ Set Branch Protection Rules
4. ⏳ Test Pipeline with Sample Commit

See: [CICD_SETUP_GUIDE.md](CICD_SETUP_GUIDE.md) for complete setup
