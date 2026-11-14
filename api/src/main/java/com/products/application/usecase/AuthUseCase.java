package com.products.application.usecase;

import com.products.application.dto.LoginRequest;
import com.products.application.dto.LoginResponse;
import com.products.application.port.in.AuthServicePort;
import com.products.exception.InvalidCredentialsException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class AuthUseCase implements AuthServicePort {

    private static final Duration TOKEN_LIFESPAN = Duration.ofHours(1);

    @Inject
    DemoUserStore userStore;

    @Override
    public LoginResponse login(LoginRequest request) {
        Set<String> roles = userStore.authenticate(request.username(), request.password())
                .orElseThrow(InvalidCredentialsException::new);

        String token = Jwt.issuer("product-api")
                .subject(request.username())
                .groups(roles)
                .expiresAt(Instant.now().plus(TOKEN_LIFESPAN))
                .sign();

        return new LoginResponse(token, "Bearer", TOKEN_LIFESPAN.toSeconds(), request.username(), roles);
    }
}
