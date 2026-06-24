# BuildNest

> E-commerce platform for home construction materials and home décor products.

[![CI](https://github.com/pradip9096/buildnest-ecommerce-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/pradip9096/buildnest-ecommerce-platform/actions/workflows/ci.yml)
[![Security](https://github.com/pradip9096/buildnest-ecommerce-platform/actions/workflows/security.yml/badge.svg)](https://github.com/pradip9096/buildnest-ecommerce-platform/actions/workflows/security.yml)
[![Version](https://img.shields.io/badge/version-0.4.0--SNAPSHOT-blue)](CHANGELOG.md)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running Tests](#running-tests)
- [API Documentation](#api-documentation)
- [Environment Variables](#environment-variables)
- [CI/CD](#cicd)
- [Roadmap](#roadmap)
- [Changelog](#changelog)

---

## Overview

BuildNest is a full-stack e-commerce platform targeting the home construction and décor market. It exposes a Spring Boot REST API backed by MySQL, Redis, and Elasticsearch, with a React frontend under active development.

**Key capabilities:**

- Product catalogue with category hierarchy, search, and dual API versioning (v1/v2)
- Shopping cart, wishlist, and checkout flows
- Order management with inventory tracking
- JWT-based authentication with refresh token rotation and role/permission RBAC
- Audit logging via AOP — every auditable action is captured and ingested into Elasticsearch
- Redis-backed rate limiting (Bucket4j) and response caching
- Elasticsearch-driven metrics collection, alerting, and query optimisation
- Circuit breaker and time-limiter resilience patterns (Resilience4j)
- Prometheus metrics endpoint for observability

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   React 19 Frontend                  │  :5173 (dev)
└──────────────────────────┬──────────────────────────┘
                           │ HTTP / REST
┌──────────────────────────▼──────────────────────────┐
│             Spring Boot 3.5 REST API                 │  :8080
│  Controllers → Services → Repositories               │
│  JWT Auth · Rate Limiting · AOP Audit · Resilience4j │
└────────┬───────────────┬───────────────┬────────────┘
         │               │               │
    ┌────▼────┐    ┌──────▼──────┐  ┌───▼───────────┐
    │ MySQL   │    │    Redis     │  │ Elasticsearch  │
    │  8.2    │    │   7-alpine   │  │    8.17.6      │
    │ (JPA +  │    │ (Rate limit  │  │ (Audit logs,   │
    │Liquibase│    │  + Cache)    │  │  metrics,      │
    └─────────┘    └─────────────┘  │  alerting)     │
                                    └────────────────┘
                                           │
                                    ┌──────▼──────┐
                                    │  Logstash   │
                                    │  + Kibana   │
                                    │   8.17.6    │
                                    └─────────────┘
```

Schema changes are managed exclusively through **Liquibase** changesets (`backend/db/changelog/`). DDL auto is set to `validate` — no schema changes via Hibernate.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security, JWT (access 15 min / refresh 30 days) |
| Persistence | Spring Data JPA, Hibernate, MySQL 8.2 |
| Migrations | Liquibase |
| Cache / Rate Limiting | Redis 7, Bucket4j |
| Search / Analytics | Elasticsearch 8.17.6, Spring Data Elasticsearch |
| Logging | Logback, Logstash JSON encoder, Kibana |
| Resilience | Resilience4j (circuit breaker, time limiter) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Observability | Spring Boot Actuator, Prometheus |
| Build | Maven 3, Maven Wrapper |
| Containerisation | Docker, Docker Compose |
| Frontend | React 19, Vite |
| CI/CD | GitHub Actions |
| Security scanning | OWASP Dependency-Check (CVSS ≥ 7.0 fails build), CodeQL |
| Test quality | JaCoCo (≥ 50% instruction coverage), PIT mutation testing (≥ 75%) |

---

## Project Structure

```
BuildNest/
├── backend/                        # Spring Boot API
│   ├── src/
│   │   ├── main/java/com/example/buildnest_ecommerce/
│   │   │   ├── controller/
│   │   │   │   ├── public_/        # Unauthenticated endpoints
│   │   │   │   ├── auth/           # Login, registration, password reset
│   │   │   │   ├── user/           # Customer-facing endpoints
│   │   │   │   ├── admin/          # Admin-only endpoints
│   │   │   │   └── inventory/      # Inventory status
│   │   │   ├── service/            # Domain services (interface + impl)
│   │   │   ├── model/
│   │   │   │   ├── entity/         # JPA entities
│   │   │   │   ├── dto/            # Output DTOs
│   │   │   │   ├── payload/        # Request/response payloads
│   │   │   │   └── elasticsearch/  # Elasticsearch document models
│   │   │   ├── repository/         # Spring Data JPA + Elasticsearch repositories
│   │   │   ├── security/           # JWT filter, entry point, token provider
│   │   │   ├── config/             # Spring configuration beans
│   │   │   ├── aspect/             # @Auditable AOP aspect
│   │   │   ├── interceptor/        # Rate-limit headers, API sunset warnings
│   │   │   └── exception/          # GlobalExceptionHandler + domain exceptions
│   │   └── main/resources/
│   │       ├── application.properties
│   │       ├── db/changelog/       # Liquibase changesets
│   │       └── logback-spring.xml
│   ├── docker-compose.yml
│   ├── .env.example                # All 62 required variables documented
│   └── pom.xml
├── frontend/                       # React 19 / Vite (stub)
├── docs/SDLC-docs/                 # SRS, SDD, RTM, Test Plan, SDP, and more
├── CHANGELOG.md
└── README.md
```

---

## Prerequisites

| Tool | Version |
|---|---|
| JDK | 21+ |
| Maven | 3.9+ (or use `./mvnw`) |
| Docker & Docker Compose | 24+ |
| Node.js | 20+ (frontend only) |

---

## Getting Started

### 1. Clone and configure

```bash
git clone https://github.com/pradip9096/buildnest-ecommerce-platform.git
cd buildnest-ecommerce-platform/backend
cp .env.example .env
# Edit .env and populate all required values (see .env.example for descriptions)
```

### 2. Start infrastructure

```bash
# Minimum required services
docker compose up -d mysql redis elasticsearch

# Full stack (includes Kibana, Logstash, Prometheus)
docker compose up -d
```

### 3. Run the API

```bash
./mvnw spring-boot:run
# API available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui/index.html
```

### 4. Run the frontend (optional)

```bash
cd ../frontend
npm install
npm run dev
# Dev server at http://localhost:5173
```

---

## Running Tests

All commands run from the `backend/` directory.

```bash
# Full test suite
./mvnw test

# Single test class
./mvnw test -Dtest=OrderServiceImplTest

# Single test method
./mvnw test -Dtest=OrderServiceImplTest#shouldPlaceOrder

# Coverage report (opens at target/site/jacoco/index.html)
./mvnw verify

# With OWASP Dependency-Check (slow — pulls NVD data)
./mvnw verify -Powasp
```

Integration tests use an H2 in-memory database and mock all Elasticsearch beans — no running infrastructure required for `./mvnw test`.

---

## API Documentation

Swagger UI is served at `/swagger-ui/index.html` when the application is running.

### Auth flow

```
POST /api/auth/register    → create account
POST /api/auth/login       → returns { accessToken, refreshToken }
POST /api/auth/refresh     → rotate refresh token, get new access token
POST /api/auth/logout      → invalidate refresh token
```

Access tokens expire after 15 minutes. Include them as `Authorization: Bearer <token>`.

### Versioning

Product endpoints are available at `/api/v1/products` (deprecated, sunset headers added) and `/api/v2/products`. All other endpoints are unversioned.

---

## Environment Variables

All 62 required variables are documented with descriptions and example values in `backend/.env.example`. Key groups:

| Group | Variables |
|---|---|
| Database | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` |
| Redis | `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` |
| Elasticsearch | `ELASTICSEARCH_HOST`, `ELASTICSEARCH_PORT`, `ELASTICSEARCH_USERNAME`, `ELASTICSEARCH_PASSWORD` |
| JWT | `JWT_SECRET`, `JWT_ACCESS_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS` |
| Mail | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` |

---

## CI/CD

GitHub Actions workflows in `.github/workflows/`:

| Workflow | Trigger | Purpose |
|---|---|---|
| `ci.yml` | Push / PR to master | Build, test, JaCoCo coverage gate (≥ 50%) |
| `security.yml` | Push / PR to master | CodeQL analysis, OWASP Dependency-Check |
| `deploy.yml` | Push to master | Docker image build and push |
| `performance.yml` | Manual / schedule | JMeter load test suite |

---

## Roadmap

| Milestone | Scope | Target | Status |
|---|---|---|---|
| M1 — Stabilisation | Critical test defect fixes | 2026-07-04 | **Complete** (v0.2.0) |
| M2 — Quality Foundation | Test coverage, OWASP, env docs | 2026-07-18 | **Complete** (v0.3.0) |
| M3 — Technical Debt Reduction | ES upgrade, CSP hardening, circuit breaker fallbacks, coverage gate 55% | 2026-08-01 | In progress |
| M4 — Feature Development | 50 features (#60–#109) | 2026-10-24 | Planned |
| M5 — Production Readiness | 27 hardening items (#110–#136) | 2026-11-21 | Planned → v1.0.0 |

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a full history of changes by version.
