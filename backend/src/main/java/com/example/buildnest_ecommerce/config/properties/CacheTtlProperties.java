package com.example.buildnest_ecommerce.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache.ttl")
public class CacheTtlProperties {
    private long products;
    private long categories;
    private long users;
    private long orders;
    private long rateLimitStats;
    private long auditLogs;
    private long userPermissions;
    private long inventoryItems;

    public long getProducts() {
        return products;
    }

    public void setProducts(long products) {
        this.products = products;
    }

    public long getCategories() {
        return categories;
    }

    public void setCategories(long categories) {
        this.categories = categories;
    }

    public long getUsers() {
        return users;
    }

    public void setUsers(long users) {
        this.users = users;
    }

    public long getOrders() {
        return orders;
    }

    public void setOrders(long orders) {
        this.orders = orders;
    }

    public long getRateLimitStats() {
        return rateLimitStats;
    }

    public void setRateLimitStats(long rateLimitStats) {
        this.rateLimitStats = rateLimitStats;
    }

    public long getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(long auditLogs) {
        this.auditLogs = auditLogs;
    }

    public long getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(long userPermissions) {
        this.userPermissions = userPermissions;
    }

    public long getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(long inventoryItems) {
        this.inventoryItems = inventoryItems;
    }
}
