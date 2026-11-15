package com.products.support;

import io.smallrye.jwt.build.Jwt;

import java.time.Instant;
import java.util.Set;

public class TestTokenUtil {

    public static String adminToken() {
        return Jwt.issuer("product-api")
                .subject("test-admin")
                .groups(Set.of("ADMIN", "USER"))
                .expiresAt(Instant.now().plusSeconds(3600))
                .sign();
    }

    public static String userToken() {
        return Jwt.issuer("product-api")
                .subject("test-user")
                .groups(Set.of("USER"))
                .expiresAt(Instant.now().plusSeconds(3600))
                .sign();
    }
}
