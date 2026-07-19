package com.example.buildnest_ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "roles")
@ToString(exclude = "roles")
public class Permission implements AggregateRoot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    // Lazy back-reference to Role.permissions, unguarded — reached whenever
    // a raw User is serialized (User.roles EAGER -> Role.permissions EAGER
    // -> this), throwing post-transaction (#441).
    @JsonIgnore
    @ManyToMany(mappedBy = "permissions",
            fetch = jakarta.persistence.FetchType.LAZY)
    private Set<Role> roles;
}
