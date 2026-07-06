package com.unicomai.wanwu.service.bff.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BffUserContextResolverTest {

    @Test
    public void resolvesAdminFromEmptyOrBearerToken() {
        BffUserContextResolver.ResolvedUser empty = BffUserContextResolver.resolve(null);
        assertEquals("dev-admin", empty.getUserId());
        assertEquals("default-org", empty.getOrgId());
        assertEquals("admin", empty.getUsername());
        assertTrue(empty.isAdmin());

        BffUserContextResolver.ResolvedUser bearer = BffUserContextResolver.resolve("Bearer dev-token");
        assertEquals("dev-token", bearer.getToken());
        assertEquals("dev-admin", bearer.getUserId());
        assertTrue(bearer.isAdmin());
    }

    @Test
    public void resolvesAppFromBearerOrPlainToken() {
        BffUserContextResolver.ResolvedUser bearer = BffUserContextResolver.resolve("Bearer dev-token-app");
        assertEquals("dev-token-app", bearer.getToken());
        assertEquals("dev-app", bearer.getUserId());
        assertEquals("default-org", bearer.getOrgId());
        assertEquals("app", bearer.getUsername());
        assertFalse(bearer.isAdmin());

        BffUserContextResolver.ResolvedUser plain = BffUserContextResolver.resolve("dev-token-app");
        assertEquals("dev-app", plain.getUserId());
        assertFalse(plain.isAdmin());
    }

    @Test
    public void explicitHeadersOverrideTokenDefaults() {
        BffUserContextResolver.ResolvedUser resolved = BffUserContextResolver.resolve(
                "Bearer dev-token-app", "external-user", "external-org");

        assertEquals("external-user", resolved.getUserId());
        assertEquals("external-org", resolved.getOrgId());
        assertEquals("external-user", resolved.getUsername());
        assertTrue(resolved.isAdmin());
    }
}
