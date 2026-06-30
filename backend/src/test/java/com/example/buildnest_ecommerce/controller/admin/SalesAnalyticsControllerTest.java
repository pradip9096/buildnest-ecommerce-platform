package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.SalesDashboardDTO;
import com.example.buildnest_ecommerce.service.analytics.SalesAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesAnalyticsControllerTest {

    private static SalesAnalyticsService stubService(boolean throwing) {
        return new SalesAnalyticsService() {
            @Override
            public SalesDashboardDTO getDashboard(LocalDate s, LocalDate e) {
                if (throwing) throw new RuntimeException("fail");
                return new SalesDashboardDTO();
            }

            @Override
            public BigDecimal getDailyRevenue(LocalDate date) {
                if (throwing) throw new RuntimeException("fail");
                return BigDecimal.valueOf(100);
            }

            @Override
            public Double getConversionRate(LocalDate s, LocalDate e) {
                if (throwing) throw new RuntimeException("fail");
                return 20.0;
            }

            @Override
            public Double getCartAbandonmentRate(LocalDate s, LocalDate e) {
                if (throwing) throw new RuntimeException("fail");
                return 66.67;
            }

            @Override
            public BigDecimal getCustomerLifetimeValue(Long userId) {
                if (throwing) throw new RuntimeException("fail");
                return BigDecimal.valueOf(500);
            }

            @Override
            public BigDecimal getAverageOrderValue(LocalDate s, LocalDate e) {
                if (throwing) throw new RuntimeException("fail");
                return BigDecimal.valueOf(120);
            }
        };
    }

    @Test
    void happyPath_allEndpointsReturn200() {
        SalesAnalyticsController controller = new SalesAnalyticsController(stubService(false));

        assertEquals(HttpStatus.OK, controller.getDashboard(null, null).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getDashboard(
                LocalDate.now().minusDays(7), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getDailyRevenue(LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getConversionRate(
                LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getCartAbandonmentRate(
                LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAverageOrderValue(
                LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getCustomerLifetimeValue(1L).getStatusCode());
    }

    @Test
    void serviceError_allEndpointsReturn500() {
        SalesAnalyticsController controller = new SalesAnalyticsController(stubService(true));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getDashboard(null, null).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getDailyRevenue(LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getConversionRate(LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getCartAbandonmentRate(LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getAverageOrderValue(LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getCustomerLifetimeValue(1L).getStatusCode());
    }
}
