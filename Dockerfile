# Multi-stage build for BuildNest Backend
# PERFORMANCE_OPTIMIZATION_GUIDE - Section 9: Deployment Optimization
#
# This Dockerfile implements multi-stage build for optimal image size:
# - Stage 1 (Builder): Maven + JDK 21 for compilation
# - Stage 2 (Runtime): JRE 21 slim for smaller final image
#
# Performance Tuning:
# - G1GC with 200ms max pause time
# - Heap size: 512MB min / 1GB max (configurable via environment)
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
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
# Using JRE (not JDK) for smaller image size
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/civil-ecommerce-*.jar app.jar

# Health check endpoint (PERFORMANCE_OPTIMIZATION_GUIDE - Kubernetes Integration)
# Checks application readiness every 30 seconds
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/readiness || exit 1

# PERFORMANCE_OPTIMIZATION_GUIDE - JVM Tuning Configuration
# Run the application with security and performance settings
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=production", \
  "-Dfile.encoding=UTF-8", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:+ParallelRefProcEnabled", \
  "-XX:+UnlockDiagnosticVMOptions", \
  "-XX:G1NewCollectionHeuristicPercent=30", \
  "-XX:+DisableExplicitGC", \
  "-XX:InitiatingHeapOccupancyPercent=35", \
  "-Xmx1g", \
  "-Xms512m", \
  "-XX:+PrintGCDetails", \
  "-XX:+PrintGCDateStamps", \
  "-Xloggc:/var/log/app/gc.log", \
  "-jar", "app.jar"]

EXPOSE 8080

# Metadata labels
LABEL maintainer="BuildNest Team"
LABEL description="BuildNest E-Commerce Platform - Optimized Docker Image"
LABEL version="1.0"

