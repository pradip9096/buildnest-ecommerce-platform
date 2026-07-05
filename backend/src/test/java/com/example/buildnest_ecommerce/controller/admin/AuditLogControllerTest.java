package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.AuditLogPageDTO;
import com.example.buildnest_ecommerce.model.entity.AuditLog;
import com.example.buildnest_ecommerce.service.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditLogControllerTest {

    @Test
    void getAuditLogsReturnsPage() {
        AuditLogService auditLogService = mock(AuditLogService.class);
        Page<AuditLog> page = new PageImpl<>(Collections.singletonList(new AuditLog()));
        when(auditLogService.getAllAuditLogs(any())).thenReturn(AuditLogPageDTO.from(page));

        AuditLogController controller = new AuditLogController(auditLogService);
        ResponseEntity<AuditLogPageDTO> response = controller.getAuditLogs(0, 20);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements(), "response body must carry through the page's total element count");
    }
}
