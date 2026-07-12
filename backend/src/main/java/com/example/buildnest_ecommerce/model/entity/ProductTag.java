package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * A tag that customers can filter products by (PROD-03).
 */
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "product_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "products", "createdAt" })
@ToString(exclude = { "products" })
public class ProductTag implements AggregateRoot {
    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique, human-readable tag name. */
    @Column(nullable = false, unique = true)
    private String name;

    /** Unique, URL-safe identifier derived from {@link #name}. */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Timestamp the tag was created. */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Products currently carrying this tag. */
    @JsonIgnore
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Product> products;
}
