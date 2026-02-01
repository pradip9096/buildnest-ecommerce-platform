package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.entity.AuditLog;
import com.example.buildnest_ecommerce.service.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditLogControllerTest {

    @Test
    void getAuditLogsReturnsPage() {
        AuditLogService auditLogService = mock(AuditLogService.class);
        Page<AuditLog> page = new PageImpl<>(Collections.singletonList(new AuditLog()));
        when(auditLogService.getAllAuditLogs(any())).thenReturn(page);

        AuditLogController controller = new AuditLogController(auditLogService);
        assertEquals(HttpStatus.OK, controller.getAuditLogs(0, 20).getStatusCode());
    }
}
