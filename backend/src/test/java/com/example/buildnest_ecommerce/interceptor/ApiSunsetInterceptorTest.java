package com.example.buildnest_ecommerce.interceptor;

import com.example.buildnest_ecommerce.annotation.ApiSunset;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiSunsetInterceptorTest {

    @InjectMocks
    private ApiSunsetInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void preHandleWithNonHandlerMethodShouldReturnTrue() throws Exception {
        Object handler = new Object();

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result);
    }

    @Test
    void preHandleWithNoSunsetAnnotationShouldReturnTrue() throws Exception {
        Method method = TestController.class.getMethod("normalMethod");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertNull(response.getHeader("X-API-Deprecated"));
    }

    @Test
    void preHandleWithFutureSunsetDateShouldAddHeadersAndReturnTrue() throws Exception {
        Method method = TestController.class.getMethod("deprecatedMethodFuture");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertEquals("true", response.getHeader("X-API-Deprecated"));
        assertNotNull(response.getHeader("X-API-Sunset"));
        assertEquals("v1", response.getHeader("X-API-Version"));
    }

    @Test
    void preHandleWithPastSunsetDateShouldReturn410() throws Exception {
        Method method = TestController.class.getMethod("deprecatedMethodPast");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        assertEquals(410, response.getStatus());
        assertEquals("true", response.getHeader("X-API-Deprecated"));
    }

    @Test
    void preHandleWithMigrationGuideShouldIncludeHeader() throws Exception {
        Method method = TestController.class.getMethod("deprecatedMethodWithGuide");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertEquals("https://docs.example.com/migration", response.getHeader("X-API-Migration-Guide"));
    }

    @Test
    void preHandleWithMethodAnnotationShouldWork() throws Exception {
        Method method = TestController.class.getMethod("methodWithOwnSunset");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertEquals("v2", response.getHeader("X-API-Version"));
    }

    @Test
    void preHandle_expiredSunsetEnforceDisabled_logsWarnAndAllowsRequest() throws Exception {
        Method method = TestController.class.getMethod("deprecatedMethodPastNoEnforce");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result, "Request should be allowed when enforcement is disabled");
        assertEquals("true", response.getHeader("X-API-Deprecated"));
        assertNotEquals(410, response.getStatus());
    }

    @Test
    void preHandle_replacedByNonEmpty_addsReplacedByHeader() throws Exception {
        Method method = TestController.class.getMethod("deprecatedMethodWithReplacedBy");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        interceptor.preHandle(request, response, handlerMethod);

        assertEquals("/api/v2/products", response.getHeader("X-API-Replaced-By"));
    }

    @Test
    void preHandle_sunsetApproachingWithinWarningWindow_addsWarningHeader() throws Exception {
        Method method = TestController.class.getMethod("deprecatedMethodApproaching");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertNotNull(response.getHeader("X-API-Days-Until-Sunset"),
                "Warning header should be set when sunset is within warningDays");
    }

    @Test
    void preHandle_classLevelAnnotation_appliesSunsetHeaders() throws Exception {
        Method method = DeprecatedController.class.getMethod("someMethod");
        HandlerMethod handlerMethod = new HandlerMethod(new DeprecatedController(), method);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertEquals("true", response.getHeader("X-API-Deprecated"));
        assertEquals("v1", response.getHeader("X-API-Version"));
    }

    @Test
    void preHandle_expiredSunsetWithReplacedBy_includesReplacedByInErrorBody() throws Exception {
        Method method = TestController.class.getMethod("deprecatedMethodPastWithReplacedBy");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        assertEquals(410, response.getStatus());
        assertTrue(response.getContentAsString().contains("/api/v2/products"));
    }

    @Test
    void preHandle_writerThrowsOnExpiredEnforced_doesNotPropagateException() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        PrintWriter mockWriter = mock(PrintWriter.class);
        when(mockResponse.getWriter()).thenReturn(mockWriter);
        doThrow(new RuntimeException("write failed")).when(mockWriter).write(anyString());

        Method method = TestController.class.getMethod("deprecatedMethodPast");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        boolean result = assertDoesNotThrow(
                () -> interceptor.preHandle(request, mockResponse, handlerMethod));

        assertFalse(result);
    }

    // ── test fixtures ────────────────────────────────────────────────────────

    public static class TestController {
        public void normalMethod() {}

        @ApiSunset(date = "2099-12-31", version = "v1")
        public void deprecatedMethodFuture() {}

        @ApiSunset(date = "2020-01-01", version = "v1", enforce = true)
        public void deprecatedMethodPast() {}

        @ApiSunset(date = "2020-01-01", version = "v1", enforce = false)
        public void deprecatedMethodPastNoEnforce() {}

        @ApiSunset(date = "2099-12-31", version = "v1", migrationGuide = "https://docs.example.com/migration")
        public void deprecatedMethodWithGuide() {}

        @ApiSunset(date = "2099-06-30", version = "v2")
        public void methodWithOwnSunset() {}

        @ApiSunset(date = "2099-12-31", version = "v1", replacedBy = "/api/v2/products")
        public void deprecatedMethodWithReplacedBy() {}

        @ApiSunset(date = "2020-01-01", version = "v1", enforce = true, replacedBy = "/api/v2/products")
        public void deprecatedMethodPastWithReplacedBy() {}

        // warningDays exceeds the distance to 2099-12-31, so warning always fires
        @ApiSunset(date = "2099-12-31", version = "v1", warningDays = 1000000)
        public void deprecatedMethodApproaching() {}
    }

    @ApiSunset(date = "2099-12-31", version = "v1")
    public static class DeprecatedController {
        public void someMethod() {}
    }
}
