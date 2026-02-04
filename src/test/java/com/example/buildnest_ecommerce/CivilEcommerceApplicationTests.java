package com.example.buildnest_ecommerce;

import com.example.buildnest_ecommerce.config.TestElasticsearchConfig;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchAuditLogRepository;
import com.example.buildnest_ecommerce.repository.elasticsearch.ElasticsearchMetricsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = CivilEcommerceApplication.class)
@ActiveProfiles("test")
@Import({TestElasticsearchConfig.class, TestSecurityConfig.class})
@SuppressWarnings("removal")
class CivilEcommerceApplicationTests {

	@MockBean
	private ElasticsearchAuditLogRepository auditLogRepository;

	@MockBean
	private ElasticsearchMetricsRepository metricsRepository;

	@Test
	void contextLoads() {
	}

}
