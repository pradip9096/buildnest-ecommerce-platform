package com.example.buildnest_ecommerce.event;

import org.springframework.context.ApplicationEvent;

/**
 * Fired when user account is created.
 */
public class UserRegisteredEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final Long userId;
    private final String email;

    public UserRegisteredEvent(Object source, Long userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
