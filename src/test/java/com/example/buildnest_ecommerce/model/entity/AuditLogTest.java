package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    @Test
    void builderAndAccessorsWork() {
        LocalDateTime now = LocalDateTime.now();
        AuditLog log = AuditLog.builder()
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

        assertEquals(1L, log.getId());
        assertEquals(2L, log.getUserId());
        assertEquals("LOGIN", log.getAction());
        assertEquals("AUTH", log.getEntityType());
        assertEquals(3L, log.getEntityId());
        assertEquals(now, log.getTimestamp());
        assertEquals("127.0.0.1", log.getIpAddress());
        assertEquals("agent", log.getUserAgent());
        assertEquals("old", log.getOldValue());
        assertEquals("new", log.getNewValue());
        assertEquals(200, log.getHttpStatusCode());
        assertEquals("SUCCESS", log.getErrorCategory());

        log.setAction("LOGOUT");
        assertEquals("LOGOUT", log.getAction());
    }
}
