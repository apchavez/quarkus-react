package com.products.application.dto;

import java.util.List;

public record ProductsPagedResponse(
        List<ProductResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {
}