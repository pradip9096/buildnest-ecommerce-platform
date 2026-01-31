package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Cart;
import com.example.buildnest_ecommerce.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user with eager loading of items and products
     * Prevents N+1 query problems (2.2 Performance Optimization)
     */
    @EntityGraph(attributePaths = { "items", "items.product", "user" })
    Optional<Cart> findByUser(User user);

    void deleteByUser(User user);
}
