package com.example.buildnest_ecommerce.controller.admin;

import com.example.buildnest_ecommerce.model.dto.SalesDashboardDTO;
import com.example.buildnest_ecommerce.service.analytics.SalesAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalesAnalyticsControllerTest {

    @Test
    void returnsSalesAnalyticsData() {
        SalesAnalyticsService service = new SalesAnalyticsService() {
            @Override
            public SalesDashboardDTO getDashboard(LocalDate startDate, LocalDate endDate) {
                return new SalesDashboardDTO();
            }

            @Override
            public Double getDailyRevenue(LocalDate date) {
                return 100.0;
            }

            @Override
            public Double getConversionRate(LocalDate startDate, LocalDate endDate) {
                return 1.0;
            }

            @Override
            public Double getCartAbandonmentRate(LocalDate startDate, LocalDate endDate) {
                return 2.0;
            }

            @Override
            public Double getCustomerLifetimeValue(Long userId) {
                return 4.0;
            }

            @Override
            public Double getAverageOrderValue(LocalDate startDate, LocalDate endDate) {
                return 3.0;
            }
        };

        SalesAnalyticsController controller = new SalesAnalyticsController(service);
        assertEquals(HttpStatus.OK, controller.getDashboard(null, null).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getDailyRevenue(LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.OK,
                controller.getConversionRate(LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.OK,
                controller.getCartAbandonmentRate(LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.OK,
                controller.getAverageOrderValue(LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getCustomerLifetimeValue(1L).getStatusCode());
    }

    @Test
    void getDashboardWithExplicitDates() {
        SalesAnalyticsService service = new SalesAnalyticsService() {
            @Override
            public SalesDashboardDTO getDashboard(LocalDate startDate, LocalDate endDate) {
                return new SalesDashboardDTO();
            }

            @Override
            public Double getDailyRevenue(LocalDate date) {
                return 100.0;
            }

            @Override
            public Double getConversionRate(LocalDate startDate, LocalDate endDate) {
                return 1.0;
            }

            @Override
            public Double getCartAbandonmentRate(LocalDate startDate, LocalDate endDate) {
                return 2.0;
            }

            @Override
            public Double getCustomerLifetimeValue(Long userId) {
                return 4.0;
            }

            @Override
            public Double getAverageOrderValue(LocalDate startDate, LocalDate endDate) {
                return 3.0;
            }
        };

        SalesAnalyticsController controller = new SalesAnalyticsController(service);
        assertEquals(HttpStatus.OK,
                controller.getDashboard(LocalDate.now().minusDays(7), LocalDate.now()).getStatusCode());
    }

    @Test
    void handlesErrors() {
        SalesAnalyticsService service = new SalesAnalyticsService() {
            @Override
            public SalesDashboardDTO getDashboard(LocalDate startDate, LocalDate endDate) {
                return new SalesDashboardDTO();
            }

            @Override
            public Double getDailyRevenue(LocalDate date) {
                throw new RuntimeException("fail");
            }

            @Override
            public Double getConversionRate(LocalDate startDate, LocalDate endDate) {
                return 1.0;
            }

            @Override
            public Double getCartAbandonmentRate(LocalDate startDate, LocalDate endDate) {
                return 2.0;
            }

            @Override
            public Double getCustomerLifetimeValue(Long userId) {
                return 4.0;
            }

            @Override
            public Double getAverageOrderValue(LocalDate startDate, LocalDate endDate) {
                return 3.0;
            }
        };

        SalesAnalyticsController controller = new SalesAnalyticsController(service);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getDailyRevenue(LocalDate.now()).getStatusCode());
    }

    @Test
    void handlesOtherErrors() {
        SalesAnalyticsService service = new SalesAnalyticsService() {
            @Override
            public SalesDashboardDTO getDashboard(LocalDate startDate, LocalDate endDate) {
                throw new RuntimeException("fail");
            }

            @Override
            public Double getDailyRevenue(LocalDate date) {
                return 100.0;
            }

            @Override
            public Double getConversionRate(LocalDate startDate, LocalDate endDate) {
                throw new RuntimeException("fail");
            }

            @Override
            public Double getCartAbandonmentRate(LocalDate startDate, LocalDate endDate) {
                throw new RuntimeException("fail");
            }

            @Override
            public Double getCustomerLifetimeValue(Long userId) {
                throw new RuntimeException("fail");
            }

            @Override
            public Double getAverageOrderValue(LocalDate startDate, LocalDate endDate) {
                throw new RuntimeException("fail");
            }
        };

        SalesAnalyticsController controller = new SalesAnalyticsController(service);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getDashboard(null, null).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getConversionRate(LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getCartAbandonmentRate(LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                controller.getAverageOrderValue(LocalDate.now().minusDays(1), LocalDate.now()).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getCustomerLifetimeValue(1L).getStatusCode());
    }
}
