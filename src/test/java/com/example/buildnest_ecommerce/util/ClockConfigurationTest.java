package com.example.buildnest_ecommerce.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

class ClockConfigurationTest {

    @Test
    void returnsSystemClock() {
        ClockConfiguration config = new ClockConfiguration();
        Clock clock = config.clock();
        assertNotNull(clock);
        assertNotNull(clock.instant());
    }
}
