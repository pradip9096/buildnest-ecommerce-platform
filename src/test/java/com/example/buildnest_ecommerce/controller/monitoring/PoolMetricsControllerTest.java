package com.example.buildnest_ecommerce.controller.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PoolMetricsController Tests")
class PoolMetricsControllerTest {

    @Mock
    private HikariDataSource dataSource;

    @Mock
    private HikariPoolMXBean poolMXBean;

    @InjectMocks
    private PoolMetricsController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(dataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
    }

    @Test
    @DisplayName("Should return pool status metrics")
    void testPoolStatus() {
        // Arrange
        when(poolMXBean.getActiveConnections()).thenReturn(5);
        when(poolMXBean.getTotalConnections()).thenReturn(10);
        when(poolMXBean.getIdleConnections()).thenReturn(5);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        when(dataSource.getMaximumPoolSize()).thenReturn(20);

        // Act
        Map<String, Object> status = controller.poolStatus();

        // Assert
        assertNotNull(status);
        assertEquals(5, status.get("activeConnections"));
        assertEquals(10, status.get("totalConnections"));
        assertEquals(5, status.get("idleConnections"));
        assertEquals(0, status.get("waitingQueue"));
        assertEquals(20, status.get("maxPoolSize"));
        assertTrue(status.containsKey("utilizationPercentage"));
    }

    @Test
    @DisplayName("Should calculate utilization percentage correctly")
    void testUtilizationPercentageCalculation() {
        // Arrange
        when(poolMXBean.getActiveConnections()).thenReturn(8);
        when(poolMXBean.getTotalConnections()).thenReturn(10);
        when(poolMXBean.getIdleConnections()).thenReturn(2);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        when(dataSource.getMaximumPoolSize()).thenReturn(20);

        // Act
        Map<String, Object> status = controller.poolStatus();

        // Assert
        String utilization = (String) status.get("utilizationPercentage");
        assertNotNull(utilization);
        assertTrue(utilization.contains("80.00%"));
    }

    @Test
    @DisplayName("Should handle zero total connections")
    void testZeroTotalConnections() {
        // Arrange
        when(poolMXBean.getActiveConnections()).thenReturn(0);
        when(poolMXBean.getTotalConnections()).thenReturn(0);
        when(poolMXBean.getIdleConnections()).thenReturn(0);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        when(dataSource.getMaximumPoolSize()).thenReturn(20);

        // Act
        Map<String, Object> status = controller.poolStatus();

        // Assert
        assertNotNull(status);
        assertEquals(0, status.get("totalConnections"));
    }

    @Test
    @DisplayName("Should detect high utilization")
    void testHighUtilization() {
        // Arrange - 95% utilization
        when(poolMXBean.getActiveConnections()).thenReturn(19);
        when(poolMXBean.getTotalConnections()).thenReturn(20);
        when(poolMXBean.getIdleConnections()).thenReturn(1);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        when(dataSource.getMaximumPoolSize()).thenReturn(20);

        // Act
        Map<String, Object> status = controller.poolStatus();

        // Assert
        String utilization = (String) status.get("utilizationPercentage");
        assertTrue(utilization.contains("95.00%"));
    }

    @Test
    @DisplayName("Should detect waiting threads")
    void testWaitingThreads() {
        // Arrange
        when(poolMXBean.getActiveConnections()).thenReturn(10);
        when(poolMXBean.getTotalConnections()).thenReturn(10);
        when(poolMXBean.getIdleConnections()).thenReturn(0);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(5);
        when(dataSource.getMaximumPoolSize()).thenReturn(10);

        // Act
        Map<String, Object> status = controller.poolStatus();

        // Assert
        assertEquals(5, status.get("waitingQueue"));
    }

    @Test
    @DisplayName("Should return all required pool metrics")
    void testAllRequiredMetrics() {
        // Arrange
        when(poolMXBean.getActiveConnections()).thenReturn(5);
        when(poolMXBean.getTotalConnections()).thenReturn(10);
        when(poolMXBean.getIdleConnections()).thenReturn(5);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        when(dataSource.getMaximumPoolSize()).thenReturn(20);

        // Act
        Map<String, Object> status = controller.poolStatus();

        // Assert
        assertTrue(status.containsKey("activeConnections"));
        assertTrue(status.containsKey("totalConnections"));
        assertTrue(status.containsKey("idleConnections"));
        assertTrue(status.containsKey("waitingQueue"));
        assertTrue(status.containsKey("maxPoolSize"));
        assertTrue(status.containsKey("utilizationPercentage"));
    }

    @Test
    @DisplayName("Should verify MXBean is called")
    void testMXBeanCalled() {
        // Arrange
        when(poolMXBean.getActiveConnections()).thenReturn(5);
        when(poolMXBean.getTotalConnections()).thenReturn(10);
        when(poolMXBean.getIdleConnections()).thenReturn(5);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);
        when(dataSource.getMaximumPoolSize()).thenReturn(20);
        when(dataSource.getMinimumIdle()).thenReturn(5);

        // Act
        controller.poolStatus();

        // Assert
        verify(poolMXBean).getActiveConnections();
        verify(poolMXBean).getTotalConnections();
        verify(poolMXBean).getIdleConnections();
        verify(poolMXBean).getThreadsAwaitingConnection();
        verify(dataSource, times(2)).getMaximumPoolSize();
        verify(dataSource).getMinimumIdle();
    }
}
