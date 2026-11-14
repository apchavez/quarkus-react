package com.products.application.dto;

import java.util.Set;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        String username,
        Set<String> roles
) {
}
