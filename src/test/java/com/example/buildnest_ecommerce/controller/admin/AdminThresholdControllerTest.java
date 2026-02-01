package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.service.elasticsearch.ThresholdManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminThresholdControllerTest {

    @Test
    void getAndSetThresholds() {
        ThresholdManagementService service = mock(ThresholdManagementService.class);
        when(service.getAllThresholds()).thenReturn(Map.of("cpu", 80));
        when(service.getCpuThreshold()).thenReturn(80.0);
        when(service.getMemoryThreshold()).thenReturn(75.0);
        when(service.getErrorRateThreshold()).thenReturn(5.0);
        when(service.getResponseTimeThreshold()).thenReturn(500L);
        when(service.getFailedLoginThreshold()).thenReturn(3);
        when(service.getJwtRefreshThreshold()).thenReturn(2);
        when(service.getAdminOperationsThreshold()).thenReturn(10);

        AdminThresholdController controller = new AdminThresholdController(service);

        assertEquals(HttpStatus.OK, controller.getAllThresholds().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getCpuThreshold().getStatusCode());
        assertEquals(HttpStatus.OK, controller.setCpuThreshold(50).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getMemoryThreshold().getStatusCode());
        assertEquals(HttpStatus.OK, controller.setMemoryThreshold(50).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getErrorRateThreshold().getStatusCode());
        assertEquals(HttpStatus.OK, controller.setErrorRateThreshold(1.0).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getResponseTimeThreshold().getStatusCode());
        assertEquals(HttpStatus.OK, controller.setResponseTimeThreshold(100).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getFailedLoginThreshold().getStatusCode());
        assertEquals(HttpStatus.OK, controller.setFailedLoginThreshold(2).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getJwtRefreshThreshold().getStatusCode());
        assertEquals(HttpStatus.OK, controller.setJwtRefreshThreshold(2).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAdminOperationsThreshold().getStatusCode());
        assertEquals(HttpStatus.OK, controller.setAdminOperationsThreshold(2).getStatusCode());
        assertEquals(HttpStatus.OK, controller.resetThresholds().getStatusCode());
    }

    @Test
    void invalidThresholdsReturnBadRequest() {
        ThresholdManagementService service = mock(ThresholdManagementService.class);
        AdminThresholdController controller = new AdminThresholdController(service);

        assertEquals(HttpStatus.BAD_REQUEST, controller.setCpuThreshold(-1).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.setCpuThreshold(101).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.setMemoryThreshold(0).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.setMemoryThreshold(101).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.setErrorRateThreshold(-1).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.setResponseTimeThreshold(0).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.setFailedLoginThreshold(0).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.setJwtRefreshThreshold(0).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.setAdminOperationsThreshold(0).getStatusCode());
    }

    @Test
    void getAllThresholdsHandlesErrors() {
        ThresholdManagementService service = mock(ThresholdManagementService.class);
        when(service.getAllThresholds()).thenThrow(new RuntimeException("fail"));

        AdminThresholdController controller = new AdminThresholdController(service);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAllThresholds().getStatusCode());
    }
}
