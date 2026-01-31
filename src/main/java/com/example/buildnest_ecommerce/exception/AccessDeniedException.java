package com.example.buildnest_ecommerce.exception;

public class AccessDeniedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public AccessDeniedException(String message) {
        super(message);
    }
    
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
