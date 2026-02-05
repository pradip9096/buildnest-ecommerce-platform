package com.example.buildnest_ecommerce.util;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransactionLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("TRANSACTION_LOG");

    @PostConstruct
    public void logInitialized() {
        LOGGER.info("Transaction logger initialized");
    }

    public void info(String message, Object... args) {
        LOGGER.info(message, args);
    }

    public void warn(String message, Object... args) {
        LOGGER.warn(message, args);
    }

    public void error(String message, Object... args) {
        LOGGER.error(message, args);
    }
}
