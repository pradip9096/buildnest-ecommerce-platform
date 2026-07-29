package com.example.buildnest_ecommerce.util;

import com.example.buildnest_ecommerce.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * SSRF guard (OWASP A10, issue #111): rejects a caller-supplied URL whose
 * host resolves to a loopback, link-local, site-local, or wildcard
 * address, so a feature that fetches or posts to a user/admin-supplied
 * URL (e.g. webhook delivery) cannot be used to pivot the server into
 * internal-only services or cloud metadata endpoints.
 */
@Component
public class SsrfUrlValidator {

    public void validate(String url) {
        String host = URI.create(url).getHost();
        if (host == null) {
            throw new ValidationException("Target URL must include a host");
        }
        InetAddress address = resolve(host);
        if (address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isAnyLocalAddress()
                || address.isMulticastAddress()) {
            throw new ValidationException(
                    "Target URL must not resolve to a private, loopback, "
                            + "or link-local address");
        }
    }

    InetAddress resolve(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            throw new ValidationException(
                    "Target URL host could not be resolved: " + host);
        }
    }
}
