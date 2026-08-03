---
title: Container Image Registries — GHCR, Alternatives, and Free Tiers
category: tooling
tags: [container-registry, ghcr, docker-hub, ecr, gar, acr, harbor, ci-cd, devops, free-tier]
keywords: [GHCR, GitHub Container Registry, container registry, Docker Hub, ECR, Artifact Registry, Azure Container Registry, Harbor, Quay.io, GitLab Registry, free tier, self-hosted, OCI, image hosting]
objective: Explain what GHCR is, map the meaningful alternatives across cloud-managed and self-hosted options, and document which tiers are free and under what constraints — so an engineer can make a grounded registry choice without surveying every provider independently.
audience: Engineers evaluating or switching container registries, setting up CI/CD pipelines, or optimising infrastructure costs.
scope: general
source_conversations: [3b0d1946-012f-43db-86d6-22e7ae35d163]
last_updated: 2026-08-03
confidence: high
evidence_strength: moderate
related_articles: [devops-toolchain-inventory-and-verified-status.md, env-example-template-vs-env-local-secrets.md]
status: published
---

# Container Image Registries — GHCR, Alternatives, and Free Tiers

## Table of Contents

- [What Is It?](#what-is-it)
- [Why It Matters](#why-it-matters)
- [How It Works](#how-it-works)
  - [GHCR — GitHub Container Registry](#ghcr--github-container-registry)
  - [Alternative Registries](#alternative-registries)
  - [Free Tiers at a Glance](#free-tiers-at-a-glance)
  - [Self-Hosted Options (Fully Free)](#self-hosted-options-fully-free)
- [When to Use It](#when-to-use-it)
- [Examples](#examples)
- [Synthesis](#synthesis)
- [Quick Reference](#quick-reference)
- [References](#references)
- [Related Articles](#related-articles)

---

## What Is It?

A **container image registry** is a content-addressable storage and distribution service for OCI (Open Container Initiative) and Docker-format images. Engineers push built images to a registry and pull them back on deployment targets, CI runners, or Kubernetes clusters.

**GHCR** (`ghcr.io`) is GitHub's own container registry, part of GitHub Packages. It allows Docker and OCI images to be published, managed, and consumed natively within the GitHub ecosystem. Images can be linked directly to repositories, permissioned independently of source code visibility, and pulled anonymously when marked public.

---

## Why It Matters

Every containerised application — whether a microservice, a sidecar, a build tool, or a database — must be stored somewhere between `docker build` and `docker run`. The choice of registry affects:

- **Cost** — free tiers vary enormously; wrong choice incurs unexpected egress or storage bills.
- **Performance** — co-locating the registry with the deployment target (e.g., ECR with EKS) eliminates cross-region transfer latency and egress cost.
- **Security posture** — integrated IAM, image signing, and vulnerability scanning differ significantly between providers.
- **CI/CD friction** — a registry native to the CI platform (GHCR with GitHub Actions, GitLab Registry with GitLab CI) removes the need for additional credential secrets and simplifies pipeline configuration.
- **Data sovereignty** — cloud-managed registries store images in a vendor's infrastructure; self-hosted registries keep images on your own hardware.

Choosing a registry without understanding these dimensions leads to silent egress bills, insecure credential handling, or unnecessarily complex pipeline setup.

---

## How It Works

### GHCR — GitHub Container Registry

GHCR is accessed at `ghcr.io`. Key mechanics:

- **Authentication:** Uses a GitHub Personal Access Token (PAT) with `read:packages` and/or `write:packages` scopes, or the automatically provisioned `GITHUB_TOKEN` inside GitHub Actions (no extra secrets required).
- **Image addressing:** `ghcr.io/<owner>/<image-name>:<tag>` — `owner` is a GitHub username or organisation.
- **Permissions:** Container image visibility is configured independently of the source repository; a public image can coexist with a private repository, and vice versa.
- **Anonymous pull:** Public images on `ghcr.io` are pullable without any credentials.
- **GitHub Actions integration:** Outbound data transfer to GitHub Actions runners is free and unlimited, regardless of the free-tier storage/transfer caps that apply to external consumers.

```bash
# Authenticate
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Build and tag
docker build -t ghcr.io/username/image-name:latest .

# Push
docker push ghcr.io/username/image-name:latest

# Pull (public image — no auth required)
docker pull ghcr.io/username/image-name:latest
```

### Alternative Registries

Alternatives fall into three groups:

#### Developer Platforms (Git-Integrated)

| Registry | URL | Native CI | Notes |
|---|---|---|---|
| **Docker Hub** | `docker.io` | Any | The community default; largest public image library. Anonymous pull-rate limits apply (100 pulls/6 h per IP). Free plan: 1 private repo only. |
| **GitLab Container Registry** | `registry.gitlab.com` | GitLab CI | Tightly integrated with GitLab; cleanup policies, per-project/group scoping. Free tier shares the namespace's 10 GB storage quota. |
| **Quay.io** (Red Hat) | `quay.io` | Any | Enterprise-grade, provider-neutral; includes Clair vulnerability scanning and automated build triggers. No free private tier. |

#### Cloud-Provider Managed Registries

Best when your compute already runs on the same cloud. Avoids cross-provider egress fees and integrates with the provider's IAM.

| Registry | Provider | Best for |
|---|---|---|
| **Elastic Container Registry (ECR)** | AWS | EKS, ECS, Lambda; both public gallery and private registries |
| **Artifact Registry (GAR)** | Google Cloud | GKE, Cloud Run; also supports Helm, Maven, npm in the same service |
| **Azure Container Registry (ACR)** | Microsoft Azure | AKS, App Services; geo-replication available |
| **DigitalOcean Container Registry** | DigitalOcean | Smaller teams deploying on DigitalOcean Droplets or DOKS |

#### Self-Hosted

| Registry | Licence | Notable Features |
|---|---|---|
| **Harbor** (CNCF graduated) | Apache 2.0 | RBAC, Trivy vulnerability scanning, Notary image signing, multi-registry replication, web UI |
| **Docker Registry (`registry:2`)** | Apache 2.0 | Barebones; single `docker run` to start; no UI, no scanning; lowest operational overhead |
| **Sonatype Nexus OSS** | Apache 2.0 | Universal artifact repo (Docker + npm + Maven + PyPI + Helm in one service) |
| **JFrog Artifactory OSS** | Apache 2.0 | Same universal-repo positioning as Nexus; Artifactory is the commercial variant |

### Free Tiers at a Glance

"Free" applies differently to public vs. private images and self-hosted setups. The table below summarises the cloud-managed options:

| Registry | Public Images | Private Storage | Private Transfer | Key Limits |
|---|---|---|---|---|
| **GHCR** | Unlimited | 500 MB | 1 GB/month outbound | Transfer to GitHub Actions runners: free and unlimited. Overage is billed per GB. |
| **GitLab Registry** | Unlimited | Up to 10 GB\* | Unmetered | \*Shared across the whole namespace (code, LFS, registry). Largest free private allocation of any Git-native registry. |
| **Docker Hub** | Unlimited | 1 private repo | Unmetered | Anonymous pull-rate limits (100/6 h per IP); authenticated free users get 200/6 h. Free tier: 1 private repository only — not 1 GB. |
| **AWS ECR Public** | 50 GB storage; 500 GB/month egress | 500 MB (AWS Free Tier, first 12 months only) | 1 GB/month after free tier | Free tier expires; private ECR pricing can be significant at scale. |
| **Quay.io** | Unlimited | None on free plan | N/A | Private repositories require a paid subscription. |
| **GCP Artifact Registry** | N/A | 0.5 GB/month | 1 GB/month (within same region free) | Standard GCP pricing applies beyond the free allowance. |
| **Azure ACR** | N/A | No sustained free tier | N/A | Basic SKU (~$5/month); no free private tier. |
| **AWS ECR Private** | N/A | 500 MB (first 12 months) | 1 GB/month | Free tier is time-limited; not a long-term free option. |

### Self-Hosted Options (Fully Free)

If you control a server, VPS, or Kubernetes cluster, the three self-hosted registries listed above are open-source and have no per-image, per-user, or per-repository licensing cost. You pay only for the underlying compute and storage you already own.

The minimal viable self-hosted option:

```bash
# Start the official Docker registry on port 5000
docker run -d -p 5000:5000 --restart always --name registry registry:2
```

For production use with access control, scanning, and a web UI, Harbor is the CNCF-graduated standard — deploy via Helm on any Kubernetes cluster.

---

## When to Use It

| Scenario | Recommended Choice |
|---|---|
| Source code is on GitHub; CI uses GitHub Actions | **GHCR** — zero extra credentials, Actions egress is free |
| Source code and CI are on GitLab | **GitLab Container Registry** — same reasoning |
| Production deployment is on AWS (EKS/ECS) | **AWS ECR** — eliminates cross-provider egress; IAM role auth |
| Production deployment is on GCP (GKE/Cloud Run) | **Google Artifact Registry** — Workload Identity, same-region egress free |
| Production deployment is on Azure (AKS) | **Azure Container Registry** — managed identity auth |
| Public OSS project needing widest pull compatibility | **Docker Hub** or **GHCR** — both serve public images with no auth |
| Free private registry with the most storage | **GitLab Registry** (10 GB free, shared namespace) |
| Full data sovereignty, on-premises or private cloud | **Harbor** (self-hosted) |
| Smallest possible operational footprint, no UI needed | **`registry:2`** (self-hosted, single container) |
| All artifact types (Docker + Maven + npm) in one service | **Nexus OSS** or **JFrog Artifactory OSS** (self-hosted) |

---

## Examples

### Example 1 — GHCR in a GitHub Actions CI Workflow (real pattern)

```yaml
# .github/workflows/build.yml
jobs:
  build-and-push:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write          # required for ghcr.io push

    steps:
      - uses: actions/checkout@v4

      - name: Log in to GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}   # no extra secret needed

      - name: Build and push
        uses: docker/build-push-action@v5
        with:
          push: true
          tags: ghcr.io/${{ github.repository }}:latest
```

The `GITHUB_TOKEN` is automatically provisioned per-run; no PAT is created or rotated manually. Transfer from GHCR to the Actions runner is not counted against the 1 GB/month free outbound transfer cap.

### Example 2 — GitLab Registry in a GitLab CI Pipeline (illustrative)

```yaml
# .gitlab-ci.yml
build:
  image: docker:24
  services:
    - docker:24-dind
  variables:
    IMAGE_TAG: $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
    - docker build -t $IMAGE_TAG .
    - docker push $IMAGE_TAG
```

All three variables (`CI_REGISTRY_USER`, `CI_REGISTRY_PASSWORD`, `CI_REGISTRY`) are automatically injected by GitLab CI — no manual credential setup required.

### Example 3 — Harbor Helm Deployment (illustrative skeleton)

```bash
helm repo add harbor https://helm.goharbor.io
helm install harbor harbor/harbor \
  --namespace harbor --create-namespace \
  --set expose.type=ingress \
  --set expose.ingress.hosts.core=registry.example.com \
  --set externalURL=https://registry.example.com
```

After deployment, push images to `registry.example.com/<project>/<image>:<tag>` using standard `docker login` with a Harbor local user or OIDC-federated credentials.

---

## Synthesis

The container registry decision is not primarily a features decision — the major registries share most capabilities (push/pull, tagging, access control). It is primarily a **cost topology and integration friction** decision:

1. **Match the registry to your Git platform for CI.** GHCR for GitHub; GitLab Registry for GitLab. This eliminates manual credential management entirely.
2. **Match the registry to your runtime cloud for deployment.** ECR for AWS, GAR for GCP, ACR for Azure. Keeping images and compute in the same provider eliminates egress charges and simplifies IAM.
3. **Use self-hosted only when you have a specific reason.** Compliance, data sovereignty, or a universal artifact store requirement justifies the operational overhead of running and maintaining Harbor or Nexus. Without such a requirement, a managed registry is simpler.
4. **The free tier is adequate for most projects at early and mid stage**, provided you choose correctly: GitLab Registry offers the most private storage free (10 GB shared namespace); GHCR offers the smoothest GitHub Actions experience free. Docker Hub's free tier is effectively limited to public images in practice (1 private repository only).

The decision matters most once you have real workloads. For a new project, default to whichever registry matches your CI platform, and revisit when egress bills or compliance requirements give you a concrete reason to switch.

---

## Quick Reference

| Question | Answer |
|---|---|
| What does GHCR stand for? | GitHub Container Registry |
| GHCR image address format? | `ghcr.io/<owner>/<image>:<tag>` |
| Does GHCR require auth to pull public images? | No — public images are pullable anonymously |
| Free private storage (GHCR)? | 500 MB storage, 1 GB/month outbound transfer |
| Free private storage (GitLab)? | Up to 10 GB (shared across whole namespace) |
| Docker Hub free private repos? | 1 private repository only |
| Best free registry for GitHub Actions CI? | GHCR — `GITHUB_TOKEN` works natively; Actions egress is unlimited free |
| Best self-hosted option with full features? | Harbor (CNCF graduated project) |
| Best universal artifact store (self-hosted, free)? | Nexus OSS or JFrog Artifactory OSS |
| When does ECR free tier expire? | After 12 months (AWS Free Tier) |

---

## References

- [GitHub Packages — GHCR documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Docker Hub pricing and rate limits](https://www.docker.com/pricing/)
- [GitLab Container Registry documentation](https://docs.gitlab.com/ee/user/packages/container_registry/)
- [Harbor — CNCF project page](https://goharbor.io/)
- [AWS ECR Public Gallery](https://docs.aws.amazon.com/AmazonECR/latest/public/public-registries.html)
- [Google Artifact Registry overview](https://cloud.google.com/artifact-registry/docs/overview)
- [OCI Distribution Specification](https://github.com/opencontainers/distribution-spec)

---

## Related Articles

- [DevOps Toolchain Inventory and Verified Status](devops-toolchain-inventory-and-verified-status.md)
- [`.env.example` Template vs `.env` Local Secrets](env-example-template-vs-env-local-secrets.md)
