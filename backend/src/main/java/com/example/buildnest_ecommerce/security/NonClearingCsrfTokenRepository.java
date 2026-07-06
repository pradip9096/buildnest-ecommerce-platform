package com.example.buildnest_ecommerce.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * Delegates to the wrapped repository for everything except clearing the token.
 *
 * <p>Works around a confirmed Spring Security defect (GH-12141): {@code CsrfAuthenticationStrategy}
 * clears the XSRF-TOKEN cookie on every authentication event via {@code saveToken(null, ...)}, but
 * fails to regenerate a replacement within the same request/response cycle. Because
 * {@code JwtAuthenticationFilter} re-authenticates on every single stateless request, this fired
 * on every request and wiped the cookie before the browser ever got to use it.
 *
 * <p>The CSRF token is not a credential — it grants no access on its own — so there is no security
 * benefit to auto-clearing it here. It is refreshed deliberately by {@code GET /api/auth/csrf} and
 * by login/refresh responses; this wrapper just stops an unrelated framework quirk from deleting it
 * out from under the browser.
 */
public class NonClearingCsrfTokenRepository implements CsrfTokenRepository {

    private final CsrfTokenRepository delegate;

    public NonClearingCsrfTokenRepository(CsrfTokenRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return delegate.generateToken(request);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            return;
        }
        delegate.saveToken(token, request, response);
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return delegate.loadToken(request);
    }
}
