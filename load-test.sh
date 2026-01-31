#!/bin/bash

# load-test.sh - Performance Load Testing Script
# PERFORMANCE_OPTIMIZATION_GUIDE - Section 8: Load Testing
# Generated: January 31, 2026
#
# This script runs load tests against the BuildNest application using Apache Bench (ab).
# Results are saved to a file for analysis.
#
# Prerequisites:
#   - Apache Bench (ab) installed: apt-get install apache2-utils
#   - Application running on http://localhost:8080
#
# Usage:
#   ./load-test.sh [endpoint] [concurrent_users] [total_requests]
#   ./load-test.sh (uses defaults)
#   ./load-test.sh "/api/products" 100 10000

set -e

# Default test configuration (PERFORMANCE_OPTIMIZATION_GUIDE - Section 8)
ENDPOINT="${1:-/api/products}"
CONCURRENT_USERS="${2:-50}"
REQUESTS="${3:-5000}"
BASE_URL="http://localhost:8080"
TARGET_URL="${BASE_URL}${ENDPOINT}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULTS_FILE="load-test-results_${TIMESTAMP}.tsv"
SUMMARY_FILE="load-test-summary_${TIMESTAMP}.txt"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║  BuildNest E-Commerce Performance Load Test                ║"
echo "║  PERFORMANCE_OPTIMIZATION_GUIDE - Section 8                ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "Configuration:"
echo "  Target URL:           $TARGET_URL"
echo "  Concurrent Users:     $CONCURRENT_USERS"
echo "  Total Requests:       $REQUESTS"
echo "  Results File:         $RESULTS_FILE"
echo "  Summary File:         $SUMMARY_FILE"
echo ""

# Check if ab is installed
if ! command -v ab &> /dev/null; then
    echo "ERROR: Apache Bench (ab) not found. Install with: apt-get install apache2-utils"
    exit 1
fi

# Check if application is running
echo "Checking application availability..."
if ! curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" | grep -q "200"; then
    echo "ERROR: Application not responding at $BASE_URL"
    exit 1
fi
echo "✓ Application is running"
echo ""

# Run load test
echo "Starting load test (this may take several minutes)..."
echo "=================================================="
echo ""

# Run Apache Bench with output to both console and file
ab -n "$REQUESTS" \
   -c "$CONCURRENT_USERS" \
   -g "$RESULTS_FILE" \
   "$TARGET_URL" 2>&1 | tee "$SUMMARY_FILE"

echo ""
echo "=================================================="
echo "Load test completed!"
echo ""
echo "Results saved:"
echo "  - Summary:  $SUMMARY_FILE"
echo "  - Detailed: $RESULTS_FILE (TSV format for gnuplot)"
echo ""

# Extract and display key metrics (PERFORMANCE_OPTIMIZATION_GUIDE Baselines)
echo "Performance Analysis (PERFORMANCE_OPTIMIZATION_GUIDE Baselines):"
echo "================================================================"

# Parse results file for key metrics
if [ -f "$SUMMARY_FILE" ]; then
    
    # Extract metrics using grep and awk
    TIME_TAKEN=$(grep "Time taken for tests:" "$SUMMARY_FILE" | awk '{print $NF}' | sed 's/seconds//')
    REQ_PER_SEC=$(grep "Requests per second:" "$SUMMARY_FILE" | awk '{print $NF}')
    MEAN_TIME=$(grep "Time per request:" "$SUMMARY_FILE" | head -1 | awk '{print $NF}')
    FAILED=$(grep "Failed requests:" "$SUMMARY_FILE" | awk '{print $NF}')
    ERROR_RATE="0%"
    
    if [ "$FAILED" != "0" ]; then
        ERROR_RATE=$(echo "scale=2; ($FAILED / $REQUESTS) * 100" | bc)"%"
    fi
    
    echo ""
    echo "Key Metrics:"
    echo "  Total Time:            $TIME_TAKEN seconds"
    echo "  Requests/Second:       $REQ_PER_SEC req/s"
    echo "  Mean Response Time:    $MEAN_TIME ms"
    echo "  Failed Requests:       $FAILED (Error Rate: $ERROR_RATE)"
    echo ""
    
    # Performance Assessment (PERFORMANCE_OPTIMIZATION_GUIDE Table)
    echo "Assessment (vs PERFORMANCE_OPTIMIZATION_GUIDE Baselines):"
    echo "  Expected GET /api/products: 50-100ms"
    echo "  Expected Throughput:        > 1000 req/s"
    echo "  Expected Error Rate:        0%"
    echo ""
    
    if (( $(echo "$REQ_PER_SEC > 1000" | bc -l) )); then
        echo "  ✓ Throughput EXCELLENT: $REQ_PER_SEC req/s"
    elif (( $(echo "$REQ_PER_SEC > 500" | bc -l) )); then
        echo "  ⚠ Throughput GOOD: $REQ_PER_SEC req/s"
    else
        echo "  ✗ Throughput NEEDS IMPROVEMENT: $REQ_PER_SEC req/s"
    fi
    
    if (( $(echo "$MEAN_TIME < 200" | bc -l) )); then
        echo "  ✓ Response Time EXCELLENT: ${MEAN_TIME}ms"
    elif (( $(echo "$MEAN_TIME < 500" | bc -l) )); then
        echo "  ⚠ Response Time ACCEPTABLE: ${MEAN_TIME}ms"
    else
        echo "  ✗ Response Time NEEDS OPTIMIZATION: ${MEAN_TIME}ms"
    fi
    
    if [ "$ERROR_RATE" == "0%" ]; then
        echo "  ✓ Error Rate EXCELLENT: $ERROR_RATE"
    else
        echo "  ✗ Error Rate: $ERROR_RATE (investigate failures)"
    fi
fi

echo ""
echo "Next Steps:"
echo "  1. Review full results: cat $SUMMARY_FILE"
echo "  2. Visualize: gnuplot -e \"set terminal dumb; plot '$RESULTS_FILE'\""
echo "  3. If performance poor, refer to PERFORMANCE_OPTIMIZATION_GUIDE troubleshooting"
echo "  4. Check monitoring: http://localhost:8080/actuator/prometheus"
echo ""

# Recommendations
if (( $(echo "$REQ_PER_SEC < 500" | bc -l) )) || (( $(echo "$MEAN_TIME > 500" | bc -l) )); then
    echo "⚠ Performance Issues Detected! Recommendations:"
    echo "  1. Increase database connection pool (PERFORMANCE_OPTIMIZATION_GUIDE Section 3)"
    echo "  2. Verify cache hit rate (PERFORMANCE_OPTIMIZATION_GUIDE Section 4)"
    echo "  3. Check database slow query log (PERFORMANCE_OPTIMIZATION_GUIDE Section 3)"
    echo "  4. Monitor Redis connectivity (PERFORMANCE_OPTIMIZATION_GUIDE Section 4)"
    echo "  5. Review JVM heap usage (PERFORMANCE_OPTIMIZATION_GUIDE Section 2)"
fi

echo ""
echo "Load test script completed successfully!"
