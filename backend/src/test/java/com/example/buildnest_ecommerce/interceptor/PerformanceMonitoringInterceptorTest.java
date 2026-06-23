package com.example.buildnest_ecommerce.interceptor;

import com.example.buildnest_ecommerce.service.monitoring.PerformanceMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceMonitoringInterceptorTest {

    @Mock
    private PerformanceMonitoringService performanceMonitoringService;

    @InjectMocks
    private PerformanceMonitoringInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handler = new Object();
    }

    @Test
    void preHandleShouldSetStartTimeAttribute() throws Exception {
        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result);
        assertNotNull(request.getAttribute("startTime"));
        assertTrue((Long) request.getAttribute("startTime") > 0);
    }

    @Test
    void afterCompletionShouldRecordResponseTime() throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis() - 100);
        request.setMethod("GET");
        request.setRequestURI("/api/products");

        interceptor.afterCompletion(request, response, handler, null);

        ArgumentCaptor<String> endpointCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(performanceMonitoringService).recordResponseTime(endpointCaptor.capture(), timeCaptor.capture());

        assertEquals("GET /api/products", endpointCaptor.getValue());
        assertTrue(timeCaptor.getValue() >= 100);
    }

    @Test
    void afterCompletionWithoutStartTimeShouldNotRecordResponseTime() throws Exception {
        interceptor.afterCompletion(request, response, handler, null);

        verify(performanceMonitoringService, never()).recordResponseTime(anyString(), anyLong());
    }

    @Test
    void afterCompletionWithSlowRequestShouldLogWarning() throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis() - 1500);
        request.setMethod("POST");
        request.setRequestURI("/api/orders");
        response.setStatus(200);

        interceptor.afterCompletion(request, response, handler, null);

        verify(performanceMonitoringService).recordResponseTime(anyString(), longThat(time -> time >= 1500));
    }

    @Test
    void afterCompletionWithExceptionShouldNotThrow() throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());
        doThrow(new RuntimeException("Service error")).when(performanceMonitoringService)
                .recordResponseTime(anyString(), anyLong());

        assertDoesNotThrow(() -> interceptor.afterCompletion(request, response, handler, null));
    }
}
