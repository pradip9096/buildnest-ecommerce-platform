# Git & GitHub Backup - Standard Operating Procedure (SOP)

## Document Information
- **Project**: BuildNest E-Commerce Platform
- **Date Created**: January 31, 2026
- **Purpose**: Version control and cloud backup configuration
- **Status**: ✅ OPERATIONAL

---

## 1. WHAT WAS ACCOMPLISHED

### 1.1 Git Repository Initialization
- **Action**: Initialized local Git repository
- **Command**: `git init`
- **Result**: Created `.git/` directory in project root
- **Files Tracked**: 358 files (113,635 lines of code)
- **Initial Commit**: `9612b98` - "Initial commit"

### 1.2 Git Configuration
```powershell
git config user.name "Project User"
git config user.email "user@civil-ecommerce.local"
```

### 1.3 Remote Repository Setup
- **Platform**: GitHub
- **Repository URL**: https://github.com/pradip9096/buildnest-ecommerce-platform.git
- **Owner**: pradip9096
- **Visibility**: Private (recommended for commercial projects)

### 1.4 Authentication Configuration
- **Method**: Personal Access Token (PAT)
- **Token Scopes Required**:
  - ✅ `repo` - Full control of private repositories
  - ✅ `workflow` - Update GitHub Actions workflow files
- **Token Location**: Stored in Git Credential Manager

### 1.5 Cloud Backup Status
- **Total Commits Pushed**: 2 commits
  - Commit 1: `44f568b` - Initial code push (483 objects, 773.92 KiB)
  - Commit 2: `7a3d206` - GitHub Actions workflows (7 objects, 4.84 KiB)
- **Files Protected**: All 358 source files + CI/CD workflows
- **Backup Location**: GitHub cloud (globally redundant)

---

## 2. DAILY WORKFLOW - HOW TO USE GIT

### 2.1 Before Making Changes
```powershell
# Navigate to project directory
cd "d:\CDAC Project\civil-ecommerce\civil-ecommerce"

# Check current status
git status

# Ensure you have latest code (if working in team)
git pull origin master
```

### 2.2 After Making Code Changes
```powershell
# Step 1: Check what changed
git status

# Step 2: Review changes in detail (optional)
git diff

# Step 3: Stage all changes
git add .

# Step 4: Commit with descriptive message
git commit -m "Brief description of what you changed"

# Step 5: Push to GitHub cloud backup
git push origin master
```

### 2.3 Commit Message Best Practices
```powershell
# Good examples:
git commit -m "Fix login authentication bug in UserController"
git commit -m "Add product search API endpoint"
git commit -m "Update security config with HTTPS validation"
git commit -m "Implement rate limiting for admin endpoints"

# Bad examples (too vague):
git commit -m "fix"
git commit -m "update"
git commit -m "changes"
```

---

## 3. RECOVERY PROCEDURES

### 3.1 Restore Deleted File
```powershell
# If file was deleted but not committed yet
git restore filename.java

# If file was deleted and committed
git log -- path/to/file.java  # Find commit hash before deletion
git checkout <commit-hash> -- path/to/file.java
```

### 3.2 Undo Last Commit (Not Pushed)
```powershell
# Keep changes but undo commit
git reset --soft HEAD~1

# Discard changes and undo commit (DANGEROUS)
git reset --hard HEAD~1
```

### 3.3 Revert to Previous Version
```powershell
# See commit history
git log --oneline

# Restore entire project to specific commit
git checkout <commit-hash>

# Return to latest version
git checkout master
```

### 3.4 Complete Disaster Recovery
If your local computer crashes or hard drive fails:

1. Install Git on new computer
2. Clone from GitHub:
   ```powershell
   git clone https://github.com/pradip9096/buildnest-ecommerce-platform.git
   cd buildnest-ecommerce-platform
   ```
3. All 358 files will be restored exactly as they were

---

## 4. GITHUB AUTHENTICATION MANAGEMENT

### 4.1 Current Token Configuration
- **Token Created**: January 31, 2026
- **Token Name**: "civil-ecommerce-backup" (recommended)
- **Expiration**: Check token settings regularly
- **Security**: Never commit token to code or share publicly

### 4.2 Token Rotation (When Expired)
1. Go to: https://github.com/settings/tokens
2. Generate new token with same scopes (`repo`, `workflow`)
3. Update credential:
   ```powershell
   git config --unset credential.helper
   git config credential.helper manager-core
   git push origin master  # Will prompt for new token
   ```

### 4.3 Multiple Computer Setup
If working from multiple computers:
1. Each computer needs Git installed
2. Clone repository: `git clone <repo-url>`
3. Authenticate with same PAT token
4. Always `git pull` before making changes
5. Always `git push` after committing changes

---

## 5. VERIFICATION CHECKLIST

### 5.1 Daily Health Check
- [ ] Run `git status` - should show "working tree clean" or list uncommitted changes
- [ ] Run `git log --oneline -5` - should see recent commits
- [ ] Visit GitHub repository URL - should see latest commit timestamp matches local

### 5.2 Monthly Verification
- [ ] Verify token hasn't expired (Settings → Tokens)
- [ ] Check GitHub repository size (should be ~1 MB currently)
- [ ] Test disaster recovery:
  ```powershell
  # Clone to temporary directory
  git clone https://github.com/pradip9096/buildnest-ecommerce-platform.git temp-test
  cd temp-test
  # Verify files present
  ls -r
  # Delete test clone
  cd ..
  Remove-Item -Recurse -Force temp-test
  ```

---

## 6. TROUBLESHOOTING

### 6.1 "Authentication failed"
**Cause**: Token expired or invalid
**Solution**:
1. Generate new token at https://github.com/settings/tokens
2. Ensure `repo` and `workflow` scopes are checked
3. Use new token when prompted

### 6.2 "Remote rejected - workflow scope"
**Cause**: Token missing `workflow` scope
**Solution**:
1. Edit token at https://github.com/settings/tokens
2. Check the `workflow` box
3. Click "Update token"
4. Push again: `git push origin master`

### 6.3 "Working tree has uncommitted changes"
**Cause**: Local changes not committed
**Solution**:
```powershell
# Option 1: Commit changes
git add .
git commit -m "Description of changes"
git push origin master

# Option 2: Discard changes (CAREFUL!)
git restore .
```

### 6.4 "Merge conflict"
**Cause**: Different changes on GitHub vs local
**Solution**:
```powershell
git pull origin master
# Git will show conflict markers in files
# Edit files to resolve conflicts
git add .
git commit -m "Resolve merge conflicts"
git push origin master
```

---

## 7. SECURITY BEST PRACTICES

### 7.1 What NOT to Commit
- ❌ API keys, passwords, secrets
- ❌ Database credentials
- ❌ Personal access tokens
- ❌ `application-prod.properties` with real credentials
- ❌ Large binary files (videos, images > 10MB)
- ❌ Compiled files (`.class`, `target/` folder)

### 7.2 `.gitignore` File Status
Current `.gitignore` should exclude:
- `target/` - Maven build outputs
- `*.class` - Compiled Java files
- `*.log` - Log files
- `.env` - Environment variables
- IDE files (`.idea/`, `.vscode/`)

### 7.3 Audit Trail
Every change is tracked with:
- Who made the change (user.name/user.email)
- When it was made (timestamp)
- What changed (diff)
- Why it was made (commit message)

---

## 8. CURRENT REPOSITORY STATE

### 8.1 Branch Structure
- **Active Branch**: `master`
- **Total Branches**: 1 (currently single-branch workflow)
- **Recommended**: Keep master stable, create feature branches for major changes

### 8.2 File Statistics
```
Total Files: 358
Total Lines: 113,635
Repository Size: ~1 MB (compressed)
```

### 8.3 Key Directories Protected
- ✅ `src/main/java/` - All Java source code
- ✅ `src/main/resources/` - Configuration files
- ✅ `src/test/java/` - All test files
- ✅ `pom.xml` - Maven dependencies
- ✅ `.github/workflows/` - CI/CD pipelines
- ✅ `kubernetes/` - Deployment manifests
- ✅ `terraform/` - Infrastructure as Code

---

## 9. ADVANCED OPERATIONS (OPTIONAL)

### 9.1 Create Feature Branch
```powershell
# Create and switch to new branch
git checkout -b feature/new-payment-gateway

# Make changes and commit
git add .
git commit -m "Add Stripe payment integration"

# Push branch to GitHub
git push origin feature/new-payment-gateway

# Merge back to master when ready
git checkout master
git merge feature/new-payment-gateway
git push origin master
```

### 9.2 View File History
```powershell
# See all changes to specific file
git log --follow -- src/main/java/com/example/SecurityConfig.java

# See who changed what line
git blame src/main/java/com/example/SecurityConfig.java
```

### 9.3 Compare Versions
```powershell
# Compare working directory to last commit
git diff

# Compare two commits
git diff 9612b98 7a3d206

# Compare specific file across commits
git diff 9612b98 7a3d206 -- pom.xml
```

---

## 10. CONTACT & ESCALATION

### 10.1 GitHub Repository Access
- **Repository Owner**: pradip9096
- **Access Level**: Full control (owner)
- **Team Members**: Add collaborators at Settings → Collaborators

### 10.2 Support Resources
- **Git Documentation**: https://git-scm.com/doc
- **GitHub Guides**: https://guides.github.com/
- **Token Management**: https://github.com/settings/tokens
- **Repository Settings**: https://github.com/pradip9096/buildnest-ecommerce-platform/settings

---

## 11. MAINTENANCE SCHEDULE

| Task | Frequency | Command |
|------|-----------|---------|
| Commit changes | After each coding session | `git add . && git commit -m "msg"` |
| Push to GitHub | End of work day | `git push origin master` |
| Pull latest code | Start of work day | `git pull origin master` |
| Verify token | Monthly | Check https://github.com/settings/tokens |
| Test disaster recovery | Quarterly | Clone to temp directory and verify |
| Review commit history | As needed | `git log --oneline --graph` |

---

## 12. REVISION HISTORY

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| 2026-01-31 | 1.0 | Initial SOP creation - Git/GitHub setup complete | GitHub Copilot |

---

## APPENDIX A: USEFUL GIT COMMANDS REFERENCE

```powershell
# Status & Information
git status                          # Show working tree status
git log --oneline -10               # Show last 10 commits
git log --graph --all               # Visualize branch history
git show <commit-hash>              # Show details of specific commit
git diff                            # Show unstaged changes
git diff --staged                   # Show staged changes

# Basic Operations
git add .                           # Stage all changes
git add filename.java               # Stage specific file
git commit -m "message"             # Commit staged changes
git push origin master              # Push to GitHub
git pull origin master              # Pull from GitHub

# Undo Operations
git restore filename.java           # Discard changes to file
git restore --staged filename.java  # Unstage file
git reset --soft HEAD~1             # Undo last commit, keep changes
git reset --hard HEAD~1             # Undo last commit, discard changes (DANGEROUS)

# Branch Operations
git branch                          # List branches
git checkout -b new-branch          # Create and switch to branch
git checkout master                 # Switch to master branch
git merge feature-branch            # Merge branch into current branch

# Remote Operations
git remote -v                       # Show remote URLs
git remote add origin <url>         # Add remote repository
git fetch origin                    # Download changes without merging
git clone <url>                     # Clone repository to new location

# Inspection
git log --follow -- filename.java   # File history
git blame filename.java             # Who changed each line
git diff HEAD~1 HEAD                # Compare last two commits
```

---

## APPENDIX B: EMERGENCY CONTACTS

**Critical Issues (Data Loss, Security Breach)**
- Immediately notify: Repository owner (pradip9096)
- Backup verification: Check GitHub repository immediately
- Lock compromised tokens: https://github.com/settings/tokens

**Non-Critical Issues (Questions, Training)**
- Consult this SOP document first
- Review Git documentation: https://git-scm.com/doc
- GitHub community support: https://github.community/

---

**END OF DOCUMENT**

*This SOP should be reviewed and updated whenever Git/GitHub configuration changes.*
