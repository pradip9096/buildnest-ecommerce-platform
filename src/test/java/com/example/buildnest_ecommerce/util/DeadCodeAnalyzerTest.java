package com.example.buildnest_ecommerce.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeadCodeAnalyzerTest {

    @Test
    void analyzeDeadCodeReturnsSummary() {
        DeadCodeAnalyzer analyzer = new DeadCodeAnalyzer();
        String summary = analyzer.analyzeDeadCode();
        assertNotNull(summary);
        assertTrue(summary.contains("Dead Code Removal Summary"));
    }
}
