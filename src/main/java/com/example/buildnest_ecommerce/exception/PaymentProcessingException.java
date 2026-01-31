package com.example.buildnest_ecommerce.exception;

/**
 * Exception for payment processing failures.
 */
public class PaymentProcessingException extends BuildNestException {
    private static final long serialVersionUID = 1L;

    public PaymentProcessingException(String message) {
        super("PAYMENT_FAILED", message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super("PAYMENT_FAILED", message, cause);
    }
}
