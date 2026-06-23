package com.example.buildnest_ecommerce.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "webhook")
public class WebhookProperties {
    private final Events events = new Events();
    private final Handlers handlers = new Handlers();
    private int maxRetries;
    private int retryDelayMs;

    public Events getEvents() {
        return events;
    }

    public Handlers getHandlers() {
        return handlers;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(int retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public static class Events {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Handlers {
        private boolean payment = true;
        private boolean inventory = true;
        private boolean order = true;
        private boolean alert = true;

        public boolean isPayment() {
            return payment;
        }

        public void setPayment(boolean payment) {
            this.payment = payment;
        }

        public boolean isInventory() {
            return inventory;
        }

        public void setInventory(boolean inventory) {
            this.inventory = inventory;
        }

        public boolean isOrder() {
            return order;
        }

        public void setOrder(boolean order) {
            this.order = order;
        }

        public boolean isAlert() {
            return alert;
        }

        public void setAlert(boolean alert) {
            this.alert = alert;
        }
    }
}
