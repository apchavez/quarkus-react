package com.products.application.usecase;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Hardcoded demo users for this portfolio project — not a real user store.
 * A production system would back this with a persisted, hashed credential store.
 */
@ApplicationScoped
public class DemoUserStore {

    private record DemoUser(String passwordHash, Set<String> roles) {
    }

    private final Map<String, DemoUser> users = Map.of(
            "admin", new DemoUser(BcryptUtil.bcryptHash("admin123"), Set.of("ADMIN", "USER")),
            "user", new DemoUser(BcryptUtil.bcryptHash("user123"), Set.of("USER"))
    );

    public Optional<Set<String>> authenticate(String username, String password) {
        DemoUser user = users.get(username);
        if (user == null || !BcryptUtil.matches(password, user.passwordHash())) {
            return Optional.empty();
        }
        return Optional.of(user.roles());
    }
}
