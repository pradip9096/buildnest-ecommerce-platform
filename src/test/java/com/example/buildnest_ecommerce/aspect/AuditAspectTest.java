package com.example.buildnest_ecommerce.aspect;

import com.example.buildnest_ecommerce.security.CustomUserDetails;
import com.example.buildnest_ecommerce.service.audit.AuditLogService;
import com.example.buildnest_ecommerce.service.elasticsearch.ElasticsearchIngestionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void auditMethodLogsActionAndUsesForwardedIp() throws Throwable {
        AuditLogService auditLogService = mock(AuditLogService.class);
        ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
        AuditAspect aspect = new AuditAspect(auditLogService);
        ReflectionTestUtils.setField(aspect, "elasticsearchIngestionService", ingestionService);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        Method method = TestTarget.class.getMethod("updateUser", Long.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[] { 1L });
        TestEntity result = new TestEntity(1L);
        when(joinPoint.proceed()).thenReturn(result);

        setAuthContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Object returned = aspect.auditMethod(joinPoint);

        assertSame(result, returned);
        ArgumentCaptor<Object> oldValueCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);

        verify(auditLogService).logAction(eq(1L), eq("UPDATE_USER"), eq("USER"), eq(1L),
                ipCaptor.capture(), eq("JUnit"), oldValueCaptor.capture(), eq(result));
        assertEquals("203.0.113.1", ipCaptor.getValue());
        assertNotNull(oldValueCaptor.getValue());

        verify(ingestionService).indexAuditLog(eq(1L), eq("UPDATE_USER"), eq("USER"), eq(1L),
                eq("203.0.113.1"), eq("JUnit"), anyString(), anyString());
    }

    @Test
    void auditMethodUsesRemoteAddrWhenNoForwardedHeader() throws Throwable {
        AuditLogService auditLogService = mock(AuditLogService.class);
        AuditAspect aspect = new AuditAspect(auditLogService);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        Method method = TestTarget.class.getMethod("viewUser", Long.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[] { 2L });
        TestEntity result = new TestEntity(2L);
        when(joinPoint.proceed()).thenReturn(result);

        setAuthContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.10");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        aspect.auditMethod(joinPoint);

        verify(auditLogService).logAction(eq(1L), eq("VIEW_USER"), eq("USER"), eq(2L),
                eq("192.168.1.10"), isNull(), isNull(), eq(result));
    }

    @Test
    void auditMethodContinuesWhenElasticsearchFails() throws Throwable {
        AuditLogService auditLogService = mock(AuditLogService.class);
        ElasticsearchIngestionService ingestionService = mock(ElasticsearchIngestionService.class);
        AuditAspect aspect = new AuditAspect(auditLogService);
        ReflectionTestUtils.setField(aspect, "elasticsearchIngestionService", ingestionService);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        Method method = TestTarget.class.getMethod("updateUser", Long.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[] { 1L });
        TestEntity result = new TestEntity(1L);
        when(joinPoint.proceed()).thenReturn(result);
        doThrow(new RuntimeException("fail")).when(ingestionService)
                .indexAuditLog(any(), any(), any(), any(), any(), any(), any(), any());

        setAuthContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Object returned = aspect.auditMethod(joinPoint);

        assertSame(result, returned);
        verify(auditLogService).logAction(eq(1L), eq("UPDATE_USER"), eq("USER"), eq(1L),
                eq("10.0.0.1"), isNull(), any(), eq(result));
    }

    @Test
    void auditMethodRethrowsException() throws Throwable {
        AuditLogService auditLogService = mock(AuditLogService.class);
        AuditAspect aspect = new AuditAspect(auditLogService);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestTarget.class.getMethod("updateUser", Long.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[] { 1L });
        when(joinPoint.proceed()).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> aspect.auditMethod(joinPoint));
        verify(auditLogService, never()).logAction(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void auditMethodHandlesMissingAuthAndRequest() throws Throwable {
        AuditLogService auditLogService = mock(AuditLogService.class);
        AuditAspect aspect = new AuditAspect(auditLogService);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        Method method = TestTarget.class.getMethod("viewUser", Long.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[] { 5L });
        when(joinPoint.proceed()).thenReturn(new Object());

        Object returned = aspect.auditMethod(joinPoint);

        assertNotNull(returned);
        verify(auditLogService).logAction(isNull(), eq("VIEW_USER"), eq("USER"), eq(5L),
                isNull(), isNull(), isNull(), any());
    }

    private void setAuthContext() {
        CustomUserDetails details = new CustomUserDetails(1L, "user", "u@example.com", "pass",
                Collections.emptyList(), true, true, true, true);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(details, null,
                details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    static class TestTarget {
        @Auditable(action = "UPDATE_USER", entityType = "USER")
        public TestEntity updateUser(Long id) {
            return new TestEntity(id);
        }

        @Auditable(action = "VIEW_USER", entityType = "USER")
        public TestEntity viewUser(Long id) {
            return new TestEntity(id);
        }
    }

    static class TestEntity {
        private final Long id;

        TestEntity(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        @Override
        public String toString() {
            return "TestEntity{" + "id=" + id + '}';
        }
    }
}
