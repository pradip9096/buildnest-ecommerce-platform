# Multi-stage build for BuildNest Backend (#124, OPS-06)
# PERFORMANCE_OPTIMIZATION_GUIDE - Section 9: Deployment Optimization
#
# This Dockerfile implements multi-stage build for optimal image size:
# - Stage 1 (Builder): Maven + JDK 21 for compilation
# - Stage 2 (Runtime): JRE 21 Alpine, non-root, for a minimal production image
#
# Performance Tuning:
# - G1GC with 200ms max pause time
# - Container-aware heap sizing via -XX:MaxRAMPercentage (no fixed -Xmx/-Xms)
# - Parallel reference processing for faster GC
# - Disable explicit GC to prevent full GC pauses

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Build the application
COPY src ./src
RUN ./mvnw clean package -DskipTests \
  && find target -maxdepth 1 -name '*.jar' \
       ! -name '*-javadoc.jar' ! -name '*-sources.jar' ! -name '*.jar.original' \
       -exec cp {} target/app.jar \;

# Stage 2: Runtime
# Using JRE (not JDK) on an Alpine base for the smallest final image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user (OPS-06 acceptance criterion) — Alpine uses addgroup/adduser, not
# useradd/groupadd. Own the app and GC-log directories so the JVM can write to both.
# curl is installed because the production profile (see ENTRYPOINT below) force-enables
# HTTPS server-wide (SecurityConfig.validateHttpsInProduction) with a self-signed or
# internal CA cert — busybox's own wget has no flag to skip TLS verification, so it
# cannot reach an HTTPS-only health endpoint the way curl -k can.
RUN apk add --no-cache curl \
  && addgroup -S buildnest && adduser -S buildnest -G buildnest \
  && mkdir -p /var/log/app \
  && chown -R buildnest:buildnest /app /var/log/app

# Copy built JAR from builder stage
COPY --from=builder --chown=buildnest:buildnest /app/target/app.jar app.jar

USER buildnest

# Health check endpoint (PERFORMANCE_OPTIMIZATION_GUIDE - Kubernetes Integration)
# Checks application readiness every 30 seconds over HTTPS (-k: the production profile's
# cert is self-signed/internal-CA, not a publicly trusted cert).
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -sk -f https://localhost:8080/actuator/health/readiness || exit 1

# PERFORMANCE_OPTIMIZATION_GUIDE - JVM Tuning Configuration
# Run the application with security and performance settings
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=production,logstash", \
  "-Dfile.encoding=UTF-8", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:+ParallelRefProcEnabled", \
  "-XX:+DisableExplicitGC", \
  "-XX:InitiatingHeapOccupancyPercent=35", \
  "-Xlog:gc*:file=/var/log/app/gc.log:time,uptime:filecount=5,filesize=10M", \
  "-jar", "app.jar"]

EXPOSE 8080

# Metadata labels
LABEL maintainer="BuildNest Team"
LABEL description="BuildNest E-Commerce Platform - Optimized Docker Image"
LABEL version="1.0"

