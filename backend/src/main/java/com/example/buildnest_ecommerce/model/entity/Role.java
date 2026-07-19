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
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "users")
@EqualsAndHashCode(exclude = "users")
public class Role implements AggregateRoot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    // Lazy back-reference to User.roles, unguarded — reached whenever a
    // raw User is serialized (User.roles is EAGER), throwing post-
    // transaction since this collection is never itself initialized (#441).
    @JsonIgnore
    @ManyToMany(mappedBy = "roles", fetch = jakarta.persistence.FetchType.LAZY)
    private Set<User> users;

    // jpa-rule-exception: pending decision, not a permanent carve-out like
    // User.roles above — converting to LAZY changes real permission-loading
    // behavior across the security/authorization system and needs its own
    // dedicated change with real testing, not a drive-by fix (#441). See
    // #506 for the actual decision (stay EAGER with a documented reason,
    // matching User.roles, or convert to LAZY with explicit fetch-joins).
    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions;
}
