package com.example.buildnest_ecommerce;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.event.DomainEventListener;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import com.example.buildnest_ecommerce.service.inventory.InventoryMonitoringService;
import com.example.buildnest_ecommerce.service.notification.NotificationService;
import com.example.buildnest_ecommerce.service.scheduler.InventoryMonitoringScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = CivilEcommerceApplication.class)
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
@SuppressWarnings("removal")
class CivilEcommerceApplicationTests {

	@MockitoBean
	private ElasticsearchAuditLogRepository auditLogRepository;

	@MockitoBean
	private ElasticsearchMetricsRepository metricsRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private DomainEventListener domainEventListener;

	@Autowired
	private InventoryMonitoringService inventoryMonitoringService;

	@Autowired
	private InventoryMonitoringScheduler inventoryMonitoringScheduler;

	@Test
	void contextLoads() {
	}

	/**
	 * Regression guard for #345: these four classes were accidentally gated
	 * behind {@code elasticsearch.enabled}, which is {@code false} in the
	 * "test" profile (application-test.properties) — matching the production
	 * default. If any of them silently regresses back to being ES-gated,
	 * {@code @Autowired} above fails context refresh before this test body
	 * ever runs, so a passing contextLoads() already partially proves this;
	 * this test makes the guarantee explicit and independently assertable.
	 */
	@Test
	void previouslyEsGatedBeansAreRegisteredWhenElasticsearchDisabled() {
		assertNotNull(notificationService, "NotificationService must be a bean regardless of elasticsearch.enabled");
		assertNotNull(domainEventListener, "DomainEventListener must be a bean regardless of elasticsearch.enabled");
		assertNotNull(inventoryMonitoringService,
				"InventoryMonitoringService must be a bean regardless of elasticsearch.enabled");
		assertNotNull(inventoryMonitoringScheduler,
				"InventoryMonitoringScheduler must be a bean regardless of elasticsearch.enabled");
	}

}
