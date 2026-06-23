package com.example.buildnest_ecommerce.model.elasticsearch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ElasticsearchAuditLog - Conditional Branch Coverage Tests")
class ElasticsearchAuditLogConditionalTest {

    @Test
    @DisplayName("ID field - null vs non-null branches")
    void testIdField() {
        ElasticsearchAuditLog log1 = new ElasticsearchAuditLog();
        log1.setId(null);
        assertNull(log1.getId());

        ElasticsearchAuditLog log2 = new ElasticsearchAuditLog();
        log2.setId("log-001");
        assertEquals("log-001", log2.getId());
    }

    @Test
    @DisplayName("UserId field - null and numeric variations")
    void testUserIdField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setUserId(null);
        assertNull(log.getUserId());

        log.setUserId(0L);
        assertEquals(0L, log.getUserId());

        log.setUserId(100L);
        assertEquals(100L, log.getUserId());
    }

    @Test
    @DisplayName("Action field - CREATE/UPDATE/DELETE variations")
    void testActionField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        // Null branch
        log.setAction(null);
        assertNull(log.getAction());

        // CREATE branch
        log.setAction("CREATE");
        assertEquals("CREATE", log.getAction());

        // UPDATE branch
        log.setAction("UPDATE");
        assertEquals("UPDATE", log.getAction());

        // DELETE branch
        log.setAction("DELETE");
        assertEquals("DELETE", log.getAction());
    }

    @Test
    @DisplayName("EntityType field - type variations")
    void testEntityTypeField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setEntityType(null);
        assertNull(log.getEntityType());

        log.setEntityType("User");
        assertEquals("User", log.getEntityType());

        log.setEntityType("Product");
        assertEquals("Product", log.getEntityType());
    }

    @Test
    @DisplayName("EntityId field - null and various IDs")
    void testEntityIdField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setEntityId(null);
        assertNull(log.getEntityId());

        log.setEntityId(0L);
        assertEquals(0L, log.getEntityId());

        log.setEntityId(999L);
        assertEquals(999L, log.getEntityId());
    }

    @Test
    @DisplayName("Timestamp field - null vs LocalDateTime")
    void testTimestampField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setTimestamp(null);
        assertNull(log.getTimestamp());

        LocalDateTime now = LocalDateTime.now();
        log.setTimestamp(now);
        assertEquals(now, log.getTimestamp());
    }

    @Test
    @DisplayName("IP Address field - null vs various IPs")
    void testIpAddressField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setIpAddress(null);
        assertNull(log.getIpAddress());

        log.setIpAddress("192.168.1.1");
        assertEquals("192.168.1.1", log.getIpAddress());

        log.setIpAddress("127.0.0.1");
        assertEquals("127.0.0.1", log.getIpAddress());
    }

    @Test
    @DisplayName("UserAgent field - null vs populated")
    void testUserAgentField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setUserAgent(null);
        assertNull(log.getUserAgent());

        log.setUserAgent("Mozilla/5.0");
        assertEquals("Mozilla/5.0", log.getUserAgent());
    }

    @Test
    @DisplayName("OldValue field - null vs value")
    void testOldValueField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setOldValue(null);
        assertNull(log.getOldValue());

        log.setOldValue("old_value");
        assertEquals("old_value", log.getOldValue());
    }

    @Test
    @DisplayName("NewValue field - null vs value")
    void testNewValueField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setNewValue(null);
        assertNull(log.getNewValue());

        log.setNewValue("new_value");
        assertEquals("new_value", log.getNewValue());
    }

    @Test
    @DisplayName("Severity field - null and severity levels")
    void testSeverityField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setSeverity(null);
        assertNull(log.getSeverity());

        log.setSeverity("INFO");
        assertEquals("INFO", log.getSeverity());

        log.setSeverity("WARN");
        assertEquals("WARN", log.getSeverity());

        log.setSeverity("ERROR");
        assertEquals("ERROR", log.getSeverity());

        log.setSeverity("CRITICAL");
        assertEquals("CRITICAL", log.getSeverity());
    }

    @Test
    @DisplayName("HTTP Status Code field - null and various codes")
    void testHttpStatusCodeField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setHttpStatusCode(null);
        assertNull(log.getHttpStatusCode());

        log.setHttpStatusCode(200);
        assertEquals(200, log.getHttpStatusCode());

        log.setHttpStatusCode(400);
        assertEquals(400, log.getHttpStatusCode());

        log.setHttpStatusCode(500);
        assertEquals(500, log.getHttpStatusCode());
    }

    @Test
    @DisplayName("ErrorCategory field - null and 5 error category branches")
    void testErrorCategoryField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        // Null branch
        log.setErrorCategory(null);
        assertNull(log.getErrorCategory());

        // SUCCESS (2xx)
        log.setErrorCategory("SUCCESS");
        assertEquals("SUCCESS", log.getErrorCategory());

        // CLIENT_ERROR (4xx)
        log.setErrorCategory("CLIENT_ERROR");
        assertEquals("CLIENT_ERROR", log.getErrorCategory());

        // SERVER_ERROR (5xx)
        log.setErrorCategory("SERVER_ERROR");
        assertEquals("SERVER_ERROR", log.getErrorCategory());

        // REDIRECT (3xx)
        log.setErrorCategory("REDIRECT");
        assertEquals("REDIRECT", log.getErrorCategory());
    }

    @Test
    @DisplayName("Endpoint field - null vs populated")
    void testEndpointField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setEndpoint(null);
        assertNull(log.getEndpoint());

        log.setEndpoint("/api/users");
        assertEquals("/api/users", log.getEndpoint());
    }

    @Test
    @DisplayName("AdditionalContext field - null vs populated map")
    void testAdditionalContextField() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();

        log.setAdditionalContext(null);
        assertNull(log.getAdditionalContext());

        Map<String, Object> context = new HashMap<>();
        context.put("key1", "value1");
        log.setAdditionalContext(context);
        assertEquals(1, log.getAdditionalContext().size());
    }

    @Test
    @DisplayName("Complete object with all 16 fields populated")
    void testCompleteElasticsearchAuditLog() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> context = new HashMap<>();
        context.put("requestId", "req-123");

        ElasticsearchAuditLog log = ElasticsearchAuditLog.builder()
                .id("log-001")
                .userId(100L)
                .action("UPDATE")
                .entityType("User")
                .entityId(999L)
                .timestamp(now)
                .ipAddress("192.168.1.1")
                .userAgent("Chrome/120")
                .oldValue("oldData")
                .newValue("newData")
                .severity("INFO")
                .httpStatusCode(200)
                .errorCategory("SUCCESS")
                .endpoint("/api/users/999")
                .additionalContext(context)
                .build();

        assertEquals("log-001", log.getId());
        assertEquals(100L, log.getUserId());
        assertEquals("UPDATE", log.getAction());
        assertEquals("User", log.getEntityType());
        assertEquals(999L, log.getEntityId());
        assertEquals(now, log.getTimestamp());
        assertEquals("192.168.1.1", log.getIpAddress());
        assertEquals("Chrome/120", log.getUserAgent());
        assertEquals("oldData", log.getOldValue());
        assertEquals("newData", log.getNewValue());
        assertEquals("INFO", log.getSeverity());
        assertEquals(200, log.getHttpStatusCode());
        assertEquals("SUCCESS", log.getErrorCategory());
        assertEquals("/api/users/999", log.getEndpoint());
        assertNotNull(log.getAdditionalContext());
    }

    @Test
    @DisplayName("NoArgsConstructor - default state")
    void testNoArgsConstructor() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();
        assertNull(log.getId());
        assertNull(log.getUserId());
        assertNull(log.getAction());
    }

    @Test
    @DisplayName("AllArgsConstructor - 16 parameter constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ElasticsearchAuditLog log = new ElasticsearchAuditLog(
                "log-001", 100L, "CREATE", "User", 50L, now,
                "192.168.1.1", "Mozilla/5.0", "oldVal", "newVal",
                "WARN", 201, "CLIENT_ERROR", "/api/endpoint",
                new HashMap<>());

        assertNotNull(log.getId());
        assertEquals(100L, log.getUserId());
    }

    @Test
    @DisplayName("Builder pattern - fluent construction")
    void testBuilderPattern() {
        ElasticsearchAuditLog log = ElasticsearchAuditLog.builder()
                .id("audit-1")
                .userId(50L)
                .action("DELETE")
                .build();

        assertEquals("audit-1", log.getId());
        assertEquals(50L, log.getUserId());
        assertEquals("DELETE", log.getAction());
    }

    @Test
    @DisplayName("Lombok equals() method")
    void testEquals() {
        ElasticsearchAuditLog log1 = new ElasticsearchAuditLog();
        log1.setId("log-001");
        log1.setUserId(100L);

        ElasticsearchAuditLog log2 = new ElasticsearchAuditLog();
        log2.setId("log-001");
        log2.setUserId(100L);

        assertEquals(log1, log2);
    }

    @Test
    @DisplayName("Lombok hashCode() method")
    void testHashCode() {
        ElasticsearchAuditLog log1 = new ElasticsearchAuditLog();
        log1.setId("log-001");

        ElasticsearchAuditLog log2 = new ElasticsearchAuditLog();
        log2.setId("log-001");

        assertEquals(log1.hashCode(), log2.hashCode());
    }

    @Test
    @DisplayName("Lombok toString() method")
    void testToString() {
        ElasticsearchAuditLog log = new ElasticsearchAuditLog();
        log.setId("log-001");

        String str = log.toString();
        assertNotNull(str);
        assertTrue(str.contains("ElasticsearchAuditLog"));
    }

    @Test
    @DisplayName("Multiple audit logs with different actions in loop")
    void testMultipleAuditLogsWithDifferentActions() {
        String[] actions = { "CREATE", "UPDATE", "DELETE" };

        for (int i = 0; i < 10; i++) {
            ElasticsearchAuditLog log = new ElasticsearchAuditLog();
            log.setId("log-" + i);
            log.setUserId((long) i);
            log.setAction(actions[i % 3]); // Cycles through CREATE, UPDATE, DELETE

            assertNotNull(log.getId());
            assertTrue(log.getUserId() >= 0);
            assertTrue(actions[i % 3].equals(log.getAction()));
        }
    }
}
