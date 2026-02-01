package com.example.buildnest_ecommerce.service.audit;

import com.example.buildnest_ecommerce.model.entity.AuditLog;
import com.example.buildnest_ecommerce.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditLogServiceTest {

    @Test
    void logActionSavesAuditLogWithDefaults() {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();

        AuditLogService service = new AuditLogService(repository, objectMapper);

        service.logAction(null, null, null, 5L, "127.0.0.1", "agent", "old", "new");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(0L, saved.getUserId());
        assertEquals("UNKNOWN_ACTION", saved.getAction());
        assertEquals("UNKNOWN_ENTITY", saved.getEntityType());
        assertEquals(5L, saved.getEntityId());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    void logActionHandlesSerializationFailure() throws Exception {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("fail"));

        AuditLogService service = new AuditLogService(repository, objectMapper);
        service.logAction(1L, "ACT", "ENTITY", 2L, "ip", "ua", new Object(), new Object());

        verify(repository, never()).save(any());
    }

    @Test
    void queryMethodsReturnRepositoryResults() {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        AuditLogService service = new AuditLogService(repository, objectMapper);

        Page<AuditLog> page = new PageImpl<>(List.of(new AuditLog()));
        when(repository.findByUserId(eq(1L), any())).thenReturn(page);
        when(repository.findByAction(eq("LOGIN"), any())).thenReturn(page);
        when(repository.findByEntityTypeAndEntityId(eq("ORDER"), eq(2L), any())).thenReturn(page);
        when(repository.findByTimestampBetween(any(), any(), any())).thenReturn(page);
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        assertEquals(1, service.getAuditLogsByUserId(1L, PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, service.getAuditLogsByAction("LOGIN", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1,
                service.getAuditLogsByEntity("ORDER", 2L, PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, service.getAuditLogsByDateRange(LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, service.getAllAuditLogs(PageRequest.of(0, 10)).getTotalElements());
    }
}
