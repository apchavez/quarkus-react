package com.products.application.dto;

import java.time.Instant;

public record ProductResponse(
        String id,
        String sku,
        String name,
        String description,
        String category,
        Double price,
        Integer stock,
        Boolean active,
        String userUpdated,
        Instant created,
        Instant updated) {
}
