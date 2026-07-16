# Multi-stage build for BuildNest Frontend (React/Vite)
#
# Stage 1 (Builder): Node.js — installs deps and runs `npm run build`
# Stage 2 (Runtime): nginx-unprivileged — serves the built static assets
#   as a non-root user on port 8080, matching OPS-07's acceptance criteria.

# Stage 1: Build
FROM node:22-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
# --legacy-peer-deps: eslint-plugin-react@7.37.5's peer range caps at eslint ^9.7,
# but the repo pins eslint ^10.3.0 — a pre-existing conflict unrelated to this
# container build (see issue #125's follow-up for the real dependency fix).
RUN npm ci --legacy-peer-deps

COPY . .
RUN npm run build

# Stage 2: Runtime
# nginxinc/nginx-unprivileged already runs as a non-root user and listens
# on 8080 by default — all nginx working directories are pre-owned by it.
FROM nginxinc/nginx-unprivileged:1.27-alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY security-headers.conf /etc/nginx/security-headers.conf

HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget -q --spider http://localhost:8080/ || exit 1

EXPOSE 8080

LABEL maintainer="BuildNest Team"
LABEL description="BuildNest E-Commerce Platform - Frontend (nginx, non-root)"
