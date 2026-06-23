package com.example.buildnest_ecommerce.interceptor;

import com.example.buildnest_ecommerce.annotation.ApiSunset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

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

    // Test controller classes
    public static class TestController {
        public void normalMethod() {
        }

        @ApiSunset(date = "2099-12-31", version = "v1")
        public void deprecatedMethodFuture() {
        }

        @ApiSunset(date = "2020-01-01", version = "v1", enforce = true)
        public void deprecatedMethodPast() {
        }

        @ApiSunset(date = "2099-12-31", version = "v1", migrationGuide = "https://docs.example.com/migration")
        public void deprecatedMethodWithGuide() {
        }

        @ApiSunset(date = "2099-06-30", version = "v2")
        public void methodWithOwnSunset() {
        }
    }
}
