package com.example.buildnest_ecommerce.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {
    private Throttle login = new Throttle();
    private Throttle passwordReset = new Throttle();
    private Throttle refreshToken = new Throttle();
    private Throttle productSearch = new Throttle();
    private Throttle admin = new Throttle();
    private Throttle user = new Throttle();
    private Throttle api = new Throttle();

    public Throttle getLogin() {
        return login;
    }

    public void setLogin(Throttle login) {
        this.login = login;
    }

    public Throttle getPasswordReset() {
        return passwordReset;
    }

    public void setPasswordReset(Throttle passwordReset) {
        this.passwordReset = passwordReset;
    }

    public Throttle getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(Throttle refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Throttle getProductSearch() {
        return productSearch;
    }

    public void setProductSearch(Throttle productSearch) {
        this.productSearch = productSearch;
    }

    public Throttle getAdmin() {
        return admin;
    }

    public void setAdmin(Throttle admin) {
        this.admin = admin;
    }

    public Throttle getUser() {
        return user;
    }

    public void setUser(Throttle user) {
        this.user = user;
    }

    public Throttle getApi() {
        return api;
    }

    public void setApi(Throttle api) {
        this.api = api;
    }

    public static class Throttle {
        private int requests;
        private int duration;

        public int getRequests() {
            return requests;
        }

        public void setRequests(int requests) {
            this.requests = requests;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
    }
}
