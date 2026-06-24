package com.example.buildnest_ecommerce.service.checkout;

import java.util.Optional;

public interface CheckoutSessionStore {

    void save(Long userId, CheckoutSession session);

    Optional<CheckoutSession> find(Long userId);

    void delete(Long userId);
}
