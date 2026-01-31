package com.example.buildnest_ecommerce.exception;

import com.example.buildnest_ecommerce.model.payload.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private WebRequest webRequest;
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        httpServletRequest = mock(HttpServletRequest.class);
        webRequest = new ServletWebRequest(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException correctly")
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Product not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(
                exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(404, errorResponse.getStatusCode());
        assertEquals("Product not found", errorResponse.getMessage());
        assertEquals("Resource not found", errorResponse.getError());
        assertEquals("/api/test", errorResponse.getPath());
    }

    @Test
    @DisplayName("Should handle AccessDeniedException correctly")
    void testHandleAccessDeniedException() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(
                exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(403, errorResponse.getStatusCode());
        assertEquals("Access denied", errorResponse.getMessage());
        assertEquals("Access denied", errorResponse.getError());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException correctly")
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatusCode());
        assertEquals("Invalid argument", errorResponse.getMessage());
        assertEquals("Invalid argument", errorResponse.getError());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException correctly")
    void testHandleMethodArgumentNotValidException() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("product", "name", "must not be blank");
        FieldError fieldError2 = new FieldError("product", "price", "must be positive");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                exception, null, HttpStatus.BAD_REQUEST, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatusCode());
        assertTrue(errorResponse.getMessage().contains("Validation") || errorResponse.getMessage().contains("name")
                || errorResponse.getMessage().contains("price"));
        assertEquals("Validation failed", errorResponse.getError());
    }

    @Test
    @DisplayName("Should include timestamp in error response")
    void testErrorResponseIncludesTimestamp() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(
                exception, webRequest);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should handle null message in exception")
    void testHandleNullMessage() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException(null);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(
                exception, webRequest);

        // Assert
        assertNotNull(response);
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
    }

    @Test
    @DisplayName("Should extract correct path from web request")
    void testPathExtraction() {
        // Arrange
        when(httpServletRequest.getRequestURI()).thenReturn("/api/products/123");
        ResourceNotFoundException exception = new ResourceNotFoundException("Not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(
                exception, webRequest);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("/api/products/123", errorResponse.getPath());
    }

    @Test
    @DisplayName("Should handle empty validation errors")
    void testHandleEmptyValidationErrors() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                exception, null, HttpStatus.BAD_REQUEST, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should use correct HTTP status codes")
    void testHttpStatusCodes() {
        // Test NOT_FOUND
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> notFoundResponse = exceptionHandler.handleResourceNotFoundException(
                notFoundEx, webRequest);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());

        // Test FORBIDDEN
        AccessDeniedException accessDeniedEx = new AccessDeniedException("Forbidden");
        ResponseEntity<ErrorResponse> forbiddenResponse = exceptionHandler.handleAccessDeniedException(
                accessDeniedEx, webRequest);
        assertEquals(HttpStatus.FORBIDDEN, forbiddenResponse.getStatusCode());

        // Test BAD_REQUEST
        IllegalArgumentException badRequestEx = new IllegalArgumentException("Bad request");
        ResponseEntity<ErrorResponse> badRequestResponse = exceptionHandler.handleIllegalArgumentException(
                badRequestEx, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST, badRequestResponse.getStatusCode());
    }
}
