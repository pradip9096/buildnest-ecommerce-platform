package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogCoverageTest {

    @Test
    void equalsAndHashCodeCoverNullsAndDifferences() {
        LocalDateTime now = LocalDateTime.now();

        AuditLog base = AuditLog.builder()
                .id(1L)
                .userId(2L)
                .action("LOGIN")
                .entityType("AUTH")
                .entityId(3L)
                .timestamp(now)
                .ipAddress("127.0.0.1")
                .userAgent("agent")
                .oldValue("old")
                .newValue("new")
                .httpStatusCode(200)
                .errorCategory("SUCCESS")
                .build();

        AuditLog same = AuditLog.builder()
                .id(1L)
                .userId(2L)
                .action("LOGIN")
                .entityType("AUTH")
                .entityId(3L)
                .timestamp(now)
                .ipAddress("127.0.0.1")
                .userAgent("agent")
                .oldValue("old")
                .newValue("new")
                .httpStatusCode(200)
                .errorCategory("SUCCESS")
                .build();

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, null);
        assertNotEquals(base, "not-audit");

        AuditLog diffAction = AuditLog.builder()
                .id(1L)
                .userId(2L)
                .action("LOGOUT")
                .entityType("AUTH")
                .entityId(3L)
                .timestamp(now)
                .httpStatusCode(200)
                .errorCategory("SUCCESS")
                .build();

        AuditLog diffStatus = AuditLog.builder()
                .id(1L)
                .userId(2L)
                .action("LOGIN")
                .entityType("AUTH")
                .entityId(3L)
                .timestamp(now)
                .httpStatusCode(500)
                .errorCategory("SERVER_ERROR")
                .build();

        assertNotEquals(base, diffAction);
        assertNotEquals(base, diffStatus);
    }

    @Test
    void toStringIncludesKeyFields() {
        AuditLog log = AuditLog.builder()
                .id(9L)
                .action("LOGIN")
                .entityType("AUTH")
                .httpStatusCode(200)
                .errorCategory("SUCCESS")
                .build();

        String text = log.toString();
        assertTrue(text.contains("AuditLog"));
        assertTrue(text.contains("LOGIN"));
        assertTrue(text.contains("AUTH"));
    }
}
