package com.example.buildnest_ecommerce.service.elasticsearch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("ThresholdManagementService tests")
@SuppressWarnings("unchecked")
class ThresholdManagementServiceTest {

    @Test
    @DisplayName("Should get and set thresholds")
    void testThresholdAccess() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn(null);

        ThresholdManagementService service = new ThresholdManagementService(redisTemplate);

        assertEquals(80.0, service.getCpuThreshold());
        service.setCpuThreshold(70.0);
        verify(valueOps).set(eq("threshold:cpu"), eq(70.0));

        assertEquals(90.0, service.getMemoryThreshold());
        service.setMemoryThreshold(85.0);
        verify(valueOps).set(eq("threshold:memory"), eq(85.0));

        assertEquals(5.0, service.getErrorRateThreshold());
        service.setErrorRateThreshold(2.0);
        verify(valueOps).set(eq("threshold:error-rate"), eq(2.0));
    }

    @Test
    @DisplayName("Should manage HTTP status thresholds and resets")
    void testHttpStatusThresholdsAndReset() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn(null);

        ThresholdManagementService service = new ThresholdManagementService(redisTemplate);

        assertEquals(10, service.getHttpStatusThreshold(500));
        service.setHttpStatusThreshold(500, 15);
        verify(valueOps).set(eq("threshold:http-status:500"), eq(15));

        Map<String, Object> all = service.getAllThresholds();
        assertTrue(all.containsKey("cpuThreshold"));

        service.resetAllThresholds();
        verify(redisTemplate).delete(anyList());
    }

    @Test
    @DisplayName("Should manage additional thresholds and read stored values")
    void testAdditionalThresholds() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        when(valueOps.get(eq("threshold:response-time"))).thenReturn("1234");
        when(valueOps.get(eq("threshold:failed-logins"))).thenReturn("7");
        when(valueOps.get(eq("threshold:jwt-refresh-failures"))).thenReturn("4");
        when(valueOps.get(eq("threshold:admin-operations"))).thenReturn("15");

        ThresholdManagementService service = new ThresholdManagementService(redisTemplate);

        assertEquals(1234L, service.getResponseTimeThreshold());
        assertEquals(7, service.getFailedLoginThreshold());
        assertEquals(4, service.getJwtRefreshThreshold());
        assertEquals(15, service.getAdminOperationsThreshold());

        service.setResponseTimeThreshold(2500L);
        verify(valueOps).set(eq("threshold:response-time"), eq(2500L));

        service.setFailedLoginThreshold(9);
        verify(valueOps).set(eq("threshold:failed-logins"), eq(9));

        service.setJwtRefreshThreshold(6);
        verify(valueOps).set(eq("threshold:jwt-refresh-failures"), eq(6));

        service.setAdminOperationsThreshold(20);
        verify(valueOps).set(eq("threshold:admin-operations"), eq(20));
    }
}
