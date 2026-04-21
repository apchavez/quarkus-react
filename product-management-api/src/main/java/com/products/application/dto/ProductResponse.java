package com.products.application.dto;

public record ProductResponse(
        String id,
        String sku,
        String name,
        String description,
        String category,
        Double price,
        Integer stock,
        Boolean active,
        String userUpdated) {
}