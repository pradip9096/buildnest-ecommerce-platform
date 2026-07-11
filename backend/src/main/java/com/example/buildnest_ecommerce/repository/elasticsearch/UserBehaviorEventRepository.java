package com.example.buildnest_ecommerce.repository.elasticsearch;

import com.example.buildnest_ecommerce.model.elasticsearch.UserBehaviorEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for user behaviour analytics events (ANL-02, #65).
 */
@Repository
public interface UserBehaviorEventRepository extends ElasticsearchRepository<UserBehaviorEvent, String> {

    /**
     * Events of a given type within a time range — the basis for all
     * behaviour-metric aggregation (page views per product, funnel counts,
     * cart abandonment).
     */
    List<UserBehaviorEvent> findByEventTypeAndTimestampBetween(String eventType, LocalDateTime start,
            LocalDateTime end);
}
