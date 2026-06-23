package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    /**
     * Find inactive users (last login before specified date) for engagement
     * campaigns
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin < :lastLoginBefore")
    List<User> findInactiveUsers(@Param("lastLoginBefore") LocalDateTime lastLoginBefore);

    /**
     * Find users with high order value for VIP programs
     */
    @Query("SELECT u FROM User u WHERE u.id IN " +
            "(SELECT DISTINCT o.user.id FROM Order o " +
            "GROUP BY o.user.id HAVING SUM(o.totalAmount) >= :minValue)")
    List<User> findUsersWithHighOrderValue(@Param("minValue") BigDecimal minValue);
}
