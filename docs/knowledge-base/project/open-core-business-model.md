# Open-Core Business Model — Free OSS + Paid Cloud Services

**Category:** Software Industry > Business Models  
**Tags:** `open-source`, `saas`, `open-core`, `pricing`, `business-model`  
**Last Updated:** 2026-06-30

---

## Overview

The **open-core model** is a software business strategy where the core product is open source (free, MIT or similar licence), and additional cloud-hosted services or enterprise features are sold on top of it. It is one of the most common monetisation strategies for developer tools.

---

## How It Works

```
Open Source Core (free, runs locally)
         +
Cloud Services (paid, runs on vendor servers)
         =
Open-Core Product
```

The free tier gives you the software itself — you install it, run it locally, and use it without paying. The paid tier gives you **automation, scalability, and convenience** that requires the vendor's infrastructure.

---

## Real Examples

| Product | Free (OSS) | Paid (Cloud) |
|---|---|---|
| **SonarQube** | Community Edition — run locally, manual analysis | SonarQube Cloud — auto-triggers on PR, hosted analysis, private repos |
| **GitLab** | Self-hosted Community Edition | GitLab.com cloud, enterprise SSO, advanced CI/CD |
| **Elasticsearch** | Open source engine | Elastic Cloud — managed hosting, APM, security features |
| **ECC (Everything Claude Code)** | MIT-licensed agents, skills, hooks — run locally | Cloud GitHub App — auto-triggers on PR, private repo scanning |
| **Grafana** | Open source dashboards | Grafana Cloud — hosted, alerting, Loki, Tempo |

---

## Free Tier vs Paid Tier — Core Difference

| | Free (OSS) | Paid (Cloud) |
|---|---|---|
| **Where it runs** | Your machine | Vendor's servers |
| **Trigger** | You manually invoke it | Automated by events (e.g., push, PR open) |
| **Private repos** | Often not supported | Supported (requires auth) |
| **Public repos** | Supported | Supported (often free) |
| **Setup** | You install and configure | Vendor manages it |
| **Updates** | You pull new versions | Automatic |

---

## Why Public Repos Are Often Free

Both the OSS tier and the paid tier typically give free access for **public repositories**. The business logic:

- Public repos have no secrets — lower risk for the vendor
- Open source maintainers are an influential demographic who spread the word
- Closed-source commercial projects represent the paying customer base

**Example:** SonarQube Cloud is free for unlimited analysis of public GitHub repos. ECC's free tier covers public repos with 10 analyses/month.

---

## GitHub App vs Local Tool

In the open-core model, the paid tier typically delivers value through a **GitHub App**:

| | Local Tool (free) | GitHub App (paid) |
|---|---|---|
| **What it is** | Software on your machine | Bot connected to your GitHub account |
| **Trigger** | You run it manually | GitHub events (push, PR open, merge) |
| **Result** | Output in your terminal | Comment or review on the PR in GitHub |
| **Team benefit** | One person at a time | Fires for every developer on every PR |
| **Setup** | Clone and configure | Install GitHub App, grant repo access |

---

## When to Use Free vs Paid

| Situation | Recommendation |
|---|---|
| Solo developer, public project | Free OSS — full value, no cost |
| Solo developer, private project | Evaluate: is automation worth the cost? |
| Small team, private repo | Paid — consistent automated reviews on every PR without manual effort |
| Large org, compliance needs | Enterprise — SSO, audit logs, SLA |

---

## Open-Core vs Pure Open Source vs Pure SaaS

| Model | Example | Code available? | Paid for what? |
|---|---|---|---|
| **Pure OSS** | Linux, PostgreSQL | Yes | Nothing (community) or support |
| **Open-core** | SonarQube, GitLab, ECC | Core only | Cloud services, enterprise features |
| **Pure SaaS** | GitHub, Slack | No | Everything — software + hosting |

---

## Key Insight

> Installing the open-source repo gives you the **same algorithms and logic** as the paid tier. What you pay for is **automation, hosting, and integration** — not better intelligence.

For developer tools like code analysers or Claude Code extensions, the OSS version running locally with Claude Code can often match or exceed the paid tier's analytical quality. The paid tier's value is **removing the manual trigger** — it just happens on every PR without anyone remembering to run it.

---

## See Also

- `docs/knowledge-base/claude-code-extension-mechanisms.md` — ECC is an open-core Claude Code extension pack
