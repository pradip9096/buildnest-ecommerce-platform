package com.example.buildnest_ecommerce.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuditLog Branch Coverage Tests")
class AuditLogConditionalTest {

    @Test
    @DisplayName("Test ID field null and non-null")
    void testIdField() {
        AuditLog log1 = AuditLog.builder().id(null).build();
        assertNull(log1.getId());

        AuditLog log2 = AuditLog.builder().id(1L).build();
        assertEquals(1L, log2.getId());
    }

    @Test
    @DisplayName("Test userId field variations")
    void testUserIdField() {
        AuditLog log1 = AuditLog.builder().userId(null).build();
        assertNull(log1.getUserId());

        AuditLog log2 = AuditLog.builder().userId(0L).build();
        assertEquals(0L, log2.getUserId());

        AuditLog log3 = AuditLog.builder().userId(100L).build();
        assertEquals(100L, log3.getUserId());
    }

    @Test
    @DisplayName("Test action field variations")
    void testActionField() {
        AuditLog log1 = AuditLog.builder().action(null).build();
        assertNull(log1.getAction());

        AuditLog log2 = AuditLog.builder().action("CREATE").build();
        assertEquals("CREATE", log2.getAction());

        AuditLog log3 = AuditLog.builder().action("UPDATE").build();
        assertEquals("UPDATE", log3.getAction());

        AuditLog log4 = AuditLog.builder().action("DELETE").build();
        assertEquals("DELETE", log4.getAction());
    }

    @Test
    @DisplayName("Test entityType field")
    void testEntityTypeField() {
        AuditLog log1 = AuditLog.builder().entityType(null).build();
        assertNull(log1.getEntityType());

        AuditLog log2 = AuditLog.builder().entityType("User").build();
        assertEquals("User", log2.getEntityType());

        AuditLog log3 = AuditLog.builder().entityType("Product").build();
        assertEquals("Product", log3.getEntityType());
    }

    @Test
    @DisplayName("Test entityId field")
    void testEntityIdField() {
        AuditLog log1 = AuditLog.builder().entityId(null).build();
        assertNull(log1.getEntityId());

        AuditLog log2 = AuditLog.builder().entityId(0L).build();
        assertEquals(0L, log2.getEntityId());

        AuditLog log3 = AuditLog.builder().entityId(999L).build();
        assertEquals(999L, log3.getEntityId());
    }

    @Test
    @DisplayName("Test timestamp field")
    void testTimestampField() {
        AuditLog log1 = AuditLog.builder().timestamp(null).build();
        assertNull(log1.getTimestamp());

        LocalDateTime now = LocalDateTime.now();
        AuditLog log2 = AuditLog.builder().timestamp(now).build();
        assertEquals(now, log2.getTimestamp());
    }

    @Test
    @DisplayName("Test ipAddress field")
    void testIpAddressField() {
        AuditLog log1 = AuditLog.builder().ipAddress(null).build();
        assertNull(log1.getIpAddress());

        AuditLog log2 = AuditLog.builder().ipAddress("192.168.1.1").build();
        assertEquals("192.168.1.1", log2.getIpAddress());

        AuditLog log3 = AuditLog.builder().ipAddress("127.0.0.1").build();
        assertEquals("127.0.0.1", log3.getIpAddress());
    }

    @Test
    @DisplayName("Test userAgent field")
    void testUserAgentField() {
        AuditLog log1 = AuditLog.builder().userAgent(null).build();
        assertNull(log1.getUserAgent());

        AuditLog log2 = AuditLog.builder().userAgent("Mozilla/5.0").build();
        assertEquals("Mozilla/5.0", log2.getUserAgent());
    }

    @Test
    @DisplayName("Test oldValue field")
    void testOldValueField() {
        AuditLog log1 = AuditLog.builder().oldValue(null).build();
        assertNull(log1.getOldValue());

        AuditLog log2 = AuditLog.builder().oldValue("old_data").build();
        assertEquals("old_data", log2.getOldValue());
    }

    @Test
    @DisplayName("Test newValue field")
    void testNewValueField() {
        AuditLog log1 = AuditLog.builder().newValue(null).build();
        assertNull(log1.getNewValue());

        AuditLog log2 = AuditLog.builder().newValue("new_data").build();
        assertEquals("new_data", log2.getNewValue());
    }

    @Test
    @DisplayName("Test httpStatusCode field variations")
    void testHttpStatusCodeField() {
        AuditLog log1 = AuditLog.builder().httpStatusCode(null).build();
        assertNull(log1.getHttpStatusCode());

        AuditLog log2 = AuditLog.builder().httpStatusCode(200).build();
        assertEquals(200, log2.getHttpStatusCode());

        AuditLog log3 = AuditLog.builder().httpStatusCode(400).build();
        assertEquals(400, log3.getHttpStatusCode());

        AuditLog log4 = AuditLog.builder().httpStatusCode(500).build();
        assertEquals(500, log4.getHttpStatusCode());
    }

    @Test
    @DisplayName("Test errorCategory field variations")
    void testErrorCategoryField() {
        AuditLog log1 = AuditLog.builder().errorCategory(null).build();
        assertNull(log1.getErrorCategory());

        AuditLog log2 = AuditLog.builder().errorCategory("SUCCESS").build();
        assertEquals("SUCCESS", log2.getErrorCategory());

        AuditLog log3 = AuditLog.builder().errorCategory("CLIENT_ERROR").build();
        assertEquals("CLIENT_ERROR", log3.getErrorCategory());

        AuditLog log4 = AuditLog.builder().errorCategory("SERVER_ERROR").build();
        assertEquals("SERVER_ERROR", log4.getErrorCategory());

        AuditLog log5 = AuditLog.builder().errorCategory("REDIRECT").build();
        assertEquals("REDIRECT", log5.getErrorCategory());
    }

    @Test
    @DisplayName("Test complete AuditLog object")
    void testCompleteAuditLog() {
        LocalDateTime now = LocalDateTime.now();

        AuditLog log = AuditLog.builder()
                .id(1L)
                .userId(100L)
                .action("UPDATE")
                .entityType("User")
                .entityId(50L)
                .timestamp(now)
                .ipAddress("192.168.1.1")
                .userAgent("Chrome/90")
                .oldValue("old_email@example.com")
                .newValue("new_email@example.com")
                .httpStatusCode(200)
                .errorCategory("SUCCESS")
                .build();

        assertEquals(1L, log.getId());
        assertEquals(100L, log.getUserId());
        assertEquals("UPDATE", log.getAction());
        assertEquals("User", log.getEntityType());
        assertEquals(50L, log.getEntityId());
        assertEquals(now, log.getTimestamp());
        assertEquals("192.168.1.1", log.getIpAddress());
        assertEquals("Chrome/90", log.getUserAgent());
        assertEquals("old_email@example.com", log.getOldValue());
        assertEquals("new_email@example.com", log.getNewValue());
        assertEquals(200, log.getHttpStatusCode());
        assertEquals("SUCCESS", log.getErrorCategory());
    }

    @Test
    @DisplayName("Test no-args constructor")
    void testNoArgsConstructor() {
        AuditLog log = new AuditLog();
        assertNull(log.getId());
        assertNull(log.getUserId());
    }

    @Test
    @DisplayName("Test all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        AuditLog log = new AuditLog(
                1L, 100L, "CREATE", "User", 50L,
                now, "192.168.1.1", "Chrome", "old", "new", 200, "SUCCESS");

        assertEquals(1L, log.getId());
        assertEquals(100L, log.getUserId());
        assertEquals("CREATE", log.getAction());
    }

    @Test
    @DisplayName("Test equals and hashCode")
    void testEqualsAndHashCode() {
        AuditLog log1 = AuditLog.builder().id(1L).action("CREATE").build();
        AuditLog log2 = AuditLog.builder().id(1L).action("CREATE").build();

        assertEquals(log1, log2);
        assertEquals(log1.hashCode(), log2.hashCode());
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        AuditLog log = AuditLog.builder().id(1L).action("CREATE").build();
        String str = log.toString();
        assertNotNull(str);
        assertTrue(str.length() > 0);
    }

    @Test
    @DisplayName("Test different action types with id variations")
    void testMultipleActionTypesWithIds() {
        for (int i = 0; i < 10; i++) {
            AuditLog log = AuditLog.builder()
                    .id((long) i)
                    .userId(100L + i)
                    .action(i % 3 == 0 ? "CREATE" : i % 3 == 1 ? "UPDATE" : "DELETE")
                    .build();
            assertEquals((long) i, log.getId());
        }
    }

    @Test
    @DisplayName("Test field setters")
    void testFieldSetters() {
        AuditLog log = new AuditLog();

        log.setId(1L);
        assertEquals(1L, log.getId());

        log.setUserId(100L);
        assertEquals(100L, log.getUserId());

        log.setAction("UPDATE");
        assertEquals("UPDATE", log.getAction());

        log.setEntityType("Product");
        assertEquals("Product", log.getEntityType());

        log.setEntityId(50L);
        assertEquals(50L, log.getEntityId());

        log.setHttpStatusCode(404);
        assertEquals(404, log.getHttpStatusCode());

        log.setErrorCategory("CLIENT_ERROR");
        assertEquals("CLIENT_ERROR", log.getErrorCategory());
    }
}
