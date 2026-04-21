package com.products.application.dto;

public record ProductRequest(
                String sku,
                String name,
                String description,
                String category,
                Double price,
                Integer stock,
                Boolean active) {
}